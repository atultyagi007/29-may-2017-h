/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fc_interface")
public class FCInterface {

    public String getFqdd() {
        return fqdd;
    }

    public void setFqdd(String fqdd) {
        this.fqdd = fqdd;
    }

    private String fqdd;

    public String getWwpn() {
        return wwpn;
    }

    public void setWwpn(String wwpn) {
        this.wwpn = wwpn;
    }

    @XmlElement(name = "connected_switch")
    public String getConnectedSwitch() {
        return connectedSwitch;
    }

    public void setConnectedSwitch(String connectedSwitch) {
        this.connectedSwitch = connectedSwitch;
    }

    @XmlElement(name = "active_zoneset")
    public String getActiveZoneset() {
        return activeZoneset;
    }

    public void setActiveZoneset(String activeZoneset) {
        this.activeZoneset = activeZoneset;
    }

    @XmlElement(name = "connected_zones")
    public List<String> getConnectedZones() {
        return connectedZones;
    }

    public void setConnectedZones(List<String> connectedZones) {
        this.connectedZones = connectedZones;
    }

    private String wwpn;
    private String connectedSwitch;
    private String activeZoneset;
    private List<String> connectedZones;

}