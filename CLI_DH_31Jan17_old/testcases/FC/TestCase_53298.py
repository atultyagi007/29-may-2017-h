'''
Created on Sep 8, 2014


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
    Add an server to the deployment with compellent storage
    """ 
    
    tc_Id =""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        logger = self.getLoggerInstance()
        logger.info(" Running Pre Run Setup")
        #logger.info(" Cleaning Server Pools.......")
        #respCSP = self.cleanServerPool()
        #logger.info(respCSP)
        logger.info(" Getting the devices : ")
        response =self.getDeviceList()
        discoveredDevicesValue=response[0]
        logger.info( " discoveredDevicesValue : ")
        logger.info(discoveredDevicesValue)
        servers = []
        serverType = serverPoolValue.TestCase_89864
        for device in discoveredDevicesValue:
            devicetype=device['deviceType']
            serverModel=device['model']
            if (devicetype == "RackServer" and serverType in serverModel):
                servers.append(device)
        
        logger.info(' Servers : ')
        logger.debug(servers)
        if(len(servers) > 0):
            poolName = globalVars.serverPoolName
            response = self.createServerPool(servers,poolName)
            logger.info('response after createServerPool : ')
            logger.debug(response)
        else:
            print " No RackServers found of type : '%s'",str(serverType)


        
    def test_createTemplate(self):
        testCaseID=self.getTestCaseID(__file__)
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        
        payload = self.readFile(globalVars.filename_TestCase_53298)
        self.getResources()
        networkType=inputForNetworkType.TestCase_53298_networkType
        statausCreateTemplae = False
        if not networkType:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkType=='RackHperV_FC_2Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.RackHyperV_FC_2Port)
        elif networkType=='RackHperV_FC_4Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.RackHyperV_FC_4Port)
        elif networkType=='BladeHyperV_FC_2Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.BladeHyperV_FC_2Port)
        elif networkType=='BladeHyperV_FC_4Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.BladeHyperV_FC_4Port)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( 'ERROR : missing network configuration for the test case ')
            return
        storageId =""
        scvmmId=""
        
        compellentList = globalVars.resourceInfo['COMPELLENT']
        print " COMPELLENT LIST : %s"%str(compellentList)
        for device in compellentList:
            if device['ip']==globalVars.compellent_FC:
                storageId = device['refid']
            
        if len(compellentList) == 0:
            print "Required no. of COMPELLENT not available"
            
            
        
        self.scvmmRes = self.getReqResource(limit=1, resourceType='SCVMM', deviceType=None)
        if len(self.scvmmRes) == 0:
            print " Required no. of scvmm not present "
        else:
            scvmmId = self.scvmmRes[0]["refid"]
        
        globalVars.refIdSCVMM=scvmmId
        globalVars.refIdEQLogic=storageId    
        templateResponse= self.createTemplate(payload, storageId, scvmmId)              
        logger.info( "printing the response from the create template")
        logger.debug( templateResponse.content)
        logger.info( "printing the status code")
        logger.info(templateResponse.status_code)
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template created  and published Successfully','Server : Rack Server'])
            self.log_data( 'Successfully created  and published Template ')
            statausCreateTemplae =self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
        return statausCreateTemplae
            
    def test_publishTemplate(self):
        logger = self.getLoggerInstance()
        logger.info("getting published templateID in test_publishTemplate ")
        logger.info(globalVars.publishedTemplateID)
        
        testCaseID=self.getTestCaseID(__file__)
        #response = self.authenticate()
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_53298'
        deplyDesc='On a successful HyperV deployment and scale up a server '
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        logger.info( "printing the response from the deploy template")
        logger.debug( deployResponse.content)
        logger.info( "printing the status code")
        logger.info( deployResponse.status_code)
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
                        
                        self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        break
                    else:
                        
                        self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed',  "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        break
            loop -= 1
            self.log_data( 'Going to call the ScaleUp  for the testcase id : %s'%str(testCaseID))
            self.test_scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc)
            
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
        return statausDeploy
    
    def test_scaleUpTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc):
        logger = self.getLoggerInstance()
        testCaseID=self.getTestCaseID(__file__)
        payload = self.readFile(globalVars.filename_TestCase_53298_scaleup_server)
        network_Type = inputForNetworkType.TestCase_53298_scaleUp_networkType
        if not network_Type:
            print " Please define the server and port type value in inputForNetworkType parameter file for scaleup "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for scaleup')
            return
        if network_Type=='RackHperV_FC_2Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.RackHyperV_FC_2Port)
        elif network_Type=='RackHperV_FC_4Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.RackHyperV_FC_4Port)
        elif network_Type=='BladeHyperV_FC_2Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.BladeHyperV_FC_2Port)
        elif network_Type=='BladeHyperV_FC_4Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.BladeHyperV_FC_4Port)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( 'ERROR : missing network configuration for the test case ')
            return
        scaleUpResponse = self.scaleUpServerAndDeployHyperVTemplate(templateID, deploymentRefId, DeplName, deplyDesc, payload)
        logger.info( "printing the response from the scaleUp deploy template")
        logger.debug( scaleUpResponse.content)
        logger.info( "printing the status code")
        logger.info( scaleUpResponse.status_code)
        if scaleUpResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            #deploymentRefId = self.getDeploymentId(DeplName)
            loop = 60
            deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
            deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            
            while loop:
                resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                if resDS.lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    if resDS.lower() in ("complete"):
                        
                        self.log_TestData(["", "", "", str(self.tc_Id),'Success',' scaleUp Template Deployed Successfully', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully ScaleUped Service for the Deployment Name : %s'%DeplName)
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
                        
                        self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment ScaleUped Service Failed','Server : Blade Server',  "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment ScaleUped Service Failed for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
                        
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Failed to deploy scaleUp Service'])
            self.log_data('Deployment scaleUp Service Failed for the Deployment Name : %s'%DeplName)
    

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
    #test.preRunSetup()
    status = test.test_createTemplate()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1) 

