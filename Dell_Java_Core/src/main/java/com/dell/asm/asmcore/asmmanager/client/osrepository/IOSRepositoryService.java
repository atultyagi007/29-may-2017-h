package com.dell.asm.asmcore.asmmanager.client.osrepository;

import com.dell.asm.asmcore.asmmanager.client.osrepository.OSRepository;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.HttpURLConnection;
import java.util.List;

/**
 * OS repository Service for ASM Manager.
 *
 */
@Path("/osRepository")
@Api("/OSRepository")
public interface IOSRepositoryService {

    @DELETE
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Deletes an existing ASM OS Repository", response = OSRepository.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Successfully deleted OS repo", response = OSRepository.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify id is correct"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Bad Request, verify id is correct") })
    public Response deleteOSRepository(@PathParam("id") String id);

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Gets a list of existing ASM OS Repositories", response = OSRepository.class)
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "All OSRepository Retrieved") })
    public List<OSRepository> getOSRepositories();

    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Gets the ASM OS Repository by its ID", response = OSRepository.class)
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OSRepository Retrieved") })
    public OSRepository getOSRepositoryById(@PathParam("id") String id);

    @PUT
    @Path("/sync/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Syncs the ASM OS Repository by its ID", response = OSRepository.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OSRepository Sync") })
    public OSRepository syncOSRepositoryById(@PathParam("id") String id,
                                             @ApiParam("osRepo") OSRepository osRepo);

    @POST
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value= "Creates a new ASM OS Repository", response=OSRepository.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "OS repository created"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify OS repository data object is correct") })
    public OSRepository createOSRepository(OSRepository osRepo);

    @POST
    @Path("/connection")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value= "Tests the connection to a remote path", response=OSRepository.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Connection to remote path successful") })
    public Response testConnection(OSRepository osRepo);
    

    @PUT
    @Path("/{id}")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Update OSRepository", response = OSRepository.class)
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "OSRepository updated"),
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to Update OSRepository or problem with the Resource Adapters",
                response = AsmDetailedMessageList.class),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "OSRepository not found",
                response = AsmDetailedMessageList.class) })
    public OSRepository updateOSRepository(@PathParam("id") String id,
                                           @ApiParam("") OSRepository osRepo,
                                           @ApiParam("Sync ISO") @DefaultValue("false") @QueryParam("sync") Boolean sync) throws WebApplicationException;
    

}
