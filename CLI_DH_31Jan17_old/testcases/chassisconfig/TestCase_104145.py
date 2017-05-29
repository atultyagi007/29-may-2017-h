'''
Created on Oct 28, 2014

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
    Discover a chassis and configure the chassis, server and switches with existing credentials and IPs
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
        logger.debug('CREDENTIAL RESPONSE')
        logger.info(creResponse)
        
        
        
    def configureChassis(self):
        
        resGCL,statGCL = self.discoverChassisList(unmanaged=False)
        logger = self.getLoggerInstance()
        logger.debug(statGCL)
        logger.debug( "response After discoverChassisList : ")
        logger.debug(resGCL)
        
        returnVal,response = self.verifyChassisDiscovery(resGCL)
        if response is not None:
            device = response['devices']
            for vals in device:
                self.chassisServiceTag = vals['serviceTag']
                #self.chassisRefId = vals['refId']
            print "service Tag : "
            print self.chassisServiceTag
            #print self.chassisRefId
            
        if (returnVal == True):
            refId,chassisIP,deviceRefId = self.getdeviceRefId()
            logger.debug( " refId, chassisIP , deviceRefId : ")
            logger.debug( refId)
            logger.debug(chassisIP)
            logger.debug(deviceRefId)
             
            resCP,statCP = self.postConfigureProcess(deviceRefId)
            logger.debug( " response After postConfigureProcess :  ")
            logger.debug(resCP)
            logger.debug(statCP)
             
            resCD = self.postConfigureDiscover(deviceRefId)
            logger.debug(resCD)
            time.sleep(300)
            
                         
        else:
            self.log_TestData([str(self.tc_Id),'Failed','Failed to configure chassis'])
            self.log_data(" Unable to discover Chassis as Success ")
             
        
    def getDefaultCredName(self,type):
        credentialInfo,status = self.loadCredentialInputs()
        for credential in credentialInfo:
            if (credential["Type"]==type):
                return credential["Name"]
                    
    def postConfigureDiscover(self,refId):
        logger = self.getLoggerInstance()
        logger.debug( " in configureDiscover : ")
        payload = self.readFile(globalVars.filename_TestCase_104145)
        payload = payload.replace("$credId_Chassis",self.chassisCredentialId).replace("$credId_Switch",
                    self.switchCredentialId).replace("$ServerCredId",self.serverCredentialId).replace("$deviceRefId",refId)
        
        response = self.postChassisDiscovery(payload)
        return response
    
    
                
    def postConfigureProcess(self,refId):
        logger = self.getLoggerInstance()
        logger.debug( " in ConfigureProcess : ")
                
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
        
        switchCredName = self.getDefaultCredName("SWITCH")
        if self.credentialMap.has_key(switchCredName):
            self.switchCredentialId = self.credentialMap[switchCredName]
            
        else:
            return "Switch Credentials not defined", False


        payload = self.readFile(globalVars.filename_TestCase_104145)
        payload = payload.replace("$credId_Chassis",self.chassisCredentialId).replace("$credId_Switch",
                    self.switchCredentialId).replace("$ServerCredId",self.serverCredentialId).replace("$deviceRefId",refId)
        
        logger.debug( " payload in ChassisConfigureProcess : ")
        logger.debug( payload )
        
   
        response,status = self.getResponse("POST", "ChassisConfigureProcess", payload=payload)
        return response,status
    
    
    def getStatus(self,resDS):
        resB = True
        for resource in resDS:
            devices = resource['devices']
            self.log_data( " ============= DEVICES List : ======================== ")
            self.log_data( devices)
             
            status = resource['status']
            self.log_data( " ******** RESOURCE  STATUS : ")
            self.log_data(status)
            if(status.lower()=="inprogress"):
                resB = False
        
        print " resB value : %s"%str(resB)
        return resB
    
    
    def verifyChassisConfiguration(self): 
        ret = False
        while True:
            resDS,statDS = self.getResponse("GET", "Discovery")
            ret = self.getStatus(resDS)
            print " ret : %s"%str(ret)
            if ret == False:
                time.sleep(120)
            else:
                break
         
        if ret == True:
            resGM, statGM = self.getResponse("GET", "ManagedDevice")
            print "Payload managed device :"
            print resGM
            if not statGM:
                print " Failed to get Resource Info : %s"%str(resGM)
            
                #time.sleep(30)
            discoveredDevices = []
            for resource in resGM:
                discoveredDevices.append(resource["serviceTag"])
                print " resource[serviceTag] : %s"%str(resource['serviceTag'])
                if resource['serviceTag']==self.chassisServiceTag:
                    self.chIPAddr = resource['ipAddress']
                    self.log_data(" Chassis IP Address : %s"%str(self.chIPAddr))
                    print " Chassis IP Address : %s"%str(self.chIPAddr)
                resMD, statMD = self.getResponse("GET", "Chassis")
                print "Chassis get:"
                print resMD
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
                            self.log_data(' Configuration Successfull but not all the servers/switches were discovered')
                            
                        else:
                            print "Discovered Device count matches ==> Actual Server Count: %s, Discovered: %s and\Actual IOM Count: %s, Discovered IOMS: %s "%(chassisConfigParam.actServerCount, serverCount, chassisConfigParam.actIOMCount, iomCount)
                            self.log_TestData(["","","",str(self.tc_Id),'Success','Chassis configured with all the servers/switches discovered'])
                            self.log_data(' Configuration Successfull ')
                        
            
                     
            
        
                          
if __name__ == "__main__":
    test = Testcase()
    test.test_Login()
    test.configureChassis()
    test.verifyChassisConfiguration()
    os.chdir(current_dir)
    


