'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that the Read-only user can view the following settings: Application logs, 
    Backup and Restore, Credentials Management, Networks, Users, Virtual Appliance Management and Virtual Identity Pools. 
    Getting Started is not listed.
'''

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Available options under Settings for Read-only.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        @testcase: 99489
        Prerequisite : Available options under Settings for Read-only.
        """
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Verify Options
        self.verifyOptions(["Getting Started"])
