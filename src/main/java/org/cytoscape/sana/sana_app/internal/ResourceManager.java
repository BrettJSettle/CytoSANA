package org.cytoscape.sana.sana_app.internal;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.CloneNetworkTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;

public class ResourceManager {
	public CySwingApplication cyapp;
	public CyNetworkViewManager cyviewmgr;
	public CyNetworkViewFactory cyviewfac;
	public CyApplicationManager cyappmgr;
	public CyNetworkFactory cynetfac;
	public CyServiceRegistrar cyreg;
	public CyEventHelper cyeventmgr;
	public CyNetworkManager cynetmgr;
	public DialogTaskManager cytaskmgr;
	public VisualMappingManager vmm;
	public CyRootNetworkManager cyrootmgr;
	public UndoSupport undo;
	public CyLayoutAlgorithmManager layoutManager;
	public CySwingApplication swingapp;
	public LoadVizmapFileTaskFactory vizmapLoader;
	public CloneNetworkTaskFactory cloneFactory;
	
	public SanaTaskManager taskmgr;

	public ResourceManager(final CySwingAppAdapter cyadapter) {
		taskmgr = new SanaTaskManager();
		cyapp = cyadapter.getCySwingApplication();
		cynetmgr = cyadapter.getCyNetworkManager();
		cyviewmgr = cyadapter.getCyNetworkViewManager();
		cyviewfac = cyadapter.getCyNetworkViewFactory();
		cynetfac = cyadapter.getCyNetworkFactory();
		cyappmgr = cyadapter.getCyApplicationManager();
		cyreg = cyadapter.getCyServiceRegistrar();
		cyeventmgr = cyadapter.getCyEventHelper();
		cytaskmgr = cyadapter.getDialogTaskManager();
		cyrootmgr = cyadapter.getCyRootNetworkManager();
		vmm = cyadapter.getVisualMappingManager();
		undo = cyadapter.getUndoSupport();
		layoutManager = cyadapter.getCyLayoutAlgorithmManager();
		swingapp = cyadapter.getCySwingApplication();
		vizmapLoader =cyadapter.get_LoadVizmapFileTaskFactory();
		cloneFactory = cyadapter.get_CloneNetworkTaskFactory();
	}
	
	public void teardown(){
		cyapp = null;
		cynetmgr = null;
		cyviewmgr = null;
		cyviewfac = null;
		cyappmgr = null;
		cyreg = null;
		cyeventmgr = null;
		cytaskmgr = null;
		cynetfac = null;
		cyrootmgr = null;
		swingapp = null;
		vizmapLoader = null;
		vmm = null;
		cloneFactory = null;
	}
}
