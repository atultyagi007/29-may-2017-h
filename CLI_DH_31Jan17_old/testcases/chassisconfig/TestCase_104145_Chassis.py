'''
Created on Feb 28, 2017

@author: raj.patel
Description:
/Chassis/{refId}    get
/Chassis/{refId}    delete

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

    def discoveryOfChassis(self):      
        logger = self.getLoggerInstance()
        creResponse,result = self.setupCredentials()             
        logger.debug('RESULT')
        logger.debug('CREDENTIAL RESPONSE')
        logger.info(creResponse)
        resourcesInfo,status = self.getResourcesInfo()
        if not status:
            return resourcesInfo,False
        try:
            for resources in resourcesInfo:
                if resources["Type"] == "CHASSIS":
                    
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
        except Exception as e2:
            self.log_data( 'Exception occurred while  doDiscovery ')
            self.log_data(str(e2))
    
    def preRunSetUp(self):
        self.discoveryOfChassis()
    
            
    def postRunSetUp(self):
        self.discoveryOfChassis()
        
    def runTestCase(self):
        
        """
        This is the execution starting function
        """
        
        response,status = self.getResponse("GET", "Chassis")
        print response
        if not status:
            print "Not get chassis list"
            self.log_data("Getting chassis list Info failed")
            self.log_TestData([str(self.tc_Id), "", "",str(self.tc_Id), 'Test case failed', 'Success','Test case failed'])
            return False
        
        refId=""
        for device in response:
                refId = device['refId']
                self.log_data(" refID of device : ")
                self.log_data(refId)
                resp,status = self.getResponse("GET", "Chassis",refId=refId)
                if not status:
                    print resp
                    self.log_TestData([str(self.tc_Id), "", "",str(self.tc_Id), 'Test case failed', 'Success','Test case failed'])
                    return False
                
                    
        for device in response:
                refId = device['refId']
                self.log_data(" refID of device : ")
                self.log_data(refId)
                response,status = self.getResponse("DELETE", "Chassis",refId=refId)
                if not status:
                    self.log_TestData([str(self.tc_Id), "", "",str(self.tc_Id), 'Test case failed', 'Success','Test case failed'])
                    return False        
        
        self.log_TestData([str(self.tc_Id), "", "",str(self.tc_Id), 'get and delete api of /Chassis is fine','Successfully Test case passed'])
        
        
        
                     
            
        
                          
if __name__ == "__main__":
    test = Testcase()
    test.test_Login()
    test.preRunSetUp()
    test.runTestCase()
    test.postRunSetUp()
    os.chdir(current_dir)
    


