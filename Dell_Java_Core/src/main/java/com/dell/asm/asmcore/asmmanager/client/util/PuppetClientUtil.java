package com.dell.asm.asmcore.asmmanager.client.util;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedDevice;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.common.utilities.ASMCommonsMessages;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class PuppetClientUtil
{
    private static final Logger logger = Logger.getLogger(PuppetClientUtil.class);

    /**
     * Get Puppet module name.
     * @param discoverDeviceType
     * @param deviceType
     * @return
     */
    public static String getPuppetModuleName(DiscoverDeviceType discoverDeviceType, DeviceType deviceType) {
        String puppetModuleName = null;
        if (discoverDeviceType != null) {
            puppetModuleName = discoverDeviceType.getPuppetModuleName();
        }
        if (puppetModuleName == null && deviceType != null) {
            puppetModuleName = deviceType.name();
        }
        return StringUtils.lowerCase(puppetModuleName);
    }

    /**
     * Puppet certificates must be all lower-case and they must not be all digits. We
     * tag it with the device type to ensure its not all digits.
     * discoveredDiscoveredType, deviceType and serviceTag
     *
     * @param discoverDeviceType
     * @param deviceType
     * @param serviceTag
     * @return
     */
    public static String toCertificateName(DiscoverDeviceType discoverDeviceType, DeviceType deviceType, String serviceTag) {
        String certNamePrefix;
        switch (deviceType) {
        case BladeServer:
        case RackServer:
        case TowerServer:
        case FXServer:
            // For historical reasons the cert names for dell servers start with
            // "bladeserver" or "rackserver", but the puppet module name is "idrac".
            certNamePrefix = deviceType.name().toLowerCase();
            break;
        default:
            certNamePrefix = getPuppetModuleName(discoverDeviceType, deviceType);
            if (certNamePrefix == null) {
                logger.error("Failed to get puppet module name for type " + discoverDeviceType + " " + serviceTag);
                throw new LocalizedWebApplicationException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        ASMCommonsMessages.internalServerError());
            }
            break;
        }

        return certNamePrefix + "-" + serviceTag.toLowerCase();
    }

    /**
     * Get puppet device cert name.
     * @param device
     * @return
     */
    public static String toCertificateName(ManagedDevice device) {
        return toCertificateName(device.getDiscoverDeviceType(), device.getDeviceType(), device.getServiceTag());
    }

}
