'''
Created on Dec 10, 2015

@author: rajeev.kumar

Description:Verify that on the All Resources page, the Read-only is unable to Launch element manager on each of the resources.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    the Read-only is unable to Launch element manager on each of the resources.
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
        
        self.get_Check_Element_Resources("All")
        
        self.logout()