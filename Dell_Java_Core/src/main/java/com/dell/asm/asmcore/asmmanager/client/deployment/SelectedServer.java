/**************************************************************************
 *   Copyright (c) 2012 - 2015 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import com.dell.asm.asmcore.asmmanager.client.hardware.RAIDConfiguration;
import java.util.List;

import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SelectedServer")
public class SelectedServer implements Comparable<SelectedServer> {
    private String refId;
    private String serviceTag;
    private String componentId;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    private String ipAddress;

    public List<SelectedNIC> getNics() {
        return nics;
    }

    public void setNics(List<SelectedNIC> nics) {
        this.nics = nics;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    private List<SelectedNIC> nics;
    private boolean exactMatch;

    public boolean isMatchUnordered() {
        return matchUnordered;
    }

    public void setMatchUnordered(boolean matchUnordered) {
        this.matchUnordered = matchUnordered;
    }

    private boolean matchUnordered;

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public RAIDConfiguration getRaidConfiguration() {
        return raidConfiguration;
    }

    public void setRaidConfiguration(RAIDConfiguration raidConfiguration) {
        this.raidConfiguration = raidConfiguration;
    }

    private RAIDConfiguration raidConfiguration;

    public SelectedServer(){
        raidConfiguration = new RAIDConfiguration();
    }

    public SelectedServer(String refId, String serviceTag, String ipAddress, String compId, RAIDConfiguration configuration) {
        this.refId = refId;
        this.serviceTag = serviceTag;
        this.componentId = compId;
        this.ipAddress = ipAddress;
        raidConfiguration = configuration;
    }

    @Override
    public int compareTo(SelectedServer o) {
        return serviceTag.compareTo(o.serviceTag);
    }
}
