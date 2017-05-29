'''
Created on Aug 23, 2014

@author: dheeraj_si
'''
import requests
import time
import json
import datetime
import initialSetupReqValue
import globalVars
from utilityModule import UtilBase

class InitialTestBase(UtilBase):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        
        UtilBase.__init__(self)
    
    
    #Added for License 
    def authenticatelicense(self):
        startTime = datetime.datetime.now()   
        uri = self.buildUrl("license")
        putData = self.getlicensePayload()
        response = requests.put(uri,data=putData, verify=False)#Put Request for License
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(uri,"PUT",self.headers,putData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        return response.text
    
        #Added for License
    def getlicensePayload(self):
        payload = self.readFile(globalVars.licensePayload)
        payload = payload.replace("$key",initialSetupReqValue.key).replace("$certi",initialSetupReqValue.certi)
        # Key and Certi has been saved in licenseFile.py 
        return payload
    
    #Added for Timezone 
    def putTimeZone(self):
        startTime = datetime.datetime.now()
        url = self.buildUrl("Timezone")
        uri = globalVars.serviceUriInfo["Timezone"]
        logger = self.getLoggerInstance()
        logger.info(' Going to Timezone at ')
        logger.debug(uri)
        putData = self.getTimeZonePayload()
        headers=self.generateSecurityHeader(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.put(url,data=putData,headers=headers, verify=False)#Put Request for Timezone
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,"PUT",headers,putData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        return response.content
    
        #Added for TimeZone
    def getTimeZonePayload(self):
        payload = self.readFile(globalVars.timezonePayload)
        payload = payload.replace("$timeZoneId",initialSetupReqValue.timezone)# timezone has been saved in timezoneFile.py 
        return payload

    #Added for NTP 
    def putNTP(self):
        startTime = datetime.datetime.now()
        url = self.buildUrl("NTP")
        uri = globalVars.serviceUriInfo["NTP"]
        logger = self.getLoggerInstance()
        logger.info(' Going to NTP at ')
        logger.debug(url)
        putData = self.getNTPPayload()
        headers=self.generateSecurityHeader(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.put(url,data=putData,headers=headers, verify=False)#Put Request for NTP
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,"PUT", headers,putData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        
        if(response.status_code==204):
            logger.info( ' Successfully set the NTP settings ')
        else:
            logger.info( ' Error in Setting NTP ')
        
        return response.content
        
        #Added for NTP
    def getNTPPayload(self):
        payload = self.readFile(globalVars.NTPPayload)
        payload = payload.replace("$NTPServer",initialSetupReqValue.NTPServer)# NTPServer has been saved in ntpFile.py 
        return payload
    
    #Added for Proxy 
    def putProxy(self):
        startTime = datetime.datetime.now()
        
        url = self.buildUrl("proxy")
        uri = globalVars.serviceUriInfo["proxy"]
#         uri = self.buildHeaderUri("proxyTest")
        logger = self.getLoggerInstance()
        logger.info( ' Going to proxy at ')
        logger.debug(url)
        putData = self.getProxyPayload()
        headers=self.generateSecurityHeader(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.put(url,data=putData,headers=headers, verify=False)#Put Request for Proxy
        
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,"PUT", headers,putData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        return response.content
    
        #Added for Proxy
    def getProxyPayload(self):
        payload = self.readFile(globalVars.ProxyPayload)
        payload = payload.replace("$SetEnabledFlag",initialSetupReqValue.SetEnabledFlag).replace("$SetPort",initialSetupReqValue.SetPort).replace("$SetUserFlag",initialSetupReqValue.SetUserFlag)# Keyvalues of Proxy has been saved in ProxyFile1.py 
        return payload
    
    
    def getProxyStatus(self):
        url = self.buildUrl("proxy")
        uri = globalVars.serviceUriInfo["proxy"]
        logger = self.getLoggerInstance()
        logger.info(' Going to Proxy at ') 
        logger.debug(url)
        startTime = datetime.datetime.now()
        headers = self.generateSecurityHeader(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,"GET", headers,""), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        logger.info(' Response of Proxy ')
        logger.info('status code')
        logger.debug(response.status_code)
        logger.info('response content ')
        logger.debug(response.content)
        return response.text
    
    def putWizard(self):
        startTime = datetime.datetime.now()
        
        url = self.buildUrl("Wizard")
        uri = globalVars.serviceUriInfo["Wizard"]
        logger = self.getLoggerInstance()
        logger.info(' Going to Wizard at ')
        logger.debug(url)
        putData = self.getWizardPayload()
        headers=self.generateSecurityHeader(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.put(url,data=putData,headers=headers, verify=False)#Put Request for Proxy
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,"PUT", headers,putData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        
        logger.info(' Wizard Status is "')
        logger.info(response.status_code)
        logger.info(' wizard response ')
        logger.info(response)
        return response.content
        
        
    def getWizardPayload(self):
        payload = self.readFile(globalVars.WizardPayload)
        payload = payload.replace("$Completed",initialSetupReqValue.Completed).replace("$Started",initialSetupReqValue.Started)# Keyvalues of Proxy has been saved in ProxyFile1.py
        return payload
    
    def getWizardStatus(self):
        url = self.buildUrl("Wizard")
        uri = globalVars.serviceUriInfo["Wizard"]
        logger = self.getLoggerInstance()
        logger.info(' Going to Wizard at ')
        logger.debug(url)
        response=requests.get(url)
        startTime = datetime.datetime.now()
        headers = self.generateSecurityHeader(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,"GET", headers,""), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        logger.info( 'Response of Wizard Status is ')
        logger.info ( 'status code : ')
        logger.info(response.status_code)
        logger.info(' Content : ')
        logger.debug(str(response.content))
        return response.content,response.status_code

#Added for DHCP 
    def putDHCP(self):
        startTime = datetime.datetime.now()
        
        url = self.buildUrl("DHCP")
        uri = globalVars.serviceUriInfo["DHCP"]
        logger = self.getLoggerInstance()
        logger.info( ' Going to DHCP at ')
        logger.debug(url)
        putData = self.getDHCPPayload()
        headers=self.generateSecurityHeader(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.put(url,data=putData,headers=headers, verify=False)#Put Request for Proxy
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,"PUT", headers,putData), response.status_code, "", startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        return response
    
        #Added for DHCP
    def getDHCPPayload(self):
        payload = self.readFile(globalVars.dhcpPayload)
        payload = payload.replace("$Enable",initialSetupReqValue.DHCP_Enabled).replace("$Subnet",initialSetupReqValue.DHCP_Subnet).replace("$Netmask",initialSetupReqValue.DHCP_Netmask)
        payload = payload.replace("$StartIP",initialSetupReqValue.DHCP_StartingIpAddress).replace("$EndIP",initialSetupReqValue.DHCP_EndingIpAddress).replace("$DefaultLTime",initialSetupReqValue.DHCP_DefaultLeaseTime)
        payload = payload.replace("$MaxLTime",initialSetupReqValue.DHCP_MaxLeaseTime).replace("$DNS",initialSetupReqValue.DHCP_Dns).replace("$Gateway",initialSetupReqValue.DHCP_Gateway).replace("$Domain",initialSetupReqValue.DHCP_Domain)   
        return payload
    
    def postProxy(self):
        startTime = datetime.datetime.now()
        
        url = self.buildUrl("proxyTest")
#         uri = globalVars.serviceUriInfo["proxy"]
        uri = self.buildHeaderUri("proxyTest")
        logger = self.getLoggerInstance()
        logger.info( ' Going to proxy at ')
        logger.debug(url)
        putData = self.getProxyPayload()
        headers=self.generateSecurityHeader(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.post(url,data=putData,headers=headers, verify=False)
        
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,"POST", headers,putData), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        if response.status_code in(200,201,202,203,204):
            return response.text, True
        else:
            return "Not able to test proxy",False
        