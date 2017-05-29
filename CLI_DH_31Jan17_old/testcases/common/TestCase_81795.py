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
from DiscoveryResources import Testcase
import json
import globalVars
import templateInputReqValue
import DiscoveryResources
import time
import copy

''' Discover storage as unmanaged, after discovering should be able to manage it from resource page '''

class TestCase(DiscoverResourceTestBase,DiscoveryResources.Testcase):
   
    tc_Id = ""
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        DiscoveryResources.Testcase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        
        """
        self.log_data(" PreRunSetUp : ")
        response = self.authenticate()
        refIDList =self.getRefIDManageDevice('equallogic')
        for refId in refIDList:
            resDD, statDD = self.getResponse("DELETE", "ManagedDevice", refId=refId)
            print " statDD : %s"%str(statDD)
            print " resDD : %s"%str(resDD)
        
        
    def runTestCase(self):
        
        self.log_data(" Running Test Case :: ")
        creResponse,result = self.setupCredentials()
        responDS,stateDS = self.discoverStorage(unmanaged=True)
        if stateDS:
            self.log_data( 'Successfully Discovered Storage ')
            self.log_data( " response after Discovery : %s"%str(responDS))
            self.changeState()
      
               
    def changeState(self):
       
        self.log_data("Changing the state to Managed : ")
        loop = 60
        refIDList = []
        while loop:
            time.sleep(30)
            refList =self.getRefIDManageDevice('equallogic')
            if len(refList)>0:
                refIDList=copy.deepcopy(refList)
                break;
            loop -= 1
        
        for refId in refIDList:
            resDD,statDD =  self.getResponse("GET", "ManagedDevice",refId=refId)
            print " statDD : %s"%str(statDD)
            print " resDD : %s"%str(resDD)
            
            #response = self.convertResponse(resDD)
            #print " resDD : %s"%str(response)
            self.deviceInfo = resDD
            resCRS, statCRS = self.changeResourceState(refId, self.deviceInfo, state="DISCOVERED")
            self.log_data(" status : %s"%str(statCRS))
            self.log_data(" response : %s"%str(resCRS))
        

        
        
if __name__ == "__main__":
    test = TestCase()
    test.preRunSetup()
    test.runTestCase()
    os.chdir(current_dir)
    
   

    