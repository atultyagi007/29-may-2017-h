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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

import com.dell.pg.jraf.api.ref.IPolicyRef;
import com.dell.pg.orion.common.print.Dump;

@Entity
@Table(name="policy", schema="public" ,uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
public class PolicyRefEntity implements IPolicyRef, Serializable{
	
	public PolicyRefEntity(){
		
	}
	
	public PolicyRefEntity(IPolicyRef policyRef){
		this.refId = policyRef.getRefId();
		this.refType = policyRef.getRefType();
		this.displayName = policyRef.getDisplayName();
		this.deviceType = policyRef.getDeviceType();
	}
	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "policy_ref_id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String policyRefId;
	
//	@ManyToOne(optional = false, fetch = FetchType.EAGER)
//	@JoinColumn(name = "name", referencedColumnName="name")
//	public TemplateEntity template;
	
	@Column(name="name")
	private String name;
	
	@Column(name ="ref_id")
	private String refId;
	
	@Column(name ="ref_type")
	private String refType;	

	@Column(name ="display_name")
	private String displayName;
	
	@Column(name ="device_type")
	private String deviceType;

	
	public String getPolicyRefId() {
		return policyRefId;
	}

	
	public void setPolicyRefId(String policyRefId) {
		this.policyRefId = policyRefId;
	}

	
	public void getName(){
	}

	
	public void setName(String name){
		this.name = name;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String type) {
		this.deviceType = type;
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
}
