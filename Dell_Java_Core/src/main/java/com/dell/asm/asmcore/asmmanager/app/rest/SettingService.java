package com.dell.asm.asmcore.asmmanager.app.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.client.setting.ISettingRepositoryService;
import com.dell.asm.asmcore.asmmanager.client.setting.Setting;
import com.dell.asm.asmcore.asmmanager.db.GenericDAO;
import com.dell.asm.asmcore.asmmanager.db.entity.SettingEntity;
import com.dell.asm.rest.common.exception.LocalizedWebApplicationException;

@Path("/setting")
public class SettingService implements ISettingRepositoryService {
    
    private final static GenericDAO genericDAO = GenericDAO.getInstance();
    private final static Logger logger = Logger.getLogger(SettingService.class);

    @Override
    public Setting create(Setting setting) {
        SettingEntity entity = new SettingEntity(setting);

        try {
            entity = genericDAO.create(entity);
        } catch (Exception e) {
            logger.error("Exception while creating setting" + setting.getName(), e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
        
        return (entity != null) ? entity.getSetting() : null;
    }

    @Override
    public Setting update(Setting setting, String id) {

        logger.debug("Update setting for: " + setting.getName());
        SettingEntity entity = null;
        try {
            // Validation logic for the file is included in the createEntity
            entity = new SettingEntity(setting);

            genericDAO.update(entity);
        } catch (LocalizedWebApplicationException e) {
            logger.error("LocalizedWebApplicationException while updating setting " + setting.getName(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Exception while updating setting " + setting.getName(), e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }

        logger.debug("Update setting done for: " + setting.getName() + ". ID = " + setting.getId());
        
        return (entity != null) ? entity.getSetting() : null;
    }

    @Override
    public Setting get(String name) {
        logger.debug("Getting setting for name: " + name);

        final SettingEntity setting = genericDAO.getByName(name, SettingEntity.class);

        return (setting != null) ? setting.getSetting() : null;
    }
    
    @Override
    public void delete(final String id) {
        try {
            genericDAO.delete(id, SettingEntity.class);
        } catch (Exception e) {
            logger.error("Exception while deleting setting with id " + id, e);
            throw new LocalizedWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR,
                    AsmManagerMessages.internalError());
        }
    }
}
