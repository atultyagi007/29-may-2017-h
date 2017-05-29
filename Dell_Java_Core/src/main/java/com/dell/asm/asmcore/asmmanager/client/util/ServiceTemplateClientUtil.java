/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class ServiceTemplateClientUtil {

    private static final Logger LOGGER = Logger.getLogger(ServiceTemplateClientUtil.class);
    /**
     * max number of port groups with the same network name i.e. ISCSI 1, ISCSI 2 etc
     */
    private static final int MAX_PG_NUM = 2;

    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();


    private static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector ai = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(ai);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Utility method that adds the upgraded service template components to the given service template
     *
     * @param serviceTemplate
     * @param upgradedComponents
     */
    public static void updateServiceTemplateWithUpgradedSettings(final ServiceTemplate serviceTemplate,
                                                                 final List<ServiceTemplateComponent> upgradedComponents) {

        if (serviceTemplate != null && CollectionUtils.isNotEmpty(upgradedComponents)) {
            final Map<String, ParameterEntry> parameterMap = generateServiceTemplateParameterMap(serviceTemplate);

            // override/update components with updates
            for (final ServiceTemplateComponent upgradedComponent : upgradedComponents) {
                for (final ServiceTemplateCategory upgradedResource : upgradedComponent.getResources()) {
                    for (final ServiceTemplateSetting upgradedParameter : upgradedResource.getParameters()) {
                        final String parameterMapKey =
                                generateParameterMapKey(upgradedComponent, upgradedResource, upgradedParameter);
                        if (parameterMap.containsKey(parameterMapKey)) {
                            final ParameterEntry parameterEntry = parameterMap.get(parameterMapKey);
                            parameterEntry.getParameter().setValue(upgradedParameter.getValue());
                        } else {
                            // this is a new parameter ??? LOG error
                            LOGGER.error("Parameter " + upgradedParameter.getId() + " cannot be found for resource " +
                                    upgradedResource.getId() + " on component " + upgradedComponent.getId());
                        }
                    }
                }
            }
        }
    }

    /**
     * Utility method to get a service template with only the invalid components that need to be upgraded
     *
     * @param serviceTemplate
     */
    public static void filterServiceTemplateForOnlyUpgradableSettings(final ServiceTemplate serviceTemplate) {
        if (serviceTemplate != null) {
            if (CollectionUtils.isEmpty(serviceTemplate.getComponents())) {
                // there are no components so there is nothing to upgrade 
                LOGGER.warn("Service template " + serviceTemplate.getId() +
                        " does not have any components to filter for upgradable parameters!");
                return;
            }

            Map<String, Map<String, ParameterEntry>> settingsMap = generateServiceTemplateSettingsMap(serviceTemplate);
            // loop through template and add upgradable parameters to map
            final Map<String, ParameterEntry> upgradableParameterMap = new HashMap<String, ParameterEntry>();
            final List<String> importantDependencyTargets = new ArrayList();

            if (!serviceTemplate.getTemplateValid().isValid()) {
                for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                    if (!component.getComponentValid().isValid()) {
                        upgradeServiceTemplateComponents(upgradableParameterMap, settingsMap, component);
                    }
                }

                // create a map of dependency targets for options in all upgradable settings in template
                for (ParameterEntry entry : upgradableParameterMap.values()) {
                    if (entry.getParameter().getOptions() != null) {
                        for (ServiceTemplateOption option : entry.getParameter().getOptions()) {
                            if (StringUtils.isNotEmpty(option.getDependencyTarget()) &&
                                    !importantDependencyTargets.contains(option.getDependencyTarget())) {
                                importantDependencyTargets.add(option.getDependencyTarget());
                            }
                        }
                    }
                }

            }

            // now that the upgradable map has been generated loop through the templates parameters and
            // filter out parameters that are not upgradable
            for (final Iterator<ServiceTemplateComponent> componentIter = serviceTemplate.getComponents().iterator();
                 componentIter.hasNext(); ) {
                final ServiceTemplateComponent component = componentIter.next();
                if (CollectionUtils.isEmpty(component.getResources())) {
                    LOGGER.warn("Service template component " + component.getId() +
                            " does not have any resources to filter for upgradable parameters!");
                    continue;
                }
                for (final Iterator<ServiceTemplateCategory> resourceIter = component.getResources().iterator();
                     resourceIter.hasNext(); ) {
                    final ServiceTemplateCategory resource = resourceIter.next();
                    if (CollectionUtils.isEmpty(resource.getParameters())) {
                        LOGGER.warn("Service template resource " + resource.getId() +
                                " does not have any parameters to filter for upgradable parameters!");
                        continue;
                    }
                    for (final Iterator<ServiceTemplateSetting> parameterIter = resource.getParameters().iterator();
                         parameterIter.hasNext(); ) {
                        final ServiceTemplateSetting parameter = parameterIter.next();
                        if (!isImportantParameter(upgradableParameterMap, component, resource, parameter, importantDependencyTargets)) {
                            parameterIter.remove();
                        }
                    }
                    // if there are no parameters left in the resource then remove
                    if (CollectionUtils.isEmpty(resource.getParameters())) {
                        resourceIter.remove();
                    }
                }
                // if there are no resources left in the component then remove
                if (CollectionUtils.isEmpty(component.getResources())) {
                    componentIter.remove();
                }
            }
        }
    }

    /**
     * Recursive Method for determing upgradable settings and finding all parent and child dependencies
     * along with the dependencies of the dependents.
     *
     * @param upgradableParameterMap
     * @param settingsMap
     * @param component
     */
    private static void upgradeServiceTemplateComponents(Map<String, ParameterEntry> upgradableParameterMap,
                                                         Map<String, Map<String, ParameterEntry>> settingsMap,
                                                         ServiceTemplateComponent component) {
        if (!component.getComponentValid().isValid()) {
            for (final ServiceTemplateCategory resource : component.getResources()) {
                for (final ServiceTemplateSetting parameter : resource.getParameters()) {
                    String parameterKey = generateParameterMapKey(component, resource, parameter);
                    //If already in the upgradable map ignore
                    if (upgradableParameterMap.get(parameterKey) == null) {
                        //determine if child of a setting already added to the upgradable map
                        if (childDependencyForUpgradableParameter(settingsMap, component, parameter, upgradableParameterMap)) {
                            LOGGER.info("Found child dependency parameter of parameter that needs to be upgraded " + parameter);
                            updateUpgradableMapParameters(settingsMap, upgradableParameterMap, component, parameterKey, findSettingDependency(settingsMap, component, parameter.getId()));
                        } else if (isVisibleRequiredParameter(parameter)
                                && requiredDependenciesSatisfied(settingsMap, component, parameter)
                                && isMissingPasswordOrBlank(parameter)) {
                            LOGGER.info("Found invalid parameter that needs to be upgraded " + parameter);
                            updateUpgradableMapParameters(settingsMap, upgradableParameterMap, component, parameterKey, findSettingDependency(settingsMap, component, parameter.getId()));
                        }
                    }
                }
            }
        }
    }

    /**
     * For the passed parameter check its options - if an option has a dependency target, add this D.T. to a map.
     * Also if a parameter has dependency target itself - add it to a map.
     *
     * @param settingsMap
     * @param upgradableParameterMap
     * @param component
     * @param parameterKey
     * @param parameterEntry
     */
    private static void updateUpgradableMapParameters(Map<String, Map<String, ParameterEntry>> settingsMap,
                                                      Map<String, ParameterEntry> upgradableParameterMap,
                                                      ServiceTemplateComponent component,
                                                      String parameterKey,
                                                      ParameterEntry parameterEntry) {

        if (upgradableParameterMap.get(parameterKey) == null) {
            // check if the upgradable setting has an option with a dependency target
            for (ServiceTemplateOption option : parameterEntry.getParameter().getOptions()) {
                if (StringUtils.isNotBlank(option.getDependencyTarget())) {
                    LOGGER.info("Found option dependency parameter " + option.getDependencyTarget() + " of parameter that needs to be upgraded " + parameterEntry.getParameter().getId());
                    ParameterEntry optionParameterEntry = findSettingDependency(settingsMap, component, option.getDependencyTarget());
                    String optionTargetKey = generateParameterMapKey(optionParameterEntry.getComponent(), optionParameterEntry.getResource(), optionParameterEntry.getParameter());
                    if (upgradableParameterMap.get(optionTargetKey) == null) {
                        upgradableParameterMap.put(optionTargetKey, optionParameterEntry);
                        upgradeServiceTemplateComponents(upgradableParameterMap, settingsMap, component);
                    }
                }
            }

            if (StringUtils.isNotBlank(parameterEntry.getParameter().getDependencyTarget())) {
                ParameterEntry dependentEntry = findSettingDependency(settingsMap, component, parameterEntry.getParameter().getDependencyTarget());
                if (dependentEntry != null) {
                    String componentDependencyTargetKey = generateParameterMapKey(dependentEntry.getComponent(), dependentEntry.getResource(), dependentEntry.getParameter());
                    updateUpgradableMapParameters(settingsMap, upgradableParameterMap, component, componentDependencyTargetKey, dependentEntry);
                }
            }

            upgradableParameterMap.put(parameterKey, parameterEntry);
            upgradeServiceTemplateComponents(upgradableParameterMap, settingsMap, component);
        }
    }

    /**
     * Adds a setting to the settingsMap
     *
     * @param settingsMap
     * @param component
     * @param resource
     * @param parameter
     */
    private static void updateSettingsMap(Map<String, Map<String, ParameterEntry>> settingsMap,
                                          ServiceTemplateComponent component,
                                          ServiceTemplateCategory resource,
                                          ServiceTemplateSetting parameter) {
        String settingKey = generateSimpleKey(component.getId(), parameter.getId());
        Map<String, ParameterEntry> settingEntry = settingsMap.get(settingKey);
        if (settingEntry == null) {
            settingEntry = new HashMap<String, ParameterEntry>();
            settingsMap.put(settingKey, settingEntry);
        }
        settingEntry.put(generateParameterMapKey(component, resource, parameter), new ParameterEntry(component, resource, parameter));
    }

    /**
     * Searches the settingsMap for an entry and then returns the ParameterEntry for the setting id
     *
     * @param settingsMap
     * @param component
     * @param parameterId
     * @return
     */
    private static ParameterEntry findSettingDependency(final Map<String, Map<String, ParameterEntry>> settingsMap,
                                                        ServiceTemplateComponent component,
                                                        String parameterId) {
        ParameterEntry parameterEntry = null;
        String settingsKey = generateSimpleKey(component.getId(), parameterId);
        Map<String, ParameterEntry> settingsEntry = settingsMap.get(settingsKey);
        if (settingsEntry != null) {
            //Should only be one entry in the list.
            for (Map.Entry<String, ParameterEntry> entry : settingsEntry.entrySet()) {
                parameterEntry = entry.getValue();
            }
        }
        return parameterEntry;
    }

    /**
     * Check to see if the parameter should be kept
     *
     * @param upgradableParameterMap
     * @param component
     * @param resource
     * @param parameter
     * @return
     */
    private static boolean isImportantParameter(Map<String, ParameterEntry> upgradableParameterMap,
                                                ServiceTemplateComponent component,
                                                ServiceTemplateCategory resource,
                                                ServiceTemplateSetting parameter,
                                                List<String> importantDependencyTargets) {
        String parameterKey = generateParameterMapKey(component, resource, parameter);
        if (upgradableParameterMap.containsKey(parameterKey))
            return true;

        // check options for each parameter. They might depend on this parameter. We can't catch it earlier.
        if (importantDependencyTargets.contains(parameter.getId()))
            return true;

        return false;
    }

    public static boolean containsUpgradableSettings(final ServiceTemplate serviceTemplate) {
        if (serviceTemplate != null) {
            if (CollectionUtils.isEmpty(serviceTemplate.getComponents())) {
                // there are no components so there is nothing to upgrade
                return false;
            }

            Map<String, Map<String, ParameterEntry>> settingsMap = generateServiceTemplateSettingsMap(serviceTemplate);
            // loop through template and find upgradable parameters
            if (!serviceTemplate.getTemplateValid().isValid()) {
                for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
                    if (!component.getComponentValid().isValid()) {
                        for (final ServiceTemplateCategory resource : component.getResources()) {
                            for (final ServiceTemplateSetting parameter : resource.getParameters()) {
                                if (isVisibleRequiredParameter(parameter)
                                        && requiredDependenciesSatisfied(settingsMap, component, parameter)
                                        && isMissingPasswordOrBlank(parameter)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    private static boolean isVisibleRequiredParameter(final ServiceTemplateSetting parameter) {
        if (parameter != null) {
            return !parameter.isHideFromTemplate() && parameter.isRequired();
        }
        return Boolean.FALSE;
    }

    private static boolean requiredDependenciesSatisfied(final Map<String, Map<String, ParameterEntry>> settingsMap,
                                                         final ServiceTemplateComponent component,
                                                         final ServiceTemplateSetting parameter) {
        if (StringUtils.isBlank(parameter.getDependencyTarget())) {
            // there are no further dependencies
            return Boolean.TRUE;
        } else {
            final String settingsKey = generateSimpleKey(component.getId(), parameter.getId());
            Map<String, ParameterEntry> settingsEntry = settingsMap.get(settingsKey);
            if (settingsEntry != null) {
                final ParameterEntry dependencyTarget = findSettingDependency(settingsMap, component, parameter.getDependencyTarget());
                if (dependencyTarget != null) {
                    final String dependencyValue = dependencyTarget.getParameter().getValue();
                    for (final String value : StringUtils.split(parameter.getDependencyValue(), ",")) {
                        if (StringUtils.equals(dependencyValue, value)) {
                            LOGGER.info("Found required dependency " + dependencyTarget.getParameter().getId() + ": " + value);
                            // NOTE[fcarta] Its understood there is a possibility for circular dependencies. In the
                            // event an error like this should occur we are ok with a StackOverflowException being
                            // thrown. This would indicate a programmatic error in a default template. Currently users dont
                            // have the ability to modify default templates.
                            return requiredDependenciesSatisfied(settingsMap, component, dependencyTarget.getParameter());
                        }
                    }
                }
            }
        }
        // dependency not found or dependency value match not made
        return Boolean.FALSE;
    }

    /**
     * Check to see if dependency is in the upgradable map
     *
     * @param component
     * @param parameter
     * @param upgradableParameterMap
     * @return
     */
    private static boolean childDependencyForUpgradableParameter(Map<String, Map<String, ParameterEntry>> settingsMap,
                                                                 ServiceTemplateComponent component,
                                                                 ServiceTemplateSetting parameter,
                                                                 Map<String, ParameterEntry> upgradableParameterMap) {
        if (StringUtils.isBlank(parameter.getDependencyTarget())) {
            // there are no further dependencies
            return Boolean.FALSE;
        } else {
            final ParameterEntry dependencyEntry = findSettingDependency(settingsMap, component, parameter.getDependencyTarget());
            final String parentDependencyKey = generateParameterMapKey(dependencyEntry.getComponent(), dependencyEntry.getResource(), dependencyEntry.getParameter());
            if (upgradableParameterMap.get(parentDependencyKey) != null) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private static boolean isMissingPasswordOrBlank(final ServiceTemplateSetting parameter) {
        if (parameter != null) {
            if (ServiceTemplateSettingType.PASSWORD.equals(parameter.getType())) {
                // if this is a password field determine if its is missing (when it is "") or just removed (when null)
                return parameter.getValue() != null && StringUtils.isBlank(parameter.getValue());
            } else {
                // not a password field
                return StringUtils.isBlank(parameter.getValue());
            }
        }
        return Boolean.FALSE;
    }

    private static Map<String, ParameterEntry> generateServiceTemplateParameterMap(
            final ServiceTemplate serviceTemplate) {
        final Map<String, ParameterEntry> parameterMap = new HashMap<String, ParameterEntry>();
        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            for (final ServiceTemplateCategory resource : component.getResources()) {
                for (final ServiceTemplateSetting parameter : resource.getParameters()) {
                    parameterMap.put(
                            generateParameterMapKey(component, resource, parameter),
                            new ParameterEntry(component, resource, parameter));
                }
            }
        }
        return parameterMap;
    }

    /**
     * Loops through all components, resources, and parameters building a reference based on component and parameter
     *
     * @param serviceTemplate
     * @return
     */
    private static Map<String, Map<String, ParameterEntry>> generateServiceTemplateSettingsMap(final ServiceTemplate serviceTemplate) {
        final Map<String, Map<String, ParameterEntry>> settingsMap = new HashMap<String, Map<String, ParameterEntry>>();
        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            for (final ServiceTemplateCategory resource : component.getResources()) {
                for (final ServiceTemplateSetting parameter : resource.getParameters()) {
                    updateSettingsMap(settingsMap, component, resource, parameter);
                }
            }
        }
        return settingsMap;
    }

    private static String generateParameterMapKey(final ServiceTemplateComponent component,
                                                  final ServiceTemplateCategory resource, final ServiceTemplateSetting parameter) {
        return generateSimpleKey(component.getId(), resource.getId(), parameter.getId());
    }

    private static String generateSimpleKey(final String... keys) {
        return StringUtils.join(keys, "_");
    }

    private static class ParameterEntry {
        private final ServiceTemplateComponent component;
        private final ServiceTemplateCategory resource;
        private final ServiceTemplateSetting parameter;

        private ParameterEntry(final ServiceTemplateComponent component, final ServiceTemplateCategory resource,
                               final ServiceTemplateSetting parameter) {
            this.component = component;
            this.resource = resource;
            this.parameter = parameter;
        }

        public ServiceTemplateComponent getComponent() {
            return component;
        }

        public ServiceTemplateCategory getResource() {
            return resource;
        }

        public ServiceTemplateSetting getParameter() {
            return parameter;
        }
    }

    public static List<Network> findManagementNetworks(ServiceTemplateComponent component) {
        ServiceTemplateCategory networkResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);
        if (networkResource == null) {
            return null;
        }

        // Check the network configuration widget
        ServiceTemplateSetting networking = networkResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
        if (networking != null && networking.getNetworkConfiguration() != null) {
            return networking.getNetworkConfiguration().getNetworks(NetworkType.HYPERVISOR_MANAGEMENT);
        } else {
            // Could be minimal sever component that just has a drop-down for management network
            ServiceTemplateSetting networkSetting = networkResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID);
            if (networkSetting != null) {
                return networkSetting.getNetworks();
            }
        }

        return null;
    }

    public static Network findDefaultStaticNetwork(ServiceTemplateComponent component) {
        String defStaticNetworkId = getDefaultGateway(component);
        if (defStaticNetworkId != null && !defStaticNetworkId.equals("")) {
            List<Network> networks = findStaticNetworks(component);
            if (networks != null && !networks.isEmpty()) {
                for (Network net : networks) {
                    if (net.getId().equals(defStaticNetworkId)) {
                        return net;
                    }
                }
            }
        }
        return null;
    }

    public static String getDefaultGateway(ServiceTemplateComponent component) {
        return component.getParameterValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_DEFAULT_GATEWAY_ID);
    }

    public static List<Network> findStaticManagementNetworks(ServiceTemplateComponent component) {
        List<Network> networks = findManagementNetworks(component);
        List<Network> ret = new ArrayList<>();
        if (networks != null) {
            for (Network network : networks) {
                if (network != null
                        && network.isStatic()
                        && network.getStaticNetworkConfiguration() != null
                        && StringUtils.isNotBlank(network.getStaticNetworkConfiguration().getIpAddress())) {
                    ret.add(network);
                }
            }
        }
        return ret;
    }

    public static List<Network> findStaticUINetworks(ServiceTemplateComponent component) {
        List<Network> networks = findStaticNetworks(component);
        List<Network> uiNetworks = new ArrayList<>();
        for (Network network : networks) {
            if (NetworkType.PRIVATE_LAN.equals(network.getType()) ||
                    NetworkType.PUBLIC_LAN.equals(network.getType()) ||
                    NetworkType.HYPERVISOR_MANAGEMENT.equals(network.getType())) {
                uiNetworks.add(network);
            }
        }
        return uiNetworks;
    }

    public static List<Network> findStaticNetworks(ServiceTemplateComponent component) {
        List<Network> networks = ServiceTemplateClientUtil.findNetworks(component);

        List<Network> ret = new ArrayList<>();
        if (networks != null) {
            for (Network network : networks) {
                if (network != null
                        && network.isStatic()
                        && network.getStaticNetworkConfiguration() != null) {
                    ret.add(network);
                }
            }
        }
        return ret;
    }

    public static List<Network> findNetworks(ServiceTemplate serviceTemplate) {
        List<Network> networks = new ArrayList<Network>();

        for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            networks.addAll(ServiceTemplateClientUtil.findNetworks(component));
        }

        return networks;
    }

    public static List<Network> findNetworks(ServiceTemplateComponent component) {
        List<Network> networks = new ArrayList<Network>();

        if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType())) {
            ServiceTemplateCategory networkResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);
            if (networkResource != null) {
                // Check the network configuration widget
                ServiceTemplateSetting networking = networkResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
                if (networking != null && networking.getNetworkConfiguration() != null && networking.getNetworkConfiguration().getNetworks() != null) {
                    networks.addAll(networking.getNetworkConfiguration().getNetworks());
                } else {
                    // Could be minimal sever component that just has a drop-down for management network
                    ServiceTemplateSetting networkSetting = networkResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID);
                    if (networkSetting != null && networkSetting.getNetworks() != null) {
                        networks.addAll(networkSetting.getNetworks());
                    }
                }
            }
        } else if (ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
            ServiceTemplateCategory vmSettingsResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE);
            if (vmSettingsResource == null) {
                vmSettingsResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE);
            }
            if (vmSettingsResource != null) {
                ServiceTemplateSetting networkInterfaces = vmSettingsResource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID);
                if (networkInterfaces != null && networkInterfaces.getNetworks() != null && networkInterfaces.getNetworks().size() > 0) {
                    networks.addAll(networkInterfaces.getNetworks());
                }
            }
        }

        return networks;
    }

    public static void addDefaultSelectOption(ServiceTemplateSetting setting) {
        setting.getOptions().add(new ServiceTemplateOption(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT_ID, null, null));
        setting.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT_ID);
    }

    /**
     * Helper to add a setting "Create new ..."
     *
     * @param component      component to process
     * @param options        list of options to select from
     * @param setId          ID of target param  (i.e. volume, datacenter, cluster etc)
     * @param labelCreateNew label in drop down "Create new ..."
     * @param labelNew       label in front of new input field "new xxx"
     */
    public static ServiceTemplateSetting processNewSetting(ServiceTemplateComponent component,
                                                           Collection<ServiceTemplateOption> options,
                                                           String setId,
                                                           String labelCreateNew,
                                                           String labelNew,
                                                           boolean hideFromTemplate) {

        for (ServiceTemplateCategory resource : component.getResources()) {
            ServiceTemplateSetting set = processNewSetting(resource, options, setId, labelCreateNew, labelNew, hideFromTemplate);
            if (set != null)
                return set;
        }
        return null;
    }

    /**
     * Helper to add a setting "Create new ..."
     *
     * @param resource       Resource to process
     * @param options        list of options to select from
     * @param setId          ID of target param  (i.e. volume, datacenter, cluster etc)
     * @param labelCreateNew label in drop down "Create new ..."
     * @param labelNew       label in front of new input field "new xxx"
     */
    public static ServiceTemplateSetting processNewSetting(ServiceTemplateCategory resource,
                                                           Collection<ServiceTemplateOption> options,
                                                           String setId,
                                                           String labelCreateNew,
                                                           String labelNew,
                                                           boolean hideFromTemplate) {

        for (int i = 0; i < resource.getParameters().size(); i++) {
            ServiceTemplateSetting set = resource.getParameters().get(i);
            if (set.getId().equals(setId)) {
                set.getOptions().clear();
                addDefaultSelectOption(set);
                set.getOptions().add(new ServiceTemplateOption(labelCreateNew, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX, null, null));
                if (options != null) {
                    set.getOptions().addAll(options);
                }
                set.setType(ServiceTemplateSetting.ServiceTemplateSettingType.ENUMERATED);

                ServiceTemplateSetting setNew = new ServiceTemplateSetting();
                setNew.setType(ServiceTemplateSetting.ServiceTemplateSettingType.STRING);
                setNew.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + setId);
                setNew.setDisplayName(labelNew);
                setNew.setRequired(set.isRequired());
                setNew.setRequiredAtDeployment(set.isRequiredAtDeployment());
                setNew.setHideFromTemplate(hideFromTemplate);
                setNew.setGroup(set.getGroup());
                setNew.setDependencyTarget(setId);
                setNew.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX);

                resource.getParameters().add(i + 1, setNew);

                return setNew;
            }
        }
        return null;
    }

    /**
     * Create unique identifier for VDS name setting
     *
     * @param networksId Unique ID for networks combination, i.e. PXE + HVM or WKL1 + WKL2 + WKL3
     * @return
     */
    public static String createVDSID(String networksId) {
        return ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID + "::" + networksId;
    }

    /**
     * Create unique identifier for port group setting
     *
     * @param networksId Unique ID for networks combination, i.e. PXE + HVM or WKL1 + WKL2 + WKL3
     * @param networkId  Network ID for port group
     * @param pgIdx      Index of the port group if there are more than one for the same network type in the same VDS, i.e. ISCSI1
     * @return
     */
    public static String createVDSPGID(String networksId, String networkId, int pgIdx) {
        return ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID + "::" + networksId + "::" + networkId + "::" + pgIdx;
    }

    /**
     * Create VDS Name setting
     *
     * @param cluster
     * @param vdsCategory
     * @param id
     * @param displayName
     * @param groupName
     * @param options
     * @return
     */
    public static ServiceTemplateSetting createVDSNameSetting(ServiceTemplateComponent cluster,
                                                              ServiceTemplateCategory vdsCategory, String id,
                                                              String displayName, String groupName, Collection<ServiceTemplateOption> options) {
        ServiceTemplateSetting set = createVDSSetting(vdsCategory, id, displayName, groupName);
        ServiceTemplateSetting setNew = processNewSetting(cluster, options, id, "Create VDS Name ...", "New VDS Name", false);
        setNew.setGenerated(true);
        return set;
    }

    /**
     * Create VFDS port group setting
     *
     * @param vdsCategory
     * @param id
     * @param displayName
     * @param groupName
     * @param options
     * @return
     */
    public static ServiceTemplateSetting createVDSPGSetting(ServiceTemplateComponent cluster,
                                                            ServiceTemplateCategory vdsCategory, String id,
                                                            String displayName, String groupName, Collection<ServiceTemplateOption> options) {
        ServiceTemplateSetting set = createVDSSetting(vdsCategory, id, displayName, groupName);
        ServiceTemplateSetting setNew = processNewSetting(cluster, options, id, "Create Port Group ...", "New Port Group", false);
        setNew.setGenerated(true);
        return set;
    }

    private static ServiceTemplateSetting createVDSSetting(ServiceTemplateCategory vdsCategory, String id,
                                                           String displayName, String groupName) {
        ServiceTemplateSetting vdsNameSetting = new ServiceTemplateSetting();
        vdsNameSetting.setDisplayName(displayName);
        vdsNameSetting.setId(id);
        vdsNameSetting.setType(ServiceTemplateSetting.ServiceTemplateSettingType.ENUMERATED);
        vdsNameSetting.setGroup(groupName);
        vdsNameSetting.setDependencyTarget(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_ID);
        vdsNameSetting.setDependencyValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_VDS_DST_ID);
        vdsNameSetting.setRequired(false);
        vdsNameSetting.setGenerated(true);
        // add in the end of Group
        int i = 0;
        int idx = -1;
        for (ServiceTemplateSetting parameter : vdsCategory.getParameters()) {
            i++;
            if (parameter.getGroup() != null && parameter.getGroup().equals(groupName)) {
                idx = i;
            }
        }
        if (idx < 0)
            idx = vdsCategory.getParameters().size();

        vdsCategory.getParameters().add(idx, vdsNameSetting);
        return vdsNameSetting;
    }

    /**
     * Clone options and set new dependency target.
     *
     * @param options
     * @param vdsNameId New dependency target ID
     * @return
     */
    public static List<ServiceTemplateOption> copyOptions(Collection<ServiceTemplateOption> options, String vdsNameId) {
        List<ServiceTemplateOption> newOptions = new ArrayList<>();
        for (ServiceTemplateOption option : options) {
            ServiceTemplateOption newOption = new ServiceTemplateOption(option.getName(), option.getValue(),
                    option.getDependencyTarget(), option.getDependencyValue(), new HashMap<>(option.getAttributes()));

            if (vdsNameId != null)
                newOption.setDependencyTarget(vdsNameId);
            newOptions.add(newOption);
        }
        return newOptions;
    }

    /**
     * Find existing port group setting or create new one.
     *
     * @param cluster    template component - Cluster
     * @param networksId ID for networks combo
     * @param name       PG network display name i.e. "Workload"
     * @param networkId  PG network ID
     * @param idx        PG network index i.e. 1 or 2
     * @return
     */
    public static ServiceTemplateSetting getPortGroup(ServiceTemplateComponent cluster, String networksId, String name,
                                                      String networkId, int idx, boolean createNew) {
        ServiceTemplateCategory vdsCategory = cluster.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID);
        if (vdsCategory == null)
            return null;

        ServiceTemplateSetting vdsName = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                createVDSID(networksId));

        if (vdsName == null)
            return null;

        String pgID = createVDSPGID(networksId, networkId, idx);

        // this group must always exist
        ServiceTemplateSetting vdsPG1 = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID);

        for (ServiceTemplateSetting vdsPG : vdsCategory.getParameters()) {
            if (vdsPG.getId().equals(pgID)) {
                vdsPG.setOptions(copyOptions(vdsPG1.getOptions(), vdsName.getId()));
                return vdsPG;
            }
        }

        // reached the end of created PGs, create new
        if (createNew)
            return createVDSPGSetting(cluster, vdsCategory, pgID,
                    name, vdsName.getGroup(), copyOptions(vdsPG1.getOptions(), vdsName.getId()));
        else
            return null;
    }


    /**
     * Returns true if given PG belongs to VDS name
     *
     * @param vdsId VDS name ID
     * @param pgId  VDS PG ID
     * @return
     */
    public static boolean isVDSGroup(String vdsId, String pgId) {
        if (vdsId == null || pgId == null)
            return false;

        if (!vdsId.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID + "::"))
            return false;

        if (!pgId.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID + "::"))
            return false;

        String[] arrVDS = vdsId.split("::");
        String[] arrPG = pgId.split("::");

        if (arrVDS.length < 2 || arrPG.length < 2)
            return false;

        return arrPG[1].equals(arrVDS[1]);
    }

    /**
     * From VDS name or PG setting ID get the IDs of partition networks, i.e. xxx::yyy::zzz
     *
     * @param vdsId
     * @return
     */
    public static String extractNetworksID(String vdsId) {
        if (vdsId == null)
            return null;

        if (!vdsId.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID + "::") &&
                !vdsId.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID + "::"))
            return null;

        String[] arrVDS = vdsId.split("::");
        if (arrVDS.length < 2)
            return null;

        return arrVDS[1];
    }

    /**
     * From VDS PG setting ID get the network ID
     * Used by ASMUI
     *
     * @param vdsId
     * @return
     */
    public static String extractNetworkID(String vdsId) {
        if (vdsId == null)
            return null;

        if (!vdsId.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID + "::"))
            return null;

        String[] arrVDS = vdsId.split("::");
        if (arrVDS.length < 3)
            return null;

        return arrVDS[2];
    }

    /**
     * From VDS name or PG setting ID get the IDs of partition networks, i.e. xxx::yyy::zzz
     *
     * @param vdsId
     * @return
     */
    public static String replaceNetworksID(String vdsId, String newNetworksID) {
        if (vdsId == null)
            return null;

        if (!vdsId.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_NAME_ID + "::") &&
                !vdsId.contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_VDS_PG_ID + "::"))
            return null;

        String[] arrVDS = vdsId.split("::");
        if (arrVDS.length < 2)
            return null;

        arrVDS[1] = newNetworksID;

        return StringUtils.join(arrVDS, "::");
    }


    public static boolean scaleupNetworkPortGroups(ServiceTemplateComponent cluster, String portGroupName, boolean newPortGroup,
                                                   List<String> networks, String networkId, String networkName, int maxPgNum) {
        ServiceTemplateCategory vdsCategory = cluster.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID);
        if (vdsCategory == null) {
            LOGGER.error("No VDS category found");
            return false;
        }

        // old ID for this VDS
        Collections.sort(networks);
        String networksId = StringUtils.join(networks, ":");

        // new ID for this VDS. Order of networks is significant!
        List<String> newNetworkList = new ArrayList<>();
        newNetworkList.addAll(networks);
        newNetworkList.add(networkId);
        Collections.sort(newNetworkList);
        String newNetworksId = StringUtils.join(newNetworkList, ":");

        ServiceTemplateSetting vdsName = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                createVDSID(networksId));

        // VDS may exist if we have redundancy = true and such call was already made for the first port
        if (vdsName != null) {

            // new ID
            String newVDSID = createVDSID(newNetworksId);

            for (ServiceTemplateSetting vdsPG : vdsCategory.getParameters()) {
                if (isVDSGroup(vdsName.getId(), vdsPG.getId())) {
                    // replace VDS ID
                    String newId = replaceNetworksID(vdsPG.getId(), newNetworksId);
                    if (newId == null) {
                        LOGGER.error("Cannot replace VDS networks ID for " + vdsPG.getId());
                        return false;
                    }
                    // lookup for dependency targets with such ID
                    replaceIDinDependencies(vdsCategory.getParameters(), vdsPG.getId(), newId);
                    vdsPG.setId(newId);
                }
            }

            // lookup for dependency targets with such ID
            replaceIDinDependencies(vdsCategory.getParameters(), vdsName.getId(), newVDSID);
            // replace VDS name ID
            vdsName.setId(newVDSID);
            // don't forget about $new$
            String vID = createVDSID(networksId);
            vdsName = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + vID);
            vdsName.setId(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + newVDSID);
        } else {

            vdsName = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                    createVDSID(newNetworksId));

            // make sure we have new VDS at this point
            if (vdsName == null) {
                LOGGER.error("No VDS found in the cluster parameters " + newNetworksId);
                return false;
            }
        }

        // add new group
        for (int i = 1; i <= maxPgNum; i++) {
            String portGroupDisplayName = networkName + " Port Group";
            if (i > 1)
                portGroupDisplayName += " " + i;

            ServiceTemplateSetting vdsPG = getPortGroup(cluster, newNetworksId, portGroupDisplayName, networkId, i, false);
            if (vdsPG == null) {
                vdsPG = getPortGroup(cluster, newNetworksId, portGroupDisplayName, networkId, i, true);
                if (vdsPG == null) {
                    LOGGER.error("Cannot create VDS PG for  " + networkName);
                    return false;
                }

                vdsPG.setHideFromTemplate(false);
                if (newPortGroup) {
                    vdsPG.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX);
                    vdsPG = cluster.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_VDS_ID,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CREATE_NEW_PREFIX + vdsPG.getId());
                }
                vdsPG.setValue(portGroupName);
                return true;
            }
        }

        LOGGER.warn("Cannot add more than " + maxPgNum + " port groups to VDS. All groups already exist.");
        return true;
    }

    private static void replaceIDinDependencies(List<ServiceTemplateSetting> parameters, String oldId, String newId) {
        if (parameters == null || oldId == null) return;
        for (ServiceTemplateSetting vdsPG : parameters) {
            if (oldId.equals(vdsPG.getDependencyTarget())) {
                vdsPG.setDependencyTarget(newId);
            }
        }
    }

    /**
     * Returns all of the NetworkIds that are assigned in the ServiceTemplate.
     *
     * @param serviceTemplate the template whose NetworkIds will be returned.
     * @return all of the NetworkIds that are assigned in the ServiceTemplate.
     */
    public static Set<String> getNetworkIds(ServiceTemplate serviceTemplate) {

        HashSet<String> networkIds = new HashSet<String>();

        if (serviceTemplate != null) {
            for (ServiceTemplateComponent component : serviceTemplate.getComponents()) {

                if (ServiceTemplateComponent.ServiceTemplateComponentType.SERVER.equals(component.getType()) ||
                        ServiceTemplateComponent.ServiceTemplateComponentType.VIRTUALMACHINE.equals(component.getType())) {
                    for (ServiceTemplateCategory resource : component.getResources()) {
                        // if server networking category or esxi vm category or hyperv vm category
                        if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID.equals(resource.getId()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE.equals(resource.getId()) ||
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE.equals(resource.getId())) {
                            for (ServiceTemplateSetting param : resource.getParameters()) {
                                // if server networking configuration parameter
                                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID.equals(param.getId())) {
                                    NetworkConfiguration configuration = ServiceTemplateClientUtil.deserializeNetwork(param.getValue());
                                    networkIds.addAll(configuration.getAllNetworkIds());
                                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERVISOR_NETWORK_ID.equals((param.getId())) ||
                                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID.equals(param.getId())) {
                                    if (!(param.getValue().isEmpty() || param.getValue().equals("-1"))) {
                                        // value may be a comma-separated list, but if that is the case it will lead with a comma,
                                        // e.g. ,1,2,3.  The reject below gets rid of the initial empty element
                                        if (param != null && param.getNetworks() != null) {
                                            for (Network network : param.getNetworks()) {
                                                if (network != null && network.getId() != null) {
                                                    networkIds.add(network.getId());
                                                }
                                            }
                                        }
                                        // NetworkIds are stored in a comma separated list as values of this param 
                                        for (String guid : param.getValue().split(",")) {
                                            if (!guid.isEmpty()) {
                                                networkIds.add(guid);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return networkIds;
    }

    /**
     * Currently uses ObjectMapper. Will NOT throw exception but return null if json is wrong.
     * It does NOT require adding "networkConfiguration:" to JSON as for MarshalUtil.fromJSON
     *
     * @param value
     * @return
     */
    public static com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration deserializeNetwork(String value) {
        try {
            return OBJECT_MAPPER.readValue(value, com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration.class);
            //String configString = "{ \"networkConfiguration\" : " + value + "}";
            //return MarshalUtil.fromJSON(com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration.class, configString);
        } catch (IOException e) {
            LOGGER.error("Network configuration has invalid value: " + value);
        }

        return null;
    }

    public static List<Network> findVMStaticNetworks(ServiceTemplateComponent component) {
        List<Network> ret = new ArrayList<>();
        ServiceTemplateSetting setNetwork = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID);
        if (setNetwork == null) {
            setNetwork = component.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE,
                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_NETWORK_ID);
        }

        if (setNetwork != null && setNetwork.getNetworks() != null) {
            for (Network network : setNetwork.getNetworks()) {
                if (network.isStatic() && network.getStaticNetworkConfiguration() != null &&
                        network.getStaticNetworkConfiguration().getIpAddress() != null) {
                    ret.add(network);
                }
            }
        }
        return ret;
    }

    /**
     * For given storage components finds volume name which could be stored in different settings.
     * If volume name is to be entered at deployment, or generated and is currently mull the method will
     * return empty string.
     * Returns null only if the resource is not storage or expected attributes are not there.
     *
     * @param resource
     * @return
     */
    public static String getVolumeNameForStorageComponent(ServiceTemplateCategory resource) {
        if (resource == null) {
            return null;
        }
        for (ServiceTemplateSetting param : resource.getParameters()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(param.getId())) {
                ServiceTemplateSetting nameSetting = null;
                if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE.equals(param.getValue())) {
                    nameSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_GENERATED);
                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING.equals(param.getValue())) {
                    nameSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_EXISTING);
                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW.equals(param.getValue())) {
                    nameSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW);
                } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT.equals(param.getValue())) {
                    nameSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_NEW_AT_DEPLOYMENT);
                }
                if (nameSetting == null) {
                    String err = "Cannot find volume name setting in the service template, volume mode: " + param.getValue() + "\n";
                    try {
                        err += param.getValue() + OBJECT_MAPPER.writeValueAsString(resource);
                    } catch (JsonProcessingException e) {
                        LOGGER.error("JSON serialization failed", e);
                    }
                    LOGGER.error(err);
                    return null;
                }

                return nameSetting.getValue() != null ? nameSetting.getValue() : "";
            }
        }
        return null;
    }

    /**
     * Returns true if storage volume in this template resource meant to be new - either user entered or generated
     *
     * @param resource
     * @return
     */
    public static boolean isNewStorageVolume(ServiceTemplateCategory resource, boolean includeGenerated) {
        if (resource == null)
            return false;

        for (ServiceTemplateSetting param : resource.getParameters()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(param.getId())) {
                return isNewStorageVolume(param, includeGenerated);
            }
        }
        return false;
    }

    /**
     * Returns true if storage volume in this template resource was entered as "existing"
     *
     * @param resource
     * @return
     */
    public static boolean isExistingVolume(ServiceTemplateCategory resource) {
        if (resource == null)
            return false;

        for (ServiceTemplateSetting param : resource.getParameters()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(param.getId())) {
                return (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_EXISTING.equals(param.getValue()));
            }
        }
        return false;
    }

    /**
     * Returns true if storage volume in this template resource was entered as "generated"
     *
     * @param resource
     * @return
     */
    public static boolean isGeneratedVolume(ServiceTemplateCategory resource) {
        if (resource == null)
            return false;

        for (ServiceTemplateSetting param : resource.getParameters()) {
            if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(param.getId())) {
                return (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE.equals(param.getValue()));
            }
        }
        return false;
    }

    /**
     * Returns true if storage volume in this template resource meant to be new - either user entered or generated
     *
     * @param param
     * @return
     */
    public static boolean isNewStorageVolume(ServiceTemplateSetting param, boolean includeGenerated) {
        if (param == null)
            return false;

        return (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TITLE_ID.equals(param.getId()) && (
                (includeGenerated && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_AUTOGENERATE.equals(param.getValue())) ||
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_NOW.equals(param.getValue()) ||
                        ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VOLUME_NAME_OPTION_CREATE_DEPLOYMENT.equals(param.getValue())));
    }
}
