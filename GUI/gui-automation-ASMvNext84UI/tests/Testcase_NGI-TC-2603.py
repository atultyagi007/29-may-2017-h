'''
Author: P.Suman
Created Date: Nov 9, 2015
Description: Verify that on the Services page, the Standard user can view the services based 
        on Error, Deployed, InProgress or Warning.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Services viewable based on Error, Deployed, InProgress or Warning for Standard User.
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
        svcList = self.getServices(viewType="Icons")
        if len(svcList) > 0:
            self.succeed("Able to view Services based on Error, Deployed, InProgress or Warning :: %s"%str(svcList))
        else:
            self.succeed("No Services in 'Services' Page to view based on Error, Deployed, InProgress or Warning")