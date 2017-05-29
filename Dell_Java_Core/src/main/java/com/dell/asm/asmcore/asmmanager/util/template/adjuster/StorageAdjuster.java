/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.template.adjuster;

import org.apache.commons.lang.StringUtils;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;

public class StorageAdjuster implements IComponentAdjuster {

    private static StorageAdjuster instance;

    /**
     * Private constructor
     */
    private StorageAdjuster() {

    }

    public static StorageAdjuster getInstance() {
        if (instance == null)
            instance = new StorageAdjuster();
        return instance;
    }

    @Override
    public void refine (ServiceTemplateComponent refineComponent, ServiceTemplate referencedTemplate) {
        refineStorageForSDRS(refineComponent, referencedTemplate);
    }

    /**
     * Update storage component with storage DRS option
     * @param storageComponent Storage Component ServiceTemplateComponent
     * @param referencedTemplate referenced ServiceTemplate
     */
    private void refineStorageForSDRS(ServiceTemplateComponent storageComponent, ServiceTemplate referencedTemplate) {
        ServiceTemplateComponent relatedVCenterComponent = ServiceTemplateUtil.getAssociatedClusterComponentFromStorage(storageComponent, referencedTemplate.fetchComponentsMap());
        if (relatedVCenterComponent != null) {
            for (String resourceID : ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_RESOURCE_LIST) {
                ServiceTemplateSetting addSDRS = storageComponent
                        .getParameter(resourceID,ServiceTemplateSettingIDs.SERVICE_TEMPLATE_STORAGE_ADD_TO_SDRS_ID);
                ServiceTemplateSetting sdrsConfig = relatedVCenterComponent
                        .getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_SDRS_ID);
                if (addSDRS != null && sdrsConfig != null && sdrsConfig.getValue().equals("true")) {
                    ServiceTemplateSetting sdrsName = relatedVCenterComponent
                            .getParameter(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_ESX_CLUSTER_COMP_ID, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_CLUSTER_CLUSTER_DSC_ID);
                    if (sdrsName != null && StringUtils.isNotBlank(sdrsName.getValue()))
                        addSDRS.getOptions().add(new ServiceTemplateOption(sdrsName.getValue(), sdrsName.getValue(), null, null));
                    addSDRS.setHideFromTemplate(false);
                }
            }
        }
    }


}
