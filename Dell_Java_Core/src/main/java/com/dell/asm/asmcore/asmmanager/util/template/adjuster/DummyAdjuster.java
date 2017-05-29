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

public class DummyAdjuster implements IComponentAdjuster {

    private static DummyAdjuster instance;

    /**
     * Private constructor.
     */
    private DummyAdjuster() {

    }

    public static DummyAdjuster getInstance() {
        if (instance == null)
            instance = new DummyAdjuster();
        return instance;
    }

    @Override
    public void refine (ServiceTemplateComponent refineComponent, ServiceTemplate referencedTemplate) {
        // Do nothing.
    }
}
