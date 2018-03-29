package org.cytoscape.sana.sana_app.internal;

import org.cytoscape.app.swing.*;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.sana.sana_app.internal.util.SanaAlignmentUtil;

import java.awt.event.ActionEvent;

public final class SanaApp extends AbstractCySwingApp implements SetCurrentNetworkListener {
	public static final String VERSION_TEXT = "Sana v0.0.1";

	public CyNetworkViewWriterFactory writerFactory;
	public static ResourceManager rm;
	public ContextMenu contextmenu;

	private boolean shuttingDown = false;

	public SanaMainPanel mainPanel;

	private AbstractCyAction installEverythingAction, removeEverythingAction;

	public SanaApp(CySwingAppAdapter adapter) {
		super(adapter);
		rm = new ResourceManager(adapter);
		installLoader();
	}

	private void installLoader() {
		installEverythingAction = new AbstractCyAction("Load Sana", rm.cyappmgr, "", rm.cyviewmgr) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				initApp();
			}
		};
		installEverythingAction.setPreferredMenu("Apps");
		addMenuAction(installEverythingAction);

	}

	private void clearLoader() {
		if (rm.cyapp != null && installEverythingAction != null) {
			rm.cyapp.removeAction(installEverythingAction);
			installEverythingAction = null;
		}

	}

	private void installUnloader() {
		removeEverythingAction = new AbstractCyAction("Unload Sana", rm.cyappmgr, "", rm.cyviewmgr) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (rm.cyapp != null && removeEverythingAction != null) {
					rm.cyapp.removeAction(removeEverythingAction);
					removeEverythingAction = null;
				}
				clearLoader();
				Unloadable.unloadAll();
				installLoader();

			}
		};
		removeEverythingAction.setPreferredMenu("Apps");
		addMenuAction(removeEverythingAction);
	}

	public void initApp() {
		log("Starting Sana plugin...");

		install();
		refresh();

		log("Ready.");

		clearLoader();
		installUnloader();
	}

	public static void log(String s) {
		System.out.println(s);
	}

	public void addMenuAction(AbstractCyAction m) {
		rm.cyapp.addAction(m);
	}

	public void addViewContextMenuEntry(String submenu, String label, ContextMenuAction action) {
		contextmenu.addNetworkViewEntry(submenu, label, action);
	}

	private void install() {

		mainPanel = new SanaMainPanel();
		contextmenu = new ContextMenu();

	}

	public void teardown() {
		shuttingDown = true;
		clearLoader();
		if (removeEverythingAction != null)
			rm.cyapp.removeAction(removeEverythingAction);
		Unloadable.unloadAll();

		rm.teardown();

		mainPanel = null;
		// sessmgr = null;
		contextmenu = null;

	}

	public void refresh() {
		if (shuttingDown)
			return;

		mainPanel.refresh();
	}

	public void repaint() {
		if (shuttingDown)
			return;

		rm.swingapp.getJFrame().repaint();
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent arg0) {
		CyNetwork net = arg0.getNetwork();
		if (mainPanel != null){
			mainPanel.getUI().setVisualizeEnabled(SanaAlignmentUtil.isAlignmentNetwork(net));
		}
	}

}
