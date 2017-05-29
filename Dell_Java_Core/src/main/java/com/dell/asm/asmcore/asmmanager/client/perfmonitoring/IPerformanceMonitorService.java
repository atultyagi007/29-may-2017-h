/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.perfmonitoring;

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
import javax.ws.rs.core.Response;

import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * This Device Inventory Service for ASM Manager.
 * 
 */
@Path("/PerformanceMetric")
@Api("/PerformanceMetric")
public interface IPerformanceMonitorService {

    
    /**
     * Retrieve performance monitoring for 13 G servers
     * 
     * @param refId and @param duration
     */
	 
    /**
     * Retrieve performance monitoring for 13 G servers
     * 
     * @param refId and @param duration
     */
	    @GET
	    @Path("/{refId}/{duration}/{time}")
	    @RolesAllowed({ AsmConstants.USERROLE_READONLY })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Produces({ MediaType.APPLICATION_JSON })
	    @ApiOperation(notes = "minimum role allowed: ReadOnly", value = "Retrieve performance metric on refId", response = PerformanceMetric.class,  responseContainer = "List")
	    @ApiResponses({ @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Device retrieved from performance metric inventory"),
	            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Device not found in performance metric inventory") })
	    PerformanceMetric[] getDevicePerformanceMonitoring(@ApiParam("Reference Id") @PathParam("refId") String refId, @ApiParam("duration") @PathParam("duration") String duration, @PathParam("time") String time);
	    
    
}
