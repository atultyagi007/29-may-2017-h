'''
Author: Raj.Patel
Created Date: Sep 13, 2016
Description: Verify that the Add process is successful if the path and credentials are all correct and the OS repo is downloaded successfully.
Covered test cases 2710,2151,2096,2007.
Merged Testcases : NGI_TC_2710 : Successful download using HTTP.
                   NGI_TC_2151 : Test Connection.
                   NGI_TC_2096 : Remove an added repo.
                   NGI_TC_2007 : Pending state during add.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Successful download using HTTP.       
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
        
        osrepofromFTPPath={'Repository Name': 'rhel6.7', 
                           'Source Path': '\\10.255.7.219\SELab\LAB\SaiK3\ISOs\SK_RHEL67.iso', 
                           'User Name': '', 
                           'Password': '', 
                           'Image Type': 'Red Hat 6'}
        
        self.addOSRepoFromPath(osrepofromFTPPath,verifyOSRepo=True)
        
        self.logout()
        
        