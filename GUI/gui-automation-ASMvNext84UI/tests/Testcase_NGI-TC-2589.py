'''
Author: nidhi.aishwarya
Created Date: Dec 10, 2015
Description:  Verify that for a Standard user, the storage volume utilization on the Dashboard for each storage group is correct and displays the utilization where the Standard user is owner or has access to .
Test Flow : 1) Login as Standard user
            2) Verify individual storages on dashboard with Storages on Resources page            
'''
from globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for a Standard user, the storage volume utilization on the Dashboard for each storage group is correct and displays the utilization where the Standard user is owner or has access to .
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
           
        self.get_DashboardPage("Storage Volume")
        
        #logout standard user
        self.logout()
        
        
    
        
