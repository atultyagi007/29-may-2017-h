'''
Created on march 23, 2017

@author: raj.patel
Description:
/FirmwareRepository/{id} post
/FirmwareRepository/{id} put

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
import globalVars
from encodings import raw_unicode_escape
import os
import time

class Testcase(DiscoverResourceTestBase):
    
    td_Id=""
    refId=""
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
        
    def uploadNFSCatalog(self):
        
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        self.log_data("Running Test Case ::: ")
        response,status = self.createRepoFromNFS()
        if status:
            self.refId=response["id"]
            print "refID"
        print "====== RESPONSE2 : ===== "
        print response
        
        downloadStatusRepo = self.getAllNFSCatalog()
        
        if downloadStatusRepo:
            self.refId=response["id"]
            print "refID"
            print self.refId
            response = self.setDefaultCatalogTrue(self.refId)
            if response.status_code in (200, 201, 202, 203, 204):
                self.log_TestData(["", "", "", str(self.tc_Id),'Success','Repository downloaded successfully selected as a default','Selected as a default'])
            self.setDefaultCatalogFalse(self.refId)
            
            #self.setDefaultCatalogTrue(refId)
        
        
        
    def getAllNFSCatalog(self):
        
        counter = 110
        while counter > 0 :
            resFR, statFR = self.getResponse("GET", "FirmwareRepository",refId=self.refId)
            if not statFR:
                print " Could not get response from FirmwareRepository : %s"%str(resFR)
                self.log_data( 'Could not get response from FirmwareRepositor : %s'%str(resFR))
            print " resMD : "
            print resFR
            dStatus = resFR['downloadStatus']
            if dStatus.lower()=='available':
                print " Repository Download Successful : %s"%str(resFR)
                self.log_data( 'Repository Download Successful  : %s'%str(resFR))
                self.log_TestData(["", "", "", str(self.tc_Id),'Success','Repository Downloaded Successfully'])
                return True
                break
            elif dStatus.lower()=='error':
                print " Repository download failed %s"%str(resFR)
                self.log_data( 'Repository download failed : %s'%str(resFR))
                self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Failed to download Repository'])
                return False
                break
            else:
                time.sleep(120)
                counter = counter - 1
                print " Downloading firmware packages and  FirmwareRepository download status is : %s"%str(dStatus)
                self.log_data( 'FirmwareRepository download status is  : %s'%str(dStatus))            

        
        
if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    test.uploadNFSCatalog()
    os.chdir(current_dir)
    


    
        
        
        
