package org.cytoscape.sana.sana_app.internal.rest.parameters;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Parameters for passing to AMatReaderTask
 * 
 * @author brettjsettle
 *
 */

@ApiModel(value = "Alignment Parameters", description = "Send two networks and some parameters to an alignment service")
public class AlignmentParameters {
	
	@ApiModelProperty(value = "SUID of the base network for alignment", required = true)
	public Long networkAsuid;

	@ApiModelProperty(value = "SUID of the network to be aligned", required = true)
	public Long networkBsuid;

	@ApiModelProperty(value = "Parameters to be passed to SANA", required = true)
	public SanaParameters sana_args;
	
	public AlignmentParameters(final long network1SUID, final long network2SUID,
			final SanaParameters params) {
		this.networkAsuid = network1SUID;
		this.networkBsuid = network2SUID;
		this.sana_args = params;
	}

}
