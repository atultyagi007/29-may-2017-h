'''
Author: P.Suman
Created Date: Oct 29, 2015
Description: Verify that on the Firmware page, the Standard User is unable to View Bundles 
        of each of the repository and the details of each bundle.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard User unable to View Bundles of each of the repository and the details of each bundle
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
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Verify Getting Started Page
        self.verifyLandingPageOptions(userRole='Standard')
        
        #Verify Bundle Info
        #self.getBundleInformation("All")
        
