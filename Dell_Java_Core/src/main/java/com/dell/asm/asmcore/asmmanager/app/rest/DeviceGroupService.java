/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.admin.rest.UserResource;
import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.DeviceGroup;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.GroupUser;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.GroupUserList;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.IDeviceGroupService;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDeviceList;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.DeviceGroupUtil;
import com.dell.asm.asmcore.asmmanager.util.DeviceInventoryUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.user.model.IUserResource;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.model.Link;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.RestUtil;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.asm.usermanager.db.entity.UserEntity;
import com.dell.pg.orion.common.context.ServiceContext;

/**
 * Device Group Service implementation for ASM Manager.
 *
 */
@Path("/DeviceGroup")
public class DeviceGroupService implements IDeviceGroupService {
    private static final Logger LOGGER = Logger.getLogger(DeviceGroupService.class);

    @Context
    private HttpServletRequest servletRequest;

    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;

    private static final Set<String> validDeviceGroupSortColumns = new HashSet<>();
    private static final Set<String> validDeviceGroupFilterColumns = new HashSet<>();
    private static int MAX_GLOBAL_SERVERS = 1000000; 
    

    static {

        validDeviceGroupSortColumns.add("name");
        validDeviceGroupSortColumns.add("description");
        validDeviceGroupSortColumns.add("createdBy");
        validDeviceGroupSortColumns.add("createdDate");
        validDeviceGroupSortColumns.add("updatedBy");
        validDeviceGroupSortColumns.add("updatedDate");
    }

    static {

        validDeviceGroupFilterColumns.add("name");
        validDeviceGroupFilterColumns.add("description");
        validDeviceGroupFilterColumns.add("createdBy");
        validDeviceGroupFilterColumns.add("createdDate");
        validDeviceGroupFilterColumns.add("updatedBy");
        validDeviceGroupFilterColumns.add("updatedDate");
        validDeviceGroupFilterColumns.add("users");
        validDeviceGroupFilterColumns.add("devices");
    }

    private DeviceGroupDAO deviceGroupDAO = DeviceGroupDAO.getInstance();
    private DeviceInventoryDAO deviceInventoryDAO = new DeviceInventoryDAO();
    private DeviceInventoryService deviceInventoryService = new DeviceInventoryService();
    private UserResource userResource = new UserResource();

    /**
     * Create Group along with device and user in Inventory.
     *
     * @param group
     *            the device group object to be created in inventory.
     *
     * @return device group
     */
    @Override
    public DeviceGroup createDeviceGroup(DeviceGroup group) {

        DeviceGroup groupResponse = null;
        try {
        	if(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_NAME.equalsIgnoreCase(group.getGroupName())) {
        		throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
        				AsmManagerMessages.duplicateDeviceGroupName(group.getGroupName()));	
        	} else {
        		DeviceGroupEntity entity = DeviceInventoryUtils.toEntity(group, true, true);
        		DeviceGroupUtil.validateField(entity.getName());
        		DeviceGroupUtil.validateDeviceObject(entity);
        		DeviceGroupUtil.validateUserObject(entity);

        		DeviceGroupEntity createdEntity = deviceGroupDAO.createGroupDevice(entity);
        		List<UserEntity> userList = DeviceGroupUtil.getDeviceGroupUsers(new ArrayList<Long>(createdEntity.getGroupsUsers()));
        		groupResponse = DeviceInventoryUtils.toDTO(createdEntity, userList, true, true);
        		refreshLinks(groupResponse); 
        	}
        } catch (AsmManagerCheckedException adex) {
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID
                    || adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD) {
                throw new LocalizedWebApplicationException(Response.Status.CONFLICT, adex.getEEMILocalizableMessage());
            }
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.INVALID_ID) {
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, adex.getEEMILocalizableMessage());
            }
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, adex.getEEMILocalizableMessage());
            }
            String msg = "Create Device Group" + adex.getMessage();
            LOGGER.error(msg, adex);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

        return groupResponse;
    }

    /**
     * Retrieve all Device Group from Inventory.
     *
     *  @return list of device group
     */
    @Override
    public DeviceGroup[] getAllDeviceGroup(String sort, List<String> filter, Integer offset, Integer limit) {

        ArrayList<DeviceGroup> deviceGroupList = new ArrayList<DeviceGroup>();

        try {
            // Add the global one first
            deviceGroupList.add(this.getGlobalServerPool());
            
            // Parse the sort parameter.
            // Any sort exceptions are already in case in a WebApplicationException with an Status code=400
            SortParamParser sp = null;
            if (null == sort || "".equals(sort)) {
                sp = new SortParamParser("name", validDeviceGroupSortColumns);
            } else {
                sp = new SortParamParser(sort, validDeviceGroupSortColumns);
            }
            List<SortParamParser.SortInfo> sortInfos = sp.parse();

            // Build filter list from filter params ( comprehensive )
            FilterParamParser filterParser = new FilterParamParser(filter, validDeviceGroupFilterColumns);
            List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

            int totalRecords =0;

            List<DeviceGroupEntity> resultList = deviceGroupDAO.getAllDeviceGroup(sortInfos, filterInfos, null);
            if( resultList != null ) {
                for (DeviceGroupEntity entity : resultList)
                {
                    if (checkUserPermissions(entity)) {
                        totalRecords++;
                    }
                }
            }

            if (totalRecords > 0) { // get paginated results only if there are any records
                PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest, servletResponse, httpHeaders, uriInfo);
                int pageOffSet;
                if (offset == null) {
                    pageOffSet = 0;
                } else {
                    pageOffSet = offset;
                }

                int pageLimit;
                if (limit == null) {
                    pageLimit = paginationParamParser.getMaxLimit();
                } else {
                    pageLimit = limit;
                }

                PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, totalRecords);

                boolean fetchDevices = true;
                boolean fetchUsers = true;

                for (FilterParamParser.FilterInfo filterInfo : filterInfos) {

                    if ("users".equals(filterInfo.getColumnName())) {
                        if (filterInfo.getColumnValue().contains("false"))
                            fetchUsers = false;
                    }

                    if ("devices".equals(filterInfo.getColumnName())) {
                        if (filterInfo.getColumnValue().contains("false"))
                            fetchDevices = false;
                    }

                }
                resultList = deviceGroupDAO.getAllDeviceGroup(sortInfos, filterInfos, paginationInfo);
                if( resultList != null )
                {
                    Set<Long> invalidIdsSet = new HashSet<>();

                    for (DeviceGroupEntity entity : resultList) 
                    {
                        if (!checkUserPermissions(entity)) {
                            continue;
                        }

                        if (!fetchDevices)
                            entity.setDeviceInventories(null);

                        if (!fetchUsers)
                            entity.setGroupsUsers(null);

                        List<UserEntity> userList = entity.getGroupsUsers() != null ? DeviceGroupUtil.getDeviceGroupUsers(new ArrayList<Long>(entity
                                .getGroupsUsers())) : null;
                        DeviceGroup groupResponse = DeviceInventoryUtils.toDTO(entity, userList, fetchDevices, fetchUsers);
                        Set<Long> set = DeviceGroupUtil.invalidGroupUserIds(entity);
                        invalidIdsSet.addAll(set);
                        refreshLinks(groupResponse);
                        deviceGroupList.add(groupResponse);

                    }

                    DeviceGroupUtil.deleteGroupUsersAssociationFromDB(invalidIdsSet);
                }
                if (httpHeaders!=null && servletResponse!=null) {
                    UriBuilder linkBuilder = RestUtil.getProxyBaseURIBuilder(uriInfo, servletRequest, httpHeaders);

                    linkBuilder.replaceQuery(null);
                    linkBuilder.path("/devicegroupservice");
                    if (sort != null) {
                        linkBuilder.queryParam(AsmConstants.QUERY_PARAM_SORT, sort);
                    }

                    for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
                        linkBuilder.queryParam(AsmConstants.QUERY_PARAM_FILTER, filterInfo.buildValueString());
                    }

                    servletResponse.addHeader(AsmConstants.DELL_TOTAL_COUNT_HEADER, Integer.toString(totalRecords));
                    // Common library to add link headers in response headers
                    paginationParamParser.addLinkHeaders(paginationInfo, linkBuilder);
                }
            } 
        } catch (AsmManagerCheckedException adex) {
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND) {
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, adex.getEEMILocalizableMessage());
            }
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, adex.getEEMILocalizableMessage());
            }
            String msg = "Fetch Device Groups" + adex.getMessage();
            LOGGER.error(msg, adex);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }
        return deviceGroupList.toArray(new DeviceGroup[deviceGroupList.size()]);
    }

    /**
     * Retrieve Device Group from Inventory based on refId.
     *
     * @param refId
     *            Group's refId
     *
     *  @return device group
     */
    @Override
    public DeviceGroup getDeviceGroup(String refId) {

        DeviceGroup groupResponse = null;
        try {

            DeviceGroupUtil.validateField(refId);
            DeviceGroupUtil.validateInputInteger(refId);
            DeviceGroupEntity entity = deviceGroupDAO.getDeviceGroupById(Long.parseLong(refId));

            if (!checkUserPermissions(entity)) {
                LOGGER.debug("Refused access to server pool ID=" + refId + " for user " + getUserId() + " because of lack of permissions");
                throw new LocalizedWebApplicationException(
                        Response.Status.NOT_FOUND,
                        AsmManagerMessages.serverPoolNotFound(refId));
            }

            List<UserEntity> userList = DeviceGroupUtil.getDeviceGroupUsers(new ArrayList<Long>(entity.getGroupsUsers()));
            groupResponse = DeviceInventoryUtils.toDTO(entity, userList, true, true);

            DeviceGroupUtil.deleteGroupUsersAssociation(entity);
            refreshLinks(groupResponse);

        } catch (AsmManagerCheckedException adex) {
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, adex.getEEMILocalizableMessage());
            }
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND) {
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, adex.getEEMILocalizableMessage());
            }

            String msg = "Fetch Device Groups" + adex.getMessage();
            LOGGER.error(msg, adex);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

        return groupResponse;
    }

    /**
     * Update Device Group in Inventory.
     *
     * @param refId
     *            the refId of the device group to be removed from inventory.
     *
     * @param group
     *            the device group object to be updated.
     *
     *  @return device group
     */
    @Override
    public DeviceGroup updateDeviceGroup(String refId, DeviceGroup group) {

    	DeviceGroup groupResponse = null;
    	try {
    		if(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_NAME.equalsIgnoreCase(group.getGroupName())) {
    			throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_NAME,
    					AsmManagerMessages.duplicateDeviceGroupName(group.getGroupName()));	
    		} else {
    			DeviceGroupUtil.validateField(refId);
    			DeviceGroupEntity entity = DeviceInventoryUtils.toEntity(group, true, true);
    			DeviceGroupUtil.validateField(entity.getName());
    			DeviceGroupUtil.validateDeviceObject(entity);
    			DeviceGroupUtil.validateUserObject(entity);
    			entity.setSeqId(Long.parseLong(refId));

    			DeviceGroupEntity createdEntity = deviceGroupDAO.updateGroupDevice(entity);
    			List<UserEntity> userList = DeviceGroupUtil.getDeviceGroupUsers(new ArrayList<Long>(createdEntity.getGroupsUsers()));
    			groupResponse = DeviceInventoryUtils.toDTO(createdEntity, userList, true, true);
    			refreshLinks(groupResponse);
    		}

    	} catch (AsmManagerCheckedException adex) {
    		if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
    			throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, adex.getEEMILocalizableMessage());
    		}
    		if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.INVALID_ID) {
    			throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, adex.getEEMILocalizableMessage());
    		}
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.DUPLICATE_NAME) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, adex.getEEMILocalizableMessage());
            }
    		String msg = "Create Device Group" + adex.getMessage();
    		LOGGER.error(msg, adex);
    		throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
    	}

    	return groupResponse;
    }

    /**
     * Delete Device Group from Inventory based on refId.
     *
     * @param refId
     *            the refId of the device group to be removed from inventory.
     */
    @Override
    public Response deleteDeviceGroup(String refId) {
        Long deviceGroupId = null;
        try {
            deviceGroupId = Long.parseLong(refId);
        } catch (NumberFormatException nfe) {
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.invalidDeviceGroupRequest(refId));
        }

        DeviceGroupEntity deviceGroupEntity = null;
        try {
            deviceGroupEntity = deviceGroupDAO.getDeviceGroupById(deviceGroupId);

        } catch (AsmManagerCheckedException adex) {
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
                throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, adex.getEEMILocalizableMessage());
            }
            if (adex.getReasonCode() == AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND) {
                throw new LocalizedWebApplicationException(Response.Status.NOT_FOUND, adex.getEEMILocalizableMessage());
            }
            String msg = "Fetch Device Group" + adex.getMessage();
            LOGGER.error(msg, adex);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }

        if (deviceGroupEntity == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        deviceGroupDAO.deleteDeviceGroup(deviceGroupEntity);

        return Response.noContent().build();
    }

    private DeviceGroup refreshLinks(DeviceGroup group, UriBuilder baseUriBuilder) {
        URI uri;

        synchronized (group) {
            // self URI
            uri = RestUtil.getAbsoluteUri(baseUriBuilder, "DeviceGroup", Long.toString(group.getGroupSeqId()));
            group.setLink(new Link(group.getGroupName(), uri.toString(), Link.RelationType.SELF));
        }

        return group;
    }

    private DeviceGroup refreshLinks(DeviceGroup group) {
        if (httpHeaders != null)
            return refreshLinks(group, RestUtil.getProxyBaseURIBuilder(uriInfo, servletRequest, httpHeaders));
        else
            return group;
    }

    /**
     * Check is logged user is allowed to access this deployment.
     * @param entity
     * @return
     */
    private boolean checkUserPermissions(DeviceGroupEntity entity) {
        Long userId= getUserId();
        User thisUser = null;
        // TODO: remove when asm_deployer gets REST headers
        if (userId == 0) {
            userId = (long) 1;
        }

        IUserResource adminProxy = ProxyUtil.getAdminProxy();
        ProxyUtil.setProxyHeaders(adminProxy, servletRequest);

        thisUser = adminProxy.getUser(userId);

        if (thisUser.getRole().equals(AsmConstants.USERROLE_READONLY)) return true;
        if (thisUser.getRole().equals(AsmConstants.USERROLE_ADMINISTRATOR)) return true;

        for (Long ref: entity.getGroupsUsers()) {
            if (userId ==  ref) {
                return true;
            }
        }

        return false;
    }

    private long getUserId() {
        try {
            ServiceContext.Context sc = ServiceContext.get();
            if (sc.getApiKey()==null) {
                // lost securitycontext
                LOGGER.error("Lost SecurityContext.");
                return (long) 0;
            }
            return sc.getUserId();
        } catch (Exception e) {

            LOGGER.error("Unable to get user context", e);
            return (long) 0;
        }
    }
    
    private DeviceGroup getGlobalServerPool() {
        DeviceGroup gsp = new DeviceGroup();
        gsp.setGroupName(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_NAME);
        gsp.setGroupSeqId(Long.parseLong(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID));
        gsp.setManagedDeviceList(new ManagedDeviceList());
        gsp.getManagedDeviceList().setManagedDevices(new ArrayList<ManagedDevice>());

        List<String> filter = new ArrayList<String>();
        filter.add("eq,deviceType,RackServer, TowerServer, BladeServer, Server, FXServer");
        filter.add("eq,serverpool,"+ ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID);
        ManagedDevice[] mdList = deviceInventoryService.getAllDeviceInventory(null, filter, 0, MAX_GLOBAL_SERVERS);
        
        if (mdList!=null) {
            for (ManagedDevice dto : mdList) {
                gsp.getManagedDeviceList().getManagedDevices().add(dto);
            }
        }

        // The Global Pool must list ALL users that are in the system
        User[] globalPoolUsers = this.userResource.getUsers(null,  null,  null,  null);
        List<GroupUser> globalGroupUsers = DeviceInventoryUtils.userToGroupUserDTO(globalPoolUsers);
        GroupUserList groupUserList = new GroupUserList();
        groupUserList.setGroupUsers(globalGroupUsers);
        groupUserList.setTotalRecords(globalGroupUsers.size());
        gsp.setGroupUserList(groupUserList);
        
        return gsp;
    }
    

}