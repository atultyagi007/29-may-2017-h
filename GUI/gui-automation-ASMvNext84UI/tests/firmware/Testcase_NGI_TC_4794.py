'''
Created on Feb 21, 2017

@author: preetam.sethi
Description : Verify a Deployment will error out if selected a Default catalog for In service Update and there is no default catalog
Test Flow   :   Deploy a service. In the wizard, select to manage the firmware and select the ASM default catalog.
                Click on Finish to kick off the deployment.

'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)
import datetime
class Testcase(Manager.Manager): 
    """
    Verify a Deployment will error out if selected a Default catalog for In service Update and there is no default catalog
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
        volName='HCLVol'+datetime.datetime.now().strftime("%H%M")
        storageList=self.getResources("Storage")
        storageNameList=[storage["Resource Name"] for storage in storageList if "EqualLogic" in storage['Manufacturer /Model']]
        deviceName=storageNameList[0]
        self.components='{"Storage1":{"Type":"EqualLogic", "Name":"%s", "VolumeName":"%s", "Size":"10GB","AuthType":"IQN/IP","AuthUser":"grpadmin","AuthPwd":"dell1234","IQNIP":"172.31.64.241"}}'%(deviceName,volName)     
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
        defaultRepo=self.getDefaultRepository()
        if defaultRepo!='Select':
            self.omit("Default repository set to %s"%defaultRepo)
        templateName="Template_4794"
        self.createTemplate(templateName=templateName,components=self.components, publishTemplate=True,manageFirmware=True, repositoryName="Use ASM appliance default catalog")
        #Deploy Service
        #=======================================================================
        self.serviceName = "Service_" + templateName
        try:
            self.deployService(templateName, serviceName=self.serviceName)
            self.failure('Deployment Initiated successfully', raiseExc=True)
        except:
            self.succeed('Deployment Initiation Failed')
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    