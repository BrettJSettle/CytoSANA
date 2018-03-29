package org.cytoscape.sana.sana_app.internal.rest.parameters;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "SANA Parameters", description = "Send two networks and some parameters to an alignment service")

public class SanaParameters {
	@ApiModelProperty(value = "Time limit in minutes, does not include preprocessing", example = "1", required = true)
	public Integer time;

	@ApiModelProperty(value = "Alpha weight for sana algorithm", example = "1", required = false)
	public Float alpha;

	@ApiModelProperty(value = "Beta weight for sana algorithm", example = "0", required = false)
	public Float beta;

	@ApiModelProperty(value = "Network is a bipartite graph with two distinct node types", example = "false", required = false)
	public Boolean nodesHaveTypes;

	public SanaParameters(final int timeLimit, final float alpha, final float beta, final boolean nodesHaveTypes) {
		this.time = timeLimit;
		this.alpha = alpha;
		this.beta = beta;
		this.nodesHaveTypes = nodesHaveTypes;
	}

	@ApiModelProperty(hidden = true)
	public Map<String, String> getQueryParameters() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (time != null)
			map.put("t", String.valueOf(time));
		if (alpha != null)
			map.put("alpha", String.valueOf(alpha));
		if (beta != null)
			map.put("beta", String.valueOf(beta));
		if (nodesHaveTypes != null)
			map.put("nodes-have-types", String.valueOf(nodesHaveTypes));

		return map;
	}

}
