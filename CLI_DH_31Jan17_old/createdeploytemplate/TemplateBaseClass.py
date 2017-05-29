'''
Created on Aug 24, 2014

@author: dheeraj_si
'''

import requests
import time
import json
import globalVars
from utilityModule import UtilBase
import logging
import logging.config
from logging import Logger, getLogger
from cookielib import logger
import templateInputReqValueHyperV
import inputReqValueESXI
import inputForWorkFlowCases

import datetime
import xml.etree.ElementTree as ET

class TemplateTestBase(UtilBase):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        UtilBase.__init__(self)
        
    def setNetworkList(self):
        logger = self.getLoggerInstance()
        logger.info( "getting the list of network")
        response =self.getNetworkList()
        discoveredNetworkValue=response[0]
        print response
        for network in discoveredNetworkValue:
            networkName=network['name']
            if networkName == "autoPublicLAN":
                templateInputReqValueHyperV.workloadID=network['id']
            elif networkName == "PXE":
                templateInputReqValueHyperV.pXEID=network['id']
            elif networkName == "autoHypervisorManagement":
                templateInputReqValueHyperV.hypervisorManagementID=network['id']
            elif networkName == "vMotion":
                templateInputReqValueHyperV.vMotionID=network['id']
            elif networkName == "Cluster Private":
                templateInputReqValueHyperV.clusterPrivateID=network['id']
            elif networkName == "iSCSI":
                templateInputReqValueHyperV.iSCSIID=network['id']
            elif networkName == "FCoE1":
                templateInputReqValueHyperV.FCoE1ID = network['id']
            elif networkName == "FCoE2":
                templateInputReqValueHyperV.FC_oE2_ID = network['id']
            elif networkName == "FIP":
                templateInputReqValueHyperV.FIPID = network['id']    
            elif networkName == "iSCSI1":
                templateInputReqValueHyperV.ISC_1_ID = network['id']
            elif networkName == "Fileshare":
                templateInputReqValueHyperV.FileshareID = network['id']            
            else:
                print " no  match found while getting the network list"
                logger.info( "no  match found while getting the network list")
                
    def  getNetworkList(self):
        """
        Get the list of Networks
        """
        return self.getResponse("GET", "Network")

    def createTemplate(self, payload, refIdStorage = None, refIDSCVMM = None):
        """
        Creates ServiceTemplate
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Template")
        logger.info(url)
        
        postData = self.getTemplatePayload(payload, refIdStorage, refIDSCVMM)
        logger.info(' template payload : ')
        logger.debug(postData)
        
        uri = globalVars.serviceUriInfo["Template"]
        logger.info(uri)
        headers=self.generateSecurityHeader(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        logger.info("printing the header of the create template")
        logger.info(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response
    
    def getServerPoolId(self,poolName):
        response = self.getResponse("GET", "ServerPool")
        poolList = response[0]
        self.log_data( " poolName : ")
        self.log_data(poolName)
        foundGroup =False
        groupSeqId=""
        for grpName in poolList:
            if (grpName['groupName'] == poolName):
                foundGroup=True
                groupSeqId = grpName['groupSeqId']
                break
            else:
                foundGroup=False
        
        return str(groupSeqId), foundGroup
    
    def getTemplatePayload(self, payload, refIdStorage = None, refIDSCVMM = None):
        
        poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolName)
        if not foundGroup:
            poolId = templateInputReqValueHyperV.GlobalPool
        Volume_1= self.getVolumeName()
        Volume_2= self.getVolumeName()
        NewClusterName= self.getClusterName()
        globalVars.dcNamescvmm = NewClusterName
        Host_1= self.getHostName()
        Host_2= self.getHostName()
        NewHostGroup= self.getHostGroupName()
        globalVars.clusterNamescvmm = NewHostGroup        
        
        payload = payload.replace("$target_boot_device_value", templateInputReqValueHyperV.target_boot_device_value).replace("$OSImage_value", templateInputReqValueHyperV.OSImage_value).replace("$os_image_version", templateInputReqValueHyperV.os_image_version).replace("$AdminConfirmPassword", templateInputReqValueHyperV.AdminConfirmPassword).replace("$Adminpassword", templateInputReqValueHyperV.Adminpassword)
        payload = payload.replace("$product_key", templateInputReqValueHyperV.product_key).replace("$ntp", templateInputReqValueHyperV.ntp).replace("$domain_name", templateInputReqValueHyperV.domain_name).replace("$fqdn", templateInputReqValueHyperV.fqdn).replace("$DomainAdminPsswd", templateInputReqValueHyperV.DomainAdminPsswd).replace("$Domainconfirm", templateInputReqValueHyperV.Domainconfirm)
        payload = payload.replace("$Workload", templateInputReqValueHyperV.workloadID).replace("$PXE", templateInputReqValueHyperV.pXEID).replace("$HypervisorManagement", templateInputReqValueHyperV.hypervisorManagementID).replace("$VMotion", templateInputReqValueHyperV.vMotionID).replace("$ClusterPrivate", templateInputReqValueHyperV.clusterPrivateID).replace("$ISCSI", templateInputReqValueHyperV.iSCSIID)
        payload = payload.replace("$Volume_1", Volume_1).replace("$Volume_2", Volume_2).replace("$HyperMgmtClusterValue", templateInputReqValueHyperV.HyperMgmtClusterValue).replace("$NewClusterName", NewClusterName)
        payload = payload.replace("$GlobalPool", poolId).replace("$domainadminuser", templateInputReqValueHyperV.domainadminuser)
        payload = payload.replace("$OS_Image_valueR2",templateInputReqValueHyperV.OS_Image_valueR2).replace("$os_image_vR2",templateInputReqValueHyperV.os_image_vR2).replace("$prod_key_R2",templateInputReqValueHyperV.prod_key_R2)
        payload = payload.replace("$hostname",templateInputReqValueHyperV.hostname).replace("$description",templateInputReqValueHyperV.description).replace("$vmtemplatename",templateInputReqValueHyperV.vmtemplatename).replace("$newhost",templateInputReqValueHyperV.newhost).replace("$path",templateInputReqValueHyperV.path)
        payload = payload.replace("$blockdynamicoptimization",templateInputReqValueHyperV.blockdynamicoptimization).replace("$highlyavailable",templateInputReqValueHyperV.highlyavailable).replace("$cpucount",templateInputReqValueHyperV.cpucount).replace("$memorymb",templateInputReqValueHyperV.memorymb).replace("$startaction",templateInputReqValueHyperV.startaction).replace("$stopaction",templateInputReqValueHyperV.stopaction)
        payload = payload.replace("$centosvmtemplatename",templateInputReqValueHyperV.centosvmtemplatename)
        payload = payload.replace("$Host_1",Host_1).replace("$Host_2",Host_2).replace("$NewHostGroup",NewHostGroup).replace("$ExistingHost",templateInputReqValueHyperV.ExistingHost).replace("$R2HyperVMgmtValue",templateInputReqValueHyperV.R2HyperVMgmtValue)
        payload = payload.replace("$serversource",templateInputReqValueHyperV.serversource).replace("$ServerEntry",templateInputReqValueHyperV.ServerEntry).replace("$managementip",templateInputReqValueHyperV.managementip).replace("$migration",templateInputReqValueHyperV.migration).replace("$staticIP",templateInputReqValueHyperV.staticIP)
        payload = payload.replace("$AutogenerateHostName",templateInputReqValueHyperV.AutogenerateHostName)
        payload = payload.replace("$Volume1_size",templateInputReqValueHyperV.Volume1_size).replace("$Volume2_size",templateInputReqValueHyperV.Volume2_size)


        
        if refIdStorage:
            payload = payload.replace("$StorageRefId",refIdStorage)
        if refIDSCVMM:
            payload = payload.replace("$HyperMgmtClusterValue", refIDSCVMM)
        
        return payload
    
    def getPublishedTemplateData(self, idvalue):
        """
        Return a whole template based on the template ID
        """
        
        url = self.buildUrl("Template", idvalue)
        uri = globalVars.serviceUriInfo["Template"]
        
        headers=self.generateHeaderDeploy(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        
        getrespone = requests.get(url, headers=headers, verify=False)
        
        return getrespone.content
    
    def deployTemplate(self, DeplName, deplyDesc,specificstandarduser=None):
        """
        Deploy ServiceTemplate
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy")
        deployName=DeplName
        deployDesc=deplyDesc
        logger.info("printing deploy url")
        logger.info(url)
        postData = self.getDeployPayload(deployName, deployDesc,specificstandarduser=specificstandarduser)
        logger.info(' deploy payload : ')
        logger.debug(postData)
        uri = globalVars.serviceUriInfo["Deploy"]
        self.log_data(uri)
        headers=self.generateHeaderDeploy(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.content, startTime, endTime, elapsedTime)
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response
    
    def getDeployPayload(self, DeplName, deplyDesc,specificstandarduser=None):
        logger = self.getLoggerInstance()
        userId = None
        if specificstandarduser:
            payload = self.readFile(globalVars.deploy_filename_standardUser)
            userId=self.getUserSeqId(specificstandardname=specificstandarduser)
        else:
            payload = self.readFile(globalVars.deploy_filename)
        subPayload=self.readFile(globalVars.publishedTemp_filename)
        payload = payload.replace("$dynamic_ServiceTemplate", subPayload)
        payload =payload.replace("ServiceTemplate", "serviceTemplate")
        payload =payload.replace("$userName", globalVars.userName).replace("$DeplName", DeplName).replace("$deplyDesc", deplyDesc).replace("$noOfDeploy", inputReqValueESXI.numberOfDeployments)
        payload = payload.replace("$userSeqId",str(userId))
        if inputReqValueESXI.scheduleddeployment == 'Y':
            payload =payload.replace("$scheduledTimestamp", inputReqValueESXI.scheduledTimestamp)
        else:
            payload =payload.replace("$scheduledTimestamp", '')
            
        
        logger.info("printing  payload of deployment")
        #logger.debug(payload)
        
        return payload
    
    def getdeployTemplate(self, url):
        """
        Deploy ServiceTemplate
        """
        uri = globalVars.serviceUriInfo["Deploy"]
        self.log_data( "url")
        self.log_data(url)
        
        headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data("Printing headers information")
        self.log_data(headers)
        response = requests.get(url, headers=headers, verify=False)
        data = json.loads(response.text)
        result =self.convertUTA(data)
        self.log_data('result')
        self.log_data(result)
        
        #print resutlt["id"]
        
        
        #print ' depylment name '
        
        #print resutlt["deploymentName"]
        
        #print resutlt[0]["id"]
        
        #response = requests.post(uri, data=postData)
        #time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response
    
    
    def getDeploymentId(self, deploymentName):
        """
        Gets the Deployment Reference Id  
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy")
        logger.info("printing deploy url")
        logger.info(url)
        uri = globalVars.serviceUriInfo["Deploy"]
        #headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url, headers=headers, verify=False)
        #data = json.loads(response.text)
        #result =self.convertUTA(data)
        deployID=''
        root = ET.fromstring(response.content)
        for cmpnt in root.findall('Deployment'):
                indTD = cmpnt.find('deploymentName')
                resultDS = indTD.text
                if resultDS == deploymentName:
                    indID = cmpnt.find('id')
                    deployID = indID.text
        
        
        
#         for device in result:
#             if device['deploymentName']==deploymentName:
#                 deployID = device['id']
#                 self.log_data(" deployment ID")
#                 self.log_data(deployID)
                
        return deployID
    
    
    def getAllDeploymentIdList(self, deploymentName, deployRefId):
        """
        Gets the Deployment Reference Id  
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy")
        logger.info("printing deploy url")
        logger.info(url)
        uri = globalVars.serviceUriInfo["Deploy"]
        headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url, headers=headers, verify=False)
        data = json.loads(response.text)
        result =self.convertUTA(data)
        deployID=''
        deployIDList = []
        for device in result:
            if deploymentName in device['deploymentName']:
                deployID = device['id']
                if deployID != deployRefId:
                    deployIDList.append(deployID)
                
        self.log_data(" deployment ID List")
        self.log_data(deployIDList)               
        return deployIDList

    
    
    def getDeploymentInfo(self, refId, responseType="json"):
        """
        Gets the Deployment Reference Id  
        """
        return self.getResponse("GET", "Deploy", refId=refId, responseType=responseType)
        
        
    
    def scaleUpdeployTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc, fileName):
        """
        Scale up Server to the existing Deployment
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy", deploymentRefId)
        logger.info("printing deploy scaleup  url")
        logger.info(url)
        postData = self.getscaleUpPayload(templateID, deploymentRefId,  DeplName, deplyDesc, fileName)
        logger.info(' deploy scaleup payload : ')
        logger.debug(postData)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data( headers)
        response = requests.put(url, data=postData, headers=headers, verify=False)
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        
        return response
    
    
    def getscaleUpPayload(self,templateID, deploymentRefId,  DeplName, deplyDesc, fileName):
    
        logger = self.getLoggerInstance()
        payload = self.readFile(fileName)
        
        refIdSCVMM = globalVars.refIdVCenter
        refIdEQ =  globalVars.refIdEQLogic
        
        resDT, statDT = self.getSuccessfullyDeploymentxml(deploymentRefId)
        if not statDT:
            return "Unable to retrieve Deployed Template: %s"%resDT, statDT       
                  
        temp = str(resDT).rpartition("</components>")
        deploymentTemplate = temp[0] + temp[1] + payload + temp[2]    
       
        deploymentTemplate = deploymentTemplate.replace("<retry>false</retry>", "<retry>true</retry>")
        payload =self.getTemplatePayload(deploymentTemplate, refIdEQ, refIdSCVMM)
        logger.info("printing  payload of scaleup deployment")
        logger.debug(payload)
        
        return payload
    
    def createTemplateEsxi(self, payload,refIdVC = None,refIDEQ = None,existTemplateName=""):
        """
        Creates ServiceTemplate
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Template")
        logger.info(url)
        
        postData = self.getTemplatePayloadEsxi(payload,refIdVC,refIDEQ)
        logger.info(' template payload : ')
        logger.debug(postData)
        
        uri = globalVars.serviceUriInfo["Template"]
        logger.info(uri)
        resRes,state=self.getResponse("GET", "Template")
        print resRes
        if existTemplateName and state:
            
            for res in resRes:
                template_name=res["templateName"].strip()
                if template_name==existTemplateName:
                    print "template is allready exit"
                    refId=res["id"]
                    resResContent = self.getPublishedTemplateData(refId)
                    print "..........."
                    if resResContent:
                        url = self.buildUrl("Template",refId=refId)
                        headers=self.generateSecurityHeader(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
                        response = requests.put(url, data=postData, headers=headers, verify=False)
                        logger.info("printing the header of the create template")
                        logger.info(headers)
                        startTime = datetime.datetime.now()
                        endTime = datetime.datetime.now()
                        elapsedTime="%s"%(endTime-startTime)
#             self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
                        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.content, startTime, endTime, elapsedTime)
        #response = requests.post(url, data=postData, headers=headers)
                        time.sleep(globalVars.defaultWaitTime)
#         time.sleep(globalVars.defaultWaitTime)
                        return response
                    
        
        headers=self.generateSecurityHeader(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        logger.info("printing the header of the create template")
        logger.info(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
#         self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.content, startTime, endTime, elapsedTime)
        #response = requests.post(url, data=postData, headers=headers)
        time.sleep(globalVars.defaultWaitTime)
#         time.sleep(globalVars.defaultWaitTime)
        return response
    
    def TemplateEsxi(self, payload,refIdVC = None,refIDEQ = None,existTemplateName=""):
        """
        Creates ServiceTemplate
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Template")
        logger.info(url)
        
        postData = self.getTemplatePayloadEsxi(payload,refIdVC,refIDEQ)
        logger.info(' template payload : ')
        logger.debug(postData)
        
        uri = globalVars.serviceUriInfo["Template"]
        logger.info(uri)
        resRes,state=self.getResponse("GET", "Template")
        print resRes
        if existTemplateName and state:
            print "template is allready exit"
            for res in resRes:
                template_name=res["templateName"].strip()
                if template_name==existTemplateName:
                    refId=res["id"]
#                     resTempsRes,statTemp=self.getResponse("GET", "Template",refId=refId)
                    print "..........."
                    if True:
                        url = self.buildUrl("Template",refId=refId)
                        headers=self.generateSecurityHeader(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
                        response = requests.put(url, data=postData, headers=headers)
                        logger.info("printing the header of the create template")
                        logger.info(headers)
                        startTime = datetime.datetime.now()
                        endTime = datetime.datetime.now()
                        elapsedTime="%s"%(endTime-startTime)
#             self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
                        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.content, startTime, endTime, elapsedTime)
        #response = requests.post(url, data=postData, headers=headers)
                        time.sleep(globalVars.defaultWaitTime)
#         time.sleep(globalVars.defaultWaitTime)
                        return response
                    
        
        headers=self.generateSecurityHeader(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        logger.info("printing the header of the create template")
        logger.info(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
#         self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.content, startTime, endTime, elapsedTime)
        #response = requests.post(url, data=postData, headers=headers)
        time.sleep(globalVars.defaultWaitTime)
#         time.sleep(globalVars.defaultWaitTime)
        return response
    
    
    def getTemplatePayloadEsxi(self, payload,refIdVC = None,refIDEQ = None,specificstandarduser=None):
        
        poolId=''
        if globalVars.testCaseFlowName =='exsi':
            poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameExsi)
            if not foundGroup:
                poolId = templateInputReqValueHyperV.GlobalPool
        elif globalVars.testCaseFlowName == "fcoe_flex_fc_mxl":
                poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameMxl)
                if not foundGroup:
                    poolId = templateInputReqValueHyperV.GlobalPool
        elif globalVars.testCaseFlowName == "fcoe_flexioa":
                poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameFlexIoa)
                if not foundGroup:
                    poolId = templateInputReqValueHyperV.GlobalPool
        elif globalVars.testCaseFlowName == "fcoe_nobrocade":
                poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameNoBrocade)
                if not foundGroup:
                    poolId = templateInputReqValueHyperV.GlobalPool
        elif globalVars.testCaseFlowName == "fcoe_withbrocade":
                poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameWithBrocade)
                if not foundGroup:
                    poolId = templateInputReqValueHyperV.GlobalPool
        elif globalVars.testCaseFlowName == "fx2":
                poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameFX2)
                if not foundGroup:
                    poolId = templateInputReqValueHyperV.GlobalPool
        elif globalVars.testCaseFlowName == "minimal":
                poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameMinimal)
                if not foundGroup:
                    poolId = templateInputReqValueHyperV.GlobalPool
        elif globalVars.testCaseFlowName == "multiservice":
                poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameMinimal)
                if not foundGroup:
                    poolId = templateInputReqValueHyperV.GlobalPool
        elif globalVars.testCaseFlowName == "raid":
                poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolNameRaid)
                if not foundGroup:
                    poolId = templateInputReqValueHyperV.GlobalPool
        else:
            poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolName)
            if not foundGroup:
                poolId = templateInputReqValueHyperV.GlobalPool    
            
        
#         poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolName)
#         if not foundGroup:
#             poolId = templateInputReqValueHyperV.GlobalPool
                        
        self.log_data( "  pool ID value : ")
        self.log_data( poolId)
        self.log_data( " RefId of VCenter : %s"%str(refIdVC))
        self.log_data( " RefId of Storage : %s"%str(refIDEQ))
        VM5_Host= self.getVMHostName()
        globalVars.VMs.append(VM5_Host)
        Volume_1= self.getVolumeName()
        Volume_2= self.getVolumeName()
        Volume_3= self.getVolumeName()
        Volume_4= self.getVolumeName()
        NewClusterName= self.getClusterName()
        globalVars.clusterName=NewClusterName
        newdatacenter =self.getDataCenterName()
        globalVars.datacenterName = newdatacenter
        VM1_Host= self.getVMHostName()
        globalVars.VMs.append(VM1_Host)
        Host1= self.getHostName()
        VM2_Host= self.getVMHostName()
        globalVars.VMs.append(VM2_Host)
        Host2= self.getHostName()
        VM3_Host= self.getVMHostName()
        globalVars.VMs.append(VM3_Host)
        Host3 =self.getHostName()
        VM4_Host= self.getVMHostName()
        globalVars.VMs.append(VM4_Host)
        userId = ""
        if specificstandarduser :
            userId=self.getUserSeqId(specificstandardname=specificstandarduser)
        
        
        payload = payload.replace("$target_boot_device_value", inputReqValueESXI.target_boot_device_value).replace("$OSImage_value", inputReqValueESXI.OSImage_value).replace("$os_image_version", inputReqValueESXI.os_image_version).replace("$AdminConfirmPassword", inputReqValueESXI.AdminConfirmPassword).replace("$Adminpassword", inputReqValueESXI.Adminpassword)
        payload = payload.replace("$product_key", inputReqValueESXI.product_key).replace("$migration",inputReqValueESXI.migration).replace("$Iqnip",inputReqValueESXI.Iqnip)
        payload = payload.replace("$Workload", templateInputReqValueHyperV.workloadID).replace("$PXE", templateInputReqValueHyperV.pXEID).replace("$HypervisorManagement", templateInputReqValueHyperV.hypervisorManagementID).replace("$VMotion", templateInputReqValueHyperV.vMotionID).replace("$ClusterPrivate", templateInputReqValueHyperV.clusterPrivateID).replace("$ISCSI", templateInputReqValueHyperV.iSCSIID).replace("$FCoE1",\
                    templateInputReqValueHyperV.FCoE1ID).replace("$FC_oE_2",templateInputReqValueHyperV.FC_oE2_ID).replace("$FIP",templateInputReqValueHyperV.FIPID).replace("$ISC_1",templateInputReqValueHyperV.ISC_1_ID).replace("$Fileshare",templateInputReqValueHyperV.FileshareID)
        payload = payload.replace("$Volume_1", Volume_1).replace("$Volume_2", Volume_2).replace("$Volume_3", Volume_3).replace("$NewClusterName", NewClusterName).replace("$Volume_4", Volume_4).replace("$iscsiinitiator",inputReqValueESXI.iscsiinitiator)
        payload = payload.replace("$GlobalPool", poolId).replace("$ESXIiamge", inputReqValueESXI.ESXIiamge).replace("$serverpool", inputReqValueESXI.serverpool).replace("$server1pool", inputReqValueESXI.server1pool).replace("$server2pool", inputReqValueESXI.server2pool)
        payload = payload.replace("$esxiimagetype",inputReqValueESXI.esxiimagetype).replace("$ExistingCluster",inputReqValueESXI.ExistingCluster)
        payload = payload.replace("$newdatacenter",newdatacenter).replace("$cpucountvalue",inputReqValueESXI.cpucountvalue).replace("$disksizevalue",inputReqValueESXI.disksizevalue).replace("$memoryvalue",inputReqValueESXI.memoryvalue).replace("$templateIdValue",inputReqValueESXI.templateIdValue)
        payload = payload.replace("$Host1",Host1).replace("$Host2",Host2).replace("$Host3",Host3).replace("$VM1_Host",VM1_Host).replace("$VM2_Host",VM2_Host).replace("$VM3_Host",VM3_Host).replace("$VM4_Host",VM4_Host).replace("$VM5_Host",VM5_Host)
        payload = payload.replace("$cpu_count",inputReqValueESXI.cpu_count).replace("$image_value",inputReqValueESXI.image_value).replace("$os_image_type",inputReqValueESXI.os_image_type).replace("$disk_size",inputReqValueESXI.disk_size).replace("$mem_val",inputReqValueESXI.mem_val).replace("$esxiimagetype",inputReqValueESXI.esxiimagetype)
        payload = payload.replace("$cpuvalue",inputReqValueESXI.cpuvalue).replace("$diskvalue",inputReqValueESXI.diskvalue).replace("$memvalue",inputReqValueESXI.memvalue).replace("$clonetype",inputReqValueESXI.clonetype).replace("$vmname",inputReqValueESXI.vmname).replace("$sourcedatacenter",inputReqValueESXI.sourcedatacenter).replace("$DataCenter",inputReqValueESXI.DataCenter).replace("$ClusterName",inputReqValueESXI.ClusterName)
        payload = payload.replace("$prod_key",inputReqValueESXI.prod_key).replace("$os_version",inputReqValueESXI.os_version).replace("$image_value",inputReqValueESXI.image_value)
        payload = payload.replace("$managementIp",inputReqValueESXI.managementIp).replace("$staticIP",inputReqValueESXI.staticIP).replace("$serversource",inputReqValueESXI.serversource).replace("$ServerEntry",inputReqValueESXI.ServerEntry).replace("$Vol1_Iqnip",inputReqValueESXI.Vol1_Iqnip).replace("$Vol2_Iqnip",inputReqValueESXI.Vol2_Iqnip).replace("$ntpserver",inputReqValueESXI.ntpserver)
        payload = payload.replace("$VolumeSize_1",inputReqValueESXI.VolumeSize_1).replace("$VolumeSize_2",inputReqValueESXI.VolumeSize_2).replace("$VolumeSize_3",inputReqValueESXI.VolumeSize_3)
        payload = payload.replace("$AutogenerateHostName",inputReqValueESXI.AutogenerateHostName).replace("$VMAutogenerate",inputReqValueESXI.VMAutogenerate)
        payload = payload.replace("$MediaLocation",inputReqValueESXI.MediaLocation).replace("$sapwd",inputReqValueESXI.sapwd).replace("$agtsvcpwd",inputReqValueESXI.agtsvcpwd).replace("$assvcpwd",inputReqValueESXI.assvcpwd).replace("$rssvcpwd",inputReqValueESXI.rssvcpwd).replace("$sqlsvcpwd",inputReqValueESXI.sqlsvcpwd).replace("$netdirectory",inputReqValueESXI.netdirectory)
        payload = payload.replace("$userSeqId",str(userId))
#         print payload
        if refIdVC:
            payload = payload.replace("$VCenterRefId",refIdVC)
        if refIDEQ:
            payload = payload.replace("$StorageRefId",str(refIDEQ))
        
        return payload
    
    def template_teardown(self, deploymentRefId):
        """
        Teardown to the existing Deployment
        """
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "DELETE", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data(headers)
        response = requests.delete(url, headers=headers, verify=False)
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response
    
    def scaleUpdeployEsxiTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc, fileName):
        """
        Scale up Server to the existing Deployment
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy", deploymentRefId)
        logger.info("printing deploy scaleup  url")
        logger.info(url)
        postData = self.getscaleUpEsxiPayload(templateID, deploymentRefId,  DeplName, deplyDesc, fileName)
        logger.info(' deploy scaleup payload : ')
        logger.debug(postData)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data(headers)
        startTime = datetime.datetime.now()
        response = requests.put(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response
    
    
    def getscaleUpEsxiPayload(self,templateID, deploymentRefId,  DeplName, deplyDesc, fileName):
    
        logger = self.getLoggerInstance()
        payload = self.readFile(fileName)
        refIdVCenter = globalVars.refIdVCenter
        refIdEQ =  globalVars.refIdEQLogic
        
        resDT, statDT = self.getSuccessfullyDeploymentxml(deploymentRefId)
        if not statDT:
            return "Unable to retrieve Deployed Template: %s"%resDT, statDT       
                  
        temp = str(resDT).rpartition("</components>")
        deploymentTemplate = temp[0] + temp[1] + payload + temp[2]    
       
        deploymentTemplate = deploymentTemplate.replace("<retry>false</retry>", "<retry>true</retry>")
        
        payload =self.getTemplatePayloadEsxi(deploymentTemplate,refIdVCenter, refIdEQ)

        
        logger.info("printing  payload of scaleup deployment")
        #logger.debug(payload)
        
        return payload
    
    
    def getSuccessfullyDeploymentxml(self, refId):
        """
        Gets the Deployment Status
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy", refId)
        logger.info("printing deploy url")
        logger.info(url)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + refId
        headers=self.generateHeaderDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data(headers)
        response = requests.get(url, headers=headers, verify=False)
        result = response.text
        logger.info("printing GET result of the deploy url :%s"%str(url))
        logger.info(result)
        self.log_data("printing GET result of the deploy url :%s"%str(url))
        self.log_data(result)
        return result, True

    def scaleUpServerAndDeployEsxiTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc, payload):
        """
        Scale up  to the existing Deployment
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy", deploymentRefId)
        logger.info("printing deploy scaleup  url")
        logger.info(url)
        postData = self.getScaleUpServerEsxiPayload(templateID, deploymentRefId, DeplName, deplyDesc, payload)
        logger.info(' deploy scaleup payload : ')
        logger.debug(postData)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data(headers)
        startTime = datetime.datetime.now()
        response = requests.put(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response


    def getScaleUpServerEsxiPayload(self,templateID, deploymentRefId,  DeplName, deplyDesc, payload):
        
        logger = self.getLoggerInstance()
        refIdVCenter = globalVars.refIdVCenter
        refIdEQ =  globalVars.refIdEQLogic
        
        resDT, statDT = self.getSuccessfullyDeploymentxml(deploymentRefId)
        if not statDT:
            return "Unable to retrieve Deployed Template: %s"%resDT, statDT       
                  
        temp = str(resDT).rpartition("</components>")
        deploymentTemplate = temp[0] + temp[1] + payload + temp[2]    
       
        deploymentTemplate = deploymentTemplate.replace("<retry>false</retry>", "<retry>true</retry>")
        
        payload2 =self.getTemplatePayloadEsxi(deploymentTemplate,refIdVCenter, refIdEQ)        
        logger.info("printing  payload of scaleup deployment")
        #logger.debug(payload)
        return payload2

    def checkForDeploymentStatus(self, deploymentRefId, tc_Id, deplyDesc ):
        loop = 60
        deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
        deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            
        while loop:
            resDS, statDS = self.getDeploymentStatus(deploymentRefId)
            if resDS.lower() in ("in_progress", "pending"):
                time.sleep(120)
            else:
                if resDS.lower() in ("complete"):
                    self.log_TestData(["", "", "",str(tc_Id), deplyDesc, 'Success','Template Deployed Successfully','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath])
                    self.log_data( 'Successfully Deployed Service for the Deployment refID : %s'%deploymentRefId)
                    break
                else:
                    print "Deployment Status: %s"%resDS
                    self.log_TestData(["", "", "",str(tc_Id), deplyDesc, 'Failed','Deployment Service Failed','Server : Blade Server', "deploymentLogPath: %s"%deploymentLogPath  ])
                    self.log_data('Deployment Service Failed for the Deployment refID : %s'%deploymentRefId)
                    break
        loop -= 1
        
    
    def checkAndDeleteDeployment(self, deploymentRefId, tc_Id):
        loop = 60
        deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
        deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            
        while loop:
            resDS, statDS = self.getDeploymentStatus(deploymentRefId)
            if resDS.lower() in ("in_progress", "pending"):
                time.sleep(120)
            else:
                if resDS.lower() in ("complete"):
                    
                    self.log_data( 'Now going to call the teardown of service ')
                    self.cleanDeployedService(deploymentRefId)
                    self.test_cleanDeployedTemplates(deploymentRefId)
                    break
                else:
                    
                    if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            self.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.test_cleanDeployedTemplates(deploymentRefId)
                            
                    else:
                            self.log_data('did not call the teardown of service for the Deployment refID : %s'%deploymentRefId)
                         
                         
                    break

        loop -= 1


    def createTemplateWorkFlows(self, payload,refIdVC = None,refIDEQ = None, refIDSCVMM = None):
        """
        Creates ServiceTemplate
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Template")
        logger.info(url)
        
        postData = self.getTemplatePayloadWorkFlows(payload,refIdVC,refIDEQ, refIDSCVMM)
        logger.info(' template payload : ')
        logger.debug(postData)
        
        uri = globalVars.serviceUriInfo["Template"]
        logger.info(uri)
        headers=self.generateSecurityHeader(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        logger.info("printing the header of the create template")
        logger.info(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        #response = requests.post(url, data=postData, headers=headers)
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response

    
    def getTemplatePayloadWorkFlows(self, payload,refIdVC = None,refIDEQ = None, refIDSCVMM = None):
        
        poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolName)
        if not foundGroup:
            poolId = templateInputReqValueHyperV.GlobalPool
                        
        self.log_data( "  pool ID value : ")
        self.log_data( poolId)
        self.log_data( " RefId of VCenter : %s"%str(refIdVC))
        self.log_data( " RefId of Storage : %s"%str(refIDEQ))
        VM5_Host= self.getVMHostName()
        Volume_1= self.getVolumeName()
        Volume_2= self.getVolumeName()
        Volume_3= self.getVolumeName()
        Volume_4= self.getVolumeName()
        NewClusterName= self.getClusterName()
        newdatacenter =self.getDataCenterName()
        VM1_Host= self.getVMHostName()
        Host1= self.getHostName()
        VM2_Host= self.getVMHostName()
        Host2= self.getHostName()
        VM3_Host= self.getVMHostName()
        Host3 =self.getHostName()
        VM4_Host= self.getVMHostName()
        NewHostGroup= self.getHostGroupName()
        
        payload = payload.replace("$target_boot_device_value", inputForWorkFlowCases.target_boot_device_value).replace("$target_boot_device_iSCSI", inputForWorkFlowCases.target_boot_device_iSCSI).replace("$OSImage_value", inputForWorkFlowCases.OSImage_value).replace("$os_image_version", inputForWorkFlowCases.os_image_version).replace("$AdminConfirmPassword", inputForWorkFlowCases.AdminConfirmPassword).replace("$Adminpassword", inputForWorkFlowCases.Adminpassword)
        payload = payload.replace("$product_key", inputForWorkFlowCases.product_key).replace("$migration",inputForWorkFlowCases.migration).replace("$Iqnip",inputForWorkFlowCases.Iqnip)
        payload = payload.replace("$Workload", templateInputReqValueHyperV.workloadID).replace("$PXE", templateInputReqValueHyperV.pXEID).replace("$HypervisorManagement", templateInputReqValueHyperV.hypervisorManagementID).replace("$VMotion", templateInputReqValueHyperV.vMotionID).replace("$ClusterPrivate", templateInputReqValueHyperV.clusterPrivateID).replace("$ISCSI", templateInputReqValueHyperV.iSCSIID).replace("$FCoE1",\
                    templateInputReqValueHyperV.FCoE1ID).replace("$FC_oE_2",templateInputReqValueHyperV.FC_oE2_ID).replace("$FIP",templateInputReqValueHyperV.FIPID)
        payload = payload.replace("$Volume_1", Volume_1).replace("$Volume_2", Volume_2).replace("$Volume_3", Volume_3).replace("$NewClusterName", NewClusterName).replace("$Volume_4", Volume_4)
        payload = payload.replace("$GlobalPool", poolId).replace("$ESXIiamge_5_1", inputForWorkFlowCases.ESXIiamge_5_1).replace("$ESXIiamge_5_5", inputForWorkFlowCases.ESXIiamge_5_5).replace("$serverpool", inputForWorkFlowCases.serverpool).replace("$server1pool", inputForWorkFlowCases.server1pool).replace("$server2pool", inputForWorkFlowCases.server2pool)
        payload = payload.replace("$esxiimagetype",inputForWorkFlowCases.esxiimagetype).replace("$ExistingCluster",inputForWorkFlowCases.ExistingCluster)
        payload = payload.replace("$newdatacenter",newdatacenter).replace("$cpucountvalue",inputForWorkFlowCases.cpucountvalue).replace("$disksizevalue",inputForWorkFlowCases.disksizevalue).replace("$memoryvalue",inputForWorkFlowCases.memoryvalue).replace("$templateIdValue",inputForWorkFlowCases.templateIdValue)
        payload = payload.replace("$Host1",Host1).replace("$Host2",Host2).replace("$Host3",Host3).replace("$VM1_Host",VM1_Host).replace("$VM2_Host",VM2_Host).replace("$VM3_Host",VM3_Host).replace("$VM4_Host",VM4_Host).replace("$VM5_Host",VM5_Host)
        payload = payload.replace("$cpu_count",inputForWorkFlowCases.cpu_count).replace("$image_value",inputForWorkFlowCases.image_value).replace("$os_image_type",inputForWorkFlowCases.os_image_type).replace("$disk_size",inputForWorkFlowCases.disk_size).replace("$mem_val",inputForWorkFlowCases.mem_val).replace("$esxiimagetype",inputForWorkFlowCases.esxiimagetype)
        payload = payload.replace("$cpuvalue",inputForWorkFlowCases.cpuvalue).replace("$diskvalue",inputForWorkFlowCases.diskvalue).replace("$memvalue",inputForWorkFlowCases.memvalue).replace("$clonetype",inputForWorkFlowCases.clonetype).replace("$vmname",inputForWorkFlowCases.vmname).replace("$sourcedatacenter",inputForWorkFlowCases.sourcedatacenter).replace("$DataCenter",inputForWorkFlowCases.DataCenter).replace("$ClusterName",inputForWorkFlowCases.ClusterName)
        payload = payload.replace("$prod_key",inputForWorkFlowCases.prod_key).replace("$os_version",inputForWorkFlowCases.os_version).replace("$image_value",inputForWorkFlowCases.image_value)
        payload = payload.replace("$managementIp",inputForWorkFlowCases.managementIp).replace("$staticIP",inputForWorkFlowCases.staticIP).replace("$serversource",inputForWorkFlowCases.serversource).replace("$ServerEntry",inputForWorkFlowCases.ServerEntry).replace("$Vol1_Iqnip",inputForWorkFlowCases.Vol1_Iqnip).replace("$Vol2_Iqnip",inputForWorkFlowCases.Vol2_Iqnip)
        payload = payload.replace("$ExistingVol_1",inputForWorkFlowCases.ExistingVol_1).replace("$ExistingVol_2",inputForWorkFlowCases.ExistingVol_2)
        
        
        payload = payload.replace("$ntp", inputForWorkFlowCases.ntp).replace("$domain_name", inputForWorkFlowCases.domain_name).replace("$fqdn", inputForWorkFlowCases.fqdn).replace("$DomainAdminPsswd", inputForWorkFlowCases.DomainAdminPsswd).replace("$Domainconfirm", inputForWorkFlowCases.Domainconfirm)
        payload = payload.replace("$Volume_1", Volume_1).replace("$Volume_2", Volume_2).replace("$HyperMgmtClusterValue", inputForWorkFlowCases.HyperMgmtClusterValue).replace("$NewClusterName", NewClusterName).replace("$ClusterIpaddress", inputForWorkFlowCases.ClusterIpaddress)
        payload = payload.replace("$GlobalPool", poolId).replace("$domainadminuser", inputForWorkFlowCases.domainadminuser).replace("$serverpool", inputForWorkFlowCases.serverpool).replace("$server1pool", inputForWorkFlowCases.server1pool).replace("$server2pool", inputForWorkFlowCases.server2pool)
        payload = payload.replace("$OS_Image_valueR2",inputForWorkFlowCases.OS_Image_valueR2).replace("$os_image_vR2",inputForWorkFlowCases.os_image_vR2).replace("$prod_key_R2",inputForWorkFlowCases.prod_key_R2)
        payload = payload.replace("$hostname",inputForWorkFlowCases.hostname).replace("$description",inputForWorkFlowCases.description).replace("$vmtemplatename",inputForWorkFlowCases.vmtemplatename).replace("$newhost",inputForWorkFlowCases.newhost).replace("$path",inputForWorkFlowCases.path)
        payload = payload.replace("$blockdynamicoptimization",inputForWorkFlowCases.blockdynamicoptimization).replace("$highlyavailable",inputForWorkFlowCases.highlyavailable).replace("$cpucount",inputForWorkFlowCases.cpucount).replace("$memorymb",inputForWorkFlowCases.memorymb).replace("$startaction",inputForWorkFlowCases.startaction).replace("$stopaction",inputForWorkFlowCases.stopaction)
        payload = payload.replace("$centosvmtemplatename",inputForWorkFlowCases.centosvmtemplatename)
        payload = payload.replace("$Host_1",Host1).replace("$Host_2",Host2).replace("$NewHostGroup",NewHostGroup).replace("$ExistingHost",inputForWorkFlowCases.ExistingHost).replace("$R2HyperVMgmtValue",inputForWorkFlowCases.R2HyperVMgmtValue)
        
        if refIdVC:
            payload = payload.replace("$VCenterRefId",refIdVC)
        if refIDEQ:
            payload = payload.replace("$StorageRefId",str(refIDEQ))
        if refIDSCVMM:
            payload = payload.replace("$HyperMgmtClusterValue", refIDSCVMM)
        
        
        return payload   
    
    def scaleUpdeployWorkFlowsTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc, payload):
        """
        Scale up Server to the existing Deployment
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy", deploymentRefId)
        logger.info("printing deploy scaleup  url")
        logger.info(url)
        postData = self.getscaleUpWorkFlowsPayload(templateID, deploymentRefId,  DeplName, deplyDesc, payload)
        logger.info(' deploy scaleup payload : ')
        logger.debug(postData)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data(headers)
        startTime = datetime.datetime.now()
        response = requests.put(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response
    
    
    def getscaleUpWorkFlowsPayload(self,templateID, deploymentRefId,  DeplName, deplyDesc, payload):
    
        logger = self.getLoggerInstance()
        refIdVCenter = globalVars.refIdVCenter
        refIdEQ =  globalVars.refIdEQLogic
        
        resDT, statDT = self.getSuccessfullyDeploymentxml(deploymentRefId)
        if not statDT:
            return "Unable to retrieve Deployed Template: %s"%resDT, statDT       
                  
        temp = str(resDT).rpartition("</components>")
        deploymentTemplate = temp[0] + temp[1] + payload + temp[2]    
       
        deploymentTemplate = deploymentTemplate.replace("<retry>false</retry>", "<retry>true</retry>")
        
        payload2 =self.getTemplatePayloadWorkFlows(deploymentTemplate,refIdVCenter, refIdEQ)

        
        logger.info("printing  payload of scaleup deployment")
        #logger.debug(payload)
        
        return payload2
    
    def doVCenterValidations(self,refIdVC):
        print " in doVCenterValidatons "
        count = 0
        logger = self.getLoggerInstance()
        url = self.buildUrl("VCenter", refIdVC)
        logger.info("printing  url")
        logger.info(url)
        uri = globalVars.serviceUriInfo["User"]+ "/" + refIdVC
        headers=self.generateHeaderDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data(headers)
        startTime = datetime.datetime.now()
        response = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \n"%(url,'GET',headers), response.status_code, response.text, startTime, endTime, elapsedTime)
        result = response.text
        #xmlString = ET.fromstring(result)
        datacenter = globalVars.datacenterName
        print " datacenter name in Template : %s"%str(datacenter)
        self.log_data( 'datacenter name in Template : %s'%str(datacenter))
        cluster = globalVars.clusterName
        print " cluster name in Template : %s"%str(cluster)
        self.log_data( 'cluster name in Template : %s'%str(cluster))
        if result.find(datacenter):
            print " Datacenter created : %s"%str(datacenter)
            self.log_data( 'Datacenter created : %s'%str(datacenter))
        else:
            print " Datacenter :%s not found"%str(datacenter)
            self.log_data( 'Datacenter  not found : %s'%str(datacenter))
        if result.find(cluster):
            print " Cluster created : %s"%str(cluster)
            self.log_data( 'Cluster created : %s'%str(cluster))
        else:
            print " Cluster :%s not found"%str(cluster)
            self.log_data( 'Cluster not found : %s'%str(cluster))
        print " VM list in globalVars : "
        print globalVars.VMs
        for vm in globalVars.VMs:
            if result.find(vm):
                print " VM Created : %s"%str(vm)
                self.log_data( 'VM Created : %s'%str(cluster))
                count=count + 1
        if count == 0:
            print " NO VM's created "
                
    def doScvmmValidation(self,scvmmId):
        print " in doScvmmValidation : "
        resMD,statMD = self.getResponse("GET", "ManagedDevice")
        for devices in resMD:
            if devices['deviceType']=='scvmm' and devices['refId']==scvmmId:
                result = devices
        facts = result['facts']
        print " SCVMM facts : %s"%str(facts)
        datacenter = globalVars.dcNamescvmm
        print " datacenter name in Template : %s"%str(datacenter)
        self.log_data( 'datacenter name in Template : %s'%str(datacenter))
        cluster = globalVars.clusterNamescvmm
        print " cluster name in Template : %s"%str(cluster)
        self.log_data( 'cluster name in Template : %s'%str(cluster))
        if  facts.find(datacenter):
            print " Datacenter created : %s"%str(datacenter)
            self.log_data( 'Datacenter created : %s'%str(datacenter))
        else:
            print " Datacenter :%s not found"%str(datacenter)
            self.log_data( 'Datacenter  not found : %s'%str(datacenter))
        if facts.find(cluster):
            print " Cluster created : %s"%str(cluster)
            self.log_data( 'Cluster created : %s'%str(cluster))
        else:
            print " Cluster :%s not found"%str(cluster)
            self.log_data( 'Cluster not found : %s'%str(cluster))


    def scaleUpServerAndDeployHyperVTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc, payload):
        """
        Scale up  to the existing Deployment
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy", deploymentRefId)
        logger.info("printing deploy scaleup  url")
        logger.info(url)
        postData = self.getScaleUpServerHyperVPayload(templateID, deploymentRefId, DeplName, deplyDesc, payload)
        logger.info(' deploy scaleup payload : ')
        logger.debug(postData)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( "Printing headers information")
        self.log_data(headers)
        startTime = datetime.datetime.now()
        response = requests.put(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response


    def getScaleUpServerHyperVPayload(self,templateID, deploymentRefId,  DeplName, deplyDesc, payload):
        
        logger = self.getLoggerInstance()
        refIdSCVMM = globalVars.refIdSCVMM
        refIdEQ =  globalVars.refIdEQLogic
        
        
        
        resDT, statDT = self.getSuccessfullyDeploymentxml(deploymentRefId)
        if not statDT:
            return "Unable to retrieve Deployed Template: %s"%resDT, statDT       
                  
        temp = str(resDT).rpartition("</components>")
        deploymentTemplate = temp[0] + temp[1] + payload + temp[2]    
       
        deploymentTemplate = deploymentTemplate.replace("<retry>false</retry>", "<retry>true</retry>")
        
        payload2 =self.getTemplatePayload(deploymentTemplate,refIdSCVMM, refIdEQ)        
        logger.info("printing  payload of scaleup deployment")
        #logger.debug(payload)
        return payload2
    
    def setEeachNetworkData(self):
        
        print "dheeraj"
        print globalVars.workloadID
        
        if globalVars.workloadID  != "":
            resultWorkLoad, statWorkLoad = self.getNetworkByRefID(globalVars.workloadID)
            print "resultWorkLoad"
            print resultWorkLoad
            print "statWorkLoad"
            print statWorkLoad
            if statWorkLoad:
                globalVars.workloadIDNetwork=resultWorkLoad
                
                
        if globalVars.pXEID  != "":
            resultPXE, statPXE = self.getNetworkByRefID(globalVars.pXEID)
            if statPXE:
                globalVars.pXEIDNetwork=resultPXE
                
        if globalVars.vMotionID  != "":
            resultVMotion, statVMotion = self.getNetworkByRefID(globalVars.vMotionID)
            if statVMotion:
                globalVars.vMotionIDNetwork=resultVMotion
                
        if globalVars.hypervisorManagementID  != "":
            resultHyperMgmt, statHyperMgmt = self.getNetworkByRefID(globalVars.hypervisorManagementID)
            if statHyperMgmt:
                globalVars.hypervisorManagementIDNetwork=resultHyperMgmt
                
        if globalVars.clusterPrivateID  != "":
            resultCluPvt, statCluPvt = self.getNetworkByRefID(globalVars.clusterPrivateID)
            if statCluPvt:
                globalVars.clusterPrivateIDNetwork=resultCluPvt
                
                
        if globalVars.iSCSIID  != "":
            resultiSCSI, statiSCSI = self.getNetworkByRefID(globalVars.iSCSIID)
            if statiSCSI:
                globalVars.iSCSIIDNetwork=resultiSCSI
        
        if globalVars.FCoE1ID  != "":
            resultFCoE, statFCoE = self.getNetworkByRefID(globalVars.FCoE1ID)
            if statFCoE:
                globalVars.FCoE1IDNetwork=resultFCoE
                
         
                
                            
    def getNetworkByRefID(self, refId):
        """
        Gets the Networks by refid
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Network", refId)
        logger.info("printing Network url")
        logger.info(url)
        uri = globalVars.serviceUriInfo["Network"]+ "/" + refId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data("Printing headers information")
        self.log_data(headers)
        self.statDS =False
        self.resultDS=''
        #response = requests.get(url, headers=headers)
        startTime = datetime.datetime.now()
        networkResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), networkResponse.status_code, networkResponse.content, startTime, endTime, elapsedTime)
        
        if networkResponse.status_code in (200, 201, 202, 203, 204):
            
            self.resultDS = networkResponse.content
            self.resultDS = self.resultDS.replace("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""",'').replace("""<Network xmlns="http://pg.dell.com/asm/virtualservices/network">""",'').replace("</Network>", '')
            self.statDS =True
            
        else:
            self.statDS =False
            self.resultDS = 'Not Found'
            
       
        
        return self.resultDS, self.statDS 
    
    def createTemplateForSpecificUser(self, payload,refIdVC = None,refIDEQ = None,specificstandarduser=None):
        """
        Creates ServiceTemplate
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Template")
        logger.info(url)
        
        postData = self.getTemplatePayloadEsxi(payload,refIdVC,refIDEQ,specificstandarduser=specificstandarduser)
        logger.info(' template payload : ')
        logger.debug(postData)
        print postData
        uri = globalVars.serviceUriInfo["Template"]
        logger.info(uri)
        headers=self.generateSecurityHeader(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        logger.info("printing the header of the create template")
        logger.info(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)

        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, response.content, startTime, endTime, elapsedTime)

        time.sleep(globalVars.defaultWaitTime)

        return response,postData
    
    def getUserSeqId(self,specificstandardname=""):
        """
        Creates ServiceTemplate
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Template")
        logger.info(url)
        
        resInfo,Status = self.getResponse("GET","User")
        logger.info('User Response')
        logger.debug(resInfo)
        if Status:
            for res in resInfo:
                if res['userName']== specificstandardname:
                    userSeqId=res['userSeqId']
                    return userSeqId
    