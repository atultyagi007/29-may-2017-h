/**************************************************************************
 *   Copyright (c) 2015 - 2016 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.firmwarerepository;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareBundle;
import com.dell.asm.asmcore.asmmanager.client.firmware.SoftwareComponent;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.pg.asm.server.client.device.FirmwareInventory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Firmware repository Service for ASM Manager.<br>
 * <br>
 * There are three types of Firmware Repositories: <br>
 *  - Embedded ASM Firmware Repository<br>
 *  - Default Firmware Repository <br>
 *  - Service Level Firmware Repository <br>
 *  <br>
 * ASM ships with an Embedded Firmware Repository.  This is a repository that all devices are checked against.  If a 
 * device is below the minimum firmware level listed in the embedded ASM firmware repository then the device will be 
 * marked as Upgrade Required.  This is the ONLY way a device may be marked as Upgrade Required.  It's required
 * as this is the minimum version for devices that ASM has been validated to work against. <br>
 * <br>
 * The Default Firmware Repository is a repository that is applied to all devices that are either not in a Service
 * or in a Service that does not have a Service Level Firmware Repository.  If a device is below the minimum firmware 
 * level listed in the Default Firmware Repository then the device will be marked as Non-Compliant (out of compliance).
 * <br> <br>
 * The Service Level Firmware Repository is a repository that is applied only to Servers that are in the Service to 
 * which the Service Level Firmware Repository is assigned.  If a device is below the minimum firmware level listed in 
 * the Service level Firmware Repository then the device will be marked as Non-Compliant.  When a Service Level Firmware
 * Repository is assigned to a Service then the firmware validation will only be checked against the Service Level 
 * Firmware Repository and the Default Firmware Repository checks will no longer be applied to the devices associated 
 * with the Service.  
 */
@Path("/firmwareRepository")
@Api("/FirmwareRepository")
public interface IFirmwareRepositoryService {

    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @DELETE
    @Path("/{id}")
    public Response deleteRepository(@PathParam("id") String refId);

    @POST
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Add a new ", response = FirmwareInventory.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Firmware repository created"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify firmware repository data object is correct") })
    FirmwareRepository createFirmwareRepository(
            @ApiParam(value = "ASM firmware repository to create", required = true)
            FirmwareRepository firmwareRepository);

    @GET
    @Path("/softwarecomponent")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve all FirmwareRepository with filter, sort, paginate which returns Array of firmwarerepository.class", response = FirmwareRepository.class, responseContainer = "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "All FirmwareRepository Retrived on filter, sort, paginate") })
    List<SoftwareComponent> getSoftwareComponents(@ApiParam("Component ID") @QueryParam("componentId") String componentId,
                                                  @ApiParam("Device ID") @QueryParam("deviceId") String deviceId,
                                                  @ApiParam("Sub Device ID") @QueryParam("subDeviceId") String subDeviceId,
                                                  @ApiParam("Vendor ID") @QueryParam("vendorId") String vendorId,
                                                  @ApiParam("Sub Vendor ID") @QueryParam("subVendorId") String subVendorId,
                                                  @ApiParam("System ID") @QueryParam("systemId") String systemId,
                                                  @ApiParam("Type") @QueryParam("type") String type,
                                                  @ApiParam("Operating System") @QueryParam("operatingSystem") String operatingSystem);

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve all FirmwareRepository with filter, sort, paginate which returns Array of firmwarerepository.class", response = FirmwareRepository.class, responseContainer = "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "All FirmwareRepository Retrived on filter, sort, paginate") })
    List<FirmwareRepository> getFirmwareRepositories(@ApiParam("Sort Column") @QueryParam("sort") String sort,
                                                     @ApiParam("Filter Criteria") @QueryParam("filter") List<String> filter,
                                                     @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam("offset") Integer offset,
                                                     @ApiParam("Page Limit") @DefaultValue("50") @QueryParam("limit") Integer limit,
                                                     @Context UriInfo ui,
                                                     @ApiParam(value = "Hydrate related objects", required = false) @QueryParam("related") boolean related,
                                                     @ApiParam(value = "Hydrate software bundle objects", required = false) @QueryParam("bundles") boolean bundles,
                                                     @ApiParam(value = "Hydrate software component objects", required = false) @QueryParam("components") boolean components);

    @GET
    @Path("/softwarebundle/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(value = "Retrieve an individual Software Bundle", response = SoftwareBundle.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "retrieved", response = SoftwareBundle.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify id is correct"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Bad Request, verify id is correct") })
    SoftwareBundle getSoftwareBundle(@ApiParam(value = "Id", required = true) @PathParam("id") String id);

    @GET
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(value = "Retrieve an individual Chassis Device", response = FirmwareRepository.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "retrieved", response = FirmwareRepository.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify id is correct"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Bad Request, verify id is correct") })
    FirmwareRepository getFirmwareRepository(@ApiParam(value = "Repo Id", required = true) @PathParam("id") String id,
                                             @ApiParam(value = "Hydrate related objects", required = false) @QueryParam("related") boolean related,
                                             @ApiParam(value = "Hydrate software bundle objects", required = false) @QueryParam("bundles") boolean bundles,
                                             @ApiParam(value = "Hydrate software component objects", required = false) @QueryParam("components") boolean components);

    @PUT
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Updates an existing ASM FirmwareRepository", response = FirmwareRepository.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Update completed successfully"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify credential data object is correct"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Credential to be updated was not found.") })
    FirmwareRepository update(
            @ApiParam(value = "Id of FirmwareRepository to update", required = true)
            @PathParam("id")
            String id,
            @ApiParam(value = "ASM FirmwareRepository to update", required = true)
            FirmwareRepository firmwareRepository);

    @POST
    @Path("/connection")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Tests the connection to a remote path", response = FirmwareRepository.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Connection to remote path successful") })
    public Response testConnection(FirmwareRepository firmwareRepository);
}
