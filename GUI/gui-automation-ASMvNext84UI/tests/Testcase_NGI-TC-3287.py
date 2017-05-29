'''
Created on Feb 09, 2016

@author: raj.patel
Description:Export and Import Templates. 
Test Flow    : 1) Login as admin create a template.
               2) Export and Import Templates. 
'''
from globalImports import *
from datetime import datetime

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Export and Import Templates.
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
#         self.createTemplate(self.templateName, self.storageName, publishTemplate=True ,volumeName=self.volumeName)
        self.createTemplateBasic(self.templateName, self.storageName, publishTemplate=True, volumeName=self.volumeName, volumeSize=self.volumeSize)
     
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        self.logDesc("Post Run Cleanup")
        #login with admin
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Delete service created
#         self.deleteService(self.serviceName)
        #Delete Template
#         self.deleteTemplate(self.templateName)
        #logout of application
        self.logout()
 
 
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        self.exportAndImportTemplate(self.templateName)
        self.verifySpecificStandadrdUserTemplate(self.templateName)
        self.exportAndImportTemplate(self.templateName)
#         self.creatTemplate_specificStandarUser()
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
        self.serviceName = "TestService" + currentTime
        self.templateName = "TestTemplate" + currentTime
        self.storageName = "testStorage"
        self.volumeName = "HclVol"+currentTime
        self.volumeSize= "100GB"
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
        