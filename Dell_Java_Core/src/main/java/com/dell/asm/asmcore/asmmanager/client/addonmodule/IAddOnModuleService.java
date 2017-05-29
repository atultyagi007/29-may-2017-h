package com.dell.asm.asmcore.asmmanager.client.addonmodule;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
* AddOnModule Service for ASM Manager.
*
*/
@Path("/addOnModule")
@Api("/addOnModule")
public interface IAddOnModuleService {

    @POST
    @Path("/")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a new AddOnModule", response = AddOnModule.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "AddOnModule created"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify AddOnModule upload URL is correct") })
    public AddOnModule createAddOnModule(@ApiParam(value = "AddOnModule", required = true) AddOnModule addOnModule);
    
    @GET
    @Path("/{id}")
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(value = "Retrieve an individual AddOnModule", response = AddOnModule.class)
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "retrieved", response = AddOnModule.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify id is correct"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Bad Request, verify id is correct") })
    public AddOnModule getAddOnModule(@ApiParam(value = "addOnModuleId", required = true) @PathParam("id") String addOnModuleId);

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve all AddOnModule with filter, sort, paginate which returns Array of AddOnModule.class", response = AddOnModule.class, responseContainer= "List")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "All AddOnModule Retrived on filter, sort, paginate") })    
    public List<AddOnModule> getAddOnModules(@ApiParam("Sort Column") @QueryParam("sort") String sort,
            @ApiParam("Filter Criteria") @QueryParam("filter") List<String> filter,
            @ApiParam("Pagination Offset") @DefaultValue("0") @QueryParam("offset") Integer offset,
            @ApiParam("Page Limit") @DefaultValue("50") @QueryParam("limit") Integer limit);
    
	
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @DELETE
    @Path("/{id}")
    public Response deleteAddOnModule(@PathParam("id") String addOnModuleId);
}
