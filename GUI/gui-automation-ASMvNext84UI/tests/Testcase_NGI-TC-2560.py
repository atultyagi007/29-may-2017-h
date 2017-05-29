'''
Author: nidhi.aishwarya
Created Date: Nov 25, 2015
Description: Verify that for a Read-only user, the following information is displayed on the Dashboard: Service Overview, Deployed/Error/ In progress Services, Total Server Utilization, Total Storage capacity, Recent Activity, Learn
Note : 'License Information', 'Deploy with recently used template' Sections mentioned in testcases not present in GUI hence not automated
Test Flow : 1) Login as ReadOnly user
            2) Verify individual options on dashboard 
'''

from globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only can view the templates based on category.
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

        self.get_DashboardOptions()
        time.sleep(5)
        
        self.logout()
        
        
        
    
        
