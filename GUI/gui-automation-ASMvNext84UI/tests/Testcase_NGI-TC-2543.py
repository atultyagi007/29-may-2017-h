'''
Author: nidhi.aishwarya
Created Date: Dec 11, 2015
Description:  Verify that for a Read-only user, the overall storage volume utilization on the Dashboard is correct.
Test Flow : 1) Login as ReadOnly user
            2) Click on storages tooltip at Storage Volumne Utilization graph on  dashboard page and perform verifications             
'''
from globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for a Read-only user, the overall storage volume utilization on the Dashboard is correct.
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
        
    
        self.get_DashboardPage("Total Storage Utilization")
        
        self.logout()
        
        
        
    
        
