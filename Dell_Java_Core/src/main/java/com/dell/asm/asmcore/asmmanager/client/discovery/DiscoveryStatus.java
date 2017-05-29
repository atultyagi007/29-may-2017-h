package com.dell.asm.asmcore.asmmanager.client.discovery;

public enum DiscoveryStatus {
    
    
    PENDING("Pending"),
    CONNECTED("Connected"),
    UNSUPPORTED("Unsupported"),
    INPROGRESS("Inprogress"),
    SUCCESS("Success"),
    FAILED("Failed to discover"),
    ERROR("Error discovering"),
    IGNORE("Ignore device at this time");
    
    private String _label;
    
    private DiscoveryStatus(String label){_label = label;}
    
    public String getLabel(){return _label;}
       
    public String getValue(){return name();}
       
    @Override
    public String toString(){return _label;} 
}
