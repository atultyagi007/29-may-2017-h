'''
Author: P.Suman
Created Date: Oct 30 2015
Description: Verify that on the All Resources page, the Read-only user cannot Run Inventory on resources
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only user cannot Run Inventory on resources
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
        
        #Fetch Resources
        result = self.getResources()
        if len(result) > 0:
            self.succeed("Able to fetch Resources Info :: %s"%(str(result)))
        else:
            self.failure("There are no Resources to Run Inventory :: %s"%(str(result)), resultCode=BaseClass.OMITTED, raiseExc=True)
        
        #Run Inventory on the Resopurce
        self.runInventory("chkDevice_0", negativeScenario=True)