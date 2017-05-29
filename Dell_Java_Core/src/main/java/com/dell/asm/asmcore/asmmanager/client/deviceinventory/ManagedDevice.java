/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.asm.asmcore.asmmanager.client.devicegroup.DeviceGroupList;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.rest.common.model.Link;
import com.dell.pg.orion.common.print.Dump;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * The Managed Device exposed through REST interfaces. Used to support the minimal device inventory view.
 */
@XmlRootElement(name = "ManagedDevice")
@ApiModel()
public class ManagedDevice implements Serializable {
	
	private static final long serialVersionUID = 8931770248828392445L;

	@ApiModelProperty(value = "Ref ID", required = true)
    private String refId;

    @ApiModelProperty(value = "Ref Type")
    private String refType;

    @ApiModelProperty(value = "IP Address", required = true)
    private String ipAddress;

    @ApiModelProperty(value = "Service Tag", required = true)
    private String serviceTag;

    @ApiModelProperty(value = "Model")
    private String model;

    @ApiModelProperty(value = "Device Type", required = true)
    private DeviceType deviceType;

    @ApiModelProperty(value = "Discovery Device Type", required = true)
    private DiscoverDeviceType discoverDeviceType;

    @ApiModelProperty(value = "Name")
    private String displayName;

    @ApiModelProperty(value = "ManagedState")
    private ManagedState managedState;

    @ApiModelProperty(value = "State")
    private DeviceState state;

    @ApiModelProperty(value = "In Use")
    private boolean inUse;

    @ApiModelProperty(value = "Service names")
    private List<ServiceReference> serviceReferences = new ArrayList<>();

    @ApiModelProperty(value = "Localizable Status Message")
    private DeviceState statusMessage;

    @ApiModelProperty(value = "Firmware Catalog Name")
    private String firmwareName;

    @ApiModelProperty(value = "Manufacturer")
    private String manufacturer;

    @ApiModelProperty(value = "systemId")
    private String systemId;

    @ApiModelProperty(value = "Health")
    private DeviceHealth health;

    @ApiModelProperty(value = "Health Message")
    private String healthMessage;

    @ApiModelProperty(value = "Operating System")
    private String operatingSystem;

    @ApiModelProperty(value = "Number of CPUs")
    private int numberOfCPUs;

    @ApiModelProperty(value = "CPU Type")
    private String cpuType;

    @ApiModelProperty(value = "Nics")
    private int nics;

    @ApiModelProperty(value = "Memory in GB")
    private int memoryInGB;

    @ApiModelProperty(value = "Last Date/Time Infrastructure Template was Applied")
    private GregorianCalendar infraTemplateDate;

    @ApiModelProperty(value = "Id of Last Infrastructure Template Applied")
    private String infraTemplateId;

    @ApiModelProperty(value = "Last Date/Time Server Template was Applied")
    private GregorianCalendar serverTemplateDate;

    @ApiModelProperty(value = "Id of Last Server Template Applied")
    private String serverTemplateId;

    @ApiModelProperty(value = "Last Inventory Date/Time")
    private GregorianCalendar inventoryDate;

    @ApiModelProperty(value = "Last Compliance Check Date/Time")
    private GregorianCalendar complianceCheckDate;

    @ApiModelProperty(value = "Discovery Date/Time")
    private GregorianCalendar discoveredDate;

    @ApiModelProperty(value = "Device Group List")
    private DeviceGroupList deviceGroupList;

    @ApiModelProperty(value = "Link to Device Detail")
    private Link detailLink;

    @ApiModelProperty(value = "Id of Device Credential")
    private String credId;

    private CompliantState compliance;

    @ApiModelProperty(value = "Number of Failures")
    private int failuresCount;

    @ApiModelProperty(value = "Chassis ID")
    private String chassisId;

    @ApiModelProperty(value = "device facts")
    private String facts;

    @ApiModelProperty(value = "device initial configuration template")
    private String config;

    @ApiModelProperty(value = "Hostname")
    private String hostname;

    public DiscoverDeviceType getDiscoverDeviceType() {
        return discoverDeviceType;
    }

    public void setDiscoverDeviceType(DiscoverDeviceType discoverDeviceType) {
        this.discoverDeviceType = discoverDeviceType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getFailuresCount() {
        return failuresCount;
    }

    public void setFailuresCount(int failuresCount) {
        this.failuresCount = failuresCount;
    }


    public String getChassisId() {
        return chassisId;
    }

    public void setChassisId(String chassisId) {
        this.chassisId = chassisId;
    }

	public String getFacts() {
        return facts;
    }

    public void setFacts(String facts) {
        this.facts = facts;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
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

    public ManagedState getManagedState() { return managedState; }

    public void setManagedState(ManagedState managedState) { this.managedState = managedState; }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public List<ServiceReference> getServiceReferences() {
        return serviceReferences;
    }

    public void setServiceReferences(List<ServiceReference> serviceReferences) {
        this.serviceReferences = serviceReferences;
    }

    public String getFirmwareName() { return firmwareName; }

    public void setFirmwareName(String name) { this.firmwareName = name; }

    public DeviceState getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(DeviceState statusMessage) {
        this.statusMessage = statusMessage;
    }

    public DeviceHealth getHealth() {
        return health;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getCpuType() {
        return cpuType;
    }

    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setHealth(DeviceHealth health) {
        this.health = health;
    }

    public String getHealthMessage() {
        return healthMessage;
    }

    public void setHealthMessage(String healthMessage) {
        this.healthMessage = healthMessage;
    }

    public GregorianCalendar getInfraTemplateDate() {
        if (infraTemplateDate != null) {
            return (GregorianCalendar) infraTemplateDate.clone();
        } else {
            return null;
        }
    }

    public void setInfraTemplateDate(GregorianCalendar infraTemplateDate) {
        if (infraTemplateDate != null) {
            this.infraTemplateDate = (GregorianCalendar) infraTemplateDate.clone();
        } else {
            this.infraTemplateDate = null;
        }
    }

    public String getInfraTemplateId() {
        return infraTemplateId;
    }

    public void setInfraTemplateId(String infraTemplateId) {
        this.infraTemplateId = infraTemplateId;
    }

    public GregorianCalendar getServerTemplateDate() {
        if (serverTemplateDate != null) {
            return (GregorianCalendar) serverTemplateDate.clone();
        } else {
            return null;
        }
    }

    public void setServerTemplateDate(GregorianCalendar serverTemplateDate) {
        if (serverTemplateDate != null) {
            this.serverTemplateDate = (GregorianCalendar) serverTemplateDate.clone();
        } else {
            this.serverTemplateDate = null;
        }
    }

    public String getServerTemplateId() {
        return serverTemplateId;
    }

    public void setServerTemplateId(String serverTemplateId) {
        this.serverTemplateId = serverTemplateId;
    }

    public GregorianCalendar getInventoryDate() {
        if (inventoryDate != null) {
            return (GregorianCalendar) inventoryDate.clone();
        } else {
            return null;
        }
    }

    public void setInventoryDate(GregorianCalendar inventoryDate) {
        if (inventoryDate != null) {
            this.inventoryDate = (GregorianCalendar) inventoryDate.clone();
        } else {
            this.inventoryDate = null;
        }
    }

    public GregorianCalendar getComplianceCheckDate() {
        if (complianceCheckDate != null) {
            return (GregorianCalendar) complianceCheckDate.clone();
        } else {
            return null;
        }
    }

    public void setComplianceCheckDate(GregorianCalendar complianceCheckDate) {
        if (complianceCheckDate != null) {
            this.complianceCheckDate = (GregorianCalendar) complianceCheckDate.clone();
        } else {
            this.complianceCheckDate = null;
        }
    }

    public GregorianCalendar getDiscoveredDate() {
        if (discoveredDate != null) {
            return (GregorianCalendar) discoveredDate.clone();
        } else {
            return null;
        }
    }

    public void setDiscoveredDate(GregorianCalendar discoveredDate) {
        if (discoveredDate != null) {
            this.discoveredDate = (GregorianCalendar) discoveredDate.clone();
        } else {
            this.discoveredDate = null;
        }
    }

    public DeviceGroupList getDeviceGroupList() {
        return deviceGroupList;
    }

    public void setDeviceGroupList(DeviceGroupList deviceGroupList) {
        this.deviceGroupList = deviceGroupList;
    }

	public Link getDetailLink() {
		return detailLink;
	}

	public void setDetailLink(Link detailLink) {
		this.detailLink = detailLink;
	}

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public int getNumberOfCPUs() {
        return numberOfCPUs;
    }

    public void setNumberOfCPUs(int numberOfCPUs) {
        this.numberOfCPUs = numberOfCPUs;
    }

    public int getMemoryInGB() {
        return memoryInGB;
    }

    public void setMemoryInGB(int memoryInGB) {
        this.memoryInGB = memoryInGB;
    }

    public String getCredId() {
        return credId;
    }

    public void setCredId(String credId) {
        this.credId = credId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Dump.toString(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that, new String[] { "infraTemplateDate", "serverTemplateDate", "inventoryDate", "complianceCheckDate", "discoveredDate" });
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

	public CompliantState getCompliance() {
		return compliance;
	}

	public void setCompliance(CompliantState compliance) {
		this.compliance = compliance;
	}

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public int getNics() {
        return nics;
    }

    public void setNics(int nics) {
        this.nics = nics;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

}
