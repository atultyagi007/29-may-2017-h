package com.dell.asm.asmcore.asmmanager.app.rest;

import java.io.IOException;
import java.net.URL;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSetting;
import com.dell.pg.orion.common.utilities.MarshalUtil;
import com.dell.wsman.xmlconfig.Component;
import com.dell.wsman.xmlconfig.SystemConfiguration;
import com.google.common.base.Charsets;
import junit.framework.Assert;

public class ServiceTemplateServiceParseTest {
	
	ServiceTemplateService service = new ServiceTemplateService(null,null,null,null,null,null,null,null,null,null,null,null);
	static SystemConfiguration sysConfig;
	static String expectedResult;
	static TreeMap<String, Component> networkInterfaces = new TreeMap<String, Component>();

    @BeforeClass
	public static void setup() throws IOException {
		URL resource = ServiceTemplateServiceParseTest.class.getClassLoader().getResource("ServiceTemplateServiceParseTest.xml");
		assert resource != null;
		String xml = IOUtils.toString(resource, Charsets.UTF_8);
		sysConfig = MarshalUtil.unmarshal(SystemConfiguration.class, xml);

		resource = ServiceTemplateServiceParseTest.class.getClassLoader().getResource("ServiceTemplateServiceParseTest-expected.json");
		assert resource != null;
		expectedResult = IOUtils.toString(resource, Charsets.UTF_8);

		//Iterate over the settings from the device, not really a better way to do this :(
        for (Component c: sysConfig.getComponent()) 
        {    
        	
        	if (c.getFQDD() != null && c.getFQDD().toLowerCase().startsWith("nic"))
        		networkInterfaces.put(c.getFQDD(), c);
        }
	      
	}

	@Test
	public void test() 
	{		
		ServiceTemplateSetting serverSettingNetwork = new ServiceTemplateSetting();
		service.applyNetworkCustomization(serverSettingNetwork, networkInterfaces, false);
		
		//If network generation changes we will need to update the expected result value and recommit the test
		System.out.println(JSONObject.quote(serverSettingNetwork.getValue()));
		
		//We are simply asserting on length here because the primary keys of the created network configs will be different
		Assert.assertTrue("Wrong value created expected: " + expectedResult + " Received: " + serverSettingNetwork.getValue(),
				expectedResult.length() == serverSettingNetwork.getValue().length() );
	}
}
