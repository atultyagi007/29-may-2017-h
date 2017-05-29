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
import logging
import logging.config
from logging import Logger, getLogger
from cookielib import logger

class Testcase(UtilBase):
    
    tc_Id = ""
   
    
    def __init__(self):
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def test_cleanePublishedTemplates(self):
        
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))
        
    


if __name__ == "__main__":
    test = Testcase()
    test.test_cleanePublishedTemplates()
    os.chdir(current_dir)
