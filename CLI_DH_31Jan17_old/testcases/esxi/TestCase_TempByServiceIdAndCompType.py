'''
Created on April 13, 2017

@author: raj.patel
Description:Retrieve Default Template with components refined for specified template ID
/ServiceTemplate/service/{serviceId}/{componentType}    get

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
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping
import templateInputReqValueHyperV


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Retrieve Default Template with components refined for specified template ID
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
        resDevice,status=self.getResponse("GET", "Deploy")
        if status and resDevice > 0:
            for resInfo in resDevice:
                serviceId=resInfo['id']
                print serviceId
                print resInfo
                componentType=resInfo['serviceTemplate']['components'][0]['type']
                print componentType
                deviceId = "service"+"/"+serviceId+"/"+componentType
                resDevice1,statusInfo=self.getResponse("GET", "Template",refId=deviceId)
                if statusInfo:
                        self.log_data("Result:",resDevice1)
                        self.log_data("Successfully tested /ServiceTemplate/service/{serviceId}/{componentType} with GET action:")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /ServiceTemplate/service/{serviceId}/{componentType} with GET action','Test case passed'])
                        return True
                else:
                        self.log_data("Test case Failed")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed for /ServiceTemplate/service/{serviceId}/{componentType} with GET action', 'Failed','Test case failed'])
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

