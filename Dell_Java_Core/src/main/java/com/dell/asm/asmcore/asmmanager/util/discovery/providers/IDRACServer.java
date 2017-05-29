/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.deployment.FilterEnvironment;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.common.utilities.ASMCommonsUtils;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.server.client.device.Server;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class IDRACServer extends PuppetDevice {

    // Class Variables
    private static final Logger logger = Logger.getLogger(IDRACServer.class);
    
    static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        return mapper;
    }    

    // Member Variables
    private DeviceInventoryDAO deviceInventoryDAO = null;
    private FirmwareUtil firmwareUtil = null;

    /**
     * Default constructor for the class.
     */
    public IDRACServer() {
        this(new DeviceInventoryDAO(),
             new FirmwareUtil());
    }
    
    /**
     * Constructor that sets the necessary resource access for the class. 
     */
    public IDRACServer(DeviceInventoryDAO deviceInvDao,
                       FirmwareUtil firmwareUtil) {
        this.deviceInventoryDAO = deviceInvDao;
        this.firmwareUtil = firmwareUtil;
    }

    /**
     * Initial value for device type. Used to get the credentials.
     * Will be adjusted once we have facts.
     * @return
     */
    @Override
    public DeviceType getInventoryDeviceType() {
        return DeviceType.Server;
    }

    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setServerPoolId(device.getServerPoolId());
        result.setUnmanaged(device.isUnmanaged());
        result.setReserved(device.isReserved());
        result.setConfig(device.getConfig());
        result.setDiscoverDeviceType(this.getDiscoverDeviceType());
        result.setIpAddress(device.getIpAddress());
        result.setParentJobId(device.getParentJob()); // must set, not-null
        result.setRefType(com.dell.pg.asm.server.client.ClientUtils.DISCOVERY_IP_REF_TYPE);
        if (device.getParentDeviceType() == null) {
            result.setDeviceType(DeviceType.RackServer);
        }
        else if (device.getParentDeviceType() == DiscoverDeviceType.CMC_FX2) {
            result.setDeviceType(DeviceType.FXServer);
        }
        else {
            result.setDeviceType(DeviceType.BladeServer);
        }
        result.setServerType("");

        setFirmwareComponentIDSettable(true);
        //genericPuppetInvDetails(fwPuppetInv, result);

        if (factLabelToValue != null) {
            String refId = factLabelToValue.get("refId");
            if (StringUtils.isNotEmpty(refId)) {
                processSuccess(refId);
            }
        }

        if (StringUtils.isEmpty(result.getDeviceRefId())){
            result.setStatus(DiscoveryStatus.ERROR);
            EEMILocalizableMessage eemiMessage = AsmManagerMessages.discoveryServiceException(device.getIpAddress());
            result.setStatusMessage(eemiMessage.getDisplayMessage().localize());
            logger.warn("error discovering device with IP:" + device.getIpAddress() +", no facts returned by asm-deployer");
            DiscoveryJobUtils.updateDiscoveryResult(result);
        }
    }

    private void processSuccess(String deviceRefId) {
        Server server = ProxyUtil.getDeviceServerProxyWithHeaderSet().getServer(deviceRefId);
        if (server==null) {
            logger.error("Server not found in DB: " + deviceRefId);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        // for inventory run we don't change device type
        if (!device.isFromInventoryJob()) {
            if (server.getServerType() == Server.ServerType.BLADE) {
                if (device.getParentDeviceType() == null) {
                    logger.debug("Ignore Blade for non-chassis discovery: " + server.getManagementIP() + "/" + server.getServiceTag());
                    result.setStatus(DiscoveryStatus.IGNORE);
                    DiscoveryJobUtils.updateDiscoveryResult(result);
                    return;
                }
                if (device.getParentDeviceType() == DiscoverDeviceType.CMC_FX2) {
                    result.setDeviceType(DeviceType.FXServer);
                }
                else {
                    result.setDeviceType(DeviceType.BladeServer);
                }

                result.setChassisId(device.getParentDeviceId());
            } 
            else {
                String model = FilterEnvironment.extractModelNumberFromString(server.getModel());
                if (model!=null && model.startsWith("T")) {
                    result.setDeviceType(DeviceType.TowerServer);
                } 
                else {
                    result.setDeviceType(DeviceType.RackServer);
                }
            }
        } 
        else {
            // restore chassis reference if it was broken in the past runs
            if (server.getServerType() == Server.ServerType.BLADE) {
                if (StringUtils.isNotEmpty(device.getParentDeviceId())) {
                    result.setChassisId(device.getParentDeviceId());
                }
            }
        }


        result.setDeviceRefId(deviceRefId);
        result.setStatus(DiscoveryStatus.CONNECTED);
        result.setSystemId(server.getDeviceId());
        result.setModel(server.getModel());
        result.setServiceTag(server.getServiceTag());
        result.setServerType(server.getServerType().value());
        result.setVendor(ASMCommonsUtils.VENDOR_NAME_DELL);
        result.setCredId(server.getCredentialId());
        result.setHealthState(server.getHealth().value());
        result.setDisplayName(server.getIdracName());

        // prepare facts for some server related info
        StringBuilder sb = new StringBuilder();
        sb.append("memoryInGB=");
        sb.append((int)(server.getMemory()/1024));
        sb.append(";");
        sb.append("nics=");
        sb.append(server.getNetworkInterfaceList()!=null?server.getNetworkInterfaceList().size():0);
        sb.append(";");

        if( server.getProcessorList() != null && server.getProcessorList().size() > 0   )
        {
            sb.append("numberOfCPUs=");
            sb.append( server.getProcessorList().size());
            sb.append(";");

            sb.append("cpuType=");
            sb.append(server.getProcessorList().get(0).getModel());
            sb.append(";");
        }

        if(StringUtils.isNotEmpty(server.getHostName())) {
            sb.append("hostname=");
            sb.append(server.getHostName());
            sb.append(";");
        }

        result.setFacts(sb.toString());

        Set<FirmwareDeviceInventoryEntity> currentFirmwareInventory;
        currentFirmwareInventory = DiscoveryJobUtils.getFirmwareDeviceEntities(server,result.getParentJobId(), result.getJobId());
        DiscoveryJobUtils.updateFirmwareDiscoveryResult(result, currentFirmwareInventory);

        try
        {
            DeviceInventoryEntity devInv = this.deviceInventoryDAO.getDeviceInventory(result.getDeviceRefId());
            if (devInv != null) {
                logger.debug("Updating devInv " + devInv + " with ip: " + devInv.getIpAddress() + " The rediscovered IP was: " + result.getIpAddress());
                this.deviceInventoryDAO.deleteFirmwareDeviceInventoryForDevice(devInv.getRefId());

                Set<FirmwareDeviceInventory> firmwareDeviceInventory = 
                        this.firmwareUtil.fetchVibsFirmwareDeviceInventory(devInv, result.getParentJobId(), result.getJobId());
                
                if (firmwareDeviceInventory != null && !firmwareDeviceInventory.isEmpty()) {
                    if (this.result.getFirmwareDeviceInventories() != null) {
                        this.result.getFirmwareDeviceInventories().addAll(firmwareDeviceInventory);
                    }
                    else {
                        this.result.setFirmwareDeviceInventories(new ArrayList(firmwareDeviceInventory));
                    }
                }
                
                this.deviceInventoryDAO.updateDeviceInventory(devInv);
            }
        } catch (AsmManagerCheckedException e) {
            logger.warn("Error while updating device for refid: " + result.getDeviceRefId() + ".  The rest of the update was successful.");
        }
    }


    
}
