/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;

import com.dell.asm.asmcore.asmmanager.client.deployment.Deployment;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.StaticNetworkConfiguration;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Charsets;

public class DnsUtilIT {
    public static final String PRIMARY_DNS = "143.166.216.237";
    public static final String TEST_HOST = "insideportal.dell.com";
    public static final String TEST_SUFFIX = "us.dell.com";

    private DnsUtil util;

    @Before
    public void setUp() {
        util = new DnsUtil();
    }

    @Test
    public void testReverseDnsLookup() throws IOException {
        // Look up an IP so that we can do reverse DNS on it later
        String fqdn = TEST_HOST;
        Lookup l = new Lookup(fqdn);
        SimpleResolver simpleResolver = new SimpleResolver(PRIMARY_DNS);
        l.setResolver(simpleResolver);
        l.run();
        assertEquals("Lookup failed", Lookup.SUCCESSFUL, l.getResult());
        String ip = l.getAnswers()[0].rdataToString();

        // Build static network config
        String actual = util.reverseLookup(ip, PRIMARY_DNS);
        assertTrue("Got invalid hostname for " + fqdn + ": " + actual, actual.startsWith("moss"));
    }

    @Test
    public void testReverseDnsLookupWithNullServer() throws IOException {
        // Look up an IP so that we can do reverse DNS on it later
        String fqdn = TEST_HOST;
        Lookup l = new Lookup(fqdn);
        SimpleResolver simpleResolver = new SimpleResolver(PRIMARY_DNS);
        l.setResolver(simpleResolver);
        l.run();
        assertEquals("Lookup failed", Lookup.SUCCESSFUL, l.getResult());
        String ip = l.getAnswers()[0].rdataToString();

        // Build static network config
        String actual = util.reverseLookup(ip, null, PRIMARY_DNS);
        assertTrue("Got invalid hostname for " + fqdn + ": " + actual, actual.startsWith("moss"));
    }

    @Test
    public void testDnsLookup() throws TextParseException {
        String ip = util.lookup(TEST_HOST, PRIMARY_DNS);
        System.out.println("Resolved " + TEST_HOST + " to " + ip);
    }
}
