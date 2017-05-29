package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.ConfigureStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceConfigureEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.SortParamParser;

public class DeviceConfigureDAOIT {

	private DeviceConfigureDAO dao = DeviceConfigureDAO.getInstance();

	@Context
	private HttpServletRequest servletRequest;

	@Context
	private HttpServletResponse servletResponse;

	@Context
	private HttpHeaders httpHeaders;

	@Context
	private UriInfo uriInfo;

	private static final Set<String> validSortColumns = new HashSet<>();

	static {
		validSortColumns.add("status");
		validSortColumns.add("marshalledDeviceConfigureData");
		validSortColumns.add("createdDate");
		validSortColumns.add("createdBy");
		validSortColumns.add("updatedDate");
		validSortColumns.add("updatedBy");
	}

	private static final Set<String> validFilterColumns = new HashSet<>();

	static {
		validFilterColumns.add("status");
		validFilterColumns.add("marshalledDeviceConfigureData");
		validFilterColumns.add("createdDate");
		validFilterColumns.add("createdBy");
		validFilterColumns.add("updatedDate");
		validFilterColumns.add("updatedBy");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.dell.asm.asmcore.asmmanager.db.DeviceConfigureDAO#getInstance()}
	 * .
	 */
	public void testGetInstance() {
		assertNotNull(dao);
	}

	@Test
	public void testAll() {

		testGetInstance();
		testCreateDeviceConfigure();
		testGetDeviceConfigure();
		testGetAllDeviceConfigureEntitiesWithSortOnStatus0To20();
		testGetAllDeviceConfigureEntitiesWithSortOnStatus20To40();
		testGetAllDeviceConfigureEntitiesWithSortOnMarshalledDataByAsc();
		testGetAllDeviceConfigureEntitiesWithSortOnMarshalledDataByDesc();
		testGetAllDeviceConfigureEntitiesWithEqfilterOnStatus();
		testGetAllDeviceConfigureEntitiesWithEqfilterWithMultipleValuesOnStatus();
		testGetAllDeviceConfigureEntitiesWithContainsfilterOnStatus();
		testDeleteAllDeviceConfigureEntityData();
	}

	public void testCreateDeviceConfigure() {

		char[] charVals = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
				'w', 'x', 'y', 'z' };

		ConfigureStatus[] configureStatuses = { ConfigureStatus.ERROR,
				ConfigureStatus.FAILED, ConfigureStatus.INPROGRESS,
				ConfigureStatus.PENDING, ConfigureStatus.SUCCESS };

		DeviceConfigureEntity deviceConfigure = null;

		for (int i = 0; i < 100; i++) {

			deviceConfigure = new DeviceConfigureEntity();
			deviceConfigure.setId(UUID.randomUUID().toString());
			deviceConfigure
					.setMarshalledDeviceConfigureData(charVals[(int) (Math
							.random() * 26)]
							+ ""
							+ charVals[(int) (Math.random() * 26)]
							+ ""
							+ charVals[(int) (Math.random() * 26)]
							+ ""
							+ charVals[(int) (Math.random() * 26)]);
			deviceConfigure
					.setStatus(configureStatuses[(int) (Math.random() * 5)]);

			try {
				dao.createDeviceConfigure(deviceConfigure);
			} catch (AsmManagerCheckedException amde) {
				if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
					fail();
				}
			}
			deviceConfigure = null;
		}
	}

	public void testGetDeviceConfigure() {

		DeviceConfigureEntity deviceConfigure = new DeviceConfigureEntity();
		deviceConfigure.setId(UUID.randomUUID().toString());
		deviceConfigure
				.setMarshalledDeviceConfigureData("testmarshalledDeviceData");
		deviceConfigure.setStatus(ConfigureStatus.INPROGRESS);

		try {
			deviceConfigure = dao.createDeviceConfigure(deviceConfigure);
		} catch (AsmManagerCheckedException amde) {
			if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
				fail();
			}
		}

		System.out.println(" The device configure Id : "
				+ deviceConfigure.getId());
		String deviceConfigureId = deviceConfigure.getId();

		DeviceConfigureEntity retrievedDeviceConfigure = dao
				.getDeviceConfigureEntityById(deviceConfigureId);
		assertNotNull(retrievedDeviceConfigure);
		dao.deleteDiscoveryResult(deviceConfigureId);

	}

	public void testGetAllDeviceConfigureEntitiesWithSortOnStatus0To20() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		// Parse the sort parameter.
		// Any sort exceptions are already encased in a WebApplicationException
		// with an Status code=400

		String sort = "status";

		// No sort information
		SortParamParser sp = new SortParamParser(sort, validSortColumns);
		List<SortParamParser.SortInfo> sortInfos = sp.parse();

		List<String> filterArray = null;
		FilterParamParser filterParser = new FilterParamParser(filterArray,
				validFilterColumns);
		List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

		Integer pageLimit = 20;
		Integer pageOffSet = 0;

		int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
				pageOffSet, pageLimit, countValue);

		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
		List<DeviceConfigureEntity> entities = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("=========================================================================");
		System.out
				.println("===========testGetAllDeviceConfigureEntitiesWithSortOnStatus0To20========");
		System.out
				.println("=========================================================================");

		for (DeviceConfigureEntity deviceConfigureEntity : entities) {
			System.out.println(" Device ID : " + deviceConfigureEntity.getId()
					+ "\t MarshalledDeviceConfigureData : "
					+ deviceConfigureEntity.getMarshalledDeviceConfigureData()
					+ "\t Status : " + deviceConfigureEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceConfigureEntitiesWithSortOnStatus20To40() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		// Parse the sort parameter.
		// Any sort exceptions are already encased in a WebApplicationException
		// with an Status code=400

		String sort = "status";

		// No sort information
		SortParamParser sp = new SortParamParser(sort, validSortColumns);
		List<SortParamParser.SortInfo> sortInfos = sp.parse();

		List<String> filterArray = null;
		FilterParamParser filterParser = new FilterParamParser(filterArray,
				validFilterColumns);
		List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

		Integer pageLimit = 20;
		Integer pageOffSet = 20;

		int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
				pageOffSet, pageLimit, countValue);

		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
		List<DeviceConfigureEntity> entities = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("==========================================================================");
		System.out
				.println("===========testGetAllDeviceConfigureEntitiesWithSortOnStatus20To40========");
		System.out
				.println("==========================================================================");

		for (DeviceConfigureEntity deviceConfigureEntity : entities) {
			System.out.println(" Device ID : " + deviceConfigureEntity.getId()
					+ "\t MarshalledDeviceConfigureData : "
					+ deviceConfigureEntity.getMarshalledDeviceConfigureData()
					+ "\t Status : " + deviceConfigureEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceConfigureEntitiesWithSortOnMarshalledDataByAsc() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		// Parse the sort parameter.
		// Any sort exceptions are already encased in a WebApplicationException
		// with an Status code=400

		String sort = "marshalledDeviceConfigureData";

		// No sort information
		SortParamParser sp = new SortParamParser(sort, validSortColumns);
		List<SortParamParser.SortInfo> sortInfos = sp.parse();

		List<String> filterArray = null;
		FilterParamParser filterParser = new FilterParamParser(filterArray,
				validFilterColumns);
		List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

		Integer pageLimit = 20;
		Integer pageOffSet = 0;

		int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
				pageOffSet, pageLimit, countValue);

		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
		List<DeviceConfigureEntity> entities = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("=================================================================================");
		System.out
				.println("===========testGetAllDeviceConfigureEntitiesWithSortOnMarshalledDataByAsc========");
		System.out
				.println("=================================================================================");

		for (DeviceConfigureEntity deviceConfigureEntity : entities) {
			System.out.println(" Device ID : " + deviceConfigureEntity.getId()
					+ "\t MarshalledDeviceConfigureData : "
					+ deviceConfigureEntity.getMarshalledDeviceConfigureData());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceConfigureEntitiesWithSortOnMarshalledDataByDesc() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		// Parse the sort parameter.
		// Any sort exceptions are already encased in a WebApplicationException
		// with an Status code=400

		String sort = "-marshalledDeviceConfigureData";

		// No sort information
		SortParamParser sp = new SortParamParser(sort, validSortColumns);
		List<SortParamParser.SortInfo> sortInfos = sp.parse();

		List<String> filterArray = null;
		FilterParamParser filterParser = new FilterParamParser(filterArray,
				validFilterColumns);
		List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

		Integer pageLimit = 20;
		Integer pageOffSet = 0;

		int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
				pageOffSet, pageLimit, countValue);

		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
		List<DeviceConfigureEntity> entities = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("==================================================================================");
		System.out
				.println("===========testGetAllDeviceConfigureEntitiesWithSortOnMarshalledDataByDesc========");
		System.out
				.println("==================================================================================");

		for (DeviceConfigureEntity deviceConfigureEntity : entities) {
			System.out.println(" Device ID : " + deviceConfigureEntity.getId()
					+ "\t MarshalledDeviceConfigureData : "
					+ deviceConfigureEntity.getMarshalledDeviceConfigureData());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceConfigureEntitiesWithEqfilterOnStatus() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		String sort = null;

		// No sort information
		SortParamParser sp = new SortParamParser(sort, validSortColumns);
		List<SortParamParser.SortInfo> sortInfos = sp.parse();

		String[] filter = { "eq,status,SUCCESS" };

		List<String> filterArray = Arrays.asList(filter);
		FilterParamParser filterParser = new FilterParamParser(filterArray,
				validFilterColumns);
		List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

		Integer pageLimit = 20;
		Integer pageOffSet = 0;

		int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
				pageOffSet, pageLimit, countValue);

		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
		List<DeviceConfigureEntity> entities = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("========================================================================");
		System.out
				.println("===========testGetAllDeviceConfigureEntitiesWithEqfilterOnStatus========");
		System.out
				.println("========================================================================");

		for (DeviceConfigureEntity deviceConfigureEntity : entities) {
			System.out.println(" Device ID : " + deviceConfigureEntity.getId()
					+ "\t MarshalledDeviceConfigureData : "
					+ deviceConfigureEntity.getMarshalledDeviceConfigureData()
					+ "\t Status : " + deviceConfigureEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceConfigureEntitiesWithEqfilterWithMultipleValuesOnStatus() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		// Parse the sort parameter.
		// Any sort exceptions are already encased in a WebApplicationException
		// with an Status code=400

		String sort = null;

		// No sort information
		SortParamParser sp = new SortParamParser(sort, validSortColumns);
		List<SortParamParser.SortInfo> sortInfos = sp.parse();

		String[] filter = { "eq,status,SUCCESS,INPROGRESS" };

		List<String> filterArray = Arrays.asList(filter);
		FilterParamParser filterParser = new FilterParamParser(filterArray,
				validFilterColumns);
		List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

		Integer pageLimit = 20;
		Integer pageOffSet = 0;

		int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
				pageOffSet, pageLimit, countValue);

		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
		List<DeviceConfigureEntity> entities = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("==========================================================================================");
		System.out
				.println("===========testGetAllDeviceConfigureEntitiesWithEqfilterWithMultipleValuesOnStatus========");
		System.out
				.println("==========================================================================================");

		for (DeviceConfigureEntity deviceConfigureEntity : entities) {
			System.out.println(" Device ID : " + deviceConfigureEntity.getId()
					+ "\t MarshalledDeviceConfigureData : "
					+ deviceConfigureEntity.getMarshalledDeviceConfigureData()
					+ "\t Status : " + deviceConfigureEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceConfigureEntitiesWithContainsfilterOnStatus() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		// Parse the sort parameter.
		// Any sort exceptions are already encased in a WebApplicationException
		// with an Status code=400

		String sort = null;

		// No sort information
		SortParamParser sp = new SortParamParser(sort, validSortColumns);
		List<SortParamParser.SortInfo> sortInfos = sp.parse();

		String[] filter = { "co,status,P" };

		List<String> filterArray = Arrays.asList(filter);
		FilterParamParser filterParser = new FilterParamParser(filterArray,
				validFilterColumns);
		List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

		Integer pageLimit = 20;
		Integer pageOffSet = 0;

		int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
				pageOffSet, pageLimit, countValue);

		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
		List<DeviceConfigureEntity> entities = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("==============================================================================");
		System.out
				.println("===========testGetAllDeviceConfigureEntitiesWithContainsfilterOnStatus========");
		System.out
				.println("==============================================================================");

		for (DeviceConfigureEntity deviceConfigureEntity : entities) {
			System.out.println(" Device ID : " + deviceConfigureEntity.getId()
					+ "\t MarshalledDeviceConfigureData : "
					+ deviceConfigureEntity.getMarshalledDeviceConfigureData()
					+ "\t Status : " + deviceConfigureEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testDeleteAllDeviceConfigureEntityData() {

		List<String> refIdStrings = new ArrayList<>();
		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		SortParamParser sp = new SortParamParser(null, validSortColumns);
		List<SortParamParser.SortInfo> sortInfos = sp.parse();

		List<String> filterArray = null;
		FilterParamParser filterParser = new FilterParamParser(filterArray,
				validFilterColumns);
		List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

		Integer pageLimit = 50;
		Integer pageOffSet = 0;

		int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(
				pageOffSet, pageLimit, countValue);

		List<DeviceConfigureEntity> entities = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		paginationInfo = paginationParamParser.new PaginationInfo(50, 50,
				countValue);

		List<DeviceConfigureEntity> entities1 = dao
				.getAllDeviceConfigureEntities(sortInfos, filterInfos,
						paginationInfo);

		for (DeviceConfigureEntity entity : entities)
			refIdStrings.add(entity.getId());

		for (DeviceConfigureEntity entity : entities1)
			refIdStrings.add(entity.getId());

		for (String id : refIdStrings)
			dao.deleteDiscoveryResult(id);
	}
}
