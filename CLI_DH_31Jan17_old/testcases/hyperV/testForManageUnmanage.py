'''
Created on Aug 29, 2014

@author: waseem.irshad
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../discoverresources'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/common'))
sys.path.append(os.path.abspath('../../createdeploytemplate'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from DiscoverResourceBaseClass import DiscoverResourceTestBase
import json
from util import globalVars
import templateInputReqValueHyperV
import DiscoveryResources

class TestCase(DiscoverResourceTestBase,DiscoveryResources.Testcase):
   
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        DiscoveryResources.Testcase.__init__(self)
    
    def test_login(self):        
        response = self.authenticate()
        
        
    def test_manageUnmanageServer(self):
        resDD, statDD = self.getDeviceList()
        self.log_data( " Managed Devices : ")
        self.log_data( resDD)
        
        
        refIds = []
        for device in resDD:
            devicetype=device['deviceType']
            if devicetype == "RackServer":
                refId = device['refId']
                refIds.append(refId)
        for refID in refIds:
            resDel, statDel = self.getResponse("DELETE", "ManagedDevice", refId=refID)
            if not statDel:
                print " Unable to remove Discovered server with refId : '%s'" %str(refID)
            else:
                print " Able to remove discovered Server "
                
    def test_discoveryServers(self):
        creResponse,result = self.setupCredentials()
        logger = self.getLoggerInstance()               
        logger.debug('RESULT')
        logger.debug('CREDENTIAL RESPONSE')
        logger.info(creResponse)               
        response = self.discoverServer()
        logger.debug('response after discoverServer  ')
        logger.info(response)        
        discStatus = self.verifyDiscoveryStatus(response)
        logger.debug(' response after verifyDiscoveryStatus ')
        logger.info(discStatus)        
        resmD =  self.manageDevice(discStatus)
        logger.debug(' PRINTING MANAGE DEVCE ')
        logger.info(resmD)
                
    def test_ServerPoolCreation(self):
        #self.test_discoveryServers()
        resSPCreate = self.test_ServerPool()
        
        
    
        
    def test_testForgroupSeqId(self):
        response = self.getResponse("GET", "ServerPool")
        
        poolList = response[0]
        for grpName in poolList:
            if (grpName['groupName'] == globalVars.serverPoolName):
                groupSeqId = grpName['groupSeqId']
        self.log_data( " groupSeqID is : ")
        self.log_data( groupSeqId)
        templateInputReqValueHyperV.GlobalPool = groupSeqId
    
        
if __name__ == "__main__":
    test = TestCase()
    test.test_ServerPoolCreation()
    test.test_testForgroupSeqId()
    #test.test_manageUnmanageServer()
    os.chdir(current_dir)
   

    