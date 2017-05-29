package com.dell.asm.asmcore.asmmanager.client.deviceinventory;

/**
 * Firmware is currently managed based on the ASM baseline firmware repository settings that ship with ASM and the
 * default firmware repository that is set as part of a Deployment's settings. <br>
 * <br>
 * This class represents the Compliance state that a Device may be in based on the Firmware settings of a Deployment. 
 */
public enum CompliantState {
    
    /**
     * This state is only used to indicate no known compliance can be identified.  It's only returned/used when a 
     * a null value is passed into the Compliant.fromValue(String) method. 
     */
    NA("NA"),
    
    /**
     * Indicates the Device is compliant with the baseline and the default firmware repository settings of a Deployment.
     */
    COMPLIANT("compliant"),
    
    /**
     * Indicates the Device is not compliant with the default firmware repository settings of a Deployment.
     */
    NONCOMPLIANT("noncompliant"),
    
    /**
     * Indicates the Device's compliance is not yet known or identified.  Typically set/returned when beginning the
     * discovery process for a Device.
     */
    UNKNOWN("unknown"),
    
    /**
     * Indicates the device is not up to date with the minimum ASM baseline firmware repository settings.  The ASM
     * baseline firmware settings are the firmware repository/requirements that ship with ASM.
     */
    UPDATEREQUIRED("updaterequired");

    
    private String _label;
    
    private CompliantState(String label){_label = label;}
    
    public String getLabel(){return _label;}
       
    public String getValue(){return name();}
       
    @Override
    public String toString(){return _label;}

    public static CompliantState fromValue(String value) {
        if (value == null)
            return NA;

        for (CompliantState type : CompliantState.values()) {
            if (type.name().equals(value))
                return type;
        }
        throw new IllegalArgumentException("Wrong value for CompliantState: " + value +
                ". Supported values are: NA,COMPLIANT,NONCOMPLIANT,UNKNOWN,UPDATEREQUIRED");
    }
}
