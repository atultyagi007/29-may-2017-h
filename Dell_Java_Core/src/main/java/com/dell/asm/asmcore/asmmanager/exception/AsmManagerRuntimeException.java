/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.exception;

public class AsmManagerRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 2878623939990092900L;

    public AsmManagerRuntimeException() {
    }

    public AsmManagerRuntimeException(Throwable cause) {
        super(cause);
    }

    public AsmManagerRuntimeException(String message) {
        super(message);
    }

    public AsmManagerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsmManagerRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
