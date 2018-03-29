package org.cytoscape.sana.sana_app.internal.rest.parameters;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.sana.sana_app.internal.CyActivator;

public class AlignmentData {
	public String url;
	public String cx;
	public CyNetwork netA, netB, resultNetwork;
	public AlignmentParameters params;
	
	public AlignmentData(CyNetwork netA, CyNetwork netB, AlignmentParameters params){
		this.netA = netA;
		this.netB = netB;
		this.params = params;
		this.url = CyActivator.getSanaURLProperty();
	}
	
}
