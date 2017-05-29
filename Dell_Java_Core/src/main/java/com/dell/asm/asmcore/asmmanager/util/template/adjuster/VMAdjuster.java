/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.template.adjuster;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.vsphere.DatacenterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ManagedObjectDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VMTemplateDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VirtualMachineDTO;
import com.dell.asm.asmcore.asmmanager.util.PuppetModuleUtil;
import com.dell.asm.asmcore.asmmanager.client.util.VcenterInventoryUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class VMAdjuster implements IComponentAdjuster {

    private static final Logger LOGGER = Logger.getLogger(VMAdjuster.class);

    private static VMAdjuster instance;

    /**
     * Private constructor.
     */
    private VMAdjuster() {

    }

    public static VMAdjuster getInstance() {
        if (instance == null)
            instance = new VMAdjuster();
        return instance;
    }

    @Override
    public void refine (ServiceTemplateComponent refineComponent, ServiceTemplate referencedTemplate) {
        final Map<String, String> relComps = refineComponent.getRelatedComponents();
        List<String> hypervisors = new ArrayList<>();
        if (MapUtils.isNotEmpty(relComps)) {
            for (String key : relComps.keySet()) {
                final ServiceTemplateComponent relComp = referencedTemplate.getTemplateComponent(key);
                if (relComp != null && relComp.getType() == ServiceTemplateComponent.ServiceTemplateComponentType.CLUSTER) {
                    ServiceTemplateSetting hvSetting = relComp.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID,
                            ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ASM_GUID);
                    if (hvSetting != null && StringUtils.isNotEmpty(hvSetting.getValue())) {
                        // vCenter refID
                        hypervisors.add(hvSetting.getValue());
                    }
                }
            }
        }
        // for scale up case the cluster might be connected to some server
        refineVMByVCenter(refineComponent, hypervisors);
    }

    private void refineVMByVCenter(ServiceTemplateComponent vmComponent, List<String> hypervisors) {
        // for new component we check getId(), for existing - getComponentId(). Thanks to UI guys for clarity.
        if (!ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_VC_CLONE_COMPONENT_ID.equals(vmComponent.getComponentID()) &&
                !ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_VC_CLONE_COMPONENT_ID.equals(vmComponent.getId())) {
            return;
        }

        if (CollectionUtils.isEmpty(hypervisors))
            return;

        // create a list of facts for vCenters
        List<ManagedObjectDTO> vCenterList = new ArrayList<>();
        List<ArrayList<String>> hypervisorSpecs = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        for (String refId: hypervisors) {
            try {
                Map<String, String> deviceDetails = PuppetModuleUtil.getPuppetDevice(refId);
                if (deviceDetails == null) continue;
                vCenterList.add(VcenterInventoryUtils.convertPuppetDeviceDetailsToDto(deviceDetails));
                hypervisorSpecs.add(mapper.readValue(deviceDetails.get("customization_specs"), ArrayList.class));
            } catch (Exception e1) {
                LOGGER.error("Could not find deviceDetails for " + refId, e1);
            }
        }

        // datacenter
        ServiceTemplateSetting dcSettinhg = vmComponent.getParameter(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_TEMPLATE_DC);

        Iterator<ServiceTemplateOption> iterator = dcSettinhg.getOptions().iterator();
        while (iterator.hasNext()) {
            ServiceTemplateOption option = iterator.next();
            // skip "" for select prompt
            if (StringUtils.isEmpty(option.getValue())) continue;

            if (option.getValue().equals(dcSettinhg.getValue())) continue;

            if (!vCenterContains(vCenterList, option.getValue(), DatacenterDTO.class)) {
                iterator.remove();
            }
        }

        // vm source
        ServiceTemplateSetting vm = vmComponent.getParameter(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_TEMPLATE);

        iterator = vm.getOptions().iterator();
        while (iterator.hasNext()) {
            ServiceTemplateOption option = iterator.next();
            // skip "" for select prompt
            if (StringUtils.isEmpty(option.getValue())) continue;

            if (option.getValue().equals(vm.getValue())) continue;

            if (!vCenterContains(vCenterList, option.getValue(), VirtualMachineDTO.class) &&
                    !vCenterContains(vCenterList, option.getValue(), VMTemplateDTO.class)) {
                iterator.remove();
            }
        }

        // customization specs
        ServiceTemplateSetting specs = vmComponent.getParameter(
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VM_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_VCENTER_VM_CUSTOM_SPEC);

        iterator = specs.getOptions().iterator();
        while (iterator.hasNext()) {
            ServiceTemplateOption option = iterator.next();
            // skip "" for select prompt
            if (StringUtils.isEmpty(option.getValue())) continue;

            if (option.getValue().equals(specs.getValue())) continue;

            if (!vCenterContainsCustomSpecs(hypervisorSpecs, option.getValue())) {
                iterator.remove();
            }
        }

    }

    /**
     * For each vCenter refId get the map of internal DTO and look up for specified object
     * @param hypervisors
     * @param value
     * @return
     */
    private boolean vCenterContains(List<ManagedObjectDTO> hypervisors, String value, Class clazz) {
        if (hypervisors == null ||
                hypervisors.isEmpty() || value == null || clazz == null) return false;

        for (ManagedObjectDTO vCenter: hypervisors) {
            List<ManagedObjectDTO> objects = vCenter.getAllDescendantsByType(clazz);
            if (objects != null) {
                for (ManagedObjectDTO dto: objects) {
                    if (value.equals(dto.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * For each vCenter refId get the attributes and look up for customization_specs
     * @param hypervisorSpecs
     * @param value
     * @return
     */
    private boolean vCenterContainsCustomSpecs(List<ArrayList<String>> hypervisorSpecs, String value) {
        if (hypervisorSpecs == null ||
                hypervisorSpecs.isEmpty() || value == null) return false;

        for (ArrayList<String> customSpecList: hypervisorSpecs) {
            if (customSpecList == null) continue;
            for (String customSpecName : customSpecList){
                if (value.equals(customSpecName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
