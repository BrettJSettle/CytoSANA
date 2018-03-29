package org.cytoscape.sana.sana_app.internal.task;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.sana.sana_app.internal.ColumnNames;
import org.cytoscape.sana.sana_app.internal.SanaApp;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class MergedNetworkLayoutTask extends AbstractTask {
	private CyNetwork network;
	private CyNetworkView sourceNetworkView, resultView;
	private boolean overlap;

	public MergedNetworkLayoutTask(CyNetwork network, CyNetwork sourceNetwork, boolean overlap) {
		this.network = network;
		this.overlap = overlap;

		for (CyNetworkView view : SanaApp.rm.cyviewmgr.getNetworkViews(sourceNetwork)) {
			sourceNetworkView = view;
			break;
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		Collection<CyNetworkView> views = SanaApp.rm.cyviewmgr.getNetworkViews(network);
		if (views.isEmpty()) {
			resultView = SanaApp.rm.cyviewfac.createNetworkView(network);
			SanaApp.rm.cyviewmgr.addNetworkView(resultView);
		} else {
			resultView = views.iterator().next();
		}
		System.out.println("ResultView: " + resultView);
		
		createSideBySideView();

		InputStream f = getClass().getResourceAsStream("/sana_style.xml");

		Set<VisualStyle> vsSet = SanaApp.rm.vizmapLoader.loadStyles(f);
		for (VisualStyle s : vsSet) {
			SanaApp.rm.vmm.addVisualStyle(s);
			SanaApp.rm.vmm.setVisualStyle(s, resultView);

		}
		resultView.updateView();

		SanaApp.rm.cyeventmgr.flushPayloadEvents();
		SanaApp.rm.cyviewmgr.addNetworkView(resultView);
		resultView.fitContent();
		SanaApp.rm.cyeventmgr.flushPayloadEvents();
	}

	public void createSideBySideView() {
		HashMap<Integer, Point2D.Double> location_map = new HashMap<Integer, Point2D.Double>();
		double xmin = 0, xmax = 0;
		Double yVal = null;
		for (CyRow row : network.getDefaultNodeTable().getMatchingRows(ColumnNames.SOURCE_NETWORK_ID, 1)) {
			CyNode node = network.getNode(row.get(CyNode.SUID, Long.class));
			View<CyNode> nodeView = resultView.getNodeView(node);
			Integer alignmentId = row.get(ColumnNames.SANA_ALIGNMENT_ID, Integer.class);
			if (sourceNetworkView != null) {
				View<CyNode> oldview = sourceNetworkView.getNodeView(node);
				double x = oldview.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = oldview.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				xmin = Math.min(x, xmin);
				xmax = Math.max(x, xmax);
				yVal = yVal == null ? y : Math.max(yVal, y);
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
				if (alignmentId != null && node != null) {
					location_map.put(alignmentId, new Point2D.Double(x, y));
				}
			}
		}
		if (yVal == null) {
			yVal = 0.0;
		}
		double xdiff = overlap ? 0 : xmax - xmin;
		Set<View<CyNode>> unmapped = new HashSet<View<CyNode>>();
		for (CyRow row : network.getDefaultNodeTable().getMatchingRows(ColumnNames.SOURCE_NETWORK_ID, 2)) {
			CyNode node = network.getNode(row.get(CyNode.SUID, Long.class));
			Integer alignmentId = row.get(ColumnNames.SANA_ALIGNMENT_ID, Integer.class);
			View<CyNode> nodeView = resultView.getNodeView(node);
			if (alignmentId != null && location_map.containsKey(alignmentId)) {
				Point2D.Double loc = location_map.get(alignmentId);
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, loc.x + xdiff);
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, loc.y);
			} else {
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xmax + xdiff);
				yVal += 20;
				nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yVal);
				unmapped.add(nodeView);
			}
		}

	}

	public CyNetworkView getResultView() {
		return resultView;
	}

}
