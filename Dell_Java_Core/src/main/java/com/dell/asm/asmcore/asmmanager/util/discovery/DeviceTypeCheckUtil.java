/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.common.model.CommandResponse;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.pg.orion.common.utilities.ConfigurationUtils;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.security.credential.CredentialDAO;
import com.dell.pg.orion.security.credential.entity.CredentialEntity;
import com.dell.pg.orion.security.credential.entity.StorageCredentialEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;

/**
 * Device check utility to figure out the device type given an IP address
 */
public final class DeviceTypeCheckUtil {
    public static final String PROP_TEST_INVENTORY_PATH = "com.dell.asm.asmcore.sample.inventory";
    public static final String TEMP_PATH = "/tmp";
    private static final Logger LOGGER = Logger.getLogger(DeviceTypeCheckUtil.class);

    static final SampleInventory SAMPLE_INVENTORY = buildSampleInventory();
    private static final String EQUALLOGIC_MANAGEMENT_IP = "Management-Ipaddress";
    private static final String EQUALLOGIC_GROUP_IP = "Group-Ipaddress";
    private static final String PUPPET_EQL_PATH = "/etc/puppetlabs/puppet/modules/equallogic/files/";
    public static final String PUPPET_SCVMM_PATH = "/etc/puppetlabs/puppet/modules/scvmm";
    public static final String SCVMM_CACHE_PATH = "/opt/Dell/ASM/cache";
    public static final String PUPPET_NETAPP_PATH = "/etc/puppetlabs/puppet/modules/netapp";
    public static final String PUPPET_EMC_VNX_PATH = "/etc/puppetlabs/puppet/modules/vnx";

    @XmlRootElement
    static class SampleInventory {
        private Map<String, DiscoverDeviceType> inventory = new HashMap<>();

        public Map<String, DiscoverDeviceType> getInventory() {
            return inventory;
        }

        public void setInventory(Map<String, DiscoverDeviceType> inventory) {
            this.inventory = inventory;
        }

        public void addInventory(String ipAddres, DiscoverDeviceType deviceType) {
            inventory.put(ipAddres, deviceType);
        }
    }

    private static SampleInventory buildSampleInventory() {
        String path = System.getProperty(PROP_TEST_INVENTORY_PATH);
        if (StringUtils.isBlank(path)) {
            return null;
        } else {
            try {
                URL url = ConfigurationUtils.resolvePropertiesFile(path,
                        SampleInventory.class.getClassLoader());
                String xml = IOUtils.toString(url);
                return MarshalUtil.unmarshal(SampleInventory.class, xml);
            } catch (IllegalStateException e) {
                LOGGER.error("Failed to unmarshal inventory file at " + path, e);
                return null;
            } catch (IOException e) {
                LOGGER.error("Failed to read sample inventory file at " + path, e);
                return null;
            }
        }
    }

    private DeviceTypeCheckUtil() {
    }


    /**
     * HTTP request extractor
     *
     * @param urlToRead device URL
     * @return device type string
     * @throws IOException
     */
    public static String getHTML(String urlToRead) throws IOException {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd = null;
        String line;
        StringBuffer result = new StringBuffer();

        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            if (conn instanceof HttpsURLConnection) {
                HttpsURLConnection sslConn = (HttpsURLConnection) conn;
                sslConn.setHostnameVerifier(hv);
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{tmNoCheck}, new SecureRandom());
                sslConn.setSSLSocketFactory(sslContext.getSocketFactory());
            }

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(AsmManagerApp.CONNECT_TIMEOUT); // timeout value
            conn.setReadTimeout(AsmManagerApp.CONNECT_TIMEOUT);
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (RuntimeException e) {
            throw new IOException("Could not connect to the url: " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Could not connect to the url: " + urlToRead);
        } finally {
            if (rd != null)
                rd.close();
        }
        return result.toString();
    }

    /**
     * HTTP POST with basic auth
     *
     * @param urlToRead device URL
     * @return http response message
     * @throws IOException
     */
    public static String httpPost(String urlToRead, String username, String password) throws IOException {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd = null;
        String line;
        StringBuffer result = new StringBuffer();

        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            if (conn instanceof HttpsURLConnection) {
                HttpsURLConnection sslConn = (HttpsURLConnection) conn;
                sslConn.setHostnameVerifier(hv);
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{tmNoCheck}, new SecureRandom());
                sslConn.setSSLSocketFactory(sslContext.getSocketFactory());
            }
            conn.setDoOutput(true);
            conn.setConnectTimeout(AsmManagerApp.CONNECT_TIMEOUT); // timeout value
            conn.setReadTimeout(AsmManagerApp.CONNECT_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty ("x-dell-api-version", "2.0");
            conn.setRequestProperty("Authorization", encodeCredentials(username, password));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setFixedLengthStreamingMode("{}".length());
            conn.getOutputStream().write("{}".getBytes(Charset.forName("UTF-8")));

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (RuntimeException e) {
            throw new IOException("Could not connect to the url: " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Could not connect to the url: " + urlToRead);
        } finally {
            if (rd != null)
                rd.close();
        }
        return result.toString();
    }
    public static int httpGet(String urlToRead) throws IOException {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd = null;
        StringBuffer result = new StringBuffer();
        int responseCode = 400;

        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10);
            responseCode = conn.getResponseCode();

        } catch (RuntimeException e) {
            throw new IOException("Could not connect to the url: " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Could not connect to the url: " + urlToRead);
        } finally {
            if (rd != null)
                rd.close();
        }
        return responseCode;
    }

    private static String encodeCredentials(String username, String password){
        return "Basic {" + Base64.encodeBase64URLSafeString((username + ":" + password).getBytes(Charset.forName("UTF-8"))) + "}";
    }

    public static DiscoverDeviceType checkDeviceType(InfrastructureDevice device) {
        if (SAMPLE_INVENTORY != null && SAMPLE_INVENTORY.getInventory().get(device.getIpAddress()) != null) {
            return SAMPLE_INVENTORY.getInventory().get(device.getIpAddress());
        }

        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;

        if ((device.getRequestedDeviceType()==null ||
                DeviceType.isChassis(device.getRequestedDeviceType())) && StringUtils.isNotEmpty(device.getChassisCredentialId())) {
            retVal = checkIdracCmc(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;
        }
        if ((device.getRequestedDeviceType()==null ||
                DeviceType.isServer(device.getRequestedDeviceType())) && StringUtils.isNotEmpty(device.getServerCredentialId())) {
            retVal = checkIdracCmc(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;
            retVal = checkCServer(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;
        }
        if ((device.getRequestedDeviceType()==null ||
                DeviceType.isSwitch(device.getRequestedDeviceType())) && StringUtils.isNotEmpty(device.getSwitchCredentiallId())) {
            retVal = checkBrocade(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;
            retVal = checkForce10(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;
            retVal = checkPowerConnect(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;
            retVal = checkCiscoNexus(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;

        }
        if ((device.getRequestedDeviceType()==null ||
                DeviceType.isStorage(device.getRequestedDeviceType())) && StringUtils.isNotEmpty(device.getStorageCredentialId())) {
            retVal = checkEql(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;

            retVal = checkCompllent(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;
            retVal = checkEmcVnx(device.getIpAddress(), device);

            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;

            retVal = checkNetApp(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;

        }
        if ((device.getRequestedDeviceType()==null ||
                DeviceType.isVcenter(device.getRequestedDeviceType())) && StringUtils.isNotEmpty(device.getvCenterCredentialId())) {
            retVal = checkVCenter(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;

        }
        if ((device.getRequestedDeviceType()==null ||
                DeviceType.isSCVMM(device.getRequestedDeviceType())) && StringUtils.isNotEmpty(device.getScvmmCredentialId())) {
            retVal = checkSCVMM(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;

        }
        if ((device.getRequestedDeviceType()==null ||
                DeviceType.em == device.getRequestedDeviceType()) && StringUtils.isNotEmpty(device.getEmCredentialId())) {
            retVal = checkEM(device.getIpAddress(), device);
            if (retVal!=DiscoverDeviceType.UNKNOWN)
                return retVal;
        }

        return retVal;
    }

    private static DiscoverDeviceType checkBrocade(String ipAddress, InfrastructureDevice device) {
        String html = String.format("http://%s/switchExplorer_installed.html", ipAddress);
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;

        try {
            String response = getHTML(html);
            device.setDiscoveryResponse(response);
            LOGGER.debug("In DeviceTypeCheck.checkBrocade84IOM " + response);
            if (response.contains("M5424") ||
                    response.contains("_FC_IOM")) {
                retVal = DiscoverDeviceType.DELL_IOM_84;
            }else if (response.contains("com.brocade.web.switchview.SwitchExplorerApplet")) {
                retVal = DiscoverDeviceType.BROCADE;
            }
        } catch (IOException ioe) {
            LOGGER.debug("In DeviceTypeCheck.checkBrocade84IOM, could not connect to " + ipAddress + " because of " + ioe.getMessage() + ".  Ignoring.");
        }
        return retVal;
    }

    /**
     * Creates an SSH session to the given server on a custom TCP port
     * using the provided credentials.  This is equivalent to Expect's
     * <code>spawn ssh $hostname</code>.
     *
     * This method is essentially the same as ExpectUtils.SSH, but it
     * properly disconnects the ssh session. ExpectUtils.SSH is buggy
     * and leaves the ssh connection open. <b>Do not use ExpectUtils.SSH!</b>
     *
     * @param hostname the DNS or IP address of the remote server
     * @param username the account name to use when authenticating
     * @param password the account password to use when authenticating
     * @return the controlling Expect4j instance
     * @throws Exception on a variety of errors
     */
    private static Expect4j ssh(String hostname, String username, String password) throws Exception {
        final int port = 22;
        LOGGER.debug("Creating SSH session with " + hostname + ":" + port + " as " + username);

        JSch jsch = new JSch();
        final Session session = jsch.getSession(username, hostname, port);
        if (password != null) {
            LOGGER.trace("Setting the Jsch password to the one provided (not shown)");
            session.setPassword(password);
        }

        Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setDaemonThread(true);
        session.connect(3 * 1000);   // making a connection with timeout.

        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setPtyType("vt102");

        Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream()) {
            @Override
            public void close() {
                super.close();
                session.disconnect();
            }
        };

        channel.connect(5 * 1000);

        return expect;
    }

    private static DiscoverDeviceType checkForce10(String ipAddress, InfrastructureDevice device) {
        Expect4j ssh = null;
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;
        CredentialEntity cred = getCredentialDAO().findById(device.getSwitchCredentiallId());

        try {
            Closure closure = new Closure() {
                StringBuffer buffer = new StringBuffer();
                @Override
                public void run(ExpectState expectState) throws Exception {
                    LOGGER.debug("Forec10 SSH output: " + expectState.getBuffer());
                    buffer.append(expectState.getBuffer());
                }
                @Override
                public String toString() {
                    return buffer.toString();
                }
            };

            ssh = ssh(ipAddress, cred.getUsername(), cred.getPasswordData().getString());

            // wait for prompt
            if (ssh.expect("#") == 0) {
                // send version command
                ssh.send("show version\n");

                List<Match> f10Pattern =  new ArrayList<Match>();
                Match mat = new RegExpMatch("(Dell Force10|Dell Real Time Operating System Software)", closure);
                f10Pattern.add(mat);

                List<Match> iomPattern =  new ArrayList<Match>();
                Match mat2 = new RegExpMatch("(I/O-Aggregator|MXL)", closure);
                iomPattern.add(mat2);

                List<Match> fxPattern =  new ArrayList<Match>();
                Match mat3 = new RegExpMatch("IOA|PE-FN", closure);
                fxPattern.add(mat3);

                List<Match> s4810Pattern =  new ArrayList<Match>();
                Match mat4 = new RegExpMatch("S4810|S4820", closure);
                s4810Pattern.add(mat4);

                List<Match> s5000Pattern =  new ArrayList<Match>();
                Match mat5 = new RegExpMatch("S5000", closure);
                s5000Pattern.add(mat5);

                List<Match> s6000Pattern =  new ArrayList<Match>();
                Match mat6 = new RegExpMatch("S6000", closure);
                s6000Pattern.add(mat6);

                List<Match> s4048Pattern =  new ArrayList<Match>();
                Match mat7 = new RegExpMatch("S4048", closure);
                s4048Pattern.add(mat7);

                int ret = ssh.expect(f10Pattern);
                if (ret >= 0) {
                    retVal = DiscoverDeviceType.FORCE10;
                    // we have Force10. Multiple types are recognized
                    if (ssh.expect(iomPattern) >= 0) {
                        retVal = DiscoverDeviceType.FORCE10IOM;
                    }else if (ssh.expect(fxPattern) >= 0) {
                        retVal = DiscoverDeviceType.FX2_IOM;
                    }else if (ssh.expect(s4810Pattern) >= 0) {
                        retVal = DiscoverDeviceType.FORCE10_S4810;
                    }else if (ssh.expect(s5000Pattern) >= 0) {
                        retVal = DiscoverDeviceType.FORCE10_S5000;
                    }else if (ssh.expect(s6000Pattern) >= 0) {
                        retVal = DiscoverDeviceType.FORCE10_S6000;
                    }else if (ssh.expect(s4048Pattern) >= 0) {
                        retVal = DiscoverDeviceType.FORCE10_S4048;
                    }
                }else{
                    LOGGER.debug("Unsuccessful check for Force10 pattern, error code: " + expect4jError(ret));
                }
            }
            device.setDiscoveryResponse(closure.toString());
        } catch (Exception e) {
            LOGGER.debug("In DeviceTypeCheck.checkForce10, could not connect to " + ipAddress + " because of " + e.getMessage() + ".  Ignoring.");
        } finally {
            if (ssh != null) {
                try {
                    ssh.send("exit\n");
                } catch (IOException e) {
                    LOGGER.warn("Could not cleanly close ssh connection");
                }
                ssh.close();
            }
        }

        return retVal;
    }

    private static String expect4jError(int ret) {
        switch (ret) {
            case -4:
                return "RET_TRIED_ONCE";
            case -3:
                return "RET_EOF";
            case -2:
                return "RET_TIMEOUT";
            case -1:
                return "RET_UNKNOWN";
            default:
                return String.valueOf(ret);
        }
    }

    /**
     * @param ipAddress
     * @return
     */

    private static DiscoverDeviceType checkCiscoNexus(String ipAddress, InfrastructureDevice device) {
        LOGGER.debug("In DeviceTypeCheck.checkCiscoNexus,  connecting to " + ipAddress);
        Expect4j ssh = null;
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;
        CredentialEntity cred = getCredentialDAO().findById(device.getSwitchCredentiallId());

        try {
            Closure closure = new Closure() {
                StringBuffer buffer = new StringBuffer();
                @Override
                public void run(ExpectState expectState) throws Exception {
                    LOGGER.debug("CiscoNexus SSH output: " + expectState.getBuffer());
                    buffer.append(expectState.getBuffer());
                }
                @Override
                public String toString() {
                    return buffer.toString();
                }

            };

            ssh = ssh(ipAddress, cred.getUsername(), cred.getPasswordData().getString());
            ssh.send("show version");

            List<Match> pattern =  new ArrayList<Match>();
            Match mat = new RegExpMatch("Cisco Nexus", closure);
            pattern.add(mat);

            if (ssh.expect(pattern) >= 0) {
                retVal = DiscoverDeviceType.CISCONEXUS;
            }
            device.setDiscoveryResponse(closure.toString());
        } catch (Exception e) {
            LOGGER.debug("In DeviceTypeCheck.checkCiscoNexus, could not connect to " + ipAddress + " because of " + e.getMessage() + ".  Ignoring.");
        } finally {
            if (ssh != null) {
                try {
                    ssh.send("exit\n");
                } catch (IOException e) {
                    LOGGER.warn("Could not cleanly close ssh connection");
                }
                ssh.close();
            }
        }
        return retVal;
    }

    private static DiscoverDeviceType checkPowerConnect(String ipAddress, InfrastructureDevice device) {
        Expect4j ssh = null;
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;

        // todo we have a mixed model.  TOR is the only ping sweep that requires credential.
        CredentialEntity cred = getCredentialDAO().findById(device.getSwitchCredentiallId());
        try {
            ssh = ssh(ipAddress, cred.getUsername(), cred.getPasswordData().getString());
            Closure closure = new Closure() {
                StringBuffer buffer = new StringBuffer();
                @Override
                public void run(ExpectState expectState) throws Exception {
                    LOGGER.debug("PowerConnect SSH output: " + expectState.getBuffer());
                    buffer.append(expectState.getBuffer());
                }
                @Override
                public String toString() {
                    return buffer.toString();
                }
            };

            // wait for prompt
            if (ssh.expect(">") == 0) {
                // send version command
                ssh.send("show system\n");

                List<Match> patternStart =  new ArrayList<Match>();
                Match matStart = new RegExpMatch("Dell Networking", closure);
                patternStart.add(matStart);

                List<Match> pattern2 =  new ArrayList<Match>();
                Match mat2 = new RegExpMatch("N3024|N3048", closure);
                pattern2.add(mat2);

                List<Match> pattern3 =  new ArrayList<Match>();
                Match mat3 = new RegExpMatch("N4032|N4064|N4032F|N4064F", closure);
                pattern3.add(mat3);

                List<Match> pattern =  new ArrayList<Match>();
                Match mat = new RegExpMatch("(PowerConnect|Dell Networking N)", closure);
                pattern.add(mat);

                if (ssh.expect(patternStart)>=0) {
                    if (ssh.expect(pattern2) >= 0) {
                        retVal = DiscoverDeviceType.POWERCONNECT_N3000;
                    } else if (ssh.expect(pattern3) >= 0) {
                        retVal = DiscoverDeviceType.POWERCONNECT_N4000;
                    } else if (ssh.expect(pattern) >= 0) {
                        retVal = DiscoverDeviceType.POWERCONNECT;
                    }
                }
            }
            device.setDiscoveryResponse(closure.toString());

        } catch (Exception e) {
            LOGGER.debug("In DeviceTypeCheck.POWERCONNECT, could not connect to " + ipAddress + " because of " + e.getMessage() + ".  Ignoring.");
        } finally {
            if (ssh != null) {
                try {
                    ssh.send("exit\n");
                } catch (IOException e) {
                    LOGGER.warn("Could not cleanly close ssh connection");
                }
                ssh.close();
            }
        }
        return retVal;
    }

    private static DiscoverDeviceType checkIdracCmc(String ipAddress, InfrastructureDevice device) {
        String html = String.format("https://%s/cgi-bin/discover", ipAddress);
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;

        try {
            String response = getHTML(html);
            device.setDiscoveryResponse(response);
            if (response.contains("<ENDPOINTTYPE>iDRAC7</ENDPOINTTYPE>")) {
                retVal = DiscoverDeviceType.IDRAC7;
            } else if (response.contains("<ENDPOINTTYPE>CMC</ENDPOINTTYPE>")) {
                if (response.contains("<ENDPOINTVER>3.0</ENDPOINTVER>")) {
                    retVal = DiscoverDeviceType.CMC_FX2;
                }else if (response.contains("<ENDPOINTVER>2.0</ENDPOINTVER>")){
                    retVal = DiscoverDeviceType.UNKNOWN; //blocking discovery of VRTX Chassis per ASM-2635
                } else {
                    retVal = DiscoverDeviceType.CMC;
                }
            } else if (response.contains("<ENDPOINTTYPE>iDRAC8</ENDPOINTTYPE>")) {
                retVal = DiscoverDeviceType.IDRAC8;
            }
        } catch (IOException ioe) {
            LOGGER.debug("In DeviceTypeCheck.checkIdracCmc, could not connect to " + ipAddress + " because of " + ioe.getMessage() + ".  Ignoring.");
        }
        return retVal;
    }

    private static DiscoverDeviceType checkCServer(String ipAddress, InfrastructureDevice device) {
        String html = String.format("https://%s/login.html", ipAddress);
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;

        try {
            String response = getHTML(html);
            device.setDiscoveryResponse(response);
            if (response.contains("Dell Remote Management Controller")) {
                retVal = DiscoverDeviceType.CSERVER;
            }
        } catch (IOException ioe) {
            LOGGER.debug("In DeviceTypeCheck.checkCServer, could not connect to " + ipAddress + " because of " + ioe.getMessage() + ".  Ignoring.");
        }
        return retVal;
    }

    private static DiscoverDeviceType checkVCenter(String ipAddress, InfrastructureDevice device) {
        String vcenterHtml = String.format("https://%s/en/welcomeRes.js", ipAddress);
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;
        try {
            String vcenterResponse = getHTML(vcenterHtml);
            device.setDiscoveryResponse(vcenterResponse);
            if (vcenterResponse.contains("var ID_VMWVC2 = \"VMware vSphere\";")) {
                retVal = DiscoverDeviceType.VCENTER;
            }
        } catch (IOException ioe) {
            LOGGER.debug("In DeviceTypeCheck.checkVCenter, could not connect to " + ipAddress + " because of " + ioe.getMessage() + ".  Ignoring.");
        }
        return retVal;
    }

    private static DiscoverDeviceType checkEql(String ipAddress, InfrastructureDevice device) {
        CredentialEntity cred = getCredentialDAO().findById(device.getStorageCredentialId());
        if (!(cred instanceof StorageCredentialEntity)) {
            LOGGER.warn("Invalid credential passed for equallogic identification of "
                    + ipAddress + ": " + cred);
            return DiscoverDeviceType.UNKNOWN;
        }
        StorageCredentialEntity storageCred = (StorageCredentialEntity) cred;
        if (StringUtils.isBlank(storageCred.getSnmpCommunityString())) {
            LOGGER.debug("No snmp community string passed for " + ipAddress +
                    "; skipping equallogic identification");
            return DiscoverDeviceType.UNKNOWN;
        }
        String eqlHtml = String.format("http://%s/groupmgr.html", ipAddress);
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;
        try {
            int eqlResponse = httpGet(eqlHtml);
            if ( eqlResponse == 200 ) {
                String eqlManagementIP = getEqualLogicInfo(EQUALLOGIC_MANAGEMENT_IP, ipAddress, storageCred, device);
                if (StringUtils.isBlank(eqlManagementIP)) {
                    // ASM-1018B: if (and only if) no management IP defined, allow Group IP for discovery.
                    eqlManagementIP = getEqualLogicInfo(EQUALLOGIC_GROUP_IP, ipAddress, storageCred, null);
                }
                if (StringUtils.equals(eqlManagementIP, ipAddress)) {
                    retVal = DiscoverDeviceType.EQUALLOGIC;
                } else {
                    retVal = DiscoverDeviceType.EQUALLOGIC_NODISCOVER;
                }
            } else {
                // many other device support HTTP GET, we don't know what is that
                retVal = DiscoverDeviceType.UNKNOWN;
            }
        } catch (IOException ioe) {
            LOGGER.debug("In DeviceTypeCheck.checkEql, could not connect to " + ipAddress + " because of " + ioe.getMessage() + ".  Ignoring.");
        }
        return retVal;
    }

    private static String getEqualLogicInfo(String targetInfoName, String ipAddress, CredentialEntity cred, InfrastructureDevice device) {
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();
        String[] eqlCommand = new String[] { "/bin/bash", "-c", "python" +
                " " + ExecuteSystemCommands.sanitizeShellArgument(PUPPET_EQL_PATH) + "pythonEquallogic.py" +
                " " + ExecuteSystemCommands.sanitizeShellArgument(ipAddress) +
                " " + ExecuteSystemCommands.sanitizeShellArgument(cred.getUsername()) +
                " " + ExecuteSystemCommands.sanitizeShellArgument(cred.getPasswordData().getString()) +
                " " + "discoverygrpparamsshow | grep" +
                " " + ExecuteSystemCommands.sanitizeShellArgument(targetInfoName) };
        String result = null;
        try {
            CommandResponse cmdresponse = cmdRunner.runCommandWithConsoleOutput(eqlCommand);
            LOGGER.debug("Return code: " + cmdresponse.getReturnCode() + " Return message: " + cmdresponse.getReturnMessage());
            if (device!=null)
                device.setDiscoveryResponse(cmdresponse.getReturnMessage());
            if (cmdresponse.getReturnCode().equalsIgnoreCase("0")) {
                String output = cmdresponse.getReturnMessage();
                Pattern p = Pattern.compile(String.format("%s:(.*)$", targetInfoName));
                Matcher m = p.matcher(output);
                if (m.matches()) {
                    result = m.group(1).trim();
                    LOGGER.info(String.format("getEqualLogicInfo(%s,%s,<credential>): Found match: Result = %s", targetInfoName, ipAddress, result));
                }
            }
        } catch (Exception e) {
            LOGGER.debug("In DeviceTypeCheck.getEqualLogicInfo(), could not connect to " + ipAddress + " because of " + e.getMessage()
                    + ".  Ignoring.");
        }
        return result;
    }

    private static DiscoverDeviceType checkEM(String ipAddress, InfrastructureDevice device) {
        String emHtml = String.format("https://%s:3033/em/EnterpriseManager", ipAddress);
        String loginURL = String.format("https://%s:3033/api/rest/ApiConnection/Login", ipAddress);
        String sResponse = null;
        try {
            sResponse = getHTML(emHtml);
        }catch(IOException ioe) {
            LOGGER.debug("GET html request for " + emHtml + " failed:",ioe);
        }
        try {
            if (sResponse!=null && (sResponse.contains("Enterprise Manager") ||
                    sResponse.contains("Storage Manager"))) {
                CredentialEntity cred = getCredentialDAO().findById(device.getEmCredentialId());
                String username = StringUtils.isEmpty(cred.getDomain()) ? cred.getUsername() : cred.getDomain() + "\\" + cred.getUsername();
                String response = httpPost(loginURL, username, cred.getPasswordData().getString());

                if (StringUtils.isNotEmpty(response)) {
                    device.setDiscoveryResponse(response);

                    ObjectMapper mapper = new ObjectMapper();
                    Map params = mapper.readValue(response, Map.class);
                    Boolean connected = (Boolean) params.get("connected");
                    if (Boolean.TRUE.equals(connected)) {
                        return DiscoverDeviceType.EM;
                    }else{
                        LOGGER.debug("Login passed but property connected is not true. Response: " + response);
                    }
                }
            }
        }catch(Exception e) {
            LOGGER.debug("POST login request for " + loginURL + " failed:",e);
        }
        return DiscoverDeviceType.UNKNOWN;
    }

    private static DiscoverDeviceType checkCompllent(String ipAddress, InfrastructureDevice device) {
        String compllentHtml = String.format("https://%s/SystemExplorer.asp", ipAddress);
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;
        CredentialEntity cred = getCredentialDAO().findById(device.getStorageCredentialId());
        try {
            String compellentResponse = getHTML(compllentHtml);
            if (compellentResponse!=null && compellentResponse.toUpperCase().contains("COMPELLENT")) {
                // Ok, we know this device is compellent, but we don't know if it's the master or a slave.
                // Make a compellent api call to get the IP of the master, then compare it to this IP.
                // if they don't match, this is a slave and we want to skip it.

//                -bash-4.1# java -jar /etc/puppetlabs/puppet/modules/compellent/lib/puppet/files/CompCU-6.3.jar -host 172.17.10.41 -user Admin -password P@ssw0rd -c "system show" -xml xmlfilename
//                <compellent>
//                <system>
//                <SerialNumber>24260</SerialNumber>
//                <Name>SC8000-10-50</Name>
//                <ManagementIP>172.17.10.40</ManagementIP>
//                <Version>6.3.2.16</Version>
//                <OperationMode>Normal</OperationMode>
//                <PortsBalanced>No</PortsBalanced>
//                <MailServer></MailServer>
//                <BackupMailServer></BackupMailServer>
//                </system>
//                </compellent>

                // call compellent command line
                File devicesDir = new File(TEMP_PATH);
                File tempFile = File.createTempFile(ipAddress, ".tmp.conf", devicesDir);
                ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();

                String[] volumeCommand = {"java", "-jar",
                        "/etc/puppetlabs/puppet/modules/compellent/lib/puppet/files/CompCU.jar",
                        "-host", ipAddress,
                        "-user", cred.getUsername(),
                        "-password", cred.getPasswordData().getString(),
                        "-c", "system show -xml " + tempFile.getAbsolutePath()};

                //LOGGER.debug("Calling Compellent : " + volumeCommand);
                CommandResponse cmdresponse = cmdRunner.runCommandWithConsoleOutput(volumeCommand);
                LOGGER.debug("Return code: " + cmdresponse.getReturnCode() + " Return message: " + cmdresponse.getReturnMessage());
                device.setDiscoveryResponse(cmdresponse.getReturnMessage());
                if (cmdresponse.getReturnCode().equalsIgnoreCase("0")) {
                    // read the generated XML file, look for managementIP
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(tempFile);

                    NodeList nList = doc.getElementsByTagName("ManagementIP");
                    String reportedManagerIP = null;
                    for (int temp = 0; temp < nList.getLength(); temp++) {

                        Node nNode = nList.item(temp);
                        reportedManagerIP = nNode.getTextContent();
                    }

                    // compare the management ip we found with the one we're discovering
                    if (reportedManagerIP != null && reportedManagerIP.equals(ipAddress)) {
                        retVal = DiscoverDeviceType.COMPELLENT;
                    } else {
                        LOGGER.info("Skipping compellent IP " + ipAddress + " because it is not the management IP, " + reportedManagerIP);
                    }
                }

                try {
                    tempFile.delete();
                } catch (Exception e) {
                    LOGGER.warn("could not delete temp file " + tempFile.getAbsolutePath() + ", ignoring.");
                }
            }
            else {
                LOGGER.debug("Check for Compellent didn't detect a known pattern in HTML response: " + compellentResponse);
                device.setDiscoveryResponse(compellentResponse);
            }
        } catch (Exception ioe) {
            LOGGER.debug("In DeviceTypeCheck.checkCompllent, could not connect to " + ipAddress + " because of " + ioe.getMessage() + ".  Ignoring.");
        }

        return retVal;
    }

    /**
     * @param ipAddress
     * @return
     */
    private static DiscoverDeviceType checkNetApp(String ipAddress, InfrastructureDevice device) {
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;

        CommandResponse cmdresponse;
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();

        CredentialEntity cred = getCredentialDAO().findById(device.getStorageCredentialId());

        LOGGER.debug("Checking for NetApp using script");
        try {
            String[] cmdAndArgs = {
                    "/opt/puppet/bin/ruby",
                    PUPPET_NETAPP_PATH + "/files/netapp_conn_check.rb",
                    ipAddress, cred.getUsername()
            };
            Map<String,String> envMap = new HashMap<>();
            envMap.put("PASSWORD", cred.getPassword());
            cmdresponse = cmdRunner.runCommandWithEnvironmentVariables(cmdAndArgs, envMap);
            device.setDiscoveryResponse(cmdresponse.getReturnMessage());
            if (cmdresponse.getReturnCode().equals("0")) {
                retVal = DiscoverDeviceType.NETAPP;
            }else{
                LOGGER.debug("NetApp identification command returned: " + cmdresponse.getReturnMessage());
            }

        } catch (Exception e) {
            LOGGER.debug("In DeviceTypeCheck.checkNetApp, could not connect to " + ipAddress + " because of " + e.getMessage() + ".  Ignoring.");
        }
        return retVal;
    }

    private static DiscoverDeviceType checkSCVMM(String ipAddress, InfrastructureDevice device) {
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;

        CommandResponse cmdresponse;
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();

        CredentialEntity cred = getCredentialDAO().findById(device.getScvmmCredentialId());

        try {
            String userName = (StringUtils.isNotEmpty(cred.getDomain()))?(cred.getDomain() + "\\" + cred.getUsername()):cred.getUsername();
            String[] cmdAndArgs = {
                    PUPPET_SCVMM_PATH + "/bin/discovery.rb",
                    "--quick", "--server", ipAddress,
                    "--username", userName
            };
            Map<String,String> envMap = new HashMap<>();
            envMap.put("PASSWORD",cred.getPassword());
            cmdresponse = cmdRunner.runCommandWithEnvironmentVariables(cmdAndArgs, envMap);
            device.setDiscoveryResponse(cmdresponse.getReturnMessage());
            if (cmdresponse.getReturnCode().equals("0")) {
                retVal = DiscoverDeviceType.SCVMM;
            }

        } catch (Exception e) {
            LOGGER.debug("In DeviceTypeCheck.checkSCVMM, could not connect to " + ipAddress + " because of " + e.getMessage() + ".  Ignoring.");
        }
        return retVal;
    }

    // Trust Manager which will NOT do any Cert checking
    static X509TrustManager tmNoCheck = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    static HostnameVerifier hv = new HostnameVerifier() {
        @Override
        public boolean verify(String urlHostName, SSLSession session) {
            return true;
        }
    };
    private static CredentialDAO credentialDAO;

    private static CredentialDAO getCredentialDAO() {
        if (null == credentialDAO) {
            credentialDAO = CredentialDAO.getInstance();
        }
        return credentialDAO;
    }

    private static DiscoverDeviceType checkEmcVnx(String ipAddress, InfrastructureDevice device) {
        DiscoverDeviceType retVal = DiscoverDeviceType.UNKNOWN;

        CommandResponse cmdresponse;
        ExecuteSystemCommands cmdRunner = ExecuteSystemCommands.getInstance();

        CredentialEntity cred = getCredentialDAO().findById(device.getStorageCredentialId());

        LOGGER.debug("Checking for EMC VNX using script");
        try {
            String[] cmdAndArgs = {
                    "/opt/puppet/bin/ruby",
                    PUPPET_EMC_VNX_PATH + "/files/emc_vnx_conn_check.rb",
                    ipAddress, cred.getUsername(), cred.getPassword()
            };
            cmdresponse = cmdRunner.runCommandWithConsoleOutput(cmdAndArgs);
            device.setDiscoveryResponse(cmdresponse.getReturnMessage());
            if (cmdresponse.getReturnCode().equals("0")) {
                retVal = DiscoverDeviceType.VNX;
            }
            else {
                LOGGER.debug("DeviceType was not EMC VNX.  EMC VNX identification command returned: " +
                        cmdresponse.getReturnMessage());
            }

        } catch (Exception e) {
            LOGGER.debug("In DeviceTypeCheck.checkEmcVnx, could not connect to " + ipAddress + " because of " +
                    e.getMessage() + ".  Ignoring.");
        }
        return retVal;
    }



}
