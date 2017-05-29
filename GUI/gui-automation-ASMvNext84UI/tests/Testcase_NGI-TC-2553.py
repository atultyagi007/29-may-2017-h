'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that the Admin is able to edit a Read-only user and disable it
'''

from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Edit a Read-only user and disable it.
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
        
        #Verify Login user is Admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Verify Readonly user is able to login if Enabled
        self.verifyCurrentUser(userRole='Read only', loginAsUser=False)
        
        #Disable Readonly User
        self.disableLocalUser(userName=globalVars.readOnlyUser, verifyUser=True, useEdit=True, currentPassword=self.loginPassword)