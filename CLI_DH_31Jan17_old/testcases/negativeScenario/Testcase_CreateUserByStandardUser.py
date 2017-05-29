'''
Created on April 19, 2017

@author: raj.patel
Description:Validate Standard User cannot create an admin user /or any user.

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
  Validate Standard User cannot create an admin user /or any user. 
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
        response = self.authenticate(role="standard")
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        
        
        payload = self.readFile(globalVars.userPayload)
        
        payload = payload.replace("user_name", globalVars.standardUser).replace("user_pwd", 
                   globalVars.standardPwd).replace("user_domain", globalVars.domain).replace("login_user", "admin").replace("user_role","standard").replace("first_name","hcl")           
        
        resDevice,status=self.getResponse("POST", "User",payload=payload)
        print status
        print resDevice
        response = self.authenticate(role="standard")
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        payload = self.readFile(globalVars.userPayload)
        
        payload = payload.replace("user_name", "autohcladmin").replace("user_pwd", 
                   "autopassword").replace("user_domain", globalVars.domain).replace("login_user", "autostandard").replace("user_role","Administrator").replace("first_name","hcltest")           
        
        resDevice,resStatus_code,status=self.getResponseByWithoutAuthentication("POST", "User",payload=payload)
        if resStatus_code==403 and status:
            self.log_data("Test case passed.Standard user can not create any user")
            self.log_data(resDevice)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case passed.Standard user can not create any user','Success','Successfully Test case passed'])
            return True
        else:
            self.log_data("Test case Failed")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
               
        
        
        
if __name__ == "__main__":
    test = Testcase()
    status=test.runTestCase()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)