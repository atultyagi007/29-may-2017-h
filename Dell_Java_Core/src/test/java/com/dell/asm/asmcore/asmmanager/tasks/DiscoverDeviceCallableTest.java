package com.dell.asm.asmcore.asmmanager.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveredDevices;
import com.dell.asm.common.utilities.ExecuteSystemCommands;
import com.dell.asm.common.model.CommandResponse;



public class DiscoverDeviceCallableTest {
    @Test
    public void testEquallogicVersionParsing() {
        //String matchfw = "{\n  \"Env02-Member-01\": \"V6.0.2 (R294004) (H1)\"\n}";
        //assertEquals("6.0.2", DiscoverDeviceCallable.parseEquallogicVersion(matchfw));
    }

    @Test
    public void testDiscoverBMCServer() {
        /*
        try {

            String serverMsg = "FRU Device Description : Builtin FRU Device (ID 0)\n" + "Chassis Type                    : Other \n"
                    + "Chassis Part Number     :\n" + " Chassis Serial                  :   \n"
                    + "Board Mfg Date        : Tue May 22 19:59:00 2012 \n" + "Board Mfg             : Dell Inc.\n"
                    + "Board Product         : Radon\n" + "Board Extra           : CN0HYFFG7016325K00ACA01\n" + "Board Extra           : 20120630 \n"
                    + "Product Manufacturer  : Dell Inc.\n" + "Product Name          : PowerEdge C6220\n" + "Product Version       : \n"
                    + "Product Serial        : 18306V1 \n" + "Product Asset Tag     : \n";

            DiscoverDeviceCallable d = new DiscoverDeviceCallable(null, null);
            DiscoveredDevices result = new DiscoveredDevices();
            result.setIpAddress("10.0.0.0");
            d.parseBMCOutput(result, serverMsg);
            assertEquals("PowerEdge C6220", result.getModel());
            assertEquals("Dell Inc.", result.getVendor());
        } catch (Exception e) {

        }
*/
    }


}
