'''
Created on March 1, 2017

@author: raj.patel
Description: Test case to verify  Network's URI:
/Network/{networkId}/usageids    get
/Network/{networkId}/ipaddresses  get
'''

import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../definenetwork'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from DefNetworkBaseClass import DefNetworkTestBase
import json
import time


class Testcase(DefNetworkTestBase):
    tc_Id = "" 
    
    def __init__(self):
        DefNetworkTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
    
    def test_login(self):
        response = self.authenticate()
        loginResponse  = json.loads(response)
        logger = self.getLoggerInstance()
        logger.info(' login response in network test ')
        logger.debug(loginResponse)
        
        
    def test_Network(self):
        logger = self.getLoggerInstance()
        resNET, statNET = self.getResponse("GET", "Network")
        
        netIDList = [network["id"] for network in resNET]
        self.log_data("Network id")
        self.log_data(netIDList)
#         for nwid in netIDList:
#             
#             resultNW, statusNW = self.usedNetworksIdList(nwid=nwid)
#             logger.debug(resultNW)
#             if statusNW:
#                 self.log_TestData(["", "", "",str(self.tc_Id),'Successfully get used network id:%s'%(nwid),"SUCCESS","Used Networks id Successfully get"])
#             else:
#                 self.log_TestData(["", "", "",str(self.tc_Id),"Failed to get used network id",'Failure',"Failed to get used Networks"])   
        
        
        for nwid in netIDList:
            
            resultNW, statusNW = self.findNetworksIdAddress(nwid=nwid,verify=False)
            print resultNW
            logger.info("IP Addresses")
            logger.debug(resultNW)
            if statusNW:
                self.log_TestData(["", "", "",str(self.tc_Id),'Successfully get network ip:%s'%(nwid),"SUCCESS","Successfully get network ip"])
            else:
                self.log_TestData(["", "", "",str(self.tc_Id),"Failed to get used network id",'Failure',"Failed to get Networks ip"])   
        
        

        
       

if __name__ == "__main__":
    test = Testcase()
    
    test.test_login()
    status = test.test_Network()
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
    else:
        os.chdir(current_dir)
        sys.exit(1)
        
