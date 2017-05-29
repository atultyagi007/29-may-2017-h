'''
Author: rajeev.kumar
Created Date: Nov 24, 2015
Description: Verify that for a Read-only user, clicking on Total services on the Dashboard takes to Services page, which lists all
             the services.
Test Flow : 1) Login as ReadOnly user
            2) Click on Total Services link at dashboard page and perform verifications
'''
from tests.globalImports import *
from libs.product.pages import Services

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only user, clicking on Total services on the Dashboard takes to Services page, which lists all the services.
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
       
        self.get_View_Link("Total Services")
        
        self.logout()
       
        