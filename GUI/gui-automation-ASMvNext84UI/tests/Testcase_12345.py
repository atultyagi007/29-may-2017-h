'''
Created on Jun 30, 2015

@author: P.Suman
'''

from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Testcase
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        @testcase: 12345
        Prerequisite : Discovery and Initial Setup
        """
        #self.setupCredentials()
        
        #self.setupNetworks()
        
        #self.discoverResources()
        
        self.getVcenterDetails("172.31.32.195")
        
