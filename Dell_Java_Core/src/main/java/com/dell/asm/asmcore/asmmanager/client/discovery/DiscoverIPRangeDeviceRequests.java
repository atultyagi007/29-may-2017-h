package com.dell.asm.asmcore.asmmanager.client.discovery;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;

@XmlRootElement(name = "DeviceDiscoveryRequests")
@ApiModel(value="Device Discovery Request",
		  description="Captures properties needed to perform a device discovery ")

public class DiscoverIPRangeDeviceRequests {
	
	/**
	 * 
	 */
	
	@XmlElement(required=true, name="DiscoverIPRangeDeviceRequest")
	private Set<DiscoverIPRangeDeviceRequest> reqs;
	

	
	public Set<DiscoverIPRangeDeviceRequest> getDiscoverIpRangeDeviceRequests() {
		if (reqs == null)
			reqs = new HashSet<DiscoverIPRangeDeviceRequest>();
		return reqs;
	}

	public DiscoverIPRangeDeviceRequests(Set<DiscoverIPRangeDeviceRequest> reqs) {
		for (DiscoverIPRangeDeviceRequest req : reqs) {
			getDiscoverIpRangeDeviceRequests().add(new DiscoverIPRangeDeviceRequest(req));
		}
	}
	
	public DiscoverIPRangeDeviceRequests() {
	}
	
	

}
