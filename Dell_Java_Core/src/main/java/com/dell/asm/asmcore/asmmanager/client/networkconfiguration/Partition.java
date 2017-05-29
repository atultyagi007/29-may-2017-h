/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.networkconfiguration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;

@XmlType(propOrder = { "id", "name", "networks", "networkObjects",
        "minimum", "maximum", "lanMacAddress", "iscsiMacAddress", "iscsiIQN", "wwnn", "wwpn", "portNo", "partitionNo",
        "partitionIndex", "fqdd", "macAddress" })
public class Partition {

    private String id;
    private String name;
    private List<String> networks;       // Network guids sent by guid
    private List<Network> networkObjects; // Network objects corresponding to above guids
    private int minimum;
    private int maximum;
    private String lanMacAddress;
    private String iscsiMacAddress;
    private String iscsiIQN;
    private String wwnn;
    private String wwpn;
    private String macAddress;
    private int portNo;
    private int partitionNo;
    private int partitionIndex;
    private String fqdd;

    @XmlElement(name = "mac_address")
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @XmlElement(name = "port_no")
    public int getPortNo() {
        return portNo;
    }

    public void setPortNo(int portNo) {
        this.portNo = portNo;
    }

    @XmlElement(name = "partition_no")
    public int getPartitionNo() {
        return partitionNo;
    }

    public void setPartitionNo(int partitionNo) {
        this.partitionNo = partitionNo;
    }

    @XmlElement(name = "partition_index")
    public int getPartitionIndex() {
        return partitionIndex;
    }

    public void setPartitionIndex(int partitionIndex) {
        this.partitionIndex = partitionIndex;
    }

    public String getFqdd() {
        return fqdd;
    }

    public void setFqdd(String fqdd) {
        this.fqdd = fqdd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getNetworks() {
        return networks;
    }

    public void setNetworks(List<String> networks) {
        this.networks = networks;
    }

    public List<Network> getNetworkObjects() {
        return networkObjects;
    }

    public void setNetworkObjects(List<Network> networkObjects) {
        this.networkObjects = networkObjects;
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public String getLanMacAddress() {
        return lanMacAddress;
    }

    public void setLanMacAddress(String lanMacAddress) {
        this.lanMacAddress = lanMacAddress;
    }

    public String getIscsiIQN() {
        return iscsiIQN;
    }

    public void setIscsiIQN(String iscsiIQN) {
        this.iscsiIQN = iscsiIQN;
    }

    public String getIscsiMacAddress() {
        return iscsiMacAddress;
    }

    public void setIscsiMacAddress(String iscsiMacAddress) {
        this.iscsiMacAddress = iscsiMacAddress;
    }

    public String getWwnn() {
        return wwnn;
    }

    public void setWwnn(String wwnn) {
        this.wwnn = wwnn;
    }

    public String getWwpn() {
        return wwpn;
    }

    public void setWwpn(String wwpn) {
        this.wwpn = wwpn;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("networks", networks)
                .append("networkObjects", networkObjects)
                .append("minimum", minimum)
                .append("maximum", maximum)
                .append("lanMacAddress", lanMacAddress)
                .append("iscsiIQN", iscsiIQN)
                .append("iscsiMacAddress", iscsiMacAddress)
                .append("wwnn", wwnn)
                .append("wwpn", wwpn)
                .toString();
    }

    public Partition() {
        super();
        networks = new ArrayList<>();
    }

    public void clearNetworks() {
        this.networkObjects.clear();
        this.networks.clear();
    }
}

