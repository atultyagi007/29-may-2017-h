'''
Created on Sep 30, 2014

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


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Deploy 2 blade servers using ESX 5.5 with 1 new compellent volume to a new cluster
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
            if (devicetype == "RackServer" and serverType in serverModel):
                servers.append(device)
        
        if(len(servers) > 0):
            poolName = globalVars.serverPoolName
            response = self.createServerPool(servers,poolName)
        else:
            print " No RackServers found of type : '%s'",str(serverType)
            

        
    def test_createTemplate(self):
        
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_102192)
        ipAddressVC = globalVars.configInfo["VCENTER"]["start_ip"]
        
        ipAddressCP = globalVars.configInfo["Storage"]["start_ip"]
        refIdVCenter = self.getRefIDForDevice('vcenter', ipAddressVC)
        refIdEQ = self.getRefIDForDevice('compellent', ipAddressCP)
        self.log_data(" Going to Create Template :: ")
        templateResponse= self.createTemplateEsxi(payload,refIdVCenter,refIdEQ) 
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template created  and published Successfully'])
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
        DeplName='TestCase_102192'
        deplyDesc='Deploy  2 blade servers using ESX 5.5 with 1 new compellent volume to a new cluster'
        self.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        if deployResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            deploymentRefId = self.getDeploymentId(DeplName)
            loop = 60
            while loop:
                resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                if resDS.lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    if resDS.lower() in ("complete"):
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template Deployed Successfully'])
                        self.log_data( 'Successfully Deployed Template ')
                        break
                    else:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to deploy template'])
                        self.log_data('Failed to deploy template')
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to deploy template'])
            self.log_data('Failed to deploy template')
        
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
        
    def test_cleanDeployedService(self):
        
        response = self.cleanUpServices()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))
    

        
       
        
if __name__ == "__main__":
    test = Testcase()
    #test.preRunSetup()
    #test.test_createTemplate()
    loop = globalVars.noOfLoops
    while loop>0:
        try:
            #test.preRunSetup()
            try:
                test.test_createTemplate()
            except Exception as e1:
                print 'Some Exception Occurred while Creating the template'
                print "Exception Reason while Creating the template"
                print str(e1)
            test.test_cleanDeployedService()
            test.test_cleanePublishedTemplates()
            test.test_cleaneServerPool()
            loop -= 1
        except Exception as e:
            print 'Some Exception Occurred while running the looping script'
            print "Exception Reason"
            print str(e)
            loop -= 1