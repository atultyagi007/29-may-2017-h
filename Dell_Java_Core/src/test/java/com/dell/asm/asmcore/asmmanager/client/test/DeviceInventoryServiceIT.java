/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/

package com.dell.asm.asmcore.asmmanager.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.dell.asm.rest.common.util.RestUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.IDeviceInventoryService;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.i18n2.AsmDetailedMessage;
import com.dell.asm.i18n2.AsmDetailedMessageList;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.IChassisService;

public class DeviceInventoryServiceIT {

    private IDeviceInventoryService deviceInventoryProxy = TestUtil.createProxyWithTestAuth(TestUtil.ASM_URL, IDeviceInventoryService.class);

    private IChassisService chassisDeviceProxy = TestUtil.createProxyWithTestAuth(TestUtil.CHASSIS_RA_URL, IChassisService.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateDeleteDeviceInventoryWithChassis() {
        // --------------- Create a device.
        Chassis dev1 = new Chassis();
        dev1.setDisplayName("rest dev1");
        dev1.setRefType("chassisRefType");
        dev1.setServiceTag("test");
        dev1.setManagementIP("11.111.2.3");
        dev1.setModel("test");
        Chassis dev1Created = chassisDeviceProxy.createChassis(dev1);
        Response response = TestUtil.getResponse(chassisDeviceProxy);

        ManagedDevice deviceInventory = new ManagedDevice();
        deviceInventory.setRefId(dev1Created.getRefId());
        deviceInventory.setIpAddress("192.168.11.123");
        deviceInventory.setModel("IOA");
        deviceInventory.setDeviceType(DeviceType.ChassisM1000e);
        deviceInventory.setRefType("none");
        deviceInventory.setState(DeviceState.READY);
        deviceInventory.setServiceTag("IOM");
        deviceInventory.setDiscoveredDate(new GregorianCalendar());
        List<ManagedDevice> listOfDevices = new ArrayList<ManagedDevice>();
        listOfDevices.add(deviceInventory);
        deviceInventoryProxy.createDeviceInventory(listOfDevices.toArray(new ManagedDevice[listOfDevices.size()]));
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        ManagedDevice result = deviceInventoryProxy.getDeviceInventory(dev1Created.getRefId());
        assertEquals(DeviceType.ChassisM1000e, result.getDeviceType());
        assertEquals(DeviceState.READY, result.getState());

        deviceInventoryProxy.deleteDeviceInventory(dev1.getRefId(),false);
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

    }

    @Test
    public void testCreateDeviceInventoryWithDupServiceTag() {
        try {
            deviceInventoryProxy.getDeviceInventory("4");
        } catch (WebApplicationException swae) {
            Response response = TestUtil.getResponse(deviceInventoryProxy);
            if (response.getStatus() != Response.Status.NOT_FOUND.getStatusCode()) {
                deviceInventoryProxy.deleteDeviceInventory("4",false);
            }
        }
        ManagedDevice deviceInventory = new ManagedDevice();
        deviceInventory.setRefId("4");
        deviceInventory.setIpAddress("192.168.11.123");
        deviceInventory.setModel("IOA");
        deviceInventory.setDeviceType(DeviceType.ChassisM1000e);
        deviceInventory.setRefType("none");
        deviceInventory.setState(DeviceState.READY);
        deviceInventory.setServiceTag("test-1");
        List<ManagedDevice> listOfDevices = new ArrayList<ManagedDevice>();
        listOfDevices.add(deviceInventory);
        deviceInventoryProxy.createDeviceInventory(listOfDevices.toArray(new ManagedDevice[listOfDevices.size()]));
        Response response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(DeviceState.READY, deviceInventoryProxy.getDeviceInventory("4").getState());
        deviceInventory = new ManagedDevice();
        deviceInventory.setRefId("5");
        deviceInventory.setIpAddress("192.168.11.123");
        deviceInventory.setModel("IOA");
        deviceInventory.setDeviceType(DeviceType.ChassisM1000e);
        deviceInventory.setRefType("none");
        deviceInventory.setState(DeviceState.CONFIGURATION_ERROR);
        deviceInventory.setServiceTag("test-1");
        try {
            listOfDevices = new ArrayList<ManagedDevice>();
            listOfDevices.add(deviceInventory);
            deviceInventoryProxy.createDeviceInventory(listOfDevices.toArray(new ManagedDevice[listOfDevices.size()]));
            response = TestUtil.getResponse(deviceInventoryProxy);
            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            Object responseEntity = response.getEntity();
            assertNotNull("Response entity was null", responseEntity);
        } catch (WebApplicationException e) {
            Client client = TestUtil.getClient(deviceInventoryProxy);
            // response = TestUtil.getResponse(deviceInventoryProxy);
            // assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            // Object responseEntity = response.getEntity();
            // assertNotNull("Response entity was null", responseEntity);

            AsmDetailedMessageList errors = RestUtil.unwrapExceptionToAsmDetailedMessageList(e);

            AsmDetailedMessage asmDetailedMessage = errors.getMessages().get(0);
            assertTrue(asmDetailedMessage.getDisplayMessage().indexOf("test-1") > 0);
        }
        deviceInventoryProxy.deleteDeviceInventory("4",false);
    }

//    @Test
//    public void testGetAllDeviceInventory() {
//        deviceInventoryProxy.getAllDeviceInventory();
//    }

    @Test
    public void testUpdateDeviceInventory() {
        // --------------- Create a device.
        Chassis dev2 = new Chassis();
        dev2.setDisplayName("rest dev2");
        dev2.setRefType("chassisRefType");
        Chassis dev2Created = chassisDeviceProxy.createChassis(dev2);
        Response response = TestUtil.getResponse(chassisDeviceProxy);

        ManagedDevice deviceInventory = new ManagedDevice();
        deviceInventory.setRefId(dev2Created.getRefId());
        deviceInventory.setIpAddress("192.168.11.124");
        deviceInventory.setModel("IOA");
        deviceInventory.setDeviceType(DeviceType.ChassisM1000e);
        deviceInventory.setRefType("none");
        deviceInventory.setState(DeviceState.CONFIGURATION_ERROR);
        deviceInventory.setServiceTag("IOM-3");
        List<ManagedDevice> listOfDevices = new ArrayList<ManagedDevice>();
        listOfDevices.add(deviceInventory);
        deviceInventoryProxy.createDeviceInventory(listOfDevices.toArray(new ManagedDevice[listOfDevices.size()]));
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        ManagedDevice result = deviceInventoryProxy.getDeviceInventory(dev2Created.getRefId());
        result.setState(DeviceState.READY);

        deviceInventoryProxy.updateDeviceInventory("5", result);
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        deviceInventory = new ManagedDevice();
        deviceInventory.setRefId("5");
        deviceInventory.setIpAddress("192.168.11.124");
        deviceInventory.setModel("IOA");
        deviceInventory.setDeviceType(DeviceType.ChassisM1000e);
        deviceInventory.setRefType("none");
        deviceInventory.setState(DeviceState.READY);
        deviceInventory.setServiceTag("IOM-3");
        deviceInventoryProxy.updateDeviceInventory("5", deviceInventory);
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        deviceInventoryProxy.updateDeviceInventory(dev2Created.getRefId(), result);
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        result = deviceInventoryProxy.getDeviceInventory(dev2Created.getRefId());
        assertEquals(DeviceState.READY, result.getState());
        deviceInventoryProxy.deleteDeviceInventory(dev2Created.getRefId(),false);

        deviceInventoryProxy.updateDeviceInventory(dev2Created.getRefId(), result);
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteDeviceInventory() {
        Chassis dev1 = new Chassis();
        dev1.setDisplayName("rest dev2");
        dev1.setRefType("chassisRefType");
        dev1.setServiceTag("testdelete");
        dev1.setManagementIP("172.152.0.79");
        dev1.setModel("PowerEdge M1000e");
        Chassis dev1Created = chassisDeviceProxy.createChassis(dev1);
        Response response = TestUtil.getResponse(chassisDeviceProxy);

        ManagedDevice deviceInventory = new ManagedDevice();
        deviceInventory.setRefId(dev1Created.getRefId());
        deviceInventory.setIpAddress("172.152.0.79");
        deviceInventory.setModel("PowerEdge M1000e");
        deviceInventory.setDeviceType(DeviceType.ChassisM1000e);
        deviceInventory.setRefType("none");
        deviceInventory.setState(DeviceState.READY);
        deviceInventory.setServiceTag("IOM");
        deviceInventory.setDiscoveredDate(new GregorianCalendar());
        List<ManagedDevice> listOfDevices = new ArrayList<ManagedDevice>();
        listOfDevices.add(deviceInventory);
        deviceInventoryProxy.createDeviceInventory(listOfDevices.toArray(new ManagedDevice[listOfDevices.size()]));
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        
        deviceInventoryProxy.deleteDeviceInventory(dev1Created.getRefId(),false);
        response = TestUtil.getResponse(deviceInventoryProxy);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    //Display the List of all Devices Inventory
    @Test
    public void retrieveAllDeviceInventoryTest() {

        // Sort descending by displayName (minus sign indicates descending sort)
        String sort = "-displayName";

        //the filter need to be comma seperated with eq or contains
        String[] filter = { "eq,createdBy,system" };

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), 0, 10);
        //assertNotNull("Null device inventory list", devicesList);

        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;
        MultivaluedMap<String, Object> headers = WebClient.client(deviceInventoryProxy).getResponse().getMetadata();

        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            List<Object> values = entry.getValue();
            count++;
            System.out.println(count + " " + entry.getKey());
            for (Object value : values) {
                System.out.println(" " + value);
            }
        }

        // print devices
        count = 0;
        System.out.println("\n\n===========================================================");
        System.out.println("==== Print All Device Inventory( considered Descending) ====");
        System.out.println("============================================================");
        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getRefId());
        }


    }

    //Display the List of all Devices Inventory
    @Test
    public void retrieveAllDeviceInventoryTestFilterByStatus() {

        // Sort descending by displayName (minus sign indicates descending sort)
        String sort = "-displayName";

        //the filter need to be comma seperated with eq or contains
        String[] filter = { "eq,state,READY" };

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), 0, 10);
        //assertNotNull("Null device inventory list", devicesList);

        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;
        MultivaluedMap<String, Object> headers = WebClient.client(deviceInventoryProxy).getResponse().getMetadata();

        System.out.println("== response headers ==");
        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            List<Object> values = entry.getValue();
            count++;
            System.out.println(count + " " + entry.getKey());
            for (Object value : values) {
                System.out.println(" " + value);
            }
        }

        // print devices
        count = 0;
        System.out.println("\n\n===========================================================");
        System.out.println("==== Print All Device Inventory( considered Descending) ====");
        System.out.println("============================================================");
        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getState());
        }

    }

    //Display the List of all Devices Inventory
    @Test
    public void retrieveAllDeviceInventoryTestSortByStatus() {

        // Sort descending by displayName (minus sign indicates descending sort)
        String sort = "-state";

        //the filter need to be comma seperated with eq or contains
        String[] filter = { "eq,createdBy,system" };

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), 0, 10);
        //assertNotNull("Null device inventory list", devicesList);

        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;
        MultivaluedMap<String, Object> headers = WebClient.client(deviceInventoryProxy).getResponse().getMetadata();

        System.out.println("== response headers ==");
        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            List<Object> values = entry.getValue();
            count++;
            System.out.println(count + " " + entry.getKey());
            for (Object value : values) {
                System.out.println(" " + value);
            }
        }

        // print devices
        count = 0;
        System.out.println("\n\n===========================================================");
        System.out.println("==== Print All Device Inventory( considered Descending) ====");
        System.out.println("============================================================");
        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getState());
        }

    }

    @Test
    public void ascendingSortTest() {

        // Sort ascending by ip address
        String sort = "ipAddress";

        //the filter need to be comma seperated with (eq->equals or co->contains)
        String[] filter = { "eq,createdBy,system" };

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), 0, 10);
        //assertNotNull("Null device inventory list", devicesList);

        assertTrue(devicesList.length > 0);

        System.out.println("\n\n=============================================");
        System.out.println("==== Print Device Inventory in Ascending ====");
        System.out.println("=============================================");

        int count = 0;

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getRefId());
        }
    }

    @Test
    public void descendingSortTest() {

        // Sort descending by ip address
        String sort = "-ipAddress";

        //the filter need to be comma seperated with (eq->equals or co->contains)
        String[] filter = { "eq,createdBy,system" };

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), 0, 10);
        //assertNotNull("Null device inventory list", devicesList);

        assertTrue(devicesList.length > 0);

        System.out.println("\n\n=============================================");
        System.out.println("==== Print Device Inventory in Descending ====");
        System.out.println("=============================================");

        int count = 0;

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getRefId());
        }
    }


    //invalid Sort columns, with filter given
    @Test
    public void invalidSortColumnTest() {

        // provided invalid column for sorting
        String sort = "-invalidColumn";

        //the filter need to be comma seperated with (eq->equals or co->contains)
        String[] filter = { "eq,createdBy,system" };

        try {
            ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), 0, 10);
            //assertNull("Null device inventory list", devicesList);
            assertTrue(devicesList.length > 0);

            fail("No exception thrown on invalid sort column");

        } catch (Exception e) {
            // Success
        }
    }


    //invalid Filter columns, with sort given
    @Test
    public void invalidFilterColumnTest() {

        //Sort descending by ip address
        String sort = "-ipAddress";

        //provided invalid column for filtering
        String[] filter = { "eq,invalidColumn,system" };

        try {
            ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), 0, 10);
            //assertNull("Null device inventory list", devicesList);

            assertTrue(devicesList.length > 0);
            fail("No exception thrown on invalid filter column");

        } catch (Exception e) {
            // Success
        }
    }


//    @Test
//    public void getComprehensiveFilteredDeviceInventoryTest() {
//
//        String[] filter = { "eq,ipAddress,10.128.129.123" };
//
//        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(null, Arrays.asList(filter), 0, 10).getManagedDevices();
//        //assertNotNull("Null device inventory list", devicesList);
//
//        assertTrue(devicesList.length > 0);
//
//        // print devices
//        int count = 0;
//        System.out.println("\n\n=============================================");
//        System.out.println("==== List Device Inventory with specific ip Address ====");
//        System.out.println("=============================================");
//
//        for (ManagedDevice device : devicesList) {
//            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getRefId());
//        }
//    }

    //Pagination Test
    @Test
    public void getPaginatedDeviceInventoryTest() {

        //taken ascending sort with ip address
        String sort = "ipAddress";

        //filter by specific createdBy
        String[] filter = { "eq,createdBy,system" };


        //Values 
        /* 
         * Method :- deviceInventoryProxy.getAllDeviceInventory1(sort, filterList, offset, limit) 
         * offset starts with 0, will take all values; if given 1 will start from second value
         * limit is the number of records we want
         * 
         * Provided 10 for testing, can give maximum 50
        */

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), 0, 10);
        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;

        System.out.println("\n\n=============================================");
        System.out.println("==== List Device Inventory with Pagination ====");
        System.out.println("=============================================");

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getRefId());
        }
    }


    //Pagination Test with null offest and limit values
    @Test
    public void getPaginatedDeviceInventoryTestWithOffsetAndOrLimitAsNull() {

        //taken ascending sort with ip address
        String sort = "ipAddress";

        //filter by specific createdBy
        String[] filter = { "eq,createdBy,system" };

        //Values
      /* 
       * Method :- deviceInventoryProxy.getAllDeviceInventory1(sort, filterList, offset, limit) 
       * If offset set to null, then default value of 0 will be inistallized 
       * If limit set to null, then max value of 50 will be inistallized 
       * 
       * 
       * Provided 10 for testing, can give maximum 50
      */

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, Arrays.asList(filter), null, null);
        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;

        System.out.println("\n\n=============================================================================");
        System.out.println("==== List Device Inventory with Pagination for offset &/or limit as Null ====");
        System.out.println("=============================================================================");

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getRefId());
        }
    }


    //Pagination Test with null filter and null sort
    @Test
    public void getPaginatedDeviceInventoryTestWithfilterAndOrSortAsNull() {

        String sort = null;

        //Values
      /* 
       * Method :- deviceInventoryProxy.getAllDeviceInventory1(sort, filterList, offset, limit) 
       * If offset set to null, then default value of 0 will be inistallized 
       * If 
       * Provided 10 for testing, can give maximum 50
      */

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, null, 0, 10);
        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;

        System.out.println("\n\n=============================================================================");
        System.out.println("==== List Device Inventory with Pagination for offset &/or limit as Null ====");
        System.out.println("=============================================================================");

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getRefId());
        }
    }


    //all params null test
    @Test
    public void getPaginatedDeviceInventoryTestWithAllParamsNull() {

        String sort = null;
        List<String> filter = null;
        Integer offset = null;
        Integer limit = null;

        //Values
      /* 
       * Method :- deviceInventoryProxy.getAllDeviceInventory1(sort, filterList, offset, limit) 
       * If filter list is null, then all the device inventory are returned
      */

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, filter, offset, limit);
        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;

        System.out.println("\n\n========================================================================================");
        System.out.println("==== List Device Inventory with Pagination for filter/ sort / offset/ limit as Null ====");
        System.out.println("========================================================================================");

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType() + "  " + device.getRefId());
        }
    }


    @Test
    public void getPaginatedDeviceInventoryTestWithMultipleFilters() {

        // Sort ascending by refid
        String sort = "refId";

        //the filter need to be comma seperated with eq or contains
        String[] filter = { "eq,deviceType,ChassisM1000e", "eq,state,READY" };

        List<String> filterList = Arrays.asList(filter);

        //Values
      /* 
       * Method :- deviceInventoryProxy.getAllDeviceInventory1(sort, filterList, offset, limit) 
       * If filter list is null, then all the device inventory are returned
      */

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, filterList, 0, 50);
        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;

        System.out.println("\n\n========================================================================================");
        System.out.println("==== List Device Inventory with Pagination for more than 1 filters================= ====");
        System.out.println("========================================================================================");

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType());
        }
    }
    //Helper Methods//


    @Test
    public void getPaginatedDeviceInventoryTestWithMultipleSort() {

        // Sort ascending by refid
        String sort = "refId,-ipAddress";

        //the filter need to be comma seperated with eq or contains
        //String[] filter = null;

        List<String> filterList = null;

        //Values
      /* 
       * Method :- deviceInventoryProxy.getAllDeviceInventory1(sort, filterList, offset, limit) 
       * If filter list is null, then all the device inventory are returned
      */

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, filterList, 0, 50);
        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;

        System.out.println("\n\n========================================================================================");
        System.out.println("==== List Device Inventory with Pagination for more than 1 sorts=========================");
        System.out.println("========================================================================================");

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType());
        }
    }

    //all params null test
    @Test
    public void getPaginatedDeviceInventoryTestWithMultipleSortAndMultipleFilters() {

        // Sort ascending by refid
        String sort = "refId,-ipAddress";

        //the filter need to be comma seperated with eq or contains
        //the filter need to be comma seperated with eq or contains
        String[] filter = { "eq,deviceType,ChassisM1000e", "eq,state,READY" };

        List<String> filterList = Arrays.asList(filter);

        //Values
      /* 
       * Method :- deviceInventoryProxy.getAllDeviceInventory1(sort, filterList, offset, limit) 
       * If filter list is null, then all the device inventory are returned
      */

        ManagedDevice[] devicesList = deviceInventoryProxy.getAllDeviceInventory(sort, filterList, 0, 50);
        assertTrue(devicesList.length > 0);

        // print response headers
        int count = 0;

        System.out.println("\n\n========================================================================================");
        System.out.println("==== List Device Inventory with Pagination for more than 1 filters and more than 1 sort==");
        System.out.println("========================================================================================");

        for (ManagedDevice device : devicesList) {
            System.out.println(++count + "] " + device.getRefId() + " " + device.getModel() + " " + device.getDeviceType());
        }
    }
}
