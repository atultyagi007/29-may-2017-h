'''
Author: nidhi-a
Created Date: Dec 3, 2015
Description: Verify that for a Standard user, the servers in use in server pools on the Dashboard is correct and displays only the server pools that the Standard user has access to.
Test Flow : 1) Login as Standard user
            2) Click on any individual Server on Server pool at dashboard page and perform verifications
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Standard user, the servers in use in server pools on the Dashboard is correct and displays only the server pools that the Standard user has access to.
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
        
        self. get_ServerPool("Standard")
        
        self.logout()