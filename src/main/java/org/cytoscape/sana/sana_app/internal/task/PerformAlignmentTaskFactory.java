package org.cytoscape.sana.sana_app.internal.task;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.sana.sana_app.internal.rest.parameters.AlignmentData;
import org.cytoscape.sana.sana_app.internal.rest.parameters.AlignmentParameters;
import org.cytoscape.sana.sana_app.internal.rest.response.AlignmentResult;
import org.cytoscape.sana.sana_app.internal.task.PerformAlignmentTask.AlignmentException;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class PerformAlignmentTaskFactory extends AbstractTaskFactory {
	private final AlignmentData data;
	private PerformAlignmentTask alignmentTask;

	public PerformAlignmentTaskFactory(CyNetwork netA, CyNetwork netB, AlignmentParameters params) {
		super();
		data = new AlignmentData(netA, netB, params);
	}

	@Override
	public TaskIterator createTaskIterator() {
		ConstructCxList cxTask = new ConstructCxList(data);
		TaskIterator ti = new TaskIterator(cxTask);
		try {
			alignmentTask = new PerformAlignmentTask(data);
			ti.append(alignmentTask);
		} catch (AlignmentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ti;
	}

	public boolean isRunning() {
		return data.resultNetwork == null;
	}

	public AlignmentResult getResult() {
		return new AlignmentResult(alignmentTask.getNetwork().getSUID());
	}

}
