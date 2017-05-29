'''
Created on Feb 21, 2016

@author: Dheeraj.Singh
'''
from libs.product import BaseClass
from libs.product import utility
import time

tc_id=utility.get_tc_data(__file__)

class Testcase(BaseClass.TestBase): 
    """
    Discovery of different Resources like  Server, Chassis, Storage, Switch, VCenter, SCVMM, EnterpriseManager
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        BaseClass.TestBase.__init__(self, tc_id, args, **kwargs)
    
    
    def test_doDiscovery(self):
        
        finalStat = True
        creResponse,result = self.setupCredentials()             
        if result:
            self.succeed("Successfully Create Credentials or Manage existing Credentials %s"%creResponse)   
        else:
            self.failure("Failed to Create Credentials or Manage existing Credentials %s"%creResponse)
            
        resourcesInfo,status = self.getResourcesInfo()
        if not status:
            return resourcesInfo,False
        try:
            for resources in resourcesInfo:
                if resources["Type"] == "SERVER":
                    
                    respServer,resSer = self.discoverServer(resources)
                    
                    try:
                        self.getDiscoveryResourceStatus(respServer)
                        
                    except Exception as e1:
                        utility.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        utility.log_data(str(e1))
                        
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respServer["id"]) 
                    utility.log_data("Total Discovery Time of Servers in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of Servers in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    utility.log_data("Total Discovery Time of Servers in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of Servers in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    
                    if resSer:
                        self.succeed("Server Discovered Successfully %s"%respServer)   
                    else:
                        self.failure("Failed to Discover Server  %s"%respServer)
                    time.sleep(120)
                    
                elif resources["Type"] == "CHASSIS":
                    
                    respChassis,resCh = self.discoverChassis(resources)
                    
                    
                    try:
                        self.getDiscoveryResourceStatus(respChassis)
                        
                    except Exception as e1:
                        utility.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        utility.log_data(str(e1))
                        
                    
                    utility.log_data( 'Total time taken in discovery of  CHASSIS  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of CHASSIS IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respChassis["id"]) 
                    utility.log_data("Total Discovery Time of CHASSIS in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of CHASSIS in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    utility.log_data("Total Discovery Time of CHASSIS in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of CHASSIS in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    
                    if resCh:
                        self.succeed("CHASSIS Discovered Successfully %s"%respChassis)
                    else:
                        self.failure("Failed to Discover CHASSIS  %s"%respChassis) 
                    time.sleep(120)
                    
                elif resources["Type"] == "STORAGE":
                    
                    respStorage,resStrg = self.discoverStorage(resources)
                    
                    try:
                        self.getDiscoveryResourceStatus(respStorage)
                        
                    except Exception as e1:
                        utility.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        utility.log_data(str(e1))
                        
                    
                    
                    utility.log_data( 'Total time taken in discovery of  STORAGE  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of STORAGE IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respStorage["id"]) 
                    self.log_data("Total Discovery Time of STORAGE in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of STORAGE in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    utility.log_data("Total Discovery Time of STORAGE in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of STORAGE in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    if resStrg:
                        self.log_TestData(["", "", "",str(self.tc_Id),'Success',"STORAGE Discovered Successfully"])
                        self.succeed("STORAGE Discovered Successfully %s"%respStorage)
                    else:
                        self.failure("Failed to Discover STORAGE  %s"%respStorage)  
                    time.sleep(120)
                    
                elif resources["Type"] == "SWITCH":
                    
                    respSwitch,resSw = self.discoverSwitch(resources)
                    
                    try:
                        self.getDiscoveryResourceStatus(respSwitch)
                        
                    except Exception as e1:
                        utility.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        utility.log_data(str(e1))
                        
                    
                    
                    utility.log_data( 'Total time taken in discovery of  SWITCH  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of SWITCH IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respSwitch["id"]) 
                    utility.log_data("Total Discovery Time of SWITCH in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of SWITCH in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    utility.log_data("Total Discovery Time of SWITCH in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of SWITCH in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    
                    if resSw:
                        self.succeed("SWITCH Discovered Successfully %s"%respSwitch)
                    else:
                        self.failure("Failed to Discover SWITCH  %s"%respSwitch)  
                    time.sleep(120)
                    
                elif resources["Type"] == "VCENTER":
                    
                    respVC,resVC = self.discoverVCenter(resources)
                    
                    try:
                        self.getDiscoveryResourceStatus(respVC)
                        
                    except Exception as e1:
                        utility.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        utility.log_data(str(e1))
                        
                    
                    
                    utility.log_data( 'Total time taken in discovery of  VCENTER  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of VCENTER IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respVC["id"]) 
                    utility.log_data("Total Discovery Time of VCENTER in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of VCENTER in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    utility.log_data("Total Discovery Time of VCENTER in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of VCENTER in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    if resVC:
                        self.succeed("VCENTER Discovered Successfully %s"%respVC)
                    else:
                        self.failure("Failed to Discover VCENTER  %s"%respVC)  
                    time.sleep(120)
                    
                elif resources["Type"] == "SCVMM":
                    
                    respVM,resVM = self.discoverVM(resources)
                    
                    try:
                        self.getDiscoveryResourceStatus(respVM)
                        
                    except Exception as e1:
                        utility.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        utility.log_data(str(e1))
                        
                    
                    
                    utility.log_data( 'Total time taken in discovery of  SCVMM  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of SCVMM IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respVM["id"]) 
                    utility.log_data("Total Discovery Time of SCVMM in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of SCVMM in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    utility.log_data("Total Discovery Time of SCVMM in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of SCVMM in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    if resVM:
                        self.succeed("SCVMM Discovered Successfully %s"%respVM)
                    else:
                        self.failure("Failed to Discover SCVMM  %s"%respVM)   
                    time.sleep(120)
                    
                elif resources["Type"] == "EnterpriseManager":
                    
                    respVM,resVM = self.discoverEMC(resources)
                    
                    try:
                        self.getDiscoveryResourceStatus(respVM)
                        
                    except Exception as e1:
                        utility.log_data( 'Exception occurred while  getting DiscoveryResourceStatus ')
                        utility.log_data(str(e1))
                        
                    
                    
                    utility.log_data( 'Total time taken in discovery of  EnterpriseManager  Start IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"])
                    
                    print 'Total time taken in discovery of EnterpriseManager IP Address : %s'%resources["START_IP"]+'\t End IP Address : %s'%resources["END_IP"]
                    
                    elaspedTimeMillis=self.getJobExecutionStatusByJobID(respVM["id"]) 
                    utility.log_data("Total Discovery Time of EnterpriseManager in milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis + "\n")
                    print "Total Discovery Time of EnterpriseManager in  milliseconds  from  JRAF/jobhistory API : %s" % elaspedTimeMillis
                    utility.log_data("Total Discovery Time of EnterpriseManager in hours:minutes:seconds:milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n")
                    print "Total Discovery Time of EnterpriseManager in in Hours:Minutes:Seconds:Milliseconds from  JRAF/jobhistory API : %s" % self.convertMillistoHumanTime(elaspedTimeMillis) + "\n"
                    if resVM:
                        self.succeed("EnterpriseManager Discovered Successfully %s"%respVM)
                    else:
                        self.failure("Failed to Discover EnterpriseManager  %s"%respVM)   
                    time.sleep(120)
                else:
                    print " Invalid Resource Type "
        
        except Exception as e2:
            self.log_data( 'Exception occurred while  doDiscovery ')
            self.log_data(str(e2))
            
            finalStat=False
            
            
        
        return finalStat
        
                

    
    def runTestCase(self):
        """
        This is the execution starting function
        """
        
        #Login
        self.login()        
                    
        #Performing Do Discovery setup         
        statusDR = self.test_doDiscovery()
        
        if statusDR:
            self.succeed("Discovery of different Resources Step Successfully Completed ")
            
        else:
            self.failure("Failed to Discovery of different Resources ")
              
        time.sleep(120)
            
            
    
        
            
    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        Discovery of different Resources like  Server, Chassis, Storage, Switch, VCenter, SCVMM, EnterpriseManager        
        """        
        
        self.runTestCase()
        
        