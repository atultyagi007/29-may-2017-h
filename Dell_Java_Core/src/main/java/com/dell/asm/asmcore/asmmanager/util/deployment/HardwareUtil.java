/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.deployment;


import com.dell.asm.asmcore.asmmanager.client.hardware.RAIDConfiguration;
import com.dell.asm.asmcore.asmmanager.client.hardware.VirtualDisk;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.TemplateRaidConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.VirtualDiskConfiguration;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerNotEnoughDisksException;
import com.dell.asm.localizablelogger.LocalizableMessageService;
import com.dell.asm.server.client.policy.RaidLevel;
import com.dell.pg.asm.server.client.device.Controller;
import com.dell.pg.asm.server.client.device.PhysicalDisk;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Prepare RAID configuration
 */
public class HardwareUtil {
    private static final Logger logger = Logger.getLogger(HardwareUtil.class);
    private static HardwareUtil instance;

    private final static int MAX_DRIVES_RAID1 = 2;

    public HardwareUtil() {
    }

    public static synchronized HardwareUtil getInstance() {
        if (instance == null)
            instance = new HardwareUtil();
        return instance;
    }

    /**
     * Inputs:
     Virtual Disks (VDs)
     Number
     Raid level
     Minimum / Exactly / Maximum (UI only shows Minimum and Exactly for now, but I am going to consider all 3 here)
     # of Disks
     Type: Any / Require SSD / Require HDD
     Minimum total # of Hot Spares
     Minimum # of SSD Hot Spares (subset of the total above)

     Assigning Physical Drives: (filtering is true if this does not fail)
     For each controller:
     Create list of SSDs sorted by Size and Drive number (available SSDs)
     Create list of HDDs sorted by Size and Drive number (available HDDs)
     Consume Min SSD Hot Spares from available SSDs. If not enough, fail (go to next controller)
     Consume (Min total hot spares – Min SSD Hot Spares) from available HDDs, falling back to available SSDs if needed. If not enough, fail
     Distribute minimum disks. For each VD:
     If Type is Require SSD, consume min # required from available SSDs. If not enough, fail
     If Type is Require HDD, consume min # required from available HDDs. If not enough, fail
     If Type is Any, consume min # required from SSDs, falling back to HDDs if needed. If not enough, fail
     Distribute remaining disks. For each VD:
     If VD contains SSDs (as determined previously), and has Minimum or Maximum set, consume one SSD if the total number of disks in the VD is less than the Maximum (if any)
     If VD contains HDDs (as determined previously), and has Minimum or Maximum set, consume one HDD if the total number of disks in the VD is less than the Maximum (if any)
     Continue "Distribute remaining didks" until all HDDs and SSDs are consumed or all VDs have reached their Maximum number of disks (if any).
     All remaining disks go into the Global Hot Spare.
     If here, break and use this controller

     * @param controllers
     * @param filterEnvironment
     * @return
     * @throws AsmManagerNotEnoughDisksException
     */
    public RAIDConfiguration prepareRAID(List<Controller> controllers, FilterEnvironment filterEnvironment) throws AsmManagerNotEnoughDisksException {
        RAIDConfiguration raidConfiguration = new RAIDConfiguration();

        if (filterEnvironment.getRaidConfiguration()==null)
            return raidConfiguration;

        if (controllers == null || controllers.size()==0) {
            throw new AsmManagerNotEnoughDisksException("No RAID controllers reported by IDRAC");
        }

        if (filterEnvironment.getRaidConfiguration().getRaidtype() == TemplateRaidConfiguration.RaidTypeUI.basic) {
            // make up virtual disks
            filterEnvironment.getRaidConfiguration().getVirtualdisks().clear();

            VirtualDiskConfiguration vdisk = new VirtualDiskConfiguration();
            vdisk.setDisktype(VirtualDiskConfiguration.DiskMediaType.any);
            vdisk.setRaidlevel(filterEnvironment.getRaidConfiguration().getBasicraidlevel());
            vdisk.setComparator(VirtualDiskConfiguration.ComparatorValue.minimum);
            vdisk.setId(UUID.randomUUID().toString());
            vdisk.setNumberofdisks(RaidLevel.fromUIValue(vdisk.getRaidlevel().name()).getMinDisksRequired());
            filterEnvironment.getRaidConfiguration().getVirtualdisks().add(vdisk);
        }

        RAIDConfiguration externalConfig = assignDisks(controllers, filterEnvironment, true);
        RAIDConfiguration internalConfig = assignDisks(controllers, filterEnvironment, false);

        if (internalConfig == null ) {
            throw new AsmManagerNotEnoughDisksException("No suitable RAID controllers. There are some that could not be used by current template.");
        }

        if (externalConfig != null ) {
            internalConfig.setExternalHddHotSpares(externalConfig.getExternalHddHotSpares());
            internalConfig.setExternalSsdHotSpares(externalConfig.getExternalSsdHotSpares());
            internalConfig.setExternalVirtualDisks(externalConfig.getExternalVirtualDisks());
        }
        return internalConfig;
    }

    private RAIDConfiguration assignDisks(List<Controller> controllers, FilterEnvironment filterEnvironment, boolean forExternal){
        AsmManagerNotEnoughDisksException lastError = null;
        RAIDConfiguration raidConfiguration = null;

        // Embedded Controller unable to deploy to ESX or RedHat6/CentOS 6 (Windows and SLES OK)
        if (!filterEnvironment.isWindowsOS()) {
            rejectEmbeddedController(controllers);
        }

        ControllerComparator controllerComparator = new ControllerComparator();
        Collections.sort(controllers, controllerComparator);

        for (Controller controller : controllers) {

            //RaidConfigWrapper will return data related to internal or external depending on how it's initialized.
            RaidConfigWrapper raidConfigWrapper = new RaidConfigWrapper(filterEnvironment.getRaidConfiguration(), controller, forExternal);
            // reset prepared configuration
            raidConfigWrapper.reset();
            HashSet<String> enclosures = raidConfigWrapper.getEnclosures();

            try {
                /**
                 * For each controller:
                 * Create list of SSDs sorted by Size and Drive number (available SSDs)
                 * Create list of HDDs sorted by Size and Drive number (available HDDs)
                 */
                List<PhysicalDisk> physicalDisks = controller.getPhysicalDisks();
                List<PhysicalDisk> availableSsds = new ArrayList<>();
                List<PhysicalDisk> availableHdds = new ArrayList<>();
                List<PhysicalDisk> ssdPool = new ArrayList<>();
                List<PhysicalDisk> hddPool = new ArrayList<>();
                List<PhysicalDisk> diskPool = new ArrayList<>(); // all available disks, need to detect disk type when we ask First or Last

                for (PhysicalDisk pd : physicalDisks) {
//                    //Disk FQDD will be in the form of DiskName:EnclosureFQDD, so we can get the Enclosure FQDD easily from it
                    String enclosureFqdd = pd.getFqdd().split(":", 2)[1];
                    boolean isEmbeddedController = controller.getFqdd().contains("Embedded");
                    // Embedded Controllers do not have enclosures
                    if(enclosures.contains(enclosureFqdd) || (isEmbeddedController && !forExternal)) {
                        if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.SSD) {
                            availableSsds.add(pd);
                            ssdPool.add(pd);
                        } else if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.HDD) {
                            availableHdds.add(pd);
                            hddPool.add(pd);
                        }
                    }
                }

                int requestedDrivesNum = 0;
                int ssdCount = 0;
                int hddCount = 0;

                /**
                 * Distribute minimum disks. For each VD:
                 If Type is Require SSD, consume min # required from available SSDs. If not enough, fail
                 If Type is Require HDD, consume min # required from available HDDs. If not enough, fail
                 If Type is Any, consume min # required from SSDs, falling back to HDDs if needed. If not enough, fail
                 */
                int vdCnt = 0;
                Comparator<PhysicalDisk> lastComparator = null;

                List<VirtualDisk> currentVirtualDisks = raidConfigWrapper.getWorkingVirtualDisks();
                for (VirtualDiskConfiguration vdc : raidConfigWrapper.getVirtualDisks()) {

                    Comparator<PhysicalDisk> comparator;
                    // first VD requires special sorting
                    if (vdCnt==0) {
                        if (VirtualDiskConfiguration.DiskMediaType.first.equals(vdc.getDisktype())) {
                            comparator = new DiskComparatorAscending();
                        } else {
                            comparator = new DiskComparator();
                        }
                    }else{
                        comparator = new DiskComparator();
                    }

                    if (comparator != lastComparator) {
                        Collections.sort(availableSsds, comparator);
                        Collections.sort(availableHdds, comparator);
                        Collections.sort(ssdPool, comparator);
                        Collections.sort(hddPool, comparator);

                        diskPool.clear();
                        diskPool.addAll(ssdPool);
                        diskPool.addAll(hddPool);
                        Collections.sort(diskPool, comparator);

                        lastComparator = comparator;
                    }

                    vdCnt++;

                    // for Hadoop cases First and Last we detect media type from the first
                    // drive in all available list. This lits is already sorted as needed.
                    PhysicalDisk.PhysicalMediaType mt = null;
                    if (vdc.getDisktype().isPhysiclaDisdType()) {
                        mt = PhysicalDisk.PhysicalMediaType.fromUIValue(vdc.getDisktype().name());
                    }else{
                        if (diskPool.size()>0) {
                            mt = diskPool.get(0).getMediaType();
                        }else{
                            // this is just a safe harbor, if we don't have available disks - nothing will happen
                            mt = PhysicalDisk.PhysicalMediaType.ANY;
                        }
                    }

                    VirtualDisk virtualDisk = new VirtualDisk();
                    virtualDisk.setController(controller.getFqdd());
                    virtualDisk.setConfiguration(vdc);
                    virtualDisk.setRaidLevel(vdc.getRaidlevel());
                    virtualDisk.setMediaType(mt);

                    currentVirtualDisks.add(virtualDisk);

                    if (vdc.getComparator() == VirtualDiskConfiguration.ComparatorValue.minimum ||
                            vdc.getComparator() == VirtualDiskConfiguration.ComparatorValue.exact)
                        requestedDrivesNum = vdc.getNumberofdisks();
                    else {
                        requestedDrivesNum = RaidLevel.fromUIValue(vdc.getRaidlevel().name()).getMinDisksRequired();
                    }

                    if (PhysicalDisk.PhysicalMediaType.SSD.equals(mt)) {
                        requestedDrivesNum = consumeDisks(availableSsds, virtualDisk.getPhysicalDisks(), requestedDrivesNum);

                        if (requestedDrivesNum > 0) {
                            throw new AsmManagerNotEnoughDisksException("Not enough SSDs for virtual disk #" + vdCnt);
                        }

                    } else if (PhysicalDisk.PhysicalMediaType.HDD.equals(mt)) {
                        requestedDrivesNum = consumeDisks(availableHdds, virtualDisk.getPhysicalDisks(), requestedDrivesNum);

                        if (requestedDrivesNum > 0) {
                            throw new AsmManagerNotEnoughDisksException("Not enough HDDs for virtual disk #" + vdCnt);
                        }

                    } else {
                        int remains = consumeDisks(availableSsds, virtualDisk.getPhysicalDisks(), requestedDrivesNum);

                        if (remains > 0) {
                            // can't get all from SSD, release
                            releaseDisks(ssdPool, availableSsds, virtualDisk.getPhysicalDisks());
                            // try HDDs
                            remains = consumeDisks(availableHdds, virtualDisk.getPhysicalDisks(), requestedDrivesNum);
                        }

                        if (remains > 0) {
                            throw new AsmManagerNotEnoughDisksException("Not enough SSDs and HDDs for virtual disk #" + vdCnt);
                        }
                    }
                }

                // Consume Min SSD Hot Spares from available SSDs. If not enough, fail (go to next controller)
                if (raidConfigWrapper.isEnableGlobalHotspares()) {
                    /*
                     * If only SSD type virtual disks are created, the Global Hot spare must be all SSD.
                     * If only HDD type virtual disks are created, the Global Hot spare must be all HDD.
                    */
                    for (VirtualDisk vd : raidConfigWrapper.getWorkingVirtualDisks()) {
                        for (String fqdd : vd.getPhysicalDisks()) {
                            for (PhysicalDisk pd : physicalDisks) {
                                if (pd.getFqdd().equals(fqdd)) {
                                    if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.SSD) {
                                        ssdCount++;
                                    }else if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.HDD) {
                                        hddCount++;
                                    }
                                }
                            }
                        }
                    }


                    // if no HDD VD, all hot spare must be SSD
                    int requestedSSDHS = 0;
                    requestedDrivesNum = (hddCount == 0)? raidConfigWrapper.getGlobalHotspares() : raidConfigWrapper.getMinimumSsd();
                    requestedSSDHS = requestedDrivesNum;
                    if (availableSsds.size() >= requestedDrivesNum) {
                        requestedDrivesNum = consumeDisks(availableSsds, raidConfigWrapper.getWorkingSsdHotSpares(), requestedDrivesNum);
                    }
                    if (requestedDrivesNum > 0) {
                        throw new AsmManagerNotEnoughDisksException("Not enough SSDs for hot spares");
                    }

                    // Consume (Min total hot spares – Min SSD Hot Spares) from available HDDs, falling back to available SSDs if needed. If not enough, fail
                    requestedDrivesNum = raidConfigWrapper.getGlobalHotspares() - requestedSSDHS;
                    if ((availableHdds.size() + availableSsds.size()) >= requestedDrivesNum) {
                        if (hddCount > 0) {
                            requestedDrivesNum = consumeDisks(availableHdds, raidConfigWrapper.getWorkingHddHotSpares(), requestedDrivesNum);
                        }

                        if (requestedDrivesNum > 0 && ssdCount > 0) {
                            requestedDrivesNum = consumeDisks(availableSsds, raidConfigWrapper.getWorkingSsdHotSpares(), requestedDrivesNum);
                        }
                    }

                    if (requestedDrivesNum > 0) {
                        throw new AsmManagerNotEnoughDisksException("Not enough SSD and HDD for hot spares");
                    }
                }


                /**
                 * Distribute remaining disks. For each VD:
                 * If VD contains SSDs (as determined previously), and has Minimum or Maximum set, consume one SSD if the total number of disks in the VD is less than the Maximum (if any)
                 * If VD contains HDDs (as determined previously), and has Minimum or Maximum set, consume one HDD if the total number of disks in the VD is less than the Maximum (if any)
                 */
                int maxAllowed = 0;
                int numAllocatedDisks = 0;
                for (VirtualDisk vd : currentVirtualDisks) {
                    if (vd.getConfiguration().getComparator() == VirtualDiskConfiguration.ComparatorValue.maximum) {
                        maxAllowed = vd.getConfiguration().getNumberofdisks();
                    }else{
                        maxAllowed = Integer.MAX_VALUE;
                    }
                    numAllocatedDisks += vd.getPhysicalDisks().size();
                }


                while ((availableSsds.size() > 0 || availableHdds.size() > 0) && numAllocatedDisks<maxAllowed){
                    int nAlloc = numAllocatedDisks;
                    for (VirtualDisk vd : currentVirtualDisks) {
                        if (vd.getConfiguration().getComparator() != VirtualDiskConfiguration.ComparatorValue.exact) {

                            if (vd.getConfiguration().getComparator() == VirtualDiskConfiguration.ComparatorValue.maximum &&
                                    vd.getPhysicalDisks().size() == vd.getConfiguration().getNumberofdisks()) {
                                continue;
                            }
                            // special case: limit max drives for raid1
                            if (vd.getRaidLevel() == VirtualDiskConfiguration.UIRaidLevel.raid1 &&
                                    vd.getPhysicalDisks().size() == MAX_DRIVES_RAID1) {
                                continue;
                            }

                            int incrementNumber = getIncrementNumber(vd.getRaidLevel());

                            if (vd.getConfiguration().getDisktype() == VirtualDiskConfiguration.DiskMediaType.requiressd
                                    && availableSsds.size()>=incrementNumber) {
                                int remains = consumeDisks(availableSsds, vd.getPhysicalDisks(), incrementNumber);
                                if (remains==0)
                                    numAllocatedDisks+=incrementNumber;
                                else
                                    releaseDisks(ssdPool, availableSsds, vd.getPhysicalDisks());

                            } else if (vd.getConfiguration().getDisktype() == VirtualDiskConfiguration.DiskMediaType.requirehdd
                                    && availableHdds.size()>=incrementNumber) {
                                int remains = consumeDisks(availableHdds, vd.getPhysicalDisks(), incrementNumber);
                                if (remains==0)
                                    numAllocatedDisks+=incrementNumber;
                                else
                                    releaseDisks(hddPool, availableHdds, vd.getPhysicalDisks());

                            } else {
                                // what type we choose for ANY? find that by the first disk
                                PhysicalDisk.PhysicalMediaType mediaType = null;
                                for (PhysicalDisk pd : physicalDisks) {
                                    if (pd.getFqdd().equals(vd.getPhysicalDisks().get(0))) {
                                        mediaType = pd.getMediaType();
                                        break;
                                    }
                                }
                                if (mediaType == PhysicalDisk.PhysicalMediaType.SSD && availableSsds.size()>=incrementNumber) {
                                    int remains = consumeDisks(availableSsds, vd.getPhysicalDisks(), incrementNumber);
                                    if (remains == 0)
                                        numAllocatedDisks+=incrementNumber;
                                    else
                                        releaseDisks(ssdPool, availableSsds, vd.getPhysicalDisks());
                                }else if (mediaType == PhysicalDisk.PhysicalMediaType.HDD && availableHdds.size()>=incrementNumber) {
                                    int remains = consumeDisks(availableHdds, vd.getPhysicalDisks(), incrementNumber);
                                    if (remains == 0)
                                        numAllocatedDisks+=incrementNumber;
                                    else
                                        releaseDisks(hddPool, availableHdds, vd.getPhysicalDisks());
                                }
                            }
                            // check if no more disks left
                            if ((availableSsds.size() == 0 && availableHdds.size() == 0) ||
                                    numAllocatedDisks==maxAllowed)
                                break;
                        }
                    }
                    // if we were unable to allocate any disks for any VD - quit
                    if (nAlloc == numAllocatedDisks)
                        break;
                }

                /**
                 * All remaining disks go into the Global Hot Spare.
                 */
                if (raidConfigWrapper.isEnableGlobalHotspares()) {
                    // recalculate ssd and hdd VD
                    ssdCount = 0;
                    hddCount = 0;
                    for (VirtualDisk vd : raidConfigWrapper.getWorkingVirtualDisks()) {
                        for (String fqdd : vd.getPhysicalDisks()) {
                            for (PhysicalDisk pd : physicalDisks) {
                                if (pd.getFqdd().equals(fqdd)) {
                                    if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.SSD) {
                                        ssdCount++;
                                    }else if (pd.getMediaType() == PhysicalDisk.PhysicalMediaType.HDD) {
                                        hddCount++;
                                    }
                                }
                            }
                        }
                    }

                    if (availableSsds.size() > 0 && ssdCount>0) {
                        consumeDisks(availableSsds, raidConfigWrapper.getWorkingSsdHotSpares(), availableSsds.size());
                    }
                    if (availableHdds.size() > 0 && hddCount>0) {
                        consumeDisks(availableSsds, raidConfigWrapper.getWorkingHddHotSpares(), availableHdds.size());
                    }
                }


                /**
                 * If here, break and use this controller
                 */
                lastError = null;
                raidConfiguration = raidConfigWrapper.getWorkingRaidConfig();
                break;
            }catch(AsmManagerNotEnoughDisksException e) {
                // use next controller
                logger.debug("Controller " + controller.getFqdd() + " failed to satisfy RAID requirements: " + e.getMessage());
                lastError = e;
            }
        }
        if (lastError!=null)
            throw lastError;
        return raidConfiguration;
    }

    /**
     * Reject Embedded Controller
     *
     * This removes the embedded (S130) controller from the controller list
     *
     * @param controllers
     */
    public void rejectEmbeddedController(List<Controller> controllers) {
        Iterator<Controller> i = controllers.iterator();
        while (i.hasNext()){
            Controller controller = i.next();
            if (controller.isEmbeddedController()) {
                logger.debug("Removed embedded controller " + controller.getFqdd());
                i.remove();
            }
        }
    }

    private int getIncrementNumber(VirtualDiskConfiguration.UIRaidLevel raidLevel) {
        switch (raidLevel) {
            case raid0:
                return 1;
            case raid1:
                return 2;
            case raid5:
            case raid6:
                return 1;
            case raid10:
                return 2;
            case raid50:
                return 3;
            default:
                return 1;
        }
    }

    /**=
     * Remove disks from supplied list and add removed FQDD to destination.
     * @param source List of Disks
     * @param dest List of Strings
     * @param requestedDrivesNum Number of remaining disks to process. 0 if resource had enough disks. In this case dest.size() == requestedDrivesNum
     * @return
     */
    private int consumeDisks(List<PhysicalDisk> source, List<String> dest, int requestedDrivesNum) {
        Iterator<PhysicalDisk> it = source.iterator();
        while (it.hasNext() && requestedDrivesNum>0) {
            PhysicalDisk pd = it.next();
            dest.add(pd.getFqdd());
            it.remove();
            requestedDrivesNum--;
        }
        return requestedDrivesNum;
    }

    private void releaseDisks(List<PhysicalDisk> pool, List<PhysicalDisk> source, List<String> dest) {
        Iterator<String> it = dest.iterator();
        while (it.hasNext()) {
            String fqdd = it.next();
            for (PhysicalDisk pd: pool) {
                if (pd.getFqdd().equals(fqdd)){
                    source.add(pd);
                    break;
                }
            }
            it.remove();
        }
        Collections.sort(source, new DiskComparator());
    }

    /**
     * Compare controllers by type
     *
     */
    public static class ControllerComparator implements Comparator<Controller> {
        // Prefer integrated slot/raid, then Embedded (S130), then Stash (Modular)
        Integer getControllerTypeRank(String fqdd) {
            if (StringUtils.contains(fqdd, "Embedded")) {
                return 1;
            } else if (StringUtils.contains(fqdd, "Modular")) {
                return 2;
            } else {
                return 0;
            }
        }

        @Override
        public int compare(Controller a, Controller b) {
            return getControllerTypeRank(a.getFqdd()).compareTo(getControllerTypeRank(b.getFqdd()));
        }
    }

    /**
     * Compare disks by size and slot.
     */
    private class DiskComparator implements Comparator<PhysicalDisk> {
        @Override
        public int compare(PhysicalDisk x, PhysicalDisk y) {
            if (x.getSize()> y.getSize())
                return -1;
            else if (x.getSize()< y.getSize())
                return 1;
            else {
                if (x.getDriveNumber()<y.getDriveNumber())
                    return 1;
                else if (x.getDriveNumber()> y.getDriveNumber())
                    return -1;
                else
                    return 0;
            }
        }
    }

    /**
     * Compare disks by size and slot.
     */
    private class DiskComparatorAscending implements Comparator<PhysicalDisk> {
        @Override
        public int compare(PhysicalDisk x, PhysicalDisk y) {
            if (x.getSize()> y.getSize())
                return -1;
            else if (x.getSize()< y.getSize())
                return 1;
            else {
                if (x.getDriveNumber()<y.getDriveNumber())
                    return -1;
                else if (x.getDriveNumber()> y.getDriveNumber())
                    return 1;
                else
                    return 0;
            }
        }
    }

}
