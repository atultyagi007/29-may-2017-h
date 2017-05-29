'''
Author: P.Suman
Created Date: Dec 17, 2015
Description: Enter correct password after 5 mins, should get to Dashboard page
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Enter correct password after 5 mins, should get to Dashboard page
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
        #Logout from appliance
        self.logout()
    
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
        #Login with Admin Credentials with WaitTime of 5 minutes
        self.login(username=self.loginUser, password=self.loginPassword, newInstance=False, negativeScenario=False, waitTime=5)
        #Verify Landing Page
        self.verifyPageLaunch(pageTitle="Active System Manager", userRole="Administrator")
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    