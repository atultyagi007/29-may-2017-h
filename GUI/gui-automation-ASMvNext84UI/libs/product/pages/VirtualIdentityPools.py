"""
Author: P Suman/Raj Patel/Saikumar Kalyankrishnan
Created/Modified: Oct 7th 2015/Feb 25th 2016
Description: Functions/Operations related to Virtual Identity Pools Page
"""

from CommonImports import *
from libs.product.objects.Common import Common
from libs.product.objects.VirtualIdentityPools import VirtualIdentityPools

class VirtualIdentityPools(Navigation, Common, VirtualIdentityPools):
    """
    Description:
        Class which includes Functions/Operations related to Virtual Identity Pools
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Virtual Identity Pools class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Virtual Identity Pools"
        utility.execLog("Virtual Identity Pools")
    
    def loadPage(self):
        """
        Description:
            API to Load Virtual Identity Pools Page
        """
        try:
            utility.execLog("Loading Virtual Identity Pools Page...")
            self.browserObject, status, result = self.selectOption("Virtual Identity Pools")
            if status:
                utility.execLog(result)
                self.browserObject, status, result = self.validatePageTitle()
                return self.browserObject, status, result
            else:
                return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to Load Virtual Identity Pools Page :: Error --> %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Virtual Identity Pools Page
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('title'))), action="CLICK")
            if not title:
                title = self.pageTitle
            getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('title'))), action="GET_TEXT")
            if title not in getCurrentTitle:
                utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
                return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
            else:
                utility.execLog("Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title))
                # Adding wait Time for Virtual Identity Table to Load Completely
                time.sleep(3)
                return self.browserObject, True, "Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title)
        except Exception as e:
            return self.browserObject, False, "Exception Validating Page Title :: Exception --> %s" % (str(e) + format_exc())
    
    def getOptions(self):
        """
        Description:
            API to get Options and their Accessibility for Virtual Identity Pools Page 
        """
        optionList = {}
        try:
            utility.execLog("Reading Virtual Identity Pools Table...")
            tableName = self.VirtualIdentityPoolsObjects('poolsTable')
            poolSelected = False
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Virtual Identity Pools defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows: %s" % str(countRows))
            if countRows > 0:
                # Selecting First Row to get Options and their Accessibility for Virtual Identity Pools Page
                firstRow = "//table[@id='%s']/tbody/tr[1]//input" % tableName
                self.handleEvent(EC.presence_of_element_located((By.XPATH, firstRow)), action="CLICK")
                poolSelected = True
            # Possible Options Enabled based on No. of Virtual Identity Pools
            if poolSelected:
                options = {"Create": self.VirtualIdentityPoolsObjects('createPool'),
                           "Export": self.VirtualIdentityPoolsObjects('exportPool'),
                           "Delete": self.VirtualIdentityPoolsObjects('deletePool'),
                           "Update MAC Pool": self.VirtualIdentityPoolsObjects('editMACPool'),
                           "Update IQN Pool": self.VirtualIdentityPoolsObjects('editIQNPool'),
                           "Update WWPN Pool": self.VirtualIdentityPoolsObjects('editWWPNPool'),
                           "Update WWNN Pool": self.VirtualIdentityPoolsObjects('editWWNNPool')
                           }
            else:
                options = {"Create": self.VirtualIdentityPoolsObjects('createPool'),
                           "Update MAC Pool": self.VirtualIdentityPoolsObjects('editMACPool'),
                           "Update IQN Pool": self.VirtualIdentityPoolsObjects('editIQNPool'),
                           "Update WWPN Pool": self.VirtualIdentityPoolsObjects('editWWPNPool'),
                           "Update WWNN Pool": self.VirtualIdentityPoolsObjects('editWWNNPool')
                           }
            # Checking if Possible Options are Disabled/Enabled
            for optName, optValue in options.items():
                utility.execLog("Validating Option: '%s'" % optName)
                disabled = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                if "true" in disabled:
                    optionList[optName] = "Disabled"
                else:
                    optionList[optName] = "Enabled"
            if poolSelected:
                return self.browserObject, True, optionList
            else:
                return self.browserObject, False, "There should be at least one Virtual Identity Pool defined to verify 'Export' and 'Delete' Options :: Available Options '%s'" % str(optionList)
        except Exception as e:
            return self.browserObject, False, "Exception while reading Options on Virtual Identity Pools Page :: Error -> %s" % (str(e) + format_exc())
    
    def getDetails(self):
        """
        Description:
            API to get Existing Virtual Identity Pools
        """
        poolList = []
        try:
            utility.execLog("Reading Virtual Identity Pools Table...")
            tableName = self.VirtualIdentityPoolsObjects('poolsTable')
            # Processing Columns
            getColumns = "//table[@id='%s']//thead//th" % tableName
            # Get No. of Columns i.e. No. of Parameters for a Virtual Identity Pool
            countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
            utility.execLog("Total Number of Columns: %s" % str(countColumns))
            tableColumns = []
            for col in range(2, countColumns + 1):
                getColumnHeader = "//table[@id='%s']//thead//th[%i]" % (tableName, col)
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="IS_DISPLAYED"):
                    columnName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getColumnHeader)), action="GET_TEXT")
                    tableColumns.append(columnName)
                    utility.execLog("Able to fetch Column Name: '%s'" % columnName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch '%s' Virtual Identity Pools Table Columns '%s'" % (tableName, str(tableColumns)))
            # Processing Rows
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Virtual Identity Pools defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows: %s" % str(countRows))
            for row in range(1, countRows + 1):
                tableElements = []
                for col in range(2, countColumns + 1):
                    getDetail = "//table[@id='%s']/tbody/tr[%i]/td[%i]" % (tableName, row, col)
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="IS_DISPLAYED"):
                        parameterValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="GET_TEXT")
                        tableElements.append(parameterValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Successfully fetched Virtual Identity Pools Info: '%s'" % str(tempDict))
                # Consolidating All Parameter Values Together
                poolList.append(tempDict)
            return self.browserObject, True, poolList
        except Exception as e:
            return self.browserObject, False, "Unable to read Virtual Identity Pools :: Error -> %s" % (str(e) + format_exc())

    def selectVirtualPool(self, virtualPoolName):
        """
        Select specified Virtual Identity Pool using Virtual Identity Pool Name
        """
        try:
            utility.execLog("Reading Virtual Identity Pools Table...")
            tableName = self.VirtualIdentityPoolsObjects('poolsTable')
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Virtual Identity Pools defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows: %s" % str(countRows))
            # Using Name Column to Locate the Virtual Identity Pool; Column Value = 2
            for row in range(1, countRows + 1):
                getPoolName = "//table[@id='%s']/tbody/tr[%i]/td[2]" % (tableName, row)
                poolName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getPoolName)), action="GET_TEXT")
                if virtualPoolName == poolName:
                    # Select 'Virtual Identity Pool'
                    selectRow = "//table[@id='%s']/tbody/tr[%i]//input" % (tableName, row)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, selectRow)), action="CLICK")
                    utility.execLog("Able to Select Virtual Identity Pool: '%s'" % str(virtualPoolName))
                    return self.browserObject, True, "Able to Select Virtual Identity Pool: '%s'" % str(virtualPoolName)
            utility.execLog("Failed to Select Virtual Identity Pool: '%s'" % str(virtualPoolName))
            return self.browserObject, False, "Failed to Select Virtual Identity Pool: '%s'" % str(virtualPoolName)
        except Exception as e:
            return self.browserObject, False, "Exception while Selecting Virtual Identity Pool :: Error -> %s" % (str(e) + format_exc())

    def getSummaryDetails(self, virtualPoolName='Global'):
        """
        Description:
            API to get Summary of Virtual Identity Pools
        """
        summaryResults = {}
        try:
            utility.execLog("Selecting the Virtual Identity Pool '%s'" % virtualPoolName)
            self.browserObject, status, result = self.selectVirtualPool(virtualPoolName)
            if not status:
                return self.browserObject, False, result
            utility.execLog("Fetching Summary Details for Virtual Identity Pool '%s'" % virtualPoolName)
            summary = self.VirtualIdentityPoolsObjects('summarySection')
            getSections = "//*[@id='%s']//div[@class='row']/div" % summary
            countSections = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getSections))))
            for section in range(1, countSections + 1):
                getSection = getSections + "[%i]" % section
                getLegend = getSection + "//legend"
                legendName = self.handleEvent(EC.element_to_be_clickable((By.XPATH, getLegend)), action="GET_TEXT")
                utility.execLog("Legend: %s" % str(legendName))
                # Getting values for each Legend
                getFields = getSection + "//div[@class='form-group']"
                countFields = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getFields))))
                labels = []
                values = []
                for field in range(1, countFields + 1):
                    getField = getFields + "[%i]" % field
                    getLabel = getField + "//label"
                    labelName = self.handleEvent(EC.element_to_be_clickable((By.XPATH, getLabel)), action="GET_TEXT")
                    labels.append(labelName)
                    getValue = getField + "//p"
                    value = self.handleEvent(EC.element_to_be_clickable((By.XPATH, getValue)), action="GET_TEXT")
                    values.append(value)
                summaryDict = dict(zip(labels, values))
                utility.execLog(summaryDict)
                # Consolidating all information
                summaryResults[legendName] = summaryDict
            utility.execLog("%s's Summary: %s" % (virtualPoolName, summaryResults))
            return self.browserObject, True, summaryResults
        except Exception as e:
            return self.browserObject, False, "Exception generated while fetching Virtual Identity Pools Summary :: Error -> %s" % str(e)

    def createVirtualIdentityPool(self, virtualPoolName="TestPool", iqnPrefix="iqn.1988.com.dell"):
        """
        Creates a Basic Virtual Identity Pool
        Default: TestPool/iqn.1988.com.dell
        """
        try:
            utility.execLog("Creating Virtual Identity Pool...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualIdentityPoolsObjects('createPool'))), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Create Virtual Identity Pool Form"
            # Verifying Dialog PopUp Title Header
            if "Create Virtual Identity Pool" not in currentTitle:
                    utility.execLog("Failed to Verify Create Virtual Identity Pool Page :: Actual --> '%s' :: Expected --> '%s'") % (currentTitle, "Create Virtual Identity Pool")
                    return self.browserObject, False, "Failed to Verify Create Virtual Identity Pool Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Create Virtual Identity Pool")
            utility.execLog("Create Virtual Identity Pool Page Loaded and Verified Successfully")
            # Checking Page-1 Pool Information Loaded
            self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.VirtualIdentityPoolsObjects('pagePoolInfo'))))
            # Setting Virtual Pool Name
            utility.execLog("Step-1 Pool Information Loaded")
            utility.execLog("Setting Virtual Pool Name: '%s'" % virtualPoolName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualIdentityPoolsObjects('poolName'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualIdentityPoolsObjects('poolName'))), action="SET_TEXT", setValue=virtualPoolName)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('next'))), action="CLICK")
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('cancel'))), action="CLICK")
                    return self.browserObject, False, "Failed to Create Virtual Identity Pool Page '%s' :: Error --> '%s'" % (virtualPoolName, errorMessage)
            except:
                pass
            utility.execLog("Moving to Step-2...")
            # Checking Page-2 Virtual MAC Loaded
            self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.VirtualIdentityPoolsObjects('pageVirtualMAC'))))
            utility.execLog("Step-2 Virtual MAC Loaded")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('next'))), action="CLICK")
            utility.execLog("Moving to Step-3...")
            # Checking Page-3 Virtual IQN Loaded
            self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.VirtualIdentityPoolsObjects('pageVirtualIQN'))))
            utility.execLog("Step-3 Virtual IQN Loaded")
            utility.execLog("Setting IQN Prefix: '%s'" % iqnPrefix)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualIdentityPoolsObjects('iqnPrefix'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualIdentityPoolsObjects('iqnPrefix'))), action="SET_TEXT", setValue=iqnPrefix)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('next'))), action="CLICK")
            utility.execLog("Moving to Step-4...")
            # Checking Page-4 Virtual WWPN Loaded
            self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.VirtualIdentityPoolsObjects('pageVirtualWWPN'))))
            utility.execLog("Step-4 Virtual WWPN Loaded")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('next'))), action="CLICK")
            utility.execLog("Moving to Step-5...")
            # Checking Page-5 Virtual WWNN Loaded
            self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.VirtualIdentityPoolsObjects('pageVirtualWWNN'))))
            utility.execLog("Step-5 Virtual WWNN Loaded")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('next'))), action="CLICK")
            utility.execLog("Moving to Step-6...")
            # Checking Page-6 Summary Loaded
            self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.VirtualIdentityPoolsObjects('pageSummary'))))
            utility.execLog("Step-5 Summary Loaded")
            utility.execLog("Clicking on 'Finish'...")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.VirtualIdentityPoolsObjects('finish'))), action="CLICK")
            time.sleep(2)
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header/Text
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.VirtualIdentityPoolsObjects('submitConfirm'))))
                utility.execLog("Confirm Create Virtual Identity Pool Page Loaded and Verified Successfully. Clicking 'Yes'...")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            except:
                return self.browserObject, False, "Unable to Load Confirm Create Virtual Identity Pool Form"

            # Verifying Dialog PopUp Title Header
            # currentTitle = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.CommonObjects('GetFormTitle'))))
            # if "Confirm" not in currentTitle:
            #    utility.execLog("Failed to Verify Confirm Create Virtual Identity Pool Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm"))
            #    return self.browserObject, False, "Failed to Verify Confirm Create Virtual Identity Pool Page :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm")

            time.sleep(3)
            # Checking for Error Creating/Editing a Virtual Identity Pool
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Create Virtual Identity Pool Page '%s' :: Error --> '%s'" % (virtualPoolName, errorMessage)
            except:
                # VALIDATION: Selecting newly created Virtual Identity Pool
                self.browserObject, status, result = self.selectVirtualPool(virtualPoolName)
                if status:
                    return self.browserObject, True, "Created new Virtual Identity Pool '%s'" % str(virtualPoolName)
                else:
                    return self.browserObject, False, "Failed to Create Virtual Identity Pool Page '%s' :: Error --> '%s'" % (virtualPoolName, "Validation Error")
        except Exception as e:
            return self.browserObject, False, "Exception generated while creating Virtual Identity Pool %s :: Error -> %s" % (str(virtualPoolName), str(e))

    def deleteVirtualIdentityPool(self, virtualPoolName):
        """
        Deletes existing Virtual Identity Pool using Virtual Identity Pool Name
        """
        try:
            utility.execLog("Selecting the Virtual Identity Pool '%s' to be Deleted" % virtualPoolName)
            self.browserObject, status, result = self.selectVirtualPool(virtualPoolName)
            if not status:
                return self.browserObject, False, result
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualIdentityPoolsObjects('deletePool'))), action="CLICK")
            utility.execLog("Checking for Confirm Box...")
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Confirm Box To Delete Virtual Identity Pool"
            if "Confirm" in currentTitle:
                utility.execLog("Confirm Box Loaded...Confirming to Delete Virtual Identity Pool: '%s'" % virtualPoolName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            else:
                utility.execLog("Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'") % (currentTitle, "Confirm")
                return self.browserObject, False, "Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm")
            # Checking for Error Deleting a Virtual Identity Pool
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Delete Virtual Identity Pool :: '%s' :: Error -> %s" % (virtualPoolName, errorMessage)
            except:
                time.sleep(3)
                # VALIDATION: Selecting deleted Virtual Identity Pool
                self.browserObject, status, result = self.selectVirtualPool(virtualPoolName)
                if status:
                    return self.browserObject, False, "Failed to Delete Virtual Identity Pool :: '%s' :: Error -> %s" % (virtualPoolName, "Validation Error")
                else:
                    return self.browserObject, True, "Successfully Deleted Virtual Identity Pool: '%s'" % virtualPoolName
        except Exception as e:
            return self.browserObject, False, "Exception while Deleting Virtual Identity Pool :: '%s' :: Error -> %s" % (virtualPoolName, str(e) + format_exc())

    def verifyExportEnabled(self, virtualPoolName):
        """
        Description:
            API to verify if Export option is enabled for a selected Virtual Identity Pool
        """
        try:
            utility.execLog("Selecting the Virtual Identity Pool '%s'" % virtualPoolName)
            self.browserObject, status, result = self.selectVirtualPool(virtualPoolName)
            if not status:
                return self.browserObject, False, result
            utility.execLog("Validating 'Export' Option for '%s'" % virtualPoolName)
            disabled = self.handleEvent(EC.presence_of_element_located((By.ID, self.VirtualIdentityPoolsObjects('exportPool'))), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
            if "true" in disabled:
                return self.browserObject, True, "Export Button is Disabled for '%s'" % str(virtualPoolName)
            else:
                return self.browserObject, True, "Export Button is Enabled for '%s'" % str(virtualPoolName)
        except Exception as e:
            return self.browserObject, False, "Exception generated while verifying Export option for Virtual Identity Pool '%s'  :: Error -> %s" % (str(virtualPoolName), str(e))

    def exportData(self):
        """
        Description:
            API to Export All Identity Pools to Text File
        """
        try:
            utility.execLog("Clicking on 'Export All'...")
            # Select All Pools
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualIdentityPoolsObjects('selectAllPools'))), action="CLICK")
            # Click on 'Export'
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.VirtualIdentityPoolsObjects('exportPool'))), action="CLICK")
            utility.execLog("Checking for Confirm Box...")
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Confirm Box To Export All Virtual Identity Pool"
            if "Confirm" in currentTitle:
                utility.execLog("Confirm Box Loaded...Confirming to Export All Virtual Identity Pool")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            else:
                utility.execLog("Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'") % (currentTitle, "Confirm")
                return self.browserObject, False, "Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm")
            time.sleep(10)
            # Location: /downloads
            utility.execLog("Clicked on 'Export All'")
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Export All Identity Pools :: Error -> %s" % (str(errorMessage))
            except:
                return self.browserObject, True, "Successfully initiated Export All Identity Pools to Text File /downloads"
        except Exception as e:
            return self.browserObject, False, "Exception while Exporting All Identity Pools :: Error -> %s" % (str(e) + format_exc())