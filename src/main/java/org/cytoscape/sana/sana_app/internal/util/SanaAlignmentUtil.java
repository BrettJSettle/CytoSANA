package org.cytoscape.sana.sana_app.internal.util;

import org.cytoscape.model.*;
import org.cytoscape.sana.sana_app.internal.ColumnNames;
import org.cytoscape.sana.sana_app.internal.SanaApp;
import org.cytoscape.view.model.CyNetworkView;

import java.util.*;

public final class SanaAlignmentUtil {
	private SanaAlignmentUtil() {
	}

	public static class InvalidParametersException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public InvalidParametersException(String message) {
			super(message);
		}
	}

	public static boolean isAlignmentNetwork(CyNetwork net) {
		return net != null && net.getDefaultNetworkTable().getColumn(ColumnNames.ALIGNMENT_ALGORITHM) != null;
	}

	public static void generateMappingEdges(CyNetwork net) {
		if (isAlignmentNetwork(net)) {
			CyColumn alignment_id_column = net.getDefaultNodeTable().getColumn(ColumnNames.SANA_ALIGNMENT_ID);
			List<Integer> alignment_ids = alignment_id_column.getValues(Integer.class);
			Set<Integer> alignment_set = new HashSet<Integer>(alignment_ids);

			int count = 0;
			for (Integer i : alignment_set) {
				if (i == null)
					continue;
				if (mapPair(net, i)) {
					count++;
				}
			}

			System.out.println("Added " + count + " of " + alignment_set.size() + " alignments");
		}

		Iterator<CyNetworkView> viewIter = SanaApp.rm.cyviewmgr.getNetworkViews(net).iterator();
		if (viewIter.hasNext())
			viewIter.next().updateView();
	}

	public static boolean mapPair(CyNetwork network, int alignment_id) {
		Collection<CyRow> rows = network.getDefaultNodeTable().getMatchingRows(ColumnNames.SANA_ALIGNMENT_ID,
				alignment_id);
		CyNode nodeA = null, nodeB = null;
		for (CyRow row : rows) {
			Integer id = row.get(ColumnNames.SOURCE_NETWORK_ID, Integer.class);
			Long suid = row.get(CyNetwork.SUID, Long.class);
			CyNode node = network.getNode(suid);
			if (id == 1)
				nodeA = node;
			else if (id == 2)
				nodeB = node;
		}
		if (nodeA != null && nodeB != null) {
			CyEdge e = network.addEdge(nodeA, nodeB, false);
			CyRow erow = network.getRow(e);
			erow.set(ColumnNames.SOURCE_NETWORK_ID, -1);
			String nameA = network.getRow(nodeA).get(CyNetwork.NAME, String.class);
			String nameB = network.getRow(nodeB).get(CyNetwork.NAME, String.class);
			erow.set(CyNetwork.NAME, nameA + " aligns to " + nameB);
			erow.set(CyEdge.INTERACTION, "aligns to (SANA)");
			return true;
		}
		return false;
	}

	public static void removeMappingEdges(CyNetwork net) {
		final List<CyEdge> rm = new ArrayList<CyEdge>();

		for (CyRow e : net.getDefaultEdgeTable().getMatchingRows(ColumnNames.SOURCE_NETWORK_ID, -1)) {
			long suid = e.get(CyNetwork.SUID, Long.class);
			rm.add(net.getEdge(suid));
		}

		net.removeEdges(rm);

		Iterator<CyNetworkView> viewIter = SanaApp.rm.cyviewmgr.getNetworkViews(net).iterator();
		if (viewIter.hasNext())
			viewIter.next().updateView();
	}

	public static boolean isAlignmentNetworkSelected() {
		if (SanaApp.rm == null)
			return false;
		CyNetwork net = SanaApp.rm.cyappmgr.getCurrentNetwork();
		return isAlignmentNetwork(net);
	}

	public static void validateNetwork(CyNetwork net, Boolean nodesHaveTypes) throws InvalidParametersException {
		if (net == null){
			throw new InvalidParametersException(
					"Network is null.");
		}
		String name = net.getRow(net).get(CyNetwork.NAME, String.class);
		if (nodesHaveTypes != null && nodesHaveTypes) {
			if (!networkIsBipartite(net)) {
				throw new InvalidParametersException(
						"Network must be bipartite if the \"Nodes Have Types\" box is checked." + name
								+ " is not bipartite.");
			}
		}
		if (hasSelfLoops(net)) {
			throw new InvalidParametersException(
					"Network " + name + " contains self loops. SANA will not run with self loops.");
		}

		if (hasDuplicateEdges(net)) {
			throw new InvalidParametersException(
					"Network " + name + " contains duplicate edges. SANA will not run with duplicate edges.");
		}
	}

	private static boolean hasSelfLoops(CyNetwork net) {
		for (CyEdge e : net.getEdgeList()) {
			if (e.getSource() == e.getTarget())
				return true;
		}
		return false;
	}

	private static boolean hasDuplicateEdges(CyNetwork net) {
		for (CyEdge edge : net.getEdgeList()) {
			List<CyEdge> edges = net.getConnectingEdgeList(edge.getSource(), edge.getTarget(), CyEdge.Type.ANY);
			if (edges.size() > 1)
				return true;
		}
		return false;
	}

	private static boolean networkIsBipartite(CyNetwork net) {
		HashMap<CyNode, CyNode> nodeMap = new HashMap<CyNode, CyNode>();
		for (CyEdge edge : net.getEdgeList()) {
			if (nodeMap.containsKey(edge.getTarget()) || nodeMap.containsValue(edge.getSource())) {
				return false;
			}
			nodeMap.put(edge.getSource(), edge.getTarget());
		}

		return true;
	}

}
