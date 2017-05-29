/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery;

import java.util.List;

public class DiscoveryProviders {

    public List<IDiscoveryProvider> getDiscoverableDevices() {
        return discoverableDevices;
    }

    public void setDiscoverableDevices(List<IDiscoveryProvider> discoverableDevices) {
        this.discoverableDevices = discoverableDevices;
    }

    private List<IDiscoveryProvider> discoverableDevices;
}
