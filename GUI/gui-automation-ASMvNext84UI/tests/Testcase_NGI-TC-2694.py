'''
Author: P.Suman
Created Date: Nov 19, 2015
Description: Verify that if the Admin assigns all users while creating template without creating any user, 
            there is no failure
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Assign all users while creating template without creating any user
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
        
        #Deleting Standard User
        userList = self.getLocalUsers(userName=globalVars.standardUser)
        if len(userList) > 0:
            self.deleteLocalUser(globalVars.standardUser, verifyUser=True)
        
        #Creates Sample Template if not exists
        self.createSampleTemplate(templateName=templateName, deleteAndCreate=True, publishedTemplate=True)
