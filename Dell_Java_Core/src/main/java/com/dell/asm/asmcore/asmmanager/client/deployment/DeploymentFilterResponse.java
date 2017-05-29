/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeploymentFilterResponse")
public class DeploymentFilterResponse {

    public List<SelectedServer> getSelectedServers() {
        if (selectedServers==null)
            selectedServers = new ArrayList<>();

        return selectedServers;
    }

    public void setSelectedServers(List<SelectedServer> selectedServers) {
        this.selectedServers = selectedServers;
    }

    public List<RejectedServer> getRejectedServers() {
        if (rejectedServers==null)
            rejectedServers = new ArrayList<>();

        return rejectedServers;
    }

    public void setRejectedServers(List<RejectedServer> rejectedServers) {
        this.rejectedServers = rejectedServers;
    }

    private List<SelectedServer> selectedServers;
    private List<RejectedServer> rejectedServers;

    public int getNumberRequestedServers() {
        return numberRequestedServers;
    }

    public void setNumberRequestedServers(int numberRequestedServers) {
        this.numberRequestedServers = numberRequestedServers;
    }

    public int getNumberSelectedServers() {
        return getSelectedServers().size();
    }

    private int numberRequestedServers = 0;

    public String getFailedPoolId() {
        return failedPoolId;
    }

    public void setFailedPoolId(String failedPoolId) {
        this.failedPoolId = failedPoolId;
    }

    private String failedPoolId;

    public String getFailedPoolName() {
        return failedPoolName;
    }

    public void setFailedPoolName(String failedPoolName) {
        this.failedPoolName = failedPoolName;
    }

    private String failedPoolName;

}
