package com.dell.asm.asmcore.asmmanager.client.configure;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ConfigurationRequest")
@ApiModel(value="Configuration Request",
                  description="configure networking, credentials etc for chassis/servers/ioms")
public class ConfigurationRequest {

    public DiscoveredDeviceConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(DiscoveredDeviceConfiguration configuration) {
        this.configuration = configuration;
    }

    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    @ApiModelProperty(value="Device configuration")
    private DiscoveredDeviceConfiguration configuration;

    @ApiModelProperty(value="Devices to configure (refIds)")
    private List<String> devices;
    
}
