package com.dell.asm.asmcore.asmmanager.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.StringUtils;
import com.dell.pg.asm.identitypoolmgr.db.BaseEntityAudit;


public class GenericDAO {

	private static final Logger logger = Logger.getLogger(GenericDAO.class);
    private BaseDAO dao = BaseDAO.getInstance();

    private static GenericDAO instance;

    private GenericDAO() {
    }

    public static synchronized GenericDAO getInstance() {
        if (instance == null)
            instance = new GenericDAO();
        return instance;
    }

    public Session getNewSession() {
        return dao._database.getNewSession();
    }
    
    public String extractUserFromRequest() {
        return dao.extractUserFromRequest();
    }
    
    /**
     * This can handle both create or update.  
     * @param genericEntity
     * @return
     */
	public <T extends BaseEntityAudit> T create(T genericEntity) {


        Session session = null;
        Transaction tx = null;

        try {
        	
        	session = dao._database.getNewSession();
        	tx = session.beginTransaction();
        			
        			
        	 // Create and execute command.
            String hql = "from " + genericEntity.getClass().getSimpleName() + " where id = :id";            
            Query query = session.createQuery(hql);
            query.setString("id", genericEntity.getId());
            T baseEntity =  (T)query.setMaxResults(1).uniqueResult();
                        
            if (baseEntity == null)
            {
            	genericEntity.setCreatedBy(dao.extractUserFromRequest());
            	genericEntity.setCreatedDate(new Date());	
            }
        	
        	genericEntity.setUpdatedBy(genericEntity.getCreatedBy());
        	genericEntity.setUpdatedDate(genericEntity.getCreatedDate());
        	
            session.saveOrUpdate(genericEntity);            
            tx.commit();
        } catch (ConstraintViolationException cve) {
        	logger.error("Error seen while creating", cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.error("Unable to rollback transaction during create: " + ex);
            }

        } catch (Exception e) {
            logger.error("Caught exception during object creation: ", e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create : ", ex);
            }            
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create: " + ex);
            }
        }

        return genericEntity;    
	}
	
	public <T extends BaseEntityAudit> T update(T genericEntity) {

        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            genericEntity.setUpdatedDate(new Date());
            genericEntity.setUpdatedBy(dao.extractUserFromRequest());
            session.saveOrUpdate(genericEntity);

            // Commit transaction.
            tx.commit();
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
            throw new AsmManagerInternalErrorException("Update device", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "update device");
        }
        
		return genericEntity;    
	}
	
    public <T extends BaseEntityAudit> T evict(T genericEntity) {
        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            session.evict(genericEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during eviction: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during eviction: " + ex);
            }
            if (e instanceof AsmManagerCheckedException) {
                throw e;
            }
            throw new AsmManagerInternalErrorException("Evict", "GenericDAO", e);
        } finally {
            cleanupSession(session, "evict");
        }

        return genericEntity;
    }
	
        public <T extends BaseEntityAudit> T merge(T genericEntity) {

        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            genericEntity.setUpdatedDate(new Date());
            genericEntity.setUpdatedBy(dao.extractUserFromRequest());
            session.merge(genericEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during merge device: " + e);
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
            throw new AsmManagerInternalErrorException("Merge device", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "merge device");
        }
        
                return genericEntity;    
        }
	
	
	public <T extends BaseEntityAudit> T get(String id, Class<T> t) {


        Session session = null;
        Transaction tx = null;
        T baseEntity = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from " + t.getSimpleName() + " where id = :id";            
            Query query = session.createQuery(hql);
            query.setString("id", id);
            baseEntity =  (T)query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get " + t.getSimpleName() + " for id: " + id+ ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get by id: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve " + t.getSimpleName(), "GenericDAO", e);
        } finally {
            cleanupSession(session, "get firmware by name");
        }

        return baseEntity;    
	}	
	
	public <T extends BaseEntityAudit> T getByName(String name, Class<T> t) {

        Session session = null;
        Transaction tx = null;
        T baseEntity = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from " + t.getSimpleName() + " where name = :name";
            Query query = session.createQuery(hql);
            query.setString("name", name);
            baseEntity =  (T)query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
        	logger.warn("Caught exception during get " + t.getSimpleName() + " for name: " + name + ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get by name: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve " + t.getSimpleName(), "GenericDAO", e);
        } finally {
            cleanupSession(session, "get firmware by name");
        }

        return baseEntity;
    }
	
	public <T extends BaseEntityAudit> List<T> getByNameStartsWith(String name, Class<T> t) {

        Session session = null;
        Transaction tx = null;
        List<T> entityList = new ArrayList<>();

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from " + t.getSimpleName() + " where substring(name, 0, :length) = :name";
            Query query = session.createQuery(hql);
            query.setInteger("length", name.length() + 1);
            query.setString("name", name);
            for (Object o : query.list()) {
                entityList.add(t.cast(o));
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
        	logger.warn("Caught exception during get " + t.getSimpleName() + " for name: " + name + ", "  + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get by name: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve " + t.getSimpleName(), "GenericDAO", e);
        } finally {
            cleanupSession(session, "get firmware by name");
        }

        return entityList;
    }

	public <T extends BaseEntityAudit> List<T> getForEquals(Map<String, Object> attributeMap, Class<T> t)
	{
		return getForEquals(attributeMap, t, false);
	}
	
	
	public <T extends BaseEntityAudit> List<T> getForEquals(Map<String, Object> attributeMap, Class<T> t, boolean singleResult)
	{
		
	      Session session = null;
	      Transaction tx = null;
	        
	      List<T> entityList = new ArrayList<T>();
	        

	      try {
	    	  session = dao._database.getNewSession();
	    	  tx = session.beginTransaction();
	    	  Criteria criteria = session.createCriteria(t);
	    	  
	    	  Iterator<String> itr = attributeMap.keySet().iterator();
	    	  while (itr.hasNext())
	    	  {
	    		  String attribute = itr.next();	    			 
	    		  criteria.add(Restrictions.eq(attribute, attributeMap.get(attribute)));
	    	  }
	    	  if (singleResult)
	    		  criteria.setMaxResults(1);
	    	  
	    	  for (Object result : criteria.list()) {
	    		  entityList.add((T)result);
	    	  }

	    	  // Commit transaction.
	    	  tx.commit();
	      } catch (Exception e) {
	    	  logger.error("Caught exception during get all " + t.getSimpleName() + ": " + e, e);
	    	  try {
	    		  if (tx != null) {
	    			  tx.rollback();
	    		  }
	    	  } catch (Exception ex) {
	    		  logger.warn("Unable to rollback transaction during get for equals " + t.getSimpleName() + ": " + ex);
	    	  }
	    	  logger.error("Caught exception during get for equals " + t.getSimpleName() + ": " + e, e);
	      } finally {
	    	  cleanupSession(session, "get all deployments");
	      }
	        
	      return entityList;
	}
	
	/**
	 * A bespoke non generic lookup method for finding software components.
	 * @param attributeMap
	 * @param systemId
	 * @return
	 */
	public List<SoftwareComponentEntity> getForEquals(Map<String, Object> attributeMap, String systemId)
	{
		
	      Session session = null;
	      Transaction tx = null;
	        
	      List<SoftwareComponentEntity> entityList = new ArrayList<SoftwareComponentEntity>();
	        

	      try {
	    	  session = dao._database.getNewSession();
	    	  tx = session.beginTransaction();
	    	  Criteria criteria = session.createCriteria(SoftwareComponentEntity.class, "sc");
	    	  
	    	  Iterator<String> itr = attributeMap.keySet().iterator();
	    	  while (itr.hasNext())
	    	  {
	    		  String attribute = itr.next();
	    		  if (attributeMap.get(attribute) == null || (attributeMap.get(attribute) instanceof String && StringUtils.isEmpty((String)attributeMap.get(attribute))))
	    			  criteria.add(Restrictions.isNull(attribute));
	    		  else
	    			  criteria.add(Restrictions.eq(attribute, attributeMap.get(attribute)));
	    	  }	    	  
	    	  
	    	  if (!StringUtils.isEmpty(systemId))
	    	  {
		    	  criteria.createAlias("systemIDs", "sysid");
		    	  criteria.add(Restrictions.eq("sysid.systemId", systemId));
	    	  }
	    	  criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);	    	  	    	 
	    	  
	    	  for (Object result : criteria.list()) {
	    		  entityList.add((SoftwareComponentEntity)result);
	    	  }

	    	  // Commit transaction.
	    	  tx.commit();
	      } catch (Exception e) {
	    	  logger.error("Caught exception during get all SoftwareComponentEntity: " + e, e);
	    	  try {
	    		  if (tx != null) {
	    			  tx.rollback();
	    		  }
	    	  } catch (Exception ex) {
	    		  logger.warn("Unable to rollback transaction during get for equals SoftwareComponentEntity: " + ex);
	    	  }
	    	  logger.error("Caught exception during get for equals SoftwareComponentEntity: " + e, e);
	      } finally {
	    	  cleanupSession(session, "get all deployments");
	      }
	        
	      return entityList;
	}
	

    public <T extends BaseEntityAudit> List<T> getAll(Class<T> t) {

        Session session = null;
        Transaction tx = null;
        
        List<T> entityList = new ArrayList<T>();

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from " + t.getSimpleName();
            Query query = session.createQuery(hql);
            for (Object result : query.list()) {
            	entityList.add((T) result);
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all " + t.getSimpleName() + ": " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all " + t.getSimpleName() + ": " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all " + t.getSimpleName(), "GenericDAO", e);
        } finally {
            cleanupSession(session, "get all deployments");
        }

        return entityList;
    }

	
	public <T extends BaseEntityAudit> void delete(String id, Class<T> t) {

        logger.trace("Deleting " + t.getSimpleName() + " from database: " + id);
        Session session = null;
        Transaction tx = null;

        try {
            session = dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from " + t.getSimpleName() + " where id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            
            BaseEntityAudit baseEntity = (BaseEntityAudit) query.setMaxResults(1).uniqueResult();

            session.delete(baseEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete deployment: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete deployment: " + ex);
            }
            throw new AsmManagerInternalErrorException("Delete deployment", "DeviceInventoryDAO", e);
        } finally {
            cleanupSession(session, "delete deployment");
        }
    
	}
	
	public void cleanupSession(Session session, String operation) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (Exception ex) {
            logger.warn("Unable to close session during " + operation + ": " + ex);
        }
    }
}
