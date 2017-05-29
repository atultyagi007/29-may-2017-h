'''
Created on Oct 13, 2014

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
import serverPoolValue
import DiscoverResourceBaseClass
from DiscoverResourceBaseClass import DiscoverResourceTestBase


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Deploy 2 Blade servers with HyperV on Windows 2012 non-R2 with 2 storage volumes and a cluster
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
        payload = self.readFile(globalVars.filename_BladeServer_TestCase_78805)
        ipAddressSCVMM = globalVars.configInfo["SCVMM"]["start_ip"]
        
        ipAddressEQ = globalVars.configInfo["Storage"]["start_ip"]
        
        refIdSCVMM = self.getRefIDForDevice('scvmm', ipAddressSCVMM)
        
        refIdEQ = self.getRefIDForDevice('equallogic', ipAddressEQ)
        statausCreateTemplae = False
        templateResponse= self.createTemplate("payload", refIdEQ, refIdSCVMM)              
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template created  and published Successfully'])
            self.log_data( 'Successfully created  and published Template ')
            statausCreateTemplae = self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
        return statausCreateTemplae
            
    def test_publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_78805_BladeServer'
        deplyDesc='Deploy 2 Blade servers with HyperV on Windows 2012 non-R2 with 2 storage volumes and a cluster'
        
        self.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        statausDeploy = False
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
                        statausDeploy = True
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template Deployed Successfully'])
                        self.log_data( 'Successfully Deployed Template ')
                        self.log_data( 'Now going to call the teardown of service ')
                        self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
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
        loop = 60
        while loop:
            resDS, statDS = self.getDeploymentStatus(deploymentRefId)
            if resDS.lower() in ("in_progress"):
                time.sleep(120)
            else:
                print "Deployment Status: %s"%resDS
                self.log_TestData(["", "", "",str(self.tc_Id),'Success','Service Teardown Successfully'])
                break
                

            loop -= 1
        
        
        
    def test_cleanePublishedTemplates(self):
        
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))

    
        
       

if __name__ == "__main__":
    test = Testcase()
    test.preRunSetup()
    status = test.test_createTemplate()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)  