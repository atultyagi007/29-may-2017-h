/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ServerNetworkObjects")
public class ServerNetworkObjects {

    enum PhysicalType {
        BLADE, RACK, SLED
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    /**
     * RACK, BLADE, SLED
     * @return
     */
    @XmlElement(name = "physical_type")
    public PhysicalType getPhysicalType() {
        return physicalType;
    }

    public void setPhysicalType(PhysicalType physicalType) {
        this.physicalType = physicalType;
    }

    @XmlElement(name = "serial_number")
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @XmlElement(name = "razor_policy_name")
    public String getRazorPolicyName() {
        return razorPolicyName;
    }

    public void setRazorPolicyName(String razorPolicyName) {
        this.razorPolicyName = razorPolicyName;
    }

    private String name;
    private String server;
    private PhysicalType physicalType;
    private String serialNumber;
    private String razorPolicyName;

    @XmlElement(name = "network_config")
    public NetworkConfiguration getNetworkConfig() {
        return networkConfig;
    }

    public void setNetworkConfig(NetworkConfiguration networkConfig) {
        this.networkConfig = networkConfig;
    }

    private NetworkConfiguration networkConfig;

    @XmlElement(name = "related_switches")
    public List<String> getRelatedSwitches() {
        return relatedSwitches;
    }

    public void setRelatedSwitches(List<String> relatedSwitches) {
        this.relatedSwitches = relatedSwitches;
    }

    private List<String> relatedSwitches;

    @XmlElement(name = "connected_switches")
    public List<PortConnection> getPortConnections() {
        return portConnections;
    }

    public void setPortConnections(List<PortConnection> portConnections) {
        this.portConnections = portConnections;
    }

    private List<PortConnection> portConnections;

    @XmlElement(name = "fc_interfaces")
    public List<FCInterface> getFcInterfaces() {
        return fcInterfaces;
    }

    public void setFcInterfaces(List<FCInterface> fcInterfaces) {
        this.fcInterfaces = fcInterfaces;
    }

    private List<FCInterface> fcInterfaces;

    @XmlElement(name = "fcoe_interfaces")
    public List<FCInterface> getFcoeInterfaces() {
        return fcoeInterfaces;
    }

    public void setFcoeInterfaces(List<FCInterface> fcoeInterfaces) {
        this.fcoeInterfaces = fcoeInterfaces;
    }

    private List<FCInterface> fcoeInterfaces;

    public ServerNetworkObjects(){}
}
