package com.dell.asm.asmcore.asmmanager.util.integration;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.util.discovery.DiscoveryJobUtils;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IChassisService;

public class ChassisProxyTest {

    @Test
    public void testAllWithChassis() {

        IChassisService chassisDeviceProxy = ProxyUtil.getDeviceChassisProxyWithHeaderSet();

        // --------------- Create a device.
        Chassis dev1 = ChassisDeviceMockUtils.makeChassisDeviceForTest();
        Chassis dev1Created = chassisDeviceProxy.createChassis(dev1);
        Response response = WebClient.client(chassisDeviceProxy).getResponse();
        assertNotNull("Unable to create Chassis device", dev1Created);
        assertNotNull("Unable to create Chassis device", response);
        assertNotNull("Unable to create Chassis device", dev1Created.getRefId());
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        
        Chassis chassis = chassisDeviceProxy.getChassis(dev1Created.getRefId());
        
        DiscoveredDevices discoveredDevices = new DiscoveredDevices();
        discoveredDevices.setJobId(UUID.randomUUID().toString());
        discoveredDevices.setStatus(DiscoveryStatus.CONNECTED);
        discoveredDevices.setDeviceType(DeviceType.ChassisM1000e);
        discoveredDevices.setModel(chassis.getModel());
        discoveredDevices.setServiceTag(chassis.getServiceTag());
        System.out.println("chassis servercount: "+chassis.getServerCount());
        discoveredDevices.setServerCount(chassis.getServerCount());
        discoveredDevices.setIomCount(chassis.getIomCount());
        discoveredDevices.setServerType( "");
        System.out.println("chassis IOMcount: "+chassis.getIomCount() + " :entity value:" +discoveredDevices.getIomCount());
        discoveredDevices.setIpAddress(chassis.getManagementIP());
        discoveredDevices.setParentJobId(UUID.randomUUID().toString());
        discoveredDevices.setRefId(chassis.getRefId());
        discoveredDevices.setRefType(chassis.getRefType());
        discoveredDevices.setDiscoverDeviceType(DiscoverDeviceType.CMC);
        
        DiscoveryJobUtils.updateDiscoveryResult(discoveredDevices);

    }
}
