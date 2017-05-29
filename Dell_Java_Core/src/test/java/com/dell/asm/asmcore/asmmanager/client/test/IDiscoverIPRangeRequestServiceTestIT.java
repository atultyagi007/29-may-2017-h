/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.client.discovery.IDiscoverIPRangeDevicesService;
import com.dell.asm.asmcore.asmmanager.client.discovery.JobStatus;
//import com.dell.pg.orion.jobmgr.JobManager;
//import com.dell.pg.orion.jobmgr.JobManagerException;
//import com.dell.pg.orion.jobmgr.JobStatus;



public class IDiscoverIPRangeRequestServiceTestIT
{
 private static final String URL = TestUtil.ASM_URL;
 private static final String URLCred= "http://localhost:9080/ChassisRA/chassis/credential";

 void testIpRangeRrquest(DiscoverIPRangeDeviceRequests requests)
 {
  IDiscoverIPRangeDevicesService service
    = TestUtil.createProxyWithTestAuth(URL, IDiscoverIPRangeDevicesService.class);

  
  DiscoveryRequest discoveryResult = new DiscoveryRequest();
  discoveryResult.setDiscoveryRequestList(requests);
  discoveryResult = service.deviceIPRangeDiscoveryRequest(discoveryResult);

   
 //		String status = null;
//		while (true) {
//			try {
//				status = JobManager.getInstance().getJobHistoryManager()
//						.getExecHistoryStatus(jobid);
//				System.out.println("status:" +status);
//			} catch (JobManagerException e) {
//				System.out.println(e.getMessage());
//				
//			}
//			if (JobStatus.isTerminal(status))
//				
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				System.out.println(e.getMessage());
//				
//			}
//		
//		}
 }
 
    //convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
	
 void testIpRangeMonitorJob(DiscoverIPRangeDeviceRequests requests)
 {
  IDiscoverIPRangeDevicesService service
    = TestUtil.createProxyWithTestAuth(URL, IDiscoverIPRangeDevicesService.class);

  // Create
  //JobStatus jobStatus = service.deviceIPRangeDiscoveryRequest(requests);
  String parentJobName = "Job-99163a29-bd24-4bce-8274-1ae9f36ef815"; //jobStatus.getJobName();
  
 
	  DiscoveryRequest result = service.getDiscoveryRequest(parentJobName);
	  System.out.println("parentJob Status " + result.getStatus());	  
	  System.out.println("parentJob Messages " + result.getStatusMessage());
	  if ( result.getStatus() == DiscoveryStatus.FAILED ||
			  result.getStatus() == DiscoveryStatus.SUCCESS ) {
		 
	  }
	  dumpDevice(result.getDevices());
	  
		
	
	
  
 }



	private void dumpDevice(List<DiscoveredDevices> devices) {
		if ( devices != null && !devices.isEmpty() ) {
			System.out.println(" devices size " + devices.size());
			for ( DiscoveredDevices device : devices ) {
				dumpDevice(device);
			}			
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
		System.out.println(" device refId " + device.getDeviceRefId());
		System.out.println(" s count " + device.getServerCount());
		System.out.println(" i count " + device.getIomCount());
		System.out.println(" status " + device.getStatus());
		System.out.println(" status message " + device.getStatusMessage());
	}
	

 @Test
 public void testIPCRUD2()
 {
	 
	 ASMIPDiscoveryRequestTest test = new ASMIPDiscoveryRequestTest();

	 DiscoverIPRangeDeviceRequests request = test.buildNamedDTO(UUID.randomUUID().toString());
	 testIpRangeRrquest(request);
 }
 
 @Test
 public void testIpRangeMonitorJob()
 {
	 
	 ASMIPDiscoveryRequestTest test = new ASMIPDiscoveryRequestTest();

	 DiscoverIPRangeDeviceRequests request = test.buildNamedDTO(UUID.randomUUID().toString());
	 testIpRangeMonitorJob(request);
 }


}
