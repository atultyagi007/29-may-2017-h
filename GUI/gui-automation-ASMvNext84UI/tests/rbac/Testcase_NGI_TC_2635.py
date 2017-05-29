'''
Author       : ankit.manglic
Created Date : Jan 14, 2016
Description  : Verify that if a standard user becomes a admin, the change takes effect correctly. Verify all pages and settings.
Test Flow    : 1) Login as admin and change the role of Standard user to Admin.
               2) Login as standard user and verify elements are enabled on Dashboard, Service and Resources page which otherwise are disabled. 
'''

from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)


class Testcase(Manager.Manager): 
    """
    Standard user becomes a Admin, the change takes effect correctly.
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs) 
        
        
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)        
        #Change Standard user Role to Admin
        self.changeUserRole(userName=globalVars.standardUser, currentPassword=self.loginPassword, newRole="Administrator")
        #Login as Read-only user with standard role.
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #Verify Settings for Read-only user with standard role
        self.verifyAdminRolePagesSettings()
        
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        #Again Login with Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Change Standard user Role to Standard
        self.changeUserRole(userName=globalVars.standardUser, currentPassword=self.loginPassword, newRole="Standard")
        
        self.logout()
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.runTestCase()
        
        self.postRunCleanup()
        
        
         