'''
Created on March 06, 2017

@author: raj.patel
Description: Test case to verify      AddOnModule's URI:
/AddOnModule    get
/AddOnModule    post

'''
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../initialsetup'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from AddOnModule import AddOnModuleTestBase
import json
import time
from util import globalVars
import chassisConfigParam


class Testcase(AddOnModuleTestBase):
    
    tc_Id = ""
    
    def __init__(self):
        AddOnModuleTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
        
    def test_Login(self):
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)

        
        
        
    def runTestCase(self):
        """
        This is the execution starting function
        """
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        self.log_data("Running Test Case ::: ")
        
#         self.getPaylodGeneral()

        resGet,state = self.getResponse("GET", "AddOnModule")
        if not state:
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
        else:
            self.log_data("Payload::")
            self.log_data(resGet)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfuly Get the add on module','Success','Successfully Test case passed'])
        
        for res in resGet:
            refId=res["id"]
            reRes,status = self.getAddOnModuleById(refId)
            if status:
                self.log_data("Successfully get add on module by id")
                self.log_TestData(["", "", "",str(self.tc_Id), 'Successfuly get add on module info by refId','Success','Successfully Test case passed'])
            else:
                self.log_data("Not able to get add on module info")
                self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                break
        
        
if __name__ == "__main__":
    test = Testcase()
    test.runTestCase()
    os.chdir(current_dir)