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
import networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping

class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    On a successful VMWare deployment, scale up a VM/Clone VM/Clone VM template.

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
        
        payload = self.readFile(globalVars.filename_TestCase_126316_before_scaleup)
        self.getResources()
        
        networkTypeServer1=inputForNetworkType.TestCase_126316_networkType_server1
        networkTypeServer2=inputForNetworkType.TestCase_126316_networkType_server2
        
        if not networkTypeServer1:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        else:
            if networkTypeServer1=='Blade_ISCSICOMP_DIV_ESXI_2PORT':
                payload=payload.replace("$networkConfiguration1",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_2PORT)
            elif networkTypeServer1=='Blade_ISCSICOMP_DIV_ESXI_4PORT':
                payload=payload.replace("$networkConfiguration1",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_4PORT)
            elif networkTypeServer1=='Rack_ISCSICOMP_DIV_ESXI_2PORT':
                payload=payload.replace("$networkConfiguration1",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_2PORT)
            elif networkTypeServer1=='Rack_ISCSICOMP_DIV_ESXI_4PORT':
                payload=payload.replace("$networkConfiguration1",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_4PORT)
            else:
                print " ERROR : missing network configuration for the test case "
                self.log_data( 'ERROR : missing network configuration for the test case ')
                return
            
            
            
        if not networkTypeServer2:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        else:
            if networkTypeServer2=='Blade_ISCSICOMP_DIV_ESXI_2PORT':
                payload=payload.replace("$networkConfiguration2",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_2PORT)
            elif networkTypeServer2=='Blade_ISCSICOMP_DIV_ESXI_4PORT':
                payload=payload.replace("$networkConfiguration2",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_4PORT)
            elif networkTypeServer2=='Rack_ISCSICOMP_DIV_ESXI_2PORT':
                payload=payload.replace("$networkConfiguration2",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_2PORT)
            elif networkTypeServer2=='Rack_ISCSICOMP_DIV_ESXI_4PORT':
                payload=payload.replace("$networkConfiguration2",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_4PORT)
            else:
                print " ERROR : missing network configuration for the test case "
                self.log_data( 'ERROR : missing network configuration for the test case ')
                return
            
        self.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        
        compellentList = globalVars.resourceInfo['COMPELLENT']
        print " COMPELLENT LIST : %s"%str(compellentList)
        for device in compellentList:
            if device['ip']==globalVars.compellent_ISCSI:
                storageId = device['refid']
        if len(compellentList) == 0:
            print "Required no. of COMPELLENT not available"
            
        vcenterList = globalVars.resourceInfo['VCENTER']
        print " VCENTER LIST : %s"%str(vcenterList)
        for device in vcenterList:
            if device['ip']==globalVars.vcenter_ISCSI:
                vcenterId = device['refid']
                
        if len(vcenterList) == 0:
            print "Required no. of VCENTER not available"
        
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
                                    
            self.log_TestData(["", "", "", str(testCaseID), testCaseDescriptionMapping.TestCase_126316, 'Success','Template created  and published Successfully','Server : Blade Server'])
            self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "", str(testCaseID),testCaseDescriptionMapping.TestCase_126316, 'Failed','Failed to create/published template'])
            
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
        DeplName='TestCase_126316'
        deplyDesc=testCaseDescriptionMapping.TestCase_126316
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
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(testCaseID), deplyDesc, 'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Going to do VCenter Validation before  ScaleUp')
                        self.doVCenterValidations(globalVars.refIdVCenter)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(testCaseID), deplyDesc, 'Failed','Deployment Service Failed','Server : Blade Server',  "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        break
            loop -= 1
            self.log_data( 'Going to call the ScaleUp  for the testcase id : %s'%str(testCaseID))
            self.test_scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc)
            
        
        else:
            self.log_TestData(["", "", "", str(testCaseID), deplyDesc, 'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
    
    def test_scaleUpTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc):
        logger = self.getLoggerInstance()
        testCaseID=self.getTestCaseID(__file__)
        
        payload = self.readFile(globalVars.filename_TestCase_126316_afte_scaleup_Server)
        network_Type = inputForNetworkType.TestCase_126316_scaleUp_networkType
        if not network_Type:
            print " Please define the server and port type value in inputForNetworkType parameter file for scaleup "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for scaleup ')
            return
        if network_Type=='Blade_ISCSICOMP_DIV_ESXI_2PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_2PORT)
        elif network_Type=='Blade_ISCSICOMP_DIV_ESXI_4PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_4PORT)
        elif network_Type=='Rack_ISCSICOMP_DIV_ESXI_2PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_2PORT)
        elif network_Type=='Rack_ISCSICOMP_DIV_ESXI_4PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_4PORT)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( 'ERROR : missing network configuration for the test case ')
            return
        
        
        scaleUpResponse = self.scaleUpServerAndDeployEsxiTemplate(templateID, deploymentRefId, DeplName, deplyDesc, payload)
        
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
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(testCaseID), deplyDesc, 'Success',' scaleUp Template Deployed Successfully', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully ScaleUped Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Going to do VCenter Validation before TearDown after ScaleUp')
                        self.doVCenterValidations(globalVars.refIdVCenter)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        self.log_data( 'Going to do VCenter Validation after TearDown')
                        self.doVCenterValidations(globalVars.refIdVCenter)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(testCaseID), deplyDesc, 'Failed','Deployment ScaleUped Service Failed','Server : Blade Server',  "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment ScaleUped Service Failed for the Deployment Name : %s'%DeplName)
                        if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            self.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.test_cleanDeployedTemplates(deploymentRefId)
                        
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(testCaseID), deplyDesc, 'Failed','Failed to deploy scaleUp Service'])
            self.log_data('Deployment scaleUp Service Failed for the Deployment Name : %s'%DeplName)
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

