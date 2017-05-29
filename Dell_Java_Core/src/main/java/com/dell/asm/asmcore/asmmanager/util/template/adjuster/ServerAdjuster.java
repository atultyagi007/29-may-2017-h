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
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

public class ServerAdjuster implements IComponentAdjuster {

    private static ServerAdjuster instance;

    /**
     * Private constructor.
     */
    private ServerAdjuster() {

    }

    public static ServerAdjuster getInstance() {
        if (instance == null)
            instance = new ServerAdjuster();
        return instance;
    }

    @Override
    public void refine(ServiceTemplateComponent refineComponent, ServiceTemplate referencedTemplate) {
        // lookup for storage
        final Set<String> relComps = refineComponent.getAssociatedComponents().keySet();
        boolean foundISCSIStorage = false;
        boolean foundEQLStorage = false;

        if (CollectionUtils.isNotEmpty(relComps)) {
            final Map<String, ServiceTemplateComponent> componentMap = referencedTemplate.fetchComponentsMap();
            for (String key : relComps) {
                final ServiceTemplateComponent relComp = componentMap.get(key);
                if (relComp != null && ServiceTemplateComponent.ServiceTemplateComponentType.STORAGE.equals(relComp.getType())) {
                    // need EQL or Compellent with ISCSI portype
                    ServiceTemplateCategory eqlCat = relComp.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_EQL_COMP_ID);
                    if (eqlCat != null) {
                        foundISCSIStorage = true;
                        foundEQLStorage = true;
                        break;
                    } else {
                        ServiceTemplateSetting porttype = relComp.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_COMPELLENT_COMP_ID,
                                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ID);
                        if (porttype != null && ServiceTemplateSettingIDs.SERVICE_TEMPLATE_COMPELLENT_PORTTYPE_ISCSI.equals(porttype.getValue())) {
                            foundISCSIStorage = true;
                        }
                    }
                }
                if (foundEQLStorage && foundISCSIStorage) {
                    break;
                }
            }
        }

        refineServerComponentForISCSIInitiator(refineComponent, foundISCSIStorage);
        refineServerComponentForEQL(refineComponent, foundEQLStorage);
    }

    private void refineServerComponentForISCSIInitiator(ServiceTemplateComponent refineComponent, boolean enable) {
        ServiceTemplateSetting settings = refineComponent.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_ISCSI_ID);
        if (settings != null) {
            settings.setHideFromTemplate(!enable);
        }
    }

    private void refineServerComponentForEQL(ServiceTemplateComponent refineComponent, boolean enable) {
        ServiceTemplateSetting settings = refineComponent.getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_RESOURCE,
                ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_EQL_MEM_ID);
        if (settings != null) {
            settings.setHideFromTemplate(!enable);
        }
    }
}
