'''
Author: nidhi.aishwarya
Created Date: Nov 23, 2015
Description: Verify that for a Read-only user, clicking on any individual storage type on the Dashboard displays all the available 
             storages under Resources.
Test Flow : 1) Login as ReadOnly user
            2) Click on any individual Resource at dashboard page and perform verifications
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that for a Read-only user, clicking on any individual storage type on the Dashboard displays all the available 
             storages under Resources.
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
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Read only')
    
        self.getSection_Storage()
        
        self.logout()
        
        
        
    
        
