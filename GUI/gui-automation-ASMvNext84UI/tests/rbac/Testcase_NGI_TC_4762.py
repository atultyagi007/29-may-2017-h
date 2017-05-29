'''
Author: P.Suman
Created Date: Nov 10, 2015
Description: Verify that on the Services page, the Read-only user can 
            view the services in list form and block form.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Services viewable in list form and block form for Read-only.
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
        
        #Verify Services in List form
        svcList = self.getServices(viewType="List")
        if len(svcList) > 0:
            self.succeed("Able to view Services in 'List' Form :: %s"%str(svcList))
        else:
            self.succeed("No Services in 'Services' Page to view in 'List' Form")
        
        #Verify Services in Icons form
        svcList = self.getServices(viewType="Icons")
        if len(svcList) > 0:
            self.succeed("Able to view Services in 'Icons' Form :: %s"%str(svcList))
        else:
            self.succeed("No Services in 'Services' Page to view in 'Icons' Form")