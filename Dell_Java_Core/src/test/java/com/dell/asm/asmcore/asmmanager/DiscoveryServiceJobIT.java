package com.dell.asm.asmcore.asmmanager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import com.dell.asm.asmcore.asmmanager.app.rest.DiscoverIPRangeDevicesService;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryRequest;

public class DiscoveryServiceJobIT {

    /**
     * @param args
     */
    public static void main(String[] args) {

        DiscoverIPRangeDevicesService service = new DiscoverIPRangeDevicesService();
        try {
//			//String parentJobName ="Job-6d5992ff-2b20-4531-a0c4-32484cd5bc61";
//  		    DiscoveryResult result = service.getDiscoveryResult(parentJobName, null, null, null, null);
//			System.out.println("parentJob Status" + result.getStatus());	  
//			System.out.println("parentJob Messages" + result.getStatusMessage());
//			dumpDevice(result.getDevices());

            // DiscoveryResult result = service.deviceIPRangeDiscoveryRequest(createDiscoveryRequestList());
            DiscoveryRequest result = service.getDiscoveryRequest("Job-99163a29-bd24-4bce-8274-1ae9f36ef815");
            dumpDevice(result.getDevices());
        } catch (WebApplicationException e) {
            System.out.println(e);
            System.out.println("got webexception status" + e.getResponse().getStatus());
            throw e;
            //System.out.println("got webexception message" +e.getResponse().getEntity());
        }
        //createChassisCredential();
        //createServerCredential();

    }


    public static DiscoverIPRangeDeviceRequests createDiscoveryRequestList() {

        Set<DiscoverIPRangeDeviceRequest> reqs = new HashSet<DiscoverIPRangeDeviceRequest>();
        DiscoverIPRangeDeviceRequest r1 = new DiscoverIPRangeDeviceRequest();
        r1.setDeviceChassisCredRef("8a52f006417c0aec01417c0b39cf0002");
        r1.setDeviceServerCredRef("8a52f006417c0aec01417c0b9d8c000a");
        r1.setDeviceEndIp("10.128.76.17");
        r1.setDeviceStartIp("10.128.76.14");
        reqs.add(r1);
//		 DiscoverIPRangeDeviceRequest r2 = new DiscoverIPRangeDeviceRequest();
//		 r2.setDeviceCredRef("8a00a9ad410e3ce801410e3ceb530002");
//		 r2.setDeviceDomain("deviceDomain2");
//		 r2.setDeviceStartIp("192.168.76.20");
//		 r2.setDeviceEndIp("192.168.76.25");
        //reqs.add(r2);
        DiscoverIPRangeDeviceRequests ret = new DiscoverIPRangeDeviceRequests(reqs);
        return ret;
    }

    public static void dumpDevice(List<DiscoveredDevices> devices) {
        System.out.println(" devices size " + devices.size());
        for (DiscoveredDevices device : devices) {
            dumpDevice(device);
        }
    }


    public static void dumpDevice(DiscoveredDevices device) {
        System.out.println(" PID " + device.getParentJobId());
        System.out.println(" CPID " + device.getJobId());
        System.out.println(" RefId " + device.getRefId());
        System.out.println(" RefType " + device.getRefType());
        System.out.println(" devRefId " + device.getDeviceRefId());
        System.out.println(" ip " + device.getIpAddress());
        System.out.println(" STAG " + device.getServiceTag());
        System.out.println(" model " + device.getModel());
        System.out.println(" device type " + device.getDeviceType());
        System.out.println(" s count " + device.getServerCount());
        System.out.println(" i count " + device.getIomCount());
        System.out.println(" status " + device.getStatus());
        System.out.println(" status message " + device.getStatusMessage());
    }
}
