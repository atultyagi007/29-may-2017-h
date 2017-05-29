/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.PolicyRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerDAOException;
import com.dell.pg.jraf.api.profile.data.ITemplateData;
import com.dell.pg.jraf.api.ref.IPolicyRef;

public class PolicyRefDAO {

	 // Logger.
    private static final Logger logger = Logger.getLogger(PolicyRefDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static PolicyRefDAO instance;

    private PolicyRefDAO() {
    }

    public static synchronized PolicyRefDAO getInstance() {
        if (instance == null)
            instance = new PolicyRefDAO();
        return instance;
    }

   
    public PolicyRefEntity createPolicyRef(PolicyRefEntity policyRef) throws AsmManagerDAOException {

    	Session session = null;
        Transaction tx = null;
        // Save the template in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction(); 
            session.save(policyRef);
            // Commit transaction and clean up.
            tx.commit();
        } catch (Exception e) {
        	logger.warn("Caught exception during template creation: " + e);
        	try {if (tx != null) tx.rollback();} catch (Exception e2){
        		logger.warn("Caught exception during template creation" +e2);
        	}
        } finally {
			try {
				if (session != null)
					session.close();
			} catch (Exception e2) {
				logger.warn("Caught exception during template creation" +e2);
			}
		}

        return policyRef;
    }
    
   
//    public TemplateEntity getTemplateByName(String templateName)
//    		throws AsmManagerDAOException {
//
//        Session session = null;
//        Transaction tx = null;
//        TemplateEntity templateEntity = null;
//
//        try {
//            session = _dao._database.getNewSession();
//            tx = session.beginTransaction();
//
//            // Create and execute command.
//            String hql = "from TemplateEntity where templateName =:templateName";
//            Query query = session.createQuery(hql);
//            query.setString("templateName", templateName);
//            templateEntity = (TemplateEntity) query.setMaxResults(1).uniqueResult();
//
//            // Commit transaction.
//            tx.commit();
//        } catch (Exception e) {
//            logger.warn("Caught exception during get template for template name: " + templateName + ", "  + e);
//            try {
//        	if (tx != null) {
//        	    tx.rollback();
//                }
//            } catch (Exception ex) {
//                logger.warn("Unable to rollback transaction during get template: " + ex);
//            }
//            throw new AsmManagerDAOException("Caught exception during get template for template name: "
//        	+ templateName + ", "  + e, e);
//        } finally {
//            try {
//        	if (session != null) {
//                    session.close();
//                }
//            } catch (Exception ex) {
//                logger.warn("Unable to close session during get device: " + ex);
//            }
//        }
//
//        return templateEntity;
//    }
//    
//    
//    public List<TemplateEntity> getAllTemplates() throws AsmManagerDAOException {
//
//        Session session = null;
//        Transaction tx = null;
//        List<TemplateEntity> entityList = new ArrayList<TemplateEntity>();
//
//        try {
//            session = _dao._database.getNewSession();
//            tx = session.beginTransaction();
//
//            // Create and execute command.
//            String hql = "from TemplateEntity";
//            Query query = session.createQuery(hql);
//            for (Object result : query.list()) {
//            	entityList.add((TemplateEntity) result);
//            }
//
//            // Commit transaction.
//            tx.commit();
//        } catch (Exception e) {
//            logger.warn("Caught exception during get all templates: " + e);
//            try {
//                if (tx != null) {
//        	    tx.rollback();
//        	}
//            } catch (Exception ex) {
//        	logger.warn("Unable to rollback transaction during get all templates: " + ex);
//            }
//            throw new AsmManagerDAOException("Caught exception during get all templates: ", e);
//        } finally {
//            try {
//        	if (session != null) {
//        	    session.close();
//        	}
//            } catch (Exception ex) {
//        	logger.warn("Unable to close session during get all devices: " + ex);
//            }
//        }
//
//        return entityList;
//    }
//    
//       public void updateTemplate(TemplateEntity newTemplate) throws AsmManagerDAOException {
//
//        
//    }
//    
//    public void deleteTemplate(String templateName) throws AsmManagerDAOException {
//
//    	logger.info("Deleting template : " + templateName);
//        Session session = null;
//        Transaction tx = null;
//
//        try {
//            session = _dao._database.getNewSession();
//            tx = session.beginTransaction();
//
//            String hql = "delete TemplateEntity where templateName = :templateName";
//            Query query = session.createQuery(hql);
//            query.setString("templateName", templateName);
//            query.executeUpdate();
//
//            // Commit transaction.
//            tx.commit();
//        } catch (Exception e) {
//            logger.warn("Caught exception during delete template: " + e);
//            try {
//                if (tx != null) {
//        	    tx.rollback();
//        	}
//            } catch (Exception ex) {
//        	logger.warn("Unable to rollback transaction during delete template: " + ex);
//            }
//            throw new AsmManagerDAOException("Caught exception during delete template: ", e);
//        } finally {
//            try {
//                if (session != null) {
//        	    session.close();
//        	}
//            } catch (Exception ex) {
//                logger.warn("Unable to close session during delete template: " + ex);
//            }
//        }
//    }
}
