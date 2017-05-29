/**************************************************************************
 *   Copyright (c) 2017 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.db;

import com.dell.asm.asmcore.asmmanager.AsmManagerMessages;
import com.dell.asm.asmcore.asmmanager.db.entity.TemplateEntity;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerCheckedException;
import com.dell.asm.asmcore.asmmanager.exception.AsmManagerInternalErrorException;
import com.dell.asm.rest.common.util.FilterParamParser;
import com.dell.asm.rest.common.util.PaginationParamParser.PaginationInfo;
import com.dell.asm.rest.common.util.SortParamParser;
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

public class ConfigureTemplateDAO {

    // Logger.
    private static final Logger logger = Logger.getLogger(ConfigureTemplateDAO.class);

    // DB access.
    private BaseDAO baseDAO;

    // Singleton instance.
    private static ConfigureTemplateDAO instance;

    private ConfigureTemplateDAO() {
    }

    public static synchronized ConfigureTemplateDAO getInstance() {
        if (instance == null)
            instance = new ConfigureTemplateDAO();
        return instance;
    }

    public BaseDAO getBaseDAO() {
        if (baseDAO == null) {
            baseDAO = BaseDAO.getInstance();
        }
        return baseDAO;
    }

    public void setBaseDAO(BaseDAO baseDAO) {
        this.baseDAO = baseDAO;
    }
}
