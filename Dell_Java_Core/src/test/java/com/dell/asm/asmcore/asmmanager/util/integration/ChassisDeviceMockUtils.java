package com.dell.asm.asmcore.asmmanager.util.integration;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.RandomStringUtils;

import com.dell.pg.asm.chassis.client.ClientUtils;
import com.dell.pg.asm.chassis.client.device.Chassis;
import com.dell.pg.asm.chassis.client.device.Controller;
import com.dell.pg.asm.chassis.client.device.Health;
import com.dell.pg.asm.chassis.client.device.IOM;
import com.dell.pg.asm.chassis.client.device.IOMLocationType;
import com.dell.pg.asm.chassis.client.device.KVM;
import com.dell.pg.asm.chassis.client.device.PowerSupply;
import com.dell.pg.asm.chassis.client.device.Server;
import com.dell.pg.asm.chassis.client.device.ServerSlotType;

public class ChassisDeviceMockUtils {

    /**
     * @return ChassisDevice
     */
    public static Chassis makeChassisDeviceForTest() {
        return makeChassisDeviceForTest(null);
    }

    /**
     * @param refId
     * @return ChassisDevice
     */
    public static Chassis makeChassisDeviceForTest(String refId) {
        Chassis chassisDevice = new Chassis();
        chassisDevice.setUrl(null);
        chassisDevice.setRefType(ClientUtils.CHASSIS_REF_TYPE);
        chassisDevice.setDisplayName(ClientUtils.DEVICE_DISPLAY_NAME);
        chassisDevice.setRefId(refId);

        chassisDevice.setDeviceId(null);
        chassisDevice.setManagementIP("10.255.7.111");
        chassisDevice.setManagementIPStatic(true);
        chassisDevice.setCredentialRefId("credentialRefId");
        chassisDevice.setServiceTag("chassisstag" + RandomStringUtils.randomAlphanumeric(7));
        chassisDevice.setAssetTag("chassisatag" + RandomStringUtils.randomAlphanumeric(7));
        chassisDevice.setHealth(Health.GREEN);
        chassisDevice.setModel("M1000 e");
        chassisDevice.setName("mychassis");
        chassisDevice.setDatacenter("Datacenter12");
        chassisDevice.setAisle("11");
        chassisDevice.setRack("12");
        chassisDevice.setRackslot("13");
        chassisDevice.setDnsName("mychassisdns");

        chassisDevice.setPowerCapPercent(10);
        chassisDevice.setDefaultPowerCapUpperBoundWatts(16685);
        chassisDevice.setDefaultPowerCapUpperBoundBTU(56931);
        chassisDevice.setDefaultPowerCapLowerBoundWatts(2715);
        chassisDevice.setDefaultPowerCapLowerBoundBTU(9263);

        chassisDevice.setMidPlaneVersion("1.1");
        
        GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        chassisDevice.setLastFirmwareUpdateTime(now);
        chassisDevice.setLastUpdateTime(now);

        chassisDevice.setServers(getServers());
        chassisDevice.setIOMs(getIOMs());
        chassisDevice.setControllers(getControllers());
        chassisDevice.setPowerSupplies(getPowerSupplies());
        chassisDevice.setKVMs(getKVMs());
        chassisDevice.setServerCount(chassisDevice.getServers().size());
        chassisDevice.setIomCount(chassisDevice.getIOMs().size());
        return chassisDevice;
    }

    private static List<Server> getServers() {

        List<Server> servers = new ArrayList<Server>();

        for (int i = 1; i <= 16; i++) {
            Server server = new Server();
            server.setId(null);
            server.setServiceTag("serverstag" + RandomStringUtils.randomAlphanumeric(7));
            server.setHealth(Health.GREEN);
            server.setAssetTag("serveratag" + RandomStringUtils.randomAlphanumeric(7));
            server.setManagementIP("10.255.3." + i);
            server.setManagementIPStatic(false);
            server.setModel("M620");
            server.setSlot(i + "");
            server.setSlotName("SLOT " + i);
            server.setSlotType(ServerSlotType.HALF);
            server.setSupported(true);
            servers.add(server);
        }

        return servers;

    }

    private static List<IOM> getIOMs() {

        String[] slot = new String[] { "A1", "A2", "B1", "B2", "C1" };
        List<IOM> ioms = new ArrayList<IOM>();

        for (int i = 1; i <= 5; i++) {
            IOM iom = new IOM();
            iom.setId(null);
            iom.setServiceTag("iomstag" + RandomStringUtils.randomAlphanumeric(7));
            iom.setHealth(Health.GREEN);
            iom.setManagementIP("10.255.4." + i);
            iom.setManagementIPStatic(true);
            iom.setModel("Force10");
            iom.setLocation(IOMLocationType.fromValue(slot[i - 1]));
            iom.setSlot(i);
            iom.setSupported(true);
            ioms.add(iom);
        }

        return ioms;

    }

    private static List<Controller> getControllers() {

        List<Controller> controllers = new ArrayList<Controller>();

        for (int i = 1; i <= 2; i++) {
            Controller controller = new Controller();
            controller.setId(null);
            controller.setControllerName((i == 1 ? "primary" : "secondary"));
            controller.setControllerPrimary((i == 1)); // ? true : false
            controller.setControllerFWVersion("1.0.0");
            controllers.add(controller);
        }

        return controllers;

    }

    private static List<PowerSupply> getPowerSupplies() {

        List<PowerSupply> powerSups = new ArrayList<PowerSupply>();

        for (int i = 1; i <= 6; i++) {
            PowerSupply powerSup = new PowerSupply();
            powerSup.setId(null);
            powerSup.setCapacity("100");
            powerSup.setPowerStatus("on");
            powerSup.setPresent(true);
            powerSup.setSlot(i + "");
            powerSups.add(powerSup);
        }

        return powerSups;

    }

    private static List<KVM> getKVMs() {

        List<KVM> kvms = new ArrayList<KVM>();

        for (int i = 1; i <= 4; i++) {
            KVM kvm = new KVM();
            kvm.setId(null);
            kvm.setFirmwareVersion("1.0.0");
            kvm.setManufacturer("Dell");
            kvm.setName("chassiskvm");
            kvm.setPartNumber("P123");
            kvm.setPresent(true);
            kvms.add(kvm);
        }

        return kvms;

    }

}
