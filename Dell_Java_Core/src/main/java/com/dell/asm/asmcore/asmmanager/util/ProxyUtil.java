/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util;

import com.dell.asm.alcm.client.service.IDatabaseService;
import com.dell.asm.alcm.client.service.INTPService;
import com.dell.asm.alcm.client.service.IWizardStatusService;
import com.dell.asm.asmcore.admin.rest.UserResource;
import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.app.rest.ConfigureDevicesService;
import com.dell.asm.asmcore.asmmanager.app.rest.DeploymentService;
import com.dell.asm.asmcore.asmmanager.app.rest.DeviceGroupService;
import com.dell.asm.asmcore.asmmanager.app.rest.DeviceInventoryService;
import com.dell.asm.asmcore.asmmanager.app.rest.DiscoverIPRangeDevicesService;
import com.dell.asm.asmcore.asmmanager.client.configure.IConfigureDevicesService;
import com.dell.asm.asmcore.asmmanager.client.deployment.IDeploymentService;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.IDeviceGroupService;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.IDiscoverIPRangeDevicesService;
import com.dell.asm.asmcore.asmmanager.tasks.DiscoverDeviceCallable;
import com.dell.asm.asmcore.user.model.IUserResource;
import com.dell.asm.asmdeployer.client.IAsmDeployerService;
import com.dell.asm.rest.common.client.AuthenticatedRestAPIWebClient;
import com.dell.asm.rest.common.model.AccessKeyPair;
import com.dell.asm.rest.common.model.Link;
import com.dell.asm.rest.common.util.RestUtil;
import com.dell.asm.rest.helpers.AsmJacksonJsonProvider;
import com.dell.asm.server.app.rest.ServerDeviceService;
import com.dell.asm.server.app.rest.ServerDiscoveryRequestService;
import com.dell.asm.usermanager.LocalSystemKeys;
import com.dell.pg.asm.chassis.app.rest.ChassisDiscoveryRequestService;
import com.dell.pg.asm.chassis.app.rest.ChassisService;
import com.dell.pg.asm.chassis.client.device.IChassisService;
import com.dell.pg.asm.chassis.client.discovery.IChassisDiscoveryRequestService;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.server.client.device.IServerDeviceService;
import com.dell.pg.asm.server.client.discovery.IServerDiscoveryRequestService;
import com.dell.pg.jraf.client.discoverymgr.IJrafDiscoveryManagerService;
import com.dell.pg.jraf.client.jobmgr.IJrafJobHistoryService;
import com.dell.pg.jraf.client.jobmgr.IJrafJobManagerService;
import com.dell.pg.jraf.client.profmgr.IJrafProfileManagerService;
import com.dell.pg.jraf.client.profmgr.IJrafTemplateService;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

/**
 * Utilities class to convert REST web service exposed DeviceInventory to DeviceInventoryEntity.
 *
 */
public final class ProxyUtil {
    private ProxyUtil() {}

    public static final int MAX_POLL_ITER = 30 * 60; // 60 minutes
    public static final int POLLING_INTERVAL = 2000; // msec

    private static final String HOST = "localhost"; // hostname of chassis hosting webapps
    private static final int PORT = 9080;
    private static final String JRAF_APP_PATH = "/JRAF";
    private static final String NETWORK_SERVICE_PATH = "/VirtualServices";
    private static final String ASM_MANAGER_PATH = "/AsmManager";
    private static final String ALCM_PATH = "/alcm";
    private static final int pollCount = 5;

    // WARNING:

    private static final IJrafDiscoveryManagerService dmProxy;
    private static final IJrafProfileManagerService profileMgrProxy;
    private static final IJrafJobHistoryService historyProxy;
    private static final IJrafTemplateService jrafTemplateProxy;
    private static final INetworkService networkProxy;
    private static IAsmDeployerService asmDeployerProxy = null;
    private static final IJrafJobManagerService jobManager;
    private static final IDatabaseService alcmDbService;
    private static final IWizardStatusService alcmStatusService;
    private static final INTPService alcmNTPService;

    private static final Logger logger = Logger.getLogger(DiscoverDeviceCallable.class);

    static {
        dmProxy = createProxyWithAuth(getJrafUrl(), IJrafDiscoveryManagerService.class);
        profileMgrProxy = createProxyWithAuth(getJrafUrl(), IJrafProfileManagerService.class);
        historyProxy = createProxyWithAuth(getJrafUrl(), IJrafJobHistoryService.class);
        jrafTemplateProxy = createProxyWithAuth( getJrafUrl(), IJrafTemplateService.class);
        networkProxy = createProxyWithAuth( getNetworkServiceURL(), INetworkService.class);

        jobManager = createProxyWithAuth(getJrafUrl(), IJrafJobManagerService.class);
        alcmDbService = createProxyWithAuth( getALCMServiceURL(), IDatabaseService.class);
        alcmStatusService = createProxyWithAuth( getALCMServiceURL(), IWizardStatusService.class);
        alcmNTPService = createProxyWithAuth( getALCMServiceURL(), INTPService.class);
    }

    public static void initAsmDeployerProxy() {
        // asm-deployer does not require the auth headers the java services do and always uses json
        JacksonJaxbJsonProvider provider = new AsmJacksonJsonProvider();
        provider.setAnnotationsToUse(new Annotations[]{Annotations.JACKSON, Annotations.JAXB});

        asmDeployerProxy = JAXRSClientFactory.create(AsmManagerApp.asmDeployerApiUrl, IAsmDeployerService.class,
                Collections.singletonList(provider));
        WebClient.client(asmDeployerProxy).type(MediaType.APPLICATION_JSON);
        WebClient.client(asmDeployerProxy).accept(MediaType.APPLICATION_JSON);
    }

    public static <T> T createProxyWithAuth(String baseAddress, Class<T> cls) {
        final LocalSystemKeys keyManager = LocalSystemKeys.getLocalSystemKeys();
        final AccessKeyPair keys = new AccessKeyPair().setApiKey(keyManager.getApplicationApiKey())
                .setApiSecret(keyManager.getApplicationApiSecret());
        return AuthenticatedRestAPIWebClient.createProxy(baseAddress, cls, keys);
    }

    private static <T> T createThreadSafeProxy(Object proxy, Class<T> klazz) {
        Client client = WebClient.client(proxy);
        return JAXRSClientFactory.fromClient(client, klazz);
    }

    private static <T> T createThreadSafeProxyWithHeaderSet(Object proxy, Class<T> klazz) {
        WebClient.client(proxy).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
        WebClient.client(proxy).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_TYPE);
        WebClient.client(proxy).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
        Client client = WebClient.client(proxy);

        return JAXRSClientFactory.fromClient(client, klazz);
    }

    private static <T> T createThreadSafeProxyWithHeaderSetXmlOnly(Object proxy, Class<T> klazz) {
        WebClient.client(proxy).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
        WebClient.client(proxy).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
        Client client = WebClient.client(proxy);

        return JAXRSClientFactory.fromClient(client, klazz, true);
    }

    /**
     * Compose the URL of the JRAF webapp
     *
     * @return JRAF webapp URL
     */
    public static String getJrafUrl() {
        try {
            return new URL("http", HOST, PORT, JRAF_APP_PATH).toString();
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public static String getNetworkServiceURL() {
        try {
            return new URL("http", HOST, PORT, NETWORK_SERVICE_PATH).toString();
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    private static String getALCMServiceURL() {
        try {
            return new URL("http", HOST, PORT, ALCM_PATH).toString();
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public static IJrafDiscoveryManagerService getDmProxy() {
        return createThreadSafeProxy(dmProxy, IJrafDiscoveryManagerService.class);
    }

    public static IJrafProfileManagerService getProfileMgrProxy() {
        return createThreadSafeProxy(profileMgrProxy, IJrafProfileManagerService.class);
    }

    public static IChassisService getDeviceChassisProxy() {
        return new ChassisService();
    }

    public static IChassisService getDeviceChassisProxyWithHeaderSet() {
        return getDeviceChassisProxy();
    }

    public static IChassisDiscoveryRequestService getChassisDiscoveryProxyWithHeaderSet() {
        return new ChassisDiscoveryRequestService();
    }

    public static IServerDiscoveryRequestService getServerDiscoveryProxyWithHeaderSet() {
        return new ServerDiscoveryRequestService();
    }

    public static IServerDeviceService getDeviceServerProxy() {
        return new ServerDeviceService();
    }

    public static IServerDeviceService getDeviceServerProxyWithHeaderSet() {
        return getDeviceServerProxy();
    }

    public static IJrafJobHistoryService getHistoryProxy() {
        return createThreadSafeProxy(historyProxy, IJrafJobHistoryService.class);
    }

    public static IJrafTemplateService getTemplateJrafProxy() {
        return createThreadSafeProxy(jrafTemplateProxy, IJrafTemplateService.class);
    }

    public static INetworkService getNetworkProxy() {
        return createThreadSafeProxyWithHeaderSetXmlOnly(networkProxy, INetworkService.class);
    }

    public static IUserResource getAdminProxy() {
        return new UserResource();
    }

    public static IDatabaseService getAlcmDBProxy() {
        return createThreadSafeProxyWithHeaderSetXmlOnly(alcmDbService, IDatabaseService.class);
    }

    public static IWizardStatusService getAlcmStatusProxy() {
        return createThreadSafeProxyWithHeaderSetXmlOnly(alcmStatusService, IWizardStatusService.class);
    }

    public static INTPService getAlcmNTPProxy() {
        return createThreadSafeProxyWithHeaderSetXmlOnly(alcmNTPService, INTPService.class);
    }

    public static IDeviceGroupService getServerPoolProxy() {
        return new DeviceGroupService();
    }

    public static IDeviceInventoryService getInventoryProxy() {
        return new DeviceInventoryService();
    }

    public static IDeploymentService getDeploymentProxy() {
        return new DeploymentService();
    }

    public static IAsmDeployerService getAsmDeployerProxy() {
        return createThreadSafeProxy(asmDeployerProxy, IAsmDeployerService.class);
    }

    public static IDiscoverIPRangeDevicesService getDiscoveryProxy() {
        return new DiscoverIPRangeDevicesService();
    }

    public static IConfigureDevicesService getDeviceConfigurationProxy() {
        return new ConfigureDevicesService();
    }

    public static IJrafJobManagerService getJobManagerProxy() {
        return createThreadSafeProxyWithHeaderSet(jobManager, IJrafJobManagerService.class);
    }

    public static void setProxyHeaders(Object ret, HttpServletRequest servletRequest) {
        Client client = WebClient.client(ret);
        if (client!=null) {
            if (servletRequest == null) {
                logger.error("Null server request.");
                throw new IllegalArgumentException("Servlet request must not be null");
            }

            String acceptHeader = servletRequest.getHeader(HttpHeaders.ACCEPT);
            if (!MediaType.APPLICATION_XML.equals(acceptHeader) && !MediaType.APPLICATION_JSON.equals(acceptHeader)) {
                // default to XML; IOM delete does not like text/plain ...
                acceptHeader = MediaType.APPLICATION_XML;
            }

            client.header(HttpHeaders.ACCEPT, acceptHeader);

            String contentTypeHeader = servletRequest.getHeader(HttpHeaders.CONTENT_TYPE);
            if (!StringUtils.isBlank(contentTypeHeader)) {
                WebClient.client(ret).header(HttpHeaders.CONTENT_TYPE, contentTypeHeader);
            }
        }
    }

    /* use setProxyHeader if calling the proxy services at the service layer
     *  if calling from a utilty class or a callable set the proxy headers before calling the RA
     */
    public static void setProxyHeaders(Object ret) {
        Client client = WebClient.client(ret);
        if (client!=null) {
            client.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            client.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
        }
    }

    public static Link buildJobLink(String title,String jobName, HttpServletRequest servletRequest,UriInfo uriInfo,HttpHeaders httpHeaders) {
        UriBuilder newBuilder = RestUtil.getProxyBaseURIBuilder(uriInfo, servletRequest, httpHeaders);

        newBuilder.replacePath("/JRAF/jobmgrHistory/pollExecStatus");
        newBuilder.queryParam("jobName",jobName);
        return new Link(title,newBuilder.build().toString(), Link.RelationType.MONITOR);
    }

    /**
     * Build detail device link which points to RA with refId.
     * @param type the device type.
     * @param title the title of the link.
     * @param refId the refId of the device.
     * @param servletRequest the servlet request.
     * @param uriInfo the uri info.
     * @param httpHeaders the http headers.
     * @return detail device link which points to RA with refId.
     */
    public static Link buildDeviceDetailLink(DeviceType type, String title,String refId, HttpServletRequest servletRequest,UriInfo uriInfo,HttpHeaders httpHeaders) {
        UriBuilder newBuilder = RestUtil.getProxyBaseURIBuilder(uriInfo, servletRequest, httpHeaders);
        if (DeviceTypeUtils.getResourceAdaptorType(type).equals(com.dell.pg.asm.chassis.client.ClientUtils.DEVICE_TYPE)) {
            newBuilder.replacePath(ASM_MANAGER_PATH + "/Chassis/" + refId);
        } else if (DeviceTypeUtils.getResourceAdaptorType(type).equals(com.dell.pg.asm.server.client.ClientUtils.DEVICE_TYPE))  {
            newBuilder.replacePath(ASM_MANAGER_PATH + "/Server/" + refId);
        } else {
            return null;
        }
        return new Link(title, newBuilder.build().toString(), Link.RelationType.DESCRIBEDBY);
    }
}
