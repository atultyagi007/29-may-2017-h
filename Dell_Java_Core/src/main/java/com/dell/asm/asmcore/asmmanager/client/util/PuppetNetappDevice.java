/**************************************************************************
 *   Copyright (c) 2015 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.client.util;

import java.util.List;

/**
 * Represents a NetApp device from the Puppet Inventory.
 */
public class PuppetNetappDevice {
    
    // Member Variables
    private List<AggregateData> aggregateDatas;
    private String clientCert;
    private String clientNoop;
    private String clientVersion;
    private List<DiskData> diskDatas;
    private String domain;
    private String fqdn;
    private String hardwareIsa;
    private String hostName;
    private String interfaceIps;
    private String interfaces;
    private String ipaddress;
    private String ipaddressC0A;
    private String ipaddressC0B;
    private String ipaddressE0M;
    private String ipaddressE0P;
    private String ipaddressFcoeCifsNifs;
    private String ipaddressOneGbVif;
    private String isClustered;
    private List<LunData> lunDatas;
    private String macAddress;
    private String macAddressC0A;
    private String macAddressC0B;
    private String macAddressE0A;
    private String macAddressE0B;
    private String macAddressE0M;
    private String macAddressE0P;
    private String macAddressE1A;
    private String macAddressE1B;
    private String macAddressFcoeCifsNfs;
    private String macAddressOneGbVif;
    private String manufacturer;
    private String memorySize;
    private String memorySizeMb;
    private String mtuC0A;
    private String mtuC0B;
    private String netMask;
    private String netMaskC0A;
    private String netMaskC0B;
    private String netMaskE0M;
    private String netMaskE0P;
    private String netMaskFcoeCifsNfs;
    private String netMaskOneGbVif;
    private String operatingSystem;
    private String operatingSystemRelease;
    private String partnerSerialNumber;
    private String partnerSystemId;
    private String processorCount;
    private String productName;
    private String serialNumber;
    private String systemMachineType;
    private String systemRevision;
    private String totalAggregates;
    private String totalDisks;
    private String totalLuns;
    private String totalVolumes;
    private String uniqueId;
    private String version;
    private String name;
    private List<VolumeData> volumeDatas;
    
    /**
     * Default Constructor.
     */
    public PuppetNetappDevice() { }
    
    
    
    public List<AggregateData> getAggregateDatas() {
        return aggregateDatas;
    }


    public void setAggregateDatas(List<AggregateData> aggregateDatas) {
        this.aggregateDatas = aggregateDatas;
    }


    public String getClientCert() {
        return clientCert;
    }


    public void setClientCert(String clientCert) {
        this.clientCert = clientCert;
    }


    public String getClientNoop() {
        return clientNoop;
    }


    public void setClientNoop(String clientNoop) {
        this.clientNoop = clientNoop;
    }


    public String getClientVersion() {
        return clientVersion;
    }


    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }


    public List<DiskData> getDiskDatas() {
        return diskDatas;
    }


    public void setDiskDatas(List<DiskData> diskDatas) {
        this.diskDatas = diskDatas;
    }


    public String getDomain() {
        return domain;
    }


    public void setDomain(String domain) {
        this.domain = domain;
    }


    public String getFqdn() {
        return fqdn;
    }


    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }


    public String getHardwareIsa() {
        return hardwareIsa;
    }


    public void setHardwareIsa(String hardwareIsa) {
        this.hardwareIsa = hardwareIsa;
    }


    public String getHostName() {
        return hostName;
    }


    public void setHostName(String hostName) {
        this.hostName = hostName;
    }


    public String getInterfaceIps() {
        return interfaceIps;
    }


    public void setInterfaceIps(String interfaceIps) {
        this.interfaceIps = interfaceIps;
    }


    public String getInterfaces() {
        return interfaces;
    }


    public void setInterfaces(String interfaces) {
        this.interfaces = interfaces;
    }


    public String getIpaddress() {
        return ipaddress;
    }


    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }


    public String getIpaddressC0A() {
        return ipaddressC0A;
    }


    public void setIpaddressC0A(String ipaddressC0A) {
        this.ipaddressC0A = ipaddressC0A;
    }


    public String getIpaddressC0B() {
        return ipaddressC0B;
    }


    public void setIpaddressC0B(String ipaddressC0B) {
        this.ipaddressC0B = ipaddressC0B;
    }


    public String getIpaddressE0M() {
        return ipaddressE0M;
    }


    public void setIpaddressE0M(String ipaddressE0M) {
        this.ipaddressE0M = ipaddressE0M;
    }


    public String getIpaddressE0P() {
        return ipaddressE0P;
    }


    public void setIpaddressE0P(String ipaddressE0P) {
        this.ipaddressE0P = ipaddressE0P;
    }


    public String getIpaddressFcoeCifsNifs() {
        return ipaddressFcoeCifsNifs;
    }


    public void setIpaddressFcoeCifsNifs(String ipaddressFcoeCifsNifs) {
        this.ipaddressFcoeCifsNifs = ipaddressFcoeCifsNifs;
    }


    public String getIpaddressOneGbVif() {
        return ipaddressOneGbVif;
    }


    public void setIpaddressOneGbVif(String ipaddressOneGbVif) {
        this.ipaddressOneGbVif = ipaddressOneGbVif;
    }


    public String getIsClustered() {
        return isClustered;
    }


    public void setIsClustered(String isClustered) {
        this.isClustered = isClustered;
    }


    public List<LunData> getLunDatas() {
        return lunDatas;
    }


    public void setLunDatas(List<LunData> lunDatas) {
        this.lunDatas = lunDatas;
    }


    public String getMacAddress() {
        return macAddress;
    }


    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }


    public String getMacAddressC0A() {
        return macAddressC0A;
    }


    public void setMacAddressC0A(String macAddressC0A) {
        this.macAddressC0A = macAddressC0A;
    }


    public String getMacAddressC0B() {
        return macAddressC0B;
    }


    public void setMacAddressC0B(String macAddressC0B) {
        this.macAddressC0B = macAddressC0B;
    }


    public String getMacAddressE0A() {
        return macAddressE0A;
    }


    public void setMacAddressE0A(String macAddressE0A) {
        this.macAddressE0A = macAddressE0A;
    }


    public String getMacAddressE0B() {
        return macAddressE0B;
    }


    public void setMacAddressE0B(String macAddressE0B) {
        this.macAddressE0B = macAddressE0B;
    }


    public String getMacAddressE0M() {
        return macAddressE0M;
    }


    public void setMacAddressE0M(String macAddressE0M) {
        this.macAddressE0M = macAddressE0M;
    }


    public String getMacAddressE0P() {
        return macAddressE0P;
    }


    public void setMacAddressE0P(String macAddressE0P) {
        this.macAddressE0P = macAddressE0P;
    }


    public String getMacAddressE1A() {
        return macAddressE1A;
    }


    public void setMacAddressE1A(String macAddressE1A) {
        this.macAddressE1A = macAddressE1A;
    }


    public String getMacAddressE1B() {
        return macAddressE1B;
    }


    public void setMacAddressE1B(String macAddressE1B) {
        this.macAddressE1B = macAddressE1B;
    }


    public String getMacAddressFcoeCifsNfs() {
        return macAddressFcoeCifsNfs;
    }


    public void setMacAddressFcoeCifsNfs(String macAddressFcoeCifsNfs) {
        this.macAddressFcoeCifsNfs = macAddressFcoeCifsNfs;
    }


    public String getMacAddressOneGbVif() {
        return macAddressOneGbVif;
    }


    public void setMacAddressOneGbVif(String macAddressOneGbVif) {
        this.macAddressOneGbVif = macAddressOneGbVif;
    }


    public String getManufacturer() {
        return manufacturer;
    }


    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }


    public String getMemorySize() {
        return memorySize;
    }


    public void setMemorySize(String memorySize) {
        this.memorySize = memorySize;
    }


    public String getMemorySizeMb() {
        return memorySizeMb;
    }


    public void setMemorySizeMb(String memorySizeMb) {
        this.memorySizeMb = memorySizeMb;
    }


    public String getMtuC0A() {
        return mtuC0A;
    }


    public void setMtuC0A(String mtuC0A) {
        this.mtuC0A = mtuC0A;
    }


    public String getMtuC0B() {
        return mtuC0B;
    }


    public void setMtuC0B(String mtuC0B) {
        this.mtuC0B = mtuC0B;
    }


    public String getNetMask() {
        return netMask;
    }


    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }


    public String getNetMaskC0A() {
        return netMaskC0A;
    }


    public void setNetMaskC0A(String netMaskC0A) {
        this.netMaskC0A = netMaskC0A;
    }


    public String getNetMaskC0B() {
        return netMaskC0B;
    }


    public void setNetMaskC0B(String netMaskC0B) {
        this.netMaskC0B = netMaskC0B;
    }


    public String getNetMaskE0M() {
        return netMaskE0M;
    }


    public void setNetMaskE0M(String netMaskE0M) {
        this.netMaskE0M = netMaskE0M;
    }


    public String getNetMaskE0P() {
        return netMaskE0P;
    }


    public void setNetMaskE0P(String netMaskE0P) {
        this.netMaskE0P = netMaskE0P;
    }


    public String getNetMaskFcoeCifsNfs() {
        return netMaskFcoeCifsNfs;
    }


    public void setNetMaskFcoeCifsNfs(String netMaskFcoeCifsNfs) {
        this.netMaskFcoeCifsNfs = netMaskFcoeCifsNfs;
    }


    public String getNetMaskOneGbVif() {
        return netMaskOneGbVif;
    }


    public void setNetMaskOneGbVif(String netMaskOneGbVif) {
        this.netMaskOneGbVif = netMaskOneGbVif;
    }


    public String getOperatingSystem() {
        return operatingSystem;
    }


    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }


    public String getOperatingSystemRelease() {
        return operatingSystemRelease;
    }


    public void setOperatingSystemRelease(String operatingSystemRelease) {
        this.operatingSystemRelease = operatingSystemRelease;
    }


    public String getPartnerSerialNumber() {
        return partnerSerialNumber;
    }


    public void setPartnerSerialNumber(String partnerSerialNumber) {
        this.partnerSerialNumber = partnerSerialNumber;
    }


    public String getPartnerSystemId() {
        return partnerSystemId;
    }


    public void setPartnerSystemId(String partnerSystemId) {
        this.partnerSystemId = partnerSystemId;
    }


    public String getProcessorCount() {
        return processorCount;
    }


    public void setProcessorCount(String processorCount) {
        this.processorCount = processorCount;
    }


    public String getProductName() {
        return productName;
    }


    public void setProductName(String productName) {
        this.productName = productName;
    }


    public String getSerialNumber() {
        return serialNumber;
    }


    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }


    public String getSystemMachineType() {
        return systemMachineType;
    }


    public void setSystemMachineType(String systemMachineType) {
        this.systemMachineType = systemMachineType;
    }


    public String getSystemRevision() {
        return systemRevision;
    }


    public void setSystemRevision(String systemRevision) {
        this.systemRevision = systemRevision;
    }


    public String getTotalAggregates() {
        return totalAggregates;
    }


    public void setTotalAggregates(String totalAggregates) {
        this.totalAggregates = totalAggregates;
    }


    public String getTotalDisks() {
        return totalDisks;
    }


    public void setTotalDisks(String totalDisks) {
        this.totalDisks = totalDisks;
    }


    public String getTotalLuns() {
        return totalLuns;
    }


    public void setTotalLuns(String totalLuns) {
        this.totalLuns = totalLuns;
    }


    public String getTotalVolumes() {
        return totalVolumes;
    }


    public void setTotalVolumes(String totalVolumes) {
        this.totalVolumes = totalVolumes;
    }


    public String getUniqueId() {
        return uniqueId;
    }


    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }


    public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        this.version = version;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public List<VolumeData> getVolumeDatas() {
        return volumeDatas;
    }


    public void setVolumeDatas(List<VolumeData> volumeDatas) {
        this.volumeDatas = volumeDatas;
    }



    public static class AggregateData {
        
        // Member Variables
        private String name;
        private String state;
        private String diskCount;
        private String volumeCount;
        private String sizeTotal;
        private String sizeUsed;
        private String sizePercentageUsed;
        private String sizeAvailable;
        
        /**
         * Default constructor for the class.
         */
        public AggregateData() { }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getDiskCount() {
            return diskCount;
        }

        public void setDiskCount(String diskCount) {
            this.diskCount = diskCount;
        }

        public String getVolumeCount() {
            return volumeCount;
        }

        public void setVolumeCount(String volumeCount) {
            this.volumeCount = volumeCount;
        }

        public String getSizeTotal() {
            return sizeTotal;
        }

        public void setSizeTotal(String sizeTotal) {
            this.sizeTotal = sizeTotal;
        }

        public String getSizeUsed() {
            return sizeUsed;
        }

        public void setSizeUsed(String sizeUsed) {
            this.sizeUsed = sizeUsed;
        }

        public String getSizePercentageUsed() {
            return sizePercentageUsed;
        }

        public void setSizePercentageUsed(String sizePercentageUsed) {
            this.sizePercentageUsed = sizePercentageUsed;
        }

        public String getSizeAvailable() {
            return sizeAvailable;
        }

        public void setSizeAvailable(String sizeAvailable) {
            this.sizeAvailable = sizeAvailable;
        }
    }
    
    public static class DiskData {
        
        // Member Variables
        private String name;
        private String serialNumber;
        private String diskModel;
        
        /**
         * Default constructor for the class.
         */
        public DiskData() { }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getDiskModel() {
            return diskModel;
        }

        public void setDiskModel(String diskModel) {
            this.diskModel = diskModel;
        }
    }

    public static class LunData {
        
        // Member Variables
        private String path;
        private String size;
        private String sizeUsed;
        private String mapped;
        private String state;
        private String readOnly;
        private String spaceReservedEnabled;
        
        /**
         * Default constructor for the class.
         */
        public LunData() { }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getSizeUsed() {
            return sizeUsed;
        }

        public void setSizeUsed(String sizeUsed) {
            this.sizeUsed = sizeUsed;
        }

        public String getMapped() {
            return mapped;
        }

        public void setMapped(String mapped) {
            this.mapped = mapped;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getReadOnly() {
            return readOnly;
        }

        public void setReadOnly(String readOnly) {
            this.readOnly = readOnly;
        }

        public String getSpaceReservedEnabled() {
            return spaceReservedEnabled;
        }

        public void setSpaceReservedEnabled(String spaceReservedEnabled) {
            this.spaceReservedEnabled = spaceReservedEnabled;
        }
    }
    
  
    public static class VolumeData {
        
        // Member Variables
        private String name;
        private String sizeTotal;
        private String sizeAvailable;
        private String sizeUsed;
        private String type;
        private String state;
        private String spaceReserveEnabled; 
        
        /**
         * Default constructor for the class.
         */
        public VolumeData() { }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSizeTotal() {
            return sizeTotal;
        }

        public void setSizeTotal(String sizeTotal) {
            this.sizeTotal = sizeTotal;
        }

        public String getSizeAvailable() {
            return sizeAvailable;
        }

        public void setSizeAvailable(String sizeAvailable) {
            this.sizeAvailable = sizeAvailable;
        }

        public String getSizeUsed() {
            return sizeUsed;
        }

        public void setSizeUsed(String sizeUsed) {
            this.sizeUsed = sizeUsed;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getSpaceReserveEnabled() {
            return spaceReserveEnabled;
        }

        public void setSpaceReserveEnabled(String spaceReserveEnabled) {
            this.spaceReserveEnabled = spaceReserveEnabled;
        }
    }

    /**
     * Returns the VolumeData with the given volume name or null if no match can be found.
     * 
     * @param volumeName the VolumeData will contain.
     * @return the VolumeData with the given volume name or null if no match can be found.
     */
    public VolumeData getVolumeDataByVolumeName(String volumeName) {
        VolumeData volumeData = null;
        
        for (VolumeData vd : this.getVolumeDatas()) {
            if(volumeName.equals(vd.getName())) {
                volumeData = vd;
                return volumeData;
            }
        }
        
        return volumeData;
    }
    
}
