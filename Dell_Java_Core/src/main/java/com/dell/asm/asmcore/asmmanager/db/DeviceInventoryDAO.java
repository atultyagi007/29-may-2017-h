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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.firmware.SourceType;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceLastJobStateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.JobType;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerDAOException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser.PaginationInfo;
import com.dell.asm.rest.common.util.SortParamParser;

public class DeviceInventoryDAO {
    // Logger.
    private static final Logger logger = Logger.getLogger(DeviceInventoryDAO.class);

    // DB access.
    private BaseDAO _dao = BaseDAO.getInstance();

    private static final String SERVICE_FILTER_PARAM = "service";
    private static final String SERVERPOOL_FILTER_PARAM = "serverpool";

    public DeviceInventoryEntity createDeviceInventoryForDiscoveryFailed(DeviceInventoryEntity device)
            throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;

        // Save the device in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            logger.info("Creating device in inventory: " + device.getRefId());

            device.setCreatedDate(new GregorianCalendar());
            device.setDiscoveredDate(new GregorianCalendar());
            device.setCreatedBy(_dao.extractUserFromRequest());
            device.setState(DeviceState.DISCOVERY_FAILED);
            session.save(device);

            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during device inventory creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            if (cve.getConstraintName().contains("refid")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            } else if (cve.getConstraintName().contains("service_tag")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_SERVICETAG,
                        AsmManagerMessages.duplicateServiceTag(cve.getSQLException().getMessage()));
            } else {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRecord(cve.getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during device inventory creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Create devices", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create device: " + ex);
            }
        }

        return device;


    }

    /**
     * Create Device Inventory.
     *
     * @param device
     * @return the entity
     */
    public DeviceInventoryEntity createDeviceInventory(DeviceInventoryEntity device)
            throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;

        // Save the device in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            logger.info("Creating device in inventory: " + device.getRefId());

            device.setCreatedDate(new GregorianCalendar());
            device.setDiscoveredDate(new GregorianCalendar());
            device.setCreatedBy(_dao.extractUserFromRequest());
            device.setState(DeviceState.READY);
            session.save(device);

            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during device inventory creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            if (cve.getConstraintName().contains("refid")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            } else if (cve.getConstraintName().contains("service_tag")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_SERVICETAG,
                        AsmManagerMessages.duplicateServiceTag(cve.getSQLException().getMessage()));
            } else {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRecord(cve.getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during device inventory creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Create devices", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create device: " + ex);
            }
        }

        return device;
    }

    /**
     * Create Devices Inventory.
     *
     * @param devices list of devices to add to inventory
     * @return the entities
     */
    public List<DeviceInventoryEntity> createDeviceInventory(List<DeviceInventoryEntity> devices)
            throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;

        // Save the device in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            for (DeviceInventoryEntity device : devices) {
                device.setCreatedDate(new GregorianCalendar());
                device.setCreatedBy(_dao.extractUserFromRequest());
                device.setDiscoveredDate(new GregorianCalendar());
                if (device.getManagedState() == null)
                    device.setManagedState(ManagedState.MANAGED);
                if (device.getState() == null)
                    device.setState(DeviceState.READY);
                session.save(device);
            }
            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during device inventory creation: " + cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            if (cve.getConstraintName().contains("refid")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_REFID,
                        AsmManagerMessages.duplicateRefId(cve.getSQLException().getMessage()));
            } else if (cve.getConstraintName().contains("service_tag")) {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_SERVICETAG,
                        AsmManagerMessages.duplicateServiceTag(cve.getSQLException().getMessage()));
            } else {
                throw new AsmManagerCheckedException(
                        AsmManagerCheckedException.REASON_CODE.DUPLICATE_RECORD,
                        AsmManagerMessages.duplicateRecord(cve.getSQLException().getMessage()));
            }
        } catch (Exception e) {
            logger.warn("Caught exception during device inventory creation: " + e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Add device", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create device: " + ex);
            }
        }

        return devices;
    }

    /**
     * Retrieve Device Inventory.
     *
     * @return the entity
     */
    public DeviceInventoryEntity getDeviceInventory(String refId) {

        Session session = null;
        Transaction tx = null;
        DeviceInventoryEntity deviceInventoryEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeviceInventoryEntity where refId = :refId";
            Query query = session.createQuery(hql);
            query.setString("refId", refId);
            deviceInventoryEntity = (DeviceInventoryEntity) query.setMaxResults(1).uniqueResult();

            if (deviceInventoryEntity != null) {
                Hibernate.initialize(deviceInventoryEntity.getDeviceInventoryComplianceEntities());
            }

            this.setFirmwareBasedOnDeployment(deviceInventoryEntity);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get device for refId: " + refId + ", " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.error("Unable to rollback transaction during get device: " + refId, ex);
            }
            // not found landed hee 
            // throw new AsmManagerInternalErrorException("Retrieve device", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get device: " + ex);
            }
        }

        return deviceInventoryEntity;
    }


    public DeviceInventoryEntity getDeviceInventoryByServiceTag(String serviceTag) {

        Session session = null;
        Transaction tx = null;
        DeviceInventoryEntity deviceInventoryEntity = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeviceInventoryEntity where serviceTag = :serviceTag";
            Query query = session.createQuery(hql);
            query.setString("serviceTag", serviceTag);
            deviceInventoryEntity = (DeviceInventoryEntity) query.setMaxResults(1).uniqueResult();

            this.setFirmwareBasedOnDeployment(deviceInventoryEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get device for serviceTag: " + serviceTag + ", " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve device", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get device: " + ex);
            }
        }

        return deviceInventoryEntity;
    }

    /**
     * Retrieve all from Device Inventory.
     *
     * @return list of entities
     */
    public List<DeviceInventoryEntity> getAllDeviceInventory() {

        Session session = null;
        Transaction tx = null;
        List<DeviceInventoryEntity> entityList = new ArrayList<DeviceInventoryEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeviceInventoryEntity";
            Query query = session.createQuery(hql);
            for (Object result : query.list()) {
                entityList.add((DeviceInventoryEntity) result);
            }

            // Commit transaction.
            this.setFirmwareBasedOnDeployment(entityList);
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all devices in inventory: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all devices: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all devices", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all devices: " + ex);
            }
        }

        return entityList;
    }

    /**
     * Update Device Inventory.
     *
     * @param newDevice the device to update.
     */
    public void updateDeviceInventory(DeviceInventoryEntity newDevice) throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;
        logger.info("DeviceInventoryDAO.updateDeviceInventory for device with refId " + newDevice.getRefId());
        logger.info("DeviceInventoryDAO.updateDeviceInventory for device with ipAddress " + newDevice.getIpAddress());
        logger.info("DeviceInventoryDAO.updateDeviceInventory for device with serviceTag " + newDevice.getServiceTag());

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from DeviceInventoryEntity where refId = :refId";
            Query query = session.createQuery(hql);
            query.setString("refId", newDevice.getRefId());
            DeviceInventoryEntity deviceInventoryEntity = (DeviceInventoryEntity) query.setMaxResults(1).uniqueResult();
            if (deviceInventoryEntity == null) {
                throw new AsmManagerCheckedException(AsmManagerCheckedException.REASON_CODE.RECORD_NOT_FOUND,
                        AsmManagerMessages.notFound(newDevice.getRefId()));
            }

            deviceInventoryEntity.setRefType(newDevice.getRefType());
            deviceInventoryEntity.setDeviceType(newDevice.getDeviceType());
            deviceInventoryEntity.setManagedState(newDevice.getManagedState());
            deviceInventoryEntity.setState(newDevice.getState());
            deviceInventoryEntity.setDisplayName(newDevice.getDisplayName());
            deviceInventoryEntity.setServiceTag(newDevice.getServiceTag());
            deviceInventoryEntity.setIpAddress(newDevice.getIpAddress());
            deviceInventoryEntity.setModel(newDevice.getModel());
            deviceInventoryEntity.setComplianceCheckDate(newDevice.getComplianceCheckDate());
            deviceInventoryEntity.setDiscoveredDate(newDevice.getDiscoveredDate());
            deviceInventoryEntity.setHealth(newDevice.getHealth());
            deviceInventoryEntity.setHealthMessage(newDevice.getHealthMessage());
            deviceInventoryEntity.setInfraTemplateDate(newDevice.getInfraTemplateDate());
            deviceInventoryEntity.setInfraTemplateId(newDevice.getInfraTemplateId());
            deviceInventoryEntity.setIdentityRefId(newDevice.getIdentityRefId());
            deviceInventoryEntity.setInventoryDate(newDevice.getInventoryDate());
            deviceInventoryEntity.setServerTemplateDate(newDevice.getServerTemplateDate());
            deviceInventoryEntity.setServerTemplateId(newDevice.getServerTemplateId());
            deviceInventoryEntity.setConfig(newDevice.getConfig());
            deviceInventoryEntity.setSystemId(newDevice.getSystemId());
            deviceInventoryEntity.setOsImageType(newDevice.getOsImageType());
            deviceInventoryEntity.setOsAdminPassword(newDevice.getOsAdminPassword());
            deviceInventoryEntity.setOsIpAddress(newDevice.getOsIpAddress());
            deviceInventoryEntity.setFailuresCount(newDevice.getFailuresCount());
            deviceInventoryEntity.setCompliant(newDevice.getCompliant());
            deviceInventoryEntity.setChassisId(newDevice.getChassisId());
            deviceInventoryEntity.setUpdatedDate(new GregorianCalendar());
            deviceInventoryEntity.setUpdatedBy(_dao.extractUserFromRequest());
            if (newDevice.getDeviceGroupList() != null && !newDevice.getDeviceGroupList().isEmpty()) {
                deviceInventoryEntity.setDeviceGroupList(newDevice.getDeviceGroupList());
            }

            deviceInventoryEntity.setFacts(newDevice.getFacts());
            session.saveOrUpdate(deviceInventoryEntity);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during update device: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during update device: " + ex);
            }
            if (e instanceof StaleObjectStateException) {
                StaleObjectStateException sose = ((StaleObjectStateException) e);
                logger.warn("StaleObjectStateException for device with refId " + newDevice.getRefId());
                logger.warn("StaleObjectStateException for device with ipAddress " + newDevice.getIpAddress());
                logger.warn("StaleObjectStateException for device with serviceTag " + newDevice.getServiceTag());
            }

            if (e instanceof AsmManagerCheckedException) {
                throw e;
            }
            throw new AsmManagerInternalErrorException("Update device", "DeviceInventoryDAO", e);
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
     * Delete Device Inventory by refId.
     *
     * @param refId the refId used to delete from inventory.
     */
    public void deleteDeviceInventory(String refId) {

        logger.info("Deleting device from inventory: " + refId);
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            String hql = "delete DeviceInventoryEntity where refId = :refId";
            Query query = session.createQuery(hql);
            query.setString("refId", refId);
            query.executeUpdate();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete device: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Delete device", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete device: " + ex);
            }
        }
    }

    /**
     * Delete Device Inventory and its association with Device Group.
     *
     * @param device to be delete from inventory.
     */
    public void deleteDeviceInventory(DeviceInventoryEntity device) {

        logger.info("Deleting device from inventory: " + device.getRefId());
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // String hql = "delete DeviceInventoryEntity where refId = :refId";
            // Query query = session.createQuery(hql);
            // query.setString("refId", refId);
            // query.executeUpdate();
            session.delete(device);
            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during delete device: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during delete device: " + ex);
            }
            throw new AsmManagerInternalErrorException("Delete device", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during delete device: " + ex);
            }
        }
    }
    /*************************************************************************************
     ***************/
    /**
     * Retrieve all from Device Inventory.
     *
     * @return list of entities
     */
    @SuppressWarnings("unchecked")
    public List<DeviceInventoryEntity> getAllDeviceInventory(List<SortParamParser.SortInfo> sortInfos,
                                                             List<FilterParamParser.FilterInfo> filterInfos, PaginationInfo paginationInfo)
            throws AsmManagerDAOException {

        Session session = null;
        Transaction tx = null;
        List<DeviceInventoryEntity> entityList = new ArrayList<DeviceInventoryEntity>();

        try {
            // NOTE[fcarta] need to at this here so we dont nest the session / transaction
            List<String> refIdsServices = ListUtils.EMPTY_LIST;
            List<String> refIdsGroups = ListUtils.EMPTY_LIST;
            List<String> refIdsAllGroups = ListUtils.EMPTY_LIST;

            boolean spFilterSet = false;
            boolean spGlobalFilterSet = false;
            if (CollectionUtils.isNotEmpty(filterInfos)) {
                for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
                    // if there is a service filter then grab the ref id for the device
                    if (StringUtils.equals(SERVICE_FILTER_PARAM, filterInfo.getColumnName())) {
                        refIdsServices = getRefIdsOfDevicesByDeploymentIds(filterInfo.getColumnValue());
                    } else if (StringUtils.equals(SERVERPOOL_FILTER_PARAM, filterInfo.getColumnName())) {
                        if (filterInfo.getColumnValue().contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID)) {
                            refIdsAllGroups = getRefIdsOfDevicesByGroupIds(filterInfo.getColumnValue());
                            spGlobalFilterSet = true;
                        } else {
                            refIdsGroups = getRefIdsOfDevicesByGroupIds(filterInfo.getColumnValue());
                            spFilterSet = true;
                        }
                    }
                }
            }

            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DeviceInventoryEntity.class);
            if (sortInfos != null) {
                BaseDAO.addSortCriteria(criteria, sortInfos);
            }

            if (filterInfos != null) {
                final List<FilterParamParser.FilterInfo> notFound =
                        BaseDAO.addFilterCriteria(criteria, filterInfos, DeviceInventoryEntity.class);
                if (CollectionUtils.isNotEmpty(notFound)) {

                    for (FilterParamParser.FilterInfo filterInfo : notFound) {
                        // if this is a filter by service then we need to get the subset of ref ids for devices
                        // only in the deployment id subset passed
                        if (StringUtils.equals(SERVICE_FILTER_PARAM, filterInfo.getColumnName())) {
                            if (CollectionUtils.isNotEmpty(refIdsServices)) {
                                criteria.add(Restrictions.in("refId", refIdsServices));
                            }
                        } else if (StringUtils.equals(SERVERPOOL_FILTER_PARAM, filterInfo.getColumnName())) {
                            if (spGlobalFilterSet) {
                                if (refIdsAllGroups.isEmpty()) {
                                    // this means there are no servers in any server pool - all are in Global
                                    criteria.add(Restrictions.sqlRestriction("(1=1)"));
                                } else {
                                    criteria.add(Restrictions.not(Restrictions.in("refId", refIdsAllGroups)));
                                }
                            } else if (spFilterSet) {
                                if (refIdsGroups.isEmpty()) {
                                    criteria.add(Restrictions.sqlRestriction("(1=0)"));
                                } else {
                                    criteria.add(Restrictions.in("refId", refIdsGroups));
                                }
                            }
                        } else {
                            criteria.createAlias("deviceInventoryEntity", "deviceInventoryEntityAlias");
                            criteria.add(Restrictions.eq("deviceInventoryEntityAlias.deviceKey", filterInfo.getColumnName()));
                            if (filterInfo.getColumnValue().size() == 1) {
                                criteria.add(Restrictions.eq("deviceInventoryEntityAlias.deviceValue", filterInfo
                                        .getColumnValue().get(0)));
                            } else if (filterInfo.getColumnValue().size() > 1) {
                                criteria.add(Restrictions.in("deviceInventoryEntityAlias.deviceValue",
                                        filterInfo.getColumnValue()));
                            }
                        }
                    }
                }
            }

            if (paginationInfo != null) {
                int offset = paginationInfo.getOffset();
                int limit = paginationInfo.getLimit();
                int pageSize = limit;
                criteria.setFirstResult(offset);
                criteria.setMaxResults(pageSize);
            }

            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            entityList = criteria.list();
            this.setFirmwareBasedOnDeployment(entityList);
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all devices in inventory: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all devices: " + ex);
            }
            throw new AsmManagerDAOException("Caught exception during get all devices in inventory: ", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all devices: " + ex);
            }
        }

        return entityList;
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

            Criteria criteria = session.createCriteria(DeviceInventoryEntity.class);

            final List<FilterParamParser.FilterInfo> notFound =
                    BaseDAO.addFilterCriteria(criteria, filterInfos, DeviceInventoryEntity.class);
            if (CollectionUtils.isNotEmpty(notFound)) {

                for (FilterParamParser.FilterInfo filterInfo : notFound) {
                    // if this is a filter by service then we need to get the subset of ref ids for devices
                    // only in the deployment id subset passed
                    if (StringUtils.equals(SERVICE_FILTER_PARAM, filterInfo.getColumnName())) {
                        final List<String> refIds = getRefIdsOfDevicesByDeploymentIds(filterInfo.getColumnValue());
                        criteria.add(Restrictions.in("refId", refIds));
                    } else if (StringUtils.equals(SERVERPOOL_FILTER_PARAM, filterInfo.getColumnName())) {
                        if (filterInfo.getColumnValue().contains(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID)) {
                            final List<String> refIds = getRefIdsOfDevicesByGroupIds(filterInfo.getColumnValue());
                            if (refIds.isEmpty()) {
                                criteria.add(Restrictions.sqlRestriction("(1=0)"));
                            } else {
                                criteria.add(Restrictions.not(Restrictions.in("refId", refIds)));
                            }
                        } else {
                            final List<String> refIds = getRefIdsOfDevicesByGroupIds(filterInfo.getColumnValue());
                            if (refIds.isEmpty()) {
                                criteria.add(Restrictions.sqlRestriction("(1=0)"));
                            } else {
                                criteria.add(Restrictions.in("refId", refIds));
                            }
                        }
                    } else {
                        criteria.createAlias("deviceInventoryEntity", "deviceInventoryEntityAlias");
                        criteria.add(Restrictions.eq("deviceInventoryEntityAlias.deviceKey", filterInfo.getColumnName()));
                        if (filterInfo.getColumnValue().size() == 1) {
                            criteria.add(Restrictions.eq("deviceInventoryEntityAlias.deviceValue", filterInfo
                                    .getColumnValue().get(0)));
                        } else if (filterInfo.getColumnValue().size() > 1) {
                            criteria.add(Restrictions.in("deviceInventoryEntityAlias.deviceValue",
                                    filterInfo.getColumnValue()));
                        }
                    }
                }
            }

            totalRecords = (long) criteria.setProjection(Projections.rowCount()).uniqueResult();

            tx.commit();
        } catch (Exception e) {
            logger.error("Exception while getting getTotalRecords", e);
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

        return (int) totalRecords;
    }


    /**
     * Helper method to get the device ref Id based on the Service Tag
     *
     * @param serviceTag - a string that is hte service tag of hte device
     * @return String - the Ref Id of the device
     */
    public String getRefIdOfDevice(String serviceTag) {
        logger.info("Retrieving ref Id of Device from inventory with service tag : " + serviceTag);
        Session session = null;
        Transaction tx = null;

        String refIdString = "";

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            String hql = "select refId from DeviceInventoryEntity where serviceTag = :serviceTag";
            Query query = session.createQuery(hql);
            query.setString("serviceTag", serviceTag);
            refIdString = (String) query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get ref ID from device inventory from service tag: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction: " + ex);
            }
            throw new AsmManagerInternalErrorException("Get Ref Id from Service Tag", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during ref Id retrieval: " + ex);
            }
        }

        return refIdString;
    }

    /**
     * Helper method to get the device ref Id based on the Service Tag
     *
     * @param ipAddress - a string that is the ipAddress of the device
     * @return String - the Ref Id of the device
     */
    public String getRefIdOfDeviceByIpAddress(String ipAddress) {
        logger.info("Retrieving ref Id of Device from inventory with ipAddress : " + ipAddress);
        Session session = null;
        Transaction tx = null;

        String refIdString = "";

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            String hql = "select refId from DeviceInventoryEntity where ipAddress = :ipAddress";
            Query query = session.createQuery(hql);
            query.setParameter("ipAddress", ipAddress);
            refIdString = (String) query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get ref ID from device inventory by ipAddress: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction: " + ex);
            }
            throw new AsmManagerInternalErrorException("Get Ref Id by ipAddress", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during ref Id retrieval: " + ex);
            }
        }

        return refIdString;
    }

    @SuppressWarnings("unchecked")
    // NOTE[fcarta] - Its noted that concatenating a sql string leaves the query vulnerable to SQL
    // injection attacks. In this case this method is private and will only be called with trusted
    // UUIDs.
    private List<String> getRefIdsOfDevicesByDeploymentIds(final List<String> deploymentIds) {
        final String deploymentIdsJoined = "'" + StringUtils.join(deploymentIds, "','") + "'";
        logger.info("Retrieving ref Ids of Devices for the given deploymentIds : " + deploymentIdsJoined);
        Session session = null;
        Transaction tx = null;

        List<String> refIds = ListUtils.EMPTY_LIST;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            final String sql =
                    //"select device_id from deployment_to_device_map where deployment_id in ( :deploymentIds )";
                    "select device_id from deployment_to_device_map where deployment_id in (" + deploymentIdsJoined + ")";
            SQLQuery query = session.createSQLQuery(sql);
            //query.setParameter("deploymentIds", deploymentIdsJoined);
            refIds = query.list();
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception getting refIds of deployment ids " + deploymentIdsJoined, e);
            throw new AsmManagerInternalErrorException("Get Ref Ids by Deployment Ids", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during ref Id retrieval: " + ex);
            }
        }

        return refIds;
    }


    @SuppressWarnings("unchecked")
    private List<String> getRefIdsOfDevicesByGroupIds(final List<String> ids) {
        final String idsJoined = "'" + StringUtils.join(ids, "','") + "'";
        Session session = null;
        Transaction tx;

        List<String> refIds = ListUtils.EMPTY_LIST;
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            final String sql;
            if (idsJoined.equals("'" + ServiceTemplateSettingIDs.SERVICE_TEMPLATE_SERVER_POOL_GLOBAL_ID + "'")) {
                sql = "select distinct devices_inventory_seq_id from groups_device_inventory";
            } else {
                sql = "select devices_inventory_seq_id from groups_device_inventory where groups_seq_id in (" + idsJoined + ")";
            }
            SQLQuery query = session.createSQLQuery(sql);
            refIds = query.list();
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception getting refIds of groups ids " + idsJoined, e);
            throw new AsmManagerInternalErrorException("Get Ref Ids by Group Ids", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during ref Id retrieval: " + ex);
            }
        }

        return refIds;
    }

    /**
     * Retrieve all device's ids.
     *
     * @param deviceSeqIds - list of device's ids.
     * @return list of device's ids.
     */
    @SuppressWarnings("unchecked")
    public List<DeviceInventoryEntity> getDevicesByIds(List<String> deviceSeqIds) {
        Session session = null;
        Transaction tx = null;

        List<DeviceInventoryEntity> deviceInvList = new ArrayList<DeviceInventoryEntity>();

        if (null == deviceSeqIds || deviceSeqIds.size() <= 0)
            return deviceInvList;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(DeviceInventoryEntity.class);
            Criterion idRest = Restrictions.in("refId", deviceSeqIds);
            criteria.add(idRest);
            deviceInvList = criteria.list();

            this.setFirmwareBasedOnDeployment(deviceInvList);
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all device by id: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all device by id: " + ex);
            }
            throw new AsmManagerInternalErrorException("Get Device Ids by id", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all device  by id: " + ex);
            }
        }
        return deviceInvList;

    }

    public List<DeviceInventoryEntity> getDevicesByServiceTags(List<String> serviceTags) {
        Session session = null;
        Transaction tx = null;

        List<DeviceInventoryEntity> deviceInvList = new ArrayList<DeviceInventoryEntity>();

        if (null == serviceTags || serviceTags.size() <= 0)
            return deviceInvList;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(DeviceInventoryEntity.class);
            Criterion idRest = Restrictions.in("serviceTag", serviceTags);
            criteria.add(idRest);
            deviceInvList = criteria.list();

            this.setFirmwareBasedOnDeployment(deviceInvList);
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all device by id: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all device by id: " + ex);
            }
            throw new AsmManagerInternalErrorException("Get Device Ids by id", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all device  by id: " + ex);
            }
        }
        return deviceInvList;

    }

    public DeviceLastJobStateEntity createLastJob(DeviceLastJobStateEntity lastJob)
            throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;

        // Save the last job in the db.
        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            logger.info("Creating device last job in inventory: " + lastJob.getDeviceRefId());

            lastJob.setCreatedDate(new GregorianCalendar());
            session.save(lastJob);

            // Commit transaction.
            tx.commit();
        } catch (ConstraintViolationException cve) {
            logger.warn("Caught exception during last job creation: " + cve, cve);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create last job: " + ex);
            }
        } catch (Exception e) {
            logger.warn("Caught exception during last job inventory creation: " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create last job: " + ex);
            }
            throw new AsmManagerInternalErrorException("Create last job", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create last job: " + ex);
            }
        }

        return lastJob;
    }

    public DeviceLastJobStateEntity getLastJob(String deviceRefId, JobType jobType)
            throws AsmManagerCheckedException {
        Session session = null;
        Transaction tx = null;
        DeviceLastJobStateEntity lastJob = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeviceLastJobStateEntity where deviceRefId = :deviceRefId and jobType = :jobType";
            Query query = session.createQuery(hql);
            query.setString("deviceRefId", deviceRefId);
            query.setString("jobType", jobType.name());
            lastJob = (DeviceLastJobStateEntity) query.setMaxResults(1).uniqueResult();

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get last job for refId: " + deviceRefId + ", " + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get last job: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve last job", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get last job: " + ex);
            }
        }

        return lastJob;

    }

    /**
     * Create a new or update existing last job state table based on given device ref id
     * and job type.
     *
     * @param deviceRefId
     * @param jobType
     * @param successful
     * @param description
     */
    public void createOrUpdateLastJob(String deviceRefId, JobType jobType, DeviceState successful,
                                      String description) {
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();
            String hql = "from DeviceLastJobStateEntity where deviceRefId = :deviceRefId and jobType = :jobType";
            Query query = session.createQuery(hql);
            query.setString("deviceRefId", deviceRefId);
            query.setString("jobType", jobType.name());
            DeviceLastJobStateEntity lastJob = (DeviceLastJobStateEntity) query.setMaxResults(1).uniqueResult();
            if (lastJob == null) {
                lastJob = new DeviceLastJobStateEntity();
                lastJob.setDeviceRefId(deviceRefId);
                lastJob.setJobType(jobType);
                lastJob.setCreatedDate(new GregorianCalendar());
            } else {
                lastJob.setUpdatedDate(new GregorianCalendar());
            }
            lastJob.setDescription(description);
            lastJob.setJobState(successful);

            session.saveOrUpdate(lastJob);

            // Commit transaction.
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during create or update last job: ", e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during create or update last job: " + ex);
            }
            throw new AsmManagerInternalErrorException("Create or Update Last Job", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during create or update last job: " + ex);
            }
        }
    }

    /**
     * Set device state to specified state.  If clearOsData is set to true then
     * some additional OS related fields are also set to null when the requested
     * state is READY.
     *
     * @param refId
     * @param state
     * @param clearOsData
     */
    public void setDeviceState(String refId, DeviceState state, boolean clearOsData) {
        DeviceState oldState = null;
        DeviceInventoryEntity theDevice =  null;

        try {
            theDevice =  getDeviceInventory(refId);

            oldState = theDevice.getState();
            if (oldState == state) return;

            // we don't read firmware components and don't need lazy init here
            theDevice.setState(state);
            if (state == DeviceState.READY && clearOsData == true) {
                theDevice.setOsIpAddress(null);
                theDevice.setOsAdminPassword(null);
                theDevice.setOsImageType(null);
            }

            updateDeviceInventory(theDevice);
            logger.debug("Update state to " + state.getValue() + " passed for device " + theDevice.getServiceTag());
        } catch (LazyInitializationException le) {
            // most probably that will never happen but I wanted to be sure we are safe on this call
            logger.warn("LIE in setDeviceState");
        } catch (AsmManagerCheckedException e) {
            // non-critical, do not stop deployment
            logger.error("Update state to " + state.getValue() + " failed for device " + theDevice.getServiceTag() + " . Rolled back to " + oldState.getValue(), e);
            theDevice.setState(oldState);
        }
    }

    /**
     * This overloaded version of setDeviceState will not modify any data other than
     * the requested state change.  If you want the reset behavior you have to explicitly
     * supply the third argument.
     *
     * @param refId
     * @param state
     */
    public void setDeviceState(String refId, DeviceState state) {
        setDeviceState(refId, state, false);
    }

    /**
     * For existing code that was passing the device as the first argument.
     *
     * @param currDevice
     * @param state
     */
    public void setDeviceState(DeviceInventoryEntity currDevice, DeviceState state, boolean clearOsData) {
        if (currDevice == null || state == null) {
            return;
        }
        setDeviceState(currDevice.getRefId(), state, clearOsData);
    }


    /**
     * Retrieve all from Device Inventory with the given device type.
     * @return list of entities
     */
    public List<DeviceInventoryEntity> getAllDeviceInventoryByDeviceType(DeviceType deviceType) {

        Session session = null;
        Transaction tx = null;
        List<DeviceInventoryEntity> entityList = new ArrayList<DeviceInventoryEntity>();

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from DeviceInventoryEntity where deviceType = :deviceType";
            Query query = session.createQuery(hql);
            query.setString("deviceType", deviceType.getValue());
            for (Object result : query.list()) {
                entityList.add((DeviceInventoryEntity) result);
            }
            
            this.setFirmwareBasedOnDeployment(entityList);
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during get all devices by device type in inventory: " + deviceType.getValue(),e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during get all devices: " + ex);
            }
            throw new AsmManagerInternalErrorException("Retrieve all devices for deviceType: " + deviceType.getValue(), "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during get all devices: " + ex);
            }   
        }

        return entityList;
    }
    
    public Set<FirmwareDeviceInventoryEntity> getFirmwareDeviceInventoryByRefId(String deviceRefId) {
        HashSet<FirmwareDeviceInventoryEntity> firmwareDeviceInventory = new HashSet<FirmwareDeviceInventoryEntity>();
        
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "from FirmwareDeviceInventoryEntity where device_inventory = :deviceRefId";
            Query query = session.createQuery(hql);
            query.setString("deviceRefId", deviceRefId);

            for (Object result : query.list()) {
                firmwareDeviceInventory.add((FirmwareDeviceInventoryEntity) result);
            }
            
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during getFirmwareDeviceInventoryByRefId with deviceRefId: " + 
                    deviceRefId + ", "  + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.error("Unable to rollback transaction during getFirmwareDeviceInventoryByRefId with refId: " + 
                        deviceRefId, ex);
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during getFirmwareDeviceInventoryByRefId with refId: " + 
                        deviceRefId + ex);
            }
        }
        
        return firmwareDeviceInventory;
    }
    
    public void deleteFirmwareDeviceInventoryForDevice(String deviceRefId) {
        
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "delete " + FirmwareDeviceInventoryEntity.class.getName() + " where device_inventory = :deviceRefId";
            Query q = session.createQuery(hql).setParameter("deviceRefId", deviceRefId);
            q.executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during deleteFirmwareDeviceInventoryForDevice with deviceRefId: " + 
                    deviceRefId + ", "  + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.error("Unable to rollback transaction during deleteFirmwareDeviceInventoryForDevice with refId: " + 
                        deviceRefId, ex);
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during deleteFirmwareDeviceInventoryForDevice with refId: " + 
                        deviceRefId + ex);
            }
        }        
    }
    
    public void deleteFirmwareDeviceInventoryForDeviceWithOperatingSystem(String deviceRefId) {
        
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "delete " + FirmwareDeviceInventoryEntity.class.getName() + " where device_inventory = :deviceRefId and operating_system IS NOT NULL";
            Query q = session.createQuery(hql).setParameter("deviceRefId", deviceRefId);
            q.executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during deleteFirmwareDeviceInventoryForDevice with deviceRefId: " + 
                    deviceRefId + ", "  + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.error("Unable to rollback transaction during deleteFirmwareDeviceInventoryForDevice with refId: " + 
                        deviceRefId, ex);
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during deleteFirmwareDeviceInventoryForDevice with refId: " + 
                        deviceRefId + ex);
            }
        }        
    }
    
    public void deleteFirmwareDeviceInventoryForDevice(String deviceRefId, SourceType sourceType) {
        
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "delete " + FirmwareDeviceInventoryEntity.class.getName() + " where device_inventory = :deviceRefId and source = :sourceType";
            Query query = session.createQuery(hql);
            query.setString("deviceRefId", deviceRefId);
            query.setString("sourceType", sourceType.getValue());
            query.executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during deleteFirmwareDeviceInventoryForDevice with deviceRefId: " + 
                    deviceRefId + ", "  + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.error("Unable to rollback transaction during deleteFirmwareDeviceInventoryForDevice with refId: " + 
                        deviceRefId, ex);
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during deleteFirmwareDeviceInventoryForDevice with refId: " + 
                        deviceRefId + ex);
            }
        }        
    }    
    
    
    /**
     * This will delete all of the entries in the firmware_deviceinventory table for the given device id where
     * an operating system value is present.  Only software inventory should have an operating_system value set. 
     */
    public void deleteSoftwareDeviceInventoryForDevice(String deviceRefId) {
        
        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            // Create and execute command.
            String hql = "delete " + FirmwareDeviceInventoryEntity.class.getName() + " where device_inventory = :deviceRefId and operating_system IS NOT NULL";
            Query q = session.createQuery(hql).setParameter("deviceRefId", deviceRefId);
            q.executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during deleteFirmwareDeviceInventoryForDevice with deviceRefId: " + 
                    deviceRefId + ", "  + e, e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
                logger.error("Unable to rollback transaction during deleteFirmwareDeviceInventoryForDevice with refId: " + 
                        deviceRefId, ex);
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during deleteFirmwareDeviceInventoryForDevice with refId: " + 
                        deviceRefId + ex);
            }
        }        
    }
    
    
    public FirmwareDeviceInventoryEntity createFirmwareDeviceInventory(FirmwareDeviceInventoryEntity firmDevInvEntity)
            throws AsmManagerCheckedException {

        Session session = null;
        Transaction tx = null;

        try {
            session = _dao._database.getNewSession();
            tx = session.beginTransaction();

            logger.debug("Creating FirmwareDeviceInventoryEntity with ipAddress: " + firmDevInvEntity.getIpaddress() + 
            		" and ComponentName: " + firmDevInvEntity.getName() + 
            		" and Version: " + firmDevInvEntity.getVersion() + 
            		" and Source: " + firmDevInvEntity.getSource() +
            		" and Device refId: " + firmDevInvEntity.getDeviceID());
            
            firmDevInvEntity.setCreatedDate(new GregorianCalendar().getTime());
            firmDevInvEntity.setCreatedBy(_dao.extractUserFromRequest());
            session.save(firmDevInvEntity);
            
            tx.commit();
        } catch (Exception e) {
            logger.warn("Caught exception during device createFirmwareDeviceInventory: " + e);
            try {
                if (tx != null) {
                tx.rollback();
                }
            } catch (Exception ex) {
                logger.warn("Unable to rollback transaction during createFirmwareDeviceInventory: " + ex);
            }
            throw new AsmManagerInternalErrorException("CeateFirmwareDeviceInventory", "DeviceInventoryDAO", e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception ex) {
                logger.warn("Unable to close session during createFirmwareDeviceInventory: " + ex);
            }
        }

        return firmDevInvEntity;
    }    
    

    private void setFirmwareBasedOnDeployment(List<DeviceInventoryEntity> devInvEntList) {
        if (devInvEntList != null && !devInvEntList.isEmpty()) {
            for (DeviceInventoryEntity devInvEntity : devInvEntList) {
                this.setFirmwareBasedOnDeployment(devInvEntity);
            }
        }
    }

    /*
     * Firmware name must be null if the device is part of a deployment that is not managing firmware.  This logic,
     * coupled with the DeviceInventoryDAO's @formula's SQL ensure that the firmware name is set properly based on 
     * these rules:
     * 
     * - If Device is Part of a Service and deployment's manageFirmware = true then return assigned Catalog
     * - If Device is part of a Service and deployment's manageFirmware = false then return null (show Unknown)
     * - If Device is not part of a Service and if default catalog exists then return default catalog
     * - If Device is not part of a Service and there is no default catalog then return embedded catalog 
     * 
     * Due to the limitations of standard SQL in the Formula we were not able to implement all rules, thus this method 
     * ensures 2nd rule above, that returns null, is honored.
     */
    private void setFirmwareBasedOnDeployment(DeviceInventoryEntity devInvEntity) {
        if (devInvEntity != null && devInvEntity.getDeployments() != null && !devInvEntity.getDeployments().isEmpty()) {
            if (devInvEntity.getDeviceType().isSharedDevice() && devInvEntity.getDeviceType().isFirmwareComplianceManaged()) {
                return; // We don't reset the firmware for something like Storage if it's managing firmware
            }
            else if (!devInvEntity.getDeployments().get(0).isManageFirmware() && 
                    !devInvEntity.getDeviceType().isSharedDevice()) {
                // Only device types who we're managing firmware on should have a null returned
                devInvEntity.setFirmwareName(null);
            }
        }
    }
    
}
