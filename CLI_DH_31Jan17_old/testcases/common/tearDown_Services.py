'''
Created on Aug 19, 2014

@author: dheeraj.singh
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from utilityModule import UtilBase
import json
from logging import Logger, getLogger
from cookielib import logger

class Testcase(UtilBase):
    
    tc_Id = ""
   
    
    def __init__(self):
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def test_login(self):
        response = self.authenticate()
        loginResponse  = json.loads(response)
        logger = self.getLoggerInstance()
        
        
            
    def test_cleanDeployedTemplates(self):
        
        response = self.cleanUpServices()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))
        
        
    


if __name__ == "__main__":
    test = Testcase()
    test.test_login()
    test.test_cleanDeployedTemplates()
    os.chdir(current_dir)
