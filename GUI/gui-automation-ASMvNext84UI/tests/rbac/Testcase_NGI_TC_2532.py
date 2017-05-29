'''
Author: P.Suman
Created Date: Oct 20, 2015
Description: Verify that the Read-only can navigate to the Resources page by selecting Resources
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Resources page for Read-only
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Verify Options
        resList = self.getResources()
        self.succeed("Able to Navigate to Resources Page and View Resources :: %s"%str(resList))
        