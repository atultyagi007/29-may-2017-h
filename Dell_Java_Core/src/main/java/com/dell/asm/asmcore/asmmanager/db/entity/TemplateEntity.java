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
import java.util.Calendar;
import java.util.Collection;
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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

import com.dell.pg.jraf.api.ref.IPolicyRef;
import com.dell.pg.orion.common.print.Dump;

@Entity
@Table(name = "template", schema = "public",
uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
public class TemplateEntity implements Serializable{ //  ITemplateData,
	
	private static final long serialVersionUID = -2743341089836876258L;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name="TemplatePolicyRef", joinColumns = @JoinColumn(name ="templateId" ),
			 inverseJoinColumns = @JoinColumn(name = "policyRefId"))
	private Collection<PolicyRefEntity> policyRefEntities = new ArrayList<PolicyRefEntity>();
	
	@Transient
	private Set<IPolicyRef> policyRefs;
	
	@Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@GeneratedValue(generator = "system-uuid")
    @Column(name = "template_id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String templateId;
    
    @Column(name ="name" , unique = true, nullable = false)
    private String name;
    
    @Column(name="template_type")
    private String templateType;
    
    @Column(name="template_desc")
    private String templateDesc;
    
    @Column(name="wizard_page_number")
    private Integer wizardPageNumber;

//	@Column(name="display_name")
//    private String displayName;
//    
//    @Column(name="device_type")
//    private String deviceType;
    
    @Column(name="created_date")
	@Temporal(TemporalType.TIMESTAMP)
    private Calendar createdDate;
    
    @Column(name="created_by")
    private String createdBy;
    
    @Column(name="updated_date")
	@Temporal(TemporalType.TIMESTAMP)
    private Calendar updatedDate;
    
    @Column(name="updated_by")
    private String updatedBy;
    
    @Column(name="state")
    private boolean state;
    
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
	 
	public String getTemplateType() {
		return templateType;
	}

	
	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	
	public String getTemplateDesc() {
		return templateDesc;
	}

	
	public void setTemplateDesc(String templateDesc) {
		this.templateDesc = templateDesc;
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

	
	public void setUpdatedDate(GregorianCalendar updatedDate) {
        this.updatedDate = (updatedDate == null) ? null : (GregorianCalendar)updatedDate.clone();
	}

	
	public String getUpdatedBy() {
		return updatedBy;
	}

	
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	
	public boolean getState() {
		return state;
	}

	
	public void setState(boolean state) {
		this.state = state;
	}
	

	
	public Collection<PolicyRefEntity> getPolicyRefEntities() {
		return policyRefEntities;
	}
	
	public void setPolicyRefEntities(Collection<PolicyRefEntity> policyEntities) {
		this.policyRefEntities = policyEntities;
	}
	// Always returns an empty set. Use getPolicyRefEntities() to get policy references.
	public Set<IPolicyRef> getPolicies()
	{
	 if (policyRefs == null)
	 policyRefs = new HashSet<IPolicyRef>();
	 return policyRefs;
	}
	
	// use setPolicyRefEntities(Collection<PolicyRefEntity> policyEntities) to set policy references.
	void setPolicies(Set<IPolicyRef> policyRefs)
	{
	  this.policyRefs = policyRefs;
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
    
   
}
