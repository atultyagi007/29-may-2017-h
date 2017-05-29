'''
Author: P.Suman
Created Date: Oct 30, 2015
Description: Verify that on the Users page, the Read-only user can select the Directory Service and view all the information.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only can select the Directory Service and view all the information.
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
        dsList = self.getDirectoryServices()
        if len(dsList) > 0:
            self.succeed("Read only User is able to read all Directory Service information")
        else:
            self.failure("There are no Directory Services to view information", resultCode=BaseClass.OMITTED, raiseExc=True)
