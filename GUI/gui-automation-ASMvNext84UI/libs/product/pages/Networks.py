"""
Author: P Suman/Saikumar Kalyankrishnan/HCL Team
Created/Modified: Oct 5th 2015/Feb 22nd 2017
Description: Functions/Operations related to Networks Page
"""

from CommonImports import *
from libs.product.objects.Common import Common
from libs.product.objects.Networks import Networks

class Networks(Navigation, Common, Networks):
    """
    Description:
        Class which includes all Functions/Operations related to Networks Management
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Networks class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Networks"
        utility.execLog("Networks")
    
    def loadPage(self):
        """
        Description:
            API to Load Networks Page
        """
        try:
            utility.execLog("Loading Networks Page...")
            self.browserObject, status, result = self.selectOption("Networks")
            if status:
                utility.execLog(result)
                self.browserObject, status, result = self.validatePageTitle()
                return self.browserObject, status, result
            else:
                return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Networks Page :: Error --> %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Networks Page
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NetworksObjects('title'))), action="CLICK")
            if not title:
                title = self.pageTitle
            getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NetworksObjects('title'))), action="GET_TEXT")
            if title not in getCurrentTitle:
                utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
                return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
            else:
                utility.execLog("Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title))
                return self.browserObject, True, "Successfully Loaded %s Page & Validated Page Title: '%s'" % (title, title)
        except Exception as e:
            return self.browserObject, False, "Exception Validating Page Title :: Exception --> %s" % (str(e) + format_exc())
    
    def getOptions(self):
        """
        Description:
            API to get Options and their Accessibility for Networks Page
        """
        optionList = {}
        try:
            utility.execLog("Reading Networks Table...")
            tableName = self.NetworksObjects('nwTable')
            networkSelected = False
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Networks defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows/Networks: %s" % str(countRows))
            if countRows > 0:
                # Selecting First Row to get Options and their Accessibility for Networks Page
                if countRows == 1:
                    getColumns = "//table[@id='%s']//tbody//tr//td" % tableName
                    countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
                    if countColumns == 1:
                        utility.execLog("There are no items available :: Zero Networks Defined")
                        networkSelected = False
                    else:
                        firstRow = "//table[@id='%s']/tbody/tr[1]" % tableName
                        self.handleEvent(EC.presence_of_element_located((By.XPATH, firstRow)), action="CLICK")
                        networkSelected = True
            # Possible Options Enabled based on No. of Networks
            if networkSelected:
                possibleOptions = [self.NetworksObjects('defineNW'), self.NetworksObjects('editNW'), self.NetworksObjects('deleteNW'), self.NetworksObjects('exportAll')]
            else:
                possibleOptions = [self.NetworksObjects('defineNW'), self.NetworksObjects('exportAll')]
            # Checking if Possible Options are Disabled/Enabled
            for option in possibleOptions:
                optionName = self.handleEvent(EC.presence_of_element_located((By.ID, option)), action="GET_TEXT")
                utility.execLog("Validating Option: '%s'" % optionName)
                disabled = self.handleEvent(EC.presence_of_element_located((By.ID, option)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                if "true" in disabled:
                    optionList[optionName] = "Disabled"
                else:
                    optionList[optionName] = "Enabled"
            if networkSelected:
                return self.browserObject, True, optionList
            else:
                return self.browserObject, False, "There should be at least one Network defined to verify 'Edit' and 'Delete' Options :: Available Options '%s'" % str(optionList)
        except Exception as e:
            return self.browserObject, False, "Exception while reading Options on Networks Page :: Error -> %s" % (str(e) + format_exc())
    
    def getDetails(self):
        """
        Description:
            API to get Existing Networks
        """
        networkList = []
        try:
            utility.execLog("Reading Networks Table...")
            tableName = self.NetworksObjects('nwTable')
            # Processing Columns
            getColumns = "//table[@id='%s']//thead//th" % tableName
            # Get No. of Columns i.e. No. of Parameters for a Network
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
            utility.execLog("Able to fetch '%s' Network Table Columns '%s'" % (tableName, str(tableColumns)))
            # Processing Rows
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Networks defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Rows/Networks: %s" % str(countRows))
            # To check for 0 Networks: 'There are no items available.'
            if countRows == 1:
                getColumns = "//table[@id='%s']//tbody//tr//td" % tableName
                countColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getColumns))))
                if countColumns == 1:
                    countRows = 0
                    utility.execLog("There are no items available :: Zero Networks Defined")
            # Parsing through every Column Per Row
            for row in range(1, countRows + 1):
                tableElements = []
                for col in range(1, countColumns + 1):
                    getDetail = "//table[@id='%s']/tbody/tr[%i]/td[%i]" % (tableName, row, col)
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="IS_DISPLAYED"):
                        parameterValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, getDetail)), action="GET_TEXT")
                        tableElements.append(parameterValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Successfully fetched Networks Info: '%s'" % str(tempDict))
                # Consolidating All Parameter Values Together
                networkList.append(tempDict)
                utility.execLog(networkList)
            return self.browserObject, True, networkList      
        except Exception as e:
            return self.browserObject, False, "Unable to read Networks :: Error -> %s" % (str(e) + format_exc())

    def defineNetwork(self, networkName, networkDescription, networkType, networkVLAN, configureStatic=False,
                      subnet="", gateway="", primaryDNS="", secondaryDNS="", dnsSuffix="",
                      startingIPAddress="", endingIPAddress="", editStatus=False):
        """
        Defines/Creates/Edits a Network
        """
        task = ""  # Define or Edit based on editStatus
        try:
            if editStatus:
                self.browserObject, status, result = self.selectNetwork(networkName)
                if not status:
                    return self.browserObject, False, result
                taskID = self.NetworksObjects('editNW')
                task = "Edit"
            else:
                taskID = self.NetworksObjects('defineNW')
                task = "Define"
            # Clicking on Define/Edit based on editStatus
            self.handleEvent(EC.element_to_be_clickable((By.ID, taskID)), action="CLICK")
            # Verifying whether Dialog PopUp is displayed and fetching the Title Header
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Define/Edit Form"
            # Verifying Dialog PopUp Title Header
            if editStatus:
                if "Edit Network" not in currentTitle:
                    utility.execLog("Failed to Verify %s Network Page :: Actual --> '%s' :: Expected --> '%s'" % (task, currentTitle, "Edit Network"))
                    return self.browserObject, False, "Failed to Verify %s Network Page :: Actual --> '%s' :: Expected --> '%s'" % (task, currentTitle, "Edit Network")
            else:
                if "Define Network" not in currentTitle:
                    utility.execLog("Failed to Verify %s Network Page :: Actual --> '%s' :: Expected --> '%s'" % (task, currentTitle, "Define Network"))
                    return self.browserObject, False, "Failed to Verify %s Network Page :: Actual --> '%s' :: Expected --> '%s'" % (task, currentTitle, "Define Network")
            utility.execLog("%s Network Page Loaded and Verified Successfully" % task)
            # Defining/Editing Networks based on editStatus
            # Setting Network Name
            utility.execLog("Setting Network Name: '%s'" % networkName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('networkName'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('networkName'))), action="SET_TEXT", setValue=networkName)
            # Setting Network Description
            utility.execLog("Setting Network Description: '%s'" % networkDescription)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('networkDescription'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('networkDescription'))), action="SET_TEXT", setValue=networkDescription)
            # Setting Network Type
            # Setting Network Type Possible only while Defining Network | Disabled while Editing Networks
            if not editStatus:
                utility.execLog("Setting Network Type: '%s'" % networkType)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('networkType'))), action="SELECT", setValue=networkType, selectBy="VISIBLE_TEXT")
            # Setting VLAN ID
            utility.execLog("Setting Network VLAN: '%s'" % networkVLAN)
            if "Hardware Management" not in networkType:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('vlanID'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('vlanID'))), action="SET_TEXT", setValue=networkVLAN)
            else:
                utility.execLog("Setting Network VLAN is not required for %s Network" % networkType)
            # Static IP Configuration
            if configureStatic:
                utility.execLog("Setting Static Network Configuration for %s '%s' to '%s'" % (networkType, networkName, configureStatic))
                # Static IP Configuration Mandatory for Hardware Management and Hypervisor Management
                if "Hardware Management" not in networkType and "Hypervisor Management" not in networkType:
                    disabled = self.handleEvent(EC.presence_of_element_located((By.ID, self.NetworksObjects('configStaticIP'))), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                    if "true" not in disabled:
                        if not self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('configStaticIP'))), action="IS_SELECTED"):
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('configStaticIP'))), action="CLICK")
                else:
                    utility.execLog("Configuring Static IP is Mandatory for '%s'" % networkType)
                utility.execLog("Setting Static Network Subnet: '%s'" % subnet)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('subnetMask'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('subnetMask'))), action="SET_TEXT", setValue=subnet)
                utility.execLog("Setting Static Network Gateway: '%s'" % gateway)
                if "Hardware Management" not in networkType:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('gateway'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('gateway'))), action="SET_TEXT", setValue=gateway)
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('gatewayHW'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('gatewayHW'))), action="SET_TEXT", setValue=gateway)
                utility.execLog("Setting Static Network Primary DNS: '%s'" % primaryDNS)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('primaryDNS'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('primaryDNS'))), action="SET_TEXT", setValue=primaryDNS)
                utility.execLog("Setting Static Network Secondary DNS: '%s'" % secondaryDNS)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('secondaryDNS'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('secondaryDNS'))), action="SET_TEXT", setValue=secondaryDNS)
                utility.execLog("Setting Static Network DNS Suffix '%s'" % dnsSuffix)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('DNSSuffix'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('DNSSuffix'))), action="SET_TEXT", setValue=dnsSuffix)
                # Configuring IP Ranges
                utility.execLog("Setting Starting and Ending IPAddress from '%s' to '%s'" % (startingIPAddress, endingIPAddress))
                if not editStatus:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('addIPRange'))), action="CLICK")
                utility.execLog("Setting Starting IP Address: '%s'" % startingIPAddress)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NetworksObjects('startIPRange'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NetworksObjects('startIPRange'))), action="SET_TEXT", setValue=startingIPAddress)
                utility.execLog("Setting Ending IP Address: '%s'" % endingIPAddress)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NetworksObjects('endIPRange'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.NetworksObjects('endIPRange'))), action="SET_TEXT", setValue=endingIPAddress)
            # Clicking on 'Save'
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('saveNW'))), action="CLICK")
            time.sleep(3)
            # Checking for Error Defining/Editing a Network
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('cancelNW'))), action="CLICK")
                    return self.browserObject, False, "Failed to %s Network :: '%s' with VLAN :: '%s' :: Error -> %s" % (task, networkName, networkVLAN, errorMessage)
            except:
                # Refresh Table
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('refreshTable'))), action="CLICK")
                time.sleep(3)
                # VALIDATION: Selecting newly created Network
                self.browserObject, status, result = self.selectNetwork(networkName)
                if status:
                    return self.browserObject, True, "%s Network Successful:: '%s' with VLAN :: '%s'" % (task, networkName, networkVLAN)
                else:
                    return self.browserObject, False, "Failed to %s Network :: '%s' with VLAN :: '%s' :: Error -> %s" % (task, networkName, networkVLAN, "Validation Error")
        except Exception as e:
            return self.browserObject, False, "Failed to %s Network :: '%s' with VLAN :: '%s' :: Error -> %s" % (task, networkName, networkVLAN, str(e) + format_exc())
    
    def selectNetwork(self, networkName):
        """
        Select specified Network using Network Name
        """
        try:
            utility.execLog("Reading Networks Table...")
            tableName = self.NetworksObjects('nwTable')
            getRows = "//table[@id='%s']//tbody//tr" % tableName
            # Get No. of Rows i.e. No. of Networks defined
            countRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, getRows))))
            utility.execLog("Total Number of Networks: %s" % str(countRows))
            # Using Name Column to Locate the Network; Column Value = 1
            for row in range(1, countRows + 1):
                getNWName = "//table[@id='%s']/tbody/tr[%i]/td[1]" % (tableName, row)
                nwName = self.handleEvent(EC.presence_of_element_located((By.XPATH, getNWName)), action="GET_TEXT")
                if networkName == nwName:
                    selectRow = "//table[@id='%s']/tbody/tr[%i]" % (tableName, row)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, selectRow)), action="CLICK")
                    utility.execLog("Able to Select Network: '%s'" % str(networkName))
                    return self.browserObject, True, "Able to Select Network: '%s'" % str(networkName)
            utility.execLog("Failed to Select Network: '%s'" % str(networkName))
            return self.browserObject, False, "Failed to Select Network: '%s'" % str(networkName)
        except Exception as e:
            return self.browserObject, False, "Exception generated while Selecting Networks :: Error -> %s" % (str(e) + format_exc())

    def deleteNetwork(self, networkName):
        """
        Deletes existing Network using Network Name
        """
        try:
            utility.execLog("Selecting the Network '%s' to be Deleted" % networkName)
            self.browserObject, status, result = self.selectNetwork(networkName)
            if not status:
                return self.browserObject, False, result
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('deleteNW'))), action="CLICK")
            utility.execLog("Checking for Confirm Box...")
            try:
                currentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('GetFormTitle'))), action="GET_TEXT")
            except:
                return self.browserObject, False, "Unable to Load Confirm Box To Delete Network"
            if "Confirm" in currentTitle:
                utility.execLog("Confirm Box Loaded...Confirming to Delete Network: '%s'" % networkName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            else:
                utility.execLog("Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm"))
                return self.browserObject, False, "Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (currentTitle, "Confirm")
            # Checking for Error Deleting a Network
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Delete Network :: '%s' :: Error -> %s" % (networkName, errorMessage)
            except:
                # Refresh Table
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('refreshTable'))), action="CLICK")
                time.sleep(3)
                # VALIDATION: Selecting deleted Network
                self.browserObject, status, result = self.selectNetwork(networkName)
                if status:
                    return self.browserObject, False, "Failed to Delete Network :: '%s' :: Error -> %s" % (networkName, "Validation Error")
                else:
                    return self.browserObject, True, "Successfully Deleted Network: '%s'" % networkName
        except Exception as e:
            return self.browserObject, False, "Exception while Deleting Network :: '%s' :: Error -> %s" % (networkName, str(e) + format_exc())
            
    def exportData(self):
        """
        Description:
            API to Export All Networks to CSV File
        """
        try:
            utility.execLog("Clicking on 'Export All'...")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.NetworksObjects('exportAll'))), action="CLICK")
            # Location: /downloads
            time.sleep(10)
            utility.execLog("Clicked on 'Export All'")
            try:
                errorRedBox = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))), wait_time=10)
                if errorRedBox:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                    return self.browserObject, False, "Failed to Export All Networks :: Error -> %s" % (str(errorMessage))
            except:
                return self.browserObject, True, "Successfully initiated Export All Networks to CSV /downloads"
        except Exception as e:
            return self.browserObject, False, "Exception while Exporting All Networks :: Error -> %s" % (str(e) + format_exc())

    def selectNetworkByType(self, serviceName, network, serverCount, vmCount, OSIPList, networkList):
        """
        Select Specified Network
        INCOMPLETE
        """
        error_found=[]
        sanFlag = 0
#         self.ipList[network] = []
        try:
            utility.execLog("Reading Networks Table")
            tableName = "networks_table"
            xpath = "//table[@id='%s']/tbody/tr"%tableName
            totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            for row in range(1, totalRows + 1):
                xpath = "//table[@id='%s']/tbody/tr[%i]/td[contains(@data-bind,'networkType')]"%(tableName, row)
                nwType = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")

                xpath = "//table[@id='%s']/tbody/tr[%i]/td/span[contains(@data-bind,'text: name')]"%(tableName, row)
                nwName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")

                if network == nwType or network == nwName:
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td/span[contains(@data-bind,'staticordhcp')]"%(tableName, row)
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") != "Static":
                        break

                    xpath = "//table[@id='%s']/tbody/tr[%i]"%(tableName, row)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLICK")
                    time.sleep(2)
                    utility.execLog("Able to Select Network '%s'"%str(network))
                    if (nwType == "SAN [iSCSI]"):
                        xCount, error = self.sanValidation(serviceName, network)
                        if error:
                            error_found.append(True)
                        else:
                            sanFlag = sanFlag + xCount

                        break
                    else:
                        if (self.serviceIPValidation(serviceName, nwType, serverCount, vmCount, OSIPList, networkList)):
                            error_found.append(True)
                        else:
                            error_found.append(False)

                        break

            if network == "SAN [iSCSI]" and self.staticIP:
                if sanFlag == serverCount * 2:
                    error_found.append(False)
                else:
                    error_found.append(True)
            else:
                error_found.append(False)

            utility.execLog(error_found)
            if True in error_found:
                return False
            else:
                return True
        except Exception as e:
            utility.execLog("Unable to read Networks :: Error -> %s"%(str(e) + format_exc()))
            return False
    
    def serviceIPValidation(self, serviceName, networkType, serverCount, vmCount, OSIPList, networkList):
        """        
        Description: Get/Verify IP Details for a Deployed Service
        INCOMPLETE
        """ 
        OSIP=[]
        error = False
#         mgmtFlag=0
#         wlFlag=0   #  Workload flag
        count=0
#         associatedNetwork=False
        try:
            utility.execLog("Reading the Static IP Table for %s" %networkType)
            tableName = "network_details_static_ip_table"
            xpath = "//table[@id='%s']/tbody/tr"%tableName
            totalRows = len(self.browserObject.find_elements_by_xpath(xpath)) 
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            for row in range(1, totalRows + 1):
                xpath = "//table[@id='%s']/tbody/tr[%i]/td[3]/a[contains(@data-bind,'serviceName')]"%(tableName, row)
                if (self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") == serviceName):
#                     associatedNetwork=True
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[1]/span"%(tableName, row)
                    if(self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") == "In Use"):
                        xpath="//table[@id='%s']/tbody/tr[%i]/td[2]/span"%(tableName, row)
                        try:
                            self.ipList[networkType].append(self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT"))
                        except:
                            self.ipList[networkType] = []
                            self.ipList[networkType].append(self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT"))
                        count += 1;
#                         if (networkType == "Hypervisor Management"):
#                             mgmtFlag=1
                        xpath = "//table[@id='%s']/tbody/tr[%i]/td[2]/span"%(tableName, row)
                        OSIP.append(self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT"))
#                         if (networkType == "Public LAN" or networkType == "Private LAN"):
#                             wlFlag=1
                    else:
                        error=True
                        utility.execLog("IP assigned to Service % s, but status showing as Available in %s" %(serviceName, networkType))
#             if associatedNetwork:
            if networkType == "Hypervisor Management" or networkType == "Hypervisor Migration" or networkType == "Hypervisor Cluster Private":           
                if(set(OSIPList) == set(OSIP) and count == serverCount):
                    utility.execLog("Successfully Validated IPs: %s" %networkType)
                else:
                    error = True
                    utility.execLog("Error in Validating IPs: %s" %networkType)
            elif networkType == "Public LAN" or networkType == "Private LAN":
                if "Hypervisor Management" in networkList:
                    """
                    We have VM's, so compare number of IP's found to vmCount
                    """    
                    if set(OSIPList) == set(OSIP) and count != vmCount:
                        error = True
                        utility.execLog("Error in Validating IPs: %s" %networkType)
                    else:
                        utility.execLog("Successfully Validated IPs: %s" %networkType)
                else:
                    """
                    We must be dealing with a baremetal deployment
                    """
                    if set(OSIPList) == set(OSIP) and count != serverCount:
                        error = True
                        utility.execLog("Error in Validating IPs: %s" %networkType)
                    else:
                        utility.execLog("Successfully Validated IPs: %s" %networkType)
            else:           
                if count == serverCount:
                    utility.execLog("Successfully Validated IPs: %s" %networkType)
                else:
                    error = True
                    utility.execLog("Error in Validating IPs: %s" %networkType)  
            return error
        except Exception as e:
            utility.execLog("Exception in validating Networks :: Error -> %s"%(str(e) + format_exc()))   
            return False          
        
    def sanValidation (self, serviceName, networkType):
        """        
        Description: Get/Verify SAN[iSCSI] Details for a Deployed Service
        INCOMPLETE
        """ 
        count=0
        error=False
        try:
            utility.execLog("Reading the Static IP Table for %s" %networkType)
            tableName = "network_details_static_ip_table"
            xpath = "//table[@id='%s']/tbody/tr"%tableName
            totalRows = len(self.browserObject.find_elements_by_xpath(xpath)) 
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            for row in range(1, totalRows + 1):
                xpath = "//table[@id='%s']/tbody/tr[%i]/td[3]/a"%(tableName, row)
                if (self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") == serviceName):
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[1]/span"%(tableName, row)
                    if(self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") == "In Use"):
                        xpath="//table[@id='%s']/tbody/tr[%i]/td[2]/span"%(tableName, row)
                        self.ipList[networkType].append(self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT"))
                        count=count+1;
                    else:
                        error=True
                        utility.execLog("IP assigned to Service % s, but status showing as Available in %s" %(serviceName, networkType))
            return count, error
        except Exception as e:
            utility.execLog("Exception in validating Networks :: Error -> %s"%(str(e) + format_exc()))   
            return count, False   
        
    def serviceNetworkIPValidation(self, serviceName, networkList, serverCount, vmCount, OSIPList):
        """        
        Description: Get/Verify Network Details for a Deployed Service
        """ 
        try:
            error=[]
            self.ipList = {}
            for network in networkList:
                if (network!="OS Installation"):
                    if (self.selectNetworkByType(serviceName, network, serverCount, vmCount, OSIPList, networkList)):
                        utility.execLog("Successfully validated %s Network/IPs for %s" % (network,serviceName))
                        error.append(False)
                    else:
                        utility.execLog("Error in validating %s Network/IPs for %s. Check logs/reports for specific errors" % (network,serviceName))
                        error.append(True)
            utility.execLog(error)
            if True in error:
                return self.browserObject, False, "Error in validating Networks/IPs for %s. Check logs/reports for specific errors." % serviceName
            else:
                return self.browserObject, True, self.ipList
        except Exception as e:
            return self.browserObject, False, "Exception in validating Networks :: Error -> %s" % (str(e) + format_exc())
    
    