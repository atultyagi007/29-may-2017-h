'''
Author: nidhi.aishwarya
Created Date: Jan 11, 2016
Description:  Verify that the Utilization by server pool on the Dashboard displays all the server pools and the servers in use in each pool.
Test Flow : 1) Login as Admin user
            2) Click on Server Utilization graph tooltip and on each server at server pool at dashboard page and perform verifications             
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that the Utilization by server pool on the Dashboard displays all the server pools and the servers in use in each pool.
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
        
        self.get_DashboardPage("Server Utilization")
        
        self.get_DashboardPage("Total Server Utilization")
        
        self.logout()
        
        
        
    
        
