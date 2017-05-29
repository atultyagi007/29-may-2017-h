/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.util.razor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.app.AsmManagerApp;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class RazorUtil {
    public static final String NODES_NAME_KEY = "name";

    public static final String NODE_HW_INFO_KEY = "hw_info";
    public static final String NODE_HW_INFO_UUID_KEY = "uuid";
    public static final String NODE_HW_INFO_SERIAL_KEY = "serial";

    public static final String NODE_FACTS_KEY = "facts";
    public static final String FACT_IPADDRESS_KEY = "ipaddress";
    public static final String FACT_MACADDRESS_KEY = "macaddress";
    public static final String FACT_MANUFACTURER_KEY = "manufacturer";
    public static final String NODE_POLICY = "policy";
    private static final Logger LOGGER = Logger.getLogger(RazorUtil.class);

    private RazorUtil() {
    }

    /**
     * Parse the return value of the razor nodes GET api, e.g.
     * http://RAZOR_HOST/api/collections/nodes
     * into a list of node names.
     *
     * @param mapper ObjectMapper to parse JSON
     * @param json Json nodes data
     * @return List of node names
     */
    public static List<String> parseNodeNamesJson(ObjectMapper mapper, String json) throws IOException {
        List<String> ret = new ArrayList<>();
        Map repoMap = mapper.readValue(json, Map.class);
        List list = (List) repoMap.get("items");
        for (Object o : list) {
            Map elem = (Map) o;
            String name = (String) elem.get(NODES_NAME_KEY);
            if (StringUtils.isEmpty(name)) {
                LOGGER.warn("Invalid node element " + elem + " in " + json);
            } else {
                ret.add(name);
            }
        }
        return ret;
    }

    /**
     * Parse the return value of the razor node GET api, e.g.
     * http://RAZOR_HOST/api/collections/nodes/node2
     *
     * @param mapper ObjectMapper to parse JSON
     * @param nodeJson Json node data
     * @return Device object
     */
    public static RazorDevice parseNodeJson(ObjectMapper mapper, String nodeJson) throws IOException 
    {
        Map node = mapper.readValue(nodeJson, Map.class);        
        Map hwInfo = (Map) node.get(NODE_HW_INFO_KEY);
        String uuid = (String) hwInfo.get(NODE_HW_INFO_UUID_KEY);
        RazorDevice ret = new RazorDevice();
        ret.setId( (String)node.get("name") );
        ret.setUuid(uuid);
        
        Map policy = (Map) node.get(NODE_POLICY);
        if (policy == null || policy.isEmpty())
        {
            ret.setStatus(RazorDeviceStatus.UNPROVISIONED);
        }
        else
        {
            //System.out.println("Policy...");
            for (Object key : policy.keySet()) 
            {
                String policyName = (String) key;
                Object policyValueObject = policy.get(policyName);
                String policyValueString = policyValueObject.toString();
                ret.getPolicy().put( policyName, policyValueString);
                
                //System.out.println(policyName + ":" + policyValueString);
            }
            //System.out.println("");
            ret.setStatus(RazorDeviceStatus.PROVISIONED); 
        }
        
        List tagslist = (List) node.get("tags");
        if( tagslist != null && tagslist.size() > 0 )
        {
            for (Object aTagslist : tagslist) {
                Map tags = (Map) aTagslist;
                if (tags != null && tags.size() > 0) {
                    //System.out.println("Tag...");
                    for (Object key : tags.keySet()) {
                        String tagName = (String) key;
                        Object tagValueObject = tags.get(tagName);
                        String tagValueString = tagValueObject.toString();
                        ret.getTags().put(tagName, tagValueString);

                        //System.out.println(tagName + ":" + tagValueString);
                    }
                    //System.out.println("");
                }
            }        	
        }
        
        Map facts = (Map) node.get(NODE_FACTS_KEY);
        if (facts == null) {
            LOGGER.warn("No facts found for node uuid " + ret.getId());
        } 
        else 
        {
            for (Object key : facts.keySet()) {
                String factName = (String) key;
                Object factValueObject = facts.get(factName);
                // Fact values are generally strings, but can be other raw types like integers,
                // e.g. for physicalprocessorcount
                String factValue = factValueObject.toString();
                ret.getFacts().put(factName, factValue);
                switch (factName) {
                case FACT_IPADDRESS_KEY:
                    ret.setIpAddress(factValue);
                    break;
                case FACT_MACADDRESS_KEY:
                    ret.setMacAddress(factValue);
                    break;
                case FACT_MANUFACTURER_KEY:
                    ret.setManufacturer(factValue);
                    break;
                }
            }
        }

        return ret;
    }

    public static SortedMap<String, RazorDevice> getRazorDevicesHelper()
    {
        ObjectMapper mapper = new ObjectMapper();
        WebClient client = WebClient.create(AsmManagerApp.razorApiUrl);
        client.accept("application/json");
        String json = client.path("collections/nodes").get(String.class);

        try {
            List<String> nodeNames = RazorUtil.parseNodeNamesJson(mapper, json);
            SortedMap<String, RazorDevice> ret = new TreeMap<>();
            for (String name : nodeNames) {
                String nodeJson = client.path(name).get(String.class);
                RazorDevice device = RazorUtil.parseNodeJson(mapper, nodeJson);
                if (StringUtils.isEmpty(device.getId())) {
                	LOGGER.warn("Failed to get id for razor node: " + nodeJson);
                } else {
                    ret.put(device.getId(), device);
                }
                client.back(false);
            }
            return ret;
        } catch (IOException e) {
            throw new AsmManagerRuntimeException(e);
        }
    }

    /**
     * Parse the return value of the razor nodes GET api, with Query string:
     * http://RAZOR_HOST/api/collections/nodes?serial=&lt;serial&gt;
     * into a list of node names, expecting 1 name.
     *
     * Then, using that name, call the GET api again:
     * http://RAZOR_HOST/api/collections/nodes/&lt;name&gt;
     *
     * @param serial number of Razor device from hw_info
     * @return First matching Razor device or null
     */
    public static RazorDevice getNodeBySerial(String serial)
    {
	RazorDevice device = null;
	ObjectMapper mapper = new ObjectMapper();
	WebClient client = WebClient.create(AsmManagerApp.razorApiUrl);
	client.accept("application/json");
	client.query(NODE_HW_INFO_SERIAL_KEY,serial);
	String json = client.path("collections/nodes").get(String.class);

	// Parse the json for a list of nodes, hopefully only 1.
	try {
	    List<String> nodeNames = RazorUtil.parseNodeNamesJson(mapper, json);

	    if ( nodeNames.size() == 0 ) {
		LOGGER.warn( "No Razor devices found with Serial Number = " + serial);	    
	    } else {
		if ( nodeNames.size() > 1 ) {
		    LOGGER.error( "Multiple Razor devices found with Serial Number = " + serial + " Returning the first one.");
		} 
		String nodeName = nodeNames.get(0);
		String nodeJson = client.resetQuery().path(nodeName).get(String.class);
		device = RazorUtil.parseNodeJson(mapper, nodeJson);
	    }
	} catch (IOException e) {
	    throw new AsmManagerRuntimeException(e);
	}

	return device;
    }
}
