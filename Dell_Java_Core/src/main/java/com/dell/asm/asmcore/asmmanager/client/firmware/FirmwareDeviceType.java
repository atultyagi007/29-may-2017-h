package com.dell.asm.asmcore.asmmanager.client.firmware;

public class FirmwareDeviceType {
	 private FirmwareDeviceType() {}
     public enum DeviceTypes {
    	 Switch("switch"),
    	 Storage("storage");
    	 String enumValue;

    	 DeviceTypes(String value) {
             enumValue = value;
         }

         @Override
         public String toString() {
             return enumValue;
         }
     }
     
     public enum DeviceModelSwitch {
        DELL_S4048_ON("Dell Networking S4048-ON/S4048T-ON"),
    	DELL_S4810("Dell Networking S4810/S4820"),
    	DELL_S5000("Dell Networking S5000"),
    	DELL_S6000("Dell Networking S6000"),
    	DELL_IOM("Dell PowerEdge M I/O Aggregator Switch"),
    	DELL_MXL("Dell Networking MXL 10/40GbE blade switch"),
    	DELL_FN2210("Dell Networking FN2210"),
    	DELL_FN410("Dell Networking FN410"),
    	DELL_N3000("Dell Networking N3000"),
    	DELL_N4000("Dell Networking N4000");
    	String enumValue;
    	DeviceModelSwitch(String value) {
    		enumValue = value;
    	}
    	 @Override
         public String toString() {
             return enumValue;
         }
     }
     
     public enum DeviceModelStorage {
    	DELL_PS4100("Dell PS4100"),
    	DELL_PS4110("Dell PS4110"),
    	DELL_PS4210("Dell PS4210"),
    	DELL_PS6100("Dell PS6100"),
    	DELL_PS6110("Dell PS6110"),
    	DELL_PS6210("Dell  PS6210"),
    	DELL_PS6510("Dell PS6510"),
    	DELL_SC8000("Dell SC8000"),
    	DELL_SC200("Dell SC200"),
    	DELL_SC220("Dell SC220"),
    	DELL_SC280("Dell SC280"),
    	DELL_SC4020("Dell SC4020");
    	
    	String enumValue;
    	DeviceModelStorage(String value) {
    		enumValue = value;
    	}
    	 @Override
         public String toString() {
             return enumValue;
         }
    	
     }
	 
}
	