'''
Created on April 26, 2017

@author: raj.patel
Description:Testcase covers these api
/Timezone/all  get
/WizardStatus/  get
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
Testcase covers these api:
/Timezone/all  get
/WizardStatus/  get
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
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
                
        resDevice,status1=self.getResponse("GET", "AllTimezone")
        print resDevice
        availableTimeZones=resDevice['availableTimeZones']
        noOfavailableTimeZones=len(availableTimeZones)
         
        if noOfavailableTimeZones > 0 and status1:
            self.log_data("Getting all time zone")
            self.log_data("Successfully tested /Timezone/all with GET action:")
            self.log_data(resDevice)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Timezone/all with GET action:','Success','Successfully tested api'])
             
        else:
            self.log_data("Test case Failed")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
        
        resInfo,status2=self.getResponse("GET", "Wizard")
        print resInfo
        if len(resInfo) > 0 and status2:
            self.log_data("Getting Wizard")
            self.log_data("Successfully tested /WizardStatus/ with GET action:")
            self.log_data(resInfo)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /WizardStatus/ with GET action:','Success','Test case passed'])
            
        else:
            self.log_data("Test case Failed")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
        return True
        
        
        
if __name__ == "__main__":
    test = Testcase()
    status = test.runTestCase()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)