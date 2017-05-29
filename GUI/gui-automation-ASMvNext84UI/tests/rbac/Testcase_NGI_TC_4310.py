'''
Created on Jan 5, 2016

@author: rajeev.kumar
Description:Verify that, on the Service detail page for an warning service, the Standard user can Delete, Retry, View all settings,
 Migrate, Adjust Resources and Export to file for a service created by self.
 PreRequisites : Warning Service is present 

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    the Service detail page for an warning service, the Standard user can Delete, Retry, View all settings,
 Migrate, Adjust Resources and Export to file for a service created by self.
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
        
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)    

        self.get_ServicesPage("Standard","WarningServices")
        
        self.logout()