package com.dell.asm.asmcore.asmmanager.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;

@Entity
@Table(name = "softwarecomponent_systemid", schema = "public")
@DynamicUpdate
public class SystemIDEntity extends BaseEntityAudit {

	private static final long serialVersionUID = 2L;

	@Column(name = "system_id")
	private String systemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "software_component", referencedColumnName = "id")
	private SoftwareComponentEntity softwareComponentEntity;

	public SystemIDEntity() {
		super();
	}

	public SystemIDEntity(String id) {
		super();
		this.systemId = id;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId= systemId;
	}

	public SoftwareComponentEntity getSoftwareComponentEntity() {
		return softwareComponentEntity;
	}

	public void setSoftwareComponentEntity(
			SoftwareComponentEntity softwareComponentEntity) {
		this.softwareComponentEntity = softwareComponentEntity;
	}

}
