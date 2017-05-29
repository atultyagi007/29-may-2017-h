/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Formula;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;

@Entity
@TypeDef(name="inet", typeClass = InetType.class)
@Table(name = "device_inventory")
public class DeviceInventoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ref_id", unique = true, updatable = false, nullable = false)
    private String refId;

    @Column(name = "ref_type")
    private String refType;

    @Column(name = "ip_address", nullable = false)
    @Type(type = "inet")
    private String ipAddress;

    @Column(name = "service_tag", nullable = false)
    private String serviceTag;

    @Column(name = "model")
    private String model;

    @Column(name = "device_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;
    
    @Column(name = "vendor")
    private String vendor;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "managed_state")
    @Enumerated(EnumType.STRING)
    private ManagedState managedState;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private DeviceState state;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "created_date")
    private GregorianCalendar createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_date")
    private GregorianCalendar updatedDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "health")
    @Enumerated(EnumType.STRING)
    private DeviceHealth health;

    @Column(name = "health_message")
    private String healthMessage;

    @Column(name = "system_id")
    private String systemId;

    @Column(name = "infra_template_date")
    private GregorianCalendar infraTemplateDate;

    @Column(name = "infra_template_id")
    private String infraTemplateId;
    
    @Column(name = "identity_refId")
    private String identityRefId;

    @Column(name = "server_template_date")
    private GregorianCalendar serverTemplateDate;

    @Column(name = "server_template_id")
    private String serverTemplateId;

    @Column(name = "inventory_date")
    private GregorianCalendar inventoryDate;

    @Column(name = "compliance_check_date")
    private GregorianCalendar complianceCheckDate;

    @Column(name = "compliant")
    private String compliant;

    @Column(name = "discovered_date")
    private GregorianCalendar discoveredDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "groups_device_inventory", joinColumns = { @JoinColumn(name = "devices_inventory_seq_id") }, inverseJoinColumns = { @JoinColumn(name = "groups_seq_id") })
    private List<DeviceGroupEntity> deviceGroupList = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "deviceRefId")
    private List<DeviceLastJobStateEntity> deviceLastJobList = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name="deployment_to_device_map",
            joinColumns = @JoinColumn( name="device_id"),
            inverseJoinColumns = @JoinColumn( name="deployment_id")
    )
    private List<DeploymentEntity> deployments;

    @Formula("(select count(*) from deployment_to_device_map m where m.device_id = ref_id)")
    private int deploymentCount;

    @Formula("(select coalesce((select 'Service Catalog - ' || f.name from deployment_to_device_map dm join deployment d on dm.deployment_id = d.id join firmware_repository f on d.firmware_repository = f.id where device_type like '%Server%' and dm.device_id=ref_id limit 1),(select 'Default Catalog - ' ||  f.name from firmware_repository f where f.is_default='t'),(select 'Embedded Catalog - ' || f.name from firmware_repository f where f.is_embedded='t')))")
    private String firmwareName;
    
    @Version
    @Column(name="OPTLOCK")
    private Long version;

    @Column(name = "cred_id")
    private String credId;
    
    @Column(name = "os_ip_address")
    private String osIpAddress;

    @Column(name = "os_image_type")
    private String osImageType;

    @Column(name = "os_admin_password")
    private String osAdminPassword;

    @OneToMany(mappedBy = "deviceInventoryComplianceId.deviceInventory", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DeviceInventoryComplianceEntity> deviceInventoryComplianceEntities;

    @Column(name = "facts")
    private String facts;

    @Column(name = "config")
    private String config;

    @Column(name = "discover_device_type")
    @Enumerated(EnumType.STRING)
    private DiscoverDeviceType discoverDeviceType;

    @Column(name = "n_failures")
    private int failuresCount;

    @Column(name = "chassis_id")
    private String chassisId;

    @Column(name = "resource_type")
    private String resourceType;

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

    public DiscoverDeviceType getDiscoverDeviceType() {
        return discoverDeviceType;
    }

    public void setDiscoverDeviceType(DiscoverDeviceType discoverDeviceType) {
        this.discoverDeviceType = discoverDeviceType;
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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

        // need to save UI representation of device type for correct sorting on it
        if (deviceType!=null) {
            this.setResourceType(deviceType.getLabel());
        }else{
            this.setResourceType(null);
        }
    }

    public String getCompliant() {
        return compliant;
    }

    public void setCompliant(String compliant) {
        this.compliant = compliant;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ManagedState getManagedState() { return managedState; }

    public void setManagedState(ManagedState managedState) { this.managedState = managedState; }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    public int getDeploymentCount() { return deploymentCount; }

    public void setDeploymentCount(int count) {this.deploymentCount = count;}

    public String getFirmwareName() { return firmwareName; }

    public void setFirmwareName(String name) { this.firmwareName = name; }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public GregorianCalendar getCreatedDate() {
        if (createdDate != null) {
            return (GregorianCalendar) createdDate.clone();
        } else {
            return null;
        }
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setCreatedDate(GregorianCalendar createdDate) {
        if (createdDate != null) {
            this.createdDate = (GregorianCalendar) createdDate.clone();
        } else {
            this.createdDate = null;
        }
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public GregorianCalendar getUpdatedDate() {
        if (updatedDate != null) {
            return (GregorianCalendar) updatedDate.clone();
        } else {
            return null;
        }
    }

    public void setUpdatedDate(GregorianCalendar updatedDate) {
        if (updatedDate != null) {
            this.updatedDate = (GregorianCalendar) updatedDate.clone();
        } else {
            this.updatedDate = null;
        }
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public DeviceHealth getHealth() {
        return health;
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

    public String getIdentityRefId() {
		return identityRefId;
	}

	public void setIdentityRefId(String identityRefId) {
		this.identityRefId = identityRefId;
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

    public List<DeploymentEntity> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<DeploymentEntity> deployments) {
        this.deployments = deployments;
    }

    public String getOsIpAddress() {
        return osIpAddress;
    }

    public void setOsIpAddress(String osIpAddress) {
        this.osIpAddress = osIpAddress;
    }

    public String getOsImageType() {
        return osImageType;
    }

    public void setOsImageType(String osImageType) {
        this.osImageType = osImageType;
    }

    public String getOsAdminPassword() {
        return osAdminPassword;
    }

    public void setOsAdminPassword(String osAdminPassword) {
        this.osAdminPassword = osAdminPassword;
    }

    // ----------------------- Misc. Methods -------------------
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serviceTag == null) ? 0 : serviceTag.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DeviceInventoryEntity)) {
            return false;
        }
        final DeviceInventoryEntity other = (DeviceInventoryEntity) obj;
        if (serviceTag == null) {
            if (other.serviceTag != null) {
                return false;
            }
        } else if (!serviceTag.equals(other.serviceTag)) {
            return false;
        }
        return true;
    }


    // Dump contents.


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("refId", refId)
                .append("ipAddress", ipAddress)
                .append("serviceTag", serviceTag)
                .append("model", model)
                .append("deviceType", deviceType)
                .append("managedState", managedState)
                .append("state", state)
                .toString();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = new GregorianCalendar();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = new GregorianCalendar();
    }

    public List<DeviceGroupEntity> getDeviceGroupList() {
        return deviceGroupList;
    }

    public void setDeviceGroupList(List<DeviceGroupEntity> deviceGroupList) {
        this.deviceGroupList = deviceGroupList;
    }

    public void addDeviceGroup(DeviceGroupEntity deviceGroup) {
        this.deviceGroupList.add(deviceGroup);
    }

    public List<DeviceLastJobStateEntity> getDeviceLastJobList() {
        return deviceLastJobList;
    }

    public void setDeviceLastJobList(List<DeviceLastJobStateEntity> deviceLastJobList) {
        this.deviceLastJobList = deviceLastJobList;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setCredId(String credId) {
       this.credId = credId;
    }

    public String getCredId() {
        return credId;
    }

    public Set<DeviceInventoryComplianceEntity> getDeviceInventoryComplianceEntities() {
        return deviceInventoryComplianceEntities;
    }
    
    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
    
}