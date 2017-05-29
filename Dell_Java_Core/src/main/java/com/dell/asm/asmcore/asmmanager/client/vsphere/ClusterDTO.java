/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.vsphere;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlRootElement(name = "cluster")
public class ClusterDTO extends ManagedObjectDTO {

	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).toString();
	}
    
    /**
     * Returns a boolean indicating whether Virtual Distributed Switch is enabled for any Host in this cluster.
     * 
     * @return a boolean indicating whether Virtual Distributed Switch is enabled for any Host in this cluster.
     */
    public boolean isVdsEnabled() {
    	boolean isVdsEnabled = false;
    	
    	List<PortGroupDTO> portGroups = this.getPortGroups();
    	if (portGroups != null && !portGroups.isEmpty()) {
    		for (PortGroupDTO portGroup : portGroups) {
    			if (DISTRIBUTED_VIRTUAL_PORT_GROUP.equals(portGroup.getObjType())) {
    				isVdsEnabled = true;
    				break;
    			}
    		}
    	}
    	
    	return isVdsEnabled;
    }
    
}
