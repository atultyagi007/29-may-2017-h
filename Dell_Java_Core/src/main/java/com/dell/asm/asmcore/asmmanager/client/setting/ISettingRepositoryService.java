package com.dell.asm.asmcore.asmmanager.client.setting;

import java.net.HttpURLConnection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/setting")
public interface ISettingRepositoryService {

	 @POST
	 @Path("/")
	 @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
	 @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	 @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	 @ApiOperation(value = "Add a new ", response = Setting.class)
	 @ApiResponses(value = {
			 @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Firmware repository created"),
			 @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify firmware repository data object is correct") })
	 Setting create(
			 @ApiParam(value = "ASM setting to create", required = true)
			 Setting setting);
	 
	 @PUT
	 @Path("/{id}")
	 @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
	 @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	 @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	 @ApiOperation(value = "Add a new ", response = Setting.class)
	 @ApiResponses(value = {
			 @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Firmware repository created"),
			 @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify firmware repository data object is correct") })
	 Setting update(
			 @ApiParam(value = "ASM setting to update", required = true) Setting setting, 
			 @ApiParam(value = "Id of the setting", required = true) @PathParam("id")  String id);
	 
	 //For settings we select via the name.  Id is only present due to inheritance.
	 @GET
	 @Path("/{name}")
	 @RolesAllowed({ AsmConstants.USERROLE_READONLY })
	 @ApiOperation(value = "Retrieve a setting", response = Setting.class)
	 @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "retrieved", response = Setting.class),
		 @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify name is correct"),
		 @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Bad Request, verify name is correct") })
	 Setting get(@ApiParam(value = "Name of setting", required = true) @PathParam("name") String name);
	    
	 @DELETE
	 @Path("/{id}")
	 @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
	 @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
         @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	 @ApiOperation(value = "Delete a setting ", response = Setting.class)
	 @ApiResponses(value = {
	         @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Setting deleted"),
	         @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify setting data object is correct") })
	 public void delete(@ApiParam(value = "Id of the setting", required = true) @PathParam("id")  String id);
}
