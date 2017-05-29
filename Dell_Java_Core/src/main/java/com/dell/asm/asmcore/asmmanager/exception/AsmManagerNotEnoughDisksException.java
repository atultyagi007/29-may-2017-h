package com.dell.asm.asmcore.asmmanager.exception;

public class AsmManagerNotEnoughDisksException extends RuntimeException {
    private static final long serialVersionUID = 115291752498925926L;

    public AsmManagerNotEnoughDisksException() {
        super();
    }

    public AsmManagerNotEnoughDisksException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AsmManagerNotEnoughDisksException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsmManagerNotEnoughDisksException(String message) {
        super(message);
    }

    public AsmManagerNotEnoughDisksException(Throwable cause) {
        super(cause);
    }
}
