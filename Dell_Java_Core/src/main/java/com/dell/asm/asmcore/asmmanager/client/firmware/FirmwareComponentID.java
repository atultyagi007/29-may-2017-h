package com.dell.asm.asmcore.asmmanager.client.firmware;

public enum FirmwareComponentID {
	

    COMPONENT_POWERCONNECT("31123"),
    COMPONENT_POWERCONNECTN3000("31131"),
    COMPONENT_POWERCONNECTN4000("31132"),
    COMPONENT_FORCE10S4810("29629"),
    COMPONENT_FORCE10S5000("29630"),
    COMPONENT_FORCE10S6000("29631"),
    COMPONENT_FORCE10GENERIC("29632"),
    COMPONENT_FORCE10IOM("31122"),
    COMPONENT_FORCE10FX2("31130"),
    COMPONENT_NETWORKING_S4048_ON("29633"),
    COMPONENT_BROCADE("31124"),
    COMPONENT_IOMX84("31125"),
    COMPONENT_COMPELLENT("31128"),
    COMPONENT_NETAPP("31127"),
    COMPONENT_EQUALLOGIC("31129"),
    COMPONENT_EMC_VNX("31126");
	   
	  

	    private String _label;

	    private FirmwareComponentID(String label) {
	        _label = label;
	    }

	    public String getLabel() {
	        return _label;
	    }

	    public String getValue() {
	        return name();
	    }

	    @Override
	    public String toString() {
	        return _label;
	    }

}
