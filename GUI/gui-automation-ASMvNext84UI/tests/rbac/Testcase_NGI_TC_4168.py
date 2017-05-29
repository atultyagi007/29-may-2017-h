'''
Created on Jan 13, 2015

@author: ankit.manglic
Description:Verify that on the Service detail page, the Read-only is unable to open Device console of the components in the services. 
Test Flow    : 1) Login as admin create a template and deploy service.
               2) Login as Read Only user and view details of the service deployed in step 1.
               3) Verify device console link is not enabled for the service. 
'''
from tests.globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    the Service detail page, the Read-only is unable to open Device console of the components in the services.
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
        self.createTemplateBasic(self.templateName, self.storageName, publishTemplate=True, volumeName=self.volumeNameTest)
        #Deploys a Service if does not exist
        self.deployService(self.templateName, self.serviceName)
     
     
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Delete service created
        self.deleteService(self.serviceName)
        #Delete Template
        self.deleteTemplate(self.templateName)
        #logout of application
        self.logout()
 
 
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Read only', loginAsUser=True)
        #Verify Device console link disabled
        self.verifyServiceDeviceConsoleLink(serviceName=self.serviceName)
        
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
        self.templateName = "TestTemplate" + currentTime
        self.storageName = "testStorage"
        self.volumeNameTest = "TestVolume" + currentTime
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
        