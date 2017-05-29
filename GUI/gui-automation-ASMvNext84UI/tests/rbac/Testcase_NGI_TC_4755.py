'''
Author: P.Suman
Created Date: Oct 7, 2015
Description: Verify that on the the Virtual Identity Pools page, the Read-only user is 
            unable to update pool identities.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only unable to update pool identities
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
        
        #Verify Options
        self.verifyOptions(optionList=["Update MAC Pool", "Update IQN Pool", "Update WWPN Pool", "Update WWNN Pool"], pageName="VirtualIdentityPools")
