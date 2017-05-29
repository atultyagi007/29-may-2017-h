'''
Author       : ankit.manglic
Created Date : Aug 08, 2016
Description  : Verify that if a Admin becomes a Read-only, the change takes effect correctly. Verify all pages and settings.
Test Flow    : 1) Login as admin and change the role of Admin user to Read only.
               2) Login as RadOnly user and verify elements are disable on Dashboard, Service and Resources page. 
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
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True) 
        # Create admin user
        self. createLocalUser("AdminTempUser","Pass1234")       
        #Change standard user Role to Read Only
        self.changeUserRole(userName='AdminTempUser', currentPassword=self.loginPassword, newRole="Read only")
        #Login as standard user with Read only role.
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
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
        self.changeUserRole(userName='AdminTempUser', currentPassword=self.loginPassword, newRole="Administrator")
        
        self.logout()
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.runTestCase()
        
        self.postRunCleanup()
        
        
         