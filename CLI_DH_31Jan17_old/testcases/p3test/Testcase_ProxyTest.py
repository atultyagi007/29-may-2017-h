'''
Created on May 5, 2017

@author: raj.patel
Description:These test cover api
/Proxy/test    post

'''
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../initialsetup'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from InitialBaseClass import InitialTestBase
import json
import time


class Testcase(InitialTestBase): 
    tc_Id = "" 
    
    def __init__(self):
        InitialTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
    
    def test_login(self):
        response = self.authenticate()
        self.log_data( "Setting Login, Response : %s"%str(response))
        logger = self.getLoggerInstance()
        logger.info(' Login Response is ')
        logger.debug(response)
             

     
       
    def test_Proxy(self):
        response,status = self.postProxy()
        if status:
            self.log_data( "Setting Proxy, Response : %s"%str(response))
            ProxyResponse  = json.loads(response)
            logger = self.getLoggerInstance()
            logger.info('Test Proxy Response is ')
            logger.debug(ProxyResponse)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Proxy/test with POST action:','Success','Test case passed'])
            return True
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
            
    
       

if __name__ == "__main__":
    test = Testcase()

    test.test_login()
    
    status=test.test_Proxy()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
           
    else:
        os.chdir(current_dir)
        sys.exit(1)
