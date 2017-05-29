/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.usermanager.LocalUserManager;
import com.dell.asm.usermanager.LocalUserManagerFactory;
import com.dell.asm.usermanager.db.entity.UserEntity;

/**
 * JUnit test class to verifying Device Group DAO implementation.
 * 
 */
public class DeviceGroupDAOIT {
    private DeviceInventoryDAO dao = new DeviceInventoryDAO();
    private DeviceGroupDAO instance = DeviceGroupDAO.getInstance();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        assertNotNull(dao);
    }

    /**
     * test CRUD operation on Device group.
     * 
     */
    @Test
    public void testDeviceGroupDAO() {
        cleanup();
        List<DeviceInventoryEntity> testCreateTwoDeviceInventory = testCreateTwoDeviceInventory();
        DeviceGroupEntity gentity = new DeviceGroupEntity();
        Set<Long> userList = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            userList.add(new Long(i));
        }
        gentity.setName("abc");
        gentity.setDescription("aaaa");
        gentity.setGroupsUsers(userList);
        gentity.setDeviceInventories(testCreateTwoDeviceInventory);
        try {
            // test create
            DeviceGroupEntity createGroupDevice = instance.createGroupDevice(gentity);

            Long seqId = createGroupDevice.getSeqId();

            // test retrieve
            DeviceGroupEntity deviceGroupById = instance.getDeviceGroupById(seqId);
            assert deviceGroupById != null;

            deviceGroupById.getGroupsUsers().remove(new Long(5));

            // test update
            DeviceGroupEntity updateGroupDevice = instance.updateGroupDevice(deviceGroupById);
            assert updateGroupDevice.getGroupsUsers().contains(new Long(5)) == false;

            // test delete
            instance.deleteDeviceGroup(updateGroupDevice);
            // }

        } catch (Exception amde) {
            amde.printStackTrace();
            fail();
            // }
        }

    }

    /**
     * cleanup device inventory and group table data.
     * 
     */
    private void cleanup() {
        List<DeviceInventoryEntity> allDeviceInventory = dao.getAllDeviceInventory();
        if (allDeviceInventory != null) {
            for (DeviceInventoryEntity die : allDeviceInventory) {
                dao.deleteDeviceInventory(die);
            }
        }
        try {
            List<DeviceGroupEntity> allDeviceGroup = instance.getAllDeviceGroup(null, null, null);
            if (allDeviceGroup != null) {
                for (DeviceGroupEntity dg : allDeviceGroup) {
                    instance.deleteDeviceGroup(dg);
                }
            }
        } catch (AsmManagerCheckedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * utility for populating device inventory in database.
     * 
     */

    public List<DeviceInventoryEntity> testCreateTwoDeviceInventory() {

        /***************
         * 
         * Delete the complete device_inventory table
         * 
         * **/
        String[] refIdsStrings = { "1", "2", "3", "8a22e882417b2c4501417b2c47620000", "5", "6", "8a22e882417b2c4501417b2c47e10009", "8", "9",
                "8a22e882417b2c4501417b2c48110012", "11", "8a22e882417b2c4501417b2c481f001b", "13", "8a22e882417b2c4501417b2c483b0024",
                "8a22e882417b2c4501417b2c4849002d" };

        DeviceType[] devTypes = { DeviceType.ChassisM1000e, DeviceType.RackServer, DeviceType.RackServer, DeviceType.AggregatorIOM, DeviceType.unknown, DeviceType.ChassisM1000e,
                DeviceType.AggregatorIOM, DeviceType.ChassisM1000e, DeviceType.RackServer, DeviceType.AggregatorIOM, DeviceType.ChassisM1000e, DeviceType.AggregatorIOM, DeviceType.ChassisM1000e,
                DeviceType.AggregatorIOM, DeviceType.AggregatorIOM };

        String[] serviceTags = { "ServiceTag1", "serverstagBbCmYoG", "ServiceTag22", "iomstaggzfDKab", "ServiceTag3", "ServiceTag13",
                "iomstagRJbw4S7", "ServiceTag19", "ServiceTag15", "iomstagk0KitTv", "ServiceTag11", "iomstagBKuwbza", "ServiceTag18",
                "iomstagWjidCvy", "newiom" };

        String[] ipAddresses = { "10.128.129.123", "9.128.129.123", "10.128.129.122", "10.118.129.123", "1.128.129.123", "1.128.100.123",
                "10.128.129.119", "10.101.129.123", "4.128.129.123", "3.128.129.123", "10.128.000.123", "10.000.000.123", "10.128.129.190",
                "10.128.129.111", "10.128.129.112" };

        // keeping common
        String[] model = { "chassis", "server", "server", "iom", "Unknown", "chassis", "iom", "chassis", "server", "iom", "chassis", "iom",
                "chassis", "iom", "iom" };

        // keeping common
        String refType = "Unknown";

        String[] displayNameStrings = { "abc", "xyz", "mlm", "drt", "adv", "wqwqw", "xyqq", "wqrere", "reff", "geere", "deee", "sgssds", "tttnmk",
                "tttnwqwqmk", "cscsdstttnmk" };

        DeviceState[] deviceStates = { DeviceState.READY, DeviceState.READY, DeviceState.READY, DeviceState.CONFIGURATION_ERROR,
                DeviceState.CONFIGURATION_ERROR, DeviceState.READY, DeviceState.READY, DeviceState.READY,
                DeviceState.PENDING_CONFIGURATION_TEMPLATE, DeviceState.READY, DeviceState.READY, DeviceState.READY,
                DeviceState.READY, DeviceState.READY, DeviceState.READY };

        // DeviceState deviceState = DeviceState.DISCOVERED;

        // Creating entity 1
        DeviceInventoryEntity entity = null;
        List<DeviceInventoryEntity> list = new ArrayList<>();

        for (int i = 0; i < refIdsStrings.length; i++) {
            entity = new DeviceInventoryEntity();
            entity.setRefId(refIdsStrings[i]);
            entity.setDeviceType(devTypes[i]);
            entity.setServiceTag(serviceTags[i]);
            entity.setIpAddress(ipAddresses[i]);
            entity.setModel(model[i]);
            entity.setRefType(refType);
            entity.setDisplayName(displayNameStrings[i]);
            entity.setState(deviceStates[i]);

            /*
             * DeviceGroupEntity gentity = new DeviceGroupEntity(); gentity.setName("abc_" + serviceTags[i]); gentity.setDescription("aaaa");
             */
            // /deviceGroupEntityList.add(gentity);

            try {
                // DeviceGroupEntity deviceGroupById = instance.getDeviceGroupById(new Long(++i));
                List<DeviceGroupEntity> deviceGroupEntityList = new ArrayList<>();
                // deviceGroupEntityList.add(deviceGroupById);
                // entity.setDeviceGroupList(deviceGroupEntityList);
                dao.createDeviceInventory(entity);
                list.add(entity);
            } catch (AsmManagerCheckedException amde) {
                if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD) {
                    fail();
                }
            }

            entity = null;
        }
        return list;
    }

    /**
     * test Create Device With DeviceGroup.
     * 
     */
    @Test
    public void testCreateDeviceWithDeviceGroup() {
        // drop the database first to avoid primary key constraint violation
        // user group list
        Set<Long> userList = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            userList.add(new Long(i));
        }
        // create Device Groups
        List<DeviceGroupEntity> deviceGroupEntityList = new ArrayList<>();
        try {
            for (int j = 0; j < 5; j++) {
                DeviceGroupEntity gentity = new DeviceGroupEntity();

                gentity.setName("abc" + j);
                gentity.setDescription("aaaa");
                gentity.setGroupsUsers(userList);
                // gentity.setDeviceInventories(testCreateTwoDeviceInventory);
                deviceGroupEntityList.add(gentity);

                instance.createGroupDevice(gentity);
            }
            DeviceInventoryEntity entity = new DeviceInventoryEntity();
            entity.setRefId("1");
            entity.setDeviceType(DeviceType.ChassisM1000e);
            entity.setServiceTag("ServiceTag");
            entity.setIpAddress("10.128.129.123");
            entity.setModel("Unknown");
            entity.setRefType("Unknown");
            entity.setDisplayName("None");
            entity.setState(DeviceState.READY);
            entity.setDeviceGroupList(deviceGroupEntityList);
            entity.setDiscoverDeviceType(DiscoverDeviceType.CMC);
            dao.createDeviceInventory(entity);
            // entity.setRefId("2");
            // dao.createDeviceInventory(entity);
        } catch (AsmManagerCheckedException amde) {
            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID) {
                fail();
            }
        }

    }

    /**
     * test Get Device With DeviceGroup.
     * 
     */
    @Test
    public void testGetDeviceWithDeviceGroup() {
        DeviceInventoryEntity deviceInventory = dao.getDeviceInventory("1");
        assert deviceInventory.getDeviceGroupList().size() == 5;

        // DeviceInventoryEntity deviceInventory2 = dao.getDeviceInventory("1");
        // assert deviceInventory2.getDeviceGroupList().size()==5;

        try {
            DeviceGroupEntity deviceGroupById = instance.getDeviceGroupById(new Long(1));
            assert deviceGroupById.getDeviceInventories().size() == 1;
        } catch (AsmManagerCheckedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * test Update Device With Device Group.
     * 
     */
    @Test
    public void testUpdateDeviceWithDeviceGroup() {
        DeviceInventoryEntity deviceInventory = dao.getDeviceInventory("1");
        deviceInventory.getDeviceGroupList().remove(4);

        try {
            dao.updateDeviceInventory(deviceInventory);
            deviceInventory = dao.getDeviceInventory("1");
            assert deviceInventory.getDeviceGroupList().size() == 4;
        } catch (AsmManagerCheckedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * test DeleteDevice With DeviceGroup.
     * 
     */
    @Test
    public void testDeleteDeviceWithDeviceGroup() {
        DeviceInventoryEntity deviceInventory = dao.getDeviceInventory("1");
        dao.deleteDeviceInventory(deviceInventory);
        try {
            DeviceGroupEntity deviceGroupById = instance.getDeviceGroupById(new Long(1));
            assert deviceGroupById.getDeviceInventories().size() == 0;
        } catch (AsmManagerCheckedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private List<DeviceInventoryEntity> testGetAccessiblePoolServersHelper(String tag, long userId, String poolId) throws SQLException {
        List<DeviceGroupDAO.BriefServerInfo> servers = instance.getAccessiblePoolServers(userId, poolId);
        List<DeviceInventoryEntity> ret = new ArrayList<>();
        for (DeviceGroupDAO.BriefServerInfo server : servers) {
            ret.add(dao.getDeviceInventory(server.getRefId()));
        }
        Collections.sort(ret, new Comparator<DeviceInventoryEntity>() {
            @Override
            public int compare(DeviceInventoryEntity o1, DeviceInventoryEntity o2) {
                return o1.getServiceTag().compareTo(o2.getServiceTag());
            }
        });
        for (int i = 0; i < ret.size(); ++i) {
            System.out.println(tag + " [" + (i + 1) + "] = " + ret.get(i).getServiceTag());
        }
        return ret;
    }

    @Test
    public void testGetAccessiblePoolServers() throws SQLException, AsmManagerCheckedException {
        List<DeviceGroupEntity> groups = instance.getAllDeviceGroup(null, null, null);

        LocalUserManager userManager = LocalUserManagerFactory.getUserManager();
        List<UserEntity> users = userManager.getAllUsers(null, null, null);
        for (UserEntity user : users) {
            List<DeviceInventoryEntity> allServers = testGetAccessiblePoolServersHelper(user.getUserName() + " ALL",
                    user.getUserSeqId(),
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ALL_ID);
            System.out.println();

            Set<DeviceInventoryEntity> seenServers = new HashSet<>();
            List<DeviceInventoryEntity> servers = testGetAccessiblePoolServersHelper(user.getUserName() + " GLOBAL",
                    user.getUserSeqId(),
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID);
            seenServers.addAll(servers);
            System.out.println();

            for (DeviceGroupEntity group : groups) {
                servers = testGetAccessiblePoolServersHelper(user.getUserName() + " " + group.getName(),
                        user.getUserSeqId(),
                        group.getSeqId().toString());
                seenServers.addAll(servers);
                System.out.println();
            }

            assertEquals("All servers size did not match global plus all pools",
                    allServers.size(), seenServers.size());
        }
    }

    @Test
    public void testGetAccessibleServers() throws SQLException, AsmManagerCheckedException {
        LocalUserManager userManager = LocalUserManagerFactory.getUserManager();
        List<UserEntity> users = userManager.getAllUsers(null, null, null);
        for (UserEntity user : users) {
            List<DeviceInventoryEntity> servers = testGetAccessiblePoolServersHelper(user.getUserName() + " ALL",
                    user.getUserSeqId(),
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ALL_ID);
            System.out.println();
            for (DeviceInventoryEntity server : servers) {
                List<DeviceGroupDAO.BriefServerInfo> accessibleServers = instance.getAccessibleServers(
                        user.getUserSeqId(), Arrays.asList(server.getRefId()));
                assertEquals(1, accessibleServers.size());
            }

            List<DeviceInventoryEntity> inaccessibleServers = dao.getAllDeviceInventory();
            inaccessibleServers.removeAll(servers);

            for (DeviceInventoryEntity inaccessibleServer : inaccessibleServers) {
                List<DeviceGroupDAO.BriefServerInfo> accessibleServers = instance.getAccessibleServers(
                        user.getUserSeqId(), Arrays.asList(inaccessibleServer.getRefId()));
                assertEquals(0, accessibleServers.size());
            }
        }
    }

    @Test
    public void testGetName() throws AsmManagerCheckedException {
        List<DeviceGroupEntity> groups = instance.getAllDeviceGroup(null, null, null);
        for (DeviceGroupEntity group : groups) {
            assertEquals(group.getName(), instance.getPoolName(group.getSeqId().toString()));
        }

        assertEquals("Global", instance.getPoolName(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID));
        assertEquals("All Servers", instance.getPoolName(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ALL_ID));
        assertEquals("Unknown", instance.getPoolName("-45"));
    }
}
