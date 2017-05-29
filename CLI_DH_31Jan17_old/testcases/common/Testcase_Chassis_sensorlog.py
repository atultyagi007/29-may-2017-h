'''
Created on Marchs 29, 2017

@author: raj.patel
Prerequisites:One Chassis must be discovered.
/Chassis/{refId}/sensorlog        get
/Chassis/{refId}/sensorlog        delete
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
                resId=chassisInfo['refId']
                managementIP = chassisInfo['managementIP']
                refId = resId+"/"+"sensorlog"
                print managementIP
                self.log_data("Device ip is:%s"%(managementIP))
                resDevice,status=self.getResponse("GET", "Chassis",refId=refId)
                if status:
                    self.log_data("Payload:",resDevice)
                    self.log_data("Successfully tested /Chassis/{refId}/sensorlog with GET action:" +managementIP)
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Chassis/{refId}/sensorlog with GET action for:' +managementIP,'Test case passed'])
                    
                else:
                    self.log_data("Test case Failed")
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                    return False
                
                resDevice1,status1=self.getResponse("DELETE", "Chassis",refId=refId)
                if status1:
                    self.log_data("Successfully deleted sensorlog:",resDevice1)
                    self.log_data("Successfully tested /Chassis/{refId}/sensorlog with DELETE action:" +managementIP)
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Chassis/{refId}/sensorlog with DELETE action for:' +managementIP,'Test case passed'])
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