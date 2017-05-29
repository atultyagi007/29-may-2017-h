'''
Author: P.Suman
Created Date: Nov 5, 2015
Description: Verify that the Standard user can navigate to the Services page by selecting Services
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Services page for Standard User
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
        
        #Verify Services in List form
        svcList = self.getServices()
        if len(svcList) > 0:
            self.succeed("Able to read Services in 'List' Form :: %s"%str(svcList))
        else:
            self.succeed("No Services in 'Services' Page to view in 'List' form")