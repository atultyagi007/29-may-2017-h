'''
Created on Jun 10, 2015

@author: waseem.irshad
'''


from util import globalVars
from util.utilityModule import UtilBase
from createdeploytemplate.TemplateBaseClass import TemplateTestBase
import json
import time
import xml.etree.cElementTree as ET
from discoverresources.DiscoverResourceBaseClass import DiscoverResourceTestBase
from testcases.common import serverPoolValue
from createdeploytemplate import inputReqValueESXI, networkConfiguration
from createdeploytemplate import inputForNetworkType


class ImportServerTest(UtilBase,TemplateTestBase,DiscoverResourceTestBase):
    
    tc_Id = "" 
    
    def __init__(self):
        UtilBase.__init__(self)
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
        testCaseID=self.getTestCaseID(__file__)
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        
        payload = self.readFile(globalVars.filename_Import_server_payload)
        self.getResources()   
        networkType = inputForNetworkType.TestCase_102213_networkType 
        if not networkType:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case')
            return
        if networkType=='Rack_Esxi_2PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_Esxi_2PORT)
        elif networkType=='Rack_Esxi_4PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_Esxi_4PORT)
        elif networkType=='Blade_Esxi_2PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_Esxi_2PORT)
        elif networkType=='Blade_Esxi_4PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_Esxi_4PORT)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data("ERROR : missing network configuration for the test case ")
            return
        serverRefID1=''
        if inputReqValueESXI.serversource=="manual":
            serverList = globalVars.resourceInfo['SERVER']
            print " Going to get refIds for manual selection of servers : "
            print " SERVER LIST : %s"%str(serverList)
            for device in serverList:
                if device['ip']==inputReqValueESXI.ServerEntry:
                    serverRefID1 = device['refid']
                if device['ip']==inputReqValueESXI.ImportReferenceServerIP:
                    refId1 = device['refid']
            payload =  payload.replace("$serversource","manual").replace("$ServerEntry",serverRefID1)
        logger.info( " Getting server Config xml : ")
        response = self.getResponse("GET", "Server",refId=refId1)
        logger.info("REFERENCE SERVER CONFIG INFORMATION ")
        logger.info(response)
        config_xml = response[0]["config"]
        payload=payload.replace("$Configxml",config_xml)  
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
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "",str(testCaseID),'Success','Template created  and published Successfully','Server : Blade Server'])
            self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "",str(testCaseID),'Failed','Failed to create/published template'])
            
            
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
        DeplName='Import Server Depl'
        deplyDesc='deploy a template with 1 storage volume and then try to add 2 volumes to a successful deployment'
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
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.log_data( 'Now going to call the teardown of service ')
                        self.cleanDeployedService(deploymentRefId)
                        self.test_cleanDeployedTemplates(deploymentRefId)
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


    
        
        
        
if __name__ == "__main__":
    test = ImportServerTest()
    test.preRunSetup()
    test.test_createTemplate()
    test.test_publishTemplate()
        