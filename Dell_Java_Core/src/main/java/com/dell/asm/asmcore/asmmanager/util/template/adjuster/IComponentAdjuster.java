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

/**
 * Refine component (storage, cluster server etc) by adjusting it's settings - hide/show,
 * make required/ optional.
 * It looks up for related components in referenced template and makes appropriate changes
 * in target component.
 * For example, if ISCSI storage is connected to Server, enable Server ISCSI Initiator settings.
 * For VM - set DC and Cluster selection options based on connected Cluster.
 */
public interface IComponentAdjuster {
    void refine (ServiceTemplateComponent refineComponent, ServiceTemplate referencedTemplate);
}
