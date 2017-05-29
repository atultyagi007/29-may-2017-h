/**************************************************************************
 *   Copyright (c) 2013 - 2016 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;

public enum DeviceState {
    
    /**
     * State when a device is not busy as part of an update or deployment.
     * Also the default state when a device is first discovered.
     */
    READY("Ready"),
    
    /**
     * State the device goes in when the Firmware is being upgraded.
     */
    UPDATING("Updating"),

    /**
     * State the device goes in when the Firmware upgrade failed.
     */
    UPDATE_FAILED("Update Failed"),

    /**
     * State the device is in when there is an error during the discovery process or when updating the firmware.
     */
    CONFIGURATION_ERROR("Configuration Error"),
    
    PENDING_CONFIGURATION_TEMPLATE("Pending Configuration Template"),
    
    /**
     * State the device goes into prior to being deleted and removed from the device inventory.
     */
    PENDING_DELETE("Pending Delete"),
    
    PENDING("Pending"),
    DISCOVERY_FAILED("Discovery Failed"),
    
    /**
     * State a device is in when it is unable to deploy as part of a service successfully.
     */
    DEPLOYMENT_ERROR("Deployment Error"),
    
    DELETE_FAILED("Delete Failed"),
    DELETED("Deleted"),
    DEPLOYED("Deployed"),
    DEPLOYING("Deploying");
    
    private String _label;
    
    private DeviceState(String label){_label = label;}
    
    public String getLabel(){return _label;}
       
    public String getValue(){return name();}
       
    @Override
    public String toString(){return _label;}

    public static DeviceState fromDeploymentStatusType(DeploymentStatusType status) {
        switch (status) {
            case PENDING:
                return DeviceState.PENDING;
            case IN_PROGRESS:
                return DeviceState.DEPLOYING;
             // the deployment state can only change to UPDATING only from COMPLETE, we don't want servers change
            case FIRMWARE_UPDATING:    
            case COMPLETE:
                return DeviceState.DEPLOYED;
            case CANCELLED:
            case ERROR:
            default:
                return DeviceState.DEPLOYMENT_ERROR;
        }
    }
    
    /**
     * Returns true if the status indicates it's available for a deployment, or false if the status indicates
     * it's not available for a deployment.  
     *  
     * @return true if the status indicates it's available for a deployment, or false if the status indicates
     *      it's not available for a deployment.  
     */
    public boolean isAvailable() {
        return (this.equals(READY));
    }


    /**
     * Returns true if the status indicates the device is in an existing service, or false if the status indicates
     * it's not in an existing service.
     * 
     * @return true if the status indicates the device is in an existing service, or false if the status indicates
     *      it's not in an existing service.
     */
    public boolean isInExistingService() {
        return (this.equals(PENDING) || this.equals(DEPLOYED) || this.equals(DEPLOYING));
    }
    
    
}
