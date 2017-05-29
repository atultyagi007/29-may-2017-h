'''
Created on Jan 22, 2017

@author: raj.patel
Description:
/OSRepository/{id}    get
/OSRepository/{id}    put
/OSRepository/sync/{id}   put

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
import TestCase_102623OSRepo
import RepositoryParamFile


'''
    This testcase handles put action of
/OSRepository/{id}    get
/OSRepository/{id}    put
/OSRepository/sync/{id}   put 
'''

class Testcase(DiscoverResourceTestBase):
    
    td_Id=""
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
                 
        
    def runTestcase(self):
        
        response = self.authenticate()
        logger = self.getLoggerInstance()
        logger.debug('Login Response is')
        logger.info(response)
        self.log_data("Running Test Case ::: ")
        
        status1 = self.createOSRepositoryAndConnection(imageType = RepositoryParamFile.OSRepimageType,name = RepositoryParamFile.OSReponametest1,sourcePath=RepositoryParamFile.OSRepsourcePath)
        if not status1:
            logger.info(" ==== Failed to Create OS Repository =====")
            print "Failed to create Repository"
           
        if status1:
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully test connection and created os repo','Success','Successfully test connection and  created os repo for post..'])
        else:
            print "###########################"
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
        
        
        status = self.createOSRepository(imageType = RepositoryParamFile.OSRepimageType,name = RepositoryParamFile.OSReponametest,sourcePath=RepositoryParamFile.OSRepsourcePath)
        if not status:
            logger.info(" ==== Failed to Create OS Repository =====")
            print "Failed to create Repository"
        if status:
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully created os repo','Success','Successfully created os repo for post..'])
        else:
            print "###########################"
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
        
         
        statusPut = self.putRepository()
        if statusPut:
            print "**********"
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully modified os repo','Success','Successfully modified os repo for put..'])
        else:
            print "###########################"
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
        
        statusPut = self.syncRepository()
        if statusPut:
            print "**********"
            self.log_TestData(["", "", "",str(self.tc_Id), 'Successfully synchronized os repo','Success','Successfully sync os repo for put..'])
        else:
            print "###########################"
            self.log_TestData(["", "", "",str(self.tc_Id), 'Test case failed', 'Failed','Test case failed'])
        
        
if __name__ == "__main__":
    test = Testcase()
    test.runTestcase()
    os.chdir(current_dir)
        
        
        
