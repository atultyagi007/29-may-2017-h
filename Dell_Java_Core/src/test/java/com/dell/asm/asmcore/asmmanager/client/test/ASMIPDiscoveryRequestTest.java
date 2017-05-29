/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;


import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;


public class ASMIPDiscoveryRequestTest extends AbstractMarshalTest<DiscoverIPRangeDeviceRequests> {
    public ASMIPDiscoveryRequestTest() {
        super(DiscoverIPRangeDeviceRequests.class);
    }

    public static DiscoverIPRangeDeviceRequests buildDTO(String refId) {
        return new DiscoverIPRangeDeviceRequests(createDiscoveryRequestList());
    }

    @SuppressWarnings("unchecked")
    public static Set<DiscoverIPRangeDeviceRequest> createDiscoveryRequestListValidIps() {

        Set<DiscoverIPRangeDeviceRequest> reqs = new HashSet();
        DiscoverIPRangeDeviceRequest r1 = new DiscoverIPRangeDeviceRequest();
        r1.setDeviceChassisCredRef("8a52f0064171d5d4014171d602540002");
        r1.setDeviceServerCredRef("8a52f0064171d5d4014171d64dc8000a");
        r1.setDeviceEndIp("192.168.113.24");
        r1.setDeviceStartIp("192.168.113.19");
        reqs.add(r1);

        return reqs;
    }

    @SuppressWarnings("unchecked")
    public static Set<DiscoverIPRangeDeviceRequest> createDiscoveryRequestListInValidIps() {

        Set<DiscoverIPRangeDeviceRequest> reqs = new HashSet();
        DiscoverIPRangeDeviceRequest r1 = new DiscoverIPRangeDeviceRequest();
        r1.setDeviceChassisCredRef("8a52f0064171d5d4014171d602540002");
        r1.setDeviceServerCredRef("8a52f0064171d5d4014171d64dc8000a");
        // r1.setDeviceCredRef("8a00a9ad410e6eeb01410e6eee65xxx");
        r1.setDeviceEndIp("192.168.113.20");
        r1.setDeviceStartIp("192.168.113.19");
        reqs.add(r1);

        return reqs;
    }

    @SuppressWarnings("unchecked")
    public static Set<DiscoverIPRangeDeviceRequest> createDiscoveryRequestList() {

        Set<DiscoverIPRangeDeviceRequest> reqs = new HashSet();
//        DiscoverIPRangeDeviceRequest r1 = new DiscoverIPRangeDeviceRequest();
//        r1.setDeviceChassisCredRef("8a90620a426c571901426c572d510000");
//        r1.setDeviceServerCredRef("8a90620a426c571901426c5772970002");
//        // r1.setDeviceEndIp("192.168.113.20");
//        r1.setDeviceStartIp("192.168.113.20");
//        reqs.add(r1);

        DiscoverIPRangeDeviceRequest r2 = new DiscoverIPRangeDeviceRequest();
        r2.setDeviceChassisCredRef("8a90620a426c571901426c572d510000");
        r2.setDeviceServerCredRef("8a90620a426c571901426c5772970002");
        r2.setDeviceStartIp("192.168.76.22");
       // r2.setDeviceEndIp("192.168.76.25");
        reqs.add(r2);

        return reqs;
    }

    @Override
    DiscoverIPRangeDeviceRequests buildNamedDTO(String refId) {

        DiscoverIPRangeDeviceRequests ret = buildDTO(refId);


        return ret;

    }
}
