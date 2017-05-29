/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.vsphere;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

/**
 * 
 */
public class ClusterDTOTest {

    
    @Test
    public void testIsVdsEnabled() {
        
        ClusterDTO clusterDto = new ClusterDTO();
        PortGroupDTO portGroupDTO = new PortGroupDTO();
        portGroupDTO.setObjType(ClusterDTO.DISTRIBUTED_VIRTUAL_PORT_GROUP);
        ArrayList<ManagedObjectDTO> portGroups = new ArrayList<ManagedObjectDTO>();
        portGroups.add(portGroupDTO);
        clusterDto.setChildren(portGroups);
        
        boolean isVdsEnabled = clusterDto.isVdsEnabled();
        assertTrue("VDS should be enabled but is false!", isVdsEnabled);
        
        portGroupDTO = new PortGroupDTO();
        portGroupDTO.setObjType("some other port type");
        portGroups.clear();
        portGroups.add(portGroupDTO);

        clusterDto.setChildren(portGroups);
        
        isVdsEnabled = clusterDto.isVdsEnabled();
        assertTrue("VDS should NOT be enabled but is true!", !isVdsEnabled);
    }
    
}
