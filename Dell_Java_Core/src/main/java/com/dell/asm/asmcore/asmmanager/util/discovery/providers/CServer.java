/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.common.model.CommandResponse;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.CredentialEntity;

public class CServer extends CLIDevice {
    private static final Logger logger = Logger.getLogger(CServer.class);
    private static final String  CSERIES_SYSTEMID = "1304";
    private DeviceInventoryDAO deviceInventoryDAO = null;
    
    /**
     * Default constructor for the class.
     */
    public CServer() {
        this(new DeviceInventoryDAO());
    }
    
    /**
     * Constructor that sets the necessary resource access for the class. 
     */
    public CServer(DeviceInventoryDAO deviceInventoryDAO) {
        this.deviceInventoryDAO = deviceInventoryDAO;
    }
    
    @Override
    protected void executeCommand() {
        CommandResponse cmdresponse;
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();
        String serverMsg = null;

        CredentialEntity cred = CredentialDAO.getInstance().findById(device.getServerCredentialId());
        if (cred != null) {
            Set<FirmwareDeviceInventoryEntity> fwCSeriesInvSet = new HashSet<>();
            Map<String, String> firmwareVersions = new HashMap<String, String>();
            String[] cmdLine = {
                    "/opt/dell/pec/bmc",
                    "-H",
                    device.getIpAddress(),
                    "allinfo"
            };
            // String cmdLine = "ipmitool -I lan -H " + device.getIpAddress() + " -U " + cred.getUsername() + " -P " + cred.getPassword() + " fru";
            try {
                cmdresponse = cmdRunner.runCommandWithConsoleOutputWithCheckNewLine(cmdLine, true, true);
                if (cmdresponse.getReturnCode().equals("0")) {
                    serverMsg = cmdresponse.getReturnMessage();
                    if (serverMsg != null) {
                        logger.debug("ipmi response returned for server " + serverMsg);

                        firmwareVersions = parseBMCOutput(serverMsg);
                        if (result.getModel() != null) {
                            result.setRefType(com.dell.pg.asm.server.client.ClientUtils.DISCOVERY_IP_REF_TYPE);
                            //for inventory, check if the service tag already exists in device inventory table, if yes, then use that devicerefID to
                            //update the firmware device inventory table
                            DeviceInventoryEntity devInven = this.deviceInventoryDAO.getDeviceInventoryByServiceTag(result.getServiceTag());
                            if (devInven != null)
                                result.setDeviceRefId(devInven.getRefId());
                            else
                                result.setDeviceRefId(UUID.randomUUID().toString());
                            result.setSystemId(CSERIES_SYSTEMID);
                            for (Map.Entry<String, String> entry : firmwareVersions.entrySet()) {
                                FirmwareDeviceInventoryEntity fwCSeriesInv = new FirmwareDeviceInventoryEntity();
                                fwCSeriesInv.setVersion(entry.getValue());
                                if (StringUtils.contains(entry.getKey(), "BIOS"))
                                    fwCSeriesInv.setComponentID(getFirmwareComponentID());
                                fwCSeriesInv.setName(entry.getKey());
                                fwCSeriesInv.setIpaddress(result.getIpAddress());
                                fwCSeriesInv.setServicetag(result.getServiceTag());
                                fwCSeriesInv.setParent_job_id(result.getParentJobId());
                                fwCSeriesInv.setJobId(result.getJobId());
                                fwCSeriesInvSet.add(fwCSeriesInv);
                            }
                            DiscoveryJobUtils.updateFirmwareDiscoveryResult(result, fwCSeriesInvSet);
                            try {
                                DeviceInventoryEntity devInv = this.deviceInventoryDAO.getDeviceInventory(result.getDeviceRefId());

                                if (devInv != null) {
                                    logger.debug("Updating devInv " + devInv + " with ip: " + devInv.getIpAddress() + " The rediscovered IP was: " + result.getIpAddress());
                                    if (!devInv.getIpAddress().equals(result.getIpAddress())) {
                                        devInv.setIpAddress(result.getIpAddress());
                                    }
                                    this.deviceInventoryDAO.updateDeviceInventory(devInv);
                                    this.deviceInventoryDAO.deleteFirmwareDeviceInventoryForDevice(devInv.getRefId());
                                    if (result.getFirmwareDeviceInventories() != null) {
                                        for (FirmwareDeviceInventory fdi : result.getFirmwareDeviceInventories()) {
                                            fdi.setId(null);
                                            FirmwareDeviceInventoryEntity fdiEntity = new FirmwareDeviceInventoryEntity(fdi);
                                            fdiEntity.setDeviceInventoryId(devInv.getRefId());
                                            this.deviceInventoryDAO.createFirmwareDeviceInventory(fdiEntity);
                                        }
                                    }

                                } else {
                                    if (device.isFromInventoryJob()) {
                                        logger.debug("Found new blade: service tag=" + result.getServiceTag() + ", IP=" + result.getIpAddress());
                                        // device will be added in DiscoverIpRangeJob.updateInventory()
                                    }
                                }
                            } catch (AsmManagerCheckedException e) {
                                result.setStatus(DiscoveryStatus.ERROR);
                                logger.warn("Error while updating device for refid: " + result.getDeviceRefId() + ".  The rest of the update was successful.");
                                EEMILocalizableMessage eemiMessage = AsmManagerMessages.discoveryServiceException(device.getIpAddress());
                                result.setStatusMessage(eemiMessage.getDisplayMessage().localize());
                            }
                        }
                    }
                } else {
                    logger.debug("BMC server discovery command returned: " + cmdresponse.getReturnMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, String> parseBMCOutput(String serverMsg) {
        Map<String, String> firmwareVersions = new HashMap<String, String>();
        String[] tokens = serverMsg.split("\n");
        String model = null;
        String serviceTag = null;
        String vendor = null;
        String biosVersion = "BIOS version";
        String bmcVersion = "BMC version";
        String fcbVersion = "FCB version";
        if (tokens != null && tokens.length > 0) {
            for (int i = 0; i < tokens.length; i++) {
                String[] strings = tokens[i].split(":");
                if (strings != null && strings.length == 2) {
                    if (StringUtils.contains(strings[0], "Product Name"))
                        model = strings[1];
                    if (StringUtils.contains(strings[0], "Product Serial"))
                        serviceTag = strings[1];
                    if (StringUtils.contains(strings[0], "Product Manufacturer"))
                        vendor = strings[1];
                    if (StringUtils.contains(strings[0], biosVersion))
                        firmwareVersions.put("BIOS", StringUtils.trim(strings[1]));
                    if (StringUtils.contains(strings[0], bmcVersion))
                        firmwareVersions.put("BMC", StringUtils.trim(strings[1]));
                    if (StringUtils.contains(strings[0], fcbVersion))
                        firmwareVersions.put("FCB", StringUtils.trim(strings[1]));

                }
            }

        }
        if (model != null) {
            result.setModel(StringUtils.trim(model));
            result.setServiceTag(StringUtils.trim(serviceTag));
            result.setVendor(StringUtils.trim(vendor));
            result.setDisplayName(result.getServiceTag());
        }

        return firmwareVersions;
    }

}
