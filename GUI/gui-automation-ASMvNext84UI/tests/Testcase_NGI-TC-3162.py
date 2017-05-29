'''
Author: P.Suman
Created Date: Dec 18, 2015
Description: Enter wrong password 5 times, should throw error saying account is locked for 5 mins
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Enter wrong password 5 times, should throw error saying account is locked for 5 mins
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
        #Login with wrong Credentials for 4 times
        for _ in range(4):
            self.login(username=self.loginUser, password='12345', newInstance=False, negativeScenario=True)
        #Login with wrong Credentials for 5th time
        self.login(username=self.loginUser, password='12345', newInstance=False, negativeScenario=True,
                   pattern="Your account has been locked due to repeated failed login attempts")
        time.sleep(420)
        #Login as Admin
        self.login(username=self.loginUser, password=self.loginPassword, newInstance=False, 
                   negativeScenario=False)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    