/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmdeployer.client;

import javax.xml.bind.annotation.XmlEnumValue;

public enum AsmDeviceStatusType {
    @XmlEnumValue("requested")REQUESTED,
    @XmlEnumValue("in_progress")IN_PROGRESS,
    @XmlEnumValue("success")SUCCESS,
    @XmlEnumValue("failed")FAILED,
    @XmlEnumValue("unknown")UNKNOWN;

    public boolean isTerminalStatus() {
        return this == SUCCESS || this == FAILED;
    }
}
