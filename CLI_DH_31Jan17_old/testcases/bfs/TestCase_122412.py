'''
Created on Sep 15, 2015

@author: waseem.irshad
'''


'''
    TestCase : Create template for BFS and deploy 1 server with Compellent storage
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
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration
import inputForNetworkType
import testCaseDescriptionMapping


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    
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
#             response = self.createServerPool(servers,poolName)
#             logger.info('response after createServerPool : ')
#             logger.debug(response)
        else:
            print " No BladeServers found of type : '%s'",str(serverType)

    
    def test_createTemplate(self):
        testCaseID=self.getTestCaseID(__file__)
        statausCreateTemplate = ''
        publishedTemplateID = ''
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.filename_TestCase_122412)
        self.getResources()
        globalVars.testCaseFlowName ='exsi'
#         networkType = inputForNetworkType.TestCase_122412_networkType
#         if not networkType:
#             print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
#             self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case')
#             return
#         if networkType=='Rack_Esxi_2PORT':
#             payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_Esxi_2PORT)
#         elif networkType=='Rack_Esxi_4PORT':
#             payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_Esxi_4PORT)
#         elif networkType=='Blade_Esxi_2PORT':
#             payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_Esxi_2PORT)
#         elif networkType=='Blade_Esxi_4PORT':
#             payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_Esxi_4PORT)
#         else:
#             print " ERROR : missing network configuration for the test case "
#             self.log_data("ERROR : missing network configuration for the test case ")
#             return
        self.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        compellentList = globalVars.resourceInfo['COMPELLENT']
        print " COMPELLENT LIST : %s"%str(compellentList)
        for device in compellentList:
            if device['ip']==globalVars.compellent_FCoE_MXL:
                storageId = device['refid']
            
        if len(compellentList) == 0:
            print "Required no. of COMPELLENT not available"
            
#         self.storageRes = self.getReqResource(limit=1, resourceType='COMPELLENT', deviceType=None)
#         if len(self.storageRes) == 0:
#             print "Required no. of Storage not available"
#         else:
#             storageId = self.storageRes[0]["refid"]
        self.vcenterRes = self.getReqResource(limit=1, resourceType='VCENTER', deviceType=None)
        if(len(self.vcenterRes)) == 0:
            print " Required no. of vcenter not found "
        else:
            vcenterId = self.vcenterRes[0]["refid"]
            
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
            publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "",str(testCaseID),testCaseDescriptionMapping.TestCase_122412, 'Success','Template created  and published Successfully','Server : Blade Server'])
            statausCreateTemplate= self.test_publishTemplate(publishedTemplateID)
            
        else:
            self.log_TestData(["", "", "",str(testCaseID), testCaseDescriptionMapping.TestCase_122412, 'Failed','Failed to create/published template'])    
        return statausCreateTemplate
    
    
    def test_publishTemplate(self,publishedTemplateID):
        logger = self.getLoggerInstance()
        logger.info("getting published templateID in test_publishTemplate ")
        logger.info(globalVars.publishedTemplateID)
        statausDeploy = False
        testCaseID=self.getTestCaseID(__file__)
        #response = self.authenticate()
#         templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
#         self.writeFile(globalVars.publishedTemp_filename, templateResult)
#         tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
#         root = tree.getroot()
#         ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_122412'
        deplyDesc=testCaseDescriptionMapping.TestCase_122412
        deployResponse = self.deployTemplate(DeplName,deplyDesc,publishedTemplateID)
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
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(testCaseID),'Failed','Deployment Service Failed','Server : Blade Server' , "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(testCaseID),'Failed','Deployment Service Failed'])
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
    
    status = test.test_createTemplate()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)

