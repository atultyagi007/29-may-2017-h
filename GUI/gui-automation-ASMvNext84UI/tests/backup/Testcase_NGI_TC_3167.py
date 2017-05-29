'''
Created on Feb 8, 2016

@author: rajeev.kumar
Description : Schedule a back up to run at specific date/time
Test Flow   :Login as a Admin user and schedule as a back run time/date.
Warning    : Unable to validate Last Backup Date/status,it should take 30 minute interval time.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Schedule a back up to run at specific date/time
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
        
        self.getBackUp_ScheduleTime("BackupAndRestore")
        self.logout()