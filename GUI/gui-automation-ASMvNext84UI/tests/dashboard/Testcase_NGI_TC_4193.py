'''
Author: nidhi.aishwarya
Created Date: Dec 11, 2015
Description:  Overall storage volume utilization on the Dashboard for Standard user.
Test Flow  :1)Login as a Admin & create/verify standard user
            2)Login as a standard user & verify Overall storage volume on dashboard page.
             
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Overall storage volume utilization on the Dashboard for Standard user.
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
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Standard')
    
        self.get_DashboardPage("Total Storage Utilization")
        
        #logout Standard user
        self.logout()
        
        
        
    
        
