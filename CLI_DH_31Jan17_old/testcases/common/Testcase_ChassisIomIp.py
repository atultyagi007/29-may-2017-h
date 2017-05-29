'''
Created on Marchs 29, 2017

@author: raj.patel
Prerequisites:One Chassis must be discovered.
/Chassis/iom/ip/{ip}      get
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
        resDevice,status=self.getResponse("GET", "Chassis")
        
        if status and len(resDevice)> 0 :
            for chassisInfo in resDevice:
                ip = chassisInfo['ioms'][0]['managementIP']
                model = chassisInfo['ioms'][0]['model']
                self.log_data("Device model is %s and ip is:%s"%(model,ip))
                resDevice,status=self.getResponse("GET", "Chassisiomip",refId=ip)
                if status:
                    self.log_data("Payload:",resDevice)
                    self.log_data("Successfully tested /Chassis/iom/ip/{ip}with GET action:" +ip)
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Chassis/iom/ip/{ip} with GET action for:' +ip,'Test case passed'])
                    break
                else:
                    self.log_data("Test case Failed")
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                    return False
        else:
            self.log_data("Please discover chassis.It's prerequisites")        
        
        
        
if __name__ == "__main__":
    test = Testcase()
    test.runTestCase()
    os.chdir(current_dir)