'''
Created on June 2, 2016

@author: preetam.sethi
Description :Try to update a resource which is in non-complaint state

Test Flow  :1.    Login as admin
            2.    Check the firmware status in Resources before doing upgrade.. It should be either Non-compliant or Upgrade required.
            3.    Do the firmware upgrade
            4.    Check the firmware status is updated to Compliant.


prerequisites:Resource should be discovered
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Try to update a C Series Server resource which is in non-complaint state
    """
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
        
   
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        
        #Login as Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        self.updateCSServerFirmware("Servers")