/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmdeployer.client;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

@XmlRootElement(name = "device")
public class AsmDeployerDevice {
    String certName;
    String host;
    String port;
    String path;
    String scheme;
    Map<String, String> arguments = new HashMap<>();
    String query;
    String username;
    String password;
    String encodedPassword;
    String url;
    String provider;
    Map<String, String> facts = new HashMap<>();
    AsmDeviceStatusType discoveryStatus;

    @XmlElement(name = "cert_name")
    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    @XmlElement(name = "user")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement(name = "pass")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "host")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @XmlElement(name = "port")
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @XmlElement(name = "path")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @XmlElement(name = "scheme")
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    @XmlElement(name = "query")
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @XmlElement(name = "enc_password")
    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    @XmlElement(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name = "provider")
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @XmlElement(name = "facts")
    public Map<String, String> getFacts() {
        return facts;
    }

    public void setFacts(Map<String, String> facts) {
        this.facts = facts;
    }

    @XmlElement(name = "discovery_status")
    public AsmDeviceStatusType getDiscoveryStatus() {
        return discoveryStatus;
    }

    public void setDiscoveryStatus(AsmDeviceStatusType discoveryStatus) {
        this.discoveryStatus = discoveryStatus;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("certName", certName)
                .add("host", host)
                .add("discoveryStatus", discoveryStatus)
                .toString();
    }
}
