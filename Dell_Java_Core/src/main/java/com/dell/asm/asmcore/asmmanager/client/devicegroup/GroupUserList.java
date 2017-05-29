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
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.dell.pg.orion.common.print.Dump;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * User list DTO.
 */
@XmlRootElement(name = "GroupUserList")
public class GroupUserList implements Serializable {

	private static final long serialVersionUID = 1457008838334579709L;
	private List<GroupUser> groupUsers;
    private long totalRecords;

    public GroupUserList() {
    }

    public GroupUserList(List<GroupUser> groupUsers) {
        this.groupUsers = groupUsers;
    }

    /**
     * Gets the user list.
     * 
     * @return the user list.
     */
    @XmlElement(name = "GroupUser")
    @ApiModelProperty(value = "Group User list")
    public List<GroupUser> getGroupUsers() {
        return this.groupUsers;
    }

    /**
     * Sets the user list.
     * 
     * @param groupUsers
     *            the new list.
     */
    public void setGroupUsers(List<GroupUser> groupUsers) {
        this.groupUsers = groupUsers;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
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
        return EqualsBuilder.reflectionEquals(this, that);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
