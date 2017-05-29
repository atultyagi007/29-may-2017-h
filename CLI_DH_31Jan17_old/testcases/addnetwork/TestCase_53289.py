'''
Created on Nov 25, 2015

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
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import serverPoolValue
import inputReqValueESXI, networkConfiguration, inputForNetworkType
import testCaseDescriptionMapping
import templateInputReqValueHyperV


class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    iS - CO - Div - On a successful VMWare deployment, scale up a network
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        
#         respCSP = self.cleanServerPool()
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
        self.authenticate() 
        statausCreateTemplae = False
        response = self.authenticate()
        self.setNetworkList()
        payload = self.readFile(globalVars.filename_TestCase_53289)
        self.getResources()
        
        networkTypeServer1=inputForNetworkType.TestCase_53289_networkType
        #networkTypeServer2=inputForNetworkType.TestCase_126299_networkType_server2
        
        if not networkTypeServer1:
            print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
            self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
            return
        if networkTypeServer1=='Rack_ISCSICOMP_COV_ESXI_2PORT':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.Rack_ISCSICOMP_COV_ESXI_2PORT)
        elif networkTypeServer1=='Rack_ISCSICOMP_COV_ESXI_4PORT':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.Rack_ISCSICOMP_COV_ESXI_4PORT)
        elif networkTypeServer1=='Blade_ISCSICOMP_DIV_ESXI_2PORT':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_2PORT)
        elif networkTypeServer1=='Blade_ISCSICOMP_DIV_ESXI_4PORT':
            payload=payload.replace("$networkConfiguration1",networkConfiguration.Blade_ISCSICOMP_DIV_ESXI_4PORT)
        else:
            print " ERROR : missing network configuration for the test case "
            self.log_data( ' ERROR : missing network configuration for the test case ')
            return
        
        
#         if not networkTypeServer2:
#             print " Please define the server and port type value in inputForNetworkType parameter file for this test case "
#             self.log_data( 'Please define the server and port type value in inputForNetworkType parameter file for this test case ')
#             return
#         if networkTypeServer2=='Rack_ISCSICOMP_COV_ESXI_2PORT':
#             payload=payload.replace("$networkConfiguration2",networkConfiguration.Rack_ISCSICOMP_COV_ESXI_2PORT)
#         elif networkTypeServer2=='Rack_ISCSICOMP_COV_ESXI_4PORT':
#             payload=payload.replace("$networkConfiguration2",networkConfiguration.Rack_ISCSICOMP_COV_ESXI_4PORT)
#         elif networkTypeServer2=='Blade_ISCSICOMP_COV_ESXI_2PORT':
#             payload=payload.replace("$networkConfiguration2",networkConfiguration.Blade_ISCSICOMP_COV_ESXI_2PORT)
#         elif networkTypeServer2=='Blade_ISCSICOMP_COV_ESXI_4PORT':
#             payload=payload.replace("$networkConfiguration2",networkConfiguration.Blade_ISCSICOMP_COV_ESXI_4PORT)
#         else:
#             print " ERROR : missing network configuration for the test case "
#             self.log_data( ' ERROR : missing network configuration for the test case ')
#             return
        
        self.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        
        compellentList = globalVars.resourceInfo['COMPELLENT']
        print " COMPELLENT LIST : %s"%str(compellentList)
        for device in compellentList:
            if device['ip']==globalVars.compellent_ISCSI:
                storageId = device['refid']
        if len(compellentList) == 0:
            print "Required no. of COMPELLENT not available"
            
        vcenterList = globalVars.resourceInfo['VCENTER']
        print " VCENTER LIST : %s"%str(vcenterList)
        for device in vcenterList:
            if device['ip']==globalVars.vcenter_ISCSI:
                vcenterId = device['refid']
                
        if len(vcenterList) == 0:
            print "Required no. of VCENTER not available"
        
        
        globalVars.refIdVCenter=vcenterId
        globalVars.refIdEQLogic=storageId
        
        templateResponse= self.createTemplateEsxi(payload,vcenterId,storageId) 
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            self.log_TestData(["", "", "",str(self.tc_Id), testCaseDescriptionMapping.TestCase_53289, 'Success','Template created  and published Successfully','Server : Blade Server'])
            self.log_data( 'Successfully created  and published Template ')
            statausCreateTemplae = self.test_publishTemplate()
            
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), testCaseDescriptionMapping.TestCase_53289, 'Failed','Failed to create/published template'])
            self.log_data( 'Failed to create/published template ')
            
        return statausCreateTemplae
            
    def test_publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        self.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        statausDeploy = False
        DeplName='TestCase_53289'
        deplyDesc=testCaseDescriptionMapping.TestCase_53289
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
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                        self.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.ScaleUpNetwork(deploymentRefId)
#                         self.log_data( 'Going to do VCenter Validation before TearDown ')
#                         self.doVCenterValidations(globalVars.refIdVCenter)
#                         self.log_data( 'Now going to call the teardown of service ')
#                         self.cleanDeployedService(deploymentRefId)
#                         self.test_cleanDeployedTemplates(deploymentRefId)
#                         self.log_data( 'Now going to call the teardown of Template ')
#                         self.test_cleanePublishedTemplates()
#                         self.log_data( 'Going to do VCenter Validation after TearDown')
#                         self.doVCenterValidations(globalVars.refIdVCenter)
                        break
                    else:
                        print "Deployment Status: %s"%resDS
                        self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                        self.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        self.ScaleUpNetwork(deploymentRefId)
#                         self.runWebServiceAPI(str(self.tc_Id), "Fail", "Run in regression test")
#                         if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
#                             self.log_data( 'Now going to call the teardown of service ')
#                             self.cleanDeployedService(deploymentRefId)
#                             self.test_cleanDeployedTemplates(deploymentRefId)
                        
                        break
            loop -= 1
        
        else:
            self.log_TestData(["", "", "",str(self.tc_Id), deplyDesc, 'Failed','Deployment Service Failed'])
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
                            netcon.text = networkConfiguration.Add_One_Network_Div_Esxi
                            
                            
        deploymentResponse =  ET.tostring(self.root)
        print " \n MOFIFIED XML : \n "
        print deploymentResponse
        self.setNetworkList()
        deploymentResponse = deploymentResponse.replace("$Workload", templateInputReqValueHyperV.workloadID).replace("$PXE", templateInputReqValueHyperV.pXEID).replace("$HypervisorManagement", templateInputReqValueHyperV.hypervisorManagementID).replace("$VMotion", templateInputReqValueHyperV.vMotionID).replace("$ISCSI", templateInputReqValueHyperV.iSCSIID).replace("$ISC_1", templateInputReqValueHyperV.ISCSI1).replace("$Add_Network_1",templateInputReqValueHyperV.Add_network_id_1)                                                
        print "\n XML AFTER CHANGING THE NETWORK PAYLOAD :  "
        print deploymentResponse
        
#         print " \n\n Going to Add Network to VM ::"
#         s1 = "<id>network_interfaces</id><value>"+templateInputReqValueHyperV.workloadID+"</value>"
#         s2 = "<id>network_interfaces</id><value>"+templateInputReqValueHyperV.workloadID+","+templateInputReqValueHyperV.Add_network_id_1+"</value>"
#         if s1 in deploymentResponse:
#             deploymentResponse = deploymentResponse.replace(s1,s2)
#             resNET, statNET = self.getResponse("GET", "Network")
#             print " \n GET Network Response : \n%s"%str(resNET)
#             for nw in resNET:
#                 if nw["name"] == "Workload":
#                     vlanIdWl = nw["vlanId"]
#                     descWl = nw['description']
#                     idWL = nw['id']
#                     print " \n nw[static] == %s"%str(nw['static'])
#                     if nw["static"]=="true" or nw['static']==True:
#                         print " \n IN IF ...."
#                         print " \n INFO :  WORKLOAD IS STATIC "
#                         staticnwcnf = nw["staticNetworkConfiguration"]
#                         print " \n staticnwcnf = %s"%str(staticnwcnf)
#                         gateway = staticnwcnf['gateway']
#                         print "\n gateway : %s"%str(gateway)
#                         subnet = staticnwcnf['subnet']
#                         print "\n subnet : %s"%str(subnet)
#                         primaryDns = staticnwcnf['primaryDns']
#                         print "\n primaryDns : %s"%str(primaryDns)
#                         secondaryDns = staticnwcnf['secondaryDns']
#                         print " \n secondaryDns : %s"%str(secondaryDns)
#                         ipRangeId = staticnwcnf["ipRange"][0]["id"]
#                         print " \n ipRangeId : %s"%str(ipRangeId)
#                         startingIp = staticnwcnf["ipRange"][0]["startingIp"]
#                         print " \n startingIp : %s"%str(startingIp)
#                         endingIp = staticnwcnf["ipRange"][0]["endingIp"]
#                         print " \n endingIp : %s"%str(endingIp)
#                         temp = self.readFile(globalVars.vm_wl_static)
#                         print "\n WorkloadID : %s"%str(idWL)
#                         print " \n Workload Description : %s"%str(descWl)
#                         temp = temp.replace("$WorkLoadId",idWL).replace("$WLDesc",descWl).replace("$WLVlanID",str(vlanIdWl)).replace("$gatewayIP",str(gateway)).replace("$subnetIP",str(subnet)).replace("$primaryDNS",str(primaryDns)).replace("$secondaryDNS",str(secondaryDns)).replace("$ipRangeID",ipRangeId).replace("$startingIP",str(startingIp)).replace("$endingIP",str(endingIp))
#                         print " \n\n Workload xml with static configuration : %s"%str(temp)
#                         temp1 = temp + "<networks><id>$Add_network_1_id</id><name>$nw_name</name><type>PUBLIC_LAN</type><vlanId>$nw_vlan_id</vlanId><static>false</static></networks>"
#                         temp1 = temp1.replace("$Add_network_1_id",templateInputReqValueHyperV.Add_network_id_1).replace("$nw_name",inputForNetworkType.network_name_0).replace("$nw_vlan_id",inputForNetworkType.network_vlan_0)
#                         print " \n Final xml to be replaced : %s"%str(temp1)
#                         if temp in deploymentResponse:
#                             deploymentResponse = deploymentResponse.replace(temp,temp1)
#                             
#                     else:
#                         print " \n IN ELSE ..."
#                         temp = """<networks><id>$WorkloadID</id><name>Workload</name><description>WorkloadNetwork</description><type>PUBLIC_LAN</type><vlanId>$VlanIdWl</vlanId><static>false</static></networks><networks><id>$Add_network_1_id</id><name>$nw_name</name><type>PUBLIC_LAN</type><vlanId>$nw_vlan_id</vlanId><static>false</static></networks>"""  
#                         temp = temp.replace("$WorkloadID",templateInputReqValueHyperV.workloadID).replace("$VlanIdWl",str(vlanIdWl)).replace("$Add_network_1_id",templateInputReqValueHyperV.Add_network_id_1).replace("$nw_name",inputForNetworkType.network_name_0).replace("$nw_vlan_id",inputForNetworkType.network_vlan_0)
#                         print " \n temp : %s"%str(temp)
#              
#                         temp1 = """<networks><id>$WorkloadID</id><name>Workload</name><description>Workload</description><type>PUBLIC_LAN</type><vlanId>$VlanIdWl</vlanId><static>false</static></networks><options><value>$WorkloadID</value><name>Workload</name></options>"""
#                         temp1 = temp1.replace("$WorkloadID",templateInputReqValueHyperV.workloadID).replace("$VlanIdWl",str(vlanIdWl))
#                         print " \n temp 1 : %s"%str(temp1)
#                         if temp1 in deploymentResponse:
#                             print " \n Found temp1 in Response !!!"
#                             deploymentResponse = deploymentResponse.replace(temp1,temp)
#             
#         print "\n\n  XML AFTER CHANGING THE VM NETWORK PAYLOAD :  "
#         print deploymentResponse
#         
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


    
    def test_sanity(self):
        self.test_createTemplate()
        self.test_cleaneServerPool()
       
   
            
       
        

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

