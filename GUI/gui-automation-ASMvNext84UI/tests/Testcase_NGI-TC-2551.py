'''
Author: P.Suman
Created Date: Sep 28, 2015
Description: Verify that on the Backup and Restore page, the Read-only user is unable to 
    initiate a Backup Now or Restore Now.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only unable to initiate a Backup Now or Restore Now
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
        self.verifyOptions(optionList=["Backup Now", "Restore Now"], pageName="BackupAndRestore")
