'''
Created on Sep 18, 2014

@author: waseem.irshad
deviceType = vcenter
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
import globalVars
import templateInputReqValue
import DiscoveryResources
import time
import copy

''' Discover VCenter as managed, after discovering should be able to unmanage it from resource page '''

class TestCase(DiscoverResourceTestBase,DiscoveryResources.Testcase):
   
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        DiscoveryResources.Testcase.__init__(self)
        
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        
        """
        response = self.authenticate()
        refIDList =self.getRefIDManageDevice('vcenter')
        for refId in refIDList:
            resDD, statDD = self.getResponse("DELETE", "ManagedDevice", refId=refId)
            
        
        
    def runTestCase(self):
        
        creResponse,result = self.setupCredentials()
        responDS,stateDS = self.discoverVCenter(unmanaged = False)
        if stateDS:
            self.changeState()
      
        
        
    def changeState(self):
       
        
        loop = 60
        refIDList = []
        while loop:
            time.sleep(30)
            refList =self.getRefIDManageDevice('vcenter')
            if len(refList)>0:
                refIDList=copy.deepcopy(refList)
                break;
            loop -= 1
        
        for refId in refIDList:
            resDD,statDD =  self.getResponse("GET", "ManagedDevice",refId=refId)
            
            
            self.deviceInfo = resDD
            resCRS, statCRS = self.changeResourceState(refId, self.deviceInfo, state="UNMANAGED")
            
        

        
        
if __name__ == "__main__":
    test = TestCase()
    test.preRunSetup()
    test.runTestCase()
    os.chdir(current_dir)
    
    
   

    