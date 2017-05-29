package com.dell.asm.asmcore.asmmanager.exception;

public class AsmManagerInvalidCredentialException extends RuntimeException {
	    private static final long serialVersionUID = -4832782278200513781L;

	    public AsmManagerInvalidCredentialException() {
	        super();
	    }

	    public AsmManagerInvalidCredentialException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	        super(message, cause, enableSuppression, writableStackTrace);
	    }

	    public AsmManagerInvalidCredentialException(String message, Throwable cause) {
	        super(message, cause);
	    }

	    public AsmManagerInvalidCredentialException(String message) {
	        super(message);
	    }

	    public AsmManagerInvalidCredentialException(Throwable cause) {
	        super(cause);
	    }
}
