/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsUtil {
    private static final Logger LOGGER = Logger.getLogger(DnsUtil.class);

    public String reverseLookup(String ipAddress, String... servers) {
        if (ipAddress == null) {
            throw new IllegalArgumentException("IP address must not be null");
        }

        // Ensure no null servers passed
        List<String> serversList = new ArrayList<>();
        for (String server : servers) {
            if (server != null) {
                serversList.add(server);
            }
        }

        if (serversList.size() < 1) {
            return null;
        }

        try {
            Resolver resolver = new ExtendedResolver(serversList.toArray(new String[serversList.size()]));
            Name name = ReverseMap.fromAddress(ipAddress);
            Lookup l = new Lookup(name, Type.PTR);
            l.setResolver(resolver);
            l.run();
            Record[] answers = l.getAnswers();
            if (answers == null || answers.length < 1 || answers[0].rdataToString() == null) {
                LOGGER.warn("Reverse lookup failed for " + ipAddress + " on DNS servers " + servers);
                return null;
            } else {
                return answers[0].rdataToString();
            }
        } catch (UnknownHostException e) {
            LOGGER.warn("Reverse lookup failed for ip " + ipAddress + " on DNS servers " + servers, e);
            return null;
        }
    }

    public String lookup(String hostName, String... servers) throws TextParseException {
        if (hostName == null) {
            throw new IllegalArgumentException("Hostname must not be null");
        }

        // Ensure no null servers passed
        List<String> serversList = new ArrayList<>();
        for (String server : servers) {
            if (server != null) {
                serversList.add(server);
            }
        }

        if (serversList.size() < 1) {
            return null;
        }

        try {
            Resolver resolver = new ExtendedResolver(serversList.toArray(new String[serversList.size()]));
            Lookup l = new Lookup(hostName, Type.A);
            l.setResolver(resolver);
            l.run();
            Record[] answers = l.getAnswers();
            if (answers == null || answers.length < 1 || answers[0].rdataToString() == null) {
                LOGGER.warn("DNS lookup failed for " + hostName + " on DNS servers " + servers);
                return null;
            } else {
                return answers[0].rdataToString();
            }
        } catch (UnknownHostException e) {
            LOGGER.warn("DNS lookup failed for " + hostName + " on DNS servers " + servers);
            return null;
        }
    }
}
