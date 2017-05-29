'''
Created on Nov 3, 2014

@author: waseem.irshad
'''


import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from DiscoverResourceBaseClass import DiscoverResourceTestBase
import json
import time
from util import globalVars
import chassisConfigParam


'''
    Discover a chassis and configure the chassis to use static IP for switches with new credentials
    Pre Requisite : Hardware Management network is defined in Networks with IP Range.
'''

class Testcase(DiscoverResourceTestBase):
    
    tc_Id = ""
    chassisCredentialId = ""
    switchCredentialId = ""
    serverCredentialId = ""
    chassisServiceTag = ""
    chassisRefId = ""
    chIPAddr = ""
    resDS = []
    statDS = False
   
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def test_Login(self):
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        creResponse,result = self.setupCredentials()
        logger = self.getLoggerInstance()               
        logger.debug('RESULT')
        logger.debug('CREDENTIAL RESPONSE')
        logger.info(creResponse)
        
        
        
    def configureChassis(self):
        logger = self.getLoggerInstance()
        resGCL,statGCL = self.discoverChassisList(unmanaged = False)
        logger.info(" in fn configureChassis ")
        returnVal,response = self.verifyChassisDiscovery(resGCL)
        if response is not None:
            device = response['devices']
            for vals in device:
                self.chassisServiceTag = vals['serviceTag']
                #self.chassisRefId = vals['refId']
            self.log_data( " Chassis service Tag : ")
            self.log_data( self.chassisServiceTag)
            #print self.chassisRefId
        
        if returnVal == False:
            print " Unable to Discover chassis "
        if (returnVal == True):
            refId,chassisIP,deviceRefId = self.getdeviceRefId()
            logger.info( " refId chassisIP  deviceRefId : ")
            logger.info(refId)
            logger.info(chassisIP)
            logger.info(deviceRefId)
            newSwitchCred = self.getNewSwitchCredential()
            resCP,statCP = self.postConfigureProcess(deviceRefId,newSwitchCred)
            resCD,statusCD = self.postConfigureDiscover(deviceRefId,newSwitchCred)
            time.sleep(300)
                
        else:
            self.log_TestData(["","","",str(self.tc_Id),'Failed','Failed to configure chassis'])
            self.log_data(" Unable to discover Chassis as Success ")

                        
        
    def getNewSwitchCredential(self):
        credentialName = chassisConfigParam.newSwitchCredName
        if credentialName == " ":
            print " Please provide new credential name in parameter file"
        newSwitchPassword = chassisConfigParam.newSwitchPsssword
        if newSwitchPassword == " ":
            print " Please provide new credential password in parameter file"
                 
        payload = self.readFile(globalVars.credentialPayload)
        payload = payload.replace("cre_type","iomCredential").replace("cre_label",
                    credentialName).replace("cre_password",newSwitchPassword).replace("cre_username","root")
        payload = payload.replace(r"<cre_id>", "").replace("cre_protocol","TELNET").replace("cre_snmp","public")
        payload = payload.replace('\n', '').replace('\t', '')
        response,status = self.getResponse("POST", "Credential", payload)
        newSwitchCredentialId = response["credential"]["id"]
        
        
                     
                    
    def postConfigureDiscover(self,refId,credId):
        logger = self.getLoggerInstance()
        logger.info(" in fn postConfigureDiscover ")
        logger.info(" networkId : ")
        logger.info(self.networkId)

        payload = self.readFile(globalVars.filename_TestCase_104148)
        payload = payload.replace("$credId_Chassis",self.chassisCredentialId).replace("$NetworkId",self.networkId).replace("$credId_Switch",
                    credId).replace("$ServerCredId",self.serverCredentialId).replace("$deviceRefId",refId)
        responseP, statusP = self.postChassisDiscovery(payload)
        return responseP,statusP
    
    def getDefaultCredName(self,deviceType):
        credentialInfo,status = self.loadCredentialInputs()
        for credential in credentialInfo:
            if (credential["Type"]==deviceType):
                return credential["Name"]
        
    def postConfigureProcess(self,refId,credId):
        logger = self.getLoggerInstance()
        self.networkId,stat = self.getNetworkID()
        logger.info(" in fn postConfigureProcess ")
        
        chassisCredName = self.getDefaultCredName("CHASSIS")
        
        #credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(chassisCredName):
            self.chassisCredentialId = self.credentialMap[chassisCredName]
        else:
            return "Chassis Credentials not defined", False
        
        serverCredName = self.getDefaultCredName("SERVER")
        # credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(serverCredName):
            self.serverCredentialId = self.credentialMap[serverCredName]
        else:
            return "Server Credentials not defined", False

        payload = self.readFile(globalVars.filename_TestCase_104148)
        payload = payload.replace("$credId_Chassis",self.chassisCredentialId).replace("$NetworkId",self.networkId).replace("$credId_Switch",
                    credId).replace("$ServerCredId",self.serverCredentialId).replace("$deviceRefId",refId)
        
        logger.info(" going to POST ChassisConfigureProcess")
        response,status = self.getResponse("POST", "ChassisConfigureProcess", payload=payload)
        return response,status
    
    
    
    def verifyChassisConfiguration(self): 
        ret = False
        while True:
            resDS,statDS = self.getResponse("GET", "Discovery")
            ret = self.getStatus(resDS)
            if ret == False:
                time.sleep(120)
            else:
                break
         
        if ret == True:
            resGM, statGM = self.getResponse("GET", "ManagedDevice")
            if not statGM:
                print " Failed to get Resource Info : %s"%str(resGM)
                #time.sleep(30)
            discoveredDevices = []
            for resource in resGM:
                discoveredDevices.append(resource["serviceTag"])
                print " resource[serviceTag] : %s"%str(resource['serviceTag'])
                if resource['serviceTag']==self.chassisServiceTag:
                    self.chIPAddr = resource['ipAddress']
                    self.log_data( " Chassis IP Address : %s"%str(self.chIPAddr))
                resMD, statMD = self.getResponse("GET", "Chassis")
                if not statMD:
                    return resMD, statMD
                for chassis in resMD:
                    if chassis["managementIP"] == self.chIPAddr:
                        iomCount = 0
                        serverCount = 0
                        serverCountMiss = 0
                        iomCountMiss = 0
                        for server in chassis["servers"]:
                            if server['serviceTag'] in discoveredDevices:
                                print "Server: %s Service Tag: %s Slot Name: %s  Status:DISCOVERED "%(server["managementIP"], server["serviceTag"], 
                                                                                   server["slotName"])
                                serverCount+=1
                            else:
                                print "Server: %s Service Tag: %s Slot Name: %s  Status:DISCOVERY FAILED <br>"%(server["managementIP"], server["serviceTag"], 
                                                                                   server["slotName"])
                                serverCountMiss+=1
                        for ioms in chassis['ioms']:
                            if ioms['serviceTag'] in discoveredDevices:
                                print "IOM: %s Service Tag: %s Slot: %s Status:DISCOVERED <br>"%(ioms["managementIP"], ioms["serviceTag"], 
                                                                                       ioms["slot"])
                                iomCount+=1
                            else:
                                print "IOM: %s Service Tag: %s Slot: %s Status:DISCOVERY FAILED <br>"%(ioms["managementIP"], ioms["serviceTag"], 
                                                                                       ioms["slot"])
                                iomCountMiss+=1
                        
                        if serverCountMiss > 0 or iomCountMiss > 0:
                            print "Discovered Device count mismatch ==> Actual Server Count: %s, Discovered: %s and\Actual IOM Count: %s, Discovered IOMS: %s "%(chassisConfigParam.actServerCount, serverCount, chassisConfigParam.actIOMCount, iomCount)
                            self.log_TestData(["","","",str(self.tc_Id),'Success','Chassis configured but all the servers/switches were not discovered'])
                            self.log_data(' Configuration Successfull but all the servers/switches were not discovered')
                            
                        else:
                            print "Discovered Device count matches ==> Actual Server Count: %s, Discovered: %s and\Actual IOM Count: %s, Discovered IOMS: %s "%(chassisConfigParam.actServerCount, serverCount, chassisConfigParam.actIOMCount, iomCount)
                            self.log_TestData(["","","",str(self.tc_Id),'Success','Chassis configured with all the servers/switches discovered'])
                            self.log_data(' Configuration Successfull ')
                            
                            
    
    def getStatus(self,resDS):
        resB = True
        for resource in resDS:
            devices = resource['devices']
            self.log_data( " ============= DEVICES List : ======================== ")
            self.log_data( devices)
             
            status = resource['status']
            self.log_data( " ******** RESOURCE STATUS : ")
            self.log_data( status)
            if(status.lower()=="inprogress"):
                resB = False
        
        return resB

        
            
        
if __name__ == "__main__":
    test = Testcase()
    test.test_Login()
    test.configureChassis()
    test.verifyChassisConfiguration()
    os.chdir(current_dir)



