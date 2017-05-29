package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "IpAddressing")
public class IpAddressing {
	
	AsmIpTypeEnum chassisIpType;
	String chassisNetworkRef;
	AsmIpTypeEnum serverIpType;
	String serverNetworkRef;
	AsmIpTypeEnum iomIpType;
	String iomNetworkRef;
	
	
	public AsmIpTypeEnum getChassisIpType() {
		return chassisIpType;
	}
	public void setChassisIpType(AsmIpTypeEnum chassisIpType) {
		this.chassisIpType = chassisIpType;
	}
	public String getChassisNetworkRef() {
		return chassisNetworkRef;
	}
	public void setChassisNetworkRef(String chassisNetworkRef) {
		this.chassisNetworkRef = chassisNetworkRef;
	}
	public AsmIpTypeEnum getServerIpType() {
		return serverIpType;
	}
	public void setServerIpType(AsmIpTypeEnum serverIpType) {
		this.serverIpType = serverIpType;
	}
	public String getServerNetworkRef() {
		return serverNetworkRef;
	}
	public void setServerNetworkRef(String serverNetworkRef) {
		this.serverNetworkRef = serverNetworkRef;
	}
	public AsmIpTypeEnum getIomIpType() {
		return iomIpType;
	}
	public void setIomIpType(AsmIpTypeEnum iomIpType) {
		this.iomIpType = iomIpType;
	}
	public String getIomNetworkRef() {
		return iomNetworkRef;
	}
	public void setIomNetworkRef(String iomNetworkRef) {
		this.iomNetworkRef = iomNetworkRef;
	}
	
	

}
