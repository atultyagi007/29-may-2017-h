'''
Author: P.Suman
Created Date: Oct 30 2015
Description: Verify that on the Resources page, the Read-only can view All Resources.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only can view All Resources
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
        
        #Verify Resources
        resList = self.getResources()
        if len(resList) > 0:
            self.succeed("Able to fetch All Resources Info :: %s"%(str(resList)))
        else:
            self.succeed("There are no Resources on Resources Page :: %s"%(str(resList)))