/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.tasks.listener;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;

public class FirmwareUpdateJobListener implements JobListener {

    private static final Logger logger = Logger.getLogger(FirmwareUpdateJobListener.class);

    public enum FirmwareUpdateJobStatus {
        INITIALIZING,
        STARTING,
        CANCELLED,
        COMPLETE;
    }

    public static final String LISTENER_NAME = "FirmwareUpdateJobListener-";
    private JobKey jobKey;
    private String name;
    private FirmwareUpdateJobStatus status = FirmwareUpdateJobStatus.INITIALIZING;

    public FirmwareUpdateJobListener(JobKey jobKey) {
        this.jobKey = jobKey;
        this.name = LISTENER_NAME  +
                jobKey.getName() + "-" +
                jobKey.getGroup();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        status = FirmwareUpdateJobStatus.STARTING;
        logger.debug("Job " + getName() + "is starting!");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        status = FirmwareUpdateJobStatus.CANCELLED;
        logger.debug("Job " + getName() + "was cancelled!");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        status = FirmwareUpdateJobStatus.COMPLETE;
        logger.debug("Job " + getName() + "has finished!");
    }

    public JobKey getJobKey() {
        return jobKey;
    }

    public void setJobKey(JobKey jobKey) {
        this.jobKey = jobKey;
    }

    public FirmwareUpdateJobStatus getStatus() {
        return status;
    }

    public void setStatus(FirmwareUpdateJobStatus status) {
        this.status = status;
    }
}