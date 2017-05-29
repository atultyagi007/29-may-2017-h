'''
Created on Oct 19, 2015

@author: waseem.irshad
'''

import os
import sys
import requests
import datetime
 
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
import templateInputReqValueHyperV

class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Add 2 network to 2 hosts on a successful ESXi deployment
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
        statausCreateTemplae = False
        payload = self.readFile(globalVars.filename_TestCase_111119)
        self.getResources()
        networkType=inputForNetworkType.TestCase_111119_networkType
        if not networkType:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
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
            self.log_data( ' ERROR : missing network configuration for the test case ')
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
                                  
            self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template created  and published Successfully','Server :2 Blade Servers'])
            statausCreateTemplae = self.test_publishTemplate()
            
        else:
            self.log_data( 'Failed to create/published template ')
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Failed to create/published template'])
            
        return statausCreateTemplae
            
    def test_publishTemplate(self):
        
        testCaseID=self.getTestCaseID(__file__)
        statausDeploy = False
        templateResult= self.getPublishedTemplateData(templateIdValue)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        DeplName='TestCase_111119'
        deplyDesc='Add 2 network to 2 hosts on a successful ESXi deployment'
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
                        statausDeploy = True
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.ScaleUpNetwork(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of service ')
#                         self.cleanDeployedService(deploymentRefId)
#                         self.test_cleanDeployedTemplates(deploymentRefId)
                        #self.log_data( 'Now going to call the teardown of Template ')
                        #self.test_cleanePublishedTemplates()
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        self.ScaleUpNetwork(deploymentRefId)
                        #self.log_data( 'Now going to call the teardown of service ')
                        #self.cleanDeployedService(deploymentRefId)
                        #self.test_cleanDeployedTemplates(deploymentRefId)
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(self.tc_Id),'Failed','Deployment Service Failed'])
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
        
        payload = ""
        self.log_data(" Going to Create second Network : Cust_Public_1 :: ")
        payload = self.readFile(globalVars.addNetworkPayload)
        payload = payload.replace("nw_name", inputForNetworkType.network_name_1).replace("nw_desc",inputForNetworkType.network_desc_1).replace("nw_type",inputForNetworkType.network_type_1).replace("nw_vlan",inputForNetworkType.network_vlan_1).replace("nw_static", "false")                        
        payload = payload.replace("nw_id", "")
        payload = payload.replace('\n', '').replace('\t', '')              
        response,status = self.getResponse("POST", "Network", payload=payload)
        

        url = self.buildUrl("Deploy",deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        
        self.root = ET.fromstring(deploymentResponse.content)
        
        print ET.tostring(self.root)
        for st in self.root.findall('serviceTemplate'):
            for cmpnt in st.findall('components'):
                for res in cmpnt.findall('resources'):
                    for param in res.findall('parameters'):
                        if param.findtext('id') == 'network_configuration':
                            print " \n Found networkConfiguration : \n"
                            netcon = param.find('value')
                            netcon.text = networkConfiguration.Add_Two_Network_Esxi
                             
                             
        deploymentResponse =  ET.tostring(self.root)
        
        self.setNetworkList()
        deploymentResponse = deploymentResponse.replace("$Workload", templateInputReqValueHyperV.workloadID).replace("$PXE", templateInputReqValueHyperV.pXEID).replace("$HypervisorManagement", templateInputReqValueHyperV.hypervisorManagementID).replace("$VMotion", templateInputReqValueHyperV.vMotionID).replace("$ISCSI", templateInputReqValueHyperV.iSCSIID).replace("$Add_Network_1",templateInputReqValueHyperV.Add_network_id_1).replace("$Add_Network_2",templateInputReqValueHyperV.Add_network_id_2)                                                
         
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headersPut=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        payload=deploymentResponse
        startTime = datetime.datetime.now()
        responseAddNw = requests.put(url, data=payload, headers=headersPut)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headersPut,payload), responseAddNw.status_code, responseAddNw.text, startTime, endTime, elapsedTime)
        
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
                        self.log_TestData(["", "", "", str(self.tc_Id),'Success',' scaleUp of Network done Successfully', 'Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully ScaleUped Network for the Deployment Name : %s'%DeplName)
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

