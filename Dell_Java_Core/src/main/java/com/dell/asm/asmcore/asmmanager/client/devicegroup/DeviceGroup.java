/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.devicegroup;

import java.io.Serializable;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDeviceList;
import com.dell.asm.rest.common.model.Link;
import com.dell.pg.orion.common.print.Dump;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * DeviceGroup DTO class.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DeviceGroup")
@XmlType(name = "DeviceGroup", propOrder = {"link", "groupSeqId", "groupName", "groupDescription", "createdDate", "createdBy", "updatedDate", "updatedBy",
        "managedDeviceList", "groupUserList", })
@ApiModel()
public class DeviceGroup implements Serializable {

	private static final long serialVersionUID = -6506447206462082143L;
	
	@XmlElement(name = "groupSeqId")
    private Long groupSeqId;
    @XmlElement(name = "groupName", required = true)
    private String groupName;
    @XmlElement(name = "groupDescription")
    private String groupDescription;
    @XmlElement(name = "createdDate")
    private GregorianCalendar createdDate;
    @XmlElement(name = "createdBy")
    private String createdBy;
    @XmlElement(name = "updatedDate")
    private GregorianCalendar updatedDate;
    @XmlElement(name = "updatedBy")
    private String updatedBy;

    @XmlElement(name = "managedDeviceList")
    private ManagedDeviceList managedDeviceList;
    @XmlElement(name = "groupUserList")
    private GroupUserList groupUserList;

    @XmlElement(name = "link")
    private Link link;

    /**
     * @return the groupSeqId
     */
    @ApiModelProperty(value = "Device Group sequence id", required = false)
    public Long getGroupSeqId() {
        return groupSeqId;
    }

    /**
     * @param groupSeqId
     *            the groupSeqId to set
     */
    public void setGroupSeqId(Long groupSeqId) {
        this.groupSeqId = groupSeqId;
    }

    /**
     * @return the groupName
     */
    @ApiModelProperty(value = "Device Group name", required = true)
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName
     *            the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the groupDescription
     */
    @ApiModelProperty(value = "Device Group description", required = false)
    public String getGroupDescription() {
        return groupDescription;
    }

    /**
     * @param groupDescription
     *            the groupDescription to set
     */
    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    /**
     * @return the createdDate
     */
    @ApiModelProperty(value = "Device Group created date", required = false)
    public GregorianCalendar getCreatedDate() {
        if (createdDate != null) {
            return (GregorianCalendar) createdDate.clone();
        } else {
            return null;
        }
    }

    /**
     * @param createdDate
     *            the createdDate to set
     */
    public void setCreatedDate(GregorianCalendar createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy
     *            the createdBy to set
     */
    @ApiModelProperty(value = "Device Group created by", required = false)
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the updatedDate
     */
    @ApiModelProperty(value = "Device Group updated date", required = false)
    public GregorianCalendar getUpdatedDate() {
        if (updatedDate != null) {
            return (GregorianCalendar) updatedDate.clone();
        } else {
            return null;
        }
    }

    /**
     * @param updatedDate
     *            the updatedDate to set
     */
    public void setUpdatedDate(GregorianCalendar updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * @return the updatedBy
     */
    @ApiModelProperty(value = "Device Group updated by", required = false)
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy
     *            the updatedBy to set
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return the managedDeviceList
     */
    @ApiModelProperty(value = "Device Group device list", required = false)
    public ManagedDeviceList getManagedDeviceList() {
        return managedDeviceList;
    }

    /**
     * @param managedDeviceList
     *            the managedDeviceList to set
     */
    public void setManagedDeviceList(ManagedDeviceList managedDeviceList) {
        this.managedDeviceList = managedDeviceList;
    }

    /**
     * @return the groupUserList
     */
    @ApiModelProperty(value = "Device Group user list", required = false)
    public GroupUserList getGroupUserList() {
        return groupUserList;
    }

    /**
     * @param groupUserList
     *            the groupUserList to set
     */
    public void setGroupUserList(GroupUserList groupUserList) {
        this.groupUserList = groupUserList;
    }

    /**
     * @return the link
     */
    @ApiModelProperty(value = "Self Link")
    public Link getLink() {
        return link;
    }

    /**
     * @param link
     *            the link to set
     */
    public void setLink(Link link) {
        this.link = link;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Dump.toString(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that, new String[] { "createdDate", "updatedDate" });
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
