package org.cytoscape.sana.sana_app.internal;

import java.util.Collections;
import java.util.List;
import org.cytoscape.ci.model.CIError;

public class AlignmentServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1989414902459208086L;
	public final List<CIError> ciErrors;

	public AlignmentServiceException(String message, List<CIError> ciErrors) {
		super(message);
		this.ciErrors = Collections.unmodifiableList(ciErrors);
	}

	public List<CIError> getCIErrors() {
		return ciErrors;
	}
}