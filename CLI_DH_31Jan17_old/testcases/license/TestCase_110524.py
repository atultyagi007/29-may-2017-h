'''
Created on Nov 9, 2015

@author: waseem.irshad
'''

import os
import sys
import json

run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)

sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../testcases/License'))
sys.path.insert(0,os.path.abspath('../../testcases/chassisconfig'))

import globalVars
import time
from utilityModule import UtilBase
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import TestCase_110525


class Testcase(UtilBase,DiscoverResourceTestBase):
    
    '''
    Discover more resources, available resources should reflect the change
    Pre-Requisite : License should be present in the Appliance
    '''
    
    tc_Id=""
    def __init__(self):
        UtilBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def checkAvailableResourcesBeforeDiscovery(self):
        print " \n Going to calculate available resources before discovery ...."
        obj = TestCase_110525.TestCase()
        obj.calculateAvailableResources()
        
        #calculateAvailableResources
        
    def discoverResources(self):
        creResponse,result = self.setupCredentials()
        resourcesInfo,status = self.getResourcesInfo()
        print " \n Resource Info : %s"%str(resourcesInfo)
        if not status:
            return resourcesInfo,False
        for resources in resourcesInfo:
            if resources["Type"] == "SERVER":
                print "\n Going to Discover Server...."
                self.log_data("\n Going to Discover Server....")
                respServer,resSer = self.discoverServer(resources)
                self.log_data(" \n Discovery Response : ")
                self.log_data(respServer)
                stat = self.getDiscoveryResourceStatus(respServer)
                if stat == True:
                    self.log_data(" Server Resource Discovered ")
            elif resources["Type"] == "STORAGE":
                print "\n Going to Discover Storage...."
                self.log_data("\n Going to Discover Storage....")
                respStorage,resStrg = self.discoverStorage(resources)
                stat = self.getDiscoveryResourceStatus(respStorage)
                if stat == True:
                    self.log_data(" Storage Resource Discovered ")
            elif resources["Type"] == "SWITCH":
                print "\n Going to Discover Switch...."
                self.log_data("\n Going to Discover Switch....")
                respSwitch,resSw = self.discoverSwitch(resources)
                statSD = self.getDiscoveryResourceStatus(respSwitch)
                if statSD == True:
                    self.log_data(" Switch Resource Discovered ")
                

        
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
            
            
    def getDiscoveryResourceStatus(self, discoveryPostResponse):

        time.sleep(globalVars.defaultWaitTime)
        wait = 10  
        resDS = None
        discoveryStatus = False
               
        while wait:
            refId=discoveryPostResponse["id"]
            discoveryResponse = self.getDiscoveryStatusByRefID(refId)
            print " \n Discovery Status : %s"%str(discoveryResponse.status_code)
            if discoveryResponse.status_code in (200, 201, 202, 203, 204):
                discoveryStatus = True
                data = json.loads(discoveryResponse.text)
                resDS=  self.convertUTA(data)
                if "No information found for Discovery" in resDS:
                    break
                
                status = resDS["status"]
                print " \n Status if resources : %s"%str(status)            
                if status.lower() == "inprogress":
                    time.sleep(120)
                    continue
                else:
                    time.sleep(globalVars.defaultWaitTime)
                            
            elif discoveryResponse.status_code == 404:
                break
            wait = wait -1    
        return discoveryStatus
    
    def checkAvailableResourcesAfterDiscovery(self):
        print " \n Going to calculate available resources after discovery ...."
        obj = TestCase_110525.TestCase()
        obj.calculateAvailableResources()
    
    
if __name__ == "__main__":
    test = Testcase()
    test.checkAvailableResourcesBeforeDiscovery()
    test.discoverResources()
    time.sleep(30)
    test.checkAvailableResourcesAfterDiscovery()
    
        
        
    
