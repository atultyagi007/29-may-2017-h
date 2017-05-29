'''
Created on March 3, 2017

@author: raj.patel
Description:Test case to verify  DiscoveryRequest's URI:
/DiscoveryRequest/{id}    delete
/DiscoveryRequest/discoveryresult/{id}    get (Not valid)
 
'''
import os
import sys
import xml.etree.ElementTree as ET
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
        
    def getDiscoveryResourceStatus(self, discoveryPostResponse):

#         time.sleep(globalVars.defaultWaitTime)
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

    def discoveryOfServer(self):      
        logger = self.getLoggerInstance()

        resourcesInfo,status = self.getResourcesInfo()
        if not status:
            return resourcesInfo,False
        try:
            for resources in resourcesInfo:
                if resources["Type"] == "SERVER":
                    
                    respServer,resSer = self.discoverServer(resources)
#                     refID=respServer["id"]
#                     resDevice,state=self.getResponse("GET", "DiscoveryResultList",refId=refID)
#                     if state:
#                         self.log_data("Successfully get resource by id uri /DiscoveryRequest/discoveryresult")
#                 
#                     else:
#                         self.log_data("Resources not get by id response code is 204:")
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
                        time.sleep(5)
                        return respServer , True
        except Exception as e2:
            self.log_data( 'Exception occurred while  doDiscovery ')
            self.log_data(str(e2))
    
       
        
    def runTestCase(self):
        """
        This is the execution starting function
        """
        self.log_data("Running Test Case ::: ")
        
        respServer ,status = self.discoveryOfServer()
        print respServer
        if status :
            deviceIP = respServer['discoveryRequestList']['discoverIpRangeDeviceRequests'][0]['deviceStartIp']
            Id=respServer["id"]
            resDevice,status=self.getResponse("DELETE", "Discovery",refId=Id)
            if status:
                self.log_data("Test case passed successfuly delete and post URI Tested")
                self.log_TestData(["", "", "",str(self.tc_Id), 'Test case passed successfully delete and post URI Tested','Successfully Test case passed'])
            else:
                self.log_data("Test case Failed")
                self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
        
        
if __name__ == "__main__":
    test = Testcase()
    test.test_Login()
    test.runTestCase()
    os.chdir(current_dir)