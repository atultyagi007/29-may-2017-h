/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.WebClient;

import com.dell.asm.rest.common.client.AuthenticatedRestAPIWebClient;
import com.dell.asm.rest.common.model.AccessKeyPair;
import com.dell.asm.usermanager.LocalSystemKeys;
import com.dell.pg.jraf.client.jobmgr.IJrafJobHistoryService;
import com.dell.pg.jraf.client.jobmgr.JrafJobExecStatus;


public class TestUtil
{ // Fixed test server address.
    //http://localhost:9080/AsmManager/asmcore/amsmanager
    public static final String ASM_URL = getBaseContainerUrl() + "/AsmManager";
    public static final String IOM_RA_URL = getBaseContainerUrl() + "/IOMRA";
    public static final String CHASSIS_RA_URL = getBaseContainerUrl() + "/ChassisRA";
    public static final String SERVER_RA_URL = getBaseContainerUrl() + "/ServerRA";
    public static final String ENCRYPTION_MGR_URL = getBaseContainerUrl() + "/ServerRA";
    public static final String JRAF_URL = getBaseContainerUrl() + "/JRAF";
    public static final String IDENTITY_URL  = getBaseContainerUrl() + "/VirtualServices";
    

    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_PASSWORD = "password";

    public static String getBaseContainerUrl() {
        String host = System.getProperty("TEST_HOST");
        if (host == null || host.isEmpty()) {
            host = "localhost";
        }
        return "http://" + host + ":9080";
    }

    public static <T> T createProxyWithTestAuth(String baseAddress, Class<T> cls) {
        final LocalSystemKeys keyManager = LocalSystemKeys.getLocalSystemKeys();
        final AccessKeyPair keys = new AccessKeyPair().setApiKey(keyManager.getApplicationApiKey())
                .setApiSecret(keyManager.getApplicationApiSecret());
        T ret = AuthenticatedRestAPIWebClient.createProxy(baseAddress, cls, keys);
        setTestAuth(ret);
        return ret;
    }

 static void setTestAuth(Object proxy)
 {
  setProxyAuth(proxy, TEST_USERNAME, TEST_PASSWORD);
 }

 static Response getResponse(Object proxy) {
         return WebClient.client(proxy).getResponse();
 }

 static Client getClient(Object proxy) {
         return WebClient.client(proxy);
 }

 static void setProxyAuth(Object proxy, String user, String password)
 {
  WebClient.client(proxy).reset();
  WebClient.client(proxy).header("Authorization", buildBasicAuthHeader(user, password));
 }

 static String buildBasicAuthHeader(String user, String password)
 {
  String userpass = user + ":" + password;
  return "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
 }

 static JrafJobExecStatus pollJob(IJrafJobHistoryService _jobHistoryProxy, String jobName)
 {
  JrafJobExecStatus status;
  while (true)
   {
    // See if we reached a terminal state.
    status = _jobHistoryProxy.pollExecStatus(jobName);
    if (status.isTerminal()) break;

    // Sleep and then retry.
    try {Thread.sleep(2000);}
    catch (InterruptedException e){throw new RuntimeException("Sleep was interrupted.");}
   }
  return status;
 }

 /* ------------------------------------------------------------------------- */
 /* prettyPrintDevice:                                                        */
 /* ------------------------------------------------------------------------- */
// static void prettyPrintDevice(IomDevice iomDevice)
// {
//  say("Properties for device: " + iomDevice.getDisplayName()+ ":");
//  List<String> keys = new ArrayList<>(iomDevice.getValues().keySet());
//  Collections.sort(keys);
//  for (String key : keys)
//    say("  " + key + " = " + iomDevice.getValues().get(key));
// }

 /* ------------------------------------------------------------------------- */
 /* say:                                                                      */
 /* ------------------------------------------------------------------------- */
 static void say(String s){System.out.println(s);}
 
 /* use setProxyHeader if calling the proxy services at the service layer
  *  if calling from a utilty class or a callable set the proxy headers before calling the RA
  */
 public static void setProxyHeaders(Object ret) {
     WebClient.client(ret).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
     WebClient.client(ret).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
 }
}


