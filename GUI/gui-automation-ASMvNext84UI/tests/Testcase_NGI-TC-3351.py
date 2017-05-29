'''
Author: P.Suman
Created Date: Dec 24, 2015
Description: Verify that the admin is able to view and select any steps i.e Define Networks, Discover Resources 
        and View and Publish Templates. 
        Initial setup is not available after initial setup of the appliance.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Getting Started option under Setting.
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

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Verify Options On Getting Started Page
        self.getGettingStartedOptions(actualOptions=["Define Networks", "Discover Resources", "Define Existing Services", 
                    "Configure Resources", "Publish Templates"], enableOptions=["Define Networks", "Discover Resources", "Define Existing Services", 
                    "Configure Resources"])
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    