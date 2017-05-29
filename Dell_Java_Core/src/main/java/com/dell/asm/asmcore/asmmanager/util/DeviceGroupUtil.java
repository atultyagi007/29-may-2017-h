/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.usermanager.db.UsersDAO;
import com.dell.asm.usermanager.db.entity.UserEntity;

public final class DeviceGroupUtil {
    
    private static DeviceInventoryDAO DEVICE_INVENTORY_DAO = new DeviceInventoryDAO();  // Remove after static methods are removed

    //private constructor to avoid instantiation
    private DeviceGroupUtil() {

    }

    /**
     * Validate String for null or blank
     * 
     * @param str - string to be validated
     * 
     * @throws AsmManagerCheckedException
     */
    public static void validateField(String str) throws AsmManagerCheckedException {

        if (null == str || "".equals(str.trim()) || str.trim().length() > 100) {

            throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST,
                    AsmManagerMessages.invalidDeviceGroupRequest(str));
        }
    }

    /**
     * Validate Devices
     * 
     * @param entity - device group entity to be validated
     * 
     * @throws AsmManagerCheckedException
     */
    public static void validateDeviceObject(DeviceGroupEntity entity) throws AsmManagerCheckedException {

        if(null == entity.getDeviceInventories())
            return;
        
        List<String> device_Ids = getDeviceIds(entity.getDeviceInventories());
        List<DeviceInventoryEntity> deviceInvList = DEVICE_INVENTORY_DAO.getDevicesByIds(device_Ids);
        Map<String, DeviceInventoryEntity> deviceIdInventoryMap = getDeviceIdInventoryMap(deviceInvList);
        
        List<String> invalidDeviceIds = new ArrayList<>();
        for (String str : device_Ids) {
            if (!deviceIdInventoryMap.keySet().contains(str)) {
                invalidDeviceIds.add(str);
            }
        }

        if (invalidDeviceIds.size() > 0) {
            throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.INVALID_ID,
                    AsmManagerMessages.deviceIdsNotFound(invalidDeviceIds.toString()));
        }
        entity.setDeviceInventories(deviceInvList);
        
    }

    /**
     * Retrieve Device's id
     * 
     * @param entityList - list of device inventory entity
     * 
     * @return list of device's id
     */
    public static List<String> getDeviceIds(List<DeviceInventoryEntity> entityList) {

        List<String> deviceIdList = new ArrayList<String>();

        if (null == entityList || entityList.size() <= 0)
            return deviceIdList;

        for (DeviceInventoryEntity entity : entityList) {
            deviceIdList.add(entity.getRefId());
        }

        return deviceIdList;

    }
    
    /**
     * Map Device id and associated inventory
     * 
     * @param entityList - list of device inventory entity
     * 
     * @return map of device id and device
     */
    public static Map<String, DeviceInventoryEntity> getDeviceIdInventoryMap(List<DeviceInventoryEntity> entityList) {

    	Map<String, DeviceInventoryEntity> deviceIdInventoryMap = new HashMap<String, DeviceInventoryEntity>();

        if (!CollectionUtils.isEmpty(entityList)) {
	        for (DeviceInventoryEntity entity : entityList) {
	        	deviceIdInventoryMap.put(entity.getRefId(), entity);
	        }
        }

        return deviceIdInventoryMap;

    }

    /**
     * Validate Users
     * 
     * @param entity - device group entity to be validated
     * 
     * @throws AsmManagerCheckedException
     */
    public static void validateUserObject(DeviceGroupEntity entity) throws AsmManagerCheckedException {

        Set<Long> invalidUserIds = invalidGroupUserIds(entity);

        if (invalidUserIds.size() > 0) {
            throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.INVALID_ID, AsmManagerMessages.userIdsNotFound(invalidUserIds
                    .toString()));
        }

    }

    /**
     * Validate Integer
     * 
     * @param value - integer to be validated
     * 
     * @throws AsmManagerCheckedException
     */
    public static void validateInputInteger(String value) throws AsmManagerCheckedException {

        try {
            Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST,
                    AsmManagerMessages.invalidDeviceGroupRequest(value));
        }
    }

    /**
     * Retrieve Device Group Users by id's
     * 
     * @param ids - id of user(s) to be retrieved
     * 
     * @return list of user(s)
     */
    public static List<UserEntity> getDeviceGroupUsers(List<Long> ids) {

        if (null == ids || ids.size() <= 0)
            return null;

        List<UserEntity> userList = UsersDAO.getInstance().getUsersObjectByIds(ids);
        return userList;

    }

    /**
     * Delete association between device group and users
     * 
     * @param entity - device group entity
     * 
     * @throws AsmManagerCheckedException
     */
    public static void deleteGroupUsersAssociation(DeviceGroupEntity entity) throws AsmManagerCheckedException {

        Set<Long> sets = invalidGroupUserIds(entity);
        deleteGroupUsersAssociationFromDB(sets);

    }

    
    /**
     * Retrieve invalid users
     * 
     * @param entity - device group entity
     * 
     * @return invalid user ids
     */
    public static Set<Long> invalidGroupUserIds(DeviceGroupEntity entity) {

        Set<Long> invalidUserIds = new HashSet<>();
        if(null == entity.getGroupsUsers())
            return invalidUserIds;
        
        List<Long> user_Ids = new ArrayList<Long>(entity.getGroupsUsers());
        List<Long> userList = UsersDAO.getInstance().getUsersIdById(user_Ids);
        

        for (Long id : user_Ids) {
            if (!userList.contains(id))
                invalidUserIds.add(id);
        }

        return invalidUserIds;
    }

    /**
     * Delete association between device group users by ids
     * 
     * @param ids - set of user ids
     * 
     * @throws AsmManagerCheckedException
     */
    public static void deleteGroupUsersAssociationFromDB(Set<Long> ids) throws AsmManagerCheckedException {

        if (null == ids || ids.size() <= 0)
            return;
        DeviceGroupDAO.getInstance().deleteGroupUsersAssociation(ids);
    }

}
