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
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;


@Entity
@Table(name = "DeviceDiscover", schema = "public",
uniqueConstraints = @UniqueConstraint(columnNames = { "id" }))
public class DeviceDiscoverEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", columnDefinition = "id", insertable = true, updatable = false, nullable = false, unique = true)
    private String id;

    @Column(name = "marshalledDeviceDiscoverData")
    private String marshalledDeviceDiscoverData;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DiscoveryStatus status;

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

    public String getMarshalledDeviceDiscoverData() {
        return marshalledDeviceDiscoverData;
    }

    public DiscoveryStatus getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMarshalledDeviceDiscoverData(String marshalledDeviceDiscoverData) {
        this.marshalledDeviceDiscoverData = marshalledDeviceDiscoverData;
    }

    public void setStatus(DiscoveryStatus status) {
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
