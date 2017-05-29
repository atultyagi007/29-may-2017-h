'''
Created on March 2, 2017

@author: raj.patel
Description: Test case to verify  Deployment's URI:
/Deployment get

'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
# sys.path.insert(0,os.path.abspath('../../setup'))
# sys.path.append(os.path.abspath('../../util'))
# sys.path.append(os.path.abspath('../../testcases/common'))

sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from TemplateBaseClass import TemplateTestBase
import globalVars
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Get Deployement of Service 
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
    
        
    def runTestCase(self):
        testCaseID=self.getTestCaseID(__file__)
        logger = self.getLoggerInstance()
        #self.removelogfile()
        response = self.authenticate()
        logger.info(response)
        resGet,state = self.getResponse("GET", "Deploy")
        logger.debug("Deployment Info :")
        logger.info(resGet)
        if not state:
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
            return False
        else:
            self.log_data("Payload::")
            self.log_data(resGet)
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully get the deployment of all services','Success','Successfully Test case passed'])
            return True
        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    status = test.runTestCase()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)   

