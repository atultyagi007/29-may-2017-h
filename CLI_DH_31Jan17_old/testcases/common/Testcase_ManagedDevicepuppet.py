'''
Created on March 27, 2017

@author: raj.patel
Prerequisites:One Chassis must be discovered.
/ManagedDevice/puppet/{certName}    get
/ManagedDevice/puppet/{certName}    put
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
            for res in resDevice:
                deviceType = res['deviceType']
                serviceTag = res['serviceTag']
                newServiceTag = "H79DWW2"
                newcertName=""
                self.log_data("Device type is %s and Service tag is:%s"%(deviceType,serviceTag))
                certName = deviceType+"-"+serviceTag
                resDevice,status=self.getResourceByCertname("GET", "ManagedDevicepuppet",refId=certName)
                if status:
                    self.log_data("Payload:",resDevice)
                    self.log_data("Successfully /ManagedDevice/puppet/{certName} with GET action for ServiceTag:" +serviceTag)
                    self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /ManagedDevice/puppet/{certName} with GET action for ServiceTag:' +serviceTag,'Test case passed'])
                    if serviceTag != newServiceTag:
                        resDevice=resDevice.replace("<serviceTag>"+serviceTag+"</serviceTag>","<serviceTag>"+newServiceTag+"</serviceTag>") 
                    resDevice1,state=self.getResponse("PUT", "ManagedDevicepuppet",payload=resDevice,refId=certName)
                    if state:
                        self.log_data("Test case passed Successfully tested /ManagedDevice/puppet/{certName} with PUT action")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /ManagedDevice/puppet/{certName} with PUT action','Test case passed'])
                    else:
                        self.log_data("Test case Failed")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                        return False
                    
                    if state:
                        newcertName = deviceType+"-"+newServiceTag
                        resDevice=resDevice.replace("<serviceTag>"+newServiceTag+"</serviceTag>","<serviceTag>"+serviceTag+"</serviceTag>")
                    else:
                        return False
                    afterPutresDevice,stat1=self.getResponse("PUT", "ManagedDevicepuppet",payload=resDevice,refId=newcertName)
                    if stat1:
                        self.log_data("Test case passed successfully and reverted service tag")
                        break
                    else:
                        self.log_data("Test case Failed")
                        return False       
                    
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