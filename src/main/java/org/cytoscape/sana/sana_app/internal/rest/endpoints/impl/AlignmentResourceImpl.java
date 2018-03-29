package org.cytoscape.sana.sana_app.internal.rest.endpoints.impl;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cytoscape.ci.CIErrorFactory;
import org.cytoscape.ci.CIResponseFactory;
import org.cytoscape.ci.CIWrapping;
import org.cytoscape.ci.model.CIError;
import org.cytoscape.ci.model.CIResponse;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.sana.sana_app.internal.AlignmentServiceException;
import org.cytoscape.sana.sana_app.internal.ColumnNames;
import org.cytoscape.sana.sana_app.internal.SanaApp;
import org.cytoscape.sana.sana_app.internal.rest.AlignmentResource;
import org.cytoscape.sana.sana_app.internal.rest.parameters.AlignmentParameters;
import org.cytoscape.sana.sana_app.internal.rest.parameters.SanaParameters;
import org.cytoscape.sana.sana_app.internal.rest.parameters.VisualizeParameters;
import org.cytoscape.sana.sana_app.internal.task.MergedNetworkLayoutTask;
import org.cytoscape.sana.sana_app.internal.task.PerformAlignmentTaskFactory;
import org.cytoscape.sana.sana_app.internal.util.SanaAlignmentUtil;
import org.cytoscape.sana.sana_app.internal.util.SanaAlignmentUtil.InvalidParametersException;
import org.cytoscape.work.TaskIterator;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignmentResourceImpl implements AlignmentResource {
	private final ServiceTracker ciResponseFactoryTracker, ciErrorFactoryTracker;
	private static final Logger logger = LoggerFactory.getLogger(AlignmentResource.class);

	public static final String INVALID_PARAMETERS_CODE = "1";
	public static final String TASK_EXECUTION_ERROR_CODE = "2";
	Response response = null;

	private final static String resourceErrorRoot = "urn:cytoscape:ci:sana-app:v1";

	public AlignmentResourceImpl(ServiceTracker ciResponseFactoryTracker, ServiceTracker ciErrorFactoryTracker) {
		this.ciResponseFactoryTracker = ciResponseFactoryTracker;
		this.ciErrorFactoryTracker = ciErrorFactoryTracker;
	}

	private CIError buildCIError(int status, String resourcePath, String code, String message, Exception e) {
		CIErrorFactory ciErrorFactory = (CIErrorFactory) ciErrorFactoryTracker.getService();
		return ciErrorFactory.getCIError(status, resourceErrorRoot + ":" + resourcePath + ":" + code, message);
	}

	private Response buildErrorResponse(Status server_code, String error_code, Exception e) {
		return Response.status(server_code).type(MediaType.APPLICATION_JSON)
				.entity(buildCIErrorResponse(server_code.getStatusCode(), "aMatReader", error_code, e.getMessage(), e))
				.build();
	}

	CIResponse<Object> buildCIErrorResponse(int status, String resourcePath, String code, String message, Exception e) {
		CIResponseFactory ciResponseFactory = (CIResponseFactory) ciResponseFactoryTracker.getService();
		CIResponse<Object> response = ciResponseFactory.getCIResponse(new Object());
		CIError error = buildCIError(status, resourcePath, code, message, e);

		if (e != null) {
			logger.error(message, e);
			if (e instanceof AlignmentServiceException) {
				response.errors.addAll(((AlignmentServiceException) e).getCIErrors());
			}
		} else {
			logger.error(message);
		}

		response.errors.add(error);
		return response;
	}

	@Override
	@CIWrapping
	public Response runAlignment(AlignmentParameters params) {
		CyNetwork netA = SanaApp.rm.cynetmgr.getNetwork(params.networkAsuid);
		CyNetwork netB = SanaApp.rm.cynetmgr.getNetwork(params.networkBsuid);
//		if (params.sana_args == null){
//			params.sana_args = new SanaParameters(1, 1, 0, false);
//		}
		System.out.println(params.sana_args);

		for (CyNetwork net : new CyNetwork[] { netA, netB }) {
			try {
				SanaAlignmentUtil.validateNetwork(net, params.sana_args.nodesHaveTypes);
			} catch (InvalidParametersException e) {
				return buildErrorResponse(Response.Status.BAD_REQUEST, INVALID_PARAMETERS_CODE, e);
			}
		}
		if (netA == null || netB == null) {
			return buildErrorResponse(Response.Status.BAD_REQUEST, INVALID_PARAMETERS_CODE,
					new Exception("Invalid SUIDs (" + params.networkAsuid + ", " + params.networkBsuid + ")"));
		}

		PerformAlignmentTaskFactory tf = new PerformAlignmentTaskFactory(netA, netB, params);

		String nameA = netA.getDefaultNetworkTable().getRow(netA.getSUID()).get(CyNetwork.NAME, String.class);
		String nameB = netB.getDefaultNetworkTable().getRow(netB.getSUID()).get(CyNetwork.NAME, String.class);

		SanaApp.rm.taskmgr.execute(String.format("Aligning %s and %s", nameA, nameB), tf.createTaskIterator(), null);

		// wait for sana to finish, to return the resulting network SUID
		while (tf.isRunning()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(tf.getResult()).build();
	}

	@Override
	@CIWrapping
	public Response runVisualization(Long suid, VisualizeParameters params) {
		CyNetwork network = SanaApp.rm.cynetmgr.getNetwork(suid);
		if (network == null) {
			return buildErrorResponse(Response.Status.BAD_REQUEST, INVALID_PARAMETERS_CODE,
					new Exception("No network with suid " + suid));
		}
		CyTable networkTable = network.getDefaultNetworkTable();
		if (networkTable.getColumn(ColumnNames.ALIGNMENT_ALGORITHM) == null) {
			return buildErrorResponse(Response.Status.BAD_REQUEST, INVALID_PARAMETERS_CODE,
					new Exception("Network with suid " + suid + " is not an alignment result."));
		}
		CyRow row = network.getRow(network);
		List<Long> suids = row.getList(ColumnNames.SOURCE_NETWORKS_LIST, Long.class);
		CyNetwork sourceNetwork = SanaApp.rm.cynetmgr.getNetwork(suids.get(0));

		MergedNetworkLayoutTask task = new MergedNetworkLayoutTask(network, sourceNetwork, params.overlap);
		String name = networkTable.getRow(network.getSUID()).get(CyNetwork.NAME, String.class);
		TaskIterator ti = new TaskIterator(task);
		SanaApp.rm.taskmgr.execute(String.format("Visualizing %s", name), ti, null);
		return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).build();
	}

}
