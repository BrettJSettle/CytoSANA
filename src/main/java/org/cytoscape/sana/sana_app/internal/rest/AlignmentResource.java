package org.cytoscape.sana.sana_app.internal.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.cytoscape.ci.model.CIResponse;
import org.cytoscape.sana.sana_app.internal.rest.parameters.AlignmentParameters;
import org.cytoscape.sana.sana_app.internal.rest.parameters.VisualizeParameters;
import org.cytoscape.sana.sana_app.internal.rest.response.AlignmentResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Apps: SANA")
@Path("/sana/v1")
public interface AlignmentResource {
	
	@ApiModel(
			value="Alignment Response",
			parent=CIResponse.class)
    public static class CIAlignmentResponse extends CIResponse<AlignmentResult>{
    }
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/align/sana")
	@ApiOperation(value = "Run the SANA alignment algorithm on two Cytoscape networks",
	notes = "Returns SUID of the merged network created from the alignment",
			response = CIAlignmentResponse.class)
	@ApiResponses(
			value = {
						@ApiResponse(code = 404, message = "Network does not exist", response = CIAlignmentResponse.class)
					}
			)
	public Response runAlignment(@ApiParam(value = "Alignment Parameters", required = true) AlignmentParameters params
			);

	@ApiOperation(value = "Visualize the alignment as a single network", notes = "Create a new network view for the alignment result.", response = CIAlignmentResponse.class)
	@PUT
	@Path("/visualize/{networkSUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runVisualization(
			@ApiParam(value = "Network SUID") @PathParam("networkSUID") final Long networkSUID,
			@ApiParam(value = "Overlap networks", required = false) final VisualizeParameters params);
}
