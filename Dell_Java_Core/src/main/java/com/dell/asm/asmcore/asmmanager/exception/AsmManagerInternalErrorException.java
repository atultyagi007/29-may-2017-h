/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.exception;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.i18n2.exception.AsmRuntimeException;

public class AsmManagerInternalErrorException extends AsmRuntimeException {

    private static final long serialVersionUID = 3493776831770296545L;

    public AsmManagerInternalErrorException(String operation, String module) {
        super("Internal Error Occurred during: " + operation + " in Component " + module,
                AsmManagerMessages.internalError());
    }

    public AsmManagerInternalErrorException(String operation, String module, Exception e) {
        super("Internal Error Occurred during: " + operation + " in Component " + module
                + " with Exception " + e, AsmManagerMessages.internalError(), e);
    }
}
