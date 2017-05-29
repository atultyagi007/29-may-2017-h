/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.tasks;

import java.util.List;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.deployment.ChassisDeviceState;
import com.dell.pg.orion.jobmgr.IJobHistoryManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.dell.pg.orion.jobmgr.JobStatus;

public class JobUtils {

    private static final Logger LOGGER = Logger.getLogger(JobUtils.class);

    /**
     * All components must be from the same deployment.
     * @param components
     * @param chassisState overrides state for chassis device
     */
    public static void updateChassisStatus(final List<ChassisDeviceState> components, final DeviceState chassisState, final DeviceInventoryDAO deviceInventoryDAO) {
        DeviceInventoryEntity devInv = null;
        for (ChassisDeviceState cds : components) {
            try {
                devInv = deviceInventoryDAO.getDeviceInventory(cds.getDeviceId());
            } catch (Exception e) {
                LOGGER.error("updateChassisStatus failed for " + cds.getDeviceId(), e);
            }
            if (devInv == null) {
                LOGGER.error("updateChassisStatus could not find device " + cds.getDeviceId());
                continue;
            }

            // configuration job only affects chassis and IOMs, ignore blades
            if (!DeviceType.isChassis(devInv.getDeviceType()) &&
                    devInv.getDeviceType() != DeviceType.dellswitch)
                continue;

            if (!devInv.getManagedState().equals(ManagedState.UNMANAGED)) {

                DeviceState newState = cds.getDeviceState();
                if (DeviceType.isChassis(devInv.getDeviceType()) &&
                        chassisState != null) {
                    newState = chassisState;
                }
                devInv.setState(newState);
                LOGGER.debug("Updating device " +  devInv.getServiceTag() + " type " + devInv.getDeviceType().getLabel() + " to device state " + newState.getLabel());
                try {
                    deviceInventoryDAO.updateDeviceInventory(devInv);
                } catch (AsmManagerCheckedException e) {
                    LOGGER.error("Cannot update device_inventory for device: " + cds.getDeviceId(), e);
                }
            }
        }
    }

    public static void resetChassisStatus(final DeviceInventoryEntity devInv) {
        // for now, only change chassis state
        if (DeviceType.isChassis(devInv.getDeviceType())) {
            if (devInv.getState().equals(DeviceState.CONFIGURATION_ERROR))
                devInv.setState(DeviceState.READY);
        }
    }
}
