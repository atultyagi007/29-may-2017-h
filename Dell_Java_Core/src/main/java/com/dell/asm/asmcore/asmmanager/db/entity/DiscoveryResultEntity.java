/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db.entity;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.wordnik.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryResult;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;
import com.dell.pg.orion.common.print.Dump;

@Entity
@Table(name = "discovery_result")
@TypeDef(name="inet", typeClass = InetType.class)
public class DiscoveryResultEntity  extends BaseEntityAudit{    
      
    @Column(name = "parent_job_id", updatable = false, nullable = false)
    private String parentJobId;
   
    @Column(name = "job_id", updatable = false)
    private String jobId;
     
    @Column(name = "ref_type", nullable = false)
    private String refType;
    
    @Column(name = "system_id")
    private String system_id;
   
    @Column(name = "ref_id", nullable = false)
    private String refId;    
    
    @Column(name = "deviceRef_id")
    private String deviceRefId;  
    
    @Column(name = "ip_address", nullable = false)
    @Type(type = "inet")
    private String ipAddress;

    @Column(name = "service_tag")
    private String serviceTag;

    @Column(name = "model")
    private String model;

    @Column(name = "vendor")
    private String vendor;
    
    @Column(name = "device_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    public String getServerPoolId() {
        return serverPoolId;
    }

    public void setServerPoolId(String serverPoolId) {
        this.serverPoolId = serverPoolId;
    }

    @Column(name = "server_pool")
    private String serverPoolId;

    public DiscoverDeviceType getDiscoverDeviceType() {
        return discoverDeviceType;
    }

    public void setDiscoverDeviceType(DiscoverDeviceType discoverDeviceType) {
        this.discoverDeviceType = discoverDeviceType;
    }

    @Column(name = "discover_device_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscoverDeviceType discoverDeviceType;

    @Column(name = "server_type")
    private String serverType;
    
    @Column(name = "server_count")
    private int serverCount;
    
    @Column(name = "iom_count")
    private int iomCount;
    
 
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DiscoveryStatus status;
    
    @Column(name = "status_message")
    private String statusMessage;
    
    @Column(name = "health_state")
    private String healthState;
    
    @Column(name = "health_status_msg")
    private String healthStatusMsg;
    
    @OneToMany(mappedBy = "discoveryResultEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FirmwareDeviceInventoryEntity> firmwareList;

    public boolean isUnmanaged() {
        return unmanaged;
    }

    public void setUnmanaged(boolean unmanaged) {
        this.unmanaged = unmanaged;
    }

    @Column(name = "unmanaged")
    private boolean unmanaged = false;

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @Column(name = "reserved")
    private boolean reserved = false;

    public String getFacts() {
        return facts;
    }

    public void setFacts(String facts) {
        this.facts = facts;
    }

    @Column(name = "facts")
    private String facts;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Column(name = "config")
    private String config;

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

    public void setDeviceType(com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType deviceType2) {
        this.deviceType = deviceType2;
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

    public String getIpaddress() {
        return ipAddress;
    }


    public void setIpaddress(String ipaddress) {
        this.ipAddress = ipaddress;
    }


    public String getIpAddress() {
        return ipAddress;
    }


    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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

    // ----------------------- Misc. Methods -------------------
    // Dump contents.
    @Override
    public String toString() {
        return Dump.toString(this);
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
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


    public String getHealthStatusMsg() {
        return healthStatusMsg;
    }


    public void setHealthStatusMsg(String healthStatusMsg) {
        this.healthStatusMsg = healthStatusMsg;
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

    public Set<FirmwareDeviceInventoryEntity> getFirmwareList() {
    	if (firmwareList == null)
    		firmwareList = new HashSet<FirmwareDeviceInventoryEntity>();
        return firmwareList;
    }
    
    public void addFirmwareDeviceInventoryEntity(FirmwareDeviceInventoryEntity fdie)
    {
    	if (firmwareList == null)
    		firmwareList = new HashSet<FirmwareDeviceInventoryEntity>();
    	
    	if (fdie != null)
    	{
    		firmwareList.add(fdie);
    		fdie.setDiscoveryResultEntity(this);
    	}
    }
    
    public void removeFirmwareDeviceInventoryEntity(FirmwareDeviceInventoryEntity fdie)
    {
    	if (firmwareList == null)
    		firmwareList = new HashSet<FirmwareDeviceInventoryEntity>();    	
    	else if (fdie != null)
    	{
    		firmwareList.remove(fdie);    		
    	}
    }

    /**
     * Made private to prevent hibernate issues.  If you want to update the firmwaredeviceinventoryenties associated with this
     * Use the add/remove meethods.
     * @param firmwareList
     */
    public void setFirmwareList(Set<FirmwareDeviceInventoryEntity> firmwareList) {
    	if (this.firmwareList != null && firmwareList != null)
    	{
    		this.firmwareList.clear();
    		this.firmwareList.addAll(firmwareList);    	
    	}
    	else
    	{
    		this.firmwareList = firmwareList;
    	}
    	
    	if (firmwareList != null)
    		for (FirmwareDeviceInventoryEntity fdie : firmwareList)
    		{
    			fdie.setDiscoveryResultEntity(this);
    		}
    }

   public DiscoveryResult getDiscoveryResult()
   {
	   DiscoveryResult d = new DiscoveryResult();
	   d.setDeviceRefId(this.deviceRefId);
	   d.setDeviceType(this.deviceType);
	   d.setDiscoverDeviceType(this.discoverDeviceType);
	   
	   if (this.firmwareList != null)
		   for (FirmwareDeviceInventoryEntity fdie : this.firmwareList)
		   {
			   d.getFirmwareDeviceInventories().add(fdie.getFirmwareDeviceInventory());
			   
		   }
	   d.setHealthState(this.healthState);
	   d.setHealthStatusMsg(this.healthStatusMsg);
	   d.setIomCount(this.iomCount);
	   d.setIpAddress(this.ipAddress);
	   d.setJobId(this.jobId);
	   d.setModel(this.model);
	   d.setParentJobId(this.parentJobId);
	   d.setRefId(this.refId);
	   d.setRefType(this.refType);
	   d.setServerCount(this.serverCount);
	   d.setServerType(this.serverType);
	   d.setServiceTag(this.serviceTag);
	   d.setStatus(this.status);
	   d.setStatusMessage(this.statusMessage);
	   d.setVendor(this.vendor);
       d.setConfig(this.config);
	   
	   
	   
	   return d;
   }

public String getSystem_id() {
    return system_id;
}

public void setSystem_id(String system_id) {
    this.system_id = system_id;
}

}
