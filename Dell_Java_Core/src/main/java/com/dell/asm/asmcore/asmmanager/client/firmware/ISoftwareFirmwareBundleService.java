package com.dell.asm.asmcore.asmmanager.client.firmware;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Set;

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
import javax.ws.rs.core.Response;

import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/softwareBundleFirmware")
@Api("/SoftwareBundleFirmware")
public interface ISoftwareFirmwareBundleService {
	
	@RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
	@POST
	@Path("/")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "create a new software bundle for switch or storage", response = SoftwareBundle.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "software bundle created"),
			@ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = " software bundle already exists", response = AsmDetailedMessageList.class),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid input or software bundle information  supplied or verify other input data is correct", response = AsmDetailedMessageList.class) })
	SoftwareBundle addSoftwareBundle(
			@ApiParam(value = "software bundle values  that needs to be added to ASM", required = true) SoftwareBundle softwareBundle);
	
	
	@RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
	@PUT
	@Path("/{id}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Update an existing software bundle")
	@ApiResponses({
			@ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "software bundle updated successfully"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid input for id supplied or verify other input data is correct", response = AsmDetailedMessageList.class),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "software bundle  not found", response = AsmDetailedMessageList.class),
			@ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "software bundle  with similar name already exists", response = AsmDetailedMessageList.class) })
	Response updateSoftwareBundle(
			@ApiParam(value = "Id of software bundle to update", required = true) @PathParam("id") String id,
			@ApiParam(value = "Software bundle object that contains update fields", required = true) SoftwareBundle softwareBundle);
	
	@RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
	@DELETE
	@Path("/{id}")
	@ApiOperation("Delete an existing software bundle")
	@ApiResponses({
			@ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "software bundle deleted successfully"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid software bundle id supplied", response = AsmDetailedMessageList.class) })
	Response deleteSoftwareBundle(
			@ApiParam(value = "Id of software bundle to delete", required = true) @PathParam("id") String id);

	@RolesAllowed({ AsmConstants.USERROLE_READONLY })
	@GET
	@Path("/{id}}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Find a software bundle by id", response = SoftwareBundle.class)
	@ApiResponses({
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "software bundle  retrieved successfully"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "software bundle not found", response = AsmDetailedMessageList.class) })
	SoftwareBundle getSoftwareBundle(
			@ApiParam(value = "Id of bundle to retrieve", required = true) @PathParam("id") String id);
	
	@RolesAllowed({ AsmConstants.USERROLE_READONLY })
	@GET
	@Path("/{fwRepoId}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Retrieve software bundles", response = SoftwareBundle.class, responseContainer = "List", notes = "Operation support filters, sorting and pagination. See Parameters section for details. Note that system enforces a default"
			+ " pagination if result set reaches a system max of 50 objects. Caller needs to check"
			+ " Link pagination headers in the response to see if pagination occurred.")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "software bundles is retrieved"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "software bundles not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Problem with a query parameter, check response for details", response = AsmDetailedMessageList.class) })
	List<SoftwareBundle> getAllSoftwareBundles(
			@ApiParam("Specify sort columns(Supported columns are:name,description,type,vlanId,createdBy,createdDate,updatedBy,updatedDate) in a comma separated list of column names to sort."
					+ " Default order is ascending. Column name can be prefixed with"
					+ " a minus sign to indicate descending for that column. ") @QueryParam(AsmConstants.QUERY_PARAM_SORT) String sort,
			@ApiParam("Specify filter criteria(Supported columns are:name,description,type,vlanId,createdBy,updatedBy,createdDate,updatedDate(For dates supports only eq filter also the format of input should be dd-MM-yyyy))") @QueryParam(AsmConstants.QUERY_PARAM_FILTER) List<String> filter,
			@ApiParam("Specify pagination offset") @DefaultValue("0") @QueryParam(AsmConstants.QUERY_PARAM_OFFSET) Integer offset,
			@ApiParam("Specify page limit, cannot exceed system limit of 50") @DefaultValue("50") @QueryParam(AsmConstants.QUERY_PARAM_LIMIT) Integer limit, @ApiParam(value = "Id of fw repository bundle to retrieve", required = true) @PathParam("fwRepoId") String fwRepoId);

}
