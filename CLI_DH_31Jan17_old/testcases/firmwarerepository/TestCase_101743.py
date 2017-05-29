'''
Created on Jan 22, 2015

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
import RepositoryParamFile

class Test_101743(DiscoverResourceTestBase):
    
    tc_Id=""
    
    def __init__(self):
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        
        
    def TestCase_101743(self):
        logger = self.getLoggerInstance()
        statR1 = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_1, name=RepositoryParamFile.OSRepname_1, sourcePath=RepositoryParamFile.OSRepname_1)
        if statR1 == True:
            statR2 = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_2, name=RepositoryParamFile.OSRepname_2, sourcePath=RepositoryParamFile.OSRepname_2)
        else:
            logger.info(" ==== unable to create Repository 1 ====")
            print " Unable to create  Repo 1 "
        if statR2 == True:
            statR3 = self.createOSRepository(imageType=RepositoryParamFile.OSRepimageType_2, name=RepositoryParamFile.OSRepname_2, sourcePath=RepositoryParamFile.OSRepname_2)
        else:
            logger.info(" ==== unable to create Repository 2 ====")
            print " unable to create  Repo 2 "
        if statR3 != True:
            logger.info(" ==== unable to create Repository 3 ====")
            print " Unable to create Repo 3 "
        

if __name__ == "__main__":
    test = Test_101743()
    test.TestCase_101743()
    os.chdir(current_dir)