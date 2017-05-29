'''
Created on Aug 19, 2014

@author: dheeraj.singh
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from utilityModule import UtilBase
import json
import logging
import logging.config
import time
from logging import Logger, getLogger
from cookielib import logger
import globalVars

class Testcase(UtilBase):
   
   
    tc_Id = ""
    def __init__(self):
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def test_cleaneServerPool(self):
        
        response = self.cleanServerPool()
        logger = self.getLoggerInstance()
        logger.debug('Cleane Server Pool Response is')
        logger.info(response)
        
    def test_cleanDiscoveryDevice(self):
        response = self.cleanDiscovery()
        logger = self.getLoggerInstance()
        logger.debug('Cleane descovery Devices Response is')
        logger.info(response)
        
    
    def test_cleanManagedDevice(self):
        response = self.cleanManagedDevice()
        logger = self.getLoggerInstance()
        logger.debug('Cleane ManagedDevice Devices Response is')
        logger.info(response)
        

    def test_cleanNetwork(self):
        response = self.cleanNetwork()
        logger = self.getLoggerInstance()
        logger.debug('Cleane Network Response is')
        logger.info(response)

    def test_cleanCredential(self):
        response = self.cleanCredential()
        logger = self.getLoggerInstance()
        logger.debug('Cleane Credential Response is')
        logger.info(response)
        
    def test_cleanDeployedTemplates(self):
        
        response = self.cleanUpServices()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))
        
    def test_cleanePublishedTemplates(self):
        
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))
        
    
    def cleanUpRepository(self):
        response,status = self.getResponse("GET", "osRepository")
        if "No information found" in response:
            print " No repository found"
        else:
            try:
                for repos in response:
                    resDel,statDel = self.getResponse("DELETE", "osRepository", refId=str(repos["id"]))
                    if statDel:
                        self.log_TestData(["osRepository Teardown",'Success',"osRepository Teardown Successfully with RefID : %s"%str(repos["id"])])
                    else:
                        if resDel == '400':
                            print resDel
                        else:
                            print resDel
                            self.log_TestData(["",'Failed',"Failed to Teardown osRepository with RefID : %s"%str(repos["id"])])
        
            except Exception as e5:
                self.log_data("No repository found to Remove")
                self.log_data(str(e5))
                print " unable to remove repository " 
        
    
 
        
    def doTearDown(self):
        self.test_cleanDeployedTemplates()
        time.sleep(120)
        self.test_cleanePublishedTemplates()
        self.cleanServerPool()
        self.cleanDiscovery()
        self.cleanManagedDevice()
        self.cleanNetwork()
        self.cleanCredential()
        self.cleanUpRepository()
        time.sleep(120)


if __name__ == "__main__":
    test = Testcase()
    test.authenticate()
    test.doTearDown()
    #test.cleanServerPool()
    #test.cleanDiscovery()
    #test.cleanManagedDevice()
    #test.cleanNetwork()
    #test.cleanCredential()
    os.chdir(current_dir)
    
