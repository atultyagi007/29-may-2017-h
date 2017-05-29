'''
Author: P.Suman
Created Date: Nov 18, 2015
Description: Verify that while creating a template, the Admin is unable to grant access of the service to Read-only users.
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    While creating a template, the Admin is unable to grant access of the service to Read-only users.
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
        templateName="Test Template"
        self.browserObject = globalVars.browserObject
        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
        #Check for read only user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=False)
        
        #Creates Sample Template if not exists
        self.createSampleTemplate(templateName=templateName, deleteAndCreate=True, publishedTemplate=False, userList=[globalVars.readOnlyUser], 
                            negativeScenario=True)