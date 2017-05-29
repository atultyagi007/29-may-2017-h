'''
Created on Feb 9, 2016

@author: rajeev.kumar
Description :Delete user bundle(custom)
Test Flow   :1)Login as aAdmin user and Delete existing bundles.

'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Delete user bundle(custom)
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
        
        #Login as Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        self.getCustomBundle_DeleteCustom("Repositories")
        self.logout()