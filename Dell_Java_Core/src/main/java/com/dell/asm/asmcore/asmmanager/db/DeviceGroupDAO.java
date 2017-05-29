/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db;

import com.dell.asm.rest.common.AsmConstants;
import com.dell.asm.usermanager.DBInit;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerRuntimeException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.asm.rest.common.util.StringUtils;
import com.google.common.base.Joiner;

public class DeviceGroupDAO {
    // Logger
    private static final Logger logger = Logger.getLogger(DeviceGroupDAO.class);

    // DB access
    private final BaseDAO _dao = BaseDAO.getInstance();

    // Singleton instance
    private static DeviceGroupDAO instance;

    private static final String GLOBAL_POOL_QUERY;
    private static final String SPECIFIC_POOL_QUERY;
    private static final String SPECIFIC_POOL_SYSTEM_USER_QUERY;
    private static final String POOL_GROUP_BY_CLAUSE;
    private static final String POOL_ORDER_BY_CLAUSE;
    private static final String POOL_SERVERS_COUNT;

    static {
        List<DeviceType> serverTypes = DeviceType.getAllServers();
        List<String> serverTypeNames = new ArrayList<>(serverTypes.size());
        for (DeviceType serverType : serverTypes) {
            // NOTE: serverTypeNames are enum names so they cannot contain dangerous sql characters
            // that could result in sql injection attacks.
            serverTypeNames.add("'" + serverType.name() + "'");
        }

        GLOBAL_POOL_QUERY = "SELECT ref_id, service_tag, i.state FROM "
                + "device_inventory AS i LEFT JOIN groups_device_inventory AS gd ON i.ref_id = "
                + "gd.devices_inventory_seq_id WHERE gd.groups_seq_id IS NULL AND device_type IN "
                + "(" + Joiner.on(", ").join(serverTypeNames) + ")";

        SPECIFIC_POOL_QUERY = "SELECT ref_id, service_tag, i.state FROM device_inventory "
                + "AS i JOIN groups_device_inventory AS gd ON i.ref_id = "
                + "gd.devices_inventory_seq_id JOIN groups AS g ON gd.groups_seq_id = g.seq_id JOIN "
                + "groups_users AS u ON g.seq_id = u.groups_seq_id AND u.user_seq_id = :userId"
                + " WHERE device_type IN (" + Joiner.on(", ").join(serverTypeNames) + ")";

        // no user check for SYSTEM user - request comes from asm-deployer, return all servers
        SPECIFIC_POOL_SYSTEM_USER_QUERY = "SELECT ref_id, service_tag, i.state FROM device_inventory "
                + "AS i JOIN groups_device_inventory AS gd ON i.ref_id = "
                + "gd.devices_inventory_seq_id JOIN groups AS g ON gd.groups_seq_id = g.seq_id "
                + " WHERE device_type IN (" + Joiner.on(", ").join(serverTypeNames) + ")";

        POOL_GROUP_BY_CLAUSE = " GROUP BY ref_id, service_tag, i.state";
        POOL_ORDER_BY_CLAUSE = " ORDER BY service_tag";
        POOL_SERVERS_COUNT = "SELECT Count(*) FROM device_inventory, groups_device_inventory, groups "
        		 + "WHERE device_inventory.ref_id = groups_device_inventory.devices_inventory_seq_id "
        		 + "AND device_inventory.device_type IN (" + Joiner.on(", ").join(serverTypeNames) + ") "
        		 + "AND groups_device_inventory.groups_seq_id = groups.seq_id "
        		 + "AND groups.seq_id = :poolId";
    }

    private DeviceGroupDAO() {
    }

    // Return the single instance
    public static synchronized DeviceGroupDAO getInstance() {
        if (instance == null)
            instance = new DeviceGroupDAO();
        return instance;
    }

    /**
     * Create Device Group 
     * 
     * @param entity - device group entity
     * 
     * @return entity - device group entity
     *
     * @throws AsmManagerCheckedException
     */
    public DeviceGroupEntity createGroupDevice(DeviceGroupEntity entity) throws AsmManagerCheckedException{

        Session session = null;
        Transaction tx = null;
        DeviceGroupEntity duplicateEntity = null;

        try {
            duplicateEntity = getDeviceGroupByName(entity.getName());
        } catch (AsmManagerCheckedException ex) {
            logger.warn("Device Group with same name doesnt exists: " + ex);
        }

        if (null != duplicateEntity) {
            throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                    AsmManagerMessages.duplicateDeviceGroupName(entity.getName()));
        }

        // Save the device in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            logger.info("Creating device group in inventory: ");
            entity.setCreatedDate(new GregorianCalendar());
            entity.setUpdatedDate(new GregorianCalendar());
            entity.setCreatedBy(_dao.extractUserFromRequest());
            entity.setUpdatedBy(_dao.extractUserFromRequest());
            
              if(null == entity.getDeviceInventories())
                entity.setDeviceInventories(new ArrayList<DeviceInventoryEntity>());
              
              if(null == entity.getGroupsUsers())
                 entity.setGroupsUsers(new HashSet<Long>());
              
              
            session.save(entity);

            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during device group creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device group: " + ex);
            }
            if (cve.getConstraintName().contains("seqId")) {
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID, AsmManagerMessages.duplicateRefId(cve
                        .getSQLException().getMessage()));
            } else {
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD, AsmManagerMessages.duplicateRecord(cve
                        .getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during device group inventory creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device group: " + ex);
            }
            throw new AsmManagerInternalErrorException("Create device group", "DeviceGroupDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create device group: " + ex);
            }
        }

        return entity;
    }

    
    /**
     * Retrieve Device Group by its Id
     * 
     * @param seqId - device group id
     * 
     * @return entity - device group entity
     *   
     * @throws AsmManagerCheckedException
     * @throws AsmManagerInternalErrorException
     */
    public DeviceGroupEntity getDeviceGroupById(Long seqId) throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;
        DeviceGroupEntity result = null;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            Criteria createCriteria = session.createCriteria(DeviceGroupEntity.class);
            Criterion idRest = Restrictions.eq("seqId", seqId);
            createCriteria.add(idRest);
            result = (DeviceGroupEntity) createCriteria.uniqueResult();
   
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get Device Group: " + ex);
            }
            throw new AsmManagerInternalErrorException("fetch Device Group", "DeviceGroupDAO", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        if (result == null) {
            throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND,
                    AsmManagerMessages.deviceGroupNotFound(String.valueOf(seqId)));
        }
        return result;

    }


    /**
     * Retrieve Device Group by its name
     * 
     * @param name - device group name
     * 
     * @return entity - device group entity
     *
     * @throws AsmManagerCheckedException
     * @throws AsmManagerInternalErrorException
     */
    public DeviceGroupEntity getDeviceGroupByName(String name) throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;
        DeviceGroupEntity result = null;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            Criteria createCriteria = session.createCriteria(DeviceGroupEntity.class);
            Criterion idRest = Restrictions.eq("name", name);
            createCriteria.add(idRest);
            result = (DeviceGroupEntity) createCriteria.uniqueResult();
            
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get Device Group: " + ex);
            }
            throw new AsmManagerInternalErrorException("fetch Device Group", "DeviceGroupDAO", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        if (result == null) {
            throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND,
                    AsmManagerMessages.deviceGroupNotFound(name));
        }
        return result;

    }

    /**
     * Update Device Group
     * 
     * @return updateEntity - updated device group entity
     * 
     * @throws AsmManagerCheckedException
     */
    public DeviceGroupEntity updateGroupDevice(DeviceGroupEntity updateEntity) throws AsmManagerCheckedException {

        if (updateEntity == null) {
            return null;
        }
        // Initialize locals.
        Session session = null;
        Transaction tx = null;
        DeviceGroupEntity entity = null;

        // Save the job history in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Save the new DeviceGroupEntity.
            String hql = "from DeviceGroupEntity where seqId = :id";
            Query query = session.createQuery(hql);
            query.setLong("id", updateEntity.getSeqId());
            entity = (DeviceGroupEntity) query.setMaxResults(1).uniqueResult();

            if (null != entity) {

                if(null != updateEntity.getName() || !"".equals(updateEntity.getName()))
                    entity.setName(updateEntity.getName());
                
                if(null != updateEntity.getDescription())
                    entity.setDescription(updateEntity.getDescription());
                
                entity.setUpdatedBy(_dao.extractUserFromRequest());
                entity.setUpdatedDate(new GregorianCalendar());
                
                if(null != updateEntity.getDeviceInventories())
                  entity.setDeviceInventories(updateEntity.getDeviceInventories());
                
                if(null != updateEntity.getGroupsUsers())
                   entity.setGroupsUsers(updateEntity.getGroupsUsers());
                
                session.saveOrUpdate(entity);

                // Commit transaction and clean up.
                tx.commit();
            } else {
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST,
                        AsmManagerMessages.updateDeviceGroupError(String.valueOf(updateEntity.getSeqId())));
            }

        } catch (Exception e) {
            logger.warn("Caught exception during updating device group: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during updating device group: " + ex);
            }
            throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.INVALID_REQUEST,
                    AsmManagerMessages.updateDeviceGroupError(String.valueOf(updateEntity.getSeqId())));
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during updating device group: " + ex);
            }
        }
        return entity;
    }

    /**
     * Helper method to get total number of records with filter parameters
     * 
     * @param filterInfos
     *            - List for holding filtering information parsed from filter query parameter value
     * @return int - total number of records
     */
    public Integer getTotalRecords(List<FilterParamParser.FilterInfo> filterInfos) {
        long totalRecords = 0;
        Session session = null;
        Transaction tx = null;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DeviceGroupEntity.class);
            if( filterInfos != null)
            {
                addFilterCriteria(criteria, filterInfos, DeviceGroupEntity.class);
            }
            totalRecords = (long) criteria.setProjection(Projections.rowCount()).uniqueResult();

            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                try {
                    tx.rollback();
                } catch (Exception e2) {
                    logger.warn("Error during rollback", e2);
                }
        } finally {
            try {
                if (session != null)
                    session.close();
            } catch (Exception e2) {
                logger.warn("Error during session close", e2);
            }
        }

        return (int) totalRecords;
    }


    /**
     * Retrieve all Device Groups
     * 
     * @param sortInfos - column name(s) on which the sorting is done
     * @param filterInfos - column name(s) on which the filtering is done
     * @param paginationInfo - pagination information (offset and limit)
     * 
     * @return all the device groups (paginated list)
     * 
     * @throws AsmManagerCheckedException
     */
    @SuppressWarnings("unchecked")
    public List<DeviceGroupEntity> getAllDeviceGroup(List<SortParamParser.SortInfo> sortInfos, List<FilterParamParser.FilterInfo> filterInfos,
            PaginationParamParser.PaginationInfo paginationInfo) throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;
        List<DeviceGroupEntity> result = null;

        try 
        {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(DeviceGroupEntity.class);
            
            if (paginationInfo != null) 
            {
                int offset = paginationInfo.getOffset();
                int pageSize = paginationInfo.getLimit();
                criteria.setFirstResult(offset);
                criteria.setMaxResults(pageSize);
            } 

            if (sortInfos != null) {
                addSortCriteria(criteria, sortInfos);
            }

            if (filterInfos != null) {
                addFilterCriteria(criteria, filterInfos, DeviceGroupEntity.class);
            }

            

            result = (List<DeviceGroupEntity>) criteria.list();
            tx.commit();

        } 
        catch (Exception e) 
        {
            logger.warn("Caught exception during getAllDeviceGroup: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during getAllDeviceGroup: " + ex);
            }
            throw new AsmManagerInternalErrorException("fetch Device Group", "DeviceGroupDAO", e);
        } 
        finally 
        {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during getAllDeviceGroup: " + ex);
            }
        }

        //if (result == null || result.size() == 0) {
        //    throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND,
        //            AsmManagerMessages.deviceGroupsDataNotFound(""));
        //}
        
        return result;
    }

    /**
     * Helper method for adding sorting criteria
     * 
     * @param criteria 
     *            - the query criteria
     * @param sortInfos 
     *            - list for sort specifications. Sort info are added in the order in the list
     */
    private void addSortCriteria(Criteria criteria, List<SortParamParser.SortInfo> sortInfos) {
        for (SortParamParser.SortInfo sortInfo : sortInfos) {
            if (SortParamParser.SortOrder.DESC.equals(sortInfo.getSortOrder())) {
                criteria.addOrder(Order.desc(sortInfo.getColumnName()));
            } else {
                criteria.addOrder(Order.asc(sortInfo.getColumnName()));
            }
        }
    }

    /**
     * Helper method for adding filter criteria
     * 
     * @param criteria
     *            - the filter criteria
     * @param filterInfos
     *            - list for filter specifications. filter info are added in the order in the list
     *            
     * @return List<FilterParamParser.FilterInfo>
     *                             - list of added filters
     * 
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    private List<FilterParamParser.FilterInfo> addFilterCriteria(Criteria criteria, List<FilterParamParser.FilterInfo> filterInfos,
            Class persistentClass) {

        LinkedList<FilterParamParser.FilterInfo> notFound = new LinkedList<>();

        for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
            String columnName = filterInfo.getColumnName();
            List<?> values = filterInfo.getColumnValue();

            //
            // Cast strings to the property type by scanning the persistent class
            //
            Class<?> typeClass = null;
            // try non-boolean-type naming.
            try {
                typeClass = persistentClass.getMethod("get" + StringUtils.capitalize(columnName)).getReturnType();
            } catch (NoSuchMethodException e) {
                logger.info("cannot find a method for " + columnName + " in " + persistentClass.toString());

            }
            // try boolean-type naming.
            if (typeClass == null) {
                try {
                    typeClass = persistentClass.getMethod("is" + StringUtils.capitalize(columnName)).getReturnType();
                } catch (NoSuchMethodException e) {
                    logger.info("cannot find a method for " + columnName + " in " + persistentClass.toString());
                }
            }
            // property not found. skip.
            if (typeClass == null) {
                notFound.add(filterInfo);
                continue;
            }
            if (!typeClass.isAssignableFrom(String.class)) {

                // byte/short/int/long
                if (typeClass == byte.class || typeClass == short.class || typeClass == int.class || typeClass == long.class) {
                    LinkedList<Long> castedValues = new LinkedList<>();
                    for (String stringValue : filterInfo.getColumnValue()) {
                        castedValues.add(Long.valueOf(stringValue));
                    }
                    // Set casted values
                    values = castedValues;

                    // float/double
                } else if (typeClass == float.class || typeClass == double.class) {
                    LinkedList<Double> castedValues = new LinkedList<>();
                    for (String stringValue : filterInfo.getColumnValue()) {
                        castedValues.add(Double.valueOf(stringValue));
                    }
                    // Set casted values
                    values = castedValues;
                    // boolean
                } else if (typeClass == boolean.class) {
                    LinkedList<Boolean> castedValues = new LinkedList<>();
                    for (String stringValue : filterInfo.getColumnValue()) {
                        castedValues.add(Boolean.valueOf(stringValue));
                    }
                    // Set casted values
                    values = castedValues;
                    // char
                } else if (typeClass == char.class) {
                    LinkedList<Character> castedValues = new LinkedList<>();
                    for (String stringValue : filterInfo.getColumnValue()) {
                        castedValues.add(Character.valueOf(stringValue.charAt(0)));
                    }
                    // Set casted values
                    values = castedValues;
                }
            }

            //
            // Translate filters to Hibernate Criteria
            //
            if (values.size() > 1) {
                if (FilterParamParser.FilterOperator.EQUAL.equals(filterInfo.getFilterOperator())) {
                    criteria.add(Restrictions.in(columnName, values));
                } else {
                    throw new IllegalArgumentException("filter operation '" + filterInfo.getFilterOperator() + "' is not recognized.");
                }
            } else if (values.size() == 1) {
                if (FilterParamParser.FilterOperator.EQUAL.equals(filterInfo.getFilterOperator())) {
                    criteria.add(Restrictions.eq(columnName, values.get(0)));
                } else if (FilterParamParser.FilterOperator.CONTAIN.equals(filterInfo.getFilterOperator())) {

                    // Escape '_', '%', and '\' for Hibernate.
                    String escapedString = values.get(0).toString();
                    escapedString = escapedString.replace("\\", "\\\\").replace("_", "\\_").replace("%", "\\%");

                    criteria.add(Restrictions.like(columnName, escapedString, MatchMode.ANYWHERE));
                } else {
                    throw new IllegalArgumentException("filter operation '" + filterInfo.getFilterOperator() + "' is not recognized.");
                }
            }
        }

        return notFound;
    }

    /**
     * Delete Device Group
     * 
     * @param deviceGroupEntity - device group entity to be deleted
     * 
     * @throws AsmManagerInternalErrorException
     * 
     */
    public void deleteDeviceGroup(DeviceGroupEntity deviceGroupEntity) {
        logger.info("Deleting Device Group from inventory: " + deviceGroupEntity.getSeqId());
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            session.delete(deviceGroupEntity);

            tx.commit();
            session.close();
        } catch (Exception e) {
            logger.warn("Caught exception during delete Device Group with the id " + deviceGroupEntity.getSeqId() + " : " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete Device Group with the id " + deviceGroupEntity.getSeqId() + " : " + e);
            }
            throw new AsmManagerInternalErrorException("Delete Device Group", "DeviceGroupDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete Device Group with the id " + deviceGroupEntity.getSeqId() + " : " + ex);
            }
        }
    }

    /**
     * Helper method for deleting Group Users Association
     * 
     * @param userSeqIds - user's id to be deleted
     * 
     * @throws AsmManagerCheckedException
     *
     */
    // If there is a change in groups_users table structure, then this method should be updated accordingly
    public void deleteGroupUsersAssociation(Set<Long> userSeqIds) throws AsmManagerCheckedException {

        if (userSeqIds == null || userSeqIds.size() <= 0)
            return;

        // Initialize locals.
        Session session = null;
        Transaction tx = null;
        List<BigInteger> userIds = new ArrayList<>();
        
        for (Long id : userSeqIds) {
            userIds.add(new BigInteger(String.valueOf(id)));
        }

        // Save the job history in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // delete the Group Users Association.
            String sql = "delete from groups_users WHERE user_seq_id IN (:userSeqIds)";
            Query query = session.createSQLQuery(sql);
            query.setParameterList("userSeqIds", userIds);
            query.executeUpdate();
            tx.commit();

        } catch (Exception e) {
            logger.warn("Caught exception during deleting Group Users association: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during deleting Group Users association: " + ex);
            }

        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during deleting Group Users association: " + ex);
            }
        }
    }

    public String getPoolName(final String poolId) {
        if (poolId == null) {
            throw new IllegalArgumentException("Pool must not be null");
        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID.equals(poolId)) {
            return "Global";
        } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ALL_ID.equals(poolId)) {
            return "All Servers";
        } else {
            try {
                DeviceGroupEntity group = getDeviceGroupById(Long.valueOf(poolId));
                if (group == null) {
                    return "Unknown";
                } else {
                    return group.getName();
                }
            } catch (AsmManagerCheckedException e) {
                return "Unknown";
            }
        }
    }

    public static class BriefServerInfo {
        private final String refId;
        private final String serviceTag;
        private final DeviceState state;

        public BriefServerInfo(String refId, String serviceTag, DeviceState state) {
            this.refId = refId;
            this.serviceTag = serviceTag;
            this.state = state;
        }

        public String getRefId() {
            return refId;
        }

        public String getServiceTag() {
            return serviceTag;
        }

        public DeviceState getState() {
            return state;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("refId", refId)
                    .append("serviceTag", serviceTag)
                    .toString();
        }
    }

    @SuppressWarnings("unchecked")
    private List<BriefServerInfo> buildBriefServerInfoList(List list) {
        List<BriefServerInfo> ret = new ArrayList<>(list.size());
        for (Object o : list) {
            Object[] row = (Object[])o;
            String stateString = (String)row[2];
            DeviceState state = DeviceState.valueOf(stateString);
            ret.add(new BriefServerInfo((String)row[0], (String)row[1], state));
        }
        return ret;
    }

    public List<BriefServerInfo> getAccessiblePoolServers(final long userId, final String poolId) {
        try {
            return _dao.doWithSession(new BaseDAO.CallableWithSession<List<BriefServerInfo>>() {
                @Override
                public List<BriefServerInfo> run(Session session) {
                    Map<String, Object> keyValues = new HashMap<>();

                    SQLQuery query;
                    if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID.equals(poolId)) {
                        query = session.createSQLQuery(GLOBAL_POOL_QUERY + POOL_GROUP_BY_CLAUSE + POOL_ORDER_BY_CLAUSE);
                    } else if (ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_ALL_ID.equals(poolId)
                            || poolId == null) {

                        // system user has access to all servers in the pool
                        if (userId == DBInit.SYSTEM_USER_ID) {
                            query = session.createSQLQuery(GLOBAL_POOL_QUERY + " UNION "
                                    + SPECIFIC_POOL_SYSTEM_USER_QUERY + POOL_GROUP_BY_CLAUSE + POOL_ORDER_BY_CLAUSE);
                        }else {
                            query = session.createSQLQuery(GLOBAL_POOL_QUERY + " UNION "
                                    + SPECIFIC_POOL_QUERY + POOL_GROUP_BY_CLAUSE + POOL_ORDER_BY_CLAUSE);
                            query.setParameter("userId", userId);
                        }
                    } else {
                        StringBuilder sql;
                        if (userId == DBInit.SYSTEM_USER_ID) {
                            sql = new StringBuilder(SPECIFIC_POOL_SYSTEM_USER_QUERY + " AND g.seq_id = :poolId");
                        }else {
                            sql = new StringBuilder(SPECIFIC_POOL_QUERY + " AND g.seq_id = :poolId");
                        }
                        keyValues.put("poolId", Long.valueOf(poolId));

                        sql.append(POOL_GROUP_BY_CLAUSE);
                        sql.append(POOL_ORDER_BY_CLAUSE);

                        query = session.createSQLQuery(sql.toString());
                        if (userId != DBInit.SYSTEM_USER_ID)
                            query.setParameter("userId", userId);
                    }

                    for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
                        query.setParameter(entry.getKey(), entry.getValue());
                    }

                    return buildBriefServerInfoList(query.list());
                }

                @Override
                public List<BriefServerInfo> failed(SQLException e) throws SQLException {
                    logger.error("Failed to look up servers from pool " + poolId + " for user "
                            + userId, e);
                    throw e;
                }
            });
        } catch (SQLException e) {
            throw new AsmManagerRuntimeException(e);
        }
    }

    public List<BriefServerInfo> getAccessibleServers(final long userId, final List<String> serverRefIds) {
        try {
            return _dao.doWithSession(new BaseDAO.CallableWithSession<List<BriefServerInfo>>() {
                private StringBuilder appendServerRefIds(StringBuilder sql,
                                                         Map<String, Object> keyValues) {
                    if (serverRefIds != null && serverRefIds.size() > 0) {
                        int i = 0;
                        sql.append(" AND i.ref_id IN (");
                        for (String serverRefId : serverRefIds) {
                            String name = "ref" + i++;
                            sql.append(':').append(name).append(", ");
                            keyValues.put(name, serverRefId);
                        }
                        sql.deleteCharAt(sql.length() - 1);
                        sql.deleteCharAt(sql.length() - 1);
                        sql.append(")");
                    }
                    return sql;
                }

                @Override
                public List<BriefServerInfo> run(Session session) {
                    Map<String, Object> keyValues = new HashMap<>();

                    StringBuilder sql = new StringBuilder(GLOBAL_POOL_QUERY);
                    if (userId == DBInit.SYSTEM_USER_ID) {
                        appendServerRefIds(sql, keyValues).append(" UNION ").append(SPECIFIC_POOL_SYSTEM_USER_QUERY);
                    }else {
                        appendServerRefIds(sql, keyValues).append(" UNION ").append(SPECIFIC_POOL_QUERY);
                    }
                    appendServerRefIds(sql, keyValues).append(POOL_GROUP_BY_CLAUSE).append(POOL_ORDER_BY_CLAUSE);
                    SQLQuery query = session.createSQLQuery(sql.toString());

                    if (userId != DBInit.SYSTEM_USER_ID)
                        query.setParameter("userId", userId);

                    for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
                        query.setParameter(entry.getKey(), entry.getValue());
                    }

                    return buildBriefServerInfoList(query.list());
                }

                @Override
                public List<BriefServerInfo> failed(SQLException e) throws SQLException {
                    logger.error("Failed to look up servers " + serverRefIds + " for user "
                            + userId, e);
                    throw e;
                }
            });
        } catch (SQLException e) {
            throw new AsmManagerRuntimeException(e);
        }
    }
    
	/**
	 * Returns the number of servers available in the pool.
	 * 
	 * @param poolId the id of the pool whose server count will be returned.
	 * @return the number of servers available in the pool.
	 */
    public int getNumberOfServersInPool(final String poolId) {
		int numberOfServersInPool = 0;
		if (poolId != null && poolId.trim().length() > 0) {
			// Initialize locals.
			Session session = null;
			Transaction tx = null;
			try {
				session = _dao._database.getNewSession();
				tx = session.beginTransaction();
				SQLQuery query = session.createSQLQuery(POOL_SERVERS_COUNT);
				query.setParameter("poolId", Long.valueOf(poolId));
				numberOfServersInPool = ((BigInteger) query.uniqueResult())
				        .intValue();
				tx.commit();
			} catch (Exception e) {
				logger.warn("Caught exception during getNumberOfServersInPool: "
				        + e);
				try {
					if (tx != null) {
						tx.rollback();
					}
				} catch (Exception ex) {
					logger.warn("Unable to rollback transaction during getNumberOfServersInPool: "
					        + ex);
				}
			} finally {
				try {
					if (session != null) {
						session.close();
					}
				} catch (Exception ex) {
					logger.warn("Unable to close session during getNumberOfServersInPool: "
					        + ex);
				}
			}
		}
		return numberOfServersInPool;
	}    
}
