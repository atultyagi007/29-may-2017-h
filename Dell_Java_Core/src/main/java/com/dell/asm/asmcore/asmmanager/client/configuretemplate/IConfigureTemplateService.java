/**************************************************************************
 * Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 * *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configuretemplate;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateUploadRequest;
import com.dell.asm.rest.common.AsmConstants;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.HttpURLConnection;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

@Path("/ConfigureTemplate")
@Api("/ConfigureTemplate")
public interface IConfigureTemplateService {

    @POST
    @Path("/upload")
    @ApiOperation(notes = "minimum role allowed: Administrator", value = "Upload a ServiceTemplate", response = ServiceTemplate.class)
    @RolesAllowed({AsmConstants.USERROLE_ADMINISTRATOR})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiResponses({@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "ServiceTemplate uploaded successfully"),
            @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Invalid Parameters to upload template")})
    ServiceTemplate uploadTemplate(@ApiParam("Service Template upload request") ServiceTemplateUploadRequest uploadRequest) throws WebApplicationException;

}
