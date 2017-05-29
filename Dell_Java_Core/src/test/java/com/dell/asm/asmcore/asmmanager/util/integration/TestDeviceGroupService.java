package com.dell.asm.asmcore.asmmanager.util.integration;

import static org.junit.Assert.fail;

import javax.ws.rs.WebApplicationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.dell.asm.asmcore.asmmanager.client.devicegroup.DeviceGroup;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.DeviceGroupList;
import com.dell.asm.asmcore.asmmanager.client.devicegroup.IDeviceGroupService;

public class TestDeviceGroupService {

	private static final String URL = TestUtil.SECURITY_URL;
	IDeviceGroupService proxy = TestUtil.createProxyWithTestAuth(URL,
			IDeviceGroupService.class);

	@Rule
	public ExpectedException exception = ExpectedException.none();

	/* ------------------------------------------------------------------------- */
	/* main: */
	/* ------------------------------------------------------------------------- */
	public static void main(String[] args) {
		TestDeviceGroupService testdeviceGroup = new TestDeviceGroupService();
		try {
			testdeviceGroup.testdeviceGroupRESTAPI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test device Group CRUD operations
	 * 
	 * @param
	 * 
	 * @return
	 * 
	 * @exception Exception
	 * 
	 */
	@Test
	public void testdeviceGroupRESTAPI() {

		DeviceGroup deviceGroup = new DeviceGroup();
		deviceGroup.setGroupName("TestGroup");
		deviceGroup.setGroupDescription("Test");

		DeviceGroup deviceGroup1 = proxy.createDeviceGroup(deviceGroup);
		Long seqId = deviceGroup1.getGroupSeqId();
		try {

			// test retrieve
			DeviceGroup deviceGroupById = proxy.getDeviceGroup(String
					.valueOf(seqId));
			assert deviceGroupById != null;

			DeviceGroup update_deviceGroup = new DeviceGroup();
			update_deviceGroup.setGroupName("UpdatedTestGroup");

			DeviceGroup updated_deviceGroup = proxy.updateDeviceGroup(
					String.valueOf(seqId), update_deviceGroup);
			assert updated_deviceGroup.getGroupName()
					.equals("UpdatedTestGroup");

			DeviceGroup[] deviceGroupList = proxy.getAllDeviceGroup(null,
					null, 0, 50);
			assert deviceGroupList != null;

			proxy.deleteDeviceGroup(String.valueOf(seqId));

		} catch (Exception amde) {
			amde.printStackTrace();
			fail();
			// }
		}

	}

}
