'''
Author: P.Suman
Created Date: Oct 1, 2015
Description: Verify that the deleted Read-only users are not listed in users
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Deleted Read-only user is not listed in users
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
        
        #Verify whether logged in User is admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=False)
        
        #Delete Readonly User
        self.deleteLocalUser(userName=globalVars.readOnlyUser, verifyUser=True)