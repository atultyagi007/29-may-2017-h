/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.integration;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;

import org.apache.cxf.jaxrs.client.WebClient;

import com.dell.asm.rest.common.client.AuthenticatedRestAPIWebClient;
import com.dell.asm.rest.common.model.AccessKeyPair;
import com.dell.asm.usermanager.LocalSystemKeys;

public class TestUtil {
    static final String JRAF_URL = "http://localhost:9080/AsmManager";
    static final String SECURITY_URL = "http://localhost:9080/AsmManager";

    static final String TEST_USERNAME = "testuser";
    static final String TEST_PASSWORD = "password";

    public static <T> T createProxyWithTestAuth(String baseAddress, Class<T> cls) {
        final LocalSystemKeys keyManager = LocalSystemKeys.getLocalSystemKeys();
        final AccessKeyPair keys = new AccessKeyPair().setApiKey(keyManager.getApplicationApiKey())
                .setApiSecret(keyManager.getApplicationApiSecret());
        T ret = AuthenticatedRestAPIWebClient.createProxy(baseAddress, cls, keys);
        setTestAuth(ret);
        WebClient.client(ret).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
        WebClient.client(ret).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
        return ret;
    }

    static void setTestAuth(Object proxy) {
        setProxyAuth(proxy, TEST_USERNAME, TEST_PASSWORD);
    }

    static void setProxyAuth(Object proxy, String user, String password) {
        WebClient.client(proxy).reset();
        WebClient.client(proxy).header("Authorization", buildBasicAuthHeader(user, password));
    }

    static String buildBasicAuthHeader(String user, String password) {
        String userpass = user + ":" + password;
        return "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
    }

    /*static JrafJobExecStatus pollJob(IJrafJobHistoryService _jobHistoryProxy, String jobName) {
        JrafJobExecStatus status;
        while (true) {
            // See if we reached a terminal state.
            status = _jobHistoryProxy.pollExecStatus(jobName);
            if (status.isTerminal())
                break;

            // Sleep and then retry.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Sleep was interrupted.");
            }
        }
        return status;
    }*/

    /* ------------------------------------------------------------------------- */
 /* say:                                                                      */
 /* ------------------------------------------------------------------------- */
    static void say(String s) {
        System.out.println(s);
    }
}
