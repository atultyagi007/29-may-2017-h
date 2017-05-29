/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.template.adjuster;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;

public class AdjusterFactory {

    public static IComponentAdjuster getRefiner(ServiceTemplateComponent.ServiceTemplateComponentType type) {
        if (type == null)
            throw new IllegalArgumentException("Null is not allowed for Refiner factory");

        switch (type) {
            case SERVER:
                return ServerAdjuster.getInstance();
            case CLUSTER:
                return ClusterAdjuster.getInstance();
            case VIRTUALMACHINE:
                return VMAdjuster.getInstance();
            case STORAGE:
                return StorageAdjuster.getInstance();
            default:
                return DummyAdjuster.getInstance();
        }
    }
}
