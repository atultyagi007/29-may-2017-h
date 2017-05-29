/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;

public interface IDiscoveryProvider {
    /**
     * Discovery time device type. One discovery device type can be matched to several invemtory device types, i.e.
     * IDRAC7 -&gt; BladeServer, RackServer. Often it is impossible to tell device type by ping response, need
     * more complex logic i.e. look up the parent device type (M1000 chassis, FX2 chassis, no parent device)
     * @return
     */
    DiscoverDeviceType getDiscoverDeviceType();
    void setDiscoverDeviceType(DiscoverDeviceType deviceType);

    /**
     * Some device require firmware component ID
     * @return
     */
    String getFirmwareComponentID();
    void setFirmwareComponentID(String firmwareComponentID);

    /**
     * Inventory device type.
     * @return
     */
    public DeviceType getInventoryDeviceType();
    public void setInventoryDeviceType(DeviceType inventoryDeviceType);

    /**
     * Communicate with device, gather facts, save device in the private repository if applicable (Server, Chassis, VCenter)
     * @param device
     * @return
     */
    DiscoveredDevices discoverDevices(InfrastructureDevice device);
}
