package com.dell.asm.asmcore.asmmanager.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dell.asm.asmcore.asmmanager.db.entity.PolicyRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.rest.common.util.FilterParamParser;


public class TemplateEntityDAOIT {
	private TemplateDAO dao = TemplateDAO.getInstance();
	private PolicyRefDAO policyDao = PolicyRefDAO.getInstance();

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

	
	@Test
	public void testGetInstance() {
		assertNotNull(dao);
	}
	 
	@Test
	public void testCreateTemplate() {	
		
		TemplateEntity template = new TemplateEntity();
		template.setTemplateId("3");
		template.setName("templateName");
		template.setTemplateType("TemplateType");
//		template.setDisplayName("templateName");
//		template.setDeviceType("chassis");
		GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

		template.setCreatedDate(now);
		template.setUpdatedDate(now);
		try{
		dao.createTemplate(template);	 
		} catch(AsmManagerCheckedException e){
			if(e.getReasonCode() != AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD){
				fail();
			}
		}
		PolicyRefEntity policyRef = new PolicyRefEntity();
		policyRef.setDeviceType("chassis");
		policyRef.setName("templateName");
		policyRef.setDisplayName("templateName");
		policyRef.setPolicyRefId("1");
		policyRef.setRefId("123");
		policyRef.setRefType("chassis");
		policyDao.createPolicyRef(policyRef);
		template.getPolicyRefEntities().add(policyRef);
		// dao.createTemplate(template);
		assertNotNull(template);
	}
	
	@Test
	public void testUpdateTemplate() {			
		TemplateEntity template = dao.getTemplateByName("templateName");
		if(template == null){
			fail();
		}
		// template.setDisplayName("testing");
			TemplateEntity newTemplate = null;
			try{
				newTemplate = dao.updateTemplate(template);
			} catch (Exception e){
				e.printStackTrace();
				fail();
			}
			assertNotNull(newTemplate);
	}
		
	@Test
	public void testGetTemplateByName() {		
	TemplateEntity template = dao.getTemplateByName("templateName");
	assertNotNull(template);	
		
	}	
	
	@Test
	public void testGetTemplateById() {		
	TemplateEntity template = dao.getTemplateByName("templateName");
	assertNotNull(template);	
	String id = template.getTemplateId();
	TemplateEntity newTemplate = dao.getTemplateById(id);
	assertNotNull(newTemplate);
		
	}	
		
	@Test
	public void testGetAllTemplate() {
		List<TemplateEntity> templates = null;
		    Set<String> validFilterColumns = new HashSet<>();
		        // column names should be lowercased since that is how the FilterParamParser
		        // compares them.
		        validFilterColumns.add("name");
		        validFilterColumns.add("displayName");
		        validFilterColumns.add("templatetype");
		        validFilterColumns.add("devicetype");
		        validFilterColumns.add("state");
		        validFilterColumns.add("createddate");
		        validFilterColumns.add("createdby");
		        validFilterColumns.add("updateddate");
		        validFilterColumns.add("updatedby");
		   // Build filter list from filter params ( comprehensive )
	        FilterParamParser filterParser = new FilterParamParser(null, validFilterColumns);
	        List<FilterParamParser.FilterInfo> filterInfos = filterParser.parse();
	        
		try {
			templates = dao.getAllTemplates();
			System.out.println("Size "+ templates.size());
			System.out.println("Size "+ dao.getTotalRecords(filterInfos));
			//assertNotNull(templates);
			
		} catch (Exception e){
			e.printStackTrace();
		}
				
	}	
		
	@Test
	public void testDeleteTemplate() {
	 dao.deleteTemplate("templateName");		
}	
	
}