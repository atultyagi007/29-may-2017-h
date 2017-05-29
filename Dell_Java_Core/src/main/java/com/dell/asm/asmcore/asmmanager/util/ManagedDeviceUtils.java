package com.dell.asm.asmcore.asmmanager.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;

public class ManagedDeviceUtils {

    public static HashMap<String, Boolean> deviceConfigMap = new HashMap<String, Boolean>();
    private final static int MAX_RETRY = 24 * 60 * 60;
    private final static Logger log = Logger.getLogger(ManagedDeviceUtils.class);

    public static boolean IsDeviceInProgress(DeviceState state) {
        if (state == DeviceState.PENDING_CONFIGURATION_TEMPLATE
         || state == DeviceState.DEPLOYING
         || state == DeviceState.PENDING_DELETE
        // || state == DeviceState.PENDING_POWER_ON
        // || state == DeviceState.PENDING_POWER_OFF
        // || state == DeviceState.PENDING_DETACH
        // || state == DeviceState.PENDING_DISCOVERY
        // || state == DeviceState.PENDING_INVENTORY
        // || state == DeviceState.PENDING_POWER_OFF
        // || state == DeviceState.PENDING_POWER_ON
        // || state == DeviceState.PENDING_REMOVE
        ) {
            return true;
        }
        return false;
    }

    public static boolean IsDeviceInErrorState(ManagedDevice d) {
        boolean bReturnValue = false;
        if (d != null && d.getState() != null) {
            if (d.getState() == DeviceState.CONFIGURATION_ERROR || d.getState() == DeviceState.DEPLOYMENT_ERROR) {
                bReturnValue = true;
            }
        }
        return bReturnValue;
    }

    public static boolean IsDeviceOKToOnBoard(DeviceState state) {
        if (state == DeviceState.READY) {
            return true;
        }
        if (state == DeviceState.PENDING_CONFIGURATION_TEMPLATE) {
            return true;
        }
        if (state == DeviceState.CONFIGURATION_ERROR) {
            return true;
        }
        return false;
    }

    /**
     * lock the device 
     * 
     * @param deviceGuid refId
     */
    public static void lockDevice(String deviceGuid) {
        int retry = 0;
        boolean bLocked = false;
        while (retry <= MAX_RETRY) {
            synchronized (deviceConfigMap) {
                if (!deviceConfigMap.containsKey(deviceGuid)) {
                    deviceConfigMap.put(deviceGuid, true);
                    bLocked = true;
                }
            }

            if (bLocked == false) {
                try {
                    log.info("Object " + deviceGuid + " is locked. Waiting for it to be unlocked.");
                    Thread.sleep(1000 * 60);
                } catch (Exception e) {
                }
                retry++;
            } else {
                break;
            }
        }
        // after 24 hours, continue with a new lock. Do not throw an exception.

    }

    /**
     * unlock
     * 
     * @param deviceGuid refId
     */
    public static void unlockDevice(String deviceGuid) {
        synchronized (deviceConfigMap) {
            if (deviceConfigMap.containsKey(deviceGuid)) {
                deviceConfigMap.remove(deviceGuid);
            }
        }
    }

    /**
     * check if locked 
     * @param deviceGuid refid
     * @return true if locked
     */
    public static synchronized boolean isDeviceLocked(String deviceGuid) {
        boolean bLocked = false;

        if (deviceConfigMap.containsKey(deviceGuid)) {
            bLocked = deviceConfigMap.get(deviceGuid).booleanValue();
        }
        return bLocked;
    }

}
