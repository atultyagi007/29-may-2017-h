/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ApplicationModule {
    
    private String name;
    private String version;
    private String description;
    private List<OsReleaseInfo> operatingSystemSupport = new ArrayList<>();
    private List<ModuleRequirement> requirements = new ArrayList<>();

    private List<ServiceTemplateComponent> classes = new ArrayList<>();
    private List<ServiceTemplateComponent> types = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<OsReleaseInfo> getOperatingSystemSupport() {
        return operatingSystemSupport;
    }

    public void setOperatingSystemSupport(List<OsReleaseInfo> operatingSystemSupport) {
        this.operatingSystemSupport = operatingSystemSupport;
    }

    public List<ModuleRequirement> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<ModuleRequirement> requirements) {
        this.requirements = requirements;
    }

    public List<ServiceTemplateComponent> getClasses() {
        return classes;
    }

    public void setClasses(List<ServiceTemplateComponent> classes) {
        this.classes = classes;
    }

    public List<ServiceTemplateComponent> getTypes() {
        return types;
    }

    public void setTypes(List<ServiceTemplateComponent> types) {
        this.types = types;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ApplicationModule)) {
            return false;
        }
        ApplicationModule other = (ApplicationModule) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ApplicationModule [name=").append(name)
                .append(", version=").append(version)
                .append(", description=").append(description)
                .append("]");
        return builder.toString();
    }
    
    /**
     * Copies any values from the given applicaton module object if not already set currently
     * 
     * @param other
     */
    public void merge(ApplicationModule other) {
        if (this.equals(other) || other == null) {
            return;
        }
        
        if (StringUtils.isBlank(getName()) && StringUtils.isNotBlank(other.getName())) {
            setName(other.getName());
        }
        if (StringUtils.isBlank(getVersion()) && StringUtils.isNotBlank(other.getVersion())) {
            setVersion(other.getVersion());
        }
        if (StringUtils.isBlank(getDescription()) && StringUtils.isNotBlank(other.getDescription())) {
            setDescription(other.getDescription());
        }
        if (CollectionUtils.isEmpty(operatingSystemSupport) 
                && CollectionUtils.isNotEmpty(other.getOperatingSystemSupport())) {
            setOperatingSystemSupport(other.getOperatingSystemSupport());
        }
        if (CollectionUtils.isEmpty(requirements) && CollectionUtils.isNotEmpty(other.getRequirements())) {
            setRequirements(other.getRequirements());
        }
        if (CollectionUtils.isEmpty(classes) && CollectionUtils.isNotEmpty(other.getClasses())) {
            setClasses(other.getClasses());
        }
        if (CollectionUtils.isEmpty(types) && CollectionUtils.isNotEmpty(other.getTypes())) {
            setTypes(other.getTypes());
        }
    }
}
