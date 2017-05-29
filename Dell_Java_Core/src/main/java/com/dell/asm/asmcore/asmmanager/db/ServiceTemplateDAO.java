/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db;

import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateUserRefEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.pg.orion.common.utilities.MarshalUtil;

public class ServiceTemplateDAO {

    // Logger.
    private static final Logger logger = Logger.getLogger(ServiceTemplateDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance.
    private static ServiceTemplateDAO instance;

    private ServiceTemplateDAO() {
    }

    public static synchronized ServiceTemplateDAO getInstance() {
        if (instance == null)
            instance = new ServiceTemplateDAO();
        return instance;
    }

    public ServiceTemplateEntity create() {
        return (ServiceTemplateEntity) new ServiceTemplateEntity();
    }

    public ServiceTemplateEntity createTemplate(ServiceTemplateEntity template) throws AsmManagerCheckedException {

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


    public ServiceTemplateEntity getTemplateByName(String name) {

        Session session = null;
        Transaction tx = null;
        ServiceTemplateEntity templateEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from ServiceTemplateEntity where name =:name";
            Query query = session.createQuery(hql);
            query.setString("name", name.trim());
            templateEntity = (ServiceTemplateEntity) query.setMaxResults(1).uniqueResult();

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

    public ServiceTemplateEntity getTemplateById(String Id) {

        Session session = null;
        Transaction tx = null;
        ServiceTemplateEntity templateEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from ServiceTemplateEntity where template_id =:id";
            Query query = session.createQuery(hql);
            query.setString("id", Id);
            templateEntity = (ServiceTemplateEntity) query.setMaxResults(1).uniqueResult();

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

    public List<ServiceTemplateEntity> getAllTemplates() {

        Session session = null;
        Transaction tx = null;
        List<ServiceTemplateEntity> entityList = new ArrayList<>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from ServiceTemplateEntity";
            Query query = session.createQuery(hql);
            for (Object result : query.list()) {
                entityList.add((ServiceTemplateEntity) result);
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

    public ServiceTemplateEntity updateTemplate(ServiceTemplateEntity updatedTemplate) throws AsmManagerInternalErrorException {
        Session session = null;
        Transaction tx = null;
        ServiceTemplateEntity templateEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from ServiceTemplateEntity where template_id = :id";
            Query query = session.createQuery(hql);
            query.setString("id", updatedTemplate.getTemplateId());
            templateEntity = (ServiceTemplateEntity) query.setMaxResults(1).uniqueResult();

            if (templateEntity != null) {
                templateEntity.setTemplateDesc(updatedTemplate.getTemplateDesc());
                templateEntity.setTemplateVersion(updatedTemplate.getTemplateVersion());
                templateEntity.setTemplateValid(updatedTemplate.isTemplateValid());
                templateEntity.setTemplateLocked(updatedTemplate.isTemplateLocked());
                // templateEntity.setDisplayName(updatedTemplate.getDisplayName());
                // templateEntity.setDeviceType(updatedTemplate.getDeviceType());
                templateEntity.setDraft(updatedTemplate.isDraft());
                templateEntity.setName(updatedTemplate.getName());
                templateEntity.setUpdatedBy(_dao.extractUserFromRequest());

                //TODO remove the set updated date when @PreUpdate is working
                GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                templateEntity.setUpdatedDate(now);
                templateEntity.setLastDeployedDate(updatedTemplate.getLastDeployedDate());

                templateEntity.setMarshalledTemplateData(updatedTemplate.getMarshalledTemplateData());

                templateEntity.setManageFirmware(updatedTemplate.isManageFirmware());
                templateEntity.setUseDefaultCatalog(updatedTemplate.isUseDefaultCatalog());
                templateEntity.setFirmwareRepositoryEntity(updatedTemplate.getFirmwareRepositoryEntity());

                templateEntity.setAllUsersAllowed(updatedTemplate.isAllUsersAllowed());
                if (templateEntity.getAssignedUserList()!=null) {
                    templateEntity.getAssignedUserList().clear();
                }else{
                    Set<TemplateUserRefEntity> userRefEntities = new HashSet<>();
                    templateEntity.setAssignedUserList(userRefEntities);
                }

                for (TemplateUserRefEntity uRef: updatedTemplate.getAssignedUserList()) {
                    uRef.setId(UUID.randomUUID().toString());
                    templateEntity.getAssignedUserList().add(uRef);
                }

                updateAddOnModules(templateEntity,updatedTemplate);

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


    public ServiceTemplateEntity deleteTemplate(String id) {

        logger.info("Deleting template Id : " + id);
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from ServiceTemplateEntity where template_id =:id";
            Query query = session.createQuery(hql);
            query.setString("id", id);
            ServiceTemplateEntity template = (ServiceTemplateEntity) query.setMaxResults(1).uniqueResult();

            if (template != null) {
                session.delete(template);
            }
            // Commit transaction.
            tx.commit();
            return template;
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
    public List<ServiceTemplateEntity> getAllTemplates(List<SortParamParser.SortInfo> sortInfos,
                                                List<FilterParamParser.FilterInfo> filterInfos, int offset, int pageSize) {

        Session session = null;
        Transaction tx = null;
        List<ServiceTemplateEntity> entityList = new ArrayList<>();

        try {

            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(ServiceTemplateEntity.class);
            BaseDAO.addSortCriteria(criteria, sortInfos);
            List<FilterParamParser.FilterInfo> notFound = BaseDAO.addFilterCriteria(criteria, filterInfos, ServiceTemplateEntity.class);

            //criteria.setFirstResult((pageNumber - 1) * pageSize);
            if (offset>=0)
                criteria.setFirstResult(offset);

            if (pageSize>=0)
                criteria.setMaxResults(pageSize);

            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            entityList = criteria.list();

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

    private Set<ServiceTemplateEntity> buildUniqueList(List<ServiceTemplateEntity> list) {

        Set<ServiceTemplateEntity> templates = new HashSet<>();
        for (ServiceTemplateEntity t : list) {
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

            Criteria criteria = session.createCriteria(ServiceTemplateEntity.class);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            BaseDAO.addFilterCriteria(criteria, filterInfos, ServiceTemplateEntity.class);
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

    public List<ServiceTemplateEntity> getTemplatesForUserIds(Set<String> userIdsSet) {

        Session session = null;
        Transaction tx = null;
        List<ServiceTemplateEntity> entityList = new ArrayList<ServiceTemplateEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            // Create and execute command.
            String hql = "from ServiceTemplateEntity where id in (select templateId from TemplateUserRefEntity where user_id in ( :userIdsSet ))";
            Query query = session.createQuery(hql);
            query.setParameterList("userIdsSet",userIdsSet);
            for (Object result : query.list()) {
                entityList.add((ServiceTemplateEntity) result);
            }

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all deployments: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all deployments: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all deployments for userIds", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) session.close();
            } catch (Exception e2) {
                logger.warn("Error during session close", e2);
            }
        }

        return entityList;

    }

    private void updateAddOnModules(ServiceTemplateEntity originalTemplate, ServiceTemplateEntity updatedTemplate) {
        Map<String, AddOnModuleEntity> currentMap = new HashMap<>();
        for (AddOnModuleEntity entity : originalTemplate.getAddOnModules()) {
            currentMap.put(entity.getId(), entity);
        }
        Set<AddOnModuleEntity> updatedSet = new HashSet<>();
        for (AddOnModuleEntity entity : updatedTemplate.getAddOnModules()) {
            AddOnModuleEntity currentEntity = currentMap.get(entity.getId());
            if (currentEntity == null) {
                currentEntity = entity;
            }
            updatedSet.add(currentEntity);
        }
        originalTemplate.getAddOnModules().clear();
        originalTemplate.getAddOnModules().addAll(updatedSet);
    }

    public class StreamingCSVTemplateOutput implements StreamingOutput {

        private static final int BATCH_FETCH_SIZE = 100;
        private static final String TEMPLATE_NAME = "Name";
        private static final String TEMPLATE_CATEGORY = "Category";
        private static final String TEMPLATE_STATE = "State";
        private static final String TEMPLATE_HAS_ATTACHMENTS = "Attachments";
        private static final String TEMPLATE_LAST_DEPLOYED_ON = "Last Deployed On";
        private static final String TEMPLATE_ROW_TEMPLATE = "\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\"\n";
        private static final String TEMPLATE_STATE_DRAFT = "Draft";
        private static final String TEMPLATE_STATE_PUBLISHED = "Published";
      
        @Override
        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
            final PrintWriter writer = new PrintWriter(outputStream);
            final DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");    
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            StatelessSession statelessSession = null;
            Transaction tx = null;
            ScrollableResults results = null;
            try { 
                statelessSession = _dao._database.getNewStatelessSession();
                tx = statelessSession.beginTransaction();
                final Query query = statelessSession
                        .createSQLQuery("select template_id, name, draft, last_deployed_date, marshalledtemplatedata"
                                + " from service_template" 
                                + " where template_locked = false")
                        .setFetchSize(BATCH_FETCH_SIZE)
                        .setReadOnly(true);
                results = query.scroll(ScrollMode.FORWARD_ONLY);
                if (results != null) {
                    // add the csv row headers
                    writer.write(MessageFormat.format(TEMPLATE_ROW_TEMPLATE, TEMPLATE_STATE, TEMPLATE_CATEGORY, 
                             TEMPLATE_HAS_ATTACHMENTS, TEMPLATE_NAME, TEMPLATE_LAST_DEPLOYED_ON));
                    while (results.next()) {
                        final Object[] resultRow = results.get();
                        final ServiceTemplate template = MarshalUtil.unmarshal(ServiceTemplate.class, (String)resultRow[4]);
                        writer.write(MessageFormat.format(TEMPLATE_ROW_TEMPLATE,
                                (resultRow[2] != null) ? resolveTemplateState(template.isDraft()) : StringUtils.EMPTY,
                                template.getCategory(),
                                (resultRow[0] != null) ? hasAttachments((String)resultRow[0]) : StringUtils.EMPTY,  
                                (resultRow[1] != null) ? (String)resultRow[1] : StringUtils.EMPTY,
                                (resultRow[3] != null) ? formatter.format((Timestamp)resultRow[3]) : StringUtils.EMPTY));
                        writer.flush();
                    }
                }
                
            } catch (Exception e) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception e2) {
                        logger.warn("Error during rollback", e2);
                    }
                }
            } finally {
                try {
                    if (results != null) {
                        results.close();
                    }
                } catch (Exception exception) {
                    logger.debug("Exception caught cleaning up scrolling results", exception);
                }
                try {
                    if (statelessSession != null) {
                        statelessSession.close();
                    }
                } catch (Exception exception) {
                    logger.debug("Exception caught cleaning up session", exception);
                }
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception exception) {
                    logger.debug("Exception caught cleaning up print writer", exception);
                }
            }
        }
        
        private String resolveTemplateState(final boolean isDraft) {
            return isDraft ? TEMPLATE_STATE_DRAFT : TEMPLATE_STATE_PUBLISHED;
        }
        
        private boolean hasAttachments(String templateId) {
            if (StringUtils.isBlank(templateId)) {
                return false;
            }
            final File templateDir = new File(ServiceTemplateUtil.TEMPLATE_ATTACHMENT_DIR + templateId);
            return templateDir.exists();
        }


    };
}
