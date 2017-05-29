'''
Created on April 26, 2017

@author: raj.patel
Description:Testcase covers these api
/ManagedDevice/count    get
/ManagedDevice/export/csv    get  
/ManagedDevice/withcompliance/{refId}    get
'''
import os
import sys
import csv
import datetime
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
/ManagedDevice/count    get
/ManagedDevice/export/csv    get
/ManagedDevice/withcompliance/{refId}    get
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
        resInfo1,status1=self.getResponse("GET", "CountManagedDevice")
        print resInfo1
        if status1: 
            self.log_data("Managed device count info")
            self.log_data(resInfo1)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested //ManagedDevice/count with GET action:','Success','Successfully tested api'])
              
        else:
            self.log_data("Test case Failed")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
        
        resDevice,status1=self.runService("GET", "ExportManagedDevice",responseType="file", requestContentType="file")
        print resDevice
        if status1:
            self.log_data("Exported managed device info")
            self.log_data(resDevice)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /ManagedDevice/export/csv with GET action:','Success','Successfully tested api'])
              
        else:
            self.log_data("Test case Failed")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
        
        satusInfo=False  
        resInfo,status2=self.getResponse("GET", "ManagedDeviceWcomp")
        print resInfo
        if len(resInfo) > 0 and status2:
            for res in resInfo:
                refID=res['refId']
                resInfo3,status3=self.getResponse("GET", "ManagedDeviceWcomp",refId=refID)
                if status3:
                    self.log_data("Successfully tested /ManagedDevice/withcompliance/{refId} with GET action:")
                    self.log_data(resInfo3)
                    satusInfo=True
                else:
                    satusInfo=False  
            if satusInfo:
                self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /ManagedDevice/withcompliance/{refId} with GET action:','Success','Test case passed'])
            else:
                self.log_data("Test case Failed")
                self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                return False 
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