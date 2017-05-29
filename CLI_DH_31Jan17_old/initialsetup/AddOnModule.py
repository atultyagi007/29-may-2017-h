'''
Created on Aug 24, 2014

@author: dheeraj_si
'''

from copy import deepcopy
from xml.etree import ElementTree as et
import requests
import time
import json
import datetime
import globalVars
from utilityModule import UtilBase
from cookielib import logger
import xml.etree.ElementTree as ET
import chassisConfigParam


class AddOnModuleTestBase(UtilBase):
    '''
    classdocs
    '''

    def __init__(self):
        '''
        Constructor
        '''
        UtilBase.__init__(self)
        
        
    def setupCredentials(self):
        """
        Create Credentials or Manage existing Credentials        
        """
        logger = self.getLoggerInstance()
        credentialConfig, status = self.loadCredentialInputs()
        if not status:
            return credentialConfig, False
        resCRE, statCRE = self.getResponse("GET", "Credential")
        creResponse = []
        for credential in credentialConfig:
            found = False
            if not statCRE and "No information found" in resCRE:
                found = False
            else:
                found = [creList["credential"]["id"] for creList in resCRE["credentialList"] if creList["credential"]["label"] == credential["Name"]]
            if not found:
                action = "POST"
                creId = ""
            else:
                action = "PUT"
                creId = found[0]
            resDC, statDC = self.defineCredential(credential, creId, action)
            creResponse.append(str(resDC))          
            if not statDC:
                return creResponse, False
            if not found: creId = resDC["credential"]["id"]
            self.credentialMap[credential["Name"]] = creId
        logger.info(' credentialMap : ') 
        logger.debug(self.credentialMap)
        return creResponse, True
    
    def getAddOnModule(self):
        """
        Get list of  add on module         
        """
        logger = self.getLoggerInstance()
        resCRE, statCRE = self.getResponse("GET", "AddOnModule")
        
        logger.info('Add On Module : ') 
        logger.debug(resCRE)
        return resCRE, True
    
    def createAddOnModule(self):
        """
        Get list of  add on module         
        """
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.addOnModulePayload)
        payload = payload.replace("$uploadUrl", globalVars.aDDOnModulePath)
        response,status = self.getResponse("POST", "AddOnModule", payload)
        if status:
            logger.info('Add On Module created successfully: ')
            logger.debug(response)
            self.log_data('Add On Module created successfully:')
            self.log_data('Payload:')
            self.log_data(payload)
            return response,True
        else:
            "Please check input value add on module not created",False
    
    def getAddOnModuleById(self,refId=""):
        """
        Get list of  add on module         
        """
        logger = self.getLoggerInstance()
        response,status = self.getResponse("GET", "AddOnModule", refId=refId)
        if status:
            logger.info('Add On got successfully bye refId: ')
            logger.debug(response)
            self.log_data('Add On Module get successfully:')
            self.log_data('Payload:')
            self.log_data(response)
            return response,True
        else:
            "Please check URI not able to get add on module info",False        
    
    