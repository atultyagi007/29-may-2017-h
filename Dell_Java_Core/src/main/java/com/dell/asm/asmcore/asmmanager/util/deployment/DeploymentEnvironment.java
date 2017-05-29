/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;

import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeploymentEnvironment {
    Long userID;
    // temporary variable to prevent looking at servers we have already looked at previously
    List <String> skipServers;
    // All deployments
    List<DeploymentEntity> deployments;

    public static class DeviceGroupInfo {
        private final long groupId;
        private final List<DeviceInventoryEntity> servers;

        public DeviceGroupInfo(long groupId, List<DeviceInventoryEntity> servers) {
            this.groupId = groupId;
            this.servers = servers;
        }

        public long getGroupId() {
            return groupId;
        }

        public List<DeviceInventoryEntity> getServers() {
            return servers;
        }
    }

    // All server pools
    Map<Long, DeviceGroupInfo> deviceGroupsCache = new HashMap<>();

    // Whether to only use a server once. This is the default behavior. But in the case where
    // we want to find all servers that could be used for a given server component (serer
    // selection drop-down box) it will be false.
    boolean requireUnique = true;

    public DeploymentEnvironment() {
        skipServers = new ArrayList<>();
    }

    public List<String> getSkipServers() {
        return skipServers;
    }

    public void setSkipServers(List<String> skipServers) {
        this.skipServers = skipServers;
    }

    public List<DeploymentEntity> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<DeploymentEntity> deployments) {
        this.deployments = deployments;
    }

    public DeviceGroupInfo getCachedDeviceGroup(long groupId) {
        return deviceGroupsCache.get(groupId);
    }

    public DeviceGroupInfo getCachedDeviceGroup(String groupId) {
        return deviceGroupsCache.get(Long.valueOf(groupId));
    }

    public void addCachedDeviceGroup(String groupId, List<DeviceInventoryEntity> serverRefIds) {
        DeviceGroupInfo groupInfo = new DeviceGroupInfo(Long.valueOf(groupId), serverRefIds);
        deviceGroupsCache.put(groupInfo.getGroupId(), groupInfo);
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public void markServerProcesed(String serviceTag) {
        if (requireUnique) {
            skipServers.add(serviceTag);
        }
    }

    public boolean isRequireUnique() {
        return requireUnique;
    }

    public void setRequireUnique(boolean requireUnique) {
        this.requireUnique = requireUnique;
    }
}
