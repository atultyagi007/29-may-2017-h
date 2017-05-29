'''
Created on April 18, 2017

@author: raj.patel
Description:On any API end point ,do a Get without Authentication info : Should get an error back saying NOT Authenticated
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
On any API end point ,do a Get without Authentication info : Should get an error back saying NOT Authenticated
'''
class Testcase(DiscoverResourceTestBase):
    
    tc_Id = ""
    chassisCredentialId = ""
    switchCredentialId = ""
    serverCredentialId = ""
    chassisServiceTag = ""
    chassisRefId = ""
    chIPAddr = ""
    resDS = []
    statDS = False
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
            
        
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
                
        resDevice,resStatus_code,status=self.getResponseByWithoutAuthentication("GET", "osRepository")
        print resDevice
        if resStatus_code==401 and status:
            self.log_data("Test case passed successfuly.NOT Authenticated")
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case passed successfuly.NOT Authenticated','Success','Successfully Test case passed'])
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