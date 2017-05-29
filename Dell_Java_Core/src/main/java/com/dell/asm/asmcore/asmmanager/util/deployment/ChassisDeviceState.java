/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;

import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmdeployer.client.AsmDeployerComponentStatus;

public class ChassisDeviceState {
    private String deviceId;

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private DeviceState deviceState;

    public ChassisDeviceState(String id, DeviceState state) {
        deviceId = id;
        deviceState = state;
    }

}
