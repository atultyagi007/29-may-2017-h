'''
Created on Aug 20, 2014

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
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping

class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Multiple service deployments using vendor for auto generation of host names
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
            print " No RackServers found of type : '%s'",str(serverType)
            

        
    def test_createTemplate(self):
        testCaseID=self.getTestCaseID(__file__)
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_107461)
        self.getResources()
        networkType=inputForNetworkType.TestCase_107461_networkType
        if not networkType:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case')
            return
        else:
            if networkType=='Blade_MultiService_2PORT':
                payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_MultiService_2PORT)
            elif networkType=='Blade_MultiService_4PORT':
                payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_MultiService_4PORT)
            elif networkType=='Rack_MultiService_2PORT':
                payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_MultiService_2PORT)
            elif networkType=='Rack_MultiService_4PORT':
                payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_MultiService_4PORT)
            else:
                print " ERROR : missing network configuration for the test case "
                self.log_data( 'ERROR : missing network configuration for the test case')
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
            self.log_data( 'Successfully created  and published Template ')
                                  
            self.log_TestData(["", "", "",str(self.tc_Id), testCaseDescriptionMapping.TestCase_107461, 'Success','Template created  and published Successfully','Server :2 Blade Servers'])
            self.test_publishTemplate()
            
        else:
            self.log_data( 'Failed to create/published template ')
            self.log_TestData(["", "", "",str(self.tc_Id), testCaseDescriptionMapping.TestCase_107461, 'Failed','Failed to create/published template'])
            
    def test_publishTemplate(self):
        
        testCaseID=self.getTestCaseID(__file__)
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_107461'
        deplyDesc=testCaseDescriptionMapping.TestCase_107461
        self.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        if deployResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            deployRefId = self.getDeploymentId(DeplName)
            self.checkForDeploymentStatus(deployRefId, self.tc_Id, deplyDesc)
            self.getOsHostName(deployRefId)           
            
            #Get Deployment Id List
            deploymentRefIdList = self.getAllDeploymentIdList(DeplName, deployRefId)
            for deploymentRefId in deploymentRefIdList:
                self.checkForDeploymentStatus(deploymentRefId, self.tc_Id, deplyDesc)
                self.getOsHostName(deploymentRefId)
                
            deplyRefIdFinalList = deploymentRefIdList.append(deployRefId)
            for deployRefIDDel in deplyRefIdFinalList:
                self.checkAndDeleteDeployment(deployRefIDDel, self.tc_Id)
            
        
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
        
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

