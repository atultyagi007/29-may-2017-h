'''
Author: nidhi.aishwarya
Created Date: Dec 7, 2015
Description:  Verify that for a Standard user, the Utilization by server pool on the Dashboard is correct and displays only the server pools that the Standard user has access to.
Test Flow : 1) Login as Standard user
            2) Verify Servers used and Total Servers on Server pools page with Dashboard page       
'''
from globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for a Standard user, the Utilization by server pool on the Dashboard is correct and displays only the server pools that the Standard user has access to.
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
        
        self.logout()
        
        
        
    
        
