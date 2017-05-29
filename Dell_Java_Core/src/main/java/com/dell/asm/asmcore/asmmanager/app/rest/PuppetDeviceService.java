/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.puppetdevice.IPuppetDeviceService;
import com.dell.asm.asmcore.asmmanager.client.puppetdevice.PuppetDevice;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;

/**
 * Implementation of Device Inventory REST Service for ASM Manager.
 * 
 */
@Path("/PuppetDevice")
public class PuppetDeviceService implements IPuppetDeviceService {
    // Logger.
    private static final Logger logger = Logger.getLogger(PuppetDeviceService.class);


    @Context
    private HttpServletResponse servletResponse;

    @Context
    private HttpServletRequest servletRequest;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;

    @Override
    public PuppetDevice createPuppetDevice(final PuppetDevice puppetModule) throws WebApplicationException 
    {
        
        // TODO Auto-generated method stub....may implement some day....
        logger.info("Inside createPuppetDevice");
        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.serviceNotImplemented());
        //PuppetDevice returnValue = new PuppetDevice();
        //return returnValue;
    }

    @Override
    public PuppetDevice[] getAllPuppetDevices(String sort, List<String> filter, Integer offset, Integer limit) 
    {
        logger.info("Inside getAllPuppetModules");
        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.serviceNotImplemented());
        //return null;
    }

    @Override
    public PuppetDevice getPuppetDevice(String id) 
    {
        logger.info("Inside getPuppetDevice id=" + id);
        
        
        if( id == null || id.length() == 0)
        {
            return null;
        }
        PuppetDevice returnValue = new PuppetDevice();
        
        try 
        {
        	Map<String, String> deviceDetails = PuppetModuleUtil.getPuppetDevice(id);
        	returnValue.setData( deviceDetails );
        	returnValue.setName( deviceDetails.get("name") );
        } 
        catch (Exception e) 
        {
            logger.error("Exception while getting puppet module details", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.PuppetDeviceExtractionFailed(id));
            
        }
        return returnValue;
    }

    @Override
    public Response updatePuppetDevice(String id, PuppetDevice newDeviceInventory) 
    {
        logger.info("Inside updatePuppetDevice");
        // TODO Auto-generated method stub....may implement some day....
        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.serviceNotImplemented());
        //return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response deletePuppetDevice(String id, boolean forceDelete) 
    {
        logger.info("Inside deletePuppetDevice");
        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, AsmManagerMessages.serviceNotImplemented());
        // TODO Auto-generated method stub....may implement some day....
        //return Response.status(Response.Status.NO_CONTENT).build();
    }

}
