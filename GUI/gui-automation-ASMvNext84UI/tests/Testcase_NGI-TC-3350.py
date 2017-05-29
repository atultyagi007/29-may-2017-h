'''
Author: P.Suman
Created Date: Dec 15, 2015
Description: Verify that the user is unable to edit or delete default credentials.
'''

from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Unable to edit or delete default credentials.
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
        credentials=["Dell chassis default", "Dell switch default", "Dell PowerEdge iDRAC Default", "Dell PowerEdge BMC Default"]
        for credential in credentials:
            opts = self.getCredentialOptions(credential)            
            if opts["Edit"] != "Disabled" or opts["Delete"] != "Disabled":
                self.failure("Some Options are Enabled for Credential '%s' which should be Disabled :: %s"%(credential, str(opts)), raiseExc=True)                
            else:
                self.succeed("'Edit' and 'Delete' Options are Disabled for Credential '%s' :: %s"%(credential, str(opts)))
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()