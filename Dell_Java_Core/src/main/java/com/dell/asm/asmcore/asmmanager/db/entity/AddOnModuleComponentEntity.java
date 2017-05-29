/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.ForeignKey;

import com.dell.asm.asmcore.asmmanager.client.addonmodule.AddOnModuleComponentType;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntity;

@Entity
@Table(name = "add_on_module_components", schema = "public")
@DynamicUpdate
public class AddOnModuleComponentEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "add_on_module", referencedColumnName = "id")
    @ForeignKey(name = "components_to_add_on_module_fk")
    private AddOnModuleEntity addOnModuleEntity;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private AddOnModuleComponentType type;

    @Column(name = "marshalled_data")
    private String marshalledData;

    public AddOnModuleEntity getAddOnModuleEntity() {
        return addOnModuleEntity;
    }

    public void setAddOnModuleEntity(AddOnModuleEntity addOnModuleEntity) {
        this.addOnModuleEntity = addOnModuleEntity;
    }

    public AddOnModuleComponentType getType() {
        return type;
    }

    public void setType(AddOnModuleComponentType type) {
        this.type = type;
    }

    public String getMarshalledData() {
        return marshalledData;
    }

    public void setMarshalledData(String marshalledData) {
        this.marshalledData = marshalledData;
    }


}
