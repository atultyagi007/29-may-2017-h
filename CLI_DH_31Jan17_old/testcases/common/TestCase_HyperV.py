'''
Created on Jun 5, 2015

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
from util import globalVars
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import templateInputReqValue


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
         Deploy basic HyperV HD boot cluster  on single FX2 server
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
   
            

        
    def test_createTemplate(self):
        testCaseID=self.getTestCaseID(__file__)
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_HyperV)
        self.log_data(" Going to Create Template :: ")
        self.getResources()
        storageId =""
        scvmmId=""
        self.storageRes = self.getReqResource(limit=1, resourceType='STORAGE', deviceType=None)
        if len(self.storageRes) == 0:
            print "Required no. of Storage not available"
        else:
            storageId = self.storageRes[0]["refid"]
        self.scvmmRes = self.getReqResource(limit=1, resourceType='SCVMM', deviceType=None)
        if len(self.scvmmRes) == 0:
            print " Required no. of scvmm not present "
        else:
            scvmmId = self.scvmmRes[0]["refid"]
        
        serverList = globalVars.resourceInfo['SERVER']
        serverRefID1=''
        print " SERVER LIST : %s"%str(serverList)
        for device in serverList:
            if device['ip']==templateInputReqValue.ServerEntry:
                serverRefID1 = device['refid']
        
        if templateInputReqValue.serversource == 'manual':
            payload = payload.replace("$ServerEntry",serverRefID1)
            
                
        templateResponse= self.createTemplate(payload, storageId, scvmmId)              
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                        
            self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template created  and published Successfully','Server: R720 Rack Server'])
            self.log_data( 'Successfully created  and published Template ')
            self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
    def test_publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_HyperV'
        deplyDesc='Deploy basic HyperV HD boot cluster  on single FX2 server'
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
                        self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template Deployed Successfully','Server : Rack Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Now going to call the teardown of service ')
                        #self.runWebServiceAPI(str(self.tc_Id), "Pass", "Run in regression test")
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
                        self.log_data( 'Now going to call the teardown of Template ')
                        self.test_cleanePublishedTemplates()
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed','Server : Rack Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
                        self.log_data( 'Now going to call the teardown of Template ')
                        self.test_cleanePublishedTemplates()
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
        
    def test_cleaneServerPool(self):
        
        response = self.cleanServerPool()
        logger = self.getLoggerInstance()
        logger.debug('Cleane Server Pool Response is')
        logger.info(response)
        
    def test_cleanDeployedService(self):
        self.log_data( 'Now going to call the teardown of service ')
        response = self.cleanUpServices()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))     
        
    def test_cleanePublishedTemplates(self):
        self.log_data( 'Now going to call the teardown of Template ')
        response = self.cleanUpTemplates()
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Published Template Response is')
        logger.info(response)
        self.log_data("Cleaning Published Template Response is :%s"%str(response))

    def test_cleanDeployedTemplates(self, deploymentRefId):
        
        response = self.teardownServices(deploymentRefId)
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))
    

        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    #test.preRunSetup()
    test.test_createTemplate() 
    test.test_cleaneServerPool() 
    os.chdir(current_dir)
