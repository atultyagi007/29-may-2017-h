
/**************************************************************************
 *   Copyright (c) 2016 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlType;

// parameters
@XmlType(name = "ServiceTemplateOption", propOrder = {
        "value",
        "name",
        "dependencyTarget",
        "dependencyValue",
        "attributes"
})
public class ServiceTemplateOption implements Comparable<ServiceTemplateOption> {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String name;
    private String value;
    private String dependencyTarget; // show this setting when dependencyTarget value() == dependencyValue
    private String dependencyValue;
    private Map<String, String> attributes = new HashMap<>();

    /**
     * Attributes that might be useful to pass along with template setting. Example: vlanid for portgroup.
     * @return
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
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

    public ServiceTemplateOption() {
    }

    public ServiceTemplateOption(String name, String value, String depTarget, String depValue) {
        this.name = name;
        this.value = value;
        this.dependencyTarget = depTarget;
        this.dependencyValue = depValue;
    }

    public ServiceTemplateOption(String name, String value, String depTarget, String depValue, Map<String, String> attributes) {
        this.name = name;
        this.value = value;
        this.dependencyTarget = depTarget;
        this.dependencyValue = depValue;
        this.attributes = attributes;
    }

    @Override
    public int compareTo(ServiceTemplateOption o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        ServiceTemplateOption sObj = null;
        if (obj instanceof ServiceTemplateOption)
            sObj = (ServiceTemplateOption) obj;
        else
            return false;

        // options must have unique ID
        String a = this.value;
        String b = sObj.value;

        return a.equals(b);
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getValue().hashCode();
        return result;
    }
}
