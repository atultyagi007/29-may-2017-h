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
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser.PaginationInfo;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.jraf.api.profile.data.ITemplateData;

public class TemplateDAO {

    // Logger.
    private static final Logger logger = Logger.getLogger(TemplateDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static TemplateDAO instance;

    private TemplateDAO() {
    }

    public static synchronized TemplateDAO getInstance() {
        if (instance == null)
            instance = new TemplateDAO();
        return instance;
    }

    public TemplateEntity create() {
        return (TemplateEntity) new TemplateEntity();
    }

    public TemplateEntity createTemplate(TemplateEntity template) throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;
        // Save the template in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            //TODO remove the set created date when @PrePersist is working
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            template.setCreatedDate(now);
            template.setCreatedBy(_dao.extractUserFromRequest());
            session.save(template);
            // Commit transaction and clean up.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught Exception during Template creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("unable to rollback during template creation" + ex);
            }
            if (cve.getConstraintName().contains("name")) {
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRecord(cve.getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during template creation: " + e);
            try {
                if (tx != null) tx.rollback();
            } catch (Exception e2) {
                logger.warn("Caught exception during template creation" + e2);
            }
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (Exception e2) {
                logger.warn("Caught exception during template creation" + e2);
            }
        }

        return template;
    }


    public TemplateEntity getTemplateByName(String name) {

        Session session = null;
        Transaction tx = null;
        TemplateEntity templateEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from TemplateEntity where name =:name";
            Query query = session.createQuery(hql);
            query.setString("name", name);
            templateEntity = (TemplateEntity) query.setMaxResults(1).uniqueResult();

//            Collection<PolicyRefEntity> dummy;
//            dummy = templateEntity.getPolicyRefEntities();
//            dummy.isEmpty();

            // Commit transaction.
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during get template for template name: " + name + ", " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get template: " + ex);
            }
            throw new AsmManagerInternalErrorException("Create Template", "TemplateDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get device: " + ex);
            }
        }

        return templateEntity;
    }

    public TemplateEntity getTemplateById(String Id) {

        Session session = null;
        Transaction tx = null;
        TemplateEntity templateEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from TemplateEntity where template_id =:id";
            Query query = session.createQuery(hql);
            query.setString("id", Id);
            templateEntity = (TemplateEntity) query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during get template for template id: " + Id + ", " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get template: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve Template by Id", "TemplateDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get template: " + ex);
            }
        }

        return templateEntity;
    }

    public List<TemplateEntity> getAllTemplates() {

        Session session = null;
        Transaction tx = null;
        List<TemplateEntity> entityList = new ArrayList<>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from TemplateEntity";
            Query query = session.createQuery(hql);
            for (Object result : query.list()) {
                entityList.add((TemplateEntity) result);
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all templates: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all templates: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all Templates", "TemplateDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all Templates: " + ex);
            }
        }

        return entityList;
    }

    public TemplateEntity updateTemplate(TemplateEntity updatedTemplate) throws AsmManagerInternalErrorException {
        Session session = null;
        Transaction tx = null;
        TemplateEntity templateEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from TemplateEntity where template_id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", updatedTemplate.getTemplateId());
            templateEntity = (TemplateEntity) query.setMaxResults(1).uniqueResult();

            if (templateEntity != null) {
                templateEntity.setTemplateType(updatedTemplate.getTemplateType());
                templateEntity.setTemplateDesc(updatedTemplate.getTemplateDesc());
                // templateEntity.setDisplayName(updatedTemplate.getDisplayName());
                // templateEntity.setDeviceType(updatedTemplate.getDeviceType());
                templateEntity.setState(updatedTemplate.getState());
                templateEntity.setName(updatedTemplate.getName());
                templateEntity.setUpdatedBy(_dao.extractUserFromRequest());

                //TODO remove the set updated date when @PreUpdate is working
                GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                templateEntity.setUpdatedDate(now);

                templateEntity.setMarshalledTemplateData(updatedTemplate.getMarshalledTemplateData());
                session.saveOrUpdate(templateEntity);

                //commit
                tx.commit();
            } else {
                String msg = "unable to update template with name " + updatedTemplate.getName();
                logger.warn(msg);
            }

        } catch (Exception e) {
            logger.warn("Caught exception during update template: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during update template: " + ex);
            }
            // TODO: Reviewer: instanceof will always return false since a RuntimeException can't be a com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException
            //if (e instanceof AsmManagerCheckedException) {
            //    throw e;
            //}
            throw new AsmManagerInternalErrorException("update Template", "TemplateDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during update template: " + ex);
            }
        }

        return templateEntity;

    }


    public void deleteTemplate(String id) {

        logger.info("Deleting template Id : " + id);
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from TemplateEntity where template_id =:id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            TemplateEntity template = (TemplateEntity) query.setMaxResults(1).uniqueResult();

            if (template != null) {
                session.delete(template);
            }
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete template: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete template: " + ex);
            }
            throw new AsmManagerInternalErrorException("Delete Template", "TemplateDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete template: " + ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<TemplateEntity> getAllTemplates(List<SortParamParser.SortInfo> sortInfos,
                                                List<FilterParamParser.FilterInfo> filterInfos, PaginationInfo paginationInfo) {

        Session session = null;
        Transaction tx = null;
        List<TemplateEntity> entityList = new ArrayList<>();

        try {
            int offset = paginationInfo.getOffset();
            int pageSize =  paginationInfo.getLimit();

            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(TemplateEntity.class);
            BaseDAO.addSortCriteria(criteria, sortInfos);
            List<FilterParamParser.FilterInfo> notFound = BaseDAO.addFilterCriteria(criteria, filterInfos, TemplateEntity.class);

            if (notFound != null && notFound.size() > 0) {
                criteria.createAlias("templateEntity", "templateEntityAlias");
                for (FilterParamParser.FilterInfo filterInfo : notFound) {
                    criteria.add(Restrictions.eq("templateEntityAlias.deviceKey", filterInfo.getColumnName()));
                    if (filterInfo.getColumnValue().size() == 1) {
                        criteria.add(Restrictions.eq("templateEntityAlias.deviceValue", filterInfo.getColumnValue().get(0)));
                    } else if (filterInfo.getColumnValue().size() > 1) {
                        criteria.add(Restrictions.in("templateEntityAlias.deviceValue", filterInfo.getColumnValue()));
                    }
                }
            }

            //criteria.setFirstResult((pageNumber - 1) * pageSize);
            criteria.setFirstResult(offset);
            criteria.setMaxResults(pageSize);
            criteria.setFetchMode("policyRefEntities", FetchMode.SELECT);

            entityList = criteria.list();

//            Collection<PolicyRefEntity> dummy;
//            for (TemplateEntity parm : entityList) {
//              dummy = parm.getPolicyRefEntities();
//              dummy.isEmpty();
//            }

            // workaround for dupliacate templates
            //entityList.addAll(buildUniqueList(criteria.list()));

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all templates: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all templates: " + ex);
            }
            throw new AsmManagerInternalErrorException("Get All templates", "TemplateDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all devices: " + ex);
            }
        }

        logger.warn("Template DAO get all templates size: " + entityList.size());
        return entityList;
    }

    private Set<TemplateEntity> buildUniqueList(List<TemplateEntity> list) {

        Set<TemplateEntity> templates = new HashSet<>();
        for (TemplateEntity t : list) {
            templates.add(t);
        }
        return templates;
    }

    /**
     * Helper method to get total number of records with filter parameters
     *
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

            Criteria criteria = session.createCriteria(TemplateEntity.class);
            BaseDAO.addFilterCriteria(criteria, filterInfos, TemplateEntity.class);
            totalRecords = (long) criteria.setProjection(Projections.rowCount()).uniqueResult();

            tx.commit();
        } catch (Exception e) {
            if (tx != null) try {
                tx.rollback();
            } catch (Exception e2) {
                logger.warn("Error during rollback", e2);
            }
        } finally {
            try {
                if (session != null) session.close();
            } catch (Exception e2) {
                logger.warn("Error during session close", e2);
            }
        }

        logger.warn("Template DAO getTotalRecords size: " + totalRecords);
        return (int) totalRecords;
    }

}
