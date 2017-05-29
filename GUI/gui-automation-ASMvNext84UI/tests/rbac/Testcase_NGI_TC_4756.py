'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that on the Application logs page, the Read-only user is unable to Export all and Purge the Application logs
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only unable to Export all and Purge the Application logs.    
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        @testcase: 99491
        Prerequisite : Read-only unable to Export all and Purge the Application logs
        """
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Verify Options
        self.verifyOptions(pageName="Logs")
