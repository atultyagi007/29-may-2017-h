'''
Created on Aug 18, 2014

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
import requests
import xml.etree.cElementTree as ET
import time
import serverPoolValue
import DiscoverResourceBaseClass
from DiscoverResourceBaseClass import DiscoverResourceTestBase

class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Deploy a HyperV non-R2 instance on an R720 with 2 storage volumes and a cluster
    """ 
    tc_Id = ""
    
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
                 
        self.log_date( (" test case Id : %s"%str(self.tc_Id)))
        self.log_data(" Running Pre Run Setup :: ")
        self.log_data(" Creating Server Pool ")
        response = self.getDeviceList()
        discoveredDevicesValue=response[0]
        servers = []
        serverType = serverPoolValue.TestCase_78804
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
        self.log_data(" Going to Create Template :: ")
        payload = self.readFile(globalVars.filename_TestCase_78804)
        
        ipAddressSCVMM = globalVars.configInfo["SCVMM"]["start_ip"]
        
        ipAddressEQ = globalVars.configInfo["Storage"]["start_ip"]
        
        refIdSCVMM = self.getRefIDForDevice('scvmm', ipAddressSCVMM)
        
        refIdEQ = self.getRefIDForDevice('equallogic', ipAddressEQ)
        
        templateResponse= self.createTemplate(payload, refIdEQ, refIdSCVMM)              
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
#           
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='HyperV non-R2 instance on an R720 with 2 storage volumes and a cluster'
        deplyDesc='Deploy a HyperV non-R2 instance on an R720 with 2 storage volumes and a cluster'
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
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template Deployed Successfully'])
                        self.log_data( 'Successfully Deployed Template ')
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to deploy template'])
                        self.log_data('Failed to deploy template')
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to deploy template'])
            self.log_data('Failed to deploy template')
            
    

        
    def test_cleanePublishedTemplates(self):
        
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))
        
    
    def test_cleanDeployedTemplates(self):
        
        response = self.cleanUpServices()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))
        
        
                      
        

if __name__ == "__main__":
    
    
    test = Testcase()
    loop = globalVars.noOfLoops
    while loop>0:
        #test.preRunSetup()
        test.test_createTemplate()
        test.test_cleanePublishedTemplates()
        test.test_cleanDeployedTemplates()
        loop -= 1
    os.chdir(current_dir)
    
    
        
    
