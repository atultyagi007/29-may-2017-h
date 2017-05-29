'''
Author       : ankit.manglic
Created Date : Aug 08, 2016
Description  : Verify Standard user can Run Inventory on each of the resources he has access to.
'''

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)


class Testcase(Manager.Manager): 
    """
    Standard user can Run Inventory on each of the resources he has access to.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs) 
        
        
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)  
        #Run inventory
        self.runInventory("HCLDNS04")
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        
        self.logout()
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.runTestCase()
        
        self.postRunCleanup()
        
    