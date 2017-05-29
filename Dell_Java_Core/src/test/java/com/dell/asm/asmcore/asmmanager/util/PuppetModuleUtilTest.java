/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.util.PuppetCompellentDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetCompellentDevice.DiskFolder;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetCompellentDevice.ReplayProfile;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetCompellentDevice.Server;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetCompellentDevice.StorageProfile;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetCompellentDevice.Volume;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetDbUtil;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEmcDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetEquallogicDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetIdracServerDevice;
import com.dell.asm.asmcore.asmmanager.client.util.PuppetNetappDevice;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Charsets;

/**
 * 
 */
public class PuppetModuleUtilTest {
    
    private static final Logger logger = Logger.getLogger(PuppetDbUtil.class);

    @Test
    public void convertToPuppetEquallogicDevice() throws Exception {
        String path = "util/equallogic.json";
        URL resource = PuppetModuleUtilTest.class.getClassLoader().getResource(path);
        assertNotNull("Failed to load resource at " + path, resource);
        String text = IOUtils.toString(resource, Charsets.UTF_8);

        ObjectMapper objectMapper = this.buildObjectMapper();
        HashMap equallogicJson = objectMapper.readValue(text, HashMap.class);
        HashMap<String, String> deviceDetails = (HashMap<String, String>) equallogicJson.get("facts");
        
        PuppetEquallogicDevice puppetEquallogicDevice = PuppetDbUtil.convertToPuppetEquallogicDevice(deviceDetails);
        assertNotNull("PuppetEquallogicDevice cannot be null!", puppetEquallogicDevice);
        assertNotNull("certname cannot be null!", puppetEquallogicDevice.getCertName());
        
        assertEquals("certname should match!", puppetEquallogicDevice.getCertName(), "equallogic-172.17.4.10");
        assertEquals("deviceType should match!", puppetEquallogicDevice.getDeviceType(), "script");
        assertEquals("update_time should match!", puppetEquallogicDevice.getUpdateTime(), "2016-01-07 00:00:13 +0000");
        assertEquals("Group Name should match!", puppetEquallogicDevice.getGroupName(), "AS800-Env04");
        assertEquals("Management IP should match!", puppetEquallogicDevice.getManagementIp(), "172.17.4.10");
        assertEquals("Volumes should match!", puppetEquallogicDevice.getVolumes(), "16");
        assertEquals("Snapshots should match!", puppetEquallogicDevice.getSnapshots(), "0");
        
        assertNotNull("GeneralSettings cannot be null!", puppetEquallogicDevice.getGeneralSettings());
        assertNotNull("StoragePools cannot be null!", puppetEquallogicDevice.getStoragePools());
        assertNotNull("Collections cannot be null!", puppetEquallogicDevice.getCollections());
        assertNotNull("Group Disk Space cannot be null!", puppetEquallogicDevice.getGroupDiskSpace());
        assertNotNull("Volume Properties cannot be null!", puppetEquallogicDevice.getVolumesProperties());
        assertNotNull("VolumeInfo cannot be null!", puppetEquallogicDevice.getVolumesInfo());
        
        assertEquals("GeneralSettings groupName should match!", puppetEquallogicDevice.getGeneralSettings().getGroupName(), "AS800-Env04");
        assertEquals("GeneralSettings IP Address should match!", puppetEquallogicDevice.getGeneralSettings().getIpAddress(), "172.16.4.10");
        assertEquals("GeneralSettings Location should match!", puppetEquallogicDevice.getGeneralSettings().getLocation(), "default");
    }
    
    @Test
    public void convertToPuppetCompellentDevice() throws Exception {
        String path = "util/compellent.json";
        URL resource = PuppetModuleUtilTest.class.getClassLoader().getResource(path);
        assertNotNull("Failed to load resource at " + path, resource);
        String text = IOUtils.toString(resource, Charsets.UTF_8);

        ObjectMapper objectMapper = this.buildObjectMapper();
        HashMap compellentJson = objectMapper.readValue(text, HashMap.class);
        HashMap<String, String> deviceDetails = (HashMap<String, String>) compellentJson.get("facts");
        
        PuppetCompellentDevice puppetCompellentDevice = PuppetDbUtil.convertToPuppetCompellentDevice(deviceDetails);
        assertNotNull("PuppetCompellentDevice cannot be null!", puppetCompellentDevice);
        assertNotNull("SystemData cannot be null!", puppetCompellentDevice.getSystemData());
        
        assertEquals("Certname should be equal!", puppetCompellentDevice.getCertName(), "compellent-172.17.9.55");
        assertEquals("System Data Serial numbers must match!", puppetCompellentDevice.getSystemData().getSerialNumber(), "21851");
        assertEquals("System Data Names must match!", puppetCompellentDevice.getSystemData().getName(), "SC8000-9-50");
        assertEquals("System Data ManagementIps must match!", puppetCompellentDevice.getSystemData().getManagementIp(), "172.17.9.55");
        assertEquals("System Data Versions must match!", puppetCompellentDevice.getSystemData().getVersion(), "6.6.4.6");
        assertEquals("System Data OperationModes must match!", puppetCompellentDevice.getSystemData().getOperationMode(), "Normal");
        assertEquals("System Data PortBalanceds must match!", puppetCompellentDevice.getSystemData().getPortsBalanced(), "Yes");
        
        assertNotNull("DiskFolders cannot be null!", puppetCompellentDevice.getDiskFolders());
        assertNotNull("Volumes cannot be null!", puppetCompellentDevice.getVolumes());
        assertNotNull("Servers cannot be null!", puppetCompellentDevice.getServers());
        assertNotNull("ReplayProfiles cannot be null!", puppetCompellentDevice.getReplayProfiles());
        assertNotNull("StorageProfiles cannot be null!", puppetCompellentDevice.getStorageProfiles());

        assertTrue("DiskFolders cannot be empty!", puppetCompellentDevice.getDiskFolders().size() > 0);
        assertTrue("Volumes cannot be empty!", puppetCompellentDevice.getVolumes().size() > 0);
        assertTrue("Servers cannot be empty!", puppetCompellentDevice.getServers().size() > 0);
        assertTrue("ReplayProfiles cannot be empty!", puppetCompellentDevice.getReplayProfiles().size() > 0);
        assertTrue("StorageProfiles cannot be empty!", puppetCompellentDevice.getStorageProfiles().size() > 0);
        
        for (DiskFolder diskFolder : puppetCompellentDevice.getDiskFolders()) {
            assertNotNull("DiskFolder StorageTypes cannot be null!", diskFolder.getStorageTypes());
        }
        
        for (Volume volume : puppetCompellentDevice.getVolumes()) {
            assertNotNull("Volume Mappings cannot be null!", volume.getMappings());
        }
        
        // Lets make sure we can find the Local Port...
        String localPortToFind = "5000d31000555b55";
        boolean localPortFound = false;
        for (Server server : puppetCompellentDevice.getServers()) {
            assertNotNull("Server Mappings cannot be null!", server.getMappings());
            for (Server.Mapping mapping : server.getMappings()) {
                if(localPortToFind.equalsIgnoreCase(mapping.getLocalPort())) {
                    localPortFound = true;
                }
            }
        }
        assertTrue("LocalPort was not found!", localPortFound);
        
        for (ReplayProfile replayProfile : puppetCompellentDevice.getReplayProfiles()) {
            assertNotNull("ReplayProfile Volumes cannot be null!", replayProfile.getVolumes());
        }
        
        for (StorageProfile storageProfile : puppetCompellentDevice.getStorageProfiles()) {
            assertNotNull("StorageProfile Volumes cannot be null!", storageProfile.getVolumes());
        }
        
    }
    
    @Test
    public void convertToPuppetNetappDevice() throws Exception {
        String path = "util/netapp-172.17.7.70.json";
        URL resource = PuppetModuleUtilTest.class.getClassLoader().getResource(path);
        assertNotNull("Failed to load resource at " + path, resource);
        String text = IOUtils.toString(resource, Charsets.UTF_8);

        ObjectMapper objectMapper = this.buildObjectMapper();
        HashMap netappJson = objectMapper.readValue(text, HashMap.class);
        HashMap<String, String> deviceDetails = (HashMap<String, String>) netappJson.get("facts");
        
        PuppetNetappDevice puppetNetappDevice = PuppetDbUtil.convertToPuppetNetappDevice(deviceDetails);
        assertNotNull("PuppetNetappDevice cannot be null!", puppetNetappDevice);
        
        assertEquals("clientCert should be netapp-172.17.7.70!", "netapp-172.17.7.70", puppetNetappDevice.getClientCert());
        assertEquals("domain should be netapp01!", "netapp01", puppetNetappDevice.getDomain());
        assertEquals("fqdn should be adc-netapp01!", "adc-netapp01", puppetNetappDevice.getFqdn());
        assertEquals("hardwareisa should be Intel(R) Xeon(R) CPU           E5220  @ 2.33GHz!", "Intel(R) Xeon(R) CPU           E5220  @ 2.33GHz", puppetNetappDevice.getHardwareIsa());
        assertEquals("hostname should be adc-netapp01", "adc-netapp01", puppetNetappDevice.getHostName());
        assertEquals("interfaceIps should be 192.168.1.132,192.168.2.10,172.17.7.70,192.168.1.232,172.20.7.70,172.18.7.77!", "192.168.1.132,192.168.2.10,172.17.7.70,192.168.1.232,172.20.7.70,172.18.7.77", puppetNetappDevice.getInterfaceIps());
        assertEquals("ipaddress should be 172.17.7.70!", "172.17.7.70", puppetNetappDevice.getIpaddress());
        assertEquals("isClustered should be false!", "false", puppetNetappDevice.getIsClustered());

        assertNotNull("aggregateDatas list should not be null!", puppetNetappDevice.getAggregateDatas());
        assertNotNull("diskDatas list should not be null!", puppetNetappDevice.getDiskDatas());
        assertNotNull("lunDatas list should not be null!", puppetNetappDevice.getLunDatas());
        assertNotNull("volumeDatas list should not be null!", puppetNetappDevice.getVolumeDatas());
        
        assertTrue("aggregateDatas should have 1 or more items in the list!", puppetNetappDevice.getAggregateDatas().size() > 0);
        assertTrue("diskDatas should have 1 or more items in the list!", puppetNetappDevice.getDiskDatas().size() > 0);
        assertTrue("lunDatas should have 1 or more items in the list!", puppetNetappDevice.getLunDatas().size() > 0);
        assertTrue("volumeDatas should have 1 or more items in the list!", puppetNetappDevice.getVolumeDatas().size() > 0);
        
        boolean aggr1Found = false;
        for (PuppetNetappDevice.AggregateData aggregateData : puppetNetappDevice.getAggregateDatas()) {
            if("aggr1".equals(aggregateData.getName())) {
                aggr1Found = true;
                break;
            }
        }
        assertTrue("aggr1 name not found!", aggr1Found);
        
        boolean a23Found = false;
        for (PuppetNetappDevice.DiskData diskData : puppetNetappDevice.getDiskDatas()) {
            if("0a.00.23".equals(diskData.getName())) {
                a23Found = true;
                break;
            }
        }
        assertTrue("0a.00.23 name not found!", a23Found);
        
        boolean lunPathFound = false;
        for(PuppetNetappDevice.LunData lunData : puppetNetappDevice.getLunDatas()) {
            if("/vol/SMD_ESXi_LUN01_vol/SMD_ESXi_LUN01".equals(lunData.getPath())) {
                lunPathFound = true;
                break;
            }
        }
        assertTrue("/vol/SMD_ESXi_LUN01_vol/SMD_ESXi_LUN01 lun path not found!", lunPathFound);
        
        boolean volumeNameFound = false;
        for (PuppetNetappDevice.VolumeData volumeData : puppetNetappDevice.getVolumeDatas()) {
            if("CIFSVolume1".equals(volumeData.getName())) {
                volumeNameFound = true;
                break;
            }
        }
        assertTrue("CIFSVolume1 volume name not found!", volumeNameFound);
    }
    
    
    @Test
    public void convertToPuppetEmcDevice() throws Exception {
        String path = "util/emc-vnx-172.17.7.83.json";
        URL resource = PuppetModuleUtilTest.class.getClassLoader().getResource(path);
        assertNotNull("Failed to load resource at " + path, resource);
        String text = IOUtils.toString(resource, Charsets.UTF_8);

        ObjectMapper objectMapper = this.buildObjectMapper();
        HashMap<String, String> deviceDetails = objectMapper.readValue(text, HashMap.class);

        PuppetEmcDevice puppetEmcDevice = PuppetDbUtil.convertToPuppetEmcDevice(deviceDetails);
        assertNotNull("PuppetEmcDevice cannot be null!", puppetEmcDevice);
        assertNotNull("PuppetEmcDevice.Controllers cannot be null!", puppetEmcDevice.getControllers());
        assertNotNull("PuppetEmcDevice.DiskInfos cannot be null!", puppetEmcDevice.getDiskInfos());
        assertNotNull("PuppetEmcDevice.DiskPools cannot be null!", puppetEmcDevice.getDiskPools());
        assertNotNull("PuppetEmcDevice.HbaInfos cannot be null!", puppetEmcDevice.getHbaInfo());
        assertNotNull("PuppetEmcDevice.HbaInfos.HbaPortsInfos cannot be null!", puppetEmcDevice.getHbaInfo().getHbaPortsInfos());        
        assertNotNull("PuppetEmcDevice.Pools cannot be null!", puppetEmcDevice.getPools());        
        assertNotNull("PuppetEmcDevice.RaidGroups cannot be null!", puppetEmcDevice.getRaidGroups());
        assertNotNull("PuppetEmcDevice.Softwares cannot be null!", puppetEmcDevice.getSoftwares());        
        
        int numOfLuns = 0;
        for (PuppetEmcDevice.Pool pool : puppetEmcDevice.getPools()) {
            
            double allocatedCapacityInGBs = Double.parseDouble(pool.getAllocatedCapacity()) / 1024 / 1024 / 2 ;
            double rawCapacityInGBs = Double.parseDouble(pool.getRawCapacity()) / 1024 / 1024 / 2;
            double userCapacityInGBs = Double.parseDouble(pool.getUserCapacity()) / 1024 / 1024 / 2;
            
            if (logger.isDebugEnabled()) {
                
                for (PuppetEmcDevice.DiskInfo diskInfo : puppetEmcDevice.getDiskInfos()) {
                    System.out.println("DiskInfo Name: " + diskInfo.getName());
                    System.out.println("CapacityInMBs: " + diskInfo.getCapacityInMBs());
                    System.out.println("\n\n\n");
                }
           
                for (PuppetEmcDevice.DiskPool diskPool : puppetEmcDevice.getDiskPools()) {
                    System.out.println("DiskPoolName: " + diskPool.getName());
                    System.out.println("Raw Capacity: " + diskPool.getRawCapacity());
                    System.out.println("User Capacity: " + diskPool.getUserCapacity());
                    System.out.println("\n\n\n");
                }
                
                System.out.println("Pool Name: " + pool.getName());
                System.out.println("Allocated Capcity: " + pool.getAllocatedCapacity());  // Use Allowed for determining total capacity of luns
                System.out.println("Raw Capacity: " + pool.getRawCapacity());
                System.out.println("State: " + pool.getState());
                System.out.println("Status: " + pool.getStatus());
                System.out.println("TotalSubscribedCapacity: " + pool.getTotalSubscribedCapacity());
                System.out.println("User Capacity:  " + pool.getUserCapacity());
                
                System.out.println("Allocated Capacity in GBs: " + allocatedCapacityInGBs);  // Use Allowed for determining total capacity of luns
                System.out.println("Raw Capacity in GBs: " + rawCapacityInGBs);
                System.out.println("User Capacity in GBs: " + userCapacityInGBs);
    
                System.out.println("MetaDataConsumedCapacity: " + pool.getMetaDataConsumedCapacity());
                System.out.println("MetaDataSubscripedCapacity: " + pool.getMetaDataSubscribedCapacity());
                System.out.println("PrimaryDataConsumedCapacity: " + pool.getPrimaryDataConsumedCapacity());
                System.out.println("PrimarySubscribedCapacity: " + pool.getPrimarySubscribedCapacity());
                System.out.println("SecondaryDataConsumedCapacity: " + pool.getSecondaryDataConsumedCapacity());
                System.out.println("SecondaryDataConsumedCapacity: " + pool.getSecondarySubscribedCapacity());
    
                for (PuppetEmcDevice.Pool.Mlu mlu : pool.getMlus()) {
                    numOfLuns++;
                    System.out.println("Mlu Name: " + mlu.getName());
                    System.out.println("Data State: " + mlu.getDataState());
                    System.out.println("LuType: " + mlu.getLuType());
                    System.out.println("Number: " + mlu.getNumber());
                    System.out.println("State: " + mlu.getState());
                }
            }
            
            System.out.println("Total DiskPool Free Space:" + puppetEmcDevice.getTotalDiskPoolFreeSpace());
            System.out.println("Total Pool Allocated Capacity:" + puppetEmcDevice.getTotalPoolAllocatedCapacity());
            System.out.println("Total Pool Raw Capacity:" + puppetEmcDevice.getTotalPoolRawCapacity());
            System.out.println("Total Pool User Capacity:" + puppetEmcDevice.getTotalPoolUserCapacity());
            System.out.println("Total Raid Group Free Space:" + puppetEmcDevice.getTotalRaidGroupFreeSpace());
        }
        
        assertTrue("There should be 7 controllers!  Instead there are : " + puppetEmcDevice.getControllers().size(), 
                puppetEmcDevice.getControllers().size() == 7);
        assertTrue("There should be 30 diskInfos!  Instead there are : " + puppetEmcDevice.getDiskInfos().size(), 
                puppetEmcDevice.getDiskInfos().size() == 30);
        assertTrue("There should be 3 diskPools!  Instead there are : " + puppetEmcDevice.getDiskPools().size(), 
                puppetEmcDevice.getDiskPools().size() == 3);
        assertTrue("There should be 4 hbaInfo.hbaPortsInfos!  Instead there are only: " + puppetEmcDevice.getHbaInfo().getHbaPortsInfos().size(), 
                puppetEmcDevice.getHbaInfo().getHbaPortsInfos().size() == 4);
        assertTrue("There should be 3 pools!  Instead there are : " + puppetEmcDevice.getPools().size(), 
                puppetEmcDevice.getPools().size() == 3);
        assertTrue("There should be 4 raidGroups!  Instead there are : " + puppetEmcDevice.getRaidGroups().size(), 
                puppetEmcDevice.getRaidGroups().size() == 4);
        assertTrue("There should be 38 softwares!  Instead there are : " + puppetEmcDevice.getSoftwares().size(), 
                puppetEmcDevice.getSoftwares().size() == 38);
        assertTrue("There should be 13 Luns found!  Instead there are : " + puppetEmcDevice.getNumberOfLuns(),
                puppetEmcDevice.getNumberOfLuns() == 13);

        assertNotNull("Addons exist", puppetEmcDevice.getAddons());
        assertTrue("Addons thin", puppetEmcDevice.getAddons().isThinEnabled());
        assertTrue("Addons compression", puppetEmcDevice.getAddons().isCompressionEnabled());
        assertFalse("Addons snap", puppetEmcDevice.getAddons().isSnapEnabled());
    }
    
    @Test
    public void convertToPuppetIdracServerDevice() throws Exception {
        String path = "util/idrac-server-software.json";
        URL resource = PuppetModuleUtilTest.class.getClassLoader().getResource(path);
        assertNotNull("Failed to load resource at " + path, resource);
        String text = IOUtils.toString(resource, Charsets.UTF_8);

        ObjectMapper objectMapper = this.buildObjectMapper();
        ArrayList<HashMap> deviceDetails = objectMapper.readValue(text, ArrayList.class);

        PuppetIdracServerDevice puppetIdracServerDevice = PuppetDbUtil.convertToPuppetIdracServerDevice(deviceDetails);
        
        assertNotNull("puppetIdracServerDevice cannot be null!", puppetIdracServerDevice);
        assertNotNull("puppetIdracServerDevice.vibsInfos cannot be null!", puppetIdracServerDevice.getVibsInfos());
        
        if (logger.isDebugEnabled()) {
            System.out.println("NumberOfVibsInfos: " + puppetIdracServerDevice.getVibsInfos().size());
        }
    
        assertTrue("There should be 80 VibInfos! Instead there are : " + puppetIdracServerDevice.getVibsInfos().size(),
                puppetIdracServerDevice.getVibsInfos().size() == 80);
    }
    
    
    static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        return mapper;
    }
}
