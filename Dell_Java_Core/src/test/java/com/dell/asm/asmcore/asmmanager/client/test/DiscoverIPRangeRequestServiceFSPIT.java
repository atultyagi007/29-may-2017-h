/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.test;


import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.IDiscoverIPRangeDevicesService;


public class DiscoverIPRangeRequestServiceFSPIT
{
	
	 private static final String URL = TestUtil.ASM_URL;
	 
	 private static final  IDiscoverIPRangeDevicesService discoverIPserviceProxy = TestUtil.createProxyWithTestAuth(URL, IDiscoverIPRangeDevicesService.class);
	 
	
	 //Display the List of all Devices Inventory
	 @Test
	 public void retrieveDiscoveryResultByParentJobTestFilterByStatus() {
	     
		 //Populated the data with data before testing
		 // you can drop the table 'discovery_result' table and create & populate the data with run of DiscoveryResultFSPDAOIT JUnit test
		 String parentJobId = "Job-6f951902-1648-4c4f-a260-8c6a739869ae";
		 
	     // Sort descending by refid
	     String sort = "-refId";

	     //the filter need to be comma seperated with eq or contains
	     String[] filter = {"eq,status,CONNECTED"};
	     List<String> filterList = Arrays.asList(filter);

	     //Taken a limit of 10 for testing
	     List<DiscoveredDevices> discoveryJobResultsList = discoverIPserviceProxy.getDiscoveryRequest(parentJobId).getDevices();

	     assertTrue(discoveryJobResultsList.size() > 0);
	
	     // print discovered devices
	     int count = 0;
		 System.out.println("\n\n======================================================================================================");
		 System.out.println("==== Print All Device Discoved by Status 'Connected' ( considered Descending) for parent Job ID:  [ " + parentJobId + " ]");
		 System.out.println("=========================================================================================================");
	       
	     for (DiscoveredDevices result : discoveryJobResultsList) {
	         System.out.println(++count + "] " + result.getRefId() + " " + result.getModel()+ " " + result.getDeviceType() + "  "+ result.getStatus());
	     }
	
	 }
	 
	 
	 //Display the List of all Devices Inventory
	 @Test
	 public void retrieveDiscoveryResultByParentJobTestFilterByDeviceTypeAndStatus() {
	     
		 //Populated the data with data before testing
		 // you can drop the table 'discovery_result' table and create & populate the data with run of DiscoveryResultFSPDAOIT JUnit test
		 String parentJobId = "Job-6f951902-1648-4c4f-a260-8c6a739869ae";
		 
	     // Sort ascending by refid
	     String sort = "refId";

	     //the filter need to be comma seperated with eq or contains
	     String[] filter = {"eq,deviceType,chassis", "eq,status,CONNECTED"};
	     List<String> filterList = Arrays.asList(filter);

	     //Taken a limit of 10 for testing
	     List<DiscoveredDevices> discoveryJobResultsList = discoverIPserviceProxy.getDiscoveryRequest(parentJobId).getDevices();

	     assertTrue(discoveryJobResultsList.size() > 0);
	
	     // print discovered devices
	     int count = 0;
		 System.out.println("\n\n===================================================================================================================================================");
		 System.out.println("==== Print All Device Discoved by DeviceType 'chassis & 'Status 'Connected'(considered Ascending) for parent Job ID:  [ " + parentJobId + " ]");
		 System.out.println("==================================================================================================================================================");
	       
	     for (DiscoveredDevices result : discoveryJobResultsList) {
	         System.out.println(++count + "] " + result.getRefId() + " " + result.getModel()+ " " + result.getDeviceType() + "  "+ result.getStatus());
	     }
	
	 }
	 
	 
	 //Display the List of all Devices Inventory
	 @Test
	 public void retrieveDiscoveryResultByParentJobWithNoFilterNoSortWithOffsetAndLimit() {
	     
		 //Populated the data with data before testing
		 // you can drop the table 'discovery_result' table and create & populate the data with run of DiscoveryResultFSPDAOIT JUnit test
		 String parentJobId = "Job-6f951902-1648-4c4f-a260-8c6a739869ae";
		 
	     // Sort ascending by refid
	     String sort = "refId";
 
	     //the filter need to be comma seperated with eq or contains
	     String[] filter = {"eq,deviceType,chassis", "eq,status,CONNECTED"};
	     List<String> filterList = Arrays.asList(filter);

	     //Taken a limit of 10 for testing
	     List<DiscoveredDevices> discoveryJobResultsList = discoverIPserviceProxy.getDiscoveryRequest(parentJobId).getDevices();

	     assertTrue(discoveryJobResultsList.size() > 0);
	
	     // print discovered devices
	     int count = 0;
		 System.out.println("\n\n======================================================================================================");
		 System.out.println("==== Print All Device Discoved with otehrs null (considered Ascending) for parent Job ID:  [ " + parentJobId + " ]");
		 System.out.println("=========================================================================================================");
	       
	     for (DiscoveredDevices result : discoveryJobResultsList) {
	         System.out.println(++count + "] " + result.getRefId() + " " + result.getModel()+ " " + result.getDeviceType() + "  "+ result.getStatus());
	     }
	
	 }
	 
	 
	 //Display the List of all Devices Inventory
	 @Test
	 public void retrieveDiscoveryResultByParentJobWithAllOthersNull() {
	     
		 //Populated the data with data before testing
		 // you can drop the table 'discovery_result' table and create & populate the data with run of DiscoveryResultFSPDAOIT JUnit test
		 String parentJobId = "Job-6f951902-1648-4c4f-a260-8c6a739869ae";

	     //Taken a limit of 10 for testing
	     List<DiscoveredDevices> discoveryJobResultsList = discoverIPserviceProxy.getDiscoveryRequest(parentJobId).getDevices();

	     assertTrue(discoveryJobResultsList.size() > 0);
	
	     // print discovered devices
	     int count = 0;
		 System.out.println("\n\n======================================================================================================");
		 System.out.println("==== Print All Device Discoved (considered Ascending) for parent Job ID:  [ " + parentJobId + " ]");
		 System.out.println("=========================================================================================================");
	       
	     for (DiscoveredDevices result : discoveryJobResultsList) {
	         System.out.println(++count + "] " + result.getRefId() + " " + result.getModel()+ " " + result.getDeviceType() + "  "+ result.getStatus());
	     }
	
	 }

}
