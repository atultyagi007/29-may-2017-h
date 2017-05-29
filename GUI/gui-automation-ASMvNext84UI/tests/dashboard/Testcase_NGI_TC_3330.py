'''
Author: nidhi.aishwarya
Created Date: Jan 6, 2015
Description: Verify that the Total Services under Service Overview on the Dashboard gives the correct information regarding number of services and their status.
Test Flow : 1) Login as Admin user
            2) Click on Total Services link and individual link one by one at dashboard page and perform verifications                  
'''
from tests.globalImports import *
from libs.product.pages import Services

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that the Total Services under Service Overview on the Dashboard gives the correct information regarding number of services and their status.
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
       
        self.get_DashboardPage("Total Services")
        
        self.get_IndividualServices()
        
        self.logout()
       
        