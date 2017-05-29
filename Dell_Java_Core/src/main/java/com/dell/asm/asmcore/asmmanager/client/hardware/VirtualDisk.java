/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.hardware;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.VirtualDiskConfiguration;
import com.dell.pg.asm.server.client.device.PhysicalDisk;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = {"raidLevel", "physicalDisks", "controller", "configuration", "mediaType" })
public class VirtualDisk {

    private List<String> physicalDisks;

    public List<String> getPhysicalDisks() {
        if (physicalDisks==null)
            physicalDisks = new ArrayList<>();
        return physicalDisks;
    }

    public void setPhysicalDisks(List<String> physicalDisks) {
        this.physicalDisks = physicalDisks;
    }

    public VirtualDiskConfiguration.UIRaidLevel getRaidLevel() {
        return raidLevel;
    }

    public void setRaidLevel(VirtualDiskConfiguration.UIRaidLevel raidLevel) {
        this.raidLevel = raidLevel;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    private VirtualDiskConfiguration.UIRaidLevel raidLevel;
    private String controller;

    public VirtualDiskConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(VirtualDiskConfiguration configuration) {
        this.configuration = configuration;
    }

    private VirtualDiskConfiguration configuration;

    public PhysicalDisk.PhysicalMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(PhysicalDisk.PhysicalMediaType mediaType) {
        this.mediaType = mediaType;
    }

    private PhysicalDisk.PhysicalMediaType mediaType;

    public VirtualDisk(){
    }

}
