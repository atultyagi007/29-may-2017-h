package com.dell.asm.asmcore.asmmanager.client.discovery;

/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Class for holding discovery job data.
 */
@XmlRootElement(name = "JobStatus")

public class JobStatus {

    

    private String jobName;
    private String currentStatus;
    

 
    public JobStatus() {

    }

   
    public  String getJobName() {
        return jobName;
    }

    public  void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public  String getCurrentStatus() {
        return currentStatus;
    }

    public  void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    
}

