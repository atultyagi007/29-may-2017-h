package com.dell.asm.asmcore.asmmanager.client.configure;

import java.net.HttpURLConnection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * The is the restful interface to for device configuration.
 * 
 */
@Path("/Configure")
@Api(value = "/Configure")
public interface IConfigureDevicesService {

    @POST
    @Path("/process")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes ="Validate configuration and reserve IPs. Minimum role allowed: Administrator", response = String.class, value = "Job Id")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Validate configuration and reserve IPs", response = String.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify request object is correct") })
    Response processInitialConfiguration(final ConfigurationRequest discoveryRequest) throws WebApplicationException;

    @POST
    @Path("/discover")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes ="Apply configuration and run discovery. Minimum role allowed: Administrator", response = String.class, value = "Job Id")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Apply configuration and run discovery", response = String.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify request object is correct") })
    ConfigurationResponse configureAndDiscoverChassis(final ConfigurationRequest discoveryRequest) throws WebApplicationException;

    @POST
    @Path("/configure")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes ="Apply configuration. Minimum role allowed: Administrator", response = String.class, value = "Job Id")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Apply configuration", response = String.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify request object is correct") })
    String configureChassis(final ServiceTemplate configuration) throws WebApplicationException;

    @POST
    @Path("/initial_configure")
    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(notes ="Apply initial configuration. Minimum role allowed: Administrator", response = String.class, value = "Job Id")
    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Apply configuration", response = String.class),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request, verify request object is correct") })
    String initialConfigureChassis(final ServiceTemplate configuration) throws WebApplicationException;

}
