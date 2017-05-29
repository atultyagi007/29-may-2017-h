'''
Created on Nov 13, 2014

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
import globalVars
from encodings import raw_unicode_escape
import os
import time

class Test_102640(DiscoverResourceTestBase):
    
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
        
        print "====== RESPONSE2 : ===== "
        print response
        
        downloadStatusRepo=False
        
        if status:
            self.refId=response["id"]
            print "refID"
            print self.refId
            self.setDefaultCatalogTrue(self.refId)
            self.setDefaultCatalogFalse(self.refId)
            #self.setDefaultCatalogTrue(refId)
            
        if status == True:
            self.log_TestData(["", "", "", str(self.tc_Id),'Success','Repository Created Successfully'])
        else:
            self.log_TestData(["", "", "", str(self.tc_Id),'Failed','Failed to create Repository'])
            
        
        downloadStatusRepo = self.getAllNFSCatalog()
        
        return downloadStatusRepo
        
        
        
    def getAllNFSCatalog(self):
        
        while True:
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
                print " Downloading firmware packages and  FirmwareRepository download status is : %s"%str(dStatus)
                self.log_data( 'FirmwareRepository download status is  : %s'%str(dStatus))            

        
        
if __name__ == "__main__":
    test = Test_102640()
    test.getCSVHeader()
    test.uploadNFSCatalog()
#     test.getAllNFSCatalog()
    os.chdir(current_dir)
    


    
        
        
        
