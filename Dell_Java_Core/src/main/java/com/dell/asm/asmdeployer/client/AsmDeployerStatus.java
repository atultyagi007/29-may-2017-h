/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmdeployer.client;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;

@XmlRootElement(name = "deployment")
public class AsmDeployerStatus {
    private String executionId;
    private String id;
    private String name;
    private DeploymentStatusType status;
    private String message;
    // TODO: times are returned like "2014-08-26 18:26:58 +0000"
    private String startTime;
    private String endTime;

    private List<AsmDeployerComponentStatus> components;

    @XmlElement(name = "execution_id")
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeploymentStatusType getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatusType status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlElement(name = "start_time")
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @XmlElement(name = "end_time")
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<AsmDeployerComponentStatus> getComponents() {
        return components;
    }

    public void setComponents(List<AsmDeployerComponentStatus> components) {
        this.components = components;
    }
}
