'''
Created on Jan 23, 2017

@author: preetam.sethi
Description : Verify catalog not in use can be deleted
Test Flow   :In the Repository page, ensure a catalog not in use can be deleted. 
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify catalog not in use can be deleted
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
        repoList=self.getRepositories()
        repoListNames=[repo['Repository Name'] for repo in repoList if repo['Repository Name']!='ASM Minimum Required' and repo['State']!='Copying']
        if len(repoListNames)==0:
            self.omit("No not-in-use catalog available")
        repoName=repoListNames[0]
        try:
            self.deleteRepository(repoName)
            self.succeed('Able to delete not in use catalog')
        except:
            self.failure('Unable to delete not in use catalog')
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    