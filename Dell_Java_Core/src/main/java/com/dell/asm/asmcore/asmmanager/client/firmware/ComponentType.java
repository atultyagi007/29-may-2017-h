/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
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
 * Represents a ComponentType as described by the Dell Repository Manager Catalog guidelines. 
 */
@XmlRootElement(name = "ComponentType")
@ApiModel()
public enum ComponentType {
    APPLICATION ("APAC"),
    DRIVER ("DRVR"),
    BIOS ("BIOS"),
    FIRMWARE ("FRMW");
    
    private String value;
    
    private ComponentType (String valueToSet) {
        this.value = valueToSet;
    }
    
    /**
     * The value that's used to set the componentType in the DRM Catalog and in the ASM database. 
     */
    public String getValue() {
        return value;
    }

    public static ComponentType fromValue(String v) {
        return valueOf(v);
    }
}
