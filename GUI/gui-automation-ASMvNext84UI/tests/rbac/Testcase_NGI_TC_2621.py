'''
Created on Dec 13, 2015

@author: rajeev.kumar
Description: Verify that on the All Resources page, the Standard user can Launch element manager on each of the resources he has 
access to.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Resources page, the Standard user can Launch element manager on each of the resources he has access to.
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
        
        self.get_Check_DeviceConsole("All")