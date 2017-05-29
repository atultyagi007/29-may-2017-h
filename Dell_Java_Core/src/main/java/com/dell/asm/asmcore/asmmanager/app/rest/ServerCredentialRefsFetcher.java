/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.credential.ReferencesDTO;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.encryptionmgr.client.AbstractCredential;
import com.dell.asm.encryptionmgr.client.ServerCredential;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.pg.asm.server.client.ClientUtils;
import com.dell.pg.asm.server.client.device.IServerDeviceService;


/**
 * Helper class to communicate with ServerRA REST services to determine the
 * number of credential references.
 */
public class ServerCredentialRefsFetcher implements ICredentialRefsFetcher {
    private static final Logger LOGGER = Logger.getLogger(ServerCredentialRefsFetcher.class);

    private final IServerDeviceService deviceService;
    private DeviceInventoryDAO deviceInventoryDAO = new DeviceInventoryDAO();

    public ServerCredentialRefsFetcher(IServerDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public ReferencesDTO getReferences(AbstractCredential credential) {
        ReferencesDTO ret = new ReferencesDTO();

        if (credential instanceof ServerCredential) {
            String credentialId = credential.getId();
            if (credential.getLabel().equalsIgnoreCase(ClientUtils.DEFAULT_CREDENTIAL_LABEL)
                    || credential.getLabel().equalsIgnoreCase(ClientUtils.DEFAULT_BMC_CREDENTIAL_LABEL)) {
                List<String> filter = new ArrayList<String>();
                filter.add("eq,credId," + credentialId);
                FilterParamParser filterParser = new FilterParamParser(filter, DeviceInventoryService.validFilterColumns);
                List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();
                List<DeviceInventoryEntity> servers = this.deviceInventoryDAO.getAllDeviceInventory(null, filterInfos, null);
                if (servers != null && servers.size() > 0) {
                    int totalRecords = servers.size();
                    ret.setDevices(ret.getDevices() + totalRecords);
                }
            }

        }

        return ret;
    }
}
