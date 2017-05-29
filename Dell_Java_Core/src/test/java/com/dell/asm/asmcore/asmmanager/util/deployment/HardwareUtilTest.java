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
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateCategory;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateComponent;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting.ServiceTemplateSettingType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.TemplateRaidConfiguration;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerNotEnoughDisksException;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import com.dell.pg.asm.server.client.device.Controller;
import com.dell.pg.asm.server.client.device.Enclosure;
import com.dell.pg.asm.server.client.device.PhysicalDisk;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.pg.orion.security.encryption.EncryptionDAO;
import com.dell.pg.orion.security.encryption.IEncryptedString;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HardwareUtilTest {

    private static long SIZE_200_GB = 200049647616L;
    private static long SIZE_600_GB = 599550590976L;

    private HardwareUtil util;
    private static String basicRaid1 = "{\"raidtype\":\"basic\",\"basicraidlevel\":\"raid1\",\"enableglobalhotspares\":true,\"globalhotspares\":\"1\",\"minimumssd\":\"0\",\"virtualdisks\":[]," +
            "\"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}";
    private static String basicRaid1NoHotSpare = "{\"raidtype\":\"basic\",\"basicraidlevel\":\"raid1\",\"enableglobalhotspares\":true,\"globalhotspares\":\"0\",\"minimumssd\":\"0\",\"virtualdisks\":[]," +
            "\"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}";
    private static String basicRaid0 = "{\"raidtype\":\"basic\",\"basicraidlevel\":\"raid0\",\"enableglobalhotspares\":true,\"globalhotspares\":\"1\",\"minimumssd\":\"0\",\"virtualdisks\":[]," +
            "\"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}";
    private static String basicRaid50 = "{\"raidtype\":\"basic\",\"basicraidlevel\":\"raid50\",\"enableglobalhotspares\":true,\"globalhotspares\":\"1\",\"minimumssd\":\"0\",\"virtualdisks\":[]," +
            "\"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}";
    private static String advancedRaid0diskMin2SsdHddHotSpares1ssd1 = "{\"raidtype\":\"advanced\",\"basicraidlevel\":\"raid0\",\"enableglobalhotspares\":true,\"globalhotspares\":\"2\",\"minimumssd\":\"1\",\"virtualdisks\":[{\"id\":\"70B9D028-0A73-494B-A77B-5D0CF6983D30\",\"raidlevel\":\"raid0\",\"comparator\":\"minimum\",\"numberofdisks\":\"2\",\"disktype\":\"requiressd\"},{\"id\":\"7713A714-4B3E-4845-8553-22A1B4182456\",\"raidlevel\":\"raid0\",\"comparator\":\"minimum\",\"numberofdisks\":\"2\",\"disktype\":\"requirehdd\"}], \"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[], \"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}\n";
    private static String advancedRaid0diskMin2SsdHddHotSpares0 = "{\"raidtype\":\"advanced\",\"basicraidlevel\":\"raid0\",\"enableglobalhotspares\":false,\"globalhotspares\":\"1\",\"minimumssd\":\"1\",\"virtualdisks\":[{\"id\":\"70B9D028-0A73-494B-A77B-5D0CF6983D30\",\"raidlevel\":\"raid0\",\"comparator\":\"minimum\",\"numberofdisks\":\"2\",\"disktype\":\"requiressd\"},{\"id\":\"7713A714-4B3E-4845-8553-22A1B4182456\",\"raidlevel\":\"raid0\",\"comparator\":\"minimum\",\"numberofdisks\":\"2\",\"disktype\":\"requirehdd\"}], \"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[], \"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}\n";
    private static String advancedRaid0diskExt2AnyAnyHotSpares0 = "{\"raidtype\":\"advanced\",\"basicraidlevel\":\"raid0\",\"enableglobalhotspares\":false,\"globalhotspares\":\"1\",\"minimumssd\":\"1\",\"virtualdisks\":[{\"id\":\"70B9D028-0A73-494B-A77B-5D0CF6983D30\",\"raidlevel\":\"raid0\",\"comparator\":\"exact\",\"numberofdisks\":\"2\",\"disktype\":\"any\"},{\"id\":\"7713A714-4B3E-4845-8553-22A1B4182456\",\"raidlevel\":\"raid0\",\"comparator\":\"exact\",\"numberofdisks\":\"2\",\"disktype\":\"any\"}], \"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[],\"enableglobalhotsparesexternal\":\"false\", \"globalhotsparesexternal\":\"0\" ,\"minimumssdexternal\":\"0\", \"externalvirtualdisks\":[]}\n";
    private static String advancedRaid0diskMinExt2AnyAnyHotSpares0 = "{\"raidtype\":\"advanced\",\"basicraidlevel\":\"raid0\",\"enableglobalhotspares\":false,\"globalhotspares\":\"1\",\"minimumssd\":\"1\",\"virtualdisks\":[{\"id\":\"70B9D028-0A73-494B-A77B-5D0CF6983D30\",\"raidlevel\":\"raid0\",\"comparator\":\"exact\",\"numberofdisks\":\"2\",\"disktype\":\"any\"},{\"id\":\"7713A714-4B3E-4845-8553-22A1B4182456\",\"raidlevel\":\"raid0\",\"comparator\":\"minimum\",\"numberofdisks\":\"2\",\"disktype\":\"any\"}]}\n";
    private static String controllerFqdd = "RAID.Integrated.1-1";
    private static String embeddedControllerFqdd = "NonRAID.Embedded.1-1";
    private static String modularControllerFqdd = "RAID.Modular.1-1";

    private static String advanced2VDLast = "{\"raidtype\":\"advanced\",\"basicraidlevel\":\"raid1\",\"virtualdisks\":[{\"id\":\"7ECE69F1-257B-4578-8EDB-498F02D68062\",\"raidlevel\":\"raid5\",\"comparator\":\"exact\",\"numberofdisks\":3,\"disktype\":\"last\"},{\"id\":\"E2B791D9-292D-4EB3-8EDE-27AE75A84062\",\"raidlevel\":\"raid5\",\"comparator\":\"minimum\",\"numberofdisks\":3,\"disktype\":\"requiressd\"}],\"enableglobalhotspares\":true,\"globalhotspares\":1,\"minimumssd\":1,\"enableglobalhotsparesexternal\":false,\"globalhotsparesexternal\":0,\"minimumssdexternal\":0,\"externalvirtualdisks\":[]}";
    private static String advanced2VDFirst = "{\"raidtype\":\"advanced\",\"basicraidlevel\":\"raid1\",\"virtualdisks\":[{\"id\":\"7ECE69F1-257B-4578-8EDB-498F02D68062\",\"raidlevel\":\"raid5\",\"comparator\":\"exact\",\"numberofdisks\":3,\"disktype\":\"first\"},{\"id\":\"E2B791D9-292D-4EB3-8EDE-27AE75A84062\",\"raidlevel\":\"raid5\",\"comparator\":\"minimum\",\"numberofdisks\":3,\"disktype\":\"requiressd\"}],\"enableglobalhotspares\":true,\"globalhotspares\":1,\"minimumssd\":1,\"enableglobalhotsparesexternal\":false,\"globalhotsparesexternal\":0,\"minimumssdexternal\":0,\"externalvirtualdisks\":[]}";

    private static String hadoopRaidLast = "{\"raidtype\":\"advanced\",\"basicraidlevel\":\"raid1\",\"virtualdisks\":[{\"id\":\"BA9B0264-CE8E-44DC-97F0-61B50833C6D5\",\"raidlevel\":\"raid1\"" +
            ",\"comparator\":\"exact\",\"numberofdisks\":2,\"disktype\":\"last\"},{\"id\":\"84DB3DA9-D392-47D0-AA4F-1FF241BD6CFF\",\"raidlevel\":\"raid1\",\"comparator\":\"exact\",\"numberofdisks\":" +
            "2,\"disktype\":\"any\"},{\"id\":\"C7A69C1D-A1EC-430B-85C7-1B236B99B627\",\"raidlevel\":\"raid10\",\"comparator\":\"minimum\",\"numberofdisks\":4,\"disktype\":\"any\"}," +
            "{\"id\":\"20C65763-4155-4144-AD7F-62B28A8AF3D7\",\"raidlevel\":\"raid5\",\"comparator\":\"minimum\",\"numberofdisks\":3,\"disktype\":\"any\"}," +
            "{\"id\":\"EC2F3B04-447A-4354-BFB5-65FAD1BAE718\",\"raidlevel\":\"nonraid\",\"comparator\":\"minimum\",\"numberofdisks\":2,\"disktype\":\"any\"}]" +
            ",\"enableglobalhotspares\":true,\"globalhotspares\":1,\"minimumssd\":0,\"enableglobalhotsparesexternal\":false,\"globalhotsparesexternal\":0," +
            "\"minimumssdexternal\":0,\"externalvirtualdisks\":[]}";

    private static String hadoopRaidFirst = "{\"raidtype\":\"advanced\",\"basicraidlevel\":\"raid1\",\"virtualdisks\":[{\"id\":\"BA9B0264-CE8E-44DC-97F0-61B50833C6D5\"," +
            "\"raidlevel\":\"raid1\",\"comparator\":\"exact\",\"numberofdisks\":2,\"disktype\":\"first\"},{\"id\":\"84DB3DA9-D392-47D0-AA4F-1FF241BD6CFF\",\"raidlevel\":\"raid1\"," +
            "\"comparator\":\"exact\",\"numberofdisks\":2,\"disktype\":\"any\"},{\"id\":\"C7A69C1D-A1EC-430B-85C7-1B236B99B627\",\"raidlevel\":\"raid10\",\"comparator\":\"minimum\"," +
            "\"numberofdisks\":4,\"disktype\":\"any\"},{\"id\":\"20C65763-4155-4144-AD7F-62B28A8AF3D7\",\"raidlevel\":\"raid5\",\"comparator\":\"minimum\",\"numberofdisks\":3," +
            "\"disktype\":\"any\"},{\"id\":\"EC2F3B04-447A-4354-BFB5-65FAD1BAE718\",\"raidlevel\":\"nonraid\",\"comparator\":\"minimum\",\"numberofdisks\":2,\"disktype\":\"any\"}]," +
            "\"enableglobalhotspares\":true,\"globalhotspares\":1,\"minimumssd\":0,\"enableglobalhotsparesexternal\":false,\"globalhotsparesexternal\":0,\"minimumssdexternal\":0," +
            "\"externalvirtualdisks\":[]}";

    @Before
    public void setUp() {
        util = HardwareUtil.getInstance();
    }

    private List<Controller> buildControllers(int numSSD, int numHDD) {
        return buildControllers(numSSD, numHDD, 0, 0);
    }

    private List<Controller> buildControllers(int numSSD, int numHDD, int numEmbedded, int numModular) {
        List<Controller> controllers = new ArrayList<>();
        Controller controller = new Controller();
        controllers.add(controller);
        Enclosure e = new Enclosure();
        e.setFqdd("Enclosure.Internal.0-1:" + controllerFqdd);
        e.setProductName("mock");
        controller.getEnclosures().add(e);
        controller.setFqdd(controllerFqdd);
        int slotNum = -1;

        for (int i=0; i< numSSD; i++) {
            slotNum++;
            PhysicalDisk physicalDisk = new PhysicalDisk();
            physicalDisk.setDriveNumber(slotNum);
            physicalDisk.setFqdd("Disk.Bay."+slotNum+":Enclosure.Internal.0-1:" + controllerFqdd);
            physicalDisk.setSize(SIZE_600_GB);
            physicalDisk.setMediaType(PhysicalDisk.PhysicalMediaType.SSD);

            controller.getPhysicalDisks().add(physicalDisk);
        }

        for (int i=0; i< numHDD; i++) {
            slotNum++;
            PhysicalDisk physicalDisk = new PhysicalDisk();
            physicalDisk.setDriveNumber(slotNum);
            physicalDisk.setFqdd("Disk.Bay."+slotNum+":Enclosure.Internal.0-1:" + controllerFqdd);
            physicalDisk.setSize(SIZE_600_GB);
            physicalDisk.setMediaType(PhysicalDisk.PhysicalMediaType.HDD);

            controller.getPhysicalDisks().add(physicalDisk);
        }

        if (numEmbedded > 0 ) {
            Controller embController = new Controller();
            controllers.add(0, embController);
            embController.setFqdd(embeddedControllerFqdd);
            int embSlotNum = -1;

            for (int i = 0; i < numEmbedded; i++) {
                embSlotNum++;
                PhysicalDisk physicalDisk = new PhysicalDisk();
                physicalDisk.setDriveNumber(embSlotNum);
                physicalDisk.setFqdd("Disk.Direct."+embSlotNum+"-"+embSlotNum+":NonRAID.Embedded.1-1");
                physicalDisk.setSize(SIZE_200_GB);
                physicalDisk.setMediaType(PhysicalDisk.PhysicalMediaType.SSD);

                embController.getPhysicalDisks().add(physicalDisk);

            }
        }

        if (numModular > 0) {
            Controller modController = new Controller();
            controllers.add(0, modController);
            modController.setFqdd(modularControllerFqdd);
            int modSlotNum = -1;

            for (int i = 0; i < numModular; i++) {
                modSlotNum++;
                PhysicalDisk physicalDisk = new PhysicalDisk();
                physicalDisk.setDriveNumber(modSlotNum);
                physicalDisk.setFqdd("Disk.Bay."+modSlotNum+"Enclosure.Internal:RAID.Modular...");
                physicalDisk.setSize(SIZE_200_GB);
                physicalDisk.setMediaType(PhysicalDisk.PhysicalMediaType.SSD);

                modController.getPhysicalDisks().add(physicalDisk);

            }
        }

        return controllers;
    }

    private FilterEnvironment buildFilterEnvironment(String raidConfiguration) {
        FilterEnvironment environment = new FilterEnvironment();
        environment.setNetworkProxy(mock(INetworkService.class));
        environment.initEnvironment(new ServiceTemplateComponent());

        String configString = "{ \"templateRaidConfiguration\" : " + raidConfiguration + "}";
        TemplateRaidConfiguration configuration = MarshalUtil.fromJSON(TemplateRaidConfiguration.class, configString);
        environment.setRaidConfiguration(configuration);
        return environment;
    }

    @Test
    public void testEnoughSSDAndHDD() {
        FilterEnvironment fe = buildFilterEnvironment(advancedRaid0diskMin2SsdHddHotSpares1ssd1);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(3, 3), fe);

        assertEquals("SSD hot spares number", 1, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 1, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 2, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 2, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("MediaType VD 1", PhysicalDisk.PhysicalMediaType.SSD, raidConfiguration.getVirtualDisks().get(0).getMediaType());
        assertEquals("RAID Level VD 1",  "raid0", raidConfiguration.getVirtualDisks().get(0).getRaidLevel().name());
        assertEquals("VD 1 Controller", controllerFqdd, raidConfiguration.getVirtualDisks().get(0).getController());

        assertEquals("Disks in VD 2 number", 2, raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().size());
        assertEquals("MediaType VD 2", PhysicalDisk.PhysicalMediaType.HDD, raidConfiguration.getVirtualDisks().get(1).getMediaType());
        assertEquals("RAID Level VD 2", "raid0", raidConfiguration.getVirtualDisks().get(1).getRaidLevel().name());
        assertEquals("VD 2 Controller", controllerFqdd, raidConfiguration.getVirtualDisks().get(1).getController());
    }

    @Test
    public void testExtraDrives() {
        FilterEnvironment fe = buildFilterEnvironment(advancedRaid0diskMin2SsdHddHotSpares1ssd1);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(4, 4), fe);

        assertEquals("SSD hot spares number", 1, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 1, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 2, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 3, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("Disks in VD 2 number", 3, raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().size());

        raidConfiguration = util.prepareRAID(buildControllers(5, 5), fe);

        assertEquals("SSD hot spares number", 1, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 1, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 2, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 4, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("Disks in VD 2 number", 4, raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().size());
    }

    @Test
    public void testAnyDrives() {
        FilterEnvironment fe = buildFilterEnvironment(advancedRaid0diskExt2AnyAnyHotSpares0);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(1, 4), fe);

        assertEquals("SSD hot spares number", 0, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 0, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 2, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 2, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("Disks in VD 2 number", 2, raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().size());

        try {
            raidConfiguration = util.prepareRAID(buildControllers(0, 3), fe);
            fail("Expect failure: Not enough SSDs and HDDs for virtual disk # 2");
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough SSDs and HDDs for virtual disk #2", e.getMessage());
        }
    }

    @Test
    public void testAnyDrivesExtra() {
        FilterEnvironment fe = buildFilterEnvironment(advancedRaid0diskMinExt2AnyAnyHotSpares0);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(1, 5), fe);

        assertEquals("SSD hot spares number", 0, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 0, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 2, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 2, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("Disks in VD 2 number", 3, raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().size());

    }
    @Test
    public void testEnoughNotEnoughDisksforVD() {
        FilterEnvironment fe = buildFilterEnvironment(advancedRaid0diskMin2SsdHddHotSpares0);
        try {
            RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(1, 2), fe);
            fail("Expect failure: not enough SSDs for VD 1");
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough SSDs for virtual disk #1", e.getMessage());
        }

        try {
            RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(2, 1), fe);
            fail("Expect failure: not enough HDDs for VD 2");
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough HDDs for virtual disk #2", e.getMessage());
        }

    }

    @Test
    public void testEnoughNotEnoughSSD() {
        FilterEnvironment fe = buildFilterEnvironment(advancedRaid0diskMin2SsdHddHotSpares1ssd1);
        try {
            RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(2, 3), fe);
            fail("Expect failure: not enough SSDs for VD 1");
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough SSDs for hot spares", e.getMessage());
        }

        try {
            RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(0, 3), fe);
            fail("Expect failure: not enough SSDs for hot spare");
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough SSDs for virtual disk #1", e.getMessage());
        }

    }

    @Test
    public void testEnoughNotEnoughHDD() {
        FilterEnvironment fe = buildFilterEnvironment(advancedRaid0diskMin2SsdHddHotSpares1ssd1);
        try {
            RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(3, 2), fe);
            fail("Expect failure: not enough HDDs for VD 2");
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough SSD and HDD for hot spares", e.getMessage());
        }

        try {
            RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(1, 0), fe);
            fail("Expect failure: not enough HDDs for hot spare");
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough SSDs for virtual disk #1", e.getMessage());
        }

    }

    @Test
    public void testEnoughDisksBasic() {
        FilterEnvironment fe = buildFilterEnvironment(basicRaid0);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(4, 0), fe);

        assertEquals("SSD hot spares number", 1, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 0, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 1, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 3, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("MediaType VD 1", PhysicalDisk.PhysicalMediaType.ANY, raidConfiguration.getVirtualDisks().get(0).getMediaType());
        assertEquals("RAID Level VD 1",  "raid0", raidConfiguration.getVirtualDisks().get(0).getRaidLevel().name());
        assertEquals("VD 1 Controller", controllerFqdd, raidConfiguration.getVirtualDisks().get(0).getController());
    }

    @Test
    public void testEnoughDisksBasicWithEmbedded() {
        FilterEnvironment fe = buildFilterEnvironment(basicRaid0);
        fe.setWindowsOS(true);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(0,0,2, 0), fe);

        assertEquals("VD number", 1, raidConfiguration.getVirtualDisks().size());
    }

    @Test
    public void testEnoughDisksBasicRaid1() {
        FilterEnvironment fe = buildFilterEnvironment(basicRaid1);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(7, 0), fe);

        assertEquals("SSD hot spares number", 5, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 0, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 1, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 2, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("MediaType VD 1", PhysicalDisk.PhysicalMediaType.ANY, raidConfiguration.getVirtualDisks().get(0).getMediaType());
        assertEquals("RAID Level VD 1",  "raid1", raidConfiguration.getVirtualDisks().get(0).getRaidLevel().name());
        assertEquals("VD 1 Controller", controllerFqdd, raidConfiguration.getVirtualDisks().get(0).getController());
    }

    @Test
    public void testEnoughDisksBasicRaid50() {
        FilterEnvironment fe = buildFilterEnvironment(basicRaid50);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(10, 0), fe);

        assertEquals("SSD hot spares number", 1, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 0, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 1, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 9, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("MediaType VD 1", PhysicalDisk.PhysicalMediaType.ANY, raidConfiguration.getVirtualDisks().get(0).getMediaType());
        assertEquals("RAID Level VD 1",  "raid50", raidConfiguration.getVirtualDisks().get(0).getRaidLevel().name());
        assertEquals("VD 1 Controller", controllerFqdd, raidConfiguration.getVirtualDisks().get(0).getController());
    }

    @Test
    public void testDrivesUseFirst() {
        FilterEnvironment fe = buildFilterEnvironment(advanced2VDFirst);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(7, 3), fe);

        assertEquals("SSD hot spares number", 1, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 0, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 2, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 3, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("Disks in VD 2 number", 3, raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().size());

        // make sure 1st VD 1st drive has Bay 0
        assertEquals("", "Disk.Bay.0:Enclosure.Internal.0-1:" + controllerFqdd,
            raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().get(0));
    }

    @Test
    public void testDrivesUseLast() {
        FilterEnvironment fe = buildFilterEnvironment(advanced2VDLast);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(7, 3), fe);

        assertEquals("SSD hot spares number", 1, raidConfiguration.getSsdHotSpares().size());
        assertEquals("HDD hot spares number", 0, raidConfiguration.getHddHotSpares().size());
        assertEquals("VD number", 2, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 3, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());
        assertEquals("Disks in VD 2 number", 6, raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().size());

        // make sure 1st VD 1st drive has Bay 9
        assertEquals("", "Disk.Bay.9:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().get(0));
    }

    @Test
    public void testBasicStashDrivesNotSelected() {
        FilterEnvironment fe = buildFilterEnvironment(basicRaid0);
        RAIDConfiguration raidConfiguration = util.prepareRAID(buildControllers(7, 3, 2, 2), fe);

        assertEquals("Disk is last", "Disk.Bay.6:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().get(0));
    }

    @Test
    public void testFailForNonRaid01Embedded() {
        FilterEnvironment fe = buildFilterEnvironment(basicRaid50);
        fe.setWindowsOS(true);

        try {
            RAIDConfiguration raidConfig = util.prepareRAID(buildControllers(0,8,2,0), fe);
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough SSDs and HDDs for virtual disk #1", e.getMessage());
        }
    }

    @Test
    public void testFailForNonWindowsEmbedded() {
        FilterEnvironment fe = buildFilterEnvironment(basicRaid1NoHotSpare);

        try {
            RAIDConfiguration raidConfig = util.prepareRAID(buildControllers(0,0,2,0), fe);
        }catch(AsmManagerNotEnoughDisksException e) {
            assertEquals("Not enough SSDs and HDDs for virtual disk #1", e.getMessage());
        }
    }

    @Test
    public void testWindowsEmbedded() {
        FilterEnvironment fe = buildFilterEnvironment(basicRaid1NoHotSpare);
        fe.setWindowsOS(true);

        RAIDConfiguration raidConfig = util.prepareRAID(buildControllers(0,0,2,0), fe);

        assertEquals("VD number", 1, raidConfig.getVirtualDisks().size());
    }

    @Test
    public void testDrivesHadoopLast() {
        FilterEnvironment fe = buildFilterEnvironment(hadoopRaidLast);
        List<Controller> controllers = buildControllers(0, 14);

        // set size
        for (Controller controller : controllers) {
            for (int i=0; i< controller.getPhysicalDisks().size(); i++) {
                PhysicalDisk pd = controller.getPhysicalDisks().get(i);
                if (i<3 || i> 10)
                    pd.setSize(700550590976L);
            }
        }

        RAIDConfiguration raidConfiguration = util.prepareRAID(controllers, fe);

        assertEquals("VD number", 5, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 2, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());

        assertEquals("", "Disk.Bay.13:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().get(0));
        assertEquals("", "Disk.Bay.12:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().get(1));

        assertEquals("", "Disk.Bay.11:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().get(0));
        assertEquals("", "Disk.Bay.2:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().get(1));

    }

    @Test
    public void testDrivesHadoopFirst() {
        FilterEnvironment fe = buildFilterEnvironment(hadoopRaidFirst);
        List<Controller> controllers = buildControllers(0, 14);

        // set size
        for (Controller controller : controllers) {
            for (int i=0; i< controller.getPhysicalDisks().size(); i++) {
                PhysicalDisk pd = controller.getPhysicalDisks().get(i);
                if (i<3 || i> 10)
                    pd.setSize(700550590976L);
            }
        }

        RAIDConfiguration raidConfiguration = util.prepareRAID(controllers, fe);

        assertEquals("VD number", 5, raidConfiguration.getVirtualDisks().size());
        assertEquals("Disks in VD 1 number", 2, raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().size());

        assertEquals("", "Disk.Bay.0:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().get(0));
        assertEquals("", "Disk.Bay.1:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(0).getPhysicalDisks().get(1));

        assertEquals("", "Disk.Bay.13:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().get(0));
        assertEquals("", "Disk.Bay.12:Enclosure.Internal.0-1:" + controllerFqdd,
                raidConfiguration.getVirtualDisks().get(1).getPhysicalDisks().get(1));

    }

    @Test
    public void testSortedControllers() {
        List<Controller> controllers = buildControllers(2,2,2,2);


        HardwareUtil.ControllerComparator controllerComparator = new HardwareUtil.ControllerComparator();

        Collections.sort(controllers, controllerComparator);

        assertEquals("Embedded at end", modularControllerFqdd,
                controllers.get(controllers.size() - 1).getFqdd());
    }

    @Test
    public void testRejectEmbeddedController() {
        List<Controller> controllers = buildControllers(2,2,3, 0);

        util.rejectEmbeddedController(controllers);

        assertEquals("Embedded controller ejected", 1, controllers.size());
    }
}
