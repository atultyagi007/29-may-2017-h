/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.firmware;

import javax.xml.bind.annotation.XmlEnumValue;

public enum RepositoryStatus {
    @XmlEnumValue("pending") PENDING("pending"),
    @XmlEnumValue("copying") COPYING("copying"),
    @XmlEnumValue("error") ERROR("error"),
    @XmlEnumValue("available") AVAILABLE("available");

    private final String value;

    RepositoryStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
