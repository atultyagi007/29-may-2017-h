'''
Created on March 03, 2017

@author: preetam.sethi
Description :  Verify the overall service status changes according to the catalog used.
Test Flow   :  In the Service change the catalog to different ones 2-3 times and verify that
             the overall status changes depending on the compliance of the components.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)
import datetime
class Testcase(Manager.Manager): 
    """
    Verify the overall service status changes according to the catalog used.
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
        volName='HCLVol'+datetime.datetime.now().strftime("%H%M%S")
        self.storageList=self.getResources("Storage")
        self.storageIDList=[(storage["Resource Name"],storage['IP Address']) for storage in self.storageList if "EqualLogic" in storage['Manufacturer /Model']]
        self.resourceName, self.resourceIP=self.storageIDList[0]
        self.components='{"Storage1":{"Type":"EqualLogic", "Name":"%s", "VolumeName":"%s", "Size":"10GB","AuthType":"IQN/IP","AuthUser":"grpadmin","AuthPwd":"dell1234","IQNIP":"172.31.64.241"}}'%(self.resourceName,volName)     
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
        repoList=self.getRepositories()
        repoNameList=[repo["Repository Name"] for repo in repoList if repo['State']=='Available' and repo['Repository Name']!='ASM Minimum Required']
        if len(repoNameList)<2:
            self.omit('Not enough number of catalogs available for the test')
        repoName1=repoNameList[0]
        repoName2=repoNameList[1]
        templateName="Template_4800"
        self.createTemplate(templateName=templateName,components=self.components, publishTemplate=True,manageFirmware=True, repositoryName=repoName1)
        templateFirmware=self.getTemplateFirmwarePackage(templateName)
        if templateFirmware==repoName1:
            self.succeed('Catalog is displayed in the Template info')
        else:
            self.failure('Catalog is not displayed in the Template info', raiseExc=True)
        
        #Deploy Service
        #=======================================================================
        self.serviceName = "Service_" + templateName
        selectedRepo=self.deployService(templateName, serviceName=self.serviceName, manageFirmware=True, repositoryOnly=True, getSelectedRepo=True)
        if selectedRepo==templateFirmware:
            self.succeed('catalog displayed in the Deployment wizard is the same as Template') 
        else:
            self.failure('catalog displayed in Deployment wizard and Template are not same', raiseExc=True)
        self.deployService(templateName=templateName, serviceName=self.serviceName,repositoryName=repoName2, manageFirmware=True)
        #Wait for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'"%self.serviceName)
            else:
                self.failure("Failed to Deploy Service '%s'"%self.serviceName, raiseExc=True)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time"%self.serviceName, raiseExc=True)
        #Verify Firmware Package of resource in service
        resourceInfo=self.getDeviceInfoTable(self.resourceIP)
        if templateFirmware in resourceInfo['Firmware/Software Compliance']:
            self.succeed('Firmware compliance of resource  as per firmware selected in service wizard')
        else:
            self.failure('Firmware compliance of resource not as per firmware selected in service wizard', raiseExc=True)  
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    