/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

public class NicFQDD {
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    private String prefix;
    private String locator;
    private String card;
    private String port;

    public String getPartition() {
        return partition;
    }

    private String partition;

    public static String LOCATOR_CHASSIS_SLOT = "ChassisSlot";
    public static String LOCATOR_INTEGRATED = "Integrated";
    public static String LOCATOR_EMBEDDED = "Embedded";

    public NicFQDD(String fqdd) {
        String[] fqdds = fqdd.trim().split("\\.");
        prefix = fqdds[0];
        locator = fqdds[1];
        String cardPortPartition = fqdds[2];
        if (cardPortPartition.contains("-")) {
            String[] arr = cardPortPartition.split("-");
            card = arr[0];
            port = arr[1];
            if (arr.length == 3) {
                partition = arr[2];
            }
        }else{
            card = cardPortPartition;
            port = "";
        }

        // special case for Embedded NICs like X520
        if (locator.equals(LOCATOR_EMBEDDED)) {
            port = card;
            card = "1";
        }
    }

    public boolean isChassisSlot() {
        return LOCATOR_CHASSIS_SLOT.equals(locator);
    }

    public boolean isIntegarted() {
        return LOCATOR_INTEGRATED.equals(locator);
    }

    @Override
    public String toString() {
        return (partition != null) ?
                getPrefix() + "." + getLocator() + "." + getCard() + "-" + getPort() + "-" + getPartition():
                getPrefix() + "." + getLocator() + "." + getCard() + "-" + getPort();
    }

    public String getCardKey() {
        return prefix + "." + locator + "." + card;
    }
}