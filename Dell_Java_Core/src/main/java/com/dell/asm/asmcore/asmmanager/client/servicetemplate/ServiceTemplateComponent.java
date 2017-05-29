
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import com.dell.asm.asmcore.asmmanager.client.common.xml.adapters.ComplexStringMapAdapter;
import com.google.common.base.Objects;

/**
 * ServiceTemplateComponent can be a bit confusing as it contains a .id field and a .componentId field, which, depending
 * on when you look at it, can have the exact same values.  Part of the confusion is due to the defaultTemplate.xml 
 * containing .id values but not .componentId values and the other confusion is due to moving .id field value to the 
 * .componentId field at template edit time. <br>
 * <br>
 * In the defaultTemplate.xml file it contains a list of components with their default values/settings.  These 
 * components are uniquely identified by the 'id' field.  This 'id' field will contain values along the lines of 
 * 'component-server-1' for example. <br>
 * <br>
 * At template edit time, the UI side code will populate the component .id value with a UUID, and then the UI code will
 * move the previous .id value of the component (remember this came from the default template and will be something 
 * like 'component-server-1') to the .componentId field.  So .id will go from being 'component-server-1' to being 
 * something along the lines of 'ECB8765E-291B-40AF-B344-43C51389CE34' and the .componentId will go from being empty 
 * to being 'component-server-1'. 
 */
@XmlRootElement(name = "component")
@XmlType(name = "ServiceTemplateComponent", propOrder = {
        "id",
        "componentID",
        "componentValid",
        "puppetCertName",
        "name",
        "type",
        "subType",
        "teardown",
        "helpText",
        "IP",
        "configFile",
        "serialNumber",
        "asmGUID",
        "relatedComponents",
        "associatedComponents",
        "resources",
        "refId",
        "cloned",
        "clonedFromId",
        "manageFirmware",
        "brownfield"
})
public class ServiceTemplateComponent {
    public enum ServiceTemplateComponentType {
        CONFIGURATION("configuration"),
        TOR("switch"),
        STORAGE("storage"),
        SERVER("server"),
        CLUSTER("cluster"),
        VIRTUALMACHINE("vm"),
        SERVICE("application"),
        TEST("test");

        private String _label;

        private ServiceTemplateComponentType(String label) {
            _label = label;
        }

        public String getLabel() {
            return _label;
        }

    }

    public enum ServiceTemplateComponentSubType {
        CLASS("class"),
        TYPE("type"),
        HYPERVISOR("HYPERVISOR");

        private String _label;

        private ServiceTemplateComponentSubType(String label) {
            _label = label;
        }

        public String getLabel() {
            return _label;
        }
    }

    // Member Variables
    private String id;
    private String ComponentID;
    private ServiceTemplateValid componentValid = ServiceTemplateValid.getDefaultInstance();
    private String Name;
    private String HelpText;
    private String clonedFromId;
    private boolean teardown;
    private ServiceTemplateComponentType type;
    private ServiceTemplateComponentSubType subType;
    private Map<String, String> relatedComponents = new HashMap<String,String>(); // pointer to IDs of one or more entities in service template that it is pointing to.
    private Map<String, Map<String, String>> associatedComponents = new HashMap<String,Map<String,String>>();
    private List<ServiceTemplateCategory> resources = new ArrayList<>();
    private boolean brownfield = false;
    private String puppetCertName;
    // These are set once the component is mapped to a physical resource i.e. server, switch, VM, Cluster etc
    private String IP;
    private String SerialNumber;
    private String AsmGUID; // guid in the table device_inventory
    private Boolean cloned = Boolean.FALSE;
    private String ConfigFile;
    private boolean manageFirmware;
    
    
    /**
     * Note this refId does NOT map to the refId in the device inventory table for this component.  Instead this refId 
     * reflects the refId of the "Reference Server" that the component received it's settings from.  If you're looking 
     * for the refId from device_inventory table, then that value will be found in the getAsmGUID() method.
     * @return refId of the "Reference Server" this component received it's settings from... 
     */
    public String getRefId() {
        return refId;
    }

    /**
     * Note this refId does NOT map to the refId in the device inventory table for this component.  Instead this refId 
     * reflects the refId of the "Reference Server" that the component received it's settings from.  If you're looking 
     * for the refId from device_inventory table, then that value will be found in the getAsmGUID() method.
     */
    public void setRefId(String refId) {
        this.refId = refId;
    }

    String refId;

    public String getPuppetCertName() {
        return puppetCertName;
    }

    public void setPuppetCertName(String puppetCertName) {
        this.puppetCertName = puppetCertName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getComponentID() {
        return ComponentID;
    }

    public void setComponentID(String componentID) {
        ComponentID = componentID;
    }

    public ServiceTemplateValid getComponentValid() {
        return componentValid;
    }

    public void setComponentValid(ServiceTemplateValid componentValid) {
        this.componentValid = componentValid;
    }

    public ServiceTemplateComponentType getType() {
        return type;
    }

    public void setType(ServiceTemplateComponentType type) {
        this.type = type;
    }

    public ServiceTemplateComponentSubType getSubType() {
        return subType;
    }

    public void setSubType(ServiceTemplateComponentSubType subType) {
        this.subType = subType;
    }

    /**
     * Get Map of related component ids and names
     * @deprecated As of 8.2, use {@link #getAssociatedComponents()} instead
     * @return
     */
    @Deprecated
    public Map<String, String> getRelatedComponents() {
        if (relatedComponents == null) {
            relatedComponents = new HashMap<>();
        }
        return relatedComponents;
    }

    public void setRelatedComponents(Map<String, String> relatedComponents) {
        this.relatedComponents = relatedComponents;
    }

    /**
     * Returns the map of associated components.  The key is the component id and the value is a
     * simple hashmap of properties associated with the relationship.  Example is install order of
     * application components on a server component.
     * NOTE: The values are a HashMap instead of the Map interface due to marshalling requirements of a
     * default constructor.
     * @return associated components
     */
    @XmlJavaTypeAdapter(ComplexStringMapAdapter.class)
    public Map<String, Map<String, String>> getAssociatedComponents() {
        if (associatedComponents.isEmpty()) {
            if (!relatedComponents.isEmpty()) {
                for (Map.Entry<String,String> entry : relatedComponents.entrySet()) {
                    addAssociatedComponentName(entry.getKey(),entry.getValue());
                }
            }
        }
        return associatedComponents;
    }

    public void setAssociatedComponents(Map<String, Map<String, String>> associatedComponents) {
        this.associatedComponents = associatedComponents;
    }

    /**
     * Adds the component and its name to the map of associated components.
     * @param componentID
     * @param componentName
     */
    public void addAssociatedComponentName(String componentID, String componentName) {

        // This will need to be removed with deprecated getRelatedComponents() removal
        if (getRelatedComponents().get(componentID) == null) {
            getRelatedComponents().put(componentID, componentName);
        }
        Map<String,String> associatedProperties = associatedComponents.get(componentID);
        if (associatedProperties == null) {
            associatedProperties = new HashMap<>();
            associatedComponents.put(componentID,associatedProperties);
        }
        associatedProperties.put(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPONENT_NAME,componentName);
    }

    public void removeAssociatedComponent(String componentID) {
        // This will need to be removed with deprecated getRelatedComponents() removal
        getRelatedComponents().remove(componentID);
        associatedComponents.remove(componentID);
    }

    public void removeAllAssociatedComponents(Set<String> componentIds) {
        // This will need to be removed with deprecated getRelatedComponents() removal
        getRelatedComponents().keySet().removeAll(componentIds);
        associatedComponents.keySet().removeAll(componentIds);
    }

    public String getHelpText() {
        return HelpText;
    }

    public void setHelpText(String helpText) {
        HelpText = helpText;
    }

    public List<ServiceTemplateCategory> getResources() {
        return resources;
    }

    public void setResources(List<ServiceTemplateCategory> categories) {
        this.resources = categories;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String iP) {
        IP = iP;
    }

    public String getConfigFile() {
        return ConfigFile;
    }

    public void setConfigFile(String configFile) {
        ConfigFile = configFile;
    }

    /**
     * While this is called the AsmGUID, this value actually maps to and reflects the value of the refId from the
     * asm_manager's device_inventory tabel's 'refid' field.
     */
    public String getAsmGUID() {
        return AsmGUID;
    }

    public void setAsmGUID(String asmGUID) {
        AsmGUID = asmGUID;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = serialNumber;
    }

    public String getClonedFromId() {
        return clonedFromId;
    }

    public void setClonedFromId(String clonedFromId) {
        this.clonedFromId = clonedFromId;
    }

    public boolean isTeardown() {
        return teardown;
    }

    public void setTeardown(boolean teardown) {
        this.teardown = teardown;
    }

    public Boolean getCloned() {
        return cloned;
    }

    public void setCloned(Boolean cloned) {
        this.cloned = cloned;
    }

    public boolean hasHardDiskBoot() {
        ServiceTemplateSetting targetBoot = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);

        return (targetBoot != null) && (targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD));
    }

    public boolean hasDiskBoot() {
        ServiceTemplateSetting targetBoot = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);

        return (targetBoot != null) && (targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_HD) ||
                targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD) ||
                targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID_VSAN) ||
                targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_SD_RAID) ||
                targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_AHCI_VSAN));
    }

    public boolean hasSanBoot() {
        ServiceTemplateSetting targetBoot = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);

        return (targetBoot != null) && (targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI) ||
                targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_FC));
    }

    public boolean hasSanISCSIBoot() {
        ServiceTemplateSetting targetBoot = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);

        return (targetBoot != null) && (targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ISCSI));
    }

    public boolean hasNoneBoot() {
        ServiceTemplateSetting targetBoot = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_ID);
        return (targetBoot == null) ||
                targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE) ||
                targetBoot.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_TARGET_BOOTDEVICE_NONE_WITH_RAID);
    }

    public boolean hasHyperV() {
        boolean result = false;
        if (ServiceTemplateComponentType.SERVER.equals(getType())) {
            ServiceTemplateSetting hypervInstall = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
            result = hasDiskBoot() && (hypervInstall != null) && Boolean.parseBoolean(hypervInstall.getValue().toLowerCase());
        } else if (ServiceTemplateComponentType.CLUSTER.equals(getType())) {
            result = (getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMP_ID) != null);
        }
        return result;
    }

    public boolean hasESXInstall() {
        boolean result = false;
        if (ServiceTemplateComponentType.SERVER.equals(getType())) {
            ServiceTemplateSetting razorInstall = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
            return (razorInstall != null) && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE.equals(razorInstall.getValue());
        }
        return result;
    }

    public boolean hasESX(Map<String, String> reposToTaskMap) {
        boolean result = false;

        if (ServiceTemplateComponentType.SERVER.equals(getType())) {
            String task = null;
            ServiceTemplateSetting osImageId = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
            if (osImageId != null && reposToTaskMap != null) {
                task = reposToTaskMap.get(osImageId.getValue());
            }
            result = hasDiskBoot() && (task != null) && task.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE);
        } else if (ServiceTemplateComponentType.CLUSTER.equals(getType())) {
            result = (getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID) != null);
        }
        return result;
    }

    public void isHypervisor(Map<String, String> reposToTaskMap) {
        if (ServiceTemplateComponentType.SERVER.equals(getType())) {
            boolean result = false;
            final boolean diskBoot = hasDiskBoot();
            ServiceTemplateSetting hypervInstall = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_HV_INSTALL);
            if (hypervInstall != null) {
                result = diskBoot && Boolean.parseBoolean(hypervInstall.getValue().toLowerCase());
            }
            if (!result) {
                String task = null;
                ServiceTemplateSetting osImageId = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
                if (osImageId != null && reposToTaskMap != null) {
                    task = reposToTaskMap.get(osImageId.getValue());
                }
                if (task != null) {
                    result = diskBoot && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE.equals(task);
                }
            }
            if (result) {
                this.setSubType(ServiceTemplateComponentSubType.HYPERVISOR);
            } else {
                this.setSubType(null);
            }
        }
    }

    public boolean hasBareMetal(Map<String, String> repoToTaskMap) {
        ServiceTemplateSetting osRepo = getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_IMAGE_ID);
        return (osRepo == null) || hasSanBoot() || (!hasHyperV() && !hasESX(repoToTaskMap) && !hasNoneBoot());
    }

    /**
     * Find setting in any resource.
     *
     * @deprecated In cases where a setting id exists in multiple ServiceTemplateCategory objects it
     * is unpredictable which will be returned. Use {@link #getParameter(String, String)} instead.
     *
     * @param sSettingID The setting id
     * @return The ServiceTemplateSetting with the setting id
     */
    @Deprecated
    public ServiceTemplateSetting getTemplateSetting(String sSettingID) {
        for (ServiceTemplateCategory category : getResources()) {
            for (ServiceTemplateSetting setting : category.getParameters()) {
                if (setting.getId().equals(sSettingID))
                    return setting;
            }
        }
        return null;
    }

    /**
     * Find parameter in specified resource.
     *
     * @param resourceId The resource id
     * @param parameterId The parameter id
     *
     * @return The matching parameter, or null if none.
     */
    public ServiceTemplateSetting getParameter(String resourceId, String parameterId) {
        ServiceTemplateCategory resource = getTemplateResource(resourceId);
        if (resource != null) {
            for (ServiceTemplateSetting param : resource.getParameters()) {
                if (parameterId.equals(param.getId())) {
                    return param;
                }
            }
        }
        return null;
    }

    /**
     * Find parameter value in specified resource.
     *
     * @param resourceId The resource id
     * @param parameterId The parameter id
     *
     * @return The matching parameter value, or null if none.
     */
    public String getParameterValue(String resourceId, String parameterId) {
        ServiceTemplateSetting parameter = getParameter(resourceId, parameterId);
        return parameter == null ? null : parameter.getValue();
    }

    /**
     * Find resource in template
     * @param sResID
     * @return
     */
    public ServiceTemplateCategory getTemplateResource(String sResID) {
        for (ServiceTemplateCategory category : getResources()) {
            if (category.getId().equals(sResID))
                return category;
        }
        return null;
    }

    public boolean checkMinimalServerComponent() {
        if (getType() != ServiceTemplateComponentType.SERVER) return false;

        for (ServiceTemplateCategory category : getResources()) {
            if (category.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_IDRAC_RESOURCE))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("ComponentID", ComponentID)
                .add("componentValid", componentValid)
                .add("puppetCertName", puppetCertName)
                .add("Name", Name)
                .add("type", type)
                .add("teardown", teardown)
                .add("HelpText", HelpText)
                .add("IP", IP)
                .add("ConfigFile", ConfigFile)
                .add("SerialNumber", SerialNumber)
                .add("asmGUID", AsmGUID)
                .add("relatedComponents", relatedComponents)
                .add("resources", resources)
                .add("refId", refId)
                .add("cloned", cloned)
                .add("clonedFromId", clonedFromId)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceTemplateComponent that = (ServiceTemplateComponent) o;

        if (AsmGUID != null ? !AsmGUID.equals(that.AsmGUID) : that.AsmGUID != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (puppetCertName != null ? !puppetCertName.equals(that.puppetCertName) : that.puppetCertName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (puppetCertName != null ? puppetCertName.hashCode() : 0);
        result = 31 * result + (AsmGUID != null ? AsmGUID.hashCode() : 0);
        return result;
    }

    /**
     * Returns a boolean indicating if the categories param's value matches the given value. 
     *
     * @param categoryId The resource id to search for
     * @param parameterId The parameter id to search for
     *
     * @return a boolean indicating if the categories param's value matches the given value.  
     */
    public boolean doesParameterValueMatch(String categoryId, String parameterId, String value) {
        return StringUtils.equals(getParameterValue(categoryId, parameterId), value);
    }

    /**
     * Replaces the the given component relationship with a new one.
     *
     * @param componentId the componentId of the relationship to replace.
     * @param newCompId the new componentId of the relationship to replace.
     * @param newCompName the new name of the related component to replace.
     */
    public void replaceRelatedComponent(String componentId, String newCompId, String newCompName) {
        this.removeAssociatedComponent(componentId);
        this.addAssociatedComponentName(newCompId, newCompName);
    }
    
    /**
     * Returns a boolean indicating if this component was created through brownfield discovery.
     * 
     * @return a boolean indicating if this component was created through brownfield discovery.
     */
    public boolean isBrownfield() {
        return brownfield;
    }
    
    /**
     * Sets whether this component was created through brownfield discovery.
     * 
     * @param brownfield a boolean indicating if this component was creategd through brownfield discovery.
     */
    public void setBrownfield(boolean brownfield) {
        this.brownfield = brownfield;
    }
    
    /**
     * Removes the related component with the given component id. 
     * 
     * @param componentId the id of the component to remove.
     */
    public void removeRelatedComponent(String componentId) {
        this.getRelatedComponents().remove(componentId);
    }

    public boolean isManageFirmware() {
        return manageFirmware;
    }

    public void setManageFirmware(boolean manageFirmware) {
        this.manageFirmware = manageFirmware;
    }

    /**
     * Returns boolean value to reflect whether the OS image is linux or not
     * 
     * @return true if the component is SERVER type and its OS image type is Linux-based system
     */
    public boolean hasLinuxOS() {
        if (!ServiceTemplateComponentType.SERVER.equals(this.getType())) {
            return false;
        }
        ServiceTemplateSetting osImageType = this.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
        if (osImageType == null || osImageType.getValue() == null) {
            return false;
        }
        String osImageTypeValue = osImageType.getValue().toLowerCase();
        return osImageTypeValue.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT6_VALUE)
                || osImageTypeValue.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_REDHAT7_VALUE)
                || osImageTypeValue.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE11_VALUE) 
                || osImageTypeValue.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_SUSE12_VALUE);
    }

    /**
     * Returns boolean value to reflect whether the OS image is Windows or not
     *
     * @note This does include HyperV
     *
     * @return true if the component is SERVER type and it's OS image type is Windows
     */
    public boolean hasWindowsOS() {
        if (!ServiceTemplateComponentType.SERVER.equals(this.getType())) {
            return false;
        }
        ServiceTemplateSetting osImageType = this.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
        if (osImageType == null || osImageType.getValue() == null) {
            return false;
        }
        String osImageTypeValue = osImageType.getValue().toLowerCase();
        return osImageTypeValue.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE)
                || osImageTypeValue.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE)
                || osImageTypeValue.equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERV_VALUE);
    }

    /**
     * This is a device configuration template and component is a Chassis.
     * @return
     */
    public boolean hasCMCConfigJob() {
        for (ServiceTemplateCategory category: this.getResources()) {
            if (category.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_INITIAL_CONFIG_ID)) {
                return true;
            }
        }
        return false;
    }

}
