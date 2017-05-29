'''
Created on Nov 30, 2015

@author: rajeev.kumar

Description :Verify that for a Standard user, the Total services under Service Overview on the Dashboard gives the correct information.
Test Flow   :1)Login as a Admin & create/verify standard user
            2)Login as a standard user & verify  the Total services under Service Overview on the Dashboard
'''
from globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Standard user, the Total services under Service Overview on the Dashboard gives the correct information.
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
        self.get_DashboardPage("Total Services")
        
        #logout Standard user
        self.logout()
        