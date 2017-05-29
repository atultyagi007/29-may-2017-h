'''
Author: dheeraj.singh
Created Date: Jun 30, 2016
Description: Enable DHCP setting  without  Domain and  DNS server  and do a deployment.
Test Flow    : 1) Log into the ASM UI and navigate to the Virtual Appliance Management page, 
              2) Enable DHCP setting without  Domain and  DNS server
              3) Do a Deployment
              
'''
from tests.globalImports import *
from libs.product.pages import Resources, Dashboard
from libs.product import globalVars

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
     Enable DHCP setting without  Domain and  DNS server and do a deployment.

    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
       
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)
      
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")     
           
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        Cleans the data created by this script
        """
        
        self.logout()
        
    
    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        subnet= globalVars.DHCP_Subnet  
        netMask= globalVars.DHCP_Netmask 
        startIpAddres= globalVars.DHCP_StartingIpAddress 
        endIpAddress= globalVars.DHCP_StartingIpAddress 
        gateway= globalVars.DHCP_Gateway 
        dns= ''
        domain= '' 
        self.enableDisableDHCPSettingMgr(resourceAction='Enable', subnet=subnet, netMask=netMask, startIpAddres=startIpAddres, endIpAddress=endIpAddress, gateway=gateway, dns=dns, domain=domain)
        
        

        
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
         
        self.postRunCleanup()
