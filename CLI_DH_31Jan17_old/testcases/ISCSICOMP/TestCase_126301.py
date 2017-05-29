'''
Created on Sep 18, 2015

@author: Aman Matta
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
    End to end deployment with compellent stoage, ESXi host, VMWare cluster, VMs and application.
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        
#         respCSP = self.cleanServerPool()
        response =self.getDeviceList()
        discoveredDevicesValue=response[0]
        servers = []
        serverType = serverPoolValue.TestCase_78810
        for device in discoveredDevicesValue:
            devicetype=device['deviceType']
            serverModel=device['model']
            if (devicetype == "BladeServer" and serverType in serverModel):
                servers.append(device)
        
        if(len(servers) > 0):
            poolName = globalVars.serverPoolName
            response = self.createServerPool(servers,poolName)
        else:
            print " No BladeServers found of type : '%s'",str(serverType)
            

        
    def test_createTemplate(self):
        
        statausCreateTemplae = False
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_126301)
        self.getResources()
        
        networkTypeServer1=inputForNetworkType.TestCase_126301_networkType_server1
        networkTypeServer2=inputForNetworkType.TestCase_126301_networkType_server2
        
        if not networkTypeServer1:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkTypeServer1=='Rack_ISCSICOMP_DIV_ESXI_2PORT':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_2PORT)
        elif networkTypeServer1=='Rack_ISCSICOMP_DIV_ESXI_4PORT':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_4PORT)
        elif networkTypeServer1=='Blade_ISCSICOMP_DIV_ESXI_2PORT':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_2PORT)
        elif networkTypeServer1=='Blade_ISCSICOMP_DIV_ESXI_4PORT':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_4PORT)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( ' ERROR : missing network configuration for the test case ')
            return
        
        
        if not networkTypeServer2:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkTypeServer2=='Rack_ISCSICOMP_DIV_ESXI_2PORT':
            payload=payload.replace("$networkConfiguration2",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_2PORT)
        elif networkTypeServer2=='Rack_ISCSICOMP_DIV_ESXI_4PORT':
            payload=payload.replace("$networkConfiguration2",networkConfiguration.Rack_ISCSICOMP_DIV_ESXI_4PORT)
        elif networkTypeServer2=='Blade_ISCSICOMP_DIV_ESXI_2PORT':
            payload=payload.replace("$networkConfiguration2",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_2PORT)
        elif networkTypeServer2=='Blade_ISCSICOMP_DIV_ESXI_4PORT':
            payload=payload.replace("$networkConfiguration2",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_4PORT)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( ' ERROR : missing network configuration for the test case ')
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
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "",str(self.tc_Id), testCaseDescriptionMapping.TestCase_126301, 'Success','Template created  and published Successfully','Server : Blade Server'])
            self.log_data( 'Successfully created  and published Template ')
            statausCreateTemplae = self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), testCaseDescriptionMapping.TestCase_126301, 'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
        return statausCreateTemplae
            
    def test_publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        statausDeploy = False
        DeplName='TestCase_126301'
        deplyDesc=testCaseDescriptionMapping.TestCase_126301
        self.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
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
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Going to do VCenter Validation before TearDown ')
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
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        self.runWebServiceAPI(str(self.tc_Id), "Fail", "Run in regression test")
                        if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            self.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.test_cleanDeployedTemplates(deploymentRefId)
                        
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
        return statausDeploy
        
    def test_cleaneServerPool(self):
        
        response = self.cleanServerPool()
        logger = self.getLoggerInstance()
        logger.debug('Clean Server Pool Response is')
        logger.info(response)
        
    def test_cleanDeployedTemplates(self, deploymentRefId):
        
        response = self.teardownServices(deploymentRefId)
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))
#         if response.status_code in (200, 201, 202, 203, 204):
#             self.log_TestData([str(self.tc_Id),'Success','Service Teardown Successfully'])
#         else:
#             self.log_TestData([str(self.tc_Id),'Failed','Failed to Teardown Service'])
#         time.sleep(120)
                
        
        
    def test_cleanePublishedTemplates(self):
        
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))


    
    def test_sanity(self):
        self.test_createTemplate()
        self.test_cleaneServerPool()
       
   
            
       
        

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

