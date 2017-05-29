'''
Author: Raj.Patel
Created Date: Sep 19, 2016
Description: Verify that if the specified path is incorrect, then ASM throws an error.
Covered test cases 2200.
Merged Testcases : NGI_TC_2200 : Share does not exists.
'''
from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that if the specified path is incorrect, then ASM throws an error.       
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
        
        osrepofromFTPPath={'Repository Name': 'TestRepo', 
                           'Source Path': '\\10.255.7.219\SELab\LAB\ISOs\Cent-7-x86_64-DVD150301.iso', 
                           'User Name': '', 
                           'Password': '', 
                           'Image Type': 'Red Hat 7'}
        
        self.verifyErrorRepopath(osrepofromFTPPath)
        
        self.logout()
        
        