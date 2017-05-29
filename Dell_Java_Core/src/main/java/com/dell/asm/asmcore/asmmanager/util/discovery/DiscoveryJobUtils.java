package com.dell.asm.asmcore.asmmanager.util.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryResult;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceDiscoverEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.common.utilities.ValidatedInet4Address;
import com.dell.asm.common.utilities.ValidatedInet4Range;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.i18n2.exception.AsmValidationException;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.Controller;
import com.dell.pg.asm.server.client.device.FirmwareInventory;
import com.dell.pg.asm.server.client.device.Server;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.CredentialEntity;

/**
 * utilities class used by discovery job
 * 
 * @author Bapu_Patil
 * 
 */
public final class DiscoveryJobUtils {
    private DiscoveryJobUtils() {
    }

    private static final Logger logger = Logger.getLogger(DiscoveryJobUtils.class);
    private static final DiscoveryResultDAO discoveryResultDAO = DiscoveryResultDAO.getInstance();
    private static final DeviceDiscoverDAO deviceDiscoverDao = DeviceDiscoverDAO.getInstance();
    private final static String CHASSIS_COMPONENT_ID = "13793";
    private final static String FX_CHASSIS_COMPONENT_ID = "101688";
    private static final GenericDAO genericDAO = GenericDAO.getInstance();
    private static final DeviceInventoryDAO deviceInventoryDAO = new DeviceInventoryDAO();
    
    /**
     * Load Server Credential
     * 
     * @param credentialId
     *            string containing credential ID to be looked up
     * @throws WebApplicationException
     */
    public static void validateServerCredential(String credentialId, List<EEMILocalizableMessage> msgList) throws WebApplicationException {

        CredentialDAO dao = CredentialDAO.getInstance();

        CredentialEntity credential = dao.findById(credentialId);
        if (credential == null) {
            logger.debug("Invalid Credential specified. credential Id:" + credentialId);
            msgList.add(AsmManagerMessages.invalidCredentials(credentialId));

        }
    }

    /**
     * Validate the discovery request for invalid IP and credentials.
     * 
     * @param discoverIPRangeDeviceRequests
     *            list of devices
     * @throws IllegalArgumentException
     *             error
     */
    public static void validateDiscoveryRequest(
            com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests)
            throws LocalizedWebApplicationException {

        List<EEMILocalizableMessage> msgList = new ArrayList<>();
        validateForOverlappingRanges(discoverIPRangeDeviceRequests, msgList);

        for (com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest deviceInfo : discoverIPRangeDeviceRequests
                .getDiscoverIpRangeDeviceRequests()) {

            boolean bChassisCredsPresent = false;
            boolean bServerCredsPresent = false;
            boolean bVCenterCredsPresent = false;
            boolean bSwitchCredsPresent = false;
            boolean bStorageCredsPresent = false;
            boolean bScvmmCredsPresent = false;
            boolean bEMCredsPresent = false;

            // Validate credentials
            if (deviceInfo.getDeviceChassisCredRef() != null && deviceInfo.getDeviceChassisCredRef().length() > 0) 
            {
                validateServerCredential(deviceInfo.getDeviceChassisCredRef(), msgList);
                logger.info( "Chassis creds okay");
                bChassisCredsPresent = true;
            } 
            //else 
            //{
            //    msgList.add(AsmManagerMessages.missingCredentials());
            //}

            if (deviceInfo.getDeviceServerCredRef() != null && deviceInfo.getDeviceServerCredRef().length() > 0) 
            {
                validateServerCredential(deviceInfo.getDeviceServerCredRef(), msgList);
                logger.info( "Server creds okay");
                bServerCredsPresent = true;
            } 
            
            if (deviceInfo.getDeviceStorageCredRef() != null && deviceInfo.getDeviceStorageCredRef().length() > 0) 
            {
                validateServerCredential(deviceInfo.getDeviceStorageCredRef(), msgList);
                logger.info( "Storage creds okay");
                bStorageCredsPresent = true;
            } 

            if (deviceInfo.getDeviceSwitchCredRef() != null && deviceInfo.getDeviceSwitchCredRef().length() > 0) 
            {
                validateServerCredential(deviceInfo.getDeviceSwitchCredRef(), msgList);
                logger.info( "Switch creds okay");
                bSwitchCredsPresent = true;
            } 

            if (deviceInfo.getDeviceVCenterCredRef() != null && deviceInfo.getDeviceVCenterCredRef().length() > 0) 
            {
                validateServerCredential(deviceInfo.getDeviceVCenterCredRef(), msgList);
                logger.info( "vCenter creds okay");
                bVCenterCredsPresent = true;
            }

            if (deviceInfo.getDeviceSCVMMCredRef() != null && deviceInfo.getDeviceSCVMMCredRef().length() > 0)
            {
                validateServerCredential(deviceInfo.getDeviceSCVMMCredRef(), msgList);
                logger.info( "SCVMM creds okay");
                bScvmmCredsPresent = true;
            }

            if (StringUtils.isNotEmpty(deviceInfo.getDeviceEMCredRef())){
                validateServerCredential(deviceInfo.getDeviceEMCredRef(), msgList);
                bEMCredsPresent = true;
            }

            if( !bServerCredsPresent && !bChassisCredsPresent && !bStorageCredsPresent && !bSwitchCredsPresent
                    && !bVCenterCredsPresent && !bScvmmCredsPresent && !bEMCredsPresent)
            {
                msgList.add(AsmManagerMessages.missingCredentials());
            }
            
        } // for loop

        if (msgList.size() > 0) {
            logger.debug("Validate Discovery Request msgList." + msgList.size());
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, msgList);
        }
        logger.debug("Discovery request validation done.");
    }

    /**
     * validate if first 3 parts of the IP are same
     * 
     * @param ip1
     *            start ip
     * @param ip2
     *            end ip
     * @throws IllegalArgumentException
     */
    public static void validateIpSameSubnet(String ip1, String ip2, List<EEMILocalizableMessage> msgList) throws WebApplicationException {
        if ((ip1 != null && !ip1.isEmpty()) && (ip2 != null && !ip2.isEmpty())) {
            if (ip1.substring(0, ip1.lastIndexOf(".")).equalsIgnoreCase(ip2.substring(0, ip2.lastIndexOf(".")))) {
                logger.debug("Ip on same subnet ip: " + ip1 + " " + ip2);
            } else {
                logger.debug("Specified Ips NOT on the same subnet: " + ip1 + " " + ip2);
                msgList.add(AsmManagerMessages.invalidIPSubnet(ip1, ip2));
            }
        }
    }

    public static DiscoveryResultEntity toEntity(DiscoveredDevices discoveredDevices) {
        if (discoveredDevices == null) {
            return null;
        }
        DiscoveryResultEntity entity = new DiscoveryResultEntity();
        entity.setParentJobId(discoveredDevices.getParentJobId());
        entity.setJobId(discoveredDevices.getJobId());
        entity.setRefId(discoveredDevices.getRefId());
        entity.setIpaddress(discoveredDevices.getIpAddress());
        entity.setServiceTag(discoveredDevices.getServiceTag());
        entity.setDeviceType(discoveredDevices.getDeviceType());
        entity.setStatus(discoveredDevices.getStatus());
        entity.setStatusMessage(discoveredDevices.getStatusMessage());
        entity.setModel(discoveredDevices.getModel());
        entity.setIomCount(discoveredDevices.getIomCount());
        entity.setServerCount(discoveredDevices.getServerCount());
        entity.setRefType(discoveredDevices.getRefType());
        entity.setServerType(discoveredDevices.getServerType());
        entity.setDeviceRefId(discoveredDevices.getDeviceRefId());
        entity.setVendor( discoveredDevices.getVendor());
        entity.setFacts(discoveredDevices.getFacts());
        entity.setUnmanaged(discoveredDevices.isUnmanaged());
        entity.setReserved(discoveredDevices.isReserved());
        entity.setConfig(discoveredDevices.getConfiguration());
        entity.setSystem_id(discoveredDevices.getSystemId());
        entity.setDiscoverDeviceType(discoveredDevices.getDiscoverDeviceType());
        entity.setServerPoolId(discoveredDevices.getServerPoolId());
        return entity;
    }
    
    public static Set<FirmwareDeviceInventoryEntity> getFirmwareDeviceEntities(Server server, String parentJobId, String jobId) {
        Set<FirmwareDeviceInventoryEntity> currentFirmwareInventory = new HashSet<FirmwareDeviceInventoryEntity>();
        List<FirmwareInventory> firmwareList = server.getFirmwareList();
        if (firmwareList != null && !firmwareList.isEmpty()) {
            for (FirmwareInventory fwInv : firmwareList) {
              FirmwareDeviceInventoryEntity entity = new FirmwareDeviceInventoryEntity();
                // Skip firmware components which idrac will not update
                // Skip FX2 CMC for servers, we are using in chassis mode
                if (!fwInv.getUpdateable() || (fwInv.getComponentID() != null && fwInv.getComponentID().equalsIgnoreCase(FX_CHASSIS_COMPONENT_ID)))
                   continue;
                if (fwInv.getComponentID() != null)
                    entity.setComponentID(fwInv.getComponentID());
                if (fwInv.getComponentType() != null)
                    entity.setComponentType(fwInv.getComponentType());
                if (fwInv.getDeviceID() != null)
                    entity.setDeviceID(fwInv.getDeviceID());
                if (fwInv.getName() != null)
                    entity.setName(fwInv.getName());
                if (fwInv.getSubdeviceID() != null)
                    entity.setSubdeviceID(fwInv.getSubdeviceID());
                if (fwInv.getSubvendorID() != null)
                    entity.setSubvendorID(fwInv.getSubvendorID());
                if (fwInv.getVendorID() != null)
                    entity.setVendorID(fwInv.getVendorID());
                if (fwInv.getVersion() != null)
                    entity.setVersion(fwInv.getVersion());
                if (fwInv.getFqdd() != null)
                    entity.setFqdd(fwInv.getFqdd());
                if (fwInv.getLastUpdateTime() != null)
                    entity.setLastUpdateTime(fwInv.getLastUpdateTime());
                if (parentJobId != null)
                    entity.setParent_job_id(parentJobId);
                if (jobId != null)
                    entity.setJobId(jobId);
               
                entity.setIpaddress(server.getManagementIP());
                entity.setServicetag(server.getServiceTag());
                currentFirmwareInventory.add(entity);

            }
           
        }
        return currentFirmwareInventory;
    }
    
    public static Set<FirmwareDeviceInventoryEntity> firmwareChassisDeviceEntity(Chassis chassis, DiscoveredDevices result) {
        Set<FirmwareDeviceInventoryEntity> currentFirmwareInventory = new HashSet<FirmwareDeviceInventoryEntity>();
        List<Controller> firmwareList = chassis.getControllers();
        if (firmwareList != null && !firmwareList.isEmpty()) {
            for (Controller fwInv : firmwareList) {
                FirmwareDeviceInventoryEntity entity = new FirmwareDeviceInventoryEntity();
                if (result.getDiscoverDeviceType() == DiscoverDeviceType.CMC_FX2) {
                    entity.setComponentID(FX_CHASSIS_COMPONENT_ID);
                }else {
                    entity.setComponentID(CHASSIS_COMPONENT_ID);
                }
                entity.setComponentType("chassis");
                if (fwInv.getControllerName() != null)
                    entity.setName(fwInv.getControllerName());
                if (fwInv.getControllerFWVersion() != null)
                    entity.setVersion(fwInv.getControllerFWVersion());
                if (chassis.getLastFirmwareUpdateTime() != null)
                    entity.setLastUpdateTime(chassis.getLastFirmwareUpdateTime().getTime());
                if (result != null)
                {
                    entity.setParent_job_id(result.getParentJobId());
                    entity.setJobId(result.getJobId());
                }
                entity.setIpaddress(chassis.getManagementIP());
                entity.setServicetag(chassis.getServiceTag());
                currentFirmwareInventory.add(entity);

            }
           
        }
        return currentFirmwareInventory;
    }


    public static DiscoveredDevices toModel(DiscoveryResultEntity discoveryResultEntity) {
        if (discoveryResultEntity == null) {
            return null;
        }
        DiscoveredDevices model = new DiscoveredDevices();
        model.setJobId(discoveryResultEntity.getJobId());
        model.setParentJobId(discoveryResultEntity.getParentJobId());
        model.setRefId(discoveryResultEntity.getRefId());
        model.setRefType(discoveryResultEntity.getRefType());
        model.setIpAddress(discoveryResultEntity.getIpaddress());
        model.setServiceTag(discoveryResultEntity.getServiceTag());
        model.setDeviceType(discoveryResultEntity.getDeviceType());
        model.setStatus(discoveryResultEntity.getStatus());
        model.setStatusMessage(discoveryResultEntity.getStatusMessage());
        model.setModel(discoveryResultEntity.getModel());
        model.setIomCount(discoveryResultEntity.getIomCount());
        model.setServerCount(discoveryResultEntity.getServerCount());
        model.setDeviceRefId(discoveryResultEntity.getDeviceRefId());
        model.setVendor( discoveryResultEntity.getVendor());
        model.setFacts(discoveryResultEntity.getFacts());
        model.setUnmanaged(discoveryResultEntity.isUnmanaged());
        model.setReserved(discoveryResultEntity.isReserved());
        model.setConfiguration(discoveryResultEntity.getConfig());
        model.setDiscoverDeviceType(discoveryResultEntity.getDiscoverDeviceType());
        model.setServerPoolId(discoveryResultEntity.getServerPoolId());

        Set<FirmwareDeviceInventoryEntity> firmwares = discoveryResultEntity.getFirmwareList();
        if (firmwares != null)
        	for (FirmwareDeviceInventoryEntity firmware : firmwares)
        	{
        		logger.trace("Adding firmware: " + firmware.getId());
        		model.getFirmwareDeviceInventories().add(firmware.getFirmwareDeviceInventory());        		
        	}
                
        return model;
    }

    public static void updateDiscoveryResult(DiscoveredDevices discoveredDevices) {
        DiscoveryResultEntity entity = toEntity(discoveredDevices);
        try {
            discoveryResultDAO.createOrUpdateDiscoveryResult(entity);
        } catch (AsmManagerCheckedException e) {
            logger.error("updateDiscoveryResult failed to update the Database.",e);
        }

    }
    

    public static void updateFirmwareDiscoveryResult(DiscoveredDevices discoveredDevices, Set<FirmwareDeviceInventoryEntity> fwServer) {
        DiscoveryResultEntity entity = toEntity(discoveredDevices);
        try {
            // update firmware on DiscoveredDevices as well
            discoveredDevices.setFirmwareDeviceInventories(new ArrayList<FirmwareDeviceInventory>());
            Set<FirmwareDeviceInventoryEntity> fwEntities = entity.getFirmwareList();
            if (fwEntities != null) {
               for (FirmwareDeviceInventoryEntity e : fwEntities)
                   genericDAO.delete(e.getId(), FirmwareDeviceInventoryEntity.class);
               fwEntities.clear();
            }
            
            if (fwServer != null && !fwServer.isEmpty()) {
                for (FirmwareDeviceInventoryEntity fwD : fwServer) {
                    deviceInventoryDAO.createFirmwareDeviceInventory(fwD);
                    entity.addFirmwareDeviceInventoryEntity(fwD);
                    discoveredDevices.getFirmwareDeviceInventories().add(fwD.getFirmwareDeviceInventory());
                }
            }
            discoveryResultDAO.createOrUpdateDiscoveryResult(entity);

        } catch (AsmManagerCheckedException e) {
            logger.error("updateDiscoveryResult failed to update the Database.", e);
        }

    }
    
    /**
     * Expand the IP range and validate
     * 
     * @param deviceInfo
     *            DiscoverIPRangeDeviceRequest
     * @return list of ips or error
     */
    private static Set<String> expandIpAddresses(com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest deviceInfo,
            List<EEMILocalizableMessage> msgList) throws WebApplicationException {
        Set<String> ipAddresslist = new HashSet<>();

        if (deviceInfo.getDeviceStartIp() == null || deviceInfo.getDeviceStartIp().isEmpty()) {
            String msg = "invalid IP or range";
            logger.debug(msg);
            msgList.add(AsmManagerMessages.invalidIPRange(deviceInfo.getDeviceStartIp(), deviceInfo.getDeviceEndIp()));
        } else if (deviceInfo.getDeviceEndIp() == null || deviceInfo.getDeviceEndIp().isEmpty()) {
            // just add the first IP
            new ValidatedInet4Address(deviceInfo.getDeviceStartIp());
            ipAddresslist.add(deviceInfo.getDeviceStartIp());
        } else {

            // First check if IPs first 3 parts are same
            validateIpSameSubnet(deviceInfo.getDeviceStartIp(), deviceInfo.getDeviceEndIp(), msgList);

            // we have the range specified
            try {
                ValidatedInet4Range validatedRange = new ValidatedInet4Range(deviceInfo.getDeviceStartIp(), deviceInfo.getDeviceEndIp());
                List<String> addressStrings = validatedRange.getAddressStrings();
                for (String address : addressStrings) {
                    ipAddresslist.add(address);
                }
            } catch (AsmValidationException e) {
                msgList.add(e.getEEMILocalizableMessage());
            }
        }

        return ipAddresslist;
    }

    public static void validateForOverlappingRanges(
            com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests discoverIPRangeDeviceRequests,
            List<EEMILocalizableMessage> msgList) {

        Set<String> ipAddresslist = new HashSet<String>();

        for (com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest deviceInfo : discoverIPRangeDeviceRequests
                .getDiscoverIpRangeDeviceRequests()) {

            Set<String> ipAddressesIntheRange = expandIpAddresses(deviceInfo, msgList);
            for (String ipAddress : ipAddressesIntheRange) {
                if (ipAddresslist.contains(ipAddress)) {

                    msgList.add(AsmManagerMessages.ipRangeIsOverlapping(deviceInfo.getDeviceStartIp(), deviceInfo.getDeviceEndIp()));
                    break;

                } else {

                    ipAddresslist.add(ipAddress);
                }
            }

        }
    }

    public static boolean validateDeviceDiscoverRequest(String id) throws WebApplicationException {
        if (id == null) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(id));
        }
        DeviceDiscoverEntity deviceDiscoverEntity = deviceDiscoverDao.getDeviceDiscoverEntityById(id);

        if (deviceDiscoverEntity == null) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(id));
        }

        // may need to add more validation
        return true;
    }

    public static DiscoveryResult getDiscoveryResult(String id) {
        List<DiscoveryResultEntity> entities = getDiscoveryResultEntities(id);
        if (entities!=null && entities.size()>0)
            return entities.get(0).getDiscoveryResult();
        return null;
    }

    public static List<DiscoveryResult> getDiscoveryResults(String id) {
        List<DiscoveryResult> results = new ArrayList<>();
        List<DiscoveryResultEntity> entities = getDiscoveryResultEntities(id);
        if (entities!=null && entities.size()>0) {
            for (DiscoveryResultEntity entity: entities) {
                results.add(entity.getDiscoveryResult());
            }
        }
        return results;
    }

    public static List<DiscoveryResultEntity> getDiscoveryResultEntities(String id) {
        GenericDAO genericDAO = GenericDAO.getInstance();
        logger.debug("Getting discovery result for device refId: " + id);

        //No longer enforcing name uniqueness
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("deviceRefId", id);

        return genericDAO.getForEquals(attributes, DiscoveryResultEntity.class);
    }

    public static DiscoveryResultEntity getDiscoveryResultEntity(String id) {
        List<DiscoveryResultEntity> results = getDiscoveryResultEntities(id);
        if (results != null && results.size() > 0)
            return results.get(0);

        return null;
    }

}
