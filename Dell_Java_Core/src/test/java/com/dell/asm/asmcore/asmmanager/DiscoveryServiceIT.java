package com.dell.asm.asmcore.asmmanager;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;


import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequest;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverIPRangeDeviceRequests;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.CredentialEntity;

public class DiscoveryServiceIT {

    /**
     * @param args
     */
    public static void main(String[] args) {

//		DiscoverIPRangeService service = new DiscoverIPRangeService();
//		try {
//		service.deviceIPRangeDiscoveryRequest(createDiscoveryRequestList());
//		} catch (WebApplicationException e){
//		    
//			System.out.println("got webexception status" +e.getResponse().getStatus());
//			//System.out.println("got webexception message" +e.getResponse().getEntity());
//			
//		}
        createChassisCredential();
        createServerCredential();

    }


    public static DiscoverIPRangeDeviceRequests createDiscoveryRequestList() {

        Set<DiscoverIPRangeDeviceRequest> reqs = new HashSet<DiscoverIPRangeDeviceRequest>();
        DiscoverIPRangeDeviceRequest r1 = new DiscoverIPRangeDeviceRequest();
        r1.setDeviceChassisCredRef("8a52f0064171d5d4014171d602540002");
        r1.setDeviceServerCredRef("8a52f0064171d5d4014171d64dc8000a");
        r1.setDeviceEndIp("10.128.76.14");
        r1.setDeviceStartIp("10.128.76.17");
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

    public static void createChassisCredential() {

        CredentialDAO credentialDAO = CredentialDAO.getInstance();
        CredentialEntity credEnt = new CredentialEntity();
        //domain, dtype, label, passwordId, updateDate, username, id
        credEnt.setType("CHASSIS");
        credEnt.setLabel("Credential Label");
        credEnt.setUsername("root");
        credEnt.setPassword("calvin");


        // credEnt.setDType("Chassis");

        credentialDAO.save(credEnt);

    }

    public static void createServerCredential() {

        CredentialDAO credentialDAO = CredentialDAO.getInstance();
        CredentialEntity credEnt = new CredentialEntity();
        //domain, dtype, label, passwordId, updateDate, username, id
        credEnt.setType("SERVER");
        credEnt.setLabel("Credential Label");
        credEnt.setUsername("root");
        credEnt.setPassword("calvin");


        // credEnt.setDType("Chassis");

        credentialDAO.save(credEnt);
    }

}
