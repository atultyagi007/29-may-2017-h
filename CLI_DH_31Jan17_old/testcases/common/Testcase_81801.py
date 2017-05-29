'''
Created on Sep 18, 2014

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
import DiscoveryResources
import time
import copy

''' Create server pool with unmanaged servers '''

class TestCase(DiscoverResourceTestBase,DiscoveryResources.Testcase):
   
    tc_Id=""
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        DiscoveryResources.Testcase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        
        """
        self.log_data(" Running Pre Run SetUp::")
        response = self.authenticate()
        refIDList =self.getRefIDManageDevice('RackServer')
        for refId in refIDList:
            self.log_data(" Removing Servers ")
            resDD, statDD = self.getResponse("DELETE", "ManagedDevice", refId=refId)
        
        
    def runTestCase(self):
        
        self.log_data(" Running Test Case ::")
        creResponse,result = self.setupCredentials()
        responDS,stateDS = self.discoverServer(unmanaged=True)
        if stateDS:
            self.log_data('Successfully Discovered servers to create Pool ')
            
        resMD,statMD = self.getResponse("GET", "ManagedDevice")
        servers = []
        for device in resMD:
            if device['deviceType'] == "RackServer":
                servers.append(device)
        if(len(servers)!=0):
            self.log_data(" Creating ServerPool with Name UnmanagedPool")
            responseCSP,statCSP = self.createServerPool(servers,'UnmanagedPool')
            self.log_data(" status : %s"%str(statCSP))
            self.log_data( " response : %s"%str(responseCSP))
        
        
if __name__ == "__main__":
    test = TestCase()
    test.preRunSetup()
    test.runTestCase()
    os.chdir(current_dir)
