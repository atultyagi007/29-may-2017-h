'''
Created on May 17, 2016

@author: preetam.sethi
Description : upload a catalog from network path--cifs, select it as default catlog while uploading
            NGI-TC-3206 merged.

Test Flow  :1)Login as a Admin user and 
            2)Download catlog from Network path CIFS as default catlog, by selecting make default option
            3) Verify downloaded catlog is set to default 

prerequisites:Firmware catlog should be there.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    upload a catalog from network path--cifs, select it as default catlog while uploading and verify after upload.
    Merged Testcase NGI-TC-3206 
    """
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
        
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        self.logout()   
   
    def runTestCase(self):        
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
#        arguments option, networkPath, localPath, makeDefault= True, testConnection= False, testNegative = False
        self.addFirmwareRepository("networkPath",globalVars.catlogRepository["cifsPath"],"", True, False,False)
        self.verifyDefaultFirmware("networkPath",globalVars.catlogRepository["cifsPath"])
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()