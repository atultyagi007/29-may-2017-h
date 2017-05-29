/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.exception;

public class AsmManagerDAOException extends RuntimeException {
    private static final long serialVersionUID = -4832782278200513781L;
    private REASON_CODE reasonCode;

    public enum REASON_CODE {
        RECORD_NOT_FOUND,
        DUPLICATE_RECORD,
        DUPLICATE_REFID,
        DUPLICATE_SERVICETAG,
        DUPLICATE_JOBID
    }

    public AsmManagerDAOException() {
        super();
    }

    public AsmManagerDAOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AsmManagerDAOException(REASON_CODE reasonCode, String message, Throwable cause) {
        super(message, cause);
        this.reasonCode = reasonCode;
    }

    public AsmManagerDAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsmManagerDAOException(REASON_CODE reasonCode, String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public AsmManagerDAOException(String message) {
        super(message);
    }

    public AsmManagerDAOException(REASON_CODE reasonCode, Throwable cause) {
        super(cause);
        this.reasonCode = reasonCode;
    }

    public AsmManagerDAOException(Throwable cause) {
        super(cause);
    }

    public REASON_CODE getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(REASON_CODE reasonCode) {
        this.reasonCode = reasonCode;
    }
}

