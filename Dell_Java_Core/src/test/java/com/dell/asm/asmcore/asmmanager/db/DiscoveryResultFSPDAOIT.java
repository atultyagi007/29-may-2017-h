/**
 * 
 */
package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerDAOException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.SortParamParser;

/**
 * @author Yao_Lu1
 * 
 */
public class DiscoveryResultFSPDAOIT {

	    private DiscoveryResultDAO dao = DiscoveryResultDAO.getInstance();
	    
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
		   validSortColumns.add("parentJobId");
		   validSortColumns.add("model");
		   validSortColumns.add("serviceTag");
		   validSortColumns.add("refId");
		   validSortColumns.add("refType");
		   validSortColumns.add("deviceType");
		   validSortColumns.add("ipAddress");
		   validSortColumns.add("serverCount");
		   validSortColumns.add("iomCount");
		   validSortColumns.add("status");
		   validSortColumns.add("statusMessage");
		   validSortColumns.add("healthState");
		   validSortColumns.add("healthStatusMsg");
		   validSortColumns.add("createdDate");
		   validSortColumns.add("createdBy");
		   validSortColumns.add("updatedDate");
		   validSortColumns.add("updatedBy");
	   }

	   private static final Set<String> validFilterColumns = new HashSet<>();

	   static {
		   //do not remove parent job id from filter set
		   validFilterColumns.add("parentJobId");
		   validFilterColumns.add("model");
		   validFilterColumns.add("serviceTag");
		   validFilterColumns.add("refId");
		   validFilterColumns.add("refType");
		   validFilterColumns.add("deviceType");
		   validFilterColumns.add("ipAddress");
		   validFilterColumns.add("serverCount");
		   validFilterColumns.add("iomCount");
		   validFilterColumns.add("status");
		   validFilterColumns.add("statusMessage");
		   validFilterColumns.add("healthState");
		   validFilterColumns.add("healthStatusMsg");
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
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#getInstance()}.
     */
    @Test
    public void testGetInstance() {
        assertNotNull(dao);
    }


    /**
     * Test method for
     * {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#createDiscoveryResult(com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity)}
     * .
     */
    @Test
    public void testCreateDiscoveryResult() {
        
    	String[] parentJobIds = {"Job-6f951902-1648-4c4f-a260-8c6a739869ae", "Job-7T551902-1648-4c4f-a260-8c6a739869ae","Job-6f951902-1648-4c4f-a260-8c6a739869ae"
    			,"Job-6f951902-1648-4c4f-a260-8c6a739869ae","Job-6f951902-1648-4c4f-a260-8c6a739869ae","Job-6f951902-1648-4c4f-a260-8c6a739869ae","Job-6f951902-1648-4c4f-a260-8c6a739869ae",
    			"Job-32SD4343-1648-4c4f-a260-8c6a739869ae","Job-R6T51902-1648-4c4f-a260-8c6a739869ae","Job-W3433TR2-1648-4c4f-a260-8c6a739869ae","Job-32SD4343-1648-4c4f-a260-8c6a739869ae",
    			"Job-32SD4343-1648-4c4f-a260-8c6a739869ae","Job-32SD4343-1648-4c4f-a260-8c6a739869ae", "11"};
    	
    	String[] jobIds = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
    	
    	
    	String[] refIdsStrings = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
    	
    	
    	DeviceType[] devTypes = {DeviceType.ChassisM1000e, DeviceType.RackServer, DeviceType.RackServer, DeviceType.AggregatorIOM, DeviceType.unknown, DeviceType.ChassisM1000e, DeviceType.AggregatorIOM, DeviceType.ChassisM1000e, 
    			DeviceType.RackServer, DeviceType.AggregatorIOM, DeviceType.ChassisM1000e, DeviceType.AggregatorIOM, DeviceType.ChassisM1000e, DeviceType.ChassisM1000e};
    	
    	String[] serviceTags = {"ServiceTag1", "ServiceTag12", "ServiceTag22", "ServiceTag4", "ServiceTag3", "ServiceTag13", "ServiceTag16", "ServiceTag19", "ServiceTag15", 
    			"ServiceTag10", "ServiceTag11", "ServiceTag71", "ServiceTag18", "ServiceTag25"};
    	
    	String[] ipAddresses = {"10.128.129.123", "9.128.129.123", "10.128.129.122", "10.118.129.123", "1.128.129.123", "1.128.100.123", "10.128.129.119", "10.101.129.123", "4.128.129.123", 
    			"3.128.129.123", "10.128.000.123", "10.000.000.123", "10.128.129.190", "10.128.129.134"};
    	
    	//keeping common
    	String model = "Unknown";
    	
    	//keeping common
    	String refType = "Unknown";
    	
    	DiscoveryStatus[] discoveryStatuses = {DiscoveryStatus.CONNECTED, DiscoveryStatus.CONNECTED, DiscoveryStatus.CONNECTED, DiscoveryStatus.UNSUPPORTED, DiscoveryStatus.FAILED, DiscoveryStatus.INPROGRESS,
    			DiscoveryStatus.UNSUPPORTED, DiscoveryStatus.SUCCESS, DiscoveryStatus.PENDING, DiscoveryStatus.PENDING, DiscoveryStatus.CONNECTED, DiscoveryStatus.FAILED, DiscoveryStatus.SUCCESS, DiscoveryStatus.SUCCESS};

        String healthState = "Ok";

        String healthStatusMsg = "Ok";

    	DiscoveryResultEntity entity = null;

        for(int i=0; i<refIdsStrings.length; i++){
        	entity = new DiscoveryResultEntity();
        	
        	entity.setParentJobId(parentJobIds[i]);
        	entity.setJobId(jobIds[i]);
        	
		    entity.setRefId(refIdsStrings[i]);
		    entity.setDeviceType(devTypes[i]);
		    entity.setServiceTag(serviceTags[i]);
		    entity.setIpaddress(ipAddresses[i]);
		    entity.setModel(model);
		    entity.setRefType(refType);
		    entity.setStatusMessage("Discovery successul");
		    entity.setStatus(discoveryStatuses[i]);
		    entity.setHealthState(healthState);
		    entity.setHealthStatusMsg(healthStatusMsg);
		    
		    try {
				dao.createDiscoveryResult(entity);
		    } catch (AsmManagerCheckedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (AsmManagerDAOException amde) {
		        if (amde.getReasonCode() != AsmManagerDAOException.REASON_CODE.DUPLICATE_REFID) {
		            //fail();
		        }
		    }
        }

	    }


    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#getDiscoveryResult()}.
     */
    @Test
    public void testGetDiscoveryByJobWithNoFilterandNoSortCriteria() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//Provide test parentJobId here
    	String parentJobId = "11";
    	
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	// testing with decending order
    	String sort = "-refId";
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        // Parse the filter parameter.
        // Any filter exceptions are already encased in a WebApplicationException with an Status code=400
        // logic to get the parentJobId related discovery results, adding the below to filter criteria
        String parentJobIdTofilter = "eq,parentJobId," + parentJobId;
        
        List<String> filterArray = new ArrayList<String>();
        filterArray.add(parentJobIdTofilter);		
        		
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        //considered limit as max as 50 and offset as 0 for all results
        Integer pageLimit = 50;
        Integer pageOffSet = 0;
        
        int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/************************************************************************************
		 * If filter is null, all the discovery results for the parent jobId are returned
		 ************************************************************************************/

        List<DiscoveryResultEntity> result = dao.getDiscoveryResult(parentJobId, sortInfos, filterInfos, paginationInfo);
        
        assertTrue(result.size() > 0);
    }
    
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#getDiscoveryResult()}.
     */
    @Test
    public void testGetDiscoveryByJobWithSortOnIpAddress() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//Provide test parentJobId here
    	String parentJobId = "Job-6f951902-1648-4c4f-a260-8c6a739869ae";
    	
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	// testing with decending order
    	String sort = "ipAddress";
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        // Parse the filter parameter.
        // Any filter exceptions are already encased in a WebApplicationException with an Status code=400
        // logic to get the parentJobId related discovery results, adding the below to filter criteria
        String parentJobIdTofilter = "eq,parentJobId," + parentJobId;
        
        List<String> filterArray = new ArrayList<String>();
        filterArray.add(parentJobIdTofilter);		
        		
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        //considered limit as max as 50 and offset as 0 for all results
        Integer pageLimit = 50;
        Integer pageOffSet = 0;
        
        int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

		//Validation criteria
		//Having here set 'sort' String as null or 'filterArray' List<String> as null
		/************************************************************************************
		 * If filter is null, all the discovery results for the parent jobId are returned
		 ************************************************************************************/

        List<DiscoveryResultEntity> result = dao.getDiscoveryResult(parentJobId, sortInfos, filterInfos, paginationInfo);
        
        System.out.println("===================================");
        System.out.println("======== Results Displayed ========");
        System.out.println("===================================");
        for(DiscoveryResultEntity e : result)
        	System.out.println(" Device Ip : " + e.getIpaddress());
        
        
        assertTrue(result.size() > 0);
    }
    
    /**
     * Test method for {@link com.dell.asm.asmcore.asmmanager.db.DiscoveryResultDAO#getDiscoveryResult()}.
     */
    @Test
    public void testGetDiscoveryByJobWithMultipleFilterandNoSortCriteria() {
    	
    	PaginationParamParser paginationParamParser = new PaginationParamParser(servletRequest,servletResponse,httpHeaders,uriInfo);
    	
    	//Provide test parentJobId here
    	String parentJobId = "11";
    	
        // Parse the sort parameter.
        // Any sort exceptions are already encased in a WebApplicationException with an Status code=400
    	// testing with ascending order
    	String sort = "refId";
        SortParamParser sp = new SortParamParser(sort, validSortColumns);
        List<SortParamParser.SortInfo> sortInfos = sp.parse();

        // Parse the filter parameter.
        // Any filter exceptions are already encased in a WebApplicationException with an Status code=400
        String[] filterString = {"eq,status,CONNECTED"};
        List<String> filterArray = Arrays.asList(filterString);
    
        // logic to get the parentJobId related discovery results, adding the below to filter criteria
        String parentJobIdTofilter = "eq,parentJobId," + parentJobId;
        filterArray.add(parentJobIdTofilter);		
        		
        FilterParamParser filterParser = new FilterParamParser(filterArray, validFilterColumns);
        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();

        //considered limit as max as 50 and offset as 0 for all results
        Integer pageLimit = 50;
        Integer pageOffSet = 0;
        
        int countValue = dao.getTotalRecords(filterInfos);

		PaginationParamParser.PaginationInfo paginationInfo = paginationParamParser.new PaginationInfo(pageOffSet, pageLimit, countValue);

        List<DiscoveryResultEntity> result = dao.getDiscoveryResult(parentJobId, sortInfos, filterInfos, paginationInfo);

        assertTrue(result.size() > 0);
    }
}
