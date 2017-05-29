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
import datetime
from string import join

class DefNetworkTestBase(UtilBase):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        UtilBase.__init__(self)
        
    def setupNetworks(self):
        """
        Create Networks or Manage existing Network        
        """
        networkConfig, status = self.loadNetworkInputs()
        if not status:
            return networkConfig, False
        resNET, statNET = self.getResponse("GET", "Network")
        netResponse = []
        for network in networkConfig:
            found = False
            if network["VLANID"] == "":
                continue
            if not statNET and "No information found" in resNET:
                found = False
            else:
                found = [nw["id"] for nw in resNET if network["Name"] == str(nw["name"])]            
            if not found:
                action = "POST"
                networkId = ""
            else:
                action = "PUT"
                networkId = found[0]
            resDN, statDN = self.defNetworkInfo(network, networkId, action)
            netResponse.append(str(resDN))
            if not statDN:
                return netResponse, False
            if not found: networkId = resDN["id"]
            self.networkMap[network["Type"]] = networkId
        return netResponse, True
    
    
    def loadNetworkInputs(self):
        """
        Loads the Network Information provided in Network.csv
        """ 
      
        configFile = globalVars.networkConfig
       
        try:
            result, status = self.readCsvFile(configFile)
            
            if not status:
                return "Unable to read Configuration File: %s"%configFile , False
            header = result[0]      
            return [dict(zip(header,result[row])) for row in xrange(1,len(result))], True
        except:
            return "Columns mismatch in the Configuration File: %s"%configFile, False
        
    def defNetworkInfo(self, networkInfo, networkId, action):
        """
        Defines new Network with provided VLAN and Network Type
        """        
        payload = self.readFile(globalVars.networkPayload)
        staticvalue = networkInfo["Static"]
        statval =    staticvalue.lower()
    
        payload = payload.replace("nw_name", networkInfo["Name"]).replace("nw_desc",
                                networkInfo["Description"]).replace("nw_type", networkInfo["Type"]).replace(
                                "nw_vlan", networkInfo["VLANID"]).replace("nw_static", statval).replace(
                                "nw_gateway", networkInfo["Gateway"]).replace("nw_subnet", networkInfo["Subnet"]).replace(
                                "nw_startip", networkInfo["StartIP"]).replace("nw_endip", networkInfo["EndIP"]).replace(
                                "nw_dns", networkInfo["PrimaryDNS"]).replace("second_dns", networkInfo["SecondaryDNS"]).replace("nw_suffix", networkInfo["DNSSuffix"])
        payload = payload.replace("nw_id", networkId)
        payload = payload.replace('\n', '').replace('\t', '')              
        return self.getResponse(action, "Network", payload, refId=networkId)

    def usedNetworksIdList(self,nwid="",action = "GET", payload=""):
        """
        Getting used Networks id..Network must be defined         
        """
        
        
        url = self.buildUrl("Network")
        url = url+"/"+"%s"%nwid+"/"+"usageids"
        logger = self.getLoggerInstance() 
        logger.info( "url") 
        logger.info(url)
   
        uri = self.buildHeaderUri("Network")
        uri = uri+"/"+"%s"%nwid+"/"+"usageids"
        startTime = datetime.datetime.now()
        logger.info(startTime)
    
        headers = self.generateSecurityHeader(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        if response.text != "":
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
        
        else:
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
            time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
        # requests.codes.OK:
            if response.text != "":
                data = json.loads(response.text)
                return self.convertUTA(data), True
            else:
                return "No information found", False
                
        else:
            "Not able to fetch used network id", False
    
    def findNetworksIdAddress(self,nwid="",action = "GET", payload="",verify=False):
        """
        Getting used Networks id..Network must be defined         
        """
        
        
        url = self.buildUrl("Network")
        url = url+"/"+"%s"%nwid+"/"+"ipaddresses"
        logger = self.getLoggerInstance() 
        logger.info( "url") 
        logger.info(url)
   
        uri = self.buildHeaderUri("Network")
        uri = uri+"/"+"%s"%nwid+"/"+"ipaddresses"
        startTime = datetime.datetime.now()
        logger.info(startTime)
    
        headers = self.generateSecurityHeader(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url, headers=headers,verify=verify)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        if response.text != "":
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
        
        else:
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
            time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
        # requests.codes.OK:
            if response.text != "":
                data = json.loads(response.text)
                return self.convertUTA(data), True
            else:
                return "No information found", False
                
        else:
            "Not able to get network ip list", False    

