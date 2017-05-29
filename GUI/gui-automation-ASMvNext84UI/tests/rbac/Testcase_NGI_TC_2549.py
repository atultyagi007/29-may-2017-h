'''
Author: P.Suman
Created Date: Nov 20, 2015
Description: Verify that for each template, the Read-only can View detail of each component in the Template detail page
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Read-only can View detail of each component
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
        self.createSampleTemplate(templateName=templateName, publishedTemplate=True, deleteAndCreate=True)
         
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        
        #Get Template Options
        tempSettings = self.getTemplateSettings(templateName="Test Template", eachComponent=True)
        if len(tempSettings) <= 0:
            self.failure("Failed to fetch Template '%s' Settings :: %s"%(templateName, str(tempSettings)), raiseExc=True)
        self.succeed("Successfully fetched Template '%s' Settings :: %s"%(templateName, str(tempSettings)))
        
        #Verification
        self.verifyTemplateSettings(tempSettings)