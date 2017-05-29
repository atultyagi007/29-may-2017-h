/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.devicegroup;

import java.net.HttpURLConnection;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Device Group Service interface for ASM Manager.
 * 
 */
@Path("/DeviceGroup")
@Api("/DeviceGroup")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface IDeviceGroupService {
    /**
     * Create Group along with device and user in Inventory.
     * 
     * @param group
     *            the device group object to be created in inventory.
     *            
     * @return device group
     */
    @POST
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @ApiOperation(notes ="minimum role allowed: Administrator", value = "Create Device Group in Inventory", response = DeviceGroup.class)
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Device Group created in inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Device Group object to create in inventory, first error encountered error causes an error response to return. Call will not return a list of errors."),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Group already exists in inventory, first error encountered error causes an error response to return. Call will not return a list of errors.") })
    DeviceGroup createDeviceGroup(DeviceGroup group);

    /**
     * Retrieve all Device Group from Inventory.
     * 
     *  @return list of device group
     */
    @GET
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes ="minimum role allowed: ReadOnly", value = "Retrieve all Device Group from Inventory with filter, sort, paginate, return Device Group which contains list of Managed Devices and User if exist", response = DeviceGroup.class , responseContainer = "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device Group retrieved from inventory on filter, sort, paginate") })
    DeviceGroup[] getAllDeviceGroup(
            @ApiParam(value="Valid sort columns: name,description,createdBy,createdDate,updatedBy,updatedDate") @QueryParam(AsmConstants.QUERY_PARAM_SORT) String sort,
            @ApiParam("Valid filter columns: name,description,users,devices,createdBy,updatedBy,createdDate,updatedDate") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter,
            @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam(AsmConstants.QUERY_PARAM_OFFSET) Integer offset,
            @ApiParam("Page Limit") @DefaultValue("50") @QueryParam(AsmConstants.QUERY_PARAM_LIMIT) Integer limit);


    /**
     * Retrieve Device Group from Inventory based on refId.
     * 
     * @param refId
     *            Group's refId
     *            
     *  @return device group
     */
    @GET
    @Path("/{refId}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes ="minimum role allowed: ReadOnly", value = "Retrieve Device Group from Inventory based on refId", response = DeviceGroup.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device Group retrieved from inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device Group not found in inventory") })
    DeviceGroup getDeviceGroup(@ApiParam("Reference Id") @PathParam("refId") String refId);

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
    @PUT
    @Path("/{refId}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @ApiOperation(notes ="minimum role allowed: Administrator", value = "Update Device Group in Inventory", response = DeviceGroup.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Device Group updated in inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Device group id"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device Group not found in inventory") })
    DeviceGroup updateDeviceGroup(@ApiParam("Reference Id") @PathParam("refId") String refId,
            @ApiParam("Device Group to be updated") DeviceGroup group);

    /**
     * Delete Device Group from Inventory based on refId.
     * 
     * @param refId
     *            the refId of the device group to be removed from inventory.
     */
    @DELETE
    @Path("/{refId}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @ApiOperation(notes ="minimum role allowed: Administrator", value = "Delete Device Group from Inventory", response = Response.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Device Group deleted from inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Unable to delete Device Group from inventory") })
    Response deleteDeviceGroup(@ApiParam("Reference Id") @PathParam("refId") String refId);
}
