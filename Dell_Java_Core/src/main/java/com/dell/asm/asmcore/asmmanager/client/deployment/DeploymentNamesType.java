/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import javax.xml.bind.annotation.XmlEnumValue;

public enum DeploymentNamesType {

    @XmlEnumValue("os_host_name")
    OS_HOST_NAME("os_host_name"),

    @XmlEnumValue("vm_name")
    VM_NAME("vm_name"),

    @XmlEnumValue("volume_name")
    STORAGE_VOLUME_NAME("volume_name");

    private String _label;

    private DeploymentNamesType(String label) {
        _label = label;
    }

    public String getLabel() {
        return _label;
    }

    public String getValue() {
        return name();
    }

    @Override
    public String toString() {
        return _label;
    }
}
