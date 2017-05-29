'''
Author       : HCL
Created Date : Feb 02, 2016
Description  : Navigate to the Services page and verify all information is correct and all links work and point to the correct page.
Test Flow    : 1) Login as admin Navigate to the Services page.
               2) Verify all information is correct and all links work and point to the correct page
'''

from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
        Verify all information is correct and all links work and point to the correct page
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
        self.verifyServices()
     
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
