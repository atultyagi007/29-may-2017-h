/**************************************************************************
 * Copyright (c) 2017 Dell Inc. All rights reserved.                      *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.configuretemplate;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ConfigureTemplateSetting", propOrder = {
        "id",
        "value",
        "displayName",
        "type",
        "toolTip",
        "required",
        "deviceType",
        "dependencyTarget",
        "dependencyValue",
        "group",
        "readOnly",
        "generated",
        "infoIcon",
        "step",
        "maxLength",
        "min",
        "max",
        "networks",
        "options",
        "optionsSortable"
})
public class ConfigureTemplateSetting {

    public enum ConfigureTemplateSettingType {
        BOOLEAN,
        STRING,
        PASSWORD,
        INTEGER,
        LIST,  // comma separated list of string
        TEXT,
        ENUMERATED,
        RADIO
    }

    private static int MAXLENGTH = 256;

    private String id;
    private String value;
    private String displayName;
    private ConfigureTemplateSettingType type;
    private String toolTip;
    private boolean required = true;
    private String deviceType;
    private String dependencyTarget;
    private String dependencyValue;
    private String group;
    private boolean readOnly = false;
    private boolean generated = false;
    private boolean infoIcon = false;
    private Integer step = 1;
    private Integer maxLength = MAXLENGTH;
    private Integer min; // for integer values
    private Integer max; // for integer values
    private Set<Network> networks;
    private Set<ConfigureTemplateOption> options;
    private boolean optionsSortable = true;

    public ConfigureTemplateSetting() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ConfigureTemplateSettingType getType() {
        return type;
    }

    public void setType(ConfigureTemplateSettingType type) {
        this.type = type;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public boolean isInfoIcon() {
        return infoIcon;
    }

    public void setInfoIcon(boolean infoIcon) {
        this.infoIcon = infoIcon;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Set<Network> getNetworks() {
        if (networks == null) {
            networks = new LinkedHashSet<>();
        }
        return networks;
    }

    public void setNetworks(Set<Network> networks) {
        this.networks = networks;
    }

    public Set<ConfigureTemplateOption> getOptions() {
        if (options == null) {
            options = new LinkedHashSet<>();
        }
        return options;
    }

    public void setOptions(Set<ConfigureTemplateOption> options) {
        this.options = options;
    }

    public boolean isOptionsSortable() {
        return optionsSortable;
    }

    public void setOptionsSortable(boolean optionsSortable) {
        this.optionsSortable = optionsSortable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigureTemplateSetting)) return false;

        ConfigureTemplateSetting that = (ConfigureTemplateSetting) o;

        return getId().equals(that.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
