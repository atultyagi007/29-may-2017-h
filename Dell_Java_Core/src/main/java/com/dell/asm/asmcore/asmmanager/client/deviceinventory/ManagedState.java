/**************************************************************************
 * Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 * *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

/**
 * ManagedState controls what actions are available on devices.
 *
 * For example:
 *   Whether a device can be automatically selected for deployment.
 *   Whether a device can receive firmware updates.
 *
 * These states were extracted from DeviceState in order to isolate
 * changes to this state through explicit user interaction.
 */
public enum ManagedState {

    /**
     * State when a device is managed by ASM.
     * Means the device can be deployed to and
     * receive firmware updates.
     */
    MANAGED("managed"),

    /**
     * State when a device is not to be managed by ASM.
     * Means the device cannot be deployed to and cannot
     * receive firmware updates.
     */
    UNMANAGED("unmanaged"),

    /**
     * State when a device is reserved by ASM.
     * Means the device cannot be deployed to except
     * as part of a brownfield deployment. But it can
     * receive firmware updates.
     */
    RESERVED("reserved");

    private String _label;

    private ManagedState(String label){_label = label;}

    public String getLabel(){return _label;}

    public String getValue(){return name();}

    @Override
    public String toString(){return _label;}

    /**
     * Returns true if the state indicates it's reserved or a sub-state of reserved.
     *
     * @return true if the state is logically any substate of RESERVED.
     */
    public boolean isReserved() {
        return (this.equals(RESERVED));
    }

}
