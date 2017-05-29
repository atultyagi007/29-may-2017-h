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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import com.dell.pg.orion.common.print.Dump;

@Entity
@Table(name = "service_template", schema = "public",
uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
public class ServiceTemplateEntity implements Serializable{ //  ITemplateData,
	
    private static final long serialVersionUID = -2743341089836876259L;
	
    @Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    @Column(name = "template_id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String templateId;
    
    @Column(name ="name" , unique = true, nullable = false)
    private String name;
    
    @Column(name="template_desc")
    private String templateDesc;
    
    @Column(name="template_version")
    private String templateVersion;
    
    @Column(name="template_valid")
    private boolean templateValid;
    
    @Column(name="template_locked")
    private boolean templateLocked;
    
    @Column(name="wizard_page_number")
    private Integer wizardPageNumber;

    @Column(name="created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar createdDate;
    
    @Column(name="created_by")
    private String createdBy;
    
    @Column(name="updated_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar updatedDate;
        
    @Column(name="last_deployed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastDeployedDate;
    
    @Column(name="updated_by")
    private String updatedBy;
    
    @Column(name="draft")
    private boolean draft;

    @Column(name="all_users")
    private boolean allUsersAllowed;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name="template_id")
    private Set<TemplateUserRefEntity> assignedUserList = new HashSet<>();
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="firmware_repository")
    private FirmwareRepositoryEntity firmwareRepositoryEntity;       
    
    @Column(name = "manage_firmware")
    private boolean manageFirmware;

    @Column(name = "use_default_catalog")
    private boolean useDefaultCatalog;

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(name = "service_template_add_on_module", joinColumns = { @JoinColumn(name = "service_template_id") }, inverseJoinColumns = { @JoinColumn(name = "add_on_module_id") })
    private Set<AddOnModuleEntity> addOnModules = new HashSet<>();
    
    public boolean isManageFirmware() {
		return manageFirmware;
	}

	public void setManageFirmware(boolean manageFirmware) {
		this.manageFirmware = manageFirmware;
	}

    public boolean isUseDefaultCatalog() {
        return useDefaultCatalog;
    }

    public void setUseDefaultCatalog(boolean useDefaultCatalog) {
        this.useDefaultCatalog = useDefaultCatalog;
    }

    public FirmwareRepositoryEntity getFirmwareRepositoryEntity() {
		return firmwareRepositoryEntity;
	}

	public void setFirmwareRepositoryEntity(
			FirmwareRepositoryEntity firmwareRepositoryEntity) {
		this.firmwareRepositoryEntity = firmwareRepositoryEntity;
	}

	public void setCreatedDate(Calendar createdDate) {
		this.createdDate = createdDate;
	}

	public void setUpdatedDate(Calendar updatedDate) {
		this.updatedDate = updatedDate;
	}

	public void setLastDeployedDate(Calendar lastDeployedDate) {
		this.lastDeployedDate = lastDeployedDate;
	}

	public Set<TemplateUserRefEntity> getAssignedUserList() {
        return assignedUserList;
    }

    public void setAssignedUserList(Set<TemplateUserRefEntity> assignedUserList) {
        this.assignedUserList = assignedUserList;
    }

    public boolean isAllUsersAllowed() {
        return allUsersAllowed;
    }

    public void setAllUsersAllowed(boolean allUsersAllowed) {
        this.allUsersAllowed = allUsersAllowed;
    }

    private String marshalledTemplateData;
    
    //--------------Accessors-------------------

    public String getMarshalledTemplateData() {
		return marshalledTemplateData;
	}


	public void setMarshalledTemplateData(String marshalledTemplateData) {
		this.marshalledTemplateData = marshalledTemplateData;
	}
	
	public String getTemplateId() {
		return templateId;
	}

	
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getName() {
		return name;
	}

	
	public void setName(String templateName) {
		this.name = templateName;
	}
	
	public String getTemplateDesc() {
		return templateDesc;
	}

	
	public void setTemplateDesc(String templateDesc) {
		this.templateDesc = templateDesc;
	}
	
    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public boolean isTemplateValid() {
        return templateValid;
    }

    public void setTemplateValid(boolean templateValid) {
        this.templateValid = templateValid;
    }

    public boolean isTemplateLocked() {
        return templateLocked;
    }

    public void setTemplateLocked(boolean templateLocked) {
        this.templateLocked = templateLocked;
    }

    public Integer getWizardPageNumber() {
		return wizardPageNumber;
	}


	public void setWizardPageNumber(Integer wizardPageNumber) {
		this.wizardPageNumber = wizardPageNumber;
	}


//	public String getDisplayName() {
//		return displayName;
//	}
//
//	
//	public void setDisplayName(String displayName) {
//		this.displayName = displayName;
//	}
//
//	
//	public String getDeviceType() {
//		return deviceType;
//	}
//
//	
//	public void setDeviceType(String device_type) {
//		this.deviceType = device_type;
//	}

	
	public GregorianCalendar getCreatedDate() {
        return (createdDate == null) ? null : (GregorianCalendar)createdDate.clone();
	}

	
	public void setCreatedDate(GregorianCalendar createdDate) {
        this.createdDate = (createdDate == null) ? null : (GregorianCalendar)createdDate.clone();

	}

	
	public String getCreatedBy() {
		return createdBy;
	}

	
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	
	public GregorianCalendar getUpdatedDate() {
        return (updatedDate == null) ? null : (GregorianCalendar)updatedDate.clone();
	}

    public GregorianCalendar getLastDeployedDate() {
        return (lastDeployedDate == null) ? null : (GregorianCalendar)lastDeployedDate.clone();
    }
    
    public void setLastDeployedDate(GregorianCalendar lastDeployedDate) {
        this.lastDeployedDate = (lastDeployedDate == null) ? null : (GregorianCalendar)lastDeployedDate.clone();
    }	
	
	public void setUpdatedDate(GregorianCalendar updatedDate) {
        this.updatedDate = (updatedDate == null) ? null : (GregorianCalendar)updatedDate.clone();
	}

	
	public String getUpdatedBy() {
		return updatedBy;
	}

	
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

    public Set<AddOnModuleEntity> getAddOnModules() {
        return addOnModules;
    }

    public void setAddOnModules(Set<AddOnModuleEntity> addOnModules) {
        this.addOnModules = addOnModules;
    }

    //---- misc methods-----
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
    
    @PrePersist
    protected void onCreate() {
    	createdDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    } 
   
    @PreUpdate
    protected void onUpdate() {
      updatedDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    }


    public boolean isDraft() {
        return draft;
    }


    public void setDraft(boolean draft) {
        this.draft = draft;
    }
    
   
}
