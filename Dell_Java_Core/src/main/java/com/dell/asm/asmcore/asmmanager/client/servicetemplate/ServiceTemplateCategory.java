
/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Objects;

// resources
@XmlType(name = "ServiceTemplateCategory", propOrder = {
        "id",
        "displayName",
        "parameters",
})
public class ServiceTemplateCategory
{    
    String id;
    String displayName;
    List<ServiceTemplateSetting> parameters = new ArrayList<ServiceTemplateSetting>();
    
    /**
     * Default constructor for the class.
     */
    public ServiceTemplateCategory() { }
    
    /**
     * Sets the id and display name from the id and display name of the ServiceTemplateSettingDef.
     * 
     * @param serviceTemplateSettingDef the service template setting definition used to set the id and display name.
     */
    public ServiceTemplateCategory(ServiceTemplateSettingDef serviceTemplateSettingDef) {
        this.setId(serviceTemplateSettingDef.getId());
        this.setDisplayName(serviceTemplateSettingDef.getDisplayName());
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public List<ServiceTemplateSetting> getParameters() {
        return parameters;
    }
    public void setParameters(List<ServiceTemplateSetting> settings) {
        this.parameters = settings;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String name) {
        displayName = name;
    }

    public ServiceTemplateSetting getParameter(String paramId) {
        for (ServiceTemplateSetting setting : getParameters()) {
            if (setting.getId().equals(paramId))
                return setting;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ServiceTemplateCategory) {
            if (((ServiceTemplateCategory) o).getId()==null &&
                    this.getId()!=null)
                return false;

            return ((ServiceTemplateCategory) o).getId().equals(this.getId());
        }
        return false;
    }

    @Override
    public int hashCode(){
        if (this.getId()==null)
            return 0;
        return this.getId().hashCode();
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("displayName", displayName)
                .add("parameters", parameters)
                .toString();
    }
}
