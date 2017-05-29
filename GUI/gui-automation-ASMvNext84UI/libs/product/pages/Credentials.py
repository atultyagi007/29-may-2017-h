"""
Author: P Suman/Saikumar Kalyankrishnan/HCL Team
Created/Modified: Oct 5th 2015/Feb 22nd 2017
Description: Functions/Operations related to Credentials Page
"""

from CommonImports import *
from libs.product.objects.Common import Common
from libs.product.objects.Credentials import Credentials

class Credentials(Navigation, Common, Credentials):
    """
    Description:
        Class which includes all Functions/Operations related to Credentials Management
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Credentials class
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Credentials Management"
        utility.execLog("Credentials Management")
    
    def loadPage(self):
        """
        Description:
            API to load Credentials Management Page
        """
        try:
            utility.execLog("Loading Credential Management Page...")
            self.browserObject, status, result = self.selectOption("Credentials Management")
            if status:
                utility.execLog(result)
                self.browserObject, status, result = self.validatePageTitle()
                return self.browserObject, status, result
            else:
                return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Credential Management Page :: Error --> %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Credentials Management Page
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CredentialsObjects('title'))), action="CLICK")
            if not title:
                title = self.pageTitle
            getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CredentialsObjects('title'))), action="GET_TEXT")
            if title not in getCurrentTitle:
                utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
                return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
            else:
                utility.execLog("Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title))
                return self.browserObject, True, "Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title)
        except Exception as e:
            return self.browserObject, False, "Exception Validating Page Title :: Exception --> %s" % (str(e) + format_exc())
    
    def getOptions(self, credentialName=None):
        """
        Description:
            API to get Options and their Accessibility for Credential Management Page 
        """
        optionList = {}
        try:
            utility.execLog("Reading Credentials Table...")
            tableName = self.CredentialsObjects('credentialsTable')
            credentialSelected = False
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Credentials defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows: %s" % str(countRows))
            if countRows > 0:
                # If credentialName, select specified credentialName
                if credentialName:
                    utility.execLog("Selecting the Credential '%s'..." % credentialName)
                    self.browserObject, status, result = self.selectCredential(credentialName)
                    if status:
                        credentialSelected=True
                else:
                    # Else Selecting First Row to get Options and their Accessibility for Credential Page
                    # Default Credentials will have Edit & Delete Disabled
                    firstRow = "//table[@id='%s']/tbody/tr[1]" % tableName
                    self.handleEvent(EC.presence_of_element_located((By.XPATH, firstRow)), action="CLICK")
                    credentialSelected = True
            if credentialSelected:
                possibleOptions = [self.CredentialsObjects('createCredentials'), self.CredentialsObjects('editCredentials'), self.CredentialsObjects('deleteCredentials')]
                # Checking if Possible Options are Disabled/Enabled
                for option in possibleOptions:
                    optionName = self.handleEvent(EC.presence_of_element_located((By.ID, option)), action="GET_TEXT")
                    utility.execLog("Validating Option: '%s'" % optionName)
                    disabled = self.handleEvent(EC.presence_of_element_located((By.ID, option)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                    if "true" in disabled:
                        optionList[optionName] = "Disabled"
                    else:
                        optionList[optionName] = "Enabled"
                return self.browserObject, True, optionList
            else:
                return self.browserObject, False, "There should be at least one Credential selected in order to get Options and their Accessibility"
        except Exception as e:
            return self.browserObject, False, "Exception while reading Options on Credentials Page :: Error -> %s" % (str(e) + format_exc())
    
    def getDetails(self):
        """
        Description:
            API to get Existing Credentials
            Page-1 20 Credentials
        """
        credentialList = []
        try:
            utility.execLog("Reading Credentials Table...")
            tableName = self.CredentialsObjects('credentialsTable')
            # Processing Columns
            getColumns = "//table[@id='%s']//thead//th" % tableName
            # Get No. of Columns i.e. No. of Parameters for a Credential
            countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
            utility.execLog("Total Number of Columns: %s" % str(countColumns))
            tableColumns = []
            for col in range(1, countColumns + 1):
                getColumnHeader = "//table[@id='%s']//thead//th[%i]" % (tableName, col)
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="IS_DISPLAYED"):
                    columnName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="GET_TEXT")
                    tableColumns.append(columnName)
                    utility.execLog("Able to fetch Column Name: '%s'" % columnName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch '%s' Credentials Table Columns '%s'" % (tableName, str(tableColumns)))
            # Processing Rows
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Credentials defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows: %s" % str(countRows))
            # Parsing through every Column Per Row
            for row in range(1, countRows + 1):
                tableElements = []
                for col in range(1, countColumns + 1):
                    getDetail = "//table[@id='%s']/tbody/tr[%i]/td[%i]" % (tableName, row, col)
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="IS_DISPLAYED"):
                        parameterValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="GET_TEXT")
                        tableElements.append(parameterValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Successfully fetched Credentials Info: '%s'" % str(tempDict))
                # Consolidating All Parameter Values Together
                credentialList.append(tempDict)
            return self.browserObject, True, credentialList
        except Exception as e:
            return self.browserObject, False, "Unable to read Credentials :: Error -> %s" % str(e)
        
    def selectCredential(self, credentialName):
        """
        Select specified Credential using Credential Name
        """
        try:
            utility.execLog("Reading Credentials Table...")
            tableName = self.CredentialsObjects('credentialsTable')
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Credentials defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows: %s" % str(countRows))
            # Using Name Column to Locate the Credential; Column Value = 1
            for row in range(1, countRows + 1):
                getCredentialName = "//table[@id='%s']/tbody/tr[%i]/td[1]" % (tableName, row)
                currentCredentialName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getCredentialName)), action="GET_TEXT")
                if currentCredentialName == credentialName:
                    selectRow = "//table[@id='%s']/tbody/tr[%i]" % (tableName, row)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, selectRow)), action="CLICK")
                    utility.execLog("Able to Select Credential: '%s'" % str(credentialName))
                    time.sleep(2)
                    return self.browserObject, True, "Able to Select Credential: '%s'" % str(credentialName)
            utility.execLog("Failed to Select Credential: '%s'" % str(credentialName))
            return self.browserObject, False, "Failed to Select Credential: '%s'" % str(credentialName)
        except Exception as e:
            return self.browserObject, False, "Exception while Selecting Credentials :: Error -> %s" % str(e)
    
    def createCredential(self, credentialName, credentialType, credentialUsername, credentialPassword, credentialDomain="", credentialSNMPConfig="", editStatus=False):
        """
        Creates/Edits a Credential
        """
        task = ""  # Create or Edit based on editStatus
        try:
            if editStatus:
                self.browserObject, status, result = self.selectCredential(credentialName)
                if not status:
                    return self.browserObject, False, result
                taskID = self.CredentialsObjects('editCredentials')
                task = "Edit"
            else:
                taskID = self.CredentialsObjects('createCredentials')
                task = "Create"
            # Clicking on Create/Edit based on editStatus
            self.handleEvent(EC.element_to_be_clickable((By.ID, taskID)), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Define/Edit Form"
            # Verifying Dialog PopUp Title Header
            if editStatus:
                if "Edit Credentials" not in currentTitle:
                    utility.execLog("Failed to Verify %s Credentials Page :: Actual --> '%s' :: Expected --> '%s'" % (task, currentTitle, "Edit Credentials"))
                    return self.browserObject, False, "Failed to Verify %s Credentials Page :: Actual --> '%s' :: Expected --> '%s'" % (task, currentTitle, "Edit Credentials")
            else:
                if "Create Credentials" not in currentTitle:
                    utility.execLog("Failed to Verify %s Credentials Page :: Actual --> '%s' :: Expected --> '%s'" % (task, currentTitle, "Create Credentials"))
                    return self.browserObject, False, "Failed to Verify %s Credentials Page :: Actual --> '%s' :: Expected --> '%s'" % (task, currentTitle, "Create Credentials")
            utility.execLog("%s Credentials Page Loaded and Verified Successfully" % task)
            # Creating/Editing Credentials based on editStatus
            if not editStatus:            
                # Set Credential Type
                utility.execLog("Setting Credential Type: '%s'" % credentialType)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsType'))), action="SELECT", setValue=credentialType, selectBy="VISIBLE_TEXT")
            # Set Credential Name
            utility.execLog("Setting Credential Name: '%s'" % credentialName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsName'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsName'))), action="SET_TEXT", setValue=credentialName)
            # Set Username
            utility.execLog("Setting Username: '%s'" % credentialUsername)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsUsername'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsUsername'))), action="SET_TEXT", setValue=credentialUsername)
            #Set Password
            utility.execLog("Setting Password: '%s'" % credentialPassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsPassword'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsPassword'))), action="SET_TEXT", setValue=credentialPassword)
            # Set Confirm Password
            utility.execLog("Setting Confirm Password: '%s'" % credentialPassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsConfirmPW'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsConfirmPW'))), action="SET_TEXT", setValue=credentialPassword)
            # Setting Optional Fields
            if credentialType in ["SCVMM", "vCenter", "Element Manager"]:
                #Set Domain
                utility.execLog("Setting Domain: '%s'" % credentialDomain)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsDomain'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsDomain'))), action="SET_TEXT", setValue=credentialDomain)
            if credentialType in ["Switch", "Storage"]:
                # Set SNMP Configuration
                utility.execLog("Setting SNMP Configuration: '%s'" % credentialSNMPConfig)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsSNMP'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsSNMP'))), action="SET_TEXT", setValue=credentialSNMPConfig)
            # Clicking on 'Save'
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsSave'))), action="CLICK")
            time.sleep(3)
            # Checking for Error Creating/Editing a Credential
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsCancel'))), action="CLICK")
                    return self.browserObject, False, "Failed to %s Credential :: '%s' of Type :: '%s' :: Error -> '%s'" % (task, credentialName, credentialType, errorMessage)
            except:
                # Refresh Table
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsRefresh'))), action="CLICK")
                time.sleep(3)
                # VALIDATION: Selecting newly created Credential
                self.browserObject, status, result = self.selectCredential(credentialName)
                if status:
                    return self.browserObject, True, "%s Credential Successful :: '%s' of Type :: '%s'" % (task, credentialName, credentialType)
                else:
                    return self.browserObject, False, "Failed to %s Credential :: '%s' of Type :: '%s' :: Error -> '%s'" % (task, credentialName, credentialType, "Validation Error")
        except Exception as e:
                return self.browserObject, False, "Failed to %s Credential :: '%s' of Type :: '%s' :: Error -> '%s'" % (task, credentialName, credentialType, str(e))
    
    def deleteCredential(self, credentialName):
        """
        Deletes a Non-Default Existing Credential
        """
        try:
            utility.execLog("Deleting Credential: %s" % credentialName)
            self.browserObject, status, result = self.selectCredential(credentialName)
            if not status:
                return self.browserObject, False, result
            # Checking for Default Credentials - 'Delete' will be Disabled
            disabled = self.handleEvent(EC.presence_of_element_located((By.ID, self.CredentialsObjects('deleteCredentials'))), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
            if "true" in disabled:
                return self.browserObject, False, "Unable to Delete Default Credential: %s" % credentialName
            # Clicking on Delete
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('deleteCredentials'))), action="CLICK")
            utility.execLog("Checking for Confirm Box...")
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Confirm Box To Delete Credential"
            if "Confirm" in currentTitle:
                utility.execLog("Confirm Box Loaded...Confirming to Delete Credential: '%s'" % credentialName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            else:
                utility.execLog("Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm"))
                return self.browserObject, False, "Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm")
            # Checking for Error Deleting a Credential
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))),action="GET_TEXT")
                    return self.browserObject, False, "Failed to Delete Credential :: '%s' :: Error -> %s" % (credentialName, errorMessage)
            except:
                # Refresh Table
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CredentialsObjects('credentialsRefresh'))), action="CLICK")
                time.sleep(3)
                # VALIDATION: Selecting deleted Credential
                self.browserObject, status, result = self.selectCredential(credentialName)
                if status:
                    return self.browserObject, False, "Failed to Delete Credential :: '%s' :: Error -> %s" % (credentialName, "Validation Error")
                else:
                    return self.browserObject, True, "Successfully Deleted Credential: '%s'" % credentialName
        except Exception as e:
            return self.browserObject, False, "Exception while Deleting Credential :: '%s' :: Error -> %s" % (credentialName, str(e) + format_exc())