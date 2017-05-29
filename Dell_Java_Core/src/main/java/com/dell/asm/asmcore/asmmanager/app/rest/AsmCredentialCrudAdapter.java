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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.credential.AsmCredentialDTO;
import com.dell.asm.asmcore.asmmanager.client.credential.ReferencesDTO;
import com.dell.asm.encryptionmgr.client.AbstractCredential;
import com.dell.asm.encryptionmgr.rest.CredentialCrudAdapter;
import com.dell.asm.i18n2.EEMILocalizableMessage;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.asm.rest.common.model.Link;
import com.dell.asm.rest.helpers.CrudAdapter;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.CredentialEntity;

public class AsmCredentialCrudAdapter implements CrudAdapter<CredentialEntity, AsmCredentialDTO> {

    private static final Logger logger = Logger.getLogger(AsmCredentialCrudAdapter.class);

    private final CredentialCrudAdapter delegate;
    private final List<ICredentialRefsFetcher> refFetchers;

    public AsmCredentialCrudAdapter(CredentialDAO dao, List<ICredentialRefsFetcher> refFetchers) {
        this.delegate = new CredentialCrudAdapter(dao);
        this.refFetchers = new ArrayList<>(refFetchers);
    }

    @Override
    public CredentialEntity create(CredentialEntity t) {
        return delegate.create(t);
    }

    @Override
    public void delete(CredentialEntity t) {
        HashMap<String, Integer> credIdToUsageCount = new AsmCredentialService().getCredToDeviceListMap();
        if (credIdToUsageCount.containsKey(t.getId()) && credIdToUsageCount.get(t.getId()) > 0) {
			logger.error("Prevented attempt to delete a credential that's already in use " + t.getLabel());
            throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                    AsmManagerMessages.credentialInUse(t.getLabel()));
        } else {
            delegate.delete(t);
        }
    }

    @Override
    public CredentialEntity get(String id) {
        return delegate.get(id);
    }

    @Override
    public List<CredentialEntity> getAll() {
        return delegate.getAll();
    }

    @Override
    public CredentialEntity update(CredentialEntity t) {
        return delegate.update(t);
    }

    private AsmCredentialDTO buildDtoWithoutRefs(CredentialEntity credentialEntity) {
        AbstractCredential abstractCredential = delegate.buildDto(credentialEntity);
        if (abstractCredential == null) {
            return null;
        } else {
            return new AsmCredentialDTO(abstractCredential);
        }
    }

    private ReferencesDTO getCredentialReferences(AbstractCredential credential) {
        ReferencesDTO ret = new ReferencesDTO();
        for (ICredentialRefsFetcher refFetcher : refFetchers) {
            ReferencesDTO refs = refFetcher.getReferences(credential);
            ret.addReferences(refs);
        }
        return ret;
    }

    @Override
    public AsmCredentialDTO buildDto(CredentialEntity credentialEntity) {
        AsmCredentialDTO ret = buildDtoWithoutRefs(credentialEntity);
        if (ret != null) {
            ret.addReferences(getCredentialReferences(ret.getCredential()));
        }
        return ret;
    }

    @Override
    public List<EEMILocalizableMessage> validate(AsmCredentialDTO dto) {
        AbstractCredential credential = dto.getCredential();
        if (credential == null) {
            return Arrays.asList(AsmManagerMessages.noCredentialSpecified());
        } else {
            return delegate.validate(credential);
        }
    }

    @Override
    public CredentialEntity buildEntity(AsmCredentialDTO asmCredentialDTO) {
        return delegate.buildEntity(asmCredentialDTO.getCredential());
    }

    @Override
    public void prepareDtoForUpdate(AsmCredentialDTO body, CredentialEntity credentialEntity) {
        delegate.prepareDtoForUpdate(body.getCredential(), credentialEntity);
    }

    @Override
    public Object getId(AsmCredentialDTO asmCredentialDTO) {
        return delegate.getId(asmCredentialDTO.getCredential());
    }

    @Override
    public String getLabel(AsmCredentialDTO asmCredentialDTO) {
        return delegate.getLabel(asmCredentialDTO.getCredential());
    }

    @Override
    public void setLink(AsmCredentialDTO asmCredentialDTO, Link Link) {
        asmCredentialDTO.setLink(Link);
    }

    @Override
    public void setId(AsmCredentialDTO body, Object bodyId) {
        delegate.setId(body.getCredential(), bodyId);
    }
}
