'''
Created on Jan 13, 2016

@author: rajeev.kumar
Description:While deploying a service, if the Admin selects Deploy Now, the 
deployment starts immediately.
Test Flow  :1)Login as a Admin user
            2)create Template & Deploy a service with Deploy now options.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    deploying a service, if the Admin selects Deploy Now, the deployment starts
    immediately.
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
        #check service device console
        status = self.get_check_DeployServices_Now("Administrator")
        
        self.logout()