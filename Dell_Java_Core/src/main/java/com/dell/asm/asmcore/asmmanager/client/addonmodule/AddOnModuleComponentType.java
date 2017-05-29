/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.addonmodule;

import javax.xml.bind.annotation.XmlEnumValue;

public enum AddOnModuleComponentType {

    @XmlEnumValue("type")
    TYPE("type"),

    @XmlEnumValue("class")
    CLASS("class");

    private String _label;

    private AddOnModuleComponentType(String label) {
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
