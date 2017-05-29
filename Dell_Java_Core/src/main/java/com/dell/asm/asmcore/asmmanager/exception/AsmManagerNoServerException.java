package com.dell.asm.asmcore.asmmanager.exception;

public class AsmManagerNoServerException extends RuntimeException {
    private static final long serialVersionUID = 1342291752478925926L;

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    private String poolId;

    public AsmManagerNoServerException() {
        super();
    }

    public AsmManagerNoServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AsmManagerNoServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsmManagerNoServerException(String message, String poolId) {
        super(message);
        this.poolId = poolId;
    }

    public AsmManagerNoServerException(Throwable cause) {
        super(cause);
    }
}
