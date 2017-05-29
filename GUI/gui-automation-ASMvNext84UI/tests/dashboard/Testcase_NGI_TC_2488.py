'''
Created on Dec 1, 2015

@author: rajeev.kumar

Description : Verify that for a Read-only user, the Total services under Service Overview on the Dashboard gives the correct 
information.
Test Flow  :1)Login as a Admin user & Create/verify Read-only user 
            2)Login as a Read-only user and Landing to Dashboard page & verify Total services under Service Overview on Dashboard

'''
from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Verify that for a Read-only user, the Total services under Service Overview on the Dashboard gives the correct information.
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
        

        self.get_DashboardPage("Total Services")
        
        #Logout Read-only user
        self.logout()