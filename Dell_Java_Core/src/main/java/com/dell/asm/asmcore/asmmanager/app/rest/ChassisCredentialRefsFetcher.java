/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.rest.common.util.FilterParamParser;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.dell.asm.encryptionmgr.client.*;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.credential.ReferencesDTO;
import com.dell.asm.rest.common.AsmConstants;
import com.dell.pg.asm.chassis.client.ClientUtils;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IChassisService;

/**
 * Helper class to communicate with ChassisRA REST services to determine the
 * number of credential references.
 */
public class ChassisCredentialRefsFetcher implements ICredentialRefsFetcher {
    private static final Logger LOGGER = Logger.getLogger(ChassisCredentialRefsFetcher.class);

    private final IChassisService deviceService;

    public ChassisCredentialRefsFetcher(IChassisService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public ReferencesDTO getReferences(AbstractCredential credential) {
        final ReferencesDTO ret = new ReferencesDTO();
        final String credentialId = credential.getId();

        // Chassis devices only reference chassis credentials, so no point in looking for others
        if (credential instanceof ChassisCredential) {
            List<String> filters = Arrays.asList("eq,credentialRefId," + credentialId);
            List<Chassis> chassis = deviceService.getChassises(null, filters, 0, 1);// getting only 1 record since we are interested in total count only.
            ret.setDevices(ret.getDevices() + chassis.size());
        }

        credential.accept(new CredentialVisitor() {
            @Override
            public void visit(ChassisCredential credential) {

            }

            @Override
            public void visit(IomCredential credential) {

            }

            @Override
            public void visit(ServerCredential credential) {
 
            }

            @Override
            public void visit(VCenterCredential credential) {
            }

            @Override
            public void visit(StorageCredential credential) {
            }

            @Override
            public void visit(SCVMMCredential credential) {
            }

            @Override
            public void visit(EMCredential credential) {
            }

        });

        return ret;
    }
}
