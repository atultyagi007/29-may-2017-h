'''
Created on Jan 7, 2016
@author: HCL

Description:Log into the ASM UI and navigate to the Dashboard page. 
 Verify that clicking the links for "Learn about Service Deployments" and "Learn about Templates" will open up popup windows which display the respective help pages for each.

'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
      Verify that clicking the links for "Learn about Service Deployments" and "Learn about Templates" will open up popup windows which display the respective help pages for each.    
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
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Administrator')
        self.getCurrentUser()
        
        # Navigating to Learn Section
        self.getSection_Learn()
        