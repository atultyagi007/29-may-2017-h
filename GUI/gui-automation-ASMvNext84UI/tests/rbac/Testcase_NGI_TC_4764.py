'''
Author: P.Suman
Created Date: Nov 2, 2015
Description: Verify that on the Templates page, the Standard user can view the templates in list form and block form.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager):
    """
    Standard User can view the templates in list form and block form.
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
        
        #Verify My Templates in List form
        tempList = self.getTemplates(option="My Templates", viewType="List")
        if len(tempList) > 0:
            self.succeed("Able to read 'Sample Templates' in 'List' Form :: %s"%str(tempList))
        else:
            self.succeed("No Templates in 'Sample Templates' Tab to view in 'List' form")
        
        #Verify My Templates in Icons form
        tempList = self.getTemplates(option="My Templates", viewType="Icons")
        if len(tempList) > 0:
            self.succeed("Able to read 'My Templates' in 'Icons' Form :: %s"%str(tempList))
        else:
            self.succeed("No Templates in 'My Templates' Tab to view in 'Icons' form")
        
        self.logout()    