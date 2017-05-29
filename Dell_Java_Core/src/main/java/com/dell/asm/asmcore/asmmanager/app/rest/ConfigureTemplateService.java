/**************************************************************************
 * Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 * *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import com.dell.asm.alcm.client.model.DatabaseBackupSettings;
import com.dell.asm.alcm.client.service.IDatabaseService;
import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.ConfigureTemplateOption;
import com.dell.asm.asmcore.asmmanager.client.configuretemplate.IConfigureTemplateService;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplate;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateSettingIDs;
import com.dell.asm.asmcore.asmmanager.client.servicetemplate.ServiceTemplateUploadRequest;
import com.dell.asm.asmcore.asmmanager.db.DeviceGroupDAO;
import com.dell.asm.asmcore.asmmanager.db.FirmwareRepositoryDAO;
import com.dell.asm.asmcore.asmmanager.db.ServiceTemplateDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceGroupEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.DeviceInventoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.FirmwareRepositoryEntity;
import com.dell.asm.asmcore.asmmanager.db.entity.ServiceTemplateEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.util.ProxyUtil;
import com.dell.asm.asmcore.asmmanager.util.ServiceTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.configuretemplate.ConfigureTemplateUtil;
import com.dell.asm.asmcore.asmmanager.util.osrepository.OSRepositoryUtil;
import com.dell.asm.asmcore.asmmanager.util.razor.RazorRepo;
import com.dell.asm.asmcore.user.model.IUserResource;
import com.dell.asm.asmcore.user.model.User;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;
import com.dell.pg.asm.identitypool.api.common.model.NetworkType;
import com.dell.pg.asm.identitypool.api.network.INetworkService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@Path("/ConfigureTemplate")
public class ConfigureTemplateService implements IConfigureTemplateService {

    private static final Logger logger = Logger.getLogger(ConfigureTemplateService.class);

    private ServiceTemplateDAO serviceTemplateDAO;

    private FirmwareRepositoryDAO firmwareRepositoryDAO;

    private DeviceGroupDAO deviceGroupDAO;

    private IUserResource userResourceProxy;

    private IDatabaseService alcmDatabaseService;

    private INetworkService networkService;

    private OSRepositoryUtil osRepositoryUtil;

    private ServiceTemplateUtil serviceTemplateUtil;

    @Context
    private HttpServletRequest servletRequest;

    public ConfigureTemplateService() {
    }

    @Override
    public ServiceTemplate uploadTemplate(ServiceTemplateUploadRequest uploadRequest) {
        ServiceTemplate svc = null;
        try {

            if (uploadRequest.getFileData() == null) {
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.noTemplateFile());
            }

            // encrypt
            if (uploadRequest.isUseEncPwdFromBackup()) {
                DatabaseBackupSettings databaseBackupSettings = getAlcmDatabaseService().getDatabaseBackupSettings();
                String encPassword = databaseBackupSettings.getEncryptionPassword();
                if (encPassword == null) {
                    throw new LocalizedWebApplicationException(
                            Response.Status.BAD_REQUEST,
                            AsmManagerMessages.noBackupPassword());
                }
                uploadRequest.setEncryptionPassword(encPassword);
            }

            try {
                svc = ServiceTemplateUtil.importTemplate(uploadRequest.getFileData(), uploadRequest.getEncryptionPassword());
            } catch (Exception e) {
                logger.error(e);
                throw new LocalizedWebApplicationException(
                        Response.Status.BAD_REQUEST,
                        AsmManagerMessages.badTemplateFile(e.getMessage()));
            }

            if (svc != null) {
                if (svc.getId() != null) {
                    ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateDAO().getTemplateById(svc.getId());
                    if (serviceTemplateEntity != null) {
                        throw new LocalizedWebApplicationException(
                                Response.Status.BAD_REQUEST,
                                AsmManagerMessages.uploadedServiceTemplateExists());
                    }
                }

                if (uploadRequest.getDescription() != null)
                    svc.setTemplateDescription(uploadRequest.getDescription());
                if (uploadRequest.getCategory() != null && uploadRequest.getCategory().length() > 0)
                    svc.setCategory(uploadRequest.getCategory());
                if (uploadRequest.getTemplateName() != null)
                    svc.setTemplateName(uploadRequest.getTemplateName());

                svc.setManageFirmware(uploadRequest.isManageFirmware());
                if (uploadRequest.isManageFirmware()) {
                    if (uploadRequest.isUseDefaultCatalog()) {
                        svc.setUseDefaultCatalog(true);
                        svc.setFirmwareRepository(null);
                    } else if (StringUtils.isNotBlank(uploadRequest.getFirmwarePackageId())) {
                        FirmwareRepositoryEntity firmwareRepositoryEntity = getFirmwareRepositoryDAO().get(uploadRequest.getFirmwarePackageId());
                        if (firmwareRepositoryEntity != null) {
                            svc.setFirmwareRepository(firmwareRepositoryEntity.getSimpleFirmwareRepository());
                        }
                    }
                }
                if (uploadRequest.isManagePermissions()) {

                    svc.setAllUsersAllowed(uploadRequest.isAllStandardUsers());
                    Set<User> users = new HashSet<>();
                    svc.setAssignedUsers(users);

                    ProxyUtil.setProxyHeaders(getUserResourceProxy(), getServletRequest());

                    List<String> fUser = new ArrayList<>();
                    if (uploadRequest != null && uploadRequest.getAssignedUsers() != null) {
                        for (String userName : uploadRequest.getAssignedUsers()) {
                            fUser.clear();
                            fUser.add("eq,userName," + userName);
                            User[] lUsers = getUserResourceProxy().getUsers(fUser, null, null, null);
                            if (lUsers != null && lUsers.length == 1) {
                                users.add(lUsers[0]);
                            }
                        }
                    }
                }

                addServiceTemplateConfiguration(svc);

                svc.setTemplateName(null);
                svc.setTemplateDescription(null);
                svc.setCategory(null);
                svc.setDraft(true);
                svc.setTemplateLocked(true);
                svc.setInConfiguration(true);

                ServiceTemplateUtil.stripPasswords(svc, ServiceTemplateSettingIDs.SERVICE_TEMPLATE_PASSWORD_DEFAULT_TO_REMOVE);
            }

        } catch (LocalizedWebApplicationException e) {
            logger.error(
                    "LocalizedWebApplicationException while importing service template", e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while importing service template", e);
            throw new LocalizedWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        return svc;
    }

    /**
     * Parses the ServiceTemplate and adds a ServiceTemplateComponent to the service template in the
     * configuration field for configuration purposes.
     *
     * @param serviceTemplate service template to parse and add configuration
     */
    public void addServiceTemplateConfiguration(ServiceTemplate serviceTemplate) {
        if (serviceTemplate != null) {

            serviceTemplate.setTemplateType(ServiceTemplateSettingIDs.SERVICE_TEMPLATE_TYPE_VALIDATED_SYSTEMS);

            // Get networks for this appliance
            Map<NetworkType, Set<ConfigureTemplateOption>> networkTypesMap = ConfigureTemplateUtil.getNetworksMap(getNetworkService());

            //Get OS Images and RepoNames
            final List<RazorRepo> razorImages = getOsRepositoryUtil().getRazorOSImages(false);
            final Map<String, String> repoNames = getOsRepositoryUtil().mapRazorRepoNamesToAsmRepoNames();

            //Get vcenter and hyperv entities
            final List<DeviceInventoryEntity> vCenterEntities = getServiceTemplateUtil().getVCenterEntities(false);
            final List<DeviceInventoryEntity> hypervEntities = getServiceTemplateUtil().getHypervEntities();

            // get server pools
            List<DeviceGroupEntity> deviceGroups = null;
            try {
                deviceGroups = getDeviceGroupDAO().getAllDeviceGroup(null, null, null);
            } catch (AsmManagerCheckedException e) {
                logger.error("Unable to get device groups", e);
            }

            // get Storage Devices
            final Map<String, List<DeviceInventoryEntity>> storageDeviceMap = getServiceTemplateUtil().getStorageDevicesMap();

            ConfigureTemplateUtil.parseServiceTemplate(serviceTemplate,
                                                       networkTypesMap,
                                                       razorImages,
                                                       repoNames,
                                                       vCenterEntities,
                                                       hypervEntities,
                                                       deviceGroups,
                                                       storageDeviceMap);
        }
    }

    public ServiceTemplateDAO getServiceTemplateDAO() {
        if (serviceTemplateDAO == null) {
            serviceTemplateDAO = ServiceTemplateDAO.getInstance();
        }
        return serviceTemplateDAO;
    }

    public void setServiceTemplateDAO(ServiceTemplateDAO serviceTemplateDAO) {
        this.serviceTemplateDAO = serviceTemplateDAO;
    }

    public FirmwareRepositoryDAO getFirmwareRepositoryDAO() {
        if (firmwareRepositoryDAO == null) {
            firmwareRepositoryDAO = FirmwareRepositoryDAO.getInstance();
        }
        return firmwareRepositoryDAO;
    }

    public void setFirmwareRepositoryDAO(FirmwareRepositoryDAO firmwareRepositoryDAO) {
        this.firmwareRepositoryDAO = firmwareRepositoryDAO;
    }

    public DeviceGroupDAO getDeviceGroupDAO() {
        if (deviceGroupDAO == null) {
            deviceGroupDAO = DeviceGroupDAO.getInstance();
        }
        return deviceGroupDAO;
    }

    public void setDeviceGroupDAO(DeviceGroupDAO deviceGroupDAO) {
        this.deviceGroupDAO = deviceGroupDAO;
    }

    public IUserResource getUserResourceProxy() {
        if (userResourceProxy == null) {
            userResourceProxy = ProxyUtil.getAdminProxy();
        }
        return userResourceProxy;
    }

    public void setUserResourceProxy(IUserResource userResourceProxy) {
        this.userResourceProxy = userResourceProxy;
    }

    public IDatabaseService getAlcmDatabaseService() {
        if (alcmDatabaseService == null) {
            alcmDatabaseService = ProxyUtil.getAlcmDBProxy();
        }
        return alcmDatabaseService;
    }

    public void setAlcmDatabaseService(IDatabaseService alcmDatabaseService) {
        this.alcmDatabaseService = alcmDatabaseService;
    }

    public INetworkService getNetworkService() {
        if (networkService == null) {
            networkService = ProxyUtil.getNetworkProxy();
        }
        return networkService;
    }

    public void setNetworkService(INetworkService networkService) {
        this.networkService = networkService;
    }

    public OSRepositoryUtil getOsRepositoryUtil() {
        if (osRepositoryUtil == null) {
            osRepositoryUtil = new OSRepositoryUtil();
        }
        return osRepositoryUtil;
    }

    public void setOsRepositoryUtil(OSRepositoryUtil osRepositoryUtil) {
        this.osRepositoryUtil = osRepositoryUtil;
    }

    public ServiceTemplateUtil getServiceTemplateUtil() {
        if (serviceTemplateUtil == null) {
            serviceTemplateUtil = new ServiceTemplateUtil();
        }
        return serviceTemplateUtil;
    }

    public void setServiceTemplateUtil(ServiceTemplateUtil serviceTemplateUtil) {
        this.serviceTemplateUtil = serviceTemplateUtil;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}
