/**************************************************************************
 * Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 * *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configuretemplate;

import com.google.common.base.Objects;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlType;

// resources
@XmlType(name = "ConfigureTemplateCategory", propOrder = {
        "id",
        "displayName",
        "deviceType",
        "parameters"
})
public class ConfigureTemplateCategory {
    String id;
    String displayName;
    String deviceType;
    Set<ConfigureTemplateSetting> parameters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<ConfigureTemplateSetting> getParameters() {
        if (parameters == null) {
            parameters = new LinkedHashSet<>();
        }
        return parameters;
    }

    public void setParameters(Set<ConfigureTemplateSetting> settings) {
        this.parameters = settings;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        displayName = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConfigureTemplateCategory) {
            if (((ConfigureTemplateCategory) o).getId() == null &&
                    this.getId() != null)
                return false;

            return ((ConfigureTemplateCategory) o).getId().equals(this.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null)
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

    public ConfigureTemplateSetting getParameter(String parameterId) {
        if (parameters != null && parameters.size() > 0) {
            for (ConfigureTemplateSetting setting : parameters) {
                if (setting.getId().equals(parameterId)) {
                    return setting;
                }
            }
        }
        return null;
    }

    public Map<String,ConfigureTemplateSetting> getParameterMap() {
        Map<String, ConfigureTemplateSetting> parametersMap = new HashMap<>();
        for (ConfigureTemplateSetting setting : getParameters()) {
            parametersMap.put(setting.getId(), setting);
        }
        return parametersMap;
    }
}
