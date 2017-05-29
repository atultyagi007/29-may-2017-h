'''
Created on Nov 24, 2015

@author: rajeev.kumar

Description:Verify that for a Standard user, clicking on Total services on the Dashboard takes to Services page, 
which lists the services created by the Standard user or given access to by the Admin.
Test Flow:1)Login as a Admin & create/verify standard user
            2)Login as a standard user & clicking on Total services on the Dashboard takes to Services page.

'''
from globalImports import *
from libs.product.pages import Services

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   Verify that for a Standard user, clicking on Total services on the Dashboard takes to Services page,which lists the services 
   created by the Standard user or given access to by the Admin.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        utility.execLog(tc_id)
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
      
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Standard')
        self.getCurrentUser()
        self.get_View_Link("Total Services")
        
        #logout Standard user
        self.logout()
        
