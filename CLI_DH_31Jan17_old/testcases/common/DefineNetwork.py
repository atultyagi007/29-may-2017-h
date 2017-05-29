'''
Created on March 1, 2017

@author: raj.patel
Description: Test case to verify  Network's URI:
/Network get
/Network post
/Network/{networkId} put
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
        
        
    def test_Network(self):
        
        status = False
        resultNW, statusNW = self.setupNetworks()
        logger = self.getLoggerInstance()
        logger.info(' ==================NETWORK RESPONSE================ ')
        logger.debug( ' Setting Networks, Response : %s'%str(resultNW))
        logger.info(' print network response status ')
        logger.debug(statusNW)
        if statusNW:
            status= True
            self.log_TestData(["", "", "",str(self.tc_Id),'Success',"Define Networks Step Successfully Completed"])
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failure',"Failed to Define Networks"])   
        time.sleep(120)
        return status
        
       

if __name__ == "__main__":
    test = Testcase()
    
#     test.test_login()
    status = test.test_Network()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)
        
