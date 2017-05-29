'''
Author: nidhi.aishwarya
Created Date: Jan 11,2016
Description:  Verify Total Storage Capacity and Capacity by Storage Group on the Dashboard
Test Flow : 1) Login as Admin user
            2) Click on Storage Volume Utilization graph tooltip and each storage type on dashboard page and perform verifications             
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify Total Storage Capacity and Capacity by Storage Group on the Dashboard
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
        
    
        self.get_DashboardPage("Total Storage Utilization")
        
        self.get_DashboardPage("Storage Volume")
        
        self.logout()
        
        
        
    
        
