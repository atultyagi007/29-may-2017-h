package com.dell.asm.asmcore.asmmanager.util;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;

public class DiscoveryJobUtilTestIT {

    @Test
    public void testValidateForOverlappingRanges() {
        DiscoverIPRangeDeviceRequests ipranges = createDiscoveryRequestList();

        try {
            com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils.validateDiscoveryRequest(createDiscoveryRequestList());
        }catch (LocalizedWebApplicationException e) {
            System.out.println("get message list" +e.getEEMILocalizedMessageList());
            assertNotNull("Exception", e.getEEMILocalizedMessageList());
        }

    }
    
    public static DiscoverIPRangeDeviceRequests createDiscoveryRequestList() {

        Set<DiscoverIPRangeDeviceRequest> reqs = new HashSet<DiscoverIPRangeDeviceRequest>();
        DiscoverIPRangeDeviceRequest r1 = new DiscoverIPRangeDeviceRequest();
        r1.setDeviceStartIp("10.128.76.14");
       
        r1.setDeviceChassisCredRef("8a90620a426c571901426c572d510000");
        r1.setDeviceServerCredRef("8a90620a426c571901426c5772970002");
        //r1.setDeviceEndIp("10.128.76.17");
        
        reqs.add(r1);
        DiscoverIPRangeDeviceRequest r2 = new DiscoverIPRangeDeviceRequest();
        r2.setDeviceStartIp("10.128.76.13");
        r2.setDeviceChassisCredRef("8a90620a426c571901426c572d510000");
        r2.setDeviceServerCredRef("8a90620a426c571901426c5772970002");
       // r2.setDeviceEndIp("10.128.76.15");
      
        
 
        reqs.add(r2);
        DiscoverIPRangeDeviceRequests ret = new DiscoverIPRangeDeviceRequests(reqs);
        return ret;
    }

}
