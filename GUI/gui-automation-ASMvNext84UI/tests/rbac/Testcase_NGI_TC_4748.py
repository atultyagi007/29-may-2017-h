'''
Author: P.Suman
Created Date: Nov 6, 2015
Description: Verify that on the Server Pools page, the Read-only can view all the server pools 
        and the servers and users for each pool.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only can view all the server pools and the servers and users for each pool
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
        
        #Login as Read only user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Get All Server Pools
        poolList = self.getServerPools()
        if len(poolList) > 0:
            self.succeed("Able to fetch Server Pools and Servers and Users for each Pool :: %s"%(str(poolList)))
        else:
            self.succeed("There are No Server Pools :: %s"%(str(poolList)))        