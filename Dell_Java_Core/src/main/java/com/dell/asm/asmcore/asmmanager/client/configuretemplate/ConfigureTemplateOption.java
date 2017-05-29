/**************************************************************************
 *   Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configuretemplate;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlType;

// parameters
@XmlType(name = "ConfigureTemplateOption", propOrder = {
        "id",
        "name",
        "dependencyTarget",
        "dependencyValue",
        "attributes"
})
public class ConfigureTemplateOption {

    private String id;
    private String name;
    private String dependencyTarget; // show this setting when dependencyTarget value() == dependencyValue
    private String dependencyValue;
    private Map<String, String> attributes = new HashMap<>();

    public ConfigureTemplateOption() {
    }

    public ConfigureTemplateOption(String id,
                                   String name) {
        this.id = id;
        this.name = name;
    }

    public ConfigureTemplateOption(String id, String name, String depTarget, String depValue) {
        this.id = id;
        this.name = name;
        this.dependencyTarget = depTarget;
        this.dependencyValue = depValue;
    }

    public ConfigureTemplateOption(String id ,String name,  String depTarget, String depValue, Map<String, String> attributes) {
        this.id = id;
        this.name = name;
        this.dependencyTarget = depTarget;
        this.dependencyValue = depValue;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDependencyTarget() {
        return dependencyTarget;
    }

    public void setDependencyTarget(String dependencyTarget) {
        this.dependencyTarget = dependencyTarget;
    }

    public String getDependencyValue() {
        return dependencyValue;
    }

    public void setDependencyValue(String dependencyValue) {
        this.dependencyValue = dependencyValue;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigureTemplateOption)) return false;

        ConfigureTemplateOption that = (ConfigureTemplateOption) o;

        if (!getId().equals(that.getId())) return false;
        return getName().equals(that.getName());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }
}
