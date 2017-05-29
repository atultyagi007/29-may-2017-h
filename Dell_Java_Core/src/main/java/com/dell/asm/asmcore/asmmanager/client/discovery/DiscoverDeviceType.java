/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.discovery;

/**
 * The DiscoverDeviceType is a type we detect on discovery stage. Used for puppet certificates.
 */
public enum DiscoverDeviceType {

    CMC("cmc", "script"),
    CMC_FX2("cmc", "script"),
    IDRAC7("idrac", "script"),
    IDRAC8("idrac", "script"),
    UNKNOWN(null, "https"),
    SERVER(null, "https"), // generic server?
    CSERVER(null, "https"),
    VRTX(null, "ssh"),
    VCENTER("vcenter", "script"),
    EQUALLOGIC("equallogic", "script"),
    COMPELLENT("compellent", "script"),
    FORCE10("dell_ftos", "script"),
    FORCE10_S4810("dell_ftos", "script"),
    FORCE10_S5000("dell_ftos", "script"),
    FORCE10_S6000("dell_ftos", "script"),
    FORCE10_S4048("dell_ftos", "script"),
    FORCE10IOM("dell_iom", "script"),
    FX2_IOM("dell_iom", "script"),
    DELL_IOM_84("iom_8x4", "script"),
    BROCADE("brocade_fos", "script"),
    POWERCONNECT("dell_powerconnect", "script"),
    POWERCONNECT_N3000("dell_powerconnect", "script"),
    POWERCONNECT_N4000("dell_powerconnect", "script"),
    NETAPP("netapp", "https"),
    CISCONEXUS("cisconexus5k", "script"),
    EQUALLOGIC_NODISCOVER("equallogic_nodiscover", "https"),
    SCVMM("scvmm", "script"),
    BMC("ipmi", "https"),
    EM("compellent", "script"),
    VNX("vnx", "script");

    private final String puppetModuleName;
    private final String connectType;

    private DiscoverDeviceType(String puppetModuleName, String connectType) {
        this.puppetModuleName = puppetModuleName;
        this.connectType = connectType;
    }

    public String getPuppetModuleName() {
        return puppetModuleName;
    }

    public String getConnectType() {
        return connectType;
    }

    public static DiscoverDeviceType fromPuppetModule(String puppetModuleName) {
        if (puppetModuleName==null) return null;
        for (DiscoverDeviceType deviceType : DiscoverDeviceType.values()) {
            if (deviceType.getPuppetModuleName()!=null && deviceType.getPuppetModuleName().equalsIgnoreCase(puppetModuleName))
                return deviceType;
        }
        return null;
    }

    public static DiscoverDeviceType fromName(String value) {
        for (DiscoverDeviceType deviceType : DiscoverDeviceType.values()) {
            if (deviceType.name().equals(value))
                return deviceType;
        }
        return null;
    }

    public static boolean isIOM(DiscoverDeviceType discoverDeviceType) {
        return discoverDeviceType != null &&
                (FORCE10IOM.equals(discoverDeviceType) ||
                        FORCE10IOM.equals(discoverDeviceType) ||
                        FX2_IOM.equals(discoverDeviceType) ||
                        DELL_IOM_84.equals(discoverDeviceType));
    }
}
