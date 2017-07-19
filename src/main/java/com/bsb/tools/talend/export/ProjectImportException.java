package com.bsb.tools.talend.export;

/**
 * Exception thrown when a project failed to be imported.
 *
 * @author Sebastien Gerard
 */
public class ProjectImportException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProjectImportException(String message) {
        super(message);
    }

    public ProjectImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
