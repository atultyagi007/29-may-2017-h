'''
Created on Feb 21, 2017

@author: preetam.sethi
Description : Verify that templates displays the name of the catalog selected
Test Flow   :   Create a template and select a catalog.
                Change the catalog to Default catalog.
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)
import datetime
class Testcase(Manager.Manager): 
    """
    Verify that templates displays the name of the catalog selected
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
        defaultRepo=self.getDefaultRepository()
        repoNameList=[repo['Repository Name'] for repo in repoList if repo['State']=='Available' and repo["Repository Name"]!="ASM Minimum Required" and repo['Repository Name']!=defaultRepo]
        if len(repoNameList)==0:
            self.omit("Not enough catalogs available for the test")
        repoName=repoNameList[0]
        self.createTemplate(templateName="Template_4795",components=self.components, publishTemplate=True,manageFirmware=True, repositoryName=repoName)
        templateFirmware=self.getTemplateFirmwarePackage('Template_4795')
        if templateFirmware==repoName:
            self.succeed('Successfully created template with firmware %s'%templateFirmware)
        else:
            self.failure('Template Firmware Package not set to %s'%templateFirmware, raiseExc=True)
            
        self.editTemplate(templateName="Template_4795", managePermissions=False, manageFirmware=True,firmwareName='Use ASM appliance default catalog')
        templateFirmware=self.getTemplateFirmwarePackage('Template_4795')
        if templateFirmware=='Use ASM appliance default catalog':
            self.succeed('templates displays the name of the catalog selected')
        else:
            self.failure('templates does not display the name of the catalog selected', raiseExc=True)
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    