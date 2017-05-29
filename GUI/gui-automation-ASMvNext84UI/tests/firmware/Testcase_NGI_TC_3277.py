'''
Created on Jan 22, 2016

@author: rajeev.kumar
Description :when server is part of a failed deployment, try to update FW.
Test Flow   :1)Login as a Admin user & Unable to update Error compilance state server firmware.
PreRequisties:Error state compliance Server should be there.
'''
from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
  when server is part of a failed deployment, try to update FW.

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
        #self.getOptionDetails_ServerResources("Resources")
        self.isUpdateFirmwareEnabled(["172.31.60.177"], negativeScenario=True)
        
        self.logout()
