'''
Author: P.Suman
Created Date: Oct 8, 2015
Description: Verify that on the Templates page, the Standard user cannot view the default templates
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user cannot view the default templates
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
        
        #Verify Sample Templates in List form
        tempList = self.getTemplates(option="Sample Templates", viewType="List")
        if len(tempList) > 0:
            self.failure("Standard User able to see Sample Templates :: %s"%(str(result)), raiseExc=True)
        else:
            self.succeed("No Templates in 'Sample Templates' Tab to view for Standard User")