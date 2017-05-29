/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.discovery.providers;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoverDeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.chassis.client.device.IOM;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

public class SwitchForce10 extends PuppetDevice {
    private static final Logger logger = Logger.getLogger(SwitchForce10.class);

    protected void mergeFacts(Map<String, String> factLabelToValue, DiscoveredDevices result, FirmwareDeviceInventoryEntity fwPuppetInv) {
        result.setDeviceType(getInventoryDeviceType());

        // this could be IOM or TOR. Try as IOM first.
        String service_tag = getIomServiceTag(result.getIpAddress());

        if (service_tag!=null)
            result.setServiceTag(service_tag);
        else {
            service_tag = factLabelToValue.get("system_management_unit_service_tag");
            if (service_tag != null) {
                if (!service_tag.contains("N/A"))
                    result.setServiceTag(service_tag);
                else
                    service_tag = null;
            }

            if (service_tag == null) {
                // if we are sure it is IOM, quit here
                if (result.getDiscoverDeviceType() == DiscoverDeviceType.FORCE10IOM || result.getDiscoverDeviceType() == DiscoverDeviceType.FX2_IOM) {
                    // this is the ERROR, must skip and find the reason
                    logger.error("Cannot find service tag for IOM: " + result.getIpAddress());
                    throw new LocalizedWebApplicationException(
                            Response.Status.INTERNAL_SERVER_ERROR,
                            AsmManagerMessages.internalError());
                }else{
                    // for TOR service tag is not that important. We can use any unique value, i.e. cert name
                    result.setServiceTag(factLabelToValue.get("name"));
                }
            }
        }

        result.setDisplayName(factLabelToValue.get("hostname"));

        if(factLabelToValue.get("model") != null)
            result.setModel(factLabelToValue.get("model"));
        else
            result.setModel("NOT FOUND");
        if(factLabelToValue.get("dell_force10_application_software_version") != null)
            fwPuppetInv.setVersion(factLabelToValue.get("dell_force10_application_software_version"));
        fwPuppetInv.setComponentType("FRMW");
        genericPuppetInvDetails(fwPuppetInv, result);
        setFirmwareComponentIDSettable(true);
    }

    private String getIomServiceTag(String ipAddress) {
        IOM iom = null;
        try {
            iom = ProxyUtil.getDeviceChassisProxyWithHeaderSet().getIomByIP(ipAddress);
        }catch(Exception e) {
            logger.error("Cannot fetch IOM by IP address: " + ipAddress, e);
        }
        if (iom!=null)
            return iom.getServiceTag();
        else
            return null;
    }
}
