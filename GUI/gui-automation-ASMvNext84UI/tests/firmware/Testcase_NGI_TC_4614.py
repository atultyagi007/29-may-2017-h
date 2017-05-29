'''
Created on July 20, 2016

@author: raj.patel
Description : Block IOM configuration, if not define any uplinks.
Test Flow  :1)Login as a Admin user and show the exists file and allow add new file.
prerequisites:Firmware cataloge should be there and 172.31.61.177 should be discovered.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Block IOM configuration, if not define any uplinks.
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
        chassisList=self.getResources('Dell Chassis')
        chassis=chassisList[0]
        chassisIp=chassis['IP Address']
        chasisName=chassis['Resource Name']
        self.blockIOMConfiguration(chassisIp, chasisName,upLinkCheked=True ,DefinedUplink=False,ports="")
        self.logout()