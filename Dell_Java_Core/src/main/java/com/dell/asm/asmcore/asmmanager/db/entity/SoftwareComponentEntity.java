package com.dell.asm.asmcore.asmmanager.db.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.ForeignKey;

import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareComponent;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;

/**
 * Table to represent a firmware catalog uploaded into ASM system
 */
@Entity
@Table(name = "software_component", schema = "public")
@DynamicUpdate
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class SoftwareComponentEntity extends BaseEntityAudit {
	
	private static final long serialVersionUID = 1L;

    @Column(name = "package_id")
    private String packageId;
	@Column(name = "dell_version")
	private String dellVersion;
	@Column(name = "vendor_version")
	private String vendorVersion;
	@Column(name = "category")
	private String category;
	@Column(name = "component_type")
	private String componentType;
	@Column(name = "component_id")
	private String componentId;
	@Column(name = "device_id")
	private String deviceId;
	@Column(name = "sub_device_id")
	private String subDeviceId;
	@Column(name = "vendor_id")
	private String vendorId;
	@Column(name = "sub_vendor_id")
	private String subVendorId;
	@Column(name = "path")
	private String path;
	@Column(name = "operating_system")
	private String operatingSystem;
	@Column(name = "remote_protocol")
	private String remoteProtocol;
	
	@OneToMany(mappedBy = "softwareComponentEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<SystemIDEntity> systemIDs;
	
    @Column(name = "hash_md5")
    private String hashMd5;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firmware_repository", referencedColumnName = "id")
    @ForeignKey(name = "software_component_firmware_repository_fk")
    private FirmwareRepositoryEntity firmwareRepositoryEntity;

	public SoftwareComponentEntity()
	{
		super();
	}
	
	public SoftwareComponent getSoftwareComponent()
	{
		SoftwareComponent softwareComponent = new SoftwareComponent();
		softwareComponent.setId(this.getId());
        softwareComponent.setPackageId(packageId);
		softwareComponent.setDellVersion(this.dellVersion);
		softwareComponent.setVendorVersion(this.vendorVersion);
		softwareComponent.setComponentId(this.componentId);
		softwareComponent.setDeviceId(this.deviceId);
		softwareComponent.setSubDeviceId(this.subDeviceId);
		softwareComponent.setVendorId(this.vendorId);
		softwareComponent.setPath(this.path);
		softwareComponent.setName(this.getName());
		softwareComponent.setSubVendorId(this.subVendorId);
		softwareComponent.setCreatedBy(this.getCreatedBy());
		softwareComponent.setCreatedDate(this.getCreatedDate());
		softwareComponent.setUpdatedBy(this.getUpdatedBy());
		softwareComponent.setUpdatedDate(this.getUpdatedDate());
		softwareComponent.setCategory(this.category);
		softwareComponent.setComponentType(this.componentType);
        softwareComponent.setHashMd5(hashMd5);
        softwareComponent.setOperatingSystem(this.operatingSystem);
        for (SystemIDEntity id : this.getSystemIDs())
        	softwareComponent.getSystemIDs().add(id.getId());
		
		return softwareComponent;
	}
	
	public SoftwareComponentEntity(SoftwareComponent softwareComponent)
	{
		super();
        this.packageId = softwareComponent.getPackageId();
		this.dellVersion = softwareComponent.getDellVersion();
		this.vendorVersion = softwareComponent.getVendorVersion();
		this.componentId = softwareComponent.getComponentId();
		this.deviceId = softwareComponent.getDeviceId();
		this.subDeviceId = softwareComponent.getSubDeviceId();
		this.vendorId = softwareComponent.getVendorId();
		this.subVendorId = softwareComponent.getSubVendorId();
		this.path = softwareComponent.getPath();
        this.hashMd5 = softwareComponent.getHashMd5();
		this.category = softwareComponent.getCategory();
		this.componentType = softwareComponent.getComponentType();
		for (String id : softwareComponent.getSystemIDs())
			this.getSystemIDs().add(new SystemIDEntity(id));
		
		this.setCreatedBy(softwareComponent.getCreatedBy());
		this.setCreatedDate(softwareComponent.getCreatedDate());
		this.setId(softwareComponent.getId());
		this.setUpdatedBy(softwareComponent.getUpdatedBy());
		this.setUpdatedDate(softwareComponent.getUpdatedDate());
		this.setName(softwareComponent.getName());		
		this.setOperatingSystem(softwareComponent.getOperatingSystem());
	}

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getDellVersion() {
		return dellVersion;
	}

	public void setDellVersion(String dellVersion) {
		this.dellVersion = dellVersion;
	}

	public String getVendorVersion() {
		return vendorVersion;
	}

	public void setVendorVersion(String vendorVersion) {
		this.vendorVersion = vendorVersion;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getSubDeviceId() {
		return subDeviceId;
	}

	public void setSubDeviceId(String subDeviceId) {
		this.subDeviceId = subDeviceId;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public String getSubVendorId() {
		return subVendorId;
	}

	public void setSubVendorId(String subVendorId) {
		this.subVendorId = subVendorId;
	}

	public FirmwareRepositoryEntity getFirmwareRepositoryEntity () {
		return firmwareRepositoryEntity ;
	}

	public void setFirmwareRepositoryEntity (FirmwareRepositoryEntity firmwareRepositoryEntity ) {
		this.firmwareRepositoryEntity  = firmwareRepositoryEntity ;
	}

	public void setPath(String path) {
		this.path = path;
	}	
	
	public String getPath()
	{
		return path;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

    public String getHashMd5() {
        return hashMd5;
    }

    public void setHashMd5(String hashMd5) {
        this.hashMd5 = hashMd5;
    }

	public Set<SystemIDEntity> getSystemIDs() {
		if (systemIDs == null)
			systemIDs = new HashSet<SystemIDEntity>();
		
		return systemIDs;
	}

	public void setSystemIDs(Set<SystemIDEntity> systemIDs) {
		this.systemIDs = systemIDs;
	}

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getRemoteProtocol() {
        return remoteProtocol;
    }

    public void setRemoteProtocol(String remoteProtocol) {
        this.remoteProtocol = remoteProtocol;
    }
    
}
