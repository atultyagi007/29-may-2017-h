'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that on Settings page, the Read-only user can select the Users option 
            and view all the user information
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Users option for Read-only
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
        userList = self.getLocalUsers()
        if len(userList) > 0:
            self.succeed("Read only User is able to select Users option and view all User information")
        else:
            self.failure("Read only User failed to view all User information", raiseExc=True)
