'''
Created on Dec 22, 2015

@author: rajeev.kumar
Description:Verify that on the Service detail page, the Standard user can open Device console of the components in the services 
he owns or has access to.

'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    the Service detail page, the Standard user can open Device console of the components in the services he owns or has access to.
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
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #check service device console
        self.get_check_Services_DeviceConsole("Standard")
        self.logout()