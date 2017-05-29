'''
Created on Sep 8, 2014

@author: dheeraj_si
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
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping

class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    1 server delayed deployment with server scaleup
    """ 
    
    tc_Id=""
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
            if (devicetype == "BladeServer" and serverType in serverModel):
                servers.append(device)
        
        logger.info(' Servers : ')
        logger.debug(servers)
        if(len(servers) > 0):
            poolName = globalVars.serverPoolName
            response = self.createServerPool(servers,poolName)
            logger.info('response after createServerPool : ')
            logger.debug(response)
        else:
            print " No BladeServers found of type : '%s'",str(serverType)


        
    def test_createTemplate(self):
        testCaseID=self.getTestCaseID(__file__)
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        
        payload = self.readFile(globalVars.filename_TestCase_110636_before_scaleup_server)
        
        self.getResources()
        networkType=inputForNetworkType.TestCase_110636_networkType
        if not networkType:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkType=='Blade_Fx2_2PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_Fx2_2PORT)
        elif networkType=='Blade_Fx2_4PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_Fx2_4PORT)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( 'ERROR : missing network configuration for the test case ')
            return
        self.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        self.storageRes = self.getReqResource(limit=1, resourceType='STORAGE', deviceType=None)
        if len(self.storageRes) == 0:
            print "Required no. of Storage not available"
        else:
            storageId = self.storageRes[0]["refid"]
        self.vcenterRes = self.getReqResource(limit=1, resourceType='VCENTER', deviceType=None)
        if(len(self.vcenterRes)) == 0:
            print " Required no. of vcenter not found "
        else:
            vcenterId = self.vcenterRes[0]["refid"]
            
        globalVars.refIdVCenter=vcenterId
        globalVars.refIdEQLogic=storageId
        templateResponse= self.createTemplateEsxi(payload,vcenterId,storageId) 
        logger.info( "printing the response from the create template")
        logger.debug( templateResponse.content)
        logger.info( "printing the status code")
        logger.info(templateResponse.status_code)
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "", str(self.tc_Id), testCaseDescriptionMapping.TestCase_110636, 'Success','Template created  and published Successfully','Server : Blade Server'])
            self.log_data( 'Successfully created  and published Template ')
            self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "", str(self.tc_Id), testCaseDescriptionMapping.TestCase_110636, 'Failed','Failed to create/published template'])
            
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
        DeplName='TestCase_110636'
        deplyDesc=testCaseDescriptionMapping.TestCase_110636
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        logger.info( "printing the response from the deploy template")
        logger.debug( deployResponse.content)
        logger.info( "printing the status code")
        logger.info( deployResponse.status_code)
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
                        
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Success','Template Deployed Successfully','Server : Blade  Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        break
                    else:
                        
                        print "Test Case Id  %s"%str(self.tc_Id) + " \t and Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        break
            loop -= 1
            self.log_data( 'Going to call the ScaleUp  for the testcase id : %s'%str(testCaseID))
            self.test_scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc)
            
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed'])
    
    def test_scaleUpTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc):
        logger = self.getLoggerInstance()
        testCaseID=self.getTestCaseID(__file__)
        payload = self.readFile(globalVars.filename_TestCase_110636_after_scaleup_server)
        network_Type = inputForNetworkType.TestCase_110636_scaleUp_networkType
        if not network_Type:
            print " Please define the server and port type value in inputForNetworkType parameter file for scaleup "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for scaleup ')
            return
        elif network_Type=='Blade_Fx2_2PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_Fx2_2PORT)
        elif network_Type=='Blade_Fx2_4PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_Fx2_4PORT)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( 'ERROR : missing network configuration for the test case')
            return
            
        scaleUpResponse = self.scaleUpServerAndDeployEsxiTemplate(templateID, deploymentRefId, DeplName, deplyDesc, payload)
        #scaleUpResponse = self.scaleUpdeployEsxiTemplate(templateID, deploymentRefId, DeplName, deplyDesc, globalVars.filename_TestCase_110636_after_scaleup_server)
        logger.info( "printing the response from the scaleUp deploy template")
        logger.debug( scaleUpResponse.content)
        logger.info( "printing the status code")
        logger.info( scaleUpResponse.status_code)
        if scaleUpResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            #deploymentRefId = self.getDeploymentId(DeplName)
            deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
            deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            loop = 60
            while loop:
                resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                if resDS.lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    if resDS.lower() in ("complete"):
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Success','scaleUp Service Successfully','Server : Blade  Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully scaleUp Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        
                        break
                    else:
                        print "Test Case Id  %s"%str(self.tc_Id) + " \t and Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','scaleUp Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            self.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.test_cleanDeployedTemplates(deploymentRefId)
#                         
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),deplyDesc, 'Failed','Scaleup Deployment Service Failed'])
            if globalVars.enableTearDownService:
                self.log_data( 'Now going to call the teardown of service ')
                self.cleanDeployedService(deploymentRefId)
                self.test_cleanDeployedTemplates(deploymentRefId)

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
    test.test_createTemplate()  
    os.chdir(current_dir)

