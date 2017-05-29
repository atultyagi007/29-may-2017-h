/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.devicegroup.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.devicegroup.DeviceGroup;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.GroupUser;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.GroupUserList;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.IDeviceGroupService;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDeviceList;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.usermanager.db.UsersDAO;
import com.dell.asm.usermanager.db.entity.UserEntity;
import com.dell.asm.usermanager.exception.UserManagerDAOException;

public class DeviceGroupServiceTest {

    private static final String URL = TestUtil.ASMMANAGER_RA_URL;

    /* ------------------------------------------------------------------------- */
    /* main: */
    /* ------------------------------------------------------------------------- */
    public static void main(String[] args) {
        DeviceGroupServiceTest test = new DeviceGroupServiceTest();
        try {
            test.allTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ------------------------------------------------------------------------- */
    /* allTest: */
    /* ------------------------------------------------------------------------- */
    @Test
    public void allTest() {

        // Setup all the managed devices and users needed.
        Map<String, List<String>> entityTypeSeqIdsMap = preSetup();

        IDeviceGroupService proxy = TestUtil.createProxyWithTestAuth(URL, IDeviceGroupService.class);

        // --------------- Create a server pool.
        DeviceGroup serverPool1 = new DeviceGroup();
        serverPool1.setGroupName("rest server pool1");
        serverPool1.setGroupDescription("rest server pool1 desc");
        List<ManagedDevice> managedDevices1 = buildManagedDeviceWithRefIds(entityTypeSeqIdsMap.get("DEVICES1"));
        ManagedDeviceList managedDeviceList = new ManagedDeviceList();
        managedDeviceList.setManagedDevices(managedDevices1);
        serverPool1.setManagedDeviceList(managedDeviceList);
        List<GroupUser> groupUsers1 = buildGroupUserWithRefIds(entityTypeSeqIdsMap.get("USERS1"));
        GroupUserList groupUserList = new GroupUserList();
        groupUserList.setGroupUsers(groupUsers1);
        serverPool1.setGroupUserList(groupUserList);
        DeviceGroup serverPool1Created = (DeviceGroup) proxy.createDeviceGroup(serverPool1);

        assertNotNull("Unable to create serverPool1Created", serverPool1Created);
        assertNotSame("The DB should have assigned a refId", new Long(0), serverPool1Created.getGroupSeqId());
        assertEquals("Changed groupName", serverPool1.getGroupName(), serverPool1Created.getGroupName());
        assertNotNull("Url not assigned", serverPool1Created.getLink());

        // --------------- Retrieve the server pool.
        DeviceGroup serverPool1Retrieved = proxy.getDeviceGroup(serverPool1Created.getGroupSeqId().toString());
        assertEquals("Retrieved server pool not same as created server pool", serverPool1Created, serverPool1Retrieved);

        // --------------- Create another server pool.
        DeviceGroup serverPool2 = new DeviceGroup();
        serverPool2.setGroupName("rest server pool2");
        serverPool2.setGroupDescription("rest server pool2 desc");
        List<ManagedDevice> managedDevices2 = buildManagedDeviceWithRefIds(entityTypeSeqIdsMap.get("DEVICES2"));
        ManagedDeviceList managedDeviceList2 = new ManagedDeviceList();
        managedDeviceList2.setManagedDevices(managedDevices2);
        serverPool2.setManagedDeviceList(managedDeviceList2);
        List<GroupUser> groupUsers2 = buildGroupUserWithRefIds(entityTypeSeqIdsMap.get("USERS2"));
        GroupUserList groupUserList2 = new GroupUserList();
        groupUserList2.setGroupUsers(groupUsers2);
        serverPool2.setGroupUserList(groupUserList2);
        DeviceGroup serverPool2Created = (DeviceGroup) proxy.createDeviceGroup(serverPool2);

        assertNotNull("Unable to create serverPool2Created", serverPool2Created);

        // --------------- Retrieve both server pools.
        DeviceGroup[] serverPools = proxy.getAllDeviceGroup(null, null, null, null);
        assertTrue("Expected to retrieve at least 2 entities", (serverPools != null && serverPools.length >= 2));
        Set<String> serverPoolRefIds = new HashSet<String>();
        if (serverPools != null) {
            for (DeviceGroup serverPool : serverPools) {
                serverPoolRefIds.add(serverPool.getGroupSeqId().toString());
            }
        }
        assertTrue("serverPool1Created not found in list", serverPoolRefIds.contains(serverPool1Created.getGroupSeqId().toString()));
        assertTrue("serverPool2Created not found in list", serverPoolRefIds.contains(serverPool2Created.getGroupSeqId().toString()));

        // --------------- Update a server pool.
        String updatedPoolDescription = "UPDATED rest server pool1 desc";
        serverPool1Created.setGroupDescription(updatedPoolDescription);
        serverPool1Created.getGroupUserList().setGroupUsers(null);
        serverPool1Created.getManagedDeviceList().setManagedDevices(null);
        proxy.updateDeviceGroup(serverPool1Created.getGroupSeqId().toString(), serverPool1Created);

        DeviceGroup serverPool1Updated = proxy.getDeviceGroup(serverPool1Created.getGroupSeqId().toString());
        assertNotNull("Unable to retrieve updated server pool", serverPool1Updated);
        assertEquals("GroupDescription not updated", updatedPoolDescription, serverPool1Updated.getGroupDescription());
        assertEquals("Unexpected refId change", serverPool1Created.getGroupSeqId(), serverPool1Updated.getGroupSeqId());
        assertEquals("Changed user list", serverPool1Created.getGroupUserList().getGroupUsers(), serverPool1Updated.getGroupUserList()
                .getGroupUsers());
        assertEquals("Changed device list", serverPool1Created.getManagedDeviceList().getManagedDevices(), serverPool1Updated.getManagedDeviceList()
                .getManagedDevices());

        // -------------- Delete all server pools.
        proxy.deleteDeviceGroup(serverPool1Created.getGroupSeqId().toString());
        proxy.deleteDeviceGroup(serverPool2Created.getGroupSeqId().toString());

        DeviceGroup[] serverPools2 = proxy.getAllDeviceGroup(null, null, null, null);
        Set<String> serverPoolRefIds2 = new HashSet<String>();
        if (serverPools2 != null) {
            for (DeviceGroup serverPool : serverPools2) {
                serverPoolRefIds2.add(serverPool.getGroupSeqId().toString());
            }
        }
        assertFalse("serverPool1Created found in list", serverPoolRefIds2.contains(serverPool1Created.getGroupSeqId().toString()));
        assertFalse("serverPool2Created found in list", serverPoolRefIds2.contains(serverPool2Created.getGroupSeqId().toString()));

        // Cleanup any devices and users created.
        postSetup(entityTypeSeqIdsMap);
    }

    /**
     * @param entityTypeSeqIdsMap
     */
    private void postSetup(Map<String, List<String>> entityTypeSeqIdsMap) {

        // --------------- Delete one or more managed devices.
        Set<String> deviceRefIds = new HashSet<String>();
        deviceRefIds.addAll(entityTypeSeqIdsMap.get("DEVICES1"));
        deviceRefIds.addAll(entityTypeSeqIdsMap.get("DEVICES2"));
        if (CollectionUtils.isNotEmpty(deviceRefIds)) {
            IDeviceInventoryService proxy = TestUtil.createProxyWithTestAuth(URL, IDeviceInventoryService.class);
            for (String deviceRefId : deviceRefIds) {
                proxy.deleteDeviceInventory(deviceRefId, true);
                ManagedDevice deletedDevice = proxy.getDeviceInventory(deviceRefId);
                assertTrue("The DB should not return device", DeviceState.PENDING_DELETE == deletedDevice.getState());
            }
        }

        // --------------- Delete one or more users.
        Set<String> userRefIds = new HashSet<String>();
        userRefIds.addAll(entityTypeSeqIdsMap.get("USERS1"));
        userRefIds.addAll(entityTypeSeqIdsMap.get("USERS2"));
        if (CollectionUtils.isNotEmpty(deviceRefIds)) {
            for (String userRefId : userRefIds) {
                UsersDAO.getInstance().deleteUser(Long.parseLong(userRefId));
                try {
                    UsersDAO.getInstance().getUser(Long.parseLong(userRefId));
                } catch (UserManagerDAOException ue) {
                    assertTrue("The DB should not return user",
                            StringUtils.containsIgnoreCase("User not found for Id: " + userRefId, ue.getMessage()));
                }
            }
        }

    }

    /**
     * @return Map<String, List<String>>
     */
    private Map<String, List<String>> preSetup() {

        Map<String, List<String>> entityTypeSeqIdsMap = new HashMap<String, List<String>>();

        // --------------- Create one or more managed devices.
        IDeviceInventoryService proxy = TestUtil.createProxyWithTestAuth(URL, IDeviceInventoryService.class);

        List<String> uuids = new ArrayList<String>();
        uuids.add(UUID.randomUUID().toString());
        uuids.add(UUID.randomUUID().toString());
        uuids.add(UUID.randomUUID().toString());
        uuids.add(UUID.randomUUID().toString());
        Collections.sort(uuids);

        ManagedDevice device1 = new ManagedDevice();
        device1.setIpAddress("172.17.251.11");
        device1.setServiceTag("SVCTAG" + RandomStringUtils.randomAlphanumeric(7));
        device1.setDeviceType(DeviceType.BladeServer);
        device1.setRefId(uuids.get(0));
        device1.setModel("M620");
        device1.setState(DeviceState.READY);

        ManagedDevice device2 = new ManagedDevice();
        device2.setIpAddress("172.17.251.12");
        device2.setServiceTag("SVCTAG" + RandomStringUtils.randomAlphanumeric(7));
        device2.setDeviceType(DeviceType.RackServer);
        device2.setRefId(uuids.get(1));
        device2.setModel("R620");
        device2.setState(DeviceState.READY);

        ManagedDevice device3 = new ManagedDevice();
        device3.setIpAddress("172.17.251.13");
        device3.setServiceTag("SVCTAG" + RandomStringUtils.randomAlphanumeric(7));
        device3.setDeviceType(DeviceType.BladeServer);
        device3.setRefId(uuids.get(2));
        device3.setModel("M620");
        device3.setState(DeviceState.READY);

        ManagedDevice device4 = new ManagedDevice();
        device4.setIpAddress("172.17.251.14");
        device4.setServiceTag("SVCTAG" + RandomStringUtils.randomAlphanumeric(7));
        device4.setDeviceType(DeviceType.RackServer);
        device4.setRefId(uuids.get(3));
        device4.setModel("R620");
        device4.setState(DeviceState.READY);

        ManagedDevice[] devicesCreated = (ManagedDevice[]) proxy.createDeviceInventory(new ManagedDevice[] { device1, device2, device3, device4 });
        assertTrue("The DB should return devices", (devicesCreated != null && devicesCreated.length > 0));

        List<String> deviceRefIds1 = new ArrayList<String>();
        List<String> deviceRefIds2 = new ArrayList<String>();
        for (int i = 0; i < devicesCreated.length; i++) {
            assertTrue("The DB should return device refId", StringUtils.isNotEmpty(devicesCreated[i].getRefId()));
            if (i < 2) {
                deviceRefIds1.add(devicesCreated[i].getRefId());
            } else {
                deviceRefIds2.add(devicesCreated[i].getRefId());
            }
        }
        entityTypeSeqIdsMap.put("DEVICES1", deviceRefIds1);
        entityTypeSeqIdsMap.put("DEVICES2", deviceRefIds2);

        // --------------- Create one or more users.
        UserEntity user1 = new UserEntity();
        user1.setFirstName("jane");
        user1.setLastName("doe");
        user1.setUserName("janedoe");
        user1.setPassword("jane");
        UserEntity user1Created = UsersDAO.getInstance().createUser(user1);
        assertTrue("The DB should return user", (user1Created != null && user1Created.getUserSeqId() > 0));

        UserEntity user2 = new UserEntity();
        user2.setFirstName("john");
        user2.setLastName("doe");
        user2.setUserName("johndoe");
        user2.setPassword("john");
        UserEntity user2Created = UsersDAO.getInstance().createUser(user2);
        assertTrue("The DB should return user", (user2Created != null && user2Created.getUserSeqId() > 0));

        UserEntity user3 = new UserEntity();
        user3.setFirstName("jane");
        user3.setLastName("doe");
        user3.setUserName("janedoe");
        user3.setPassword("jane");
        UserEntity user3Created = UsersDAO.getInstance().createUser(user3);
        assertTrue("The DB should return user", (user3Created != null && user3Created.getUserSeqId() > 0));

        UserEntity user4 = new UserEntity();
        user4.setFirstName("john");
        user4.setLastName("doe");
        user4.setUserName("johndoe");
        user4.setPassword("john");
        UserEntity user4Created = UsersDAO.getInstance().createUser(user4);
        assertTrue("The DB should return user", (user4Created != null && user4Created.getUserSeqId() > 0));

        List<String> userRefIds1 = new ArrayList<String>();
        userRefIds1.add(Long.toString(user1Created.getUserSeqId()));
        userRefIds1.add(Long.toString(user2Created.getUserSeqId()));
        entityTypeSeqIdsMap.put("USERS1", userRefIds1);

        List<String> userRefIds2 = new ArrayList<String>();
        userRefIds2.add(Long.toString(user3Created.getUserSeqId()));
        userRefIds2.add(Long.toString(user4Created.getUserSeqId()));
        entityTypeSeqIdsMap.put("USERS2", userRefIds2);

        return entityTypeSeqIdsMap;

    }

    /**
     * @param deviceRefIds
     * @return List<ManagedDevice>
     */
    private List<ManagedDevice> buildManagedDeviceWithRefIds(List<String> deviceRefIds) {
        List<ManagedDevice> managedDevices = null;
        if (CollectionUtils.isNotEmpty(deviceRefIds)) {
            managedDevices = new ArrayList<ManagedDevice>();
            for (String deviceRefId : deviceRefIds) {
                ManagedDevice managedDevice = new ManagedDevice();
                managedDevice.setRefId(deviceRefId);
                managedDevices.add(managedDevice);
            }
        }
        return managedDevices;
    }

    /**
     * @param userRefIds
     * @return List<GroupUser>
     */
    private List<GroupUser> buildGroupUserWithRefIds(List<String> userRefIds) {
        List<GroupUser> groupUsers = null;
        if (CollectionUtils.isNotEmpty(userRefIds)) {
            groupUsers = new ArrayList<GroupUser>();
            for (String userRefId : userRefIds) {
                GroupUser groupUser = new GroupUser();
                groupUser.setUserSeqId(Long.parseLong(userRefId));
                groupUsers.add(groupUser);
            }
        }
        return groupUsers;
    }

}
