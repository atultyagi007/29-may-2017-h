/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.client.pupetmodule.PuppetDiscoveryRequest;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmcore.asmmanager.util.discovery.IDiscoveryProvider;
import com.dell.asm.asmcore.asmmanager.util.discovery.InfrastructureDevice;
import com.dell.asm.common.utilities.ASMCommonsUtils;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class PuppetDevice implements IDiscoveryProvider {

    // Member Variables
    protected InfrastructureDevice device;
    protected DiscoveredDevices result;
    protected String existingRefId;
    private DeviceType inventoryDeviceType = DeviceType.unknown;
    private DiscoverDeviceType discoverDeviceType = DiscoverDeviceType.UNKNOWN;
    private String firmwareComponentID = "";
    private boolean firmwareComponentIDSettable = false;
    private DeviceInventoryDAO deviceInventoryDAO = null;
    
    // Class Variables
    private static final Logger logger = Logger.getLogger(PuppetDevice.class);

    /**
     * Default constructor for the class.
     */
    public PuppetDevice() {
        this(new DeviceInventoryDAO());
    }
    
    /**
     * Constructor that sets the necessary resource access for the class.
     */
    public PuppetDevice(DeviceInventoryDAO deviceInvDao) {
        this.deviceInventoryDAO = deviceInvDao;
    }
    
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

    public boolean isFirmwareComponentIDSettable() {
        return firmwareComponentIDSettable;
    }

    public void setFirmwareComponentIDSettable(boolean firmwareComponentIDSettable) { 
        this.firmwareComponentIDSettable = firmwareComponentIDSettable; 
    }


    @Override
    public DiscoveredDevices discoverDevices(InfrastructureDevice device) {
        logger.debug("Discovering puppet device: " + device.getIpAddress() + ", type=" + getInventoryDeviceType());
        this.device = device;

        try {
            result = new DiscoveredDevices();
            initSimpleFacts(getDiscoverDeviceType().getPuppetModuleName());

            Set<FirmwareDeviceInventoryEntity> fwPuppetInvSet = new HashSet<>();
            FirmwareDeviceInventoryEntity fwPuppetInv = new FirmwareDeviceInventoryEntity();
            PuppetDiscoveryRequest createdRequest = new PuppetDiscoveryRequest();

            createdRequest.setIpAddress(device.getIpAddress());
            createdRequest.setCredentialId(device.getStorageCredentialId());
            createdRequest.setPuppetModuleName(getDiscoverDeviceType().getPuppetModuleName());
            createdRequest.setConnectType(getDiscoverDeviceType().getConnectType());
            createdRequest.setExistingRefId(device.getExistingRefId());
            createdRequest.setQuickDiscovery(device.isQuickDiscovery());

            DeviceInventoryEntity inventoryDevice = null;
            if (device.getExistingRefId() != null) {
                inventoryDevice = deviceInventoryDAO.getDeviceInventory(device.getExistingRefId());
            }

            if (DeviceType.isSwitch(getInventoryDeviceType())) {
                createdRequest.setCredentialId(device.getSwitchCredentiallId());
            }else if (DeviceType.isStorage(getInventoryDeviceType())) {
                createdRequest.setCredentialId(device.getStorageCredentialId());
            }else if (DeviceType.isVcenter(getInventoryDeviceType())) {
                createdRequest.setCredentialId(device.getvCenterCredentialId());
            }else if (DeviceType.isSCVMM(getInventoryDeviceType())) {
                createdRequest.setCredentialId(device.getScvmmCredentialId());
            }else if (DeviceType.isServer(getInventoryDeviceType())) {
                createdRequest.setConnectType("script");
                createdRequest.setScriptPath("asm/bin/idrac-discovery.rb");
                createdRequest.setPuppetModuleName("asm");
                createdRequest.setCredentialId(device.getServerCredentialId());
                if (inventoryDevice != null) {
                    createdRequest.setExistingRefId(PuppetModuleUtil.toCertificateName(inventoryDevice.getDiscoverDeviceType(),
                            inventoryDevice.getDeviceType(), inventoryDevice.getIpAddress()));
                }
            }else if (DeviceType.isChassis(getInventoryDeviceType())) {
                createdRequest.setConnectType("script");
                createdRequest.setScriptPath("asm/bin/chassis-discovery.rb");
                createdRequest.setPuppetModuleName("asm");
                createdRequest.setCredentialId(device.getChassisCredentialId());
                if (inventoryDevice != null) {
                    createdRequest.setExistingRefId(PuppetModuleUtil.toCertificateName(inventoryDevice.getDiscoverDeviceType(),
                            inventoryDevice.getDeviceType(), inventoryDevice.getIpAddress()));
                }

            }else if (DeviceType.em.equals(getInventoryDeviceType())) {
                createdRequest.setCredentialId(device.getEmCredentialId());
            }

            result.setChassisId(device.getParentDeviceId());
            result.setCredId(createdRequest.getCredentialId());
            DiscoveryJobUtils.updateDiscoveryResult(result);

            // Call to utility to invoke puppet command line.  Blocks until complete
            Map<String, String> facts = PuppetModuleUtil.discoverAndShowFacts(createdRequest, true);
            // Copy facts into result
            ObjectMapper mapper = new ObjectMapper();
            result.setFacts(mapper.writeValueAsString(facts));

            mergeSimpleFacts(facts, result);
            mergeFacts(facts, result, fwPuppetInv);

            if (isFirmwareComponentIDSettable() && getFirmwareComponentID()!=null)
                fwPuppetInv.setComponentID(getFirmwareComponentID());

            if (result.getDeviceType() != DeviceType.genericswitch &&
                    !DeviceType.isServer(result.getDeviceType()) &&
                    !DeviceType.isChassis(result.getDeviceType())) {
                fwPuppetInvSet.add(fwPuppetInv);
                DiscoveryJobUtils.updateFirmwareDiscoveryResult(result,  fwPuppetInvSet);
            }
            else {
                DiscoveryJobUtils.updateDiscoveryResult(result);
            }

            // check if we got new IOM's
            if (device.isFromInventoryJob() && result.getDeviceType() == DeviceType.dellswitch) {
                DeviceInventoryEntity devInv = deviceInventoryDAO.getDeviceInventory(result.getDeviceRefId());
                if (devInv!=null) {
                    deviceInventoryDAO.deleteFirmwareDeviceInventoryForDevice(devInv.getRefId());
                }
            }
        } catch (Exception e) {
            result.setStatus(DiscoveryStatus.ERROR);
            EEMILocalizableMessage eemiMessage = AsmManagerMessages.puppetException(device.getIpAddress(), getDiscoverDeviceType().getPuppetModuleName());
            result.setStatusMessage(eemiMessage.getDisplayMessage().localize());
            logger.warn("error discovering Puppet device with IP:" + device.getIpAddress() + ", jobId=" + result.getJobId(), e);
            DiscoveryJobUtils.updateDiscoveryResult(result);
        } finally {
            logger.debug("Finished discovering Puppet device with IP:" + device.getIpAddress() + ", status: " + result.getStatus() + ", jobId=" + result.getJobId());
        }
        return result;
    }

    protected void initSimpleFacts(String puppetModuleName) {
        result.setServerPoolId(device.getServerPoolId());
        result.setUnmanaged(device.isUnmanaged());
        result.setReserved(device.isReserved());
        result.setConfig(device.getConfig());
        result.setDiscoverDeviceType(this.getDiscoverDeviceType());
        result.setIpAddress(device.getIpAddress());
        result.setStatus(DiscoveryStatus.PENDING);
        result.setVendor(ASMCommonsUtils.VENDOR_NAME_DELL);

        result.setParentJobId(device.getParentJob()); // must set, not-null
        result.setDeviceType(this.getInventoryDeviceType());
        result.setJobId(result.getParentJobId() + "=" + device.getIpAddress());

        result.setRefId(UUID.randomUUID().toString());
        result.setDeviceRefId(puppetModuleName + "-" + device.getIpAddress());
        result.setRefType("puppetDevice");
    }

    protected void mergeSimpleFacts(Map<String, String> factLabelToValue, DiscoveredDevices result) {
        String parsableIp = factLabelToValue.get("name");
        int dashIndex = parsableIp.lastIndexOf("-");
        result.setIpAddress(parsableIp.substring(dashIndex + 1));
        result.setStatus(DiscoveryStatus.CONNECTED);
        if (factLabelToValue.containsKey("Manufacturer")) {
            result.setVendor(factLabelToValue.get("Manufacturer"));
        } else if (factLabelToValue.containsKey("manufacturer")) {
            result.setVendor(factLabelToValue.get("manufacturer"));
        }
    }

    protected void genericPuppetInvDetails(FirmwareDeviceInventoryEntity fwPuppetInv, DiscoveredDevices result) {
        fwPuppetInv.setIpaddress(result.getIpAddress());
        fwPuppetInv.setName(result.getVendor());
        fwPuppetInv.setServicetag(result.getServiceTag());
        fwPuppetInv.setParent_job_id(result.getParentJobId());
        fwPuppetInv.setJobId(result.getJobId());
    }

    abstract protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv);
}
