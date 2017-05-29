'''
Created on Aug 24, 2014

@author: dheeraj_si
'''

from copy import deepcopy
from xml.etree import ElementTree as et
import requests
import time
import json
import datetime
import globalVars
from utilityModule import UtilBase
from cookielib import logger
import xml.etree.ElementTree as ET
import chassisConfigParam


class DiscoverResourceTestBase(UtilBase):
    '''
    classdocs
    '''
    poolName = "serverPool"

    def __init__(self):
        '''
        Constructor
        '''
        UtilBase.__init__(self)
        
        
    def setupCredentials(self):
        """
        Create Credentials or Manage existing Credentials        
        """
        logger = self.getLoggerInstance()
        credentialConfig, status = self.loadCredentialInputs()
        if not status:
            return credentialConfig, False
        resCRE, statCRE = self.getResponse("GET", "Credential")
        creResponse = []
        for credential in credentialConfig:
            found = False
            if not statCRE and "No information found" in resCRE:
                found = False
            else:
                found = [creList["credential"]["id"] for creList in resCRE["credentialList"] if creList["credential"]["label"] == credential["Name"]]
            if not found:
                action = "POST"
                creId = ""
            else:
                action = "PUT"
                creId = found[0]
            resDC, statDC = self.defineCredential(credential, creId, action)
            creResponse.append(str(resDC))          
            if not statDC:
                return creResponse, False
            if not found: creId = resDC["credential"]["id"]
            self.credentialMap[credential["Name"]] = creId
        logger.info(' credentialMap : ') 
        logger.debug(self.credentialMap)
        return creResponse, True
    
    
    def loadCredentialInputs(self):
        """
        Loads the Credential Information provided in Credential.csv
        """ 
      
        configFile = globalVars.credentialConfig 
       
        try:
            result, status = self.readCsvFile(configFile)
            print result
            if not status:
                return "Unable to read Configuration File: %s"%configFile , False
            header = result[0]      
            return [dict(zip(header,result[row])) for row in xrange(1,len(result))], True
        except:
            return "Columns mismatch in the Configuration File: %s"%configFile, False
        
    def defineCredential(self, credentialInfo, creId="", action="POST"):
        """
        Defines new Credential with provided Username and Password
        """ 
        domain = credentialInfo["Domain"] 
        payload = self.readFile(globalVars.credentialPayload)
        payload = payload.replace("cre_type", globalVars.credentialTag[credentialInfo["Type"]]).replace("cre_label",
                                credentialInfo["Name"]).replace("cre_username", credentialInfo["Username"]).replace(
                                "cre_password", credentialInfo["Password"]).replace("cre_protocol",
                                credentialInfo["Protocol"]).replace("cre_snmp", credentialInfo["SNMP"])
        if creId != "":
            payload = payload.replace(r"<cre_id>", "<id>" + creId + "</id>")
        else:
            payload = payload.replace(r"<cre_id>", "")
            
        if  domain != "":
            payload = payload.replace(r"<domain>", "<domain>" + domain + "</domain>")
        else:
            payload = payload.replace(r"<domain>", "")
        payload = payload.replace('\n', '').replace('\t', '')
        return self.getResponse(action, "Credential", payload, refId=creId)
    
    def discoverServer(self, resource,  unmanaged = False):
        """
        Discovers servers within start_ip and end_ip
        """
        resDP, statDP = self.getServerDiscoveryPayload(resource, unmanaged = unmanaged)
        if not statDP:
            return resDP, False
        resDR, statDR = self.getResponse("POST", "Discovery", payload=resDP)      
#         time.sleep(globalVars.defaultWaitTime)
        if statDR:
            refId=resDR["id"]
            #self.manageDeviceDiscovery(refId, resource)
            
        return resDR, statDR

    def getServerDiscoveryPayload(self, resource,unmanaged=False):
        """
        Returns Server Discovery Payload 
        """
        serverCredentialId = ""
        creName = resource["CredentialName"]
        # credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(creName):
            serverCredentialId = self.credentialMap[creName]
        else:
            return "Server Credentials not defined", False
        
        
        payload = self.readFile(globalVars.serverDiscPayload)
        payload = payload.replace("start_ip", resource["START_IP"]).replace("end_ip",
                                resource["END_IP"]).replace("default_server_cred_ref",
                                        serverCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n', '').replace('\t', '')
        return payload, True
    
     
    def verifyDiscoveryStatus(self, discRequestResult):
        """
        Verify Discovery Status
        """
        logger = self.getLoggerInstance()   
        jobId = discRequestResult[0]["id"]   
        time.sleep(globalVars.defaultWaitTime)     
        status =  "in progress"
        count = 0
        while(count < 10 ):
            time.sleep(globalVars.defaultWaitTime)
            time.sleep(globalVars.defaultWaitTime)
            count = count + 1
            resDS, statDS = self.getResponse("GET", "Discovery", refId=jobId)
            logger.info(' resDS ')
            logger.debug(resDS)
            if not statDS:
                logger.info(' could not get response from server ')
                return resDS, False                
            status = resDS["status"]
            logger.info('status after getResponse ')
            logger.debug(status)
            if(status.lower() == "success"):
                break        
        discoveredDevices = []
        if status.lower() == "success":                        
            devices = resDS["devices"]            
            for device in devices:
                tempdict = {}
                for key in device.keys():
                    tempdict[key] = device[key]
                discoveredDevices.append(tempdict)
        self.log_data( 'discoveredDevices') 
        self.log_data(discoveredDevices)           
        return discoveredDevices, True
    
    def manageDevice(self, discoveredDevices):
        """
        Create Device in Inventory, return array of Managed Devices created
        """
        loop = 1
        discoveredDevicesValue = discoveredDevices[0]
        logger = self.getLoggerInstance()
        logger.info(' !!!!!! discoveredDevicesValue !!!!!!! ')
        logger.debug(discoveredDevicesValue)
        logger.info(' length of discover device : ')
        logger.info(len(discoveredDevicesValue))
        
        payload = self.generateManagedDevicePayload(globalVars.manageDevicePayload, self.mdParameters, len(discoveredDevicesValue))
        logger.info(' Printing Payload.........')
        logger.debug(payload)
                
        for device in discoveredDevicesValue:
            payload = payload.replace("device_type" + str(loop), device["deviceType"]).replace("ip_address" + str(loop),
                        device["ipAddress"]).replace("device_model" + str(loop), device["model"]).replace("device_refid" + str(loop),
                        device["deviceRefId"]).replace("device_serviceTag" + str(loop), device["serviceTag"]).replace("device_vendor" + str(loop),
                        device["vendor"])
            loop += 1
        payload = payload.replace('\n', '').replace('\t', '')
        logger.info('Printing updated payload .......')
        logger.debug(payload)                 
        return self.getResponse("POST", "ManagedDevice", payload)
    
    def generateManagedDevicePayload(self, payload, mdParameters, mdCount=1):
        """
        Generates Managed Devices payload
        """
        payload = self.readFile(payload)
        deviceList = et.fromstring(payload)        
        for _ in xrange(1, mdCount):
            device = deepcopy(deviceList.find("ManagedDevice"))
            deviceList.append(device)
        
        logger = self.getLoggerInstance()
        logger.info('Total no of devices in deviceList>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>')
        logger.debug(deviceList)    
        loop = 1
        for device in deviceList.findall("ManagedDevice"):               
            for subDevice in device:
                if subDevice.text is not None and subDevice.text in mdParameters:      
                    subDevice.text = subDevice.text + str(loop)
            loop += 1
        logger.info('device List ')
        logger.debug(et.tostring(deviceList)) 
        return et.tostring(deviceList, encoding='utf8')
    
    
    def manageSwitch(self,discoveredDevices):
        
        payload = self.generateSwitchDevicePayload(discoveredDevices)
        
        return self.getResponse("POST", "ManagedDevice",payload)
    
    def manageStorage(self,discoveredDevices):
        
        payload = self.generateStorageDevicePayload(discoveredDevices)
        
        return self.getResponse("POST", "ManagedDevice",payload)
    
    def manageChassis(self,discoveredDevices):
        
        payload = self.generateChassisDevicePayload(discoveredDevices)
        
        return self.getResponse("POST", "ManagedDevice",payload)
    
    def generateSwitchDevicePayload(self,discoveredDevices):
        switchDict = discoveredDevices[0]
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.manageDevicePayload)
        payload = payload.replace("device_type",switchDict["deviceType"]).replace("ip_address",switchDict["ipAddress"]).replace("device_model",
                    switchDict["model"]).replace("device_refid",switchDict["deviceRefId"]).replace("device_serviceTag",
                    switchDict["serviceTag"]).replace("device_vendor",switchDict["vendor"])
        logger.info('  printing manage device payload for switch : ')
        logger.debug(payload)
        return payload
    
    def generateStorageDevicePayload(self,discoveredDevices):
        storageDict = discoveredDevices[0]
        
        payload = self.readFile(globalVars.manageDevicePayload)
        payload = payload.replace("device_type",storageDict["deviceType"]).replace("ip_address",storageDict["ipAddress"]).replace("device_model",
                    storageDict["model"]).replace("device_refid",storageDict["deviceRefId"]).replace("device_serviceTag",
                    storageDict["serviceTag"]).replace("device_vendor",storageDict["vendor"])
        
        logger = self.getLoggerInstance()
        logger.info('  printing manage device payload for storage : ')
        logger.debug(payload)
        return payload
    
    def generateChassisDevicePayload(self,discoveredDevices):
        chassisDict = discoveredDevices[0]
        
        payload = self.readFile(globalVars.manageDevicePayload)
        payload = payload.replace("device_type",chassisDict["deviceType"]).replace("ip_address",chassisDict["ipAddress"]).replace("device_model",
                    chassisDict["model"]).replace("device_refid",chassisDict["deviceRefId"]).replace("device_serviceTag",
                    chassisDict["serviceTag"]).replace("device_vendor",chassisDict["vendor"])
        
        logger = self.getLoggerInstance()
        logger.info('  printing manage device payload for chassis : ')
        logger.debug(payload)
        return payload
    
    def verifySwitchDiscoveryStatus(self, discRequestResult):
        """
        Verify Discovery Status
        """
        logger = self.getLoggerInstance()
        logger.info(' IN verifySwitchDiscoveryStatus : ')
        jobId = discRequestResult["id"]
        logger.info(' discRequestResult ')
        logger.debug(discRequestResult)
        logger.info(' jobId ')
        logger.debug(jobId)
            
        status =  "in progress"
        count = 0
        while(count < 10 ):
            time.sleep(globalVars.defaultWaitTime)
            count = count + 1
            resDS, statDS = self.getResponse("GET", "Discovery", refId=jobId)
            logger.info(' resDS ')
            logger.debug(resDS)
            if not statDS:
                logger.info(' could not get response from server ')
                return resDS, False                
            status = resDS["status"]
            logger.info('status after getResponse ')
            logger.debug(status)
            if(status.lower() == "success"):
                break        
        discoveredDevices = []
        if status.lower() == "success":                        
            devices = resDS["devices"]            
            for device in devices:
                tempdict = {}
                for key in device.keys():
                    tempdict[key] = device[key]
                discoveredDevices.append(tempdict)
        logger.info(' discoveredDevices in VerifyDiscovery = ')   
        logger.debug(discoveredDevices)
                 
        return discoveredDevices, True
                
    
    def verifyStorageDiscoveryStatus(self, discRequestResult):
        """
        Verify Storage Discovery Status
        """
        logger = self.getLoggerInstance()
        logger.info(' IN verifyStorageDiscoveryStatus : ')
        logger.info(' discRequestResult : ')
        logger.debug(discRequestResult)
        jobId = discRequestResult["id"]
        logger.info(' job id ')
        logger.debug(jobId)
        time.sleep(globalVars.defaultWaitTime)     
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(120)
        resDS, statDS = self.getResponse("GET", "Discovery", refId=jobId)
        logger.info(' resDS ')
        logger.debug(resDS)
        
        if not statDS:
            logger.info('could not get response from server !! ')
            return resDS, False                
        status = resDS["status"]
        logger.info(' status after GET : ')
        logger.info(status)
        discoveredDevices = []
        if status.lower() == "success":                        
            devices = resDS["devices"]            
            for device in devices:
                tempdict = {}
                for key in device.keys():
                    tempdict[key] = device[key]
                discoveredDevices.append(tempdict)   
        
        logger.info(' discoveredDevices in Verify Storage ' )
        logger.info(discoveredDevices)  
        return discoveredDevices, True
    
    def verifyChassisDiscoveryStatus(self, discRequestResult):
        """
        Verify Chassis Discovery Status
        """
        logger = self.getLoggerInstance()
        logger.info(' IN verifyChassisDiscoveryStatus : ') 
        logger.info(' discRequestResult : ') 
        logger.info(discRequestResult)
        
        jobId = discRequestResult["id"]
        logger.info(' jobId ')
        logger.debug(jobId)    
        time.sleep(globalVars.defaultWaitTime)     
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(160)
        resDS, statDS = self.getResponse("GET", "Discovery", refId=jobId)
        logger.info(' resDS : ')
        logger.info(resDS)
        if not statDS:
            logger.info(' could not get response !! ')
            return resDS, False                
        status = resDS["status"]
        logger.info('status after GET : ')
        logger.info(status)
        discoveredDevices = []
        if status.lower() == "success":                        
            devices = resDS["devices"]            
            for device in devices:
                tempdict = {}
                for key in device.keys():
                    tempdict[key] = device[key]
                discoveredDevices.append(tempdict)   
        logger.info(' discoveredDevices in Verify Storage = ')
        logger.info(discoveredDevices)         
        return discoveredDevices, True
    
    def getSwitchDiscoveryPayload(self,resource,unmanaged=False):
        
        switchCredentialId = ""
        creName = resource["CredentialName"]
        #credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(creName):
            switchCredentialId = self.credentialMap[creName]
        else:
            return "Switch Credentials not defined", False
        payload = self.readFile(globalVars.switchDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_switch_cred_ref", 
                                        switchCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        logger = self.getLoggerInstance()
        logger.info(' --------- SWITCH PAYLOAD ----------- ')
        logger.debug(payload)
        return payload, True
    
    def getStorageDiscoveryPayload(self,resource,unmanaged=False):
        
        storageCredentialId = ""
        creName = resource["CredentialName"]
        #credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(creName):
            storageCredentialId = self.credentialMap[creName]
        else:
            return "Storage Credentials not defined", False
        
        payload = self.readFile(globalVars.storageDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_storage_cred_ref", 
                                        storageCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        logger = self.getLoggerInstance()
        logger.info(' --------- STORAGE PAYLOAD ----------- ')
        logger.debug(payload)
        return payload, True
    
    def getChassisDiscoveryPayload(self,resource,unmanaged=False):
        
        chassisCredentialId = ""
        creName = resource["CredentialName"]
        print " chassisServerCre: "
        print  resource["ChassisServerCre"]
        chassisServerCre = resource["ChassisServerCre"]
        
        print "chassisSwitchCre : "
        print resource["ChassisSwitchCre"]
        chassisSwitchCre = resource["ChassisSwitchCre"]
        #credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(creName):
            chassisCredentialId = self.credentialMap[creName]
        else:
            return "Chassis Credentials not defined", False
        
        switchCredentialId = ""
        #credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(chassisSwitchCre):
            switchCredentialId = self.credentialMap[chassisSwitchCre]
        else:
            return "Switch Credentials not defined", False
        
        serverCredentialId = ""
        # credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(chassisServerCre):
            serverCredentialId = self.credentialMap[chassisServerCre]
        else:
            return "Server Credentials not defined", False
        
        payload = self.readFile(globalVars.chassisDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_chassis_cred_ref", 
                                        chassisCredentialId).replace("manage_in_asm", str(unmanaged).lower()).replace("default_server_cred_ref",serverCredentialId).replace("$default_switch_cred_ref",switchCredentialId)
        payload = payload.replace('\n','').replace('\t','')
        logger = self.getLoggerInstance()
        logger.info(' --------- CHASSIS PAYLOAD ----------- ')
        logger.debug(payload)
        return payload, True

    def discoverSwitch(self, resource, unmanaged=False):
        """
        Discovers switch with start and end ip
        """
        
        responseDS,statusDS = self.getSwitchDiscoveryPayload(resource, unmanaged=unmanaged)
        if not statusDS:
            return responseDS,False
        responseDSR,statusDSR = self.getResponse("POST", "Discovery", payload=responseDS)
        time.sleep(globalVars.defaultWaitTime)
        return responseDSR,statusDSR
    
    def discoverStorage(self, resource,  unmanaged=False):
        """
        Discovers storage with start and end ip
        """
        Logger = self.getLoggerInstance()
        print " in discoverStorage : unmanaged %s"%str(unmanaged)
        responseDisStr,statusDisStr = self.getStorageDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statusDisStr:
            logger.info(' could not get payload for storage discovery ')
            return responseDisStr,False
        responseDSTRG,statusDSTRG = self.getResponse("POST", "Discovery", payload=responseDisStr)
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return responseDSTRG,statusDSTRG
    
    def discoverChassis(self, resource,  unmanaged=False):
        """
        Discovers chassis with start and end ip
        """
        logger = self.getLoggerInstance()
        responseDisCh,statusDisCh = self.getChassisDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statusDisCh:
            logger.info(' could not get payload for chassis discovery ')
            return responseDisCh,False
        responseDSCH,statusDSCH = self.getResponse("POST", "Discovery", payload=responseDisCh)
        time.sleep(globalVars.defaultWaitTime)
        
        if statusDSCH:
            refId=responseDSCH["id"]
            #self.manageDeviceDiscovery(refId, resource)
            #self.manageResourceChassis(resource)
        return responseDSCH,statusDSCH

    def  getDeviceList(self):
        return self.getResponse("GET", "ManagedDevice")
    
    def getDiscoveryResourceList(self):
        return self.getResponse("GET", "Discovery")
    
    def getResourceList(self):
        print "#####"
        url = self.buildUrl("ManagedDevice")
        logger = self.getLoggerInstance() 
        logger.info( "url") 
        logger.info(url)
        
        uri = self.buildHeaderUri("ManagedDevice")
        headers = self.generateHeaderDeploy(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url,headers=headers)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.content != "":
                print response.content
                self.log_data("Payload :%s"%response.content)
                return response.content, True
        
            else: 
                return response.content, False
        else:
            return "Not able to get device list", False
            
    def createServerPool(self, deviceList,poolName):
        """
        Creates Server Pool with the provided Servers
        """
        logger = self.getLoggerInstance()
        logger.info (" Creating Server Pool ")
        logger.info(" number of servers ")
        logger.info( len(deviceList))
        payload = self.generateMDGroupPayload(globalVars.serverPoolPayload, ["device_refid"], len(deviceList))
        loop = 1
        for device in deviceList:
            payload = payload.replace("device_refid" + str(loop), device['refId'])
            loop += 1
        payload = payload.replace("login_user",globalVars.userName)
        payload = payload.replace("$pool_name",poolName)
        payload = payload.replace('\n', '').replace('\t', '')
        logger.info( " Create ServerPool Payload : ")
        logger.debug(payload)
        return self.getResponse("POST", "ServerPool", payload)
    
    def generateMDGroupPayload(self, payload, mdParameters, mdCount=1):
        """
        Generates Managed Device Group payload
        """
        payload = self.readFile(payload)
        deviceGroup = et.fromstring(payload)        
        deviceList = deviceGroup.find("managedDeviceList")
        for _ in xrange(mdCount - 1):        
            device = deepcopy(deviceList.find("ManagedDevice"))
            deviceList.append(device)
        loop = 1
        for device in deviceList.findall("ManagedDevice"):               
            for subDevice in device:
                if subDevice.text is not None and subDevice.text in mdParameters:      
                    subDevice.text = subDevice.text + str(loop)
            loop += 1
        return et.tostring(deviceGroup, encoding='utf8')
    
    def discoverVM(self, resource, unmanaged=False):
        """
        Discovers VM with start and end ip
        """
        logger = self.getLoggerInstance()
        resVM,statVM = self.getVMDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statVM:
            logger.info(' could not get payload for VM discovery ')
            return resVM,False
        responseDSVM,statusDSVM = self.getResponse("POST", "Discovery", payload=resVM)
        time.sleep(globalVars.defaultWaitTime)
        return responseDSVM,statusDSVM
    
    def getVMDiscoveryPayload(self,resource,unmanaged=False):
        
        VMCredentialId = ""  
        creName = resource["CredentialName"]      
        if self.credentialMap.has_key(creName):
            VMCredentialId = self.credentialMap[creName]
            print " VMCredentialId : "
            print VMCredentialId
        else:
            return "SCVMM Credentials not defined", False
        payload = self.readFile(globalVars.VMDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_scvmm_cred_ref", 
                                        VMCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        logger = self.getLoggerInstance()
        logger.info(' --------- SCVMM PAYLOAD ----------- ')
        logger.debug(payload)
        return payload, True
    
    
    def discoverEMC(self, resource, unmanaged=False):
        """
        Discovers EnterpriseManager with start and end ip
        """
        logger = self.getLoggerInstance()
        resEMC,statEMC = self.getEMCDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statEMC:
            logger.info(' could not get payload for EnterpriseManager discovery ')
            return resEMC,False
        responseDSVM,statusDSVM = self.getResponse("POST", "Discovery", payload=resEMC)
        time.sleep(globalVars.defaultWaitTime)
        return responseDSVM,statusDSVM
    
    def getEMCDiscoveryPayload(self,resource,unmanaged=False):
        
        EMCCredentialId = ""  
        creName = resource["CredentialName"]      
        if self.credentialMap.has_key(creName):
            EMCCredentialId = self.credentialMap[creName]
            print " EMCCredentialId : "
            print EMCCredentialId
        else:
            return "EMCCredentialId Credentials not defined", False
        payload = self.readFile(globalVars.EnterpriseManagerDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_emc_cred_ref", 
                                        EMCCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        logger = self.getLoggerInstance()
        logger.info(' --------- EnterpriseManager discovery PAYLOAD ----------- ')
        logger.debug(payload)
        return payload, True
    
    def manageVM(self,discoveredDevices):
        payload = self.generateVMDevicePayload(discoveredDevices)
        
        return self.getResponse("POST", "ManagedDevice",payload)
    
    def generateVMDevicePayload(self,discoveredDevices):
        VMDict = discoveredDevices[0]
        payload = self.readFile(globalVars.manageDevicePayload)
        payload = payload.replace("device_type",VMDict["deviceType"]).replace("ip_address",VMDict["ipAddress"]).replace("device_model",
                    VMDict["model"]).replace("device_refid",VMDict["deviceRefId"]).replace("device_serviceTag",
                    VMDict["serviceTag"]).replace("device_vendor",VMDict["vendor"])
        
        logger = self.getLoggerInstance()
        logger.info('  printing manage device payload for SCVMM : ')
        logger.debug(payload)
        return payload
    
    
    
    def verifyVMDiscoveryStatus(self,discRequestResult):
        """
        Verify SCVMM Discovery Status
        """
        logger = self.getLoggerInstance()
        logger.info(' IN verifyVMDiscoveryStatus : ') 
        logger.info(' discRequestResult : ') 
        logger.info(discRequestResult)
        jobId = discRequestResult["id"]
        logger.info(' jobId ')
        logger.debug(jobId)    
        time.sleep(globalVars.defaultWaitTime)     
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(100)
        resDS, statDS = self.getResponse("GET", "Discovery", refId=jobId)
        logger.info(' resDS : ')
        logger.info(resDS)
        if not statDS:
            logger.info(' could not get response !! ')
            return resDS, False                
        status = resDS["status"]
        logger.info('status after GET : ')
        logger.info(status)
        discoveredDevices = []
        if status.lower() == "success":                        
            devices = resDS["devices"]            
            for device in devices:
                tempdict = {}
                for key in device.keys():
                    tempdict[key] = device[key]
                discoveredDevices.append(tempdict)   
        logger.info(' discoveredDevices in Verify VM = ')
        logger.info(discoveredDevices)         
        return discoveredDevices, True
    
    def discoverVCenter(self, resource, unmanaged=False):
        logger = self.getLoggerInstance()
        resVC,statVC = self.getVCenterDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statVC:
            logger.info(' could not get payload for VM discovery ')
            return resVC,False
        responseDVC,statusDVC = self.getResponse("POST", "Discovery", payload=resVC)
        time.sleep(globalVars.defaultWaitTime)
        return responseDVC,statusDVC


    def getVCenterDiscoveryPayload(self,resource,unmanaged=False):
        
        VCenterCredentialId = ""    
        creName = resource["CredentialName"]    
        if self.credentialMap.has_key(creName):
            VCenterCredentialId = self.credentialMap[creName]
        else:
            return "VCENTER Credentials not defined", False
        payload = self.readFile(globalVars.VCenterDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_vcenter_cred_ref", 
                                        VCenterCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        logger = self.getLoggerInstance()
        logger.info(' --------- VCENTER PAYLOAD ----------- ')
        logger.debug(payload)
        return payload, True



    def getRefIDManageDevice(self,deviceType):
        """
        get the refID of ManageDevice
        """
        logger = self.getLoggerInstance()
        logger.info(' IN getRefIDManageDevice : ') 
        logger.info('get  of ManageDevice : ')
        resMD,statMD = self.getResponse("GET", "ManagedDevice")
        #response = self.convertResponse(resMD)
        print " statMD : %s"%str(statMD)
        print " resMD : %s"%str(resMD)
        refIDList = []
        for device in resMD:
            if device['deviceType']==deviceType:
                refId = device['refId']
                print " refID of device : "
                print refId
                refIDList.append(refId)
        return refIDList
    
    def configureResource(self):
        """
        Configure Resource 
        """
        try:
            
            logger = self.getLoggerInstance()
            responseDisCh,statusDisCh = self.getConfigureResourcePayload()
        
            if not statusDisCh:
                logger.info(' could not get payload for Configure Resource')
                return responseDisCh,False
        
            responseDSCH,statusDSCH = self.getResponse("POST", "Configure", payload=responseDisCh)
            time.sleep(globalVars.defaultWaitTime)
            return responseDSCH,statusDSCH
        except Exception as e1:
            self.log_data( 'Exception occurred while  payload for Configure Resource ')
            self.log_data(str(e1))
    
    
    def getConfigureResourcePayload(self):
        
        
        self.getResources()
        self.log_data(" Going to Create Template :: ")
        
        chassisId=""
        self.chassisRes = self.getReqResource(limit=1, resourceType='CHASSIS', deviceType=None)
        if len(self.chassisRes) == 0:
            print "Required no. of Chasis not available"
            return "No Chasis Discovered", False
        else:
            chassisId = self.chassisRes[0]["refid"]
        
        response =self.getManagedDeviceRefID(chassisId)
        root = ET.fromstring(response.content)
        print " ROOT : "
        print ET.tostring(root)
        indTD = root.find('config')
        payload = indTD.text
        
        
        logger = self.getLoggerInstance()
        logger.info(' --------- Configure  Resource ----------- ')
        logger.debug(payload)
        return payload, True
    
    
    def getdiscoveryDeviceID(self):
        
        resDS, statDS = self.getResponse("GET", "Discovery")
        
        deviceID=''
     
    
        for result in resDS:
            devices = result["devices"]
            if len(devices) >0:
                deviceID = devices[0]['deviceRefId']
                print 'device refid'
                print deviceID
                break
        
        return deviceID 
    
    
    '''
        ################################################################################
       <-- Common function used for Chassis Configuration test cases Start --> 
    '''
   
    def getChassisListDiscoveryPayload(self,unmanaged=False):
        logger = self.getLoggerInstance()
        logger.info(" in fn getChassisListDiscoveryPayload ")
        chassisCredentialId = ""
        switchCredentialId = ""
        serverCredentialId = ""
        credInfo,status = self.loadCredentialInputs()
        for creds in credInfo:
            if creds["Type"]=="CHASSIS":
                if self.credentialMap.has_key(creds["Name"]):
                    chassisCredentialId = self.credentialMap[creds["Name"]]
                else:
                    return "Chassis Credentials not defined", False
            if creds["Type"]=="SERVER":
                if self.credentialMap.has_key(creds["Name"]):
                    serverCredentialId = self.credentialMap[creds["Name"]]
                else:
                    return "Server Credentials not defined", False
            if creds["Type"]=="SWITCH":
                if self.credentialMap.has_key(creds["Name"]):
                    switchCredentialId = self.credentialMap[creds["Name"]]
                else:
                    return "Switch Credentials not defined", False
                     
        logger.info( "chassisCredentialId  switchCredentialId  serverCredentialId : ")
        logger.info(chassisCredentialId)
        logger.info(switchCredentialId)
        logger.info(serverCredentialId)
        
        payload = self.readFile(globalVars.chassisDiscPayload)
        payload = payload.replace("$start_ip",chassisConfigParam.startIP).replace("$end_ip", 
                    chassisConfigParam.endIP).replace("$default_chassis_cred_ref", chassisCredentialId).replace("manage_in_asm", 
                    str(unmanaged).lower()).replace("default_server_cred_ref",serverCredentialId).replace("$default_switch_cred_ref",switchCredentialId)
        payload = payload.replace('\n','').replace('\t','')
        
        logger.debug("--------- CHASSIS LIST DISCOVERY PAYLOAD ----------- ")
        logger.debug( payload)
        return payload, True

    
    def discoverChassisList(self, unmanaged=False):
        """
        Discovers chassis with start and end ip
        """
        logger = self.getLoggerInstance()
        logger.info(" in fn discoverChassisList ")
        jobId = ""
        #responseDisCh,statusDisCh = self.getChassisListDiscoveryPayload(unmanaged=unmanaged)
        responseDisCh,statusDisCh = self.getChassisListDiscoveryPayload(unmanaged = unmanaged)
        if not statusDisCh:
            logger.info(' could not get payload for chassis discovery ')
            return responseDisCh,False
        responseDSCH,statusDSCH = self.getResponse("POST", "ChassisListDiscovery", payload=responseDisCh)
#         responseDSCH,statusDSCH = self.getResponse("POST", "Discovery", payload=responseDisCh)
        return responseDSCH,statusDSCH
    
    
    def verifyChassisDiscovery(self,responseDisCh):
        logger = self.getLoggerInstance()
        jobId = responseDisCh["id"]
        print jobId
        logger.debug( " in verifyDiscovery job ID : ")
        logger.debug(jobId)
        time.sleep(globalVars.defaultWaitTime)
#         time.sleep(globalVars.defaultWaitTime)
        status =  "inprogress"
        count = 0
        retVal = False
        while(count < 60 ):
            time.sleep(globalVars.defaultWaitTime)
#             time.sleep(globalVars.defaultWaitTime)
            count = count + 1
            resDS, statDS = self.getResponse("GET", "Discovery", refId=jobId)
            logger.debug( ' Response from GET Discovery : ')
            logger.debug( resDS)
            if not statDS:
                print " could not get response from server "
                return resDS,False
            status = resDS["status"]
            if(status.lower() == "success"):
                retVal = True
                break
        return retVal,resDS
    

    def getdeviceRefId(self):
        logger = self.getLoggerInstance()
        resp,stat = self.getResponse("GET", "Discovery")
        print resp
        time.sleep(globalVars.defaultWaitTime)
        logger.debug( " Response of GET Discovery in fn getdeviceRefId : ")
        logger.debug(resp)
        refId = ""
        deviceRefId = ""
        IPAddress = ""
        for result in resp:
            devices = result['devices']
            if result['status']=="SUCCESS" and len(devices)>0:
                if devices[0]["ipAddress"] == chassisConfigParam.startIP:
                    IPAddress = devices[0]["ipAddress"]
                    deviceRefId = devices[0]["deviceRefId"]
                    refId = devices[0]["refId"]
                    break
        return refId,IPAddress,deviceRefId
    
    
    def getNetworkID(self):
        logger = self.getLoggerInstance()
        networkId = ""
        resNET, statNET = self.getResponse("GET", "Network")
        if not statNET and "No information found" in resNET:
            return " No Network Information Found ",False
        else:
            for nw in resNET:
                if str(nw["name"])=="NEWHARDWAREMANAGEMENT" and str(nw["type"])=="HARDWARE_MANAGEMENT":
                    networkId = nw["id"]
        logger.info( " retreived NetworkID : ")
        logger.info(networkId)            
        return networkId,True
    
    
    def postChassisDiscovery(self,payload):
        logger = self.getLoggerInstance()
        logger.info(" going to POST ChassisConfigureDiscover")
        url = self.buildUrl("ChassisConfigureDiscover")
        logger.info("printing ChassisConfigureDiscover url")
        logger.info(url)
        
        uri = globalVars.serviceUriInfo["ChassisConfigureDiscover"]
        logger.info(uri)
        headers=self.generateHeaderforDiscoverChassis(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        logger.info("Printing headers information")
        logger.info(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=payload, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.text != "":
                data = json.loads(response.text)
                return self.convertUTA(data), True
            else:
                return "No information found for ChassisConfigureDiscover", False
        else:
            return str(response.status_code) + " " + str(self.convertUTA(response.text)), False
    
    
    '''
       <-- Common function used for Chassis Configuration test cases End -->
       ################################################################################ 
    '''
    
    def manageDeviceDiscovery(self, refId, resource):
        wait = 10  
        resDS = None
        discoveryStatus = False      
        while wait:
            resDS, statDS = self.getResponse("GET", "Discovery", refId=refId)
            if "No information found for Discovery" in resDS:
                break            
            if statDS:
                status = resDS["status"]
                if status.lower() == "inprogress":
                    continue
                else:
                    time.sleep(globalVars.defaultWaitTime)
            wait = wait -1
        if resDS is None or resDS == "":
            return "Device taking longer than expected to Discover", False
        elif "No information found for Discovery" in resDS:
            discoveryStatus = True
        if discoveryStatus:            
            resMD, statMD = self.getResponse("GET", "ManagedDevice")
            if not statMD:
                self.log_data("Unable to fetch Managed Device Information during Discovery: %s"%resMD)
                return "Unable to fetch Managed Device Information during Discovery: %s"%resMD, False
            for device in resMD:
                ipRange = self.getIPRange(resource["START_IP"], resource["END_IP"])
                for ip in ipRange:
                    if device["ipAddress"] == ip:
                        globalVars.ipMap[device["ipAddress"]] = device["refId"]        
        return globalVars.ipMap, True

    def getResourceByCertname(self,action,requestType,refId):
        print "#####"
        url = self.buildUrl(requestType, refId=refId)
        logger = self.getLoggerInstance() 
        logger.info( "url") 
        logger.info(url)
        
        uri = self.buildHeaderUri(requestType, refId=refId)
        headers = self.generateHeaderDeploy(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url,headers=headers,verify=False)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.content != "":
                print response.content
                self.log_data("Payload :%s"%response.content)
                return response.content, True
        
            else: 
                return response.content, False
        else:
            return "Not able to get device list", False   



    
    
