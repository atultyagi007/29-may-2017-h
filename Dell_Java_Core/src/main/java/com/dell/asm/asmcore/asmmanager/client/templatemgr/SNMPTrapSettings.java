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

/**
 * SNMP Trap destination settings, part of the templates
 * @author Bapu_Patil
 *
 */
@XmlRootElement(name = "SNMPTrapSettings")
public class SNMPTrapSettings
{
    private String snmpDestination;
    private String snmpCommunityString;
    
    public SNMPTrapSettings() { }
    
    public String getSnmpDestination() {
        return snmpDestination;
    }
    public void setSnmpDestination(String snmpDestination) {
        this.snmpDestination = snmpDestination;
    }
    public String getSnmpCommunityString() {
        return snmpCommunityString;
    }
    public void setSnmpCommunityString(String snmpCommunityString) {
        this.snmpCommunityString = snmpCommunityString;
    }
    
}