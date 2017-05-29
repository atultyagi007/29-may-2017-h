package com.dell.asm.asmcore.asmmanager.client.network;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.dell.asm.rest.common.AsmConstants;

@Path("/deviceNetworks")
public interface IDeviceNetworkService {

    @RolesAllowed({ AsmConstants.USERROLE_ADMINISTRATOR })
    @DELETE
    @Path("/{networkId}")
    Response deleteNetwork(@PathParam("networkId") String networkId);
}
