/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.snmp.providers;

import org.apache.log4j.Logger;

import com.dell.pg.asm.chassis.client.ClientUtils;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.jraf.api.discovery.DiscoveryResult;
import com.dell.pg.jraf.api.ref.IDeviceRef;
import com.dell.pg.jraf.api.ref.IDiscoveryRequestRef;
import com.dell.pg.jraf.api.spi.IDiscoveryProvider;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.CredentialEntity;

/**
 * Implementation of a chassis discovery provider. Supports discovering a chassis device given a single IP address or a range of IP addresses.
 */
public class SNMPEventBasedDiscoveryProvider implements IDiscoveryProvider {

    private static final Logger _logger = Logger.getLogger(SNMPEventBasedDiscoveryProvider.class);

    // Singleton instance.
    private static final IDiscoveryProvider _instance = new SNMPEventBasedDiscoveryProvider();
    //private static final RacadmIPDiscoveryReqDAO discoveryRequestDAO = RacadmIPDiscoveryReqDAO.getInstance();

    /**
     * Cannot instantiate DiscoveryProvider directly
     */
    private SNMPEventBasedDiscoveryProvider() {
    }

    /**
     * Get singleton instance
     * 
     * @return instance of IComplianceProvider
     */
    public static synchronized IDiscoveryProvider getInstance() {
        return _instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dell.pg.jraf.api.names.IDeviceType#getDeviceType()
     */
    @Override
    public String getDeviceType() {
        return ClientUtils.DEVICE_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dell.pg.jraf.api.spi.IDiscoveryProvider#discoverDevices(com.dell.pg.jraf.api.ref.IDiscoveryRequestRef)
     */
    @Override
    public DiscoveryResult discoverDevices(IDiscoveryRequestRef ref) {
        DiscoveryResult discoveryResult = new DiscoveryResult();
/*
        // Validate input data
        if (ref == null || StringUtils.isBlank(ref.getRefId()) || StringUtils.isBlank(ref.getRefType())) {
            throw logErrorAndBuildException("Incomplete discovery request reference " + ref);
        }

        if (VcenterUtils.DISCOVERY_IP_REF_TYPE.equalsIgnoreCase(ref.getRefType())) {

            // Fetch discovery request entity
            RacadmIPDiscoveryReqEntity discoveryReqEntity = discoveryRequestDAO.getDiscoveryRequest(ref.getRefId());
            if (discoveryReqEntity == null) {

                logErrorAndBuildException("Cannot find RacadmIPDiscoveryReqEntity for reference " + ref.getRefId());

            } else {

                // Fetch specified credential entity
                CredentialEntity credential = this.getCredential(discoveryReqEntity.getCredentialId());

                // Fetch inventory details for the chassis
                IDeviceRef device = this.discoverByIPAddress(discoveryReqEntity.getIpAddress(), credential, false);
                discoveryResult.addDeviceRef(discoveryReqEntity.getIpAddress(), device);
            }

        } else {
            logErrorAndBuildException("Discovery request with ref id '" + ref.getRefId() + "' isn't type Chassis (actual type: '" + ref.getRefType()
                    + "')");
        } */
        return discoveryResult;
    }

    /**
     * Returns the credentials.
     * 
     * @param credentialId
     * 
     * @return CredentialEntity
     */
    
    private CredentialEntity getCredential(String credentialId) {

        CredentialEntity credential = CredentialDAO.getInstance().findById(credentialId);
/*        
        if (credential == null) {
            throw logErrorAndBuildException("Credential not found with credentialId " + credentialId + " could not be found.");
        } else if (!credential.getType().equalsIgnoreCase(VcenterUtils.DEVICE_TYPE)) {
            throw logErrorAndBuildException("Credential with credentialId " + credentialId + " isn't type Chassis (actual type : '"
                    + credential.getType() + "'");
        }
*/
        return credential;

    }

    /**
     * Discover chassis device using its ip address
     * 
     * @param ipAddress
     *            - the ip address of chassis
     * @param credential
     *            - the credentials of chassis
     * @param checkCertificate
     *            - check certificate or not
     * 
     * @return IDeviceRef - reference to discovered chassis device
     */
    private IDeviceRef discoverByIPAddress(String ipAddress, CredentialEntity credential, boolean checkCertificate) {

        Chassis chassisDevice = null;
/*
        try {
            // TODO: Replace the last parameter on the previous call with line below credential.getCheckCertificate());
            ChassisEntity chassisEntity = ChassisInventoryAction.getInstance().getChassisInventory(ipAddress, credential, checkCertificate);

            if (chassisEntity != null)
                chassisDevice = ChassisDeviceUtils.toChassisDevice(chassisEntity);

        } catch (InventoryException ie) {
            throw new DiscoveryException("Exception during discovery: " + ie.getMessage(), ie);
        }
*/
        return chassisDevice;

    }

    /**
     * Log an error message and return an exception
     * 
     * @param msg
     *            - message to log
     * 
     * @return DiscoveryException - an appropriate discovery exception
     */
/*    
    private DiscoveryException logErrorAndBuildException(String msg) {
        _logger.error(msg);
        return new DiscoveryException(msg);
    }
*/
}