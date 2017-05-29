'''
Author: P.Suman
Created Date: Dec 14, 2015
Description: Create a user with same Username and Password (like dell1234,dell1234) should throw error
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Create a user with same Username and Password (like dell1234,dell1234) should throw error
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
        #Delete an existing User to create one
        users = self.getLocalUsers(userName="testuser")
        if len(users) > 0:
            self.deleteLocalUser(userName="testuser")
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        #Delete an existing User to create one
        users = self.getLocalUsers(userName="testuser")
        if len(users) > 0:
            self.deleteLocalUser(userName="testuser")

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Create TestUser
        self.createLocalUser(userName="testuser", userPassword="testuser",  
                       userRole="Read only", enableUser=True, verifyUser=False, negativeScenario=True)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    