'''
Created on July 21, 2016

@author: raj.patel
Description : Remove the rule which only applicable to FNIOA and need to verify this is in the UI.
Test Flow  :1)Login as a Admin user and show the exists file and allow add new file.
prerequisites:Firmware cataloge should be there.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Remove the rule which only applicable to FNIOA and need to verify this is in the UI.
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
        
        #Login as Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
#         self.configureFXChassis("Chassis")
        chassisList=self.getResources('Dell Chassis')
        chassis=chassisList[0]
        chassisIp=chassis['IP Address']
        chasisName=chassis['Resource Name']
        self.blockIOMConfiguration(chassisIp, chasisName,upLinkCheked=True ,DefinedUplink=True,ports="VLT")

#         self.logout()