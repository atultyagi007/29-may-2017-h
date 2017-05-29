'''
Created on Dec 28, 2015

@author: waseem.irshad
'''

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that on Settings page, the Read-only user can select the Virtual Identity Pool option and view all the information.
    
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
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
            
        #Verify 
        resList = self.getVirtualIdentityDetails()
        if len(resList) > 0:
            self.succeed("Able to fetch All Resources Info :: %s"%(str(resList)))
        else:
            self.succeed("There are no Resources on Resources Page :: %s"%(str(resList))) 
            
        self.logout()    