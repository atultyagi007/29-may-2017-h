'''
Author: P.Suman
Created Date: Nov 13, 2015
Description: Verify that on the Templates page, the Standard user can deploy a service from a template shared by the Admin
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Standard user can deploy a service from a template shared by the Admin
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
        self.createSampleTemplate(templateName=templateName)
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        
        #Get Template Options
        self.getTemplateOptions(actualOptions=["Deploy Service"], enableOptions=["Deploy Service"], templateName=templateName)
        self.succeed("Standard user can deploy a service from a template shared by the Admin")
        
