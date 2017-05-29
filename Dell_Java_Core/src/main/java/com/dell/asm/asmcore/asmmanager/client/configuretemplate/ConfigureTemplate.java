/**************************************************************************
 * Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 * *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configuretemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "ConfigureTemplate")
@XmlType(name = "ConfigureTemplate", propOrder = {
        "id",
        "categories"
})
public class ConfigureTemplate {

    private String id;
    private Set<ConfigureTemplateCategory> categories;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<ConfigureTemplateCategory> getCategories() {
        if (categories == null) {
            categories = new HashSet<>();
        }
        return categories;
    }

    public void setCategories(Set<ConfigureTemplateCategory> categories) {
        this.categories = categories;
    }

    public Map<String, ConfigureTemplateCategory> getCategoriesMap() {
        Map<String, ConfigureTemplateCategory> categoryMap = new HashMap<>();
        for (ConfigureTemplateCategory category : getCategories()) {
            categoryMap.put(category.getId(), category);
        }
        return categoryMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigureTemplate)) return false;

        ConfigureTemplate that = (ConfigureTemplate) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
