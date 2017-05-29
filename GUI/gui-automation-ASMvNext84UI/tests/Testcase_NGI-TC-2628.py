'''
Author: nidhi.aishwarya
Created Date: Nov 24, 2015
Description: Verify that for a Standard user, clicking on any individual server pool on the Dashboard displays information about that server pool under Resources.
Test Flow  :1)Login as standard user
            2)Click on individual server pool under Server pool utilization section on dashboard and perform verifications
'''
from globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user, clicking on any individual server pool on the Dashboard displays information about that server pool under Resources
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
        
        self.getSection_ServerPool()
        
        self.logout()
        
        
    
        
