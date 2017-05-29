'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that on Settings page, the Read-only user can select the Backup and Restore option and view all the information 
        under Settigns and Details and Scheduled backups.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Backup and Restore option for Read-only
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
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Verify Options
        self.getOptionDetails("BackupAndRestore")
