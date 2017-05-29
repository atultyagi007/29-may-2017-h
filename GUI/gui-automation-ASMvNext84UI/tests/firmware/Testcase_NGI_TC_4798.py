'''
Created on March 03, 2017

@author: preetam.sethi
Description :  Set a catalog in Template and deploy without changing the catalog
Test Flow   :  Create and publish a template with a catalog     
               Deploy the template and verify that the catalog displayed in the wizard is the same.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)
import datetime
class Testcase(Manager.Manager): 
    """
    Set a catalog in Template and deploy without changing the catalog
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
        repoList=self.getRepositories()
        repoNameList=[repo["Repository Name"] for repo in repoList if repo['State']=='Available' and repo['Repository Name']!='ASM Minimum Required']
        repoName=repoNameList[0]
        templateName="Template_4798"
        self.createTemplate(templateName=templateName,components=self.components, publishTemplate=True,manageFirmware=True, repositoryName=repoName)
        #Deploy Service
        #=======================================================================
        self.serviceName = "Service_" + templateName
        selectedRepo=self.deployService(templateName, serviceName=self.serviceName, manageFirmware=True, repositoryOnly=True, getSelectedRepo=True)
        if selectedRepo==repoName:
            self.succeed('catalog displayed in the Deployment wizard is the same as Template') 
        else:
            self.failure('catalog displayed in Deployment wizard and Template are not same', raiseExc=True)
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    