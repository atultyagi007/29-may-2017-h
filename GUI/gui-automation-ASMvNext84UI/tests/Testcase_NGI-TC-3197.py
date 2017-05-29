'''
Created on Feb 10, 2016

@author: rajeev.kumar
Description :Click on compliance report and check if its accurate with bundle version.
Test Flow   :1)Login as a Admin user and upload custom bundle
            :2)go to resource and check compilane report
prerequisites:Firmware catloge should be there.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Click on compliance report and check if its accurate with bundle version
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
        
        self.getCustomBundle_CompilaneReport("Repositories")
        self.getCustomBundle_CompilaneReports("Resources")
        self.logout()