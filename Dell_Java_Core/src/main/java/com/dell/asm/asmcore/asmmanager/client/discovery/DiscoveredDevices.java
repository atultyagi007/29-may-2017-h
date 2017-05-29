/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.discovery;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * The Device Discover Result exposed through REST interfaces.
 */
@XmlRootElement(name = "DiscoveredDevices")
@ApiModel()
public class DiscoveredDevices {
	
    @ApiModelProperty(value="Parent Job ID",required=true)
    private String parentJobId;

    @ApiModelProperty(value="Job ID")
    private String jobId;

    @ApiModelProperty(value="Ref ID",required=true)
    private String refId;

    @ApiModelProperty(value="Device Ref ID",required=true)
    private String deviceRefId;
    
    @ApiModelProperty(value="Ref Type",required=true)
    private String refType;

    @ApiModelProperty(value="IP Address",required=true)
    private String ipAddress;

    @ApiModelProperty(value="Service Tag", required=true)
    private String serviceTag;

    @ApiModelProperty(value="Model")
    private String model;

    @ApiModelProperty(value="vendor")
    private String vendor;
    
    @ApiModelProperty(value="systemId")
    private String systemId;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @ApiModelProperty(value="Display Name")
    private String displayName;

    //private String deviceType = ClientUtils.DEVICE_TYPE;
    @ApiModelProperty(value="Device Type")
    private DeviceType deviceType;

    public DiscoverDeviceType getDiscoverDeviceType() {
        return discoverDeviceType;
    }

    public void setDiscoverDeviceType(DiscoverDeviceType discoverDeviceType) {
        this.discoverDeviceType = discoverDeviceType;
    }

    @ApiModelProperty(value="Discover Device Type")
    private DiscoverDeviceType discoverDeviceType;

    @ApiModelProperty(value="Server Type")
    private String serverType;
    
    
    @ApiModelProperty(value="Server Count")
    private int serverCount;

    @ApiModelProperty(value="IOM Count")
    private int iomCount;

    @ApiModelProperty(value="Status")
    private DiscoveryStatus status;

    @ApiModelProperty(value="Status Message")
    private String statusMessage;

    @ApiModelProperty(value="healthState")
    private String healthState;

    @ApiModelProperty(value="health Status Message")
    private String healthStatusMessage;
    
    @ApiModelProperty(value="firmwareDeviceInventories")
    private List<FirmwareDeviceInventory> firmwareDeviceInventories;
       

    public List<FirmwareDeviceInventory> getFirmwareDeviceInventories() {
    	if (firmwareDeviceInventories == null)
    		firmwareDeviceInventories = new ArrayList<FirmwareDeviceInventory>();
		return firmwareDeviceInventories;
	}

	public void setFirmwareDeviceInventories(
			List<FirmwareDeviceInventory> firmwareDeviceInventories) {
		this.firmwareDeviceInventories = firmwareDeviceInventories;
	}

	public boolean isUnmanaged() {
        return unmanaged;
    }

    public void setUnmanaged(boolean unmanaged) {
        this.unmanaged = unmanaged;
    }

    @ApiModelProperty(value="Managed or Unmanaged")
    private boolean unmanaged = false;

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @ApiModelProperty(value="Reserved")
    private boolean reserved = false;

    public String getServerPoolId() {
        return serverPoolId;
    }

    public void setServerPoolId(String serverPoolId) {
        this.serverPoolId = serverPoolId;
    }

    private String serverPoolId;

    public String getCredId() {
        return credId;
    }

    public void setCredId(String credId) {
        this.credId = credId;
    }

    @ApiModelProperty(value="Credentials Ref ID",required=true)
    private String credId;


    public String getFacts() {
        return facts;
    }

    public void setFacts(String facts) {
        this.facts = facts;
    }

    @ApiModelProperty(value="Device Facts")
    private String facts;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @ApiModelProperty(value="Device initial configuration template")
    private String config;

    public String getChassisId() {
        return chassisId;
    }

    public void setChassisId(String chassisId) {
        this.chassisId = chassisId;
    }

    @ApiModelProperty(value="chassis ID")
    private String chassisId;


    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @ApiModelProperty(value="Initial Configuration - packed as a service template")
    private String configuration;

    public String getParentJobId() {
        return parentJobId;
    }

    public void setParentJobId(String parentJobId) {
        this.parentJobId = parentJobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public int getServerCount() {
        return serverCount;
    }

    public void setServerCount(int serverCount) {
        this.serverCount = serverCount;
    }

    public int getIomCount() {
        return iomCount;
    }

    public void setIomCount(int iomCount) {
        this.iomCount = iomCount;
    }

    public DiscoveryStatus getStatus() {
        return status;
    }

    public void setStatus(DiscoveryStatus status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getHealthState() {
        return healthState;
    }

    public void setHealthState(String healthState) {
        this.healthState = healthState;
    }

    public String getHealthStatusMessage() {
        return healthStatusMessage;
    }

    public void setHealthStatusMessage(String healthStatusMessage) {
        this.healthStatusMessage = healthStatusMessage;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getDeviceRefId() {
        return deviceRefId;
    }

    public void setDeviceRefId(String deviceRefId) {
        this.deviceRefId = deviceRefId;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

}
