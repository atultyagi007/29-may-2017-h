/**************************************************************************
 *   Copyright (c) 2012 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.vsphere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlRootElement(name = "managedObject")
@XmlType(name = "ManagedObject", propOrder = {
        "name",
        "objType",
        "objValue",
        "children",
        "attributes"
})
@XmlSeeAlso({ VCenterRootDTO.class, ClusterDTO.class, DatacenterDTO.class, DatastoreDTO.class, HostDTO.class,
        ResourcePoolDTO.class, FolderDTO.class, VirtualMachineDTO.class, VMTemplateDTO.class, VirtualAppDTO.class})
public abstract class ManagedObjectDTO {
    private String name;
    private String objType;
    private String objValue;
    private HashMap<String, Object> attributes = new HashMap<String,Object>();
    private List<ManagedObjectDTO> children = new ArrayList<>();
    
    public final static String DISTRIBUTED_VIRTUAL_PORT_GROUP = "DistributedVirtualPortgroup";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public String getObjValue() {
        return objValue;
    }

    public void setObjValue(String objValue) {
        this.objValue = objValue;
    }

    @XmlElementRef
    public List<ManagedObjectDTO> getChildren() {
        return children;
    }

    public void setChildren(List<ManagedObjectDTO> children) {
        this.children = children;
    }

    public <T> List<T> getAllDescendantsByType(Class<T> klazz){
        return getChildrenHelper(klazz, true);
    }

    public <T> List<T> getChildrenByType(Class<T> klazz) {
        return getChildrenHelper(klazz, false);
    }

    private <T> List<T> getChildrenHelper(Class<T> klazz, boolean getAllDescendants){
        List<T> ret = new ArrayList<>();
        for (ManagedObjectDTO child : children) {
            if (klazz.isAssignableFrom(child.getClass())) {
                T cast = klazz.cast(child);
                ret.add(cast);
            }
            if(getAllDescendants) {
                ret.addAll(child.getChildrenHelper(klazz, getAllDescendants));
            }
        }
        return ret;
    }

    public HashMap<String, Object> getAttributes() { return attributes; }
    
    public Object getAttribute(String attributeName){
        return this.attributes.get(attributeName);
    }

    public void setAttributes(HashMap<String, Object> attributes) { this.attributes = attributes; }

    public List<ClusterDTO> getClusters() {
        return getChildrenHelper(ClusterDTO.class, true);
    }

    public List<DatacenterDTO> getDatacenters() {
        return getChildrenHelper(DatacenterDTO.class, true);
    }

    public List<HostDTO> getHosts() {
        return getChildrenHelper(HostDTO.class, true);
    }

    public List<ResourcePoolDTO> getResourcePools() {
        return getChildrenHelper(ResourcePoolDTO.class, true);
    }

    public List<VirtualMachineDTO> getVirtualMachines() {
        return getChildrenHelper(VirtualMachineDTO.class, true);
    }
    
    public List<DatastoreDTO> getDatastores() {
        return getChildrenHelper(DatastoreDTO.class, true);
    }
    
    /**
     * Returns the Cluster with the given name, or null if a Cluster with the name cannot be found.
     * 
     * @param clusterName the name of the Cluster to return.
     * @return the Cluster with the given name, or null if a Cluster with the name cannot be found.
     */
    public ClusterDTO getCluster(String clusterName){
        ClusterDTO clusterDtoReturn = null;
        
        if(clusterName != null){
            List<ClusterDTO> clusters = this.getClusters();
            if(clusters != null){
                for(ClusterDTO clusterDto : clusters){
                    if(clusterName.equals(clusterDto.getName())){
                        clusterDtoReturn = clusterDto;
                        break;
                    }
                }
            }
        }
        
        return clusterDtoReturn;
    }
    
    /**
     * Returns the Datacenter with the given name, or null if a Datacenter with the name cannot be found.
     * 
     * @param datacenterName the name of the Datacenter to return.
     * @return the Datacenter with the given name, or null if a Datacenter with the name cannot be found.
     */
    public DatacenterDTO getDatacenter(String datacenterName){
        DatacenterDTO datacenterDtoReturn = null;
        
        if(datacenterName != null){
            List<DatacenterDTO> datacenters = this.getDatacenters();
            if(datacenters != null){
                for(DatacenterDTO datacenterDto : datacenters){
                    if(datacenterName.equals(datacenterDto.getName())){
                        datacenterDtoReturn = datacenterDto;
                        break;
                    }
                }
            }
        }
        
        return datacenterDtoReturn;
    }

    public List<VDSDTO> getVDSs() {
        return getChildrenHelper(VDSDTO.class, true);
    }

    public List<PortGroupDTO> getPortGroups() {
        return getChildrenHelper(PortGroupDTO.class, true);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("objValue", objValue)
                .append("objType", objType)
                .append("name", name)
                .toString();
    }
    
    /**
     * Returns the Datacenter that the cluster belongs to or null if the Cluster is not part of any given Datacenter.
     * 
     * @param cluster the cluster that will be searched for in the VCenter's Datacenters.
     * @return the Datacenter that the cluster belongs to or null if the Cluster is not part of any given Datacenter.
     */
    public DatacenterDTO getDatacenter(ClusterDTO clusterToFind) {
    	DatacenterDTO datacenterFoundIn = null;
    	
    	List<DatacenterDTO> datacenters = this.getDatacenters();
    	if (clusterToFind != null && datacenters != null && !datacenters.isEmpty()) {
    		for (DatacenterDTO datacenter : datacenters) {
    			ClusterDTO cluster = datacenter.getCluster(clusterToFind.getName());
    			if (cluster != null) {
    				datacenterFoundIn = datacenter;
    				break;
    			}
    		}
    	}
    	
    	return datacenterFoundIn;
    }    
}
