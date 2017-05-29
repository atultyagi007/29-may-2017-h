'''
Created on Jul 25, 2014

@author: dheeraj_si

'''
import os
import sys
run_dir=os.path.abspath(os.path.dirname(__file__))
current_dir = os.getcwd()
os.chdir(run_dir)
sys.path.insert(0,os.path.abspath('../../initialsetup'))
sys.path.append(os.path.abspath('../../util'))
sys.path.append(os.path.abspath('../../testcases/firmwarerepository'))

from InitialBaseClass import InitialTestBase
import json
import time


class Testcase(InitialTestBase): 
    tc_Id = "" 
    
    def __init__(self):
        InitialTestBase.__init__(self)
        self.tc_Id = self.getTestCaseID(__file__)
    
    def test_login(self):
        response = self.authenticate()
        self.log_data( "Setting Login, Response : %s"%str(response))
        logger = self.getLoggerInstance()
        logger.info(' Login Response is ')
        logger.debug(response)
             

    def test_timezone(self):
        response = self.putTimeZone()
        self.log_data( "Setting TimeZone, Response : %s"%str(response))
        TimeZoneResponse  = json.loads(response)
        logger = self.getLoggerInstance()
        logger.info(' TimeZone Response is ')
        logger.debug(TimeZoneResponse)
    
    def get_timezone(self):
        logger = self.getLoggerInstance()
        resRes,state=self.getResponse("GET", "Timezone")
        print resRes
        if state:
            self.log_data( "Get TimeZone, Response : %s"%str(resRes))
        else:
            self.log_data("Get action of /TimeZoneis not working")  
        
        logger.info(' TimeZone Response is ')
        logger.debug(resRes)
     
    def test_NTP(self):
        response = self.putNTP()
        self.log_data( "Setting NTP, Response : %s"%str(response))
        logger = self.getLoggerInstance()
        logger.info(' NTP Response is ')
        logger.debug(response)
        
       
    def test_Proxy(self):
        response = self.putProxy()
        self.log_data( "Setting Proxy, Response : %s"%str(response))
        ProxyResponse  = json.loads(response)
        logger = self.getLoggerInstance()
        logger.info(' Proxy Response is ')
        logger.debug(ProxyResponse)
        
    def test_DHCP(self):
        response = self.putDHCP()
        self.log_data( "Setting DHCP, Response : %s"%str(response))
    
       
    def test_ProxyStatus(self):
        response = self.getProxyStatus()
        ProxyStatusResponse  = json.loads(response)
        logger = self.getLoggerInstance()
        logger.info(' ProxyStatusResponse is ')
        logger.debug(ProxyStatusResponse)
       
    def test_Wizard(self):
        statWiz = False
        putresponse = self.putWizard()
        self.log_data( "Setting Complete Wizard, Response : %s"%str(putresponse))
        response,status_code = self.getWizardStatus()
        PutWizardResponse  = json.loads(putresponse)
        logger = self.getLoggerInstance()
        logger.info(' Put Wizard response is ')
        logger.debug(PutWizardResponse)
        WizardResponse  = json.loads(response)
        logger.info(' Wizard Status is ')
        logger.debug(WizardResponse)
        if status_code in (200, 201, 202, 203, 204):
            statWiz = True
        return statWiz
 
    def test_ServerCred(self):
        response = self.postServerCred()
        ServerCredResponse  = json.loads(response)
        logger = self.getLoggerInstance()
        logger.info(' ServerCredResponse is ')
        logger.debug(ServerCredResponse)
        
        
    def disablefirewall(self):
        
        try:
            command1 = 'service iptables save'
            command2 = 'service iptables stop'
            command3 = 'chkconfig iptables off'
            self.executeSudoCommand(command1)
            self.executeSudoCommand(command2)
            self.executeSudoCommand(command3)
            
            time.sleep(30)
        except Exception as e1:
            self.log_data( 'Exception occurred while  executeSudoCommand to disable firewall')
            self.log_data(str(e1))
        
        
        
        
    def doInitialSetup(self):
        self.disablefirewall()
        self.test_login()
        self.test_timezone()
        self.test_NTP()
        self.test_Proxy()
        self.test_ProxyStatus()
        self.test_DHCP()
        self.test_Wizard()
        time.sleep(120)
        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"Initial Setup Successfully Completed"])
     
    
       

if __name__ == "__main__":
    test = Testcase()
#     test.disablefirewall()
#     test.test_login()
#     test.get_timezone() 
#       
#     test.test_timezone()
#     test.test_NTP()
    test.test_Proxy()
    test.test_ProxyStatus()
    test.test_DHCP()
    status =test.test_Wizard()
    time.sleep(120)
    test.log_TestData(["", "", "",str(test.tc_Id),'Success',"Initial Setup Successfully Completed"])
    if status==True:
        os.chdir(current_dir)
        sys.exit(0)
           
    else:
        os.chdir(current_dir)
        sys.exit(1)
