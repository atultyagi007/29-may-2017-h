'''
Created on Jan 15, 2016

@author: Dheeraj.Singh
'''
import os
import time
import utility
import random
import traceback
import datetime
from copy import deepcopy
from libs.product import globalVars
from libs.core.dellunit.case import DellTestCase
from libs.core.dellunit.unittest2 import SkipTest
import paramiko
from libs.product import requests
import json
import xml.etree.ElementTree as ET
import base64
import hashlib
import hmac
from random import randrange


BLOCKED, ERROR, FAILURE, OMITTED, SUCCESS = range(1, 6)   
STATUS = {BLOCKED : "BLOCKED", 
          ERROR : "ERROR", 
          FAILURE : "FAILURE", 
          OMITTED : "OMITTED", 
          SUCCESS : "PASSED"}

COLOR = {"PASSED" : "#ffffff",
         "FAILURE" : "#ff9999",
         "ERROR" : "#ff0000", 
         "BLOCKED" : "#ff5999",
         "OMITTED" : "#ddeeff",
         "DESC": " #FE9A2E",
         } 

STATUS_HTML_HEADER_TMPL = """<html><body><table class="heading_table" border=1> <tr align=center bgcolor=#A4A4A4> <th> Steps </th> <th> Procedure </th> <th> Description </th><th> Status </th></tr>\n""" 
STATUS_HTML_TMPL = """<tr align=center bgcolor=%s> <td> Step %s </td> <td> %s </td> <td> %s </td><td> %s </td></tr>\n"""
STATUS_HTML_DESC_TMPL = """<tr align=center bgcolor=%s> <td colspan=4> %s </td> </tr>\n"""
FINAL_STATUS_HTML_TMPL = """</table>\n\n<br><br><TABLE class="heading_table" border=1><tr align=center bgcolor=%s> <td> Final Status </td> <td> %s </td> <td> %s </td> </tr></TABLE>\n"""


class TestBase(DellTestCase):
    """
    Baseclass with all Product Level functions
    """    
    
    
    scaleUPEnable = False
    finalScaleupStoragePayload=""
    finalScaleupServerPayload=""
    finalScaleupClusterPayload=""
    finalScaleupVMPayload=""
    finalScaleupApplicationPayload=""
    networkConfValue=""
    
    def __init__(self, *args, **kwargs):
        """
        Initialization        
        """
        DellTestCase.__init__(self, "test_functionality")
        self.tcID = args[0]
        self.iteration = utility.iteration
        self._step_results = []
        self.msg = []
        self.finalStatus = SUCCESS
        self.FAILED = False
        self.applianceIP = globalVars.configInfo['Appliance']['ip']
        self.loginUser = globalVars.configInfo['Appliance']['username']
        self.loginPassword = globalVars.configInfo['Appliance']['password']  
        self.delResource = globalVars.configInfo['Appliance']['del_resource']
        execLogLoc = os.path.join(utility.logsdir, "exec_log")
        self.xmlString = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>"""
        self.executionTime = {}
        self.serverGroup = []
        self.runInventoryList = {}
        
        while True:
            if utility.getFileLists(location=execLogLoc, recursion=True, 
                                 filePatt="%s_%s_log.txt" % (self.tcID, self.iteration))[1]:
                self.iteration += 1
            else:
                break
            
        # Setting global tcID and iteration
        utility.set_tc_id(self.tcID)
        utility.set_Iteration(self.iteration)
                
    
    def setUp(self):
        """
        Add Log URLs
                
        """
        # Setting global tcID and iteration
        utility.set_tc_id(self.tcID)
        utility.set_Iteration(self.iteration)
               
        # Building execFile location
        execFile = "%s_%s_log.txt" % (utility.tc_id, utility.iteration)
        execFile = os.path.join("exec_log", execFile)
        
        # Building StatusFile location
        statusFile = "%s_%s_log.html" % (utility.tc_id, utility.iteration)
        statusFile = os.path.join("status_log", statusFile)
        
        # Create respective URLs
        execURL = execFile.replace("\\", "/")
        stepsURL = statusFile.replace("\\", "/")
        
        # Adding the log URLs
        self.add_test_url("Execution log", execURL)
        self.add_test_url("Steps", stepsURL)
        
        #=======================================================================
        # self.getResources()
        # self.getNetworks()
        # self.getCredentials()
        #=======================================================================
        
        #Manage All Resources
        #self.manageAllResources()
        
        #Resource Info
        #self.succeed("Set Up", "Resource Information: %s"%str(globalVars.resourceInfo))
        
        #=======================================================================
        # if not globalVars.preReqStatus:
        #     self.failure("Pre-Requisite failed")
        #=======================================================================
    
    
    def tearDown(self):
        """
        Teardown
        """
        for k,v in self.executionTime.items():
            self.succeed("%s %s"%(k,v))
            
        # Generate Status file
        statusFile = os.path.join(globalVars.logsDir, "status_log", "%s_%s_log.html" % \
                                 (self.tcID, self.iteration))
        with open(statusFile, "w") as fptr:
            fptr.write("<html>\n<head />\n")
            fptr.write("<body>\n")
            fptr.write("<table width='100%' border='1'>\n")
            fptr.write(STATUS_HTML_HEADER_TMPL)
            counter = 1
            for elems in self._step_results:
                if elems["name"].lower() == "logheader":
                    fptr.write(STATUS_HTML_DESC_TMPL % (COLOR["DESC"], 
                                                        elems["description"]))
                    continue
                fptr.write(STATUS_HTML_TMPL % (COLOR.get(elems["result"].upper()), 
                                               counter, elems["name"], 
                                               elems["description"], 
                                               elems["result"].upper()))
                counter += 1
            
            # Writing Final Status of the test case
            color = COLOR[STATUS[self.finalStatus]]
            if self.finalStatus == SUCCESS:
                color = "#ccff99"
                self.msg.append("No Failures")
            fptr.write(FINAL_STATUS_HTML_TMPL % (color, "<br />".join(self.msg), 
                                                 STATUS[self.finalStatus]))
            fptr.write("<a href='javascript:window.close()'>Close</a>\n")
            fptr.write("</body></html>")
        
    
    def logDesc(self, description=""):
        """
        Description:
            Special formatting of the status html messages which indicates the 
            nature of operation being performed in the test case.
            
        Input:
            description (String): Header Message
            color (String): HTML hexadecmimal color codes
            
        Output:
            None
        
        """
        description = description.strip()
        if description:
            #===================================================================
            # if not self.passExpectation:
            #     description = "Negative Scenario - %s" % description
            #===================================================================
            self.add_step_result("logheader", description, "")

    
    @classmethod
    def func_exec(cls, fun):
        """
        Description:
            Decorator to wrap the test_functionality method execution of the 
            test case
             
        Input:
            cls (Classname): Class name of the called function
            func (Functionmane): Name of the called function
         
        Output:
            Returns function handler
         
        """
        def func_handler(*kwarg,**kwargs):
            try:
                fun(*kwarg,**kwargs)
            except AssertionError, ae:
                utility.log_data(traceback.format_exc())
                raise ae 
            except SkipTest, se:
                utility.log_data(traceback.format_exc())
                raise se
            except Exception, e:
                utility.log_data(traceback.format_exc())
                #kwarg[0].finalStatus = min(kwarg[0].finalStatus, ERROR)
                #kwarg[0].msg.append(e.__repr__()) 
                raise e
            return fun
        return func_handler
    
    
    def manageAllResources(self):
        """
        Updates all Resources state to Managed
        """
        for resources in globalVars.resourceInfo.values():
            for resource in resources:
                if str(resource["state"]).lower() == "unmanaged":
                    resRI, statRI = self.getResourceInfo(resource["refid"])
                    if not statRI:
                        self.failure("", "Unable to get Information of %s IP: %s to 'Managed"%(resource["deviceType"], resource["ip"]))
                    resCR, statCR = self.changeResourceState(resource["refid"], resRI, 'DISCOVERED')
                    if not statCR:
                        self.failure("", "Unable to change state of %s IP: %s to 'Managed"%(resource["deviceType"], resource["ip"]))
        
    
    def failure(self, mesg, resultCode=FAILURE, contOnFail=False):
        """
        Description:
            Overriding the DellTestCase.fail method to capture the failure 
            status(fail, omitted, blocked etc) messages in the HTML format on 
            the fly.
            
        Input:
            mesg (String): Status message
            resultCode (Int): Result of the operation like fail, blocked etc 
                taken from global variables defined here like FAIELD, BLOCKED, 
                OMITTED etc.
            contOnFail(bool): Flag to continue on failure
                
        Output:
            None
        
        """
        #self.powerDownServer()
        #=======================================================================
        # if not contOnFail and self.delResource=="1":
        #     if hasattr(self, "deploymentRefId"):
        #         self.deleteResource()
        #=======================================================================
        #self.deleteServices()
        #self.powerDownServer()
        self.finalStatus = min(self.finalStatus, resultCode)
        resultCode = STATUS[resultCode]
        self.add_step_result("", mesg, resultCode)
        #utility.log_data("\n" + mesg)
        for k,v in self.executionTime.items():
            utility.log_data("%s %s"%(k,v))
        if not contOnFail:        
            if resultCode.__contains__("OMIT"):
                self.omit(mesg)
            elif resultCode.__contains__("BLOCK"):
                self.block(mesg)
            else:
                self.fail(mesg) 
    
    
    def succeed(self, mesg, resultCode=SUCCESS):
        """
        Description:
            Method to capture the success status(pass) messages in the HTML 
            format on the fly.
            
        Input:
            mesg (String): Status message
            resultCode (Int): Result of the successful operation like pass
                taken from global variables defined here SUCCESS.
                
        Output:
            None
        
        """
        resultCode = STATUS[resultCode]
        self.add_step_result("", mesg, resultCode)        
        #utility.log_data("\n" + mesg)
            
    
    def getResponse(self, action, requestType, payload="", refId=None):
        """
        Gets the Response by calling corresponding   
        """        
                
        url = self.buildUrl(requestType, refId=refId)
        
        
        uri = self.buildHeaderUri(requestType, refId=refId)
        
        startTime = datetime.datetime.now()              
        if action == "POST":
            
            headers = self.generateSecurityHeader(uri, 'POST', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.post(url, data=payload, headers=headers)
        elif action == "PUT":
            headers = self.generateSecurityHeader(uri, 'PUT', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.put(url, data=payload, headers=headers)
        elif action == "DELETE":
            headers = self.generateSecurityHeader(uri, 'DELETE', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.delete(url, headers=headers)
        else:
            headers = self.generateHeaderGetDeploy(uri, 'GET', globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            response = requests.get(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        if action == "GET":
            utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, "", startTime, endTime, elapsedTime)
            
        else:
            utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,action,headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.text != "":
                data = json.loads(response.text)
                return utility.convertUTA(data), True
            else:
                if action == "PUT":
                    return response.text, True
                elif action == "GET":
                    return "No information found for %s" % requestType, False
                elif action == "DELETE":
                    return "No information found for %s" % requestType, True   
                else:
                    return "No information found for %s" % requestType, False
        else:
            if response.status_code == 400:
                return str(response.status_code), False
            else:
                return str(response.status_code) + " " + str(utility.convertUTA(response.text)), False

    
    #===========================================================================
    # def login(self, authStatus=False, loginUser="admin", loginPwd="admin", loginDomain="ASMLOCAL"):
    #     """
    #     Confirm User access credentials
    #     """
    #     payload = utility.readFile(globalVars.loginPayload)        
    #     payload = payload.replace("$login_user", loginUser).replace("$login_pwd", loginPwd).replace("$login_domain", loginDomain)                
    #     resLN, statLN = self.getResponse("POST", "Login", payload, authStatus=authStatus)
    #     if not statLN:
    #         return resLN, False
    #     globalVars.apiKey = str(resLN["apiKey"])
    #     globalVars.apiSecret = str(resLN["apiSecret"])                              
    #     return resLN, True
    #===========================================================================
    
    
    def setTimeZone(self):
        """
        Sets the Time Zone
        """
        payload = utility.readFile(globalVars.timeZonePayload)
        return self.getResponse("PUT", "Timezone", payload)
    
    
    def setNTP(self):
        """
        Sets the Time Zone
        """
        payload = utility.readFile(globalVars.ntpPayload)
        payload = payload.replace("ntp_ip",globalVars.configInfo["Appliance"]["ntp_server_ip"])
        return self.getResponse("PUT", "NTP", payload)    
    
    
    def setProxy(self):
        """
        Sets the Proxy
        """
        payload = utility.readFile(globalVars.proxyPayload)
        return self.getResponse("PUT", "Proxy", payload)
    
    
    def setDHCP(self):
        """
        Sets the Proxy
        """
        payload = utility.readFile(globalVars.dhcpPayload)
        return self.getResponse("PUT", "DHCP", payload)
    
    
    def setCompleteWizard(self):
        """
        Sets the Wizard Status
        """
        payload = utility.readFile(globalVars.WizardPayload)   
        resWC, statWC = self.getResponse("PUT", "Wizard", payload)
        if not statWC:
            return resWC, False
        resWC, statWC = self.getResponse("GET","Wizard")
        if not statWC:
            return resWC, False
        if resWC["isSetupCompleted"]:
            return resWC, True
        else:
            return resWC, False
        
    
    def initialSetup(self):
        """
        Performs initial Set Up
        """
        resIS, statIS = self.login()
        print "Setting Login, Response : %s"%str(resIS)        
        if not statIS:
            return "Unable to Authenticate : %s"%str(resIS), False
        #=======================================================================
        # resIS, statIS = utility.login(authStatus=True)
        # print "Setting Login, Response : %s"%str(resIS)        
        # if not statIS:
        #     return "Unable to Authenticate : %s"%str(resIS), False      
        #=======================================================================
        resIS, statIS = self.setTimeZone()
        print "Setting TimeZone, Response : %s"%str(resIS)                
        if not statIS:
            return "Unable to set Timezone : %s"%str(resIS), False        
        resIS, statIS = self.setNTP()
        print "Setting NTP, Response : %s"%str(resIS)        
        if not statIS:
            return "Unable to set NTP : %s"%str(resIS), False
        resIS, statIS = self.setProxy()
        print "Setting Proxy, Response : %s"%str(resIS)
        if not statIS:
            return "Unable to set Proxy : %s"%str(resIS), False
        resIS, statIS = self.setDHCP()
        print "Setting DHCP, Response : %s"%str(resIS)
        if not statIS:
            return "Unable to set Networks : %s"%str(resIS), False
        
        resIS, statIS = self.setCompleteWizard()
        print "Setting Complete Wizard, Response : %s"%str(resIS)        
        if not statIS:
            return "Unable to set CompleteWizard : %s"%str(resIS), False        
        return "Successfully set Login, NTP, TimeZone, Proxy, DHCP, Complete Wizard", True

    
    
    def configureProcess(self, deviceRefId):
        """
        Defines new Credential with provided Username and Password
        """    
        payload = utility.readFile(globalVars.configureProcessPayload)
        payload = payload.replace("$server_cred_ref", globalVars.credentialMap["autoServer"]).replace("$chassis_cred_ref", 
                   globalVars.credentialMap["autoChassis"]).replace("$iom_switch_cred_ref", 
                   globalVars.credentialMap["autoIOM"]).replace("$deviceRefId", deviceRefId)                
        return self.getResponse("POST", "ConfigureProcess", payload)
    
    
    def configureDiscover(self, deviceRefId):
        """
        Defines new Credential with provided Username and Password
        """    
        payload = utility.readFile(globalVars.configureDiscoverPayload)
        payload = payload.replace("$server_cred_ref", globalVars.credentialMap["autoServer"]).replace("$chassis_cred_ref", 
                   globalVars.credentialMap["autoChassis"]).replace("$iom_switch_cred_ref", 
                   globalVars.credentialMap["autoIOM"]).replace("$deviceRefId", deviceRefId)                
        return self.getResponse("POST", "ConfigureDiscover", payload)
    
    
    def getNetworks(self):
        """
        Get Existing Networks        
        """        
        resNET, statNET = self.getResponse("GET", "Network")
        if not statNET:
            return "Unable to fetch Network Information", False
        for network in resNET:
            globalVars.networkMap[network["type"]] = network["id"]
    
    
    def getCredentials(self):
        """
        Get Existing Credentials        
        """        
        resCRE, statCRE = self.getResponse("GET", "Credential")
        if not statCRE:
            return "Unable to fetch Credential Information", False
        for credential in resCRE["credentialList"]:
            globalVars.credentialMap[credential["credential"]["label"]] = credential["credential"]["id"]
    
    
        
    
    
    def getResourceToDiscover(self, ip):
        """
        Does Discovery and creates Resources in Inventory
        """
        deviceList = utility.readExcel(globalVars.inputFile, "Discovery")
        deviceInfo = []
        for device in deviceList:
            found  = utility.inIPRange(device["StartIP"], device["EndIP"], ip)
            if found:
                deviceInfo.append(deepcopy(device)) 
                deviceInfo[0]["StartIP"] = ip
                deviceInfo[0]["EndIP"] = ip
                return deviceInfo, True
        return deviceInfo, False
    
    
    def getResourceInfo(self, refId):
        """
        Gets Managed Resource Info
        """
        return self.getResponse("GET", "ManagedDevice", refId=refId)
    
    
    def changeResourceState(self, refId, resourceInfo, state='DISCOVERED'):
        """
        Edits Managed Resource Info
        """
        payload = utility.readFile(globalVars.editMDPayload)
        for k,v in globalVars.mdParameters.items():
            if k == "device_refid":
                payload = payload.replace(k, resourceInfo["refId"])
            elif k == "device_vendor":
                payload = payload.replace(k, resourceInfo["manufacturer"])            
            else:
                payload = payload.replace(k, resourceInfo[v])
        payload = payload.replace("device_state",state)      
        return self.getResponse("PUT", "ManagedDevice", payload, refId=refId)
    
    
    def doDiscovery(self, resourceList=None, unmanaged=False):
        """
        Does Discovery and creates Devices in Inventory
        """
        if not resourceList:
            resourceList = utility.readExcel(globalVars.inputFile, "Discovery")  
        for device in resourceList:            
            self.logDesc("Discovery Request Initiated for Device IP Range :: %s"%(device["StartIP"] + " - " + device["EndIP"]))
            startTime = datetime.datetime.now()
            resDS, statDS = self.discoverResource(device, unmanaged=unmanaged)
            if not statDS:
                return resDS, False
            endTime = datetime.datetime.now()
            logDesc = device["Type"] + "==>" + device["StartIP"] + " - " + device["EndIP"] + " Discovery Time:" 
            self.logRunTime(startTime, endTime, logDesc)
        if len(self.runInventoryList) > 0:            
            self.runInventory()
        return globalVars.ipMap, True
    
    
    def filterData(self, reqCom, srcCom):
        """
        Filters components and returns matched objects
        """
        resList = []
        comCount = 0
        for k, v in reqCom.items():
            for _ in xrange(v):
                for component in srcCom:
                    if str(component["Type"]).lower() == str(k).lower():
                        resList.append(component)
                        comCount += 1
            if v != comCount:
                return "Required no of %s(s) not found Required: %s , Available: %s"%(k, v, comCount), False
        return resList
    
    
    def discoverResource(self, resource, unmanaged=False):
        """
        Discovers devices within start_ip and end_ip and verifies Discovery Status
        """         
        payload = utility.readFile(globalVars.discPayload)
        payload = payload.replace("start_ip", resource["StartIP"]).replace("end_ip", resource["EndIP"]).replace(
                                        "manage_in_asm", str(unmanaged).lower())
        self.serverCreId = None
        self.storageCreId = None
        self.switchCreId = None
        self.vcenterCreId = None
        self.chassisCreId = None
        if resource["ServerCre"] is not None:
            payload = payload.replace("Server_Cre", globalVars.credentialMap[resource["ServerCre"]])
            self.serverCreId = globalVars.credentialMap[resource["ServerCre"]]
        else:
            payload = payload.replace("Server_Cre","")
        if resource["StorageCre"] is not None:
            payload = payload.replace("Storage_Cre", globalVars.credentialMap[resource["StorageCre"]])
            self.storageCreId = globalVars.credentialMap[resource["StorageCre"]]
        else:
            payload = payload.replace("Storage_Cre","")
        if resource["SwitchCre"] is not None:
            payload = payload.replace("Switch_Cre", globalVars.credentialMap[resource["SwitchCre"]])
            self.switchCreId = globalVars.credentialMap[resource["SwitchCre"]]
        else:
            payload = payload.replace("Switch_Cre","")
        if resource["ChassisCre"] is not None:
            payload = payload.replace("Chassis_Cre", globalVars.credentialMap[resource["ChassisCre"]])
            self.chassisCreId = globalVars.credentialMap[resource["ChassisCre"]]
        else:
            payload = payload.replace("Chassis_Cre","")
        if resource["VCenterCre"] is not None:
            payload = payload.replace("VCenter_Cre", globalVars.credentialMap[resource["VCenterCre"]])
            self.vcenterCreId = globalVars.credentialMap[resource["VCenterCre"]]
        else:
            payload = payload.replace("VCenter_Cre","") 
        if resource["Type"] == "CHASSIS":
            #resDR, statDR = self.getResponse("POST", "DiscoverChassis", payload=payload)
            resDR, statDR = self.getResponse("POST", "Discovery", payload=payload)
        else:
            resDR, statDR = self.getResponse("POST", "Discovery", payload=payload)
        if not statDR:
            return resDR, False
        time.sleep(30)
        wait = 120
        resDS = None
        discoveryStatus = False
        deviceRefId = ""      
        while wait:
            resDS, statDS = self.getResponse("GET", "Discovery", refId=resDR["id"])
            if "No information found for Discovery" in resDS or not statDS:
                discoveryStatus = True
                break
            if statDS:
                status = resDS["status"]
                if status.lower() == "success":
                    discoveryStatus = True
                    break
                elif status.lower() == "inprogress":
                    continue
                else:
                    time.sleep(2)
            wait = wait -1
        if resDS is None or resDS == "":
            return "Device taking longer than expected to Discover", False
        if discoveryStatus:                        
            #===================================================================
            # if resource["Type"] == "CHASSIS":
            #     resDS, statDS = self.getResponse("GET", "Discovery", refId=resDR["id"])
            #     if type(resDS) == dict and resDS.has_key("devices"):
            #         devices = resDS["devices"]
            #         if len(devices) > 0:
            #             if type(devices[0]) == dict and (devices[0]).has_key("deviceRefId"):
            #                 deviceRefId = devices[0]["deviceRefId"]
            #                 self.configureProcess(deviceRefId)
            #                 resCD, statCD = self.configureDiscover(deviceRefId)
            #                 if not statCD:
            #                     return resCD, statCD
            #                 jobName = resCD["jobNames"][0]
            #                 if jobName:
            #                     wait = 60
            #                     while wait: 
            #                         resJS, statJS = self.getResponse("GET", "JobStatus", refId=jobName)
            #                         if not statJS:
            #                             return resJS, statJS
            #                         if resJS == "IN_PROGRESS":
            #                             continue
            #                         elif resJS == "SUCCESSFUL":
            #                             time.sleep(1)
            #                             break
            #                         wait = wait - 1
            #===================================================================
            resMD, statMD = self.getResponse("GET", "ManagedDevice")
            if not statMD:
                return "Unable to fetch Managed Device Information during Discovery: %s"%resMD, False
            for device in resMD:
                ipRange = utility.getIPRange(resource["StartIP"], resource["EndIP"])
                for ip in ipRange:
                    if device["ipAddress"] == ip:
                        globalVars.ipMap[device["ipAddress"]] = device["refId"]
                        if resource["Type"] == "CHASSIS":
                            serverCount = 0
                            iomCount = 0
                            try:
                                serverCount = int(resource["Devices"])
                            except:
                                serverCount = 0
                            try:
                                iomCount = int(resource["IOMS"])
                            except:
                                iomCount = 0
                            resVD, statVD = self.verifyDeviceCount(serverCount, iomCount, device["ipAddress"])
                            if not statVD:
                                self.runInventoryList[device["ipAddress"]] = [device["refId"], serverCount, iomCount]
                                #self.failure(resVD, contOnFail=True)
                        #=======================================================
                        # else:
                        #     self.verifyDeviceCount(1, 0, device["ipAddress"], chassis=False)
                        #=======================================================
        return globalVars.ipMap, True
    
    
    def waitForChassisDiscovery(self, refId):
        time.sleep(globalVars.defaultWaitTime)
        resDS, statDS = self.getResponse("GET", "Discovery", refId=refId)
        if not statDS:
            pass
        
    def runInventory(self):
        """
        Runs Inventory on the specified Device
        """
        self.logDesc("Running Inventory again on Device IP(s)  :: %s"%(",".join(self.runInventoryList.keys())))
        for key in self.runInventoryList.keys():
            refId = self.runInventoryList[key][0]
            resMD, statMD = self.getResponse("GET", "ManagedDevice", refId = refId, responseType="xml", requestContentType="xml")
            if not statMD:
                self.failure("","Failed to get Device Info %s"%str(resMD), BLOCKED)
            payload = resMD #.replace(self.xmlString,"")
            resMD, statMD = self.getResponse("PUT", "ManagedDevice", payload, responseType="xml", requestContentType="xml", refId=refId)        
            if not statMD:
                self.failure("","Unable to Run Inventory %s"%str(resMD), BLOCKED)
        time.sleep(120)
        discoveryFailed = False
        for key in self.runInventoryList.keys():
            #self.logDesc("Run Inventory Result for Device IP :: %s"%(key))
            serverCount = self.runInventoryList[key][1]
            iomCount = self.runInventoryList[key][2]
            resVD, statVD = self.verifyDeviceCount(serverCount, iomCount, key)
            if not statVD:
                discoveryFailed = True
        if discoveryFailed:
            self.failure("Failed to Discover all Devices")
            
    
    def verifyDeviceCount(self, actServerCount, actIOMCount, ipAddress, chassis=True):
        """
        Verify Chassis Devices and IOMS
        """
        self.logDesc("Discovery Result for Device IP :: %s"%(ipAddress))
        resGM, statGM = self.getResponse("GET", "ManagedDevice")
        if not statGM:
            self.failure("","Failed to get Resource Info %s"%str(resGM), BLOCKED)
        message = ""
        status = False
        if chassis:
            if actServerCount == 0 and actIOMCount == 0:
                return "Verification of Device Count not required", True        
            resMD, statMD = self.getResponse("GET", "Chassis")
            if not statMD:
                return resMD, statMD
            discoveredDevices = []
            for resource in resGM:
                discoveredDevices.append(resource["serviceTag"])
            for chassis in resMD:
                if chassis["managementIP"] == ipAddress:
                    iomCount = 0
                    serverCount = 0
                    logInfo = ""
                    serverCountMiss = 0
                    iomCountMiss = 0
                    for server in chassis["servers"]:
                        if server["serviceTag"] in discoveredDevices:
                            logInfo += "Server: %s Service Tag: %s Slot Name: %s  Status:DISCOVERED <br>"%(server["managementIP"], server["serviceTag"], 
                                                                                   server["slotName"])
                            serverCount += 1
                        else:
                            logInfo += "Server: %s Service Tag: %s Slot Name: %s  Status:DISCOVERY FAILED <br>"%(server["managementIP"], server["serviceTag"], 
                                                                                   server["slotName"])
                            serverCountMiss += 1
                    for iom in chassis["ioms"]:
                        if iom["serviceTag"] in discoveredDevices:
                            logInfo += "IOM: %s Service Tag: %s Slot: %s Status:DISCOVERED <br>"%(iom["managementIP"], iom["serviceTag"], 
                                                                                       iom["slot"])
                            iomCount += 1
                        else:
                            logInfo += "IOM: %s Service Tag: %s Slot: %s Status:DISCOVERY FAILED <br>"%(iom["managementIP"], iom["serviceTag"], 
                                                                                       iom["slot"])
                            iomCountMiss += 1 
                    if serverCountMiss > 0 or iomCountMiss > 0:
                        message = "Discovered Device count mismatch ==> Actual Server Count: %s, Discovered: %s and\
                            Actual IOM Count: %s, Discovered IOMS: %s "%(actServerCount, serverCount, actIOMCount, iomCount)
                        self.succeed(message)
                        message = "Servers Missed ==> %s, IOMS Missed ==> %s"%(serverCountMiss, iomCountMiss)                    
                        self.succeed(message)
                        status = False
                    else:
                        message = "Discovered Device count matches ==> Actual Server Count: %s, Discovered: %s and\
                            Actual IOM Count: %s, Discovered IOMS: %s "%(actServerCount, serverCount, actIOMCount, iomCount)
                        status = True
                        self.succeed(message)        
                    self.succeed(logInfo)
        else:
            for resource in resGM:
                if resource["ipAddress"] == ipAddress:
                    self.succeed("Discovered Device ==> IP Address::%s  ServiceTag::%s"%(resource["ipAddress"], resource["serviceTag"]))
                    status = True
                    break
        return message, status 
            
    
    def removeResource(self, refId, ip):
        """
        Removes Discovered Device
        """
        resDD, statDD = self.getResponse("DELETE", "ManagedDevice", refId=refId)
        if not statDD:
            return resDD, False
        if globalVars.ipMap.has_key(ip):
            globalVars.ipMap.pop(ip)
        time.sleep(globalVars.defaultWaitTime)
        return resDD, True
    
    
    def manageResource(self, discoveredResources, unmanaged=False):
        """
        Create Device in Inventory, return array of Managed Devices created
        """
        chassisRefId = []
        failedList = []
        for device in discoveredResources:
            time.sleep(globalVars.defReqWaitTime)
            if "chassis" in str(device["deviceType"]).lower():
                chassisRefId.append(device["deviceRefId"])                
            resMD, statMD = self.getResponse("GET", "ManagedDevice", refId = device["deviceRefId"])
            if statMD:
                globalVars.ipMap[device["ipAddress"]] = device["deviceRefId"]
            else:
                payload = utility.readFile(globalVars.manageDevicePayload)                     
                if not globalVars.ipMap.has_key(device["ipAddress"]):                    
                    for k,v in globalVars.mdParameters.items():  
                        payload = payload.replace(k, device[v])
                    if "server" in str(device["deviceType"]).lower():
                        if self.serverCreId:
                            payload = payload.replace("device_creid", self.serverCreId)
                    elif "equallogic" in str(device["deviceType"]).lower():
                        if self.storageCreId:
                            payload = payload.replace("device_creid", self.storageCreId)
                    elif "vcenter" in str(device["deviceType"]).lower():
                        if self.vcenterCreId:
                            payload = payload.replace("device_creid", self.vcenterCreId)
                    elif "switch" in str(device["deviceType"]).lower():
                        if self.switchCreId:
                            payload = payload.replace("device_creid", self.switchCreId)
                    elif "chassis" in str(device["deviceType"]).lower():
                        if self.chassisCreId:
                            payload = payload.replace("device_creid", self.chassisCreId)
                if unmanaged:
                    payload = payload.replace("device_state","UNMANAGED")
                else:
                    payload = payload.replace("device_state","DISCOVERED")
                payload = payload.replace("login_user", self.loginUser)                
                resMD, statMD = self.getResponse("POST", "ManagedDevice", payload)
                if not statMD:
                    failedList.append(resMD)
                for device in resMD:
                    globalVars.ipMap[device["ipAddress"]] = device["refId"]
            if chassisRefId:
                self.manageChassis(chassisRefId)          
        else:            
            return "Device(s) already created in Inventory", True        
        if failedList:
            return failedList, False
        else:
            "", True
    
    
    def getResources(self):
        """
        Gets all Resources available
        """
        resMD, statMD = self.getResponse("GET", "GetManagedDevice")
        if not statMD:
            self.failure("","Failed to get Resource Info %s"%str(resMD), self.BLOCKED)
            print " Failed to get ResourceInfo %s"%str(resMD)
        resServer = []
        resStorage = []
        resVCenter = []
        resChassis = []
        resVM = []
        resComp = []
        resswitch =[]
        resElementMgr =[]
        resNetApp =[]
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
            elif "netapp" in str(resource["deviceType"]).lower():
                resNetApp.append({"ip":resource["ipAddress"],"refid":resource["refId"],"deviceType":resource["deviceType"],"serviceTag":resource["serviceTag"],"state":resource["state"]}) 
            
            else:
                utility.log_data("deviceType is : %s"%str(resource["deviceType"]).lower())
                
  
            
        globalVars.resourceInfo["SERVER"] = resServer
        globalVars.resourceInfo["STORAGE"] = resStorage
        globalVars.resourceInfo["VCENTER"] = resVCenter
        globalVars.resourceInfo["CHASSIS"] = resChassis
        globalVars.resourceInfo["SCVMM"] = resVM
        globalVars.resourceInfo["COMPELLENT"] = resComp
        globalVars.resourceInfo["SWITCH"] = resswitch
        globalVars.resourceInfo["ElementManager"] = resElementMgr
        globalVars.resourceInfo["NETAPP"] = resNetApp
        
        return resServer, resStorage, resVCenter, resChassis, resVM, resComp, resswitch, resElementMgr  
    
    
    
    def getServerPoolInfo(self, refId=None):
        """
        Gets Managed Device Info
        """
        if refId:
            return self.getResponse("GET", "ServerPool", refId=refId)
        else:
            return self.getResponse("GET", "ServerPool")
    
    
    def getChoice(self, limit, maxLimit):
        """
        Description:
            Return a random element from validVal list which is not same as 
            the oldVal element.
        """
        tempVal = []
        validRange = range(maxLimit)
        while len(tempVal) < limit:
            newVal = random.choice(validRange)
            if newVal not in tempVal:
                tempVal.append(newVal)
        tempVal.sort()
        return tempVal
    
    
    def getReqResource(self, limit=1, resourceType="SERVER", deviceType=None):
        """
        Gets Servers from Resource Info
        """        
        resources = []
        if resourceType == "SERVER":
            #===================================================================
            # serverList = ["H2WF3W1","FSRR1V1","84WF3W1","GFV63W1","H4FJKT1","H90SLV1","15WF3W1","CKFJKT1","G2WF3W1",
            #     "F2WF3W1","D4WF3W1","J2WF3W1","4MW63W1","23WF3W1","D3WF3W1","G3WF3W1","B3WF3W1",
            #     "73WF3W1","CFV63W1","9FV63W1"]
            #===================================================================
            #===================================================================
            # serverList = ["8FV63W1","7FV63W1","53WF3W1","3KFJKT1","97QR1V1","H4WF3W1","38QR1V1","H3WF3W1","64WF3W1","D1X63W1",
            #     "C2WF3W1","54WF3W1","G4WF3W1","5MW63W1","HFV63W1","33WF3W1","D2WF3W1","F4WF3W1","63WF3W1","74WF3W1"]
            #===================================================================

            #=====================================================
            # "97QR1V1","H4WF3W1","38QR1V1","H90SLV1","15WF3W1","H3WF3W1","CKFJKT1","9MW63W1","G2WF3W1","64WF3W1",
            # "F2WF3W1","D1X63W1","D4WF3W1","J2WF3W1","C2WF3W1","54WF3W1","G4WF3W1","5MW63W1","83WF3W1",
            # "4MW63W1","23WF3W1","D3WF3W1","G3WF3W1","F3WF3W1","B3WF3W1","73WF3W1","HFV63W1","33WF3W1","D2WF3W1","CFV63W1",
            # "B4WF3W1","BFV63W1","DFV63W1","9FV63W1","F4WF3W1","63WF3W1","74WF3W1"]
            #=====================================================
            #===================================================================
            # serverList = ['111PS22', '10WMS22', '111MS22', '10TNS22', '112NS22', '10YMS22', '10XNS22', '10VMS22', 'H393S22', 
            #               '10WNS22', '10ZNS22', '10YNS22', '110NS22', '10ZLS22', '112PS22', '110MS22', '113PS22', '10WLS22', 
            #               '112MS22', '111NS22', '113MS22', '10XMS22', '110PS22', 'H392S22', '10ZMS22', '114MS22', '10VLS22', 
            #               '10XLS22', '113NS22', '10TMS22']
            #===================================================================
            resources = globalVars.resourceInfo[resourceType]
            #===================================================================
            # for resource in globalVars.resourceInfo[resourceType]:
            #     if resource["serviceTag"] in serverList:
            #         resources.append(resource)
            # resources = [resource for resource in resources if resource["health"] in ("OK", "GREEN") and resource["state"] in ("DISCOVERED")]
            #===================================================================
        else:
            resources = globalVars.resourceInfo[resourceType]
        #=======================================================================
        # if resourceType == "SERVER":
        #     resources = [resource for resource in resources if resource["health"] == "OK"]
        #=======================================================================
        if deviceType:
            #resList = [resource for resource in resources if deviceType in str(resource["deviceType"]).lower() and resource["health"] in ("OK", "GREEN")]
            resList = [resource for resource in resources if deviceType in str(resource["deviceType"]).lower()]
        else:
            resList = resources
        if len(resList) > limit:
            tempResList = []
            randomVal = self.getChoice(limit, len(resList))
            for val in randomVal:
                tempResList.append(resList[val])
            return tempResList
        else:
            return resList   
        
    
    def verifySPCreate(self, poolName, resourceInfo):
        """
        Verifies whether Server Pool has been created or not
        """
        self.succeed("Verification ::: ")
        
        resSP, statSP = self.getServerPoolInfo()
        if not statSP:
            self.failure("Unable to fetch Server Pool Information Result: %s"%str(resSP))
        position = -1
        for item in resSP:
            if item["groupName"] == poolName:
                position = resSP.index(item)
        if position == "-1":
            self.failure("Unable to fetch Server Pool Information (%s) Result: %s"%(poolName, str(resSP)))
            
        self.serverPoolId = str(resSP[position]["groupSeqId"])
        
        if self.serverPoolId is None:
            self.failure("Unable to fetch Server Pool Sequence Id %s"%resSP)
        
        #Verify Server Pool Name
        if resSP[position]["groupName"] != poolName:
            self.failure("Unable to verify Server Pool Name Expected: '%s' Actual: %s"%(poolName, resSP[position]["groupName"]))
        self.succeed("Verified Server Pool Name Expected: '%s' Actual: %s"%(poolName, resSP[position]["groupName"]))
        
        found = False
        failedList = []
        #Verify Devices
        for device in resSP[position]["managedDeviceList"]["managedDevices"]:
            found = False
            for resource in resourceInfo:
                if resource["ip"] == device["ipAddress"] and resource["serviceTag"] == device["serviceTag"]:
                    found = True
            if not found:
                failedList.append(device["ipAddress"])
        if failedList:
            self.failure("Unable to verify Server Info in Server Pool '%s'"%",".join(failedList))            
        self.succeed("Able to verify Server Details (%s) in Server Pool"%str(resourceInfo))
        
    
    def deployService(self, deployTitle):
        """
        Creates a Service Template and Deploys ESXi on a Server Pool
        """        
        #Authenticate User
        self.login(authStatus=True, loginUser=self.loginUser, loginPwd=self.loginPassword)
        
        resST, statST = self.getResponse("GET", "Template", refId=self.templateId, responseType="xml")
        if not statST:
            return "Unable to retrieve Service Template: %s"%resST, statST
        
        serviceTemplate = resST.replace(self.xmlString, "").replace("ServiceTemplate", "serviceTemplate") 
        
        #Initiate Deployment
        payload = utility.readFile(globalVars.deploymentTemplatePayload)
        payload = payload.replace("$service_template", serviceTemplate)
        payload = payload.replace("$login_user", self.loginUser)
        payload = payload.replace("$deploy_title", deployTitle)
        payload = payload.replace("$deployment_id", "")
        resDT, statDT = self.getResponse("POST", "Deploy", payload, responseType="xml")
        if not statDT:
            return "Unable to Deploy Service Template: %s"%resDT, statDT        
        return resDT, True
    
    
    def scaleUpResource(self, scaleupTemplate, configParam, modifyRC=None):
        """
        Scale up Server to the existing Deployment
        """
        #Authenticate User
        self.login(authStatus=True, loginUser=self.loginUser, loginPwd=self.loginPassword)        
        
        #Create Service Template
        templateName = os.path.join(globalVars.templateDir, scaleupTemplate)                
        addResourcePayload = utility.readFile(templateName)
        for k,v in configParam.items():
            addResourcePayload = addResourcePayload.replace(k, v)
          
        resDT, statDT = self.getResponse("GET", "Deploy", refId=self.deploymentRefId, responseType="xml")
        if not statDT:
            return "Unable to retrieve Deployed Template: %s"%resDT, statDT       
                  
        temp = str(resDT).rpartition("</components>")
        deploymentTemplate = temp[0] + temp[1] + addResourcePayload + temp[2]    
        
        if modifyRC:
            for k,v in modifyRC.items():
                deploymentTemplate = deploymentTemplate.replace(k, v)  
        deploymentTemplate = deploymentTemplate.replace("<retry>false</retry>", "<retry>true</retry>")
        resDT, statDT = self.getResponse("PUT", "Deploy", deploymentTemplate, responseType="xml", refId=self.deploymentRefId)        
        if not statDT:
            return "Unable to Scale up Resource %s"%str(resDT), statDT
             
        return resDT, statDT
    
    
    def deleteResource(self):
        """
        Delete Resource from the existing Deployment
        """
        if not type(self.deploymentRefId) == str:
            return "", True
        #Authenticate User
        self.login(authStatus=True, loginUser=self.loginUser, loginPwd=self.loginPassword)        
         
        resDT, statDT = self.getResponse("GET", "Deploy", refId=self.deploymentRefId, responseType="xml")
        if not statDT:
            return "Unable to retrieve Deployed Template: %s"%resDT, statDT                
      
        deploymentTemplate = resDT.replace("<teardown>false</teardown>", "<teardown>true</teardown>").replace("<retry>false</retry>", "<retry>true</retry>")
        resDT, statDT = self.getResponse("PUT", "Deploy", deploymentTemplate, responseType="xml", refId=self.deploymentRefId)        
        if not statDT:
            return "Unable to Delete Resource %s"%str(resDT), statDT
        
        #Validate Deployment Completion
        loop = 90
        while loop:
            resDS, statDS = self.getDeploymentStatus(self.deploymentRefId)
            if not statDS:
                return resDS, statDS
                break;
            if resDS.lower() in ("in_progress"):
                time.sleep(120)
            else:
                if resDS.lower() in ("successfull","complete"):
                    return "Successfully Deleted Resources", True
                else:
                    return resDS, statDS
                break
            loop -= 1
             
        return "Failed to Delete Resources", False
    
    
    def deleteServices(self):
        """
        Delete Resource from the existing Deployment
        """

        #Authenticate User
        self.login(authStatus=True, loginUser=self.loginUser, loginPwd=self.loginPassword)        
         
        resDT, statDT = self.getResponse("GET", "Deploy")        
        if not statDT:
            return "Unable to Fetch Deployed Services %s"%str(resDT), statDT
        
        refIds = []
        if resDT:
            for service in resDT:
                resDT, statDT = self.getResponse("GET", "Deploy", refId=str(service["id"]), responseType="xml")
                if statDT:                
                    deploymentTemplate = resDT.replace("<teardown>false</teardown>", "<teardown>true</teardown>").replace("<retry>false</retry>", "<retry>true</retry>")
                    resDT, statDT = self.getResponse("PUT", "Deploy", deploymentTemplate, responseType="xml", refId=str(service["id"]))        
                    if statDT:
                        refIds.append(str(service["id"]))
                
        #Validate Deployment Completion
        if len(refIds) > 0:
            startTime = datetime.datetime.now()
            loop = 60
            failedList= []
            successList = []
            while loop:
                for deploymentId in refIds:
                    resDS, statDS = self.getDeploymentStatus(deploymentId)
                    if not statDS:
                        if deploymentId not in failedList:
                            failedList.append(deploymentId)                                        
                    if resDS.lower() in ("in_progress"):
                        continue
                    else:
                        if resDS.lower() in ("successfull", "complete"):
                            if deploymentId not in successList:
                                successList.append(deploymentId) 
                        else:
                            if deploymentId not in failedList:
                                failedList.append(deploymentId)
                if len(refIds) == len(failedList) + len(successList):
                    break
                time.sleep(120)
                loop -= 1
            endTime = datetime.datetime.now()
            self.logRunTime(startTime, endTime, "Teardown Process Time :")
            if failedList:
                return "Failed to Delete Deployed Services", False
            else:
                return "Successfully Deleted All Deployed Services", True
        else:
            return "No Deployment Services to Delete", True
    
    def createUser(self, userName, userPwd, userDomain="ASMLOCAL"):
        """
        Creates User to manage ASM
        """
        payload = utility.readFile(globalVars.userPayload)        
        payload = payload.replace("$user_name", userName).replace("$user_pwd", 
                   userPwd).replace("$user_domain", userDomain).replace("$login_user", self.loginUser)            
        return self.getResponse("POST", "User", payload)
        
        
    def getUserInfo(self, refId):
        """
        Gets the User Information
        """
        return self.getResponse("GET", "User", refId=refId)
    
    
    def getTimeZone(self, responseType="json"):
        """
        Gets the User Information
        """
        return self.getResponse("GET", "TimeZone", responseType=responseType)
    
    
    def createDirectoryService(self):
        """
        Creates Directory Service
        """
        payload = utility.readFile(globalVars.directoryServicePayload)        
        payload = payload.replace("$ad_host",self.ad_host).replace("$ad_port", \
                   self.ad_port).replace("$ad_name", self.ad_name).replace("$ad_binddn", \
                   self.ad_binddn).replace("$ad_password", self.ad_password).replace("$ad_domain", \
                   self.ad_domain).replace("$ad_de", self.ad_de).replace("$ad_firstname", \
                   self.ad_firstname).replace("$ad_lastname", self.ad_lastname).replace("$login_user", self.loginUser)
        resVA, statVA = self.getResponse("POST", "ValidateAD", payload)
        if not statVA:
            return resVA, statVA
        return self.getResponse("POST", "DirectoryService", payload)

    
    def getUsersFromAD(self):
        """
        Gets Users from AD
        """
        refId = self.ad_name + "/user" 
        return self.getResponse("GET", "DirectoryService", refId = refId)
    
    
    def getNetworkPortCount(self, refId):
        """
        Identifies whether a Server has 2 port or 4 port Network Card  
        """
        resSI, statSI = self.getResponse("GET", "ServerInventory", refId = refId)  
        if not statSI:
            return "Unable to fetch Server Inventory Details: %s"%str(resSI), False
        model = resSI["networkInterfaceList"][0]["productName"]        
        if "57810" in model or "5719" in model or "5720" in model or "QLogic" in model:
            return "2", True
        elif "57820" in model:
            return "4", True
        return "", False
    
    
    
    
    
    def getAllTemplates(self, responseType="json"):
        """
        Gets the Deployment Reference Id  
        """
        resDI, statDI = self.getResponse("GET", "Template", responseType=responseType)
        if not statDI:
            return "Unable to fetch Deployment Id: %s"%resDI, "Unable to fetch Deployment Job Id: %s"%resDI, statDI

    
    def getDeploymentInfo(self, refId, responseType="json"):
        """
        Gets the Deployment Reference Id  
        """
        return self.getResponse("GET", "Deploy", refId=refId, responseType=responseType)
        
        
    
    
    def setEnvironment(self):
        """
        Clears Templates, Server Pools, and Services 
        Change state of all Resources to Managed
        """
        self.succeed("Pre Run Setup ::: ")
        
        #Delete all Deployed Services
        resDR, statDR = self.deleteServices()
        if not statDR:
            self.succeed(resDR)
        else:
            self.succeed("Successfully Deleted All Resources")
        
        #Delete all Templates
        resCT, statCT = self.cleanUpTemplates(retnStatus=True)
        if not statCT:
            self.failure("Unable to remove all Automation Templates %s"%resCT)
        self.succeed("Successfully Removed All Automation Templates")
        
        #Delete all Server Pools
        resCS, statCS = self.cleanUpServerPool(retnStatus=True)
        if not statCS:
            self.failure("Unable to remove all Server Pools %s"%resCS)
        self.succeed("Successfully Removed All Server Pools")
        
        
        
        #Change State of all Resources to Managed
        self.manageAllResources()        
        self.succeed("Successfully changed state of all Resources to Managed")
    
    
    def clearEnvironment(self, deleteRes=False):
        """
        Clears Templates, Server Pools, and Services
        """
        self.succeed("Post Run Cleanup ::: ")
        
        #Delete Resource
        resDR, statDR = self.deleteServices()
        if not statDR:
            self.succeed(resDR)
        else:
            self.succeed("Successfully Deleted All Resources")
        
        #Delete all Templates
        resCT, statCT = self.cleanUpTemplates(retnStatus=True)
        if not statCT:
            self.failure("Unable to remove all Automation Templates %s"%resCT)
        self.succeed("Successfully Removed All Automation Templates")
         
        #Delete Resource
        #if deleteRes and self.delResource == "1":
        #=======================================================================
        # resDR, statDR = self.deleteServices()
        # if not statDR:
        #     self.succeed(resDR)
        # else:
        #     self.succeed("Successfully Deleted All Resources")
        #=======================================================================
        #=======================================================================
        # else:
        #     Delete all Services
        #     resCS, statCS = utility.cleanUpServices(retnStatus=True)
        #     resCS, statCS = self.deleteServices()
        #     if not statCS:
        #         self.failure("Unable to remove all Deployment Services %s"%resCS)
        #     self.succeed("Successfully Removed All Deployment Services")
        #=======================================================================
         
        #Delete all Server Pools
        resCS, statCS = self.cleanUpServerPool(retnStatus=True)
        if not statCS:
            self.failure("Unable to remove all Server Pools %s"%resCS)
        self.succeed("Successfully Removed All Server Pools")
        
        #Power Down Servers
        self.powerDownServer()
        
    
    def getHostNameOld(self):
        """
        Returns Host Name
        """
        hostName = "autoHost" + datetime.datetime.now().strftime("%Y%m%d%H%M%S%f")
        time.sleep(1)
        utility.log_data("", hostName)
        return hostName
    
    
    def baseCheckDependency(self, reqServer=None, reqStorage=None, reqVcenter=None, networkCheck=True, serverDT=None, storageDT=None, vcenterDT=None, contOnFail=False):
        """
        Checking Dependency for the test case which cannot be ignored for 
        running the test case.
        """        
        self.succeed("Check Dependency ::: ")
        
        #Log Existing Resources        
        utility.log_data("Available Resources :: %s"%str(globalVars.resourceInfo))        
        
        # Verify whether required no of Servers are available
        if reqServer:
            if serverDT:
                self.serverRes = self.getReqResource(limit=reqServer, resourceType="SERVER", deviceType=serverDT)
                self.serverType = serverDT
                if len(self.serverRes) < reqServer:                    
                    self.failure("Required no of Servers not found ==> Required: %s Available: %s"%(reqServer, len(self.serverRes)), OMITTED, contOnFail = contOnFail)
                    if contOnFail: return False
                self.succeed("Required no. of Servers available ==> Required: %s Available: %s %s"%(reqServer, len(self.serverRes), str(self.serverRes)))
            else:
                self.serverRes = self.getReqResource(limit=reqServer, resourceType="SERVER", deviceType="rack")
                self.serverType = "rack"
                if len(self.serverRes) < reqServer:
                    self.serverRes = self.getReqResource(limit=reqServer, resourceType="SERVER", deviceType="blade")
                    self.serverType = "blade"
                if len(self.serverRes) < reqServer:
                    self.failure("Required no of Servers not found ==> Required: %s Available: %s"%(reqServer, len(self.serverRes)), OMITTED, contOnFail = contOnFail)
                    if contOnFail: return False
                self.succeed("Required no. of Servers available ==> Required: %s Available: %s %s"%(reqServer, len(self.serverRes), str(self.serverRes)))
        
        # Verify whether required Storage is available
        if reqStorage:
            self.storageRes = self.getReqResource(limit=reqStorage, resourceType="STORAGE", deviceType=storageDT)
            if len(self.storageRes) < reqStorage:
                self.failure("Required Storage not found ==> Required: %s Available: %s"%(reqStorage, len(self.storageRes)), OMITTED, contOnFail = contOnFail)
            self.succeed("Required Storage available ==> Required: %s Available: %s %s"%(reqStorage, len(self.storageRes), str(self.storageRes)))
        
        # Verify whether required VCenter is available
        if reqVcenter:
            self.vcenterRes = self.getReqResource(limit=reqVcenter, resourceType="VCENTER", deviceType=vcenterDT)
            if len(self.vcenterRes) < reqVcenter:
                self.failure("Required VCenter not found ==> Required: %s Available: %s"%(reqVcenter, len(self.vcenterRes)), OMITTED, contOnFail = contOnFail)
                if contOnFail: return False
            self.succeed("Required VCenter available ==> Required: %s Available: %s %s"%(reqVcenter, len(self.vcenterRes), str(self.vcenterRes)))
                
        if networkCheck:
            #Verify PXE Network 
            if globalVars.networkMap["PXE"] == "":
                self.failure("PXE Network not defined", OMITTED)
            
            #Verify Hypervisor Management Network
            if globalVars.networkMap["HYPERVISOR_MANAGEMENT"] == "":
                self.failure("HYPERVISOR MANAGEMENT Network not defined", OMITTED)
            
            #Verify Hypervisor Migration Network
            if globalVars.networkMap["HYPERVISOR_MIGRATION"] == "":
                self.failure("HYPERVISOR MIGRATION Network not defined", OMITTED)
            
            #Verify SAN ISCSI Network
            if globalVars.networkMap["STORAGE_ISCSI_SAN"] == "":
                self.failure("STORAGE SAN ISCSI Network not defined", OMITTED)
            
            #Verify Workload Network 
            if globalVars.networkMap["PUBLIC_LAN"] == "":
                self.failure("WORKLOAD Network not defined", OMITTED)
        return True
    
    
    
            
                    
    def deployMultipleTemplates(self, reqServer=2, reqStorage=1, reqVcenter=1, tcConfig=None, loopCount=60, scaleUP=False, scaleUPConfig=None, contOnFail = False):
        """
        Scaling and Performance
        """
        self.serverPoolId = ""
        network_config = storageId = storageName = clusterId = clusterName = storageIP = ""
        count = 1       
        startTime = datetime.datetime.now() 
        if self.serverRes:
            for itm in self.serverRes:
                if itm not in self.serverGroup:
                    self.serverGroup.append(itm)
        for loop in xrange(1, reqServer+1, 2):        
            
            prev = loop-1
            
            #Create Server Pool
            poolName = "autoPool" + str(loop)
            resCP, statCP = self.createServerPool(self.serverRes[prev:loop+1], poolName=poolName)
            if not statCP:
                self.failure("Unable to Create Server Pool (%s) with Servers: %s Error: %s"%(poolName, str(self.serverRes[prev:loop+1]), resCP), contOnFail=contOnFail)
                if contOnFail: return False
                
            self.succeed("Able to Create Server Pool (%s) with Servers: %s "%(poolName, str(self.serverRes[prev:loop+1])))
            
            #Verify Created Server Pool
            self.verifySPCreate(poolName, self.serverRes[prev:loop+1])
                    
            #Load Network Interface Info
            resNC, statNC = self.getNetworkConfiguration(self.serverRes[prev:loop+1][0]["refid"], serverType=self.serverType)        
            if not statNC:
                self.failure("Unable to Load Network Interface Information %s"%str(resNC), contOnFail=contOnFail)
                if contOnFail: return False
            network_config = resNC
            deployTitle = "Deploy_2Server_1Cluster_ESX_" + str(count) 
            templateName =  "Template_2Server_1Cluster_ESX_" + str(count)  
            if reqStorage:
                storageId = self.storageRes[0]["refid"]
                storageName = self.storageRes[0]["serviceTag"]
                storageIP = self.storageRes[0]["ip"]
            if reqVcenter:
                clusterId = self.vcenterRes[0]["refid"]
                clusterName = self.vcenterRes[0]["serviceTag"]        
            hostName = self.getHostName()
            hostName1 = self.getHostName()       
            hostName2 = self.getHostName()
            dataCenterName = "autoDC" + str(count)
            clusterName = "autoCluster" + str(count)
            
            configParam = {"$server_pool_id":self.serverPoolId, "$network_config_info":network_config, 
                           "$login_user":self.loginUser, "$storage_id":storageId, "$storage_name":storageName, "$os_version":"esxi-5.5",
                           "$cluster_id":clusterId, "$cluster_name":clusterName, "$service_id":"", "$enableHA":"false", "$enableDRS":"false",
                           "$enableMEM":"false", "$template_name": templateName,"$host_name1":hostName1, "$host_name2":hostName2,
                           "$host_name":hostName, "$storage_ip":storageIP, "$storage_auth_type":globalVars.storageAuthType,
                           "$storage_chap_user":globalVars.chapUser,"$storage_chap_pwd":globalVars.chapPassword, "$guest_os":globalVars.linuxGuestOS,
                           "$network_workload":globalVars.networkMap["PUBLIC_LAN"], "$dc_name":dataCenterName}
            count = count + 1            
            #Check for personalized Config
            if tcConfig:
                for k in tcConfig.keys():
                    configParam[k] = tcConfig[k]
            
            #Create Template
            resCT, statCT = self.createTemplate(configParam)
            if not statCT:
                self.failure(resCT, contOnFail=contOnFail)
                if contOnFail: return False
            if reqServer:
                self.succeed("Successfully Created Service Template for Deployment: %s "%str(self.serverRes[prev:loop+1]))
            else:
                self.succeed("Successfully Created Service Template for Deployment: %s "%str(self.storageRes))
                        
            if not self.templateId:
                self.failure("Unable to fetch Template Id: %s"%resCT, contOnFail=contOnFail)
                if contOnFail: return False
            self.succeed("Template Id: %s"%self.templateId)
           
        
        self.getAllTemplates()
        
       
    
    
    def changeDRACPassword(self, idracIP, idracUser, idracPwd, idracNewPwd):
        """
        Change Idrac password
        """
        cmd = "racadm -r %s -u %s -p %s getconfig -u %s"%(idracIP, idracUser, idracPwd, idracUser)
        resCP, statCP, stCP = utility.run_local_cmd(cmd)
        if not statCP:
            return "Unable to get '%s' User index"%idracUser, False
        index = -1
        for line in str(resCP).splitlines():
            if "cfgUserAdminIndex" in line:
                index = line.split("=")[1]
                break
        if index == -1:
            return "Unable to get '%s' User index"%idracUser, False
        cmd = "racadm -r %s -u %s -p %s set iDRAC.Users.%d.Password %s"%(idracIP, idracUser, idracPwd, int(index), idracNewPwd)
        resCP, statCP = utility.run_local_cmd(cmd)
        if not statCP:
            return "Unable to change '%s' User Password to %s"%(idracUser, idracNewPwd), False
        return "Successfully changed password for '%s' User"%idracUser, True
    
    
    def verifyState(self, state, component):
        """
        Verifies whether STORAGE has discovered or not
        """
        self.succeed("Verification ::: ")
        
        resDD, statDD = self.getResourceInfo(self.refId)
        self.deviceInfo = resDD      
        if not statDD or resDD is None:
            self.failure("Unable to fetch %s State %s"%(component, str(resDD)))
        elif str(resDD["state"]).lower() != str(state).lower():
            self.failure("Unable to verify %s State as '%s' Result: %s"%(component, state, str(resDD)))
        self.succeed("Able to verify %s state as '%s'"%(component, state))
    
    
    def manageComponent(self, reqServer=None, reqStorage=None, reqVcenter=None, manageToUnmanage=True):
        """
        Manage and Unmanage Components  
        """
        #Get Discovered Resources
        component = "SERVER"
        
        if manageToUnmanage:
            fromState = "DISCOVERED"
            toState = "UNMANAGED"
            componentState = "UNMANAGED"
            unmanaged = False
        else:
            fromState = "UNMANAGED"
            toState = "DISCOVERED"
            componentState = "MANAGED"
            unmanaged = True
            
        if reqServer:
            resource = self.serverRes[0]            
        elif reqStorage:
            resource = self.storageRes[0] 
            component = "STORAGE"           
        else:
            resource = self.vcenterRes[0]
            component = "VCENTER"
        
        self.deviceIP = resource["ip"]
        self.refId = resource["refid"]
        
        #Get Device Details
        resDD, statDD = self.getResourceToDiscover(self.deviceIP)
        if not statDD:
            self.failure("Failed to get %s Details to Discover %s"%(component, str(resDD)))
        self.succeed("Able to get %s Details %s"%(component, str(resDD)))
        
        #Remove Discovered STORAGE
        resRS, statRS = self.removeResource(self.refId, self.deviceIP)
        if not statRS:
            self.failure("Unable to Remove %s %s"%(component, self.deviceIP))
        self.succeed("Able to Remove Discovered %s %s"%(component, str(resRS)))
        
        #Discover STORAGE as Unmanaged
        resDS, statDS = self.doDiscovery(resDD, unmanaged=unmanaged)
        if not statDS:
            self.failure("Unable to Discover %s %s"%(component, self.deviceIP))
        self.succeed("Able to Discover %s %s"%(component, str(resDS)))
        self.refId = globalVars.ipMap[self.deviceIP]
                
        #Update RefId
        self.getResources()
        
        #Verify Discovered STORAGE State
        self.verifyState(fromState, component)
        
        #Set STORAGE State to Manage
        resMD, statMD = self.changeResourceState(self.refId, self.deviceInfo, state=toState)
        if not statMD:
            self.failure("Unable to change state of %s %s to '%s'"%(component, self.deviceIP, componentState))
        self.succeed("Able to change state of %s %s to '%s'"%(component, self.deviceIP, componentState))
        
        #Verify Discovered STORAGE State is Manage
        self.verifyState(toState, component)
    
    
    def logRunTime(self, startTime, endTime, logDesc):
        """
        Logs Deployment Time 
        """
        elapsedTime="%s"%(endTime-startTime)
        #=======================================================================
        # self.executionTime["Start Time :"] = startTime
        # self.executionTime["End Time :"] = endTime
        #=======================================================================
        self.executionTime[logDesc] = elapsedTime
    
    
    def powerDownServer(self):
        """
        Initializes the SD Card and Power Down Server
        """
        if self.serverGroup:
            for server in self.serverGroup:
                idracIP = server["ip"]
                #===============================================================
                # cmd = "tools\plink.exe -ssh -pw calvin root@%s racadm config -g cfgVFlashSD -o cfgVFlashSDEnable 1"%idracIP
                # #cmd = "racadm -r %s -u %s -p %s calvin config -g cfgVFlashSD -o cfgVFlashSDEnable 1"%(idracIP, "root", "calvin")
                # resCP, statCP = utility.run_local_cmd(cmd, input="y")
                # self.succeed(resCP)
                # cmd = "tools\plink.exe -ssh -pw calvin root@%s racadm vflashsd initialize"%idracIP
                # #cmd = "racadm -r %s -u %s -p %s calvin vflashsd initialize"%(idracIP, "root", "calvin")
                # resCP, statCP = utility.run_local_cmd(cmd, input="y")
                # self.succeed(resCP)
                # time.sleep(60)
                #===============================================================                
                cmd = "tools\plink.exe -ssh -pw calvin root@%s racadm serveraction powerdown"%idracIP
                resCP, statCP = utility.run_local_cmd(cmd, input="y")
                self.succeed(resCP)
                time.sleep(10)
                
    
    def hardResetServer(self):
        """
        Initializes the SD Card and Power Down Server
        """
        self.getResources()
        serverList = globalVars.resourceInfo["SERVER"]        
        for server in serverList:
            cmd = "tools\plink.exe -ssh -pw calvin root@%s racadm serveraction powerdown"%server["ip"]
            resCP, statCP = utility.run_local_cmd(cmd, input="y")
            self.succeed("Hard Reset Status for IP :: %s ServiceTag :: %s ==> %s"%(server["ip"], server["serviceTag"], str(resCP).rstrip()))
            time.sleep(10)
    
    
    def racReset(self):
        """
        Performs a RAC RESET for all the servers discovered
        """
        self.getResources()
        serverList = globalVars.resourceInfo["SERVER"]
        srv = ""        
        for server in serverList:
            srv += "\"" + server["ip"] + "\","
            #===================================================================
            # cmd = "tools\plink.exe -ssh -pw calvin root@%s racadm racreset"%server["ip"]
            # resCP, statCP = utility.run_local_cmd(cmd, input="y")
            # self.succeed("Reset IDRAC for IP :: %s ServiceTag :: %s ==> %s"%(server["ip"], server["serviceTag"], str(resCP).rstrip()))
            # time.sleep(5)
            #===================================================================
        utility.log_data(srv)
        
    
    def runAllDeviceInventory(self, iteration):
        """
        Runs Inventory on the specified Device
        """
        resAD, statAD = self.getResponse("GET", "ManagedDevice")
        if not statAD:
            self.failure("","Failed to get Resource Info %s"%str(resAD), BLOCKED)        
        self.logDesc("Running Inventory on All Devices Iteration %s"%str(iteration))    
        for resource in resAD:
            refId = resource["refId"]
            self.logDesc("Running Inventory on DeviceIP:%s DeviceType:%s ServiceTag:%s"%(resource["ipAddress"], resource["deviceType"], resource["serviceTag"]))
            resMD, statMD = self.getResponse("GET", "ManagedDevice", refId = refId, responseType="xml", requestContentType="xml")
            if not statMD:
                self.failure("","Failed to get Device Info %s"%str(resMD))
            payload = resMD #.replace(self.xmlString,"")
            resMD, statMD = self.getResponse("PUT", "ManagedDevice", payload, responseType="xml", requestContentType="xml", refId=refId)        
            if not statMD:
                self.failure("","Unable to Run Inventory %s"%str(resMD))
    
    
    def syncTime(self):
        """
        Sync Time with ASM Appliance
        """        
        cmd = "tools\plink.exe -ssh -pw delladmin delladmin@%s \"date +%D-%T\""%self.applianceIP
        return utility.run_local_cmd(cmd)
    
    
    
    
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
            utility.log_data("Command  to Execute : ", command)
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
        utility.log_data("Command  output : ", "  ".join(ret["out"]))
        
        
        
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
            result, status = utility.readCsvFile(configFile)
            
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
        payload = utility.readFile(globalVars.networkPayload)
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
    
    
    
    def setupCredentials(self):
        """
        Create Credentials or Manage existing Credentials        
        """
        
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
        
        return creResponse, True
    
    
    def loadCredentialInputs(self):
        """
        Loads the Credential Information provided in Credential.csv
        """ 
      
        configFile = globalVars.credentialConfig 
       
        try:
            result, status = utility.readCsvFile(configFile)
            
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
        payload = utility.readFile(globalVars.credentialPayload)
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
    
    
    def getResourcesInfo(self):
        inputFile = globalVars.discovery 
       
        try:
            result, status = utility.readCsvFile(inputFile)
            if not status:
                return "Unable to read Discovery input File: %s"%inputFile , False
            header = result[0]      
            return [dict(zip(header,result[row])) for row in xrange(1,len(result))], True
        except:
            return "Columns mismatch in the Configuration File: %s"%inputFile, False

    def discoverServer(self, resource,  unmanaged = False):
        """
        Discovers servers within start_ip and end_ip
        """
        resDP, statDP = self.getServerDiscoveryPayload(resource, unmanaged = unmanaged)
        if not statDP:
            return resDP, False
        resDR, statDR = self.getResponse("POST", "Discovery", payload=resDP)      
        time.sleep(globalVars.defaultWaitTime)
        
            
        return resDR, statDR

    def getServerDiscoveryPayload(self, resource,unmanaged=False):
        """
        Returns Server Discovery Payload 
        """
        serverCredentialId = ""
        creName = resource["CredentialName"]
               
        if self.credentialMap.has_key(creName):
            serverCredentialId = self.credentialMap[creName]
        else:
            return "Server Credentials not defined", False
        
        
        payload = utility.readFile(globalVars.serverDiscPayload)
        payload = payload.replace("start_ip", resource["START_IP"]).replace("end_ip",
                                resource["END_IP"]).replace("default_server_cred_ref",
                                        serverCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n', '').replace('\t', '')
        return payload, True
    
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
                    utility.log_data( 'JRAF jobhistory execution elaspedTimeMillis : %s'%jobtime) 
                    utility.log_data( 'JRAF jobhistory execution status : %s'%jobStatus)  
                    break
                else:
                    jobhistoryelaspedTime= jobtime
                    utility.log_data( 'JRAF jobhistory execution elaspedTimeMillis : %s'%jobtime) 
                    utility.log_data( 'JRAF jobhistory execution status : %s'%jobStatus)
                    break
            wait = wait -1
        
        return jobhistoryelaspedTime
        

    def getjobhistoryelaspedTime(self, jobID):
        
        elaspedTime=0
        jobStatus=""
        try:
            utility.log_data("jobName is  :  %s"%jobID)
            url = self.buildUrljob("JobStatus", jobID)
            uri = globalVars.serviceUriInfo["JobStatus"] + jobID
            headers=self.generateHeaderGetDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
            startTime = datetime.datetime.now()        
            response = requests.get(url, headers=headers, verify=False)
            endTime = datetime.datetime.now()
            elapsedTime="%s"%(endTime-startTime)
            time.sleep(30)
            utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers,""), response.status_code, response.text, startTime, endTime, elapsedTime)
            data = json.loads(response.text)
            result =utility.convertUTA(data)
            result1 = result['trees']
            for resources in result1:
                elaspedTime = resources['data']['execHistory']['elaspedTimeMillis']
                jobStatus = resources['data']['execHistory']['status']
                
        except Exception as e3:
            utility.log_data( 'Exception occurred while  getting Job execution History elaspedTimeMillis ')
            utility.log_data(str(e3))

            
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
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers,""), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        return response

    
    def discoverChassis(self, resource,  unmanaged=False):
        """
        Discovers chassis with start and end ip
        """
        

        responseDisCh,statusDisCh = self.getChassisDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statusDisCh:
            utility.log_data( 'could not get payload for chassis discovery : %s'%responseDisCh) 
            
            return responseDisCh,False
        
        responseDSCH,statusDSCH = self.getResponse("POST", "Discovery", payload=responseDisCh)
        time.sleep(globalVars.defaultWaitTime)
        
        return responseDSCH,statusDSCH
    
       
    def getChassisDiscoveryPayload(self,resource,unmanaged=False):
        
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
        
        payload = utility.readFile(globalVars.chassisDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_chassis_cred_ref", 
                                        chassisCredentialId).replace("manage_in_asm", str(unmanaged).lower()).replace("default_server_cred_ref",serverCredentialId).replace("$default_switch_cred_ref",switchCredentialId)
        payload = payload.replace('\n','').replace('\t','')
        
        return payload, True    
        
        
    def discoverStorage(self, resource,  unmanaged=False):
        """
        Discovers storage with start and end ip
        """
        
        responseDisStr,statusDisStr = self.getStorageDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statusDisStr:
            return responseDisStr,False
        responseDSTRG,statusDSTRG = self.getResponse("POST", "Discovery", payload=responseDisStr)
        time.sleep(globalVars.defaultWaitTime)
        
        return responseDSTRG,statusDSTRG
    
    
        
    def getStorageDiscoveryPayload(self,resource,unmanaged=False):
        
        storageCredentialId = ""
        creName = resource["CredentialName"]
        #credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(creName):
            storageCredentialId = self.credentialMap[creName]
        else:
            return "Storage Credentials not defined", False
        
        payload = utility.readFile(globalVars.storageDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_storage_cred_ref", 
                                        storageCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        
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
    
    def getSwitchDiscoveryPayload(self,resource,unmanaged=False):
        
        switchCredentialId = ""
        creName = resource["CredentialName"]
        #credentials = self.getCredentialId(["serverCredential"])        
        if self.credentialMap.has_key(creName):
            switchCredentialId = self.credentialMap[creName]
        else:
            return "Switch Credentials not defined", False
        payload = utility.readFile(globalVars.switchDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_switch_cred_ref", 
                                        switchCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        
        return payload, True
    
    def discoverVCenter(self, resource, unmanaged=False):
        
        resVC,statVC = self.getVCenterDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statVC:
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
        payload = utility.readFile(globalVars.VCenterDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_vcenter_cred_ref", 
                                        VCenterCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        
        return payload, True
    
    def discoverVM(self, resource, unmanaged=False):
        """
        Discovers VM with start and end ip
        """
        
        resVM,statVM = self.getVMDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statVM:
            return resVM,False
        responseDSVM,statusDSVM = self.getResponse("POST", "Discovery", payload=resVM)
        time.sleep(globalVars.defaultWaitTime)
        return responseDSVM,statusDSVM
    
    def getVMDiscoveryPayload(self,resource,unmanaged=False):
        
        VMCredentialId = ""  
        creName = resource["CredentialName"]      
        if self.credentialMap.has_key(creName):
            VMCredentialId = self.credentialMap[creName]
            
        else:
            return "SCVMM Credentials not defined", False
        payload = utility.readFile(globalVars.VMDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_scvmm_cred_ref", 
                                        VMCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        
        return payload, True
    
    
    def discoverEMC(self, resource, unmanaged=False):
        """
        Discovers EnterpriseManager with start and end ip
        """
        
        resEMC,statEMC = self.getEMCDiscoveryPayload(resource, unmanaged=unmanaged)
        
        if not statEMC:
            return resEMC,False
        responseDSVM,statusDSVM = self.getResponse("POST", "Discovery", payload=resEMC)
        time.sleep(globalVars.defaultWaitTime)
        return responseDSVM,statusDSVM
    
    def getEMCDiscoveryPayload(self,resource,unmanaged=False):
        
        EMCCredentialId = ""  
        creName = resource["CredentialName"]      
        if self.credentialMap.has_key(creName):
            EMCCredentialId = self.credentialMap[creName]
            
        else:
            return "EMCCredentialId Credentials not defined", False
        payload = utility.readFile(globalVars.EnterpriseManagerDiscPayload)
        payload = payload.replace("$start_ip", resource["START_IP"]).replace("$end_ip", 
                                resource["END_IP"]).replace("$default_emc_cred_ref", 
                                        EMCCredentialId).replace("manage_in_asm", str(unmanaged).lower())
        payload = payload.replace('\n','').replace('\t','')
        
        return payload, True
    
    
    
    def chassisConfigureResource(self):
        """
        Defines new Credential with provided Username and Password
        """    
        payload = utility.readFile(globalVars.configureResourcePayload)
        payload = payload.replace("$server_cred_ref", self.credentialMap["autoServer"]).replace("$chassis_cred_ref", 
                        self.credentialMap["HVChasis"]).replace("$iom_switch_cred_ref", self.credentialMap["autoIOM"])                
        return self.getResponse("POST", "ChassisConfigureDiscover", payload)
    
    def configureResource(self):
        """
        Configure Resource 
        """
        try:
            
            
            responseDisCh,statusDisCh = self.getConfigureResourcePayload()
        
            if not statusDisCh:
                
                return responseDisCh,False
        
            responseDSCH,statusDSCH = self.getResponse("POST", "Configure", payload=responseDisCh)
            time.sleep(globalVars.defaultWaitTime)
            return responseDSCH,statusDSCH
        except Exception as e1:
            utility.log_data( 'Exception occurred while  payload for Configure Resource ')
            utility.log_data(str(e1))
    
    
    def getConfigureResourcePayload(self):
        
        
        self.getResources()
        utility.log_data(" Going to Create Template :: ")
        
        chassisId=""
        self.chassisRes = self.getReqResource(limit=1, resourceType='CHASSIS', deviceType=None)
        if len(self.chassisRes) == 0:
            return "No Chasis Discovered", False
        else:
            chassisId = self.chassisRes[0]["refid"]
        
        response =self.getManagedDeviceRefID(chassisId)
        root = ET.fromstring(response.content)
        print " ROOT : "
        print ET.tostring(root)
        indTD = root.find('config')
        payload = indTD.text
        
        return payload, True
    
    def getManagedDeviceRefID(self,refId):
        
        
        url = self.buildUrl("ManagedDevice",refId)
        uri = globalVars.serviceUriInfo["ManagedDevice"]+ "/" + refId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        
        return deploymentResponse
    
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
    
    def generateHeaderDeploy(self, uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        uri1 = uri.partition('?')
        uriFinal = uri1[0]
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uriFinal + ":" + userAgent + ":" + timestamp
        signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/xml","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers

    
    def setUpCompleteWizard(self):
        """
        Sets the Wizard Status
        """
        payload = self.readFile(globalVars.completeWizardPayload)   
        resWC, statWC = self.getResponse("PUT", "Wizard", payload)
        
        return resWC, statWC
    
    def postRequest(self,serviceName,payload):
        
        url = self.buildUrl(serviceName)
        
        uri = globalVars.serviceUriInfo[serviceName]
        
        headers=self.generateHeaderforDiscoverChassis(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
       
        startTime = datetime.datetime.now()
        response = requests.post(url, data=payload, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)  
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        time.sleep(globalVars.defReqWaitTime)
        if response.status_code in (200, 201, 202, 203, 204): 
            # requests.codes.OK:
            if response.text != "":
                data = json.loads(response.text)
                return utility.convertUTA(data), True
            else:
                return "No information found for %s"%str(serviceName), True
        else:
            return str(response.status_code) + " " + str(utility.convertUTA(response.text)), False
        
        
        
        
    def createTemplate(self, jsonPayloadFile):
        
       
        self.setNetworkList()
        
        json_data=open(jsonPayloadFile).read()

        data = json.loads(json_data)
        result =utility.convertUTA(data)
        
        
        templateName= result["Template"]["Name"]
        templateDescription= result["Template"]["Description"]
        templateFlow= result["Template"]["Flow"]
        
        globalVars.template_name = templateName
        globalVars.template_description = templateDescription
        
        storageComponentCount = result["Storage"]["Instances"]
        storageType = result["Storage"]["Type"]
        storageSize = result["Storage"]["Size"]
        storageIPAdress = result["Storage"]["IPAdress"]
        IqnIPAddresses = result["Storage"]["Initiator_IQN_IP_Addresses"]
        operatingSystemName = result["Storage"]["Operating_SystemName_Compellent"]
        portTypeCompellent = result["Storage"]["PortType_Compellent"]
        storageAuthType = result["Storage"]["Auth_Type"]
        storageCHAPUser = result["Storage"]["CHAP_User"]
        storageCHAPPassword = result["Storage"]["CHAP_Password"]
        storageAggregateName = result["Storage"]["Aggregate_Name_NetApp"]
        storageSpaceReservationMode = result["Storage"]["Space_Reservation_Mode_NetApp"]
        storageNFSTargetIP = result["Storage"]["NFS_Target_IP_NetApp"]
        
        
        serverComponentCount = result["Server"]["Instances"]
        serverTrgetBootDeviceValue = result["Server"]["target_boot_device_value"]
        serverOSImageValue = result["Server"]["OSImage_Value"]
        serverPoolName = result["Server"]["ServerPoolName"]
        globalVars.serverPoolName=serverPoolName
        ServersIPForPool = result["Server"]["ServersIPForPool"]
        iSCSIInitiator = result["Server"]["iSCSI_Initiator"]
        serversHostNameTemplate = result["Server"]["HostNameTemplate"]
        networkType = result["Server"]["Networks"]
        
        serverOsImageVersion = result["Server"]["Os_Image_Version"]
        serverProductKey = result["Server"]["Product_Key"]
        domain_name = result["Server"]["domain_name"]
        FQ_Domain_Name= result["Server"]["FQ_Domain_Name"] 
        domainAdminUsername = result["Server"]["Domain_Admin_Username"]
        domainAdminPassword = result["Server"]["Domain_Admin_Password"]
        installEqualLogicMEM = result["Server"]["Install_EqualLogic_MEM"]
        
        clusterComponentCount=  result["Cluster"]["Instances"]
        clusterType=  result["Cluster"]["Type"]
        clusterIPAdress=  result["Cluster"]["IPAdress"]
        existingClusterName=  result["Cluster"]["ExistingClusterName"]
        clusterHA=  result["Cluster"]["HA_Config"]
        clusterDRS=  result["Cluster"]["DRS_Config"]
        clusterVSANEnabled=  result["Cluster"]["VSAN_Enabled"]
        clusterSwitchType=  result["Cluster"]["SwitchType"]
        clusterIPHyperV=  result["Cluster"]["ClusterIP_HyperV"]
        
        vmComponentCount= result["VM"]["Instances"]
        vmType= result["VM"]["Type"]
        vmAutogenerateHostName= result["VM"]["AutogenerateHostName"]
        vmOS_Image= result["VM"]["OS_Image"]
        vmOs_Image_Version= result["VM"]["Os_Image_Version"]
        vmOS_Image_Type= result["VM"]["OS_Image_Type"]
        vmProduct_Key= result["VM"]["Product_Key"]
        vmVirtual_Disk_Size= result["VM"]["Virtual_Disk_Size_GB"]
        
        vmHostName= result["VM"]["hostname_HyperVCloneVM"]
        vmdescription= result["VM"]["description_HyperVCloneVM"]
        vmTemplateName= result["VM"]["vm_template_name"]
        blockDynamicOptimization= result["VM"]["block_dynamic_optimization"]
        highlyAvailable= result["VM"]["highly_available"]
        cpuCount= result["VM"]["CPU_Count"]
        memoryInMB= result["VM"]["Memory_MB"]
        startAction= result["VM"]["start_action"]
        stopAction= result["VM"]["stop_action"]
        vmPath= result["VM"]["path"]
        
        applicationComponentCount = result["Application"]["Instances"]
        applicationType = result["Application"]["Type"]
        installPackages = result["Application"]["install_packages"]
        uploadShare = result["Application"]["upload_share"]
        uploadFile = result["Application"]["upload_file"]
        executeFileCommand = result["Application"]["execute_file_command"]
        yumProxy = result["Application"]["yum_proxy"]
        windowsShare = result["Application"]["windows_postinstall_share"]
        windowsInstallCommand = result["Application"]["windows_install_command"]
        
        scaleupStorageComponentCount = result["Scaleup"]["Storage"]["Instances"]
        scaleupStorageType = result["Scaleup"]["Storage"]["Type"]
        scaleupStorageSize = result["Scaleup"]["Storage"]["Size"]
        scaleupIqnIPAddresses = result["Scaleup"]["Storage"]["Initiator_IQN_IP_Addresses"]
        scaleupOperatingSystemName = result["Scaleup"]["Storage"]["Operating_SystemName_Compellent"]
        scaleupPortTypeCompellent = result["Scaleup"]["Storage"]["PortType_Compellent"]
        
        
        
        scaleupStorageIPAdress = result["Scaleup"]["Storage"]["IPAdress"]
        
        
        scaleupStorageAuthType = result["Scaleup"]["Storage"]["Auth_Type"]
        scaleupStorageCHAPUser = result["Scaleup"]["Storage"]["CHAP_User"]
        scaleupStorageCHAPPassword = result["Scaleup"]["Storage"]["CHAP_Password"]
        scaleupStorageAggregateName = result["Scaleup"]["Storage"]["Aggregate_Name_NetApp"]
        scaleupStorageSpaceReservationMode = result["Scaleup"]["Storage"]["Space_Reservation_Mode_NetApp"]
        scaleupStorageNFSTargetIP = result["Scaleup"]["Storage"]["NFS_Target_IP_NetApp"]
        
        
        scaleupServerComponentCount = result["Scaleup"]["Server"]["Instances"]
        scaleupServerTrgetBootDeviceValue = result["Scaleup"]["Server"]["target_boot_device_value"]
        scaleupServerOSImageValue = result["Scaleup"]["Server"]["OSImage_Value"]
        scaleupISCSIInitiator = result["Scaleup"]["Server"]["iSCSI_Initiator"]
        scaleupHostNameTemplate = result["Scaleup"]["Server"]["HostNameTemplate"]
        
        scaleupNetworkType = result["Scaleup"]["Server"]["Networks"]
        
        scaleupServerOsImageVersion = result["Scaleup"]["Server"]["Os_Image_Version"]
        scaleupServerProductKey = result["Scaleup"]["Server"]["Product_Key"]
        scaleupDomain_name = result["Scaleup"]["Server"]["domain_name"]
        scaleupFQ_Domain_Name= result["Scaleup"]["Server"]["FQ_Domain_Name"] 
        scaleupDomainAdminUsername = result["Scaleup"]["Server"]["Domain_Admin_Username"]
        scaleupDomainAdminPassword = result["Scaleup"]["Server"]["Domain_Admin_Password"]
        scaleupInstallEqualLogicMEM = result["Scaleup"]["Server"]["Install_EqualLogic_MEM"]
        
        scaleupClusterComponentCount=  result["Scaleup"]["Cluster"]["Instances"]
        scaleupClusterType=  result["Scaleup"]["Cluster"]["Type"]
        scaleupExistingClusterName=  result["Scaleup"]["Cluster"]["ExistingClusterName"]
        scaleupClusterHA=  result["Scaleup"]["Cluster"]["HA_Config"]
        scaleupClusterDRS=  result["Scaleup"]["Cluster"]["DRS_Config"]
        
        
        
        scaleupClusterIPAdress=  result["Scaleup"]["Cluster"]["IPAdress"]
        
        scaleupClusterVSANEnabled=  result["Scaleup"]["Cluster"]["VSAN_Enabled"]
        scaleupClusterSwitchType=  result["Scaleup"]["Cluster"]["SwitchType"]
        scaleupClusterIPHyperV=  result["Scaleup"]["Cluster"]["ClusterIP_HyperV"]
        
        scaleupVmComponentCount= result["Scaleup"]["VM"]["Instances"]
        scaleupVmType= result["Scaleup"]["VM"]["Type"]
        scaleupVmAutogenerateHostName= result["Scaleup"]["VM"]["AutogenerateHostName"]
        scaleupVmOS_Image= result["Scaleup"]["VM"]["OS_Image"]
        scaleupVmOs_Image_Version= result["Scaleup"]["VM"]["Os_Image_Version"]
        scaleupVmOS_Image_Type= result["Scaleup"]["VM"]["OS_Image_Type"]
        scaleupVmProduct_Key= result["Scaleup"]["VM"]["Product_Key"]
        scaleupVmVirtual_Disk_Size= result["VM"]["Virtual_Disk_Size_GB"]
        
        scaleupVmHostName= result["Scaleup"]["VM"]["hostname_HyperVCloneVM"]
        scaleupVmdescription= result["Scaleup"]["VM"]["description_HyperVCloneVM"]
        scaleupVmTemplateName= result["Scaleup"]["VM"]["vm_template_name"]
        scaleupBlockDynamicOptimization= result["Scaleup"]["VM"]["block_dynamic_optimization"]
        scaleupHighlyAvailable= result["Scaleup"]["VM"]["highly_available"]
        scaleupCpuCount= result["Scaleup"]["VM"]["CPU_Count"]
        scaleupMemoryInMB= result["Scaleup"]["VM"]["Memory_MB"]
        scaleupStartAction= result["Scaleup"]["VM"]["start_action"]
        scaleupStopAction= result["Scaleup"]["VM"]["stop_action"]
        scaleupVmPath= result["Scaleup"]["VM"]["path"]
        
        scaleupApplicationComponentCount = result["Scaleup"]["Application"]["Instances"]
        scaleupApplicationType = result["Scaleup"]["Application"]["Type"]
        scaleupInstallPackages = result["Scaleup"]["Application"]["install_packages"]
        scaleupUploadShare = result["Scaleup"]["Application"]["upload_share"]
        scaleupUploadFile = result["Scaleup"]["Application"]["upload_file"]
        scaleupExecuteFileCommand = result["Scaleup"]["Application"]["execute_file_command"]
        scaleupYumProxy = result["Scaleup"]["Application"]["yum_proxy"]
        scaleupWindowsShare = result["Scaleup"]["Application"]["windows_postinstall_share"]
        scaleupWindowsInstallCommand = result["Scaleup"]["Application"]["windows_install_command"]
            
           
        payload = utility.readFile(globalVars.CommonTemplatePayload)
        finalStoragePayload=""
        finalServerPayload=""
        finalClusterPayload=""
        finalVMPayload=""
        finalApplicationPayload=""
        serverRelatedComponentFinalPayload=""
        storageRelatedComponentFinalPayload=""
        clusterRelatedComponentFinalPayload=""
        vmRelatedComponentFinalPayload=""
        ApplicationRelatedComponentFinalPayload=""
        serverAssociatedComponentFinalPayload=""
        storageAssociatedComponentFinalPayload=""
        clusterAssociatedComponentFinalPayload=""
        vmAssociatedComponentFinalPayload=""
        ApplicationAssociatedComponentFinalPayload=""
        
        ComponentCount=0
        
        storageCount =1    
        while storageComponentCount:
            
            if storageType == "Equallogic":
                
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.EqualLogic_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.EqualLogicComponent_Name+str(storageCount))
                storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = ""
                associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.EqualLogic_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.EqualLogicComponent_Name+str(storageCount))
                storageAssociatedComponentFinalPayload = storageAssociatedComponentFinalPayload + associatedComponentpayload
            
                
                storageComponentpayload = ""
                storageComponentpayload = utility.readFile(globalVars.EquallogicComponentPayload)
                storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.EqualLogic_Components_Id+str(storageCount)).replace("$Storage_name",globalVars.EqualLogicComponent_Name+str(storageCount)).replace("$Storage_componentID",globalVars.EqualLogic_componentID+str(ComponentCount)).replace("$VolumeValue","$Volume_"+str(storageCount)).replace("$VolumeSize","$VolumeSize_"+str(storageCount)).replace("$IqnipAddress",IqnIPAddresses).replace("$authTpye",storageAuthType).replace("$chapUserName",storageCHAPUser).replace("$chapPasswd",storageCHAPPassword)
                finalStoragePayload = finalStoragePayload + storageComponentpayload
                storageComponentCount -= 1
                
            elif storageType == "Compellent":
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.Compellent_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.CompellentComponent_Name+str(storageCount))
                storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = ""
                associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.Compellent_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.CompellentComponent_Name+str(storageCount))
                storageAssociatedComponentFinalPayload = storageAssociatedComponentFinalPayload + associatedComponentpayload
            
                
                
                storageComponentpayload = ""
                storageComponentpayload = utility.readFile(globalVars.CompellentComponentPayload)
                storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.Compellent_Components_Id+str(storageCount)).replace("$Storage_name",globalVars.CompellentComponent_Name+str(storageCount)).replace("$Storage_componentID",globalVars.Compellent_componentID+str(ComponentCount)).replace("$VolumeValue","$Volume_"+str(storageCount)).replace("$VolumeSize","$VolumeSize_"+str(storageCount)).replace("$OperatingSystemName",operatingSystemName).replace("$PortTypeName",portTypeCompellent)
                finalStoragePayload = finalStoragePayload + storageComponentpayload
                storageComponentCount -= 1
                
            elif storageType == "NetApp":
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.NetApp_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.NetAppComponent_Name+str(storageCount))
                storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = ""
                associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.NetApp_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.NetAppComponent_Name+str(storageCount))
                storageAssociatedComponentFinalPayload = storageAssociatedComponentFinalPayload + associatedComponentpayload
            
                
                
                storageComponentpayload = ""
                storageComponentpayload = utility.readFile(globalVars.netAPPComponentPayload)
                storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.NetApp_Components_Id+str(storageCount)).replace("$Storage_name",globalVars.NetAppComponent_Name+str(storageCount)).replace("$Storage_componentID",globalVars.NetApp_componentID+str(ComponentCount)).replace("$VolumeValue","$Volume_"+str(storageCount)).replace("$VolumeSize","$VolumeSize_"+str(storageCount)).replace("$Space_Mode",storageSpaceReservationMode).replace("$NFS_IP",storageNFSTargetIP).replace("$Aggregate_Name",storageAggregateName)
                finalStoragePayload = finalStoragePayload + storageComponentpayload
                storageComponentCount -= 1
                
                
            storageCount +=1
            ComponentCount +=1
        
        serverCount =1    
        while serverComponentCount:
            
            relatedComponentpayload = ""
            relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
            relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.Server_Components_Id+str(serverCount)).replace("$Resource_Name",globalVars.ServerComponent_Name+str(serverCount))
            serverRelatedComponentFinalPayload = serverRelatedComponentFinalPayload + relatedComponentpayload
            
            associatedComponentpayload = ""
            associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
            associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.Server_Components_Id+str(serverCount)).replace("$Resource_Name",globalVars.ServerComponent_Name+str(serverCount))
            serverAssociatedComponentFinalPayload = serverAssociatedComponentFinalPayload + associatedComponentpayload
            
            serverComponentpayload = ""
            if networkType == "ESXI_EQL_CONVERGED_2_REDUNDANCY_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCON2REDPART)
            elif networkType == "ESXI_EQL_CONVERGED_2_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCONV2REDU)
            elif networkType == "ESXI_EQL_CONVERGED_2_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCON2PART)
            elif networkType == "ESXI_EQL_CONVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServESXIEQLCOD2)
            elif networkType == "ESXI_FCOE_CONVERGED_2_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIFCOECONVE2PART)
            elif networkType == "ESXI_FCOE_CONVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIFCOECOND2)
            elif networkType == "ESXI_CPL_ISCSI_CONVERGED_2_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSICONVD2PART)
            elif networkType == "ESXI_CPL_ISCSI_CONVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSICONV2)
            elif networkType == "ESXI_EQL_CONVERGED_4_REDUNDANCY_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCON4REDPART)
            elif networkType == "ESXI_EQL_CONVERGED_4_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCONV4REDU)
            elif networkType == "ESXI_EQL_CONVERGED_4_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCON4PART)
            elif networkType == "ESXI_EQL_CONVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServESXIEQLCOD4)
            elif networkType == "ESXI_EQL_DIVERGED_4_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER4REDU)
            elif networkType == "ESXI_EQL_DIVERGED_4_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER4PART)
            elif networkType == "ESXI_EQL_DIVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServESXIEQLDIVER4)
            elif networkType == "ESXI_EQL_DIVERGED_4_REDUNDANCY_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER4REDPAR)
            elif networkType == "ESXI_EQL_DIVERGED_2_REDUNDANCY_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER2REDPAR)
            elif networkType == "ESXI_EQL_DIVERGED_2_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER2REDU)
            elif networkType == "ESXI_EQL_DIVERGED_2_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER2PART)
            elif networkType == "ESXI_EQL_DIVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServESXIEQLDIVER2)
            elif networkType == "ESXI_CPL_FC_DIVERGED_2_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER2PART)
            elif networkType == "ESXI_CPL_FC_DIVERGED_2_REDUNDANCY_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER2REDPAR)
            elif networkType == "ESXI_CPL_FC_DIVERGED_2_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER2REDU)
            elif networkType == "ESXI_CPL_FC_DIVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServESXICPLFCDIVER2)
            elif networkType == "ESXI_CPL_FC_DIVERGED_4_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER4PART)
            elif networkType == "ESXI_CPL_FC_DIVERGED_4_REDUNDANCY_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER4REDPAR)
            elif networkType == "ESXI_CPL_FC_DIVERGED_4_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER4REDU)
            elif networkType == "ESXI_CPL_FC_DIVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServESXICPLFCDIVER4)
            elif networkType == "ESXI_NETAPP_CONVERGED_2_REDUNDANCY_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCON2REDPART)
            elif networkType == "ESXI_NETAPP_CONVERGED_2_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCONV2REDU)
            elif networkType == "ESXI_NETAPP_CONVERGED_2_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCON2PART)
            elif networkType == "ESXI_NETAPP_CONVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServESXINETAPPCOD2)
            elif networkType == "ESXI_NETAPP_CONVERGED_4_REDUNDANCY_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCON4REDPART)
            elif networkType == "ESXI_NETAPP_CONVERGED_4_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCONV4REDU)
            elif networkType == "ESXI_NETAPP_CONVERGED_4_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCON4PART)
            elif networkType == "ESXI_NETAPP_CONVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServESXINETAPPCOD4)
            elif networkType == "HyperV_EQL_CONVERGED_2_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLCONV2REDU)
            elif networkType == "HyperV_EQL_CONVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLCONV2)
            elif networkType == "HyperV_EQL_CONVERGED_4_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLCONV4REDU)
            elif networkType == "HyperV_EQL_CONVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLCONV4)
            elif networkType == "HyperV_EQL_DIVERGED_2_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLDIVER2REDU)
            elif networkType == "HyperV_EQL_DIVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLDIVER2)
            elif networkType == "HyperV_EQL_DIVERGED_4_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLDIVER4REDU)
            elif networkType == "HyperV_EQL_DIVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLDIVER4)
            elif networkType == "HyperV_CPL_ISCSI_DIVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLISCSIDIVER2)
            elif networkType == "HyperV_CPL_ISCSI_DIVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLISCSIDIVER4)
            elif networkType == "HyperV_CPL_ISCSI_CONVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLISCSICONV2)
            elif networkType == "HyperV_CPL_ISCSI_CONVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLISCSICONV4)
            elif networkType == "HyperV_CPL_FC_DIVERGED_2_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLFCDIVER2REDU)
            elif networkType == "HyperV_CPL_FC_DIVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLFCDIVER2)
            elif networkType == "HyperV_CPL_FC_DIVERGED_4_REDUNDANCY":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLFCDIVER4REDU)
            elif networkType == "HyperV_CPL_FC_DIVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLFCDIVER4)
            elif networkType == "ESXI_FCOE_CONVERGED_4_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIFCOECONVE4PART)
            elif networkType == "ESXI_FCOE_CONVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServerESXIFCOECOND4)
            elif networkType == "ESXI_CPL_ISCSI_CONVERGED_4_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSICONVD4PART)
            elif networkType == "ESXI_CPL_ISCSI_CONVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSICONV4)
            elif networkType == "ESXI_CPL_ISCSI_DIVERGED_2_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSIDIVER2PART)
            elif networkType == "ESXI_CPL_ISCSI_DIVERGED_2":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSIDIVER2)
            elif networkType == "ESXI_CPL_ISCSI_DIVERGED_4_PARTITION":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSIDIVER4PART)
            elif networkType == "ESXI_CPL_ISCSI_DIVERGED_4":
                serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSIDIVER4)
            else:
                self.failure("Network Type Defined in JSON file is not correct %s"%str(networkType))
            
            
            
            serverComponentpayload = serverComponentpayload.replace("$Server_Components_Id",globalVars.Server_Components_Id+str(serverCount)).replace("$ServerComponent_Name",globalVars.ServerComponent_Name+str(serverCount)).replace("$Server_componentID",globalVars.Server_componentID+str(ComponentCount)).replace("$target_boot_device_value",serverTrgetBootDeviceValue).replace("$OSImageValue","$OSImage_"+str(serverCount)).replace("$iscsiinitiator",iSCSIInitiator).replace("$HostNameTemplate",serversHostNameTemplate).replace("$os_image_version",serverOsImageVersion).replace("$product_key",serverProductKey).replace("$domain_name",domain_name).replace("$fqdn",FQ_Domain_Name).replace("$domainadminuser",domainAdminUsername).replace("$DomainAdminPsswd",domainAdminPassword)
            finalServerPayload = finalServerPayload + serverComponentpayload
            serverComponentCount -= 1
            serverCount +=1
            ComponentCount +=1
            
        clusterCount=1
        while clusterComponentCount:
            
            if clusterType == "VMWareCluster":
                
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.VMWareCluster_Components_Id+str(clusterCount)).replace("$Resource_Name",globalVars.VMWareClusterComponent_Name+str(clusterCount))
                clusterRelatedComponentFinalPayload = clusterRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = ""
                associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.VMWareCluster_Components_Id+str(clusterCount)).replace("$Resource_Name",globalVars.VMWareClusterComponent_Name+str(clusterCount))
                clusterAssociatedComponentFinalPayload = clusterAssociatedComponentFinalPayload + associatedComponentpayload
                
                
                clusterComponentpayload = ""
                if len(existingClusterName.strip()) >1:
                    clusterComponentpayload = utility.readFile(globalVars.VMWareExistingClusterComponentPayload)
                else:
                    clusterComponentpayload = utility.readFile(globalVars.VMWareClusterComponentPayload)
                
                clusterComponentpayload = clusterComponentpayload.replace("$Cluster_Components_Id",globalVars.VMWareCluster_Components_Id+str(clusterCount)).replace("$Cluster_name",globalVars.VMWareClusterComponent_Name+str(clusterCount)).replace("$Cluster_componentID",globalVars.VMWareCluster_componentID+str(ComponentCount)).replace("$HA_Config",clusterHA).replace("$DRS_Config",clusterDRS).replace("$vSAN_Enabled",clusterVSANEnabled).replace("$ExistingCluster",existingClusterName).replace("$SwitchType",clusterSwitchType)
                finalClusterPayload = finalClusterPayload + clusterComponentpayload
                clusterComponentCount -= 1
                
            elif clusterType == "HyperVCluster":
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.HyperVCluster_Components_Id+str(clusterCount)).replace("$Resource_Name",globalVars.HyperVClusterComponent_Name+str(clusterCount))
                clusterRelatedComponentFinalPayload = clusterRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = ""
                associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.HyperVCluster_Components_Id+str(clusterCount)).replace("$Resource_Name",globalVars.HyperVClusterComponent_Name+str(clusterCount))
                clusterAssociatedComponentFinalPayload = clusterAssociatedComponentFinalPayload + associatedComponentpayload
                
                clusterComponentpayload = ""
                clusterComponentpayload = utility.readFile(globalVars.HyperVClusterComponentPayload)
                clusterComponentpayload = clusterComponentpayload.replace("$Cluster_Components_Id",globalVars.HyperVCluster_Components_Id+str(clusterCount)).replace("$Cluster_name",globalVars.HyperVClusterComponent_Name+str(clusterCount)).replace("$Cluster_componentID",globalVars.HyperVCluster_componentID+str(ComponentCount)).replace("$ClusterIPHyperV",clusterIPHyperV)
                finalClusterPayload = finalClusterPayload + clusterComponentpayload
                clusterComponentCount -= 1
                
                
            clusterCount +=1
            ComponentCount +=1
        
            
        vmCount=1
        while vmComponentCount:
            
            if vmType == "VMWareVM":
                
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.VMWareVM_Components_Id+str(vmCount)).replace("$Resource_Name",globalVars.VMWareVMComponent_Name+str(vmCount))
                vmRelatedComponentFinalPayload = vmRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = ""
                associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.VMWareVM_Components_Id+str(vmCount)).replace("$Resource_Name",globalVars.VMWareVMComponent_Name+str(vmCount))
                vmAssociatedComponentFinalPayload = vmAssociatedComponentFinalPayload + associatedComponentpayload
                
                vmComponentpayload = ""
                vmComponentpayload = utility.readFile(globalVars.VMWareVMComponentPayload)
                vmComponentpayload = vmComponentpayload.replace("$VM_Components_Id",globalVars.VMWareVM_Components_Id+str(vmCount)).replace("$VM_name",globalVars.VMWareVMComponent_Name+str(vmCount)).replace("$VM_componentID",globalVars.VMWareVM_componentID+str(ComponentCount)).replace("$VMHostName","$VMHost_"+str(vmCount)).replace("$AutogenerateHostName",vmAutogenerateHostName).replace("$OS_Image",vmOS_Image).replace("$ImageVersion",vmOs_Image_Version).replace("$OSImageType",vmOS_Image_Type).replace("$product_key",vmProduct_Key).replace("$memoryvalue",memoryInMB).replace("$disksizevalue",vmVirtual_Disk_Size).replace("$cpucountvalue",cpuCount)
                finalVMPayload = finalVMPayload + vmComponentpayload
                vmComponentCount -= 1
                
            elif vmType == "HyperVCloneVM":
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.CloneVMhyperV_Components_Id+str(vmCount)).replace("$Resource_Name",globalVars.CloneVMhyperVComponent_Name+str(vmCount))
                vmRelatedComponentFinalPayload = vmRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = ""
                associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.CloneVMhyperV_Components_Id+str(vmCount)).replace("$Resource_Name",globalVars.CloneVMhyperVComponent_Name+str(vmCount))
                vmAssociatedComponentFinalPayload = vmAssociatedComponentFinalPayload + associatedComponentpayload
                
                vmComponentpayload = ""
                vmComponentpayload = utility.readFile(globalVars.CloneVMhyperVComponentPayload)
                vmComponentpayload = vmComponentpayload.replace("$VM_Components_Id",globalVars.CloneVMhyperV_Components_Id+str(vmCount)).replace("$VM_name",globalVars.CloneVMhyperVComponent_Name+str(vmCount)).replace("$VM_componentID",globalVars.CloneVMhyperV_componentID+str(ComponentCount)).replace("$hostname",vmHostName).replace("$description",vmdescription).replace("$centosvmtemplatename",vmTemplateName).replace("$path",vmPath).replace("$blockdynamicoptimization",blockDynamicOptimization).replace("$highlyavailable",highlyAvailable).replace("$cpucount",cpuCount).replace("$memorymb",memoryInMB).replace("$startaction",startAction).replace("$stopaction",stopAction)
                finalVMPayload = finalVMPayload + vmComponentpayload
                vmComponentCount -= 1
                
                
            vmCount +=1
            ComponentCount +=1
            
            
            
        applicationCount=1
        while applicationComponentCount:
            
            if applicationType == "LinuxPostInstall":
                
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.LinuxPostInstall_Components_Id+str(applicationCount)).replace("$Resource_Name",globalVars.LinuxPostInstall_Name+str(applicationCount))
                ApplicationRelatedComponentFinalPayload = ApplicationRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = ""
                associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.LinuxPostInstall_Components_Id+str(applicationCount)).replace("$Resource_Name",globalVars.LinuxPostInstall_Name+str(applicationCount))
                ApplicationAssociatedComponentFinalPayload = ApplicationAssociatedComponentFinalPayload + associatedComponentpayload
            
                applicationComponentpayload = ""
                applicationComponentpayload = utility.readFile(globalVars.LinuxPostInstallComponentPayload)
                applicationComponentpayload = applicationComponentpayload.replace("$Application_Components_Id",globalVars.LinuxPostInstall_Components_Id+str(applicationCount)).replace("$Application_name",globalVars.LinuxPostInstall_Name+str(applicationCount)).replace("$Application_componentID",globalVars.LinuxPostInstall_componentID+str(ComponentCount)).replace("$installPackages",installPackages).replace("$uploadShare",uploadShare).replace("$uploadFile",uploadFile).replace("$executeFileCommand",executeFileCommand).replace("$yumProxy",yumProxy)
                finalApplicationPayload = finalApplicationPayload + applicationComponentpayload
                applicationComponentCount -= 1
                
            elif applicationType == "WindowPostInstall":
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.CloneVMhyperV_Components_Id+str(applicationCount)).replace("$Resource_Name",globalVars.CloneVMhyperVComponent_Name+str(applicationCount))
                ApplicationRelatedComponentFinalPayload = ApplicationRelatedComponentFinalPayload + relatedComponentpayload
                
                associatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.CloneVMhyperV_Components_Id+str(applicationCount)).replace("$Resource_Name",globalVars.CloneVMhyperVComponent_Name+str(applicationCount))
                ApplicationAssociatedComponentFinalPayload = ApplicationAssociatedComponentFinalPayload + associatedComponentpayload
                
                applicationComponentpayload = ""
                applicationComponentpayload = utility.readFile(globalVars.WindowsPostInstallComponentPayload)
                applicationComponentpayload = applicationComponentpayload.replace("$VM_Components_Id",globalVars.CloneVMhyperV_Components_Id+str(applicationCount)).replace("$VM_name",globalVars.CloneVMhyperVComponent_Name+str(applicationCount)).replace("$VM_componentID",globalVars.CloneVMhyperV_componentID+str(ComponentCount)).replace("$windowsPostinstallShare",windowsShare).replace("$installCommand",windowsInstallCommand).replace("$uploadFile",uploadFile).replace("$executeFileCommand",executeFileCommand)
                finalApplicationPayload = finalApplicationPayload + applicationComponentpayload
                applicationComponentCount -= 1
                
                
            applicationCount +=1
            ComponentCount +=1
        
        
                
        
        finalStoragePayload = finalStoragePayload.replace("$ServerComponentsEntry",serverRelatedComponentFinalPayload).replace("$ServerAssociatedComponentsEntry",serverAssociatedComponentFinalPayload)
        finalServerPayload = finalServerPayload.replace("$StorageComponentsEntry",storageRelatedComponentFinalPayload).replace("$ClusterComponentsEntry",clusterRelatedComponentFinalPayload).replace("$StorageAssociatedComponentsEntry",storageAssociatedComponentFinalPayload).replace("$ClusterAssociatedComponentsEntry",clusterAssociatedComponentFinalPayload)
        finalClusterPayload = finalClusterPayload.replace("$ServerComponentsEntry",serverRelatedComponentFinalPayload).replace("$VMComponentsEntry",vmRelatedComponentFinalPayload).replace("$ServerAssociatedComponentsEntry",serverAssociatedComponentFinalPayload).replace("$VMAssociatedComponentsEntry",vmAssociatedComponentFinalPayload)
        finalVMPayload = finalVMPayload.replace("$ApplicationComponentsEntry",ApplicationRelatedComponentFinalPayload).replace("$ClusterComponentsEntry",clusterRelatedComponentFinalPayload).replace("$ApplicationAssociatedComponentsEntry",ApplicationAssociatedComponentFinalPayload).replace("$ClusterAssociatedComponentsEntry",clusterAssociatedComponentFinalPayload)
        finalApplicationPayload = finalApplicationPayload.replace("$VMComponentsEntry",vmRelatedComponentFinalPayload)
        
        i=1
        for osImage in serverOSImageValue:
            finalServerPayload = finalServerPayload.replace("$OSImage_"+str(i),osImage)
            i +=1
       
        j=1
        for strsize in storageSize:
            finalStoragePayload = finalStoragePayload.replace("$VolumeSize_"+str(j),strsize)
            j +=1
       
        finalPayload = payload.replace("$template_name",templateName).replace("$template_description",templateDescription).replace("$login_user",globalVars.userName).replace("$StorageComponent",finalStoragePayload).replace("$ServerComponent",finalServerPayload).replace("$ClusterComponent",finalClusterPayload).replace("$VMComponent",finalVMPayload).replace("$ApplicationComponent",finalApplicationPayload)
        
        self.getResources()
        
        utility.log_data(" Going to Create Template :: ")
        storageId =""
        vcenterId=""
        scvmmId=""
        
        templateResponse=""
        
        if storageType == "Equallogic":
            equalLogicList = globalVars.resourceInfo['STORAGE']
            print " Equallogic List : %s"%str(equalLogicList)
            for device in equalLogicList:
                if device['ip']==storageIPAdress:
                    storageId = device['refid']
            
            if len(equalLogicList) == 0:
                utility.log_data("Required no. of Storage(Equallagic) not available")
                
            
        
        elif storageType == "Compellent":
            
            compellentList = globalVars.resourceInfo['COMPELLENT']
            print " COMPELLENT LIST : %s"%str(compellentList)
            for device in compellentList:
                if device['ip']==storageIPAdress:
                    storageId = device['refid']
            
            if len(compellentList) == 0:
                utility.log_data("Required no. of COMPELLENT not available")
                
        elif storageType == "NetApp":
            
            netAPPList = globalVars.resourceInfo['NETAPP']
            print " NETAPP LIST : %s"%str(netAPPList)
            for device in netAPPList:
                if device['ip']==storageIPAdress:
                    storageId = device['refid']
            
            if len(netAPPList) == 0:
                utility.log_data("Required no. of NETAPP not available")
                
              
            
        if clusterType == "VMWareCluster":
            vCenterList = globalVars.resourceInfo['VCENTER']
            print " VCENTER List : %s"%str(vCenterList)
            for device in vCenterList:
                if device['ip']==clusterIPAdress:
                    vcenterId = device['refid']
            
            if len(vCenterList) == 0:
                utility.log_data("Required no. of vCENTER not available")
                
            
        
        elif clusterType == "HyperVCluster":
            
            scvmmList = globalVars.resourceInfo['SCVMM']
            print " SCVMM LIST : %s"%str(scvmmList)
            for device in scvmmList:
                if device['ip']==clusterIPAdress:
                    scvmmId = device['refid']
            
            if len(scvmmList) == 0:
                utility.log_data("Required no. of SCVMM not available")
                
                
                
                
                
              
        serverList = globalVars.resourceInfo['SERVER']  
        
        
        self.createPoolServer(ServersIPForPool, serverList, serverPoolName)
            
        globalVars.refIdVCenter = vcenterId
        globalVars.refIdSCVMM = scvmmId
        globalVars.refIdEQLogic = storageId
        
        
        globalVars.scaleupStorageComponent = scaleupStorageComponentCount
        globalVars.scaleupServerComponent = scaleupServerComponentCount
        globalVars.scaleupClusterComponent = scaleupClusterComponentCount
        globalVars.scaleupVmComponent = scaleupVmComponentCount
        globalVars.scaleupApplicationComponent = scaleupApplicationComponentCount
        
        if (scaleupStorageComponentCount >0 or scaleupServerComponentCount >0 or scaleupClusterComponentCount>0 or scaleupVmComponentCount >0 or scaleupApplicationComponentCount>0):
            self.scaleUPEnable = True
        else:
            self.scaleUPEnable = False
            
        
        if self.scaleUPEnable:
                
            while scaleupStorageComponentCount:
            
                if scaleupStorageType == "Equallogic":
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.EqualLogic_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.EqualLogicComponent_Name+str(storageCount))
                    storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
            
                    storageComponentpayload = ""
                    storageComponentpayload = utility.readFile(globalVars.EquallogicComponentPayload)
                    storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.EqualLogic_Components_Id+str(storageCount)).replace("$Storage_name",globalVars.EqualLogicComponent_Name+str(storageCount)).replace("$Storage_componentID",globalVars.EqualLogic_componentID+str(ComponentCount)).replace("$VolumeValue","$Volume_"+str(storageCount)).replace("$VolumeSize","$VolumeSize_"+str(storageCount)).replace("$IqnipAddress",scaleupIqnIPAddresses).replace("$authTpye",scaleupStorageAuthType).replace("$chapUserName",scaleupStorageCHAPUser).replace("$chapPasswd",scaleupStorageCHAPPassword)
                    self.finalScaleupStoragePayload = self.finalScaleupStoragePayload + storageComponentpayload
                    scaleupStorageComponentCount -= 1
                
                elif scaleupStorageType == "Compellent":
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.Compellent_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.CompellentComponent_Name+str(storageCount))
                    storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
            
                    storageComponentpayload = ""
                    storageComponentpayload = utility.readFile(globalVars.CompellentComponentPayload)
                    storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.Compellent_Components_Id+str(storageCount)).replace("$Storage_name",globalVars.CompellentComponent_Name+str(storageCount)).replace("$Storage_componentID",globalVars.Compellent_componentID+str(ComponentCount)).replace("$VolumeValue","$Volume_"+str(storageCount)).replace("$VolumeSize","$VolumeSize_"+str(storageCount)).replace("$OperatingSystemName",scaleupOperatingSystemName).replace("$PortTypeName",scaleupPortTypeCompellent)
                    self.finalScaleupStoragePayload = self.finalScaleupStoragePayload + storageComponentpayload
                    scaleupStorageComponentCount -= 1
                    
                elif scaleupStorageType == "NetApp":
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.NetApp_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.NetAppComponent_Name+str(storageCount))
                    storageRelatedComponentFinalPayload = storageRelatedComponentFinalPayload + relatedComponentpayload
                
                    associatedComponentpayload = ""
                    associatedComponentpayload = utility.readFile(globalVars.associatedCompPayload)
                    associatedComponentpayload = associatedComponentpayload.replace("$Components_Id",globalVars.NetApp_Components_Id+str(storageCount)).replace("$Resource_Name",globalVars.NetAppComponent_Name+str(storageCount))
                    storageAssociatedComponentFinalPayload = storageAssociatedComponentFinalPayload + associatedComponentpayload
            
                
                
                    storageComponentpayload = ""
                    storageComponentpayload = utility.readFile(globalVars.netAPPComponentPayload)
                    storageComponentpayload = storageComponentpayload.replace("$Storage_Components_Id",globalVars.NetApp_Components_Id+str(storageCount)).replace("$Storage_name",globalVars.NetAppComponent_Name+str(storageCount)).replace("$Storage_componentID",globalVars.NetApp_componentID+str(ComponentCount)).replace("$VolumeValue","$Volume_"+str(storageCount)).replace("$VolumeSize","$VolumeSize_"+str(storageCount)).replace("$Space_Mode",scaleupStorageSpaceReservationMode).replace("$NFS_IP",scaleupStorageNFSTargetIP).replace("$Aggregate_Name",scaleupStorageAggregateName)
                    self.finalScaleupStoragePayload = self.finalScaleupStoragePayload + storageComponentpayload
                    scaleupStorageComponentCount -= 1
                
                
                
                storageCount +=1
                ComponentCount +=1
        
            
            while scaleupServerComponentCount:
            
                relatedComponentpayload = ""
                relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.Server_Components_Id+str(serverCount)).replace("$Resource_Name",globalVars.ServerComponent_Name+str(serverCount))
                serverRelatedComponentFinalPayload = serverRelatedComponentFinalPayload + relatedComponentpayload
            
                serverComponentpayload = ""
                
                if scaleupNetworkType == "ESXI_EQL_CONVERGED_2_REDUNDANCY_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCON2REDPART)
                elif scaleupNetworkType == "ESXI_EQL_CONVERGED_2_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCONV2REDU)
                elif scaleupNetworkType == "ESXI_EQL_CONVERGED_2_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCON2PART)
                elif scaleupNetworkType == "ESXI_EQL_CONVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServESXIEQLCOD2)
                elif scaleupNetworkType == "ESXI_FCOE_CONVERGED_2_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIFCOECONVE2PART)
                elif scaleupNetworkType == "ESXI_FCOE_CONVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIFCOECOND2)
                elif scaleupNetworkType == "ESXI_CPL_ISCSI_CONVERGED_2_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSICONVD2PART)
                elif scaleupNetworkType == "ESXI_CPL_ISCSI_CONVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSICONV2)
                elif scaleupNetworkType == "ESXI_EQL_CONVERGED_4_REDUNDANCY_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCON4REDPART)
                elif scaleupNetworkType == "ESXI_EQL_CONVERGED_4_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCONV4REDU)
                elif scaleupNetworkType == "ESXI_EQL_CONVERGED_4_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLCON4PART)
                elif scaleupNetworkType == "ESXI_EQL_CONVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServESXIEQLCOD4)
                elif scaleupNetworkType == "ESXI_EQL_DIVERGED_4_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER4REDU)
                elif scaleupNetworkType == "ESXI_EQL_DIVERGED_4_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER4PART)
                elif scaleupNetworkType == "ESXI_EQL_DIVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServESXIEQLDIVER4)
                elif scaleupNetworkType == "ESXI_EQL_DIVERGED_4_REDUNDANCY_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER4REDPAR)
                elif scaleupNetworkType == "ESXI_EQL_DIVERGED_2_REDUNDANCY_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER2REDPAR)
                elif scaleupNetworkType == "ESXI_EQL_DIVERGED_2_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER2REDU)
                elif scaleupNetworkType == "ESXI_EQL_DIVERGED_2_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIEQLDIVER2PART)
                elif scaleupNetworkType == "ESXI_EQL_DIVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServESXIEQLDIVER2)
                elif scaleupNetworkType == "ESXI_CPL_FC_DIVERGED_2_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER2PART)
                elif scaleupNetworkType == "ESXI_CPL_FC_DIVERGED_2_REDUNDANCY_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER2REDPAR)
                elif scaleupNetworkType == "ESXI_CPL_FC_DIVERGED_2_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER2REDU)
                elif scaleupNetworkType == "ESXI_CPL_FC_DIVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServESXICPLFCDIVER2)
                elif scaleupNetworkType == "ESXI_CPL_FC_DIVERGED_4_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER4PART)
                elif scaleupNetworkType == "ESXI_CPL_FC_DIVERGED_4_REDUNDANCY_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER4REDPAR)
                elif scaleupNetworkType == "ESXI_CPL_FC_DIVERGED_4_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLFCDIVER4REDU)
                elif scaleupNetworkType == "ESXI_CPL_FC_DIVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServESXICPLFCDIVER4)
                elif scaleupNetworkType == "ESXI_NETAPP_CONVERGED_2_REDUNDANCY_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCON2REDPART)
                elif scaleupNetworkType == "ESXI_NETAPP_CONVERGED_2_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCONV2REDU)
                elif scaleupNetworkType == "ESXI_NETAPP_CONVERGED_2_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCON2PART)
                elif scaleupNetworkType == "ESXI_NETAPP_CONVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServESXINETAPPCOD2)
                elif scaleupNetworkType == "ESXI_NETAPP_CONVERGED_4_REDUNDANCY_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCON4REDPART)
                elif scaleupNetworkType == "ESXI_NETAPP_CONVERGED_4_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCONV4REDU)
                elif scaleupNetworkType == "ESXI_NETAPP_CONVERGED_4_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXINETAPPCON4PART)
                elif scaleupNetworkType == "ESXI_NETAPP_CONVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServESXINETAPPCOD4)
                elif scaleupNetworkType == "HyperV_EQL_CONVERGED_2_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLCONV2REDU)
                elif scaleupNetworkType == "HyperV_EQL_CONVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLCONV2)
                elif scaleupNetworkType == "HyperV_EQL_CONVERGED_4_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLCONV4REDU)
                elif scaleupNetworkType == "HyperV_EQL_CONVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLCONV4)
                elif scaleupNetworkType == "HyperV_EQL_DIVERGED_2_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLDIVER2REDU)
                elif scaleupNetworkType == "HyperV_EQL_DIVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLDIVER2)
                elif scaleupNetworkType == "HyperV_EQL_DIVERGED_4_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLDIVER4REDU)
                elif scaleupNetworkType == "HyperV_EQL_DIVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVEQLDIVER4)
                elif scaleupNetworkType == "HyperV_CPL_ISCSI_DIVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLISCSIDIVER2)
                elif scaleupNetworkType == "HyperV_CPL_ISCSI_DIVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLISCSIDIVER4)
                elif scaleupNetworkType == "HyperV_CPL_ISCSI_CONVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLISCSICONV2)
                elif scaleupNetworkType == "HyperV_CPL_ISCSI_CONVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLISCSICONV4)
                elif scaleupNetworkType == "HyperV_CPL_FC_DIVERGED_2_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLFCDIVER2REDU)
                elif scaleupNetworkType == "HyperV_CPL_FC_DIVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLFCDIVER2)
                elif scaleupNetworkType == "HyperV_CPL_FC_DIVERGED_4_REDUNDANCY":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLFCDIVER4REDU)
                elif scaleupNetworkType == "HyperV_CPL_FC_DIVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServerHyperVCPLFCDIVER4)
                elif scaleupNetworkType == "ESXI_FCOE_CONVERGED_4_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIFCOECONVE4PART)
                elif scaleupNetworkType == "ESXI_FCOE_CONVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXIFCOECOND4)
                elif scaleupNetworkType == "ESXI_CPL_ISCSI_CONVERGED_4_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSICONVD4PART)
                elif scaleupNetworkType == "ESXI_CPL_ISCSI_CONVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSICONV4)
                elif scaleupNetworkType == "ESXI_CPL_ISCSI_DIVERGED_2_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSIDIVER2PART)
                elif scaleupNetworkType == "ESXI_CPL_ISCSI_DIVERGED_2":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSIDIVER2)
                elif scaleupNetworkType == "ESXI_CPL_ISCSI_DIVERGED_4_PARTITION":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSIDIVER4PART)
                elif scaleupNetworkType == "ESXI_CPL_ISCSI_DIVERGED_4":
                    serverComponentpayload = utility.readFile(globalVars.ServerESXICPLISCSIDIVER4)
                else:
                    self.failure("Network Type Defined in JSON file is not correct %s"%str(scaleupNetworkType))
            
            
                serverComponentpayload = serverComponentpayload.replace("$Server_Components_Id",globalVars.Server_Components_Id+str(serverCount)).replace("$ServerComponent_Name",globalVars.ServerComponent_Name+str(serverCount)).replace("$Server_componentID",globalVars.Server_componentID+str(ComponentCount)).replace("$target_boot_device_value",scaleupServerTrgetBootDeviceValue).replace("$OSImageValue","$OSImage_"+str(serverCount)).replace("$iscsiinitiator",scaleupISCSIInitiator).replace("$HostNameTemplate",scaleupHostNameTemplate).replace("$os_image_version",scaleupServerOsImageVersion).replace("$product_key",scaleupServerProductKey).replace("$domain_name",scaleupDomain_name).replace("$fqdn",scaleupFQ_Domain_Name).replace("$domainadminuser",scaleupDomainAdminUsername).replace("$DomainAdminPsswd",scaleupDomainAdminPassword)
                self.finalScaleupServerPayload = self.finalScaleupServerPayload + serverComponentpayload
                scaleupServerComponentCount -= 1
                serverCount +=1
                ComponentCount +=1
            
        
            while scaleupClusterComponentCount:
            
                if scaleupClusterType == "VMWareCluster":
                
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.VMWareCluster_Components_Id+str(clusterCount)).replace("$Resource_Name",globalVars.VMWareClusterComponent_Name+str(clusterCount))
                    clusterRelatedComponentFinalPayload = clusterRelatedComponentFinalPayload + relatedComponentpayload
            
                    clusterComponentpayload = ""
                    
                    if len(scaleupExistingClusterName) >1:
                        clusterComponentpayload = utility.readFile(globalVars.VMWareExistingClusterComponentPayload)
                    else:
                        clusterComponentpayload = utility.readFile(globalVars.VMWareClusterComponentPayload)
                        
                    clusterComponentpayload = clusterComponentpayload.replace("$Cluster_Components_Id",globalVars.VMWareCluster_Components_Id+str(clusterCount)).replace("$Cluster_name",globalVars.VMWareClusterComponent_Name+str(clusterCount)).replace("$Cluster_componentID",globalVars.VMWareCluster_componentID+str(ComponentCount)).replace("$HA_Config",scaleupClusterHA).replace("$DRS_Config",scaleupClusterDRS).replace("$ExistingCluster",scaleupExistingClusterName).replace("$vSAN_Enabled",scaleupClusterVSANEnabled).replace("$SwitchType",scaleupClusterSwitchType)
                    
                    self.finalScaleupClusterPayload = self.finalScaleupClusterPayload + clusterComponentpayload
                    scaleupClusterComponentCount -= 1
                
                elif scaleupClusterType == "HyperVCluster":
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.HyperVCluster_Components_Id+str(clusterCount)).replace("$Resource_Name",globalVars.HyperVClusterComponent_Name+str(clusterCount))
                    clusterRelatedComponentFinalPayload = clusterRelatedComponentFinalPayload + relatedComponentpayload
            
                    clusterComponentpayload = ""
                    clusterComponentpayload = utility.readFile(globalVars.HyperVClusterComponentPayload)
                    clusterComponentpayload = clusterComponentpayload.replace("$Cluster_Components_Id",globalVars.HyperVCluster_Components_Id+str(clusterCount)).replace("$Cluster_name",globalVars.HyperVClusterComponent_Name+str(clusterCount)).replace("$Cluster_componentID",globalVars.HyperVCluster_componentID+str(ComponentCount)).replace("$ClusterIPHyperV",scaleupClusterIPHyperV)
                    self.finalScaleupClusterPayload = self.finalScaleupClusterPayload + clusterComponentpayload
                    scaleupClusterComponentCount -= 1
                
                
                clusterCount +=1
                ComponentCount +=1
        
            
        
            while scaleupVmComponentCount:
            
                if scaleupVmType == "VMWareVM":
                
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.VMWareVM_Components_Id+str(vmCount)).replace("$Resource_Name",globalVars.VMWareVMComponent_Name+str(vmCount))
                    vmRelatedComponentFinalPayload = vmRelatedComponentFinalPayload + relatedComponentpayload
            
                    vmComponentpayload = ""
                    vmComponentpayload = utility.readFile(globalVars.VMWareVMComponentPayload)
                    vmComponentpayload = vmComponentpayload.replace("$VM_Components_Id",globalVars.VMWareVM_Components_Id+str(vmCount)).replace("$VM_name",globalVars.VMWareVMComponent_Name+str(vmCount)).replace("$VM_componentID",globalVars.VMWareVM_componentID+str(ComponentCount)).replace("$VMHostName","$VMHost_"+str(vmCount)).replace("$AutogenerateHostName",scaleupVmAutogenerateHostName).replace("$OS_Image",scaleupVmOS_Image).replace("$os_image_version",scaleupVmOs_Image_Version).replace("$OS_Image_Type",scaleupVmOS_Image_Type).replace("$product_key",scaleupVmProduct_Key).replace("$memoryvalue",scaleupMemoryInMB).replace("$disksizevalue",scaleupVmVirtual_Disk_Size).replace("$cpucountvalue",scaleupCpuCount)
                    self.finalScaleupVMPayload = self.finalScaleupVMPayload + vmComponentpayload
                    scaleupVmComponentCount -= 1
                
                elif scaleupVmType == "HyperVCloneVM":
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.CloneVMhyperV_Components_Id+str(vmCount)).replace("$Resource_Name",globalVars.CloneVMhyperVComponent_Name+str(vmCount))
                    vmRelatedComponentFinalPayload = vmRelatedComponentFinalPayload + relatedComponentpayload
            
                    vmComponentpayload = ""
                    vmComponentpayload = utility.readFile(globalVars.CloneVMhyperVComponentPayload)
                    vmComponentpayload = vmComponentpayload.replace("$VM_Components_Id",globalVars.CloneVMhyperV_Components_Id+str(vmCount)).replace("$VM_name",globalVars.CloneVMhyperVComponent_Name+str(vmCount)).replace("$VM_componentID",globalVars.CloneVMhyperV_componentID+str(ComponentCount)).replace("$hostname",scaleupVmHostName).replace("$description",scaleupVmdescription).replace("$centosvmtemplatename",scaleupVmTemplateName).replace("$path",scaleupVmPath).replace("$blockdynamicoptimization",scaleupBlockDynamicOptimization).replace("$highlyavailable",scaleupHighlyAvailable).replace("$cpucount",scaleupCpuCount).replace("$memorymb",scaleupMemoryInMB).replace("$startaction",scaleupStartAction).replace("$stopaction",scaleupStopAction)
                    self.finalScaleupVMPayload = self.finalScaleupVMPayload + vmComponentpayload
                    scaleupVmComponentCount -= 1
                
                
                vmCount +=1
                ComponentCount +=1
            
            
            
        
            while scaleupApplicationComponentCount:
            
                if scaleupApplicationType == "LinuxPostInstall":
                
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.LinuxPostInstall_Components_Id+str(applicationCount)).replace("$Resource_Name",globalVars.LinuxPostInstall_Name+str(applicationCount))
                    ApplicationRelatedComponentFinalPayload = ApplicationRelatedComponentFinalPayload + relatedComponentpayload
            
                    applicationComponentpayload = ""
                    applicationComponentpayload = utility.readFile(globalVars.LinuxPostInstallComponentPayload)
                    applicationComponentpayload = applicationComponentpayload.replace("$Application_Components_Id",globalVars.LinuxPostInstall_Components_Id+str(applicationCount)).replace("$Application_name",globalVars.LinuxPostInstall_Name+str(applicationCount)).replace("$Application_componentID",globalVars.LinuxPostInstall_componentID+str(ComponentCount)).replace("$installPackages",scaleupInstallPackages).replace("$uploadShare",scaleupUploadShare).replace("$uploadFile",scaleupUploadFile).replace("$executeFileCommand",scaleupExecuteFileCommand).replace("$yumProxy",scaleupYumProxy)
                    self.finalScaleupApplicationPayload = self.finalScaleupApplicationPayload + applicationComponentpayload
                    scaleupApplicationComponentCount -= 1
                
                elif scaleupApplicationType == "WindowPostInstall":
                    relatedComponentpayload = ""
                    relatedComponentpayload = utility.readFile(globalVars.RelatedComponentPayload)
                    relatedComponentpayload = relatedComponentpayload.replace("$Components_Id",globalVars.CloneVMhyperV_Components_Id+str(applicationCount)).replace("$Resource_Name",globalVars.CloneVMhyperVComponent_Name+str(applicationCount))
                    ApplicationRelatedComponentFinalPayload = ApplicationRelatedComponentFinalPayload + relatedComponentpayload
            
                    applicationComponentpayload = ""
                    applicationComponentpayload = utility.readFile(globalVars.WindowsPostInstallComponentPayload)
                    applicationComponentpayload = applicationComponentpayload.replace("$VM_Components_Id",globalVars.CloneVMhyperV_Components_Id+str(applicationCount)).replace("$VM_name",globalVars.CloneVMhyperVComponent_Name+str(applicationCount)).replace("$VM_componentID",globalVars.CloneVMhyperV_componentID+str(ComponentCount)).replace("$windowsPostinstallShare",scaleupWindowsShare).replace("$installCommand",scaleupWindowsInstallCommand).replace("$uploadFile",scaleupUploadFile).replace("$executeFileCommand",scaleupExecuteFileCommand)
                    self.finalScaleupApplicationPayload = self.finalScaleupApplicationPayload + applicationComponentpayload
                    scaleupApplicationComponentCount -= 1
                
                
                applicationCount +=1
                ComponentCount +=1
        
        
            
            for osImage in scaleupServerOSImageValue:
                self.finalScaleupServerPayload = self.finalScaleupServerPayload.replace("$OSImage_"+str(i),osImage)
                i +=1
       
        
            for strsize in scaleupStorageSize:
                self.finalScaleupStoragePayload = self.finalScaleupStoragePayload.replace("$VolumeSize_"+str(j),strsize)
                j +=1    
        
            if scaleupStorageType == "Equallogic":
                equalLogicList = globalVars.resourceInfo['STORAGE']
                print " Equallogic List : %s"%str(equalLogicList)
                for device in equalLogicList:
                    if device['ip']==scaleupStorageIPAdress:
                        storageId = device['refid']
            
                if len(equalLogicList) == 0:
                    utility.log_data("Required no. of Storage(Equallagic) not available")
                
            
        
            elif scaleupStorageType == "Compellent":
            
                compellentList = globalVars.resourceInfo['COMPELLENT']
                print " COMPELLENT LIST : %s"%str(compellentList)
                for device in compellentList:
                    if device['ip']==scaleupStorageIPAdress:
                        storageId = device['refid']
            
                if len(compellentList) == 0:
                    utility.log_data("Required no. of COMPELLENT not available")
                
            elif scaleupStorageType == "NetApp":
            
                netAPPList = globalVars.resourceInfo['NETAPP']
                print " NETAPP LIST : %s"%str(netAPPList)
                for device in netAPPList:
                    if device['ip']==scaleupStorageIPAdress:
                        storageId = device['refid']
            
                if len(netAPPList) == 0:
                    utility.log_data("Required no. of NETAPP not available")
                
              
            
            if scaleupClusterType == "VMWareCluster":
                vCenterList = globalVars.resourceInfo['VCENTER']
                print " VCENTER List : %s"%str(vCenterList)
                for device in vCenterList:
                    if device['ip']==scaleupClusterIPAdress:
                        vcenterId = device['refid']
            
                if len(vCenterList) == 0:
                    utility.log_data("Required no. of vCENTER not available")
                
            
        
            elif scaleupClusterType == "HyperVCluster":
            
                scvmmList = globalVars.resourceInfo['SCVMM']
                print " SCVMM LIST : %s"%str(scvmmList)
                for device in scvmmList:
                    if device['ip']==scaleupClusterIPAdress:
                        scvmmId = device['refid']
            
                if len(scvmmList) == 0:
                    utility.log_data("Required no. of SCVMM not available")
                
                
      
            
            globalVars.refIdVCenter = vcenterId
            globalVars.refIdSCVMM = scvmmId
            globalVars.refIdEQLogic = storageId
        
            
            self.finalScaleupStoragePayload = self.finalScaleupStoragePayload.replace("$ServerComponentsEntry",serverRelatedComponentFinalPayload)
            self.finalScaleupServerPayload = self.finalScaleupServerPayload.replace("$StorageComponentsEntry",storageRelatedComponentFinalPayload).replace("$ClusterComponentsEntry",clusterRelatedComponentFinalPayload).replace("$networkConfiguration",self.networkConfValue)
            self.finalScaleupClusterPayload = self.finalScaleupClusterPayload.replace("$ServerComponentsEntry",serverRelatedComponentFinalPayload).replace("$VMComponentsEntry",vmRelatedComponentFinalPayload)
            self.finalScaleupVMPayload = self.finalScaleupVMPayload.replace("$ApplicationComponentsEntry",ApplicationRelatedComponentFinalPayload).replace("$ClusterComponentsEntry",clusterRelatedComponentFinalPayload)
            self.finalScaleupApplicationPayload = self.finalScaleupApplicationPayload.replace("$VMComponentsEntry",vmRelatedComponentFinalPayload)
        
            
        
        self.setEeachNetworkData()
       
        templateResponse= self.createTemplateEsxi(finalPayload,vcenterId,storageId, scvmmId) 
        
        if templateResponse.status_code in (200, 201, 202, 203, 204):
            result  = json.loads(templateResponse.content)
            templateIdValue = result['id']
            globalVars.publishedTemplateID= templateIdValue
                                    
            
            utility.log_data( "Successfully created  and published Template, Template Name: %s"%globalVars.template_name)
            self.succeed("Template created  and published Successfully, Template Name: %s"%globalVars.template_name)
            
            statausCreateTemplae = self.publishTemplate()
            
        else:
            
            utility.log_data( 'Failed to create/published template ')
            self.failure("Failed to create/published template, Template Name: %s"%globalVars.template_name)
            
        return statausCreateTemplae
            
    def publishTemplate(self):
        
        templateResult= self.getPublishedTemplateData(globalVars.publishedTemplateID)
        utility.writeFile(globalVars.publishedTemp_filename, templateResult)
        tree = ET.ElementTree(file =globalVars.publishedTemp_filename)
        root = tree.getroot()
        ET.ElementTree(root).write(globalVars.publishedTemp_filename, xml_declaration=False)
        statausDeploy = False
        DeplName=globalVars.template_name
        deplyDesc=globalVars.template_description
        utility.log_data(" Going to Deploy Template :: ")
        deployResponse = self.deployTemplate(DeplName,deplyDesc)
        if deployResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            deploymentRefId = self.getDeploymentId(DeplName)
            loop = 60
            deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
            deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            
            while loop:
                resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                utility.log_data("Deployment Status: %s"%resDS + " \t for the Deployment Name : %s"%DeplName)
                if resDS.lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    if resDS.lower() in ("complete"):
                        statausDeploy = True
                        utility.log_data("Deployment Status: %s"%resDS + " \t for the Deployment Name : %s"%DeplName)
                        utility.log_data( 'Successfully Deployed Service for the Deployment Name : %s'%DeplName)
                        self.succeed("Successfully Deployed Service for the Deployment Name : %s"%DeplName)
                        if not self.scaleUPEnable:
                            
                            utility.log_data( 'Going to do VCenter Validation before TearDown ')
                            self.doVCenterValidations(globalVars.refIdVCenter)
                            utility.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.teardownServices(deploymentRefId)
                            utility.log_data( 'Going to do VCenter Validation after TearDown')
                            self.doVCenterValidations(globalVars.refIdVCenter)
                        break
                    else:
                        utility.log_data("Deployment Status: %s"%resDS + " \t for the Deployment Name : %s"%DeplName)
                        utility.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
                        self.failure("Deployment Service Failed for the Deployment Name :  %s"%DeplName, contOnFail=True)
                        if not self.scaleUPEnable:
                            #self.runWebServiceAPI(str(self.tc_Id), "Fail", "Run in regression test")
                            if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                                utility.log_data( 'Now going to call the teardown of service ')
                                self.cleanDeployedService(deploymentRefId)
                                self.teardownServices(deploymentRefId)
                        
                        break
            loop -= 1
            if self.scaleUPEnable:
                if globalVars.scaleupStorageComponent >0:
                    utility.log_data( 'Going to call the ScaleUp of Storage  for the Service Name : %s'%str(DeplName))
                    self.scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc, self.finalScaleupStoragePayload)
                    time.sleep(30)
                if globalVars.scaleupServerComponent >0:
                    utility.log_data( 'Going to call the ScaleUp of Server  for the Service Name : %s'%str(DeplName))
                    self.scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc, self.finalScaleupServerPayload)
                    time.sleep(30)
                if globalVars.scaleupClusterComponent >0:
                    utility.log_data( 'Going to call the ScaleUp of Cluster for the Service Name : %s'%str(DeplName))
                    self.scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc, self.finalScaleupClusterPayload)
                    time.sleep(30)
                if globalVars.scaleupVmComponent >0:
                    utility.log_data( 'Going to call the ScaleUp of VM  for the Service Name : %s'%str(DeplName))
                    self.scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc, self.finalScaleupVMPayload)
                    time.sleep(30)
                if globalVars.scaleupApplicationComponent >0:
                    utility.log_data( 'Going to call the ScaleUp of Application  for the Service Name : %s'%str(DeplName))
                    self.scaleUpTemplate(globalVars.publishedTemplateID, deploymentRefId, DeplName, deplyDesc, self.finalScaleupApplicationPayload)
                    time.sleep(30)
                    
        
        else:
            self.failure("Deployment Service Failed for the Deployment Name :  %s"%DeplName)
            utility.log_data('Deployment Service Failed for the Deployment Name : %s'%DeplName)
            
        return statausDeploy
    
    
    
    
    def scaleUpTemplate(self, templateID, deploymentRefId, DeplName, deplyDesc, scaleupCompPaylpad):
        
        
        scaleUpResponse = self.scaleUpAndDeployService(templateID, deploymentRefId, DeplName, deplyDesc, scaleupCompPaylpad)
        utility.log_data("ScaleUp Deployment Status Code: %s"%str(scaleUpResponse.status_code))
        
        if scaleUpResponse.status_code in (200, 201, 202, 203, 204):            
            
            #Get Deployment Id
            #deploymentRefId = self.getDeploymentId(DeplName)
            loop = 60
            deploymentLogSubPath = '/opt/Dell/ASM/deployments/'
            deploymentLogPath= deploymentLogSubPath + str(deploymentRefId)
            
            while loop:
                resDS, statDS = self.getDeploymentStatus(deploymentRefId)
                utility.log_data(" ScaleUp Deployment Status: %s"%resDS + " \t for the Deployment Name : %s"%DeplName)
                if resDS.lower() in ("in_progress"):
                    time.sleep(120)
                else:
                    if resDS.lower() in ("complete"):
                        
                        utility.log_data( 'Successfully ScaleUped Service for the Deployment Name : %s'%DeplName)
                        self.succeed("Successfully ScaleUped Service for the Deployment Name: %s"%DeplName)
#                         utility.log_data( 'Going to do VCenter Validation before TearDown after ScaleUp')
#                         self.doVCenterValidations(globalVars.refIdVCenter)
#                         utility.log_data( 'Now going to call the teardown of service ')
#                         self.cleanDeployedService(deploymentRefId)
#                         self.teardownServices(deploymentRefId)
#                         utility.log_data( 'Going to do VCenter Validation after TearDown')
#                         self.doVCenterValidations(globalVars.refIdVCenter)
                        break
                    else:
                        
                        utility.log_data('Deployment ScaleUped Service Failed for the Deployment Name : %s'%DeplName)
                        self.failure("Deployment ScaleUped Service Failed for the Deployment Name  :  %s"%DeplName, contOnFail=True)
                        if resDS.lower() == globalVars.deploymentStatus and globalVars.enableTearDownService:
                            utility.log_data( 'Now going to call the teardown of service ')
                            self.cleanDeployedService(deploymentRefId)
                            self.teardownServices(deploymentRefId)
                        
                        break
            loop -= 1
        
        else:
            
            utility.log_data('Deployment scaleUp Service Failed for the Deployment Name : %s'%DeplName)
            self.failure("Deployment ScaleUped Service Failed for the Deployment Name  :  %s"%DeplName)
            if globalVars.enableTearDownService:
                utility.log_data( 'Now going to call the teardown of service ')
                self.cleanDeployedService(deploymentRefId)
                self.teardownServices(deploymentRefId)
    
    
    def setNetworkList(self):
        
        response =self.getNetworkList()
        discoveredNetworkValue=response[0]
        
        for network in discoveredNetworkValue:
            networkType=network['type']
            if networkType == "PUBLIC_LAN":
                globalVars.workloadID=network['id']
                networkName=network['name']
                globalVars.workloadName=networkName
            elif networkType == "PXE":
                globalVars.pXEID=network['id']
                networkName=network['name']
                globalVars.pXEName=networkName
            elif networkType == "HYPERVISOR_MANAGEMENT":
                globalVars.hypervisorManagementID=network['id']
                networkName=network['name']
                globalVars.hypervisorManagementName=networkName
            elif networkType == "HYPERVISOR_MIGRATION":
                globalVars.vMotionID=network['id']
                networkName=network['name']
                globalVars.vMotionName=networkName
            elif networkType == "HYPERVISOR_CLUSTER_PRIVATE":
                globalVars.clusterPrivateID=network['id']
                networkName=network['name']
                globalVars.clusterPrivateName=networkName
            elif networkType == "STORAGE_ISCSI_SAN":
                if globalVars.iSCSIID == "":
                    globalVars.iSCSIID=network['id']
                    networkName=network['name']
                    globalVars.iSCSIName=networkName
                elif globalVars.ISC_1_ID =="":
                    globalVars.ISC_1_ID = network['id']
                    networkName=network['name']
                    globalVars.ISC_1_Name=networkName
                    
            elif networkType == "STORAGE_FCOE_SAN":
                if globalVars.FCoE1ID == "":
                    globalVars.FCoE1ID = network['id']
                    networkName=network['name']
                    globalVars.FCoE1Name=networkName
                elif globalVars.FC_oE2_ID =="":
                    globalVars.FC_oE2_ID = network['id']
                    networkName=network['name']
                    globalVars.FC_oE2_Name=networkName
                    
            elif networkType == "FIP_SNOOPING":
                globalVars.FIPID = network['id']
                networkName=network['name']
                globalVars.FIPName=networkName
                
            elif networkType == "FILESHARE":
                globalVars.FileshareID = network['id']
                networkName=network['name'] 
                globalVars.FileshareName=networkName           
            else:
                utility.log_data("no  match found while getting the network list  for Network Type:%s"%str(networkType))
                
                
                
                
    def setEeachNetworkData(self):
        
        
        if globalVars.workloadID  != "":
            resultWorkLoad, statWorkLoad = self.getNetworkByRefID(globalVars.workloadID)
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
                
        if globalVars.FC_oE2_ID  != "":
            resultFCoE2, statFCoE2 = self.getNetworkByRefID(globalVars.FC_oE2_ID)
            if statFCoE2:
                globalVars.FC_oE2_IDNetwork=resultFCoE2
                
        if globalVars.FIPID  != "":
            resultFIP, statFIP = self.getNetworkByRefID(globalVars.FIPID)
            if statFIP:
                globalVars.FIPIDNetwork=resultFIP
                
        if globalVars.FileshareID  != "":
            resultFileShare, statFileShare = self.getNetworkByRefID(globalVars.FileshareID)
            if statFileShare:
                globalVars.FileshareIDNetwork=resultFileShare
                
         
                
                            
    def getNetworkByRefID(self, refId):
        """
        Gets the Networks by refid
        """
        
        url = self.buildUrl("Network", refId)
        
        uri = globalVars.serviceUriInfo["Network"]+ "/" + refId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        
        self.statDS =False
        self.resultDS=''
        #response = requests.get(url, headers=headers)
        startTime = datetime.datetime.now()
        networkResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), networkResponse.status_code, networkResponse.text, startTime, endTime, elapsedTime)
        
        if networkResponse.status_code in (200, 201, 202, 203, 204):
            
            self.resultDS = networkResponse.content
            self.resultDS = self.resultDS.replace("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""",'').replace("""<Network xmlns="http://pg.dell.com/asm/virtualservices/network">""",'').replace("</Network>", '')
            self.statDS =True
            
        else:
            self.statDS =False
            self.resultDS = 'Not Found'
            
       
        return self.resultDS, self.statDS 
                
                
    def  getNetworkList(self):
        """
        Get the list of Networks
        """
        return self.getResponse("GET", "Network")
    
    def generateSecurityHeader(self, uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        uri1 = uri.partition('?')
        uriFinal = uri1[0]
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uriFinal + ":" + userAgent + ":" + timestamp
        signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/json","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers
    
    
    
    def generateHeaderGetDeploy(self, uri, httpMethod, apiKey, apiSecret, userAgent):
        """
        Generates a Security Header
        """
        uri1 = uri.partition('?')
        uriFinal = uri1[0]
        timestamp = str(long(time.time()))
        requestString = apiKey + ":" + httpMethod + ":"+ uriFinal + ":" + userAgent + ":" + timestamp
        signature =  base64.b64encode(hmac.new(str(apiSecret), msg=requestString, digestmod=hashlib.sha256).digest())
        globalVars.headers = {"Accept":"application/json","Content-Type":"application/json", "Accept-Encoding":"gzip, deflate","x-dell-auth-key":apiKey,
                    "x-dell-auth-signature":signature,"x-dell-auth-timestamp":timestamp, 'User-Agent': userAgent}
        return globalVars.headers

    
    
    def createTemplateEsxi(self, payload,refIdVC = None,refIDEQ = None, refIDSCVMM = None):
        """
        Creates ServiceTemplate
        """
        
        url = self.buildUrl("Template")
        postData = self.getTemplatePayloadEsxi(payload,refIdVC,refIDEQ, refIDSCVMM)        
        uri = globalVars.serviceUriInfo["Template"]
        headers=self.generateSecurityHeader(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        resResult=""
        if response.status_code  not in (200, 201, 202, 203, 204):
            resResult=response.text
            
            
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, resResult, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response
    
    def getTemplatePayloadEsxi(self, payload,refIdVC = None,refIDEQ = None, refIDSCVMM = None):
        
        
        poolId, foundGroup = self.getServerPoolId(globalVars.serverPoolName)
        
        if not foundGroup:
            poolId = globalVars.GlobalPool    
            

                        
        utility.log_data( "  pool ID value : ")
        utility.log_data( poolId)
        utility.log_data( " RefId of VCenter : %s"%str(refIdVC))
        utility.log_data( " RefId of Storage : %s"%str(refIDEQ))
        utility.log_data( " RefId of SCVMM : %s"%str(refIDSCVMM))
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
        
        payload = payload.replace("$AdminConfirmPassword", globalVars.AdminConfirmPassword).replace("$Adminpassword", globalVars.Adminpassword)
        payload = payload.replace("$migration",globalVars.migration)
        payload = payload.replace("$Workload", globalVars.workloadID).replace("$PXE", globalVars.pXEID).replace("$HypervisorManagement", globalVars.hypervisorManagementID).replace("$VMotion", globalVars.vMotionID).replace("$ClusterPrivate", globalVars.clusterPrivateID).replace("$ISCSI", globalVars.iSCSIID).replace("$FCoE1",\
                    globalVars.FCoE1ID).replace("$FC_oE_2",globalVars.FC_oE2_ID).replace("$FIP",globalVars.FIPID).replace("$ISC_1",globalVars.ISC_1_ID).replace("$FileShare",globalVars.FileshareID)
        payload = payload.replace("$NetworkHypervisorManagement", globalVars.hypervisorManagementIDNetwork).replace("$NetworkPXE", globalVars.pXEIDNetwork).replace("$NetworkWorkload", globalVars.workloadIDNetwork).replace("$NetworkISCSI", globalVars.iSCSIIDNetwork).replace("$NetworkVMotion", globalVars.vMotionIDNetwork).replace("$NetworkFCoE1",\
                    globalVars.FCoE1IDNetwork).replace("$NetworkFC_oE_2",globalVars.FC_oE2_IDNetwork).replace("$NetworkFIP",globalVars.FIPIDNetwork).replace("$NetworkISC_1",globalVars.ISC_1_IDNetwork).replace("$NameWorkload",globalVars.workloadName).replace("$NetworkFileShare",globalVars.FileshareIDNetwork).replace("$NetworkClusterPrivate",globalVars.clusterPrivateIDNetwork)            
        payload = payload.replace("$NameWorkload", globalVars.workloadName).replace("$NamePXE", globalVars.pXEName).replace("$NameHypervisorManagement", globalVars.hypervisorManagementName).replace("$NameVMotion", globalVars.vMotionName).replace("$NameClusterPrivate", globalVars.clusterPrivateName).replace("$NameISCSI", globalVars.iSCSIName).replace("$NameFCoE1",\
                    globalVars.FCoE1Name).replace("$NameFC_oE_2",globalVars.FC_oE2_Name).replace("$NameFIP",globalVars.FIPName).replace("$NameISC_1",globalVars.ISC_1_Name).replace("$NameFileShare",globalVars.FileshareName)
        
        payload = payload.replace("$Volume_1", Volume_1).replace("$Volume_2", Volume_2).replace("$Volume_3", Volume_3).replace("$NewClusterName", NewClusterName).replace("$Volume_4", Volume_4)
        payload = payload.replace("$GlobalPool", poolId)
        
        payload = payload.replace("$newdatacenter",newdatacenter).replace("$cpucountvalue",globalVars.cpucountvalue).replace("$disksizevalue",globalVars.disksizevalue).replace("$memoryvalue",globalVars.memoryvalue)
        payload = payload.replace("$Host1",Host1).replace("$Host2",Host2).replace("$Host3",Host3).replace("$VMHost_1",VM1_Host).replace("$VMHost_2",VM2_Host).replace("$VMHost_3",VM3_Host).replace("$VMHost_4",VM4_Host).replace("$VMHost_5",VM5_Host)
        payload = payload.replace("$cpu_count",globalVars.cpu_count).replace("$disk_size",globalVars.disk_size).replace("$mem_val",globalVars.mem_val)
        
        
        payload = payload.replace("$managementIp",globalVars.managementIp).replace("$ntpserver",globalVars.ntpserver)
        
        
        if refIdVC:
            payload = payload.replace("$VCenterRefId",refIdVC)
        if refIDEQ:
            payload = payload.replace("$StorageRefId",str(refIDEQ))
        if refIDSCVMM:
            payload = payload.replace("$HyperMgmtClusterValue", refIDSCVMM)
        
        return payload
    
    def getServerPoolId(self,poolName):
        response = self.getResponse("GET", "ServerPool")
        poolList = response[0]
        utility.log_data( " poolName : ")
        utility.log_data(poolName)
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
    
    
    def getPublishedTemplateData(self, idvalue):
        """
        Return a whole template based on the template ID
        """
        
        url = self.buildUrl("Template", idvalue)
        uri = globalVars.serviceUriInfo["Template"]+ "/" + idvalue
        
        headers=self.generateHeaderDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        
        
        
        startTime = datetime.datetime.now()
        getrespone = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers,""), getrespone.status_code, "", startTime, endTime, elapsedTime)
        
        return getrespone.content
        
        
    def deployTemplate(self, DeplName, deplyDesc):
        """
        Deploy ServiceTemplate
        """
        
        url = self.buildUrl("Deploy")
        deployName=DeplName
        deployDesc=deplyDesc
        postData = self.getDeployPayload(deployName, deployDesc)
        uri = globalVars.serviceUriInfo["Deploy"]
        utility.log_data(uri)
        headers=self.generateHeaderDeploy(uri, "POST", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        utility.log_data( "Printing headers information")
        utility.log_data(headers)
        startTime = datetime.datetime.now()
        response = requests.post(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        resResult=""
        if response.status_code  not in (200, 201, 202, 203, 204):
            resResult=response.content
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'POST',headers,postData), response.status_code, resResult, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response
    
    def getDeployPayload(self, DeplName, deplyDesc):
        
        payload = utility.readFile(globalVars.deploy_filename)
        subPayload=utility.readFile(globalVars.publishedTemp_filename)
        payload = payload.replace("$dynamic_ServiceTemplate", subPayload)
        payload =payload.replace("ServiceTemplate", "serviceTemplate")
        payload =payload.replace("$userName", globalVars.userName).replace("$DeplName", DeplName).replace("$deplyDesc", deplyDesc).replace("$noOfDeploy", globalVars.numberOfDeployments)
        if globalVars.scheduleddeployment == 'Y':
            payload =payload.replace("$scheduledTimestamp", globalVars.scheduledTimestamp)
        else:
            payload =payload.replace("$scheduledTimestamp", '')
            
        return payload
    
    def getDeploymentId(self, deploymentName):
        """
        Gets the Deployment Reference Id  
        """
        
        url = self.buildUrl("Deploy")
        
        uri = globalVars.serviceUriInfo["Deploy"]
        
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        response = requests.get(url, headers=headers, verify=False)
        
        deployID=''
        root = ET.fromstring(response.content)
        for cmpnt in root.findall('Deployment'):
                indTD = cmpnt.find('deploymentName')
                resultDS = indTD.text
                if resultDS == deploymentName:
                    indID = cmpnt.find('id')
                    deployID = indID.text
          
        return deployID
    
    def getDeploymentStatus(self, refId):
        """
        Gets the Deployment Status
        """
        
        url = self.buildUrl("Deploy", refId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + refId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        utility.log_data("Printing headers information")
        utility.log_data(headers)
        statDS =False
        resultDS=''
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, "", startTime, endTime, elapsedTime)
        
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
        utility.log_data("Deployment Job Status:%s"%str(resultDS))
        
        return resultDS, statDS 
    
    def createPoolServer(self, serverIPList, serverList, serverPoolName):
        
        #respCSP = self.cleanServerPool()
        
        
        servers = []
        
        for device in serverList:
            for ips in serverIPList:
                if device['ip'] == ips:
                    servers.append(device)
        
        print "server length"
        print len(servers)
        if(len(servers) > 0):
            self.createServerPool(servers,serverPoolName)
            
            
            
    def createServerPool(self, deviceList,poolName):
        """
        Creates Server Pool with the provided Servers
        """
        utility.log_data("Creating Server Pool with number of servers :%s"%str(len(deviceList)))
        payload = self.generateMDGroupPayload(globalVars.serverPoolPayload, ["device_refid"], len(deviceList))
        loop = 1
        for device in deviceList:
            payload = payload.replace("device_refid" + str(loop), device['refId'])
            loop += 1
        payload = payload.replace("login_user",globalVars.userName)
        payload = payload.replace("$pool_name",poolName)
        payload = payload.replace('\n', '').replace('\t', '')
        return self.getResponse("POST", "ServerPool", payload)
    
    def generateMDGroupPayload(self, payload, mdParameters, mdCount=1):
        """
        Generates Managed Device Group payload
        """
        payload = utility.readFile(payload)
        deviceGroup = ET.fromstring(payload)        
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
        return ET.tostring(deviceGroup, encoding='utf8')
        
    
    def doVCenterValidations(self,refIdVC):
        
        utility.log_data( "in doVCenterValidatons ")
        count = 0
        
        url = self.buildUrl("VCenter", refIdVC)
        
        uri = globalVars.serviceUriInfo["User"]+ "/" + refIdVC
        headers=self.generateHeaderDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        
        startTime = datetime.datetime.now()
        response = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \n"%(url,'GET',headers), response.status_code, response.text, startTime, endTime, elapsedTime)
        result = response.text
        #xmlString = ET.fromstring(result)
        datacenter = globalVars.datacenterName
        
        utility.log_data( 'datacenter name in Template : %s'%str(datacenter))
        cluster = globalVars.clusterName
        
        utility.log_data( 'cluster name in Template : %s'%str(cluster))
        if result.find(datacenter):
            utility.log_data( 'Datacenter created : %s'%str(datacenter))
        else:
            utility.log_data( 'Datacenter  not found : %s'%str(datacenter))
        if result.find(cluster):
            utility.log_data( 'Cluster created : %s'%str(cluster))
        else:
            utility.log_data( 'Cluster not found : %s'%str(cluster))
        for vm in globalVars.VMs:
            if result.find(vm):
                utility.log_data( 'VM Created : %s'%str(cluster))
                count=count + 1
        if count == 0:
            utility.log_data(" NO VM's created ")
            
                
    def doScvmmValidation(self,scvmmId):
        
        utility.log_data( " in doScvmmValidation ")
        resMD,statMD = self.getResponse("GET", "ManagedDevice")
        for devices in resMD:
            if devices['deviceType']=='scvmm' and devices['refId']==scvmmId:
                result = devices
        facts = result['facts']
        
        datacenter = globalVars.dcNamescvmm
        utility.log_data( 'datacenter name in Template : %s'%str(datacenter))
        cluster = globalVars.clusterNamescvmm
        utility.log_data( 'cluster name in Template : %s'%str(cluster))
        if  facts.find(datacenter):
            utility.log_data( 'Datacenter created : %s'%str(datacenter))
        else:
            utility.log_data( 'Datacenter  not found : %s'%str(datacenter))
        if facts.find(cluster):
            utility.log_data( 'Cluster created : %s'%str(cluster))
        else:
            utility.log_data( 'Cluster not found : %s'%str(cluster))
            
    
    def getDeploymentResponsByID(self,deploymentRefId):
        
        
        url = self.buildUrl("Deploy",deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()
        deploymentResponse = requests.get(url, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'GET',headers, ""), deploymentResponse.status_code, deploymentResponse.text, startTime, endTime, elapsedTime)
        return deploymentResponse
           
    def cleanDeployedService(self, deploymentRefId):
        
        deploymentResponse = self.getDeploymentResponsByID(deploymentRefId)
        payload = deploymentResponse.content
        
        payload= payload.replace("<teardown>false</teardown>", "<teardown>true</teardown>")
        payload= payload.replace("<id>ensure</id><value>present</value>", "<id>ensure</id><value>absent</value>")
        #.replace("<retry>false</retry>", "<retry>true</retry>")
        
        
        response = self.tearDownDevices(deploymentRefId, payload )
        
        utility.log_data("Cleaning Deployed  Services  Response Code is :%s"%str(response.status_code))
        
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
                    utility.log_data(str(e1))
                    break
                       
        
                loop -= 1
        
        else:
            utility.log_data( "Failed to Delete Resources for the deployment RefID : %s"%deploymentRefId)
            return "Failed to Delete Resources", False
        
        
    def tearDownDevices(self,deploymentRefId,payload):
        
        
        url = self.buildUrl("Deploy", deploymentRefId)
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headersPut=self.generateHeaderTearDown(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        startTime = datetime.datetime.now()        
        response = requests.put(url, data=payload, headers=headersPut, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headersPut,payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        return response
    
    
    def teardownServices(self, deploymentRefId):
        """
        Teardown to the existing Deployment
        """
        url = self.buildUrl("Deploy", deploymentRefId)
        
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "DELETE", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        
        startTime = datetime.datetime.now()
        response = requests.delete(url, headers=headers)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'DELETE',headers, ''), response.status_code, response.text, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        
        return response, True
    
    
    def scaleUpAndDeployService(self, templateID, deploymentRefId, DeplName, deplyDesc, componentPayload):
        """
        Scale up  to the existing Deployment
        """
        
        url = self.buildUrl("Deploy", deploymentRefId)
        
        
        postData = self.getScaleUpAndDeployServicePayload(templateID, deploymentRefId, DeplName, deplyDesc, componentPayload)
        
        
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + deploymentRefId
        headers=self.generateHeaderDeploy(uri, "PUT", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        
        startTime = datetime.datetime.now()
        response = requests.put(url, data=postData, headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        resResult=""
        if response.status_code  not in (200, 201, 202, 203, 204):
            resResult=response.content
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(url,'PUT',headers,postData), response.status_code, resResult, startTime, endTime, elapsedTime)
        
        time.sleep(globalVars.defaultWaitTime)
        time.sleep(globalVars.defaultWaitTime)
        return response


    def getScaleUpAndDeployServicePayload(self,templateID, deploymentRefId,  DeplName, deplyDesc, componentPayload):
        
        
        refIdVCenter = globalVars.refIdVCenter
        refIdEQ =  globalVars.refIdEQLogic
        
        resDT, statDT = self.getSuccessfullyDeploymentxml(deploymentRefId)
        if not statDT:
            utility.log_data("Unable to retrieve Deployed Template: %s"%resDT)
            return "Unable to retrieve Deployed Template: %s"%resDT, statDT       
                  
        temp = str(resDT).rpartition("</components>")
        deploymentTemplate = temp[0] + temp[1] + componentPayload + temp[2]    
       
        deploymentTemplate = deploymentTemplate.replace("<retry>false</retry>", "<retry>true</retry>")
        
        payload2 =self.getTemplatePayloadEsxi(deploymentTemplate,refIdVCenter, refIdEQ)        
        
        return payload2
    
    def getSuccessfullyDeploymentxml(self, refId):
        """
        Gets the Deployment Status
        """
        
        url = self.buildUrl("Deploy", refId)
        
        uri = globalVars.serviceUriInfo["Deploy"]+ "/" + refId
        headers=self.generateHeaderDeploy(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        
        response = requests.get(url, headers=headers, verify=False)
        result = response.content
        
        return result, True

    def buildUrl(self, feature, refId=None):
        """
        Builds a Service Url and Returns 
        """
        
        if globalVars.uriType == "Public":
            
            basePath = "https://"+  globalVars.configInfo['Appliance']['ip']
            #basePAth=http://i.p/9080
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

    def login(self, authStatus=False, loginUser="admin", loginPwd="admin", loginDomain="ASMLOCAL"):
        """
        Confirm User access credentials
        """
        payload = utility.readFile(globalVars.loginPayload)        
        payload = payload.replace("$login_user", loginUser).replace("$login_pwd", loginPwd).replace("$login_domain", loginDomain)
        payload = payload.replace('\n','').replace('\t','') 
        uri = self.buildUrl("Login")
        headers = {"Accept":"application/json","Content-Type":"application/xml", "Accept-Encoding":"gzip, deflate"}
        
        startTime = datetime.datetime.now()
        response = requests.post(uri,data=payload,headers=headers, verify=False)
        endTime = datetime.datetime.now()
        elapsedTime="%s"%(endTime-startTime)
        utility.log_data("%s\nAction: %s \nHeader: %s \nPayload: %s"%(uri,'POST',headers, payload), response.status_code, response.text, startTime, endTime, elapsedTime)
        

                       
        data = json.loads(response.text)
        result =utility.convertUTA(data)
        utility.log_data( 'Login  Respone : %s'%str(result))           
        globalVars.apiKey = str(result["apiKey"])
        globalVars.apiSecret = str(result["apiSecret"])
        
        print " api key"
        print  globalVars.apiKey
        utility.log_data( 'api key : %s'%str(globalVars.apiKey))
        
                                     
        return result , True
    
    
    def cleanUpDiscoveryRequests(self):
        """
        """
        resDR, statDR = self.getResponse("GET", "Discovery")
        if not statDR:
            return resDR, False
        for job in resDR:
            self.getResponse("DELETE", "Discovery", refId=job["id"])
    

    def cleanUpDevices(self):
        """
        """
        resDR, statDR = self.getResponse("GET", "ManagedDevice")
        if not statDR:
            return resDR, False
        for device in resDR:
            if "blade" not in str(device["deviceType"]).lower() and "I/O" not in str(device["deviceType"]).lower():
                self.getResponse("DELETE", "ManagedDevice", refId=device["refId"])
                time.sleep(60)


    def cleanUpNetworks(self):
        """
        """
        resDN, statDN = self.getResponse("GET", "Network")
        if not statDN:
            return resDN, False
        for job in resDN:
            self.getResponse("DELETE", "Network", refId=job["id"])


    def cleanUpCredentials(self):
        """
        """
        resDN, statDN = self.getResponse("GET", "Credential")
        if not statDN:
            return resDN, False
        for job in resDN:
            self.getResponse("DELETE", "Credential", refId=job["id"])


    def cleanUpServerPool(self, retnStatus=False):
        """
        Removes all Server Pools
        """
        resDN, statDN = self.getResponse("GET", "ServerPool")
        if not statDN:
            return resDN, False
        for job in resDN:            
            result, status = self.getResponse("DELETE", "ServerPool", refId=str(job["groupSeqId"]))
            if not status and retnStatus: 
                return result, status
        if retnStatus:
            return "Successfully Removed All Server Pools", True


    def cleanUpTemplates(self, retnStatus=False):
        """
        Removes all Templates
        """
        resDN, statDN = self.getResponse("GET", "Template")
        if not statDN:
            return resDN, False
        for temp in resDN:
            if temp["category"] == "Automation":
                result, status = self.getResponse("DELETE", "Template", refId=str(temp["id"]))
                if not status and retnStatus: 
                    return result, status
        if retnStatus:
            return "Successfully Removed All Templates", True


    def cleanUpServices(self, retnStatus=False):
        """
        Removes all Deployed Services
        """
        resDN, statDN = self.getResponse("GET", "Deploy")
        if not statDN:
            return resDN, False
        for temp in resDN:            
            result, status = self.getResponse("DELETE", "Deploy", refId=str(temp["id"]))
            if not status and retnStatus: 
                return result, status
        if retnStatus:
            return "Successfully Removed All Deployed Services", True


    def cleanUpChassis(self, retnStatus=False):
        """
        Removes all Deployed Services
        """
        resDN, statDN = self.getResponse("GET", "Chassis")
        if not statDN:
            return resDN, False
        for temp in resDN:            
            result, status = self.getResponse("DELETE", "Chassis", refId=str(temp["refId"]))
            if not status and retnStatus: 
                return result, status
            time.sleep(globalVars.defaultWaitTime)
        if retnStatus:
            return "Successfully Removed All Chassis", True
            

    def cleanUpServerInventory(self, retnStatus=False):
        """
        Removes all Deployed Services
        """
        resDN, statDN = self.getResponse("GET", "ServerInventory")
        if not statDN:
            return resDN, False
        for temp in resDN:            
            if "blade" not in str(temp["serverType"]).lower():
                result, status = self.getResponse("DELETE", "ServerInventory", refId=str(temp["refId"]))
                if not status and retnStatus:
                    return result, status
        if retnStatus:
            return "Successfully Removed All Servers", True


    def cleanUpUsers(self, retnStatus=False):
        """
        Removes all Templates
        """
        resUR, statUR = self.getResponse("GET", "User")
        if not statUR:
            return resUR, False
        for temp in resUR:            
            if temp["userName"] != "admin":
                result, status = self.getResponse("DELETE", "User", refId=str(temp["userSeqId"]))
                if not status and retnStatus: 
                    return result, status
        if retnStatus:
            return "Successfully Removed All Users", True


    def cleanUpDirectoryService(self, retnStatus=False):
        """
        Removes all Directory Services
        """
        resUR, statUR = self.getResponse("GET", "DirectoryService")
        if "No information found" in resUR:
            return "No Directory Services found to Remove", True
        if not statUR:
            return resUR, False
        for temp in resUR["configurationList"]:
            result, status = self.getResponse("DELETE", "DirectoryService", refId=str(temp["seqId"]))
            if not status and retnStatus: 
                return result, status
        if retnStatus:
            return "Successfully Removed All Directory Services", True
        
        
    def getHostName(self):
        """
        Returns Host Name
        """
        hostName = "HCL" + datetime.datetime.now().strftime("%d%H%M%S")
        time.sleep(1)
        utility.log_data("Host Name", hostName)
        return hostName

    def getHostGroupName(self):
        """
        Returns Host Group Name
        """
        hostGropName = "HHG" +str(randrange(10, 10000, 2))
        utility.log_data("Host Group Name", hostGropName)
        return hostGropName
    
    def getVolumeName(self):
        """
        Returns Volume Name
        """
        VolumeName = "HCLVol" +str(randrange(10, 10000, 2))
        utility.log_data("Volume Name", VolumeName)
        return VolumeName
    
    def getClusterName(self):
        """
        Returns Cluster Name
        """
        clusterName = "HCLCluster" +str(randrange(10, 10000, 2))
        utility.log_data("Cluster Name", clusterName)
        return clusterName
    
    def getDataCenterName(self):
        """
        Returns DataCenterName Name
        """
        dataCenterName = "HCLhost" +str(randrange(10, 10000, 2))
        utility.log_data(" DataCenterName Name", dataCenterName)
        return dataCenterName
    
    def getVMHostName(self):
        """
        Returns VM Host Name
        """
        vmHostName = "HCLVM" + datetime.datetime.now().strftime("%d%H%M%S")
        time.sleep(1)
        utility.log_data("VM Host Name", vmHostName)
        return vmHostName


    
    