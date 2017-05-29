package com.dell.asm.asmcore.asmmanager.db.entity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.LazyInitializationException;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

import com.dell.asm.asmcore.asmmanager.client.firmware.BundleType;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareBundle;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareComponent;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;

/**
 * Table to represent a firmware catalog uploaded into ASM system
 */
@Entity
@Table(name = "software_bundle", schema = "public")
@DynamicUpdate
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SoftwareBundleEntity extends BaseEntityAudit {
	
	private static final long serialVersionUID = 1L;

	@Column(name = "version")
	private String version;
	@Column(name = "bundle_date")
	private Date bundleDate;
	@Column(name = "userbundle")
	private boolean userBundle;
    @Column(name = "bundle_type")
    private String bundleType;
    @Column(name = "device_model")
    private String deviceModel;
    @Column(name = "criticality")
    private String criticality;
    @Column(name = "description")
    private String description;
    
    
    @ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL )
    @JoinColumn(name = "firmware_repository", referencedColumnName = "id")
    @ForeignKey(name = "software_bundle_firmware_repository_fk")
    private FirmwareRepositoryEntity firmwareRepositoryEntity;

    @ManyToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "software_bundle_component", joinColumns = { @JoinColumn(name = "software_bundle_id") }, inverseJoinColumns = { @JoinColumn(name = "software_component_id") })
    private Set<SoftwareComponentEntity> softwareComponents;    
    
	
    @Transient
    private String userBundlePath;
    @Transient
    private String userBundleHashMd5;
    
	public boolean isUserBundle() {
		return userBundle;
	}
    public void setUserBundle(boolean userBundle) {
		this.userBundle = userBundle;
	}

    public void setUserBundlePath(String path) {
	    this.userBundlePath = path;
	}
    public String getUserBundlePath() {
	    return this.userBundlePath;
	}

    public void setUserBundleHashMd5(String hashMd5) {
        this.userBundleHashMd5 = hashMd5;
    }
    public String getUserBundleHashMd5() {
        return this.userBundleHashMd5;
    }

	@Column(name = "device_type")
	private String deviceType;
	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getCriticality() {
		return criticality;
	}

	public void setCriticality(String criticality) {
		this.criticality = criticality;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public SoftwareBundle getSoftwareBundle()
	{
		SoftwareBundle sb = new SoftwareBundle();
		sb.setId(this.getId());
		sb.setVersion(version);
		sb.setName(this.getName());
		sb.setBundleDate(bundleDate);
		sb.setUserBundle(userBundle);
		sb.setDeviceType(deviceType);
		sb.setDeviceModel(deviceModel);
		sb.setCriticality(criticality);
		sb.setDescription(description);
		
		if (BundleType.SOFTWARE.getValue().equals(this.getBundleType())) {
		    sb.setBundleType(BundleType.SOFTWARE);
		}
		else {
		    sb.setBundleType(BundleType.FIRMWARE);
		}

		try {
			for (SoftwareComponentEntity sce : this.getSoftwareComponents()) {
				SoftwareComponent sc = sce.getSoftwareComponent();
				if (isUserBundle()) {
					sb.setUserBundlePath(sc.getPath());
					sb.setUserBundleHashMd5(sc.getHashMd5());
				}
				sb.getSoftwareComponents().add(sc);
			}
		} catch (LazyInitializationException e) {
			
		}
		return sb;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public FirmwareRepositoryEntity getFirmwareRepositoryEntity () {
		return firmwareRepositoryEntity ;
	}

	public void setFirmwareRepositoryEntity (FirmwareRepositoryEntity firmwareRepositoryEntity ) {
		this.firmwareRepositoryEntity  = firmwareRepositoryEntity ;
	}

	public Date getBundleDate() {
		return bundleDate;
	}

	public void setBundleDate(Date bundleDate) {
		this.bundleDate = bundleDate;
	}

	public Collection<SoftwareComponentEntity> getSoftwareComponents() {
		if (softwareComponents == null)
			this.softwareComponents = new HashSet<SoftwareComponentEntity>();
		return softwareComponents;
	}

	public void setSoftwareComponents(Set<SoftwareComponentEntity> softwareComponents) {		
		this.softwareComponents = softwareComponents;
	}
    public String getBundleType() {
        return bundleType;
    }
    public void setBundleType(String bundleType) {
        this.bundleType = bundleType;
    }	

}
