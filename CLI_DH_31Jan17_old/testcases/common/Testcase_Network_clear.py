'''
Created on Feb 22, 2016

@author: raj.patel
Description: Test case to verify  user's URI:
/Network get
/Network/{networkId} delete
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../definenetwork'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from DefNetworkBaseClass import DefNetworkTestBase
import json
import time


class Testcase(DefNetworkTestBase):
    tc_Id = "" 
    
    def __init__(self):
        DefNetworkTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
    
    def test_login(self):
        response = self.authenticate()
        loginResponse  = json.loads(response)
        logger = self.getLoggerInstance()
        logger.info(' login response in network test ')
        logger.debug(loginResponse)
        
        
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
        resCU, statCU = self.cleanNetwork()
        if not statCU:
            self.log_data("Failed to create clean network: ")
        
        if statCU:
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully verified Network delete URI', 'Success','Successfully Test case passed'])
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Success','Test case failed'])
               
       

if __name__ == "__main__":
    test = Testcase()
    
#     test.test_login()
    status = test.runTestCase()
    os.chdir(current_dir)
#     if status==True:
#         os.chdir(current_dir)
#         sys.exit(0)
#     else:
#         os.chdir(current_dir)
#         sys.exit(1)
        
        
