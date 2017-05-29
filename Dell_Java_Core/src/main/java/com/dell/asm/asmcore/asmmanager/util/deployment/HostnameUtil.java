/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.HostUnauthorizedException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.StaticNetworkConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent.ServiceTemplateComponentType;
import com.dell.asm.asmcore.asmmanager.client.util.ServiceTemplateClientUtil;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.common.utilities.ASMCommonsUtils;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;

public class HostnameUtil {
    public static final String NUM_PATTERN = "${num}";
    public static final String TAG_PATTERN = "${service_tag}";
    public static final String MODEL_PATTERN = "${model}";
    public static final String VENDOR_PATTERN = "${vendor}";
    public static final String DNS_PATTERN = "${dns}";

    private static final Logger LOGGER = Logger.getLogger(HostUnauthorizedException.class);

    private final DnsUtil dnsUtil;

    public HostnameUtil(DnsUtil dnsUtil) {
        this.dnsUtil = dnsUtil;
    }

    public HostnameUtil() {
        this(new DnsUtil());
    }

    private String safeName(String name) {
        if (name == null) return null;
        name = name.replaceAll("[^A-Za-z0-9]", "");
        return name;
    }

    public static boolean isValidHostNameTemplate(String hostName, ServiceTemplateComponent component) {
        // must not be empty and must contain a pattern that can result in a unique name
        if (StringUtils.isBlank(hostName) ||
                !(hostName.contains(NUM_PATTERN)
                        || hostName.contains(TAG_PATTERN)
                        || hostName.contains(DNS_PATTERN))) {
            return false;
        }

        // replace known patterns
        hostName = hostName.replaceAll(Pattern.quote(NUM_PATTERN), "1");
        hostName = hostName.replaceAll(Pattern.quote(TAG_PATTERN), "123");
        hostName = hostName.replaceAll(Pattern.quote(MODEL_PATTERN), "620");
        hostName = hostName.replaceAll(Pattern.quote(VENDOR_PATTERN), "Dell");
        hostName = hostName.replaceAll(Pattern.quote(DNS_PATTERN), "host");

        return isValidHostName(hostName, component);
    }
    
    public static boolean isValidHostNameTemplateForVMs(String hostName, ServiceTemplateComponent component) {
        // must not be empty and must contain a number pattern for VMs
        if (StringUtils.isBlank(hostName) ||
                !(hostName.contains(NUM_PATTERN))) {
            return false;
        }

        // replace known patterns
        hostName = hostName.replaceAll(Pattern.quote(NUM_PATTERN), "123");

        return isValidHostName(hostName, component);
    }

    /**
     * Linux and Windows hostnames use different rules for validation.
     * @param hostName  Hostname to validate.
     * @param component    The component will be used to determine what hostname validation rule to apply.
     * @return
     */
    public static boolean isValidHostName(String hostName, ServiceTemplateComponent component) {
        boolean isWindows = false;
        boolean isEsxi = false;
        ServiceTemplateSetting imageType = component.getTemplateSetting(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_OS_TYPE_ID);
        if (imageType!=null) {
            List<String> winTargets = new ArrayList<>();
            List<String> esxiTargets = new ArrayList<>();
            winTargets.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2012_VALUE);
            winTargets.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_WINDOWS2008_VALUE);
            winTargets.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_HYPERV_VALUE);
            esxiTargets.add(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_ESXI_VALUE);
            isWindows = winTargets.contains(imageType.getValue());
            isEsxi = esxiTargets.contains(imageType.getValue());
        }else{
            // hyper-v cloned vm?
            if (component.getTemplateResource(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_HV_VM_RESOURCE)!=null) {
                isWindows = true;
            }
        }

        if (isWindows && component.getType() != ServiceTemplateComponentType.VIRTUALMACHINE)
            return ASMCommonsUtils.isValidWindowsHostName(hostName);
        if (isEsxi)
            return ASMCommonsUtils.isValidEsxiHostName(hostName);
        if (component.getType() == ServiceTemplateComponentType.VIRTUALMACHINE)
        	return ASMCommonsUtils.isValidVmHostName(hostName);
        else
            return ASMCommonsUtils.isValidHostName(hostName);
    }

    public static boolean containsDnsPattern(String hostnameTemplate) {
        return hostnameTemplate != null && hostnameTemplate.contains(HostnameUtil.DNS_PATTERN);
    }

    /**
     * Auto-generate name for template that has only numeric pattern ${num}
     * @param nameTemplate template with pattern
     * @param allNames list of used names
     * @return generated name
     */
    public static String generateNameFromNumTemplate(String nameTemplate, Set<String> allNames){
        nameTemplate = nameTemplate.toLowerCase();

        if (nameTemplate.contains(NUM_PATTERN)) {
            nameTemplate = replaceNumPattern(nameTemplate, allNames);
        }

        return nameTemplate;
    }
    
    public String generateHostname(String hostnameTemplate, ServiceTemplateComponent component,
                                   DeviceInventoryEntity server, Set<String> allHostnames) {
        if (server!=null) {
            if (hostnameTemplate.contains(TAG_PATTERN)) {
                hostnameTemplate = hostnameTemplate.replaceAll(Pattern.quote(TAG_PATTERN),
                        safeName(server.getServiceTag()));
            }

            if (hostnameTemplate.contains(MODEL_PATTERN)) {
                hostnameTemplate = hostnameTemplate.replaceAll(Pattern.quote(MODEL_PATTERN),
                        safeName(server.getModel()));
            }


            if (hostnameTemplate.contains(VENDOR_PATTERN)) {
                hostnameTemplate = hostnameTemplate.replaceAll(Pattern.quote(VENDOR_PATTERN),
                        safeName(server.getVendor()));
            }
        }
        if (hostnameTemplate.contains(DNS_PATTERN)) {
            hostnameTemplate = replaceDnsPattern(hostnameTemplate, component);
        }

        hostnameTemplate = hostnameTemplate.toLowerCase();

        if (hostnameTemplate.contains(NUM_PATTERN)) {
            hostnameTemplate = replaceNumPattern(hostnameTemplate, allHostnames);
        }

        return hostnameTemplate;
    }

    String stripDnsSuffix(String hostname, String suffix) {
        // Strip trailing .
        hostname = StringUtils.removeEnd(hostname, ".");
        suffix = StringUtils.removeEnd(suffix, ".");

        // Remove suffix
        if (suffix != null) {
            if (hostname.endsWith(suffix)) {
                hostname = hostname.substring(0, hostname.length() - suffix.length());
            }
        }

        return StringUtils.removeEnd(hostname, ".");
    }

    String replaceDnsPattern(String hostnameTemplate, ServiceTemplateComponent component) {
        List<Network> networks = ServiceTemplateClientUtil.findManagementNetworks(component);
        if (networks != null) {
            StaticNetworkConfiguration config = null;
            for (Network network : networks) {
                if (network.isStatic() && network.getStaticNetworkConfiguration() != null) {
                    config = network.getStaticNetworkConfiguration();
                    break;
                }
            }

            if (config != null && !StringUtils.isBlank(config.getIpAddress())) {
                if (StringUtils.isBlank(config.getPrimaryDns()) && StringUtils.isBlank(config.getSecondaryDns())) {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.noDnsServersForManagementNetwork(component.getName()));
                }
                String hostname = dnsUtil.reverseLookup(config.getIpAddress(),
                        config.getPrimaryDns(), config.getSecondaryDns());
                if (hostname != null) {
                    hostname = stripDnsSuffix(hostname, config.getDnsSuffix());
                    return hostnameTemplate.replaceAll(Pattern.quote(DNS_PATTERN),
                            hostname.toLowerCase());
                } else {
                    throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                            AsmManagerMessages.reverseDnsLookupFailed(config.getIpAddress(),
                                    config.getPrimaryDns()));
                }
            }
        }

        throw new LocalizedWebApplicationException(Response.Status.BAD_REQUEST,
                AsmManagerMessages.staticIpRequiredForDnsHostnameTemplate());
    }

    public static String replaceNumPattern(String hostnameTemplate, Set<String> allHostnames) {
        // we might already have the same hostname with auto-generated numbers. There is no way
        // to keep the counter updated, thus we have to check for dup every time we replace
        // ${num} with next gen number.
        String hostnameWithNumPattern = hostnameTemplate;

        int hostNameCounter = 0;
        while (hostnameWithNumPattern != null && hostNameCounter <= allHostnames.size()) {
            while (hostnameWithNumPattern.contains(NUM_PATTERN)) {
                hostNameCounter++;
                hostnameWithNumPattern = hostnameWithNumPattern.replaceFirst(
                        Pattern.quote(NUM_PATTERN),
                        String.valueOf(hostNameCounter));
            }

            if (!allHostnames.contains(hostnameWithNumPattern)) {
                hostnameTemplate = hostnameWithNumPattern;
                break;
            } else {
                // number pattern was already used, try again with next gen, reset the original name to get pattern back
                hostnameWithNumPattern = hostnameTemplate;
            }
        }
        return hostnameTemplate;
    }

    /**
     * Returns true if hostname tempate contains parts specofoc for a server i.e. serviceTag, model, vendor
     * @param hostnameTemplate
     * @return
     */
    public static boolean mustRegenerateHostname(String hostnameTemplate) {
        return (hostnameTemplate.contains(TAG_PATTERN) ||
                hostnameTemplate.contains(MODEL_PATTERN)||
                hostnameTemplate.contains(VENDOR_PATTERN));
    }

}
