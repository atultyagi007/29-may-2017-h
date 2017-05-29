package com.dell.asm.asmcore.asmmanager.util;

import junit.framework.Assert;

import org.junit.Test;

public class ManagedDeviceUtilsTest {

    public static String deviceGuid="bbbBCD1234";

    @Test
    public void testLockDevice() {
        ManagedDeviceUtils.lockDevice(deviceGuid);
        Assert.assertEquals(ManagedDeviceUtils.isDeviceLocked(deviceGuid), true);
        ManagedDeviceUtils.unlockDevice(deviceGuid);
        Assert.assertEquals(ManagedDeviceUtils.isDeviceLocked(deviceGuid), false);
    }
}
