/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.client.templatemgr;

public enum TemplateTypes {
	
	Chassism1000e("Chassism1000e"), ChassisVRTX("ChassisVRTX"), RACKSERVER("RACKSERVER");

    
    private final String value;

    private TemplateTypes(String newValue) {
        value = newValue;
    }

    public String value() {
        return value;
    }

    public static TemplateTypes fromValue(String newValue) {
        for (TemplateTypes candidate : TemplateTypes.values()) {
            if (candidate.value.equals(newValue)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(newValue);
    }

}
