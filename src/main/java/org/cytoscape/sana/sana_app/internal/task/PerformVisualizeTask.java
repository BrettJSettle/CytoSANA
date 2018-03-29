package org.cytoscape.sana.sana_app.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.sana.sana_app.internal.SanaApp;
import org.cytoscape.sana.sana_app.internal.rest.parameters.VisualizeParameters;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class PerformVisualizeTask extends AbstractTask {
	private CyNetwork sourceNetwork;
	private VisualizeParameters params;
	private CyNetwork network;

	public PerformVisualizeTask(final CyNetwork network, final VisualizeParameters params,
			final CyNetwork sourceNetwork) {
		super();
		this.network = network;
		this.sourceNetwork = sourceNetwork;
		this.params = params;
	}

	@Override
	public void run(final TaskMonitor mon) throws Exception {

		MergedNetworkLayoutTask ptask = new MergedNetworkLayoutTask(network, sourceNetwork, params.overlap);
		SanaApp.rm.taskmgr.execute("Visualize", new TaskIterator(ptask), null);
	}

	@Override
	public void cancel() {
		super.cancel();
	}

}
