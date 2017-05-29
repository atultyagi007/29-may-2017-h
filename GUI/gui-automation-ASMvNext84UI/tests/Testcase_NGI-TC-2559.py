'''
Author: P.Suman
Created Date: Nov 2, 2015
Description: Verify that on the Templates page, the Read-only user can view the templates based on category.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only can view the templates based on category.
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
        
        #Get Template categories
        categories = self.getTemplateCategories()
        self.succeed("Able to fetch Template Categories :: %s"%(str(categories)))
                        
        #Read Templates under each Category 
        for category in categories:
            tempCategory = self.getTemplates(option="My Templates", templateCategory=category)
            if len(tempCategory) > 0:
                self.succeed("Able to read Templates under '%s' category :: %s"%(category, str(tempCategory)))
            else:
                self.succeed("No Templates under '%s' category"%(category))