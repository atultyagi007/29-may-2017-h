/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.util.deployment.ServiceDeploymentUtil;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmcore.asmmanager.util.firmwarerepository.FirmwareUtil;
import com.dell.asm.asmcore.asmmanager.util.tasks.JobUtils;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IChassisService;
import com.dell.pg.asm.chassis.client.device.IOM;
import com.dell.pg.asm.chassis.client.device.TagType;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDeviceList;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ServiceReference;

import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.app.rest.DeviceInventoryService;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.DeviceGroup;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.DeviceGroupList;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.GroupUser;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.GroupUserList;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.tasks.DeleteDeviceJob;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorDevice;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorUtil;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.usermanager.db.entity.UserEntity;
import com.dell.pg.jraf.client.jobmgr.JrafJobExecutionContext;
import com.dell.pg.jraf.client.jobmgr.JrafJobExecutionContextList;
import com.dell.pg.jraf.client.jobmgr.JrafJobKey;
import com.dell.pg.orion.jobmgr.JobManager;
import com.dell.pg.orion.jobmgr.JobManagerException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utilities class to convert REST web service exposed DeviceInventory to DeviceInventoryEntity.
 * 
 */
public final class DeviceInventoryUtils 
{
    private static final Logger logger = Logger.getLogger(DeviceInventoryUtils.class);
    private static final DeviceGroupList ADMIN_SERVER_POOL_GROUP_LIST = new DeviceGroupList();

    private DeviceInventoryDAO deviceInventoryDAO1;
    private FirmwareUtil firmwareUtil1;
    private ServiceDeploymentUtil serviceDeploymentUtil;
    private IChassisService chassisService;

    static{
    	ArrayList<DeviceGroup> adminServerDeviceGroupPoolList = new ArrayList<DeviceGroup>();
    	DeviceGroup adminServerPoolGroup = new DeviceGroup();      
    	adminServerPoolGroup.setCreatedBy("admin");
    	adminServerPoolGroup.setGroupName("Global");
    	adminServerPoolGroup.setGroupSeqId(new Long(-1));
    	adminServerDeviceGroupPoolList.add(adminServerPoolGroup);
    	ADMIN_SERVER_POOL_GROUP_LIST.setDeviceGroup(adminServerDeviceGroupPoolList);;
    }

    public DeviceInventoryUtils() {
    }

	public static DeviceGroupEntity toEntity(DeviceGroup deviceGroup, boolean includeDeviceInfo, boolean includeUserInfo) {
        if (deviceGroup == null || Long.valueOf(-1).equals(deviceGroup.getGroupSeqId())) {
            return null;
        }

        DeviceGroupEntity entity = new DeviceGroupEntity();
        entity.setSeqId(deviceGroup.getGroupSeqId());
        entity.setName(deviceGroup.getGroupName());
        entity.setDescription(deviceGroup.getGroupDescription());

        entity.setCreatedBy(deviceGroup.getCreatedBy());
        if (deviceGroup.getCreatedDate() != null) {
            entity.setCreatedDate(new GregorianCalendar());
        }

        entity.setUpdatedBy(deviceGroup.getUpdatedBy());
        if (deviceGroup.getUpdatedDate() != null) {
            entity.setUpdatedDate(new GregorianCalendar());
        }

        if (includeDeviceInfo) {
            List<DeviceInventoryEntity> deviceInventories = null;

            if (null != deviceGroup.getManagedDeviceList()) {

                deviceInventories = deviceGroup.getManagedDeviceList().getManagedDevices() != null ? toEntities(deviceGroup.getManagedDeviceList()
                        .getManagedDevices(), false) : new ArrayList<DeviceInventoryEntity>();
            }
            entity.setDeviceInventories(deviceInventories);
        }

        if (includeUserInfo) {
            Set<Long> userList = null;

            if (null != deviceGroup.getGroupUserList()) {
                userList = getUserIds(deviceGroup.getGroupUserList().getGroupUsers());
            }

            entity.setGroupsUsers(userList);
        }
        return entity;
    }

    public static Set<Long> getUserIds(List<GroupUser> groupUsers) {

        Set<Long> set = new HashSet<>();
        if (null == groupUsers)
            return set;

        for (GroupUser user : groupUsers) {
            set.add(user.getUserSeqId());
        }

        return set;

    }

    public static DeviceGroup toDTO(DeviceGroupEntity entity, List<UserEntity> userList, boolean includeDeviceInfo, boolean includeUserInfo) {
        if (entity == null) {
            return null;
        }

        DeviceGroup group = new DeviceGroup();
        group.setGroupSeqId(entity.getSeqId());
        group.setGroupName(entity.getName());
        group.setGroupDescription(entity.getDescription());
        group.setCreatedBy(entity.getCreatedBy());
        group.setCreatedDate(entity.getCreatedDate());
        group.setUpdatedBy(entity.getUpdatedBy());
        group.setUpdatedDate(entity.getUpdatedDate());

        if (includeDeviceInfo) {
            List<ManagedDevice> devices = entity.getDeviceInventories() != null ? toDTOs(entity.getDeviceInventories(), false)
                    : new ArrayList<ManagedDevice>();
            ManagedDeviceList deviceList = new ManagedDeviceList();
            deviceList.setManagedDevices(devices);
            deviceList.setTotalCount(devices.size());
            group.setManagedDeviceList(deviceList);
        }

        if (includeUserInfo) {
            GroupUserList groupUserList = new GroupUserList();
            List<GroupUser> groupUsers = usertoDTO(userList);
            groupUserList.setGroupUsers(groupUsers);
            groupUserList.setTotalRecords(groupUsers.size());
            group.setGroupUserList(groupUserList);
        }
        return group;
    }

    public static List<GroupUser> usertoDTO(List<UserEntity> userList) {

        List<GroupUser> groupUsers = new ArrayList<>();

        if (null == userList || userList.size() <= 0)
            return groupUsers;

        for (UserEntity entity : userList) {
            GroupUser user = new GroupUser();
            user.setUserSeqId(entity.getUserSeqId());
            user.setUserName(entity.getUserName());
            user.setFirstName(entity.getFirstName());
            user.setLastName(entity.getLastName());
            user.setEnabled(entity.isEnabled());
            if(CollectionUtils.isNotEmpty(entity.getRole())) {
                user.setRole(entity.getRole().iterator().next().getRoleName());
            }
            groupUsers.add(user);

        }

        return groupUsers;
    }

    public static List<GroupUser> userToGroupUserDTO(User[] userList) {

        List<GroupUser> groupUsers = new ArrayList<>();

        if (null == userList || userList.length <= 0)
            return groupUsers;

        for (User user : userList) {
            GroupUser groupUser = new GroupUser();
            groupUser.setUserSeqId(user.getUserSeqId());
            groupUser.setUserName(user.getUserName());
            groupUser.setFirstName(user.getFirstName());
            groupUser.setLastName(user.getLastName());
            groupUser.setEnabled(user.isEnabled());
            groupUser.setRole(user.getRole());
            groupUsers.add(groupUser);

        }

        return groupUsers;
    }    
    
    public static DeviceInventoryEntity toEntity(ManagedDevice deviceInventory, boolean includeDeviceGroupInfo) {
        if (deviceInventory == null) {
            return null;
        }
        DeviceInventoryEntity entity = new DeviceInventoryEntity();
        entity.setRefId(deviceInventory.getRefId());
        entity.setIpAddress(deviceInventory.getIpAddress());
        entity.setServiceTag(deviceInventory.getServiceTag());
        entity.setDeviceType(deviceInventory.getDeviceType());
        entity.setManagedState(deviceInventory.getManagedState());
        entity.setState(deviceInventory.getState());
        entity.setModel(deviceInventory.getModel());
        entity.setRefType(deviceInventory.getRefType());
        entity.setHealth(deviceInventory.getHealth());
        entity.setHealthMessage(deviceInventory.getHealthMessage());        
        entity.setInfraTemplateDate(deviceInventory.getInfraTemplateDate());
        entity.setInfraTemplateId(deviceInventory.getInfraTemplateId());
        entity.setServerTemplateDate(deviceInventory.getServerTemplateDate());
        entity.setServerTemplateId(deviceInventory.getServerTemplateId());
        entity.setInventoryDate(deviceInventory.getInventoryDate());
        entity.setComplianceCheckDate(deviceInventory.getComplianceCheckDate());
        entity.setDiscoveredDate(deviceInventory.getDiscoveredDate());
        entity.setVendor( deviceInventory.getManufacturer());
        entity.setConfig(deviceInventory.getConfig());
        entity.setSystemId(deviceInventory.getSystemId());
        entity.setDisplayName(deviceInventory.getDisplayName());
        entity.setChassisId(deviceInventory.getChassisId());
        if (deviceInventory.getCompliance()!=null)
            entity.setCompliant(deviceInventory.getCompliance().name());

        if (includeDeviceGroupInfo && deviceInventory.getDeviceGroupList() != null && deviceInventory.getDeviceGroupList().getDeviceGroup() != null) {
            List<DeviceGroup> deviceGroupLst = deviceInventory.getDeviceGroupList().getDeviceGroup();
            for (DeviceGroup dg : deviceGroupLst) {
                entity.addDeviceGroup(toEntity(dg, false, false));
            }
        }
        
        entity.setCredId(deviceInventory.getCredId());
        entity.setFacts(deviceInventory.getFacts());
        entity.setDiscoverDeviceType(deviceInventory.getDiscoverDeviceType());
        entity.setFailuresCount(deviceInventory.getFailuresCount());
        return entity;
    }
    
    public static ManagedDevice toDTO(DeviceInventoryEntity deviceInventoryEntity, boolean includeDeviceGroupInfo) {
    	return toDTO(deviceInventoryEntity, includeDeviceGroupInfo, false);
    }

    public static ManagedDevice toDTO(DeviceInventoryEntity deviceInventoryEntity,
                                      boolean includeDeviceGroupInfo,
                                      boolean includeDeploymentInfo) {

        if (deviceInventoryEntity == null) {
            return null;
        }
        ManagedDevice model = new ManagedDevice();
        model.setRefId(deviceInventoryEntity.getRefId());
        model.setIpAddress(deviceInventoryEntity.getIpAddress());
        model.setServiceTag(deviceInventoryEntity.getServiceTag());
        model.setDeviceType(deviceInventoryEntity.getDeviceType());
        model.setManagedState(deviceInventoryEntity.getManagedState());
        model.setState(deviceInventoryEntity.getState());
        model.setModel(deviceInventoryEntity.getModel());
        model.setHealth(deviceInventoryEntity.getHealth());
        model.setHealthMessage(deviceInventoryEntity.getHealthMessage());
        model.setInfraTemplateDate(deviceInventoryEntity.getInfraTemplateDate());
        model.setInfraTemplateId(deviceInventoryEntity.getInfraTemplateId());
        model.setServerTemplateDate(deviceInventoryEntity.getServerTemplateDate());
        model.setServerTemplateId(deviceInventoryEntity.getServerTemplateId());
        model.setInventoryDate(deviceInventoryEntity.getInventoryDate());
        model.setComplianceCheckDate(deviceInventoryEntity.getComplianceCheckDate());
        model.setManufacturer(deviceInventoryEntity.getVendor());
        model.setDiscoveredDate(deviceInventoryEntity.getDiscoveredDate());
        model.setCredId(deviceInventoryEntity.getCredId());
        model.setFacts(deviceInventoryEntity.getFacts());
        model.setConfig(deviceInventoryEntity.getConfig());
        model.setSystemId(deviceInventoryEntity.getSystemId());
        model.setFailuresCount(deviceInventoryEntity.getFailuresCount());
        model.setDisplayName(deviceInventoryEntity.getDisplayName());
        model.setCompliance(CompliantState.fromValue(deviceInventoryEntity.getCompliant()));
        model.setChassisId(deviceInventoryEntity.getChassisId());

        boolean inUse = false;
        if (deviceInventoryEntity.getDeploymentCount() > 0) {
            inUse = true;
        }
        model.setInUse(inUse);

        model.setFirmwareName(deviceInventoryEntity.getFirmwareName());

        // if call from Server Pools we don't need firmware details and can omit lazy init for deviceInventoryEntity
        // in this case includeDeviceGroupInfo will be false
        // Lazy init by access inventory DAO record significantly increases response time
        // for server pools with large number of members
        if(includeDeviceGroupInfo){
            List<DeviceGroupEntity> deviceGroupEntityList = deviceInventoryEntity.getDeviceGroupList();

            if (deviceGroupEntityList != null && !deviceGroupEntityList.isEmpty()) {
                DeviceGroupList dgList = new DeviceGroupList();
                List<DeviceGroup> deviceGroupList = new ArrayList<>(deviceGroupEntityList.size());
                for (DeviceGroupEntity dge : deviceGroupEntityList) {
                    deviceGroupList.add(toDTO(dge, Collections.EMPTY_LIST, false, false));
                }
                dgList.setDeviceGroup(deviceGroupList);
                model.setDeviceGroupList(dgList);
            }
            else{ // Set the Default Admin List to fix ASM-3588
            	model.setDeviceGroupList(DeviceInventoryUtils.ADMIN_SERVER_POOL_GROUP_LIST);
            }
        }
        model.setOperatingSystem( "N/A" ); // for now. Get this from the "Service" deployed later.
        model.setDiscoverDeviceType(deviceInventoryEntity.getDiscoverDeviceType());

        // Optimization to support more details on resources page in UI.
        if (includeDeploymentInfo) {
            // List of deployment names this device is part of
            List<DeploymentEntity> deploymentEntityList = deviceInventoryEntity.getDeployments();
            List<ServiceReference> serviceReferences = new ArrayList<>();
            if (deploymentEntityList != null && !deploymentEntityList.isEmpty()) {
                for (DeploymentEntity deploymentEntity : deploymentEntityList) {
                    serviceReferences.add(new ServiceReference(deploymentEntity.getId(), deploymentEntity.getName()));
                }
            }
            model.setServiceReferences(serviceReferences);
        }

        if(DeviceType.isRAServer(model.getDeviceType()) && !StringUtils.isEmpty(deviceInventoryEntity.getFacts()))
        {
            Map<String,String> map = new HashMap<>();
            String[] entries = deviceInventoryEntity.getFacts().split(";");
            for (String entry : entries) {
                String[] keyValue = entry.split("=");
                if(keyValue != null && keyValue.length > 1) {
                	map.put(keyValue[0],keyValue[1]);
                }
            }
            if (map.containsKey("memoryInGB"))
                model.setMemoryInGB(Integer.parseInt(map.get("memoryInGB")));
            if (map.containsKey("nics"))
                model.setNics(Integer.parseInt(map.get("nics")));
            if (map.containsKey("numberOfCPUs"))
                model.setNumberOfCPUs(Integer.parseInt(map.get("numberOfCPUs")));
            if (map.containsKey("cpuType"))
                model.setCpuType(map.get("cpuType"));
            if (map.containsKey("hostname"))
                model.setHostname(map.get("hostname"));

        }

        return model;
    }

    public static List<DeviceInventoryEntity> toEntities(List<ManagedDevice> devices, boolean includeDeviceGroupInfo) {
        List<DeviceInventoryEntity> entities = new ArrayList<>();
        for (ManagedDevice device : devices) {
            entities.add(toEntity(device, includeDeviceGroupInfo));
        }
        return entities;
    }


    public static List<ManagedDevice> toDTOs(List<DeviceInventoryEntity> devices, boolean includeDeviceGroupInfo) {
        List<ManagedDevice> models = new ArrayList<>();
        for (DeviceInventoryEntity device : devices) {
            models.add(toDTO(device, includeDeviceGroupInfo));
        }
        return models;
    }

    public boolean validateDeviceDeleteRequest(String refId, boolean forceDelete) {
        if (refId == null) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(refId));
        }
        DeviceInventoryEntity device = getDeviceInventoryDAO().getDeviceInventory(refId);
        if (device == null) {
            throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, AsmManagerMessages.notFound(refId));
        }
        if(device.getDeployments().size() > 0){
            logger.error("Error occurred in deleting device: " + device.getDeviceType() + " "+device.getServiceTag() + " is currently in use by deployment(s)");
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.deviceInUse(device.getDeviceType().toString(), device.getServiceTag()));
        }

        if (device.getState() == DeviceState.PENDING_DELETE) {
            // throw the error only if there is a pending delete job for device.

            if (isPendingDeleteJob(refId)) {
                if (DeviceType.isChassis(device.getDeviceType())) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.chassisDeviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
                } else if (DeviceType.isServer(device.getDeviceType()) || device.getDeviceType().equals(DeviceType.AggregatorIOM)) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.bladeServerOrIOMDeviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
                } else {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.deviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
                }
            }
        }

        if (ManagedDeviceUtils.IsDeviceInProgress(device.getState())) {
            if (DeviceType.isChassis(device.getDeviceType())) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.chassisDeviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
            } else if (DeviceType.isServer(device.getDeviceType()) ||
                    device.getDeviceType().equals(DeviceType.AggregatorIOM)) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.bladeServerOrIOMDeviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
            } else {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                        AsmManagerMessages.deviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
            }
        }

        // In case of chasis need to fetch the associated device and so check if the status is PENDING_
        if (DeviceType.isChassis(device.getDeviceType())) {
            List<com.dell.pg.asm.chassis.client.device.Server> chassisServers = null;
            List<com.dell.pg.asm.chassis.client.device.IOM> chassisIoms = null;
            List<DeviceInventoryEntity> chassisRelatedDevices = new ArrayList<>();
            com.dell.pg.asm.chassis.client.device.Chassis chassisDevice = ProxyUtil.getDeviceChassisProxyWithHeaderSet().getChassis(device.getRefId());
            if (chassisDevice != null) {
                chassisServers = chassisDevice.getServers();
                chassisIoms = chassisDevice.getIOMs();
                if (chassisServers != null) {
                    for (com.dell.pg.asm.chassis.client.device.Server server : chassisServers) {
                        String servServiceTag = getDeviceInventoryDAO().getRefIdOfDevice(server.getServiceTag());
                        if (servServiceTag != null) {
                            DeviceInventoryEntity deviceEntityTemp = getDeviceInventoryDAO().getDeviceInventory(servServiceTag);
                            if (deviceEntityTemp != null) {
                                if (device.getState() == DeviceState.PENDING_DELETE) {
                                    if (isPendingDeleteJob(device.getRefId())) {
                                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                AsmManagerMessages.chassisDeviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
                                    }
                                }
                                if (device.getState() == DeviceState.PENDING_CONFIGURATION_TEMPLATE) {
                                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                            AsmManagerMessages.chassisDeviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
                                }
                            }
                            chassisRelatedDevices.add(deviceEntityTemp);
                        }
                    }
                }
                if (chassisIoms != null) {
                    for (com.dell.pg.asm.chassis.client.device.IOM iom : chassisIoms) {
                        String iomServiceTag = getDeviceInventoryDAO().getRefIdOfDevice(iom.getServiceTag());
                        if (iomServiceTag != null) {
                            DeviceInventoryEntity deviceEntityTemp = getDeviceInventoryDAO().getDeviceInventory(iomServiceTag);
                            if (deviceEntityTemp != null) {
                                if (device.getState() == DeviceState.PENDING_DELETE) {
                                    if (isPendingDeleteJob(device.getRefId())) {
                                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                                AsmManagerMessages.chassisDeviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
                                    }
                                }
                                if (device.getState() == DeviceState.PENDING_CONFIGURATION_TEMPLATE) {
                                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                                            AsmManagerMessages.chassisDeviceStateNotSupportedForDeleteOperation(device.getServiceTag(), device.getState().getValue()));
                                }
                            }
                            chassisRelatedDevices.add(deviceEntityTemp);
                        }
                    }
                }
                for(DeviceInventoryEntity relatedDevice: chassisRelatedDevices){
                    if(relatedDevice.getDeployments().size() > 0){
                        logger.error("Error occurred in deleting device: " + device.getDeviceType() + " "+device.getServiceTag() + " is currently in use by deployment(s)");
                        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.deviceInUse(device.getDeviceType().toString(), device.getServiceTag()));
                    }
                }
            }
        }

        return true;
    }

    private static boolean isPendingDeleteJob(String refId) {
        // get the active jobs for this device
        JrafJobExecutionContextList jobs = ProxyUtil.getJobManagerProxy().getCurrentlyExecutingJobs();
        if (jobs!=null && jobs.getJobExecutionContexts()!=null) {
            for (JrafJobExecutionContext context : jobs.getJobExecutionContexts()) {
                JrafJobKey jKey = context.getJobKey();
                try {
                    JobDetail jobDetail = JobManager.getInstance().getJobDetail(jKey.getName());
                    if(jobDetail == null) return false;
                    if (jobDetail.getJobClass().equals(DeleteDeviceJob.class)) {
                        if (jobDetail.getJobDataMap() != null) {
                            String jobDeviceRefId = (String) jobDetail.getJobDataMap().get(DeviceInventoryService.DEVICE_RA_ID);
                            if (jobDeviceRefId!=null && refId.equals(jobDeviceRefId)) {
                                return true;
                            }

                            List<String> listOfDeviceIds = (List) jobDetail.getJobDataMap().get(DeviceInventoryService.RELATED_DEVICE_RA_IDS);
                            if (listOfDeviceIds != null) {
                                for (String rId: listOfDeviceIds) {
                                    if (refId.equals(rId)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                } catch (JobManagerException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    private static RazorDevice getRazorDevice(String sNodeName) 
    {
        ObjectMapper mapper = new ObjectMapper();        
        WebClient client = WebClient.create(AsmManagerApp.razorApiUrl);
        client.accept("application/json");
        String json = client.path("collections/nodes").get(String.class);

        try 
        {
            String nodeJson = client.path(sNodeName).get(String.class);
            RazorDevice device = RazorUtil.parseNodeJson(mapper, nodeJson);
            return device;
        } catch (IOException e) {
            throw new AsmManagerRuntimeException(e);
        }
    }

    /**
     * When IOM discovered separately from chassis we meed to identify possible parent and update device.chassisId
     * @param serviceTag IOM
     */
    private String getChassisIDforIOM(String serviceTag) {
        try {
            Chassis chassis = getChassisService().getChassisByServiceTag(serviceTag, TagType.IOM.value());
            if (chassis != null) {
                return chassis.getRefId();
            }
        } catch (WebApplicationException wex) {
            if (wex.getResponse().getStatus() != Response.Status.NOT_FOUND.getStatusCode()) {
                logger.error("Unexpected error for getChassisByServiceTag", wex);
            }
        }
        return null;
    }

    /**
     * When chassis is discovered after its IOMs the switches will have empty chassis_id.
     * This utility will find such IOMs in ASM inventory and update them.
     * @param chassisId Chassis
     */
    private void searchAndUpdateChassisIDForIOMs(String chassisId) throws AsmManagerCheckedException {
        if (chassisId == null) {
            return;
        }
        Chassis chassis = null;
        try {
            chassis = getChassisService().getChassis(chassisId);
            if (chassis != null) {
                for (IOM iom: chassis.getIOMs()) {
                    DeviceInventoryEntity entity = getDeviceInventoryDAO().getDeviceInventoryByServiceTag(iom.getServiceTag());
                    if (entity != null && !chassisId.equals(entity.getChassisId())) {
                        entity.setChassisId(chassisId);
                        getDeviceInventoryDAO().updateDeviceInventory(entity);
                    }
                }
            }
        }catch(WebApplicationException wex) {
            logger.warn("Chassis not found by id=" + chassisId, wex);
        }
    }

    /**
     * For existing inventory update firmware from discovery results
     * @param deviceInfo
     * @throws AsmManagerCheckedException
     */
    public void updateInventory(DiscoveredDevices deviceInfo) throws AsmManagerCheckedException {
        try {
            if (StringUtils.isNotBlank(deviceInfo.getDisplayName())
                    || StringUtils.isNotBlank(deviceInfo.getFacts())
                    || StringUtils.isNotBlank(deviceInfo.getChassisId())
                    || CollectionUtils.isNotEmpty(deviceInfo.getFirmwareDeviceInventories())) {
                // for puppet devices also update device inventory with new facts
                DeviceInventoryEntity devInv = getDeviceInventoryDAO().getDeviceInventory(deviceInfo.getDeviceRefId());
                if (devInv != null && devInv.getRefId() != null) {
                    getDeviceInventoryDAO().deleteFirmwareDeviceInventoryForDevice(devInv.getRefId());

                    try {
                        // TODO: why do we need to call it twice??? here and below again.
                        getDeviceInventoryDAO().updateDeviceInventory(devInv);
                        devInv = getDeviceInventoryDAO().getDeviceInventory(devInv.getRefId());
                    } catch (AsmManagerCheckedException e) {
                        logger.error("Cannot update device_inventory for device: " + deviceInfo.getDeviceRefId(), e);
                    }

                    updateChassisId(deviceInfo, devInv);

                    if (StringUtils.isNotBlank(deviceInfo.getDisplayName())) {
                        devInv.setDisplayName(deviceInfo.getDisplayName());
                    }

                    if (StringUtils.isNotBlank(deviceInfo.getFacts())) {
                        devInv.setFacts(deviceInfo.getFacts());
                    }
                    if (devInv.getDeviceType() == null) {
                        devInv.setDeviceType(deviceInfo.getDeviceType());
                    }
                    if (CollectionUtils.isNotEmpty(deviceInfo.getFirmwareDeviceInventories())) {
                        for (final FirmwareDeviceInventory fdi : deviceInfo.getFirmwareDeviceInventories()) {
                            FirmwareDeviceInventoryEntity newFW = new FirmwareDeviceInventoryEntity(fdi);
                            newFW.setDeviceInventoryId(devInv.getRefId());
                            newFW.setId(null);//just in case
                            logger.debug("Adding firmware during DeviceInventoryJob run: " + newFW.getName());
                            getDeviceInventoryDAO().createFirmwareDeviceInventory(newFW);
                        }
                    } else {
                        logger.debug("No firmware found during inventory");
                    }

                    // everything seems fine, reset the state to OK if it was set to config error.
                    JobUtils.resetChassisStatus(devInv);

                    // must update DAO and get fresh reference before run compliance check
                    getDeviceInventoryDAO().updateDeviceInventory(devInv);
                    devInv = getDeviceInventoryDAO().getDeviceInventory(devInv.getRefId());

                    // Due to past corruption of the compliance_map table, we are currently resetting the compliance_map
                    // every time on an inventory run
                    getFirmwareUtil().updateComplianceMapsAndDeviceInventoryCompliance(devInv);

                    // Update the Service deployment if the device type is managed
                    getServiceDeploymentUtil().updateDevicesDeploymentsCompliance(devInv);
                } else {
                    logger.warn("No ASM inventory device found by refId = " + deviceInfo.getRefId() + ", IP=" +
                            deviceInfo.getIpAddress() + ", service tag=" + deviceInfo.getServiceTag());
                }

            } else {
                logger.debug("Missing critical data in discovered device, inventory update skipped for refId = " +
                        deviceInfo.getRefId() + ", IP=" +
                        deviceInfo.getIpAddress() + ", service tag=" + deviceInfo.getServiceTag());
            }
        } catch (AsmManagerCheckedException e) {
            logger.error("Cannot update device_inventory for device: " + deviceInfo.getDeviceRefId(), e);
        } catch(WebApplicationException wex) {
            logger.error("Update firmware for inventory run exception for device - " + deviceInfo.getDeviceRefId()
                    + ", service tag = " + deviceInfo.getServiceTag(), wex);
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error("Cannot run compliance check for service with deployed device: "
                    + deviceInfo.getDeviceRefId(), e);
        }
    }


    /**
     * For servers and IOMs update chassis ID.
     * For chassis, lookup ASM inventory IOMs and try to find matching to provided chassis. Update their chassis ID.
     *
     * @param deviceInfo device data from inventory
     * @param devInv    device to update
     * @throws AsmManagerCheckedException
     */
    public void updateChassisId(DiscoveredDevices deviceInfo, DeviceInventoryEntity devInv) throws AsmManagerCheckedException {
        if (StringUtils.isNotBlank(deviceInfo.getChassisId())) {
            // just pass by
            devInv.setChassisId(deviceInfo.getChassisId());
        }else if (DeviceType.isSwitch(deviceInfo.getDeviceType())) {
            // possibly case: IOM discovered alone, look for parent chassis
            String chassisId = getChassisIDforIOM(deviceInfo.getServiceTag());
            if (chassisId != null) {
                devInv.setChassisId(chassisId);
            }
        }else if (DeviceType.isChassis(deviceInfo.getDeviceType())) {
            // this is chassis, we might have orphan IOMs in the inventory
            searchAndUpdateChassisIDForIOMs(deviceInfo.getDeviceRefId());
        }
    }

    /**
     * For servers and IOMs update chassis ID.
     * For chassis, lookup ASM inventory IOMs and try to find matching to provided chassis. Update their chassis ID.
     *
     * @param deviceInfo device data from inventory
     * @param devInv    device to update
     * @throws AsmManagerCheckedException
     */
    public void updateChassisId(DiscoveredDevices deviceInfo, ManagedDevice devInv) throws AsmManagerCheckedException {
        if (StringUtils.isNotBlank(deviceInfo.getChassisId())) {
            devInv.setChassisId(deviceInfo.getChassisId());
        }else if (DeviceType.isSwitch(deviceInfo.getDeviceType())) {
            String chassisId = getChassisIDforIOM(deviceInfo.getServiceTag());
            if (chassisId != null) {
                devInv.setChassisId(chassisId);
            }
        }else if (DeviceType.isChassis(deviceInfo.getDeviceType())) {
            searchAndUpdateChassisIDForIOMs(deviceInfo.getDeviceRefId());
        }
    }

    public DeviceInventoryDAO getDeviceInventoryDAO() {
        if (deviceInventoryDAO1 == null) {
            deviceInventoryDAO1 = new DeviceInventoryDAO();
        }
        return deviceInventoryDAO1;
    }

    public void setDeviceInventoryDAO(DeviceInventoryDAO deviceInventoryDAO) {
        this.deviceInventoryDAO1 = deviceInventoryDAO;
    }

    public FirmwareUtil getFirmwareUtil() {
        if (firmwareUtil1 == null) {
            firmwareUtil1 = new FirmwareUtil();
        }
        return firmwareUtil1;
    }

    public void setFirmwareUtil(FirmwareUtil firmwareUtil) {
        this.firmwareUtil1 = firmwareUtil;
    }

    public ServiceDeploymentUtil getServiceDeploymentUtil() {
        if (serviceDeploymentUtil == null) {
            serviceDeploymentUtil = new ServiceDeploymentUtil();
        }
        return serviceDeploymentUtil;
    }

    public void setServiceDeploymentUtil(ServiceDeploymentUtil serviceDeploymentUtil) {
        this.serviceDeploymentUtil = serviceDeploymentUtil;
    }

    public IChassisService getChassisService() {
        if (chassisService == null) {
            chassisService = ProxyUtil.getDeviceChassisProxyWithHeaderSet();
        }
        return chassisService;
    }

    public void setChassisService(IChassisService chassisService) {
        this.chassisService = chassisService;
    }

    /**
     * Checks if this device was discovered as a part of chassis discovery.
     * Such devices would have chassis credential ID.
     * @param deviceInventoryEntity
     * @return
     */
    public boolean isChassisDiscoveryDevice(DeviceInventoryEntity deviceInventoryEntity) {
        if (deviceInventoryEntity == null) {
            return false;
        }

        if (StringUtils.isEmpty(deviceInventoryEntity.getChassisId())) {
            return false;
        }
        if (deviceInventoryEntity.getCredId() == null) {
            return false;
        }

        DeviceInventoryEntity chassis = getDeviceInventoryDAO().getDeviceInventory(deviceInventoryEntity.getChassisId());
        if (chassis == null) {
            return false;
        }

        String iomCred = findIOMCredentialFromChassis(chassis);

        return deviceInventoryEntity.getCredId().equals(iomCred);
    }

    /**
     * From Chassis entity get the config, extract IOM credentials
     * @param entity
     * @return
     */
    public String findIOMCredentialFromChassis(DeviceInventoryEntity entity) {
        if (entity == null) {
            return null;
        }
        String config = entity.getConfig();
        if (config!=null) {
            ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, config);
            for (ServiceTemplateComponent component: template.getComponents()) {
                for (ServiceTemplateCategory resource: component.getResources()) {
                    if (resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID)) {
                        return getIOMCredentialFromConfigurationTemplate(component);
                    }
                }
            }
        }
        return null;
    }

    /**
     * From Chassis entity get the config, extract blade credentials
     * @param entity
     * @return
     */
    public String findBladeCredentialFromChassis(DeviceInventoryEntity entity) {
        if (entity == null) {
            return null;
        }
        String config = entity.getConfig();
        if (config!=null) {
            ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, config);
            for (ServiceTemplateComponent component: template.getComponents()) {
                for (ServiceTemplateCategory resource: component.getResources()) {
                    if (resource.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID)) {
                        return getServerCredentialFromConfigurationTemplate(component);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get Chassis Credential From Configuration Template
     * @param component
     * @return
     */
    public String getChassisCredentialFromConfigurationTemplate(ServiceTemplateComponent component) {
        if (component == null) {
            return null;
        }
        for (ServiceTemplateCategory category: component.getResources()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID.equals(category.getId())) {
                for (ServiceTemplateSetting setting: category.getParameters()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_CMC_CRED.equals(setting.getId())) {
                        return setting.getValue();
                    }
                }
            }
        }
        logger.error("Invalid configuration template: no credential ID for CMC, service tag:" + component.getComponentID());
        throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
    }

    /**
     * Get Server Credential From Configuration Template
     * @param component
     * @return
     */
    public String getServerCredentialFromConfigurationTemplate(ServiceTemplateComponent component) {
        if (component == null) {
            return null;
        }
        for (ServiceTemplateCategory category: component.getResources()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID.equals(category.getId())) {
                for (ServiceTemplateSetting setting: category.getParameters()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_CRED.equals(setting.getId())) {
                        return setting.getValue();
                    }
                }
            }
        }
        logger.error("Invalid configuration template: no credential ID for IDRAC, service tag:" + component.getComponentID());
        throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
    }

    /**
     * Get Rack Server Credential From Configuration Template
     * @param component
     * @return
     */
    public String getRackServerCredentialFromConfigurationTemplate(ServiceTemplateComponent component) {
        if (component == null) {
            return null;
        }
        for (ServiceTemplateCategory category: component.getResources()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_SERVER_ID.equals(category.getId())) {
                for (ServiceTemplateSetting setting: category.getParameters()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IDRAC_CRED.equals(setting.getId())) {
                        return setting.getValue();
                    }
                }
            }
        }
        logger.error("Invalid configuration template: no credential ID for IDRAC, service tag:" + component.getComponentID());
        throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
    }

    /**
     * Get IOM Credential From Configuration Template
     * @param component
     * @return
     */
    public String getIOMCredentialFromConfigurationTemplate(ServiceTemplateComponent component) {
        if (component == null) {
            return null;
        }
        for (ServiceTemplateCategory category: component.getResources()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID.equals(category.getId())) {
                for (ServiceTemplateSetting setting: category.getParameters()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_IOM_CRED.equals(setting.getId())) {
                        return setting.getValue();
                    }
                }
            }
        }
        logger.error("Invalid configuration template: no credential ID for IOM, service tag:" + component.getComponentID());
        throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
    }

    /**
     * Find device by service tag
     * @param serviceTag
     * @return
     */
    public DeviceInventoryEntity findDeviceInventoryByServiceTag(String serviceTag) {
        return getDeviceInventoryDAO().getDeviceInventoryByServiceTag(serviceTag);
    }

}
