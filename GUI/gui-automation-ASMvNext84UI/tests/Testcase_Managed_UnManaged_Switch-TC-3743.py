'''
Description: Deploy ESXi EqualLogic Converged workflow with VDS Enabled for Chinese
'''
from globalImports import *


tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Deploy ESXi EqualLogic Converged workflow with VDS Enabled
    """
    
    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)  
        self.tempList=[]
        self.IoSwitchIp = []
        self.ServerPool=[]
    
    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario 
        """
        self.logDesc("Pre Run Setup")        
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)   
        #Read Components from JSON file     
        fileName = globalVars.jsonMap["esxieql"]  
        try:   
            self.jsonResult = self.getTemplateConfiguration(fileName) 
            self.components = self.jsonResult
            details = self.getResources(resourceType="Switches")
            # Will get all the available IO switch IP address from the resource table
            for eachDetails in details:
                a= eachDetails['Manufacturer /Model']
                if(eachDetails['Manufacturer /Model'].strip() == 'I/O-Aggregator'):
                    self.IoSwitchIp.append(eachDetails['IP Address'])
            self.createServerPool(self.components)
            self.ServerPool=self.components["Server"]["ServersIPForPool"]
            self.changeIOSwitchState(self.ServerPool,self.IoSwitchIp,resourceType="Dell Chassis",resourceState="Unmanaged")
        except Exception as e:
            print e
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)   
        #Delete existing Template with same name
        self.tempList = self.getTemplates(option="My Templates")
        self.templateName = self.components["Template"]["Name"]
        for template in self.tempList:
            if self.templateName == template["Name"]:
                self.deleteTemplate(self.templateName)        
    
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Create Template and Publish         
        self.buildTemplate(self.components, publishTemplate=True)
        #Deploy Service
        self.serviceName = "Service_" + self.templateName
        self.deployService(self.templateName, serviceName=self.serviceName)
        #Wait for Deployment to fail
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.failure("Successfully Deployed Service after changing the switch to Unmanaged for IOm switch",raiseExc=True)
            else:
                self.succeed("Failed to Deploy Service")
        else:
            self.failure("Failed to Deploy Service in expected time",raiseExc=True)   
#        

        fileName = globalVars.jsonMap["esxieql_managed"]  
        try:   
            self.jsonResult = self.getTemplateConfiguration(fileName) 
            self.components = self.jsonResult
            details = self.getResources(resourceType="Switches")
            # Will get all the available IO switch IP address from the resource table
            for eachDetails in details:
                a= eachDetails['Manufacturer /Model']
                if(eachDetails['Manufacturer /Model'].strip() == 'I/O-Aggregator'):
                    self.IoSwitchIp.append(eachDetails['IP Address'])
            self.createServerPool(self.components)
            self.ServerPool=self.components["Server"]["ServersIPForPool"]
            self.changeIOSwitchState(self.ServerPool,self.IoSwitchIp,resourceType="Dell Chassis",resourceState="Managed")
        except Exception as e:
            print e
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)   
        #Delete existing Template with same name
        self.tempList = self.getTemplates(option="My Templates")
        self.templateName = self.components["Template"]["Name"]
        for template in self.tempList:
            if self.templateName == template["Name"]:
                self.deleteTemplate(self.templateName)
        self.buildTemplate(self.components, publishTemplate=True)
        self.serviceName = "Service_Managed" + self.templateName
        self.deployService(self.templateName, serviceName=self.serviceName)  
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'"%self.serviceName)
            else:
                self.failure("Failed to Deploy Service '%s'"%self.serviceName, raiseExc=True)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time"%self.serviceName, raiseExc=True)   
    
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()