/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.snmp;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.snmp.utils.SNMPTrapFrameworkUtils;
import com.dell.pg.orion.queuemanager.QMManager;
import com.dell.pg.orion.queuemanager.connections.QMException;
import com.dell.pg.orion.queuemanager.connections.QMUtils;
import com.dell.pg.orion.snmptrapmanager.ISNMPTrapHandler;
import com.dell.pg.orion.snmptrapmanager.ISNMPTrapListener;
import com.dell.pg.orion.snmptrapmanager.exception.SNMPTrapListenerException;

public final class SNMPTrapFramework {

    private SNMPTrapFramework() {

    }

    // Logger
    protected static final Logger logger = Logger.getLogger(SNMPTrapFramework.class);

    /**
     * Initialize the SNMP Trap Framework on startup of the ASMManager.
     * 
     * @return - ISNMPTrapListener
     * 
     */

    public static ISNMPTrapListener initilazieSNMPTrapFramework() {

        System.setProperty(QMUtils.QMUTILS_BROKER_NAME, QMManager.DEFAULT_INSTANCE_NAME);
        System.setProperty(QMUtils.QMUTILS_BROKER_CONNECTORS, QMManager.DEFAULT_LOCAL_CONNECTOR);
        ISNMPTrapListener snmpTrapListener = null;

        try {
            logger.info("Starting the broker for SNMP trap framework...");
            QMUtils.startBroker();
            logger.info("Broker started successfully...");

            logger.info("Starting the SNMP trap listener...");
            snmpTrapListener = SNMPTrapFrameworkUtils.createAndStartListener();

            logger.info("Creating the SNMP trap handler...");
            ISNMPTrapHandler snmpTrapHandler = SNMPTrapFrameworkUtils.createSNMPTrapHandler();

            // Add the handler to the listener
            snmpTrapListener.addTrapHandler(snmpTrapHandler);

            // Initialize the Queue Manager and register the message receiver
            SNMPTrapFrameworkUtils.listenSNMPTrap();

            logger.info("SNMP trap framework started successfully...");
        } catch (SNMPTrapListenerException e) {
            logger.info("Unable to start the SNMPTrap Framework. " + e.getMessage());
        } catch (QMException e) {
            logger.info("Unable to start the SNMPTrap Framework. " + e.getMessage());
        }
        return snmpTrapListener;
    }

    /**
     * Shutdown the SNMP Trap Framework on shutting down of the ASMManager.
     * 
     * @param snmpTrapListener - the listener to be stopped.
     */
    public static void shutdownSNMPTrapFramework(ISNMPTrapListener snmpTrapListener) {

        logger.info("Stopping the SNMP trap framework...");

        try {
            if (null == snmpTrapListener) {
                throw new SNMPTrapListenerException("SNMP trap listener is null.");
            } else {
                // Stop the listener
                snmpTrapListener.stop();
            }

            logger.info("SNMP trap framework stopped successfully...");
        } catch (SNMPTrapListenerException e) {
            logger.info("Unable to shutdown the SNMP trap framework. " + e.getMessage());
        }
    }
}
