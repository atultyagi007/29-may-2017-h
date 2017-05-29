'''
Created on Sept 13, 2016

@author: preetam.sethi
Description : Try to update firmware when firmware is in compliant state, update firmware button should be inactive in Resource page
Test Flow   :1)Login as a Admin user
             2)select server that is Compliant
             3)verify update firmware button is inactive
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify update firmware button is inactive for Compliant resources
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
        self.verifyUpdateDisabled(selectCompliant= True)
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    