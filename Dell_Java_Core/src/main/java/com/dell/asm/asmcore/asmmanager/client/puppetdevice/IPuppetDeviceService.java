/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.puppetdevice;

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

import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This Device Inventory Service for ASM Manager.
 * 
 */
@Path("/PuppetDevice")
@Api("/PuppetDevice")
public interface IPuppetDeviceService {
    /**
     * Create Managed Devices in Inventory.
     * 
     * @param puppetModule
     *            the devices to be created in inventory.
     */
    @POST
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes ="Installs a puppet module from the puppet forge", response = PuppetDevice.class, value = "the installed puppet module")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_ACCEPTED, message = "Create the discovery resource", response = PuppetDevice.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify Discovery request object is correct") })
    PuppetDevice createPuppetDevice(final PuppetDevice puppetModule) throws WebApplicationException;

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
            + " Link pagination headers in the response to see if pagination occurred.", value = "Retrieve all installed Puppet Modules.",
            response = PuppetDevice.class, responseContainer = "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Puppet Module(s) retrieved from inventory on filter, sort, paginate") })
    PuppetDevice[] getAllPuppetDevices(@ApiParam("Sort Column") @QueryParam("sort") String sort,
            @ApiParam("Filter Criteria") @QueryParam("filter") List<String> filter,
            @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam("offset") Integer offset,
            @ApiParam("Page Limit") @DefaultValue("50") @QueryParam("limit") Integer limit);

    /**
     * Retrieve Managed Device based on refId.
     * 
     * @param id
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve an installed Puppet Modules based on its name", response = PuppetDevice.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Puppet Module retrieved from inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Puppet Module not found in inventory") })
    PuppetDevice getPuppetDevice(@ApiParam("Reference Id") @PathParam("id") String id);

    /**
     * Update Device Inventory based on given deviceInventory.
     * 
     * @param newDeviceInventory
     *            the device inventory to be updated.
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Update a Puppet Module")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Puppet Module updated"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Puppet Module"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Puppet Module not installed") })
    Response updatePuppetDevice(@ApiParam("Reference Id") @PathParam("id") String name,
            @ApiParam("Device to be updated to") PuppetDevice newDeviceInventory);

    /**
     * Delete Device Inventory based on refId.
     * 
     * @param id
     *            the refId of the device to be removed from inventory.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Delete Puppet Module from Inventory -- this operation is idempotent", response = PuppetDevice.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Puppet Module deleted from inventory"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Unable to Puppet Module") })
    Response deletePuppetDevice(@ApiParam("Reference Id") @PathParam("id") String id, @ApiParam("Force Delete") @DefaultValue("false") @QueryParam("forceDelete") boolean forceDelete);    
   

}
