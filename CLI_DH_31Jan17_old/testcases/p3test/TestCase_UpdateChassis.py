'''
Created on May 03, 2017

@author: raj.patel
Description:Testcase covers these api
/Chassis/{refId}    put

'''
import os
import sys
from datetime import date, datetime
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
    Discover a chassis and update chassis configuration
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
        
        
        
    def runTestCase(self):
        
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
                self.chassisName = vals['name']

            print "service Tag : "
            print self.chassisServiceTag

        if (returnVal == True):
            refId,chassisIP,deviceRefId = self.getdeviceRefId()
            logger.debug( " refId, chassisIP , deviceRefId : ")
            logger.debug( refId)
            logger.debug(chassisIP)
            logger.debug(deviceRefId)
            
            resXML,stausInfo=self.getPayloadInXML("GET","Chassis",deviceRefId) 
            if stausInfo:
                self.newChassisName = "HCLNoida" + datetime.datetime.now().strftime("%d%m%y")
                if self.chassisName != self.newChassisName:
                    resDevice=resXML.replace("<name>"+self.chassisName+"</name>","<name>"+self.newChassisName+"</name>") 
                    print resDevice
                    resDevice1,state=self.getResponse("PUT", "Chassis",payload=resDevice,refId=deviceRefId)
                    if state:
                        self.log_data(resDevice1)
                        self.log_data("Test case passed Successfully tested /Chassis/{refId} with PUT action")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully tested /Chassis/{refId} with PUT action','Test case passed'])
                    else:
                        self.log_data("Test case Failed")
                        self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
                        return False
                    
                    if state:
                        resDevice=resDevice.replace("<name>"+self.newChassisName+"</name>","<name>"+self.chassisName+"</name>")
                        print resDevice
                    else:
                        return False
                    resDevice,stat1=self.getResponse("PUT", "Chassis",payload=resDevice,refId=deviceRefId)
                    
                    if stat1:
                        self.log_data("Test case passed successfully and reverted service tag")

                    else:
                        self.log_data("Test case Failed")
                        return False 
                    return True        
                
                
             
                 
            
        
                          
if __name__ == "__main__":
    test = Testcase()
    test.test_Login()
    status = test.runTestCase()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)
    


