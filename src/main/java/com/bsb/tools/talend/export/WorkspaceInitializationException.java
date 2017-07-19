package com.bsb.tools.talend.export;

/**
 * Exception thrown when a workspace failed to be initialized.
 *
 * @author Sebastien Gerard
 */
public class WorkspaceInitializationException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WorkspaceInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
