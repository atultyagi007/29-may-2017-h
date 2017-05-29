'''
Author: Atul
Created Date: Sept 14, 2016
Description: Error state if any problem.
1.Add a repository and Delete it during copying state to make it's state error.
2.Show error message.

'''
from tests.globalImports import *
from libs.product.pages import Repositories

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Error state if any problem.  
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
        #Login again with wrong credentials
        self.testErrorRepository()
    
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