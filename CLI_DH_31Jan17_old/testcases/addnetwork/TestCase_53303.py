'''
Created on Dec 7, 2015

@author: waseem.irshad
'''

import os
import sys
import datetime
import requests
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
import testCaseDescriptionMapping
import csv
import time
import xml.etree.cElementTree as ET
import serverPoolValue
import DiscoverResourceBaseClass
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import templateInputReqValueHyperV, networkConfiguration, inputForNetworkType

class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    NetApp - On a successful HyperV deployment, scale up a network
    """ 
    tc_Id=""
    
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        logger = self.getLoggerInstance()
        logger.info(" Running Pre Run Setup")
        logger.info(" Cleaning Server Pools.......")
        respCSP = self.cleanServerPool()
        logger.info(respCSP)
        logger.info(" Getting the devices : ")
        response =self.getDeviceList()
        discoveredDevicesValue=response[0]
        logger.info( " discoveredDevicesValue : ")
        logger.info(discoveredDevicesValue)
        servers = []
        serverType = serverPoolValue.TestCase_78811
        for device in discoveredDevicesValue:
            devicetype=device['deviceType']
            serverModel=device['model']
            if (devicetype == "RackServer" and serverType in serverModel):
                servers.append(device)
        
        logger.info(' Servers : ')
        logger.debug(servers)
        if(len(servers) > 0):
            poolName = globalVars.serverPoolName
            response = self.createServerPool(servers,poolName)
            logger.info('response after createServerPool : ')
            logger.debug(response)
        else:
            print " No RackServers found of type : '%s'",str(serverType)

        
    def test_createTemplate(self):
        testCaseID=self.getTestCaseID(__file__)
        response = self.authenticate()
        self.setNetworkList()
        logger = self.getLoggerInstance()
        logger.info("getting published templateID in test_publishTemplate ")
        logger.info(globalVars.publishedTemplateID)
        
        payload = self.readFile(globalVars.filename_TestCase_53303)
        self.getResources()
        networkType=inputForNetworkType.TestCase_53303_networkType
        statausCreateTemplae = False
        if not networkType:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkType=='Netapp_HyperV_2PORT':
            payload=payload.replace("$networkConfiguration",networkConfiguration.Netapp_HyperV_2PORT)
        elif networkType=='RackHperV_NetApp_4Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.RackHyperV_NetApp_4Port)
        elif networkType=='BladeHyperV_NetApp_2Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.BladeHyperV_NetApp_2Port)
        elif networkType=='BladeHyperV_NetApp_4Port':
            payload=payload.replace("$networkConfiguration",networkConfiguration.BladeHyperV_NetApp_4Port)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( 'ERROR : missing network configuration for the test case ')
            return
        storageId =""
        scvmmId=""
        netappList = globalVars.resourceInfo['NETAPP']
        print " NetApp LIST : %s"%str(netappList)
        for device in netappList:
            if device['ip']==globalVars.Storage_Netapp:
                storageId = device['refid']
            
        if len(netappList) == 0:
            print "Required no. of Netapp not available"
            
        self.scvmmRes = self.getReqResource(limit=1, resourceType='SCVMM', deviceType=None)
        if len(self.scvmmRes) == 0:
            print " Required no. of scvmm not present "
        else:
            scvmmId = self.scvmmRes[0]["refid"]
            
        globalVars.refIdSCVMM = scvmmId
        templateResponse= self.createTemplate(payload, storageId, scvmmId)              
        logger.info( "printing the response from the deploy template")
        logger.debug( templateResponse.content)
        logger.info( "printing the status code")
        logger.info(templateResponse.status_code)
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template created  and published Successfully','Server:2 R620 Rack Servers'])
            self.log_data( 'Successfully created  and published Template ')
            statausCreateTemplae = self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
        return statausCreateTemplae
            
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
        DeplName='Testcase_53303_scaleup'
        deplyDesc='NetApp - On a successful HyperV deployment, scale up a network'
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        logger.info( "printing the response from the deploy template")
        logger.debug( deployResponse.content)
        logger.info( "printing the status code")
        logger.info( deployResponse.status_code)
        statausDeploy = False
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
                        self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template Deployed Successfully','Server : Rack Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.ScaleUpNetwork(deploymentRefId)
#                         self.log_data( 'Going to do SCVMM Validation before TearDown')
#                         self.doScvmmValidation(globalVars.refIdSCVMM)
#                         self.log_data( 'Now going to call the teardown of service ')
#                         self.cleanDeployedService(deploymentRefId)
#                         self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
#                         self.log_data( 'Going to do SCVMM Validation after TearDown')
#                         self.doScvmmValidation(globalVars.refIdSCVMM)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed','Server : Rack Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.ScaleUpNetwork(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of service ')
#                         self.cleanDeployedService(deploymentRefId)
#                         self.test_cleanDeployedTemplates(deploymentRefId)
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
        return statausDeploy
    
    def ScaleUpNetwork(self,deploymentRefId):
        netcon = ""
        self.log_data(" Going to Create Network : Customer Public :: ")
        payload = self.readFile(globalVars.addNetworkPayload)
        payload = payload.replace("nw_name", inputForNetworkType.network_name_0).replace("nw_desc",inputForNetworkType.network_desc_0).replace("nw_type",inputForNetworkType.network_type_0 ).replace("nw_vlan",inputForNetworkType.network_vlan_0).replace("nw_static", "false")                        
        payload = payload.replace("nw_id", "")
        payload = payload.replace('\n', '').replace('\t', '')              
        response,status = self.getResponse("POST", "Network", payload=payload)
        print ' Setting Networks, Response : %s'%str(response)
#         print " \n \n GET Network before PUT : "
#         print self.getResponse("GET","Network")
        url = self.buildUrl("Deploy",deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        print "\n RESPONSE BEFORE PUT: \n"
        print deploymentResponse.content
        self.root = ET.fromstring(deploymentResponse.content)
        print " ROOT : "
        print ET.tostring(self.root)
        for st in self.root.findall('serviceTemplate'):
            for cmpnt in st.findall('components'):
                for res in cmpnt.findall('resources'):
                    for param in res.findall('parameters'):
                        if param.findtext('id') == 'network_configuration':
                            print " \n Found networkConfiguration : \n"
                            netcon = param.find('value')
                            netcon.text = networkConfiguration.Add_One_Network_Netapp_HyperV
                            
                            
        deploymentResponse =  ET.tostring(self.root)
        print " \n MOFIFIED XML : \n "
        print deploymentResponse
        self.setNetworkList()
        deploymentResponse = deploymentResponse.replace("$Workload", templateInputReqValueHyperV.workloadID).replace("$PXE", templateInputReqValueHyperV.pXEID).replace("$HypervisorManagement", templateInputReqValueHyperV.hypervisorManagementID).replace("$VMotion", templateInputReqValueHyperV.vMotionID).replace("$ClusterPrivate", templateInputReqValueHyperV.clusterPrivateID).replace("$Fileshare", templateInputReqValueHyperV.Fileshare).replace("$Add_Network_1",templateInputReqValueHyperV.Add_network_id_1)                                                
        print "\n XML AFTER CHANGING THE PAYLOAD :  "
        print deploymentResponse
        
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headersPut=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        payload=deploymentResponse
        startTime = datetime.datetime.now()
        responseAddNw = requests.put(url, data=payload, headers=headersPut)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headersPut,payload), responseAddNw.status_code, responseAddNw.text, startTime, endTime, elapsedTime)
        print " \n \n RESPONSE OF ADD NETWORK ::"
        print responseAddNw.text
        if responseAddNw.status_code in (200, 201, 202, 203, 204):        
            #Get Deployment Id
#             deploymentRefId = self.getDeploymentId(DeplName)
            loop = 60
            deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
            deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            print " \n \n GET Network After PUT : "
            print self.getResponse("GET","Network")
            while loop:
                resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                if resDS.lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    if resDS.lower() in ("complete"):
                        print "Deployment Status: %s"%resDS
                        break
                        #self.log_TestData(["", "", "", str(self.tc_Id),'Success',' scaleUp Template Deployed Successfully', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        #self.log_data( 'Successfully ScaleUped Service for the Deployment Name : %s'%DeplName)
                    else:
                        print "ScaleUp Deployment Status: %s"%resDS
                        break
            loop -= 1
        else:
            print " Failed to scaleup deployment with network "


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
    status = test.test_createTemplate()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)

