/**************************************************************************
 *   Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db.entity.configuretemplate;

import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "configure_service_template", schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = { "id" }))
public class ConfigureTemplateEntity implements Serializable{
	
    private static final long serialVersionUID = -2743341089836876259L;

    @Id
    @GeneratedValue(generator = "pg-uuid")
    @GenericGenerator(name = "pg-uuid", strategy = "uuid")
    @Column(name = "id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String id;

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

    @OneToOne(fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    private ServiceTemplateEntity serviceTemplate;

    @Column(name = "configuration")
    private String configuration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Calendar createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Calendar getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Calendar updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public ServiceTemplateEntity getServiceTemplate() {
        return serviceTemplate;
    }

    public void setServiceTemplate(ServiceTemplateEntity serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigureTemplateEntity that = (ConfigureTemplateEntity) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
