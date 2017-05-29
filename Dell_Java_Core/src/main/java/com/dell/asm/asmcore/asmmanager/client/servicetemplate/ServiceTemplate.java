/**************************************************************************
 *   Copyright (c) 2013 - 2016 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.servicetemplate;

import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.dell.asm.asmcore.asmmanager.client.firmware.FirmwareRepository;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.user.model.User;

@XmlRootElement(name = "ServiceTemplate")
@XmlType(name = "ServiceTemplate", propOrder = {
        "id",
        "templateName",
        "templateDescription",
        "templateType",
        "templateVersion",
        "templateValid",
        "templateLocked",
        "draft",
        "inConfiguration",
        "wizardPageNumber",
        "createdDate",
        "createdBy",
        "updatedDate",
        "lastDeployedDate",
        "updatedBy",
        "components",
        "category",
        "enableApps",
        "enableCluster",
        "enableServer",
        "enableStorage",
        "enableVMs",
        "allUsersAllowed",
        "assignedUsers",
        "manageFirmware",
        "useDefaultCatalog",
        "firmwareRepository",
        "attachments",
        "configuration"
})
public class ServiceTemplate {

    private String id;
    private String templateName;
    private String templateDescription;
    private String templateType;
    private String templateVersion;
    private ServiceTemplateValid templateValid = ServiceTemplateValid.getDefaultInstance();
    private boolean templateLocked = Boolean.FALSE;
    private boolean isDraft;
    private boolean inConfiguration;
    private Integer wizardPageNumber;
    private GregorianCalendar createdDate;
    private String createdBy;
    private GregorianCalendar updatedDate;
    private GregorianCalendar lastDeployedDate;
    private String updatedBy;
    private boolean manageFirmware;
    private boolean useDefaultCatalog;
    private FirmwareRepository firmwareRepository;
    private List<String> attachments;
    private Set<User> assignedUsers;
    private boolean allUsersAllowed;
    private boolean enableApps;
    private boolean enableVMs;
    private boolean enableCluster;
    private boolean enableServer;
    private boolean enableStorage;
    private String category;
    private List<ServiceTemplateComponent> components = new ArrayList<>();
    private ConfigureTemplate configuration;

    public FirmwareRepository getFirmwareRepository() {
        return firmwareRepository;
    }

    public void setFirmwareRepository(FirmwareRepository firmwareRepository) {
        this.firmwareRepository = firmwareRepository;
    }

    public String getTemplateType() {
        if (templateType == null) {
            templateType = ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_ASM;
        }
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public Set<User> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(Set<User> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public boolean isAllUsersAllowed() {
        return allUsersAllowed;
    }

    public void setAllUsersAllowed(boolean allUsersAllowed) {
        this.allUsersAllowed = allUsersAllowed;
    }

    public boolean isEnableApps() {
        return enableApps;
    }

    public void setEnableApps(boolean enableApps) {
        this.enableApps = enableApps;
    }

    public boolean isEnableVMs() {
        return enableVMs;
    }

    public void setEnableVMs(boolean enableVMs) {
        this.enableVMs = enableVMs;
    }

    public boolean isEnableCluster() {
        return enableCluster;
    }

    public void setEnableCluster(boolean enableCluster) {
        this.enableCluster = enableCluster;
    }

    public boolean isEnableServer() {
        return enableServer;
    }

    public void setEnableServer(boolean enableServer) {
        this.enableServer = enableServer;
    }

    public boolean isEnableStorage() {
        return enableStorage;
    }

    public void setEnableStorage(boolean enableStorage) {
        this.enableStorage = enableStorage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDescription() {
        return templateDescription;
    }

    public void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription;
    }

    public ServiceTemplateValid getTemplateValid() {
        return templateValid;
    }

    public void setTemplateValid(ServiceTemplateValid templateValid) {
        this.templateValid = templateValid;
    }

    public boolean isTemplateLocked() {
        return templateLocked;
    }

    public void setTemplateLocked(boolean templateLocked) {
        this.templateLocked = templateLocked;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean isDraft) {
        this.isDraft = isDraft;
    }

    public boolean isInConfiguration() {
        return inConfiguration;
    }

    public void setInConfiguration(boolean inConfiguration) {
        this.inConfiguration = inConfiguration;
    }

    public Integer getWizardPageNumber() {
        return wizardPageNumber;
    }

    public void setWizardPageNumber(Integer wizardPageNumber) {
        this.wizardPageNumber = wizardPageNumber;
    }

    public GregorianCalendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(GregorianCalendar createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public GregorianCalendar getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(GregorianCalendar updatedDate) {
        this.updatedDate = updatedDate;
    }

    public GregorianCalendar getLastDeployedDate() {
        return lastDeployedDate;
    }

    public void setLastDeployedDate(GregorianCalendar lastDeployedDate) {
        this.lastDeployedDate = lastDeployedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<ServiceTemplateComponent> getComponents() {
        return components;
    }

    public void setComponents(List<ServiceTemplateComponent> components) {
        this.components = components;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public ConfigureTemplate getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ConfigureTemplate configuration) {
        this.configuration = configuration;
    }

    public void removeUnrelatedValues() {
        for (ServiceTemplateComponent component : this.getComponents()) {
            for (ServiceTemplateCategory category : component.getResources()) {

                for (Iterator<ServiceTemplateSetting> iter = category.getParameters().iterator(); iter.hasNext(); ) {
                    ServiceTemplateSetting setting = iter.next();
                    if (StringUtils.isNotEmpty(setting.dependencyTarget)) {

                        ServiceTemplateSetting depTarget = getTemplateSetting(component, setting.dependencyTarget);
                        if (depTarget == null || !StringUtils.contains(setting.dependencyValue, depTarget.getValue())) {
                            // not used in deployment, remove it
                            iter.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * For enums look for $new$ values, if found replace with value taken from extra field used to enter new value.
     * The ID of that extra field will be "$new$" + enum ID.
     * Once done, remove those extra fields.
     */
    public void removeSelectNewOptions() {
        Map<String, String> newVals = new HashMap<>();

        for (ServiceTemplateComponent component : this.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {

                // create a map of new values and IDs where they should go
                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (param.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX)) {
                        String id = param.getId().substring(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX.length());
                        newVals.put(id, param.getValue());
                    }
                }

                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (param.getValue() != null && param.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX)) {
                        String value = newVals.get(param.getId());
                        param.setValue(value);
                        // remove options and reset type to String as a single value
                        param.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                        param.getOptions().clear();
                    }

                    // for settings with dependency on new item update it to actual value
                    if (param.getDependencyValue() != null && param.getDependencyValue().contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX)) {
                        String val = newVals.get(param.getDependencyTarget());
                        if (val != null) {
                            param.setDependencyValue(val);
                        }
                    }
                }

                Iterator<ServiceTemplateSetting> it = resource.getParameters().iterator();
                while (it.hasNext()) {
                    ServiceTemplateSetting setting = it.next();

                    if (setting.getId().startsWith(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX))
                        it.remove();
                }
            }
        }
    }

    /**
     * For params with options
     * if option list has SERVICE_TEMPLATE_CREATE_NEW_PREFIX
     * if value != SERVICE_TEMPLATE_CREATE_NEW_PREFIX
     * which means user selected existing volume/datacenter/etc
     * remove SERVICE_TEMPLATE_CREATE_NEW_PREFIX value from options
     * This will prevent user from confusion in Deployment UI.
     */
    public void removeSelectOption() {
        ServiceTemplateOption selNewOpt =
                new ServiceTemplateOption(null, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX, null, null);

        for (ServiceTemplateComponent component : this.getComponents()) {
            for (ServiceTemplateCategory resource : component.getResources()) {

                for (ServiceTemplateSetting param : resource.getParameters()) {
                    if (param.getValue() != null
                            && param.getOptions() != null
                            && param.getOptions().contains(selNewOpt)
                            && !param.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX)) {

                        List<ServiceTemplateOption> options = param.getOptions();
                        Iterator<ServiceTemplateOption> it = options.iterator();
                        while (it.hasNext()) {
                            ServiceTemplateOption option = it.next();

                            if (option.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX)) {
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Find setting in any template resource. Usable for dependencyTarget/dependencyValue
     * @param component
     * @param sSettingID
     * @return ServiceTemplateSetting
     */
    public ServiceTemplateSetting getTemplateSetting(ServiceTemplateComponent component, String sSettingID) {
        for (ServiceTemplateCategory category : component.getResources()) {
            for (ServiceTemplateSetting setting : category.getParameters()) {
                if (setting.getId().equals(sSettingID))
                    return setting;
            }
        }
        return null;
    }

    /**
     * Find resource in template
     * @param component
     * @param sResID
     * @return ServiceTemplateCategory
     */
    public ServiceTemplateCategory getTemplateResource(ServiceTemplateComponent component, String sResID) {
        for (ServiceTemplateCategory category : component.getResources()) {
            if (category.getId().equals(sResID))
                return category;
        }
        return null;
    }

    public ServiceTemplateSetting getTemplateSetting(ServiceTemplateCategory sResource, String sSettingID) {
        for (ServiceTemplateSetting param : sResource.getParameters()) {
            if (param.getId().compareToIgnoreCase(sSettingID) == 0) {
                return param;
            }
        }
        return null;
    }

    public ServiceTemplateSetting getTemplateSetting(String componentId, String sResourceId, String sSettingID) {
        for (ServiceTemplateComponent component : this.getComponents()) {
            if (component.getId().equals(componentId)) {
                return component.getParameter(sResourceId, sSettingID);
            }
        }
        return null;
    }

    public ServiceTemplateSetting getTemplateSetting(ServiceTemplateComponent.ServiceTemplateComponentType componentType, String sResourceId, String sSettingID) {
        for (ServiceTemplateComponent component : this.getComponents()) {
            if (component.getType() == componentType) {
                // it is possible to have multiple components of the same type
                ServiceTemplateSetting ret = component.getParameter(sResourceId, sSettingID);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    /**
     * Loops through all of the Components, then the Resources, and sorts the Settings options by their value.
     */
    public void sortCategorySettingOptionsByValue() {
        for (ServiceTemplateComponent component : components) {
            for (ServiceTemplateCategory category : component.getResources()) {
                for (ServiceTemplateSetting setting : category.getParameters()) {
                    if (setting.getOptions() != null && setting.getOptions().size() > 0 && setting.isOptionsSortable()) {
                        Collections.sort(setting.getOptions(), new Comparator<ServiceTemplateOption>() {
                            @Override
                            public int compare(ServiceTemplateOption arg0, ServiceTemplateOption arg1) {
                                if (arg0 != null && arg1 != null) {
                                    if (arg0.getValue() != null && arg1.getValue() != null) {
                                        return (arg0.getValue()).compareToIgnoreCase(arg1.getValue());
                                    }
                                }
                                return 0;
                            }
                        });
                    }
                }
            }
        }
    }

    public void fillPossibleValues(ServiceTemplate defaultTemplate) {
        if (defaultTemplate == null) {
            return;
        }

        for (ServiceTemplateComponent component : components) {
            for (ServiceTemplateCategory category : component.getResources()) {
                for (ServiceTemplateSetting param : category.getParameters()) {
                    // In most cases the default template has up-to-date values for the drop-down
                    // options, for example it always pulls the latest list of equallogic storage
                    // volumes from inventory. So here we refresh this template from the latest
                    // copy in the default template. However some options are specific
                    // to the settings in a component and is not stored in the default template
                    // so it has to be skipped here.
                    if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_MANUAL_SELECTION))
                        continue;
                    if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_TEMPLATE_DC))
                        continue;
                    if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_TEMPLATE))
                        continue;
                    if (param.getId().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_CUSTOM_SPEC))
                        continue;

                    ServiceTemplateSetting setting = null;
                    if (ServiceTemplateComponentType.SERVER.equals(component.getType())) {
                        setting = defaultTemplate.getTemplateSetting(component.getComponentID(), category.getId(), param.getId());
                    } else {
                        setting = defaultTemplate.getTemplateSetting(component.getType(), category.getId(), param.getId());
                    }
                    if (setting != null) {
                        param.getOptions().clear();
                        for (ServiceTemplateOption option : setting.getOptions()) {
                            param.getOptions().add(new ServiceTemplateOption(option.getName(), option.getValue(),
                                    option.getDependencyTarget(), option.getDependencyValue()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Find component by ID.
     * @deprecated Deprecated in 8.2 use {@link #findComponentById(String)} instead
     * @param sID The Component Id
     * @return ServiceTemplateComponent
     */
    @Deprecated
    public ServiceTemplateComponent getTemplateComponent(String sID) {
        for (ServiceTemplateComponent comp : this.getComponents()) {
            if (StringUtils.equals(comp.getId(), sID))
                return comp;
        }
        return null;
    }

    public void assignUniqueIDs() {
        for (ServiceTemplateComponent component : components) {
            if (component.getId() == null) {
                component.setId(UUID.randomUUID().toString());
            }
        }
    }

    /**
     * Find the components by the assigned AsmGuid
     * @param guid AsmGuid to search for
     * @return ServiceTemplateComponent
     */
    public ServiceTemplateComponent findComponentByGUID(String guid) {
        for (ServiceTemplateComponent component : this.getComponents()) {
            if (component.getAsmGUID() != null && component.getAsmGUID().equals(guid))
                return component;
        }
        return null;
    }

    /**
     * Returns the component with the given id.. 
     *
     * @param componentId the id the returned component will possess.
     * @return the component with the given id or null if a component with the id cannot be found
     */
    public ServiceTemplateComponent findComponentById(String componentId) {
        for (ServiceTemplateComponent component : this.getComponents()) {
            if (StringUtils.equals(component.getId(), componentId)) {
                return component;
            }
        }
        return null;
    }

    public void removeHiddenValues() {
        for (ServiceTemplateComponent component : this.getComponents()) {
            for (ServiceTemplateCategory category : component.getResources()) {

                for (Iterator<ServiceTemplateSetting> iter = category.getParameters().iterator(); iter.hasNext(); ) {
                    ServiceTemplateSetting setting = iter.next();
                    if (setting.hideFromTemplate) {
                        iter.remove();
                    }
                }
            }
        }
    }

    public boolean isManageFirmware() {
        return manageFirmware;
    }

    public void setManageFirmware(boolean manageFirmware) {
        this.manageFirmware = manageFirmware;
    }

    public boolean isUseDefaultCatalog() {
        return useDefaultCatalog;
    }

    public void setUseDefaultCatalog(boolean useDefaultCatalog) {
        this.useDefaultCatalog = useDefaultCatalog;
    }

    @Override
    public String toString() {
        return "ServiceTemplate{" +
                "id='" + id + '\'' +
                ", templateName='" + templateName + '\'' +
                '}';
    }

    /**
     * Returns a List of ServiceTemplateComponents whose teardown status matches the toBeTornDown.
     * Pass in true if it's desired to get the list of ServiceTemplateComponents that are to be
     * torn down, or false if it's desired to get the list of ServiceTemplateComponent that are 
     * not marked to be torn down.
     */
    public List<ServiceTemplateComponent> getComponentsMarkedForTeardDown(boolean toBeTornDown) {
        ArrayList<ServiceTemplateComponent> componentsToTearDown = new ArrayList<>();
        for (ServiceTemplateComponent component : this.getComponents()) {
            if (component.isTeardown() == toBeTornDown) componentsToTearDown.add(component);
        }
        return componentsToTearDown;
    }

    /**
     * Adds a ServiceTemplateComponent to the components list.
     *
     * @param serviceTemplateComponent the component to add to the components list.
     */
    public void addComponent(ServiceTemplateComponent serviceTemplateComponent) {
        this.getComponents().add(serviceTemplateComponent);
    }

    /**
     * Checks to see if a Component with the given componentName exists in the Component List for this ServiceTemplate.
     *
     * @param componentName the name to check for a matching component.  If this is null the method will always 
     *      return false.
     * @return a boolean indicating whether a component with the given name exists.
     */
    public boolean doesComponentWithNameExist(String componentName) {
        boolean exists = false;

        if (this.getComponents() != null && componentName != null) {
            for (ServiceTemplateComponent serviceTemplateComponent : this.getComponents()) {
                if (componentName.equals(serviceTemplateComponent.getName())) {
                    exists = true;
                    break;
                }
            }
        }

        return exists;
    }

    /**
     * Returns a boolean indicating if a Component with the given category and setting already exists with the 
     * given value.
     *
     * @param categoryId the id of the Component's category whose settings will be searched.
     * @param settingId the id of the setting (parameter) whose value will be searched.
     * @param value the criteria for matching the setting's value against.
     * @return true if a matching value for a Component's category and setting value can be found.  Returns false if 
     *      no match can be found, or if categoryId, settingId, or value are null or if the ServiceTemplate does not 
     *      have any Components as no match can be made.
     */
    public boolean doesComponentWithSettingValueExist(String categoryId, String settingId, String value) {
        boolean exists = false;

        if (categoryId != null && settingId != null && value != null && this.getComponents() != null && this.getComponents().size() > 0) {
            for (ServiceTemplateComponent serviceTemplateComponent : this.getComponents()) {
                String parameterValue = serviceTemplateComponent.getParameterValue(categoryId, settingId);
                if (value.equals(parameterValue)) {
                    exists = true;
                    break;
                }
            }
        }

        return exists;
    }


    /**
     * Removes the component with the given componentId from the component list on this service template and removes 
     * the components relationship to any other component.
     *
     * @param componentId the id of the component to remove.
     */
    public void removeComponentWithId(String componentId) {
        this.getComponents().remove(this.findComponentById(componentId));

        for (ServiceTemplateComponent serviceTemplateComponent : this.components) {
            serviceTemplateComponent.removeRelatedComponent(componentId);
        }
    }

    /**
     * Returns true if the ServiceTemplate contains a ServiceTemplateComponent with the given
     * ServiceTemplateComponentType or false if a ServiceTemplateComponent with a matching ServiceTemplateComponentType 
     * cannot be found.
     *
     * @param serviceTemplateComponentType ServiceTemplateComponentType to search for
     * @return Returns true if the ServiceTemplate contains a ServiceTemplateComponent with the given 
     *      ServiceTemplateComponentType or false if a ServiceTemplateComponent with a matching 
     *      ServiceTemplateComponentType cannot be found.
     */
    public boolean containsServiceTemplateComponentOfType(ServiceTemplateComponentType serviceTemplateComponentType) {
        boolean containsComponentOfType = false;
        for (ServiceTemplateComponent serviceTemplateComponent : this.getComponents()) {
            if (serviceTemplateComponentType.equals(serviceTemplateComponent.getType())) {
                containsComponentOfType = true;
                break;
            }
        }

        return containsComponentOfType;
    }

    /**
     * Returns a list of ServiceTemplateComponents with the given ServiceTemplateComponentType or an empty array if 
     * none are found.  
     *
     * @param serviceTemplateComponentType the type of components that will be returned.
     * @return Returns a list of ServiceTemplateComponents with the given ServiceTemplateComponentType or an empty 
     *      array if none are found.
     */
    public List<ServiceTemplateComponent> getServiceTemplateComponentsByType(ServiceTemplateComponentType serviceTemplateComponentType) {
        ArrayList<ServiceTemplateComponent> serviceTemplateComponents = new ArrayList<>();

        for (ServiceTemplateComponent serviceTemplateComponent : this.getComponents()) {
            if (serviceTemplateComponentType.equals(serviceTemplateComponent.getType())) {
                serviceTemplateComponents.add(serviceTemplateComponent);
            }
        }

        return serviceTemplateComponents;
    }

    /**
     * Returns the ServiceTemplateComponent with the matching refId or null if a match cannot be found.
     *
     * @param refId the matching ServiceTemplateComponent must contain.
     * @return the ServiceTemplateComponent with the matching refId or null if a match cannot be found.
     */
    public ServiceTemplateComponent findComponentByRefId(String refId) {
        for (ServiceTemplateComponent component : this.getComponents()) {
            if (component.getRefId() != null && component.getRefId().equals(refId))
                return component;
        }
        return null;
    }

    /**
     * Searches the giving setting and parameter for the given value and returns the ServiceTemplateComponent that 
     * contains it if a match can be found, or null if no match can be found.
     *
     * @param categoryId of the Category the returned ServiceTemplateComponent must contain.
     * @param settingId of the Setting the returned ServiceTemplateComonent must contain.
     * @param value the Setting value the returned ServiceTemplateComponent must contain.
     * @return a ServiceTemplateComponent with the given Category, Parameter, and matching Parameter value, or null
     *      if no match can be found.
     */
    public ServiceTemplateComponent findComponentBySettingAndValue(String categoryId, String settingId, String value) {
        ServiceTemplateComponent foundServiceTemplateComponent = null;

        if (categoryId != null && settingId != null && value != null && this.getComponents() != null && this.getComponents().size() > 0) {
            for (ServiceTemplateComponent serviceTemplateComponent : this.getComponents()) {
                String parameterValue = serviceTemplateComponent.getParameterValue(categoryId, settingId);
                if (value.equals(parameterValue)) {
                    foundServiceTemplateComponent = serviceTemplateComponent;
                    break;
                }
            }
        }

        return foundServiceTemplateComponent;
    }

    /**
     * Searches the for the given setting and parameter with the given value and returns the ServiceTemplateComponents 
     * that contains it if a match can be found, or an empty List if no match can be found.
     *
     * @param categoryId of the Category the returned ServiceTemplateComponent must contain.
     * @param settingId of the Setting the returned ServiceTemplateComonent must contain.
     * @param value the Setting value the returned ServiceTemplateComponent must contain.
     * @return a List of ServiceTemplateComponents with the given Category, Parameter, and matching Parameter value, or
     *      an empty List if no match can be found.
     */
    public List<ServiceTemplateComponent> findComponentsBySettingAndValue(String categoryId, String settingId, String value) {
        List<ServiceTemplateComponent> serviceTemplateComponents = new ArrayList<>();

        if (categoryId != null && settingId != null && value != null && this.getComponents() != null && this.getComponents().size() > 0) {
            for (ServiceTemplateComponent serviceTemplateComponent : this.getComponents()) {
                String parameterValue = serviceTemplateComponent.getParameterValue(categoryId, settingId);
                if (value.equals(parameterValue)) {
                    serviceTemplateComponents.add(serviceTemplateComponent);

                }
            }
        }

        return serviceTemplateComponents;
    }

    /**
     * Returns the component with the given ipAddress. 
     *
     * @param ip the ipAddress the returned component will possess.
     * @return the component with the given ipAddres or null if a component with the ipAddress cannot be found
     */
    public ServiceTemplateComponent findComponentByIp(String ip) {
        for (ServiceTemplateComponent component : this.getComponents()) {
            if (StringUtils.equals(component.getIP(), ip)) {
                return component;
            }
        }
        return null;
    }

    /**
     * Searches for the given setting and parameters.  Both settings must exist in the same category.  Will return
     * the ServiceTemplateComponent contains both values if a match can be found, or null if no match can be found.
     *
     * @param categoryId of the Category the returned ServiceTemplateComponent must contain.
     * @param settingId of the Setting the returned ServiceTemplateComonent must contain.
     * @param value the Setting value the returned ServiceTemplateComponent must contain.
     * @param settingId2 of the Setting the returned ServiceTemplateComonent must contain.
     * @param value2 the2 Setting value the returned ServiceTemplateComponent must contain.
     * @return a ServiceTemplateComponent with the given Category, Parameter, and matching Paramter value, or null
     *      if no match can be found.
     */
    public ServiceTemplateComponent findComponentBySettingAndValue(String categoryId, String settingId, String value,
                                                                   String settingId2, String value2) {
        ServiceTemplateComponent foundServiceTemplateComponent = null;

        if (categoryId != null && settingId != null && value != null && settingId2 != null && value2 != null && this.getComponents() != null && this.getComponents().size() > 0) {
            for (ServiceTemplateComponent serviceTemplateComponent : this.getComponents()) {
                ServiceTemplateCategory serviceTemplateCategory = serviceTemplateComponent.getTemplateResource(categoryId);
                if (serviceTemplateCategory != null) {
                    ServiceTemplateSetting setting1 = serviceTemplateCategory.getParameter(settingId);
                    ServiceTemplateSetting setting2 = serviceTemplateCategory.getParameter(settingId2);
                    if (setting1 != null && setting2 != null) {
                        if (value.equals(setting1.getValue()) && value2.equals(setting2.getValue())) {
                            foundServiceTemplateComponent = serviceTemplateComponent;
                            break;
                        }
                    }
                }
            }
        }

        return foundServiceTemplateComponent;
    }

    public Map<String, ServiceTemplateComponent> fetchComponentsMap() {
        Map<String, ServiceTemplateComponent> componentMap = new HashMap<>();
        for (ServiceTemplateComponent component : components) {
            componentMap.put(component.getId(), component);
        }
        return componentMap;
    }
    
    /**
     * Returns the operating system ipAddress for the given componentGuid if it exist, or null if it does not exist.
     * 
     * @param componentGuid the guid of the component whos parameters will be searched for the esxi os ipAddress.
     * @return the operating system ipAddress for the given componentGuid if it exist, or null if it does not exist.
     */
    public String findEsxiOperatingSystemIp(String componentGuid) {
        String esxiOsIp = null;
        if (componentGuid != null) {
            ServiceTemplateComponent component = this.findComponentByGUID(componentGuid);
            if (component != null) {
                List<Network> networks = ServiceTemplateClientUtil.findStaticManagementNetworks(component);
                if (networks != null && networks.size() > 0 && networks.get(0).getStaticNetworkConfiguration() != null) {
                    esxiOsIp = networks.get(0).getStaticNetworkConfiguration().getIpAddress();
                }
            }
        }
        
        return esxiOsIp;
    }

    /**
     * Returns a boolean indicating if a Component has a Component type of Cluster.
     * 
     * @return true if a Component has a ServiceTemplateComponentType of Cluster or false otherwise.
     */
    public boolean hasClusterComponentType() {
        if (this.getComponents() != null && !this.getComponents().isEmpty())
            for (ServiceTemplateComponent component : this.getComponents()) {
                if (ServiceTemplateComponentType.CLUSTER.equals(component.getType()))
                    return true;
            }
        return false;
    }
    
}
