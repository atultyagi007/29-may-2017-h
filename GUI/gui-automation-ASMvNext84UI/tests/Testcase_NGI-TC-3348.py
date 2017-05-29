'''
Author: P.Suman
Created Date: Dec 15, 2015
Description: Verify that the following options are available under Settings: 1. Backup and Restore 
    2. Credential Management 3. Getting started 4. Jobs 5. Logs 6. Networks 7. Repositories 8. Users 
    9. Virtual Appliance Management 10. Virtual Identity Pools
'''

from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Available options under Settings
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
        settingTabOptions = ("Getting Started", "Logs", "Jobs", "Users", 
                    "Networks", "Credentials Management", "Virtual Appliance Management", "Backup and Restore",
                    "Add-On Modules", "Repositories", "Virtual Identity Pools")
        #Verify Options
        options = self.getSettingsOptions()
        failedList = [k for k,v in options.items() if v=="Disabled"]
        if len(failedList) <= 0:
            self.succeed("Successfully verified Options under Settings Tab :: %s"%str(options.keys()))
        else:
            self.failure("Some options are not available in Settings Tab :: %s"%str(failedList))
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()