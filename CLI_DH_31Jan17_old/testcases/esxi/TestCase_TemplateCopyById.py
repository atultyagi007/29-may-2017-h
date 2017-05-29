'''
Created on April 10, 2017

@author: raj.patel
Description:Copy a ServiceTemplate.
/ServiceTemplate/{id}/copy    post
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

from util import utilityModule
from TemplateBaseClass import TemplateTestBase
import json
import globalVars
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping
import templateInputReqValueHyperV


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Copy a ServiceTemplate
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    
    def runTestCase(self):
        testCaseID=self.getTestCaseID(__file__)
        #self.removelogfile()
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        payload = self.readFile(globalVars.filename_TestCase_copyTemp)
        resDevice,status=self.getResponse("GET", "Template")
        if status and resDevice > 0:
            for resInfo in resDevice:
                TempId=resInfo['id']
                print TempId
                payload = payload.replace("$tempId",TempId)
                refId = TempId+"/"+"copy"
                print refId
                resDevice1,statusInfo=self.getResponse("POST", "Template",payload=payload,refId=refId)
                if statusInfo:
                    self.log_data("Payload:",resDevice1)
                    self.log_data("Successfully tested /ServiceTemplate/{id}/copy with post action:")
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /ServiceTemplate/{id}/copy with post action','Test case passed'])
                    return True
                else:
                    self.log_data("Test case Failed")
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed for /ServiceTemplate/{id}/copy with post action', 'Failed','Test case failed'])
                    return False
                
                
        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
#     test.preRunSetup()
    status = test.runTestCase()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)   

