'''
Author: nidhi.aishwarya
Created Date: Dec 9, 2015
Description:  Verify that for a Standard user, the Total server utilization on the Dashboard is correct and displays only the server pools that the Standard user has access to, including the Global pool.
Test Flow : 1) Login as Standard user
            2) Verify Total server utilization graph tooltip on dashboard              
'''
from globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for a Read-only user, the Total server utilization on the Dashboard displays all the server pools and is correct.
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
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)

        self.get_DashboardPage("Server Utilization")
        
        self.get_DashboardPage("Total Server Utilization")
        
        self.logout()
        
        
        
    
        
