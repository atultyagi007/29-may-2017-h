'''
Author: P.Suman
Created Date: Dec 21, 2015
Description: Verify that the Logs option under Settings displays the activities, their severity, category and time information.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Logs option under Setting.
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
        #Read Logs
        logsList = self.getLogs()
        self.succeed("Existing Logs on first page :: %s"%str(logsList))
        logs = logsList[0]
        if logs.has_key("Category") and logs.has_key("Severity") and logs.has_key("Date and Time") and logs.has_key("Description") and logs.has_key("User"): 
            self.succeed("Able to check fields Category, Date and Time, User, Description, Severity :: %s"%logs)
        else:
            self.failure("Some fields are missing in Category/Date and Time/User/Description Information :: %s"%logs, raiseExc=True)
        if len(logsList) > 1:
            if logs["Category"] != "" and logs["Date and Time"] != "" and logs["Description"] != "":
                self.succeed("Able to check Category, Date and Time, User, Description Information :: %s"%logs)
            else:
                self.failure("Failed to check content in Category/Date and Time/User/Description Information :: %s"%logs, raiseExc=True)
        else:
            self.succeed("There are no logs to check Category, Date and Time, User, Description Information :: %s"%logs)
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup() 
