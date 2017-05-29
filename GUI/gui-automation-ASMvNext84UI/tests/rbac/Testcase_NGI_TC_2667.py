'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that the Admin is able to create a Read-only user and enable it
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Create a Read-only user and enable it.       
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
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Delete an existing Readonly User to create one
        users = self.getLocalUsers(userName=globalVars.readOnlyUser)
        if len(users) > 0:
            self.deleteLocalUser(userName=globalVars.readOnlyUser)
        
        #Create Readonly User
        self.createLocalUser(userName=globalVars.readOnlyUser, userPassword=globalVars.rosPassword,  
                       userRole="Read only", enableUser=True, verifyUser=True)