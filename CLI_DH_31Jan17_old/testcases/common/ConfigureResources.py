'''
Created on Aug 8, 2014

@author: dheeraj.singh
'''
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))

from DiscoverResourceBaseClass import DiscoverResourceTestBase
import json
import time
import globalVars
import TestCase_102640
import FirmwareUpdate
import sys

class Testcase(DiscoverResourceTestBase):
    
    tc_Id = ""
   
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def test_login(self):
        
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        
    def test_configureResourec(self):
        
        logger = self.getLoggerInstance()
        try:
            self.setupCredentials()
            self.chassisConfigureResource() 
            time.sleep(60)                          
            response = self.configureResource()
            print 'response after Configure Resource  '
            print response
            logger.debug('response after Configure Resource  ')
            logger.info(response)
        except Exception as e1:
            self.log_data( 'Exception occurred while Configure Resource ')
            self.log_data(str(e1))
            
        wizardResponse = self.setCompleteWizard() 
        self.log_data( 'response after Completing Wizard  ')
        self.log_data( wizardResponse)
        time.sleep(10)
        logger.debug('response after Completing Wizard   ')
        logger.info(wizardResponse)
        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"Configure Resource Step Successfully Completed"])  
        
    def chassisConfigureResource(self):
        """
        Defines new Credential with provided Username and Password
        """    
        payload = self.readFile(globalVars.configureResourcePayload)
        payload = payload.replace("$server_cred_ref", self.credentialMap["autoServer"]).replace("$chassis_cred_ref", 
                        self.credentialMap["HVChasis"]).replace("$iom_switch_cred_ref", self.credentialMap["autoIOM"])                
        return self.getResponse("POST", "ChassisConfigureDiscover", payload)
        
        



    

if __name__ == "__main__":
    test = Testcase()
    
    
    test.test_login()
    test.test_configureResourec()
    time.sleep(30)
    os.chdir(current_dir)
#     test15 = TestCase_102640.Test_102640()
#     response = test15.uploadNFSCatalog()
#     time.sleep(30)
#     if response:
#         test16 = FirmwareUpdate.FirmwareUpdate()
#         test16.updateFirmwares()
    
    
