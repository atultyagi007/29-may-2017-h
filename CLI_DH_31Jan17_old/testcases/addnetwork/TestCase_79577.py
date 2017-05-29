'''
Created on Nov 24, 2015

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
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import networkConfiguration, inputForNetworkType, templateInputReqValueHyperV


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    FCoE- Flex - On a successful VMWare deployment, scale up a network    

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
            print " No BladeServers found of type : '%s'",str(serverType)
            

        
    def test_createTemplate(self):
        
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_79577)
        self.getResources()
        networkType=inputForNetworkType.TestCase_79577_networkType
        if not networkType:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        else:
            if networkType=='Blade_2Port_MXL_FLEXIOA':
                payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_2port_MXL_FLEXIOA)
            elif networkType=='Blade_4Port_MXL_FLEXIOA':
                payload=payload.replace("$networkConfiguration",networkConfiguration.Blade_4port_MXL_FLEXIOA)
            elif networkType=='Rack_2port_MXL_FLEXIOA':
                payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_2port_MXL_FLEXIOA)
            elif networkType=='Rack_4port_MXL_FLEXIOA':
                payload=payload.replace("$networkConfiguration",networkConfiguration.Rack_4port_MXL_FLEXIOA)
            else:
                print " ERROR : missing network configuration for the test case "
                self.log_data( 'ERROR : missing network configuration for the test case ')
                return
                
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
            self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template created  and published Successfully','Server : Blade Server'])
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
        DeplName='TestCase_79577'
        deplyDesc='FCoE- Flex - On a successful VMWare deployment, scale up a network'
        self.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc,publishedTemplateID)
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
                        self.log_TestData(["", "", "", str(self.tc_Id),'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.ScaleUpNetwork(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of service ')
#                         self.cleanDeployedService(deploymentRefId)
#                         self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        self.ScaleUpNetwork(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of service ')
                        #self.cleanDeployedService(deploymentRefId)
                        #self.test_cleanDeployedTemplates(deploymentRefId)
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Deployment Service Failed'])
            self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
            
    def ScaleUpNetwork(self,deploymentRefId):
        netcon = ""
        self.log_data(" Going to Create Network : Customer Public :: ")
        payload = self.readFile(globalVars.addNetworkPayload)
        payload = payload.replace("nw_name", inputForNetworkType.network_name_0).replace("nw_desc",inputForNetworkType.network_desc_0).replace("nw_type",inputForNetworkType.network_type_0 ).replace("nw_vlan",inputForNetworkType.network_vlan_0).replace("nw_static", "false")                        
        payload = payload.replace("nw_id", "")
        payload = payload.replace('\n', '').replace('\t', '')              
        response,status = self.getResponse("POST", "Network", payload=payload)
        print ' Setting Networks, Response : %s'%str(response)
        
        url = self.buildUrl("Deploy",deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        print " RESPONSE : \n"
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
                            netcon.text = networkConfiguration.Add_One_Network_FC_Flex
                            
                            
        deploymentResponse =  ET.tostring(self.root)
        print " \n MOFIFIED XML : \n "
        print deploymentResponse
        templateInputReqValueHyperV.workloadID=""
        templateInputReqValueHyperV.pXEID=""
        templateInputReqValueHyperV.hypervisorManagementID=""
        templateInputReqValueHyperV.vMotionID=""
        templateInputReqValueHyperV.clusterPrivateID=""
        templateInputReqValueHyperV.iSCSIID=""
        templateInputReqValueHyperV.FCoE1ID=""
        self.setNetworkList()
        time.sleep(15)
        deploymentResponse = deploymentResponse.replace("$Workload", templateInputReqValueHyperV.workloadID).replace("$PXE",templateInputReqValueHyperV.pXEID).replace("$HypervisorManagement", templateInputReqValueHyperV.hypervisorManagementID).replace("$VMotion", templateInputReqValueHyperV.vMotionID).replace("$ISCSI", templateInputReqValueHyperV.iSCSIID).replace("$FCoE1",templateInputReqValueHyperV.FCoE1ID).replace("$FIP",templateInputReqValueHyperV.FIPID).replace("$Add_Network_1",templateInputReqValueHyperV.Add_network_id_1)                                                
        print "\n XML AFTER CHANGING THE NETWORK PAYLOAD :  "
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
       
        
       
        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    #test.preRunSetup()
    test.test_createTemplate()
    os.chdir(current_dir)  

