'''
Created on Jan 20, 2016

@author: rajeev.kumar
Description :View bundles should show relative path to the bundle thats uploaded.
Test Flow   :1)Login as a Admin user
            :2)upload the custom bundles
PreRequisties:Firmware Catalog file should be there.

'''

from globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    View bundles should show relative path to the bundle thats uploaded.
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
        #check & update Virtual Appliance
        self.getOptionDetails_CustomBundles("Repositories")
        
        self.logout()