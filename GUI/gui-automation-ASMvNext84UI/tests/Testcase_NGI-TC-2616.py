'''
Author: P.Suman
Created Date: Nov 3 2015
Description: Verify that on the Resources page, the Standard user can view All Resources and Server Pools.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user can view All Resources and Server Pools
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
        
        #Get All Resources
        resList = self.getResources()
        if len(resList) > 0:
            self.succeed("Able to fetch Resources Info :: %s"%(str(resList)))
        else:
            self.succeed("No Resources were Discovered :: %s"%(str(resList)))
        
        #Get All Server Pools
        poolList = self.getServerPools()
        if len(poolList) > 0:
            self.succeed("Able to fetch Server Pool Info :: %s"%(str(poolList)))
        else:
            self.succeed("There are No Server Pools :: %s"%(str(poolList)))
