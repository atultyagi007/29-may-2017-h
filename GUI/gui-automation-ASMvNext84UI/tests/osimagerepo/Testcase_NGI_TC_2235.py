'''
Author: Atul
Created Date: Sept 14, 2016
Description:  Delete an ISO being used in deployment or service.:
        1:- Try to remove an ISO that is being used.
        2:- Verify that ASM does not allow deletion of an ISO that is currently being used in a service.
Prerequisite:- At list on repository's "In Use" state should be true.
'''
from tests.globalImports import *
from libs.product.pages import Repositories

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Delete an ISO being used in deployment or service.      
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
        self.verifyRepositoryDeletion(checkUse=True,checkState=False)
    
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