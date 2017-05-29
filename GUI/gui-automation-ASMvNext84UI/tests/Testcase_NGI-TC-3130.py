'''
Created on July 08, 2016

@author: ankit.manglic
Description: Import AD users and assign Read only user role
Test Flow    : 1) Login as admin.
               2) Navigate to Users page and create a new directory.
               3) Import the user and assign Read Only Role
               
Pre-requisite : A group with users and a user should be created.
               
'''
from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Import AD users and assign Read only user role
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")        
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
      
      
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        #logout of application
        self.logout()
 
  
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        self.verifyImportADUsers(self.dirType, self.dirName, self.dirUname, self.dirPass, self.host, self.port, self.protocol, self.baseDN, self.dirFilter, self.uNameAttr, self.fNameAttr, self.lNameAttr, self.emailAttr, self.dirSource, self.searchTerm, self.importRole, "pass")
    
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S') 
        
        self.dirType = "Microsoft Active Directory"
        self.dirName = "TestDirectory" + currentTime
        self.dirUname = "hcltest@ess.delllabs.net"
        self.dirPass = "hcl@1234"
        self.host = "172.31.62.1"
        self.port = "389"
        self.protocol = "Plain"
        self.baseDN = "CN=Users,DC=ess,DC=delllabs,DC=net"
        self.dirFilter = "objectClass=*"
        self.uNameAttr = "samAccountName"
        self.lNameAttr = "givenName"
        self.fNameAttr = "sn"
        self.emailAttr = "email"
        self.dirSource = self.dirName
        self.searchTerm = "TestAutowithuser"
        self.importRole = "Read Only"
        
        self.browserObject = globalVars.browserObject
 
        self.preRunSetup()
         
        self.runTestCase()
          
        self.postRunCleanup()
        