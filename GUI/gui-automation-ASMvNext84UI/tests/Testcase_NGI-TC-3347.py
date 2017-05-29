'''
Author       : raj.patel
Created Date : Jan 20, 2016
Description  : Verify that Add attachment option on template allows user to upload documents. The max size is 50MB.
Test Flow    : 1) Login as admin create a template.
               2) Add attachment option on template allows user to upload documents. The max size is 50MB
               3)Please copy and past recharge and sng file in docs folder in Project
'''

from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Verify that Add attachment option on template allows user to upload documents.
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
        #Create Template if not exists
        self.createTemplateBasic(self.templateName, self.storageName, publishTemplate=True, volumeName=self.volumeName, volumeSize=self.volumeSize)
        
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        self.deleteTemplate(self.templateName)
        self.logout()
 
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        self.addAttachment(self.templateName)
     
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
        self.templateName = "HCLTemplate" + currentTime
        self.volumeName = "HclVolume" + currentTime
        self.volumeSize = "50GB" 
        self.storageName = "testStorage"
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
        
        
