'''
Created on May 9, 2016

@author: preetam.sethi
Description : upload a catalog from FTP.dell.com

Test Flow  :1)Login as a Admin user and Try to upload a catalog from Invalid Network Path
prerequisites:Firmware catloge should be there.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Try to upload a catalog from FTP.dell.com
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
#        arguments option, networkPath, localPath, makeDefault= False, testConnection= False, testNegative = False
        self.addFirmwareRepository("defaultftp","","", False, False,False)
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()