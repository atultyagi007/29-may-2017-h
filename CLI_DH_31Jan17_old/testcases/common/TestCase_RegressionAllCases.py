'''
Created on Dec 16, 2014

@author: dheeraj.singh
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
sys.path.append(os.path.abspath('../../testcases/esxi'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))
sys.path.append(os.path.abspath('../../testcases/fcoe_flex_fc_mxl'))
sys.path.append(os.path.abspath('../../testcases/fcoe_flexioa'))
sys.path.append(os.path.abspath('../../testcases/fcoe_nobrocade'))
sys.path.append(os.path.abspath('../../testcases/fcoe_withbrocade'))
sys.path.append(os.path.abspath('../../testcases/fx2'))
sys.path.append(os.path.abspath('../../testcases/hyperV'))
sys.path.append(os.path.abspath('../../testcases/minimal'))
sys.path.append(os.path.abspath('../../testcases/multiservice'))
sys.path.append(os.path.abspath('../../testcases/raid'))
sys.path.append(os.path.abspath('../../testcases/baremetal'))


from TemplateBaseClass import TemplateTestBase
import json
import globalVars
import csv
import time
import xml.etree.cElementTree as ET
from DiscoverResourceBaseClass import DiscoverResourceTestBase
import ConfigureResources
import InitialSetup
import DefineNetwork
import DiscoveryResources
import tearDownTest
import TestCase_102623OSRepo
import TestCase_81190
import TestCase_81191
import TestCase_81192
import TestCase_81193
import TestCase_81194
import TestCase_81195
import TestCase_81196
import TestCase_81197
import TestCase_81198
import TestCase_81199
import TestCase_82125
import TestCase_82126
import TestCase_82127
import TestCase_82128
import TestCase_82129
import TestCase_82131
import TestCase_88167
import TestCase_88169
import TestCase_88170
import TestCase_102213
import TestCase_102214
import TestCase_102215
import TestCase_102216
import TestCase_102640
import FirmwareUpdate
import TestCase_104080
import TestCase_104081
import TestCase_104083
import TestCase_104087
import TestCase_104088
import TestCase_104089
import TestCase_104092

import TestCase_104061
import TestCase_104062
import TestCase_104064
import TestCase_104068
import TestCase_104069
import TestCase_104070
import TestCase_104073

import TestCase_102142
import TestCase_102144
import TestCase_102145
import TestCase_102147
import TestCase_102154
import TestCase_102156
import TestCase_102157
import TestCase_102159

import TestCase_102118
import TestCase_102120
import TestCase_102121
import TestCase_102123
import TestCase_102130
import TestCase_102132
import TestCase_102133
import TestCase_102135

import TestCase_110628
import TestCase_110629
import TestCase_110631
import TestCase_110632
import TestCase_110633
import TestCase_110634
import TestCase_110635
import TestCase_110636
import TestCase_110637

import TestCase_77215
import TestCase_77211
import TestCase_77218
import TestCase_77219
import TestCase_77452
import TestCase_78803
import TestCase_78804
import TestCase_78805
import TestCase_78806
import TestCase_78807
import TestCase_78808
import TestCase_78810
import TestCase_77216
import TestCase_78809
import TestCase_77217
import TestCase_78811
import TestCase_78913
import TestCase_78914
import TestCase_78915
import TestCase_78916
import TestCase_89862
import TestCase_89864
import TestCase_89917
import TestCase_107285
import TestCase_107286
import TestCase_107287
import TestCase_107442
import TestCase_107443
import TestCase_107444
import TestCase_107445
import TestCase_107446
import TestCase_107447
import TestCase_107448
import TestCase_107449
import TestCase_107450
import TestCase_109194
import TestCase_107315
import TestCase_107316
import TestCase_107317
import TestCase_107318
import TestCase_107319
import TestCase_107457
import TestCase_107458
import TestCase_107459
import TestCase_107460
import TestCase_107461
import TestCase_107462
import TestCase_112188
import TestCase_112189
import TestCase_112190
import TestCase_112191
import TestCase_112192
import TestCase_112193
import TestCase_112194
import TestCase_112195
import TestCase_112196
import TestCase_112197
import TestCase_112198
import TestCase_112199
import TestCase_112200
import TestCase_112201
import TestCase_112202
import TestCase_112203
import TestCase_112204
import TestCase_112205
import TestCase_112206
import TestCase_112207
import TestCase_112208
import TestCase_112209
import TestCase_112210
import TestCase_115719
import TestCase_115720
import TestCase_115721
import inputReqValueESXI
import TestCase_107262
import TestCase_107263
import TestCase_107264
import TestCase_107265
import TestCase_107266
import TestCase_107267




class Testcase(TemplateTestBase,DiscoverResourceTestBase):
    """
    Regression test case of the testcase id 81194, 81194, 81194, 81194, 81194, and 81194
    """ 
    
    tc_Id=""
    def __init__(self):
        TemplateTestBase.__init__(self)
        DiscoverResourceTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
        

       
       
        

if __name__ == "__main__":
    test = Testcase()
    test.getCSVHeader()
    globalVars.testCaseFlowName ='exsi'
#     test1 = tearDownTest.Testcase()
#     test1.doTearDown()
    test2 = InitialSetup.Testcase()
    test2.doInitialSetup()
    test3 = DefineNetwork.Testcase()
    test3.test_Network()
    test4 = DiscoveryResources.Testcase()
    test4.test_doDiscovery()
    test5 = ConfigureResources.Testcase()
    test5.test_configureResourec()
    test12 = TestCase_102623OSRepo.Test_102623osRepo()
#     test12.createOSRepositoryfromISOPath("windows2012", "win-2012",globalVars.osRepositoryPathR2)
#     time.sleep(30)
#     test12.createOSRepositoryfromISOPath("windows2012", "win-2012r2",globalVars.osRepositoryPathNonR2)
#     time.sleep(30)
#     test12.createOSRepositoryfromISOPath("redhat", "redhat6",globalVars.osRepositoryPathRedhat)
#     time.sleep(30)
    test12.createOSRepositoryfromISOPath("redhat7", "red7",globalVars.osRepositoryPath_Redhat7)
    time.sleep(30)
#     test15 = TestCase_102640.Test_102640()
#     response = test15.uploadNFSCatalog()
#     time.sleep(30)
#     if response:
#         test16 = FirmwareUpdate.FirmwareUpdate()
#         test16.updateFirmwares()

#################  ESXI TestCases ########################################
    globalVars.testCaseFlowName ='exsi'
    
    test6 = TestCase_81191.Testcase()
    test6.test_createTemplate()
    test7 = TestCase_81192.Testcase()
    test7.test_createTemplate()
    test8 = TestCase_81193.Testcase()
    test8.test_createTemplate()
    test9 = TestCase_81194.Testcase()
    test9.test_createTemplate()
    test10 = TestCase_81195.Testcase()
    test10.test_createTemplate()
    test11 = TestCase_81196.Testcase()
    test11.test_createTemplate()
    test17 = TestCase_81197.Testcase()
    test17.test_createTemplate()
    test18 = TestCase_81198.Testcase()
    test18.test_createTemplate()
    test19 = TestCase_81199.Testcase()
    test19.test_createTemplate()
    test20 = TestCase_82125.Testcase()
    test20.test_createTemplate()
    test21 = TestCase_82126.Testcase()
    test21.test_createTemplate()
    test22 = TestCase_82127.Testcase()
    test22.test_createTemplate()
    test23 = TestCase_82128.Testcase()
    test23.test_createTemplate()
    test24 = TestCase_82129.Testcase()
    test24.test_createTemplate()
    test25 = TestCase_82131.Testcase()
    test25.test_createTemplate()
    test26 = TestCase_88167.Testcase()
    test26.test_createTemplate()
    test27 = TestCase_88169.Testcase()
    test27.test_createTemplate()
    test28 = TestCase_88170.Testcase()
    test28.test_createTemplate()
    test29 = TestCase_102213.Testcase()
    test29.test_createTemplate()
    test30 = TestCase_102214.Testcase()
    test30.test_createTemplate()
    test31 = TestCase_102215.Testcase()
    test31.test_createTemplate()
    test32 = TestCase_102216.Testcase()
    test32.test_createTemplate()
    test33 = TestCase_81190.Testcase()
    test33.test_createTemplate()
    
    #################  fcoe_flex_fc_mxl TestCases ########################################
    globalVars.testCaseFlowName = "fcoe_flex_fc_mxl"
    #inputReqValueESXI.target_boot_device_value= 'SD'
    
    
    
    
    test34 = TestCase_104080.Testcase()
    test34.test_createTemplate()
    test35 = TestCase_104081.Testcase()
    test35.test_createTemplate()
    test36 = TestCase_104083.Testcase()
    test36.test_createTemplate()
    test37 = TestCase_104087.Testcase()
    test37.test_createTemplate()
    test38 = TestCase_104088.Testcase()
    test38.test_createTemplate()
    test39 = TestCase_104089.Testcase()
    test39.test_createTemplate()
    test40 = TestCase_104092.Testcase()
    test40.test_createTemplate()
    
    #################  fcoe_flexioa TestCases ########################################
    globalVars.testCaseFlowName = "fcoe_flexioa"
    
    test41 = TestCase_104061.Testcase()
    test41.test_createTemplate()
    test42 = TestCase_104062.Testcase()
    test42.test_createTemplate()
    test43 = TestCase_104064.Testcase()
    test43.test_createTemplate()
    test44 = TestCase_104068.Testcase()
    test44.test_createTemplate()
    test45 = TestCase_104069.Testcase()
    test45.test_createTemplate()
    test46 = TestCase_104070.Testcase()
    test46.test_createTemplate()
    test47 = TestCase_104073.Testcase()
    test47.test_createTemplate()
    
    #################  fcoe_nobrocade TestCases ########################################
    
    globalVars.testCaseFlowName = "fcoe_nobrocade"
    
    test48 = TestCase_102142.Testcase()
    test48.test_createTemplate()
    test49 = TestCase_102144.Testcase()
    test49.test_createTemplate()
    test50 = TestCase_102145.Testcase()
    test50.test_createTemplate()
    test51 = TestCase_102147.Testcase()
    test51.test_createTemplate()
    test52 = TestCase_102154.Testcase()
    test52.test_createTemplate()
    test53 = TestCase_102156.Testcase()
    test53.test_createTemplate()
    test54 = TestCase_102157.Testcase()
    test54.test_createTemplate()
    test55 = TestCase_102159.Testcase()
    test55.test_createTemplate()
    
    
    #################  fcoe_withbrocade TestCases ########################################
    
    globalVars.testCaseFlowName = "fcoe_withbrocade"
    
    test56 = TestCase_102118.Testcase()
    test56.test_createTemplate()
    test57 = TestCase_102120.Testcase()
    test57.test_createTemplate()
    test58 = TestCase_102121.Testcase()
    test58.test_createTemplate()
    test59 = TestCase_102123.Testcase()
    test59.test_createTemplate()
    test60 = TestCase_102130.Testcase()
    test60.test_createTemplate()
    test61 = TestCase_102132.Testcase()
    test61.test_createTemplate()
    test62 = TestCase_102133.Testcase()
    test62.test_createTemplate()
    test63 = TestCase_102135.Testcase()
    test63.test_createTemplate()
    
    #################  fx2 TestCases ########################################
    
    globalVars.testCaseFlowName = "fx2"
    
    test64 = TestCase_110628.Testcase()
    test64.test_createTemplate()
    test65 = TestCase_110629.Testcase()
    test65.test_createTemplate()
    test66 = TestCase_110631.Testcase()
    test66.test_createTemplate()
    test67 = TestCase_110632.Testcase()
    test67.test_createTemplate()
    test68 = TestCase_110633.Testcase()
    test68.test_createTemplate()
    test69 = TestCase_110634.Testcase()
    test69.test_createTemplate()
    test70 = TestCase_110635.Testcase()
    test70.test_createTemplate()
    test71 = TestCase_110636.Testcase()
    test71.test_createTemplate()
    test72 = TestCase_110637.Testcase()
    test72.test_createTemplate()
    
    
    #################  hyperV TestCases ########################################
    
    globalVars.testCaseFlowName = "hyperV"
    
    
    test73 = TestCase_77211.Testcase()
    test73.test_createTemplate()
    test74 = TestCase_77215.Testcase()
    test74.test_createTemplate()
    test75 = TestCase_77216.Testcase()
    test75.test_createTemplate()
    test76 = TestCase_77217.Testcase()
    test76.test_createTemplate()
    test77 = TestCase_77218.Testcase()
    test77.test_createTemplate()
    test78 = TestCase_77219.Testcase()
    test78.test_createTemplate()
    test79 = TestCase_77452.Testcase()
    test79.test_createTemplate()
    test80 = TestCase_78803.Testcase()
    test80.test_createTemplate()
    test81 = TestCase_78806.Testcase()
    test81.test_createTemplate()
    test82 = TestCase_78804.Testcase()
    test82.test_createTemplate()
    test83 = TestCase_78805.Testcase()
    test83.test_createTemplate()
    test84 = TestCase_78807.Testcase()
    test84.test_createTemplate()
    test85 = TestCase_78808.Testcase()
    test85.test_createTemplate()
    test86 = TestCase_78809.Testcase()
    test86.test_createTemplate()
    test87 = TestCase_78810.Testcase()
    test87.test_createTemplate()
    test88 = TestCase_78811.Testcase()
    test88.test_createTemplate()
    test89 = TestCase_78913.Testcase()
    test89.test_createTemplate()
    test90 = TestCase_78914.Testcase()
    test90.test_createTemplate()
    test91 = TestCase_78915.Testcase()
    test91.test_createTemplate()
    test92 = TestCase_78916.Testcase()
    test92.test_createTemplate()
    test93 = TestCase_89862.Testcase()
    test93.test_createTemplate()
    test94 = TestCase_89864.Testcase()
    test94.test_createTemplate()
    test95 = TestCase_89917.Testcase()
    test95.test_createTemplate()
    
    #################  minimal TestCases ########################################
    
    globalVars.testCaseFlowName = "minimal"
    inputReqValueESXI.target_boot_device_value= 'FC'
    test96 = TestCase_107285.Testcase()
    test96.test_createTemplate()
    
    test97 = TestCase_107286.Testcase()
    test97.test_createTemplate()
    
    test98 = TestCase_107287.Testcase()
    test98.test_createTemplate()
    test99 = TestCase_107442.Testcase()
    test99.test_createTemplate()
    test100 = TestCase_107443.Testcase()
    test100.test_createTemplate()
    test101 = TestCase_107444.Testcase()
    test101.test_createTemplate()
    test102 = TestCase_107445.Testcase()
    test102.test_createTemplate()
    test103 = TestCase_107446.Testcase()
    test103.test_createTemplate()
    test104 = TestCase_107447.Testcase()
    test104.test_createTemplate()
    test105 = TestCase_107448.Testcase()
    test105.test_createTemplate()
    test106 = TestCase_107449.Testcase()
    test106.test_createTemplate()
    test107 = TestCase_107450.Testcase()
    test107.test_createTemplate()
    test108 = TestCase_109194.Testcase()
    test108.test_createTemplate()
    
    #################  multiservice TestCases ########################################
    
    globalVars.testCaseFlowName = "multiservice"
    
    test109 = TestCase_109194.Testcase()
    test109.test_createTemplate()
    
    test110 = TestCase_109194.Testcase()
    test110.test_createTemplate()
    
    #################  Baremetal ########################################
    
    globalVars.testCaseFlowName = "Baremetal"
    inputReqValueESXI.target_boot_device_value= 'HD'
    
    test111 = TestCase_107262.Testcase()
    test111.test_createTemplate()
      
    test112 = TestCase_107263.Testcase()
    test112.test_createTemplate()
     
    test113 = TestCase_107264.Testcase()
    test113.test_createTemplate()
      
    test114 = TestCase_107265.Testcase()
    test114.test_createTemplate()
    
    inputReqValueESXI.target_boot_device_value= 'SD'
    
    test115 = TestCase_107266.Testcase()
    test115.test_createTemplate()
      
    test116 = TestCase_107267.Testcase()
    test116.test_createTemplate()

    
    os.chdir(current_dir)
    
