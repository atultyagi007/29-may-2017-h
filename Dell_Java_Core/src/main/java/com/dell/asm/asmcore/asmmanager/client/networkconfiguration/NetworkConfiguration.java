/**************************************************************************
 *   Copyright (c) 2014 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.networkconfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.Network;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.StaticNetworkConfiguration;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;

@XmlRootElement
@XmlType(propOrder = { "id", "servertype", "fabrics", "interfaces" })
@XmlAccessorType(XmlAccessType.FIELD)
public class NetworkConfiguration {

    private String id;
    private String servertype;
    private List<Fabric> interfaces = new ArrayList();
    @Deprecated
    private List<Fabric> fabrics = new ArrayList();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @deprecated all servers are treated as racks now
     */
    public String getServertype() {
        return servertype;
    }

    /**
     * @deprecated all servers are treated as racks now
     */
    public void setServertype(String servertype)
    {
        this.servertype = servertype;
    }

    /**
     * @deprecated Use interfaces instead
     */
    public List<Fabric> getFabrics() {
        return fabrics;
    }

    /**
     * @deprecated Use interfaces instead
     */
    public void setFabrics(List<Fabric> fabrics) {
        this.fabrics = fabrics;
    }

    public List<Fabric> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<Fabric> interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("interfaces", interfaces)
                .toString();
    }

    /**
     * Search the partitions of the given interface for partitions and/or networks matching
     * the specified types and adds corresponding information to the optional passed
     * accumulator lists.
     *
     * @param anInterface The interface
     * @param targetTypes The network types to search for. May be null to search for all types.
     * @param partitionsAccumulator The list that matching partitions will be passed to.
     * @param networksAccumulator The list that matching networks will be passed to.
     * @param staticConfigAccumulator The list that matching static network configurations
     *                                will be passed to.
     */
    private static void searchPartitions(Interface anInterface,
                                         List<NetworkType> targetTypes,
                                         List<Partition> partitionsAccumulator,
                                         List<Network> networksAccumulator,
                                         List<StaticNetworkConfiguration> staticConfigAccumulator) {
        List<Partition> partitions = anInterface.getPartitions();
        if (!anInterface.isPartitioned()) {
            // Discard all except first partition
            partitions = new ArrayList<>(Arrays.asList(partitions.get(0)));
        }
        for (Partition partition : partitions) {
            boolean partitionAdded = false;
            List<Network> networks = partition.getNetworkObjects();
            if (networks != null) {
                for (Network network : networks) {
                    if (targetTypes == null || targetTypes.contains(network.getType())) {
                        if (networksAccumulator != null) {
                            networksAccumulator.add(network);
                        }
                        if (partitionsAccumulator != null && !partitionAdded) {
                            partitionsAccumulator.add(partition);
                        }
                        if (staticConfigAccumulator != null && network.isStatic()) {
                            StaticNetworkConfiguration staticConfig = network.getStaticNetworkConfiguration();
                            if (staticConfig != null) {
                                staticConfigAccumulator.add(staticConfig);
                            }
                        }
                        partitionAdded = true;
                    }
                }
            }
        }
    }

    private void searchInterfaces(List<NetworkType> targetTypes,
                                  List<Partition> partitionsAccumulator,
                                  List<Network> networksAccumulator,
                                  List<StaticNetworkConfiguration> staticConfigAccumulator) {
        List<Fabric> interfacesForRackServers = getInterfaces();
        if (interfacesForRackServers != null) {
            for (Fabric rinterface : interfacesForRackServers) {
                if (!rinterface.isEnabled()) continue;
                if (Fabric.FC_TYPE.equals(rinterface.getFabrictype())) continue;

                int numI = rinterface.getNPorts();
                for (int i = 0; i < numI; i++) {
                    searchPartitions(rinterface.getInterfaces().get(i), targetTypes,
                            partitionsAccumulator, networksAccumulator, staticConfigAccumulator);
                }
            }
        }
    }

    /**
     * Returns all partitions containing one of the specified network types
     *
     * @param types The network types to search for.
     * @return A list of matching partitions. An empty list is returned if no matches.
     */
    public List<Partition> getPartitions(NetworkType... types) {
        List<Partition> ret = new ArrayList<>();
        List<NetworkType> targetTypes = types.length < 1 ? null : Arrays.asList(types);
        searchInterfaces(targetTypes, ret, null, null);
        return ret;
    }

    /**
     * Returns all networks in the configuration matching one of the specified types.
     * Networks may be returned multiple times if they are on multiple partitions.
     *
     * @param types The network types to search for.
     * @return A list of matching networks. An empty list is returned if no matches.
     */
    public List<Network> getNetworks(NetworkType... types) {
        List<Network> ret = new ArrayList<>();
        List<NetworkType> targetTypes = types.length < 1 ? null : Arrays.asList(types);
        searchInterfaces(targetTypes, null, ret, null);
        return ret;
    }

    /**
     * Returns all static network configurations matching one of the specified network
     * types.
     *
     * @param types The network types to search for
     * @return A list of matching static network configurations.
     */
    public List<StaticNetworkConfiguration> getStaticNetworkConfigurations(NetworkType... types) {
        List<StaticNetworkConfiguration> ret = new ArrayList<>();
        List<NetworkType> targetTypes = types.length < 1 ? null : Arrays.asList(types);
        searchInterfaces(targetTypes, null, null, ret);
        return ret;
    }

    /**
     * Returns all static IP address from a network matching one of the specified types.
     * The returned list will be unique, i.e. it will not have duplicate IPs.
     *
     * @param types The network types to search for
     * @return A list of matching static ip addresses.
     */
    public List<String> getStaticIps(NetworkType... types) {
        List<StaticNetworkConfiguration> acc = getStaticNetworkConfigurations(types);
        Set<String> unique = new HashSet<>(acc.size());
        for (StaticNetworkConfiguration conf : acc) {
            String ip = conf.getIpAddress();
            if (ip != null) {
                unique.add(ip);
            }
        }
        return new ArrayList<>(unique);
    }

    /**
     * Returns interfaces used by template definition. Since template has a lot more interface that are typically used it is helpful to get just those we need for
     * IP lookup and validation.
     * @return
     */
    public List<Interface> getUsedInterfaces() {
        List<Interface> interfaces = new ArrayList<>();
        List<Fabric> interfacesForRackServers = this.getInterfaces();
        if (interfacesForRackServers==null) return interfaces;

        for(Fabric fabric : interfacesForRackServers){
            if (!fabric.isEnabled()) continue;
            if (fabric.isUsedforfc()) continue; // legacy
            if (Fabric.FC_TYPE.equals(fabric.getFabrictype())) continue;

            int numI = fabric.getNPorts();
            for (int i=0; i< numI; i++) {
                if (fabric.isRedundancy() && (((i+1) & 1) == 0)) continue; // for redundancy check only odd interfaces: 1, 3, etc - skip even
                // promote nic type to stored interface, it wass empty
                fabric.getInterfaces().get(i).setNictype(fabric.getNictype());
                interfaces.add(fabric.getInterfaces().get(i));
                if (fabric.isRedundancy()) {
                    // duplicate it
                    interfaces.add(fabric.getInterfaces().get(i));
                }
            }
        }

        return interfaces;
    }

    public List<Interface> getAllUsedInterfaces() {
        List<Interface> interfaces = new ArrayList<>();
        List<Fabric> interfacesForRackServers = this.getInterfaces();
        if (interfacesForRackServers==null) return interfaces;

        for (Fabric rinterface : interfacesForRackServers){
            if (!rinterface.isEnabled()) continue;
            if (Fabric.FC_TYPE.equals(rinterface.getFabrictype())) continue;

            int numI = rinterface.getNPorts();
            for (int i=0; i< numI; i++) {
                interfaces.add(rinterface.getInterfaces().get(i));
            }
        }
        return interfaces;
    }
    
    /**
     * Returns all networks in the configuration for all possible NetworkTypes.
     *
     * @return A list of matching networks. An empty list is returned if no matches.
     */
    public List<Network> getNetworks(){
    	
    	return getNetworks(NetworkType.values());
    }   
    
    /**
     * Cycles through the Network interfaces and clears any unused NetworkIds.
     */
    public void clearUnusedNetworkSettings() {
        if (this.getFabrics() != null) {
            this.getFabrics().clear();
        }
    }


    /**
     * Loops through all the fabrics, then interfaces, and then partitions to retrieve ALL of the Networks.
     * 
     * @return all of the Networks that art part of this NetworkConfiguration.
     */
    public Set<String> getAllNetworkIds() {
        HashSet<String> networkIds = new HashSet<String>();
        
        // Get from interfaces  
        for (Fabric fabric : this.interfaces) {
            List<Interface> interfaces = fabric.getInterfaces();
            if(interfaces != null) {
                for (Interface fInterface : interfaces) {
                    if(fInterface != null && fInterface.getPartitions() != null) {
                        List<Partition> partitions = fInterface.getPartitions();
                        for (Partition partition : partitions) {
                            if(partition != null && partition.getNetworks() != null) { 
                                networkIds.addAll(partition.getNetworks());
                            }
                        }
                    }
                }
            }
        }
        
        return networkIds;
    }


}
