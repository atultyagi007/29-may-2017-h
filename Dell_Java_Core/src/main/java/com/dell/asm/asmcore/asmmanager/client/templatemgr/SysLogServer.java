/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlElement;


@XmlRootElement(name = "SysLogServer")
public class SysLogServer {

    @XmlElement(name = "logServerAddress")
    private String logServerAddress;

    public SysLogServer(){
        
    }
    /**
     * Gets the value of the logServerAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogServerAddress() {
        return logServerAddress;
    }

    /**
     * Sets the value of the logServerAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setlogServerAddress(String value) {
        this.logServerAddress = value;
    }

}
