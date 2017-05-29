/**************************************************************************
 *   Copyright (c) 2013 - 2015 Dell Inc. All rights reserved.             *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.client.vsphere.ClusterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.DatacenterDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.DatastoreDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.FolderDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.HostDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ManagedObjectDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.PortGroupDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.ResourcePoolDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VDSDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VMTemplateDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VirtualAppDTO;
import com.dell.asm.asmcore.asmmanager.client.vsphere.VirtualMachineDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
  This class is mostly used to parse out vcenter inventory separate from the ServiceTemplate code
 */
public class VcenterInventoryUtils {

    private static final Logger LOGGER = Logger.getLogger(VcenterInventoryUtils.class);

    public static ManagedObjectDTO convertPuppetDeviceDetailsToDto(Map<String, String> deviceDetails) throws IOException {
        HashMap<String, Object> inventory = new ObjectMapper().readValue(deviceDetails.get("inventory"), HashMap.class);
        return parseInventoryObjectToDto(inventory);
    }

    private static ManagedObjectDTO parseInventoryObjectToDto(HashMap<String, Object> obj){
        ManagedObjectDTO dto;
        String type = (String)obj.get("type");
        HashMap<String, Object> attributes = (HashMap<String, Object>) obj.get("attributes");
        switch(type) {
            case "Datacenter":
                dto = new DatacenterDTO();
                break;
            case "Datastore":
                dto = new DatastoreDTO();
                break;
            case "ComputeResource":
            case "ClusterComputeResource":
                dto = new ClusterDTO();
                break;
            case "HostSystem":
                dto = new HostDTO();
                break;
            case "ResourcePool":
                dto = new ResourcePoolDTO();
                break;
            case "Folder":
                dto = new FolderDTO();
                break;
            case "VirtualMachine":
                boolean isTemplate = (Boolean) attributes.get("template");
                if(isTemplate)
                    dto = new VMTemplateDTO();
                else
                    dto = new VirtualMachineDTO();
                break;
            case "VirtualApp":
                dto = new VirtualAppDTO();
                break;
            case "VmwareDistributedVirtualSwitch":
                dto = new VDSDTO();
                break;
            case "DistributedVirtualPortgroup":
                dto = new PortGroupDTO();
                break;

            default:
                //LOGGER.warn("Unrecognized vcenter device type: " + type + "; setting to folder");
                dto = new FolderDTO();
        }
        dto.setAttributes(attributes);
        dto.setObjType(type);
        dto.setName((String)obj.get("name"));
        List<ManagedObjectDTO> dtoChildren = new ArrayList<>();
        ArrayList<HashMap<String, Object> > objChildren = (ArrayList) obj.get("children");
        for( HashMap<String, Object> child: objChildren){
            dtoChildren.add(parseInventoryObjectToDto(child));
        }
        dto.setChildren(dtoChildren);
        return dto;
    }
}
