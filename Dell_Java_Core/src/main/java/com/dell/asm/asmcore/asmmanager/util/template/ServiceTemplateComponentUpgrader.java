package com.dell.asm.asmcore.asmmanager.util.template;

import com.dell.asm.asmcore.asmmanager.client.networkconfiguration.Fabric;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.alcm.client.model.NTPSetting;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.google.common.collect.ImmutableMap;

public class ServiceTemplateComponentUpgrader {

    // keep this constructor private so its not instantiated
    private ServiceTemplateComponentUpgrader() {
    }

    private static final Logger LOGGER = Logger.getLogger(ServiceTemplateComponentUpgrader.class);

    private final static ImmutableMap.Builder<String, String> MAP_BUILDER = new ImmutableMap.Builder<String, String>();
    private final static Map<String, String> RESOURCES_TO_COMPONENT_MAP;

    static {
        // New or changed at ServiceTemplateSchema_7.6.0.2338.xml
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID, "compellent::createvol");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_EQL_COMP_ID, "asm::volume::equallogic");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_NETAPP_COMP_ID, "netapp::create_nfs_export");
        // New or changed at ServiceTemplateSchema_8.0.0-3222.xml
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_COMP_ID, "asm::volume::compellent");
        // New or changed at ServiceTemplateSchema_8.0.1.3760.xml
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL, "asm::idrac", "asm::server", "asm::esxiscsiconfig");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_OS, "asm::server", "asm::esxiscsiconfig");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_HW, "asm::idrac", "asm::esxiscsiconfig");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMPONENT_ID, "asm::cluster");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SCVMM_CLUSTER_COMPONENT_ID, "asm::cluster::scvmm");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_VC_COMPONENT_ID, "asm::server", "asm::vm::vcenter");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_VC_CLONE_COMPONENT_ID, "asm::vm::vcenter");
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_CLONE_COMPONENT, "asm::vm::scvmm");
        addTemplateComponentIdMapping("component-linux_postinstall-1", "linux_postinstall");
        addTemplateComponentIdMapping("component-citrix_xd7-1", "citrix_xd7");
        addTemplateComponentIdMapping("component-mssql2012-1", "mssql2012");
        addTemplateComponentIdMapping("component-windows_postinstall-1", "windows_postinstall");
        // New or changed at ServiceTemplateSchema_8.1.0-1424.xml
        addTemplateComponentIdMapping(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_COMPID_ALL, "asm::idrac", "asm::bios",
                "asm::server", "asm::esxiscsiconfig");
        addTemplateComponentIdMapping("component-bmc-1", "bmc");
        RESOURCES_TO_COMPONENT_MAP = MAP_BUILDER.build();

        /**
         * Below is output from mapper.rb ruby script that parses ServiceTemplates to componentID
         * and resourceIds key/value pairs. This data was aggregated to generate the component/resource map above.

         ServiceTemplateSchema_7.6.0.2338.xml(13)
         {"component-compellent-1"=>["compellent::createvol"],
         "component-equallogic-chap-1"=>["asm::volume::equallogic"],
         "component-netapp-1"=>["netapp::create_nfs_export"],
         "6ac6e030-05a5-44e5-b40a-4d981f36278c"=>["asm::idrac", "asm::server", "asm::esxiscsiconfig"],
         "12ec8285-779c-42bc-bac4-b5e6fedd8ea6"=>["asm::cluster"],
         "578b2331-eff0-4d9c-8257-1ed263f66321"=>["asm::cluster::scvmm"],
         "7fa4bbb3-6fa5-44b3-a923-9dee1aa60409"=>["asm::server", "asm::vm::vcenter"],
         "2bc7b89f-4257-4d4b-9e14-5c4eba868254"=>["asm::vm::vcenter"],
         "17f2f0e9-d130-4140-b580-72cd776d507d"=>["asm::vm::scvmm"],
         "777a7a22-014e-40b8-b2e2-cc97a2b965b4"=>["citrix_xd7"],
         "7db268ba-1903-4597-bc02-649631347916"=>["linux_postinstall"],
         "06a5a2c0-a480-4b4e-9a31-47009bafc7a1"=>["mssql2012"],
         "d2d27565-e1fc-4e47-997c-572361845bb6"=>["windows_postinstall"]}

         ServiceTemplateSchema_8.0.0-3222.xml(13)
         {"component-compellent-1"=>["asm::volume::compellent"],
         "component-equallogic-chap-1"=>["asm::volume::equallogic"],
         "component-netapp-1"=>["netapp::create_nfs_export"],
         "483c883c-8632-48ef-8b75-5b1cc6958db8"=>["asm::idrac", "asm::server", "asm::esxiscsiconfig"],
         "a2cd7e8b-3512-487d-b76d-d77e2c3f6326"=>["asm::cluster"],
         "5051e4ee-3d35-47b4-a50a-39fa9d1daeba"=>["asm::cluster::scvmm"],
         "48485636-39b8-499e-8a4d-d6912eaebda7"=>["asm::server", "asm::vm::vcenter"],
         "5edde638-20b4-4fd3-91e4-abf44a071996"=>["asm::vm::vcenter"],
         "b5c72c2c-470a-4956-9c0b-06a2c65fe77c"=>["asm::vm::scvmm"],
         "ddff4b7f-bd54-4968-9875-0ea7631cb411"=>["citrix_xd7"],
         "e144009c-3de9-4da5-9fca-0c79029926dd"=>["linux_postinstall"],
         "0baada0e-a938-436f-85b6-5d62eac20a88"=>["mssql2012"],
         "e30755a6-e480-48d3-a879-9e5b82bf566a"=>["windows_postinstall"]}

         ServiceTemplateSchema_8.0.1.3760.xml(14)
         {"component-compellent-1"=>["asm::volume::compellent"],
         "component-equallogic-chap-1"=>["asm::volume::equallogic"],
         "component-netapp-1"=>["netapp::create_nfs_export"],
         "component-server-1"=>["asm::idrac", "asm::server", "asm::esxiscsiconfig"],
         "component-serverminimal-1"=>["asm::server", "asm::esxiscsiconfig"],
         "component-cluster-vcenter-1"=>["asm::cluster"],
         "component-cluster-hyperv-1"=>["asm::cluster::scvmm"],
         "component-virtualmachine-vcenter-1"=>["asm::server", "asm::vm::vcenter"],
         "component-virtualmachine-clonevcenter-1"=>["asm::vm::vcenter"],
         "component-virtualmachine-clonehyperv-1"=>["asm::vm::scvmm"],
         "component-citrix_xd7-1"=>["citrix_xd7"],
         "component-linux_postinstall-1"=>["linux_postinstall"],
         "component-mssql2012-1"=>["mssql2012"],
         "component-windows_postinstall-1"=>["windows_postinstall"]}

         ServiceTemplateSchema_8.1.0-1424.xml(15)
         {"component-compellent-1"=>["asm::volume::compellent"],
         "component-equallogic-chap-1"=>["asm::volume::equallogic"],
         "component-netapp-1"=>["netapp::create_nfs_export"],
         "component-server-1"=>["asm::idrac", "asm::bios", "asm::server", "asm::esxiscsiconfig"],
         "component-serverminimal-1"=>["asm::server", "asm::esxiscsiconfig"],
         "component-cluster-vcenter-1"=>["asm::cluster"],
         "component-cluster-hyperv-1"=>["asm::cluster::scvmm"],
         "component-virtualmachine-vcenter-1"=>["asm::server", "asm::vm::vcenter"],
         "component-virtualmachine-clonevcenter-1"=>["asm::vm::vcenter"],
         "component-virtualmachine-clonehyperv-1"=>["asm::vm::scvmm"],
         "component-bmc-1"=>["bmc"],
         "component-citrix_xd7-1"=>["citrix_xd7"],
         "component-linux_postinstall-1"=>["linux_postinstall"],
         "component-mssql2012-1"=>["mssql2012"],
         "component-windows_postinstall-1"=>["windows_postinstall"]}
         */
    }

    private static void addTemplateComponentIdMapping(final String componentID, final String... resourceIds) {
        MAP_BUILDER.put(generateTemplateComponentResourceKey(Arrays.asList(resourceIds)), componentID);
    }

    public static boolean componentIdIsMissingOrUnknown(final ServiceTemplateComponent component) {
        return StringUtils.isBlank(component.getComponentID()) ||
                !(componentIdFoundInValues(component.getComponentID()));
    }

    public static void assignOriginatingComponentId(final ServiceTemplateComponent component) {
        final String componentResourceKey = generateTemplateComponentResourceKey(component);
        if (RESOURCES_TO_COMPONENT_MAP.containsKey(componentResourceKey)) {
            final String componentID = RESOURCES_TO_COMPONENT_MAP.get(componentResourceKey);
            LOGGER.info("Assigning componentID " + componentID + " to component id: " + component.getId());
            component.setComponentID(componentID);
        } else {
            // NOTE[fcarta] - For now we will only log unmapped components instead of throwing exception
            LOGGER.error("Cannnot find component resource key: " + componentResourceKey +
                    " for component: " + component.getId() +
                    " with componentID: " + component.getComponentID());
        }
    }

    private static String generateTemplateComponentResourceKey(final ServiceTemplateComponent component) {
        final List<String> resourceIds = new ArrayList<String>();
        for (final ServiceTemplateCategory resource : component.getResources()) {
            resourceIds.add(resource.getId());
        }
        return generateTemplateComponentResourceKey(resourceIds);
    }

    private static String generateTemplateComponentResourceKey(final List<String> resourcesIds) {
        Collections.sort(resourcesIds); // this gives us a deterministic ordering of the resource ids for map key
        return StringUtils.join(resourcesIds.toArray());
    }

    private static boolean componentIdFoundInValues(final String componentId) {
        return RESOURCES_TO_COMPONENT_MAP.values().contains(componentId);
    }

    /**
     * Networking section, default gateway should be set to DHCP/No Gateway
     *
     * @param serviceTemplate
     */
    public static void setDefaultGateway(ServiceTemplate serviceTemplate) {
        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            ServiceTemplateCategory category = null;

            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                category = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORKING_COMP_ID);
            } else if (component.getType() == ServiceTemplateComponentType.VIRTUALMACHINE) {
                category = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE);
                if (category == null)
                    category = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE);
            }

            if (category == null)
                continue;

            ServiceTemplateSetting setting = category.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_DEFAULT_GATEWAY_ID);
            if (setting == null)
                continue;
            if (setting.getValue() == null ||
                    setting.getValue().equals(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SELECT_ID)) {
                setting.setValue(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_DHCP_NETWORK_ID);
            }
        }
    }

    /**
     * Networking section in 8.1.1 uses fabrics for blades. Move all to interfaces.
     *
     * @param serviceTemplate
     */
    public static void upgradeNetworkingTo82(ServiceTemplate serviceTemplate, boolean deployed) {
        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                for (ServiceTemplateCategory resource : component.getResources()) {
                    ServiceTemplateSetting networkingSetting = resource.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_NETWORK_CONFIG_ID);
                    if (networkingSetting != null) {
                        com.dell.asm.asmcore.asmmanager.client.networkconfiguration.NetworkConfiguration networkConfig = null;
                        if (deployed) {
                            networkConfig = networkingSetting.getNetworkConfiguration();
                        } else {
                            networkConfig = ServiceTemplateUtil.deserializeNetwork(networkingSetting.getValue());
                        }

                        if (networkConfig != null) {

                            if (StringUtils.isNotEmpty(networkConfig.getServertype())
                                    && networkConfig.getServertype().equals("blade")) {

                                for (Fabric f : networkConfig.getFabrics()) {
                                    if (f.isEnabled()) {
                                        f.setName(convertFabricName(f.getName()));
                                        f.setFabrictype(f.isUsedforfc() ? Fabric.FC_TYPE : Fabric.ETHERNET_TYPE);
                                        networkConfig.getInterfaces().add(f);
                                    }
                                }
                                networkConfig.getFabrics().clear();
                                networkConfig.setServertype(null);

                                // only save if updated
                                String json = ServiceTemplateUtil.serializeNetwork(networkConfig);
                                networkingSetting.setValue(json);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * VSAN option is Server component needs to ve==be set to "hybrid".
     *
     * @param serviceTemplate
     */
    public static void upgradeVSANto83(ServiceTemplate serviceTemplate, ServiceTemplate defaultTemplate) {
        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.SERVER) {
                ServiceTemplateCategory osResource = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
                if (osResource != null) {
                    ServiceTemplateSetting vsanSet = osResource.getParameter(ServiceTemplateSettingIDs.LOCAL_STORAGE_ID);
                    if (vsanSet != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TRUE_VALUE.equals(vsanSet.getValue())) {
                        ServiceTemplateSetting vsanTypeSet = osResource.getParameter(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID);
                        if (vsanTypeSet == null) {
                            // upgrade !
                            vsanTypeSet = defaultTemplate.getTemplateSetting(ServiceTemplateComponentType.SERVER,
                                    ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                                    ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID);
                            if (vsanTypeSet == null) {
                                throw new AsmManagerRuntimeException("Template upgrade failed - missed parameter in default template " +
                                        ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_ID);
                            }
                            vsanTypeSet.setValue(ServiceTemplateSettingIDs.LOCAL_STORAGE_TYPE_HYBRID);
                            int idx = osResource.getParameters().indexOf(vsanSet);
                            osResource.getParameters().add(idx + 1, vsanTypeSet);
                        }
                    }
                }
            }
        }
    }

    private static String convertFabricName(String name) {
        switch (name) {
            case "Fabric A":
                return "Interface 1";
            case "Fabric B":
                return "Interface 2";
            case "Fabric C":
                return "Interface 3";
            default:
                return name;
        }
    }

    /**
     * Preset NTP server to the value from initial setup. Only for default templates.
     *
     * @param serviceTemplate
     */
    public static void setDefaultNTPServer(ServiceTemplate serviceTemplate) {

        if (!serviceTemplate.isTemplateLocked())
            return;

        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            ServiceTemplateCategory category = null;

            if (component.getType() == ServiceTemplateComponentType.SERVER ||
                    component.getType() == ServiceTemplateComponentType.VIRTUALMACHINE) {
                category = component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE);
            }

            if (category == null)
                continue;

            ServiceTemplateSetting setting = category.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_LINUX_NTP_ID);
            if (setting == null) {
                setting = category.getParameter("ntp"); // legacy parameter
            }
            if (setting == null)
                continue;

            if (StringUtils.isEmpty(setting.getValue())) {
                NTPSetting ntpSet = ProxyUtil.getAlcmNTPProxy().getNTPSettings();
                if (StringUtils.isNotEmpty(ntpSet.getPreferredNTPServer())) {
                    setting.setValue(ntpSet.getPreferredNTPServer());
                }
            }
        }
    }

    /**
     * 8.3.1 ASM-7291: Storage volume now uses multiple settings.
     *
     * @param serviceTemplate
     */
    public static void upgradeStorageVolumeSettings(ServiceTemplate serviceTemplate) {
        for (final ServiceTemplateComponent component : serviceTemplate.getComponents()) {
            if (component.getType() == ServiceTemplateComponentType.STORAGE) {
                for (ServiceTemplateCategory resource: component.getResources()) {
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_RESOURCE_LIST.contains(resource.getId())) {
                        LOGGER.debug("Upgrading storage volume settings for template name = " + serviceTemplate.getTemplateName());
                        ServiceTemplateUtil.convertOldStorageVolume(resource);
                    }
                }
            }
        }
    }
}