/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.templatemgr;

/*
 * @author Praharsh_Shah
 * 
 * ASM core Template REST for infrastructure Template
 * 
 *
 */

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

import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/InfrastructureTemplate")
@Api("/InfrastructureTemplate")
public interface ITemplateManagerService {
    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve InfrastructureTemplate based on InfrastructureTemplate name", response = InfrastructureTemplate.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "InfrastructureTemplate retrived"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "InfrastructureTemplate not found") })
    InfrastructureTemplate getTemplate(@ApiParam("Infrastructure Template Id (String)")
                         @PathParam("id") String templateId) throws WebApplicationException;

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve all InfrastructureTemplates with filter, sort, paginate which returns Array of InfrastructureTemplate.class", response = InfrastructureTemplate.class, responseContainer= "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "All InfrastructureTemplates Retrived on filter, sort, paginate") })
    InfrastructureTemplate[] getAllTemplates(
            @ApiParam("Sort Column") @QueryParam("sort") String sort,
            @ApiParam("Filter Criteria") @QueryParam("filter") List<String> filter,
            @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam("offset") Integer offset,
            @ApiParam("Page Limit") @DefaultValue("50") @QueryParam("limit") Integer limit);


    @POST
    @Path("/")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Create new InfrastructureTemplate", response = InfrastructureTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "InfrastructureTemplate created"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to create InfrastructureTemplate or problems with the Resource Adapters"),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Template already exists") })
    InfrastructureTemplate createTemplate(@ApiParam("Infrastructure Template to be created") InfrastructureTemplate mgmtTemplate) throws WebApplicationException;

    @POST
    @Path("/{id}/copy")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Copy a InfrastructureTemplate", response = InfrastructureTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "InfrastructureTemplate copied successfully"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to copy InfrastructureTemplate or problems with the Resource Adapters"),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Template already exists") })
    InfrastructureTemplate copyTemplate(@ApiParam("Infrastructure Template Id (String)") @PathParam("id") String templateId, @ApiParam("InfrastructureTemplate Name") @QueryParam("newName") String name) throws WebApplicationException;


    @PUT
    @Path("/{id}")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Update Template", response = InfrastructureTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "InfrastructureTemplate updated"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to Update InfrastructureTemplate or problem with the Resource Adapters",
                    response = AsmDetailedMessageList.class),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Template not found",
                    response = AsmDetailedMessageList.class) })
    Response updateTemplate(@ApiParam("Infrastructure Template Id (String)")
                            @PathParam("id") String templateId,
                            @ApiParam("Infrastructure Template to be updated to")
                    InfrastructureTemplate mgmtTemplate) throws WebApplicationException;

    @DELETE
    @Path("/{id}")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Delete InfrastructureTemplate -- this operation is idempotent")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "InfrastructureTemplate deleted"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Unable to delete InfrastructureTemplate.",
                    response = AsmDetailedMessageList.class) })
    Response deleteTemplate(@ApiParam("Infrastructure Template Id (String)")
                            @PathParam("id") String templateId) throws WebApplicationException;
}

