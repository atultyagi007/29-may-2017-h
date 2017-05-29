/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

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

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.FirmwareComplianceReport;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/Deployment")
@Api("/Deployment")
public interface IDeploymentService {
    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve Deployment based on deployment ID", response = Deployment.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Deployment retrived"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Deployment not found") })
    Deployment getDeployment(@ApiParam("Deployment Id (String)")
                         @PathParam("id") String deploymentId) throws WebApplicationException;

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve all Deployments with filter, sort, paginate which returns Array of Deployment.class", response = Deployment.class, responseContainer= "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "All Deployment Retrived on filter, sort, paginate") })
    Deployment[] getDeployments(
            @ApiParam(value="Valid sort columns: name,createdBy,createdDate,updatedBy,updatedDate,expirationDate,deploymentDesc,marshalledTemplateData,health") @QueryParam(AsmConstants.QUERY_PARAM_SORT) String sort,
            @ApiParam("Valid filter columns: name,createdBy,createdDate,updatedBy,updatedDate,expirationDate,deploymentDesc,marshalledTemplateData,health") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter,
            @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam(AsmConstants.QUERY_PARAM_OFFSET) Integer offset,
            @ApiParam("Page Limit") @DefaultValue("50") @QueryParam(AsmConstants.QUERY_PARAM_LIMIT) Integer limit,
            @ApiParam("Use full templates including resources in response") @DefaultValue("false") @QueryParam("full") Boolean fullTemplates);
    
    @GET
    @Path("/device/{deviceId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve all Deployments for device", response = Deployment.class, responseContainer= "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Deployments retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Deployments not found") })
    Deployment[] getDeploymentsFromDeviceId(@PathParam("deviceId") String deviceId) throws WebApplicationException;

    @POST
    @Path("/")
    @ApiOperation(notes = "minimum role allowed: Operator", value = "Create new Deployment", response = Deployment.class)
    @RolesAllowed({ AsmConstants.USERROLE_OPERATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Deployment created"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to create Deployment or problems with the Resource Adapters"),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "Deployment already exists") })
    Deployment createDeployment(@ApiParam("Deployment to be created") Deployment deployment) throws WebApplicationException;

    @PUT
    @Path("/{id}")
    @ApiOperation(notes = "minimum role allowed: Operator", value = "Update Deployment", response = Deployment.class)
    @RolesAllowed({ AsmConstants.USERROLE_OPERATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Deployment updated"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to Update Deployment or problem with the Resource Adapters",
                    response = AsmDetailedMessageList.class),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Deployment not found",
                    response = AsmDetailedMessageList.class) })
    Deployment updateDeployment(@ApiParam("Deployment Id (String)")
                            @PathParam("id") String deploymentId,
                            @ApiParam("Deployment to be updated to")
    Deployment mgmtDeployment) throws WebApplicationException;

    @DELETE
    @Path("/{id}")
    @ApiOperation(notes = "minimum role allowed: Operator", value = "Delete Deployment -- this operation is idempotent")
    @RolesAllowed({ AsmConstants.USERROLE_OPERATOR })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Deployment deleted"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Unable to delete Deployment.",
                    response = AsmDetailedMessageList.class) })
    Response deleteDeployment(@ApiParam("Deployment Id (String)")
                            @PathParam("id") String deploymentId) throws WebApplicationException;

    @PUT
    @Path("/migrate/{serviceId}/{serverId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_OPERATOR })
    @ApiOperation(notes = "minimum role allowed: Operator", value = "Migrate Deployment based on deployment ID", response = Deployment.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Deployment migrated"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Deployment not found") })
    Deployment migrateDeployment(@ApiParam("Deployment ID") @PathParam("serviceId") String serviceId,
                                 @ApiParam("Template server component ID") @PathParam("serverId") String serverId,
                               @ApiParam("Target server pool ID") String targetServerPoolId) throws WebApplicationException;


    @POST
    @Path("/filter/{numOfDeployments}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Find available servers for template components", response = DeploymentFilterResponse.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "DeploymentFilterResponse retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to getAvailableServers",
                    response = AsmDetailedMessageList.class),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Unable to find available servers",
                    response = AsmDetailedMessageList.class) })
    DeploymentFilterResponse filterAvailableServers(@ApiParam("Service template")
                                                    ServiceTemplate template,
                                                    @PathParam("numOfDeployments") @ApiParam("Number of deployments") int numOfDeployments,
                                                    @ApiParam(name = "unique", value = "If true (the default), only " +
                                                            "assign a server to one component per deployment. Otherwise " +
                                                            "the same server may be assigned to multiple components.")
                                                    @QueryParam("unique") @DefaultValue("true") boolean requireUnique) throws WebApplicationException;

    @GET
    @Path("/{id}/puppetLogs/{certName}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Log retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Log not found") })
    List<PuppetLogEntry> getPuppetLogs(@PathParam("id") String deploymentId, @PathParam("certName") String certName,
                                       @ApiParam("Valid filter columns: category") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter);

    @GET
    @Path("/export/csv")
    @RolesAllowed(AsmConstants.USERROLE_READONLY)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Exports all Deployments in csv format")
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = "User doesn't have privileges to access this operation"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "No login information specified in the request"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "User Log Internal Error, contact your system administrator") })
    public Response exportAllDeployments() throws WebApplicationException;
    
    
    
    @POST
    @Path("/defineService")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Defines a brownfield service that may be a Deployment", response = Deployment.class)
    @RolesAllowed(AsmConstants.USERROLE_ADMINISTRATOR)
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ 
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Brownfield service defined."),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid parameters to define a brownfield service that may be used for a brownfield deployment.") })
    public Deployment defineService(@ApiParam(value = "Service to define", required = true) ServiceDefinition serviceDefinition) throws WebApplicationException;

    @PUT
    @Path("/migrateServer/{serviceId}/{componentId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_OPERATOR })
    @ApiOperation(notes = "minimum role allowed: Operator", value = "Mark server as failed, find another available to retry deployment ID", response = Deployment.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Server found"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Server not found") })
    Deployment migrateServerComponent(@PathParam("serviceId") String serviceId,
                                 @PathParam("componentId") String componentId) throws WebApplicationException;

    @POST
    @Path("/defineServiceDiff")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Identifies the differences from the existing service and defines an updated version of the service that that may used as a deployment", response = Deployment.class)
    @RolesAllowed(AsmConstants.USERROLE_ADMINISTRATOR)
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ 
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "ServiceTemplate Defined"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid service. Either service does not exist or it is not a brownfield service.") })
    public Deployment defineServiceDiff(@QueryParam("serviceId") String serviceId) throws WebApplicationException;

    @GET
    @Path("/serverNetworking/{serviceId}/{serverId}")
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Returns the networks that a server in a deployment is using.", response = ServerNetworkObjects.class)
    @RolesAllowed(AsmConstants.USERROLE_READONLY)
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = "User doesn't have privileges to access this operation"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "No login information specified in the request"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error, contact your system administrator") })
    public ServerNetworkObjects getServerNetworkObjects (@PathParam("serviceId") String serviceId, @PathParam("serverId") String serverId) throws WebApplicationException;

    @GET
    @Path("/network/{networkId}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve all Deployments for network", response = Deployment.class, responseContainer= "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Deployments retrieved"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Deployments not found") })
    Deployment[] getDeploymentsForNetworkId(@PathParam("networkId") String networkId) throws WebApplicationException;

    @DELETE
    @Path("/users")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Delete Users from Deployments")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Users deleted from Deployments"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Unable to delete User from Deployments.",
                    response = AsmDetailedMessageList.class) })
    Response deleteUsers(@ApiParam("Valid UserIds") @QueryParam("userId") List<String> userIds) throws WebApplicationException;
    
    @GET
    @Path("/{id}/firmware/compliancereport/")
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Returns an Array of FirmwareComplianceReports for the Devices that are in the Deployment.", response = FirmwareComplianceReport.class)
    @RolesAllowed(AsmConstants.USERROLE_READONLY)
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = "User doesn't have privileges to access this operation"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "No login information specified in the request"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error, contact your system administrator") })
    public FirmwareComplianceReport[] getFirmwareComplianceReportsForDevicesInDeployment (@PathParam("id") String serviceId) throws WebApplicationException;

    
}
