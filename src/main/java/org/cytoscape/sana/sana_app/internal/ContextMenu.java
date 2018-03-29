package org.cytoscape.sana.sana_app.internal;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNetworkViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

public class ContextMenu extends Unloadable {
	private ArrayList<NetworkViewEntry> entries = new ArrayList<NetworkViewEntry>();

	public void addNetworkViewEntry(String submenu, String label, ContextMenuAction action) {
		entries.add(new NetworkViewEntry(label, action, submenu));
	}

	@Override
	public void unload() {
		for (NetworkViewEntry e : entries)
			e.unregisterSelf();

		entries = null;
	}

	private class NetworkViewEntry {
		private CyNetworkViewContextMenuFactory netctx = new CyNetworkViewContextMenuFactory() {
			@Override
			public CyMenuItem createMenuItem(CyNetworkView cyview) {
				return NetworkViewEntry.this.createMenuItem(cyview);
			}
		};
		private CyNodeViewContextMenuFactory nodectx = new CyNodeViewContextMenuFactory() {
			@Override
			public CyMenuItem createMenuItem(CyNetworkView cyview, View<CyNode> cyNodeView) {
				return NetworkViewEntry.this.createMenuItem(cyview);
			}
		};
		private CyEdgeViewContextMenuFactory edgectx = new CyEdgeViewContextMenuFactory() {
			@Override
			public CyMenuItem createMenuItem(CyNetworkView cyview, View<CyEdge> cyEdgeView) {
				return NetworkViewEntry.this.createMenuItem(cyview);
			}
		};

		private ContextMenuAction action;
		private String label;

		public NetworkViewEntry(String label, ContextMenuAction action, String where) {
			this.label = label;
			this.action = action;
			registerSelf(where);
			SanaApp.log("Context menu entry created: " + label);
		}

		private CyMenuItem createMenuItem(final CyNetworkView cyview) {
			JMenuItem menuItem = new JMenuItem(label);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					action.onClick(cyview);
				}
			});
			return new CyMenuItem(menuItem, 0);
		}

		private void registerSelf(String where) {
			Properties p = new Properties();
			p.put("preferredMenu", "Apps." + (where != null ? where : "GEDEVO"));
			SanaApp.rm.cyreg.registerAllServices(netctx, p);
			SanaApp.rm.cyreg.registerAllServices(nodectx, p);
			SanaApp.rm.cyreg.registerAllServices(edgectx, p);
		}

		public void unregisterSelf() {
			SanaApp.rm.cyreg.unregisterAllServices(netctx);
			SanaApp.rm.cyreg.unregisterAllServices(nodectx);
			SanaApp.rm.cyreg.unregisterAllServices(edgectx);
		}
	}

}
