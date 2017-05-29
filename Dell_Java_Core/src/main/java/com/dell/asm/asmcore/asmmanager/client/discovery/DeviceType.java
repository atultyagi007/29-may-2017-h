/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The DeviceType here is defined to be used by UI, which is different from IDeviceType, that
 * each RA defines ("chassis", "server", "iom").
 * Mapping utilities between this DeviceType and IDeviceType is provided by DeviceTypeUtils.java. 
 *
 */
public enum DeviceType {
	
    // careful with label - it must be the same as in UI for correct sorting
	
    Chassis("Chassis", false, false, true), // Chassis
    Server("Server", false, false, true), // Server
    TOR("Switch", false, false, false), // Switch - Top of Rack
    ChassisM1000e("Chassis", false, false, true), // Chassis
    ChassisVRTX("Chassis", false, false, false), // Chassis
    RackServer("Rack Server", false, true, true), // Server
    BladeServer("Blade Server", false, true, true), // Server
    AggregatorIOM("Switch", false, false, true), // Switch
    MXLIOM("Switch", false, false, true), // Switch
    storage("Storage Group", true, false, false), // Storage
    compellent("Storage Group", true, true, true), // Storage
    equallogic("Storage Group", true, true, true), // Storage
    genericswitch("Switch", false, false, false), // Switch
    dellswitch("Switch", false, false, true), // Switch
    vcenter("VM Manager", true, false, false), // VM Manager
    vm("Virtual Machine", false, false, false), // Virtual Machine
    netapp("Storage Group", true, false, false), // Storage
    scvmm("VM Manager", true, false, false), // VM Manager
    unknown("Unknown", false, false, false),
    bmc("Server", false, true, true), // Dell Server
    ChassisFX("Chassis", false, false, true), // Chassis
    FXServer("FX Server", false, true, true), // Server
    em("EM", false, false, false), // Used to monitor storage
    TowerServer("Tower Server", false, true, true),
    emcvnx("Storage Group", true, true, true); // EMC Storage

    private String _label;
    private boolean calculatedForResourceHealth;
    private boolean isFirmwareComplianceManaged;
    private boolean isSharedDevice;

    private DeviceType(String label, 
                       boolean isSharedDevice, 
                       boolean calculatedForResourceHealth, 
                       boolean isFirmwareComplianceManaged) {
        _label = label;
        this.isSharedDevice = isSharedDevice;
        this.calculatedForResourceHealth = calculatedForResourceHealth;
        this.isFirmwareComplianceManaged = isFirmwareComplianceManaged;
    }

    public String getLabel() {
        return _label;
    }

    public String getValue() {
        return name();
    }

    @Override
    public String toString() {
        return _label;
    }
    
    /**
     * Returns a boolean indicating whether a device may be shared between Services at any given point in time.
     * @return a boolean indicating whether a device may be shared between Services at any given point in time. 
     */
    
    public boolean isSharedDevice() {
        return isSharedDevice;
    }


    /**
     * Indicates whether ASM manages the compliance status for the given Device type.
     */
    public boolean isFirmwareComplianceManaged() {
        return isFirmwareComplianceManaged;
    }

    public boolean isCalculatedForResourceHealth() {
        return calculatedForResourceHealth;
    }

    public void setCalculatedForResourceHealth(boolean calculatedForResourceHealth) {
        this.calculatedForResourceHealth = calculatedForResourceHealth;
    }

    /**
     * returns a boolean indicating if it's a Server.  Shortcut for calling the isServer(DeviceType) method.
     */
    public boolean isServer(){
        return DeviceType.isServer(this);
    }

    /**
     *  Check if the device_type is any kind of server.
     */
    public static boolean isServer(DeviceType device_type) {
        for (DeviceType serverType : getAllServers()) {
            if (serverType.equals(device_type)) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Check if the device_type is any kind of switch.
     */
    public static boolean isSwitch(DeviceType device_type) {
        for (DeviceType serverType : getAllSwitches()) {
            if (serverType.equals(device_type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a boolean indicating if the DeviceType is a Storage.
     * @return a boolean indicating if the DeviceType is a Storage.
     */
    public boolean isStorage() {
        for (DeviceType storageType : getAllStorage()) {
            if (storageType.equals(this)) {
                return true;
            }
        }
        if (DeviceType.storage == this) {
            return true;
        }
        
        return false;
    }
    
    
    /**
     *  Check if the device_type is any kind of storage.
     */
    public static boolean isStorage(DeviceType device_type) {
        for (DeviceType serverType : getAllStorage()) {
            if (serverType.equals(device_type)) {
                return true;
            }
        }
        if (device_type==DeviceType.storage) {
            return true;
        }

        return false;
    }

    /**
     * Returns all of the DeviceTypes that are Servers.
     * @return a List of DeviceTypes that are Servers.
     */
    public static List<DeviceType> getAllServers() {
        return Arrays.asList(Server, RackServer, BladeServer, FXServer, TowerServer);
    }
    
    /**
     * Returns all of the DeviceTypes that are Switches.
     * @return a List of DeviceTypes that are Switches.
     */
    public static List<DeviceType> getAllSwitches() {
    	List<DeviceType> allSwitches = new ArrayList<>();
    	allSwitches.add(TOR);
    	allSwitches.add(AggregatorIOM);
    	allSwitches.add(MXLIOM);
    	allSwitches.add(genericswitch);
    	allSwitches.add(dellswitch);
   
    	return allSwitches;
    }
    
    /**
     * Returns all of the DeviceTypes that are Chassis.
     * @return a List of DeviceTypes that are Chassis.
     */
    public static List<DeviceType> getAllChassis()
    {
    	List<DeviceType> allChassis = new ArrayList<>();
//    	allChassis.add(Chassis);
    	allChassis.add(ChassisM1000e);
    	allChassis.add(ChassisFX);
    	allChassis.add(ChassisVRTX);
    	
    	return allChassis;
    }
    
    /**
     * Returns all of the DeviceTypes that are Storage.
     * @return a List of DeviceTypes that are Storage.
     */
    public static List<DeviceType> getAllStorage() {
    	List <DeviceType> allStorage = new ArrayList<>();
    	//allStorage.add(storage);
    	allStorage.add(compellent);
    	allStorage.add(equallogic);
    	allStorage.add(netapp);
    	allStorage.add(emcvnx);
    	
    	return allStorage;
    }
    
    public static boolean isBlade(DeviceType device_type) {
        return device_type == BladeServer || device_type == FXServer;
    }

    public static boolean isRAServer (DeviceType device_type) {
        return device_type == BladeServer || device_type == FXServer || device_type == RackServer || device_type == TowerServer;
    }

    public static boolean isChassis(DeviceType device_type) {
        return device_type != null && isChassis(device_type.getValue());
    }

    /**
    * Returns a boolean indicating if the given DeviceType is a VCenter.
    * 
    * @param device_type to be checked if it's a VCenter
    * @return a boolean indicating if the given DeviceType is a VCenter.
    */
    public static boolean isVcenter(DeviceType device_type) {
    	return device_type != null && device_type == vcenter;
    }

    public static boolean isSCVMM(DeviceType device_type) {
        return device_type != null && device_type == scvmm;
    }

    public static boolean isChassis(String certname) {
        if (certname == null) return false;
        String deviceType = certname;

        if (certname.contains("-")) {
            String[] arr = certname.split("-");
            DiscoverDeviceType ddType = DiscoverDeviceType.fromPuppetModule(arr[0]);
            if (ddType != null) {
                return ddType == DiscoverDeviceType.CMC || ddType == DiscoverDeviceType.CMC_FX2 || ddType == DiscoverDeviceType.VRTX;
            }
            else
                deviceType = arr[0];
        }

        return (deviceType.equalsIgnoreCase(ChassisM1000e.getValue()) ||
                deviceType.equalsIgnoreCase(ChassisVRTX.getValue()) ||
                deviceType.equalsIgnoreCase(ChassisFX.getValue()) ||
                deviceType.equalsIgnoreCase(Chassis.getValue()));
    }

}
