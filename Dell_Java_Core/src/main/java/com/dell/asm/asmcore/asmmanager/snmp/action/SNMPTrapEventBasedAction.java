/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.snmp.action;

import java.util.List;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.snmp.SNMPTrapConstants;
import com.dell.asm.asmcore.asmmanager.snmp.utils.SNMPEventActionHelper;
import com.dell.asm.asmcore.asmmanager.snmp.utils.SNMPTrapFrameworkUtils;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.Server;
import com.dell.pg.jraf.api.discovery.DiscoveryResult;
import com.dell.pg.orion.snmptrapmanager.SNMPEventMsg;

/**
 * This class is responsible for performing all the SNMP trap event message based action.
 */
public final class SNMPTrapEventBasedAction {

    private SNMPTrapEventBasedAction() {

    }

    // Logger
    protected static final Logger logger = Logger.getLogger(SNMPTrapEventBasedAction.class);

    /**
     * Performs the SNMP trap event based action.
     * 
     * @param event
     *            - this is a SNMP event message
     * 
     */
    public static void performSNMPTrapBasedAction(SNMPEventMsg event) {

        String ipAddress = event.getSNMPEvent().getAgentAddress();

        switch (SNMPTrapFrameworkUtils.get(event.getSNMPEvent().getEnterpriseId())) {

        case SNMPTrapConstants.SERVER_INSERT_IN_CHASSIS: {

            Chassis chassis = SNMPEventActionHelper.getOldChassisInventory(ipAddress);
            if (null == chassis) {
                logger.debug("Unable to find chassis information for management ip " + ipAddress);
                return;
            }

            DiscoveryResult discoveryResult = SNMPEventActionHelper.runChassisDiscovery(ipAddress, chassis.getCredentialRefId());
            if (null == discoveryResult) {
                logger.debug("DiscoveryResult object is null  for management ip " + ipAddress);
                return;
            }

            List<Server> newServers = SNMPEventActionHelper.filterInsertedServer(discoveryResult, chassis, ipAddress);

            if (null == newServers) {
                logger.debug("No new Servers are discovered for management ip " + ipAddress);
                return;
            }

            // Applies the Infrastructure Configuration template to newly inserted servers.
            if (null != newServers && newServers.size() > 0) {
                logger.debug("Applying configuration template on newly discovered servers.");
                SNMPEventActionHelper.applyConfigurationTemplate(newServers, discoveryResult, ipAddress);
            }
        }
            break;

        case SNMPTrapConstants.SERVER_REMOVAL_FROM_CHASSIS: {
            Chassis chassis = SNMPEventActionHelper.getOldChassisInventory(ipAddress);
            if (null == chassis) {
                logger.debug("Unable to find chassis information for management ip " + ipAddress);
                return;
            }

            DiscoveryResult discoveryResult = SNMPEventActionHelper.runChassisDiscovery(ipAddress, chassis.getCredentialRefId());
            if (null == discoveryResult) {
                logger.debug("DiscoveryResult object is null  for management ip " + ipAddress);
                return;
            }

            List<Server> oldServers = SNMPEventActionHelper.filterRemovedServer(discoveryResult, chassis, ipAddress);

        }
            break;

        case SNMPTrapConstants.IOM_INSERT_IN_CHASSIS: {
            Chassis chassis = SNMPEventActionHelper.getOldChassisInventory(ipAddress);
            if (null == chassis) {
                logger.debug("Unable to find chassis information for management ip " + ipAddress);
                return;
            }

            DiscoveryResult discoveryResult = SNMPEventActionHelper.runChassisDiscovery(ipAddress, chassis.getCredentialRefId());
            if (null == discoveryResult) {
                logger.debug("DiscoveryResult object is null  for management ip " + ipAddress);
                return;
            }
        }
            break;

        case SNMPTrapConstants.IOM_REMOVAL_IN_CHASSIS: {
            Chassis chassis = SNMPEventActionHelper.getOldChassisInventory(ipAddress);
            if (null == chassis) {
                logger.debug("Unable to find chassis information for management ip " + ipAddress);
                return;
            }

            DiscoveryResult discoveryResult = SNMPEventActionHelper.runChassisDiscovery(ipAddress, chassis.getCredentialRefId());
            if (null == discoveryResult) {
                logger.debug("DiscoveryResult object is null  for management ip " + ipAddress);
                return;
            }
        }
            break;

        case SNMPTrapConstants.SERVER_POWER_ON: {
            Chassis chassis = SNMPEventActionHelper.getOldChassisInventory(ipAddress);
            if (null == chassis) {
                logger.debug("Unable to find chassis information for management ip " + ipAddress);
                return;
            }

            DiscoveryResult discoveryResult = SNMPEventActionHelper.runChassisDiscovery(ipAddress, chassis.getCredentialRefId());
            if (null == discoveryResult) {
                logger.debug("DiscoveryResult object is null  for management ip " + ipAddress);
                return;
            }
        }
            break;

        case SNMPTrapConstants.SERVER_POWER_OFF: {
            Chassis chassis = SNMPEventActionHelper.getOldChassisInventory(ipAddress);
            if (null == chassis) {
                logger.debug("Unable to find chassis information for management ip " + ipAddress);
                return;
            }

            DiscoveryResult discoveryResult = SNMPEventActionHelper.runChassisDiscovery(ipAddress, chassis.getCredentialRefId());
            if (null == discoveryResult) {
                logger.debug("DiscoveryResult object is null  for management ip " + ipAddress);
                return;
            }
        }
            break;

        default:
            break;
        }
    }

}
