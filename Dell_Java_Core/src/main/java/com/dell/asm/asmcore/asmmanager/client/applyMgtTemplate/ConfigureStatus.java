package com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate;

public enum ConfigureStatus {
    
    
    PENDING("Pending"),
    INPROGRESS("Inprogress"),
    SUCCESS("Success"),
    FAILED("Failed to discover"),
    ERROR("Error discovering");

    
    private String _label;
    
    private ConfigureStatus(String label){_label = label;}
    
    public String getLabel(){return _label;}
       
    public String getValue(){return name();}
       
    @Override
    public String toString(){return _label;} 
}
