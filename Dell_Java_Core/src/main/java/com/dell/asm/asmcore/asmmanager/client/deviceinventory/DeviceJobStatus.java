/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.GregorianCalendar;

@XmlRootElement(name = "DeviceJobStatus")
public class DeviceJobStatus {

    @ApiModelProperty(value = "Job Name")
    private String JobName;
    
    @ApiModelProperty(value = "Job Type")
    private String JobType;
    
    @ApiModelProperty(value = "Job Status")
    private String JobStatus;
    
    @ApiModelProperty(value = "Started By")
    private String startedBy;
    
    @ApiModelProperty(value = "Created Date")
    private GregorianCalendar createdDate;

    public DeviceJobStatus(){}
    public String getJobName() {
        return JobName;
    }

    public void setJobName(String jobName) {
        JobName = jobName;
    }

    public String getJobType() {
        return JobType;
    }

    public void setJobType(String jobType) {
        JobType = jobType;
    }

    public String getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(String startedBy) {
        this.startedBy = startedBy;
    }

    public GregorianCalendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(GregorianCalendar createdDate) {
        this.createdDate = createdDate;
    }

    public String getJobStatus() {
        return JobStatus;
    }

    public void setJobStatus(String jobStatus) {
        JobStatus = jobStatus;
    }
}
