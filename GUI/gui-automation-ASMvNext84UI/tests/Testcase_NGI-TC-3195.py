'''
Created on May 9, 2016

@author: preetam.sethi
Description : Enter a valid path with valid file name, it should pass test connection

Test Flow  :1)Login as a Admin user and Enter a valid path with valid file name, it should pass test connection

prerequisites: None.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Enter a valid path with valid file name, it should pass test connection
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
#        arguments option, networkPath, localPath, makeDefault= False
        self.getTestConnection("networkPath",globalVars.catlogRepository["nfsPathXml"])
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()