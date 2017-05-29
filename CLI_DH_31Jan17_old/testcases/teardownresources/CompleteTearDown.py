'''
Created on Nov 25, 2014

@author: waseem.irshad
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from TemplateBaseClass import TemplateTestBase
import json
import globalVars
import csv
import time
import requests
import xml.etree.ElementTree as ET
from lxml import etree
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import datetime

'''
    # Test case : To tear down a deployment completely
    # Pre-requisite : Deployment should exist in ASM
'''
class CompleteTearDown(TemplateTestBase,DiscoverResourceTestBase): 
    
    tc_Id = ""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    
    def getResponseandCompleteTeardown(self):
        
        logger = self.getLoggerInstance()
        logger.info(" ##### Test Case : Completely Tear Down a Deployment #### ")
        deplName = globalVars.depNametoTearDown
        #Get Deployment Id
        deploymentRefId = self.getDeploymentId(deplName)
        logger.info(" Deployment Name : ")
        logger.info(deplName)
        logger.info( " Deployment Ref ID : ")
        logger.info(deploymentRefId)
        logger.info(" Going to remove deployment...")
        response,status = self.teardownServices(deploymentRefId)
        
        if response.status_code in (200, 201, 202, 203, 204):
            self.log_TestData(["", "", "",str(self.tc_Id),'Success','Devices deleted Successfully'])
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to delete devices'])
            
            
if __name__ == "__main__":
    test = CompleteTearDown()
    test.getResponseandCompleteTeardown()
    os.chdir(current_dir)
