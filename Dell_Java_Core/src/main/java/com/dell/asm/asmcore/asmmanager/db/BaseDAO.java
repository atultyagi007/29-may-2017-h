/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.dell.asm.asmcore.asmmanager.client.deviceinventory.ManagedState;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.dell.asm.asmcore.asmmanager.client.applyMgtTemplate.ConfigureStatus;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.CompliantState;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceHealth;
import com.dell.asm.asmcore.asmmanager.client.deviceinventory.DeviceState;
import com.dell.asm.asmcore.asmmanager.client.discovery.DeviceType;
import com.dell.asm.asmcore.asmmanager.client.discovery.DiscoveryStatus;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.AddOnModuleOperatingSystemVersionEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentNamesRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeploymentUserRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceConfigureEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceDiscoverEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryComplianceEntity.DeviceInventoryComplianceId;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceLastJobStateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DiscoveryResultEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareDeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.MapRazorNodeNameToSerialNumberEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.OSRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.OperatingSystemVersionEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.PolicyRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SettingEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareBundleEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SoftwareComponentEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.SystemIDEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateUserRefEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.VMRefEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.SortParamParser;
import com.dell.asm.rest.common.util.StringUtils;
import com.dell.pg.orion.common.context.ServiceContext;
import com.dell.pg.orion.common.utilities.ConfigurationUtils;
import com.dell.pg.orion.databasemanager.DatabaseFactory;
import com.dell.pg.orion.databasemanager.IDatabase;
import com.dell.pg.orion.databasemanager.ResourcePathDBDefs;
import com.dell.pg.orion.databasemanager.exception.OrionDatabaseException;
import com.dell.pg.orion.databasemanager.session.IOrionHibernate;

public class BaseDAO {
	private static final Logger LOGGER = Logger.getLogger(BaseDAO.class);

	static final String CONFIGURATION_FILE = "asm_manager.db.properties";

	private static BaseDAO _instance;

	IDatabase _database;

	protected BaseDAO() throws OrionDatabaseException {
		init();
	}
	
	private synchronized void init() throws OrionDatabaseException {
		try {
		        LOGGER.info( "Initializing BaseDAO");
			// Gets the database instance
			URL dbPropFile = BaseDAO.class.getClassLoader().getResource(
					CONFIGURATION_FILE);
			Properties properties = ConfigurationUtils
					.readProperties(dbPropFile);
			ResourcePathDBDefs dbDefs = new ResourcePathDBDefs(this.getClass()
					.getClassLoader(), "schema/asm_manager");
            LOGGER.info( "Connection to: " +  properties.getProperty("hibernate.connection.url"));
			this._database = DatabaseFactory.createDatabaseInstance(properties,
					dbDefs, getEntities());
		} catch (IOException e) {
			throw new OrionDatabaseException("Unable to read "
					+ CONFIGURATION_FILE, e);
		}
	}

    public static synchronized BaseDAO getInstance() {
        try {
            // Create the singleton if necessary.
            if (_instance == null) {
                _instance = new BaseDAO();
            }
            return _instance;
        } catch (Exception e) {   // includs OrionDatabaseException
            LOGGER.error("Failed to initialize BaseDAO", e);
            throw new AsmManagerInternalErrorException("Initialize", "BaseDAO", e);
        }
    }

    protected Set<Class<?>> getEntities() {
        Set<Class<?>> entities = new HashSet<>();
        entities.add(DeviceInventoryEntity.class);
        entities.add(DeviceLastJobStateEntity.class);
        entities.add(DiscoveryResultEntity.class);
        entities.add(TemplateEntity.class);
        entities.add(ServiceTemplateEntity.class);
        entities.add(PolicyRefEntity.class);
        entities.add(DeviceGroupEntity.class);
        entities.add(DeviceConfigureEntity.class);
        entities.add(DeviceDiscoverEntity.class);
        entities.add(MapRazorNodeNameToSerialNumberEntity.class);
        entities.add(DeploymentEntity.class);
        entities.add(VMRefEntity.class);
        entities.add(SoftwareComponentEntity.class);
        entities.add(SoftwareBundleEntity.class);
        entities.add(FirmwareRepositoryEntity.class);
        entities.add(SettingEntity.class);
        entities.add(DeploymentUserRefEntity.class);
        entities.add(TemplateUserRefEntity.class);
        entities.add(FirmwareDeviceInventoryEntity.class);
        entities.add(OSRepositoryEntity.class);
        entities.add(SystemIDEntity.class);
        entities.add(DeviceInventoryComplianceEntity.class);
        entities.add(DeviceInventoryComplianceId.class);
        entities.add(AddOnModuleEntity.class);
        entities.add(AddOnModuleComponentEntity.class);
        entities.add(OperatingSystemVersionEntity.class);
        entities.add(AddOnModuleOperatingSystemVersionEntity.class);
        entities.add(DeploymentNamesRefEntity.class);
        return entities;
    }

	protected interface CallableWithSession<T> {
		T run(Session session);

		T failed(SQLException e) throws SQLException;
	}

	protected static abstract class AbstractCallableWithSession<T> implements
			CallableWithSession<T> {
		@Override
		public abstract T run(Session session);

		@Override
		public T failed(SQLException e) throws SQLException {
			throw e;
		}
	}

	protected interface RunnableWithSession {
		void run(Session session);

		void failed(SQLException e) throws SQLException;
	}

	protected static abstract class AbstractRunnableWithSession implements
			RunnableWithSession {
		@Override
		public abstract void run(Session session);

		@Override
		public void failed(SQLException e) throws SQLException {
			throw e;
		}
	}

	protected void doWithSession(final RunnableWithSession runnable)
			throws SQLException {
		doWithSession(new CallableWithSession<Void>() {
			@Override
			public Void run(Session session) {
				runnable.run(session);
				return null;
			}

			@Override
			public Void failed(SQLException e) throws SQLException {
				runnable.failed(e);
				throw e;
			}
		});
	}

	protected <T> T doWithSession(CallableWithSession<T> runnable)
			throws SQLException {
		IOrionHibernate orionHibernate = this._database.getOrionHibernate();
		try {
			Session session = null;
			Transaction transaction = null;
			try {
				// Gets the session
				session = orionHibernate.getNewSession();
				if (session == null)
					return null;
				else {
					// Gets the transaction
					transaction = session.getTransaction();
					transaction.begin();

					T ret = runnable.run(session);

					// Commit the transaction
					transaction.commit();
					return ret;
				}
			} catch (Exception e) {
				// Log error
				if (transaction != null) {
					// Rollback in case of any error.
					transaction.rollback();
				}
				LOGGER.error(e);
				throw new SQLException(e);
			} finally {
				if (session != null) {
					session.close();
				}
			}
		} catch (SQLException e) {
			runnable.failed(e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	<T> List<T> checkAndCast(List<?> all, Class<T> klazz, String errorMessage) {
		for (Object o : all) {
			if (!klazz.isAssignableFrom(o.getClass()))
				throw new IllegalArgumentException(errorMessage);
		}
		return (List<T>) all;
	}
	
    /**
     * Helper method for adding filter criteria.
     * 
     * @param criteria
     *            the filter criteria
     * @param filterInfos
     *            list for filter specifications. filter info are added in the order in the list.
     */
    @SuppressWarnings("unchecked")
    static <E extends Enum<E>> List<FilterParamParser.FilterInfo> addFilterCriteria(Criteria criteria, List<FilterParamParser.FilterInfo> filterInfos,
            Class persistentClass) {

        LinkedList<FilterParamParser.FilterInfo> notFound = new LinkedList<FilterParamParser.FilterInfo>();

        ArrayList<Class<?>> enumClasses = new ArrayList<>();
        enumClasses.add(DiscoveryStatus.class);
        enumClasses.add(DeviceType.class);
        enumClasses.add(ManagedState.class);
        enumClasses.add(DeviceState.class);
        enumClasses.add(CompliantState.class);
        enumClasses.add(ConfigureStatus.class);
        
        for (FilterParamParser.FilterInfo filterInfo : filterInfos) {
            List<?> values = filterInfo.getColumnValue();

            //
            // Cast strings to the property type
            //
            try {
                Method m = null;
                // can be getXXX or isXXX
                try {
                    m = persistentClass.getMethod("get" + StringUtils.capitalize(filterInfo.getColumnName()));
                }catch(NoSuchMethodException nm) {
                    m = persistentClass.getMethod("is" + StringUtils.capitalize(filterInfo.getColumnName()));
                }
                Class<?> typeClass = m.getReturnType();

                if (!typeClass.isAssignableFrom(String.class)) {

                    // byte/short/int/long
                    if (typeClass == byte.class || typeClass == short.class || typeClass == int.class || typeClass == long.class) {
                        LinkedList<Long> castedValues = new LinkedList<Long>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                            castedValues.add(Long.valueOf(stringValue));
                        }
                        // Set casted values
                        values = castedValues;
                        // float/double
                    } else if (typeClass == float.class || typeClass == double.class) {
                        LinkedList<Double> castedValues = new LinkedList<Double>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                            castedValues.add(Double.valueOf(stringValue));
                        }
                        // Set casted values
                        values = castedValues;
                        // boolean
                    } else if (typeClass == boolean.class) {
                        LinkedList<Boolean> castedValues = new LinkedList<Boolean>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                            castedValues.add(Boolean.valueOf(stringValue));
                        }
                        // Set casted values
                        values = castedValues;
                        // char
                    } else if (typeClass == char.class) {
                        LinkedList<Character> castedValues = new LinkedList<Character>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                            castedValues.add(Character.valueOf(stringValue.charAt(0)));
                        }
                        // Set casted values
                        values = castedValues;
                    } else if (typeClass == DiscoveryStatus.class) {
                        LinkedList<E> castedValues = new LinkedList<E>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                        	castedValues.addAll((Collection<? extends E>) findDeviceTypeSequence(stringValue, DiscoveryStatus.class));
                        }
                        values = castedValues;
                    } else if (typeClass == DeviceType.class) {              	
                        LinkedList<E> castedValues = new LinkedList<E>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                        	castedValues.addAll((Collection<? extends E>) findDeviceTypeSequence(stringValue, DeviceType.class));
                        }
                        values = castedValues;
                    } else if (typeClass == ManagedState.class) {
                        // ManagedState factored out of DeviceState as of 8.3.1
                        LinkedList<E> castedValues = new LinkedList<E>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                            castedValues.addAll((Collection<? extends E>) findDeviceTypeSequence(stringValue, ManagedState.class));
                        }
                        values = castedValues;
                    } else if (typeClass == DeviceState.class) {
                        // ManagedState factored out of DeviceState as of 8.3.1
                        LinkedList<E> castedValues = new LinkedList<E>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                            castedValues.addAll((Collection<? extends E>) findDeviceTypeSequence(stringValue, DeviceState.class));
                        }
                        values = castedValues;
                    } else if (typeClass == CompliantState.class) {
                        LinkedList<E> castedValues = new LinkedList<E>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                        	castedValues.addAll((Collection<? extends E>) findDeviceTypeSequence(stringValue, CompliantState.class));
                        }
                        values = castedValues;
                    } else if (typeClass == ConfigureStatus.class) {
                        LinkedList<E> castedValues = new LinkedList<E>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                            castedValues.addAll((Collection<? extends E>) findDeviceTypeSequence(stringValue, ConfigureStatus.class));
                        }
                        values = castedValues;
                    } else if (typeClass == DeviceHealth.class) {
                        LinkedList<E> castedValues = new LinkedList<E>();
                        for (String stringValue : filterInfo.getColumnValue()) {
                            castedValues.addAll((Collection<? extends E>) findDeviceTypeSequence(stringValue, DeviceHealth.class));
                        }
                        values = castedValues;
                    }else{
                        continue;
                    }
                }


	            //
	            // Translate filters to Hibernate Criteria
	            //
                if(enumClasses.contains(typeClass) ){
                	criteria.add(Restrictions.in(filterInfo.getColumnName(), values));	
                }else{
		            if (values.size() == 0) {
		                continue;
		            } else if (values.size() > 1) {
		                if (FilterParamParser.FilterOperator.EQUAL.equals(filterInfo.getFilterOperator())) {
		                    criteria.add(Restrictions.in(filterInfo.getColumnName(), values));
		                } else{
		                	throw new IllegalArgumentException("filter operation '" + filterInfo.getFilterOperator() + "' is not recognized.");
		                }
		            } else {
		            	
		                if (FilterParamParser.FilterOperator.EQUAL.equals(filterInfo.getFilterOperator())) {
		                    criteria.add(Restrictions.eq(filterInfo.getColumnName(), values.get(0)));
		                } else if (FilterParamParser.FilterOperator.CONTAIN.equals(filterInfo.getFilterOperator())) {
		
		                    // Escape '_', '%', and '\' for Hibernate.
		                    String escapedString = values.get(0).toString();
		                    escapedString = escapedString.replace("\\", "\\\\").replace("_", "\\_").replace("%", "\\%");
		                    
		                    criteria.add(Restrictions.like(filterInfo.getColumnName(), escapedString, MatchMode.ANYWHERE));
		                } else {
		                    throw new IllegalArgumentException("filter operation '" + filterInfo.getFilterOperator() + "' is not recognized.");
		                }
		            }
                }
	            
            } catch (NoSuchMethodException e) {
                LOGGER.info("cannot find a method for " + filterInfo.getColumnName() + " in " + persistentClass.toString());
                notFound.add(filterInfo);
                continue;
            }
        }

        return notFound;
    }

    /**
     * Helper method for adding sorting criteria.
     * 
     * @param criteria
     *            the query criteria
     * @param sortInfos
     *            list for sort specifications. Sort info are added in the order in the list.
     */
    static void addSortCriteria(Criteria criteria, List<SortParamParser.SortInfo> sortInfos) {

        for (SortParamParser.SortInfo sortInfo : sortInfos) {

            if (SortParamParser.SortOrder.DESC.equals(sortInfo.getSortOrder())) {
                criteria.addOrder(Order.desc(sortInfo.getColumnName()));
            } else {
                criteria.addOrder(Order.asc(sortInfo.getColumnName()));
            }
        }
    }
    
    public String extractUserFromRequest(){
    	String user = com.dell.asm.usermanager.DBInit.SYSTEM_USER;
    	ServiceContext.Context sc = ServiceContext.get();
    	if(sc != null && sc.getUserName() != null){
    		user = sc.getUserName();
    	}
       	return user;
       	
    }
    
    
    public static <E extends Enum<E>> LinkedList<E> findDeviceTypeSequence(String sequence, Class<E> enumType){

    	LinkedList<E> columnCriteriaList = new LinkedList<E>();
    	
    	for(E enumData : enumType.getEnumConstants()){
    		if(enumData.name().toLowerCase().equalsIgnoreCase(sequence.toLowerCase()))
    			columnCriteriaList.add(enumData);
    	}

    	return columnCriteriaList;
    }

    public static <E extends Enum<E>> LinkedList<E> findAllDeviceTypeSequence(Set<E> sequence, Class<E> enumType){

        LinkedList<E> columnCriteriaList = new LinkedList<E>();

        for(E enumData : enumType.getEnumConstants()){
            if(sequence.contains(enumData)) {
                columnCriteriaList.add(enumData);
            }
        }

        return columnCriteriaList;
    }

    public static <E extends Enum<E>> LinkedList<E> findAllDeviceTypeExceptSequence(Set<E> sequence, Class<E> enumType){

        LinkedList<E> columnCriteriaList = new LinkedList<E>();

        for(E enumData : enumType.getEnumConstants()){
            if(!sequence.contains(enumData)) {
                columnCriteriaList.add(enumData);
            }
        }

        return columnCriteriaList;
    }
}
