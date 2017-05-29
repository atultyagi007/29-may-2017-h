'''
Created on Jan 25, 2016

@author: rajeev.kumar
Description :update FW on 2 updated required server at same time.
Test Flow   :1)Login as Admin user & update firmware on updated required state.
PreRequisties:Updated Required state Servers firmware should be there.

'''
from globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
Update firmware for a server whose firmware status is pdate Required.
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
        #self.getOptionDetails_ServerUpdatedRequired("Resources")
        self.isUpdateFirmwareEnabled(["172.31.60.177", "172.31.60.182"], negativeScenario=False)
        
        self.logout()
