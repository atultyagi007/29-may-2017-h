/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.deployment;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeleteResourceRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeleteResourceRequest 
{
    public String serviceId;
    /*
     * Lists of components ID to be removed from service.
     */
    private List<String> applicationList;
    private List<String> serverList;
    private List<String> vmList;
    private List<String> clusterList;
    private List<String> volumeList;
    
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public List<String> getApplicationList() {
		return applicationList;
	}
	public void setApplicationList(List<String> applicationList) {
		this.applicationList = applicationList;
	}
	public List<String> getServerList() {
		return serverList;
	}
	public void setServerList(List<String> serverList) {
		this.serverList = serverList;
	}
	public List<String> getVmList() {
		return vmList;
	}
	public void setVmList(List<String> vmList) {
		this.vmList = vmList;
	}
	public List<String> getClusterList() {
		return clusterList;
	}
	public void setClusterList(List<String> clusterList) {
		this.clusterList = clusterList;
	}
	public List<String> getVolumeList() {
		return volumeList;
	}
	public void setVolumeList(List<String> volumeList) {
		this.volumeList = volumeList;
	}
    
    
}
