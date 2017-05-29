'''
Author: raj.patel
Created Date: Jan 8, 2016
Description:  Log into the ASM UI and navigate to the Dashboard page., 
 Verify that the Server Utilization in Services graphics properly display the total number of servers and their respective states (whether utilized in a deployed service or not).Include a mix of rack, blade and FX2 servers., 
 Mouse-over each status partition of the the graphic and verify that the popup changes to show the proper state and number of resources., 
 Deploy additional services and repeat Step 2 as necessary.             
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for a Admin user, the Server Health status on Dashboard is correct and complete.
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
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Administrator')
    
        self.get_Server_Health("Server Health")
        
        