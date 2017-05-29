'''
Created on Aug 20, 2014

@author: waseem.irshad
'''

from createdeploytemplate.TemplateBaseClass import TemplateTestBase
import json
from util import globalVars
import csv
import time
import xml.etree.cElementTree as ET
from discoverresources.DiscoverResourceBaseClass import DiscoverResourceTestBase
from testcases.common import serverPoolValue
from createdeploytemplate import inputReqValueESXI, networkConfiguration, inputForNetworkType


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Deploy server with ESXI with  storage volumes,an cluster and VM
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
        serverType = serverPoolValue.TestCase_89864
        for device in discoveredDevicesValue:
            devicetype=device['deviceType']
            serverModel=device['model']
            if (devicetype == "FXServer" and serverType in serverModel):
                servers.append(device)
        
        if(len(servers) > 0):
            poolName = globalVars.serverPoolName
            response = self.createServerPool(servers,poolName)
        else:
            print " No FXServers found of type : '%s'",str(serverType)
            

        
    def test_createTemplate(self):
        
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_MXL)
        self.getResources()
        
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
                                    
            self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template created  and published Successfully','Server : Blade Server'])
            self.log_data( 'Successfully created  and published Template ')
            self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
    def test_publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_MXL'
        deplyDesc='Deploy servers with ESXI with  storage volumes,an cluster and VM'
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
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
#                         self.log_data( 'Now going to call the teardown of service ')
#                         self.runWebServiceAPI(str(self.tc_Id), "Pass", "Run in regression test")
#                         self.cleanDeployedService(deploymentRefId)
#                         self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
#                         self.runWebServiceAPI(str(self.tc_Id), "Fail", "Run in regression test")
#                         self.log_data( 'Now going to call the teardown of service ')
#                         self.cleanDeployedService(deploymentRefId)
#                         self.test_cleanDeployedTemplates(deploymentRefId)
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
        
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
#     test.preRunSetup()
    test.test_createTemplate()  

