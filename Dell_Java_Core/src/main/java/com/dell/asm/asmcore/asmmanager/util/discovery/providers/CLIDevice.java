/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmcore.asmmanager.util.discovery.IDiscoveryProvider;
import com.dell.asm.asmcore.asmmanager.util.discovery.InfrastructureDevice;
import com.dell.asm.i18n2.EEMILocalizableMessage;

public abstract class CLIDevice implements IDiscoveryProvider {
    private static final Logger logger = Logger.getLogger(CLIDevice.class);

    protected InfrastructureDevice device;
    protected DiscoveredDevices result;
    private DeviceType inventoryDeviceType = DeviceType.unknown;

    @Override
    public DeviceType getInventoryDeviceType() {
        return inventoryDeviceType;
    }

    @Override
    public void setInventoryDeviceType(DeviceType inventoryDeviceType) {
        this.inventoryDeviceType = inventoryDeviceType;
    }

    @Override
    public void setDiscoverDeviceType(DiscoverDeviceType deviceType) {
        this.discoverDeviceType = deviceType;
    }

    private DiscoverDeviceType discoverDeviceType = DiscoverDeviceType.UNKNOWN;

    @Override
    public DiscoverDeviceType getDiscoverDeviceType() {
        return discoverDeviceType;
    }

    @Override
    public String getFirmwareComponentID() {
        return firmwareComponentID;
    }

    @Override
    public void setFirmwareComponentID(String firmwareComponentID) {
        this.firmwareComponentID = firmwareComponentID;
    }

    private String firmwareComponentID = "";

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVendor() {
        return vendor;
    }

    private String vendor;

    @Override
    public DiscoveredDevices discoverDevices(InfrastructureDevice device) {
        this.device = device;

        try {
            result = new DiscoveredDevices();

            if (DeviceType.isSwitch(getInventoryDeviceType())) {
                result.setCredId(device.getSwitchCredentiallId());
            }else if (DeviceType.isStorage(getInventoryDeviceType())) {
                result.setCredId(device.getStorageCredentialId());
            }else if (DeviceType.isVcenter(getInventoryDeviceType())) {
                result.setCredId(device.getvCenterCredentialId());
            }else if (DeviceType.isSCVMM(getInventoryDeviceType())) {
                result.setCredId(device.getScvmmCredentialId());
            }else if (DeviceType.isServer(getInventoryDeviceType())) {
                result.setCredId(device.getServerCredentialId());
            }else if (DeviceType.isChassis(getInventoryDeviceType())) {
                result.setCredId(device.getChassisCredentialId());
            }

            initSimpleFacts();
            executeCommand();

            DiscoveryJobUtils.updateDiscoveryResult(result);

        } catch (Exception e) {
            result.setStatus(DiscoveryStatus.ERROR);
            EEMILocalizableMessage eemiMessage = AsmManagerMessages.puppetException(device.getIpAddress(), getInventoryDeviceType().getValue());
            result.setStatusMessage(eemiMessage.getDisplayMessage().localize());
            logger.warn("error discovering device with IP:" + device.getIpAddress(), e);
            DiscoveryJobUtils.updateDiscoveryResult(result);
        }
        return result;
    }

    protected void initSimpleFacts() {
        result.setUnmanaged(device.isUnmanaged());
        result.setReserved(device.isReserved());
        result.setServerPoolId(device.getServerPoolId());
        result.setConfig(device.getConfig());
        result.setDiscoverDeviceType(this.getDiscoverDeviceType());
        result.setIpAddress(device.getIpAddress());
        result.setStatus(DiscoveryStatus.PENDING);
        result.setVendor(getVendor());
        result.setServiceTag(getInventoryDeviceType().getValue() + "-" + device.getIpAddress());
        result.setModel(getInventoryDeviceType().getValue());

        result.setParentJobId(device.getParentJob()); // must set, not-null
        result.setDeviceType(this.getInventoryDeviceType());
        result.setJobId(result.getParentJobId() + "=" + device.getIpAddress());

        result.setRefId(UUID.randomUUID().toString());
        result.setDeviceRefId(getInventoryDeviceType().getValue() + "-" + device.getIpAddress());
        result.setRefType("puppetDevice");
    }

    abstract protected void executeCommand();
}
