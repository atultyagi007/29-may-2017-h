package com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "DeviceIdentity")
public class DeviceIdentity {

    private String deviceRef;
    private ChassisIdentity chassisIdentity;
    private ServerIdentity serverIdentity;

    public DeviceIdentity(DeviceIdentity req) {
        this.deviceRef = req.getDeviceRef();
        this.chassisIdentity = req.getChassisIdentity();
        this.serverIdentity = req.getServerIdentity();
    }
    
    public DeviceIdentity() {
        // TODO Auto-generated constructor stub
    }
    public String getDeviceRef() {
        return deviceRef;
    }
    public void setDeviceRef(String deviceRef) {
        this.deviceRef = deviceRef;
    }
    public ChassisIdentity getChassisIdentity() {
        return chassisIdentity;
    }
    public void setChassisIdentity(ChassisIdentity chassisIdentity) {


        this.chassisIdentity = chassisIdentity;
    }
    public ServerIdentity getServerIdentity() {

        return serverIdentity;
    }
    public void setServerIdentity(ServerIdentity serverIdentity) {
        this.serverIdentity = serverIdentity;
    }
   
}
