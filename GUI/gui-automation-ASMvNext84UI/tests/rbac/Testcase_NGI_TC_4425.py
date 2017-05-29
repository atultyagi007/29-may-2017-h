'''
Author       : ankit.manglic
Created Date : Aug 09, 2015
Description  : Verify a Standard user with no Services rights can Export All Services information.
Test Flow    : 1) Login as Standard user.
               2) Click export file and verify file is exported. 
'''

from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify a Standard user with some Service rights can Export All Services information
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
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
     
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
 
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        self.exportAllServices()
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
        
        self.logout()
