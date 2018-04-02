package org.cytoscape.sana.sana_app.internal.task;

import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.sana.sana_app.internal.*;
import org.cytoscape.sana.sana_app.internal.rest.parameters.AlignmentData;
import org.cytoscape.sana.sana_app.internal.util.SanaUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.io.IOException;

public class ConstructCxList extends AbstractTask {
	private SanaJSON json;
	private AlignmentData data;

	public ConstructCxList(AlignmentData data) {
		super();
		this.data = data;
		if (data.netA == null || data.netB == null) {
			SanaUtil.msgbox("Must provide 2 networks in alignment data");
			throw new IllegalArgumentException("need 2 nets");
		}
		CyNetworkViewWriterFactory writerFactory = CyActivator.tfManager.getCxWriterFactory();

		json = new SanaJSON(writerFactory);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Constructing combined network...");
		String nets[] = new String[2];
		CyNetwork sources[] = new CyNetwork[] { data.netA, data.netB };
		for (int i = 0; i < 2; i++) {
			try {
				nets[i] = json.encode(taskMonitor, sources[i]);
			} catch (IOException e) {
				return;
			}
		}
		data.cx = "[" + nets[0] + ", " + nets[1] + "]";
	}

}
