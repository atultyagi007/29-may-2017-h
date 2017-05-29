'''
Author: nidhi.aishwarya
Created Date: Dec 7, 2015
Description:  Verify that for a Read-only user, the Utilization by server pool on the Dashboard displays all the server pools and is correct.
Test Flow : 1) Login as ReadOnly user
            2) Click on the each server at Server Utilization section on dashboard page and perform verifications for Calculations            
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for a Read-only user, the Utilization by server pool on the Dashboard displays all the server pools and is correct.
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
    
        self.get_DashboardPage("Server Utilization")
        
        self.logout()
        
        
        
    
        
