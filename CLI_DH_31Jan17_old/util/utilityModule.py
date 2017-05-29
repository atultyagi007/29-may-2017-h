'''
Created on Aug 23, 2014

@author: dheeraj_si
'''


from ConfigParser import ConfigParser
from copy import deepcopy
from csv import reader as csvreader
from traceback import format_exc
from xml.etree import ElementTree as et
import collections
import globalVars
from random import randrange
import re
import os
import datetime
import csv
import requests
import time
import base64
import hashlib
import hmac
import json
import logging
import logging.config
from logging import Logger, getLogger
from cookielib import logger
import glob
import socket
import platform
import struct
import RepositoryParamFile
import paramiko
import xml.etree.ElementTree as ET

from subprocess import *

class UtilBase:
    
    """
    MainClass with all Product Level functions
    """    
    mdParameters = ["device_type","ip_address","device_refid","device_model","device_serviceTag","device_vendor"]    
    nwConfigTypeMap = {"Private LAN":"PRIVATE_LAN(PRLAN)","Public LAN":"PUBLIC_LAN(PBLAN)","Hypervisor Management":"HYPERVISOR_MANAGEMENT(HMT)",
                       "Hypervisor Migration":"HYPERVISOR_MIGRATION(HMG)","PXE":"PXE(PXE)","Fileshare":"FS(FS)"}
    
    nwMapping = {"PXE":"","PRLAN":"","PBLAN":"","HMG":"","HMT":"","FS":"","SANISCSI":"","SANFCOE":"","HCP":""}
    networkMap = {}
    credentialMap = {}
    headers = {"Accept":"application/json","Content-Type":"application/xml; charset=UTF-8",
                        "Accept-Encoding":"gzip, deflate"}
    networkMap = {}
    rmdParameters = {"device_type":"deviceType","ip_address":"ipAddress","device_refid":"deviceRefId","device_model":"model",
                "device_serviceTag":"serviceTag","device_vendor":"vendor","device_state":"state"}


      
    logsdir = "..\..\logs"
    tc_id = ""  
    #logger = logging.getLogger('DellASM')
    ip_to_number = lambda ip: struct.unpack('!I', socket.inet_aton(ip))[0]
    number_to_ip = lambda num: socket.inet_ntoa(struct.pack('!I', num))
    
    def __init__(self):
        """
        Initialization
        """
        
        self.loadConfig()
        self.loadServices() 
        self.loadServicesPublic()
        
    def getLoggerInstance(self):
        logging.config.fileConfig('../../logs/logging.conf')
        logger = logging.getLogger('DellASM')
        return logger
    
    def getTestCaseID(self,fileName):
        file_name=os.path.basename(fileName)
        if file_name.endswith(".py"):
            class_name=file_name.replace(".py","")
        elif file_name.endswith(".pyc"):
            class_name=file_name.replace(".pyc","")
        tc_id_str=class_name.replace("TestCase_","")
        self.set_tc_id(tc_id_str)
        return tc_id_str
    
    def log_TestData(self,result):
        oFile = open("../../logs/testcasesResultInfo"+datetime.datetime.now().strftime("_%d-%m-%y")+".csv","a")
        csv_writer = csv.writer(oFile,delimiter=",")
        csv_writer.writerow(result)
        oFile.close()
        
    def set_tc_id(self,t_id):
        global tc_id
        tc_id=t_id

     
         
    def readConfig(self,fileName=None, sectionName=None):
        """
        Reads Config File and returns a dictionary 
        """ 
        globalVars.configInfo
        try:   
            config = ConfigParser()
            if not fileName:
                fileName = globalVars.configFile
            config.read(fileName)
            if sectionName:
                globalVars.configInfo[sectionName] = dict(config.items(sectionName))
            else:
                for section in config.sections():
                    globalVars.configInfo[section]=dict(config.items(section))
        except Exception,e:
            print e
            return globalVars.configInfo
    
    
    def readFile(self, fileName):
        """
        Reads text file and returns string
        """
        with open(fileName,'r') as rfp:
            data = rfp.read()
        return data
    
    def writeFile(self, fileName, data):
        """
        Write string in a text file
        """
        text_file = open(fileName, "w")
        text_file.write(data)
        text_file.close()
    
    
    def readCsvFile(self, fileName, delimiter=","):
        """
        Reads csv file and returns a List with each row as an element
        """
        if os.path.exists(fileName):
            try:
                filehandle = open(fileName, "rU")
                reader = csvreader(filehandle, delimiter=delimiter)
                
                retlist = []
                for row in reader:
                    if row:
                        retlist.append(row)
                        
                filehandle.close()
                
                return retlist, True
            except:
                return "ERROR: %s" % str(format_exc()), False
        else:
            return "ERROR: File \"%s\" does not exists." % fileName, False
        
    def loadServices(self):
        """
        Description:
            API to get Service Url's from the Services.xml file.
            
        Input:
                
        Output:
            Returns BeautifulStoneSoup element containing Service URL details
              
        """
        
        #fileName = os.path.abspath(globalVars.serviceUriInfoFile)
       # fileName = self.readFile(globalVars.serviceUriInfoFile)
        tree = et.parse(globalVars.serviceUriInfoFile)
        root = tree.getroot()
        
        for child in root.findall('url'):
            globalVars.serviceUriInfo[child.get('name')] = child.get('uri')
    
    
    
    def loadServicesPublic(self):
        """
        Description:
            API to get Service Url's from the Services.xml file.
            
        Input:
                
        Output:
            Returns BeautifulStoneSoup element containing Service URL details
              
        """
        
        #fileName = os.path.abspath(globalVars.serviceUriInfoFile)
       # fileName = self.readFile(globalVars.serviceUriInfoFile)
        tree = et.parse(globalVars.publicserviceUriInfoFile)
        root = tree.getroot()
        
        for child in root.findall('url'):
            globalVars.publicServiceUriInfo[child.get('name')] = child.get('uri')
            
                
    def loadConfig(self):
        """
        Description:
            API to get Service Url's from the Services.xml file.
            
        Input:
                
        Output:
            Returns BeautifulStoneSoup element containing Service URL details
              
        """
        self.readConfig() 
        #print  globalVars.configInfo          
        
        
        
    def get_sig( self, http_method, path, timestamp):
        """Return a signature valid for use in making DELL API calls

        :param http_method: The `http_method` used to make the API call
        :param path: The `path` used in the API call
        """
        signpath = path
        parts = []
        parts.append(globalVars.apiKey)
        parts.append(http_method)
        parts.append(signpath)
        parts.append(globalVars.userAgent)
        parts.append(timestamp)
        
        dm = hashlib.sha256
        to_sign = ':'.join([str(x) for x in parts])
        
        d = hmac.new(str(globalVars.apiSecret),msg=to_sign, digestmod=dm).digest()
        b64auth = base64.b64encode(d).decode()

        return b64auth
        

        
    def builFinaldUrl(self, uri):
        """
        Builds a Service Url and Returns 
        """
        return "http://"+  globalVars.configInfo['Appliance']['ip'] + ":" + globalVars.configInfo['Appliance']['port'] + uri
    
    def buildUrl(self, feature, refId=None):
        """
        Builds a Service Url and Returns 
        """
        
        if globalVars.uriType == "Public":
            
            basePath = "https://"+  globalVars.configInfo['Appliance']['ip']
            #basePAth=http://i.p/9080
            print globalVars.publicServiceUriInfo
            uri = globalVars.publicServiceUriInfo[feature]
            #uri = /admin/authenticate
            #print uri
            
            if (len(uri) > 3):
                
                if refId:
                    return basePath +  uri + "/" + refId
                else:
                    return basePath +  uri
                
            else:
                basePath = "http://"+  globalVars.configInfo['Appliance']['ip'] + ":" + globalVars.configInfo['Appliance']['port']
                #basePAth=http://i.p/9080
                uri = globalVars.serviceUriInfo[feature]
                #uri = /admin/authenticate
                #print uri
                if refId:
                    return basePath +  uri + "/" + refId
                else:
                    return basePath +  uri

        else:
            basePath = "http://"+  globalVars.configInfo['Appliance']['ip'] + ":" + globalVars.configInfo['Appliance']['port']
            #basePAth=http://i.p/9080
            uri = globalVars.serviceUriInfo[feature]
            #uri = /admin/authenticate
            #print uri
            if refId:
                return basePath +  uri + "/" + refId
            else:
                return basePath +  uri

    
    def buildHeaderUri(self, feature, refId=None):
        """
        Builds a header  Uri and Returns 
        """
        if globalVars.uriType == "Public":
            
            uri = globalVars.publicServiceUriInfo[feature]
            
            if (len(uri) > 3):
                if refId:
                    return uri + "/" + refId
                else:
                    return uri
            
            else:
                uri = globalVars.serviceUriInfo[feature]
                if refId:
                    return uri + "/" + refId
                else:
                    return uri
        else:
            uri = globalVars.serviceUriInfo[feature]
            if refId:
                return uri + "/" + refId
            else:
                return uri
    
    
    def convertUTA(self, data):
            """
            Converts Unicode data to ASCII
            """
            if isinstance(data, basestring):
                return str(data)
            elif isinstance(data, collections.Mapping):
                return dict(map(self.convertUTA, data.iteritems()))
            elif isinstance(data, collections.Iterable):
                return type(data)(map(self.convertUTA, data))
            else:
                return data
    
    def get_special_headers(self, http_method, path,xmlOrJson=None):
        timestamp = int(round(time.time() * 1000))
        b64auth =self.get_sig(http_method, path, timestamp)
        headers = {'x-dell-auth-key':str(globalVars.apiKey),
    'x-dell-auth-timestamp': str(timestamp),
    'x-dell-auth-signature': str(b64auth),
    'Accept': 'application/json',
    'Content-Type': 'application/xml',
    'User-Agent': globalVars.userAgent}

#         if(xmlOrJson == "template"):
#             headers = {"Accept":"application/xml","Content-Type":"application/xml; charset=UTF-8",
#                          "Accept-Encoding":"gzip, deflate","x-dell-auth-key":str(loginFile.AUTH_KEY),"x-dell-auth-signature":str(b64auth), "x-dell-auth-timestamp":str(timestamp), "User-Agent":loginFile.USER_AGENT}
#         else:
#             headers = {"Accept":"application/json","Content-Type":"application/xml; charset=UTF-8",
#                          "Accept-Encoding":"gzip, deflate","x-dell-auth-key":str(loginFile.AUTH_KEY),"x-dell-auth-signature":str(b64auth), "x-dell-auth-timestamp":str(timestamp), "User-Agent":loginFile.USER_AGENT}
        return headers
    
    def getResponse(self, action, requestType, payload="", refId=None):
        """
        Gets the Response by calling corresponding   
        """        
        print  payload      
        url = self.buildUrl(requestType, refId=refId)
        logger = self.getLoggerInstance() 
        logger.info( "url") 
        logger.info(url)
        
        uri = self.buildHeaderUri(requestType, refId=refId)
        
        startTime = datetime.datetime.now()              
        if action == "POST":
            
            headers = self.generateSecurityHeader(uri, 'POST', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.post(url, data=payload, headers=headers,verify=False)
        elif action == "PUT":
            headers = self.generateSecurityHeader(uri, 'PUT', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.put(url, data=payload, headers=headers,verify=False)
        elif action == "DELETE":
            headers = self.generateSecurityHeader(uri, 'DELETE', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.delete(url, headers=headers,verify=False)
        else:
            headers = self.generateSecurityHeader(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.get(url, headers=headers,verify=False)
        
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        if action == "GET":
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
            
        else:
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text.encode('utf-8'), startTime, endTime, elapsedTime)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.text != "":
                try:
                    data = json.loads(response.text)
                    return self.convertUTA(data), True
                    
                except:
                    try:
                        data = json.loads(response.text.encode('ascii', 'ignore').decode('ascii'))
                        return self.convertUTA(data), True
                    except:
                        return response.text.encode('utf-8'),True
                        
            else:
                if action == "PUT":
                    return response.text, True
                elif action == "GET":
                    return "No information found for %s" % requestType, False
                elif action == "DELETE":
                    return "No information found for %s" % requestType, True  
                elif (action == "POST") and (response.status_code in (200, 201, 202, 203, 204)):
                    return "No information found for %s" % requestType, True 
                else:
                    return "No information found for %s" % requestType, False
        elif response.status_code == 409:
            return str(response.content), True
        else:
            if response.status_code == 400:
                return str(response.status_code), False
            else:
                return str(response.status_code) + " " + str(self.convertUTA(response.text)), False
    
    
    
    def runService(self, action, requestType, payload="", refId=None, authStatus=True, responseType="json", requestContentType="xml", headers = globalVars.headers,
               partialLog=False, pattern=".*"):
        
        """
        Gets the Response by calling corresponding API
        """
    
        time.sleep(globalVars.defReqWaitTime)
        startTime = datetime.datetime.now()
        if refId:
            uri = globalVars.serviceUriInfo[requestType]
            if "{refId}" in uri: 
                uri = uri.replace("{refId}", refId)
            else:
                uri = uri + "/" + refId
        else:
#             uri = globalVars.serviceUriInfo[requestType]
            uri = self.buildHeaderUri(requestType, refId=refId)
        if authStatus:
            
            headers = self.generateHeader(uri, action, globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
#         url = self.buildUrl(uri)
        url = self.buildUrl(requestType, refId=refId)
        if responseType == "xml":
            headers["Accept"] = "application/xml"
        elif responseType == "file":
            headers["Accept"] = "application/octet-stream"
        else:
            headers["Accept"] = "application/json"
        if requestContentType == "xml":
            headers["Content-Type"] = "application/xml"
        elif requestContentType == "file":
            headers["Content-Type"] = "application/xml"  
        else:
            headers["Content-Type"] = "application/json"
        if action == "POST":
            response = requests.post(url, data=payload, headers=headers)
        elif action == "PUT":
            response = requests.put(url, data=payload, headers=headers)
        elif action == "DELETE":
            response = requests.delete(url, headers=headers)
        else:
            response = requests.get(url, headers=headers,verify=False)        
    #time.sleep(globalVars.defReqWaitTime)     
        if not authStatus:
            globalVars.userAgent = str(response.request.headers["User-Agent"])
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        if partialLog:
            searchRes = re.search(pattern, response.text, re.M)
            if searchRes:
                result = searchRes.group(0)
            else:
                result = response.text
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, result, startTime, endTime, elapsedTime)
        else:
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        if response.status_code in (200,201,202,203,204):
            if response.text != "":
                if responseType == "xml":
                    return response.text, True
                elif responseType == "file":
                    return response.text, True
                else:
                    data = json.loads(response.text)
                    return self.convertUTA(data), True
            else:
                if action in ("PUT","DELETE"):
                    return response.text, True
                else:
                    return "No information found for %s"%requestType, False
        else:
            return str(response.status_code) + " " + str(self.convertUTA(response.text)), False

    def generateHeader(self, uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        signature = ""
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uri + ":" + userAgent + ":" + timestamp
        try:
            signature =  base64.b64encode(hmac.new(apiSecret, msg=requestString, digestmod=hashlib.sha256).digest())
        except:
            signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/json","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers

    def getAllResponse(self, action, requestType, payload="", refId=None, authStatus=True, responseType="json", 
                    requestContentType="xml", partialLog=False, pattern=".*"):
        """
        Gets the Response for the request provided   
        """        
        if payload != "" and type(payload) == str:
            payload = payload.replace('\n','').replace('\t','')
        return self.runService(action, requestType, payload, refId, authStatus, responseType, requestContentType, partialLog=partialLog, pattern=pattern)
    

    def generateSecurityHeader(self, uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uri + ":" + userAgent + ":" + timestamp
        signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/json","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers
    
    def generateHeaderDeploy(self, uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uri + ":" + userAgent + ":" + timestamp
        signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/xml","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers
    
    
    def generateHeaderGetDeploy(self, uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uri + ":" + userAgent + ":" + timestamp
        signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/json","Content-Type":"application/json", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers


    
    def getMethodResponse(self, action, requestType, payload="", refId=None, authStatus=True, responseType="json"):
        """
        Gets the Response for the request provided   
        """        
        if payload != "":
            payload = payload.replace('\n','').replace('\t','')
        return self.runService(action, requestType, payload, refId, authStatus, responseType)

        
        
    def getAllResources(self):
        """
        Gets all Resources available
        """
        result, status = self.getResponse("GET", "ManagedDevice")
        if not status:
            print "Failed to get all the Resources"
            
        globalVars.resource_SERVERS = []
        globalVars.resource_STORAGE= []
        globalVars.resource_VCENTER=[]
        for resource in result:
            if "server" in str(resource["deviceType"]).lower():
                globalVars.resource_SERVERS.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"]})
            elif "equallogic" in str(resource["deviceType"]).lower():
                globalVars.resource_SERVERS.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"]})
            elif "vcenter" in str(resource["deviceType"]).lower():
                globalVars.resource_VCENTER({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"]})
                
    def cleanServerPool(self):
        """
        Removes all Server Pools
        """
        logger = self.getLoggerInstance()
        resultGET, statuGET = self.getResponse("GET", "ServerPool")
        logger.debug('Going to Delete all the serverPool')
        try:
            for job in resultGET:
                result, status = self.getResponse("DELETE", "ServerPool", refId=str(job["groupSeqId"]))
                if status:
                        self.log_TestData(["", "", "","ServerPool Teardown",'Success',"ServerPool Teardown Successfully with RefID : %s"%str(job["groupSeqId"])])
                else:
                    if result == '400':
                        print result
                    else:
                        print result
                        self.log_TestData(["", "", "","ServerPool Teardown",'Failed',"Failed to Teardown ServerPool with RefID : %s"%str(job["groupSeqId"])])
        
        except Exception as e1:
            logger.debug('Unable to  Remove Server Pools')
            logger.debug(str(e1))
            
            
        logger.debug('Successfully Removed All Server Pools')
        
        return "Successfully Removed All Server Pools", True
            
    def cleanDiscovery(self):
        """
        Removes all Discovery Devices
        """
        resultGET, statuGET = self.getResponse("GET", "Discovery")
        print  "resultGET"
        print resultGET
        logger = self.getLoggerInstance()
        logger.debug('Going to Delete all the Discovery Devices')
        try:
            for job in resultGET:
                result, status = self.getResponse("DELETE", "Discovery", refId=str(job["id"]))
                if status:
                        self.log_TestData(["", "", "","Discovery Teardown",'Success',"Discovery Teardown Successfully with RefID : %s"%str(job["id"])])
                else:
                    if result == '400':
                        print result
                    else:
                        print result
                        self.log_TestData(["", "", "","Discovery Teardown",'Failed',"Failed to Teardown Discovery with RefID : %s"%str(job["id"])])
        
        except Exception as e3:
            self.log_data("No Discovery Devices found to Remove")
            self.log_data(str(e3))
            return "No Discovery Devices found to Remove", True   
            
        logger.debug('Successfully Removed all Discovery Devices')
        return "Successfully Removed all Discovery Devices", True
            
    def cleanManagedDevice(self):
        """
        Removes all ManagedDevice Devices
        """
        resultGET, statuGET = self.getResponse("GET", "ManagedDevice")
        logger = self.getLoggerInstance()
        logger.debug('Going to Delete all the Managed Devices')
        try:
            for job in resultGET:
                result, status = self.getResponse("DELETE", "ManagedDevice", refId=str(job["refId"]))
                if status:
                        self.log_TestData(["", "", "","ManagedDevice Teardown",'Success',"ManagedDevice Teardown Successfully with RefID : %s"%str(job["refId"])])
                else:
                    if result == '400':
                        print result
                    else:
                        print result
                        self.log_TestData(["", "", "","ManagedDevice Teardown",'Failed',"Failed to Teardown ManagedDevice with RefID : %s"%str(job["refId"])])
        
                
        except Exception as e4:
            self.log_data("No Managed Devices found to Remove")
            self.log_data(str(e4))
            return "No Discovery Managed found to Remove", True   
        
        
        logger.debug('Successfully Removed all the Managed Devices')
        return 'Successfully Removed ManagedDevice', True
           


    def cleanNetwork(self):
        """
        Removes all Networks
        """
        resultGET, statuGET = self.getResponse("GET", "Network")
        
        if "No information found" in resultGET:
            print 'No Network found to Remove'
            self.log_data("No Network found to Remove")
            return "No Network found to Remove", True
        
        logger = self.getLoggerInstance()
        logger.debug('Going to Delete all the Networks')
        try:
            for job in resultGET:
                result, status = self.getResponse("DELETE", "Network", refId=str(job["id"]))
                if status:
                        self.log_TestData(["", "", "","Network Teardown",'Success',"Network Teardown Successfully with RefID : %s"%str(job["id"])])
                else:
                    if result == '400':
                        print result
                    else:
                        print result
                        self.log_TestData(["", "", "","Network Teardown",'Failed',"Failed to Teardown Network with RefID : %s"%str(job["id"])])
        
        
        except Exception as e5:
            self.log_data("No Network found to Remove")
            self.log_data(str(e5))
            return "No Network found to Remove", True   
            
        logger.debug('Successfully Removed all the Networks')
        return "Successfully Removed all Network", True



    def cleanCredential(self):
        """
        Removes all Credentials 
        """
        resultGET, statuGET = self.getResponse("GET", "Credential")
        logger = self.getLoggerInstance()
        try:
            result = resultGET['credentialList']
            
            logger.debug('Going to cleane all the Credentials')
            for job in result:
                id = job['credential']['id']
                result, status = self.getResponse("DELETE", "Credential", refId=id)
                if status:
                        self.log_TestData(["", "", "","Credential Teardown",'Success',"Credential Teardown Successfully with RefID : %s"%str(id)])
                else:
                    if result == '400':
                        print result
                    else:
                        print result
                        self.log_TestData(["", "", "","Credential Teardown",'Failed',"Failed to Teardown Credential with RefID : %s"%str(id)])
        
        except Exception as e5:
            self.log_data("No Credentials found to Remove")
            self.log_data(str(e5))
            return "No Credentials found to Remove", True      
        logger.debug('"Successfully cleane all the Credentials')    
        return "Successfully Removed all Credential", True
            
    def cleanUpTemplates(self):
        """
        Removes all Templates
        """
        resultGET, statuGET = self.getResponse("GET", "Template")
        if "No information found" in resultGET:
            print 'No Template found to Remove'
            self.log_data("No Template found to Remove")
            return "No Template found to Remove", True
        
                
        
        try:
            for temp in resultGET:
                if temp["category"] == "Automation":
                    result, status = self.getResponse("DELETE", "Template", refId=str(temp["id"]))
                    if status:
                        self.log_TestData(["", "", "","Template Teardown",'Success',"Template Teardown Successfully with RefID : %s"%str(temp["id"])])
                    else:
                        if result == '400':
                            print result
                        else:
                            print result
                            self.log_TestData(["", "", "","Template Teardown",'Failed',"Failed to Teardown Template with RefID : %s"%str(temp["id"])])
        
        except Exception as e2:
            self.log_data("No Template found to Remove")
            self.log_data(str(e2))
            return "No Template found to Remove", True          
        self.log_data("Successfully Removed All Templates")
        return "Successfully Removed All Templates", True
               


    def cleanUpServices(self):
        """
        Removes all Deployed Services
        """
        resultGET = self.getAllDeploymentId()
        if "No information found" in resultGET:
            print 'No Deployed  Services found to Remove'
            self.log_data("No Deployed  Services found to Remove")
            return "No Deployed  Services found to Remove", True
        
        try:
            for temp in resultGET:
                refId=temp[0]['id']
                self.log_data('refid') 
                self.log_data(refId)
                result1, status1 = self.cleanDeployedService(refId)
                self.log_data(result1)
                result, status = self.teardownServices(refId)
                if status1:
                    self.log_TestData(["", "", "","Service Teardown",'Success',"Service Teardown Successfully with RefID : %s"%refId])
                else:
                    print result1
                    self.log_TestData(["", "", "","Service Teardown",'Failed',"Failed to Teardown Service with RefID : %s"%refId])          
                
                
        except Exception as e1:
            self.log_data("No Deployed  Services found to Remove")
            self.log_data(str(e1))
            return "No Deployed  Services found to Remove", True  
        
        try:
            for temp in resultGET:
                refId=temp[0]['id']
                self.log_data('refid') 
                self.log_data(refId)
                result, status = self.teardownServices(refId)
                self.log_data(result)
                
        except Exception as e1:
            self.log_data(str(e1))
            return "No Deployed  Services found to Remove", True               
            
        self.log_data("Successfully Removed All Deployed Services")
        return "Successfully Removed All Deployed Services", True
            

    def cleanUpUsers(self):
        """
        Removes all Users
        """
        resultGET, statuGET = self.getResponse("GET", "User")
        if not statuGET:
            return resultGET, False
        for temp in resultGET:            
            if temp["userName"] != "admin":
                result, status = self.getResponse("DELETE", "User", refId=str(temp["userSeqId"]))
                if not status: 
                    return result, status
                else:
                    return "Successfully Removed All Users", True
                    
    def updateUser(self,refId):
        """
        Update user info
        """
        userName =  globalVars.ad_user_update    
        domainName = globalVars.domainName_user
        password = globalVars.pwd_standard
        payload = self.readFile(globalVars.userPayload)
        firstName = globalVars.ad_firstname        
        payload = payload.replace("user_name", userName).replace("user_pwd", 
                   password).replace("user_domain", domainName).replace("login_user", self.loginUser).replace("user_role","standard").replace("first_name",firstName)           
        
#         self.getResponse("POST", "User", payload)
        return self.getResponse("PUT", "User", payload,refId=refId)
        
        
    def cleanUpDirectoryService(self):
        """
        Removes all Directory Services
        """
        resultGET, statuGET = self.getResponse("GET", "DirectoryService")
        if "No information found" in resultGET:
            return "No Directory Services found to Remove", True
       
        for temp in resultGET["configurationList"]:
            result, status = self.getResponse("DELETE", "DirectoryService", refId=str(temp["seqId"]))
            if not status: 
                return result, status
            else:
                return "Successfully Removed All Directory Services", True
                
              

            
            
    def getManagedResourceInfo(self, refId):
        """
        Gets Managed Resource Info
        """
        return self.getResponse("GET", "ManagedDevice", refId=refId)
    
    def getServerPoolInfo(self):
        """
        Gets Server Info
        """
        return self.getResponse("GET", "ServerPool")
    
    def authenticate(self,role="Administrator"):
        uri = self.buildUrl("login")
        logger = self.getLoggerInstance()
        logger.info(' Going to Login at ' )
        logger.debug(uri)
        postData = self.getloginPayload(role)
        headers = {'content-type' : 'application/xml'}
        response = requests.post(uri,data=postData,headers=headers, verify=False)
        
        #globalVars.userAgent = str(response.request.headers["User-Agent"])
        self.log_data("Login Response Code:")
        self.log_data(response)
        self.log_data("printing user agent value")
        self.log_data(globalVars.userAgent)
        time.sleep(globalVars.defaultWaitTime)
        loginResponse  = json.loads(response.content)
        logger.info('apiKey is ') 
        logger.debug(loginResponse.get('apiKey'))
        logger.info(' Username is ') 
        logger.debug(loginResponse.get('userName'))
        logger.info(' domain is ') 
        logger.debug(loginResponse.get('domain'))
        logger.info(' role is ')
        logger.debug(loginResponse.get('role'))
        logger.info(' apiSecret is ') 
        logger.debug(loginResponse.get('apiSecret'))
        globalVars.apiKey= loginResponse.get('apiKey')
        logger.info(' Printing Auth key ')
        logger.debug(globalVars.apiKey)
        globalVars.apiSecret= loginResponse.get('apiSecret')
        logger.info(' Printing Secret key ')
        logger.debug(globalVars.apiSecret)
        return response.content
    
        #Added for InitialSetup
    def getloginPayload(self,role="Administrator"):
        payload = self.readFile(globalVars.loginPayload)
        if role == "Administrator" :
            payload = payload.replace("$userName",globalVars.userName).replace("$domain",globalVars.domain).replace("$password",globalVars.password)
            payload = payload.replace("$role",role)
        elif role=="standard":
            payload = payload.replace("$userName",globalVars.standardUser).replace("$domain",globalVars.domain).replace("$password",globalVars.standardPwd)
            payload = payload.replace("$role",role)
        return payload
    
    
    def changeResourceState(self, refId, resourceInfo, state='DISCOVERED'):
        """
        Edits Managed Resource Info
        """
        logger = self.getLoggerInstance()
        logger.info( " in function changeResourceState ")
        
        payload = self.readFile(globalVars.editMDPayload)
        for k,v in self.rmdParameters.items():
            if k == "device_refid":
                payload = payload.replace(k, resourceInfo["refId"])
            elif k == "device_vendor":
                payload = payload.replace(k, resourceInfo["manufacturer"])
            elif k == "device_state":
                payload = payload.replace(k, state)
            else:
                payload = payload.replace(k, resourceInfo[v])  
        logger.info( " paylod in changeResourceState : ")
        logger.debug(payload)       
              
        return self.getResponse("PUT", "ManagedDevice", payload, refId=refId)
    
    
    def log_data(self,cmd, status="", result="", startTime=None, endTime=None, elapsedTime=None):
        
        """
        Logs the Message
        """
        
        log_sv_dir=os.path.join(self.logsdir,"testcase_log")
        if not (os.path.exists(log_sv_dir)):
            os.makedirs(log_sv_dir)
            
        if tc_id:
            log_file=os.path.join(log_sv_dir,"%s_log.txt"%(tc_id))
        
        else:
            
            log_file=os.path.join(log_sv_dir,"log.txt")
        
        
        #print " log_file : %s"%str(log_file)
        f = open(log_file, "a")
        f.write("#"*100 + "\n")
        if tc_id and os.path.exists(os.path.join(log_sv_dir,"log.txt")):
            fl=open(os.path.join(log_sv_dir,"log.txt"))
            init_log_data=fl.read()
            fl.close()
            if os.path.exists(os.path.join(log_sv_dir,"log.txt")):
                os.remove(os.path.join(log_sv_dir,"log.txt"))
            f.write(init_log_data + "\n")

        if cmd:
            #print " cmd : %s"%str(cmd)
            f.write(str(cmd) + "\n")
        
        if result != "":
            f.write("Result: ")
            f.write("\n".join(result) if isinstance(result, list) else result + "\n")
    
        if status != "":
            f.write("\n Status: %s \n" % status)
    
        if elapsedTime is not None:
            f.write("\n")
            f.write("Elapsed Time: %s" % elapsedTime + "\n")
            f.write("Start Time: %s" % startTime + "\n")
            f.write("End Time: %s" % endTime + "\n")
        f.write("#"*100 + "\n\n")
        f.close()
        
    def teardownServices(self, deploymentRefId):
        """
        Teardown to the existing Deployment
        """
        url = self.buildUrl("Deploy", deploymentRefId)
        self.log_data( "printing deploy Teardown  url")
        self.log_data(url)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "DELETE", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data("Printing headers information")
        self.log_data(headers)
        
        startTime = datetime.datetime.now()
        response = requests.delete(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'DELETE',headers, ''), response.status_code, response.content, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        
        return response, True
    
    
    def getAllDeploymentId(self):
        """
        Gets the Deployment Reference Id  
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy")
        logger.info("printing deploy url")
        logger.info(url)
        uri = globalVars.serviceUriInfo["Deploy"]
        headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data("Printing headers information")
        self.log_data(headers)
        
        startTime = datetime.datetime.now()
        response = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ''), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        
        data = json.loads(response.text)
        result =self.convertUTA(data)
                   
        return result, True
    
    def getRefIDForDevice(self,deviceType,ipAddress):
        """
        get the refID of ManageDevice
        """
        logger = self.getLoggerInstance()
        logger.info(' IN getRefIDManageDevice : ') 
        logger.info('get  of ManageDevice : ')
        resMD,statMD = self.getResponse("GET", "ManagedDevice")
        #response = self.convertResponse(resMD)
        refId=""
        for device in resMD:
            if device['deviceType']==deviceType and device['ipAddress'] == ipAddress:
                refId = device['refId']
                self.log_data(" refID of device : ")
                self.log_data(refId)
                
        return refId

    '''
        ##################################################################
        <-- Functions to handle creation of OS/Firmware Repositories Start
        ##################################################################
    '''
        
    def createRepoFromNFS(self):
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.firmwareRepoPayload)
        payload = payload.replace("$sourceLocation",RepositoryParamFile.nfsSourceLocation)
        logger.info("Going to POST NFS Repository : ")
        response,status = self.getResponse("POST", "FirmwareRepository", payload)
        logger.debug("====== RESPONSE : ===== ") 
        print "====== RESPONSE : ===== "
        print response
        logger.debug(response)
        return response,status
    
    def getAllFWScomponent(self):
        logger = self.getLoggerInstance()
        logger.info("Going to GET all FW repository software component: ")
        response,status = self.getResponse("GET", "FWRepoSoftwareComponent")
        logger.debug("====== RESPONSE : ===== ") 
        print "====== RESPONSE : ===== "
        print response
        logger.debug(response)
        return response,status    
        
        
    def createRepoFromCFS(self):
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.firmwareRepoPayload)
        payload = payload.replace("$sourceLocation",RepositoryParamFile.cfsSourceLocation)
        logger.info("Going to POST CFS Repository : ")
        response,status = self.getResponse("POST", "FirmwareRepository", payload)
        logger.debug(" ====== RESPONSE : =====  : ")
        logger.debug(response)
        return response,status
        
        
    def createRepoFromLocalDrive(self):
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.firmwareLocalDrive)
        payload = payload.replace("$diskLocation",RepositoryParamFile.disklocation)
        logger.info(" Going to POST repository from local drive : ")
        response,status = self.getResponse("POST", "FirmwareRepository", payload)
        logger.debug(" ====== RESPONSE : =====  : ")
        logger.debug(response)
        return response,status

    '''
        ##################################################################
        Functions to handle creation of OS/Firmware Repositories END -->
        ##################################################################
    '''
        

    
    '''
        #########################################################
        <-- Function for tearing down individual resources Start 
        #########################################################
    '''
    def tearDownDevices(self,deploymentRefId,payload):
        logger = self.getLoggerInstance()
        logger.info(" Going To Tear Down resources...")
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headersPut=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( " Printing Headers for PUT : ")
        self.log_data(headersPut)
        startTime = datetime.datetime.now()        
        response = requests.put(url, data=payload, headers=headersPut, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headersPut,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        return response
    
    def deleteService(self,deploymentRefId,payload):
        logger = self.getLoggerInstance()
        logger.info(" Going To delete service...")
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headersPut=self.generateHeaderTearDown(uri, "DELETE", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data( " Printing Headers for PUT : ")
        self.log_data(headersPut)
        startTime = datetime.datetime.now()        
        response = requests.delete(url, data=payload, headers=headersPut, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'DELETE',headersPut,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        return response
    
    def getResponseFromDeploymentName(self,deplName):
        
        #Get Deployment Id
        logger = self.getLoggerInstance()
        deploymentRefId = self.getDeploymentId(deplName)
        logger.info(" Deployment Name : ")
        logger.info(deplName)
        logger.info( " Deployment Ref ID : ")
        logger.info(deploymentRefId)
        url = self.buildUrl("Deploy",deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        return deploymentResponse,deploymentRefId
    
    
    def generateHeaderTearDown(self,uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uri + ":" + userAgent + ":" + timestamp
        signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/xml","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers
    
    
    
    
    '''
        #########################################################
        Function for tearing down individual resources End --> 
        #########################################################
    '''
    
    def getReqResource(self, limit=1, resourceType="SERVER", deviceType=None):
        """
        Gets resources from Resource Info
        """        
        #utility.log_data("", globalVars.resourceInfo)
        resources = []
        
        #=======================================================================
        # if resourceType == "SERVER":
        #     #serverlist = ["64WF3W1","G2WF3W1","97QR1V1"]
        #     serverlist = ["97QR1V1","J4WF3W1","F2WF3W1"]
        #     for resource in globalVars.resourceInfo[resourceType]:
        #         if resource["serviceTag"] in serverlist:
        #             resources.append(resource)
        # else:
        #=======================================================================
        resources = globalVars.resourceInfo[resourceType]        
        if deviceType:
            resList = [resource for resource in resources if deviceType in str(resource["deviceType"]).lower()]
        else:
            resList = resources
        if len(resList) > limit:
            return resList[:limit]
        else:
            return resList   


    def getResources(self):
        """
        Gets all Resources available
        """
        resMD, statMD = self.getResponse("GET", "ManagedDevice")
        if not statMD:
            #self.failure("","Failed to get Resource Info %s"%str(resMD), self.BLOCKED)
            print " Failed to get ResourceInfo %s"%str(resMD)
        resServer = []
        resStorage = []
        resVCenter = []
        resChassis = []
        resVM = []
        resComp = []
        resswitch =[]
        resElementMgr =[]
        for resource in resMD:
            
            if "server" in str(resource["deviceType"]).lower():
                resServer.append({"ip":resource["ipAddress"],"refId":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"], "model":resource["model"]})
            elif "equallogic" in str(resource["deviceType"]).lower():
                resStorage.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"], "model":resource["model"]})
            elif "vcenter" in str(resource["deviceType"]).lower():
                resVCenter.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"], "model":''})
            elif "chassis" in str(resource["deviceType"]).lower():
                resChassis.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"], "model":resource["model"]})
            elif "scvmm" in str(resource["deviceType"]).lower():
                resVM.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"], "model":''})
            elif "compellent" in str(resource["deviceType"]).lower():
                resComp.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"], "model":resource["model"]})
            elif "switch" in str(resource["deviceType"]).lower():
                resswitch.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"], "model":resource["model"]})
            elif "em" in str(resource["deviceType"]).lower():
                resElementMgr.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"]}) 
            else:
                self.log_data(str(resource["deviceType"]).lower())
                
  
            
        globalVars.resourceInfo["SERVER"] = resServer
        globalVars.resourceInfo["STORAGE"] = resStorage
        globalVars.resourceInfo["VCENTER"] = resVCenter
        globalVars.resourceInfo["CHASSIS"] = resChassis
        globalVars.resourceInfo["SCVMM"] = resVM
        globalVars.resourceInfo["COMPELLENT"] = resComp
        globalVars.resourceInfo["SWITCH"] = resswitch
        globalVars.resourceInfo["ElementManager"] = resElementMgr
        
        return resServer, resStorage, resVCenter, resChassis, resVM, resComp, resswitch, resElementMgr  
        
        
        
    def getManagedDeviceRefID(self,refId):
        
        logger = self.getLoggerInstance()
        logger.info( " Ref ID : ")
        logger.info(refId)
        url = self.buildUrl("ManagedDevice",refId)
        uri = globalVars.serviceUriInfo["ManagedDevice"]+ "/" + refId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        
        return deploymentResponse
    
    
    def setCompleteWizard(self):
        """
        Sets the Wizard Status
        """
        payload = self.readFile(globalVars.completeWizardPayload)   
        resWC, statWC = self.getResponse("PUT", "Wizard", payload)
        
        return resWC, False
    
    def getHostName(self):
        """
        Returns Host Name
        """
        hostName = "HCL" + datetime.datetime.now().strftime("%d%H%M%S")
        time.sleep(1)
        self.log_data("Host Name", hostName)
        return hostName

    def getHostGroupName(self):
        """
        Returns Host Group Name
        """
        hostGropName = "HHG" +str(randrange(10, 10000, 2))
        self.log_data("Host Group Name", hostGropName)
        return hostGropName
    
    def getVolumeName(self):
        """
        Returns Volume Name
        """
        VolumeName = "HCLVol" +str(randrange(10, 10000, 2))
        self.log_data("Volume Name", VolumeName)
        return VolumeName
    
    def getClusterName(self):
        """
        Returns Cluster Name
        """
        clusterName = "HCLCluster" +str(randrange(10, 10000, 2))
        self.log_data("Cluster Name", clusterName)
        return clusterName
    
    def getDataCenterName(self):
        """
        Returns DataCenterName Name
        """
        dataCenterName = "HCLhost" +str(randrange(10, 10000, 2))
        self.log_data(" DataCenterName Name", dataCenterName)
        return dataCenterName
    
    def getVMHostName(self):
        """
        Returns VM Host Name
        """
        vmHostName = "HCLVM" + datetime.datetime.now().strftime("%d%H%M%S")
        time.sleep(1)
        self.log_data("VM Host Name", vmHostName)
        return vmHostName
    
    def getPaylodGeneral(self,refID=""):
        self.log_data("getPaylod...")
        headers = {"Accept":"application/xml","Content-Type":"application/xml", "Accept-Encoding":"gzip"}
        if refID:
            uri="http://172.31.41.1:9080/AsmManager/addOnModule/"+refID
        else:
            uri="http://172.31.41.1:9080/AsmManager/addOnModule"
        response = requests.get(uri, headers=headers)
        f = open("Z:\\Users\\raj.patel\\Desktop\\Workspace\\CLI_DH_31Jan17_old\\AddOnModFinal.xml","a")
        f.write((response.content).encode('utf-8'))
#         data = json.loads(response.text)
#         data=self.convertUTA(data)
#         f.write(data.text.encode('utf-8'))
        return response.content

    def createOSRepository(self,imageType,name,sourcePath, userName=None,password=None):
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.repoTestPayload)
        payload = payload.replace("$sourcePath",sourcePath).replace("$name",name).replace("$imageType",imageType)
        
        if sourcePath.startswith('ftp') or ':' in sourcePath:
            pass
        else:
            if userName is not None and password is not None:
                credential = ('<username>'+userName+'</username><password>'+password+'</password>')
                payload = payload.replace("$credential",credential)
            else:
                payload = payload.replace("$credential",'')
        logger.info("Going to POST osRepository Payload : ")
        self.log_data("Going to POST osRepository with Payload : ", payload)
        response,status = self.getResponse("POST", "osRepository", payload)
        logger.debug(" ====== RESPONSE : =====  : ")
        logger.debug(response)
        statusRep = self.checkRepositoryState(response)
        if statusRep == True:
            print " ==== OS Repository created Successfully ===="
            self.log_data("Successfully created OS Repository")
        return statusRep
    
    def createOSRepositoryAndConnection(self,imageType,name,sourcePath, userName=None,password=None):
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.repoTestPayload)
        payload = payload.replace("$sourcePath",sourcePath).replace("$name",name).replace("$imageType",imageType)
        
        if sourcePath.startswith('ftp') or ':' in sourcePath:
            pass
        else:
            if userName is not None and password is not None:
                credential = ('<username>'+userName+'</username><password>'+password+'</password>')
                payload = payload.replace("$credential",credential)
            else:
                payload = payload.replace("$credential",'')
        logger.info("Going to POST osRepository Payload : ")
        self.log_data("Going to POST osRepository with Payload : ", payload)
        response,status = self.getResponse("POST", "osRepositoryconnection", payload)
        logger.debug(" ====== RESPONSE : =====  : ")
        logger.debug(response)
        statusRep = self.checkRepositoryState(response)
        if statusRep == True:
            print " ==== OS Repository created Successfully ===="
            self.log_data("Successfully created OS Repository")
        return statusRep
    



    def checkIfRepositoryExists(self,repName):
        response,status = self.getResponse("GET", "osRepository")
        retVal = False
        self.log_data( " repName = ")
        self.log_data(repName) 
        self.log_data( " Checking If Repository Exists ")
        for repos in response:
            self.log_data( " repository name :  ")
            self.log_data( repos["name"])
            if(repos["name"]==repName and repos["state"]=='available'):
                print " Repo exists in Repository list"
                self.log_data( "Repo exists in Repository list")
                retVal = True
                return retVal
            
        return retVal
    
    def syncRepository(self,userName=None,password=None):
        logger = self.getLoggerInstance()
        response,status = self.getResponse("GET", "osRepository")
        self.log_data( " repName = ")
        repName = RepositoryParamFile.OSReponametest
        self.log_data(repName) 
        self.log_data( " Checking If Repository Exists ")
        refId = ""
        for repos in response:
            self.log_data( " repository name :  ")
            self.log_data( repos["name"])
            if(repos["state"]=="errors"):
                refId=repos["id"]
                imageType = repos["imageType"]
                name = repos["name"]
                sourcePath = repos["sourcePath"]
                break
            else:
                self.log_data( "Repo not exists in Repository list")    
                
        payload = self.readFile(globalVars.repoTestPayloadput)
         
        payload = payload.replace("$sourcePath",sourcePath).replace("$name",name).replace("$imageType",imageType)
        
        if sourcePath.startswith('ftp') or ':' in sourcePath:
            pass
        else:
            if userName is not None and password is not None:
                credential = ('<username>'+userName+'</username><password>'+password+'</password>')
                payload = payload.replace("$credential",credential)
            else:
                payload = payload.replace("$credential",'')
        if(repos["state"]=="errors"):
            logger.info("Going to PUT osRepository Payload : ")
            self.log_data("Going to PUT osRepository with Payload : ", payload)
            response,status = self.getResponse("PUT", "osRepositorysync", payload,refId=refId)
            logger.debug(" ====== RESPONSE : =====  : ")
            logger.debug(response)
            if status == True:
                print " ==== OS Repository sync Successfully ===="
                self.log_data("Successfully sync OS Repository")
                return status
#             statusRep = self.checkRepositoryState(response)
#             if statusRep == True:
#                 print " ==== OS Repository modified Successfully ===="
#                 self.log_data("Successfully modified OS Repository")
#                 return statusRep 
            else:
                self.log_data("Not able to modified OS Repository")
                return False       
        else:
            self.log_data("There is no OS Repository in error state.One should be in error state for put..")
            self.log_TestData(["", "", "",str(self.tc_Id), 'There is no OS Repository in error state','Success','One should be in error state for put..'])
            return True
    
    def putRepository(self,userName=None,password=None):
        logger = self.getLoggerInstance()
        response,status = self.getResponse("GET", "osRepository")
        self.log_data( " repName = ")
        repName = RepositoryParamFile.OSReponametest
        self.log_data(repName) 
        self.log_data( " Checking If Repository Exists ")
        refId = ""
        for repos in response:
            self.log_data( " repository name :  ")
            self.log_data( repos["name"])
            if(repos["state"]=="errors"):
                refId=repos["id"]
                imageType = repos["imageType"]
                name = repos["name"]
                break
            else:
                self.log_data( "Repo not exists in Repository list")    
                
        payload = self.readFile(globalVars.repoTestPayloadput)
        if imageType == "redhat":
            sourcePath = RepositoryParamFile.OSRepsourcePathtest
        else:
            sourcePath = RepositoryParamFile.OSRepsourcePathput 
        payload = payload.replace("$sourcePath",sourcePath).replace("$name",name).replace("$imageType",imageType)
        
        if sourcePath.startswith('ftp') or ':' in sourcePath:
            pass
        else:
            if userName is not None and password is not None:
                credential = ('<username>'+userName+'</username><password>'+password+'</password>')
                payload = payload.replace("$credential",credential)
            else:
                payload = payload.replace("$credential",'')
        if(repos["state"]=="errors"):
            logger.info("Going to PUT osRepository Payload : ")
            self.log_data("Going to PUT osRepository with Payload : ", payload)
            response,status = self.getResponse("PUT", "osRepository", payload,refId=refId)
            logger.debug(" ====== RESPONSE : =====  : ")
            logger.debug(response)
            if status == True:
                print " ==== OS Repository modified Successfully ===="
                self.log_data("Successfully modified OS Repository")
                return status
#             statusRep = self.checkRepositoryState(response)
#             if statusRep == True:
#                 print " ==== OS Repository modified Successfully ===="
#                 self.log_data("Successfully modified OS Repository")
#                 return statusRep 
            else:
                self.log_data("Not able to modified OS Repository")
                return False       
        else:
            self.log_data("There is no OS Repository in error state.One should be in error state for put..")
            self.log_TestData(["", "", "",str(self.tc_Id), 'There is no OS Repository in error state','Success','One should be in error state for put..'])
            return True

        
    def checkRepositoryState(self,response):
        logger = self.getLoggerInstance()
        logger.info(" ==== Checking Repository State === ")
        state = response["state"]
        self.log_data("Repository State is  : %s"%str(state))
        repId = response["id"]
        statusCR = False
        count = 0
        while(count < 10):
            time.sleep(120)
            count = count + 1
            result,status = self.getResponse("GET", "osRepository", refId=repId)
            logger.debug(" ====== GET RESPONSE : =====  : ")
            logger.debug(result)
            stateRep = result["state"]
            self.log_data( " State after GET : ")
            self.log_data(state)
            if(stateRep.lower()=="errors"):
                logger.debug(" ERROR IN CREATING REPOSITORY : ")
                logger.debug(result) 
                break
                
            if(stateRep.lower()=="available"):
                statusCR = True
                break
        
        return statusCR 
    
    def createRepoFromFTPShare(self):
        logger = self.getLoggerInstance()
        payload = self.readFile(globalVars.firmwareRepoPayload)
        payload = payload.replace("$sourceLocation",RepositoryParamFile.ftpSourceLocation)
        logger.info("Going to POST CFS Repository : ")
        self.log_data("Going to POST Firmware Repository with Payload : ", payload)
        response,status = self.getResponse("POST", "FirmwareRepository", payload)
        logger.debug(" ====== RESPONSE : =====  : ")
        logger.debug(response)
        return response,status


    def getDeploymentResponsByID(self,deploymentRefId):
        
        logger = self.getLoggerInstance()
        logger.info( " Deployment Ref ID : ")
        logger.info(deploymentRefId)
        url = self.buildUrl("Deploy",deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        return deploymentResponse
    
    def deleteDeployedService(self, deploymentRefId):
        
        deploymentResponse = self.getDeploymentResponsByID(deploymentRefId)
        payload = deploymentResponse.content
        
        self.log_data( 'Payload of Cleaning of Service is : %s'%str(payload))
        response = self.deleteService(deploymentRefId, payload )
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))
        print " Deployment REF ID After PUT in cleanDeployService : "
        print deploymentRefId
        if response.status_code in (200, 201, 202, 203, 204):
            #Validate Deployment Completion
            loop = 60
            while loop:
                try:
                    time.sleep(60)
                    resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                    if not statDS:
                        return resDS, True
                        break
                    if resDS.lower() in ("in_progress"):
                        time.sleep(120)
                    else:
                        return resDS, True
                        break
                except Exception as e1:
                    return str(e1), True
                    self.log_data(str(e1))
                    break
                       
        
                loop -= 1
        
        else:
            self.log_data( "Failed to Delete Resources for the deployment RefID : %s"%deploymentRefId)
            return "Failed to Delete Resources", False
        
    def cleanDeployedService(self, deploymentRefId):
        
        deploymentResponse = self.getDeploymentResponsByID(deploymentRefId)
        payload = deploymentResponse.content
        
        payload= payload.replace("<teardown>false</teardown>", "<teardown>true</teardown>")
        payload= payload.replace("<id>ensure</id><value>present</value>", "<id>ensure</id><value>absent</value>")
        #.replace("<retry>false</retry>", "<retry>true</retry>")
        
        self.log_data( 'Payload of Cleaning of Service is : %s'%str(payload))
        response = self.tearDownDevices(deploymentRefId, payload )
        logger = self.getLoggerInstance()
        logger.debug('Cleaning Deployed  Services  Response is')
        logger.info(response)
        logger.info(response)
        self.log_data("Cleaning Deployed  Services  Response is :%s"%str(response))
        print " Deployment REF ID After PUT in cleanDeployService : "
        print deploymentRefId
        if response.status_code in (200, 201, 202, 203, 204):
            #Validate Deployment Completion
            loop = 60
            while loop:
                try:
                    time.sleep(60)
                    resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                    if not statDS:
                        return resDS, True
                        break
                    if resDS.lower() in ("in_progress"):
                        time.sleep(120)
                    else:
                        return resDS, True
                        break
                except Exception as e1:
                    return str(e1), True
                    self.log_data(str(e1))
                    break
                       
        
                loop -= 1
        
        else:
            self.log_data( "Failed to Delete Resources for the deployment RefID : %s"%deploymentRefId)
            return "Failed to Delete Resources", False
    

    def getCleanuptStatus(self, refId):
        """
        Gets the Cleaneup Deployment Status
        """
        resDI, statDI = self.getAllResponse("GET", "Deploy", refId=refId, partialLog=True, pattern="\"status\":\"(\w+)\"")
        if not statDI:
            return "Unable to fetch Deployment Status: %s"%resDI, statDI
        return resDI["status"], True  
    
    
    def getDeploymentStatus(self, refId):
        """
        Gets the Deployment Status
        """
        logger = self.getLoggerInstance()
        url = self.buildUrl("Deploy", refId)
        logger.info("printing deploy url")
        logger.info(url)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + refId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data("Printing headers information")
        self.log_data(headers)
        statDS =False
        resultDS=''
        #response = requests.get(url, headers=headers)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.content, startTime, endTime, elapsedTime)
        
        if deploymentResponse.status_code in (200, 201, 202, 203, 204):
            
            root = ET.fromstring(deploymentResponse.content)
            indTD = root.find('status')
            resultDS = indTD.text
            statDS= True
            
#             data = json.loads(deploymentResponse.text)
#             result =self.convertUTA(data)
#             resultDS = result["status"]


            
        else:
            statDS =False
            resultDS = 'Not Found'
            
        
        #Change the jobSatatus to status in ASM 8.1
        #print result["jobStatus"]
        #return result["jobStatus"], True
        self.log_data("Deployment Job Status:%s"%str(resultDS))
        
        return resultDS, statDS 
    
    def manageResourceChassis(self, resource):
        """
        Create Device in Inventory, return array of Managed Devices created
        """
        chassisCredentialId = ""
        creName = resource["CredentialName"]
        chassisServerCre = resource["ChassisServerCre"]
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
        failedList = []
        time.sleep(globalVars.defReqWaitTime)        
        resMD, statMD = self.getResponse("GET", "Chassis")
        if not statMD:
            return resMD, statMD                  
        devices = []
        for chassis in resMD:        
            devices.extend(chassis["ioms"])
            for device in devices:
                refId = ""
                if device["supported"]:
                    if "I/O" in device["model"]:
                        refId = device["id"]
                    resMD, statMD = self.getResponse("GET", "ManagedDevice", refId = refId)
                    if statMD:
                        globalVars.ipMap[device["managementIP"]] = refId
                    else:
                        payload = self.readFile(globalVars.manageDevicePayload)                        
                        if not globalVars.ipMap.has_key(device["managementIP"]):                    
                            for k,v in globalVars.mdChassisParameters.items():           
                                payload = payload.replace(k, device[v])
                            if "I/O" in device["model"]:
                                payload = payload.replace("device_type","dellswitch")
                                payload = payload.replace("device_creid", switchCredentialId)
                            else:
                                payload = payload.replace("device_type","BladeServer")
                                payload = payload.replace("device_creid", serverCredentialId)
                            payload = payload.replace("device_refid", refId)
                            payload = payload.replace("login_user", self.loginUser)
                            payload = payload.replace("device_state", "DISCOVERED")
                            payload = payload.replace("device_vendor", "Dell Inc")
                            payload = payload.replace("login_user", self.loginUser)
                            resMD, statMD = self.getResponse("POST", "ManagedDevice", payload)
                            if not statMD:
                                failedList.append(resMD)
                            else:
                                for device in resMD:
                                    globalVars.ipMap[device["ipAddress"]] = device["refId"]
        if failedList:
            return failedList, False
        else:
            globalVars.ipMap, True 
            
            
            
    def getIPRange(self, startIP, endIP):
        startIP = self.ip_to_number(startIP)
        endIP = self.ip_to_number(endIP)
        ipList = []
        for i in range(startIP, endIP+1):
            curIP = str(self.number_to_ip(i))
            ipList.append(curIP)            
        return ipList
    
    
    def getCSVHeader(self):
        
        self.log_TestData(["Appliance","Build", "Flow", "Test CaseID","Test Case Description", 'Test Result','Description','Servers Details', "Deployment LogPath", "Deployment Description" ])
        self.log_TestData([globalVars.configInfo['Appliance']['ip'], globalVars.configInfo['Appliance']['build'],globalVars.configInfo['Appliance']['flow'],"", "", "","","",""])

  
    
    def getFirmwareRepositoryResponsByID(self,refId):
        
        logger = self.getLoggerInstance()
        logger.info( " firmwareRepository Ref ID : ")
        logger.info(refId)
        url = self.buildUrl("FirmwareRepository",refId)
        uri = globalVars.serviceUriInfo["FirmwareRepository"]+ "/" + refId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        return deploymentResponse
    
    
    def setDefaultCatalogTrue(self,refId):
        logger = self.getLoggerInstance()
        time.sleep(60)
        repositoryResponse = self.getFirmwareRepositoryResponsByID(refId)
        payload = repositoryResponse.content
        
        payload= payload.replace("<defaultCatalog>false</defaultCatalog>", "<defaultCatalog>true</defaultCatalog>")
        
        self.log_data( 'Payload of Cleaning of Service is : %s'%str(payload))
        
        
        logger.info(" Going To set Default Catalog to True...")
        url = self.buildUrl("FirmwareRepository", refId)
        uri = globalVars.serviceUriInfo["FirmwareRepository"]+ "/" + refId
        headersPut=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        self.log_data(" Printing Headers for PUT : ")
        self.log_data(headersPut)
        startTime = datetime.datetime.now()        
        response = requests.put(url, data=payload, headers=headersPut, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        time.sleep(30)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headersPut,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        return response

      
    def setDefaultCatalogFalse(self,refId):
        logger = self.getLoggerInstance()
        time.sleep(60)
        
        resMD, statMD = self.getResponse("GET", "FirmwareRepository")
        for resource in resMD:
            refId1=resource["id"]
            if refId == refId1:
                self.setDefaultCatalogTrue(refId)
            else:
                repositoryResponse = self.getFirmwareRepositoryResponsByID(refId1)       
                
                payload = repositoryResponse.content
        
                payload= payload.replace("<defaultCatalog>true</defaultCatalog>", "<defaultCatalog>false</defaultCatalog>")
                payload= payload.replace("<defaultCatalog>false</defaultCatalog>", "<defaultCatalog>false</defaultCatalog>")
        
                self.log_data( 'Payload of Cleaning of Service is : %s'%str(payload))
        
        
                logger.info(" Going To set Default Catalog to True...")
                url = self.buildUrl("FirmwareRepository", refId1)
                uri = globalVars.serviceUriInfo["FirmwareRepository"]+ "/" + refId1
                headersPut=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
                startTime = datetime.datetime.now()        
                response = requests.put(url, data=payload, headers=headersPut, verify=False)
                endTime = datetime.datetime.now()
                elapsedTime="%s"%(endTime-startTime)
                time.sleep(30)
                self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headersPut,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        
 
    
    def jarWrapper(self, *args):
        process = Popen(['java', '-jar']+list(args), stdout=PIPE, stderr=PIPE)
        ret = []
        while process.poll() is None:
            line = process.stdout.readline()
            if line != '' and line.endswith('\n'):
                ret.append(line[:-1])
        stdout, stderr = process.communicate()
        ret += stdout.split('\n')
        if stderr != '':
            ret += stderr.split('\n')
        ret.remove('')
        return ret
    
    def runWebServiceAPI(self, testCaseId, testCaseExecutionStatus, testCaseExecutionComments):
        
        try:
            testCaseId =  testCaseId.strip()
            args = ["../../util/QMetryWSDLClient.jar", testCaseId, testCaseExecutionStatus, testCaseExecutionComments] # Any number of args to be passed to the jar file
            #args = ['QMetryWSDLClient.jar', '102167', 'pass', "executed by Web Service API"] # Any number of args to be passed to the jar file
            results = self.jarWrapper(*args)
            
            self.log_data( 'Result  of updating qmetry with Web Service API is :')
            resulsfinal =''
            for result in results:
                resulsfinal = resulsfinal + '\n' + result.strip()
            self.log_data(resulsfinal)
                
                    
                
            
        except Exception as e1:
            self.log_data( 'Exception occurred while  updating qmetry with Web Service API ')
            self.log_data(str(e1))
        
        
    def getServerIPAddress(self, deploymentRefId):
        serverIP=""
        self.log_data("deploymentRefId is  : ", deploymentRefId)
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()        
        response = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        time.sleep(30)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers,""), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        data = json.loads(response.text)
        result =self.convertUTA(data)
        self.log_data( " ********************** RESULT of DEployment ************************")
        self.log_data( " [[[[[[  DEVICE LIST ]]]]] ")
        self.log_data( result['deploymentDevice'])
        self.log_data("deploymentDevice in  Deployment  : ", result['deploymentDevice'])
        for device in result['deploymentDevice']:
            if device['refType']=='SERVER':
                serverIP = device['ipAddress']
                
        self.log_data( " <------ SERVER IP ADDRESS ------> ")
        self.log_data(serverIP)
        self.log_data("SERVER IP ADDRESS for RAID Validation  : ", serverIP)
        return serverIP
        
    def createExportXML(self,serverIP):
        ApplianceIP = globalVars.configInfo['Appliance']['ip']
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        if not serverIP:
            self.log_data( " Server IP not present .... Quitting RAID Check ")
            return
        self.log_data("<---------GOING TO CHECK RAID VALUE --------> ")
        self.log_data("APPLIANCE IP : %s" %str(ApplianceIP))
        self.log_data("SERVER IP : %s" %str(serverIP))
        ssh.connect(ApplianceIP, username='delladmin',password='delladmin')
        command = 'wsman invoke http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/root/dcim/DCIM_LCService?SystemCreationClassName="DCIM_ComputerSystem",CreationClassName="DCIM_LCService",SystemName="DCIM:ComputerSystem",Name="DCIM:LCService" -h '+ serverIP +' -V -v -c dummy.cert -P 443 -u root -p calvin -a ExportSystemConfiguration -k "IPAddress='+ApplianceIP+'"'+' -k "ShareName=/var/nfs/" -k "ShareType=0" -k "FileName=export.xml" -k "ExportUse=1"'
        self.log_data( " COMMAND to create export xml : %s"%str(command))
        ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command(command)
        
        self.log_data("\nstdout is:\n" + ssh_stdout.read())
        self.log_data("ssh_stdin is  : ", ssh_stdin.read())
        self.log_data("ssh_stderr is   : ", ssh_stderr.read())
        time.sleep(60)
        
        sftp_client = ssh.open_sftp()
        sftp_client.chdir('/var/nfs')
        remotepath = 'export.xml'

        xmlFile=open(globalVars.exportxml,'w+')
        #time.sleep(5)
        localpath = globalVars.exportxml
        if os.path.isfile(localpath):
            sftp_client.get(remotepath,localpath)
            xmlFile.close()
        else:
            raise IOError('Could not find localFile %s !!' % localpath)
                  
        
            
    def performRAIDValidation(self): 
        fileData = self.readFile(self,globalVars.exportxml)
        root = ET.fromstring(fileData)
        foundRAID = False
        foundDisk = False
        foundRaidType = False
        for component in root.findall('Component'):
            fqdd = component.get('FQDD')
            #print fqdd
            if "RAID.Integrated" in fqdd:
                root1 = component
                foundRAID = True
                print " Component  Attribute : %s" %str(fqdd)
                self.log_data("Component FQDD  is:\n" +fqdd)
        if foundRAID == False:
            print " RAID.Integrated value not found in component "
            self.log_data(" RAID.Integrated value not found in component ")
            return False
        for comp in root1.findall('Component'):
            fqdd1 = comp.get("FQDD")
            if "Disk.Virtual" in fqdd1:
                root2 = comp
                foundDisk = True
                print fqdd1
                self.log_data('Component FQDD is : \n ' +fqdd1)
            if foundDisk == False:
                print " Disk.Virtual value not found in component "
                self.log_data(" Disk.Virtual value not found in component  ")
                break
            else:
                print " Checking for RAID Level : "
                for attribute in root2.iter('Attribute'):
                    name = attribute.get('Name')
                    if name=="RAIDTypes":
                        print " Printing Raid VAlue : "
                        print attribute.text
                        foundRaidType = True
                        self.log_data(' RAID Value : '+attribute.text)  
                if foundRaidType == False:
                    print " RAID Level not defined "
                    self.log_data(" RAID Level not defined ")

    def executeSudoCommand(self, command):
        
        applianceIP = globalVars.configInfo['Appliance']['ip']
        #command = 'service iptables stop'
        sudo = True
        password = 'delladmin'
        username='delladmin'  
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.connect(applianceIP, port=22, username='delladmin', password='delladmin')
        feed_password = False
        if sudo and username != "root":
            command = "sudo -S -p '' %s" % command
            self.log_data("Command  to Execute : ", command)
            feed_password = password
            #self.password is not None and len(self.password) > 0
        stdin, stdout, stderr = ssh.exec_command(command)
        if feed_password:
            print " password : "
            print feed_password
            stdin.write('delladmin' + "\n")
            stdin.flush()
        ret= {'out': stdout.readlines(), 
              'err': stderr.readlines(),
              'retval': stdout.channel.recv_exit_status()}
        print "  ".join(ret["out"]), "  E ".join(ret["err"]), ret["retval"]
        self.log_data("Command  output : ", "  ".join(ret["out"]))


    def getOsHostName(self, deploymentRefId):
        
        self.log_data("deploymentRefId is  : ", deploymentRefId)
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()        
        response = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        time.sleep(30)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers,""), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        data = json.loads(response.text)
        result =self.convertUTA(data)
        result1 = result['serviceTemplate']
        result2 =  result1['components']
        for resources in result2:
            result3 = resources['resources']
            for parmeter in result3:
                result4 = parmeter['parameters']
                for p in result4:
                    if p['id'] == "os_host_name":
                        self.log_data( 'Value of os_host_name  for the Deployment refID  %s'%deploymentRefId +'  is  \t : %s'%p['value'])
                        print 'Value of os_host_name  for the Deployment refID  %s'%deploymentRefId +'  is  \t : %s'%p['value']



    def generateHeaderforDiscoverChassis(self, uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uri + ":" + userAgent + ":" + timestamp
        signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/json","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers


    def getJobExecutionStatusByJobID(self, jobID):
        
        wait = 10
        jobhistoryelaspedTime= ""
            
        while wait:
            jobtime, jobStatus = self.getjobhistoryelaspedTime(jobID)
            if jobStatus.lower() in ("in_progress"):
                time.sleep(120)
            else:
                if jobStatus.lower() in ("successful"):
                    jobhistoryelaspedTime= jobtime
                    self.log_data( 'JRAF jobhistory execution elaspedTimeMillis : %s'%jobtime) 
                    self.log_data( 'JRAF jobhistory execution status : %s'%jobStatus)  
                    break
                else:
                    jobhistoryelaspedTime= jobtime
                    self.log_data( 'JRAF jobhistory execution elaspedTimeMillis : %s'%jobtime) 
                    self.log_data( 'JRAF jobhistory execution status : %s'%jobStatus)
                    break
            wait = wait -1
        
        return jobhistoryelaspedTime
        

                        

    def getjobhistoryelaspedTime(self, jobID):
        
        elaspedTime=0
        jobStatus=""
        try:
            self.log_data("jobName is  :  %s"%jobID)
            url = self.buildUrljob("JobStatus", jobID)
            uri = globalVars.serviceUriInfo["JobStatus"] + jobID
            headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            startTime = datetime.datetime.now()        
            response = requests.get(url, headers=headers, verify=False)
            endTime = datetime.datetime.now()
            elapsedTime="%s"%(endTime-startTime)
            time.sleep(30)
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers,""), response.status_code, response.text, startTime, endTime, elapsedTime)
            data = json.loads(response.text)
            result =self.convertUTA(data)
            result1 = result['trees']
            for resources in result1:
                elaspedTime = resources['data']['execHistory']['elaspedTimeMillis']
                jobStatus = resources['data']['execHistory']['status']
                
        except Exception as e3:
            self.log_data( 'Exception occurred while  getting Job execution History elaspedTimeMillis ')
            self.log_data(str(e3))

            
        return elaspedTime, jobStatus 
           
            
                

            
        
    def buildUrljob(self, feature, refId=None):
        """
        Builds a Service Url and Returns 
        """
        #print globalVars.configInfo
        #print globalVars.serviceUriInfo
        basePath = "http://"+  globalVars.configInfo['Appliance']['ip'] + ":" + globalVars.configInfo['Appliance']['port']
        #basePAth=http://i.p/9080
        uri = globalVars.serviceUriInfo[feature]
        #uri = /admin/authenticate
        #print uri
#        print basePath
        if refId:
            return basePath +  uri + refId
        else:
            return basePath +  uri
        
    
        
    def convertMillistoHumanTime(self, ms):
        milliseconds = ms %1000
        seconds=(ms/1000)%60
        minutes=(ms/(1000*60))%60
        hours=(ms/(1000*60*60))%24
   
        return "%d:%02d:%02d:%03d" % (hours, minutes, seconds, milliseconds)
    
    
    def getDiscoveryStatusByRefID(self, refId):
        
        
        url = self.buildUrl("Discovery", refId)
        uri = globalVars.serviceUriInfo["Discovery"]+ "/" + refId
        headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()        
        response = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        time.sleep(30)
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers,""), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        return response

    def authenticateForWorkflow(self,userName,Passwd):
        uri = self.buildUrl("login")
        logger = self.getLoggerInstance()
        logger.info(' Going to Login at ' )
        logger.debug(uri)
        postData = self.getWorkflowloginPayload(userName,Passwd)
        headers = {'content-type' : 'application/xml'}
        print postData
        startTime = datetime.datetime.now()
        response = requests.post(uri,data=postData,headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        
        self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(uri,"POST",headers,postData), response.status_code, response.text, startTime, endTime, elapsedTime)
        #globalVars.userAgent = str(response.request.headers["User-Agent"])
        
        self.log_data("printing user agent value")
        self.log_data(globalVars.userAgent)
        time.sleep(globalVars.defaultWaitTime)
        loginResponse  = json.loads(response.content)
        logger.info('apiKey is ') 
        logger.debug(loginResponse.get('apiKey'))
        logger.info(' Username is ') 
        logger.debug(loginResponse.get('userName'))
        logger.info(' domain is ') 
        logger.debug(loginResponse.get('domain'))
        logger.info(' role is ')
        logger.debug(loginResponse.get('role'))
        logger.info(' apiSecret is ') 
        logger.debug(loginResponse.get('apiSecret'))
        globalVars.apiKey= loginResponse.get('apiKey')
        logger.info(' Printing Auth key ')
        logger.debug(globalVars.apiKey)
        globalVars.apiSecret= loginResponse.get('apiSecret')
        logger.info(' Printing Secret key ')
        logger.debug(globalVars.apiSecret)
        return response.content

    
    def getWorkflowloginPayload(self,userName,Passwd):
        payload = self.readFile(globalVars.loginPayload)
        payload = payload.replace("$userName",userName).replace("$domain","ASMLOCAL").replace("$password",Passwd)
        return payload

    def getAllBundles(self,refId=""):
        
        #Get Deployment Id
        logger = self.getLoggerInstance()
        logger.info(" Firmware repo:")
        logger.info(refId)
        url = self.buildUrl("FirmwareRepository")
        url=url+"/"+refId+"?bundles=true"
        uri = self.buildHeaderUri("FirmwareRepository")
        uri = uri+"/"+refId+"?bundles=true"
        headers=self.generateSecurityHeader(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url, headers=headers, verify=False)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.text != "":
                data = json.loads(response.text)
                return self.convertUTA(data), True
            else:
                return str(response.status_code), False
        else:
            "Error please check URL",False
    
    def getDetails(self, action, requestType, payload="", refId=None):
        """
        Gets the Response by calling corresponding   
        """        
                
        url = self.buildUrl(requestType, refId=refId)
        logger = self.getLoggerInstance() 
        logger.info( "url") 
        logger.info(url)
        
        uri = self.buildHeaderUri(requestType, refId=refId)
        
        startTime = datetime.datetime.now()              
        if action == "POST":
            
            headers = self.generateSecurityHeader(uri, 'POST', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.post(url, data=payload, headers=headers,verify=False)
        elif action == "PUT":
            headers = self.generateSecurityHeader(uri, 'PUT', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.put(url, data=payload, headers=headers,verify=False)
        elif action == "DELETE":
            headers = self.generateSecurityHeader(uri, 'DELETE', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.delete(url, headers=headers,verify=False)
        else:
            headers = self.generateSecurityHeader(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.get(url, headers=headers,verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        if action == "GET":
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
            
        else:
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.text != "":
                try:
                    data = json.loads(response.text.encode('ascii', 'ignore').decode('ascii'))
                    return self.convertUTA(data), True
                except:
                    data = json.loads(response.text.encode('utf-8', 'ignore').decode('utf-8'))
                    return self.convertUTA(data), True
            else:
                if action == "PUT":
                    return response.text, True
                elif action == "GET":
                    return "No information found for %s" % requestType, False
                elif action == "DELETE":
                    return "No information found for %s" % requestType, True  
                elif (action == "POST") and (response.status_code in (200, 201, 202, 203, 204)):
                    return "No information found for %s" % requestType, True 
                else:
                    return "No information found for %s" % requestType, False
        elif response.status_code == 409:
            return str(response.content), True
        else:
            if response.status_code == 400:
                return str(response.status_code), False
            else:
                return str(response.status_code) + " " + str(self.convertUTA(response.text)), False
    
    def templateComponent(self, action, requestType, payload="", refId=None):
        """
        Gets the Response by calling corresponding   
        """        
                
        url = self.buildUrl(requestType, refId=refId)
        logger = self.getLoggerInstance() 
        logger.info( "url") 
        logger.info(url)
        
        uri = self.buildHeaderUri(requestType, refId=refId)
        
        startTime = datetime.datetime.now()              
        if action == "POST":
            
            headers = self.generateHeaderDeploy(uri, 'POST', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.post(url, data=payload, headers=headers,verify=False)
        elif action == "PUT":
            headers = self.generateHeaderDeploy(uri, 'PUT', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.put(url, data=payload, headers=headers,verify=False)
        elif action == "DELETE":
            headers = self.generateHeaderDeploy(uri, 'DELETE', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.delete(url, headers=headers,verify=False)
        else:
            headers = self.generateHeaderDeploy(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.get(url, headers=headers,verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        if action == "GET":
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
            
        else:
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.content, startTime, endTime, elapsedTime)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.content != "":
                try:
                    data = response.content
                    return data,True
                
                except:
                    data =response.content.encode('utf-8', 'ignore').decode('utf-8')
                    return data,True
            else:
                if action == "PUT":
                    return response.content, True
                elif action == "GET":
                    return "No information found for %s" % requestType, False
                elif action == "DELETE":
                    return "No information found for %s" % requestType, True  
                elif (action == "POST") and (response.status_code in (200, 201, 202, 203, 204)):
                    return "No information found for %s" % requestType, True 
                else:
                    return "No information found for %s" % requestType, False
        elif response.status_code == 409:
            return str(response.content), True
        else:
            if response.status_code == 400:
                return str(response.status_code), False
            else:
                return str(response.status_code) + " " + str(self.convertUTA(response.text)), False
    
    def getResponseByWithoutAuthentication(self, action, requestType, payload="", refId=None):
        """
        Gets the Response by calling without authentication   
        """        
                
        url = self.buildUrl(requestType, refId=refId)
        logger = self.getLoggerInstance() 
        logger.info( "url") 
        logger.info(url)
        
        uri = self.buildHeaderUri(requestType, refId=refId)
        
        startTime = datetime.datetime.now()              
        if action == "POST":
            
            headers = self.generateSecurityHeader(uri, 'POST', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.post(url, data=payload, headers=headers,verify=False)
        elif action == "PUT":
            headers = self.generateSecurityHeader(uri, 'PUT', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.put(url, data=payload, headers=headers,verify=False)
        elif action == "DELETE":
            headers = self.generateSecurityHeader(uri, 'DELETE', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.delete(url, headers=headers,verify=False)
        else:
            headers = self.generateSecurityHeader(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
#             headers = {"Accept":"application/json",'content-type' : 'application/xml'}
            response = requests.get(url, headers=headers,verify=False)
        
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        if action == "GET":
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
            
        else:
            self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text.encode('utf-8'), startTime, endTime, elapsedTime)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.text != "":
                try:
                    data = json.loads(response.text)
                    return self.convertUTA(data), True
                    
                except:
                    data = json.loads(response.text.encode('ascii', 'ignore').decode('ascii'))
                    return self.convertUTA(data), True
#                     data = json.loads(response.text.encode('utf-8', 'ignore').decode('utf-8'))
#                     return self.convertUTA(data), True
            else:
                if action == "PUT":
                    return response.text, True
                elif action == "GET":
                    return "No information found for %s" % requestType, False
                elif action == "DELETE":
                    return "No information found for %s" % requestType, True  
                elif (action == "POST") and (response.status_code in (200, 201, 202, 203, 204)):
                    return "No information found for %s" % requestType, True 
                else:
                    return "No information found for %s" % requestType, False
        elif response.status_code == 401:
            return str(response.content),response.status_code, True
        elif response.status_code == 403:
            return str(response.content),response.status_code, True
        elif response.status_code == 404:
            return str(response.content),response.status_code, True
        else:
            if response.status_code == 400:
                return str(response.status_code), False
            else:
                return str(response.status_code) + " " + str(self.convertUTA(response.text)), False
    
    def getPayloadInXML(self,action,requestType,refId):
        try:
                
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
                return "Not able to get device info", False  
        except Exception as e1:
            self.log_data("Exception occured")
            self.log_data(str(e1))
            return "Exception occured", False    
            
    def deleteUserfromTempOrService(self, action, requestType, payload="", refId=None):
        """
        Deleting the user from template or service   
        """        
        try:
                        
            url = self.buildUrl(requestType)
            url = url+refId
            logger = self.getLoggerInstance() 
            logger.info( "url") 
            logger.info(url)
            
            uri = self.buildHeaderUri(requestType)
            uri = uri+refId
            logger.info( "uri") 
            logger.info(uri)
            startTime = datetime.datetime.now()              
            
            headers = self.generateSecurityHeader(uri, 'DELETE', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.delete(url, headers=headers,verify=False)
            
            endTime = datetime.datetime.now()
            elapsedTime="%s"%(endTime-startTime)
            if action == "GET":
                self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
                
            else:
                self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text.encode('utf-8'), startTime, endTime, elapsedTime)
            time.sleep(globalVars.defReqWaitTime)
            if response.status_code in (200, 201, 202, 203, 204): 
                # requests.codes.OK:
                if response.text != "":
                    try:
                        data = json.loads(response.text)
                        return self.convertUTA(data), True
                        
                    except:
                        data = json.loads(response.text.encode('ascii', 'ignore').decode('ascii'))
                        return self.convertUTA(data), True
    
                else:
                    if action == "DELETE":
                        return "No information found for %s" % requestType, True  
                    
                    else:
                        return "No information found for %s" % requestType, False
        except Exception as e1:
            self.log_data("Exception occured")
            self.log_data(str(e1))
            return "Exception occured", False            
        
    def migrateServer(self, action, requestType, payload="", refId=None):
        """
        Migrating server from pool  
        """        
        try:
                        
            url = self.buildUrl(requestType, refId=refId)
            logger = self.getLoggerInstance() 
            logger.info( "url") 
            logger.info(url)
            
            uri = self.buildHeaderUri(requestType, refId=refId)
            
            startTime = datetime.datetime.now()              
            if action == "PUT":
                headers = self.generateSecurityHeader(uri, 'PUT', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
                response = requests.put(url, data=payload, headers=headers,verify=False)
            else:
                headers = self.generateSecurityHeader(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
                response = requests.get(url, headers=headers,verify=False)
            
            endTime = datetime.datetime.now()
            elapsedTime="%s"%(endTime-startTime)
            if action == "GET":
                self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
                
            else:
                self.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text.encode('utf-8'), startTime, endTime, elapsedTime)
            time.sleep(globalVars.defReqWaitTime)
            if response.status_code in (200, 201, 202, 203, 204): 
                # requests.codes.OK:
                if response.text != "":
                    data = json.loads(response.text)
                    return self.convertUTA(data), True
                            
                
            elif response.status_code == 404:
                return str(response.text), True
            else:
                return str(response.status_code), False
        except Exception as e1:
            self.log_data("Exception occured")
            self.log_data(str(e1))
            return "Exception occured", False        