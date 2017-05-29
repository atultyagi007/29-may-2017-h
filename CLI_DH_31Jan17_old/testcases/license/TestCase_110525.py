'''
Created on Nov 5, 2015

@author: waseem.irshad
'''

import os
import sys
import requests
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)

sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
import globalVars
import time
from utilityModule import UtilBase

class TestCase(UtilBase):
    
    '''
    Delete  resources, available resources should reflect the change
    '''
    
    tc_Id=""
    def __init__(self):
        UtilBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
    
        
    
    def calculateAvailableResources(self):
        self.authenticate()
        print " \n In function calculateAvailableResources : "
        respLicen,status = self.getResponse("GET","License")
        if not status:
            self.log_data(" Could not get Response from License")
        else:
            count = respLicen['nodes']
            print " Total number of Resources : %s"%str(count)
            self.log_data(" License Count : %s"%str(count))
        print " \nGoing to calculate no of Used Resources ::"
        useResCount = self.calculateUsedResources()
        availableRes = int(count) - int(useResCount)
        print " \n Total no of Available Resources = "
        print availableRes
        self.log_data(" Total no of Available Resources : %s"%str(availableRes))
        
        
        
    def returnDevicesCount(self,filterType):
        feature = "ManagedDeviceCount"
        serverFilter = """?filter=eq%2CdeviceType%2CRackServer%2CBladeServer%2CServer%2CFXServer"""
        switchFilter = """?filter=eq%2C+deviceType%2C+TOR%2C+AggregatorIOM%2C+MXLIOM%2C+genericswitch%2C+dellswitch"""
        storageFilter = """?filter=eq%2C+deviceType%2C+compellent%2C+equallogic%2C+netapp"""
        basePath = "http://"+  globalVars.configInfo['Appliance']['ip'] + ":" + globalVars.configInfo['Appliance']['port']
        uri = globalVars.serviceUriInfo[feature]
        if filterType == "ServerFilter":
            url = basePath +  uri + serverFilter
        if filterType == "IOMFilter":
            url = basePath +  uri + switchFilter
        if filterType == "StorageFilter":
            url = basePath +  uri + storageFilter
        print " URL is : %s"%str(url)    
        headers=self.generateHeaderTearDown(uri, "GET", globalVars.apiKey, globalVars.apiSecret, globalVars.userAgent)
        count = requests.get(url, headers=headers)
        return count.text
        
            
    def calculateUsedResources(self):
        print " \n In function calculateUsedResources : "
        serverCount = self.returnDevicesCount("ServerFilter")
        iomCount = self.returnDevicesCount("IOMFilter")
        storageCount = self.returnDevicesCount("StorageFilter")
        print " \n Server Count = "
        print serverCount
        print "\n iomCount = "
        print iomCount
        print "\n storageCount = "
        print storageCount
        print " \n Total no of Used Resources : \n"
        usedCount = int(serverCount)+int(iomCount)+int(storageCount) 
        print usedCount
        return int(usedCount)
        
        
        
    def deleteResources(self):
        self.authenticate()
        deviceTypeFound = False
        resCMD,statCMD = self.getResponse("GET","ManagedDeviceCount")
        if not statCMD:
            print " \n Unable to get the Devices count "
        print "\n Total resources in Resources page = "
        print resCMD
        print " \n Going to delete 2 resources : "
        resMD,statMD = self.getResponse("GET", "ManagedDevice")
        if not statMD:
            print " \n Unable to get the resources information"
        deviceType = globalVars.deviceTypetoDelete
        noOfResourcestoDelete = globalVars.countToDelete 
        count = 0 
        print " ResourcesType to Delete : %s"%str(deviceType)
        self.log_data(" Resource Type to Delete : %s"%str(deviceType))
        print " No. Of Resources to delete : %s"%str(noOfResourcestoDelete)
        self.log_data(" No of Resources to Delete : %s"%str(noOfResourcestoDelete))
        for device in resMD:
            if device['deviceType']==deviceType:
                deviceTypeFound = True
                refId = device['refId']
                print " refID of device : "
                print refId
                print "\n Count = "
                print count
                self.getResponse("DELETE","ManagedDevice",refId=refId)
                time.sleep(20)
                count = count + 1
                if count == noOfResourcestoDelete:
                    print " \n Breaking from the loop #####"
                    print "\n Count = "
                    print count
                    break
                else:
                    continue
            
        if deviceTypeFound == False:
            print " \n Unable to find the given device type"
            self.log_data("Unable to find the given device type")
                
        resCMD,statCMD = self.getResponse("GET","ManagedDeviceCount")
        if not statCMD:
            print " \n Unable to get the Devices count "
        else:
            print "\n Total resources in Resources page After Delete = "
            print resCMD
        
        
if __name__ == "__main__":
    test = TestCase()
    test.deleteResources()
    test.calculateAvailableResources()                
