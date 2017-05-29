package com.dell.asm.asmcore.asmmanager.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.ForeignKey;

import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.asmcore.asmmanager.client.firmware.SourceType;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;

@Entity
@Table(name = "firmware_deviceinventory")
public class FirmwareDeviceInventoryEntity extends BaseEntityAudit{    
	
	private static final long serialVersionUID = 3L;

    @Column(name = "version")
    private String version;
    
    @Column(name = "ipaddress")
    private String ipaddress;
    
    @Column(name = "servicetag")
    private String servicetag;
    
    @Column(name = "parent_job_id")
    private String parent_job_id;
    
    @Column(name = "job_id")
    private String jobId;    

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_time")
    private Date lastUpdateTime;

    @Column(name = "fqdd")
    private String fqdd;

    @Column(name = "component_id")
    private String componentID;

    @Column(name = "component_type")
    private String componentType;

    @Column(name = "device_id")
    private String deviceID;

    @Column(name = "vendor_id")
    private String vendorID;

    @Column(name = "subdevice_id")
    private String subdeviceID;

    @Column(name = "subvendor_id")
    private String subvendorID;
    
	@Column(name = "device_inventory")
	private String deviceInventoryId; 
	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "discovery_result", referencedColumnName = "id")
    @ForeignKey(name = "firmware_deviceinventory_discovery_result_fk")
	private DiscoveryResultEntity discoveryResultEntity;
	
    @Column(name = "operating_system")
    private String operatingSystem; 

    @Column(name = "source")
    private String source = SourceType.Device.getValue();  // Generally always loaded from the device itself

	public FirmwareDeviceInventoryEntity()
	{
		super();
	}
	
	public FirmwareDeviceInventoryEntity(FirmwareDeviceInventory firmwareDeviceInventory)
	{
		super();
		this.componentID = firmwareDeviceInventory.getComponentID();
		this.componentType = firmwareDeviceInventory.getComponentType();
		this.fqdd = firmwareDeviceInventory.getFqdd();
		this.setId(firmwareDeviceInventory.getId());
		this.ipaddress = firmwareDeviceInventory.getIpaddress();
		this.jobId = firmwareDeviceInventory.getJobId();
		this.lastUpdateTime = firmwareDeviceInventory.getLastUpdateTime();
		this.setName(firmwareDeviceInventory.getName());
		this.parent_job_id = firmwareDeviceInventory.getParent_job_id();
		this.servicetag = firmwareDeviceInventory.getServicetag();
		this.deviceID = firmwareDeviceInventory.getDeviceID();
		this.subdeviceID = firmwareDeviceInventory.getSubdeviceID();
		this.vendorID = firmwareDeviceInventory.getVendorID();
		this.subvendorID = firmwareDeviceInventory.getSubvendorID();
		this.version = firmwareDeviceInventory.getVersion();
		if (firmwareDeviceInventory.getDeviceInventory() != null) {
		    this.deviceInventoryId = firmwareDeviceInventory.getDeviceInventory().getRefId();
		}
		this.operatingSystem = firmwareDeviceInventory.getOperatingSystem();
		if (firmwareDeviceInventory.getSourceType() != null) {
		    this.source = firmwareDeviceInventory.getSourceType().getValue();
		}
	}
        
    public FirmwareDeviceInventory getFirmwareDeviceInventory()
    {
    	FirmwareDeviceInventory firmwareDeviceInventory = new FirmwareDeviceInventory();
    	firmwareDeviceInventory.setComponentID(this.componentID);
    	firmwareDeviceInventory.setComponentType(this.componentType);
    	firmwareDeviceInventory.setDeviceID(this.deviceID);
    	firmwareDeviceInventory.setFqdd(this.fqdd);
    	firmwareDeviceInventory.setId(this.getId());
    	firmwareDeviceInventory.setIpaddress(this.ipaddress);
    	firmwareDeviceInventory.setJobId(this.jobId);
    	firmwareDeviceInventory.setLastUpdateTime(this.lastUpdateTime);
    	firmwareDeviceInventory.setName(this.getName());
    	firmwareDeviceInventory.setParent_job_id(this.parent_job_id);
    	firmwareDeviceInventory.setDeviceID(this.deviceID);
    	firmwareDeviceInventory.setVendorID(this.vendorID);
    	firmwareDeviceInventory.setServicetag(this.servicetag);
    	firmwareDeviceInventory.setSubdeviceID(this.subdeviceID);
    	firmwareDeviceInventory.setSubvendorID(this.subvendorID);
    	firmwareDeviceInventory.setVersion(this.version);
    	firmwareDeviceInventory.setOperatingSystem(this.operatingSystem);
    	firmwareDeviceInventory.setSourceType(SourceType.valueOf(this.source));
    	return firmwareDeviceInventory;
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getLastUpdateTime() {
        return  (null == lastUpdateTime) ? null : (Date) lastUpdateTime.clone();
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = (null == lastUpdateTime) ? null : (Date) lastUpdateTime.clone();
    }

    public String getFqdd() {
        return fqdd;
    }

    public void setFqdd(String fqdd) {
        this.fqdd = fqdd;
    }

    public String getComponentID() {
        return componentID;
    }

    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getVendorID() {
        return vendorID;
    }

    public void setVendorID(String vendorID) {
        this.vendorID = vendorID;
    }

    public String getSubdeviceID() {
        return subdeviceID;
    }

    public void setSubdeviceID(String subdeviceID) {
        this.subdeviceID = subdeviceID;
    }

    public String getSubvendorID() {
        return subvendorID;
    }

    public void setSubvendorID(String subvendorID) {
        this.subvendorID = subvendorID;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getParent_job_id() {
        return parent_job_id;
    }

    public void setParent_job_id(String parent_job_id) {
        this.parent_job_id = parent_job_id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getServicetag() {
        return servicetag;
    }

    public void setServicetag(String servicetag) {
        this.servicetag = servicetag;
    }

	public DiscoveryResultEntity getDiscoveryResultEntity() {
		return discoveryResultEntity;
	}

	public void setDiscoveryResultEntity(DiscoveryResultEntity discoveryResultEntity) {
		this.discoveryResultEntity = discoveryResultEntity;
	}

    public String getDeviceInventoryId() {
        return deviceInventoryId;
    }

    public void setDeviceInventoryId(String deviceInventoryId) {
        this.deviceInventoryId = deviceInventoryId;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
