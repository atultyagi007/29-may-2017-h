/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.credential;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.dell.asm.encryptionmgr.client.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.dell.asm.rest.common.model.Link;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "asmCredential")
@XmlType(name = "ASMCredential", propOrder = {
        "link",
        "credential",
        "references" })
@XmlSeeAlso({ ChassisCredential.class, IomCredential.class, ServerCredential.class, StorageCredential.class, VCenterCredential.class, SCVMMCredential.class, EMCredential.class})
@ApiModel()
public class AsmCredentialDTO {
    private Link link;
    private AbstractCredential credential;
    private ReferencesDTO references;

    public AsmCredentialDTO() {
        // no-arg constructor for JAXB
        this(null);
    }

    public AsmCredentialDTO(AbstractCredential credential) {
        // shortcut for wrapping an RA credential object in an AsmCredentialDTO
        this.credential = credential;
    }

    @ApiModelProperty(value = "Self Link", required = false, notes = "ignored with POST, PUT methods")
    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    @ApiModelProperty(value = "References", required = false, notes = "ignored with POST, PUT methods")
    public ReferencesDTO getReferences() {
        return references;
    }

    public void setReferences(ReferencesDTO references) {
        this.references = references;
    }

    public void addReferences(ReferencesDTO references) {
        if (this.references == null) {
            this.references = new ReferencesDTO();
        }
        this.references.addReferences(references);
    }

    /**
     * Returns the "native" credential object. That is, the full credential
     * object that would be returned by the backing RA credential service.
     *
     * <p>For example if the credential {@code type} is {@code CHASSIS},
     * this would be the full object returned by the Chassis RA credential
     * service for this credential.
     *
     * @return The native credential object
     */
    @XmlElementRef
    @ApiModelProperty(value = "Credential", required = true)
    public AbstractCredential getCredential() {
        return credential;
    }

    public void setCredential(AbstractCredential credential) {
        this.credential = credential;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("credential", credential)
                .append("references", references)
                .toString();
    }
}
