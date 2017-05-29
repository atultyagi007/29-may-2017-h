'''
Created on Nov 04, 2014

@author: waseem.irshad
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))


from TemplateBaseClass import TemplateTestBase
import json
import globalVars
import testCaseDescriptionMapping
import csv
import time
import xml.etree.cElementTree as ET
import serverPoolValue
import DiscoverResourceBaseClass
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import templateInputReqValueHyperV, networkConfiguration, inputForNetworkType


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Deploy 2 M620 Blade servers with HyperV  non-R2 with 2 storage volumes and a cluster
    """ 
    
    tc_Id = ""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        
        self.log_data(" Running Pre Run Setup :: ")
        self.log_data(" Creating Server Pool ")
        response =self.getDeviceList()
        discoveredDevicesValue=response[0]         
        servers = []
        
        for device in discoveredDevicesValue:
            devicetype=device['deviceType']
            if (devicetype == "BladeServer"):
                servers.append(device)
        
        
        if(len(servers) > 0):
            poolName = globalVars.serverPoolName
            response = self.createServerPool(servers,poolName)
            
        else:
            print " No BladeServers found "
            
    
    def test_createTemplate(self):
        
        response = self.authenticate()
        self.setNetworkList()
        
        self.log_data(" Going to Create Template :: ")
        payload = self.readFile(globalVars.filename_TestCase_77217)
        self.getResources()
        networkTypeServer1=inputForNetworkType.TestCase_77217_networkType_server1
        networkTypeServer2=inputForNetworkType.TestCase_77217_networkType_server2
        
        statausCreateTemplae = False
        if not networkTypeServer1:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkTypeServer1=='RackHperV_2Port':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.RackHyperV_2Port)
        elif networkTypeServer1=='RackHperV_4Port':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.RackHyperV_4Port)
        elif networkTypeServer1=='BladeHyperV_2Port':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.BladeHyperV_2Port)
        elif networkTypeServer1=='BladeHyperV_4Port':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.BladeHyperV_4Port)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( 'ERROR : missing network configuration for the test case ')
            return

        if not networkTypeServer2:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkTypeServer2=='RackHperV_2Port':
            payload=payload.replace("$networkConfiguration2",networkConfiguration.RackHyperV_2Port)
        elif networkTypeServer2=='RackHperV_4Port':
            payload=payload.replace("$networkConfiguration2",networkConfiguration.RackHyperV_4Port)
        elif networkTypeServer2=='BladeHyperV_2Port':
            payload=payload.replace("$networkConfiguration2",networkConfiguration.BladeHyperV_2Port)
        elif networkTypeServer2=='BladeHyperV_4Port':
            payload=payload.replace("$networkConfiguration2",networkConfiguration.BladeHyperV_4Port)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( 'ERROR : missing network configuration for the test case ')
            return

        
        storageId =""
        scvmmId=""
        self.storageRes = self.getReqResource(limit=1, resourceType='STORAGE', deviceType=None)
        if len(self.storageRes) == 0:
            print "Required no. of Storage not available"
        else:
            storageId = self.storageRes[0]["refid"]
        self.scvmmRes = self.getReqResource(limit=1, resourceType='SCVMM', deviceType=None)
        if len(self.scvmmRes) == 0:
            print " Required no. of scvmm not present "
        else:
            scvmmId = self.scvmmRes[0]["refid"]
        
        globalVars.refIdSCVMM = scvmmId    
        templateResponse= self.createTemplate(payload, storageId, scvmmId)              
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "", str(self.tc_Id), testCaseDescriptionMapping.TestCase_77217, 'Success','Template created  and published Successfully','Server : 2 M620 Blade servers'])
            self.log_data( 'Successfully created  and published Template ')
            statausCreateTemplae = self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "", str(self.tc_Id), testCaseDescriptionMapping.TestCase_77217, 'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
        return statausCreateTemplae
            
    def test_publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_77217'
        deplyDesc=testCaseDescriptionMapping.TestCase_77217
        
        self.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        statausDeploy = False
        
        if deployResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            deploymentRefId = self.getDeploymentId(DeplName)
            loop = 60
            deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
            deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            
            while loop:
                resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                if resDS.lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    if resDS.lower() in ("complete"):
                        statausDeploy = True
                        self.log_TestData(["", "", "", str(self.tc_Id), deplyDesc, 'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Going to do SCVMM Validation before TearDown')
                        self.doScvmmValidation(globalVars.refIdSCVMM)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        self.log_data( 'Going to do SCVMM Validation after TearDown')
                        self.doScvmmValidation(globalVars.refIdSCVMM)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            self.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.test_cleanDeployedTemplates(deploymentRefId)
                        
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
        return statausDeploy
            
    def test_cleaneServerPool(self):
        
        response = self.cleanServerPool()
        logger = self.getLoggerInstance()
        logger.debug('Cleane Server Pool Response is')
        logger.info(response)
        
    def test_cleanDeployedTemplates(self, deploymentRefId):
        
        response = self.teardownServices(deploymentRefId)
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))

        
                
        
        
    def test_cleanePublishedTemplates(self):
        
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))
                
       

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    test.preRunSetup()
    status = test.test_createTemplate()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1) 