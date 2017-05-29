'''
Created on Feb 24, 2016

@author: raj.patel

/ManagedDevice/withcompliance       get
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
        
      
        
        
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        resDevice,status=self.getResponse("GET", "ManagedDeviceWcomp")
        
        if status:
            self.log_data("Number of Managed device with compliance:%i",len(resDevice))
            self.log_data("Managed device with compliance:",resDevice)
            self.log_data("Successfully tested URI /ManagedDevice/withcompliance with get action")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully /ManagedDevice/withcompliance with get URI tested','Test case passed'])
        else:
            self.log_data("Test case Failed")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
        
        
        
if __name__ == "__main__":
    test = Testcase()
    test.runTestCase()
    os.chdir(current_dir)