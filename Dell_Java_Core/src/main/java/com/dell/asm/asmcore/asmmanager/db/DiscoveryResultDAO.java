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
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerDAOException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser.PaginationInfo;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.asm.server.client.device.Server;


public final class DiscoveryResultDAO {
    // Logger.
    private static final Logger logger = Logger.getLogger(DiscoveryResultDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static DiscoveryResultDAO instance;

    private DiscoveryResultDAO() {
    }

    public static synchronized DiscoveryResultDAO getInstance() {
        if (instance == null)
            instance = new DiscoveryResultDAO();
        return instance;
    }

    /**
     * Create Discovery Result.
     * @param result
     * @return the entity
     */
    public DiscoveryResultEntity createDiscoveryResult(DiscoveryResultEntity result)
    		throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;

        // Save the device in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            session.save(result);
            result.setCreatedDate(new Date());
            result.setCreatedBy(_dao.extractUserFromRequest());
            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during discovery result creation: " + cve);
            try {
                if (tx != null) {
        	    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create discovery result: " + ex);
            }
            if (cve.getConstraintName().contains("refid")) {
            	//throw new AsmManagerDAOException(AsmManagerDAOException.REASON_CODE.DUPLICATE_JOBID, cve);
            	throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_JOBID,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            } else {
                //throw new AsmManagerDAOException(AsmManagerDAOException.REASON_CODE.DUPLICATE_RECORD, cve);
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during discovery result creation: " + e);
            try {
                if (tx != null) {
        	    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create discovery result: " + ex);
            }
            //throw new AsmManagerDAOException("Caught exception during discovery result creation: ", e);
            throw new AsmManagerInternalErrorException("Error Creating Discovery result", "DiscoveryResultDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create device: " + ex);
            }
        }

        return result;
    }
    
    /**
     * Retrieve DiscoveryResult based on parent job id.
     * @return the list of discovery results for given parent job id.
     */
    @SuppressWarnings("unchecked")
    public List<DiscoveryResultEntity> getDiscoveryResult(String parentJobId, List<SortParamParser.SortInfo> sortInfos, List<FilterParamParser.FilterInfo> filterInfos,
    		PaginationInfo paginationInfo) throws AsmManagerDAOException {

        Session session = null;
        Transaction tx = null;
        List<DiscoveryResultEntity> entityList = new ArrayList<DiscoveryResultEntity>();

        logger.debug("getDiscoveryResult for job: " + parentJobId );
        
        try {

	       	int offset = paginationInfo.getOffset();
	    	int limit = paginationInfo.getLimit();
	        int pageSize = limit;
	         
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DiscoveryResultEntity.class);
            BaseDAO.addSortCriteria(criteria, sortInfos);
            criteria.add(Restrictions.ne("serverType", Server.ServerType.BLADE.value())); // never return blades
            criteria.add(Restrictions.ne("serverType", Server.ServerType.COMPELLENT.value())); // never return compellents which now show up as iDrac7
            List<FilterParamParser.FilterInfo> notFound = BaseDAO.addFilterCriteria(criteria, filterInfos, DiscoveryResultEntity.class);
            
            if(notFound != null && notFound.size() > 0) {
                criteria.createAlias("discoveryResultEntity", "discoveryResultEntityAlias");
                for(FilterParamParser.FilterInfo filterInfo : notFound) {
                    criteria.add(Restrictions.eq("discoveryResultEntityAlias.deviceKey", filterInfo.getColumnName()));
                    if(filterInfo.getColumnValue().size() == 1) {
                        criteria.add(Restrictions.eq("discoveryResultEntityAlias.deviceValue", filterInfo.getColumnValue().get(0)));
                    } else if(filterInfo.getColumnValue().size() > 1) {
                        criteria.add(Restrictions.in("discoveryResultEntityAlias.deviceValue", filterInfo.getColumnValue()));
                    }
                }
            }

            
            //criteria.setFirstResult((pageNumber - 1) * pageSize);
            criteria.setFirstResult(offset);
            criteria.setMaxResults(pageSize);

            entityList = criteria.list();

            // Commit transaction.
            tx.commit();
            logger.debug("getDiscoveryResult for job: " + entityList.size() );
        } catch (Exception e) {
            logger.warn("Caught exception during get discovery result for job: " + parentJobId + ", "  + e);
            try {
        	if (tx != null) {
        	    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get device: " + ex);
            }
            //throw new AsmManagerDAOException("Caught exception during get discovery result for jobId: " 	+ parentJobId + ", "  + e, e);
            throw new AsmManagerInternalErrorException("Error Getting Discovery result", "DiscoveryResultDAO", e);
        } finally {
            try {
        	if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get device: " + ex);
            }
        }

        return entityList;
    }
    
    /**
     * Retrieve all from Discovery Result.
     * @return list of entities
     */
    public List<DiscoveryResultEntity> getAllDiscoveryResult() {
        Session session = null;
        Transaction tx = null;
        List<DiscoveryResultEntity> entityList = new ArrayList<DiscoveryResultEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DiscoveryResultEntity";
            Query query = session.createQuery(hql);
            for (Object result : query.list()) {
            	entityList.add((DiscoveryResultEntity) result);
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all discovery results: " + e);
            try {
                if (tx != null) {
        	    tx.rollback();
        	}
            } catch (Exception ex) {
            	logger.warn("Unable to rollback transaction during get all discovery results: " + ex);
            }
            throw new AsmManagerInternalErrorException("Error Getting Discovery result", "DiscoveryResultDAO", e);
        } finally {
            try {
            	if (session != null) {
            		session.close();
            	}
            } catch (Exception ex) {
            	logger.warn("Unable to close session during get all discovery results: " + ex);
            }
        }

        return entityList;
    }
    
    /*************************************************************************************
     ***************/
     /**
      * Retrieve all from DiscoveryResultEntity.
      * @return list of entities
      */
     @SuppressWarnings("unchecked")
     public List<DiscoveryResultEntity> getAllDiscoveryResult(List<SortParamParser.SortInfo> sortInfos, List<FilterParamParser.FilterInfo> filterInfos,PaginationInfo paginationInfo) 
                throws AsmManagerDAOException {

         Session session = null;
         Transaction tx = null;
         List<DiscoveryResultEntity> entityList = new ArrayList<DiscoveryResultEntity>();

         try {
                
                 int offset = paginationInfo.getOffset();
                 int limit = paginationInfo.getLimit();
                 int pageSize = limit;
                 
             session = _dao._database.getNewSession();
             tx = session.beginTransaction();

             Criteria criteria = session.createCriteria(DiscoveryResultEntity.class);
             BaseDAO.addSortCriteria(criteria, sortInfos);
             
             List<FilterParamParser.FilterInfo> notFound = BaseDAO.addFilterCriteria(criteria, filterInfos, DiscoveryResultEntity.class);

             if(notFound != null && notFound.size() > 0) {
                 criteria.createAlias("discoveryResultEntity", "discoveryResultEntityAlias");
                 for(FilterParamParser.FilterInfo filterInfo : notFound) {
                     criteria.add(Restrictions.eq("discoveryResultEntity.deviceKey", filterInfo.getColumnName()));
                     if(filterInfo.getColumnValue().size() == 1) {
                         criteria.add(Restrictions.eq("discoveryResultEntityAlias.deviceValue", filterInfo.getColumnValue().get(0)));
                     } else if(filterInfo.getColumnValue().size() > 1) {
                         criteria.add(Restrictions.in("discoveryResultEntityAlias.deviceValue", filterInfo.getColumnValue()));
                     }
                 }
             }
             
             //criteria.setFirstResult((pageNumber - 1) * pageSize);
             criteria.setFirstResult(offset);
             criteria.setMaxResults(pageSize);

             entityList = criteria.list();
             tx.commit();
         } catch (Exception e) {
             logger.warn("Caught exception during get all discovery entities: " + e);
             try {
                 if (tx != null) {
                    tx.rollback();
                }
             } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all discovery entities: " + ex);
             }
             throw new AsmManagerDAOException("Caught exception during get all discover entities: ", e);
         } finally {
             try {
                if (session != null) {
                    session.close();
                }
             } catch (Exception ex) {
                logger.warn("Unable to close session during get all discover entities: " + ex);
             }
         }

         return entityList;
     }
     
    /**
     * create or Update Discovery Result.
     * @param newResult the result to update.
     */
   
    public void createOrUpdateDiscoveryResult(DiscoveryResultEntity newResult) throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;
        DiscoveryResultEntity discoveryResultEntity = new DiscoveryResultEntity();
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from DiscoveryResultEntity where refId = :refId";
            Query query = session.createQuery(hql);
            query.setString("refId", newResult.getRefId());
            discoveryResultEntity = (DiscoveryResultEntity) query.setMaxResults(1).uniqueResult();
                        
            if (discoveryResultEntity == null) {
                discoveryResultEntity = new DiscoveryResultEntity();
                discoveryResultEntity.setRefId( newResult.getRefId());
                discoveryResultEntity.setRefType(newResult.getRefType());
                discoveryResultEntity.setParentJobId(newResult.getParentJobId()) ;
                discoveryResultEntity.setJobId(newResult.getJobId());
            }
            discoveryResultEntity.setDeviceRefId(newResult.getDeviceRefId());
            discoveryResultEntity.setDeviceType(newResult.getDeviceType());
            discoveryResultEntity.setStatus(newResult.getStatus());
            discoveryResultEntity.setStatusMessage(newResult.getStatusMessage());
            discoveryResultEntity.setServiceTag(newResult.getServiceTag());
            discoveryResultEntity.setIpaddress(newResult.getIpaddress());
            discoveryResultEntity.setModel(newResult.getModel());
            //logger.info("serer count and iom count: " + newResult.getServerCount() + " : "+newResult.getIomCount());
            discoveryResultEntity.setServerCount(newResult.getServerCount());
            discoveryResultEntity.setIomCount(newResult.getIomCount());
            discoveryResultEntity.setServerType( newResult.getServerType());
            discoveryResultEntity.setHealthState(newResult.getHealthState());
            discoveryResultEntity.setHealthStatusMsg(newResult.getHealthStatusMsg());
            discoveryResultEntity.setVendor(  newResult.getVendor( ));
            discoveryResultEntity.setSystem_id(newResult.getSystem_id());

            discoveryResultEntity.setUpdatedDate(new Date());
            discoveryResultEntity.setUpdatedBy(_dao.extractUserFromRequest());
            discoveryResultEntity.setFacts(newResult.getFacts());
            discoveryResultEntity.setUnmanaged(newResult.isUnmanaged());
            discoveryResultEntity.setReserved(newResult.isReserved());
            discoveryResultEntity.setConfig(newResult.getConfig());
            discoveryResultEntity.setDiscoverDeviceType(newResult.getDiscoverDeviceType());
            discoveryResultEntity.setServerPoolId(newResult.getServerPoolId());

            if (newResult.getFirmwareList() != null)
            	for (FirmwareDeviceInventoryEntity fdie : newResult.getFirmwareList())
            	{
            		discoveryResultEntity.addFirmwareDeviceInventoryEntity(fdie);
            	}

            
            session.saveOrUpdate(discoveryResultEntity);

            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during discovery result creation: " + cve);
            try {
                if (tx != null) {
        	    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create discovery result: " + ex);
            }
            if (cve.getConstraintName().contains("refid")) {
            	//throw new AsmManagerDAOException(AsmManagerDAOException.REASON_CODE.DUPLICATE_JOBID, cve);
            	throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_JOBID,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            } else {
                //throw new AsmManagerDAOException(AsmManagerDAOException.REASON_CODE.DUPLICATE_RECORD, cve);
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during update device: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during update device: " + ex);
            }
            if (e instanceof AsmManagerCheckedException) {
                throw e;
            }
            throw new AsmManagerInternalErrorException("Error updating the discovery result", "DiscoveryResultDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
               logger.warn("Unable to close session during update device: " + ex);
            }
        }
    }

    /**
     * Delete Discovery Result by parent job id.
     * @param parentJobId the parent job id used to delete from inventory.
     */
    public void deleteDiscoveryResult(String parentJobId)  {

        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // execute command.
           
            String hql="delete DiscoveryResultEntity  where parentJobId=:parentJobId";
            Query query = session.createQuery(hql);
            query.setString("parentJobId", parentJobId);
            int rowCount = query.executeUpdate();
            logger.debug("Deleted record count=" +rowCount);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete discovery result: " + e);
            try {
                if (tx != null) {
        	    tx.rollback();
        	}
            } catch (Exception ex) {
        	logger.warn("Unable to rollback transaction during delete discovery result: " + ex);
            }
            //throw new AsmManagerDAOException("Caught exception during delete discovery result: ", e);
            throw new AsmManagerInternalErrorException("Error deleting discovery result", "DiscoveryResultDAO", e);
            
        } finally {
            try {
                if (session != null) {
        	    session.close();
        	}
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete discovery result: " + ex);
            }
        }
    }
    
    

    
    /**
     * Helper method to get total number of records with filter parameters
     * @param filterInfos - List for holding filtering information parsed from filter query parameter value
     * @return int - total number of records
     */
    public Integer getTotalRecords(List<FilterParamParser.FilterInfo> filterInfos) {
        long totalRecords = 0;
        Session session = null;
        Transaction tx = null;
        try {
          session = _dao._database.getNewSession();
          tx = session.beginTransaction();
     
          Criteria criteria = session.createCriteria(DiscoveryResultEntity.class);
          BaseDAO.addFilterCriteria(criteria, filterInfos, DiscoveryResultEntity.class);
          totalRecords = (long) criteria.setProjection(Projections.rowCount()).uniqueResult();
     
          tx.commit();
         }
        catch (Exception e)
         {
          if (tx != null) try {tx.rollback();} catch (Exception e2){logger.warn("Error during rollback", e2);}
         }
        finally
         {try {if (session != null) session.close();} catch (Exception e2){logger.warn("Error during session close", e2);}}
        
        return (int)totalRecords;
    }
   
   
    public List<DiscoveryResultEntity> getDiscoveryResult(String parentJobId) throws AsmManagerDAOException {
        
        Session session = null;
        Transaction tx = null;
        List<DiscoveryResultEntity> entityList = new ArrayList<DiscoveryResultEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DiscoveryResultEntity where parentJobId = :parentJobId order by ipAddress";
            Query query = session.createQuery(hql);
            query.setString("parentJobId", parentJobId);
           
            for (Object result : query.list()) { 
                entityList.add((DiscoveryResultEntity) result);
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all discovery results: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all discovery results: " + ex);
            }
            throw new AsmManagerInternalErrorException("Error Getting Discovery result", "DiscoveryResultDAO", e);
        } finally {
            try {
                if (session != null) {
                        session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all discovery results: " + ex);
            }
        }

        return entityList;
        
    }

    /**
     * Delete all records.
     */
    public void deleteAll()  {

        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // execute command.
            String hql="delete DiscoveryResultEntity";
            Query query = session.createQuery(hql);
            int rowCount = query.executeUpdate();
            logger.debug("Deleted record count=" +rowCount);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete discovery result: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete discovery result: " + ex);
            }
            //throw new AsmManagerDAOException("Caught exception during delete discovery result: ", e);
            throw new AsmManagerInternalErrorException("Error deleting discovery result", "DiscoveryResultDAO", e);

        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete discovery result: " + ex);
            }
        }
    }

    /**
     * Delete a single Discovery Result record by device id
     * @param refId device id.
     */
    public void deleteDiscoveryResultByRefId(String refId)  {

        Session session = null;
        Transaction tx = null;
        int rowCount = 0;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // execute command.

            String hql="delete DiscoveryResultEntity  where refId=:refId";
            Query query = session.createQuery(hql);
            query.setString("refId", refId);
            rowCount = query.executeUpdate();
            logger.debug("Deleted record count=" +rowCount);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete discovery result: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete discovery result: " + ex);
            }
            //throw new AsmManagerDAOException("Caught exception during delete discovery result: ", e);
            throw new AsmManagerInternalErrorException("Error deleting discovery result", "DiscoveryResultDAO", e);

        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete discovery result: " + ex);
            }
        }
    }
}
