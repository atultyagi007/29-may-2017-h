package com.dell.asm.asmcore.asmmanager.util;

/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;

import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.user.model.IUserResource;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.pg.orion.common.context.ServiceContext;
import com.dell.pg.orion.common.utilities.ConfigurationUtils;
import com.dell.pg.orion.common.utilities.MarshalUtil;

/**
 * Utility class to provide common calls such as security checks for all Services. 
 */
public class AsmManagerUtil {
    
    // Class Variables
    /**
     * Appliance Version file name. This is a property file.
     */
    public static final String ASM_VERSION_PROPERTY_FILENAME = "asmversion.properties";
    /**
     * Property Key in the property file for the Appliance version.
     */
    public static final String VERSION_PROPERTY_KEY = "VERSION";

    private static final Logger LOGGER = Logger.getLogger(AsmManagerUtil.class);
    private static final String CURRENT_ASM_VERSION;
    
    static {
        try {
            LOGGER.info("Loading asm version from: " + ASM_VERSION_PROPERTY_FILENAME);
            final Properties properties = ConfigurationUtils.resolveAndReadPropertiesFile(ASM_VERSION_PROPERTY_FILENAME, 
                    AsmManagerUtil.class.getClassLoader());
            CURRENT_ASM_VERSION = properties.getProperty(VERSION_PROPERTY_KEY);
            LOGGER.info("Current asm version: " + CURRENT_ASM_VERSION);
        } catch (IOException e) {
            LOGGER.error("Failed to read " + ASM_VERSION_PROPERTY_FILENAME);
            throw new AsmManagerRuntimeException("Failed to read " + ASM_VERSION_PROPERTY_FILENAME);
        }
    }
    
    
    // Member Variables
    IUserResource userResource = ProxyUtil.getAdminProxy();
    
    /**
     * Default constructor for the class.
     */
    public AsmManagerUtil() {}
    
    /**
     * Constructor that sets the UserResource that will be used for retrieving User information.
     */
    public AsmManagerUtil(IUserResource userResource) {
        this.userResource = userResource;
    }
    
    /**
     * Sets the User proxy that will be used for retrieving User information. 
     * @param userProxy the User proxy that will be used for retrieving User information.
     */
    public void setAdminProxy(IUserResource userProxy) {
        this.userResource = userResource;
    }
    
    
    /**
     * Return the http status that most closely matches the semantics of the specified exception.
     *
     * @param e some exception
     * @return an Http status code.
     */
    public static Response.Status getHttpStatus(Exception e) {
        // Use when request parms are not acceptable.
        if (e instanceof IllegalArgumentException)
            return Response.Status.BAD_REQUEST;

        // Use when expected DB entry is not found.
        if (e instanceof FileNotFoundException)
            return Response.Status.NOT_FOUND;

        // Use when some SQL failure occurs.
        if (e instanceof JDBCException)
            return Response.Status.PRECONDITION_FAILED;

        // Use when some hibernate failure occurs.
        if (e instanceof HibernateException)
            return Response.Status.NOT_ACCEPTABLE;

        // Default is some server error.
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    public static String extractDisplayMessage(WebApplicationException e) {

        String msg = e.getMessage();
        Object o = null;

        //try to see if the message is a asmDetailedMessageList
        try {
            o = MarshalUtil.unmarshal(AsmDetailedMessageList.class, msg);
        } catch (Exception ex) {
            //nothing to do here
        }

        //try asmdetailed message
        if (o == null) {
            try {
                o = MarshalUtil.unmarshal(AsmDetailedMessage.class, msg);
            } catch (Exception ex2) {
                //nothing to do here
            }
        }

        StringBuilder display = new StringBuilder();
        if (o != null) {
            if (o instanceof AsmDetailedMessageList) {
                AsmDetailedMessageList dlist = (AsmDetailedMessageList) o;
                for (AsmDetailedMessage m : dlist.getMessages()) {
                    display.append(m.getDisplayMessage() + "\n");
                }
            } else if (o instanceof AsmDetailedMessage) {
                AsmDetailedMessage dm = (AsmDetailedMessage) o;
                display.append(dm.getDisplayMessage());
            }
            return display.toString();
        }

        return msg;
    }
    
    /**
     * Returns the ASM Version info.
     * 
     * @return the ASM Version info.
     */
    public static String getAsmVersion() {
        return CURRENT_ASM_VERSION;
    }
    
    
    /**
     * Returns the userId for the current securityContext or 0 if none is found.
     * 
     * @return the userId for the current securityContext or 0 if none is found.
     */
    public static long getUserId() {
        try {
            ServiceContext.Context sc = ServiceContext.get();
            if (sc.getApiKey() == null) {
                return (long) 0;
            }
            return sc.getUserId();
        } catch (Exception e) {
            LOGGER.error("Unable to get user context", e);
            return (long) 0;
        }
    }
    
    
    /**
     * Returns the User for the current request.
     * 
     * @param servletRequest
     * @return
     */
    public User getCurrentUser(HttpServletRequest servletRequest) {
        Long userId = getUserId();

        if (userId == 0) {
            if (servletRequest==null) {
                // User not found (no http context) - call  ProxyUtil
                userId = (long) 1;
            }else{
                // TODO: remove when asm_deployer gets REST headers
                LOGGER.warn("User not found in SecurityContext from http header - possibly call from trusted source: asm_deployer.");
                userId = (long) 1;
            }
        }

        if (servletRequest != null) {
            ProxyUtil.setProxyHeaders(this.userResource, servletRequest);
        }
        
        return this.userResource.getUser(userId);
    }
}
