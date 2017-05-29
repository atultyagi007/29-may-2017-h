'''
Created on Feb 9, 2016

@author: rajeev.kumar
Description :Edit bundle should show the file thats already exists, and should allow to
add new file.
Test Flow  :1)Login as a Admin user and show the exists file and allow add new file.
prerequisites:Firmware catloge should be there.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Edit bundle should show the file thats already exists, and should allow to Add new
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
        
        #Login as Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        self.getCustomBundle_AddORexist("Repositories")
        self.logout()