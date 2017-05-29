
package com.dell.asm.asmcore.asmmanager.client.discovery;


/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
import com.dell.asm.common.utilities.ValidatedInet4Address;
import com.dell.asm.common.utilities.ValidatedInet4Range;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;


import com.dell.pg.orion.common.print.Dump;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "DeviceDiscoveryRequest")
@ApiModel(value="Device Discovery Request",
description="Captures properties needed to perform a device discovery ")

public class DiscoverIPRangeDeviceRequest 
{
    @ApiModelProperty(value="Device Start IP address",notes="No format checking performed",required=true)	
    public static final String KEY_START_IP_ADDRESS = "START_IP_ADDRESS";

    @ApiModelProperty(value="Credential Ref Name",required=true)
    public static final String KEY_DEVICE_CRED_REF = "DEVICE_CRED_REF";

    @ApiModelProperty(value="Device End IP address",required=false)
    public static final String KEY_END_IP_ADDRESS = "END_IP_ADDRESS";


    private String deviceChassisCredRef;
    private String deviceServerCredRef;
    private String deviceSwitchCredRef;
    private String deviceStorageCredRef;
    private String deviceVCenterCredRef;
    private String deviceBMCServerCredRef;

    public boolean isUnmanaged() {
        return unmanaged;
    }

    public void setUnmanaged(boolean unmanaged) {
        this.unmanaged = unmanaged;
    }

    private boolean unmanaged = false;

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    private boolean reserved = false;

    public String getDeviceSCVMMCredRef() {
        return deviceSCVMMCredRef;
    }

    public void setDeviceSCVMMCredRef(String deviceSCVMMCredRef) {
        this.deviceSCVMMCredRef = deviceSCVMMCredRef;
    }

    private String deviceSCVMMCredRef;
    private String deviceStartIp;
    private String deviceEndIp;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    private String config;

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    private DeviceType deviceType;

    public String getDeviceEMCredRef() {
        return deviceEMCredRef;
    }

    public void setDeviceEMCredRef(String deviceEMCredRef) {
        this.deviceEMCredRef = deviceEMCredRef;
    }

    private String deviceEMCredRef;

    public String getServerPoolId() {
        return serverPoolId;
    }

    public void setServerPoolId(String serverPoolId) {
        this.serverPoolId = serverPoolId;
    }

    private String serverPoolId;

    public DiscoverIPRangeDeviceRequest(DiscoverIPRangeDeviceRequest req) {
        this.setDeviceStartIp(req.getDeviceStartIp());
        this.setDeviceEndIp(req.getDeviceEndIp());
        this.setDeviceChassisCredRef(req.deviceChassisCredRef);
        this.setDeviceServerCredRef(req.getDeviceServerCredRef());
        this.setDeviceBMCServerCredRef(req.getDeviceBMCServerCredRef());
        this.setDeviceEMCredRef(req.getDeviceEMCredRef());
    }

    public DiscoverIPRangeDeviceRequest() {
        // TODO Auto-generated constructor stub
    }




    public String getDeviceStartIp() {
        return deviceStartIp;
    }




    public void setDeviceStartIp(String deviceStartIp) {
        this.deviceStartIp = deviceStartIp;
    }




    public String getDeviceEndIp() {
        return deviceEndIp;
    }




    public void setDeviceEndIp(String deviceEndIp) {
        this.deviceEndIp = deviceEndIp;
    }



    //Dump contents.
    @Override
    public String toString()
    {
        return Dump.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscoverIPRangeDeviceRequest)) return false;
        if (!super.equals(o)) return false;

        DiscoverIPRangeDeviceRequest request = (DiscoverIPRangeDeviceRequest) o;


        if (deviceServerCredRef != null && ! (deviceServerCredRef.equals(request.deviceServerCredRef)))
            return false;
        if (deviceChassisCredRef != null && ! (deviceChassisCredRef.equals(request.deviceChassisCredRef)))
            return false;
        if (deviceSwitchCredRef != null && ! (deviceSwitchCredRef.equals(request.deviceSwitchCredRef)))
            return false;
        if (deviceStorageCredRef != null && ! (deviceStorageCredRef.equals(request.deviceStorageCredRef)))
            return false;
        if (deviceVCenterCredRef != null && ! (deviceVCenterCredRef.equals(request.deviceVCenterCredRef)))
            return false;
        if (deviceSCVMMCredRef != null && ! (deviceSCVMMCredRef.equals(request.deviceSCVMMCredRef)))
            return false;
        if (deviceEMCredRef != null && !(deviceEMCredRef.equals(request.deviceEMCredRef)))
            return false;

        if (deviceStartIp != null ? !deviceStartIp.equals(request.deviceStartIp) : request.deviceStartIp != null)
            return false;
        if (deviceEndIp != null ? !deviceEndIp.equals(request.deviceEndIp) : request.deviceEndIp != null)
            return false;
        if (serverPoolId != null ? !serverPoolId.equals(request.serverPoolId) : request.serverPoolId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (deviceChassisCredRef != null ? deviceChassisCredRef.hashCode() : 0);
        result = 31 * result + (deviceServerCredRef != null ? deviceServerCredRef.hashCode() : 0);
        result = 31 * result + (deviceBMCServerCredRef != null ? deviceBMCServerCredRef.hashCode() : 0);
        result = 31 * result + (deviceEMCredRef != null ? deviceEMCredRef.hashCode() : 0);
        result = 31 * result + (deviceStorageCredRef != null ? deviceStorageCredRef.hashCode() : 0);
        result = 31 * result + (deviceSwitchCredRef != null ? deviceSwitchCredRef.hashCode() : 0);
        result = 31 * result + (deviceVCenterCredRef != null ? deviceVCenterCredRef.hashCode() : 0);
        result = 31 * result + (deviceSCVMMCredRef != null ? deviceSCVMMCredRef.hashCode() : 0);
        result = 31 * result + (deviceStartIp != null ? deviceStartIp.hashCode() : 0);
        result = 31 * result + (deviceEndIp != null ? deviceEndIp.hashCode() : 0);
        result = 31 * result + (serverPoolId != null ? serverPoolId.hashCode() : 0);
        return result;
    }




    public String getDeviceChassisCredRef() {
        return deviceChassisCredRef;
    }




    public void setDeviceChassisCredRef(String deviceChassisCredRef) {
        this.deviceChassisCredRef = deviceChassisCredRef;
    }




    public String getDeviceServerCredRef() {
        return deviceServerCredRef;
    }




    public void setDeviceServerCredRef(String deviceServerCredRef) {
        this.deviceServerCredRef = deviceServerCredRef;
    }




    public String getDeviceSwitchCredRef() {
        return deviceSwitchCredRef;
    }




    public void setDeviceSwitchCredRef(String deviceSwitchCredRef) {
        this.deviceSwitchCredRef = deviceSwitchCredRef;
    }




    public String getDeviceStorageCredRef() {
        return deviceStorageCredRef;
    }




    public void setDeviceStorageCredRef(String deviceStorageCredRef) {
        this.deviceStorageCredRef = deviceStorageCredRef;
    }




    public String getDeviceVCenterCredRef() {
        return deviceVCenterCredRef;
    }




    public void setDeviceVCenterCredRef(String deviceVCenterCredRef) {
        this.deviceVCenterCredRef = deviceVCenterCredRef;
    }


    /**
     * Expand the IP range and validate
     *
     * @param discoverIpRange  DiscoverIPRangeDeviceRequest
     * @return list of ips or error
     */
    public static List<String> expandIpAddresses(DiscoverIPRangeDeviceRequest discoverIpRange) throws IllegalArgumentException {
        List<String> ipAddresslist = new ArrayList<>();

        try {
            if (discoverIpRange.getDeviceStartIp() == null || discoverIpRange.getDeviceStartIp().isEmpty()) {
                String msg = "invalid IP or range";
                throw new IllegalArgumentException(msg);
            } else if (discoverIpRange.getDeviceEndIp() == null || discoverIpRange.getDeviceEndIp().isEmpty()) {
                // just add the first IP
                new ValidatedInet4Address(discoverIpRange.getDeviceStartIp());
                ipAddresslist.add(discoverIpRange.getDeviceStartIp());
            } else {

                // First check if IPs first 3 parts are same
                validateIpSameSubnet(discoverIpRange.getDeviceStartIp(), discoverIpRange.getDeviceEndIp());

                // we have the range specified
                ValidatedInet4Range validatedRange = new ValidatedInet4Range(discoverIpRange.getDeviceStartIp(), discoverIpRange.getDeviceEndIp());
                List<String> addressStrings = validatedRange.getAddressStrings();
                for (String address : addressStrings) {
                    ipAddresslist.add(address);
                }
            }
        } catch (RuntimeException re) {
            throw new IllegalArgumentException(re.getMessage());
        }
        return ipAddresslist;
    }


    public static void validateIpSameSubnet(String ip1, String ip2) throws IllegalArgumentException {
        if ((ip1 != null && !ip1.isEmpty()) && (ip2 != null && !ip2.isEmpty())) {
            if (!ip1.substring(0, ip1.lastIndexOf(".")).equalsIgnoreCase(ip2.substring(0, ip2.lastIndexOf(".")))) {
                throw new IllegalArgumentException("Invalid IP range: IP not on the same subnet");
            }
        }
    }

    public String getDeviceBMCServerCredRef() {
        return deviceBMCServerCredRef;
    }

    public void setDeviceBMCServerCredRef(String deviceBMCServerCredRef) {
        this.deviceBMCServerCredRef = deviceBMCServerCredRef;
    }
}
