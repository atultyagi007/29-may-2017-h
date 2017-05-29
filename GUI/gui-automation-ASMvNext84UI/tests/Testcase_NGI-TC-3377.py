'''
Author: P.Suman
Created Date: Dec 23, 2015
Description: Pre-Requisite: Deploy service(s) with multiple storage vols, servers, cluster, VMs, and application on 
        ASM appliance. Verify that: 
        1. There is an export function/button which allows user to export the service data/information to a CSV file.
        2. click on the button to export the data and save it to our local drive. 
        3. Validate the CSV format file is generated and saved to a local folder. 
        4. Verify that the data in the CSV files match with the data displayed in the ASM UI as intended
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Export Data on Service Page
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
        #Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Clear Existing csv files
        self.clearExportedCSVFiles()
        #Create service if not exists
        self.createSampleTemplate(templateName="Test Template", publishedTemplate=True, volumeName="autoVolume")        
        #Deploys a Service if does not exist
        self.deploySampleService("Test Template", "Test Service", userRole="Administrator")
        
    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        #Clear Existing csv files
        self.clearExportedCSVFiles()

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")
        #Verify Export All Option is Enabled
        self.getServiceOptions(actualOptions=["Deploy New Service", "Export All"], 
                        enableOptions=["Export All"])
        #Export Data to csv
        self.exportDataToCSV(option="Services")
        #Verify Exported File 
        self.verifyExportedFile(option="asmServices")
        #Read content of csv file
        serviceInfo = self.getExportedData()        
        if len(serviceInfo) <= 0:
            self.failure("Exported CSV has no Service Information", raiseExc=True)
        else:
            self.succeed("Services Exported Data :: %s"%str(serviceInfo))
        #Get services
        serviceList = self.getServices()
        for service in serviceList:
            for key in service.keys():
                if key in ("Server", "Storage", "Cluster", "VM", "Application", "Reference Template"):
                    service.pop(key)
        self.succeed("Service Information from UI :: %s"%str(serviceList))
        #Compare Columns
        columnMismatch = set(serviceList[0].keys()).difference(set(serviceInfo[0].keys()))
        if len(columnMismatch) > 0:
            self.failure("Columns Mismatch from Exported CSV Service Information and UI :: %s"%str(columnMismatch), raiseExc=True)
        else:
            self.succeed("Columns Matched from Exported CSV and UI :: CSV '%s' UI '%s'"%(str(serviceInfo[0].keys()), str(serviceList[0].keys())))
        failList = []
        found = False
        #Compare Data
        for service in serviceList:
            found = False
            for servicecsv in serviceInfo:
                if service["Name"] == servicecsv["Name"]:
                    found = True
                    for key in service.keys():
                        if key in ("Server", "Storage", "Cluster", "VM", "Application", "Deployed On", "Reference Template"):
                            continue
                        elif key == "Status":
                            if servicecsv[key] == "COMPLETE" and service[key] not in ("Success", "Failed"):
                                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(servicecsv), str(service)))
                                break
                        else:
                            if service[key] != servicecsv[key]:
                                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(servicecsv), str(service)))
            if not found:
                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(servicecsv), str(service)))
        if len(failList) > 0:
            self.failure("Mismatch in some Services Information :: %s"%str(failList), raiseExc=True)
        else:
            self.succeed("Service Information Matched in CSV and UI")
                                
    @BaseClass.TestBase.func_exec
    def test_functionality(self):        
        """
        This is the execution starting function
        """
        self.exportedFile = None
        self.browserObject = globalVars.browserObject

        self.preRunSetup()
        
        self.runTestCase()
        
        self.postRunCleanup()
    