'''
Author: P.Suman
Created Date: Dec 24, 2015
Description: Pre-Requisite: Discover all resources on ASM appliance, including servers, switches, chassis, storage, 
        VM manager, EM Verify that:
        1. all the resources are discovered successfully.
        2. go to the page and view the details for the correct information.
        3. Ensure that there is an export function/button which allows user to export the resources data/information to a CSV file. 
        4. click on the button to export the data and save it to our local drive.
        5. Validate the CSV format file is generated and saved to a local folder.
        6. Verify that the data in the CSV files match with the data displayed in the ASM UI as intended
'''
from globalImports import *

tc_id=utility.get_tc_data(__file__)

class Testcase(Manager.Manager): 
    """
    Export Data on resource Page
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
        #Get Resources
        self.resList = self.getResources()
        if len(self.resList) == 0:
            self.discoverResources()
            #Get Resources
            self.resList = self.getResources()
            if len(self.resList) == 0:
                self.failure("There are no Resources to Export and Verify :: %s"%(str(self.resList)), 
                             resultCode=BaseClass.OMITTED, raiseExc=True)        
        
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
        self.verifyOptions(optionList=["Export All"], pageName="Resources", enableOptions=["Export All"])
        #Export Data to csv
        self.exportDataToCSV(option="Resources")
        #Verify Exported File 
        self.verifyExportedFile(option="devices")
        #Read content of csv file
        resourceInfo = self.getExportedData()
        if len(resourceInfo) <= 0:
            self.failure("Exported CSV has no Resource Information", raiseExc=True)
        else:
            self.succeed("Resources Exported Data :: %s"%str(resourceInfo))
        #Get Resources
        self.succeed("Resource Information from UI :: %s"%str(self.resList))
        for resource in self.resList:
            for key in resource.keys():
                if key == "Manufacturer/Model":
                    value = resource[key].split(" ",1)
                    if len(value) > 1:
                        manufacturer = value[0]
                        model = value[1]
                    elif len(value) == 1:
                        manufacturer = value[0]
                        model = ""
                    else:
                        manufacturer = ""
                        model = ""
                    resource.update({"Manufacturer":manufacturer, "Model":model})
                    resource.pop(key)
        self.succeed("Formatted Resource Information from UI :: %s"%str(self.resList))
        #Compare Columns
        columnMismatch = list(set(self.resList[0].keys()).difference(set(resourceInfo[0].keys())))
        if len(columnMismatch) > 0:
            if len(columnMismatch) == 1 and "OS Hostname" in columnMismatch:
                self.succeed("Columns Matched from Exported CSV and UI :: CSV '%s' UI '%s'"%(str(resourceInfo[0].keys()), str(self.resList[0].keys())))
            else:
                self.failure("Columns Mismatch from Exported CSV Resource Information and UI :: %s"%str(columnMismatch), raiseExc=True)
        else:
            self.succeed("Columns Matched from Exported CSV and UI :: CSV '%s' UI '%s'"%(str(resourceInfo[0].keys()), str(self.resList[0].keys())))
        failList = []
        found = False
        #Compare Data
        for resource in self.resList:
            found = False
            for resourcecsv in resourceInfo:
                if resource["IP Address"] == resourcecsv["IP Address"].split("/")[0]:
                    found = True
                    for key in resource.keys():
                        if key in ("OS Hostname", "IP Address"):
                            continue
                        elif key == "State":
                            if resourcecsv[key] == "DISCOVERED":
                                if resource[key] not in ("Available"):
                                    failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(resourcecsv), str(resource)))
                                    break
                            else:
                                if resourcecsv[key].upper().strip() != resource[key].upper().strip():
                                    failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(resourcecsv), str(resource)))
                                    break
                        elif key == "Firmware Status":
                            if resource[key].upper().strip().replace(" ","") != resourcecsv[key].upper().strip():
                                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(resourcecsv), str(resource)))
                                break
                        else:
                            if resource[key].strip() != resourcecsv[key].strip():
                                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(resourcecsv), str(resource)))
                                break
            if not found:
                failList.append("Mismatch in row CSV :: %s -> UI :: %s"%(str(resourcecsv), str(resource)))
        if len(failList) > 0:
            self.failure("Mismatch in '%s' Resources Information :: %s"%(len(failList), str(failList)), raiseExc=True)
        else:
            self.succeed("Resource Information Matched in CSV and UI")
                                
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
    