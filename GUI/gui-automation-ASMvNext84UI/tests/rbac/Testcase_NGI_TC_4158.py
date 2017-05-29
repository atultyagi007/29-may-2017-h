'''
Author: nidhi.aishwary
Created Date: Dec 25, 2015
Description: Verify that on the All Resources page, the Standard user can view all the resources that are part of a server pool for which they have permission and also view common or shared resources.
Test Flow    : 1) Login as standard user
               2) Navigate to All Resources page and perform verifications
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that on the All Resources page, the Standard user can view all the resources that are part of a server pool for which they have permission and also view common or shared resources.
    
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
        
        #Login as Standard user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
            
        #Verify Resources
        resList = self.getResources()
        if len(resList) > 0:
            self.succeed("Able to fetch All Resources Info :: %s"%(str(resList)))
        else:
            self.succeed("There are no Resources on Resources Page :: %s"%(str(resList))) 
            
        self.logout()    