package com.dell.asm.asmcore.asmmanager.db.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.ConfigureStatus;


@Entity
@Table(name = "DeviceConfigure", schema = "public",
uniqueConstraints = @UniqueConstraint(columnNames = { "id" }))
public class DeviceConfigureEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String id;

    @Column(name = "marshalledDeviceConfigureData")
    private String marshalledDeviceConfigureData;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ConfigureStatus status;

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

    public String getId() {
        return id;
    }

    public String getMarshalledDeviceConfigureData() {
        return marshalledDeviceConfigureData;
    }

    public ConfigureStatus getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMarshalledDeviceConfigureData(String marshalledDeviceConfigureData) {
        this.marshalledDeviceConfigureData = marshalledDeviceConfigureData;
    }

    public void setStatus(ConfigureStatus status) {
        this.status = status;
    }

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



}
