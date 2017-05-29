'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that the Admin is able to edit a Standard user and disable it
'''

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Edit a Standard user and disable it
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
        
        #Verify Readonly user is able to login if Enabled
        self.verifyCurrentUser(userRole='Standard', loginAsUser=False)
        
        #Disable Readonly User
        self.disableLocalUser(userName=globalVars.standardUser, verifyUser=True, useEdit=True, currentPassword=self.loginPassword)
        time.sleep(5)
        
        self.enableLocalUser(userName=globalVars.standardUser, verifyUser=True)