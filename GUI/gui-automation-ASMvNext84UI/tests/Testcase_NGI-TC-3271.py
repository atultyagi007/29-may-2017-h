'''
Created on Jan 20, 2016

@author: rajeev.kumar
Description :Verify that clicking on Update Virtual appliance initiates the upgrade. 
ASM displays a warning message indicating Logged in users, running jobs and scheduled 
jobs and asks for confirmation. Once confirmed, upgrade process is initiated. 
After successful upgrade, the login page is displayed.
Test Flow   :1)Login as a Admin user,navigate to Virtual Appliance Management & upadate
virtual appliance. 
'''

from globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Clicking on Update Virtual appliance initiates the upgrade.

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
        #check & update Virtual Appliance
        self.getOptionDetails_VirtualAppliance("VirtualApplianceManagement")
        
        self.logout()