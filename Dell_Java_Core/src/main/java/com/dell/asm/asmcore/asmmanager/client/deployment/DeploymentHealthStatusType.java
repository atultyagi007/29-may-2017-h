/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import javax.xml.bind.annotation.XmlEnumValue;

public enum DeploymentHealthStatusType {

    @XmlEnumValue("green")
    GREEN("green"),
    
    @XmlEnumValue("yellow")
    YELLOW("yellow"),
    
    @XmlEnumValue("red")
    RED("red");


    private String label;

    private DeploymentHealthStatusType(String newLabel) {
        label = newLabel;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return name();
    }

    @Override
    public String toString() {
        return label;
    }
}
