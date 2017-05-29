/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.snmp.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.snmp.SNMPTrapConstants;
import com.dell.asm.asmcore.asmmanager.snmp.SNMPTrapListnerConstants;
import com.dell.asm.asmcore.asmmanager.snmp.action.SNMPTrapEventBasedAction;
import com.dell.asm.business.monitoringsettingmanager.MonitoringSettingMgr;
import com.dell.asm.common.model.MonitoringSettingModel;
import com.dell.asm.common.model.TrapSettingModel;
import com.dell.asm.i18n2.exception.AsmCheckedException;
import com.dell.pg.orion.common.utilities.ConfigurationUtils;
import com.dell.pg.orion.queuemanager.IQMManager;
import com.dell.pg.orion.queuemanager.QMManager;
import com.dell.pg.orion.queuemanager.connections.QMException;
import com.dell.pg.orion.queuemanager.connections.QMUtils;
import com.dell.pg.orion.queuemanager.qmmsg.IQMMsg;
import com.dell.pg.orion.snmptrapmanager.ISNMPTrapHandler;
import com.dell.pg.orion.snmptrapmanager.ISNMPTrapListener;
import com.dell.pg.orion.snmptrapmanager.SNMPEventMsg;
import com.dell.pg.orion.snmptrapmanager.SNMPTrap;
import com.dell.pg.orion.snmptrapmanager.SNMPTrapDefinition;
import com.dell.pg.orion.snmptrapmanager.SNMPTrapHandlerSpec;
import com.dell.pg.orion.snmptrapmanager.SNMPTrapHandlerSpec.DestinationType;
import com.dell.pg.orion.snmptrapmanager.SNMPTrapListenerFactory;
import com.dell.pg.orion.snmptrapmanager.SNMPTrapListenerProtocolEnum;
import com.dell.pg.orion.snmptrapmanager.SNMPTrapListenerSpec;
import com.dell.pg.orion.snmptrapmanager.SNMPVersionEnum;
import com.dell.pg.orion.snmptrapmanager.exception.SNMPTrapListenerException;
import com.dell.pg.orion.snmptrapmanager.impl.SNMPTrapHandler;

public final class SNMPTrapFrameworkUtils {

    private SNMPTrapFrameworkUtils() {

    }

    // Logger
    protected static final Logger logger = Logger.getLogger(SNMPTrapFrameworkUtils.class);

    /**
     * Reverse lookup map for getting a TrapConstant from a enterpriseTrapId.
     */
    private static Map<String, String> lookup = new HashMap<String, String>();

    /**
     * Creates and starts the SNMP trap listener
     * 
     * @return ISNMPTrapListener
     * 
     * @throws SNMPTrapListenerException
     * 
     */
    public static ISNMPTrapListener createAndStartListener() throws SNMPTrapListenerException {

        SNMPTrapListenerProtocolEnum protocol = null;
        ISNMPTrapListener listener = null;
        URL propFileURL = null;
        String host = null;
        String value = null;
        int port = 0;

        Set<ISNMPTrapHandler> handlers = new HashSet<ISNMPTrapHandler>();
        SNMPTrapListenerSpec spec = new SNMPTrapListenerSpec();

        try {

            // Load the properties file
            propFileURL = SNMPTrapFrameworkUtils.class.getClassLoader().getResource(SNMPTrapListnerConstants.SNMP_PROPERTIES_FILE);

            // Read the properties file
            Properties props = getPropertiesFromFile(propFileURL);

            // Read Host
            MonitoringSettingModel monitoringSetting = MonitoringSettingMgr.getInstance().getMonitoringSettings();
            if (null == monitoringSetting) {
                String msg = "Host is null.";
                throw new SNMPTrapListenerException(msg);
            }
            List<TrapSettingModel> trapSettings = monitoringSetting.getTrapSettings().getTrapSetting();
            for (TrapSettingModel trapSetting : trapSettings) {
                if (trapSetting.isDefaultIp()) {
                    host = trapSetting.getDestinationIPAddress();
                    break;
                }
            }

            if (StringUtils.isBlank(host)) {
                String msg = "Host is null.";
                throw new SNMPTrapListenerException(msg);
            }
            // Read Port
            value = props.getProperty(SNMPTrapListnerConstants.PROP_PORT);
            if (StringUtils.isBlank(value)) {
                String msg = "Port is null.";
                throw new SNMPTrapListenerException(msg);
            }
            try {
                port = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SNMPTrapListenerException("Port (" + value + ") is not valid.");
            }

            // Read Protocol
            value = props.getProperty(SNMPTrapListnerConstants.PROP_PROTOCOL);
            if (StringUtils.isBlank(value)) {
                throw new SNMPTrapListenerException("Protocol is null.");
            }
            try {
                protocol = Enum.valueOf(SNMPTrapListenerProtocolEnum.class, value);
            } catch (IllegalArgumentException e) {
                throw new SNMPTrapListenerException("Protocol (" + value + ") is not valid.");
            }

            // Add the listener specifications
            spec.setListenerHost(host);
            spec.setListenerPort(port);
            spec.setProtocol(protocol);

            // Read snmp supported versions
            if (StringUtils.isBlank(props.getProperty(SNMPTrapListnerConstants.PROP_SNMP_V1))
                    & StringUtils.isBlank(props.getProperty(SNMPTrapListnerConstants.PROP_SNMP_V1))
                    & StringUtils.isBlank(props.getProperty(SNMPTrapListnerConstants.PROP_SNMP_V1))) {
                throw new SNMPTrapListenerException("SNMP version is null.");
            }

            // Add the supported versions to the listener specifications
            if (Boolean.valueOf(props.getProperty(SNMPTrapListnerConstants.PROP_SNMP_V1))) {
                spec.getSupportedSNMPVersions().add(SNMPVersionEnum.VERSION1);
            }
            if (Boolean.valueOf(props.getProperty(SNMPTrapListnerConstants.PROP_SNMP_V2))) {
                spec.getSupportedSNMPVersions().add(SNMPVersionEnum.VERSION2);
            }
            if (Boolean.valueOf(props.getProperty(SNMPTrapListnerConstants.PROP_SNMP_V3))) {
                spec.getSupportedSNMPVersions().add(SNMPVersionEnum.VERSION3);
            }

            listener = SNMPTrapListenerFactory.createTrapListener(spec, handlers);

            logger.info("SNMP trap listener started successfully at Host " + host + " , on Port " + port + " ,using " + protocol + " Protocol.");

        } catch (SecurityException e) {
            throw new SNMPTrapListenerException("Unable to start the SNMP trap listener. " + e.getMessage());
        } catch (SNMPTrapListenerException e) {
            throw new SNMPTrapListenerException("Unable to start the SNMP trap listener. " + e.getMessage());
        } catch (AsmCheckedException e) {
            throw new SNMPTrapListenerException("Unable to start the SNMP trap listener. " + e.getMessage());
        } catch (IOException e) {
            throw new SNMPTrapListenerException("Unable to start the SNMP trap listener. " + e.getMessage());
        }
        return listener;
    }

    /**
     * Creates the SNMP trap handler.
     * 
     * @return ISNMPTrapHandler
     * 
     * @throws SNMPTrapListenerException
     * 
     */
    public static ISNMPTrapHandler createSNMPTrapHandler() throws SNMPTrapListenerException {

        ISNMPTrapHandler handler = null;
        SNMPTrapHandlerSpec handlerSpec = new SNMPTrapHandlerSpec();
        handlerSpec.setBrokerInstanceName(QMManager.DEFAULT_INSTANCE_NAME);
        handlerSpec.setDestinationName(SNMPTrapListnerConstants.DEST_NAME);
        handlerSpec.setDestinationType(DestinationType.TOPIC);

        try {
            // Load the properties file
            URL propFileURL = SNMPTrapFrameworkUtils.class.getClassLoader().getResource(SNMPTrapConstants.SNMPTRAP_CONSTANT_PROPERTIES_FILE);

            // Read the properties file
            Properties props = getPropertiesFromFile(propFileURL);

            // Enumerate the properties
            Enumeration e = props.propertyNames();

            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = props.getProperty(key);
                String[] values = value.split(",");

                /**
                 * Specific Trap Id - Every SNMP Trap has a specific id.
                 */
                int genericTrapId = Integer.parseInt(values[0]);
                /**
                 * Generic Trap Id - Every SNMP Trap has a generic id. This is generally constant for all the traps from a certain org.
                 */
                int specificTrapId = Integer.parseInt(values[1]);
                /**
                 * Enterprise Trap Id - Every SNMP Trap has an enterprise trap id. An enterprise id generally identifies all the traps from an
                 * organization uniquely.
                 */
                String enterpriseTrapId = values[2];

                logger.info(key + " = " + genericTrapId + "," + specificTrapId + "," + enterpriseTrapId);
                // Returns the value for OIds
                lookup.put(enterpriseTrapId, key);

                SNMPTrapDefinition def = new SNMPTrapDefinition();
                SNMPTrap trap = new SNMPTrap();
                trap.setGenericTrapId(genericTrapId);
                trap.setSpecificTrapId(specificTrapId);
                trap.setEnterpriseTrapId(enterpriseTrapId.trim());
                def.setFormatString("Message");
                def.setSnmpTrap(trap);
                def.setTrapDefinitionId("1");
                handlerSpec.getSupportedTraps().add(def);
            }

            handler = new SNMPTrapHandler(handlerSpec);

            logger.info("SNMP trap handler created successfully.");
        } catch (SecurityException e) {
            throw new SNMPTrapListenerException("Unable to create the SNMP trap handler. " + e.getMessage());
        } catch (IOException e) {
            throw new SNMPTrapListenerException("Unable to create the SNMP trap handler. " + e.getMessage());
        }
        return handler;
    }

    /**
     * Creates the Queue Manager for listening the SNMP trap. Registers the message receiver with the destination.
     * 
     * @throws SNMPTrapListenerException
     * 
     */
    public static void listenSNMPTrap() throws SNMPTrapListenerException {

        // Create the QMManager.
        logger.info("Creating Queue Manager Instance...");

        IQMManager qmgr;
        try {
            qmgr = QMManager.createInstance(QMManager.DEFAULT_LOCAL_CONFIG);
            logger.info("Registering the Queue Manager Listener...");

            // Register message receiver.
            qmgr.receiveTopicMsg(SNMPTrapListnerConstants.DEST_NAME, new TrapListener());

        } catch (QMException e) {
            throw new SNMPTrapListenerException("Unable to start the SNMP trap listener. Unable to create and register the Queue Manager instance.");
        }
    }

    /**
     * This inner class is responsible for intercepting the SNMP traps, generating the SNMP events and forwards the same for processing.
     * 
     */
    private static final class TrapListener implements MessageListener {
        @Override
        public void onMessage(Message msg) {
            if (!(msg instanceof TextMessage)) {
                String emsg = "ERROR: Unexpected error type: " + msg.getClass().getName();
                logger.debug(emsg);
                return;

            }
            // Get JMS properties.
            TextMessage textMsg = (TextMessage) msg;

            // Unmarshall.
            IQMMsg iqmMsg = null;
            try {
                iqmMsg = QMUtils.unmarshalQMMessage(textMsg);
            } catch (QMException e) {
                String emsg = "ERROR: Unable to unmarshall text message.";
                logger.debug(emsg);
                return;

            }

            if (!(iqmMsg instanceof SNMPEventMsg))
                return;

            SNMPEventMsg eventMessage = (SNMPEventMsg) iqmMsg;
            logger.debug("enterpriseTrapId - " + eventMessage.getSNMPEvent().getEnterpriseId() + " ,agentAddress - "
                    + eventMessage.getSNMPEvent().getAgentAddress() + " ,genericTrapId - " + eventMessage.getSNMPEvent().getGenericId()
                    + " ,specificTrapId - " + eventMessage.getSNMPEvent().getSpecificId());

            if (eventMessage != null) {
                // Create a new thread
                ProcessSNMPEventTask task = new ProcessSNMPEventTask(eventMessage);
                Thread th = new Thread(task);
                // Making the thread daemon so that it should stop with the application.
                th.setDaemon(true);
                th.start();
            }
        }

    }

    /**
     * It returns the key register with particular enterpriseTrapId.
     * 
     * @param enterpriseTrapId
     * 
     * @return String - the key register with enterpriseTrapId
     * 
     */
    public static String get(String enterpriseTrapId) {

        return lookup.get(enterpriseTrapId);
    }

    // Reads the properties from the properties file.
    public static Properties getPropertiesFromFile(URL propFileURL) throws IOException {
        Properties props = ConfigurationUtils.readProperties(propFileURL);
        return props;
    }

    /**
     * This inner class is responsible for intercepting the SNMP events, and processing the same.
     * 
     */
    private static final class ProcessSNMPEventTask implements Runnable {

        private SNMPEventMsg eventMessage;

        public ProcessSNMPEventTask(SNMPEventMsg eventMessage) {
            this.eventMessage = eventMessage;
        }

        @Override
        public void run() {
            // Takes action based on the SNMP event received.
            SNMPTrapEventBasedAction.performSNMPTrapBasedAction(eventMessage);
        }
    }

}
