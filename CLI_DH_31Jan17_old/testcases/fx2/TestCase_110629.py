'''
Created on Dec 17, 2014

@author: dheeraj.singh
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
    Using FX2 Servers as rack servers for Hyper-v deployment
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        
        respCSP = self.cleanServerPool()
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
        
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_110629)
        self.getResources()
        networkType=inputForNetworkType.TestCase_110629_networkType
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
        templateResponse= self.createTemplateEsxi(payload,vcenterId,storageId) 
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "", str(self.tc_Id),testCaseDescriptionMapping.TestCase_110629, 'Success','Template created  and published Successfully','Server : Rack Server'])
            self.log_data( 'Successfully created  and published Template ')
            self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),testCaseDescriptionMapping.TestCase_110629, 'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
    def test_publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_110629'
        
        deplyDesc= testCaseDescriptionMapping.TestCase_110629
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
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Success','Template Deployed Successfully','Server : Rack Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        break
                    else:
                        print "Test Case Id  %s"%str(self.tc_Id) + " \t and Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed','Server : Rack Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data('Deployment Service Failed')
                        if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            self.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.test_cleanDeployedTemplates(deploymentRefId)
                            
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed')
        
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


    
    def test_sanity(self):
        self.test_createTemplate()
        self.test_cleaneServerPool()
       
        
       
        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    #test.preRunSetup()
    test.test_createTemplate()
    os.chdir(current_dir)  
