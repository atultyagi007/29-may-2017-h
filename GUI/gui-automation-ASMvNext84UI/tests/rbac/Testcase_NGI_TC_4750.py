"""
Author: P.Suman
Created Date: Nov 12, 2015
Description: Verify that the Read-only user is unable to deploy a service from a template
"""

from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only unable to deploy a service from a template
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
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Get Template Options
        self.getTemplateOptions(actualOptions=["Deploy Service"], disableOptions=["Deploy Service"], templateName=templateName)
