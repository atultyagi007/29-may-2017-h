'''
Created on Aug 8, 2014

@author: dheeraj.singh
'''
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.insert(0,os.path.abspath('../../testcases/chassisconfig'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))

from DiscoverResourceBaseClass import DiscoverResourceTestBase

import json
import time
import globalVars
import datetime
import xml.etree.ElementTree as ET
# from lxml import etree
from xml.sax.saxutils import unescape

class Testcase(DiscoverResourceTestBase):
    
    tc_Id = ""
   
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def test_login(self):
        
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        
        

    def test_doDiscovery(self):
        logger = self.getLoggerInstance()
        finalStat = True
        creResponse,result = self.setupCredentials()             
        logger.debug('RESULT')
        logger.debug('CREDENTIAL RESPONSE')
        logger.info(creResponse)
        resourcesInfo,status = self.getResourcesInfo()
        if not status:
            return resourcesInfo,False
        try:
            for resources in resourcesInfo:
                if resources["Type"] == "SERVER":
                    
                    respServer,resSer = self.discoverServer(resources)
                    logger.debug( "******** response after discovery of  Server : ********* ")
                    logger.debug(respServer)
                    try:
                        self.getDiscoveryResourceStatus(respServer)
                        
                    except Exception as e1:
                        self.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        self.log_data(str(e1))
                        
                    
                    self.log_data( 'Total time taken in discovery of  SERVER  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    print 'Total time taken in discovery of SERVER IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respServer["id"]) 
                    self.log_data("Total Discovery Time of Servers in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of Servers in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    self.log_data("Total Discovery Time of Servers in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of Servers in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    
                    if resSer:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"Server Discovered Successfully"])
                    time.sleep(120)
                elif resources["Type"] == "CHASSIS":
                    
                    respChassis,resCh = self.discoverChassis(resources)
                    logger.debug(" ******** response after discovery of  Chassis : ********* ")
                    logger.debug(respChassis)
                    
                    try:
                        self.getDiscoveryResourceStatus(respChassis)
                        
                    except Exception as e1:
                        self.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        self.log_data(str(e1))
                        
                    
                    self.log_data( 'Total time taken in discovery of  CHASSIS  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of CHASSIS IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respChassis["id"]) 
                    self.log_data("Total Discovery Time of CHASSIS in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of CHASSIS in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    self.log_data("Total Discovery Time of CHASSIS in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of CHASSIS in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    
                    if resCh:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"CHASSIS Discovered Successfully"])
                    time.sleep(120)
                elif resources["Type"] == "STORAGE":
                    
                    respStorage,resStrg = self.discoverStorage(resources)
                    logger.debug(" ******** response after discovery of  Storage : ********* ")
                    logger.debug(respStorage)
                    try:
                        self.getDiscoveryResourceStatus(respStorage)
                        
                    except Exception as e1:
                        self.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        self.log_data(str(e1))
                        
                    
                    
                    self.log_data( 'Total time taken in discovery of  STORAGE  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of STORAGE IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respStorage["id"]) 
                    self.log_data("Total Discovery Time of STORAGE in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of STORAGE in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    self.log_data("Total Discovery Time of STORAGE in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of STORAGE in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    if resStrg:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"STORAGE Discovered Successfully"]) 
                    time.sleep(120)
                elif resources["Type"] == "SWITCH":
                    
                    respSwitch,resSw = self.discoverSwitch(resources)
                    logger.debug(" ******** response after discovery of  Switch : ********* ")
                    logger.debug(respSwitch)
                    try:
                        self.getDiscoveryResourceStatus(respSwitch)
                        
                    except Exception as e1:
                        self.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        self.log_data(str(e1))
                        
                    
                    
                    self.log_data( 'Total time taken in discovery of  SWITCH  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of SWITCH IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respSwitch["id"]) 
                    self.log_data("Total Discovery Time of SWITCH in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of SWITCH in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    self.log_data("Total Discovery Time of SWITCH in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of SWITCH in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    
                    if resSw:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"SWITCH Discovered Successfully"])
                    time.sleep(120)
                elif resources["Type"] == "VCENTER":
                    
                    respVC,resVC = self.discoverVCenter(resources)
                    logger.debug(" ******** response  after discovery of  VCenter : ********* ")
                    logger.debug(respVC)
                    try:
                        self.getDiscoveryResourceStatus(respVC)
                        
                    except Exception as e1:
                        self.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        self.log_data(str(e1))
                        
                    
                    
                    self.log_data( 'Total time taken in discovery of  VCENTER  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of VCENTER IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respVC["id"]) 
                    self.log_data("Total Discovery Time of VCENTER in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of VCENTER in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    self.log_data("Total Discovery Time of VCENTER in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of VCENTER in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    if resVC:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"VCENTER Discovered Successfully"])
                    time.sleep(120)
                elif resources["Type"] == "SCVMM":
                    
                    respVM,resVM = self.discoverVM(resources)
                    logger.debug(" ******** response after discovery of  scvmm : ********* ")
                    logger.debug(respVM)
                    try:
                        self.getDiscoveryResourceStatus(respVM)
                        
                    except Exception as e1:
                        self.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        self.log_data(str(e1))
                        
                    
                    
                    self.log_data( 'Total time taken in discovery of  SCVMM  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of SCVMM IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respVM["id"]) 
                    self.log_data("Total Discovery Time of SCVMM in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of SCVMM in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    self.log_data("Total Discovery Time of SCVMM in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of SCVMM in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    if resVM:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"SCVMM Discovered Successfully"]) 
                    time.sleep(120)
                    
                elif resources["Type"] == "EnterpriseManager":
                    
                    respVM,resVM = self.discoverEMC(resources)
                    logger.debug(" ******** response after discovery of  EnterpriseManager : ********* ")
                    logger.debug(respVM)
                    try:
                        self.getDiscoveryResourceStatus(respVM)
                        
                    except Exception as e1:
                        self.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        self.log_data(str(e1))
                        
                    
                    
                    self.log_data( 'Total time taken in discovery of  EnterpriseManager  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of EnterpriseManager IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respVM["id"]) 
                    self.log_data("Total Discovery Time of EnterpriseManager in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of EnterpriseManager in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    self.log_data("Total Discovery Time of EnterpriseManager in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of EnterpriseManager in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    if resVM:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"EnterpriseManager Discovered Successfully"]) 
                    time.sleep(120)
                else:
                    print " Invalid Resource Type "
        
        except Exception as e2:
            self.log_data( 'Exception occurred while  doDiscovery ')
            self.log_data(str(e2))
            
            finalStat=False
            
            
        
        return finalStat
        
                

        
    def getResourcesInfo(self):
        inputFile = globalVars.discovery 
       
        try:
            result, status = self.readCsvFile(inputFile)
            if not status:
                print "Unable to read Discovery input File: %s"%inputFile , False
            header = result[0]      
            return [dict(zip(header,result[row])) for row in xrange(1,len(result))], True
        except:
            print "Columns mismatch in the Configuration File: %s"%inputFile, False

          
        
    
    
    
    def populateResourceInfo(self):
        self.getResources()
        
        
    def getDiscoveryResourceStatus(self, discoveryPostResponse):

        time.sleep(globalVars.defaultWaitTime)
        wait = 10  
        resDS = None
        discoveryStatus = False
               
        while wait:
            refId=discoveryPostResponse["id"]
            discoveryResponse = self.getDiscoveryStatusByRefID(refId)
            if discoveryResponse.status_code in (200, 201, 202, 203, 204):
                discoveryStatus = True
                data = json.loads(discoveryResponse.text)
                resDS=  self.convertUTA(data)
                if "No information found for Discovery" in resDS:
                    break
                
                status = resDS["status"]               
                if status.lower() == "inprogress":
                    time.sleep(120)
                    continue
                else:
                    time.sleep(globalVars.defaultWaitTime)
                    
                    
            elif discoveryResponse.status_code == 404:
                break
            
            wait = wait -1
        
            
            
        return discoveryStatus, discoveryPostResponse["id"]
        
        
    def testjobstatus(self):
        
        jobname='Job-cd1f6477-6398-416e-8696-345dac6035f6' 
        elaspedTime, jobStatus =self.getjobhistoryelaspedTime(jobname) 
        print elaspedTime
        print jobStatus


if __name__ == "__main__":
    test = Testcase()
    test.test_login()
    status = test.test_doDiscovery()
    #test.testjobstatus()
    
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)