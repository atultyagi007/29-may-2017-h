'''
Author       : ankit.manglic
Created Date : Jan 04, 2016
Description  : Verify that if a Standard user becomes a read-only, the change takes effect correctly. Verify all pages and settings.
Test Flow    : 1) Login as admin and change the role of Standard user to Read only.
               2) Login as standard user and verify elements are disable on Dashboard, Service and Resources page. 
'''

from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)


class Testcase(Manager.Manager): 
    """
    Standard user becomes a read-only, the change takes effect correctly.
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
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)        
        #Change standard user Role to Read Only
        self.changeUserRole(userName=globalVars.standardUser, currentPassword=self.loginPassword, newRole="Read only")
        #Login as standard user with Read only role.
        self.verifyCurrentUser(userRole='Standard', loginAsUser=True)
        #Verify Settings for standard user with role Read only
        self.verifyReadOnlyUserPagesSettings()
        
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        #Again Login with Admin user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Change standard user Role to standard
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
        
        
         