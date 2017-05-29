package com.dell.asm.asmcore.asmmanager.client.discovery;

import java.net.HttpURLConnection;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.dell.asm.rest.common.AsmConstants;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * The is the restful interface to for discovery of the devices given ip ranges and creds
 * 
 */
@Path("/DiscoveryRequest")
@Api(value = "/DiscoveryRequest")
public interface IDiscoverIPRangeDevicesService {

    @POST
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes ="Creates the discovery resource for devices in the given IP Range and returns the discovery Id to the discovery.  minimum role allowed: Administrator", response = DiscoveryRequest.class, value = "discover devices of Ip range")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_ACCEPTED, message = "Create the discovery resource", response = DiscoveryRequest.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify Discovery request object is correct") })
    DiscoveryRequest deviceIPRangeDiscoveryRequest(final DiscoveryRequest discoveryRequest) throws WebApplicationException;

    @POST
    @Path("/chassislist")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes ="Creates the discovery resource for chassis and racks in the given IP Range and returns the discovery Id to the discovery.  minimum role allowed: Administrator", response = DiscoveryRequest.class, value = "discover devices of Ip range")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_ACCEPTED, message = "Create the discovery resource", response = DiscoveryRequest.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify Discovery request object is correct") })
    DiscoveryRequest deviceIPRangeDiscoveryRequestForChassis(final DiscoveryRequest discoveryRequest) throws WebApplicationException;

    /**
     * Retrieve Device discovery result based on Discovery ID.
     * @param id the discoveryId of the job
     * @return DiscoveryJobResult job result from discovery
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(notes ="minimum role allowed: ReadOnly", value = "Retrieve Device from Inventory based on Id", response=DiscoveryRequest.class)
    @ApiResponses({@ApiResponse(code = HttpURLConnection.HTTP_OK, message="Discovery result retrieved for the job"),
    	           @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Job not found in the discovery"),
    	           @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify sort, filter, and pagination are valid")})
    DiscoveryRequest getDiscoveryRequest(
    		@ApiParam("Discovery ID")  @PathParam("id") String id);
    
    /**
     * Delete Device Inventory based on discoveryId.
     * @param id the discoveryId of the job to be removed from inventory.
     * @return Response for the operation
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @ApiOperation(notes ="minimum role allowed: Administrator", value = "Delete Device from Discover result  -- this operation is idempotent")
    @ApiResponses({@ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Job deleted from inventory")})
    Response deleteDiscoveryResult(@ApiParam("Discovery ID") 
                                   @PathParam("id") String id);
    
    /**
     * Retrieve Device discovery results.
     *
     * @param sort
     * @param filter
     * @param offset
     * @param limit
     * @return DiscoveryJobResult job result from discovery
     */
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(value = "Retrieve a Array of DiscoveryRequest", response = DiscoveryRequest.class, 
    responseContainer = "List", notes = "Operation supports filters, sorting, and pagination")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Problem with a query parameter, check response for details", response = DiscoveryRequest.class), })
    DiscoveryRequest[] getDiscoveryRequests(@ApiParam("Valid sort columns: id,status,statusMessage") @QueryParam(AsmConstants.QUERY_PARAM_SORT) String sort,
            @ApiParam("Valid filter columns: id,status,statusMessage") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter,
            @ApiParam("pagination offset") @QueryParam(AsmConstants.QUERY_PARAM_OFFSET) Integer offset,
            @ApiParam("page limit") @QueryParam(AsmConstants.QUERY_PARAM_LIMIT) Integer limit);
    
    /**
     * Retrieve Device discovery result based on Discovery ID.
     * 
     * @param id
     *            the discoveryId of the job
     * @return DiscoveryJobResult job result from discovery
     */
    @GET
    @Path("/discoveryresult/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(value = "Retrieve a Array of DiscoveryResults", response = DiscoveryResult.class, 
    responseContainer = "List", notes = "Operation support filters, sorting and pagination. See Parameters section for details. Note that system enforces a default"
            + " pagination if result set reaches a system max of 50 objects. Caller needs to check"
            + " Link pagination headers in the response to see if pagination occurred.")
    @ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Problem with a query parameter, check response for details", response = DiscoveryResult.class), })
    DiscoveryResult getDiscoveryResult(@ApiParam("ref_id") @PathParam("id") String id);
}
