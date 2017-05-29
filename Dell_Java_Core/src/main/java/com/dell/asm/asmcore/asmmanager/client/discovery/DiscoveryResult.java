package com.dell.asm.asmcore.asmmanager.client.discovery;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.wordnik.swagger.annotations.ApiModel;

@XmlRootElement(name = "DiscoveryResult")
@ApiModel(value="Discovery Result", description="Holds the discovery result")
public class DiscoveryResult {
	
	private String parentJobId;
	private String jobId;
	private String refType;
	private String refId;    
	private String deviceRefId;  
	private String ipAddress;
	private String serviceTag;
	private String model;
	private String vendor;	
	private DeviceType deviceType;
	private String serverType;
	private int serverCount;
	private int iomCount;
	private DiscoveryStatus status;
	private String statusMessage;
	private String healthState;
	private String healthStatusMsg;
	private String systemId;
	private DiscoverDeviceType discoverDeviceType;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    private String config;
	private Set<FirmwareDeviceInventory> firmwareDeviceInventories;
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
	public String getRefType() {
		return refType;
	}
	public void setRefType(String refType) {
		this.refType = refType;
	}
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	public String getDeviceRefId() {
		return deviceRefId;
	}
	public void setDeviceRefId(String deviceRefId) {
		this.deviceRefId = deviceRefId;
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
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public DeviceType getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
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
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public String getHealthState() {
		return healthState;
	}
	public void setHealthState(String healthState) {
		this.healthState = healthState;
	}
	public String getHealthStatusMsg() {
		return healthStatusMsg;
	}
	public void setHealthStatusMsg(String healthStatusMsg) {
		this.healthStatusMsg = healthStatusMsg;
	}
	public Set<FirmwareDeviceInventory> getFirmwareDeviceInventories() {
		if (firmwareDeviceInventories == null)
			firmwareDeviceInventories = new HashSet<FirmwareDeviceInventory>();
		return firmwareDeviceInventories;
	}
	public void setFirmwareDeviceInventories(
			Set<FirmwareDeviceInventory> firmwareDeviceInventories) {
		this.firmwareDeviceInventories = firmwareDeviceInventories;
	}

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public DiscoverDeviceType getDiscoverDeviceType() {
        return discoverDeviceType;
    }

    public void setDiscoverDeviceType(DiscoverDeviceType discoverDeviceType) {
        this.discoverDeviceType = discoverDeviceType;
    }


}
