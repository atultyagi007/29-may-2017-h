'''
Created on Feb 21, 2017

@author: preetam.sethi
Description : Verify a catalog in use whether in a template or service cannot be deleted
Test Flow   :   Verify a catalog in use whether in a template or service cannot be deleted
'''
from tests.globalImports import *

tc_id=utility.get_tc_data(__file__)
import datetime
class Testcase(Manager.Manager): 
    """
    Verify a catalog in use whether in a template or service cannot be deleted
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
        volName='HCLVl'+datetime.datetime.now().strftime("%H%M%S")
        self.components='{"Storage1":{"Type":"EqualLogic", "Name":"ESS-RowC-Rack7-EqualLogic6110-1", "VolumeName":"%s", "Size":"10GB","AuthType":"IQN/IP","AuthUser":"grpadmin","AuthPwd":"dell1234","IQNIP":"172.31.64.241"}}'%volName     
        
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
        self.createTemplate(templateName="Template_4822",components=self.components, publishTemplate=True,manageFirmware=True, repositoryName=repoName)
        templateFirmware=self.getTemplateFirmwarePackage('Template_4822')
        if templateFirmware==repoName:
            self.succeed('Successfully created template with firmware %s'%templateFirmware)
        else:
            self.failure('Template Firmware Package not set to %s'%templateFirmware, raiseExc=True)
        try:
            self.deleteRepository(repositoryName=repoName)
            self.failure('Repository used in template can be deleted ', raiseExc=True)
        except:
            self.succeed("Repository used in template cannot be deleted")
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()