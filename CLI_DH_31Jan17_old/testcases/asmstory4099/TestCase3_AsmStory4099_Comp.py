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


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Deploy 2 servers with compellent volume – one 12G blade (5.1) and other 12G rack (5.5) ,1 cluster and 1 VM after Scale up 13G rack (5.1) and 13G blade (5.5)
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
        
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase3_AsmStory4099_Comp)
        self.getResources()
        self.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        self.storageRes = self.getReqResource(limit=1, resourceType='COMPELLENT', deviceType=None)
        if len(self.storageRes) == 0:
            print "Required no. of Storage not available"
        else:
            storageId = self.storageRes[0]["refid"]
        self.vcenterRes = self.getReqResource(limit=1, resourceType='VCENTER', deviceType=None)
        if(len(self.vcenterRes)) == 0:
            print " Required no. of vcenter not found "
        else:
            vcenterId = self.vcenterRes[0]["refid"]
            
        serverList = globalVars.resourceInfo['SERVER']
        serverRefID1=''
        serverRefID2=''
		serverRefID3=''
        serverRefID4=''
        print " SERVER LIST : %s"%str(serverList)
        for device in serverList:
            if device['ip']==inputReqValueESXI.Server1_Entry:
                serverRefID1 = device['refid']
            if device['ip']==inputReqValueESXI.Server2_Entry:
                serverRefID2 = device['refid']
			if device['ip']==inputReqValueESXI.Server3_Entry:
                serverRefID3 = device['refid']
            if device['ip']==inputReqValueESXI.Server4_Entry:
                serverRefID4 = device['refid']
        payload = payload.replace("$serversource",'manual').replace("$Server1_Entry",serverRefID1).replace("$Server2_Entry",serverRefID2).replace("$Server3_Entry",serverRefID3).replace("$Server4_Entry",serverRefID4)

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
        statausDeploy = False
        DeplName='TestCase3_AsmStory4099_Comp'
        deplyDesc='Deploy 2 servers with Compellent volume – one 12G blade (5.1) and other 12G rack (5.5) ,1 cluster and 1 VM after Scale up 13G rack (5.1) and 13G blade (5.5)'
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
                        statausDeploy = True
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(testCaseID),'Failed','Deployment Service Failed','Server : Blade Server',  "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        break
            loop -= 1
            self.log_data( 'Going to call the ScaleUp  for the testcase id : %s'%str(testCaseID))
            #self.test_scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc)
            
        
        else:
            self.log_TestData(["", "", "",str(testCaseID),'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
        return statausDeploy
    
    def test_scaleUpTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc):
        logger = self.getLoggerInstance()
        testCaseID=self.getTestCaseID(__file__)
        scaleUpResponse = self.scaleUpdeployEsxiTemplate(templateID, deploymentRefId, DeplName, deplyDesc, globalVars.filename_TestCase3_AsmStory4099_Comp_Scaleup)
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
                        self.log_TestData(["", "", "", str(testCaseID),'Success',' scaleUp Template Deployed Successfully', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully ScaleUped Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()

                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(testCaseID),'Failed','Deployment ScaleUped Service Failed', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment ScaleUped Service Failed for the Deployment Name : %s'%DeplName)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(testCaseID),'Failed','Failed to deploy scaleUp Service'])
            self.log_data('Deployment scaleUp Service Failed for the Deployment Name : %s'%DeplName)
            
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

    

if __name__ == "__main__":
    test = Testcase()
    #test.preRunSetup()
    test.test_createTemplate()
      

