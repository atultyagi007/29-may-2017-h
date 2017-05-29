'''
Created on Sep 21, 2015

@author: dheeraj.singh
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
import time
import RepositoryParamFile

class Testcase(DiscoverResourceTestBase):
    
    """
    Upload a bundle file for switches IOA/IOM/MXL
    """ 
    
    td_Id=""
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    ''' Function to Upload bundle for switch '''    
    def UploadSwitchBundleForSwitch(self):
        resFR, statFR = self.getResponse("GET", "FirmwareRepository")
        print " Response : %s"%str(resFR)
        fwRepositoryId = ""
        for repos in resFR:
            if repos['name'] == RepositoryParamFile.FirmwareRepositoryName:
                fwRepositoryId = repos['id']
        print " REFID : %s"%str(fwRepositoryId)
        payload = self.readFile(globalVars.filename_TestCase_SwitchUpdatePost)
        payload = payload.replace("$criticality",RepositoryParamFile.criticality).replace("$switch_model",RepositoryParamFile.deviceModel).replace("$fwRepositoryId",fwRepositoryId).replace("$name",RepositoryParamFile.BundleName).replace("$bundle_Path",RepositoryParamFile.userBundlePath).replace("$version",RepositoryParamFile.BundleVersion).replace("$SoftBundleDesc",RepositoryParamFile.BundleDescription)
        print " payload : "
        print payload
        response,status = self.getResponse("POST","softwareBundleFirmware",payload = payload)
        print response, status
        
        
    
        
        
if __name__ == "__main__":
    test = Testcase()
    test.UploadSwitchBundleForSwitch()
                
        
