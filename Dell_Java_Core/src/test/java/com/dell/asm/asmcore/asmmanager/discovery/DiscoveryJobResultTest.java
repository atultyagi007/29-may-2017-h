package com.dell.asm.asmcore.asmmanager.discovery;


import static org.junit.Assert.*;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.pg.asm.server.app.rest.utils.NullAwareBeanUtil;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;

public class DiscoveryJobResultTest {

	@Test
	public void testDiscoveryJobResult() {
		try {
			DiscoveryResultEntity entity = buildResultEntity();
            DiscoveredDevices device = new DiscoveredDevices();
            NullAwareBeanUtil.getInstance().copyProperties(device, entity);
            
    		assertEquals(entity.getParentJobId(), "PID123455555");
    		assertEquals(entity.getJobId(), "JID123455555ABDC");
    		assertEquals(entity.getRefId(), "RefID1234");
    		assertEquals(entity.getRefType(), "refType1234");
    		assertEquals(entity.getIpaddress(), "ipaddress1.2.3.4");
    		assertEquals(entity.getServiceTag(), "STAG1234");
    		assertEquals(entity.getModel(), "Modelabcd");
    		assertEquals(entity.getDeviceType(), DeviceType.ChassisM1000e);
    		assertEquals(entity.getServerCount(), 100);
    		assertEquals(entity.getIomCount(),10);
    		assertEquals(entity.getStatus(),DiscoveryStatus.CONNECTED);
    		assertEquals(entity.getStatusMessage(), "good");
    		assertEquals(entity.getDiscoverDeviceType(), DiscoverDeviceType.CMC);
            //dumpDevice(device);
		}catch ( Exception e) {
			
		}
	}



	private void dumpDevice(DiscoveredDevices device) {
		System.out.println(" PID " + device.getParentJobId());
		System.out.println(" CPID " + device.getJobId());
		System.out.println(" RefId " + device.getRefId());
		System.out.println(" RefType " + device.getRefType());
		System.out.println(" ip " + device.getIpAddress());
		System.out.println(" STAG " + device.getServiceTag());
		System.out.println(" model " + device.getModel());
		System.out.println(" device type " + device.getDeviceType());
		System.out.println(" s count " + device.getServerCount());
		System.out.println(" i count " + device.getIomCount());
		System.out.println(" status " + device.getStatus());
		System.out.println(" status message " + device.getStatusMessage());
	}
	
	private DiscoveryResultEntity buildResultEntity() {
		DiscoveryResultEntity entity = new DiscoveryResultEntity();
		
		entity.setParentJobId("PID123455555");
		entity.setJobId("JID123455555ABDC");
		entity.setRefId("RefID1234");
		entity.setRefType("refType1234");
		entity.setIpaddress("ipaddress1.2.3.4");
		entity.setServiceTag("STAG1234");
		entity.setModel("Modelabcd");
		entity.setDeviceType(DeviceType.ChassisM1000e);
		entity.setServerCount(100);
		entity.setIomCount(10);
		entity.setStatus(DiscoveryStatus.CONNECTED);
		entity.setStatusMessage("good");
        entity.setDiscoverDeviceType(DiscoverDeviceType.CMC);
		return entity;
	}
}
