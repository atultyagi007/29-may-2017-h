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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.pg.orion.common.print.Dump;

@Entity
@Table(name = "device_last_job_state")
public class DeviceLastJobStateEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "device_ref_id", nullable = false)
    private String deviceRefId;

    @Column(name = "job_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(name = "job_state", nullable = true)
    @Enumerated(EnumType.STRING)
    private DeviceState jobState;

    @Column(name = "description")
    private String description;

    @Column(name = "created_date")
    private GregorianCalendar createdDate;

    @Column(name = "updated_date")
    private GregorianCalendar updatedDate;

    
    public String getDeviceRefId() {
        return deviceRefId;
    }

    public void setDeviceRefId(String deviceRefId) {
        this.deviceRefId = deviceRefId;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public DeviceState getJobState() {
        return jobState;
    }

    public void setJobState(DeviceState jobState) {
        this.jobState = jobState ;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GregorianCalendar getCreatedDate() {
        if (createdDate != null) {
            return (GregorianCalendar) createdDate.clone();
        } else {
            return null;
        }
    }

    public void setCreatedDate(GregorianCalendar createdDate) {
        if (createdDate != null) {
            this.createdDate = (GregorianCalendar) createdDate.clone();
        } else {
            this.createdDate = null;
        }
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

    @PrePersist
    protected void onCreate() {
        createdDate = new GregorianCalendar();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = new GregorianCalendar();
    }
}
