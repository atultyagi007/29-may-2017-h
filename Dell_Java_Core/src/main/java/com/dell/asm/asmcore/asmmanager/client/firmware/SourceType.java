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
 * Indicates where the FirmwareDeviceInventory was derived from (the inventory from the device, or from a catalog).  
 * Due to updates that may be required on the Software front, the system must maintain inventory for items that do
 * not yet exist on the device.  The source type helps indicate from where the inventory was obtained.
 */
@XmlRootElement(name = "SourceType")
@ApiModel()
public enum SourceType {

    Catalog ("Catalog"),
    Device ("Device");
    
    private String value;
    
    private SourceType (String valueToSet) {
        this.value = valueToSet;
    }
    
    /**
     * The value that's used to set the source value in the ASM database. 
     */
    public String getValue() {
        return value;
    }

    public static SourceType fromValue(String v) {
        if (v == null) return null;
        return valueOf(v);
    }
}
