'''
Created on Dec 17, 2015

@author: rajeev.kumar
Description:Verify that on the All Resources page, the Standard user can View details of each of the resources he has access to.

'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
   All Resources page, the Standard user can View details of each of the resources he has access to.
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
        #navigate to Resource page
        #self.get_Verify_Individual_Resources("All")
        self.verifyViewDetails(resourceType="All", verifyAll=False, negativeScenario=False)
        
        self.logout()