'''
Author       : raj.patel
Created Date : Feb 1, 2016
Description  : Deploy the cloned template.
Test Flow    : 1) Login as admin create a template.
               2) Deploy the cloned template
'''

from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Deploy the cloned template.
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
#         self.createTemplateBasic(self.templateName, publishTemplate=True, volumeName=self.volumeName, volumeSize=self.volumeSize)
        self.createTemplateBasic(self.templateName, self.storageName, publishTemplate=True, volumeName=self.volumeName, volumeSize=self.volumeSize)
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        self.logout()
 
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        self.deployClonedTemplate(self.templateName)
     
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
#         self.serviceName = "TestService151230061523"
        self.templateName = "HCLTestTemplate" + currentTime
        self.storageName="Test Storage"
        self.volumeName = "HclVolume" + currentTime
        self.volumeSize = "50GB" 
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
        
        self.logout()
