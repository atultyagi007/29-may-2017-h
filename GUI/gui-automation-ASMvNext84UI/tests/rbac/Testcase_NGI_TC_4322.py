'''
Created on Dec 14, 2015

@author: rajeev.kumar
Description:Verify that on the Service detail page, the Standard user can adjust any resources in a service owned by him.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    the Standard user can adjust any resources in a service owned by him
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
        
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        self.check_Service_OwnedBy_Standard("Standard")
        
        self.logout()