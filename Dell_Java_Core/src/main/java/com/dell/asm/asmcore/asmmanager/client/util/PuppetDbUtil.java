/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class PuppetDbUtil
{
    private static final Logger logger = Logger.getLogger(PuppetDbUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();

    /**
     * Maps the Puppet Facts, represented as a Map&lt;String, String&gt; into a domain object representing an
     * Equallogic device.
     * 
     * @param deviceDetails the Puppet Facts used to create the PuppetEquallogicDevice
     * @return an Equallogic device from the Puppet facts or null if the deviceDetails are null or empty.
     * @throws JsonMappingException if there is an error parsing the JSON returned by puppet.
     * @throws JsonParseException if there is an error parsing the JSON returned by puppet.
     * @throws IOException if there is an error parsing the JSON return by puppet.
     */
    public static PuppetEquallogicDevice convertToPuppetEquallogicDevice(Map<String, String> deviceDetails) 
            throws JsonParseException, JsonMappingException, IOException {
        
        PuppetEquallogicDevice puppetEquallogicDevice = null;
        
        if (deviceDetails != null && !deviceDetails.isEmpty()) {
            puppetEquallogicDevice = new PuppetEquallogicDevice();
            puppetEquallogicDevice.setCertName(deviceDetails.get(PuppetEqualLogicKeys.CERTNAME));
            puppetEquallogicDevice.setSnapshots(deviceDetails.get(PuppetEqualLogicKeys.SNAPSHOTS));
            puppetEquallogicDevice.setGroupMembers(deviceDetails.get(PuppetEqualLogicKeys.SNAPSHOTS));

            HashMap<String, String> collectionsMap = (HashMap<String, String>) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEqualLogicKeys.COLLECTIONS), HashMap.class);
            PuppetEquallogicDevice.Collections collections = new PuppetEquallogicDevice.Collections();
            collections.setCustomSnapshotCollections(collectionsMap
                    .get(PuppetEqualLogicKeys.Collections.CUSTOM_SNAPSHOT_COLLECTIONS));
            collections.setSnapshotCollections(collectionsMap
                    .get(PuppetEqualLogicKeys.Collections.SNAPSHOT_COLLECTIONS));
            collections.setVolumeCollections(collectionsMap
                    .get(PuppetEqualLogicKeys.Collections.VOLUME_COLLECTIONS));
            puppetEquallogicDevice.setCollections(collections);

            puppetEquallogicDevice.setDeviceType(deviceDetails
                    .get(PuppetEqualLogicKeys.DEVICE_TYPE));
            puppetEquallogicDevice.setFreeGroupSpace(deviceDetails
                    .get(PuppetEqualLogicKeys.FREE_GROUP_SPACE));
            puppetEquallogicDevice.setFwVersion(deviceDetails.get(PuppetEqualLogicKeys.FW_VERSION));

            HashMap<String, String> generalSettingsMap = (HashMap<String, String>) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEqualLogicKeys.GENERAL_SETTINGS),
                            HashMap.class);
            PuppetEquallogicDevice.GeneralSettings generalSettings = new PuppetEquallogicDevice.GeneralSettings();
            generalSettings.setGroupName(generalSettingsMap
                    .get(PuppetEqualLogicKeys.GeneralSettings.GROUP_NAME));
            generalSettings.setIpAddress(generalSettingsMap
                    .get(PuppetEqualLogicKeys.GeneralSettings.IP_ADDRESS));
            generalSettings.setLocation(generalSettingsMap
                    .get(PuppetEqualLogicKeys.GeneralSettings.LOCATION));
            puppetEquallogicDevice.setGeneralSettings(generalSettings);

            HashMap<String, String> groupDiskSpaceMap = (HashMap<String, String>) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEqualLogicKeys.GROUP_DISK_SPACE), HashMap.class);
            PuppetEquallogicDevice.GroupDiskSpace groupDiskSpace = new PuppetEquallogicDevice.GroupDiskSpace();
            groupDiskSpace.setDelegated(groupDiskSpaceMap
                    .get(PuppetEqualLogicKeys.GroupDiskSpace.DELEGATED));
            groupDiskSpace.setFree(groupDiskSpaceMap
                    .get(PuppetEqualLogicKeys.GroupDiskSpace.FREE));
            groupDiskSpace.setReplicationReserve(groupDiskSpaceMap
                    .get(PuppetEqualLogicKeys.GroupDiskSpace.REPLICATION_RESERVE));
            groupDiskSpace.setSnapshotReserve(groupDiskSpaceMap
                    .get(PuppetEqualLogicKeys.GroupDiskSpace.SNAPSHOT_RESERVE));
            groupDiskSpace.setVolumeReserve(groupDiskSpaceMap
                    .get(PuppetEqualLogicKeys.GroupDiskSpace.VOLUME_RESERVE));
            puppetEquallogicDevice.setGroupDiskSpace(groupDiskSpace);

            puppetEquallogicDevice.setGroupName(deviceDetails.get(PuppetEqualLogicKeys.GROUP_NAME));
            puppetEquallogicDevice.setManagementIp(deviceDetails
                    .get(PuppetEqualLogicKeys.MANAGEMENT_IP));
            puppetEquallogicDevice.setMemberModel(deviceDetails
                    .get(PuppetEqualLogicKeys.MEMBER_MODEL));
            puppetEquallogicDevice.setMembers(deviceDetails.get(PuppetEqualLogicKeys.MEMBERS));
            puppetEquallogicDevice.setModel(deviceDetails.get(PuppetEqualLogicKeys.MODEL));
            puppetEquallogicDevice.setName(deviceDetails.get(PuppetEqualLogicKeys.NAME));

            HashMap<String, String> snapshotInfoMap = (HashMap<String, String>) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEqualLogicKeys.SNAPSHOTS_INFO), HashMap.class);
            PuppetEquallogicDevice.SnapshotInfo snapshotInfo = new PuppetEquallogicDevice.SnapshotInfo();
            snapshotInfo.setInUse(snapshotInfoMap
                    .get(PuppetEqualLogicKeys.Snapshots.SNAPSHOTS_INFO_IN_USE));
            snapshotInfo.setOnline(snapshotInfoMap
                    .get(PuppetEqualLogicKeys.Snapshots.SNAPSHOTS_INFO_ONLINE));
            puppetEquallogicDevice.setSnapshotInfo(snapshotInfo);

            puppetEquallogicDevice.setStatus(deviceDetails.get(PuppetEqualLogicKeys.STATUS));

            Map<String, Map<String, String>> parsedJson = OBJECT_MAPPER.readValue(deviceDetails
                    .get(PuppetEqualLogicKeys.STORAGE_POOLS), Map.class);
            if (parsedJson != null && parsedJson.keySet() != null && parsedJson.keySet().size() > 0) {
                for (String key : parsedJson.keySet()) {
                    Map<String, String> parsedValue = parsedJson.get(key);
                    if (parsedValue != null && parsedValue.keySet() != null && parsedValue.keySet().size() > 0) {
                        PuppetEquallogicDevice.StoragePool pool = new PuppetEquallogicDevice.StoragePool();
                        puppetEquallogicDevice.getStoragePools().add(pool);
                        pool.setId(key);
                        pool.setName(key);
                        pool.setSize(parsedValue.get(PuppetEqualLogicKeys.TOTAL));
                        pool.setMembers(parsedValue.get(PuppetEqualLogicKeys.MEMBERS));
                    }
                }
            }

            puppetEquallogicDevice.setUpdateTime(deviceDetails
                    .get(PuppetEqualLogicKeys.UPDATE_TIME));
            puppetEquallogicDevice.setVersion(deviceDetails.get(PuppetEqualLogicKeys.VERSION));

            ArrayList<PuppetEquallogicDevice.VolumeIqnInformation> volumeIqnInformationList = new ArrayList<PuppetEquallogicDevice.VolumeIqnInformation>();
            HashMap<String, HashMap<String, String>> volumeIqnList = (HashMap<String, HashMap<String, String>>) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEqualLogicKeys.VOLUME_IQN_INFORMATION), HashMap.class);
            for (String key : volumeIqnList.keySet()) {
                HashMap<String, String> volumeProps = (HashMap<String, String>) volumeIqnList
                        .get(key);
                PuppetEquallogicDevice.VolumeIqnInformation volumeIqnInformation = new PuppetEquallogicDevice.VolumeIqnInformation();
                volumeIqnInformation.setPermissions(volumeProps.get(PuppetEqualLogicKeys.VolumeIqnInformation.PERMISSION));
                volumeIqnInformation.setSize(volumeProps.get(PuppetEqualLogicKeys.VolumeIqnInformation.SIZE));
                volumeIqnInformation.setSnapshots(volumeProps.get(PuppetEqualLogicKeys.VolumeIqnInformation.SNAPSHOTS));
                volumeIqnInformation.setStatus(volumeProps.get(PuppetEqualLogicKeys.VolumeIqnInformation.STATUS));
                volumeIqnInformation.setTargetIscsiName(volumeProps.get(PuppetEqualLogicKeys.VolumeIqnInformation.TARGET_ISCSI_NAME));
                volumeIqnInformation.setTemplate(volumeProps.get(PuppetEqualLogicKeys.VolumeIqnInformation.TEMPLATE));
                volumeIqnInformation.setTp(volumeProps.get(PuppetEqualLogicKeys.VolumeIqnInformation.TP));
                volumeIqnInformationList.add(volumeIqnInformation);
            }
            puppetEquallogicDevice.setVolumeIqnInformations(volumeIqnInformationList);

            puppetEquallogicDevice.setVolumes(deviceDetails.get(PuppetEqualLogicKeys.VOLUMES));

            HashMap<String, String> volumesInfoMap = (HashMap<String, String>) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEqualLogicKeys.VOLUMES_INFO), HashMap.class);
            PuppetEquallogicDevice.VolumesInfo volumesInfo = new PuppetEquallogicDevice.VolumesInfo();
            volumesInfo.setInUse(volumesInfoMap
                    .get(PuppetEqualLogicKeys.VolumesInfo.IN_USE));
            volumesInfo.setIscsiConnections(volumesInfoMap
                    .get(PuppetEqualLogicKeys.VolumesInfo.ISCSI_CONNECTIONS));
            volumesInfo.setOnline(volumesInfoMap
                    .get(PuppetEqualLogicKeys.VolumesInfo.ONLINE));
            volumesInfo.setTotalVolumes(volumesInfoMap
                    .get(PuppetEqualLogicKeys.VolumesInfo.TOTAL_VOLUMES));
            puppetEquallogicDevice.setVolumesInfo(volumesInfo);

            ArrayList<PuppetEquallogicDevice.VolumeProperties> volumePropertiesList = 
                    new ArrayList<PuppetEquallogicDevice.VolumeProperties>();
            HashMap<String, String> volumePropsList = (HashMap<String, String>) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEqualLogicKeys.VOLUMES_PROPERTIES), HashMap.class);
            for (String key : volumePropsList.keySet()) {
                HashMap<String, String> volumeProps = (HashMap<String, String>) OBJECT_MAPPER
                        .readValue(volumePropsList.get(key), HashMap.class);
                PuppetEquallogicDevice.VolumeProperties volumeProperties = new PuppetEquallogicDevice.VolumeProperties();
                volumeProperties.setName(key);
                volumeProperties.setBorrowedSpace(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.BORROWED_SPACE));
                volumeProperties.setIscsiConnections(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.ISCSI_CONNECTIONS));
                volumeProperties.setNumberOfOnlineSnapshots(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.NUMBER_OF_ONLINE_SNAPSHOTS));
                volumeProperties.setNumberOfSnapshots(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.NUMBER_OF_SNSAPSHOT));
                volumeProperties.setReplicationPartner(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.REPLICATION_PARTNER));
                volumeProperties.setReportedSize(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.REPORTED_SIZE));
                volumeProperties.setSnapshotReserve(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.SNAPSHOT_RESERVE));
                volumeProperties.setSnapshotsInUse(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.SNAPSHOTS_IN_USE));
                volumeProperties.setStoragePool(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.STORAGE_POOL));
                volumeProperties.setSyncrepStatus(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.SYNCREP_STATUS));
                volumeProperties.setTargetIscsiName(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.TARGET_ISCSI_NAME));
                volumeProperties.setVolumeReserve(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.VOLUME_RESERVE));
                volumeProperties.setVolumeStatus(volumeProps
                                .get(PuppetEqualLogicKeys.VolumesProperties.VOLUME_STATUS));
                volumeProperties.setVolumeUse(volumeProps
                        .get(PuppetEqualLogicKeys.VolumesProperties.VOLUME_USE));
                volumePropertiesList.add(volumeProperties);
            }
            puppetEquallogicDevice.setVolumesProperties(volumePropertiesList);
        }

        Map<String, String> parsedJson = OBJECT_MAPPER.readValue(deviceDetails.get(PuppetEqualLogicKeys.STATUS), Map.class);
        if (parsedJson != null && parsedJson.keySet() != null && parsedJson.keySet().size() > 0) {
            for (String key : parsedJson.keySet()) {
                puppetEquallogicDevice.setStatus(parsedJson.get(key));
                break;
            }
        }

        return puppetEquallogicDevice;
    }
    
    /**
     * Maps the Puppet Facts, represented as a Map&lt;String, String&gt; into a domain object representing a
     * Compellent device.
     * 
     * @param deviceDetails the Puppet Facts used to create the PuppetCompellentDevice
     * @return a Compellent device from the Puppet facts or null if the deviceDetails is null or empty.
     * @throws JsonMappingException if there is an error parsing the JSON returned by puppet.
     * @throws JsonParseException if there is an error parsing the JSON returned by puppet.
     * @throws IOException if there is an error parsing the JSON returned by puppet.
     */
    public static PuppetCompellentDevice convertToPuppetCompellentDevice(Map<String, String> deviceDetails) 
            throws JsonParseException, JsonMappingException, IOException {
        
        PuppetCompellentDevice puppetCompellentDevice = null;

        if(deviceDetails != null && !deviceDetails.isEmpty()) {
            puppetCompellentDevice = new PuppetCompellentDevice();
            puppetCompellentDevice.setCertName(deviceDetails.get(PuppetCompellentKeys.CERT_NAME));
            puppetCompellentDevice.setDeviceType(deviceDetails.get(PuppetCompellentKeys.DEVICE_TYPE));
            puppetCompellentDevice.setModel(deviceDetails.get(PuppetCompellentKeys.MODEL));
            puppetCompellentDevice.setName(deviceDetails.get(PuppetCompellentKeys.NAME));
            puppetCompellentDevice.setSystemBackupMailServer(deviceDetails.get(PuppetCompellentKeys.SYSTEM_BACKUP_MAIL_SERVER));
            puppetCompellentDevice.setSystemMailServer(deviceDetails.get(PuppetCompellentKeys.SYSTEM_MAIL_SERVER));
            puppetCompellentDevice.setSystemManagementIp(deviceDetails.get(PuppetCompellentKeys.SYSTEM_MANAGEMENT_IP));
            puppetCompellentDevice.setSystemName(deviceDetails.get(PuppetCompellentKeys.SYSTEM_NAME));
            puppetCompellentDevice.setSystemOperationMode(deviceDetails.get(PuppetCompellentKeys.SYSTEM_OPERATION_MODE));
            puppetCompellentDevice.setSystemPortsBalanced(deviceDetails.get(PuppetCompellentKeys.SYSTEM_PORTS_BALANCED));
            puppetCompellentDevice.setSystemSerialNumber(deviceDetails.get(PuppetCompellentKeys.SYSTEM_SERIAL_NUMBER));
            puppetCompellentDevice.setSystemVersion(deviceDetails.get(PuppetCompellentKeys.SYSTEM_VERSION));
            puppetCompellentDevice.setUpdateTime(deviceDetails.get(PuppetCompellentKeys.UPDATE_TIME));

            // PROCESS system_data
            HashMap systemDataDetails = (HashMap) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetCompellentKeys.SystemData.SYSTEM_DATA), HashMap.class); 
            if(systemDataDetails != null && !systemDataDetails.isEmpty()) { 
                ArrayList<HashMap> systemDataDetailsList = (ArrayList<HashMap>) systemDataDetails.get(PuppetCompellentKeys.SystemData.SYSTEM);
                if(systemDataDetailsList != null && !systemDataDetailsList.isEmpty()) {
                    for(HashMap systemDetails : systemDataDetailsList) {
                        PuppetCompellentDevice.SystemData systemData = new PuppetCompellentDevice.SystemData();
                        systemData.setBackupMailServer(getFirstValueSafelyAsString((ArrayList)systemDetails.get(PuppetCompellentKeys.SystemData.BACKUP_MAIL_SERVER)));
                        systemData.setMailServer(getFirstValueSafelyAsString((ArrayList)systemDetails.get(PuppetCompellentKeys.SystemData.MAIL_SERVER)));
                        systemData.setManagementIp(getFirstValueSafelyAsString((ArrayList)systemDetails.get(PuppetCompellentKeys.SystemData.MANAGEMENT_IP)));
                        systemData.setName(getFirstValueSafelyAsString((ArrayList)systemDetails.get(PuppetCompellentKeys.SystemData.NAME)));
                        systemData.setOperationMode(getFirstValueSafelyAsString((ArrayList)systemDetails.get(PuppetCompellentKeys.SystemData.OPERATION_MODE)));
                        systemData.setPortsBalanced(getFirstValueSafelyAsString((ArrayList)systemDetails.get(PuppetCompellentKeys.SystemData.PORTS_BALANCED)));
                        systemData.setSerialNumber(getFirstValueSafelyAsString((ArrayList)systemDetails.get(PuppetCompellentKeys.SystemData.SERIAL_NUMBER)));
                        systemData.setVersion(getFirstValueSafelyAsString((ArrayList)systemDetails.get(PuppetCompellentKeys.SystemData.VERSION)));
                        
                        puppetCompellentDevice.setSystemData(systemData);
                    }
                }
            }
            
            // Setup List...
            // PROCESS diskfolder_data
            ArrayList<PuppetCompellentDevice.DiskFolder> diskFolders = 
                    new ArrayList<PuppetCompellentDevice.DiskFolder>();
            HashMap diskFolderDataDetails = (HashMap) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetCompellentKeys.DiskFolder.DISK_FOLDER_DATA),
                            HashMap.class);
            if(diskFolderDataDetails != null) {
                ArrayList<HashMap> diskFolderDataList = (ArrayList<HashMap>) diskFolderDataDetails.get(PuppetCompellentKeys.DiskFolder.DISK_FOLDER);
                if(diskFolderDataList != null && !diskFolderDataList.isEmpty()) {
                    for(HashMap diskFolderData : diskFolderDataList) {
                        PuppetCompellentDevice.DiskFolder diskFolder = new PuppetCompellentDevice.DiskFolder();
                        diskFolder.setAllocatedSpace(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.ALLOCATED_SPACE)));
                        diskFolder.setAllocatedSpaceBlocks(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.ALLOCATED_SPACE_BLOCKS)));
                        diskFolder.setAvailableSpaceBlocks(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.AVAILABLE_SPACE_BLOCKS)));
                        diskFolder.setIndex(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.INDEX)));
                        diskFolder.setName(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.NAME)));
                        diskFolder.setNumManaged(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.NUM_MANAGED)));
                        diskFolder.setNumSpare(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.NUM_SPARE)));
                        diskFolder.setNumStorageTypes(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.NUM_STORAGE_TYPES)));
                        diskFolder.setTotalAvailableSpace(getFirstValueSafelyAsString((ArrayList)diskFolderData.get(PuppetCompellentKeys.DiskFolder.TOTAL_AVAILABLE_SPACE)));
                        
                        ArrayList<PuppetCompellentDevice.DiskFolder.StorageType> storageTypes = new ArrayList<PuppetCompellentDevice.DiskFolder.StorageType>();
                        ArrayList<HashMap> storageTypesDataList = (ArrayList<HashMap>) diskFolderData.get(PuppetCompellentKeys.DiskFolder.STORAGE_TYPES);
                        if (storageTypesDataList != null && !storageTypesDataList.isEmpty()) {
                            ArrayList<HashMap> storageTypesList = (ArrayList<HashMap>) storageTypesDataList.get(0).get(PuppetCompellentKeys.DiskFolder.STORAGE_TYPE);
                            if (storageTypesList!= null && !storageTypesList.isEmpty()) {
                                for (HashMap storageTypeData : storageTypesList) {
                                    PuppetCompellentDevice.DiskFolder.StorageType storageType = new PuppetCompellentDevice.DiskFolder.StorageType();
                                    storageType.setIndex(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.INDEX)));
                                    storageType.setName(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.NAME)));
                                    storageType.setPageSize(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.PAGE_SIZE)));
                                    storageType.setPageSizeBlocks(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.PAGE_SIZE_BLOCKS)));
                                    storageType.setRedundancy(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.REDUNDANCY)));
                                    storageType.setSpaceAllocatedBlocks(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.SPACE_ALLOCATED_BLOCKS)));
                                    storageType.setSpaceAllocatted(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.SPACE_ALLOCATED)));
                                    storageType.setSpaceUsed(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.SPACE_USED)));
                                    storageType.setSpaceUsedBlocks(getFirstValueSafelyAsString((ArrayList)storageTypeData.get(PuppetCompellentKeys.DiskFolder.StorageType.SPACE_USED_BLOCKS)));
                                    
                                    storageTypes.add(storageType);
                                }
                            }
                        }
                        diskFolder.setStorageTypes(storageTypes);                        
                        
                        diskFolders.add(diskFolder);                        
                    }
                }
            }
            puppetCompellentDevice.setDiskFolders(diskFolders);
            
            // PROCESS replay_profile
            ArrayList<PuppetCompellentDevice.ReplayProfile> replayProfiles = 
                    new ArrayList<PuppetCompellentDevice.ReplayProfile>();
            HashMap replayProfileDataDetails = (HashMap) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetCompellentKeys.ReplayProfile.REPLAY_PROFILE_DATA), HashMap.class);
            if (replayProfileDataDetails != null) {
                ArrayList<HashMap> replayProfileList = (ArrayList<HashMap>) replayProfileDataDetails.get(PuppetCompellentKeys.ReplayProfile.REPLAY_PROFILE);
                if (replayProfileList != null && !replayProfileList.isEmpty()) {
                    for (HashMap replayProfileData : replayProfileList) {
                        PuppetCompellentDevice.ReplayProfile replayProfile = new PuppetCompellentDevice.ReplayProfile();
                        replayProfile.setIndex(getFirstValueSafelyAsString((ArrayList)replayProfileData.get(PuppetCompellentKeys.ReplayProfile.INDEX)));
                        replayProfile.setName(getFirstValueSafelyAsString((ArrayList)replayProfileData.get(PuppetCompellentKeys.ReplayProfile.NAME)));
                        replayProfile.setNumRules(getFirstValueSafelyAsString((ArrayList)replayProfileData.get(PuppetCompellentKeys.ReplayProfile.NUM_RULES)));
                        replayProfile.setNumVolumes(getFirstValueSafelyAsString((ArrayList)replayProfileData.get(PuppetCompellentKeys.ReplayProfile.NUM_VOLUMES)));
                        replayProfile.setSchedule(getFirstValueSafelyAsString((ArrayList)replayProfileData.get(PuppetCompellentKeys.ReplayProfile.SCHEDULE)));
                        replayProfile.setType(getFirstValueSafelyAsString((ArrayList)replayProfileData.get(PuppetCompellentKeys.ReplayProfile.TYPE)));

                        ArrayList<PuppetCompellentDevice.ReplayProfile.Volume> volumes = new ArrayList<PuppetCompellentDevice.ReplayProfile.Volume>();
                        ArrayList<HashMap> volumesDataList = (ArrayList<HashMap>) replayProfileData.get(PuppetCompellentKeys.ReplayProfile.VOLUMES);
                        if (volumesDataList != null && !volumesDataList.isEmpty()) {
                            ArrayList<HashMap> volumesList = (ArrayList<HashMap>) volumesDataList.get(0).get(PuppetCompellentKeys.ReplayProfile.VOLUME);
                            if (volumesList != null && !volumesList.isEmpty()) {
                                for (HashMap volumeData : volumesList) {
                                    PuppetCompellentDevice.ReplayProfile.Volume volume = new PuppetCompellentDevice.ReplayProfile.Volume();
                                    volume.setDeviceId(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.ReplayProfile.Volume.DEVICE_ID)));
                                    volume.setIndex(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.ReplayProfile.Volume.INDEX)));
                                    volume.setName(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.ReplayProfile.Volume.NAME)));
                                    volume.setSerialNumber(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.ReplayProfile.Volume.SERIAL_NUMBER)));
                                    
                                    volumes.add(volume);
                                }
                            }
                        }
                        replayProfile.setVolumes(volumes);
                        
                        replayProfiles.add(replayProfile);
                    }
                }
            }
            puppetCompellentDevice.setReplayProfiles(replayProfiles);
            
            // PROCESS servers
            ArrayList<PuppetCompellentDevice.Server> servers = new ArrayList<PuppetCompellentDevice.Server>();
            HashMap serverDataDetails = (HashMap) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetCompellentKeys.ServerData.SERVER_DATA), HashMap.class);
            if (serverDataDetails != null) {
                ArrayList<HashMap> serverList = (ArrayList<HashMap>) serverDataDetails.get(PuppetCompellentKeys.ServerData.SERVER);
                if (serverList != null && !serverList.isEmpty()) {
                    for (HashMap serverData : serverList) {
                        PuppetCompellentDevice.Server server = new PuppetCompellentDevice.Server();
                        server.setConnectionStatus(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.CONNECT_STATUS)));
                        server.setFolder(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.FOLDER)));
                        server.setFolderIndex(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.FOLDER_INDEX)));
                        server.setIndex(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.INDEX)));
                        server.setName(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.NAME)));
                        server.setOs(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.OS)));
                        server.setOsIndex(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.OS_INDEX)));
                        server.setParent(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.PARENT)));
                        server.setParentIndex(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.PARENT_INDEX)));
                        server.setTransportType(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.TRANSPORT_TYPE)));
                        server.setType(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.TYPE)));
                        server.setWwnList(getFirstValueSafelyAsString((ArrayList)serverData.get(PuppetCompellentKeys.ServerData.WWN_LIST)));
                        
                        ArrayList<PuppetCompellentDevice.Server.Mapping> mappings = new ArrayList<PuppetCompellentDevice.Server.Mapping>();
                        ArrayList<HashMap> mappingDataList = (ArrayList<HashMap>) serverData.get(PuppetCompellentKeys.ServerData.MAPPINGS);
                        if (mappingDataList != null && !mappingDataList.isEmpty()) {
                            ArrayList<HashMap> mappingList = (ArrayList<HashMap>) mappingDataList.get(0).get(PuppetCompellentKeys.ServerData.MAPPING);
                            if(mappingList != null && !mappingList.isEmpty()) {
                                for(HashMap mappingData : mappingList) {
                                    PuppetCompellentDevice.Server.Mapping mapping = new PuppetCompellentDevice.Server.Mapping();
                                    mapping.setDeviceId(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.ServerData.Mapping.DEVICE_ID)));
                                    mapping.setIndex(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.ServerData.Mapping.INDEX)));
                                    mapping.setLocalPort(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.ServerData.Mapping.LOCAL_PORT)));
                                    mapping.setLun(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.ServerData.Mapping.LUN)));
                                    mapping.setRemotePort(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.ServerData.Mapping.REMOTE_PORT)));
                                    mapping.setSerialNumber(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.ServerData.Mapping.SERIAL_NUMBER)));
                                    mapping.setVolume(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.ServerData.Mapping.VOLUME)));
                                    
                                    mappings.add(mapping);
                                }
                            }
                        }
                        server.setMappings(mappings);
                        
                        servers.add(server);
                    }
                }
            }
            puppetCompellentDevice.setServers(servers);
            
            // PROCESS storage profiles
            ArrayList<PuppetCompellentDevice.StorageProfile> storageProfiles = new ArrayList<PuppetCompellentDevice.StorageProfile>();
            HashMap storageProfileDataDetails = (HashMap) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetCompellentKeys.StorageProfile.STORAGE_PROFILE_DATA), HashMap.class);
            if (storageProfileDataDetails != null) {
                ArrayList<HashMap> storageProfileList = (ArrayList<HashMap>) storageProfileDataDetails.get(PuppetCompellentKeys.StorageProfile.STORAGE_PROFILE);
                if (storageProfileList != null && !storageProfileList.isEmpty()) {
                    for (HashMap storageProfileData : storageProfileList) {
                        PuppetCompellentDevice.StorageProfile storageProfile = new PuppetCompellentDevice.StorageProfile();
                        storageProfile.setDualHistorical(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.DUAL_HISTORICAL)));
                        storageProfile.setDualRedundantWritable(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.DUAL_REDUNDANT_WRITABLE)));
                        storageProfile.setIndex(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.INDEX)));
                        storageProfile.setName(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.NAME)));
                        storageProfile.setNonRedundantHistorical(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.NON_REDUNDANT_HISTORICAL)));
                        storageProfile.setNonRedundantWritable(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.NON_REDUNDANT_WRITABLE)));
                        storageProfile.setNumVolumes(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.NUM_VOLUMES)));
                        storageProfile.setRedundantHistorical(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.REDUNDANT_HISTORICAL)));
                        storageProfile.setRedundantWriteable(getFirstValueSafelyAsString((ArrayList)storageProfileData.get(PuppetCompellentKeys.StorageProfile.REDUNDANT_WRITABLE)));
                    
                        ArrayList<PuppetCompellentDevice.StorageProfile.Volume> volumes = new ArrayList<PuppetCompellentDevice.StorageProfile.Volume>();
                        ArrayList<HashMap> volumeDataList = (ArrayList<HashMap>) storageProfileData.get(PuppetCompellentKeys.StorageProfile.VOLUMES);
                        if (volumeDataList != null && !volumeDataList.isEmpty()) {
                            ArrayList<HashMap> volumeList = (ArrayList<HashMap>) volumeDataList.get(0).get(PuppetCompellentKeys.StorageProfile.VOLUME);
                            if (volumeList != null && !volumeList.isEmpty()) {
                                for(HashMap volumeData : volumeList) {
                                    PuppetCompellentDevice.StorageProfile.Volume volume = new PuppetCompellentDevice.StorageProfile.Volume();
                                    volume.setDeviceId(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.StorageProfile.Volume.DEVICE_ID)));
                                    volume.setIndex(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.StorageProfile.Volume.INDEX)));
                                    volume.setName(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.StorageProfile.Volume.NAME)));
                                    volume.setSerialNumber(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.StorageProfile.Volume.SERIAL_NUMBER)));
                                    
                                    volumes.add(volume);
                                }
                            }
                        }
                        storageProfile.setVolumes(volumes);
                        
                        storageProfiles.add(storageProfile);
                    }
                }
            }
            puppetCompellentDevice.setStorageProfiles(storageProfiles);
            
            // PROCESS Volumes
            ArrayList<PuppetCompellentDevice.Volume> volumes = new ArrayList<PuppetCompellentDevice.Volume>();
            HashMap volumeDataDetails = (HashMap) OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetCompellentKeys.Volume.VOLUME_DATA), HashMap.class);
            if (volumeDataDetails != null) {
                ArrayList<HashMap> volumeList = (ArrayList<HashMap>) volumeDataDetails.get(PuppetCompellentKeys.Volume.VOLUME);
                if (volumeList != null && !volumeList.isEmpty()) {
                    for (HashMap volumeData : volumeList) {
                        PuppetCompellentDevice.Volume volume = new PuppetCompellentDevice.Volume();
                        volume.setActiveSize(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.ACTIVE_SIZE)));
                        volume.setActiveSizeBlocks(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.ACTIVE_SIZE_BLOCKS)));
                        volume.setConfigSize(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.CONFIG_SIZE)));
                        volume.setConfigSizeBlocks(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.CONFIG_SIZE_BLOCKS)));
                        volume.setDeviceId(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.DEVICE_ID)));
                        volume.setFolder(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.FOLDER)));
                        volume.setIndex(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.INDEX)));
                        volume.setMaxWriteSizeBlocks(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.MAX_WRITE_SIZE_BLOCKS)));
                        volume.setName(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.NAME)));
                        volume.setReadCache(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.READ_CACHE)));
                        volume.setReplaySize(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.REPLAY_SIZE)));
                        volume.setReplaySizeBlocks(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.REPLAY_SIZE_BLOCKS)));
                        volume.setSerialNumber(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.SERIAL_NUMBER)));
                        volume.setStatus(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.STATUS)));
                        volume.setStorageProfile(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.STORAGE_PROFILE)));
                        volume.setWriteCache(getFirstValueSafelyAsString((ArrayList)volumeData.get(PuppetCompellentKeys.Volume.WRITE_CACHE)));
                        
                        ArrayList<PuppetCompellentDevice.Volume.Mapping> mappings = new ArrayList<PuppetCompellentDevice.Volume.Mapping>();
                        ArrayList<HashMap> mappingDataList = (ArrayList<HashMap>) volumeData.get(PuppetCompellentKeys.Volume.MAPPINGS);
                        if (mappingDataList != null && !mappingDataList.isEmpty()) {
                            ArrayList<HashMap> mappingList = (ArrayList<HashMap>) mappingDataList.get(0).get(PuppetCompellentKeys.Volume.MAPPINGS);
                            if (mappingList != null && !mappingList.isEmpty()) {
                                for(HashMap mappingData : mappingList) {
                                    if(!mappingData.isEmpty()) {
                                        PuppetCompellentDevice.Volume.Mapping mapping = new PuppetCompellentDevice.Volume.Mapping();
                                        mapping.setIndex(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.Volume.Mapping.INDEX)));
                                        mapping.setLocalPort(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.Volume.Mapping.LOCAL_PORT)));
                                        mapping.setLun(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.Volume.Mapping.LUN)));
                                        mapping.setRemotePort(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.Volume.Mapping.REMOTE_PORT)));
                                        mapping.setServer(getFirstValueSafelyAsString((ArrayList)mappingData.get(PuppetCompellentKeys.Volume.Mapping.SERVER)));
                                        mappings.add(mapping);
                                    }
                                }
                            }
                        }
                        volume.setMappings(mappings);
                        
                        volumes.add(volume);
                    }
                }
            }
            puppetCompellentDevice.setVolumes(volumes);
            
        }
        return puppetCompellentDevice;
        
    }

    /**
     * Maps the Puppet Facts, represented as a Map&lt;String, String&gt;, into a domain object representing an
     * Netapp device.
     * 
     * @param deviceDetails the Puppet Facts used to create the PuppetNetappDevice
     * @return a Netapp device from the Puppet facts or null if the deviceDetails are null or empty.
     * @throws JsonMappingException if there is an error parsing the JSON returned by puppet.
     * @throws JsonParseException if there is an error parsing the JSON returned by puppet.
     * @throws IOException if there is an error parsing the JSON return by puppet.
     */    
    public static PuppetNetappDevice convertToPuppetNetappDevice(Map<String, String> deviceDetails) 
            throws JsonParseException, JsonMappingException, IOException {
        
        PuppetNetappDevice puppetNetappDevice = null; 

        if(deviceDetails != null && !deviceDetails.isEmpty()) {
            puppetNetappDevice = new PuppetNetappDevice();
            puppetNetappDevice.setClientCert(deviceDetails.get(PuppetNetappKeys.CLIENT_CERT));
            puppetNetappDevice.setClientNoop(deviceDetails.get(PuppetNetappKeys.CLIENT_NOOP));
            puppetNetappDevice.setClientVersion(deviceDetails.get(PuppetNetappKeys.CLIENT_VERSION));
            puppetNetappDevice.setDomain(deviceDetails.get(PuppetNetappKeys.DOMAIN));
            puppetNetappDevice.setFqdn(deviceDetails.get(PuppetNetappKeys.FQDN));
            puppetNetappDevice.setHardwareIsa(deviceDetails.get(PuppetNetappKeys.HARDWARE_ISA));
            puppetNetappDevice.setHostName(deviceDetails.get(PuppetNetappKeys.HOST_NAME));
            puppetNetappDevice.setInterfaceIps(deviceDetails.get(PuppetNetappKeys.INTERFACE_IPS));
            puppetNetappDevice.setInterfaces(deviceDetails.get(PuppetNetappKeys.INTERFACES));
            puppetNetappDevice.setIpaddress(deviceDetails.get(PuppetNetappKeys.IPADDRESS));
            puppetNetappDevice.setIpaddressC0A(deviceDetails.get(PuppetNetappKeys.IPADDRESS_C0A));
            puppetNetappDevice.setIpaddressC0B(deviceDetails.get(PuppetNetappKeys.IPADDRESS_C0B));
            puppetNetappDevice.setIpaddressE0M(deviceDetails.get(PuppetNetappKeys.IPADDRESS_EOM));
            puppetNetappDevice.setIpaddressE0P(deviceDetails.get(PuppetNetappKeys.IPADDRESS_EOP));
            puppetNetappDevice.setIpaddressFcoeCifsNifs(deviceDetails.get(PuppetNetappKeys.IPADDRESS_FCOE_CIFS_NFS));
            puppetNetappDevice.setIpaddressOneGbVif(deviceDetails.get(PuppetNetappKeys.IPADDRESS_ONE_GB_VIF));
            puppetNetappDevice.setIsClustered(deviceDetails.get(PuppetNetappKeys.IS_CLUSTERED));
            puppetNetappDevice.setMacAddress(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS));
            puppetNetappDevice.setMacAddressC0A(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_C0A));
            puppetNetappDevice.setMacAddressC0B(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_C0B));
            puppetNetappDevice.setMacAddressE0A(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_E0A));
            puppetNetappDevice.setMacAddressE0B(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_E0B));
            puppetNetappDevice.setMacAddressE0M(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_E0M));
            puppetNetappDevice.setMacAddressE0P(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_E0P));
            puppetNetappDevice.setMacAddressE1A(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_E1A));
            puppetNetappDevice.setMacAddressE1B(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_E1B));
            puppetNetappDevice.setMacAddressFcoeCifsNfs(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_FCOE_CIFS_NFS));
            puppetNetappDevice.setMacAddressOneGbVif(deviceDetails.get(PuppetNetappKeys.MAC_ADDRESS_ONE_GB_VIF));
            puppetNetappDevice.setManufacturer(deviceDetails.get(PuppetNetappKeys.MANUFACTURER));
            puppetNetappDevice.setMemorySize(deviceDetails.get(PuppetNetappKeys.MEMORY_SIZE));
            puppetNetappDevice.setMemorySizeMb(deviceDetails.get(PuppetNetappKeys.MEMORY_SIZE_MB));
            puppetNetappDevice.setMtuC0A(deviceDetails.get(PuppetNetappKeys.MTU_C0A));
            puppetNetappDevice.setMtuC0B(deviceDetails.get(PuppetNetappKeys.MTU_C0B));
            puppetNetappDevice.setName(deviceDetails.get(PuppetNetappKeys.NAME));
            puppetNetappDevice.setNetMask(deviceDetails.get(PuppetNetappKeys.NET_MASK));
            puppetNetappDevice.setNetMaskC0A(deviceDetails.get(PuppetNetappKeys.NET_MASK_C0A));
            puppetNetappDevice.setNetMaskC0B(deviceDetails.get(PuppetNetappKeys.NET_MASK_C0B));
            puppetNetappDevice.setNetMaskE0M(deviceDetails.get(PuppetNetappKeys.NET_MASK_E0M));
            puppetNetappDevice.setNetMaskE0P(deviceDetails.get(PuppetNetappKeys.NET_MASK_E0P));
            puppetNetappDevice.setNetMaskFcoeCifsNfs(deviceDetails.get(PuppetNetappKeys.NET_MASK_FCOE_CIFS_NFS));
            puppetNetappDevice.setNetMaskOneGbVif(deviceDetails.get(PuppetNetappKeys.NET_MASK_ONE_GB_VIF));
            puppetNetappDevice.setOperatingSystem(deviceDetails.get(PuppetNetappKeys.OPERATING_SYSTEM));
            puppetNetappDevice.setOperatingSystemRelease(deviceDetails.get(PuppetNetappKeys.OPERATING_SYSTEM_RELEASE));
            puppetNetappDevice.setPartnerSerialNumber(deviceDetails.get(PuppetNetappKeys.PARTNER_SERIAL_NUMBER));
            puppetNetappDevice.setPartnerSystemId(deviceDetails.get(PuppetNetappKeys.PARTNER_SYSTEM_ID));
            puppetNetappDevice.setProcessorCount(deviceDetails.get(PuppetNetappKeys.PROCESSOR_COUNT));
            puppetNetappDevice.setProductName(deviceDetails.get(PuppetNetappKeys.PRODUCT_NAME));
            puppetNetappDevice.setSerialNumber(deviceDetails.get(PuppetNetappKeys.SERIAL_NUMBER));
            puppetNetappDevice.setSystemMachineType(deviceDetails.get(PuppetNetappKeys.SYSTEM_MACHINE_TYPE));
            puppetNetappDevice.setSystemRevision(deviceDetails.get(PuppetNetappKeys.SYSTEM_REVISION));
            puppetNetappDevice.setTotalAggregates(deviceDetails.get(PuppetNetappKeys.TOTAL_AGGREGATES));
            puppetNetappDevice.setTotalDisks(deviceDetails.get(PuppetNetappKeys.TOTAL_DISKS));
            puppetNetappDevice.setTotalLuns(deviceDetails.get(PuppetNetappKeys.TOTAL_LUNS));
            puppetNetappDevice.setTotalVolumes(deviceDetails.get(PuppetNetappKeys.TOTAL_VOLUMES));
            puppetNetappDevice.setUniqueId(deviceDetails.get(PuppetNetappKeys.UNIQUE_ID));
            puppetNetappDevice.setVersion(deviceDetails.get(PuppetNetappKeys.VERSION));
            
            // PROCESS AggregategData
            ArrayList<PuppetNetappDevice.AggregateData> aggregateDatas = new ArrayList<PuppetNetappDevice.AggregateData>();
            HashMap<String, Object> aggregateListDetails = (HashMap) OBJECT_MAPPER.readValue(deviceDetails.get(PuppetNetappKeys.AGGREGATE_DATA), HashMap.class); 
            for (String key : aggregateListDetails.keySet()) {
                HashMap aggregateDataMap =  (HashMap) OBJECT_MAPPER.readValue((String)aggregateListDetails.get(key), HashMap.class); 
                if (aggregateDataMap != null) {
                    PuppetNetappDevice.AggregateData aggregateData = new PuppetNetappDevice.AggregateData();
                    aggregateData.setDiskCount((String)aggregateDataMap.get(PuppetNetappKeys.AggregateData.DISK_COUNT));
                    aggregateData.setName((String)aggregateDataMap.get(PuppetNetappKeys.AggregateData.NAME));
                    aggregateData.setSizeAvailable((String)aggregateDataMap.get(PuppetNetappKeys.AggregateData.SIZE_AVAILABLE));
                    aggregateData.setSizePercentageUsed((String)aggregateDataMap.get(PuppetNetappKeys.AggregateData.SIZE_PERCENTAGE_USED));
                    aggregateData.setSizeTotal((String)aggregateDataMap.get(PuppetNetappKeys.AggregateData.SIZE_TOTAL));
                    aggregateData.setSizeUsed((String)aggregateDataMap.get(PuppetNetappKeys.AggregateData.SIZE_USED));
                    aggregateData.setState((String)aggregateDataMap.get(PuppetNetappKeys.AggregateData.STATE));
                    aggregateData.setVolumeCount((String)aggregateDataMap.get(PuppetNetappKeys.AggregateData.VOLUME_COUNT));

                    aggregateDatas.add(aggregateData);
                }
            }
            puppetNetappDevice.setAggregateDatas(aggregateDatas);
            
            // PROCESS DiskDatas
            ArrayList<PuppetNetappDevice.DiskData> diskDatas = new ArrayList<PuppetNetappDevice.DiskData>();
            HashMap<String, Object> diskDataListDetails = (HashMap) OBJECT_MAPPER.readValue(deviceDetails.get(PuppetNetappKeys.DISK_DATA), HashMap.class); 
            for (String key : diskDataListDetails.keySet()) {
                HashMap diskDataMap =  (HashMap) OBJECT_MAPPER.readValue((String)diskDataListDetails.get(key), HashMap.class); 
                if (diskDataMap != null) {
                    PuppetNetappDevice.DiskData diskData = new PuppetNetappDevice.DiskData();
                    diskData.setDiskModel((String)diskDataMap.get(PuppetNetappKeys.DiskData.DISK_MODEL));
                    diskData.setName((String)diskDataMap.get(PuppetNetappKeys.DiskData.NAME));
                    diskData.setSerialNumber((String)diskDataMap.get(PuppetNetappKeys.DiskData.SERIAL_NUMBER));
                    
                    diskDatas.add(diskData);
                }
            }
            puppetNetappDevice.setDiskDatas(diskDatas);
            
            // PROCESS LunDatas
            ArrayList<PuppetNetappDevice.LunData> lunDatas = new ArrayList<PuppetNetappDevice.LunData>();
            HashMap<String, Object> lunDataListDetails = (HashMap) OBJECT_MAPPER.readValue(deviceDetails.get(PuppetNetappKeys.LUN_DATA), HashMap.class); 
            for (String key : lunDataListDetails.keySet()) {
                HashMap lunDataMap =  (HashMap) OBJECT_MAPPER.readValue((String)lunDataListDetails.get(key), HashMap.class); 
                if (lunDataMap != null) {
                    PuppetNetappDevice.LunData lunData = new PuppetNetappDevice.LunData();
                    lunData.setMapped((String)lunDataMap.get(PuppetNetappKeys.LunData.MAPPED));
                    lunData.setPath((String)lunDataMap.get(PuppetNetappKeys.LunData.PATH));
                    lunData.setReadOnly((String)lunDataMap.get(PuppetNetappKeys.LunData.READ_ONLY));
                    lunData.setSize((String)lunDataMap.get(PuppetNetappKeys.LunData.SIZE));
                    lunData.setSizeUsed((String)lunDataMap.get(PuppetNetappKeys.LunData.SIZE_USED));
                    lunData.setSpaceReservedEnabled((String)lunDataMap.get(PuppetNetappKeys.LunData.SPACE_RESERVE_ENABLED));
                    lunData.setState((String)lunDataMap.get(PuppetNetappKeys.LunData.STATE));
            
                    lunDatas.add(lunData);
                }
            }
            puppetNetappDevice.setLunDatas(lunDatas);
            
            // PROCESS VolumeDatas
            ArrayList<PuppetNetappDevice.VolumeData> volumeDatas = new ArrayList<PuppetNetappDevice.VolumeData>();
            HashMap<String, Object> volumeDataListDetails = (HashMap) OBJECT_MAPPER.readValue(deviceDetails.get(PuppetNetappKeys.VOLUME_DATA), HashMap.class); 
            for (String key : volumeDataListDetails.keySet()) {
                HashMap volumeDataMap =  (HashMap) OBJECT_MAPPER.readValue((String)volumeDataListDetails.get(key), HashMap.class); 
                if (volumeDataMap != null) {
                    PuppetNetappDevice.VolumeData volumeData = new PuppetNetappDevice.VolumeData();
                    volumeData.setName((String)volumeDataMap.get(PuppetNetappKeys.VolumeData.NAME));
                    volumeData.setSizeAvailable((String)volumeDataMap.get(PuppetNetappKeys.VolumeData.SIZE_AVAILABLE));
                    volumeData.setSizeTotal((String)volumeDataMap.get(PuppetNetappKeys.VolumeData.SIZE_TOTAL));
                    volumeData.setSizeUsed((String)volumeDataMap.get(PuppetNetappKeys.VolumeData.SIZE_USED));
                    volumeData.setSpaceReserveEnabled((String)volumeDataMap.get(PuppetNetappKeys.VolumeData.SPACE_RESERVE_ENABLED));
                    volumeData.setState((String)volumeDataMap.get(PuppetNetappKeys.VolumeData.STATE));
                    volumeData.setType((String)volumeDataMap.get(PuppetNetappKeys.VolumeData.TYPE));
                    
                    volumeDatas.add(volumeData);
                }
            }
            puppetNetappDevice.setVolumeDatas(volumeDatas);
        }
        
        return puppetNetappDevice;
    }

    
    /**
     * Maps the Puppet Facts, represented as a Map&lt;String, String&gt; into a domain object representing an
     * EMC Storage device.
     * 
     * @param deviceDetails the Puppet Facts used to create the PuppetEmcDevice
     * @return an Equallogic device from the Puppet facts or null if the deviceDetails are null or empty.
     * @throws JsonMappingException if there is an error parsing the JSON returned by puppet.
     * @throws JsonParseException if there is an error parsing the JSON returned by puppet.
     * @throws IOException if there is an error parsing the JSON return by puppet.
     */
    public static PuppetEmcDevice convertToPuppetEmcDevice(Map<String, String> deviceDetails) 
            throws JsonParseException, JsonMappingException, IOException {
        
        PuppetEmcDevice puppetEmcDevice = null; 
        
        if (deviceDetails != null && !deviceDetails.isEmpty()) {
            puppetEmcDevice = new PuppetEmcDevice(); 
            puppetEmcDevice.setClassicCliState(deviceDetails.get(PuppetEmcKeys.CLASSIC_CLI_STATE));
            puppetEmcDevice.setConfiguredSystemType(deviceDetails.get(PuppetEmcKeys.CONFIGURED_SYSTEM_TYPE));
            puppetEmcDevice.setCurrentSystemType(PuppetEmcKeys.CURRENT_SYSTEM_TYPE);
            puppetEmcDevice.setEmcPartNumber(deviceDetails.get(PuppetEmcKeys.EMC_PART_NUMBER));
            puppetEmcDevice.setHighWatermark(deviceDetails.get(PuppetEmcKeys.HIGH_WATERMARK));
            puppetEmcDevice.setHwSystemType(deviceDetails.get(PuppetEmcKeys.HW_SYSTEM_TYPE));
            puppetEmcDevice.setHwType(deviceDetails.get(PuppetEmcKeys.HW_TYPE));
            puppetEmcDevice.setId(deviceDetails.get(PuppetEmcKeys.ID));
            puppetEmcDevice.setLowWatermark(deviceDetails.get(PuppetEmcKeys.LOW_WATERMARK));
            puppetEmcDevice.setModelNumber(deviceDetails.get(PuppetEmcKeys.MODEL_NUMBER));
            puppetEmcDevice.setName(deviceDetails.get(PuppetEmcKeys.NAME));
            puppetEmcDevice.setNtp(deviceDetails.get(PuppetEmcKeys.NTP));
            puppetEmcDevice.setPageSize(deviceDetails.get(PuppetEmcKeys.PAGE_SIZE));
            puppetEmcDevice.setPhysicalNode(deviceDetails.get(PuppetEmcKeys.PHYSICAL_NODE));
            puppetEmcDevice.setSerialNumber(deviceDetails.get(PuppetEmcKeys.SERIAL_NUMBER));
            puppetEmcDevice.setType(deviceDetails.get(PuppetEmcKeys.TYPE));
            puppetEmcDevice.setUuidBaseAddress(deviceDetails.get(PuppetEmcKeys.UUID_BASE_ADDRESS));
            puppetEmcDevice.setWwn(deviceDetails.get(PuppetEmcKeys.WWN));
            
            puppetEmcDevice.setFreeSpaceForFile(deviceDetails.get(PuppetEmcKeys.FREE_SPACE_FOR_FILE));
            puppetEmcDevice.setFreeStoragePoolSpace(deviceDetails.get(PuppetEmcKeys.FREE_STORAGE_POOL_SPACE));
            puppetEmcDevice.setConsumedDiskSpace(deviceDetails.get(PuppetEmcKeys.CONSUMED_DISK_SPACE));
            puppetEmcDevice.setHotSpareDisks(deviceDetails.get(PuppetEmcKeys.HOT_SPARE_DISKS));
            puppetEmcDevice.setRawDiskSpace(deviceDetails.get(PuppetEmcKeys.RAW_DISK_SPACE));
            puppetEmcDevice.setHotSpareDiskSpace(deviceDetails.get(PuppetEmcKeys.HOT_SPARE_DISK_SPACE));
            
            // Process the Controllers
            ArrayList<PuppetEmcDevice.Controller> controllers = new ArrayList<PuppetEmcDevice.Controller>();
            
            HashMap controllersDetailsMap = (HashMap)  OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEmcKeys.CONTROLLERS_DATA), HashMap.class);   
            if(controllersDetailsMap != null && !controllersDetailsMap.isEmpty()) {
                ArrayList<HashMap> controllersDetails = (ArrayList<HashMap>) controllersDetailsMap.get(PuppetEmcKeys.CONTROLLERS);
                if (controllersDetails != null && !controllersDetails.isEmpty()) {
                    for (HashMap controllerMap : controllersDetails) {
                        PuppetEmcDevice.Controller controller = new PuppetEmcDevice.Controller();
                        controller.setHbaInfo((String)controllerMap.get(PuppetEmcKeys.Controllers.HBA_INFO));
                        controller.setHostId((String)controllerMap.get(PuppetEmcKeys.Controllers.HOST_ID));
                        controller.setHostIpAddress((String)controllerMap.get(PuppetEmcKeys.Controllers.HOST_IP_ADDRESS));
                        controller.setHostName((String)controllerMap.get(PuppetEmcKeys.Controllers.HOST_NAME));
                        controller.setIfServerSupportSmartPoll((String)controllerMap.get(PuppetEmcKeys.Controllers.IF_SERVER_SUPPORT_SMART_POLL));
                        controller.setIsExpandable((String)controllerMap.get(PuppetEmcKeys.Controllers.IS_EXPANDABLE));
                        controller.setIsManaged((String)controllerMap.get(PuppetEmcKeys.Controllers.IS_MANAGED));
                        controller.setIsManuallyRegistered((String)controllerMap.get(PuppetEmcKeys.Controllers.IS_MANUALLY_REGISTERED));
                        controller.setLunUsageInfo((String)controllerMap.get(PuppetEmcKeys.Controllers.LUN_USAGE_INFO));
                        controller.setNumberOfLuns((String)controllerMap.get(PuppetEmcKeys.Controllers.NUMBER_OF_LUNS));
                        controller.setOsName((String)controllerMap.get(PuppetEmcKeys.Controllers.OS_NAME));
                        controller.setOsVersionAsString((String)controllerMap.get(PuppetEmcKeys.Controllers.OS_VERSION_AS_STRING));
                        controller.setPollType((String)controllerMap.get(PuppetEmcKeys.Controllers.POLL_TYPE));
                        controller.setWwn((String)controllerMap.get(PuppetEmcKeys.Controllers.WWN));
                        
                        controllers.add(controller);
                    }
                }
            }
            
            puppetEmcDevice.setControllers(controllers);

            // Process the DiskInfo
            ArrayList<PuppetEmcDevice.DiskInfo> diskInfos = new ArrayList<PuppetEmcDevice.DiskInfo>();
            
            HashMap diskInfoDetailsMap = (HashMap)  OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEmcKeys.DISK_INFO_DATA), HashMap.class);   
            if (diskInfoDetailsMap != null && !diskInfoDetailsMap.isEmpty()) {
                ArrayList<HashMap> diskInfoDetails = (ArrayList<HashMap>) diskInfoDetailsMap.get(PuppetEmcKeys.DISK_INFO);
                if (diskInfoDetails != null && !diskInfoDetails.isEmpty()) {
                    for (HashMap diskInfoMap : diskInfoDetails) {
                        PuppetEmcDevice.DiskInfo diskInfo = new PuppetEmcDevice.DiskInfo();
                        diskInfo.setBank((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.BANK));
                        diskInfo.setBankAsInteger((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.BANK_AS_INTEGER));
                        diskInfo.setBlocksRead((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.BLOCKS_READ));
                        diskInfo.setBocksWritten((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.BLOCKS_WRITTEN));
                        diskInfo.setBus((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.BUS));
                        diskInfo.setBusyTicks((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.BUSY_TICKS));
                        diskInfo.setCapacityInMBs((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.CAPACITY_IN_MBS));
                        diskInfo.setCurrentSpeed((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.CURRENT_SPEED));
                        diskInfo.setDiskDriveCategory((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.DISK_DRIVE_CATEGORY));
                        diskInfo.setDiskState((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.DISK_STATE));
                        diskInfo.setEnclosure((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.ENCLOSURE));
                        diskInfo.setHardReadErrors((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.HARD_READ_ERRORS));
                        diskInfo.setHardWriteErrors((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.HARD_WRITE_ERRORS));
                        diskInfo.setIdleTicks((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.IDLE_TICKS));
                        diskInfo.setIsHwSpinDownCapable((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.IS_HW_SPIN_DOWN_CAPABLE));
                        diskInfo.setIsHwSpinDownQualified((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.IS_HW_SPIN_DOWN_QUALIFIED));
                        diskInfo.setIsVaultDrive((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.IS_VALUT_DRIVE));
                        diskInfo.setMaximumSpeed((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.MAXIMUM_SPEED));
                        diskInfo.setName((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.NAME));
                        diskInfo.setNumberOfUserSectors((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.NUMBER_OF_USER_SECTORS));
                        diskInfo.setPowerSavingState((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.POWER_SAVING_STATE));
                        diskInfo.setReadRetries((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.READ_RETRIES));
                        diskInfo.setReads((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.READS));
                        diskInfo.setReplacing((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.REPLACING));
                        diskInfo.setSlot((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.SLOT));
                        diskInfo.setSoftReadErrors((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.SOFT_READ_ERRORS));
                        diskInfo.setSoftWriteErrors((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.SOFT_WRITE_ERRORS));
                        diskInfo.setState((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.STATE));
                        diskInfo.setType((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.TYPE));
                        diskInfo.setWiteRetries((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.WRITE_RETRIES));
                        diskInfo.setWrites((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.WRITES));
                        diskInfo.setZeroMark((String)diskInfoMap.get(PuppetEmcKeys.DiskInfo.ZERO_MARK));
        
                        diskInfos.add(diskInfo);
                    }
                }
            }
            
            puppetEmcDevice.setDiskInfos(diskInfos);
            
            
            // Process the DiskPool
            ArrayList<PuppetEmcDevice.DiskPool> diskPools = new ArrayList<PuppetEmcDevice.DiskPool>();
            
            HashMap diskPoolDetailsMap = (HashMap)  OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEmcKeys.DISK_POOLS_DATA), HashMap.class);   
            if (diskPoolDetailsMap != null && !diskPoolDetailsMap.isEmpty()) {
                ArrayList<HashMap> diskPoolDetails = (ArrayList<HashMap>) diskPoolDetailsMap.get(PuppetEmcKeys.DISK_POOLS);
                if (diskPoolDetails != null && !diskPoolDetails.isEmpty()) {
                    for (HashMap diskPoolMap : diskPoolDetails) {
                        PuppetEmcDevice.DiskPool diskPool = new PuppetEmcDevice.DiskPool();
                        
                        // Process Disks
                        ArrayList<PuppetEmcDevice.Disk> disks = new ArrayList<PuppetEmcDevice.Disk>();
                        ArrayList<HashMap> disksDetails = (ArrayList<HashMap>) (Object)diskPoolMap.get(PuppetEmcKeys.DiskPools.DISKS);
                        if(disksDetails != null) {
                            for (HashMap disksMap : disksDetails) {
                                PuppetEmcDevice.Disk disk = new PuppetEmcDevice.Disk();
                                disk.setBank((String)disksMap.get(PuppetEmcKeys.DiskPools.Disks.BANK));
                                disk.setBankAsInteger((String)disksMap.get(PuppetEmcKeys.DiskPools.Disks.BANK_AS_INTEGER));
                                disk.setBus((String)disksMap.get(PuppetEmcKeys.DiskPools.Disks.BUS));
                                disk.setEnclosure((String)disksMap.get(PuppetEmcKeys.DiskPools.Disks.ENCLOSURE));
                                disk.setName((String)disksMap.get(PuppetEmcKeys.DiskPools.Disks.NAME));
                                disk.setSlot((String)disksMap.get(PuppetEmcKeys.DiskPools.Disks.SLOT));
                                
                                disks.add(disk);
                            }
                        }
                        
                        diskPool.setDisks(disks);
                        diskPool.setKey((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.KEY));
                        diskPool.setName((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.NAME));
                        diskPool.setNumber((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.NUMBER));
                        diskPool.setPools((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.POOLS));
                        diskPool.setRaidGroups((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.RAID_GROUPS));
                        diskPool.setRawCapacity((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.RAW_CAPACITY));
                        diskPool.setRgFreeCapacity((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.RG_FREE_CAPACITY));
                        diskPool.setRgRawCapacity((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.RG_RAW_CAPACITY));
                        diskPool.setRgUserCapacity((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.RG_USER_CAPACITY));
                        diskPool.setUserCapacity((String)diskPoolMap.get(PuppetEmcKeys.DiskPools.USER_CAPACITY));
                        
                        diskPools.add(diskPool);
                    }
                }
            }
            
            puppetEmcDevice.setDiskPools(diskPools);
            
            
            // Process the DiskPool
            HashMap hbaInfoDetailsMap = (HashMap)  OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEmcKeys.HBA_INFO_DATA), HashMap.class);   
            if (hbaInfoDetailsMap != null && !hbaInfoDetailsMap.isEmpty()) {
                HashMap hbaInfoDetails = (HashMap) hbaInfoDetailsMap.get(PuppetEmcKeys.HBA_INFO);
                if (hbaInfoDetails != null && !hbaInfoDetails.isEmpty()) {
                    PuppetEmcDevice.HbaInfo hbaInfo = new PuppetEmcDevice.HbaInfo();
    
                    // Process Disks
                    ArrayList<PuppetEmcDevice.HbaInfo.HbaPortsInfo> hbaPortsInfos = new ArrayList<PuppetEmcDevice.HbaInfo.HbaPortsInfo>();
                    ArrayList<HashMap> hbaPortsInfoDetails = (ArrayList<HashMap>) (Object)hbaInfoDetails.get(PuppetEmcKeys.HbaInfo.HBA_PORTS_INFO);
                    if(hbaPortsInfoDetails != null) {
                        for (HashMap hbaPortInfoMap : hbaPortsInfoDetails) {
                            PuppetEmcDevice.HbaInfo.HbaPortsInfo hbaPortsInfo = new PuppetEmcDevice.HbaInfo.HbaPortsInfo();
                            hbaPortsInfo.setNumberOfSpPorts((String)hbaPortInfoMap.get(PuppetEmcKeys.HbaInfo.HbaPortsInfo.NUMBER_OF_SP_PORTS));
                            hbaPortsInfo.setVendorDescription((String)hbaPortInfoMap.get(PuppetEmcKeys.HbaInfo.HbaPortsInfo.VENDOR_DESCRIPTION));
                            hbaPortsInfo.setWwn((String)hbaPortInfoMap.get(PuppetEmcKeys.HbaInfo.HbaPortsInfo.WWN));
                            
                            hbaPortsInfos.add(hbaPortsInfo);
                        }
                    }
    
                    hbaInfo.setHbaPortsInfos(hbaPortsInfos);
    
                    hbaInfo.setHostLoginStatus((String)hbaInfoDetails.get(PuppetEmcKeys.HbaInfo.HOST_LOGIN_STATUS));
                    hbaInfo.setHostManagementStatus((String)hbaInfoDetails.get(PuppetEmcKeys.HbaInfo.HOST_MANAGEMENT_STATUS));
                    hbaInfo.setIsAttachedHost((String)hbaInfoDetails.get(PuppetEmcKeys.HbaInfo.IS_ATTACHED_HOST));
                    hbaInfo.setNumberOfHbaPorts((String)hbaInfoDetails.get(PuppetEmcKeys.HbaInfo.NUMBER_OF_HBA_PORTS));
                    puppetEmcDevice.setHbaInfo(hbaInfo);
                }
            }
             
         
            // Process the DiskPool
            ArrayList<PuppetEmcDevice.Pool> pools = new ArrayList<PuppetEmcDevice.Pool>();
            
            HashMap poolsDetailsMap = (HashMap)  OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEmcKeys.POOLS_DATA), HashMap.class);   
            if (poolsDetailsMap != null && !poolsDetailsMap.isEmpty()) {
                ArrayList<HashMap> poolsDetails = (ArrayList<HashMap>) poolsDetailsMap.get(PuppetEmcKeys.POOLS);
                if (poolsDetails != null && !poolsDetails.isEmpty()) {
                    for (HashMap poolsMap : poolsDetails) {
                        PuppetEmcDevice.Pool pool = new PuppetEmcDevice.Pool();
                        pool.setActiveOperation((String)poolsMap.get(PuppetEmcKeys.Pools.ACTIVE_OPERATION));
                        pool.setActiveOperationCompletePrcnt((String)poolsMap.get(PuppetEmcKeys.Pools.ACTIVE_OPERATION_COMPLETE_PRCNT));
                        pool.setActiveOperationState((String)poolsMap.get(PuppetEmcKeys.Pools.ACTIVE_OPERATION_STATE));
                        pool.setAllocatedCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.ALLOCATED_CAPACITY));
                        pool.setAutoTieringTierInfos((String)poolsMap.get(PuppetEmcKeys.Pools.AUTO_TIERING_TIER_INFOS));
                        pool.setCompressionHaltPrcnt((String)poolsMap.get(PuppetEmcKeys.Pools.COMPRESSION_HALT_PRCNT));
                        pool.setCompressionPausePrcnt((String)poolsMap.get(PuppetEmcKeys.Pools.COMPRESSION_PAUSE_PRCNT));
                        pool.setCompressionSavings((String)poolsMap.get(PuppetEmcKeys.Pools.COMPRESSION_SAVINGS));
                        pool.setCreationTime((String)poolsMap.get(PuppetEmcKeys.Pools.CREATION_TIME));
                        pool.setDiskPoolKeys((String)poolsMap.get(PuppetEmcKeys.Pools.DISK_POOl_KEYS));
                        pool.setEfdCacheCurrentState((String)poolsMap.get(PuppetEmcKeys.Pools.EFD_CACHE_CURRENT_STATE));
                        pool.setEfdCacheState((String)poolsMap.get(PuppetEmcKeys.Pools.EFD_CACHE_STATE));
                        pool.setId((String)poolsMap.get(PuppetEmcKeys.Pools.ID));
                        pool.setInternalLuns((String)poolsMap.get(PuppetEmcKeys.Pools.INTERNAL_LUNS));
                        pool.setIsFaulted((String)poolsMap.get(PuppetEmcKeys.Pools.IS_FAULTED));
                        pool.setKey((String)poolsMap.get(PuppetEmcKeys.Pools.KEY));
                        pool.setLibraryName((String)poolsMap.get(PuppetEmcKeys.Pools.LIBRARY_NAME));
                        pool.setMetaDataConsumedCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.META_DATA_CONSUMED_CAPACITY));
                        pool.setMetaDataSubscribedCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.META_DATA_SUBSCRIBED_CAPACITY));
                        pool.setName((String)poolsMap.get(PuppetEmcKeys.Pools.NAME));
                        pool.setPoolHarvestHighThreshold((String)poolsMap.get(PuppetEmcKeys.Pools.POOL_HARVEST_HIGH_THRESHOLD));
                        pool.setPoolHarvestingEnabled((String)poolsMap.get(PuppetEmcKeys.Pools.POOL_HARVESTING_ENABLED));
                        pool.setPoolHarvestingState((String)poolsMap.get(PuppetEmcKeys.Pools.POOL_HARVESTING_STATE));
                        pool.setPoolHarvestLowThreshold((String)poolsMap.get(PuppetEmcKeys.Pools.POOL_HARVEST_LOW_THRESHOLD));
                        pool.setPrimaryDataConsumedCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.PRIMARY_DATA_CONSUMED_CAPACITY));
                        pool.setPrimarySubscribedCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.PRIMARY_SUBSCRIBED_CAPACITY));
                        pool.setRaidGroups((String)poolsMap.get(PuppetEmcKeys.Pools.RAID_GROUPS));
                        pool.setRaidType((String)poolsMap.get(PuppetEmcKeys.Pools.RAID_TYPE));
                        pool.setRawCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.RAW_CAPACITY));
                        pool.setSecondaryDataConsumedCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.SECONDARY_DATA_CONSUMED_CAPACITY));
                        pool.setSecondarySubscribedCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.SECONDARY_SUBSCRIBED_CAPACITY));
                        pool.setSharedCapAlertLevelPrcnt((String)poolsMap.get(PuppetEmcKeys.Pools.SHARED_CAP_ALERT_LEVEL_PRCNT));
                        pool.setSnapHarvestHighThreshold((String)poolsMap.get(PuppetEmcKeys.Pools.SNAP_HARVEST_HIGH_THRESHOLD));
                        pool.setSnapHarvestingEnabled((String)poolsMap.get(PuppetEmcKeys.Pools.SNAP_HARVESTING_ENABLED));
                        pool.setSnapHarvestingState((String)poolsMap.get(PuppetEmcKeys.Pools.SNAP_HAREVESTING_STATE));
                        pool.setSnapHarvestLowThreshold((String)poolsMap.get(PuppetEmcKeys.Pools.SNAP_HARVEST_LOW_THRESHOLD));
                        pool.setState((String)poolsMap.get(PuppetEmcKeys.Pools.STATE));
                        pool.setStatus((String)poolsMap.get(PuppetEmcKeys.Pools.STATUS));
                        pool.setTotalSubscribedCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.TOTAL_SUBSCRIBED_CAPACITY));
                        pool.setUserCapacity((String)poolsMap.get(PuppetEmcKeys.Pools.USER_CAPACITY));
                        
                        // Process Mlus
                        ArrayList<PuppetEmcDevice.Pool.Mlu> mlus = new ArrayList<PuppetEmcDevice.Pool.Mlu>();
                        ArrayList<HashMap> mluDetails = (ArrayList<HashMap>) (Object)poolsMap.get(PuppetEmcKeys.Pools.MLUS);
                        if(mluDetails != null) {
                            for (HashMap mluMap : mluDetails) {
                                PuppetEmcDevice.Pool.Mlu mlu = new PuppetEmcDevice.Pool.Mlu();
                                mlu.setAlignmentOffset((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.ALIGNMENT_OFFSET));
                                mlu.setAllocationOwner((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.ALLOCATION_OWNER));
                                mlu.setAllocationPolicy((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.ALLOCATION_POLICY));
                                mlu.setAutoTieringPolicy((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.AUTO_TIERING_POLICTY));
                                mlu.setAutoTieringTierInfos((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.AUTO_TIERING_TIER_INFOS));
                                mlu.setBusyTicksSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.BUSY_TICKS_SPA));
                                mlu.setBusyTicksSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.BUSY_TICKS_SPB));
                                mlu.setCbfsFileGenerationNumber((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.CBFS_FILE_GENERATION_NUMBER));
                                mlu.setCbfsFileInodeNumber((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.CBFS_FILE_INODE_NUMBER));
                                mlu.setCbfsFileSystemId((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.CBFS_FILE_SYSTEM_ID));
                                mlu.setCompressionSavings((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.COMPRESSION_SAVINGS));
                                mlu.setCompRevId((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.COMP_REV_ID));
                                mlu.setConsumedCapacity((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.CONSUMED_CAPACITY));
                                mlu.setConsumingDriverName((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.CONSUMING_DRIVER_NAME));
                                mlu.setCreatingDriverName((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.CREATING_DRIVER_NAME));
                                mlu.setCreationTime((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.CREATION_TIME));
                                mlu.setCurrentOwner((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.CURRENT_OWNER));
                                mlu.setDataState((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.DATA_STATE));
                                mlu.setDefaultOwner((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.DEFAULT_OWNER));
                                mlu.setExplicitTrespassesSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.EXPLICIT_TRESPASSES_SPA));
                                mlu.setExplicitTrespassesSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.EXPLICIT_TRESPASSES_SPB));
                                mlu.setFileMode((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.FILE_MODE));
                                mlu.setFileObjectId((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.FILE_OBJECT_ID));
                                mlu.setFileSystemId((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.FILE_SYSTEM_ID));
                                mlu.setFileSystemObjectId((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.FILE_SYSTEM_OBJECT_ID));
                                mlu.setFirstSliceId((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.FIRST_SLICE_ID));
                                mlu.setFirstSliceLength((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.FIRST_SLICE_LENGTH));
                                mlu.setFirstSliceOffset((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.FIRST_SLICE_OFFSET));
                                mlu.setFirstSlicePosition((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.FIRST_SLICE_POSITION));
                                mlu.setHarvestPriority((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HARVEST_PRIORITY));
                                mlu.setHostBlocksReadSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HOST_BLOCKS_READ_SPA));
                                mlu.setHostBlocksReadSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HOST_BLOCKS_READ_SPB));
                                mlu.setHostBlocksWrittenSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HOST_BLOCKS_WRITTEN_SPA));
                                mlu.setHostBlocksWrittentSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HOST_BLOCKS_WRITTEN_SPB));
                                mlu.setHostReadRequestsSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HOST_READ_REQUESTS_SPA));
                                mlu.setHostReadRequestsSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HOST_READ_REQUESTS_SPB));
                                mlu.setHostWriteRequestsSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HOST_WRITE_REQUESTS_SPA));
                                mlu.setHostWriteRequestsSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.HOST_WRITE_REQUESTS_SPB));
                                mlu.setIdleTicksSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IDLE_TICKS_SPA));
                                mlu.setIdleTicksSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IDLE_TICKS_SPB));
                                mlu.setImplicitTrespassesSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IMPLICIT_TRESPASSES_SPA));
                                mlu.setImplicitTrespassesSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IMPLICIT_TRESPASSES_SPB));
                                mlu.setInternalState((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.INTERNAL_STATE));
                                mlu.setIoDisposition((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IO_DISPOSITION));
                                mlu.setIsAdvsnapAttachedAllowed((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IS_ADVSNAP_ATTACHED_ALLOWED));
                                mlu.setIsAutoAssignEnabled((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IS_AUTO_ASSIGNED_ENABLED));
                                mlu.setIsAutoTrespassEnabled((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IS_AUTO_TRESPASS_ENABLED));
                                mlu.setIsCompressed((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IS_COMPRESSED));
                                mlu.setIsFaulted((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IS_FAULTED));
                                mlu.setIsInitializing((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IS_INTIALIZING));
                                mlu.setIsInternal((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IS_INTERNAL));
                                mlu.setIsTransitioning((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.IS_TRANSITIONING));
                                mlu.setLuType((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.LU_TYPE));
                                mlu.setMetaDataConsumedCapacity((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.META_DATA_CONSUMED_CAPACITY));
                                mlu.setMetaDataSubscribedCapacity((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.META_DATA_SUBSCRIBED_CAPACITY));
                                mlu.setMluObjectId((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.MLU_OBJECT_ID));
                                mlu.setName((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.NAME));
                                mlu.setNonZeroReqCntArrivalsSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.NON_ZERO_REQ_CNT_ARRIVALS_SPA));
                                mlu.setNonZeroReqCntArrivalsSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.NON_ZERO_REQ_CNT_ARRIVALS_SPB));
                                mlu.setNumber((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.NUMBER));
                                mlu.setObjectID((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.OBJECT_ID));
                                mlu.setOperationCompletePrcnt((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.OPERATION_COMPLETE_PRCNT));
                                mlu.setOperationStatus((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.OPERATION_STATUS));
                                mlu.setOperationType((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.OPERATION_TYPE));
                                mlu.setPoolKey((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.POOL_KEY));
                                mlu.setPrimaryConsumedCapacity((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.PRIMARY_CONSUMED_CAPACITY));
                                mlu.setRaidType((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.RAID_TYPE));
                                mlu.setRecoveryState((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.RECOVERY_STATE));
                                mlu.setRootSliceID((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.ROOT_SLICE_ID));
                                mlu.setRootSliceLength((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.ROOT_SLICE_LENGTH));
                                mlu.setRootSliceOffset((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.ROOT_SLICE_OFFSET));
                                mlu.setRootSlicePosition((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.ROOT_SLICE_POSITION));
                                mlu.setSecondaryConsumedCapacity((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SECONDARY_CONSUMED_CAPACITY));
                                mlu.setSecondarySubscribedCapacity((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SECONDARY_SUBSCRIBED_CAPACITY));
                                mlu.setSecondSliceId((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SECOND_SLICE_ID));
                                mlu.setSecondSliceLength((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SECOND_SLICE_LENGTH));
                                mlu.setSecondSliceOffset((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SECOND_SLICE_OFFSET));
                                mlu.setSecondSlicePosition((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SECOND_SLICE_POSITION));
                                mlu.setSnapCount((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SNAP_COUNT));
                                mlu.setSnapLunCount((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SNAP_LUN_COUNT));
                                mlu.setState((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.STATE));
                                mlu.setSubscribedCapacity((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SUBSCRIBED_CAPACITY));
                                mlu.setSumOutstandingReqsSPA((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SUM_OUTSTANDING_REQS_SPA));
                                mlu.setSumOutstandingReqsSPB((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.SUM_OUTSTANDING_REQS_SPB));
                                mlu.setThrottleRate((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.THROTTLE_RATE));
                                mlu.setTierPlacementPreference((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.TIER_PLACEMENT_PREFERENCE));
                                mlu.setUncommittedConsumption((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.UNCOMMITTED_CONSUMPTION));
                                mlu.setUserCapacity((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.USER_CAPACITY));
                                mlu.setWwn((String)mluMap.get(PuppetEmcKeys.Pools.Mlus.WWN));
                                
                                mlus.add(mlu);
                            }
                        }
                        
                        pool.setMlus(mlus);
                        pools.add(pool);
                    }
                }
            }
             
            puppetEmcDevice.setPools(pools);
            
            
            // Process the RaidGroups
            ArrayList<PuppetEmcDevice.RaidGroup> raidGroups = new ArrayList<PuppetEmcDevice.RaidGroup>();
            
            HashMap raidGroupsDetailsMap = (HashMap)  OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEmcKeys.RAID_GROUPS_DATA), HashMap.class);   
            if (raidGroupsDetailsMap != null && !raidGroupsDetailsMap.isEmpty()) {
                ArrayList<HashMap> raidGroupsDetails = (ArrayList<HashMap>) raidGroupsDetailsMap.get(PuppetEmcKeys.RAID_GROUPS);
                if (raidGroupsDetails != null && !raidGroupsDetails.isEmpty()) {
                    for (HashMap raidGroupMap : raidGroupsDetails) {
                        PuppetEmcDevice.RaidGroup raidGroup = new PuppetEmcDevice.RaidGroup();
                        raidGroup.setCapacity((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.CAPACITY));
                        raidGroup.setElementSize((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.ELEMENT_SIZE));
                        raidGroup.setFreeSpace((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.FREE_SPACE));
                        raidGroup.setId((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.ID));
                        raidGroup.setIsPrivate((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.IS_PRIVATE));
                        raidGroup.setIsRgHardwarePowerSavingEligible((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.IS_RG_HARDWARE_POWER_SAVING_ELIGIBLE));
                        raidGroup.setIsRgInStandByState((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.IS_RG_IN_STAND_BY_STATE));
                        raidGroup.setIsRgPowerSavingEligible((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.IS_RG_POWER_SAVING_ELIGIBLE));
                        raidGroup.setLargestUnboundSegmentSize((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.LARGEST_UNBOUND_SEGMENT_SIZE));
                        raidGroup.setLegalRaidTypes((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.LEGAL_RAID_TYPES));
                        raidGroup.setLunExpansionEnabled((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.LUN_EXPANSION_ENABLED));
                        raidGroup.setMaxLuns((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.MAX_LUNS));
                        raidGroup.setOperationPriority((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.OPERATION_PRIORITY));
                        raidGroup.setPercentDefragmented((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.PERCENT_DEGRAGMENTED));
                        raidGroup.setPercentExpanded((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.PERCENT_EXPANDED));
                        raidGroup.setRawCapacityBlocks((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.RAW_CAPACITY_BLOCKS));
                        raidGroup.setRgRaidType((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.RG_RAID_TYPE));
                        raidGroup.setRgUserDefinedLatencyTolerance((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.RG_USER_DEFINED_LATENCY_TOLERANCE));
                        raidGroup.setRgUserDefinedPowerSaving((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.RG_USER_DEFINED_POWER_SAVING));
                        raidGroup.setState((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.STATE));
                        raidGroup.setType((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.TYPE));
                        raidGroup.setWillBeRemoved((String)raidGroupMap.get(PuppetEmcKeys.RaidGroups.WILL_BE_REMOVED));
                        
                        // Process Disks
                        ArrayList<PuppetEmcDevice.Disk> disks = new ArrayList<PuppetEmcDevice.Disk>();
                        ArrayList<HashMap> disksDetails = (ArrayList<HashMap>) (Object)raidGroupMap.get(PuppetEmcKeys.RaidGroups.DISKS);
                        if(disksDetails != null) {
                            for (HashMap disksMap : disksDetails) {
                                PuppetEmcDevice.Disk disk = new PuppetEmcDevice.Disk();
                                disk.setBank((String)disksMap.get(PuppetEmcKeys.RaidGroups.Disks.BANK));
                                disk.setBankAsInteger((String)disksMap.get(PuppetEmcKeys.RaidGroups.Disks.BANK_AS_INTEGER));
                                disk.setBus((String)disksMap.get(PuppetEmcKeys.RaidGroups.Disks.BUS));
                                disk.setEnclosure((String)disksMap.get(PuppetEmcKeys.RaidGroups.Disks.ENCLOSURE));
                                disk.setName((String)disksMap.get(PuppetEmcKeys.RaidGroups.Disks.NAME));
                                disk.setSlot((String)disksMap.get(PuppetEmcKeys.RaidGroups.Disks.SLOT));
                                
                                disks.add(disk);
                            }
                        }
                        raidGroup.setDisks(disks);
                        
                        raidGroups.add(raidGroup);
                    }
                }
            }

            puppetEmcDevice.setRaidGroups(raidGroups);
            
            
            // Process the Softwares
            ArrayList<PuppetEmcDevice.Software> softwares = new ArrayList<PuppetEmcDevice.Software>();
            
            HashMap softwaresDetailsMap = (HashMap)  OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEmcKeys.SOFTWARES_DATA), HashMap.class);   
            if (softwaresDetailsMap != null && !softwaresDetailsMap.isEmpty()) {
                ArrayList<HashMap> softwaresDetails = (ArrayList<HashMap>) softwaresDetailsMap.get(PuppetEmcKeys.SOFTWARES);
                if (softwaresDetails != null) {
                    for (HashMap softwareMap : softwaresDetails) {
                        PuppetEmcDevice.Software software = new PuppetEmcDevice.Software();
                        software.setCommitRequired((String)softwareMap.get(PuppetEmcKeys.Softwares.COMMIT_REQUIRED));
                        software.setDescription((String)softwareMap.get(PuppetEmcKeys.Softwares.DESCRIPTION));
                        software.setIsActive((String)softwareMap.get(PuppetEmcKeys.Softwares.IS_ACTIVE));
                        software.setIsInstallationComplete((String)softwareMap.get(PuppetEmcKeys.Softwares.IS_INSTALLATION_COMPLETE));
                        software.setIsSystemSoftare((String)softwareMap.get(PuppetEmcKeys.Softwares.IS_SYSTEM_SOFTWARE));
                        software.setName((String)softwareMap.get(PuppetEmcKeys.Softwares.NAME));
                        software.setRequiredPackages((String)softwareMap.get(PuppetEmcKeys.Softwares.REQUIRED_PACKAGES));
                        software.setRevertPossible((String)softwareMap.get(PuppetEmcKeys.Softwares.REVERT_POSSIBLE));
                        software.setRevision((String)softwareMap.get(PuppetEmcKeys.Softwares.REVISION));
                        
                        softwares.add(software);
                    }
                }
            }
            puppetEmcDevice.setSoftwares(softwares);


            if (deviceDetails.containsKey(PuppetEmcKeys.ADDONS_DATA)) {
                PuppetEmcDevice.Addons addons = new PuppetEmcDevice.Addons();

                HashMap addonsDetailsMap = (HashMap) OBJECT_MAPPER
                        .readValue(deviceDetails.get(PuppetEmcKeys.ADDONS_DATA), HashMap.class);
                if (addonsDetailsMap != null && !addonsDetailsMap.isEmpty()) {
                    ArrayList<HashMap> addonDetails = (ArrayList<HashMap>) addonsDetailsMap.get(PuppetEmcKeys.ADDONS);
                    if (addonDetails != null && !addonDetails.isEmpty()) {
                        for (HashMap addonsMap : addonDetails) {
                            if (addonsMap.containsKey(PuppetEmcKeys.Addons.COMPRESSION)) {
                                addons.setCompressionEnabled((boolean) addonsMap.get(PuppetEmcKeys.Addons.COMPRESSION));
                            } else if (addonsMap.containsKey(PuppetEmcKeys.Addons.THIN)) {
                                addons.setThinEnabled((boolean) addonsMap.get(PuppetEmcKeys.Addons.THIN));
                            } else if (addonsMap.containsKey(PuppetEmcKeys.Addons.NONTHIN)) {
                                addons.setNonthinEnabled((boolean) addonsMap.get(PuppetEmcKeys.Addons.NONTHIN));
                            } else if (addonsMap.containsKey(PuppetEmcKeys.Addons.SNAP)) {
                                addons.setSnapEnabled((boolean) addonsMap.get(PuppetEmcKeys.Addons.SNAP));
                            }
                        }
                    }
                }
                puppetEmcDevice.setAddons(addons);
            }
        }
        
        // Set the Pool AvailableSpaceInGB
        if(puppetEmcDevice.getPools() != null && !puppetEmcDevice.getPools().isEmpty()) {
            HashMap poolsListDetailsMap = (HashMap)  OBJECT_MAPPER
                    .readValue(deviceDetails.get(PuppetEmcKeys.POOL_LIST), HashMap.class);   
            if (poolsListDetailsMap != null && !poolsListDetailsMap.isEmpty()) {
//                HashMap<String, String> poolDetails = (HashMap) poolsListDetailsMap.get(PuppetEmcKeys.POOL); 
                ArrayList<HashMap> poolDetails = (ArrayList<HashMap>) poolsListDetailsMap.get(PuppetEmcKeys.POOL);
                if (poolDetails != null) {
                    for (HashMap poolMap : poolDetails) {
                        for (Object poolNameKey : poolMap.keySet()) {
                            PuppetEmcDevice.Pool pool = puppetEmcDevice.getPoolByName(((String)poolNameKey).trim());
                            if (pool != null) {
                                pool.setAvailableSpaceInGB(((Double)poolMap.get(poolNameKey)).toString());
                            }
                        }
                    }
                }
            }
        }
        
            
        return puppetEmcDevice;
    }
    
    public static PuppetIdracServerDevice convertToPuppetIdracServerDevice(ArrayList<HashMap> puppetFacts) 
            throws JsonParseException, JsonMappingException, IOException {
        PuppetIdracServerDevice puppetIdracServerDevice = null;
        
        if (puppetFacts != null && !puppetFacts.isEmpty()) {
            puppetIdracServerDevice = new PuppetIdracServerDevice();
            ArrayList<PuppetIdracServerDevice.VibsInfo> vibsInfos = new ArrayList<PuppetIdracServerDevice.VibsInfo>();
            
            for (HashMap vibsInfoMap : puppetFacts) {
                // HashMap<String, Object> vibsInfoMap = (HashMap<String, Object>) OBJECT_MAPPER.readValue(vibsInfoString, HashMap.class);
                if (vibsInfoMap != null && !vibsInfoMap.isEmpty()) {
                    PuppetIdracServerDevice.VibsInfo vibsInfo = new PuppetIdracServerDevice.VibsInfo();
                    vibsInfo.setAcceptanceLevel((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.ACCEPTANCE_LEVEL));
                    vibsInfo.setConflicts((ArrayList<String>)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.CONFLICTS)); // ArrayList
                    vibsInfo.setCreationDate((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.CREATION_DATE));
                    vibsInfo.setDepends((ArrayList<String>)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.DEPENDS));  // ArrayList
                    vibsInfo.setDescription((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.DESCRIPTION));
                    vibsInfo.setHardwarePlatformsRequired((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.HARDWARE_PLATFORMS_REQUIRED));
                    vibsInfo.setId((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.ID));
                    vibsInfo.setInstallDate((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.INSTALL_DATE));
                    vibsInfo.setLiveInstallAllowed((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.LIVE_INSTALL_ALLOWED));
                    vibsInfo.setLiveRemoveAllowed((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.LIVE_REMOVE_ALLOWED));
                    vibsInfo.setMaintenanceModeRequuired((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.MAINTENANCE_MODE_REQUIRED));
                    vibsInfo.setName((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.NAME));
                    vibsInfo.setOverlay((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.OVERLAY));
                    vibsInfo.setPayloads((ArrayList<String>)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.PAYLOADS)); // ArrayList
                    vibsInfo.setProvides((ArrayList<String>)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.PROVIDES)); // ArrayList
                    vibsInfo.setReferenceUrls((ArrayList<String>)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.REFERENCE_URLS));  // ArrayList
                    vibsInfo.setReplaces((ArrayList<String>)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.REPLACES)); // ArrayList
                    vibsInfo.setStatelessReady((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.STATELESS_READY));
                    vibsInfo.setStatus((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.STATUS));
                    vibsInfo.setSummary((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.SUMMARY));
                    vibsInfo.setTags((ArrayList<String>)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.TAGS)); // ArrayList
                    vibsInfo.setType((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.TYPE));
                    vibsInfo.setVendor((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.VENDOR));
                    vibsInfo.setVersion((String)vibsInfoMap.get(PuppetIDracServerKeys.VibsInfo.VERSION));
                    
                    vibsInfos.add(vibsInfo);
                }
            }
            
            puppetIdracServerDevice.setVibsInfos(vibsInfos);
        }
        
        return puppetIdracServerDevice;
    }

    // Used to parse and return a String from ArrayLists with only one Value that is a String
    private static String getFirstValueSafelyAsString(ArrayList strings) {
        String value = "";
        if(strings != null && !strings.isEmpty()) {
            if(strings.get(0) instanceof String) {
                value = (String)strings.get(0);
            }
        }
        return value;
    }
    
    static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        return mapper;
    }
    
    
}
