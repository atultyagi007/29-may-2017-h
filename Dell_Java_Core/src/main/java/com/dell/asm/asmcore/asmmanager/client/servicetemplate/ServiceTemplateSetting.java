
/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import com.dell.asm.asmcore.asmmanager.client.hardware.RAIDConfiguration;
import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

// parameters
@XmlType(name = "ServiceTemplateSetting", propOrder = {
        "id",
        "value",
        "type",
        "displayName",
        "required",
        "requiredAtDeployment",
        "hideFromTemplate",
        "min",
        "max",
        "possibleValues",
        "possibleValuesDisplayName",
        "dependencyTarget",
        "dependencyValue",
        "networks",
        "networkConfiguration",
        "raidConfiguration",
        "options",
        "toolTip",
        "readOnly",
        "generated",
        "group",
        "infoIcon",
        "maxLength",
        "step",
        "optionsSortable"
})
public class ServiceTemplateSetting {
    public enum ServiceTemplateSettingType {
        BOOLEAN,
        STRING,
        PASSWORD,
        INTEGER,
        LIST,  // comma separated list of string
        TEXT,
        NETWORKCONFIGURATION,
        VMVIRTUALDISKCONFIGURATION,
        ENUMERATED,
        RAIDCONFIGURATION,
        BIOSCONFIGURATION,
        RADIO
    }

    private static int MAXLENGTH = 256;

    ServiceTemplateSettingType type = ServiceTemplateSettingType.STRING;
    String displayName;
    String id;
    String value;
    String toolTip;
    boolean required = true; // everything is required unless this is set to false;
    boolean requiredAtDeployment; // user must enter this when deploying the template
    boolean hideFromTemplate; // Not displayed in template builder
    String dependencyTarget; // show this setting when dependencyTarget value() == dependencyValue
    String dependencyValue;
    String group;
    boolean readOnly = false;
    boolean generated = false;
    boolean infoIcon = false;
    List<String> possibleValues = new ArrayList<String>();
    List<String> possibleValuesDisplayName = new ArrayList<String>();
    Integer step = 1;
    Integer maxLength = MAXLENGTH;
    Integer min; // for integer values
    Integer max; // for integer values
    List<Network> networks;
    NetworkConfiguration networkConfiguration;
    RAIDConfiguration raidConfiguration;
    List<ServiceTemplateOption> options = new ArrayList<>();
    boolean optionsSortable = true; // options can be sorted by values (alphabetically by default)

    public boolean isOptionsSortable() {
        return optionsSortable;
    }

    public void setOptionsSortable(boolean optionsSortable) {
        this.optionsSortable = optionsSortable;
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

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public RAIDConfiguration getRaidConfiguration() {
        return raidConfiguration;
    }

    public void setRaidConfiguration(RAIDConfiguration raidConfiguration) {
        this.raidConfiguration = raidConfiguration;
    }

    public ServiceTemplateSetting(String id, String value, ServiceTemplateSettingType type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }

    /**
     * Creates the ServiceTemplateSetting with the given setting info.
     *
     * @param id
     *            the id of the setting @see
     *            com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs).
     * @param value
     *            the value the setting will return.
     * @param displayName
     *            the display name of the value (typically used in the UI)
     * @param type
     *            the type of the setting (boolean, string, etc)
     */
    public ServiceTemplateSetting(String id, String value, String displayName,
                                  ServiceTemplateSettingType type) {
        this(id, value, type);
        this.displayName = displayName;
    }

    /**
     * Creates the ServiceTemplateSetting with the given setting info.
     *
     * @param serviceTemplateSettingDef
     *            contains the setting id and the display name for the setting.
     * @see com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs
     * @param value
     *            the value the setting will return.
     * @param type
     *            the type of the setting (boolean, string, etc)
     */
    public ServiceTemplateSetting(ServiceTemplateSettingDef serviceTemplateSettingDef, String value,
                                  ServiceTemplateSettingType type) {
        this(serviceTemplateSettingDef.getId(), value, serviceTemplateSettingDef.getDisplayName(),
                type);
    }

    public ServiceTemplateSetting() {
        this((String) null, (String) null, null);
    }



    public List<ServiceTemplateOption> getOptions() {
        return options;
    }

    public void setOptions(List<ServiceTemplateOption> options) {
        this.options = options;
    }


    public ServiceTemplateSettingType getType() {
        return type;
    }

    public void setType(ServiceTemplateSettingType type) {
        this.type = type;
    }

    /**
     * Will be removed in favor of options.
     * @return
     */
    @Deprecated
    public List<String> getPossibleValues() {
        return possibleValues;
    }

    @Deprecated
    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        displayName = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String currentValue) {
        value = currentValue;
    }

    /**
     * Will be removed in favor of options.
     * @return
     */
    @Deprecated
    public List<String> getPossibleValuesDisplayName() {
        return possibleValuesDisplayName;
    }

    @Deprecated
    public void setPossibleValuesDisplayName(List<String> possibleValuesDisplayName) {
        this.possibleValuesDisplayName = possibleValuesDisplayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
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

    public boolean isRequiredAtDeployment() {
        return requiredAtDeployment;
    }

    public void setRequiredAtDeployment(boolean requiredAtDeployment) {
        this.requiredAtDeployment = requiredAtDeployment;
    }

    public boolean isHideFromTemplate() {
        return hideFromTemplate;
    }

    public void setHideFromTemplate(boolean hideFromTemplate) {
        this.hideFromTemplate = hideFromTemplate;
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

    public List<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Network> networks) {
        this.networks = networks;
    }

    public NetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    public void setNetworkConfiguration(NetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String tooltip) {
        toolTip = tooltip;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ServiceTemplateSetting) {
            if (((ServiceTemplateSetting) o).getId() == null &&
                    this.getId() != null)
                return false;

            return ((ServiceTemplateSetting) o).getId().equals(this.getId());
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
                .add("type", type)
                .add("displayName", displayName)
                .add("id", id)
                .add("value", value)
                .add("toolTip", toolTip)
                .add("required", required)
                .add("requiredAtDeployment", requiredAtDeployment)
                .add("hideFromTemplate", hideFromTemplate)
                .add("dependencyTarget", dependencyTarget)
                .add("dependencyValue", dependencyValue)
                .add("group", group)
                .add("readOnly", readOnly)
                .add("generated", generated)
                .add("infoIcon", infoIcon)
                .add("possibleValues", possibleValues)
                .add("possibleValuesDisplayName", possibleValuesDisplayName)
                .add("step", step)
                .add("maxLength", maxLength)
                .add("min", min)
                .add("max", max)
                .add("networks", networks)
                .add("networkConfiguration", networkConfiguration)
                .add("raidConfiguration", raidConfiguration)
                .add("options", options)
                .toString();
    }
}
