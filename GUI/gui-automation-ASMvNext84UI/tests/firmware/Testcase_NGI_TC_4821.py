'''
Created on Jan 23, 2017

@author: preetam.sethi
Description : Verify the embedded or a default catalog cannot be deleted
Test Flow   :Verify the embedded or a default catalog cannot be deleted
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify the embedded or a default catalog cannot be deleted
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
        try:
            self.deleteRepository('ASM Minimum Required')
            self.failure('Able to delete Embedded Catalog')
        except:
            self.succeed('Unable to delete Embedded Catalog')
        repoList=self.getRepositories()
        defaultRepo=self.getDefaultRepository()
        if defaultRepo=='Select':
            repoListSetDefault=[repo['Repository Name'] for repo in repoList if repo['Repository Name']!='Asm Minimum Required' and repo['State']=='Available']
            if len(repoListSetDefault)==0:
                self.omit("No Suitable catalog to set default")
            defaultRepo=repoListSetDefault[0]
            self.setDefaultFirmware(defaultRepo)
        try:
            self.deleteRepository(defaultRepo)
            self.failure('Able to delete Default Catalog')
        except:
            self.succeed('Unable to delete Default Catalog')   
        
        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    