package org.cytoscape.sana.sana_app.internal.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.util.CxioUtil;
import org.cytoscape.ci.model.CIError;
import org.cytoscape.ci.model.CIResponse;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.sana.sana_app.internal.*;
import org.cytoscape.sana.sana_app.internal.rest.parameters.AlignmentData;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PerformAlignmentTask extends AbstractTask implements ObservableTask {

	private HashMap<Long, Long> edgeList;
	private StringEntity cx;
	private final AlignmentData data;
	private HashMap<Long, CyNode> suidNodeMap = new HashMap<Long, CyNode>();

	public class AlignmentException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public AlignmentException(String message) {
			super(message);
		}
	}

	public PerformAlignmentTask(AlignmentData data) throws AlignmentException {
		this.data = data;

		if (data.netA == null) {
			throw new AlignmentException("Cannot get network A with suid " + data.params.networkAsuid);
		}
		if (data.netB == null) {
			throw new AlignmentException("Cannot get network B with suid " + data.params.networkBsuid);
		}

	}

	private HttpPost createPost() throws Exception {
		URIBuilder builder = new URIBuilder(data.url);
		Map<String, String> map = data.params.sana_args.getQueryParameters();
		for (String s : map.keySet()) {
			builder.addParameter(s, map.get(s));
		}
		System.out.println(builder.toString());
		HttpPost post = new HttpPost(builder.toString());
		post.setEntity(cx);
		post.setHeader("Content-type", "application/json");
		return post;
	}

	public String getName() {
		String name1 = data.netA.getDefaultNetworkTable().getRow(data.netA.getSUID()).get(CyNetwork.NAME, String.class);
		String name2 = data.netB.getDefaultNetworkTable().getRow(data.netB.getSUID()).get(CyNetwork.NAME, String.class);

		return String.format("%s - %s SANA alignment", name1, name2);
	}

	@Override
	public void run(TaskMonitor mon) throws Exception {

		mon.setTitle("Performing Sana alignment...");
		mon.setProgress(0);

		try {
			cx = new StringEntity(data.cx);
		} catch (UnsupportedEncodingException | IllegalArgumentException e) {
			throw new AlignmentException("Unable to create cx payload");
		}

		String output = "";
		HttpPost post = createPost();

		HttpClient client = HttpClients.createDefault();
		HttpResponse response = client.execute(post);
		HttpEntity entity = response.getEntity();
		output = entity != null ? EntityUtils.toString(entity) : null;

		if (cancelled)
			return;

		// load SUID>SUID map into edgeMap global variable
		Map<String, List<AspectElement>> aspectMap = decodeResponse(output);
		loadEdgeList(aspectMap.get(EdgesElement.ASPECT_NAME));

		// join source networks and set initial columns
		data.resultNetwork = constructCombinedNetwork(getName());

		// set alignment_SUID column
		loadEdgeListInfo(data.resultNetwork, aspectMap.get(EdgesElement.ASPECT_NAME));

		findCommonEdges(data.resultNetwork);
		SanaApp.rm.cynetmgr.addNetwork(data.resultNetwork);
		SanaApp.rm.cyappmgr.setCurrentNetwork(data.resultNetwork);

		mon.setProgress(1);
		mon.setStatusMessage("Shutdown instance & writing report...");

	}

	// parse edges into SUID>SUID map
	private void loadEdgeList(List<AspectElement> edges) {
		edgeList = new HashMap<Long, Long>();
		for (AspectElement asp : edges) {
			EdgesElement edge = (EdgesElement) asp;

			long srcId = edge.getSource();
			long tgtId = edge.getTarget();
			edgeList.put(srcId, tgtId);
		}
	}

	// populate alignment columns with alignment map info
	private void loadEdgeListInfo(CyNetwork net, List<AspectElement> edges) {
		int edgeNum = 0;
		for (AspectElement asp : edges) {
			EdgesElement edge = (EdgesElement) asp;

			long srcId = edge.getSource();
			long tgtId = edge.getTarget();

			CyNode source = suidNodeMap.get(srcId);
			CyNode target = suidNodeMap.get(tgtId);

			CyRow row = net.getRow(source);
			row.set(ColumnNames.SANA_ALIGNMENT_ID, edgeNum);
			row.set(ColumnNames.COMMONALITY, 3);
			row.set(ColumnNames.NODE_PARTNER_SUID, target.getSUID());

			row = net.getRow(target);
			row.set(ColumnNames.SANA_ALIGNMENT_ID, edgeNum);
			row.set(ColumnNames.COMMONALITY, 3);
			row.set(ColumnNames.NODE_PARTNER_SUID, source.getSUID());

			edgeNum++;
		}
	}

	public Map<String, List<AspectElement>> decodeResponse(String response) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		CIResponse<?> res = objectMapper.readValue(response, CIResponse.class);

		if (res.errors.size() != 0) {
			String errStrings = "";
			for (CIError err : res.errors) {
				errStrings += err.type + ":\n  " + err.message;
			}
			throw new IOException(errStrings);

		}

		final CxReader reader = CxReader.createInstance(objectMapper.writeValueAsString(res.data),
				CxioUtil.getAllAvailableAspectFragmentReaders());
		return CxReader.parseAsMap(reader);
	}

	private void findCommonEdges(CyNetwork network) {

		for (CyEdge edge : network.getEdgeList()) {
			if (network.getRow(edge).get(ColumnNames.SOURCE_NETWORK_ID, Integer.class) == 1) {
				CyEdge partnerEdge = getPartnerEdge(network, edge);

				if (partnerEdge != null) {
					network.getRow(edge).set(ColumnNames.COMMONALITY, 3);
					network.getRow(partnerEdge).set(ColumnNames.COMMONALITY, 3);
				}
			}
		}
	}

	// get partner node within a merged alignment network with the
	// NODE_PARTNER_SUID attribute
	private CyNode getPartnerNode(CyNetwork network, CyNode node) {
		CyRow row = network.getRow(node);
		Long suid = row.get(ColumnNames.NODE_PARTNER_SUID, Long.class);
		if (suid != null) {
			return network.getNode(suid);
		}
		return null;
	}

	// get partner edge within merged alignment network by finding partner nodes
	// for the source and target and returning the connecting edge if it exists
	private CyEdge getPartnerEdge(CyNetwork network, CyEdge edge) {
		CyNode src = edge.getSource();
		CyNode tgt = edge.getTarget();

		CyNode srcPair = getPartnerNode(network, src);
		if (srcPair == null) {
			return null;
		}
		CyNode tgtPair = getPartnerNode(network, tgt);
		if (tgtPair == null) {
			return null;
		}
		List<CyEdge> edges = network.getConnectingEdgeList(srcPair, tgtPair, Type.ANY);
		if (edges.isEmpty())
			return null;
		return edges.iterator().next();
	}

	private void createColumns(CyNetwork net) {
		CyTable netTable = net.getDefaultNetworkTable();
		netTable.createListColumn(ColumnNames.SOURCE_NETWORKS_LIST, Long.class, true);
		netTable.createColumn(ColumnNames.ALIGNMENT_ALGORITHM, String.class, true);
		netTable.getRow(net.getSUID()).set(ColumnNames.ALIGNMENT_ALGORITHM, "SANA");

		CyTable nodeTable = net.getDefaultNodeTable();
		nodeTable.createColumn(ColumnNames.SANA_ALIGNMENT_ID, Integer.class, true, null);
		nodeTable.createColumn(ColumnNames.NODE_PARTNER_SUID, Long.class, true, null);
		nodeTable.createColumn(ColumnNames.SOURCE_NETWORK_ID, Integer.class, true, null);
		nodeTable.createColumn(ColumnNames.COMMONALITY, Integer.class, true, null);

		CyTable edgeTable = net.getDefaultEdgeTable();
		edgeTable.createColumn(ColumnNames.SOURCE_NETWORK_ID, Integer.class, true, null);
		edgeTable.createColumn(ColumnNames.COMMONALITY, Integer.class, true, null);
	}

	// Create a new collection with 3 subnetworks. Copies of both src networks
	// for reference, and a merged network to highlight the differences
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CyNetwork constructCombinedNetwork(String combinedName) {

		CyNetwork net = SanaApp.rm.cynetfac.createNetwork();
		net.getRow(net).set(CyNetwork.NAME, combinedName);
		createColumns(net);

		List<Long> srcSUIDList = new ArrayList<Long>();

		int networkID = 1;

		CyRootNetwork root = SanaApp.rm.cyrootmgr.getRootNetwork(net);

		CyNetwork sources[] = new CyNetwork[] { data.netA, data.netB };
		for (CyNetwork src : sources) {
			HashMap<CyNode, CyNode> nodeMap = new HashMap<CyNode, CyNode>();
			List<CyEdge> netEdges = new ArrayList<CyEdge>();

			Map<String, Class> nodeCols = getColumnsToCopy(src.getDefaultNodeTable(), net.getDefaultNodeTable());
			Map<String, Class> edgeCols = getColumnsToCopy(src.getDefaultEdgeTable(), net.getDefaultEdgeTable());

			for (CyNode srcnode : src.getNodeList()) {
				CyNode newnode = net.addNode();

				nodeMap.put(srcnode, newnode);
				CyRow newrow = net.getRow(newnode);
				newrow.set(ColumnNames.SOURCE_NETWORK_ID, networkID);
				suidNodeMap.put(srcnode.getSUID(), newnode);
				newrow.set(ColumnNames.COMMONALITY, networkID);
				CyRow oldrow = src.getRow(srcnode);

				for (Map.Entry<String, Class> e : nodeCols.entrySet()) {
					newrow.set(e.getKey(), oldrow.get(e.getKey(), e.getValue()));
				}
				newrow.set(CyNetwork.SELECTED, false);
			}

			for (CyEdge edge : src.getEdgeList()) {
				CyEdge newedge = net.addEdge(nodeMap.get(edge.getSource()), nodeMap.get(edge.getTarget()),
						edge.isDirected());

				CyRow newrow = net.getRow(newedge);
				newrow.set(ColumnNames.SOURCE_NETWORK_ID, networkID);
				newrow.set(ColumnNames.COMMONALITY, networkID);

				CyRow oldrow = src.getRow(edge);
				for (Map.Entry<String, Class> e : edgeCols.entrySet())
					newrow.set(e.getKey(), oldrow.get(e.getKey(), e.getValue()));
				netEdges.add(newedge);
			}

			// Create clone network in collection
			String srcName = src.getRow(src).get(CyNetwork.NAME, String.class);
			CyNetwork network = root.addSubNetwork(nodeMap.values(), netEdges);
			SanaApp.rm.cynetmgr.addNetwork(network);
			cloneNodeLocations(src, network, nodeMap);

			network.getDefaultNetworkTable().getRow(network.getSUID()).set(CyNetwork.NAME, srcName + " - copy");

			srcSUIDList.add(network.getSUID());
			networkID++;
		}

		net.getRow(net).set(ColumnNames.SOURCE_NETWORKS_LIST, srcSUIDList);

		return net;
	}

	// Map node XY locations using a map of (SRC, TGT) node mappings
	private void cloneNodeLocations(CyNetwork src, CyNetwork network, HashMap<CyNode, CyNode> nodeMap) {
		CyNetworkView srcView, tgtView;
		if (!SanaApp.rm.cyviewmgr.viewExists(src)) {
			return;
		}
		srcView = SanaApp.rm.cyviewmgr.getNetworkViews(src).iterator().next();
		if (!SanaApp.rm.cyviewmgr.viewExists(network)) {
			tgtView = SanaApp.rm.cyviewfac.createNetworkView(network);
			SanaApp.rm.cyviewmgr.addNetworkView(tgtView);
		} else {
			tgtView = SanaApp.rm.cyviewmgr.getNetworkViews(network).iterator().next();
		}

		for (Entry<CyNode, CyNode> entry : nodeMap.entrySet()) {
			View<CyNode> nodeView = tgtView.getNodeView(entry.getValue());
			View<CyNode> oldview = srcView.getNodeView(entry.getKey());
			double x = oldview.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double y = oldview.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
		}
		tgtView.fitContent();
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, Class> getColumnsToCopy(CyTable src, CyTable dst) {
		Map<String, Class> colsToCopy = new HashMap<String, Class>();
		for (CyColumn srccol : src.getColumns()) {
			CyColumn dstcol = dst.getColumn(srccol.getName());
			if (dstcol != null && !(dstcol.getType().equals(srccol.getType()))) {
				dst.deleteColumn(dstcol.getName());
				dstcol = null;
			}
			// FIXME: handle list columns?
			if (srccol.getListElementType() != null) {
				// SanaApp.log("skipping list column: " + srccol.getName());
				continue;
			}

			VirtualColumnInfo srcvirt = srccol.getVirtualColumnInfo();

			final boolean isvirtcol = srcvirt != null && srcvirt.isVirtual();

			if (dstcol == null && !srccol.isPrimaryKey()) {
				if (!isvirtcol)
					dst.createColumn(srccol.getName(), srccol.getType(), srccol.isImmutable(),
							srccol.getDefaultValue());
				else
					dst.addVirtualColumn(srccol.getName(), srcvirt.getSourceColumn(), srcvirt.getSourceTable(),
							srcvirt.getTargetJoinKey(), srccol.isImmutable());
			}

			if (!isvirtcol)
				colsToCopy.put(srccol.getName(), srccol.getType());
		}
		return colsToCopy;
	}

	@Override
	public void cancel() {
		super.cancel();
	}

	public String getJson() {
		return "{\"alignmentNetwork\": " + data.resultNetwork.getSUID() + "}";
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			return (R) getJson();
		} else if (type.equals(JSONResult.class)) {
			return (R) getJson();
		}
		return null;
	}

	public CyNetwork getNetwork() {
		return data.resultNetwork;
	}

}
