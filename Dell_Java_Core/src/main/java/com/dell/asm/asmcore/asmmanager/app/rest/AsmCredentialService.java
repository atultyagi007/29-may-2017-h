/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import static com.dell.asm.encryptionmgr.rest.CredentialService.buildIdNotAllowedOnCreate;
import static com.dell.asm.encryptionmgr.rest.CredentialService.buildMismatchedUpdateId;
import static com.dell.asm.encryptionmgr.rest.CredentialService.buildNotFoundException;
import static com.dell.asm.rest.common.RestCommonMessages.invalidSortColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.credential.AsmCredentialDTO;
import com.dell.asm.asmcore.asmmanager.client.credential.AsmCredentialListDTO;
import com.dell.asm.asmcore.asmmanager.client.credential.IAsmCredentialService;
import com.dell.asm.asmcore.asmmanager.db.DeviceInventoryDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.encryptionmgr.client.CredentialType;
import com.dell.asm.encryptionmgr.rest.CredentialService;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.i18n2.exception.AsmValidationException;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.asm.rest.helpers.BaseCrudService;
import com.dell.asm.rest.helpers.CrudAdapter;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.CredentialEntity;

@Path("/credential")
public class AsmCredentialService
        extends BaseCrudService<CredentialEntity, AsmCredentialDTO>
        implements IAsmCredentialService {
    private static final Logger LOGGER = Logger.getLogger(AsmCredentialService.class);

    // WARNING: if columns are added here, add them to the mapColumnName method
    private static final Set<String> VALID_SORT_COLUMNS = new HashSet<>(
            Arrays.asList("id", "uri", "type", "label",
                    "numReferencingDevices",
                    "numReferencingPolicies","username"));
    private static final Set<String> VALID_FILTER_COLUMNS = new HashSet<>(VALID_SORT_COLUMNS);

    private DeviceInventoryDAO deviceInventoryDAO = new DeviceInventoryDAO();

    public AsmCredentialService(CrudAdapter<CredentialEntity, AsmCredentialDTO> adapter) {
        super(adapter, IAsmCredentialService.class);
    }

    // For JAX-RS to instantiate the class
    public AsmCredentialService() {
        this(buildAdapter());
    }

    private static CrudAdapter<CredentialEntity, AsmCredentialDTO> buildAdapter() {
        List<ICredentialRefsFetcher> refFetchers = new ArrayList<>();
        refFetchers.add(new ChassisCredentialRefsFetcher(
                ProxyUtil.getDeviceChassisProxy()));
        refFetchers.add(new ServerCredentialRefsFetcher(
                ProxyUtil.getDeviceServerProxy()));
        CredentialDAO dao = CredentialDAO.getInstance();
        return new AsmCredentialCrudAdapter(dao, refFetchers);
    }

    @Override
    public AsmCredentialListDTO getAllCredentials(CredentialType typeFilter,
                                                  String sort, List<String> filter,
                                                  Integer offset, Integer limit) {

        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
        AsmCredentialListDTO ret = new AsmCredentialListDTO();
        try
        {
            SortParamParser sp = new SortParamParser(sort, VALID_SORT_COLUMNS);
            List<SortParamParser.SortInfo> sortInfos = sp.parse();

            // Build filter list from filter params ( comprehensive )
            FilterParamParser filterParser = new FilterParamParser(filter, VALID_FILTER_COLUMNS);
            List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

            // Build filter list from query params ( simple )
            if (typeFilter != null) {
                FilterParamParser.FilterInfo simpleFilter = filterParser.new FilterInfo("type", typeFilter.toString());
                filterInfos.add(simpleFilter);
            }

            // Map the input column names to the nested property names, e.g.
            // credential.label or references.devices
            mapSortInfos(sortInfos);
            mapFilterInfos(filterInfos);

            BaseCrudService.Slice<AsmCredentialDTO> slice = super.getAll(
                sortInfos, filterInfos, offset, limit);
            ret.setCredentialList(slice.getItems());

            // loop through devices to build count of usage for reach cred
            updateCredCount(ret.getCredentialList());

            ret.setTotalRecords(slice.getTotalItems());
        }
        catch (Exception e) {
            LOGGER.error("Failed to getAllCredentials", e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, AsmManagerMessages.internalError());
        }        
        return ret;
    }

    private void updateCredCount(List<AsmCredentialDTO> creds) {
        HashMap<String, Integer> credIdToUsageCount = getCredToDeviceListMap();

        for (AsmCredentialDTO currCred : creds) {
            if (credIdToUsageCount.containsKey(currCred.getCredential().getId())) {
                if (currCred.getReferences() == null) {
                    currCred.addReferences(null);
                }
                currCred.getReferences().setDevices(credIdToUsageCount.get(currCred.getCredential().getId()));
            }
        }
    }

    private void updateCredCount(AsmCredentialDTO cred) {
        HashMap<String, Integer> credIdToUsageCount = getCredToDeviceListMap();
        if (credIdToUsageCount.containsKey(cred.getCredential().getId())) {
            if (cred.getReferences() == null) {
                cred.addReferences(null);
            }
            cred.getReferences().setDevices(credIdToUsageCount.get(cred.getCredential().getId()));
        }
    }

    public HashMap<String, Integer> getCredToDeviceListMap() {
        List<DeviceInventoryEntity> entities = deviceInventoryDAO.getAllDeviceInventory(null, null, null);
        HashMap<String, Integer> credIdToUsageCount = new HashMap<String, Integer>();
        for (DeviceInventoryEntity currDevice : entities) {
            if (!credIdToUsageCount.containsKey(currDevice.getCredId())) {
                credIdToUsageCount.put(currDevice.getCredId(), 1);
            } else {
                credIdToUsageCount.put(currDevice.getCredId(), credIdToUsageCount.get(currDevice.getCredId()) + 1);
            }
        }
        return credIdToUsageCount;
    }


    private void mapFilterInfos(List<FilterParamParser.FilterInfo> filterInfos) {
        for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
            String columnName = mapColumnName(filterInfo.getColumnName());
            filterInfo.setColumnName(columnName);
            if ("credential.class".equals(columnName)) {
                filterInfo.setColumnValue(
                        CredentialService.getDTOClassFromCredentialType(filterInfo.getColumnValue()));
            }
        }
    }

    private void mapSortInfos(List<SortParamParser.SortInfo> sortInfos) {
        for (SortParamParser.SortInfo sortInfo : sortInfos) {
            String columnName = mapColumnName(sortInfo.getColumnName());
            sortInfo.setColumnName(columnName);
        }
    }

    private String mapColumnName(String columnName) {
        switch (columnName) {
            case "username":
                return "credential.username";
        case "id":
            return "credential.id";
        case "uri":
            return "credential.uri";
        case "type":
            return "credential.class";
        case "label":
            return "credential.label";
        case "numReferencingDevices":
            return "references.devices";
        case "numReferencingPolicies":
            return "references.policies";
        default:
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST, invalidSortColumn(columnName));
        }
    }

    @Override
    public AsmCredentialDTO getCredential(String id) {
        try {
            AsmCredentialDTO ret = super.get(id);
            updateCredCount(ret);
            return ret;
        } catch (BaseCrudService.NotFoundException e) {
            throw buildNotFoundException(e.getPathId());
        }
    }

    @Override
    public AsmCredentialDTO createCredential(AsmCredentialDTO dto) {
        try {
            return super.create(dto);
        } catch (AsmValidationException e) {
            String debugMsg = "Credential validation failed for " + dto;
            List<EEMILocalizableMessage> errors = e.getEEMILocalizableMessages();
            LOGGER.error(debugMsg + "; errors = " + errors);
            throw new AsmValidationException(debugMsg, errors);
        } catch (BaseCrudService.IdNotAllowedOnCreateException e) {
            throw buildIdNotAllowedOnCreate();
        }
    }

    @Override
    public AsmCredentialDTO updateCredential(String id, AsmCredentialDTO dto) {
        try {
            return super.update(id, dto);
        } catch (AsmValidationException e) {
            String debugMsg = "Credential validation failed for " + dto;
            List<EEMILocalizableMessage> errors = e.getEEMILocalizableMessages();
            LOGGER.error(debugMsg + "; errors = " + errors);
            throw new AsmValidationException(debugMsg, errors);
        } catch (BaseCrudService.NotFoundException e) {
            throw buildNotFoundException(id);
        } catch (BaseCrudService.MismatchedUpdateIdException e) {
            throw buildMismatchedUpdateId(e.getPathId(), (String) e.getBodyId());
        }
    }

    @Override
    public void deleteCredential(String id) {
        try {
        	// Prevent delete default credentials.
        	// For now, if the name begins with "Dell" and ends with "default" we consider it a default credential.
        	CredentialEntity entity = dao.get(id);
        	if (entity != null && entity.getLabel() != null) {
        		String label = entity.getLabel().toLowerCase();
        		if (label.startsWith("dell") && label.endsWith("default")) {
        			LOGGER.error("Prevented attempt to delete default credential " + entity.getLabel());
        	        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
        	            AsmManagerMessages.deleteDefaultCredential(entity.getLabel()));
        		}
        	}

            super.delete(id);
        } catch (BaseCrudService.NotFoundException e) {
            throw buildNotFoundException(e.getPathId());
        }
    }

}
