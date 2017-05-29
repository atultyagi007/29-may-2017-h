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

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.deployment.DeploymentStatusType;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.SortParamParser;


public class DeviceInventoryFSPDAOIT {

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
       validSortColumns.add("displayName");
       validSortColumns.add("serviceTag");
       validSortColumns.add("refId");
       validSortColumns.add("health");
       validSortColumns.add("refType");
       validSortColumns.add("deviceType");
       validSortColumns.add("ipAddress");
       validSortColumns.add("state");
       validSortColumns.add("model");
       validSortColumns.add("statusMessage");
       validSortColumns.add("createdDate");
       validSortColumns.add("createdBy");
       validSortColumns.add("updatedDate");
       validSortColumns.add("updatedBy");
       validSortColumns.add("healthMessage");
       validSortColumns.add("compliant");
       validSortColumns.add("infraTemplateDate");
       validSortColumns.add("infraTemplateId");
       validSortColumns.add("serverTemplateDate");
       validSortColumns.add("serverTemplateId");
       validSortColumns.add("inventoryDate");
       validSortColumns.add("complianceCheckDate");
       validSortColumns.add("discoveredDate");
       validSortColumns.add("identityRef");
   }

   private static final Set<String> validFilterColumns = new HashSet<>();

   static {
       validFilterColumns.add("displayName");
       validFilterColumns.add("serviceTag");
       validFilterColumns.add("refId");
       validFilterColumns.add("health");
       validFilterColumns.add("refType");
       validFilterColumns.add("deviceType");
       validFilterColumns.add("ipAddress");
       validFilterColumns.add("state");
       validFilterColumns.add("model");
       validFilterColumns.add("statusMessage");
       validFilterColumns.add("createdDate");
       validFilterColumns.add("createdBy");
       validFilterColumns.add("updatedDate");
       validFilterColumns.add("updatedBy");
       validFilterColumns.add("healthMessage");
       validFilterColumns.add("compliant");
       validFilterColumns.add("infraTemplateDate");
       validFilterColumns.add("infraTemplateId");
       validFilterColumns.add("serverTemplateDate");
       validFilterColumns.add("serverTemplateId");
       validFilterColumns.add("inventoryDate");
       validFilterColumns.add("complianceCheckDate");
       validFilterColumns.add("discoveredDate");
       validFilterColumns.add("identityRef");
   }
	
	
    private DeviceInventoryDAO deviceInventoryFSPDAO = new DeviceInventoryDAO();

    //using existing dao object for creating devices .. if db found empty
    private DeviceInventoryDAO dao = new DeviceInventoryDAO();
    
    private DeploymentDAO deploymentDao = DeploymentDAO.getInstance();
    
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
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getInstance()}.
     */
    @Test
    public void testAll() {
        assertNotNull(deviceInventoryFSPDAO);
       
        //populating the device inventory and deployment
        testCreateTwoDeviceInventoryAndDeployment();

        //Sorting with ServieTag
        testGetAllDeviceInventoryWithFSPWithSortOnServiceTag0To30();
        testGetAllDeviceInventoryWithFSPWithSortOnServiceTag30To60();
        
        //Sorting with enum Device type
        testGetAllDeviceInventoryWithFSPWithSortOnEnumDeviceType0To30();
        testGetAllDeviceInventoryWithFSPWithSortOnEnumDeviceType30To60();
        
        
        //sort on ipAddress and eq filter on model
        testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndEqFilterOnModel();
        
        //sort on ipAddress
        testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom0to20();
        testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom21to40();
        testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom41to60();
        
        //sort on ipAddress and contains filter on enum DeviceType 
        testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndcontainsFilteronEnumDeviceType();  

        testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndModelContainsErvFrom0To20();

        //devicetype rackserver and model contains erv
        testGetAllDeviceInventoryWithSortOnIpContainsFilterModelErvAndDeviceTypeEqRackServer();
        
        //multiple values for eq filter on serviceTag
        testGetAllDeviceInventoryWithSortOnIpAddressAndMultimpleValuesforServiceTagWithEqFilter();
        
        //no support for multiple values in contains filter in single string filter array

        testGetAllDeviceInventoryWithCoFilterOnComplaintStateAndSortOnIpAddress();

        testGetAllDeviceInventoryWithCoFilterOnModelAndEqFilterOnHealthWithTwoValAndSortOnIpAddress();

        testGetAllDeviceInventoryWithMultipleCoFilterAndMultimpleEqFilterOnEnumTypeAndHealthWithSortOnIpAddress();
        
        testGetAllDeviceInventoryWithSortOnIpAddressAndHealthEqualsGood();
        
        
        testGetAllDeviceInventoryWithSortOnIpAddressAndHealthContainsFilter();
       
        testDeleteAllDeviceInventoryData();
    }
    
    public void testCreateTwoDeviceInventoryAndDeployment() {

    	/***************
    	 * 
    	 * Delete the complete device_inventory table
    	 * 
    	 * **/
   	
    	DeviceType[] devTypes = {DeviceType.ChassisM1000e, DeviceType.ChassisVRTX, DeviceType.RackServer, DeviceType.AggregatorIOM};
    	CompliantState[] complaintStates = {CompliantState.COMPLIANT, CompliantState.NONCOMPLIANT, CompliantState.NA};
   	
    	//keeping common
    	String[] model = {"chassis", "server","Unknown","iom"};
    	
    	//keeping common
    	String refType = "Unknown";

    	//Creating entity 1
        DeviceInventoryEntity entity = null;
        	
        String[] healths = {"good", "ok", "bad"};
        
        char[] charVals = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't' ,'u', 'v', 'w', 'x', 'y', 'z'};
        for(int i=0; i<98; i++){
  
        	entity = new DeviceInventoryEntity();
		    entity.setRefId(UUID.randomUUID().toString());
		    entity.setDeviceType(devTypes[(int) (Math.random() * 3)]);
		    entity.setIpAddress(  (int)(Math.random() * 200) + "." +  (int)(Math.random() * 200) + "."  + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200));
		    entity.setServiceTag( charVals[ (int) (Math.random()*26) ] + "" + charVals[ (int) (Math.random()*26) ] + "" + charVals[ (int) (Math.random()*26) ] + "" + charVals[ (int) (Math.random()*26) ]);
		    entity.setModel( model[(int) (Math.random() * 4)]);
		    entity.setRefType(refType);
		    entity.setDisplayName("abcde");
		    entity.setState(DeviceState.READY);
		    entity.setHealth(DeviceHealth.UNKNOWN );		    
		    
	        try {
	            dao.createDeviceInventory(entity);
	        }  catch (AsmManagerCheckedException amde) {
	            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD) {
	                fail();
	            }
	        }
	        
	        entity = null;
        }
        
        final Set<DeviceInventoryEntity> deploymentDeviceSet = new HashSet<DeviceInventoryEntity>();
        //adding pre-defined serviceTag values for test
        for(int i =0;i < 2; i++){
	        entity = new DeviceInventoryEntity();
		    entity.setRefId(UUID.randomUUID().toString());
		    entity.setDeviceType(devTypes[(int) (Math.random() * 3)]);
		    entity.setIpAddress(  (int)(Math.random() * 200) + "." +  (int)(Math.random() * 200) + "."  + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200));
		    if( i == 0)
		    {
		    	entity.setServiceTag("serviceTag12");
		    }else {
		    	entity.setServiceTag("serviceTag24");	
			}

		    entity.setModel( model[(int) (Math.random() * 4)]);
		    entity.setRefType(refType);
		    entity.setDisplayName("abcde");
		    entity.setState(DeviceState.READY);
		    entity.setHealth(DeviceHealth.UNKNOWN);
		    
	        try {
	            dao.createDeviceInventory(entity);
	            deploymentDeviceSet.add(entity);
	        }  catch (AsmManagerCheckedException amde) {
	            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD) {
	                fail();
	            }
	        }
	        
	        entity = null;
        }
        
        // create empty deployment
        final DeploymentEntity deploymentEntity = new DeploymentEntity();
        deploymentEntity.setId(UUID.randomUUID().toString());
        deploymentEntity.setName("Test Deployment");
        deploymentEntity.setDeploymentDesc("This is for IT tests");
        deploymentEntity.setMarshalledTemplateData(StringUtils.EMPTY);
        deploymentEntity.setJobId("Jon-" + UUID.randomUUID());
        deploymentEntity.setStatus(DeploymentStatusType.COMPLETE);
        try {
            deploymentDao.createDeployment(deploymentEntity);
        }  catch (AsmManagerCheckedException amde) {
            if (amde.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD) {
                fail();
            }
        }
    }
    

    public void testGetAllDeviceInventoryWithFSPWithSortOnServiceTag0To30() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	String sort = "serviceTag";
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        List<String> filterArray = null;
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 30;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);
        
		
		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("====================================================================");
        System.out.println("=====testGetAllDeviceInventoryWithFSPWithSortOnServiceTag0To30=====");
        System.out.println("====================================================================");
        
        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + "\t ServiceTag : "+ deviceInventoryEntity.getServiceTag());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnServiceTag30To60() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	String sort = "serviceTag";
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        List<String> filterArray = null;
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 30;
        Integer pageOffSet = 30;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);
        
		
		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("====================================================================");
        System.out.println("=====testGetAllDeviceInventoryWithFSPWithSortOnServiceTag30To60=====");
        System.out.println("====================================================================");
        
        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + "\t ServiceTag  : "+ deviceInventoryEntity.getServiceTag());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnEnumDeviceType0To30() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "deviceType";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        List<String> filterArray = null;
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 30;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("====================================================================");
        System.out.println("==testGetAllDeviceInventoryWithFSPWithSortOnEnumDeviceType0To30=====");
        System.out.println("====================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnEnumDeviceType30To60() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "deviceType";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        List<String> filterArray = null;
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 30;
        Integer pageOffSet = 30;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("\n\n============================================================================");
        System.out.println("========testGetAllDeviceInventoryWithFSPWithSortOnEnumDeviceType30To60=========");
        System.out.println("===============================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndEqFilterOnModel() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        String[] filter = {"eq,model,chassis"};
        
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("===================================================================================");
        System.out.println("=======testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndEqFilterOnModel=======");
        System.out.println("===================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + " Model Type : "+ deviceInventoryEntity.getModel());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom0to20() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        List<String> filterArray = null;
        		//Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("============================================================================================");
        System.out.println("============testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom0to20====================");
        System.out.println("============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType());
		}
        
        System.out.println("=================================================================");
        System.out.println("=================================================================");
        System.out.println("=================================================================");
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom21to40() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        List<String> filterArray = null;
        		//Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 20;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("============================================================================================");
        System.out.println("==============testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom21to40=================");
        System.out.println("============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType());
		}
        
        System.out.println("=================================================================");
        System.out.println("=================================================================");
        System.out.println("=================================================================");
        
        assertTrue(entities.size() > 0);
    }
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom41to60() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        List<String> filterArray = null;
        		//Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 40;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("============================================================================================");
        System.out.println("============testGetAllDeviceInventoryWithFSPWithSortOnIpAddressFrom41to60===================");
        System.out.println("============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType());
		}
        
        System.out.println("=================================================================");
        System.out.println("=================================================================");
        System.out.println("=================================================================");
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndcontainsFilteronEnumDeviceType() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        String[] filter = {"co,deviceType,assis"};
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("============================================================================================");
        System.out.println("====testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndcontainsFilteronEnumDeviceType====");
        System.out.println("============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + " Device Type : "+ deviceInventoryEntity.getDeviceType());
		}
        
        System.out.println("=================================================================");
        System.out.println("=================================================================");
        System.out.println("=================================================================");
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndModelContainsErvFrom0To20() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        String[] filter = {"co,model,erv"};
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("=============================================================================================");
        System.out.println("=======testGetAllDeviceInventoryWithFSPWithSortOnIpAddressAndModelContainsErvFrom0To20=======");
        System.out.println("=============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + 
					"\t Model Type : "+ deviceInventoryEntity.getModel());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithSortOnIpContainsFilterModelErvAndDeviceTypeEqRackServer() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        String[] filter = {"eq,deviceType,RackServer","co,model,erv"};
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("=============================================================================================");
        System.out.println("====testGetAllDeviceInventoryWithSortOnIpContainsFilterModelErvAndDeviceTypeEqRackServer=====");
        System.out.println("=============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + 
					"\t Model Type : "+ deviceInventoryEntity.getModel());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithSortOnIpAddressAndMultimpleValuesforServiceTagWithEqFilter() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        //String[] filter = {"eq,serviceTag,serviceTag1775,serviceTag1650","eq,health,good"};
        String[] filter = {"eq,serviceTag,serviceTag12,serviceTag24"};
        
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);
		
		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("================================================================================================");
        System.out.println("===testGetAllDeviceInventoryWithSortOnIpAddressAndMultimpleValuesforServiceTagWithEqFilter======");
        System.out.println("================================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + 
					"\t Model Type : "+ deviceInventoryEntity.getModel() + "\t Helath : " + deviceInventoryEntity.getHealth());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithCoFilterOnComplaintStateAndSortOnIpAddress() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();
        String[] filter = {"co,compliant,MPLIA"};
        
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 50;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);
		
		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("=============================================================================================");
        System.out.println("=========testGetAllDeviceInventoryWithCoFilterOnComplaintStateAndSortOnIpAddress=============");
        System.out.println("=============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + 
					"\t Model Type : "+ deviceInventoryEntity.getModel() + "\t Complaint : ");
		}
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithCoFilterOnModelAndEqFilterOnHealthWithTwoValAndSortOnIpAddress() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        //String[] filter = {"co,model,assi","eq,health,good,ok"};
        String[] filter = {"co,model,assi","eq,health,good,ok"};
        
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("==================================================================================================");
        System.out.println("===testGetAllDeviceInventoryWithCoFilterOnModelAndEqFilterOnHealthWithTwoValAndSortOnIpAddress====");
        System.out.println("==================================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + 
					"\t Model Type : "+ deviceInventoryEntity.getModel() + "\t Helath : " + deviceInventoryEntity.getHealth());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithMultipleCoFilterAndMultimpleEqFilterOnEnumTypeAndHealthWithSortOnIpAddress() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        String[] filter = {"co,model,assi","eq,health,good,ok","eq,deviceType,RackServer,ChassisVRTX"};
  
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;

        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("===============================================================================================================");
        System.out.println("==testGetAllDeviceInventoryWithMultipleCoFilterAndMultimpleEqFilterOnEnumTypeAndHealthWithSortOnIpAddress======");
        System.out.println("================================================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + 
					"\t Model Type : "+ deviceInventoryEntity.getModel() + "\t Helath : " + deviceInventoryEntity.getHealth());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithSortOnIpAddressAndHealthEqualsGood() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        String[] filter = {"eq,health,good"};
  
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;

        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("=============================================================================================");
        System.out.println("=============testGetAllDeviceInventoryWithSortOnIpAddressAndHealthEqualsGood=================");
        System.out.println("=============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + 
					"\t Model Type : "+ deviceInventoryEntity.getModel() + "\t Helath : " + deviceInventoryEntity.getHealth());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     */
    public void testGetAllDeviceInventoryWithSortOnIpAddressAndHealthContainsFilter() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//display by Desc order
    	String sort = "ipAddress";
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	
    	//No sort information
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        String[] filter = {"co,health,ad"};
  
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;

        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/**
		 * If filter is null, all the device inventory are returned
		 * 
		 * */
        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("=============================================================================================");
        System.out.println("============testGetAllDeviceInventoryWithSortOnIpAddressAndHealthContainsFilter==============");
        System.out.println("=============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
			System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "+ deviceInventoryEntity.getDeviceType() + 
					"\t Model Type : "+ deviceInventoryEntity.getModel() + "\t Helath : " + deviceInventoryEntity.getHealth());
		}
        
        assertTrue(entities.size() > 0);
    }
    
    public void testGetAllDeviceInventoryWithServiceFilter() {
        
        PaginationParamParser paginationParamParser = 
                new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
        
        // no sort
        List<SortParamParser.SortInfo> sortInfos = ListUtils.EMPTY_LIST;
        
        String[] filter = {"server"};
        List<String> filterArray = Arrays.asList(filter);
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 20;
        Integer pageOffSet = 0;

        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);

        PaginationParamParser.PaginationInfo paginationInfo = 
                paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);
    
        //Validation criteria
        /**
         * If filter is null, all the device inventory are returned
         * 
         * */
        List<DeviceInventoryEntity> entities = 
                deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);

        System.out.println("=============================================================================================");
        System.out.println("========================testGetAllDeviceInventoryWithServiceFilter===========================");
        System.out.println("=============================================================================================");

        for (DeviceInventoryEntity deviceInventoryEntity : entities) {
            System.out.println(" Device Ip : " + deviceInventoryEntity.getIpAddress() + "\t Device Type : "
                    + deviceInventoryEntity.getDeviceType() + "\t Model Type : " + deviceInventoryEntity.getModel()
                    + "\t Helath : " + deviceInventoryEntity.getHealth());
        }
        
        assertTrue(entities.size() > 0);
    }    
    
  
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DeviceInventoryFSPDAO#getAllDeviceInventory()}.
     * @throws AsmManagerCheckedException 
     */
    public void testDeleteAllDeviceInventoryData() {
    	
    	List<String> refIdStrings = new ArrayList<>();
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);

        SortParamParser sp = new SortParamParser(null, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        List<String> filterArray = null;
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        Integer pageLimit = 50;
        Integer pageOffSet = 0;
        
        int countValue = deviceInventoryFSPDAO.getTotalRecords(filterInfos);
        
		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

        List<DeviceInventoryEntity> entities = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);
        
        paginationInfo = paginationParamParser.new PaginationInfo(50, 50, countValue);
        
        List<DeviceInventoryEntity> entities1 = deviceInventoryFSPDAO.getAllDeviceInventory(sortInfos, filterInfos, paginationInfo);
        
        for(DeviceInventoryEntity entity : entities)
        	refIdStrings.add(entity.getRefId());
        
        for(DeviceInventoryEntity entity : entities1)
        	refIdStrings.add(entity.getRefId());
        
        for(String refIdString : refIdStrings)
        	dao.deleteDeviceInventory(refIdString);
        
        for (final DeploymentEntity deploymentEntity :  deploymentDao.getAllDeployment(DeploymentDAO.ALL_ENTITIES)) {
            deploymentDao.deleteDeployment(deploymentEntity.getId());
        }
    }
  
}
