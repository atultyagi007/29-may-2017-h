'''
Created on April 28, 2017

@author: raj.patel
Description:Testcase covers these api
/Network/export/csv    get
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
/Network/export/csv    get
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
        
        resDevice,status1=self.runService("GET", "ExportNetwork",responseType="file", requestContentType="file")
        print resDevice
        if status1:
            self.log_data("Exported Network in csv info")
            self.log_data(resDevice)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Network/export/csv with GET action:','Success','Successfully Test case passed'])
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