'''
Author: P.Suman
Created Date: Dec 15, 2015
Description: Verify that the Credential Management option allows user to Create, edit and delete resource credentials.
'''

from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Credential management option under Setting.
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
        #Delete Credential
        self.deleteCredential("testServer", ignoreFailure=True)
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        #Delete Credential
        self.deleteCredential("testServer", ignoreFailure=True)

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Create Credential
        self.createCredential("testServer", "Server", "root", "calvin")
        #Get Credentials
        creList = self.getCredentials(credentialName="testServer")
        if len(creList) > 0:
            self.succeed("Able to fetch Credential Details after Creation :: %s"%str(creList))
        else:
            self.failure("Failed to fetch Credential Details after Creation :: %s"%str(creList), raiseExc=True)
        #Edit Credential
        self.editCredential("testServer", "Server", "testroot", "calvin")
        #Get Credentials
        creList = self.getCredentials(credentialName="testServer")
        if len(creList) > 0:
            self.succeed("Able to fetch Credential Details after Edit :: %s"%str(creList))
        else:
            self.failure("Failed to fetch Credential Details after Edit :: %s"%str(creList), raiseExc=True)
        #Delete Credential
        self.deleteCredential("testServer")
        #Get Credentials
        creList = self.getCredentials(credentialName="testServer")
        if len(creList) > 0:
            self.failure("Able to fetch Credential Details after Deletion :: %s"%str(creList), raiseExc=True)
        else:
            self.succeed("Failed to fetch Credential Details after Deletion :: %s"%str(creList))
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()