'''
Author: P.Suman
Created Date: Dec 21, 2015
Description: Verify that Generate Troubleshooting bundle link is functional under Settings.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Generate Troubleshooting bundle under Settings.
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
        #Clear Existing Trouble Shooting Bundle
        self.clearTroubleShootingBundle()
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        #Clear Existing Trouble Shooting Bundle
        self.clearTroubleShootingBundle()

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Generate Bundle
        self.generateTroubleShootingBundle()
        time.sleep(30)
        #Verify Generated Bundle
        self.verifyTroubleShootingBundle()
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup() 
