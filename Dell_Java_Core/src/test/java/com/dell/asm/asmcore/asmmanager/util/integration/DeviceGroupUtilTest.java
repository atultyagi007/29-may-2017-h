package com.dell.asm.asmcore.asmmanager.util.integration;

import static org.junit.Assert.assertNotNull;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.DeviceGroupUtil;

public class DeviceGroupUtilTest {

	@Test
	public void testvalidateUserObject() throws Exception {

	}

	@Test
	public void testvalidateDeviceObject() throws Exception {

	}

	/**
	 * Test validate Field
	 * 
	 * @param
	 * 
	 * @return
	 * 
	 * @exception WebApplicationException, NoClassDefFoundError, ExceptionInInitializerError,
	 *                Exception
	 * 
	 * @throws Exception
	 */
	@Test
	public void testValidateField() throws Exception,
			AsmManagerCheckedException {
		try {
			DeviceGroupUtil.validateField(null);
		} catch (WebApplicationException e) {
			assertNotNull("Exception", e);
		} catch (NoClassDefFoundError e) {
			assertNotNull("Exception", e);
		} catch (Exception e) {
			assertNotNull("Exception", e);
		} catch (ExceptionInInitializerError e) {
			assertNotNull("Exception", e);
		}

		try {
			DeviceGroupUtil.validateField("");
		} catch (WebApplicationException e) {
			assertNotNull("Exception", e);
		} catch (NoClassDefFoundError e) {
			assertNotNull("Exception", e);
		} catch (Exception e) {
			assertNotNull("Exception", e);
		} catch (ExceptionInInitializerError e) {
			assertNotNull("Exception", e);
		}

		try {
			DeviceGroupUtil.validateField(" ");
		} catch (WebApplicationException e) {
			assertNotNull("Exception", e);
		} catch (NoClassDefFoundError e) {
			assertNotNull("Exception", e);
		} catch (Exception e) {
			assertNotNull("Exception", e);
		} catch (ExceptionInInitializerError e) {
			assertNotNull("Exception", e);
		}

		DeviceGroupUtil.validateField("This is a test, this is only a test!");
	}

	/**
	 * Test validate Integer value
	 * 
	 * @param 
	 * 
	 * @return 
	 * 
	 * @exception
	 * 
	 * @throws NumberFormatException, AsmManagerCheckedException
	 * 
	 */
	@Test
	public void testValidateInputInteger() throws NumberFormatException,
			AsmManagerCheckedException {
		for (long i = -65535 * 2; i <= 65535 * 2; i++) {
			DeviceGroupUtil.validateInputInteger(Long.toString(i));
		}
	}

	/**
	 * Test validate invalid Integer value
	 * 
	 * @param 
	 * 
	 * @return 
	 * 
	 * @exception WebApplicationException, NoClassDefFoundError, Exception
	 * 
	 * @throws
	 * 
	 */
	@Test
	public void testBadValidateInputInteger() {
		try {
			DeviceGroupUtil
					.validateInputInteger("This is a test, this is only a test!");
		} catch (WebApplicationException e) {
			assertNotNull("Exception", e);
		} catch (NoClassDefFoundError e) {
			assertNotNull("Exception", e);
		} catch (Exception e) {
			assertNotNull("Exception", e);
		}
	}

	/**
	 * Test get Device Group User
	 * 
	 * @param 
	 * 
	 * @return
	 *  
	 * @exception 
	 * 
	 * @throws
	 * 
	 */
	@Test
	public void testgetDeviceGroupUsers() {
		DeviceGroupUtil.getDeviceGroupUsers(null);
	}

	/**
	 * Test delete Device Group User association
	 * 
	 * @param 
	 * 
	 * @return 
	 * 
	 * @exception 
	 * 
	 * @throws
	 * 
	 */

	@Test
	public void testdeleteGroupUsersAssociationFromDB()
			throws AsmManagerCheckedException {
		DeviceGroupUtil.deleteGroupUsersAssociationFromDB(null);
	}
}
