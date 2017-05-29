'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that on the Users page, the Read-only user is unable to create, edit, disbale, 
            enable or delete any user
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only unable to create, edit, disbale, enable or delete any user
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
        self.verifyUserOptions(["Create", "Edit", "Disable", "Enable", "Delete"])
