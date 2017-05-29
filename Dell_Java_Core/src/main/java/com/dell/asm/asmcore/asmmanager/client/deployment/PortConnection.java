/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Interface;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "connected_switch")
public class PortConnection {

    @XmlElement(name = "local_device")
    public String getLocalDevice() {
        return localDevice;
    }

    public void setLocalDevice(String localDevice) {
        this.localDevice = localDevice;
    }

    @XmlElement(name = "remote_device")
    public String getRemoteDevice() {
        return remoteDevice;
    }

    public void setRemoteDevice(String remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    @XmlElement(name = "local_ports")
    public List<String> getLocalPorts() {
        return localPorts;
    }

    public void setLocalPorts(List<String> localPorts) {
        this.localPorts = localPorts;
    }

    @XmlElement(name = "remote_ports")
    public List<String> getRemotePorts() {
        return remotePorts;
    }

    public void setRemotePorts(List<String> remotePorts) {
        this.remotePorts = remotePorts;
    }

    private String localDevice;
    private String remoteDevice;
    private List<String> localPorts;
    private List<String> remotePorts;

    @XmlElement(name = "local_device_type")
    public String getLocalDeviceType() {
        return localDeviceType;
    }

    public void setLocalDeviceType(String localDeviceType) {
        this.localDeviceType = localDeviceType;
    }

    @XmlElement(name = "remote_device_type")
    public String getRemoteDeviceType() {
        return remoteDeviceType;
    }

    public void setRemoteDeviceType(String remoteDeviceType) {
        this.remoteDeviceType = remoteDeviceType;
    }

    private String localDeviceType;

    private String remoteDeviceType;

}