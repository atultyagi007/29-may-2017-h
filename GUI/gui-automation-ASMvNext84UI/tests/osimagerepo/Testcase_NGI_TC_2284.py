'''
Author: Atul
Created Date: Sept 14, 2016
Description:  Delete an ISO during pending state:
        1:-Goto settings and goto repositories
        2:-Verify that if an iso is in pending state, it is unable to remove it.

'''
from tests.globalImports import *
from libs.product.pages import Repositories

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Delete an ISO during pending state.       
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
        #Verify Repository deletion
        self.verifyRepositoryDeletion(checkState=True)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        
        self.preRunSetup()
        self.runTestCase()
        self.postRunCleanup()
#         