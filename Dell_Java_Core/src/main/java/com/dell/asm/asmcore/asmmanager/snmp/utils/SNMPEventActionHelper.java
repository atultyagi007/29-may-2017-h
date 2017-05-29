/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.snmp.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.DeviceConfigureRequest;
import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.DeviceIdentity;
import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.ServerIdentity;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.snmp.providers.SNMPEventBasedDiscoveryProvider;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.pg.asm.chassis.client.ClientUtils;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.Server;
import com.dell.pg.asm.chassis.client.discovery.AbstractChassisDiscoveryRequest;
import com.dell.pg.asm.chassis.client.discovery.ChassisDiscoveryRequest;
import com.dell.pg.asm.chassis.client.discovery.IChassisDiscoveryRequestService;
import com.dell.pg.jraf.api.discovery.DiscoveryResult;

/**
 * This class is responsible for supporting the the SNMPTrapEventBasedAction class for performing all the SNMP trap event message based action.
 * 
 */
public final class SNMPEventActionHelper {

    private static DeviceInventoryDAO DEVICE_INVENTORY_DAO = new DeviceInventoryDAO(); // Remove after changing static methods
    
    private SNMPEventActionHelper() {

    }

    // Logger
    protected static final Logger logger = Logger.getLogger(SNMPEventActionHelper.class);

    /**
     * Retrieves the newly inserted servers in to the chassis.
     * 
     * @param discoveryResult
     *            - the chassis discovery result
     * @param chassis
     *            - the chassis device
     * @param ipAddress
     *            - the ip address of the chassis
     * 
     * @return list of newly inserted servers
     * 
     */
    public static List<Server> filterInsertedServer(DiscoveryResult discoveryResult, Chassis chassis, String ipAddress) {

        logger.debug("filterInsertedServer -  discoveryResult" + discoveryResult + " , chassis.getServers() " + chassis.getServers());
        if (null == discoveryResult || null == discoveryResult.getDeviceRef(ipAddress) || null == chassis)
            return null;

        List<Server> newServers = ((Chassis) discoveryResult.getDeviceRef(ipAddress)).getServers();
        List<Server> oldServers = chassis.getServers();

        if (null == oldServers || oldServers.size() <= 0 || null == newServers || newServers.size() <= 0)
            return newServers;

        Iterator<Server> it = newServers.iterator();
        while (it.hasNext()) {
            Server newServer = it.next();
            for (Server oldServer : oldServers) {
                if (newServer.getServiceTag().equals(oldServer.getServiceTag())) {
                    it.remove();
                }
            }
        }

        logger.debug("List<Server> newServers " + newServers);
        return newServers;
    }

    /**
     * Retrieves the removed servers from the chassis.
     * 
     * @param discoveryResult
     *            - the chassis discovery result
     * @param chassis
     *            - the chassis device
     * @param ipAddress
     *            - the ip address of the chassis
     * 
     * @return list of removed servers
     * 
     */
    public static List<Server> filterRemovedServer(DiscoveryResult discoveryResult, Chassis chassis, String ipAddress) {

        logger.debug("filterRemovedServer -  discoveryResult" + discoveryResult + " , chassis.getServers() " + chassis.getServers());
        if (null == discoveryResult || null == discoveryResult.getDeviceRef(ipAddress) || null == chassis)
            return null;

        List<Server> newServers = ((Chassis) discoveryResult.getDeviceRef(ipAddress)).getServers();
        List<Server> oldServers = chassis.getServers();

        if (null == oldServers || oldServers.size() <= 0 || null == newServers || newServers.size() <= 0)
            return oldServers;

        Iterator<Server> it = oldServers.iterator();
        while (it.hasNext()) {
            Server oldServer = it.next();
            for (Server newServer : newServers) {
                if (newServer.getServiceTag().equals(oldServer.getServiceTag())) {
                    it.remove();
                }
            }
        }

        logger.debug("List<Server> oldServers " + oldServers);
        return oldServers;
    }

    /**
     * Retrieves the chassis inventory from database by Ip address.
     * 
     * @param ipAddress
     *            - the ip address of the chassis
     * 
     * @return Chassis - the chassis device
     * 
     */
    public static Chassis getOldChassisInventory(String ipAddress) {

        Chassis chassis = null;
/*        ChassisEntity chassisEntity = DevicesDAO.getInstance().getDeviceByManagementIP(ipAddress);

        if (chassisEntity != null)
            chassis = ChassisDeviceUtils.toChassisDevice(chassisEntity);
        logger.debug("Details of the Chassis " + chassis); */
        return chassis;
    }

    /**
     * Retrieves the chassis after discovery by Ip address.
     * 
     * @param ipAddress
     *            - the ip address of the chassis
     * @param credentialID
     *            - the credentials of the chassis
     * 
     * @return DiscoveryResult - the discovered chassis
     * 
     */
    public static DiscoveryResult runChassisDiscovery(String ipAddress, String credentialID) {

        IChassisDiscoveryRequestService service = ProxyUtil.getChassisDiscoveryProxyWithHeaderSet();

        ChassisDiscoveryRequest chassisRequest = new ChassisDiscoveryRequest();
        chassisRequest.setIpAddress(ipAddress);
        chassisRequest.setCredentialID(credentialID);
        String refId = UUID.randomUUID().toString();
        chassisRequest.setRefId(refId);
        chassisRequest.setRefType(ClientUtils.DISCOVERY_IP_REF_TYPE);
        chassisRequest.setDeviceType(ClientUtils.DEVICE_TYPE);
        chassisRequest.setDisplayName("Discovery of chassis:" + refId);

        AbstractChassisDiscoveryRequest createdRequest = service.createDiscoveryRequest(chassisRequest);
        if (null == createdRequest)
            return null;

        logger.debug("AbstractChassisDiscoveryRequest resp " + createdRequest);
        DiscoveryResult discoveryResult = SNMPEventBasedDiscoveryProvider.getInstance().discoverDevices(createdRequest);
        logger.debug("DiscoveryResult discoveryResult " + discoveryResult);

        logger.info("Deleting the discover chassis reuest: " + createdRequest.getRefId());

        try {
            service.deleteDiscoveryRequest(createdRequest.getRefId().trim());
            logger.info("Deleted the discover chassis reuest successfully with refID: " + createdRequest.getRefId().trim());
        } catch (Exception e) {
            logger.info("Error deleting the chassis disovery request: " + e);
        }

        return discoveryResult;
    }

    /**
     * Apply the infrastructure configuration template on the new servers.
     * 
     * @param newServers
     *            - the list of new servers inserted in the chassis
     * @param discoveryResult
     *            - the discovered chassis
     * @param ipAddress
     *            - the ip address of chassis
     * 
     */
    public static void applyConfigurationTemplate(List<Server> newServers, DiscoveryResult discoveryResult, String ipAddress) {

        Chassis discoveredDevice = (Chassis) discoveryResult.getDeviceRef(ipAddress);
        String refId = discoveredDevice.getRefId();
        Set<DeviceIdentity> deviceIdentities = new HashSet<DeviceIdentity>();
        DeviceIdentity deviceIdentity = new DeviceIdentity();
        ServerIdentity serverIdentity = new ServerIdentity();
        for (Server server : newServers) {
            serverIdentity.setIdracDnsName(discoveredDevice.getDnsName());
            serverIdentity.setServiceTag(server.getServiceTag());
            deviceIdentity.setServerIdentity(serverIdentity);
            deviceIdentity.setDeviceRef(refId);
            deviceIdentities.add(deviceIdentity);
        }

        DeviceInventoryEntity entity = DEVICE_INVENTORY_DAO.getDeviceInventory(refId);

        if (null != entity) {
            DeviceConfigureRequest deviceConfigRequest = new DeviceConfigureRequest();
            deviceConfigRequest.setDeviceType(DeviceType.BladeServer);
            deviceConfigRequest.setTemplateGuid(entity.getInfraTemplateId());
            deviceConfigRequest.setDeviceIdentities(deviceIdentities);

            //IApplyInfrastructureTemplateService service = ProxyUtil.getapplyInfrastructureTemplateProxy();
            //service.applyInfrastructureTemplate(deviceConfigRequest);
        } else {
            logger.debug("Unable to apply the configuration template. Device Inventory is not available.");
        }

    }
}
