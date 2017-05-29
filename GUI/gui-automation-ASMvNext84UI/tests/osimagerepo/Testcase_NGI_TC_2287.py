'''
Author: Raj.Patel
Created Date: Sep 13, 2016
Description: Verify that the Add process is successful if the path and credentials are all correct and the OS repo is downloaded successfully.
'''
from tests.globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Successful download from CIFS share.       
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
        
        osrepofromFTPPath={'Repository Name': 'centos6.5', 
                           'Source Path': '\\10.255.7.219\SELab\LAB\ISOs\CentOS-7-x86_64-DVD150301.iso', 
                           'User Name': '', 
                           'Password': '', 
                           'Image Type': 'Red Hat 7'}
        
        self.addOSRepoFromPath(osrepofromFTPPath,verifyOSRepo=True)
        
        self.logout()
        
        