/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.integration.credential;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.credential.AsmCredentialDTO;
import com.dell.asm.asmcore.asmmanager.client.credential.AsmCredentialListDTO;
import com.dell.asm.asmcore.asmmanager.client.credential.IAsmCredentialService;
import com.dell.asm.asmcore.asmmanager.client.test.TestUtil;
import com.dell.asm.encryptionmgr.client.AbstractCredential;
import com.dell.asm.encryptionmgr.client.ChassisCredential;
import com.dell.asm.encryptionmgr.client.IomCredential;
import com.dell.asm.encryptionmgr.client.ServerCredential;

public class IAsmCredentialServiceTest {
    private static boolean CLEANUP = System.getProperty("CLEANUP") == null ? true
            : Boolean.valueOf(System.getProperty("CLEANUP"));

    private ServerCredential assertServerCredentialsEqual(ServerCredential serverDTO,
                                                          AbstractCredential abstractDTO) {
        assertNotNull(serverDTO);
        assertNotNull(abstractDTO);

        assertEquals(serverDTO.getLabel(), abstractDTO.getLabel());
        assertEquals(serverDTO.getId(), abstractDTO.getId());
        assertEquals(serverDTO.getUsername(), abstractDTO.getUsername());

        assertNull("DTO credentials should not include the password", abstractDTO.getPassword());

        assertTrue(abstractDTO instanceof ServerCredential);
        return (ServerCredential) abstractDTO;
    }

    @Test
    public void testServerCredentialCRUD() {
        IAsmCredentialService credentialService = TestUtil.createProxyWithTestAuth(
                TestUtil.ASM_URL, IAsmCredentialService.class);

        // Build server credential
        ServerCredential serverCredential = buildServerCredential();

        // Create server credential via aggregate service and validate response
        AsmCredentialDTO asmServerCredential = new AsmCredentialDTO(serverCredential);
        AsmCredentialDTO asmCreatedCredential = credentialService.createCredential(asmServerCredential);
        assertNotNull("DTO uri is null", asmCreatedCredential.getLink());
        AbstractCredential createdCredential = asmCreatedCredential.getCredential();
        // Fix up the serverCredentialId with the created value
        serverCredential.setId(createdCredential.getId());
        ServerCredential createdServerCredential
                = assertServerCredentialsEqual(serverCredential, createdCredential);
        String createdId = createdServerCredential.getId();
        assertNotNull(createdId);

        // Update server credential and validate response
        createdServerCredential.setLabel("UPDATED " + serverCredential.getLabel());
        createdServerCredential.setUsername("root");
        // copy password from original DTO; its not passed back in responses
        createdServerCredential.setPassword(serverCredential.getPassword());
        AsmCredentialDTO asmCreatedServerCredential = new AsmCredentialDTO(createdServerCredential);
        AsmCredentialDTO asmUpdatedCredential = credentialService.updateCredential(createdId,
                asmCreatedServerCredential);
        AbstractCredential updatedCredential = asmUpdatedCredential.getCredential();
        assertServerCredentialsEqual(createdServerCredential, updatedCredential);

        // Check specific get
        AsmCredentialDTO asmFoundCredential = credentialService.getCredential(createdId);
        AbstractCredential foundCredential = asmFoundCredential.getCredential();
        assertServerCredentialsEqual(createdServerCredential, foundCredential);

        // Check get all
        AsmCredentialListDTO allCredentials = credentialService.getAllCredentials(null, null, null, null, null);
        AbstractCredential foundInList = null;
        for (AsmCredentialDTO asmCredentialDTO : allCredentials.getCredentialList()) {
            AbstractCredential abstractCredentialDTO = asmCredentialDTO.getCredential();
            if (abstractCredentialDTO instanceof ServerCredential
                    && createdId.equals(abstractCredentialDTO.getId())) {
                foundInList = abstractCredentialDTO;
                break;
            }
        }
        assertServerCredentialsEqual(createdServerCredential, foundInList);

        if (CLEANUP) {
            // Delete server credential
            credentialService.deleteCredential(createdId);

            // Verify no longer found
            int responseStatus = -1;
            try {
                credentialService.getCredential(createdId);
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                responseStatus = response.getStatus();
            }
            assertEquals(404, responseStatus);
        }
    }

    private ServerCredential buildServerCredential() {
        ServerCredential serverCredential = new ServerCredential();
        serverCredential.setLabel("Test Credential-" + new Date().getTime());
        serverCredential.setDomain("Americas");
        serverCredential.setUsername("admin");
        serverCredential.setPassword("password");
        return serverCredential;
    }

    private ChassisCredential assertChassisCredentialsEqual(ChassisCredential chassisCredentialDTO,
                                                            AbstractCredential abstractCredentialDTO) {
        assertNotNull(chassisCredentialDTO);
        assertNotNull(abstractCredentialDTO);

        assertEquals(chassisCredentialDTO.getId(), abstractCredentialDTO.getId());
        assertEquals(chassisCredentialDTO.getLabel(), abstractCredentialDTO.getLabel());
        assertEquals(chassisCredentialDTO.getUsername(), abstractCredentialDTO.getUsername());

        assertNull("DTO credentials should not include the password", abstractCredentialDTO.getPassword());

        assertTrue(abstractCredentialDTO instanceof ChassisCredential);
        return (ChassisCredential) abstractCredentialDTO;
    }

    @Test
    public void testChassisCredentialCRUD() {
        IAsmCredentialService credentialService = TestUtil.createProxyWithTestAuth(
                TestUtil.ASM_URL, IAsmCredentialService.class);

        // Build chassis credential
        ChassisCredential chassisCredential = buildChassisCredential();

        // Create chassis credential via aggregate service and validate response
        AsmCredentialDTO asmCreatedCredential =
                credentialService.createCredential(new AsmCredentialDTO(chassisCredential));
        assertNotNull("Created DTO URI is null", asmCreatedCredential.getLink());
        AbstractCredential createdCredential = asmCreatedCredential.getCredential();
        // Fix up the chassisCredentialId with the created value
        chassisCredential.setId(createdCredential.getId());
        ChassisCredential createdChassisCredential
                = assertChassisCredentialsEqual(chassisCredential, createdCredential);
        assertNotNull(createdChassisCredential.getId());

        // Update chassis credential and validate response
        createdChassisCredential.setLabel("UPDATED " + chassisCredential.getLabel());
        createdChassisCredential.setUsername("root");
        // copy password from original DTO; its not passed back in responses
        createdChassisCredential.setPassword(chassisCredential.getPassword());
        AsmCredentialDTO asmUpdatedCredential = credentialService.updateCredential(
                createdChassisCredential.getId(), new AsmCredentialDTO(createdChassisCredential));
        assertChassisCredentialsEqual(createdChassisCredential, asmUpdatedCredential.getCredential());

        // Check specific get
        AsmCredentialDTO foundCredential = credentialService.getCredential(createdChassisCredential.getId());
        assertChassisCredentialsEqual(createdChassisCredential, foundCredential.getCredential());

        // Check get all
        AsmCredentialListDTO allCredentials = credentialService.getAllCredentials(null, null, null, null, null);
        AbstractCredential foundInList = null;
        for (AsmCredentialDTO asmCredentialDTO : allCredentials.getCredentialList()) {
            AbstractCredential abstractCredentialDTO = asmCredentialDTO.getCredential();
            if (abstractCredentialDTO instanceof ChassisCredential
                    && createdChassisCredential.getId().equals(abstractCredentialDTO.getId())) {
                foundInList = abstractCredentialDTO;
                break;
            }
        }
        assertChassisCredentialsEqual(createdChassisCredential, foundInList);

        if (CLEANUP) {
            // Delete chassis credential
            credentialService.deleteCredential(createdChassisCredential.getId());

            // Verify no longer found
            int responseStatus = -1;
            try {
                credentialService.getCredential(createdChassisCredential.getId());
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                responseStatus = response.getStatus();
            }
            assertEquals(404, responseStatus);
        }
    }

    private ChassisCredential buildChassisCredential() {
        ChassisCredential chassisCredential = new ChassisCredential();
        chassisCredential.setLabel("Test Credential-" + new Date().getTime());
        chassisCredential.setDomain("Americas");
        chassisCredential.setUsername("admin");
        chassisCredential.setPassword("password");
        return chassisCredential;
    }

    private IomCredential assertIomCredentialsEqual(IomCredential iomCredentialDTO,
                                                    AbstractCredential abstractCredentialDTO) {
        assertNotNull(iomCredentialDTO);
        assertNotNull(abstractCredentialDTO);

        assertEquals(iomCredentialDTO.getLabel(), abstractCredentialDTO.getLabel());
        assertEquals(iomCredentialDTO.getId(), abstractCredentialDTO.getId());
        assertEquals(iomCredentialDTO.getUsername(), abstractCredentialDTO.getUsername());

        assertNull("DTO credentials should not include the password", abstractCredentialDTO.getPassword());

        assertTrue(abstractCredentialDTO instanceof IomCredential);
        return (IomCredential) abstractCredentialDTO;
    }

    @Test
    public void testIomCredentialCRUD() {
        IAsmCredentialService credentialService = TestUtil.createProxyWithTestAuth(
                TestUtil.ASM_URL, IAsmCredentialService.class);

        // Build iom credential
        IomCredential iomCredential = buildIomCredential();

        // Create iom credential via aggregate service and validate response
        AsmCredentialDTO asmCreatedCredential = credentialService.createCredential(new AsmCredentialDTO(iomCredential));
        assertNotNull("DTO credentials should include the URI", asmCreatedCredential.getLink());
        AbstractCredential createdCredential = asmCreatedCredential.getCredential();

        // Fix up the iomCredentialId with the created value
        iomCredential.setId(createdCredential.getId());
        IomCredential createdIomCredential = assertIomCredentialsEqual(iomCredential, createdCredential);
        assertNotNull(createdCredential.getId());

        // Update iom credential and validate response
        createdIomCredential.setLabel("UPDATED " + iomCredential.getLabel());
        createdIomCredential.setUsername("root");
        // copy password from original DTO; its not passed back in responses
        createdIomCredential.setPassword(iomCredential.getPassword());
        AsmCredentialDTO updatedCredential = credentialService.updateCredential(
                createdIomCredential.getId(), new AsmCredentialDTO(createdIomCredential));
        assertIomCredentialsEqual(createdIomCredential, updatedCredential.getCredential());

        // Check specific get
        String foundId = createdCredential.getId();
        AsmCredentialDTO foundCredential = credentialService.getCredential(foundId);
        assertIomCredentialsEqual(createdIomCredential, foundCredential.getCredential());

        // Check get all
        AsmCredentialListDTO allCredentials = credentialService.getAllCredentials(null, null, null, null, null);
        AbstractCredential foundInList = null;
        for (AsmCredentialDTO asmCredentialDTO : allCredentials.getCredentialList()) {
            AbstractCredential abstractCredentialDTO = asmCredentialDTO.getCredential();
            if (abstractCredentialDTO instanceof IomCredential
                    && createdIomCredential.getId().equals(abstractCredentialDTO.getId())) {
                foundInList = abstractCredentialDTO;
                break;
            }
        }
        assertIomCredentialsEqual(createdIomCredential, foundInList);

        if (CLEANUP) {
            // Delete iom credential
            credentialService.deleteCredential(createdIomCredential.getId());

            // Verify no longer found
            int responseStatus = -1;
            try {
                credentialService.getCredential(createdCredential.getId());
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                responseStatus = response.getStatus();
            }

            assertEquals(404, responseStatus);
        }
    }

    private IomCredential buildIomCredential() {
        IomCredential iomCredential = new IomCredential();
        iomCredential.setLabel("Test Credential-" + new Date().getTime());
        iomCredential.setDomain("Americas");
        iomCredential.setUsername("admin");
        iomCredential.setPassword("password");
        iomCredential.setProtocol("SSH");
        iomCredential.setSnmpCommunityString("iluvcommunity");
        return iomCredential;
    }

    @Test
    public void testSortByType() {
        IAsmCredentialService credentialService = TestUtil.createProxyWithTestAuth(
                TestUtil.ASM_URL, IAsmCredentialService.class);

        // Make sure there is enough data to sort
        List<AsmCredentialDTO> credentials = Arrays.asList(
                credentialService.createCredential(new AsmCredentialDTO(buildServerCredential())),
                credentialService.createCredential(new AsmCredentialDTO(buildChassisCredential())),
                credentialService.createCredential(new AsmCredentialDTO(buildIomCredential()))
        );

        AsmCredentialListDTO foundList = credentialService.getAllCredentials(null, "type", null, null, null);
        List<AsmCredentialDTO> found = foundList.getCredentialList();
        AbstractCredential lastCredential = null;
        for (AsmCredentialDTO asmCredentialDTO : found) {
            AbstractCredential credential = asmCredentialDTO.getCredential();
            if (lastCredential != null) {
                String lastType = lastCredential.getClass().getSimpleName();
                String type = credential.getClass().getSimpleName();
                assertTrue("Credentials out of order " + lastCredential + " > " + credential,
                        lastType.compareToIgnoreCase(type) <= 0);
            }
            lastCredential = credential;
        }

        // Delete the credentials we created
        for (AsmCredentialDTO credential : credentials) {
            credentialService.deleteCredential(credential.getCredential().getId());
        }
    }

    @Test
    public void testIdIsOptionalOnUpdate() {
        IAsmCredentialService credentialService = TestUtil.createProxyWithTestAuth(
                TestUtil.ASM_URL, IAsmCredentialService.class);
        AbstractCredential credential = buildServerCredential();

        AsmCredentialDTO createdAsmDto = credentialService.createCredential(new AsmCredentialDTO(credential));
        AbstractCredential created = createdAsmDto.getCredential();
        String id = created.getId();
        created.setId(null);
        created.setLabel(created.getLabel() + "-" + new Date().getTime());
        credentialService.updateCredential(id, new AsmCredentialDTO(created));

        AsmCredentialDTO found = credentialService.getCredential(id);
        assertEquals(created.getLabel(), found.getCredential().getLabel());

        credentialService.deleteCredential(id);
    }
}
