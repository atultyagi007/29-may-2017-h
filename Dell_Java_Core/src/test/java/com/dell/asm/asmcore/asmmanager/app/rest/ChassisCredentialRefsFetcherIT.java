/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.cxf.endpoint.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.credential.ReferencesDTO;
import com.dell.asm.asmcore.asmmanager.util.integration.TestUtil;
import com.dell.asm.encryptionmgr.client.ChassisCredential;
import com.dell.asm.encryptionmgr.client.ICredentialService;
import com.dell.asm.encryptionmgr.rest.CredentialService;
import com.dell.asm.libext.tomcat.ContainerLifecycleListener;
import com.dell.asm.rest.helpers.filters.CXFAuthenticationFilter;
import com.dell.asm.rest.helpers.filters.ServiceContextCleanupFilter;
import com.dell.asm.rest.test.utils.TestServerUtils;
import com.dell.pg.asm.chassis.app.common.ChassisMockUtils;
import com.dell.pg.asm.chassis.app.rest.ChassisService;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IChassisService;

public class ChassisCredentialRefsFetcherIT {
    protected static Server server;
    protected static ContainerLifecycleListener lifecycleListener;

    private IChassisService deviceService;
    private ICredentialService credService;
    private ChassisCredentialRefsFetcher fetcher;

    public static List<Object> getTestProviders() {
        List<Object> providers = new LinkedList<>();
        providers.add(new CXFAuthenticationFilter());
        providers.add(new ServiceContextCleanupFilter());
        return providers;
    }

    public static List<Object> getTestResources() {
        List<Object> resources = new LinkedList<>();
        resources.add(new ChassisService());
        resources.add(new CredentialService());
        return resources;
    }

    @BeforeClass
    public static void setupServer() throws Exception {
        lifecycleListener = new ContainerLifecycleListener();
        lifecycleListener.startupHook();

        // Disable authentication
        System.setProperty("com.dell.asm.restapi.core.filters.authenticationDisabled", "true");
        server = TestServerUtils.startJettyServer(getTestResources(), getTestProviders());
        lifecycleListener.afterStartupHook();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        lifecycleListener.shutdownHook();
        server.destroy();
    }

    @Before
    public void setUp() {
        deviceService = TestUtil.createProxyWithTestAuth(
                TestServerUtils.getBaseUri(server), IChassisService.class);
        credService = TestUtil.createProxyWithTestAuth(
                TestServerUtils.getBaseUri(server), ICredentialService.class);
        fetcher = new ChassisCredentialRefsFetcher(deviceService);
    }

    @Test
    public void testDeviceRef() {
        long uniqueTestId = new Date().getTime();

        // Create chassis credential
        ChassisCredential credential = new ChassisCredential();
        credential.setLabel("Test Credential " + uniqueTestId);
        credential.setUsername("user");
        credential.setPassword("password");
        credential = (ChassisCredential) credService.createCredential(credential);

        // Verify no references exist yet
        ReferencesDTO refs1 = fetcher.getReferences(credential);
        assertNotNull(refs1);
        assertEquals(0, refs1.getPolicies());
        assertEquals(0, refs1.getDevices());

        // Create referring device
        Chassis chassis = ChassisMockUtils.makeChassisDeviceForTest();
        chassis.setCredentialRefId(credential.getId());
        chassis = deviceService.createChassis(chassis);

        // Verify ref
        ReferencesDTO refs2 = fetcher.getReferences(credential);
        assertNotNull(refs2);
        assertEquals(0, refs2.getPolicies());
        assertEquals(1, refs2.getDevices());

        // Delete chassis device
        deviceService.deleteChassis(chassis.getRefId());
        ReferencesDTO refs3 = fetcher.getReferences(credential);
        assertNotNull(refs3);
        assertEquals(0, refs3.getPolicies());
        assertEquals(0, refs3.getDevices());

        // Clean up
        credService.deleteCredential(credential.getId());
    }
}
