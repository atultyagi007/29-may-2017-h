'''
Created on march 24, 2017

@author: raj.patel
Description:
/FirmwareRepository/softwarebundle
/FirmwareRepository/softwarebundle/{id}    get

'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from DiscoverResourceBaseClass import DiscoverResourceTestBase
import globalVars
from encodings import raw_unicode_escape
import os
import time

class Testcase(DiscoverResourceTestBase):
    
    td_Id=""
    refId=""
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
        
    def runTestcase(self):
        
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        self.log_data("Running Test Case ::: ")
        
        response,status = self.getResponse("GET", "FirmwareRepository")
        if status:
            for resp in response:
                Id = resp["id"]
                
                defaultCatalog = resp["defaultCatalog"]
                if defaultCatalog == True:
                    self.log_data("FW Repository id:")
                    self.log_data(Id)
                    response1,status1 = self.getAllBundles(refId=Id)
                    if status1:
                        self.log_data("Software Bundles:")
                        self.log_data(response1)
                        softwareBundles = response1['softwareBundles']
                        for swBundle in softwareBundles:
                            bundleType=swBundle['bundleType']
                            if bundleType=="SOFTWARE" :
                                ID=swBundle['id']
                                respSWBundle,stat = self.getResponse("GET", "FWRepoSoftwareBundle",refId=ID)
                                if stat:
                                    self.log_TestData(["", "", "", str(self.tc_Id),'Success','URI:/FirmwareRepository/softwarebundle/{id} with action GET successfully tested'])
                                    return
                                else:
                                    self.log_data("FW Repository software bundle not able to get")
                                    self.log_TestData(["", "", "", str(self.tc_Id),'Failed','FW Repository software bundle not able to get','Testcase failed'])     
        
        
if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    test.runTestcase()
    os.chdir(current_dir)
    


    
        
        
        
