/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;


import java.io.IOException;
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

@Path("/ServiceTemplate")
@Api("/ServiceTemplate")
public interface IServiceTemplateService {
    
    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve ServiceTemplate based on ServiceTemplate id", response = ServiceTemplate.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "ServiceTemplate retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "ServiceTemplate not found") })
    ServiceTemplate getTemplate(@ApiParam("Infrastructure Template Id (String)")
                         @PathParam("id") String templateId, 
                         @ApiParam("Will return Managed and Reserved VM Managers if set to true, other wise only returns Discovered.") 
                         @DefaultValue("false") 
                         @QueryParam("includeBrownfieldVmMangers") Boolean includeBrownfieldVmMangers) throws WebApplicationException;

    @GET
    @Path("/device/{deviceId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Default Template customized for specified device", response = ServiceTemplate.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "ServiceTemplate retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "ServiceTemplate not found") })
    ServiceTemplate getCustomizedTemplate(@ApiParam("Device Id (String)") @PathParam("deviceId")
                                          String deviceId) throws WebApplicationException;

    @GET
    @Path("/template/{templateId}/{componentType}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Default Template with components refined for specified template ID", response = ServiceTemplate.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "ServiceTemplate retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "ServiceTemplate not found") })
    ServiceTemplate getCustomizedComponentForTemplate(@ApiParam("Template Id (String)") @PathParam("templateId") String templateId,
                                       @ApiParam("Template Component Type (String)") @PathParam("componentType") String componentType) throws WebApplicationException;

    @GET
    @Path("/service/{serviceId}/{componentType}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Default Template with components refined for specified template ID", response = ServiceTemplate.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "ServiceTemplate retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "ServiceTemplate not found") })
    ServiceTemplate getCustomizedComponentForService(
                                           @ApiParam("Service Id (String)") @PathParam("serviceId") String serviceId,
                                           @ApiParam("Template Component Type (String)") @PathParam("componentType") String componentType) throws WebApplicationException;

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve all ServiceTemplates with filter, sort, paginate which returns Array of ServiceTemplate.class", response = ServiceTemplate.class, responseContainer= "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "All ServiceTemplates Retrived on filter, sort, paginate") })
    ServiceTemplate[] getAllTemplates(
            @ApiParam(value="Supported sort columns are: name,createdBy,createdDate,updatedBy,updatedDate") @QueryParam(AsmConstants.QUERY_PARAM_SORT) String sort,
            @ApiParam("Supported filter columns are: name,draft,createdBy,updatedBy,createdDate,updatedDate") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter,
            @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam(AsmConstants.QUERY_PARAM_OFFSET) Integer offset,
            @ApiParam("Page Limit") @DefaultValue("50") @QueryParam(AsmConstants.QUERY_PARAM_LIMIT) Integer limit,
            @ApiParam("Full or Brief") @DefaultValue("false") @QueryParam("full") Boolean full);

    @POST
    @Path("/")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Create new ServiceTemplate", response = ServiceTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "ServiceTemplate created"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to create ServiceTemplate or problems with the Resource Adapters"),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Template already exists") })
    ServiceTemplate createTemplate(@ApiParam("Infrastructure Template to be created") ServiceTemplate mgmtTemplate) throws WebApplicationException;

    @POST
    @Path("/{id}/copy")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Copy a ServiceTemplate", response = ServiceTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "ServiceTemplate copied successfully"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to copy ServiceTemplate or problems with the Resource Adapters"),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Template already exists") })
    ServiceTemplate copyTemplate(@ApiParam("Infrastructure Template Id (String)") @PathParam("id") String templateId, 
    		                     @ApiParam("Infrastructure Template settings") ServiceTemplate configuration) throws WebApplicationException;

    @POST
    @Path("/{id}/mapToPhysicalResources")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Do Physical Resource allocation based on ServiceTemplate", response = ServiceTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Map Service Template to physical resources"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to copy ServiceTemplate or problems with the Resource Adapters"),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Template already exists") })
    ServiceTemplate mapToPhysicalResources(@ApiParam("Infrastructure Template Id (String)") @PathParam("id") String templateId) throws WebApplicationException;
    

    @PUT
    @Path("/{id}")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Update Template", response = ServiceTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "InfrastructureTemplate updated"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to Update ServiceTemplate",
                    response = AsmDetailedMessageList.class),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Template not found",
                    response = AsmDetailedMessageList.class) })
    Response updateTemplate(@ApiParam("Infrastructure Template Id (String)")
                            @PathParam("id") String templateId,
                            @ApiParam("Infrastructure Template to be updated to")
    ServiceTemplate mgmtTemplate) throws WebApplicationException;

    @DELETE
    @Path("/{id}")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Delete ServiceTemplate -- this operation is idempotent")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "ServiceTemplate deleted"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Unable to delete ServiceTemplate.",
                    response = AsmDetailedMessageList.class) })
    Response deleteTemplate(@ApiParam("Infrastructure Template Id (String)")
                            @PathParam("id") String templateId) throws WebApplicationException;

    @POST
    @Path("/updateParameters")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Create new ServiceTemplate", response = ServiceTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_OPERATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Parameters updated"), })
    ServiceTemplate updateParameters(@ApiParam(name = "Service template", value = "Fills in any missing parameters required to deploy the template")
                                     ServiceTemplate template) throws WebApplicationException;

    @POST
    @Path("/export")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Export a ServiceTemplate", response = ServiceTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "ServiceTemplate exported successfully"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to copy ServiceTemplate or problems with the Resource Adapters"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Template not found") })
    String exportTemplate(@ApiParam("Service Template") ServiceTemplate template,
                                 @ApiParam("Encryption password") @QueryParam("encPassword") String encPassword,
                                 @ApiParam("use password from backup") @QueryParam("useEncPwdFromBackup") boolean useEncPwdFromBackup
                                 ) throws WebApplicationException;

    @GET
    @Path("/export/csv")
    @RolesAllowed(AsmConstants.USERROLE_READONLY)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Exports all Templates in csv format")
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify input parameters are correct", response = AsmDetailedMessageList.class),
            @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = "User doesn't have privileges to access this operation"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "No login information specified in the request"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "User Log Internal Error, contact your system administrator") })
    public Response exportAllTemplates() throws WebApplicationException;
    
    @POST
    @Path("/upload")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Upload a ServiceTemplate", response = ServiceTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "ServiceTemplate uploaded successfully"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to upload template")})
    ServiceTemplate uploadTemplate(@ApiParam("Service Template upload request") ServiceTemplateUploadRequest uploadRequest) throws WebApplicationException;

    @GET
    @Path("/uploadConfig")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Default Template customized with uploaded config", response = ServiceTemplate.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "ServiceTemplate retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "ServiceTemplate not found") })
    ServiceTemplate getUploadedConfigTemplate(@ApiParam("Uploaded Config Path importConfig(String)") @QueryParam("configPath")
					      String configPath) throws WebApplicationException, IOException;

    @PUT
    @Path("/components/template/{templateId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: Operator", value = "Update Template by analysing related components.", response = ServiceTemplate.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "ServiceTemplate updated"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "ServiceTemplate not found") })
    ServiceTemplate updatedTemplateComponentsByTemplate(@ApiParam("Template Id") @PathParam("templateId") String templateId,
                                              @ApiParam("Template to update") ServiceTemplate template) throws WebApplicationException;

    @PUT
    @Path("/components/service/{serviceId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: Operator", value = "Update Template by analysing related components.", response = ServiceTemplate.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "ServiceTemplate updated"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "ServiceTemplate not found") })
    ServiceTemplate updatedTemplateComponentsByService(@ApiParam("Service Id") @PathParam("serviceId") String serviceId,
                                              @ApiParam("Template to update") ServiceTemplate template) throws WebApplicationException;

    @DELETE
    @Path("/users")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Delete Users from Templates")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Users deleted from Templates"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Unable to delete User from Templates.",
                    response = AsmDetailedMessageList.class) })
    Response deleteUsers(@ApiParam("Valid UserIds") @QueryParam("userId") List<String> userIds) throws WebApplicationException;

    @POST
    @Path("/cloneTemplate")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Create new ServiceTemplate from passed in ServiceTemplate", response = ServiceTemplate.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "ServiceTemplate created"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to create ServiceTemplate"),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Template already exists") })
    ServiceTemplate cloneTemplate(@ApiParam("Service Template to be cloned") ServiceTemplate serviceTemplate) throws WebApplicationException;

}
