/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.credential;

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

import com.dell.asm.encryptionmgr.client.CredentialType;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * The {@code IAsmCredentialService} is a REST service that is intended to aggregate
 * the REST credential services provided by JRAF Resource Adapters.
 *
 * <p>The {@link #getAllCredentials(CredentialType, String, java.util.List, Integer, Integer)} call
 * will call all of the RA {@code getAllCredentials} methods and filter, sort and
 * paginate the results.
 *
 * <p>The remaining calls are strictly pass-through calls to the corresponding
 * RA REST calls. For instance if {@link #createCredential(AsmCredentialDTO)}
 * is called with a type of {@code CHASSIS}, the entire payload will be passed
 * to the chassis {@code create} call. The response from the chassis RA will
 * be translated into a {@link AsmCredentialDTO} and returned to the original caller.
 */
@Api(value = "/credential", description = "Aggregate service for ASM credential objects")
@Path("/credential")
public interface IAsmCredentialService {
    @GET
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a list of all ASM credentials",
            notes = "Filtering, pagination and sorting supported.",
            response = AsmCredentialListDTO.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK") })
    AsmCredentialListDTO getAllCredentials(
            @ApiParam(value = "filter on credential type", allowableValues = "SERVER, CHASSIS, IOM, STORAGE, VCENTER")
            @QueryParam("type")
            CredentialType typeFilter,

            @ApiParam("Specify sort columns in a comma separated list of column names to sort." +
                    " Default order is ascending. Column name can be prefixed with" +
                    " a minus sign to indicate descending for that column. ")
            @QueryParam(AsmConstants.QUERY_PARAM_SORT)
            String sort,

            @ApiParam("Specify filter criteria, Example co,label,default")
            @QueryParam(AsmConstants.QUERY_PARAM_FILTER)
            List<String> filter,

            @ApiParam("Specify pagination offset")
            @DefaultValue("0")
            @QueryParam(AsmConstants.QUERY_PARAM_OFFSET)
            Integer offset,

            @ApiParam("Specify page limit, can not exceed the system maximum limit.")
            @DefaultValue("50")
            @QueryParam(AsmConstants.QUERY_PARAM_LIMIT)
            Integer limit);

    @GET
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Find an ASM credential by type and id", response = AsmCredentialDTO.class)
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Credential not found",
                    response = AsmDetailedMessageList.class) })
    AsmCredentialDTO getCredential(
            @ApiParam(value = "Id of credential to retrieve", required = true)
            @PathParam("id")
            String id);

    @POST
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Add a new ASM credential", response = AsmCredentialDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Credential created"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify credential data object is correct") })
    AsmCredentialDTO createCredential(
            @ApiParam(value = "ASM credential to create", required = true)
            AsmCredentialDTO credentialWrapper);

    @PUT
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Updates an existing ASM credential", response = AsmCredentialDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Update completed successfully"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify credential data object is correct"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Credential to be updated was not found.") })
    AsmCredentialDTO updateCredential(
            @ApiParam(value = "Id of credential to update", required = true)
            @PathParam("id")
            String id,

            @ApiParam(value = "ASM credential to update", required = true)
            AsmCredentialDTO credentialWrapper);

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON }) // for error messages
    @ApiOperation(value = "Deletes an existing ASM credential")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Delete completed successfully"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify credential type and id is correct") })
    void deleteCredential(
            @ApiParam(value = "Id of credential to delete", required = true)
            @PathParam("id")
            String id);
}
