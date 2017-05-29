/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.firmware;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;

/**
 * Indicates whether a Bundle is of type Software or Firmware.
 */
@XmlRootElement(name = "ComponentType")
@ApiModel()
public enum BundleType {

    SOFTWARE ("BTSW"),
    FIRMWARE ("BTFW");
    
    private String value;
    
    private BundleType (String valueToSet) {
        this.value = valueToSet;
    }
    
    /**
     * The value that's used to set the bundleType in the ASM database. 
     */
    public String getValue() {
        return value;
    }

    public static BundleType fromValue(String v) {
        return valueOf(v);
    }
    
}
