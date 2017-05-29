'''
Created on Aug 8, 2014

@author: dheeraj.singh
'''
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
from utilityModule import UtilBase


import json
import time
import globalVars
import datetime
import xml.etree.ElementTree as ET
# from lxml import etree
from xml.sax.saxutils import unescape

class Testcase(UtilBase):
    
    tc_Id = ""
   
    
    def __init__(self):
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def test_login(self):
        
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        
        
    def runInventory(self):
        """
        Runs Inventory on the specified Device
        """
        resServer, resStorage, resVCenter, resChassis, resVM, resComp, resswitch, resElementMgr = self.getResources()
        
        deviceList = []
        
        if globalVars.resourceType == "SERVER":
            print "SERVER" 
            print resServer 
            deviceList = resServer
            
        elif globalVars.resourceType == "CHASSIS":
            print "CHASSIS" 
            print resChassis 
            deviceList = resChassis
            
        elif globalVars.resourceType == "STORAGE":
            print "STORAGE" 
            print resStorage 
            deviceList = resStorage
            
        elif globalVars.resourceType == "VCENTER":
            print "VCENTER" 
            print resVCenter 
            deviceList = resVCenter
            
        elif globalVars.resourceType == "SCVMM":
            print "SCVMM" 
            print resVM 
            deviceList = resVM
            
        elif globalVars.resourceType == "COMPELLENT":
            print "COMPELLENT" 
            print resComp 
            deviceList = resComp
        elif globalVars.resourceType == "SWITCH":
            print "SWITCH" 
            print resswitch 
            deviceList = resswitch
            
        elif globalVars.resourceType == "ElementManager":
            print "ElementManager" 
            print resElementMgr 
            deviceList = resElementMgr     
            
        deviceRefID=''
        modelNo=''
        for device in deviceList:
            
            if device['ip']==globalVars.IP_Address_Resource:
                deviceRefID = device['refid']
                #modelNo = device['model']
                
        self.log_data("Running Inventory for  Device IP  :: %s"%globalVars.IP_Address_Resource + "\t and  Resource Type :: %s"%globalVars.resourceType+"\t and  Model :: %s"%modelNo)
        print "Running Inventory for  Device IP  :: %s"%globalVars.IP_Address_Resource+ "\t and Resource Type :: %s"%globalVars.resourceType+"\t and  Model :: %s"%modelNo
        respMD = self.getManagedDeviceRefID(deviceRefID)
         
        payload=respMD.content
#          , deviceRefID
        resMD, statMD = self.getResponse("PUT", "ManagedDevice", payload, deviceRefID)     
         
            
        if not statMD:
            self.log_data("","Unable to Run Inventory Because Not able to find the Device IP in Managed Device and Response is : %s"%str(resMD))
            print "Unable to Run Inventory Because Not able to find the Device IP in Managed Device and Response is %s"%str(resMD)
            return
        time.sleep(120)
        elaspedTimeMillis=self.getJobExecutionStatusByJobID(resMD['jobName'])
        self.log_data("Total Run Inventory Time in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
        print "Total Run Inventory Time  in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
        self.log_data("Total Run Inventory Time  in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
        print "Total Run Inventory Time  in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    

              
    
    
        


if __name__ == "__main__":
    test = Testcase()
     
    test.test_login()
    status = test.runInventory()
#     if status==True:
#         os.chdir(current_dir)
#         sys.exit(0)
#     else:
#         os.chdir(current_dir)
#         sys.exit(1)