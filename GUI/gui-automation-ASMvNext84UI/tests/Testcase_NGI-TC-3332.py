'''
Author: raj.patel
Created Date: Jan 8, 2016
Description:Verify that for a Admin user, the Total server utilization on the Dashboard displays all the server pools and is correct  
Test Flow    : 1) Log into the ASM UI and navigate to the Dashboard page., 
               Verify that the Server Utilization in Services graphics properly display the total number of servers and their respective states (whether utilized in a deployed service or not).Include a mix of rack, blade and FX2 servers., 
              2) Mouse-over each status partition of the the graphic and verify that the popup changes to show the proper state and number of resources., 
              3)Deploy additional services and repeat Step 2 as necessary.             
'''
from globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for a Admin user, the Total server utilization on the Dashboard displays all the server pools and is correct.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
       
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
      
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")        
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        
        self.logout()
        
    
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Check for current logged in user
        self.verifyLandingPageOptions(userRole='Administrator')
    
        serviceName = self.get_deploy_Template("Server Template")

        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
