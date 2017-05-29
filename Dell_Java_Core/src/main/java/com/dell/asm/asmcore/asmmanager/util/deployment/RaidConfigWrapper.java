package com.dell.asm.asmcore.asmmanager.util.deployment;

import com.dell.asm.asmcore.asmmanager.client.hardware.RAIDConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.TemplateRaidConfiguration;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.VirtualDiskConfiguration;
import com.dell.pg.asm.server.client.device.Controller;
import com.dell.pg.asm.server.client.device.Enclosure;
import com.dell.asm.asmcore.asmmanager.client.hardware.VirtualDisk;

import java.util.HashSet;
import java.util.List;

/*
    This class simply wraps the template (user passed) RAID configuration as well as a temporary/work in progress
        raid configuration that is used by filtering to generate the RAID configuration that gets sent to Puppet.
    It eliminates the need to have to do specific if/elses in the filtering code to call different methods depending if it's an external or internal raid filtering we're doing
*/
public class RaidConfigWrapper {
    private TemplateRaidConfiguration templateRaidConfig;
    private RAIDConfiguration workingRaidConfig;
    //enclosures will contain only the enclosures that are relevant for internal/external RAID
    private HashSet<String> enclosures;
    private boolean forExternal;

    public RaidConfigWrapper(TemplateRaidConfiguration templateRaidConfig, Controller controller, boolean forExternal) {
        this.templateRaidConfig = templateRaidConfig;
        this.forExternal = forExternal;
        this.enclosures = new HashSet<String>();
        this.workingRaidConfig = new RAIDConfiguration();
        for( Enclosure e: controller.getEnclosures()) {
            boolean isDasModel = e.getProductName().toLowerCase().contains("md1400");
            if (this.forExternal && isDasModel) {
                enclosures.add(e.getFqdd());
            } else if (!this.forExternal && !isDasModel) {
                enclosures.add(e.getFqdd());
            }
        }
    }

    public HashSet<String> getEnclosures(){
        return enclosures;
    }

    public int getMinimumSsd() {
        if (forExternal)
            return templateRaidConfig.getMinimumssdexternal();
        else
            return templateRaidConfig.getMinimumssd();
    }

    public int getGlobalHotspares() {
        if (forExternal)
            return templateRaidConfig.getGlobalhotsparesexternal();
        else
            return templateRaidConfig.getGlobalhotspares();
    }

    public List<VirtualDiskConfiguration> getVirtualDisks() {
        if (forExternal)
            return templateRaidConfig.getExternalvirtualdisks();
        else
            return templateRaidConfig.getVirtualdisks();
    }

    public boolean isEnableGlobalHotspares() {
        if (forExternal)
            return templateRaidConfig.isEnableglobalhotsparesexternal();
        else
            return templateRaidConfig.isEnableglobalhotspares();
    }

    public List<String> getWorkingSsdHotSpares() {
        if(forExternal)
            return workingRaidConfig.getExternalSsdHotSpares();
        else
            return workingRaidConfig.getSsdHotSpares();
    }

    public List<String> getWorkingHddHotSpares() {
        if(forExternal)
            return workingRaidConfig.getExternalHddHotSpares();
        else
            return workingRaidConfig.getHddHotSpares();
    }

    public List<VirtualDisk> getWorkingVirtualDisks(){
        if(forExternal)
            return workingRaidConfig.getExternalVirtualDisks();
        else
            return workingRaidConfig.getVirtualDisks();
    }

    public RAIDConfiguration getWorkingRaidConfig(){
        return workingRaidConfig;
    }

    public void reset(){
        workingRaidConfig.reset(forExternal);
    }
}
