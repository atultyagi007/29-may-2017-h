'''
Author: Raj.Patel
Created Date: Sep 19, 2016
Description: Verify that the Remove option removes the repo from ASM.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that the Remove option removes the repo from ASM       
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
        utility.execLog("Add Repository class object")
        
        osrepofromFTPPath={'Repository Name': "TestRepoName", 
                           'Source Path': 'ftp://172.24.3.50/Projects/HCL/ISO/RHEL-7.2.iso', 
                           'User Name': '', 
                           'Password': '', 
                           'Image Type': 'Red Hat 7'}
        
        self.addDeleteOS(osrepofromFTPPath,verifyOSRepo=True)
        
        self.logout()
        
        