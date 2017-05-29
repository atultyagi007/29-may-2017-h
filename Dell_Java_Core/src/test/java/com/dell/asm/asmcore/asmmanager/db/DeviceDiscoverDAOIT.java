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
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceDiscoverEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.SortParamParser;

public class DeviceDiscoverDAOIT {

	private DeviceDiscoverDAO dao = DeviceDiscoverDAO.getInstance();

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
		validSortColumns.add("marshalledDeviceDiscoverData");
		validSortColumns.add("createdDate");
		validSortColumns.add("createdBy");
		validSortColumns.add("updatedDate");
		validSortColumns.add("updatedBy");
	}

	private static final Set<String> validFilterColumns = new HashSet<>();

	static {
		validFilterColumns.add("status");
		validFilterColumns.add("marshalledDeviceDiscoverData");
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
	 * {@link com.dell.asm.asmcore.asmmanager.db.DeviceDiscoverDAO#getInstance()}
	 * .
	 */
	public void testGetInstance() {
		assertNotNull(dao);
	}

	@Test
	public void testAll() {

		testGetInstance();
		testCreateDeviceDiscover();
		testGetDeviceDiscover();
		testGetAllDeviceDiscoverEntitiesWithSortOnStatus0To20();
		testGetAllDeviceDiscoverEntitiesWithSortOnStatus20To40();
		testGetAllDeviceDiscoverEntitiesWithSortOnMarshalledDataByAsc();
		testGetAllDeviceDiscoverEntitiesWithSortOnMarshalledDataByDesc();
		testGetAllDeviceDiscoverEntitiesWithEqfilterOnStatus();
		testGetAllDeviceDiscoverEntitiesWithEqfilterWithMultipleValuesOnStatus();
		testGetAllDeviceDiscoverEntitiesWithContainsfilterOnStatus();
		testDeleteAllDeviceDiscoverEntityData();
	}

	public void testCreateDeviceDiscover() {

		char[] charVals = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
				'w', 'x', 'y', 'z' };

		DiscoveryStatus[] discoverStatuses = { DiscoveryStatus.ERROR,
				DiscoveryStatus.FAILED, DiscoveryStatus.INPROGRESS,
				DiscoveryStatus.PENDING, DiscoveryStatus.SUCCESS , DiscoveryStatus.CONNECTED, DiscoveryStatus.UNSUPPORTED};

		DeviceDiscoverEntity deviceDiscover = null;

		for (int i = 0; i < 100; i++) {

			deviceDiscover = new DeviceDiscoverEntity();
			deviceDiscover.setId(UUID.randomUUID().toString());
			deviceDiscover
					.setMarshalledDeviceDiscoverData(charVals[(int) (Math
							.random() * 26)]
							+ ""
							+ charVals[(int) (Math.random() * 26)]
							+ ""
							+ charVals[(int) (Math.random() * 26)]
							+ ""
							+ charVals[(int) (Math.random() * 26)]);
			deviceDiscover
					.setStatus(discoverStatuses[(int) (Math.random() * 7)]);

			try {
				dao.createDeviceDiscover(deviceDiscover);
			} catch (AsmManagerCheckedException amde) {
				if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
					fail();
				}
			}
			deviceDiscover = null;
		}
	}

	public void testGetDeviceDiscover() {

		DeviceDiscoverEntity deviceDiscover = new DeviceDiscoverEntity();
		deviceDiscover.setId(UUID.randomUUID().toString());
		deviceDiscover
				.setMarshalledDeviceDiscoverData("testmarshalledDeviceData");
		deviceDiscover.setStatus(DiscoveryStatus.INPROGRESS);

		try {
			deviceDiscover = dao.createDeviceDiscover(deviceDiscover);
		} catch (AsmManagerCheckedException amde) {
			if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST) {
				fail();
			}
		}

		System.out.println(" The device discover Id : "
				+ deviceDiscover.getId());
		String deviceDiscoverId = deviceDiscover.getId();

		DeviceDiscoverEntity retrievedDeviceDiscover = dao
				.getDeviceDiscoverEntityById(deviceDiscoverId);
		assertNotNull(retrievedDeviceDiscover);
		dao.deleteDiscoveryResult(deviceDiscoverId);

	}

	public void testGetAllDeviceDiscoverEntitiesWithSortOnStatus0To20() {

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
		List<DeviceDiscoverEntity> entities = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("=========================================================================");
		System.out
				.println("===========testGetAllDeviceDiscoverEntitiesWithSortOnStatus0To20========");
		System.out
				.println("=========================================================================");

		for (DeviceDiscoverEntity deviceDiscoverEntity : entities) {
			System.out.println(" Device ID : " + deviceDiscoverEntity.getId()
					+ "\t MarshalledDeviceDiscoverData : "
					+ deviceDiscoverEntity.getMarshalledDeviceDiscoverData()
					+ "\t Status : " + deviceDiscoverEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceDiscoverEntitiesWithSortOnStatus20To40() {

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
		List<DeviceDiscoverEntity> entities = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("==========================================================================");
		System.out
				.println("===========testGetAllDeviceDiscoverEntitiesWithSortOnStatus20To40========");
		System.out
				.println("==========================================================================");

		for (DeviceDiscoverEntity deviceDiscoverEntity : entities) {
			System.out.println(" Device ID : " + deviceDiscoverEntity.getId()
					+ "\t MarshalledDeviceDiscoverData : "
					+ deviceDiscoverEntity.getMarshalledDeviceDiscoverData()
					+ "\t Status : " + deviceDiscoverEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceDiscoverEntitiesWithSortOnMarshalledDataByAsc() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		// Parse the sort parameter.
		// Any sort exceptions are already encased in a WebApplicationException
		// with an Status code=400

		String sort = "marshalledDeviceDiscoverData";

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
		List<DeviceDiscoverEntity> entities = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("=================================================================================");
		System.out
				.println("===========testGetAllDeviceDiscoverEntitiesWithSortOnMarshalledDataByAsc========");
		System.out
				.println("=================================================================================");

		for (DeviceDiscoverEntity deviceDiscoverEntity : entities) {
			System.out.println(" Device ID : " + deviceDiscoverEntity.getId()
					+ "\t MarshalledDeviceDiscoverData : "
					+ deviceDiscoverEntity.getMarshalledDeviceDiscoverData());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceDiscoverEntitiesWithSortOnMarshalledDataByDesc() {

		PaginationParamParser paginationParamParser = new PaginationParamParser(
				servletRequest, servletResponse, httpHeaders, uriInfo);

		// Parse the sort parameter.
		// Any sort exceptions are already encased in a WebApplicationException
		// with an Status code=400

		String sort = "-marshalledDeviceDiscoverData";

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
		List<DeviceDiscoverEntity> entities = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("==================================================================================");
		System.out
				.println("===========testGetAllDeviceDiscoverEntitiesWithSortOnMarshalledDataByDesc========");
		System.out
				.println("==================================================================================");

		for (DeviceDiscoverEntity deviceDiscoverEntity : entities) {
			System.out.println(" Device ID : " + deviceDiscoverEntity.getId()
					+ "\t MarshalledDeviceDiscoverData : "
					+ deviceDiscoverEntity.getMarshalledDeviceDiscoverData());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceDiscoverEntitiesWithEqfilterOnStatus() {

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
		List<DeviceDiscoverEntity> entities = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("========================================================================");
		System.out
				.println("===========testGetAllDeviceDiscoverEntitiesWithEqfilterOnStatus========");
		System.out
				.println("========================================================================");

		for (DeviceDiscoverEntity deviceDiscoverEntity : entities) {
			System.out.println(" Device ID : " + deviceDiscoverEntity.getId()
					+ "\t MarshalledDeviceDiscoverData : "
					+ deviceDiscoverEntity.getMarshalledDeviceDiscoverData()
					+ "\t Status : " + deviceDiscoverEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceDiscoverEntitiesWithEqfilterWithMultipleValuesOnStatus() {

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
		List<DeviceDiscoverEntity> entities = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("==========================================================================================");
		System.out
				.println("===========testGetAllDeviceDiscoverEntitiesWithEqfilterWithMultipleValuesOnStatus========");
		System.out
				.println("==========================================================================================");

		for (DeviceDiscoverEntity deviceDiscoverEntity : entities) {
			System.out.println(" Device ID : " + deviceDiscoverEntity.getId()
					+ "\t MarshalledDeviceDiscoverData : "
					+ deviceDiscoverEntity.getMarshalledDeviceDiscoverData()
					+ "\t Status : " + deviceDiscoverEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testGetAllDeviceDiscoverEntitiesWithContainsfilterOnStatus() {

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
		List<DeviceDiscoverEntity> entities = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		System.out
				.println("==============================================================================");
		System.out
				.println("===========testGetAllDeviceDiscoverEntitiesWithContainsfilterOnStatus========");
		System.out
				.println("==============================================================================");

		for (DeviceDiscoverEntity deviceDiscoverEntity : entities) {
			System.out.println(" Device ID : " + deviceDiscoverEntity.getId()
					+ "\t MarshalledDeviceDiscoverData : "
					+ deviceDiscoverEntity.getMarshalledDeviceDiscoverData()
					+ "\t Status : " + deviceDiscoverEntity.getStatus());
		}

		assertTrue(entities.size() > 0);
	}

	public void testDeleteAllDeviceDiscoverEntityData() {

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

		List<DeviceDiscoverEntity> entities = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		paginationInfo = paginationParamParser.new PaginationInfo(50, 50,
				countValue);

		List<DeviceDiscoverEntity> entities1 = dao
				.getAllDeviceDiscoverEntities(sortInfos, filterInfos,
						paginationInfo);

		for (DeviceDiscoverEntity entity : entities)
			refIdStrings.add(entity.getId());

		for (DeviceDiscoverEntity entity : entities1)
			refIdStrings.add(entity.getId());

		for (String id : refIdStrings)
			dao.deleteDiscoveryResult(id);
	}
}
