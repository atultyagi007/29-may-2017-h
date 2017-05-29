/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

/**
 * Represents the various states a proposed component in a Brownfield Service could exist. By default DeployedDevices
 * not used in a Brownfield flow will be set to the NOT_APPLICABLE state. <br>
 * <br>
 * Statues supported:
 * <ul>
 *   <li>{@link #NEWLY_AVAILABLE}
 *   <li>{@link #NOT_APPLICABLE}
 *   <li>{@link #AVAILABLE}
 *   <li>{@link #CURRENTLY_DEPLOYED_IN_BROWNFIELD}
 *   <li>{@link #NEWLY_AVAILABLE}
 *   <li>{@link #UNAVAILABLE_RELATED_SERVER_NOT_IN_INVENTORY}
 *   <li>{@link #UNAVAILABLE_RELATED_SERVER_IN_EXISTING_SERVICE}
 *   <li>{@link #UNAVAILABLE_RELATED_SERVER_NOT_MANAGED_OR_RESERVED}
 *   <li>{@link #UNAVAILABLE_NOT_IN_INVENTORY}
 *   <li>{@link #UNAVAILABLE_NOT_MANAGED_OR_RESERVED}
 *   <li>{@link #UNAVAILABLE_IN_EXISTING_SERVICE}
 *   <li>{@link #UNAVAILABLE_THIS_DEVICE_AND_RELATED_SERVER_NOT_IN_INVENTORY}
 *   <li>{@link #UNAVAILABLE_THIS_DEVICE_AND_RELATED_SERVER_NOT_MANAGED_OR_RESERVED}
 *   <li>{@link #UNAVAILABLE_THIS_DEVICE_NOT_MANAGED_OR_RESERVED_AND_RELATED_SERVER_NOT_IN_INVENTORY}
 *   <li>{@link #UNAVAILABLE_THIS_DEVICE_NOT_MANAGED_OR_RESERVED_AND_RELATED_SERVER_IN_EXISTING_SERVICE}
 *   <li>{@link #UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_NOT_MANAGED_OR_RESERVED}
 *   <li>{@link #UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_IN_EXISTING_SERVICE}
 * </ul>
 */
public enum BrownfieldStatus {

    // WARNING - Note the ordering of the BrownfieldStatus is important as it drives the ordering of components
    // in the Brownfield discovery flows / summary screen(s).
    
    /**
     * Means it's the Device is in inventory, but not part of an existing service, and available to update a service.
     */
    NEWLY_AVAILABLE(),
    /**
     * Means this is not a Brownfield device and the Brownfield Status is simply not applicable to the current flow.
     */
    NOT_APPLICABLE(),
    /**
     * Means it's in inventory, but the server it's attached to is not available.
     */
    UNAVAILABLE_RELATED_SERVER_NOT_IN_INVENTORY(),
    /**
     * Means it's in inventory, but the server it's attached to is not available as it's in use in an existing service.
     */
    UNAVAILABLE_RELATED_SERVER_IN_EXISTING_SERVICE(),
    /**
     * Means the server it's attached to is not not available as it's not in a Managed or Reserved state.
     */
    UNAVAILABLE_RELATED_SERVER_NOT_MANAGED_OR_RESERVED(),
    /**
     * Means the device is not in inventory and not available for use in a Brownfield service.
     */
    UNAVAILABLE_NOT_IN_INVENTORY(),
    /**
     * Means the device is in Inventory but is not in a managed state or reserved state. 
     */
    UNAVAILABLE_NOT_MANAGED_OR_RESERVED(), 
    /**
     * Means the device is currently in use on a different service and thus not available for use in another Service.
     */
    UNAVAILABLE_IN_EXISTING_SERVICE(),
    /**
     * Primarily for storage, means the device is not in inventory and the attached server is not in inventory as well.
     */
    UNAVAILABLE_THIS_DEVICE_AND_RELATED_SERVER_NOT_IN_INVENTORY(),
    /**
     * Means the both the device and the server it's attached to are not marked as Managed or Reserved.
     */
    UNAVAILABLE_THIS_DEVICE_AND_RELATED_SERVER_NOT_MANAGED_OR_RESERVED(),
    /**
     * Means this device is not marked as Managed or Reserved and the server it's attached to is not in Inventory
     */
    UNAVAILABLE_THIS_DEVICE_NOT_MANAGED_OR_RESERVED_AND_RELATED_SERVER_NOT_IN_INVENTORY(),
    /**
     * Means the device is not marked as Managed or Reserved and the server it's attached to is in an existing service.
     */
    UNAVAILABLE_THIS_DEVICE_NOT_MANAGED_OR_RESERVED_AND_RELATED_SERVER_IN_EXISTING_SERVICE(),
    /**
     * Means the device is not in Inventory and the server it's attached to is not marked as Managed or Reserved.
     */
    UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_NOT_MANAGED_OR_RESERVED(),
    /**
     * Primarily for storage, means the device is not in inventory and the attached server is in an existing service.
     */
    UNAVAILABLE_THIS_DEVICE_NOT_IN_INVENTORY_AND_RELATED_SERVER_IN_EXISTING_SERVICE(),
    /**
     * Means it's currently in inventory and available for use when creating a completely new Brownfield Service.
     */
    AVAILABLE(),
    /**
     * Means it's currently deployed as part of an existing Brownfield service (typically service being displayed).
     */
    CURRENTLY_DEPLOYED_IN_BROWNFIELD(); 
    
    private int id;
 
    // Default constructor for the class.
    private BrownfieldStatus() {}
    
    /**
     * Returns true if the Attached device (assuming attached device is THIS BrownfieldStatus) is available or false
     * if it is not available.
     * 
     * @return true if the Attached device (assuming attached device is THIS BrownfieldStatus) is available or false
     *      if it is not available.
     */
    public boolean isAttachedDeviceAvailable() {
        return (this.equals(AVAILABLE) || 
                this.equals(UNAVAILABLE_RELATED_SERVER_NOT_IN_INVENTORY) ||
                this.equals(UNAVAILABLE_RELATED_SERVER_IN_EXISTING_SERVICE) || 
                this.equals(UNAVAILABLE_RELATED_SERVER_NOT_MANAGED_OR_RESERVED));
    }
    
}
