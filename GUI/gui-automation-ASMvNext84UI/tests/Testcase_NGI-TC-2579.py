'''
Author: P.Suman
Created Date: Nov 13, 2015
Description: Verify that the Standard user is unable to view Draft templates, even if he has access to it
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user unable to view draft templates
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
        templateName = "Test Template"
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Creates Sample Template if not exists
        self.createSampleTemplate(templateName=templateName, deleteAndCreate=True, publishedTemplate=False)
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Get Templates 
        tempList = self.getTemplates(templateName=templateName)        
        if len(tempList) > 0:
            self.failure("Standard User able to view Draft Templates '%s'"%templateName, raiseExc=True)
        else:
            self.succeed("Standard User not able to view Draft Template '%s' which he has access"%templateName)
