/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareDeviceInventory;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This Device Inventory Service for ASM Manager.
 * 
 */
@Path("/ManagedDevice")
@Api("/ManagedDevice")
public interface IDeviceInventoryService {
    /**
     * Create Managed Devices in Inventory.
     * 
     * @param devices
     *            the devices to be created in inventory.
     */
    @POST
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: Administrator",
            value = "Create Device in Inventory, return array of Managed Devices created",
            response = ManagedDevice.class, responseContainer= "List")
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Devices created in inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Device to create in inventory, first error encountered error causes an error response to return. Call will not return a list of errors."),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Device already exists in inventory, first error encountered error causes an error response to return. Call will not return a list of errors.") })
    ManagedDevice[] createDeviceInventory(ManagedDevice[] devices);

    /**
     * Retrieve all Managed Devices in Inventory.
     */
    @GET
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: ReadOnly. Operation support filters, sorting and pagination."
    		+ " See Parameters section for details. Note that system enforces a default"
            + " pagination if result set reaches a system max of 50 objects. Caller needs to check"
            + " Link pagination headers in the response to see if pagination occurred.", value = "Retrieve all Devices from Inventory with filter.",
            response = ManagedDevice.class, responseContainer = "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device(s) retrieved from inventory on filter, sort, paginate") })
    ManagedDevice[] getAllDeviceInventory(
            @ApiParam(value="Valid sort columns: displayName,serviceTag,refId,health,refType,deviceType,ipAddress,state,model,statusMessage,createdDate,createdBy,updatedDate,updatedBy,healthMessage,compliant,infraTemplateDate,infraTemplateId,serverTemplateDate,serverTemplateId,inventoryDate,complianceCheckDate,discoveredDate,identityRef,vendor") @QueryParam(AsmConstants.QUERY_PARAM_SORT) String sort,
            @ApiParam("Valid filter columns: displayName,serviceTag,refId,health,refType,deviceType,ipAddress,state,model,statusMessage,createdDate,createdBy,updatedDate,updatedBy,healthMessage,compliant,infraTemplateDate,infraTemplateId,serverTemplateDate,serverTemplateId,inventoryDate,complianceCheckDate,discoveredDate,identityRef,vendor,credId,service") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter,
            @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam(AsmConstants.QUERY_PARAM_OFFSET) Integer offset,
            @ApiParam("Page Limit") @DefaultValue("50") @QueryParam(AsmConstants.QUERY_PARAM_LIMIT) Integer limit);


    /**
     * Retrieve all Managed Devices in Inventory.
     */
    @GET
    @Path("/withcompliance")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: ReadOnly. Operation support filters, sorting and pagination."
            + " See Parameters section for details. Note that system enforces a default"
            + " pagination if result set reaches a system max of 50 objects. Caller needs to check"
            + " Link pagination headers in the response to see if pagination occurred.", value = "Retrieve all Devices from Inventory with filter.",
            response = ManagedDevice.class, responseContainer = "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device(s) retrieved from inventory on filter, sort, paginate") })
    ManagedDevice[] getAllDeviceInventoryWithComplianceCheck(@ApiParam("Sort Column") @QueryParam(AsmConstants.QUERY_PARAM_SORT) String sort,
                                          @ApiParam("Filter Criteria") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter,
                                          @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam(AsmConstants.QUERY_PARAM_OFFSET) Integer offset,
                                          @ApiParam("Page Limit") @DefaultValue("50") @QueryParam(AsmConstants.QUERY_PARAM_LIMIT) Integer limit);

    /**
     * Retrieve Managed Device based on refId.
     * 
     * @param refId
     */
    @GET
    @Path("/{refId}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Device from Inventory based on refId", response = ManagedDevice.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device retrieved from inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device not found in inventory") })
    ManagedDevice getDeviceInventory(@ApiParam("Reference Id") @PathParam("refId") String refId);

    /**
     * Update Device Inventory based on given deviceInventory.
     * 
     * @param newDeviceInventory
     *            the device inventory to be updated.
     */
    @PUT
    @Path("/{refId}")
    @RolesAllowed({ AsmConstants.USERROLE_OPERATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Update Device in Inventory")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Device updated in inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Device"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device not found in inventory") })
    UpdateDeviceInventoryResponse updateDeviceInventory(@ApiParam("Reference Id") @PathParam("refId") String refId,
            @ApiParam("Device to be updated to") ManagedDevice newDeviceInventory);
    
    
    /**
     * Update Device Inventory based on given deviceInventory.
     * 
     * @param request - the firmware update request
     *            the device inventory to be updated.
     */
    @PUT
    @Path("/firmware")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Update Device Firmware")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Device updated Firmware"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Device"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device not found in inventory") })
    Response updateDeviceFirmware(FirmwareUpdateRequest request);

    /**
     * Delete Device Inventory based on refId.
     * 
     * @param refId
     *            the refId of the device to be removed from inventory.
     */
    @DELETE
    @Path("/{refId}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Delete Device from Inventory -- this operation is idempotent", response = ManagedDevice.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Device deleted from inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Unable to delete from RA's inventory") })
    Response deleteDeviceInventory(@ApiParam("Reference Id") @PathParam("refId") String refId, @ApiParam("Force Delete") @DefaultValue("false") @QueryParam("forceDelete") boolean forceDelete);


    /**
     * Retrieve Managed Device based on Puppet certificate name.
     *
     * @param certName
     */
    @GET
    @Path("/puppet/{certName}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Device from Inventory based on certName", response = ManagedDevice.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device retrieved from inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device not found in inventory") })
    ManagedDevice getDeviceInventoryByCertName(@ApiParam("Certificate Name") @PathParam("certName") String certName);

    /**
     * Update Device Inventory based on given deviceInventory and Puppet certificate name.
     *
     * @param newDeviceInventory
     *            the device inventory to be updated.
     */
    @PUT
    @Path("/puppet/{certName}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Update Device in Inventory")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Device updated in inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Device"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device not found in inventory") })
    UpdateDeviceInventoryResponse updateDeviceInventoryByCertName(@ApiParam("Certificate Name") @PathParam("certName") String certName,
                                                                  @ApiParam("Device to be updated to") ManagedDevice newDeviceInventory);

    /**
     * Retrieve Managed Device Count.
     */
    @GET
    @Path("/count")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.TEXT_PLAIN })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Device total count", response = ManagedDevice.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device Count retrieved from inventory")})
    public Integer getDeviceTotalCount(@ApiParam("Filter Criteria") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter);

    @GET
    @Path("/export/csv")
    @RolesAllowed(AsmConstants.USERROLE_READONLY)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Exports all Devices in csv format")
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify input parameters are correct", response = AsmDetailedMessageList.class),
            @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = "User doesn't have privileges to access this operation"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "No login information specified in the request"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "User Log Internal Error, contact your system administrator") })
    public Response exportAllDevices() throws WebApplicationException;
    
    
    @GET
    @Path("/{refId}/firmware/compliancereport/")
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Returns a FirmwareComplianceReports for the Device.", response = FirmwareComplianceReport.class)
    @RolesAllowed(AsmConstants.USERROLE_READONLY)
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = "User doesn't have privileges to access this operation"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "No login information specified in the request"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error, contact your system administrator") })
    public FirmwareComplianceReport getFirmwareComplianceReportForDevice (@PathParam("refId") String refId) throws WebApplicationException;

    /**
     * Update Device Inventory based on device inventory ids provided.  If no device inventory ids
     * are provided, all device inventories will be updated.
     */
    @POST
    @Path("/update")
    @RolesAllowed({ AsmConstants.USERROLE_OPERATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: Operator", value = "Update Device in Inventory")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Device Inventory jobs scheduled"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Device"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device not found in inventory") })
    public UpdateDeviceInventoryResponse[] updateDeviceInventories(@ApiParam("Device Inventory Ids") @QueryParam("deviceId") List<String> deviceIds);
    
    /**
     * Retrieve Device Firmware Inventory for the given refId.
     * 
     * @param refId
     */
    @GET
    @Path("{refId}/firmware/")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Device Firmware from Inventory based on refId", response = FirmwareDeviceInventory.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device Firmware retrieved from inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device not found in inventory") })
    public FirmwareDeviceInventory[] getFirmwareDeviceInventory(@ApiParam("Reference Id") @PathParam("refId") String refId);

}
