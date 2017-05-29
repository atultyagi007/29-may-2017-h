'''
Author: nidhi.aishwarya
Created Date: Dec 3, 2015
Description: Verify that for a Read-only user, the servers in use in each server pools on the Dashboard is correct.
Test Flow : 1) Login as ReadOnly user
            2) Click on any individual Servers in Server pool at dashboard page and perform verifications
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Read-only user, the servers in use in each server pools on the Dashboard is correct.
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
        
        self.get_ServerPool("ReadOnly")
        
        self.logout()