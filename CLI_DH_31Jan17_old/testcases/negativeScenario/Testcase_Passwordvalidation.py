'''
Created on April 17, 2017

@author: raj.patel
Description:Add Validation on the Response passwords to ensure they are always blank
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
 Add Validation on the Response passwords to ensure they are always blank  
'''
class Testcase(DiscoverResourceTestBase):
    
    tc_Id = ""
    
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
            
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
        response = self.authenticate()
        loginResponse  = json.loads(response)
        print loginResponse
        loginResponse=self.convertUTA(loginResponse)
        self.log_data("Login Response:",loginResponse)
        
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        resDevice,status=self.getResponse("GET", "User")
        self.log_data("Login Response:",resDevice)
        if status:
            for resInfo in resDevice:
                if 'password' not in resInfo.keys():
                    self.log_data("Not able to get response password.Test case passed")
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Not able to get response password','Success','Test case passed'])
                    return True
                else:
                    self.log_data("Test case Failed")
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                    return False
                   
        
        
        
if __name__ == "__main__":
    test = Testcase()
    status = test.runTestCase()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)