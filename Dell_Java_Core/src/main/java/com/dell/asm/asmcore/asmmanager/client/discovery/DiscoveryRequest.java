package com.dell.asm.asmcore.asmmanager.client.discovery;

import com.dell.asm.asmcore.asmmanager.client.configure.DiscoveredDeviceConfiguration;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.dell.asm.rest.common.model.Link;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "DiscoveryRequest")
@ApiModel(value="Discovery Request",
                  description="caputures the properties for discovery request and result of the discovery result")
public class DiscoveryRequest {
    
    @ApiModelProperty(value="id",required=true)
    private String id;
          
    @ApiModelProperty(value="Device Discovery Request",
            notes="Captures properties needed to perform a device discovery ")
    private DiscoverIPRangeDeviceRequests discoveryRequestList;
    

    @ApiModelProperty(value="Status")
    private DiscoveryStatus status;

    @ApiModelProperty(value="Status Message", notes="This is a localized message, and same is stored in database")
   
    private String statusMessage;
    
    private Link link;

    @ApiModelProperty(value="Total Count of Record")
    private int totalCount;

    @ApiModelProperty(value="Device Result List")
    private List<DiscoveredDevices> devices;
    
    public DiscoveryStatus getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatus(DiscoveryStatus status) {
        this.status = status;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    public DiscoverIPRangeDeviceRequests getDiscoveryRequestList() {
        return discoveryRequestList;
    }

    public void setDiscoveryRequestList(DiscoverIPRangeDeviceRequests discoveryRequestList) {
        this.discoveryRequestList = discoveryRequestList;
    }
  
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    @XmlElement(name = "DiscoveredDevices")
    public List<DiscoveredDevices> getDevices() {
        return devices;
    }

    public void setDevices(List<DiscoveredDevices> devices) {
        this.devices = devices;
    }

   
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

}
