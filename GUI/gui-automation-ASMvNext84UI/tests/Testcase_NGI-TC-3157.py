'''
Author: P.Suman
Created Date: Dec 15, 2015
Description: Create a user with same password as admin, should create user
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Create a user with same password as admin, should create user
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
        #Delete Users
        users = self.getLocalUsers()
        for user in users:
            if user["Name"] in ("testuser", "readonlyuser"):
                self.deleteLocalUser(userName=user["Name"])
        #Create TestUser
        self.createLocalUser(userName="testuser", userPassword="testuser1234",  
                       userRole="Administrator", enableUser=True, verifyUser=True, negativeScenario=False)
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        #Delete Users
        users = self.getLocalUsers()
        for user in users:
            if user["Name"] in ("testuser", "readonlyuser"):
                self.deleteLocalUser(userName=user["Name"])

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Create Readonly user with same password as Admin
        self.createLocalUser(userName="readonlyuser", userPassword="testuser1234",  
                       userRole="Administrator", enableUser=True, verifyUser=True, negativeScenario=False)
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    