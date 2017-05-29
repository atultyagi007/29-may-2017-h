"""
Author: Raj Patel/Saikumar Kalyankrishnan
Created/Modified: Feb 15th 2016/Feb 28th 2017
Description: Functions/Operations related to Add-On Modules Page
"""

from CommonImports import *
from libs.product.objects.Common import Common
from libs.product.objects.AddOnModules import AddOnModules

class AddOnModules(Navigation, Common, AddOnModules):
    """
    Description:
        Class which includes all Functions/Operations related to Add-On Modules Page
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Add-On Modules class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Add-On Modules"
        utility.execLog("Add-On Modules")
        # FileName: Name
        self.addOnModuleList = {"SaySomething": "asm_test",
                                "SaySomething2": "asm_test",
                                "motd": "motd",
                                "asm_linux_postinstall": "asm_linux_postinstall",
                                "haproxy": "haproxy",
                                "dell-asm_test": "asm_test"
                                }
        self.selected = -1

    def loadPage(self):
        """
        Description:
            API to Load Add-On Modules Page
        """
        try:
            utility.execLog("Loading Add-On Modules Page...")
            self.browserObject, status, result = self.selectOption("Add-On Modules")
            if status:
                utility.execLog(result)
                self.browserObject, status, result = self.validatePageTitle()
                return self.browserObject, status, result
            else:
                return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Add-On Modules Page :: Error --> %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Add-On Modules Page
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.AddOnModulesObjects('title'))), action="CLICK")
            if not title:
                title = self.pageTitle
            getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.AddOnModulesObjects('title'))), action="GET_TEXT")
            if title not in getCurrentTitle:
                utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
                return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
            else:
                utility.execLog("Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title))
                return self.browserObject, True, "Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title)
        except Exception as e:
            return self.browserObject, False, "Exception Validating Page Title :: Exception --> %s" % (str(e) + format_exc())
    
    def getDetails(self):
        """
        Description:
            API to Get Existing Add-On Modules Details
        """
        addOnList = []
        try:
            utility.execLog("Reading Add-On Modules Table...")
            tableName = self.AddOnModulesObjects('addOnModuleTable')
            # Processing Columns
            getColumns = "//table[@id='%s']//thead//th" % tableName
            # Get No. of Columns i.e. No. of Parameters for a Add-On Module
            countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
            utility.execLog("Total Number of Columns: %s" % str(countColumns))
            tableColumns = []
            for col in range(1, countColumns + 1):
                getColumnHeader = "//table[@id='%s']//thead//th[%i]" % (tableName, col)
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="IS_DISPLAYED"):
                    columnName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="GET_TEXT")
                    # Not Adding 'Delete' to 'tableColumns'
                    if columnName:
                        tableColumns.append(columnName)
                        utility.execLog("Able to fetch Column Name: '%s'" % columnName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch '%s' Add-On Module Table Columns '%s'" % (tableName, str(tableColumns)))
            # Processing Rows
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Add-On Modules defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows/Add-On Modules: %s" % str(countRows))
            # To check for 0 Add-On Modules: 'No modules have been added.'
            if countRows == 1:
                getColumns = "//table[@id='%s']//tbody//tr//td" % tableName
                countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
                if countColumns == 1:
                    countRows = 0
                    utility.execLog("No modules have been added  :: Zero Add-On Modules Defined")
            # Parsing through every Column Per Row
            for row in range(1, countRows + 1):
                tableElements = []
                for col in range(1, countColumns + 1):
                    getDetail = "//table[@id='%s']/tbody/tr[%i]/td[%i]" % (tableName, row, col)
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="IS_DISPLAYED"):
                        parameterValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="GET_TEXT")
                        if parameterValue:
                            tableElements.append(parameterValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Successfully fetched Add-On Modules Info: '%s'" % str(tempDict))
                # Consolidating All Parameter Values Together
                addOnList.append(tempDict)
            return self.browserObject, True, addOnList
        except Exception as e:
            return self.browserObject, False, "Unable to read Add-On Modules :: Error -> %s" % (str(e) + format_exc())
    
    def addAddOnFile(self, addOnPath="docs\\SaySomething.zip"):
        """
        Description:
            API to Upload Add-On Module File
            Default: It will add 'Say Something' Add-On Modules included in the package
        """
        try:
            utility.execLog("Clicking on Add Add-On Module...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.AddOnModulesObjects('addModule'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Add Module Form"
            # Verifying Dialog PopUp Title Header
            if "Add Module" not in currentTitle:
                utility.execLog("Failed to Verify Add Module Page :: Actual --> '%s' Expected --> '%s'" % (currentTitle, "Add Module"))
                return self.browserObject, False, "Failed to Verify Add Module Page :: Actual --> '%s' Expected --> '%s'" % (currentTitle, "Add Module")
            utility.execLog("Add Module Page Loaded and Verified Successfully")
            # Selecting Add-On Module based on the Path | Default: 'Say_Something'
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.AddOnModulesObjects('addOnModulePath'))), action="CLEAR")
            filePath = os.path.abspath(addOnPath)
            time.sleep(3)
            # Extracting FileName from FilePath
            fileValue = filePath.split("\\")
            fileValue2 = fileValue[len(fileValue)-1]
            fileValue3 = fileValue2.split(".")
            fileNameText = fileValue3[0]
            utility.execLog("Uploaded Add-On Module File Name: '%s'" % str(fileNameText))
            # Formatting & Setting FilePath
            filePathMod = filePath.replace("\\", "\\\\")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.AddOnModulesObjects('addOnModulePath'))), action="SET_TEXT", setValue=filePathMod)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.AddOnModulesObjects('addOnModuleSave'))), action="CLICK")
            time.sleep(3)
            # Checking for Errors in Uploading an Add-On Module
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    utility.execLog("Error Message --> %s" % errorMessage)
                    if "Add-on module components already exist" in errorMessage:
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.AddOnModulesObjects('addOnModuleCancel'))), action="CLICK")
                        return self.browserObject, True, "Duplicate --> Add-On Module '%s' Already Uploaded" % fileNameText
                    else:
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.AddOnModulesObjects('addOnModuleCancel'))), action="CLICK")
                        return self.browserObject, False, "Failed to upload Add-On Module '%s' :: Error -> %s" % (fileNameText, errorMessage)
            except:
                time.sleep(3)
                # VALIDATION: Selecting newly uploaded Add-On Module
                getAddOnName = self.addOnModuleList[fileNameText]
                self.browserObject, status, result = self.selectAddOn(getAddOnName)
                if status:
                    return self.browserObject, True, "Successfully Uploaded Add-On Module '%s'" % fileNameText
                else:
                    return self.browserObject, False, "Failed to upload Add-On Module '%s' :: Error -> %s" % (fileNameText, "Validation Error")
        except Exception as e:
            return self.browserObject, False, "Exception generated while Uploading Add-On Module :: Error -> %s" % str(e)

    def selectAddOn(self, fileName):
        """
        Select specified Add-On Module using Name
        """
        try:
            utility.execLog("Reading Add-On Modules Table...")
            tableName = self.AddOnModulesObjects('addOnModuleTable')
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Add-On Modules defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Add-On Modules: %s" % str(countRows))
            # Using Name Column to Locate the Add-On Module; Column Value = 2
            for row in range(1, countRows + 1):
                getName = "//table[@id='%s']/tbody/tr[%i]/td[2]" % (tableName, row)
                AddOnName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getName)), action="GET_TEXT")
                if fileName == AddOnName:
                    selectRow = "//table[@id='%s']/tbody/tr[%i]" % (tableName, row)
                    self.selected = row
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, selectRow)), action="CLICK")
                    utility.execLog("Able to Select Add-On Module: '%s'" % str(fileName))
                    return self.browserObject, True, "Able to Select Add-On Module: '%s'" % str(fileName)
            utility.execLog("Failed to Select Add-On Module: '%s'" % str(fileName))
            return self.browserObject, False, "Failed to Select Add-On Module: '%s'" % str(fileName)
        except Exception as e:
            return self.browserObject, False, "Exception generated while Selecting Add-On Module :: Error -> %s" % (str(e) + format_exc())

    def deleteAddOnModule(self, fileName):
        """
        Description:
            API to Delete Add-On Module
        """
        try:
            if fileName in self.addOnModuleList.keys():
                fileName = self.addOnModuleList[fileName]
            utility.execLog("Selecting the Add-On Module '%s' to be Deleted" % fileName)
            tableName = self.AddOnModulesObjects('addOnModuleTable')
            self.browserObject, status, result = self.selectAddOn(fileName)
            if not status:
                return self.browserObject, False, result
            getModule = "//table[@id='%s']/tbody/tr[%i]/td[1]/button" % (tableName, self.selected)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, getModule)), action="CLICK")
            utility.execLog("Checking for Confirm Box...")
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Confirm Box To Delete Add-On Module"
            if "Confirm" in currentTitle:
                utility.execLog("Confirm Box Loaded...Confirming to Delete Add-On Module: '%s'" % fileName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            else:
                utility.execLog("Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm"))
                return self.browserObject, False, "Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm")
            # Checking for Error Deleting an Add-On Module
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Delete Add-On Module :: '%s' :: Error -> %s" % (fileName, errorMessage)
            except:
                time.sleep(3)
                # VALIDATION: Selecting deleted Add-On Module
                self.browserObject, status, result = self.selectAddOn(fileName)
                if status:
                    return self.browserObject, False, "Failed to Delete Add-On Module :: '%s' :: Error -> %s" % (fileName, "Validation Error")
                else:
                    return self.browserObject, True, "Successfully Deleted Add-On Module: '%s'" % fileName
        except Exception as e:
            return self.browserObject, False, "Exception while Deleting Add-On Module :: '%s' :: Error -> %s" % (fileName, str(e) + format_exc())

    def invalidAddOnFileName(self, fileName="docs\\adam_linux_postinstall_2.zip"):
        """
        Description:
            API to Upload Invalid Add-On Module File
        """
        try:
            self.browserObject, status, result = self.addAddOnFile(fileName)
            if not status:
                if "There is a problem with the add-on module upload file" in result or "Add-on module upload is missing the required file" in result:
                    return self.browserObject, True, "Successfully attempted to upload an Invalid Add-On Module and verified the Error Message --> %s" % result
                else:
                    return self.browserObject, False, "Failed to upload and verify an Invalid Add-On Module :: Error --> %s" % result
            else:
                return self.browserObject, False, "Uploaded an Invalid Add-On Module :: Error --> %s" % result
        except Exception as e:
            return self.browserObject, False, "Exception generated while uploading and verifying an Invalid Add-On Module :: Error --> %s" % str(e)

    def uploadAddOnFile(self):
        """
        Description:
            API to Upload and Delete Add-On Module
        """
        try:
            self.browserObject, status, result = self.addAddOnFile()
            if not status:
                return self.browserObject, False, "Failed to Upload and Delete an Add-On Module :: Error --> %s" % result
            time.sleep(2)
            self.browserObject, status, result = self.deleteAddOnModule('SaySomething')
            if not status:
                return self.browserObject, False, "Failed to Upload and Delete an Add-On Module :: Error --> %s" % result
            else:
                return self.browserObject, True, "Successfully Uploaded and Deleted an Add-On Module"
        except Exception as e:
            return self.browserObject, False, "Exception generated while Uploading and Deleting an Add-On Module :: Error --> %s" % str(e)

    def uploadDuplicateAddOnFile(self):
        """
        Description:
            API to Upload Duplicate Add-On Module
        """
        try:

            self.browserObject, status, result = self.addAddOnFile()
            if not status:
                return self.browserObject, False, "Failed to Upload Duplicate Add-On Module :: Error --> %s" % result
            if "Duplicate" in result:
                utility.execLog("Add-On Module Already Uploaded")
                return self.browserObject, True, "Successfully attempted to Upload Duplicate Add-On Module"
            else:
                utility.execLog("Uploading Duplicate Add-On Module...")
                self.browserObject, status, result = self.addAddOnFile()
                if not status:
                    return self.browserObject, False, "Failed to Upload Duplicate Add-On Module :: Error --> %s" % result
                if "Duplicate" in result:
                    return self.browserObject, True, "Successfully attempted to Upload Duplicate Add-On Module"
        except Exception as e:
            return self.browserObject, False, "Exception generated while Uploading Duplicate Add-On Module :: Error --> %s" % str(e)