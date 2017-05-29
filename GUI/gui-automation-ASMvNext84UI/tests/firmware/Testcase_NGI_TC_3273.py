'''
Created on Jan 25, 2016

@author: rajeev.kumar
Description :Update firmware for a server whose firmware status is unknown (NON Dell hardware)- button not enabled.
Test Flow   :1)Login as a Admin user
            :2)Select Server whose state is unknown and unable to update his firmware.
PreRequisties:Unknown state Servers should be there.

'''
from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
Update firmware for a server whose firmware status is unknown (NON Dell hardware)
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
        #resList = self.getResources("Servers")
        #check & update Virtual Appliance
        #self.getOptionDetails_ServerUnknown("Resources")
        self.isUpdateFirmwareEnabled(["172.31.60.177"], negativeScenario=True)
        
        self.logout()