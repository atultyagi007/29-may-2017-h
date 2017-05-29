/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmdeployer.client;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.dell.asm.asmcore.asmmanager.client.deployment.AsmDeployerLogEntry;
import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.deployment.PuppetLogEntry;
import com.dell.asm.asmcore.asmmanager.client.deployment.ServerNetworkObjects;
import com.dell.asm.asmcore.asmmanager.client.perfmonitoring.PerformanceMetric;


/**
 * Service interface for asm-deployer REST services
 */
@Path("/asm")
public interface IAsmDeployerService {
    @POST
    @Path("/device")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    AsmDeployerDevice createDevice(AsmDeployerDevice device);

    @GET
    @Path("/device/{certName}")
    @Produces({ MediaType.APPLICATION_JSON })
    AsmDeployerDevice getDevice(@PathParam("certName") String certName);

    @PUT
    @Path("/device/{certName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    AsmDeployerDevice updateDevice(@PathParam("certName") String certName, AsmDeployerDevice device);

    @DELETE
    @Path("/device/{certName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    void deleteDevice(@PathParam("certName") String certName);

    @GET
    @Path("/deployment/{id}/status")
    @Produces({ MediaType.APPLICATION_JSON })
    AsmDeployerStatus getDeploymentStatus(@PathParam("id") String deploymentId);

    @GET
    @Path("/deployment/{id}/log")
    @Produces({ MediaType.APPLICATION_JSON })
    List<AsmDeployerLogEntry> getDeploymentLogs(@PathParam("id") String deploymentId);

    @GET
    @Path("/deployment/{id}/puppet_logs/{certname}")
    @Produces({ MediaType.APPLICATION_JSON})
    List<PuppetLogEntry> getAsmPuppetLogs(@PathParam("id") String deploymentId, @PathParam("certname") String puppetCertName);

    @PUT
    @Path("/deployment/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    AsmDeployerStatus retryDeployment(@PathParam("id") String deploymentId, Deployment deployment);

    @POST
    @Path("/deployment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    AsmDeployerStatus createDeployment(Deployment deployment);

    @POST
    @Path("/process_service_profile_migration")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    AsmDeployerStatus migrateDeployment(Deployment deployment);

    @DELETE
    @Path("/deployment/{id}")
    void deleteDeployment(@PathParam("id") String deploymentId);
    
    @GET
    @Path("/metrics/{id}/{duration}/{time}")
    @Produces({ MediaType.APPLICATION_JSON })
    PerformanceMetric[] performanceMonitoring(@PathParam("id") String deviceId,
                                              @PathParam("duration") String duration,
                                              @PathParam("time") String time,
                                              @QueryParam("required") String requiredTargets);

    @GET
    @Path("/deployment/{serviceId}/server/{sererComponentId}")
    @Produces({ MediaType.APPLICATION_JSON })
    ServerNetworkObjects getServerNetworkObjects(@PathParam("serviceId") String deploymentId,
                                                 @PathParam("sererComponentId") String sererComponentId);
    
    @POST
    @Path("/deployment/{deploymentId}/log")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    void addLogMessage(@PathParam("deploymentId") String deploymentId, AsmDeployerDeploymentLogEntry logEntry);

}
