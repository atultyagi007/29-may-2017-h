'''
Created on Feb 24, 2016

@author: raj.patel
/ManagedDevice/    get    
/ManagedDevice/    post    
/ManagedDevice/{refId}    delete 
'''
import os
import sys
import xml.etree.ElementTree as ET
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from DiscoverResourceBaseClass import DiscoverResourceTestBase
import json
import time
from util import globalVars
import chassisConfigParam


'''
/ManagedDevice/    get    
/ManagedDevice/    post    
/ManagedDevice/{refId}    put    
/ManagedDevice/{refId}    delete    

'''
class Testcase(DiscoverResourceTestBase):
    
    tc_Id = ""
    chassisCredentialId = ""
    switchCredentialId = ""
    serverCredentialId = ""
    chassisServiceTag = ""
    chassisRefId = ""
    chIPAddr = ""
    resDS = []
    statDS = False
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
        
    def test_Login(self):
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        creResponse,result = self.setupCredentials()
        logger = self.getLoggerInstance()               
        logger.debug('CREDENTIAL RESPONSE')
        logger.info(creResponse)
        
        
        
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        resDeviceList,statDevice = self.getResourceList()
        if not statDevice:
            print "Getting managed device failed"
            self.log_data("Get managed dice failed")
            return "Get managed dice failed"
        self.log_data("Payload :%s"%resDeviceList)
        
        manageDeviceList = ET.fromstring(resDeviceList)
        for manageDeviceList in manageDeviceList:

            refId=manageDeviceList.find('refId').text
            inUse = manageDeviceList.find('inUse').text
            if inUse in  ("False","false"):
                resDevice,status=self.getResponse("Delete", "ManagedDevice",refId=refId)
                if status:
                    self.log_data("Successfully deleted resources")
                    break
                else:
                    self.log_data("Resources not deleted:Test case Failed")
                    return False
                
                    
        resDevice,status=self.getResponse("POST", "ManagedDevice",payload=resDeviceList)
        if status:
            self.log_data("Test case passed successfuly delete and post URI Tested")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case passed successfully delete and post URI Tested','Successfully Test case passed'])
        else:
            self.log_data("Test case Failed")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
        
        
        
if __name__ == "__main__":
    test = Testcase()
    test.runTestCase()
    os.chdir(current_dir)