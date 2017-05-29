"""
Author: P Suman/Saikumar Kalyankrishnan/HCL Team
Created/Modified: Nov 4th 2015/Mar 8th 2017
Description: Functions/Operations related to Services Page

Revision History:
--Date--      --Name--         --TestCase--         --Changes--
04Jan16     Ankit Manglic       TC-2496             Added new function getExportToFileButtonStatus()
05Jan16     Raj Patel           TC-2590             Added new function getServiceManagableByStandardUser()
13Jan16     Ankit Manglic       TC-2533             Added new function verifyServiceComponentIPaddressState()
18Jan16     Raj Patel           TC-2593             Added new function get_ServiceDetails_Firmware()
21Jan16     Ankit Manglic       N/A                 Updated xpath of table column at line 218 in function readServiceTable() for build 5541
20Jan16     Nidhi Aishwarya     TC-3276             Added new function Deploy_Service_with_FirmwareUpdate()
03Feb16     Raj Patel           TC-3332             Modified function verify_Services()
02Feb16     Raj Patel           TC-3913             Added new function verifyAllLink()
"""

from CommonImports import *
from xml.etree.ElementPath import xpath_tokenizer_re
from libs.product.pages import Templates, Resources
from libs.thirdparty.selenium.webdriver.common.action_chains import ActionChains
from datetime import datetime
from libs.product.objects.Common import Common
from libs.product.objects.Services import Services
from libs.product.pages.Portview import Portview

class Services(Navigation, Portview, Common, Services):
    """
    Description:
        Class which includes Functions/Operations related to Services Page
    """

    def __init__(self, browserObject):
        """
        Description:
            Initializing an object of Services class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Services"
        self.browserObject = browserObject
        utility.execLog("Services")
        # Class Variables
        self.serviceTableList = "serviceTable"
        self.healthIcons = "healthicons"
        self.healthServiceIcons = "healthservices"
        self.viewServiceList = "viewservicelist"
        self.viewServiceIcons = "viewserviceicons"
        self.loopCount = 5
        self.svcloopCount = 5
        self.serviceIndex = -1

    def loadPage(self):
        """
        Description:
            API to Load Services Page
        """
        try:
            utility.execLog("Loading Services Page...")
            self.browserObject, status, result = self.selectOption("Services")
            return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to Load Services Page %s" % (str(e) + format_exc())

    def validatePageTitle(self, title=None):
        """
        Description:
            API to validate Services Page
        """
        if not title:
            title = self.pageTitle
        getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ServicesObjects('title'))), action="GET_TEXT")
        if title not in getCurrentTitle:
            utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
            return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
        else:
            utility.execLog("Successfully validated Page Title: '%s'" % title)
            return self.browserObject, True, "Successfully validated Page Title: '%s'" % title
    
    def getOptions(self, serviceName=None, serviceDetailOptions=False):
        """
        Description:
            API to get Options and their Accessibility for Services Page 
        """
        optionList = {}
        try:
            time.sleep(10)
            utility.execLog("Reading Services Table")
            time.sleep(5)
            options = {"Deploy New Service":"deployLink", "Export All":"exportAllLink"}
            for optName, optValue in options.items():
                utility.execLog("Reading Option '%s'"%optName)
                result = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled") 
                if result == "true":
                    optionList[optName] = "Disabled"
                else:
                    optionList[optName] = "Enabled"
                time.sleep(1)
            if serviceName:
                self.browserObject, status, result = self.selectService(serviceName)
                if status:
                    self.browserObject, status, result = self.readServicePageOptions(serviceName)
                    if status:
                        optionList.update(result)
                    if serviceDetailOptions and optionList.has_key("View Details") and optionList["View Details"] == "Enabled":
                        self.browserObject, status, result = self.viewServiceDetails(serviceName)
                        if status:
                            time.sleep(10)
                            self.handleEvent(EC.presence_of_element_located((By.LINK_TEXT, "Edit")))
                            serviceDetailPage = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_service_detail")), action="GET_TEXT") 
                            if "%s Details"%serviceName not in serviceDetailPage:
                                utility.execLog("Failed to verify Service Detail Page :: Actual: %s, Expected: %s"%(serviceDetailPage, serviceName + " Details"))
                                return self.browserObject, True, optionList
                            utility.execLog("Moved to Service Detail Page and Verified Page Title :: Actual: %s, Expected: %s"%(serviceDetailPage, serviceName + " Details"))
                            opts = {"Edit":"//aside[@id='serviceActions']/h4[1]/a[@id='lnkEditService']", "Delete":"//aside[@id='serviceActions']/ul[3]/li/a[@id='lnkDeleteService']", "Retry":"//aside[@id='serviceActions']/ul[3]/li/a[@id='lnkRetryService']", 
                                    "Export to File":"//aside[@id='serviceActions']/ul[3]/li/button[@id='lnkExportService']", "View All Settings":"//aside[@id='serviceActions']/ul[3]/li/a[@id='lnkViewSettings']", 
                                    "Delete Resources":"//aside[@id='serviceActions']/ul[4]/li/a[@id='lnkDeleteResources']", "Add Resources":"//aside[@id='serviceActions']/ul[4]/li[@id='listItemAddActions']/div/button",
                                    "Generate Troubleshooting Bundle":"//aside[@id='serviceActions']/ul[3]/li/a[@id='generateTroubleshootLink']",
                                    "User Permissions":"//aside[@id='serviceActions']/ul[2]/li[9]/span"}
#                                     "User Permissions":"//aside[@id='serviceActions']/ul[2]/li/span[@id='userpermissions']"
                            for optName, optValue in opts.items():
                                if optName != "User Permissions":
                                    status = self.handleEvent(EC.presence_of_element_located((By.XPATH, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                                    if status == "true":
                                        optionList[optName] = "Disabled"
                                    else:
                                        optionList[optName] = "Enabled"
                                else:
                                    status = self.handleEvent(EC.presence_of_element_located((By.XPATH, optValue)), action="GET_TEXT") 
                                    optionList[optName] = status
                        else:
                            utility.execLog(result)                        
            return self.browserObject, True, optionList
        except Exception as e:
            return self.browserObject, False, "Unable to read All Options on Services Page, Able to read Options -> %s  :: Error -> %s"%(str(optionList), str(e) + format_exc())
    
    def getServices(self, serviceCategory, viewType):
        """
        Description:
            API to fetch Existing Services
        """
        serviceList = []
        try:
            utility.execLog("Click on View Type '%s'"%viewType)
            if viewType == "List":
                tableName = self.serviceTableList
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.viewServiceList)), action="CLICK")
            else:
                tableName = "Icons"
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.viewServiceIcons)), action="CLICK")
            utility.execLog("Reading %s Table"%tableName)
            svcList = self.readServiceTable(tableName, viewType)
            serviceList.extend(svcList)
            return self.browserObject, True, serviceList      
        except Exception as e:
            return self.browserObject, False, "Unable to read Services :: Error -> %s"%(str(e) + format_exc())
    
    def readServiceTable(self, tableName, viewType):
        svcList = []
        try:
            if self.loopCount > 0:
                if viewType == "Icons":
                    xpath = "//ul[@id='%s']/li"%(self.healthIcons)
                    serviceTypeCollection = len(self.browserObject.find_elements_by_xpath(xpath))
                    utility.execLog("Able to identify '%s' Table"%self.healthIcons)
                    for _ in range(0, serviceTypeCollection):
                        temp = {}                    
                        try:
                            xpath = "//ul[@id='%s']/li/span/strong"%(self.healthIcons)
                            name = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                            xpath = "//ul[@id='%s']/li/a"%(self.healthServiceIcons)                        
                            tempId = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName="id") 
                            count = 0
                            try:
                                count = int(name[name.index("(") + 1:name.rindex(")")])
                                name = name[:name.index("(")].strip() 
                            except:
                                count = 0
                            temp["Name"] = str(name)
                            temp["Id"] = str(tempId)  
                            temp["Count"] = count   
                            if count > 0:
                                services = self.readServices(name, str(tempId))
                            else:
                                services = []       
                            temp["ServiceInfo"] = services                
                            utility.execLog("Able to fetch Templates Info '%s'"%str(temp))
                            svcList.append(temp)
                        except TimeoutException as e:
                            continue
                else:
                    xpath = "//table[@id='%s']/thead/tr[1]/th"%tableName                
                    totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
                    utility.execLog("Total Number of Columns : %s"%str(totalColumns))
                    tableColumns = []
                    for col in range(1, totalColumns + 1):
                        xpath = "//table[@id='%s']/thead/tr[1]/th[%i]"%(tableName, col)
                        colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableColumns.append(colName)
                        utility.execLog("Able to fetch Column Name: '%s'"%colName)
                    tableColumns = [x for x in tableColumns if x !='']
                    utility.execLog("Able to fetch %s Table Columns '%s'"%(tableName, str(tableColumns)))
                    #xpath = "//table[@id='%s']/tbody/tr"%self.serviceTableList
                    xpath = "//table[@id='%s']/tbody/tr"%tableName                
                    totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
                    utility.execLog("Total Number of Rows : %s"%str(totalRows))
                    for row in range(1, totalRows+1):
                        tableElements = []
                        for col in range(1, totalColumns):
                            xpath = "//table[@id='%s']/tbody/tr[%i]/td[%i]"%(tableName, row, col)
                            if col == 1:
                                xpath = "//table[@id='%s']/tbody/tr[%i]/td[%i]/i/i"%(tableName, row, col)
                                colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName="class") 
                                utility.execLog("Service Status Class : '%s'"%colValue)
                                if "success" in colValue:
                                    tableElements.append("Success")
                                elif "fail" in colValue or "critical" in colValue:
                                    tableElements.append("Failed")
                                elif "info" in colValue:
                                    tableElements.append("Inprogress")
                                else:
                                    tableElements.append("Unknown")
                            else:
                                colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                                tableElements.append(colValue)
                        tempDict = dict(zip(tableColumns, tableElements))
                        xpath = "//table[@id='%s']/tbody/tr[%i]"%(tableName, row)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                        for loop in range(1,10):
                            if loop != 4:
                                xpathName = "//ul[@id='serviceSimpleDetails']/li[%i]/label"%(loop)
                                xpathValue = "//ul[@id='serviceSimpleDetails']/li[%i]/span"%(loop)
                                propName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpathName)), action="GET_TEXT") 
                                propValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpathValue)), action="GET_TEXT")
                                tempDict[propName] = propValue
                        utility.execLog("Able to fetch %s Info '%s'"%(tableName, str(tempDict)))
                        svcList.append(tempDict)
            else:
                utility.execLog("Maximum retries exceeded for reading Services Table :: Retries ('%s')"%str(self.loopCount))
                raise "Maximum retries exceeded for reading Services Table :: Retries ('%s')"%str(self.loopCount)
        except Exception as se:
            utility.execLog("Services Page reloaded '%s'"%(str(se) + format_exc()))
            self.loopCount = self.loopCount - 1
            return self.readServiceTable()
        except Exception as e:
            utility.execLog("Unable to read Services :: Error -> %s"%(str(e) + format_exc()))
            raise e
        finally:
            return svcList
    
    def readServices(self, svcCategoryName, svcCategoryId):
        svcList = []
        try:
            if self.svcloopCount > 0:
                utility.execLog("Selecting Service Type '%s'"%svcCategoryName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "ddlView")), action="SELECT", setValue=svcCategoryName)
                xpath = "//ul[@id='%s']/li"%(self.healthServiceIcons)
                serviceTypeCollection = len(self.browserObject.find_elements_by_xpath(xpath))
                utility.execLog("Able to identify '%s' Table"%self.healthServiceIcons)
                for _ in range(0, serviceTypeCollection):
                    temp = {}                    
                    try:
                        xpath = "//ul[@id='%s']/li/span/strong"%(self.healthServiceIcons)
                        name = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        xpath = "//ul[@id='%s']/li/a"%(self.healthServiceIcons)                        
                        tempId = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName="id")
                        temp["Name"] = str(name)
                        temp["Id"] = str(tempId)                            
                        utility.execLog("Able to fetch '%s' Service Info :: '%s'"%(svcCategoryName, str(temp)))
                        svcList.append(temp)
                    except TimeoutException as e:
                        continue
            else:
                utility.execLog("Maximum retries exceeded for reading '%s' Services Table :: Retries ('%s')"%(svcCategoryName, str(self.loopCount)))
                raise "Maximum retries exceeded for reading '%s' Services Table :: Retries ('%s')"%(svcCategoryName, str(self.loopCount))
        except Exception as se:
            utility.execLog("'%s' Services Page reloaded '%s'"%(svcCategoryName, str(se) + format_exc()))
            self.svcloopCount = self.svcloopCount - 1
            self.readServices()
        except Exception as e:
            utility.execLog("Unable to read '%s' Services :: Error -> %s"%(svcCategoryName, str(e) + format_exc()))
            raise e
        finally:
            #self.browserObject.find_element_by_class_name("breadcrumbs").find_element_by_tag_name("a").click()
            self.handleEvent(EC.element_to_be_clickable((By.ID, "ddlView")), action="SELECT", setValue="All")
            time.sleep(5)
            return svcList
    
    def getCategories(self):
        """
        Description:
            API to get existing Service Categories
        """
        scList = []
        try:
            utility.execLog("Reading Services Categories")     
            scList = self.handleEvent(EC.presence_of_element_located((By.ID, "ddlView")), action="GET_ELEMENTS_BY_TAG", setValue="option", returnContent="TEXT")       
            utility.execLog("Able to Read Categories on Services Page '%s'"%str(scList))
            return self.browserObject, True, scList            
        except Exception as e:
            return self.browserObject, False, "Unable to read Service Categories :: Error -> %s"%(str(e) + format_exc())
    
    def selectService(self, serviceName):
        """
        Description:
            Selects specified Service
        """
        try:
            utility.execLog("Click on View Type 'List'")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.viewServiceList)), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "ddlView")), action="SELECT", setValue="All")
            #Identify Total Rows
            xpath = "//table[@id='%s']/tbody/tr"%self.serviceTableList
            totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
            utility.execLog("Total Services Available : %s"%str(totalRows))
            selected = False
            for rowindex in xrange(1, totalRows+1):
                xpath = "//table[@id='%s']/tbody/tr[%i]/td[2]"%(self.serviceTableList, rowindex)
                curServiceName = self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="GET_TEXT")
                if curServiceName == serviceName:
                    xpath = "//table[@id='%s']/tbody/tr[%i]"%(self.serviceTableList, rowindex)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    self.serviceIndex = rowindex
                    selected = True
                    break
            if selected:
                utility.execLog("Able to Select Service '%s'"%(serviceName))                      
                return self.browserObject, True, "Able to Select Service '%s'"%(serviceName)
            else:
                utility.execLog("Failed to Select Service '%s'"%(serviceName))                      
                return self.browserObject, False, "Failed to Select Service '%s'"%(serviceName)
        except Exception as e:
            return self.browserObject, False, "Failed to select Service '%s' :: Error -> %s"%(serviceName, str(e) + format_exc())

    def viewDeploymentSettings(self, serviceName=None):
        """
        Description:
            API to get All Settings in a Template 
        """
        optionList = {}
        try:
            if serviceName:
                self.browserObject, status, result = self.viewServiceDetails(serviceName)
                if not status:
                    return self.browserObject, status, result
            time.sleep(10)
            utility.execLog("Clicking on 'View All Settings'")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "lnkViewSettings")), action="CLICK")
            utility.execLog("Verifying Service Deployment Settings Page")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "page_service_settings")), action="CLICK")
            utility.execLog("Verifying Service Deployment Settings Page")
            if "Service Deployment Settings" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify Service Deployment Settings Page Title :: Expected:'Service Deployment Settings' and Actual:%s"%self.browserObject.title
            utility.execLog("Verified Service Deployment Settings Page Title :: Expected:'Service Deployment Settings' and Actual:%s"%self.browserObject.title)
            utility.execLog("Fetching All Components") 
            templateSettings = self.handleEvent(EC.presence_of_element_located((By.ID, "TemplateSetings"))) 
            components = templateSettings.find_elements_by_xpath("./li")
            time.sleep(2)
            for component in components:
                tempInfo = {}                
                try:
                    componentName = str(component.find_element_by_tag_name("h3").text)
                except NoSuchElementException as e:
                    continue
                time.sleep(2)                
                utility.execLog("Reading Component '%s' Information"%componentName)
                baseComponents = component.find_elements_by_xpath(".//fieldset/div")                
                expandStatus = component.find_element_by_id("btncomponenttoggletemplate").get_attribute("class")
                time.sleep(2)
                if expandStatus == "collapsed":
                    utility.execLog("Component '%s' is collapsed so expanding"%componentName)
                    component.find_element_by_id("btncomponenttoggletemplate").find_element_by_class_name("icon-ic-expndr-chev-side1-thick-core").click()
                    time.sleep(5)
                    utility.execLog("Able to expand Component '%s'"%componentName)
                for basecomp in baseComponents:
                    chkSubComponents = basecomp.find_elements_by_tag_name("h4")
                    if len(chkSubComponents) <= 0:
                        continue
                    subComponentHeaders = basecomp.find_elements_by_class_name("templatebuilder-categoryheader")
                    time.sleep(2)
                    subComponentDetails = basecomp.find_elements_by_xpath("./ul")
                    time.sleep(2)
                    index = 0
                    for subComponentHeader in subComponentHeaders:
                        subComponentName = str(subComponentHeader.find_element_by_xpath(".//span/span").text)
                        time.sleep(2)
                        utility.execLog("Reading Sub Component '%s' Information in '%s'"%(subComponentName, componentName))                
                        expandStatus = subComponentHeader.find_element_by_xpath(".//span").get_attribute("class")
                        time.sleep(2)
                        if expandStatus == "collapsed":
                            utility.execLog("Sub Component '%s' is collapsed so expanding"%subComponentName)
                            subComponentHeader.find_element_by_xpath(".//span").find_element_by_class_name("icon-ic-expndr-chev-side1-thick-core").click()
                            time.sleep(5)
                            utility.execLog("Able to expand Sub Component '%s' in '%s'"%(subComponentName, componentName))
                        subComponentInfo = {}
                        if subComponentName == "Network Settings":
                            fabricConfiguration = ""
                            if subComponentDetails[index].find_element_by_id("servertype_blade").is_selected():
                                fabricConfiguration = "Blade"
                            else:
                                fabricConfiguration = "Rack"
                            subComponentInfo["Fabric Configuration"] = fabricConfiguration
                            fabricIndex = {}
                            fabricCount = 0
                            if subComponentDetails[index].find_element_by_id("chkEnableFabric_0").is_selected():
                                fabricIndex["Fabric A"] = fabricCount 
                                subComponentInfo["Enable Fabric A"] = "True"
                                fabricCount += 1
                            else:                                
                                subComponentInfo["Enable Fabric A"] = "False"
                            if subComponentDetails[index].find_element_by_id("chkEnableFabric_1").is_selected():
                                fabricIndex["Fabric B"] = fabricCount
                                subComponentInfo["Enable Fabric B"] = "True"
                                fabricCount += 1
                            else:
                                subComponentInfo["Enable Fabric B"] = "False"
                            if subComponentDetails[index].find_element_by_id("chkEnableFabric_2").is_selected():
                                fabricIndex["Fabric C"] = fabricCount
                                subComponentInfo["Enable Fabric C"] = "True"
                                fabricCount += 1
                            else:
                                subComponentInfo["Enable Fabric C"] = "False"
                            for key, value in fabricIndex.items():
                                fabric = {}
                                if subComponentDetails[index].find_element_by_id("chkRedundancy_%i"%value).is_selected():
                                    fabric["Redundancy"] = "True"
                                else:
                                    fabric["Redundancy"] = "False"
                                if subComponentDetails[index].find_element_by_id("chkPartitioned_%i"%value).is_selected():
                                    fabric["Partitioned"] = "True"
                                else:
                                    fabric["Partitioned"] = "False"
                                nicType = str(Select(subComponentDetails[index].find_element_by_id("ddlNicType_%i"%value)).first_selected_option.text)
                                fabric["NIC Type"] = nicType
                                fabric["Enabled"] = "True"
                                #Get Networks
                                ports = subComponentDetails[index].find_element_by_id("chkRedundancy_%i"%value).find_element_by_xpath("../../..").find_elements_by_tag_name("section")
                                portInfo = {}
                                for port in ports:                                    
                                    if port.text == "":
                                        break
                                    portName = str(port.text.splitlines()[0])
                                    partitions = port.find_elements_by_xpath(".//table/tbody/tr")
                                    partitionInfo = {}
                                    for partition in partitions:
                                        if partition.text == "":
                                            break
                                        partitionName = str(partition.text.splitlines()[0])
                                        partitionColumns = partition.find_elements_by_xpath(".//td")
                                        networks = partitionColumns[1].find_elements_by_xpath(".//ul/li")
                                        networkInfo = []
                                        for network in networks:                                            
                                            networkName = str(network.find_element_by_xpath(".//div/label").text)
                                            if network.find_element_by_xpath(".//div/label/input").is_selected():
                                                networkInfo.append(networkName)
                                        temp = {}
                                        temp["VLAN"] = networkInfo
                                        temp["Minimum Bandwidth"] = str(partitionColumns[2].find_element_by_tag_name("input").get_attribute("value"))
                                        temp["Maximum Bandwidth"] = str(partitionColumns[3].find_element_by_tag_name("input").get_attribute("value"))
                                        partitionInfo[partitionName] = temp
                                    portInfo[portName] = partitionInfo
                                fabric["Ports"] = portInfo
                                subComponentInfo[key] = fabric                       
                        else:
                            componentProperties = subComponentDetails[index].find_elements_by_tag_name("li")
                            utility.execLog("Reading Sub Component '%s' Properties"%subComponentName)
                            for compprop in componentProperties:
                                chkSubItems = compprop.find_elements_by_tag_name("div")
                                if len(chkSubItems) <= 0:
                                    continue  
                                key = str(compprop.find_element_by_xpath(".//div/label").text)
                                if "RAID" in key:
                                    value = str(Select(compprop.find_element_by_id("ddlBasicRaidLevel_1")).first_selected_option.text)
                                elif len(compprop.find_elements_by_xpath(".//div/p")) > 0:
                                    value = str(compprop.find_element_by_xpath(".//div/p").text)
                                else:
                                    value = ""
                                utility.execLog("Able to Read Sub Component '%s' Property '%s':'%s'"%(subComponentName, key, value))
                                subComponentInfo[key] = value
                        tempInfo[subComponentName] = subComponentInfo
                        index += 1
                optionList[componentName] = tempInfo
            return self.browserObject, True, optionList
        except Exception as e:
            return self.browserObject, False, "Unable to read Options on Service Deployment Settings Page :: Error -> %s"%(str(e) + format_exc())
        finally:
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, "cancel_form_settings")), action="CLICK")
            except:
                pass
    
    def deleteService(self, serviceName):
        """
        Deletes existing Service
        """
        try:
            self.browserObject, status, result = self.viewServiceDetails(serviceName)
            if not status:
                return self.browserObject, status, result
            time.sleep(3)
            pageTitle = self.handleEvent(EC.presence_of_element_located((By.ID, "page_service_detail")), action="GET_TEXT")
            if "%s Details"%serviceName not in pageTitle:
                utility.execLog("Failed to verify Service Detail Page :: Actual: %s, Expected: %s"%(pageTitle, serviceName + " Details"))
                return self.browserObject, False, "Failed to verify Service Detail Page :: Actual: %s, Expected: %s"%(pageTitle, serviceName + " Details")
            utility.execLog("Moved to Service Detail Page and Verified Page Title :: Actual: %s, Expected: %s"%(pageTitle, serviceName + " Details"))
            loopCount = 3
            while loopCount:
                try:
                    utility.execLog("Clicking on Delete Service Option")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "lnkDeleteService")), action="CLICK")                    
                    utility.execLog("Checking for Delete Service Page")
                    time.sleep(3)
                    pageTitle = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_delete_service")), action="GET_TEXT")
                    if "Delete" not in pageTitle:
                        utility.execLog("Failed to verify Delete Service Page :: Actual: %s, Expected: %s"%(pageTitle, "Delete"))
                        return self.browserObject, False, "Failed to verify Delete Service Page :: Actual: %s, Expected: %s"%(pageTitle, "Delete")
                    utility.execLog("Verified Delete Service Page :: Actual: %s, Expected: %s"%(pageTitle, "Delete"))            
                    utility.execLog("Clicking on 'Delete' on Delete Service Page")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_deleteservice")), action="CLICK")
                    utility.execLog("Identifying Confirm Dialog box")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "confirm_modal_form")), action="CLICK")
                    utility.execLog("Confirming to Delete Service '%s'"%serviceName)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                    break
                except Exception as se:
                    utility.execLog("Services Page reloaded '%s'"%str(se))
                    loopCount = loopCount - 1
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False)
                errorMessage = eleError.find_element_by_tag_name("h3").text
                time.sleep(2)
                return self.browserObject, False, "Failed to Initiate Service Teardown :: '%s' :: Error -> '%s'"%(serviceName, 
                                                    str(errorMessage))
            except:
                return self.browserObject, True, "Successfully Initiated Service Teardown '%s'"%serviceName           
        except Exception as e:
            return self.browserObject, False, "Failed to Initiate Service Teardown :: '%s' :: Error -> %s"%(serviceName, 
                                                   str(e) + format_exc())
    
    def editService(self, serviceName, managePermissions=True, userList=["All"], manageFirmware=False,  
                       deleteUsers=False, usersOnly=False, firmwareName=None):
        """
        Edit existing Service
        """
        try:
            self.browserObject, status, result = self.viewServiceDetails(serviceName)
            if not status:
                return self.browserObject, status, result
            serviceDetailPage = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_service_detail")), action="GET_TEXT") 
            if "%s Details"%serviceName not in serviceDetailPage:
                utility.execLog("Failed to verify Service Detail Page :: Actual: %s, Expected: %s"%(serviceDetailPage, serviceName + " Details"))
                return self.browserObject, False, "Failed to verify Service Detail Page :: Actual: %s, Expected: %s"%(serviceDetailPage, serviceName + " Details")
            utility.execLog("Moved to Service Detail Page and Verified Page Title :: Actual: %s, Expected: %s"%(serviceDetailPage, serviceName + " Details"))
            utility.execLog("Clicking on Edit Service Option")
            xpath = "//aside[@id='serviceActions']/h4[1]/a[@id='lnkEditService']"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
            editServicePage = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_edit_service")), action="GET_TEXT") 
            if "Edit Service Information" not in editServicePage:
                utility.execLog("Failed to verify Edit Service Page :: Actual: %s, Expected: %s"%(editServicePage, "Edit Service Information"))
                return self.browserObject, False, "Failed to verify Edit Service Page :: Actual: %s, Expected: %s"%(editServicePage, "Edit Service Information")
            utility.execLog("Moved to Edit Service Page and Verified Page Title :: Actual: %s, Expected: %s"%(editServicePage, "Edit Service Information"))
            if usersOnly:
                userList = []           
                utility.execLog("Reading Existing Users")
                xpath = "//div[@class='checkboxlist']/div"
                usersAdded = len(self.browserObject.find_elements_by_xpath(xpath)) + 1
                for loop in range(1, usersAdded):
                    xpath = "//div[@class='checkboxlist']/div[%i]/span"%loop
                    userList.append(self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT"))
                utility.execLog("Able to Read Existing Users for Service '%s'"%str(userList))
                return self.browserObject, True, userList
            #Manage Permissions
            if managePermissions:
                time.sleep(10)
                utility.execLog("Select 'Manage Permissions' Option")
                if not self.handleEvent(EC.element_to_be_clickable((By.ID, "managePermissions")), action="IS_SELECTED"):
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "managePermissions")), action="CLICK")
                    time.sleep(5)
                    if not self.handleEvent(EC.element_to_be_clickable((By.ID, "managePermissions")), action="IS_SELECTED"):
                        return self.browserObject, False, "Failed to Select 'Manage Permissions' option on 'Edit Service' Page"
                    utility.execLog("Selected 'Manage Permissions' Option")
                else:
                    utility.execLog("'Manage Permissions' Option is already Selected")
                deletedUsers = []
                if deleteUsers:
                    utility.execLog("Reading Existing Users")
                    xpath = "//div[@class='checkboxlist']/div"
                    usersAdded = len(self.browserObject.find_elements_by_xpath(xpath)) + 1
                    time.sleep(5)
                    for loop in range(1, usersAdded):
                        xpath = "//div[@class='checkboxlist']/div[%i]/span"%loop
                        for ruser in userList:
                            if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") == ruser:
                                xpath = "//div[@class='checkboxlist']/div[%i]/input"%loop
                                time.sleep(5)
                                self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLICK")
                                time.sleep(7)
                                utility.execLog("Selected User '%s'"%ruser)
                                deletedUsers.append(ruser)
                    time.sleep(10)
                    utility.execLog("Clicking on Remove User(s)")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "delete_user_link")), action="CLICK")
                    time.sleep(5)
                    utility.execLog("Clicking on 'Save'")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_editservice")), action="CLICK")
                    time.sleep(10)
                    failedUsers = set(userList).difference(set(deletedUsers))
                    if len(failedUsers) <= 0:
                        return self.browserObject, True, "Successfully Removed Users '%s' Access to the Service"%str(deletedUsers)
                    else:
                        return self.browserObject, False, "Failed to Remove Some/All Users '%s' Access to the Service, Removed Users '%s'"%(str(failedUsers), str(deletedUsers))
                else:           
                    if 'All' in userList:
                        utility.execLog("Selecting Users '%s' to provide Service Access"%str(userList))
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "allStandardUsers")), action="CLICK")
                    else:
                        utility.execLog("Selecting Users '%s' to provide Service Access"%str(userList))
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "specificUsers")), action="CLICK")
                        utility.execLog("Selecting 'Add Users' option")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "new_user_link")), action="CLICK")
                        utility.execLog("Clicking on Users Page")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "page_assign_user")), action="CLICK")
                        time.sleep(2)
                        if "Add User" not in self.browserObject.title:
                            return self.browserObject, False, "Failed to verify page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title)
                        utility.execLog("Verified page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title))
                        utility.execLog("Identifying Users Table 'users_table'")
                        xpath = "//table[@id='users_table']/tbody/tr"
                        totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
                        if totalRows == 1:
                            xpath = "//table[@id='users_table']/tbody/tr[1]/td"
                            cols = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
                            if cols == 1:
                                totalRows = 0  
                        utility.execLog("Total Number of Users : %s"%str(totalRows))
                        for user in userList:
                            selected = False
                            for loop in range(1, totalRows + 1):
                                xpath = "//table[@id='users_table']/tbody/tr[%i]/td[2]"%loop 
                                uName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") 
                                if user == uName:
                                    xpath = "//table[@id='users_table']/tbody/tr[%i]/td[1]"%loop
                                    utility.execLog("Adding User '%s'"%user)
                                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                                    selected = True
                            if not selected:
                                utility.execLog("Clicking on 'Cancel' in Users page")
                                self.handleEvent(EC.element_to_be_clickable((By.ID, "cancel_user_form")), action="CLICK")
                                time.sleep(5)
                                utility.execLog("Clicking on 'Cancel' in Edit Service page")
                                self.handleEvent(EC.element_to_be_clickable((By.ID, "cancel_form_editservice")), action="CLICK")
                                time.sleep(5)
                                return self.browserObject, False, "Failed to Select User '%s'"%user
                        utility.execLog("Clicking on 'Add' to save Added Users")      
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_user_form")), action="CLICK")
            if manageFirmware:
                utility.execLog("Clicking the Firmware update checkbox")
                if not self.handleEvent(EC.presence_of_element_located((By.ID,"managefirmware")), action="IS_SELECTED"):
                    self.handleEvent(EC.presence_of_element_located((By.ID, "managefirmware")), action="CLICK")
                time.sleep(2)
                utility.execLog("Selecting Firmware %s"%firmwareName)
                select = Select(self.browserObject.find_element_by_id("firmwarepackage"))
                select.select_by_visible_text(firmwareName)
                time.sleep(2)
            utility.execLog("Clicking on 'Save'")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_editservice")), action="CLICK")
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False)
                errorMessage = eleError.find_element_by_tag_name("h3").text
                time.sleep(2)
                return self.browserObject, False, "Failed to Edit Service :: '%s' :: Error -> '%s'"%(serviceName, 
                                                    str(errorMessage))
            except:
                return self.browserObject, True, "Successfully Edited Service '%s'"%serviceName           
        except Exception as e:
            return self.browserObject, False, "Failed to Edit Service :: '%s' :: Error -> %s"%(serviceName, 
                                                    str(e) + format_exc())

    def viewServiceDetails(self, serviceName):
        """
        View Details of a Service
        """
        try:
            loopCount = 3
            while loopCount:
                try:
                    self.browserObject, status, result = self.selectService(serviceName)
                    if not status:
                        return self.browserObject, False, result
                    utility.execLog("Clicking on 'View Details'")
                    self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "View Details")), action="CLICK")
                    time.sleep(5)
                    return self.browserObject, True, "Clicked on 'View Details' of Service '%s'"%serviceName
                except Exception as se:
                    utility.execLog("Services Page reloaded '%s'"%(str(se) + format_exc()))
                    loopCount = loopCount - 1
            return self.browserObject, False, "Attempted 3 times and Failed to Click on 'View Details' of Service '%s'"%serviceName
        except Exception as e:
            utility.execLog("Exception while trying to click on 'View Details' of Service '%s' :: Error -> %s"%(serviceName,str(e) + format_exc()))
            raise e
    
    def getServiceValDetails(self, serviceName, depStatus=False):
        """
        Get Deployment Service Details from View Details Page of Service
        """
        try:
            self.viewServiceDetails(serviceName)
            if depStatus:
                deploymentStatus = {}
                if globalVars.configInfo["Information"]["version"] >= "8.3":
                    upperBound = 7
                else:
                    upperBound = 6

                for i in xrange(3,upperBound):
                    if (i==3):
#                        xpath = ".//*[@id='serviceActions']/ul[1]/li[%i]/span[2]"%i
                        xpath = "//*[@id='serviceActions']/ul/li/span[@data-bind='text: _view.viewmodel.overallServiceHealthText()']"
                        lxpath = xpath + "/parent::li/label"
                    elif 4 == i:
                        xpath = "//*[@id='serviceActions']/ul/li/span[@data-bind='text: _view.viewmodel.overallServiceResourceHealthText()']"
                        lxpath = xpath + "/parent::li/label"                        
                    elif 5 == i:
                        xpath = "//*[@id='serviceActions']/ul/li/span[@data-bind='firmwarestatusicontext: firmwareCompliant']"
                        lxpath = xpath + "/parent::li/label"
                    elif 6 == i:
                        xpath = "//*[@id='serviceActions']/ul/li/span[@data-bind='text: state']"
                        lxpath = xpath + "/parent::li/label" 
                    else:
#                        xpath = ".//*[@id='serviceActions']/ul[1]/li[%i]/span"%i
                        self.failure("Unexpected index found!", raiseExc=True)
#                    lxpath = ".//*[@id='serviceActions']/ul[1]/li[%i]/label"%i
                    label = self.handleEvent(EC.presence_of_element_located((By.XPATH, lxpath)), action="GET_TEXT") 
                    deploymentStatus[label] = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") 
                utility.execLog("Deployment Status Factors %s "%str(deploymentStatus))
                return deploymentStatus
            else:
                #Deployment ID 
                deploymentID = str(self.browserObject.current_url).split("id=")[1]
                utility.execLog("URL:%s "%deploymentID)
                
                #VM Name List
                table = self.browserObject.find_element_by_id("vmTable")
                tableBody = table.find_element_by_tag_name("tbody")
                tableBodyRows = tableBody.find_elements_by_tag_name("tr")
                utility.execLog("Checking VM Count: %d" %(len(tableBodyRows)))
                vmList = []
                if len(tableBodyRows):
                    for rowindex in xrange(1, len(tableBodyRows)+1):
                        vmList.append(self.browserObject.find_element_by_xpath(".//*[@id='vmTable']/tbody/tr[%i]/td[3]"%rowindex).text)
                else:
                    utility.execLog("No VMs Found or Unable to retrieve VM Details from View Details Page of a Service")
                
                #Server OS IP List
                table = self.browserObject.find_element_by_id("serverTable")
                tableBody = table.find_element_by_tag_name("tbody")
                tableBodyRows = tableBody.find_elements_by_tag_name("tr")
                utility.execLog("Checking Server Count: %d" %(len(tableBodyRows)))
                OSIPList = []
                hostName = []
                if len(tableBodyRows):
                    for rowindex in xrange(1, len(tableBodyRows)+1):
#                        OSIPList.append(self.browserObject.find_element_by_xpath(".//*[@id='serverTable']/tbody/tr[%i]/td[5]"%rowindex).text)
                        xpath = "//*[@id='serverTable']/tbody/tr[{}]/td/".format(rowindex)
                        if globalVars.configInfo["Information"]["version"] < "8.3.1":
                            OSIPList.append(self.browserObject.find_element_by_xpath( xpath + "span[contains(@data-bind,'hypervisorIPAddressUrl')]").text)
                        else:
                            OSIPList.append(self.browserObject.find_element_by_xpath( xpath + "ul/li/a/span").text)

                        hostName.append(self.browserObject.find_element_by_xpath(
                            "//*[@id='serverTable']/tbody/tr[{}]/td[contains(@data-bind,'hostname')]".format(rowindex)).text)
                else:
                    utility.execLog("No Servers Found or Unable to retrieve Server Details from View Details Page of a Service")
                
                #Volume List
                table = self.browserObject.find_element_by_id("storageTable")
                tableBody = table.find_element_by_tag_name("tbody")
                tableBodyRows = tableBody.find_elements_by_tag_name("tr")
                count = (len(tableBodyRows))/2
                utility.execLog("Checking Storage Count: %d" %count)
                volumeList = []
                if len(tableBodyRows):
                    for rowindex in xrange(2, len(tableBodyRows)+1, 2):  
                        volumeList.append(self.browserObject.find_element_by_xpath(".//*[@id='storageTable']/tbody/tr[%i]/td/span[1]"%rowindex).text)
                else:
                    utility.execLog("No Storage Volumes Found or Unable to retrieve Storage Details from View Details Page of a Service") 
                return deploymentID, vmList, OSIPList, volumeList, hostName
        except Exception as e:
            utility.execLog("Exception while trying to get details on 'View Details' Page of Service '%s' :: Error -> %s"%(serviceName, str(e) + format_exc()))
    
    def readServicePageOptions(self, serviceName):
        """
        Read Options of a Service
        """
        try:
            loopCount = 3
            optionList = {}
            opts = {"Update Firmware":"lnkUpdateFirmware", "Export to File":"lnkExportService", "View Details":"btn-primary"}
            while loopCount:
                try:
                    for optName, optValue in opts.items():
                        utility.execLog("Reading Option '%s'"%optName)
                        xpath = "//table[@id='%s']/tbody/tr[%i]"%(self.serviceTableList, self.serviceIndex)
                        if self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName="class") == "":
                            self.browserObject, status, result = self.selectService(serviceName)
                            if not status:
                                return self.browserObject, False, result
                        if optName == "View Details":
                            status = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled") 
                        else:
                            status = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled") 
                        if status == "true":
                            optionList[optName] = "Disabled"
                        else:
                            optionList[optName] = "Enabled"
                    return self.browserObject, True, optionList
                except Exception as se:
                    utility.execLog("Services Page reloaded '%s'"%(str(se) + format_exc()))
                    loopCount = loopCount - 1
            utility.execLog("Attempted 3 times and Failed to read options '%s' of Service '%s'"%(str(opts.keys()), serviceName))
            return self.browserObject, True, optionList
        except Exception as e:
            utility.execLog("Exception while trying to click on 'View Details' of Service '%s' :: Error -> %s"%(serviceName, str(e) + format_exc()))
            raise e
    
    def generateTroubleShootingBundle(self, serviceName):
        """
        Description:
            API to Generate Troubleshooting Bundle  
        """
        try:
            utility.execLog("Reading Services Table")
            time.sleep(5)
            self.browserObject, status, result = self.selectService(serviceName)
            if not status:
                return self.browserObject, status, result
            self.browserObject, status, result = self.viewServiceDetails(serviceName)
            if not status:
                return self.browserObject, status, result
            serviceDetailPage = self.handleEvent(EC.presence_of_element_located((By.ID, "page_service_detail")), action="GET_TEXT") 
            if "%s Details"%serviceName not in serviceDetailPage:
                utility.execLog("Failed to verify Service Detail Page :: Actual: %s, Expected: %s"%(serviceDetailPage, serviceName + " Details"))
                return self.browserObject, False, "Failed to verify Service Detail Page :: Actual: %s, Expected: %s"%(serviceDetailPage, serviceName + " Details")
            utility.execLog("Moved to Service Detail Page and Verified Page Title :: Actual: %s, Expected: %s"%(serviceDetailPage, serviceName + " Details"))
            #path = "//aside[@id='serviceActions']/ul[2]/li/a[@id='generateTroubleshootLink']"
            self.handleEvent(EC.presence_of_element_located((By.LINK_TEXT, "Generate Troubleshooting Bundle")), action="CLICK")
            time.sleep(30)
            utility.execLog("Clicked on Generate Troubleshooting Bundle for Service '%s'"%serviceName)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False)
                errorMessage = eleError.find_element_by_tag_name("h3").text
                time.sleep(2)
                return self.browserObject, False, "Failed to Generate TroubleShooting Bundle for Service '%s' :: Error -> '%s'"%(serviceName, str(errorMessage))
            except:
                return self.browserObject, True, "Successfully initiated Generate TroubleShooting Bundle for Service '%s'"%serviceName
        except Exception as e:
            return self.browserObject, False, "Exception while Generating TroubleShooting Bundle for Service '%s' :: Error -> %s"%(serviceName, str(e) + format_exc())
    
    def exportData(self):
        """
        Description: 
            API to Export All Services to CSV file
        """
        try:
            utility.execLog("Clicking on 'Export All'")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "exportAllLink")), action="CLICK")
            time.sleep(30)
            utility.execLog("Clicked on 'Export All'")
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False)
                errorMessage = eleError.find_element_by_tag_name("h3").text
                time.sleep(2)
                return self.browserObject, False, "Failed to Export All Services :: Error -> '%s'"%(str(errorMessage))
            except:
                return self.browserObject, True, "Successfully initiated Export All Services"
        except Exception as e:
            return self.browserObject, False, "Exception while Exporting All Services :: Error -> %s"%(str(e) + format_exc())
	
######################## HCL################################
    def get_check_Service_Details(self,FilterBy=""):
        svcList = []
        try:
            if self.svcloopCount > 0:
                
                table = self.browserObject.find_element_by_id(self.healthServiceIcons)
                utility.execLog("Able to identify '%s' Table"%self.healthServiceIcons)
                serviceTypeCollection = table.find_elements_by_tag_name("li")
                if len(serviceTypeCollection) > 0:
                    utility.execLog("Able to identify '%s' Table Header"%self.healthServiceIcons)
                    for template in serviceTypeCollection:
                        #if template.get_attribute("visibility") == "visible":
                        if template.text != "":
                            temp = {}
                            name = template.find_element_by_tag_name("strong").text
                            tempId = template.find_element_by_tag_name("a").get_attribute("id")
                            temp["Name"] = str(name)
                            temp["Id"] = str(tempId)
                            time.sleep(5)
                            utility.execLog("clicking individual services")
                            template.find_element_by_tag_name("a").click()              
            else:
                utility.execLog("Maximum retries exceeded for reading '%s' Services Table :: Retries ('%s')"%(str(self.loopCount)))
                raise "Maximum retries exceeded for reading '%s' Services Table :: Retries ('%s')"%(str(self.loopCount))
        except StaleElementReferenceException as se:
            utility.execLog("'%s' Services Page reloaded '%s'"%(str(se)))
            self.svcloopCount = self.svcloopCount - 1
            self.readServices()
        except Exception as e:
            utility.execLog("Unable to read '%s' Services :: Error -> %s"%(str(e)))
            raise e
        finally:
            #self.browserObject.find_element_by_class_name("breadcrumbs").find_element_by_tag_name("a").click()
            Select(self.browserObject.find_element_by_id("ddlView")).select_by_visible_text("All")
            time.sleep(5)
            return "true"       


    def Deploy_Templates(self,option1):

        #Get Templates
     
        
        templateExists = False
        templatePublished = False
        templateName= "Test Template"
        pageObject = Templates.Templates(self.browserObject)
        pageObject.loadPage()
        globalVars.browserObject, status, result = pageObject.getDetails("My Templates", "All", "List")
        if templateName:
            tempList = [temp for temp in result if temp["Name"] == templateName]
        else:
            tempList = result
        for temp in tempList:
            if temp["Name"] == templateName:
                templateExists = True
                if temp["State"] == "Published":
                    templatePublished = True
        if not templatePublished and templateExists:
            utility.execLog("Template with Name 'Test Template' already exists and not Published so Deleting 'Test Template'")            
            pageObject.deleteTemplate(templateName)
        if templatePublished and templateExists:
            utility.execLog("Published Template with Name 'Test Template' already exists so not creating New Template")
        else:
            storageName = "EqualLogic"
            #Create Template
            pageObject.createTemplate(templateName, storageName, managePermissions="True", userList="All", publishTemplate="False")         

        #Get Template Options          
       
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            utility.execLog(result)
        else:
            utility.execLog(result)
        globalVars.browserObject, status, result = pageObject.getOptions(templateName=templateName)
        utility.execLog(result)
        self.browserObject = globalVars.browserObject
        self.browserObject.find_element_by_id("deployLink").click()
        utility.execLog("Navigate to Deploy Service")
        odate = time.strftime("%H:%M:%S")
        name = "ASM_GUI_Automation" + str(odate)
        utility.execLog(name)
        time.sleep(5)
        self.browserObject.find_element_by_id("servicename").send_keys(name)
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        utility.execLog("Deployed a service from a template shared by the Admin")
        time.sleep(5)
        self.browserObject.find_element_by_id("btnWizard-Finish").click()
        utility.execLog("click to finished button")
        
        #handle pop up for confirm deploy services
        time.sleep(5)
        self.browserObject.find_element_by_xpath(".//*[@id='submit_confirm_form']").click()
        time.sleep(20)  
        return templateName
      
    def verify_Services(self,option1, option2, template_name): 
        
        if option2 == "Second_Storage_Deployment":
            utility.execLog("Navigate to Deploy Service")
            date = datetime.now().strftime('%y%m%d%H%M%S')
            volume_name = "HCLVolumes"  +str(date)    
            pageObject = Templates.Templates(self.browserObject)
            pageObject.loadPage()
            time.sleep(5)
            self.browserObject.find_element_by_id("viewtemplatelist").click()
            time.sleep(2)
            tRows = self.browserObject.find_elements_by_xpath(".//*[@id='templateTable']/tbody/tr")
            utility.execLog("Length of template table %s"%str(len(tRows)))
            for row in xrange(0, len(tRows)):
                tRows[row].click()
                template = tRows[row].find_element_by_xpath("./td[4]").text
                if template == template_name:
                    utility.execLog("Template %s"%str(template))
                    time.sleep(5)
                    self.browserObject.find_element_by_id("editTemplateLink").click()
                    time.sleep(10)
                    self.browserObject.find_element_by_id("addStorage").click()
                    time.sleep(5)
                    select = Select(self.browserObject.find_element_by_id("ddlComponents"))
                    select.select_by_visible_text("EqualLogic")       
                    self.browserObject.find_element_by_id("btnContinueToSettings").click()
                    time.sleep(5) 
                    select = Select(self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='asm_guid']"))
                    select.select_by_index(1)
                    time.sleep(2)
                    self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='title']").send_keys("Specify a new storage volume name now")
                    time.sleep(2)
                    self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='volume_new']").send_keys(volume_name)
                    time.sleep(2)
                    if option1 == "Error Services":
                        self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='size']").send_keys("0MB")
                    else :
                        self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='size']").send_keys("500MB")      
                    time.sleep(5)
                    self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@data-automation-id='iqnOrIP']").send_keys("172.31.39.54")
                    time.sleep(5)
                    self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@data-automation-id='iqnOrIP']").send_keys(Keys.TAB)
                    time.sleep(2)
                    self.browserObject.find_element_by_xpath("//*[@id= 'page_componenteditor']//*[@id='submit_confirm_form']").click()
                    time.sleep(20)
                    self.browserObject.find_element_by_id("btnPublish").click()
                    time.sleep(10)
                    self.browserObject.find_element_by_id("submit_confirm_form").click()
                    time.sleep(10)
                    retry = 5
                    while retry>0:
                        time.sleep(5)                   
                        try:
                                              
                            self.browserObject.find_element_by_id("deployLink").click()
                            time.sleep(10)
                            break
                        except:
                            retry = retry-1
                            
                    utility.execLog("Navigate to Deploy Service")
                    date = datetime.now().strftime('%y%m%d%H%M%S')
                    name = "ASM_GUI_Automation" + str(date)
                    utility.execLog(name)
                    time.sleep(5)
                    self.browserObject.find_element_by_id("servicename").send_keys(name)
                    time.sleep(2)
                    self.browserObject.find_element_by_id("btnWizard-Next").click()
                    time.sleep(2)
                    self.browserObject.find_element_by_id("btnWizard-Next").click()
                    utility.execLog("Deployed a service from a template shared by the Admin")
                    time.sleep(2)
                    self.browserObject.find_element_by_id("btnWizard-Finish").click()
                    utility.execLog("click to finished button")
        
                    #handle pop up for confirm deploy services
                    time.sleep(5)
                    self.browserObject.find_element_by_xpath(".//*[@id='submit_confirm_form']").click()
                    time.sleep(20) 
                    Service_state = self.browserObject.find_element_by_xpath("//*[@id='serviceActions']/ul[2]/li[2]/span[2]").text
            
                    if (Service_state == "In progress"):
                        utility.execLog("Verified Service is in In-Progress State")
                     
                    return name  
                    
        if option1 == "user":
            
#             navObject = Navigation.Navigation(self.browserObject)
            self.selectOption("Dashboard")
            time.sleep(20)
            self.browserObject.find_element_by_xpath(".//*[@id = 'health']//*[@class = 'list-inline list-unstyled text-center']/li[3]/a/span").click()
            time.sleep(15)
            utility.execLog("Clicked on In-Progress Services")
            try:
                self.browserObject.find_element_by_id("viewservicelist").click()
            except:
                time.sleep(7)
                self.browserObject.find_element_by_id("viewservicelist").click()
            
            time.sleep(5)
            utility.execLog("Verified that ReadOnly user cannot update firmware and update firmware button is disabled")
            rows = self.browserObject.find_elements_by_xpath(".//*[@id='serviceTable']/tbody/tr")
            utility.execLog(len(rows))
            if int(len(rows))>0:
                
                self.browserObject.find_element_by_xpath(".//*[@id='serviceTable']/tbody/tr[1]/td[1]").click()
                self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li[1]/a").click()
                time.sleep(5)
                view_firmware_compliance_report_btn = self.browserObject.find_element_by_xpath(".//*[contains(@id,'lnkViewFirmwareReportTooltip')]").get_attribute("disabled")   
                time.sleep(2)
#                 change_server_firmware_baseline_btn = self.browserObject.find_element_by_id("lnkChangeBaseline").get_attribute("disabled")
                time.sleep(3)
                utility.execLog(view_firmware_compliance_report_btn)
                if(view_firmware_compliance_report_btn == "true"):
                    time.sleep(3)
                    utility.execLog("Verified user is not able to perform any action for In-Progress services")
                    return "true" 
                else:
                    return "false"
               
            else :
                utility.execLog("No Rows found for In-Progress Services hence exiting")
                return "true"
                   
    def get_ServiceDetails(self,option):
        
        #navObject = Navigation.Navigation(self.browserObject)
        self.selectOption("Services")
        time.sleep(5)
        self.browserObject.find_element_by_id("viewservicelist").click()
        time.sleep(2)
        table = self.browserObject.find_element_by_id("serviceTable")
        #Fetch Table header Details
        utility.execLog("Able to identify user Table")
        #Fetch Resource Details   
        tBody = table.find_element_by_tag_name("tbody")
        tBodyRows = tBody.find_elements_by_tag_name("tr")
        rows = len(tBodyRows)
        utility.execLog(rows)
        if(int(rows)<1):
            utility.execLog("No Service found")
            return "true"     
        
        for row in xrange(0, len(tBodyRows)):
            tBodyRows[row].click()
            time.sleep(2)
            update_firmware_button = self.browserObject.find_element_by_id("lnkUpdateFirmware").get_attribute("disabled")
            utility.execLog(update_firmware_button)
            if(update_firmware_button == "true"):
                utility.execLog("Verified that for each service the Read-only user is not able to Update Firmware")
                if option == "FirmwareActions":
                    self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li/a").click()
                    time.sleep(5)
                    delete_btn = self.browserObject.find_element_by_id("lnkDeleteService").get_attribute("disabled")
                    retry_btn = self.browserObject.find_element_by_id("lnkRetryService").get_attribute("disabled")
                    export_btn = self.browserObject.find_element_by_id("lnkExportService").get_attribute("disabled")
                    troubleshoot_btn = self.browserObject.find_element_by_id("generateTroubleshootLink").get_attribute("disabled")
                    time.sleep(3)
                    add_resource_btn = self.browserObject.find_element_by_xpath(".//*[@id='listItemAddActions']/div/button").get_attribute("disabled")
                    firmware_report_btn = self.browserObject.find_element_by_xpath(".//*[contains(@id,'lnkViewFirmwareReportTooltip')]").get_attribute("disabled")
#                     firmware_baseline_btn = self.browserObject.find_element_by_id("lnkChangeBaseline").get_attribute("disabled")
                    
                    if (delete_btn and retry_btn and export_btn and troubleshoot_btn and add_resource_btn and firmware_report_btn):
                        utility.execLog(" Verified All actions are disabled for the ReadOnly user")
                        return "true"
                    
                return "true"
            else :
                return "false"
            if int(row)>10:
                break
            
    #Testcase_2596       
    def get_check_Services_DeviceConsole(self,option1):

        #Get Templates
        templateExists = False
        templatePublished = False
        templateName= "Test Template"
        pageObject = Templates.Templates(self.browserObject)
        pageObject.loadPage()
        globalVars.browserObject, status, result = pageObject.getDetails("My Templates", "All", "List")
        if templateName:
            tempList = [temp for temp in result if temp["Name"] == templateName]
        else:
            tempList = result
        for temp in tempList:
            if temp["Name"] == templateName:
                templateExists = True
                if temp["State"] == "Published":
                    templatePublished = True
        if not templatePublished and templateExists:
            utility.execLog("Template with Name 'Test Template' already exists and not Published so Deleting 'Test Template'")            
            pageObject.deleteTemplate(templateName)
        if templatePublished and templateExists:
            utility.execLog("Published Template with Name 'Test Template' already exists so not creating New Template")
        else:
            storageName = "EqualLogic"
            #Create Template
            pageObject.createTemplate(templateName, storageName, managePermissions="True", userList="All", publishTemplate="False")         

        #Get Template Options          
       
        pageObject = Templates.Templates(self.browserObject)
        globalVars.browserObject, status, result = pageObject.loadPage()
        if status:
            utility.execLog(result)
        else:
            utility.execLog(result)
        globalVars.browserObject, status, result = pageObject.getOptions(templateName=templateName)
        utility.execLog(result)
        self.browserObject = globalVars.browserObject
        utility.execLog("Should be on Service Page to click deploy link==>>")
        time.sleep(5)
        
        self.browserObject.find_element_by_id("deployLink").click()
        utility.execLog("Navigate to Deploy Service")
        odate = time.strftime("%H:%M:%S")
        name = "ASM_GUI_Automation" + str(odate)
        utility.execLog(name)
        time.sleep(5)
        self.browserObject.find_element_by_id("servicename").send_keys(name)
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        utility.execLog("Deployed a service from a template shared by the Admin")
        time.sleep(5)
        self.browserObject.find_element_by_id("btnWizard-Finish").click()
        utility.execLog("click to finished button")
        
        #handle pop up for confirm deploy services
        time.sleep(5)
        self.browserObject.find_element_by_xpath(".//*[@id='submit_confirm_form']").click()
        time.sleep(20)
        self.browserObject.find_element_by_xpath(".//*[@id='storageTable']/tbody/tr[1]/td[3]/a/span").click()
        utility.execLog("IP link is opening a new Tab")
        time.sleep(5)
        curWindowHndl = self.browserObject.current_window_handle
        self.browserObject.switch_to_window(self.browserObject.window_handles[1])
        self.browserObject.close()
        self.browserObject.switch_to_window(curWindowHndl)
        utility.execLog("Closing new Tab")
        time.sleep(3)
        return templateName
    
    #HCL Testcase_2533       
    def get_check_Services_Readonly_DeviceConsole(self,option1):

        #Get Templates
        templateExists = False
        templatePublished = False
        templateName= "Test Template"
        pageObject = Templates.Templates(self.browserObject)
        pageObject.loadPage()
        globalVars.browserObject, status, result = pageObject.getDetails("My Templates", "All", "List")
        if templateName:
            tempList = [temp for temp in result if temp["Name"] == templateName]
        else:
            tempList = result
        for temp in tempList:
            if temp["Name"] == templateName:
                templateExists = True
                if temp["State"] == "Published":
                    templatePublished = True
        if not templatePublished and templateExists:
            utility.execLog("Template with Name 'Test Template' already exists and not Published so Deleting 'Test Template'")            
            pageObject.deleteTemplate(templateName)
        if templatePublished and templateExists:
            utility.execLog("Published Template with Name 'Test Template' already exists so not creating New Template")
        else:
            storageName = "EqualLogic"
            #Create Template
            pageObject.createTemplate(templateName, storageName, managePermissions="True", userList="All", publishTemplate="True")         

        #Get Template Options          
       
#         pageObject = Templates.Templates(self.browserObject)
#         globalVars.browserObject, status, result = pageObject.loadPage()
#         if status:
#             utility.execLog(result)
#         else:
#             utility.execLog(result)
#         globalVars.browserObject, status, result = pageObject.getOptions(templateName=templateName)
#         utility.execLog(result)
        self.browserObject = globalVars.browserObject
        self.browserObject.find_element_by_id("deployLink").click()
        utility.execLog("Navigate to Deploy Service")
        odate = time.strftime("%H:%M:%S")
        name = "ASM_GUI_Automation" + str(odate)
        utility.execLog(name)
        time.sleep(5)
        self.browserObject.find_element_by_id("servicename").send_keys(name)
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        utility.execLog("Deployed a service from a template shared by the Admin")
        time.sleep(5)
        self.browserObject.find_element_by_id("btnWizard-Finish").click()
        utility.execLog("click to finished button")
        
        #handle pop up for confirm deploy services
        time.sleep(5)
        self.browserObject.find_element_by_xpath(".//*[@id='submit_confirm_form']").click()
        time.sleep(20)
        self.browserObject.find_element_by_xpath(".//*[@id='storageTable']/tbody/tr[1]/td[3]/a/span").click()
        time.sleep(10)
        utility.execLog("Unable to open device console for read only user")
        self.browserObject.quit() 
        return templateName

    def get_ErrorServiceDetails(self,option):
        
#         navObject = Navigation.Navigation(self.browserObject)
        self.selectOption("Dashboard")
        time.sleep(10)
        self.browserObject.find_element_by_xpath(".//*[@id = 'health']//*[@class = 'list-inline list-unstyled text-center']/li[1]/a/span").click()
        time.sleep(5)
        self.browserObject.find_element_by_id("viewservicelist").click()
        time.sleep(2)
        row = self.browserObject.find_element_by_xpath(".//*[@id='serviceTable']/tbody/tr/td")
        if (row == "false"):
            utility.execLog("No Error service found")
            return "true"
        row.click()
        time.sleep(2)
        update_firmware_button = self.browserObject.find_element_by_id("lnkUpdateFirmware").get_attribute("disabled")
        export_btn = self.browserObject.find_element_by_id("lnkExportService").get_attribute("disabled")
        time.sleep(2)
        self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li/a").click()
        time.sleep(5)
        if (update_firmware_button and export_btn):
            utility.execLog("Verified that only View Details button is enabled for Error Services for ReadOnly Users")
            return "true"
        else :
            return "false"
        
        
    def Deploy_Service(self,templateName, server_name, option):
        
        utility.execLog("Deploy_Service()...Services")
#         navObject = Navigation.Navigation(self.browserObject)
        self.selectOption("Dashboard")
        time.sleep(15)
        try:
            time.sleep(5)
            self.browserObject.find_element_by_id("deployLink").click()
            time.sleep(5)
        except:
            self.browserObject.refresh()
            time.sleep(15)
            self.browserObject.find_element_by_id("deployLink").click()
            time.sleep(5)    
        select = Select(self.browserObject.find_element_by_id("selectedtemplate"))
        select.select_by_visible_text(templateName)
        
        odate = time.strftime("%H:%M:%S")
        name = "ASM_GUI_Automation" + str(odate)
        utility.execLog(name)
        time.sleep(5)
        self.browserObject.find_element_by_id("servicename").send_keys(name)
        time.sleep(5)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        time.sleep(5)
        select = Select(self.browserObject.find_element_by_xpath(".//*[@id = 'DeploySettings']//*[@class= 'form-group']//*[@data-automation-id='server_pool']"))
        
        time.sleep(3)
        selected_server_pool = select.select_by_visible_text(server_name)
        time.sleep(5)
        utility.execLog("server pool selectected successfully")
        utility.execLog(selected_server_pool)
        utility.execLog("server pool selectected successfully")
        time.sleep(2)
        spoolName=""
        try:
            svrpool=self.browserObject.find_elements_by_xpath(".//select[contains(@id,'setting_server_pool_asm')]//option")
            for spool in svrpool:
                if spool.text==server_name:
                    spoolName=spool.text
        except:
            utility.execLog("exception")
        
        
        utility.execLog("Spool %s"%str(spoolName))
        
        if option == "Migration":
            migration_check = self.browserObject.find_element_by_xpath(".//*[@id = 'DeploySettings']//*[@class= 'form-group']//*[@data-automation-id='migrate_on_failure']")
            if migration_check:
                utility.execLog("Verified that on the Service detail page, the Standard user can perform migration in a service owned by him.")
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(10)
                self.browserObject.find_element_by_id("submit_confirm_form").click()
                time.sleep(5)
                utility.execLog("Successfully selected pool and deployed service")
                return "true"
            else:
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                return "false"
                
        elif spoolName == server_name:
            utility.execLog("Verified that while deploying a service,  Standard user is able to select only the server pool that he has access")
            
            self.browserObject.find_element_by_id("btnWizard-Cancel").click()
            time.sleep(10)
            self.browserObject.find_element_by_id("submit_confirm_form").click()
            time.sleep(5)
            utility.execLog("Successfully selected pool and deployed service")
            return "true"
        else:
            utility.execLog("Assigned Server is not selected for Standard User")
            self.browserObject.find_element_by_id("btnWizard-Cancel").click()
            return "false"


    def VerifyServiceDetails(self,option):
        
#         navObject = Navigation.Navigation(self.browserObject)
        self.selectOption("Dashboard")
        time.sleep(10)
        if option == "Error Services":
            self.browserObject.find_element_by_xpath(".//*[@id = 'health']//*[@class = 'list-inline list-unstyled text-center']/li[1]/a/span").click()
        if option == "WarningServices":
            self.browserObject.find_element_by_xpath(".//*[@id = 'health']//*[@class = 'list-inline list-unstyled text-center']/li[2]/a/span").click()
        time.sleep(5)
        self.browserObject.find_element_by_id("viewservicelist").click()
        time.sleep(2)
        try:
            no_service_available_text = "There are currently no services available."
            text = self.browserObject.find_element_by_xpath(".//*[@id='serviceTable']/tfoot/tr/td").text
            if (text == no_service_available_text):  
                utility.execLog("No service found")
                return "true"
        except:
            self.browserObject.find_element_by_xpath(".//*[@id='serviceTable']/tbody/tr").click()
            time.sleep(2)
            update_firmware_button = self.browserObject.find_element_by_id("lnkUpdateFirmware").get_attribute("disabled")
            export_btn = self.browserObject.find_element_by_id("lnkExportService").get_attribute("disabled")
            time.sleep(2)
            self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li/a").click()
            time.sleep(5)
            if (update_firmware_button and export_btn):
                utility.execLog("Verified that only View Details button is enabled for Error Services for ReadOnly Users")
                return "true"
            else :
                return "false"
                
    def ServiceDetails(self,serviceName, option):
        
#         navObject = Navigation.Navigation(self.browserObject)
        self.selectOption("Services")
        utility.execLog(serviceName)
        time.sleep(5)
        self.browserObject.find_element_by_id("viewservicelist").click()
        time.sleep(2)
        table = self.browserObject.find_element_by_id("serviceTable")
        #Fetch Table header Details
        utility.execLog("Able to identify user Table")
        #Fetch Resource Details   
        tBody = table.find_element_by_tag_name("tbody")
        tBodyRows = tBody.find_elements_by_tag_name("tr")
        rows = len(tBodyRows)
        utility.execLog(rows)
        if(int(rows)<1):
            utility.execLog("No Service found")
            return "true"     
        
        for row in xrange(0, len(tBodyRows)):
            tBodyRows[row].click()
            service_name = self.browserObject.find_element_by_xpath(".//*[@id='serviceTable']/tbody/tr/td[2]").text
            if service_name == serviceName:
                utility.execLog("found service")
                time.sleep(2)
                self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li[1]/a").click()
                time.sleep(5)
                text = self.browserObject.find_element_by_xpath(".//*[@id='serverTable']/tbody/tr/td[2]").text
                template_name = self.browserObject.find_element_by_id("referencetemplate").text
                if template_name == option and text == "Server":
                    utility.execLog("Verified that the Standard user able to view service detail even if he does not have access to the server pool in the service.")
                    return "true"
                else:
                    return "false"
            
        utility.execLog("Service not found")
        return "true"
    
    
    #HCL Testcase_2645
    def viewDeployment_services_Settings(self, serviceName=None):
        """
        Description:
            API to get All Settings in a Template 
        """

        try:
            if serviceName:
                self.browserObject, status, result = self.viewDeployment_services_Setting(serviceName)
                if not status:
                    return self.browserObject, status, result
            time.sleep(10)
            utility.execLog("Clicking on 'View All Settings'")
            self.browserObject.find_element_by_id("lnkViewSettings").click()
            time.sleep(10)
            utility.execLog("Clicked on 'View All Settings'")
            utility.execLog("Verifying Service Deployment Settings Page")
            serviceAct=self.browserObject.find_element_by_id("serviceActions").text
            utility.execLog(serviceAct)
            listInfo = []
            elems = self.browserObject.find_elements_by_xpath("//ul[@class='list-unstyled infoCol']/li/span")
            for elem in elems:
                listInfo.append(elem.text)
            listInfo = [x for x in listInfo if x !='']
            utility.execLog(listInfo) 
            utility.execLog("getting view setting data:")
            
            time.sleep(10)

            self.browserObject.find_element_by_id("cancel_form_settings").click()

            utility.execLog("Closing form setting.")

            time.sleep(10)
            return self.browserObject,True,listInfo 
        except Exception as e:
            return self.browserObject, False, "Read Options on Service Deployment Settings Page :: Successfully -> %s"%str(e)
        finally:
            pass
        
        
    def viewDeployment_services_Setting(self, serviceName):
        """
        View Details of a Service
        """
        try:
            loopCount = 3
            while loopCount:
                try:
                    self.browserObject, status, result = self.selectService(serviceName)
                    if not status:
                        return self.browserObject, False, result
                    utility.execLog("Clicking on 'View Details'")
                    time.sleep(5)
                    self.browserObject.find_element_by_id("serviceDetails").find_element_by_class_name("btn-primary").click()
                    utility.execLog("Clicked on 'View Details'")
                    return self.browserObject, True, "Clicked on 'View Details' of Service '%s'"%serviceName
                except StaleElementReferenceException as se:
                    utility.execLog("Services Page reloaded '%s'"%str(se))
                    loopCount = loopCount - 1
            return self.browserObject, False, "Attempted 3 times and Failed to Click on 'View Details' of Service '%s'"%serviceName
        except Exception as e:
            utility.execLog("Exception while trying to click on 'View Details' of Service '%s' :: Error -> %s"%(serviceName, str(e)))
            raise e
        
    def Verify_StandardUserServices(self,option):
        
#         navObject = Navigation.Navigation(self.browserObject)
        self.selectOption("Dashboard")
        time.sleep(10)
        if option == "Error Services":
            self.browserObject.find_element_by_xpath(".//*[@id = 'health']//*[@class = 'list-inline list-unstyled text-center']/li[1]/a/span").click()
        elif option == "WarningServices":
            if self.browserObject.find_element_by_xpath(".//*[@id = 'health']//*[@class = 'list-inline list-unstyled text-center']/li[2]/a/span").is_displayed():
                self.browserObject.find_element_by_xpath(".//*[@id = 'health']//*[@class = 'list-inline list-unstyled text-center']/li[2]/a/span").click()
            else:
                utility.execLog("No warning service found")
                return "true" 

        time.sleep(5)
        self.browserObject.find_element_by_id("viewservicelist").click()
        time.sleep(2)
        no_service_available_text = "There are currently no services available."
        try:
            text = self.browserObject.find_element_by_xpath(".//*[@id='serviceTable']/tfoot/tr/td").text
            if (text == no_service_available_text):        
                utility.execLog("No service found")
                return "true"
        except:
            utility.execLog("Services are found")

        self.browserObject.find_element_by_xpath(".//*[@id='serviceTable']/tbody/tr/td").click()
        time.sleep(2)
        update_firmware_button = self.browserObject.find_element_by_id("lnkUpdateFirmware").get_attribute("disabled")
        export_btn = self.browserObject.find_element_by_id("lnkExportService").click()
        time.sleep(5)
        self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li/a").click()
        time.sleep(5)
        page_title = self.browserObject.find_element_by_xpath(".//*[@id='serviceActions']/h4[1]").text
        if page_title == "Service Information":
            utility.execLog("Verified that View All button is enabled for Standard user")
        if (update_firmware_button and export_btn):
            utility.execLog("Verified that only Export button and View All Settings buttons are enabled for Standard user")
            return "true"
        else :
            return "false"
    
    # Added new function for NGI-TC-2637 
    def exportFile(self, serviceName):
        """
        Export the service to a file
        """
        try:
            utility.execLog("Clicking on Export to File")
            time.sleep(5)
            path = os.path.join(os.getcwd(), globalVars.downloadDir)
            for fileName in os.listdir(os.path.join(os.getcwd(), globalVars.downloadDir)):
                fileName = os.path.join(path, fileName)
                os.remove(fileName)
            
            self.browserObject.find_element_by_xpath("//button[@id='lnkExportService']").click()
            time.sleep(10)
            utility.execLog("Verifying the downloaded export file.")
            for fileName in os.listdir(os.path.join(os.getcwd(), globalVars.downloadDir)):
                if serviceName in fileName:
                    return self.browserObject, True, "File %s successfully downloaded for service %s"%(str(fileName), str(serviceName))
                else:
                    return self.browserObject, False, "File %s downloaded failed for service %s"%(str(fileName), str(serviceName))
        except Exception as e:
            utility.execLog("Exception while trying to download the export file for service '%s' :: Error -> %s"%(serviceName, str(e)))
            raise e
        
    def get_Standard_user_ServiceDetails(self,option):
        
#         navObject = Navigation.Navigation(self.browserObject)
        self.selectOption("Services")
        time.sleep(5)
        self.browserObject.find_element_by_id("viewservicelist").click()
        time.sleep(2)
        table = self.browserObject.find_element_by_id("serviceTable")
        #Fetch Table header Details
        utility.execLog("Able to identify user Table")
        #Fetch Resource Details   
        tBody = table.find_element_by_tag_name("tbody")
        tBodyRows = tBody.find_elements_by_tag_name("tr")
        rows = len(tBodyRows)
        utility.execLog(rows)
        if(int(rows)<1):
            utility.execLog("No Service found")
            return "true"     
        
        for row in xrange(0, len(tBodyRows)):
            tBodyRows[row].click()
            time.sleep(2)
            deployed_by = tBodyRows[row].find_element_by_xpath("./td[3]").text
            update_firmware_button = self.browserObject.find_element_by_id("lnkUpdateFirmware").get_attribute("disabled")
            if(deployed_by == "admin" and update_firmware_button == "true"):
                
                utility.execLog(update_firmware_button)           
                utility.execLog("Verified that the Service is deployed by Admin user and Standard user is not able to Update Firmware")
                self.browserObject.find_element_by_xpath(".//*[@id='btnList']/li/a").click()
                time.sleep(5)
                delete_btn = self.browserObject.find_element_by_id("lnkDeleteService").get_attribute("disabled")
                retry_btn = self.browserObject.find_element_by_id("lnkRetryService").get_attribute("disabled")
                troubleshoot_btn = self.browserObject.find_element_by_id("generateTroubleshootLink").get_attribute("disabled")
                time.sleep(2)
                add_resource_btn = self.browserObject.find_element_by_xpath(".//*[@id='listItemAddActions']/div/button").get_attribute("disabled")
                firmware_report_btn = self.browserObject.find_element_by_xpath(".//*[contains(@id,'lnkViewFirmwareReportTooltip')]").get_attribute("disabled")
#                 firmware_baseline_btn = self.browserObject.find_element_by_id("lnkChangeBaseline").get_attribute("disabled")
                if (delete_btn and retry_btn and troubleshoot_btn and add_resource_btn and firmware_report_btn):
                    utility.execLog("Verified that on the Service detail page, the Standard user is not able to adjust any resources in a service shared by the Admin.")
                    return "true" 
                else:
                    return "false"
            
    
    # Created for TC-2496    
    def getExportToFileButtonStatus(self):
        """
        Verify export to file button is disabled or enabled.
        """
        utility.execLog("Verifying Export To File Button state.")
        if self.browserObject.find_element_by_xpath("//a[@id='deployLink']").get_attribute("disabled") != None:
            return True
        else:
            return False
                
    #HCL 2590
    def getServiceManagableByStandardUser(self, serviceName):
        """
        Export the service to a file
        """
        utility.execLog("Calling getServiceManagableByStandardUser() Services")
        time.sleep(5)
        counter = 10
        while counter > 0:
            if self.browserObject.find_element_by_xpath("//*[contains(@class,'btn btn-default dropdown-toggle')]").is_displayed():
                utility.execLog("%s is not In Progress State"%str(serviceName))
                break
            else:
                utility.execLog("Waiting for 1 minute. counter = %s"%str(counter))
                counter = counter - 1
                time.sleep(60)
                continue
        
        try:
            utility.execLog("Verifying that service are managable for standard user")
            time.sleep(5)
            
            if ((self.browserObject.find_element_by_id("lnkDeleteService").get_attribute("disabled")) == None ):
                utility.execLog("In True condition. Delete link verified")
                self.browserObject, "Delete link verified successfully successfully"
            else:
                    utility.execLog("In False condition.Delete link not  verified successfully")
                    return self.browserObject, False, "Delete link not  verified successfully successfully"
            
            if ((self.browserObject.find_element_by_id("lnkRetryService").get_attribute("disabled")) == None):
                utility.execLog("In True condition.Retry link verified")
                self.browserObject, True, "Retry link verified successfully successfully"
            else:
                    utility.execLog("In False condition.Retry link not  verified successfully")
                    return self.browserObject, False, "Retry link not  verified successfully successfully"
            
            if ((self.browserObject.find_element_by_xpath("//*[@id='listItemAddActions']/div/button").get_attribute("disabled")) == None):
                utility.execLog("In True condition.Add Resources link")
                self.browserObject, True, "Add Resources link verified successfully successfully"
            else:
                    utility.execLog("In False condition.Add Resources link not  verified successfully")
                    return self.browserObject, False, "Add Resources link not  verified successfully successfully"        
            
            if ((self.browserObject.find_element_by_id("lnkViewSettings").get_attribute("disabled")) == None):
                utility.execLog("In True condition.View all setting link")
                self.browserObject, True, "View all setting link verified successfully successfully"
            else:
                    utility.execLog("In False condition.View all setting link not  verified successfully")
                    return self.browserObject, False, "View all setting link not  verified successfully successfully"
            
            return self.browserObject, True, "All link verified successfully successfully"             
        except Exception as e:
            utility.execLog("Exception while trying to verifying link of  service '%s' :: Error -> %s"%(serviceName, str(e)))
            raise e
        
    # Created for TC-2533
    def verifyServiceComponentIPaddressState(self):
        """
        Verify Service component device console link is enabled.
        """
        utility.execLog("Verifying Service component IP address state.")
        try:
            if self.loopCount > 0:
                if self.browserObject.find_element_by_xpath("//table[@id='storageTable']/tbody/tr[1]/td[3]/a").get_attribute("href") == "":
                    return self.browserObject, True, "Service component IP address does not have link."
                else:
                    return self.browserObject, True, "Service component IP address have link."
            else:
                utility.execLog("Maximum retries exceeded for reading Services Table :: Retries ('%s')"%str(self.loopCount))
                raise "Maximum retries exceeded for reading Services Table :: Retries ('%s')"%str(self.loopCount)
        except Exception as e:
            self.loopCount = self.loopCount-1
            self.verifyServiceComponentIPaddressState()
            utility.log("Exception while trying to verify Service component device console link is available or not :: Error -> %s"%(str(e)))
                 
    def get_ServiceDetails_Firmware(self,option):
        
        try:
            utility.execLog("get_ServiceDetails_Firmware()...: %s"%option)
#             navObject = Navigation.Navigation(self.browserObject)
            self.selectOption("Services")
            time.sleep(5)
            self.browserObject.find_element_by_id("viewservicelist").click()
            time.sleep(2)
            table = self.browserObject.find_element_by_id("serviceTable")
            #Fetch Table header Details
            utility.execLog("Able to identify user Table")
        #Fetch Resource Details   
            tBody = table.find_element_by_tag_name("tbody")
            tBodyRows = tBody.find_elements_by_tag_name("tr")
            rows = len(tBodyRows)
            utility.execLog(rows)
            if(int(rows)<1):
                utility.execLog("No Service found")
                return "true"     
        
            for row in xrange(0, len(tBodyRows)):
                tBodyRows[row].click()
                time.sleep(2)
                update_firmware_button = self.browserObject.find_element_by_id("lnkUpdateFirmware").get_attribute("disabled")
                utility.execLog(update_firmware_button)
                if(update_firmware_button == "true"):
                    utility.execLog("Verified that for each service the %s user is not able to Update Firmware"%option)
                    
                    return self.browserObject, "true","Successfully verified update firmware link not able to update"
                else :
                    return "false"
                if int(row)>10:
                    break        
            
        except Exception as e:
            return self.browserObject, False,"Exception while trying to verifying not able to update firmware '%s'"%e    

    def Deploy_Service_with_FirmwareUpdate(self,templateName,option=None):
        
#         navObject = Navigation.Navigation(self.browserObject)
        self.selectOption("Dashboard")
        time.sleep(20)
        self.browserObject.find_element_by_id("deployLink").click()
        time.sleep(5)
        select = Select(self.browserObject.find_element_by_id("selectedtemplate"))
        select.select_by_visible_text(templateName)
        
        odate = time.strftime("%H:%M:%S")
        name = "ASM_GUI_Automation" + str(odate)
        utility.execLog(name)
        time.sleep(5)
        self.browserObject.find_element_by_id("servicename").send_keys(name)
        time.sleep(2)
        self.browserObject.find_element_by_id("managefirmware").click()
        firmware_update =  self.browserObject.find_element_by_id("managefirmware").get_attribute("checked")
        time.sleep(2)
        utility.execLog("Clicking the Firmware update checkbox")
        select = Select(self.browserObject.find_element_by_id("firmwarepackage"))
        select.select_by_index(1)
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        time.sleep(2)
        try:
            Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("autoVolume")
            time.sleep(1)
        except:
            try:
                Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("Create New Volume...")
                time.sleep(2)
                self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").send_keys("autoVolume")
                time.sleep(1)
            except:
                utility.execLog('Click Next Button')
        self.browserObject.find_element_by_id("btnWizard-Next").click()
        time.sleep(2)
        self.browserObject.find_element_by_id("btnWizard-Finish").click()
        time.sleep(2)
        self.browserObject.find_element_by_id("submit_confirm_form").click()
        time.sleep(20)
        if firmware_update:
            utility.execLog("Verified Admin user is able to update firmware")
            return "true"       
        else:
            utility.execLog("Admin user is not able to update firmware")
            return "false"


    def verifyAllLink(self):
        """
        Verify Service component device console link is enabled.
        """
        
        try:
            time.sleep(5)
            utility.execLog("verifyAllLink() Services")
            if ((self.browserObject.find_element_by_id("exportAllLink").get_attribute("disabled")) == None ):
                utility.execLog("Expoprt Link is verified successfully")
                
            else:
                    utility.execLog("ExpoprtAllLink  not  verified successfully")
                    return self.browserObject, False, "ExpoprtAllLink not  verified successfully"
            
            add_comp=self.browserObject.find_element_by_xpath("//*[@id='serviceMenuActions']/li[2]/a")
            add_comp.click()
            time.sleep(5)
            add_header=self.browserObject.find_element_by_xpath(".//*[@id='appHtml']/article/div/section/header/h1").text
            utility.execLog(add_header)
            if(add_header=="Add Existing Service"):
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(3)
                self.browserObject.find_element_by_id("submit_confirm_form").click()
                time.sleep(5)
                utility.execLog("Add Existing Service link verified successfully")
            else:
                utility.execLog("Add Existing Service link not  verified successfully")
                return self.browserObject, False, "Add Existing Service link not  verified successfully"
            
            deploy_comp=self.browserObject.find_element_by_id("deployLink")
            deploy_comp.click()
            time.sleep(5)
            deploy_header=self.browserObject.find_element_by_xpath("//*[@id='appHtml']/article/div/section/header/h1").text
            utility.execLog(deploy_header)
            if(deploy_header=="Deploy Service"):
                self.browserObject.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(3)
                self.browserObject.find_element_by_id("submit_confirm_form").click()
                time.sleep(5)
                utility.execLog("Deploy New Service link verified successfully")
            else:
                utility.execLog("Deploy New Service link not  verified successfully")
                return self.browserObject, False, "Deploy New Service link not  verified successfully"    
            
            time.sleep(5)
            
            
            return self.browserObject, True, "All link verified successfully"
            
        except Exception as e:
            return self.browserObject, False,"Exception while trying to verifying All link '%s'"%e
        
    def getServiceStatus(self, serviceName):
        """
        return 'success' , if Service deployed successfully
        else return 'failed' , if service deployment failed
        else return 'in-progress' , if service deployment in progress
        """
        utility.execLog("Navigating to service %s"%str(serviceName))
        counter = 0
        while True:
            try:
                utility.execLog("Click on View Type 'List'")
                time.sleep(10)
                self.browserObject.find_element_by_id(self.viewServiceList).click()
                time.sleep(10)
                status = self.browserObject.find_element_by_xpath("(//*[contains(text(),'%s')])[1]/parent::tr/td[1]/i/i"%str(serviceName)).get_attribute("class")
                if "critical" in status:
                    return self.browserObject, False, "failed"
                elif "success" in status:
                    return self.browserObject, True, "success"
                else:
                    return self.browserObject, True, "in-progress"
            except Exception as e:
                if counter < 3:
                    counter = counter + 1
                    continue
                else:
                    return self.browserObject, False, "Exception generated while getting Service %s status Error::-> %s"%(str(serviceName), str(e))
    
    def getServiceDetails(self, serviceName):
        """
        returns following details about service:
        
        Note: If required to add more details keep on increasing list size
        """
        try:
            detailsDict={}
            utility.execLog("Selecting Service %s"%str(serviceName))
            utility.execLog("Click on View Type 'List'")
            self.browserObject.find_element_by_id(self.viewServiceList).click()
            time.sleep(10)
#             select = Select(self.browserObject.find_element_by_id("ddlView"))
#             select.select_by_visible_text("All")
#             time.sleep(10)
            self.browserObject.find_element_by_xpath("(//*[contains(text(),'%s')])[1]"%str(serviceName)).click()
            time.sleep(2)
            utility.execLog("Selected Service %s"%str(serviceName))
            self.browserObject.find_element_by_id("serviceDetails").find_element_by_class_name("btn-primary").click()
            utility.execLog("Clicked on View Details settings.")
            time.sleep(5)
            detailsDict["VMHostName"] = self.browserObject.find_element_by_xpath("//table[@id='vmTable']/tbody/tr[1]/td[3]").text
            utility.execLog("Added Virtual Machine host name %s in list with key VMHostName"%str(detailsDict["VMHostName"]))
            detailsDict["ClusterIP"] = self.browserObject.find_element_by_xpath("//table[@id='clusterTable']/tbody/tr[1]/td[3]").text
            utility.execLog("Added Virtual Cluster IP Address %s in list with key ClusterIP"%str(detailsDict["ClusterIP"]))
            detailsDict["ServerOSIp"] = self.browserObject.find_element_by_xpath("//table[@id='serverTable']/tbody/tr[1]/td[5]").text
            utility.execLog("Added Server OS IP Address %s in list with key ServerOSIp"%str(detailsDict["ServerOSIp"]))
            self.browserObject.find_element_by_id("lnkViewSettings").click()
            utility.execLog("Clicked View All Settings button")
            time.sleep(10)
            try:
                self.browserObject.find_element_by_xpath("//h3[contains(text(),'VMWare Cluster')]/parent::span/i[1]").click()
                utility.execLog("Expanded VMWare Cluster dropdown")
                time.sleep(2)
                detailsDict["DatacenterName"] = self.browserObject.find_element_by_xpath("//*[contains(text(),'New datacenter name')]/parent::div/parent::li/div[2]/p").text
                utility.execLog("Added Data Center Name %s in list with key DatacenterName"%str(detailsDict["DatacenterName"]))
                detailsDict["ClusterName"] = self.browserObject.find_element_by_xpath("//*[contains(text(),'New cluster name')]/parent::div/parent::li/div[2]/p").text
                utility.execLog("Added Cluster Name %s in list with key ClusterName"%str(detailsDict["ClusterName"]))
                self.browserObject.find_element_by_id("cancel_form_settings").click()
                time.sleep(5)
            except Exception as e1:
                utility.execLog("Exception generated while fetching Service details on 'View All Settings' modal box Error::-> %s"%str(e1))
                self.browserObject.find_element_by_id("cancel_form_settings").click()
                time.sleep(5)
                return self.browserObject, False, "Exception generated while fetching Service details on 'View All Settings' modal box Error::-> %s"%str(e1)
            return self.browserObject, True, detailsDict
        except Exception as e:
            return self.browserObject, False, "Exception generated while fetching Service Details. Error::-> %s"%str(e)


    def delete_service(self):  
        """
        """
        self.loopCounter = 5
        while self.loopCounter > 0:
            utility.execLog("While loop started with counter %s"%str(self.loopCounter))
            try:
                serviceStatus = self.browserObject.find_elements_by_xpath("//table[@id='serviceTable']/tbody/tr/td/i/i")
                utility.execLog("Fetched services from the table , no of service => %s"%str(len(serviceStatus)))
                for elem in serviceStatus:
                    utility.execLog("Verifying the element %s status"%str(elem))
                    if not ("info" in elem.get_attribute("class")):
                        utility.execLog("Element %s is not in in-progress state"%str(elem))
                        elem.click()
                        time.sleep(10)
                        self.browserObject.find_element_by_xpath("//*[@id='serviceDetails']/ul[1]/li[1]/a").click()
                        utility.execLog("Clicked Service Details button for element %s"%str(elem))
                        time.sleep(3)
                        self.browserObject.find_element_by_id("lnkDeleteService").click()
                        utility.execLog("Clicked Delete service button for element %s"%str(elem))
                        time.sleep(3)
                        self.browserObject.find_element_by_id("submit_form_deleteservice").click()
                        utility.execLog("Clicked Service delete confirmation button for element %s"%str(elem))
                        time.sleep(2)
                        self.browserObject.find_element_by_id("submit_confirm_form").click()
                        utility.execLog("Clicked Service delete confirmation modal box button for element %s"%str(elem))
                        self.loaderCounter = 10
                        while self.loaderCounter > 0:
                            utility.execLog("Enter loader wait while loop")
                            time.sleep(2)
                            try:
                                if self.browserObject.find_element_by_class_name("loader-message").is_displayed():
                                    utility.execLog("Loader is displayed hence waiting for loop counter %s"%str(self.loadCounter))
                                    time.sleep(2)
                                    self.loaderCounter = self.loaderCounter - 1
                                    continue
                                else:
                                    utility.execLog("Loader is not displayed hence existing while loop for counter %s"%str(self.loadCounter))
                                    break
                            except:
                                utility.execLog("Exception generated while waiting for loader hence existing while loop for counter %s"%str(self.loadCounter))
                                break
                        self.delete_service()
            except Exception as e:
                utility.execLog("Exception generated while deleting services :: => %s . Attempt is %s"%(str(e),str(self.loopCounter)))
                self.loopCounter = self.loopCounter -1
                if self.loopCounter==0:
                    break
                else:
                    continue
    
    def getServerOS(self, serviceName):
        self.browserObject.find_element_by_xpath(self.ResourceDetailsObjects('viewAllSettings')).click()
        time.sleep(12)
        self.browserObject.find_element_by_xpath(self.ResourceDetailsObjects('serviceSettingsonViewAllSettings')).click()
        
        if(self.check_exists_by_xpath(self.ResourceDetailsObjects('presenceOfServerinViewAll'))):
            self.browserObject.find_element_by_xpath(self.ResourceDetailsObjects('presenceOfServerinViewAll')).click()
            esxiServerExists=self.check_exists_by_xpath(self.ResourceDetailsObjects('presenceOfESXIinViewAllSettings'))
            self.browserObject.implicitly_wait(10)
            self.browserObject.find_element_by_xpath(self.ResourceDetailsObjects('cancelButtonOnViewAllSettings')).click()
            self.browserObject.implicitly_wait(10)
            #self.browserObject.find_element_by_xpath(self.ResourceDetailsObjects('mainFormID')).click()
            #self.browserObject.find_element_by_xpath(self.ResourceDetailsObjects('maximizeASMMenu')).click()
            #self.browserObject.implicitly_wait(10)
            #self.browserObject.find_element_by_xpath(self.ResourceDetailsObjects('resourcesOption')).click()
            #self.browserObject.implicitly_wait(10)
            if(esxiServerExists):
                return self.browserObject, False, "ESXI Server Exists"
            else:
                return self.browserObject, True, "Not ESXI Server"
            
    def check_exists_by_xpath(self,xpath):
        try:
            self.browserObject.find_element_by_xpath(xpath)
        except NoSuchElementException:
            return False
        return True
            
    def exportAllServices(self):
        """
        Export all the the service to a file
        """
        try:
            utility.execLog("Clicking on Export to File")
            time.sleep(5)
            self.browserObject.find_element_by_id("exportAllLink").click()
            time.sleep(10)
            utility.execLog("Verifying the downloaded export file.")
            for fileName in os.listdir(os.path.join(os.getcwd(), globalVars.downloadDir)):
                if "asmServices" in fileName:
                    return self.browserObject, True, "All services export file %s successfully downloaded"%(str(fileName))
                else:
                    return self.browserObject, False, "All Services export file download failed"
        except Exception as e:
            utility.execLog("Exception while trying to download the export file for All services")
            raise e
        
    def serverDeploymentStatusInService(self, serviceName):
        """
        Server deployment status on Rseources page by service.
        """
        try:
            loopCount = 3
            while loopCount:
                try:
                    
                    self.browserObject, status, result = self.selectService(serviceName)
                    time.sleep(5)
                    if not status:
                        return self.browserObject, False, result
                    utility.execLog("Clicking on 'View Details'")
                    self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "View Details")), action="CLICK")
                    time.sleep(10)
                    servicedeploymentStatus = ""
                    serverState = ""
                    i=1
#                     serverIP=self.browserObject.find_element_by_xpath("//*[@id='serverTable']/tbody/tr/td[4]/a/span").text
                    serverIP=self.browserObject.find_elements_by_xpath("//*[@id='serverTable']/tbody/tr")
                    utility.execLog("Lenght are:%s"%str(len(serverIP)))
                    listIP = []
                    
                    for ip in serverIP:
                        time.sleep(10)
                        utility.execLog("!!!!!!!!!!!!!!!")
                        utility.execLog(ip)
                        utility.execLog("!!!!!!!!!!!!!!!")
                        serverip=self.browserObject.find_element_by_xpath("//*[@id='serverTable']/tbody/tr["+str(i)+"]/td[4]/a/span").text
                        utility.execLog("Services IP--->:%s"%serverip)
                        listIP.append(serverip)
                        xpath="(//*[@id='serverTable']/tbody/tr/td[1]//span[contains(@id,'error')])["+str(i)+"]"
                        xpath1="(//*[@id='serverTable']/tbody/tr/td[contains(text(),'Server')]/parent::tr/td[1]//i[@class='spinner'])["+str(i)+"]"
                        xpath2="(//*[@id='serverTable']/tbody/tr/td[1]//span[contains(@id,'pending')])["+str(i)+"]"
                        xpath3="(//*[@id='serverTable']/tbody/tr/td[1]//span[contains(@id,'complete')])["+str(i)+"]"
#                         xpath4="(//*[@id='serverTable']/tbody/tr/td[1]//span[contains(@id,'complete')])["+str(i)+"]"
                     
                        try:
                            serverState = self.browserObject.find_element_by_xpath(xpath).is_displayed()
                            servicedeploymentStatus = "Deployment Failed"
                        except:
                            utility.execLog("*****************************")
                            try:
                                serverState = self.browserObject.find_element_by_xpath(xpath1).is_displayed()
                                servicedeploymentStatus = "Deploying"
                            except:
                                utility.execLog("=========================")
                                try:
                                    serverState = self.browserObject.find_element_by_xpath(xpath2).is_displayed()
                                    servicedeploymentStatus = "Pending Updates"     
                                except:
                                    utility.execLog("#############################")
                                    try:
                                        serverState = self.browserObject.find_element_by_xpath(xpath3).is_displayed()
                                        servicedeploymentStatus = "In Use"     
                                    except:
                                        utility.execLog("----------------------------")
                        if serverState:
                            utility.execLog(servicedeploymentStatus)
                            time.sleep(5)
                            pageObject=Resources.Resources(self.browserObject)
                            pageObject.loadPage()
                            time.sleep(15)                        
                            utility.execLog("Selected Servers in Resources page")
                            globalVars.browserObject, status, result=pageObject.serverDeploymentStatus("Servers",serverip,servicedeploymentStatus)
                            utility.execLog(result)
                            utility.execLog(status)
  
                        else:
                            utility.execLog("Element not present")
                            return self.browserObject, False, "Test case failed"
                        
                        self.loadPage()
                        time.sleep(10)
                        i=i+1
                        try:
                            self.browserObject, status, result = self.selectService(serviceName)
                            time.sleep(5)
                            self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "View Details")), action="CLICK")
                            time.sleep(10)    
                            utility.execLog("Successfuly selected service")
                        except:
                            utility.execLog("Service is not selected")
                        
                    return self.browserObject, True,listIP
                except Exception as se:
                    utility.execLog("Services Page reloaded '%s'"%(str(se) + format_exc()))
                    loopCount = loopCount - 1
            return self.browserObject, False, "Attempted 3 times and Failed to Click on 'View Details' of Service '%s'"%serviceName
        except Exception as e:
            utility.execLog("Exception while trying to click on 'View Details' of Service '%s' :: Error -> %s"%(serviceName,str(e) + format_exc()))
            raise e
    
    def serverIPListInService(self,serviceName):
        """
        Export all the the service to a file
        """
        try:
            utility.execLog("Selecting service name")
            time.sleep(5)
            try:
                self.browserObject, status, result = self.selectService(serviceName)
                time.sleep(5)
                if not status:
                    return self.browserObject, False, result
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "View Details")), action="CLICK")
                time.sleep(10)
                serveripListElements=self.browserObject.find_elements_by_xpath("//*[@id='serverTable']/tbody/tr/td[4]/a/span")
                listIP = []
                for serverIPElmnt in serveripListElements:
                    listIP.append(serverIPElmnt.text)
                return self.browserObject,True,listIP
            except:
                utility.execLog("Service is not selected")
             
        except Exception as e:
            utility.execLog("Exception while trying to download the export file for All services")
            raise e
        
    def deleteServiceAfterScaleDown(self,serviceName):
        """
        Delete the service .
        """
        try:
            utility.execLog("Selecting service name")
            time.sleep(5)
            try:
                self.browserObject, status, result = self.selectService(serviceName)
                time.sleep(5)
                if not status:
                    utility.execLog("There is no service available")
                    return self.browserObject, True, result
                
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "View Details")), action="CLICK")
                time.sleep(10)
                count = 30
                while count > 0:
                    if self.browserObject.find_element_by_xpath("//*[contains(@class,'btn btn-default dropdown-toggle')]").is_displayed():
                        utility.execLog("Clicking on Delete Service Option")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "lnkDeleteService")), action="CLICK")                    
                        utility.execLog("Checking for Delete Service Page")
                        time.sleep(3)
                        pageTitle = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_delete_service")), action="GET_TEXT")
                        if "Delete" not in pageTitle:
                            utility.execLog("Failed to verify Delete Service Page :: Actual: %s, Expected: %s"%(pageTitle, "Delete"))
                            return self.browserObject, False, "Failed to verify Delete Service Page :: Actual: %s, Expected: %s"%(pageTitle, "Delete")
                        utility.execLog("Verified Delete Service Page :: Actual: %s, Expected: %s"%(pageTitle, "Delete"))            
                        utility.execLog("Clicking on 'Delete' on Delete Service Page")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_deleteservice")), action="CLICK")
                        utility.execLog("Identifying Confirm Dialog box")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "confirm_modal_form")), action="CLICK")
                        utility.execLog("Confirming to Delete Service '%s'"%serviceName)
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                        utility.execLog("Services deleted but wait it is in progress state")
                        break
                    else:
                        count = count - 1
                        time.sleep(120)
                        continue 
                
                count1 = 30
                while count1 > 0:
                    time.sleep(5)
                    self.loadPage()
                    time.sleep(7)
                    utility.execLog("Selecteing the service")
                    self.browserObject, status, result = self.selectService(serviceName)
                    utility.execLog("Succussfully selected the service ")
                    time.sleep(3)
                    if status:
                        count1 = count1-1
                        utility.execLog("Service is in progress state wait for some minute counter is:%i"%count1)
                        time.sleep(120)
                        continue 
                    else:
                        utility.execLog("Service is deleted successfully")
                        return self.browserObject, True, "Service is deleted successfully"   
                
            except:
                utility.execLog("Service is not selected")

        except Exception as e:
            utility.execLog("Exception while trying to download the export file for All services")
            raise e
        
    def getDeployedSwitchIP(self, serviceName):
        '''
        Description: Returns the Ip of the Switch In-Use for a particular Service
        '''
        try:
            utility.execLog("View Service %s details"%serviceName)
            self.viewServiceDetails(serviceName)
            utility.execLog("open Port View")
            self.handleEvent(EC.presence_of_element_located((By.ID, "tabPortView")), action="CLICK")
            time.sleep(5)
            utility.execLog("Open switch details")
            xpath= "//*[@class='Ethernet']"
            switchHostDetails=self.browserObject.find_elements_by_xpath(xpath)
            for switchHostDetail in switchHostDetails:
                if switchHostDetail.is_displayed():
                    break
            utility.execLog("Click View Details of %s Switch"%switchHostDetail.text)
            self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action= "CLICK")
            time.sleep(2)
            xpath= "//*[@id='page_portviewpopover']//*[contains(@data-bind,'ipaddress')]"
            switchIpAddress= self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action= "GET_TEXT")
            return self.browserObject, True, switchIpAddress
        except Exception as e:
            return self.browserObject, False, "Unable to read switch Ip address %s"%e
        
    def getServiceFirmwareList(self, serviceName):       
        '''
        Description: Returns list of Firmware catalog available for update at the Service Page for a particular service Resources
        '''
        try:
            utility.execLog("View Service %s details"%serviceName)
            self.viewServiceDetails(serviceName)
            time.sleep(10)
            utility.execLog("Get list of firmware available for update")
            self.browserObject.find_element_by_xpath("//*[@id='referenceFirmwareRepository']//*[contains(text(), 'Change Repository')]").click()
            time.sleep(5)
            managefirmwareChecked = self.browserObject.find_element_by_id("managefirmware").is_selected()
            if not managefirmwareChecked:
                self.browserObject.find_element_by_id("managefirmware").click()
            time.sleep(2)
            select= Select(self.browserObject.find_element_by_id("firmwarepackage"))
            firmwareList=[]
            for option in select.options:
                if option.text != "Select":
                    firmwareList.append(option.text)
            return self.browserObject, True, firmwareList
        except Exception as e:
            return self.browserObject, False, "Error while listing firmware options : %s"%e
    def brownFieldServiceSchanario(self,brownFieldServiceName, brownFieldCompName, targetVirtualMachineServiceTag, dataCenterName, clusterName,storageDiscov=False,serverDiscov=False,serverState=False,storageState=False,addfirmware=False):       
        '''
        Description:This test case covers all messages around creating a new brownfield service by adding an existing service and then verifies the service is correctly discovered
        '''
        try:
            utility.execLog("...........")
            time.sleep(5)
            serverInventory = "Yes"
            storageInventory = "Yes"
            
            xpath = "//*[@id='serviceMenuActions']/li[2]/a"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
            time.sleep(7)
            xpath = "//*[@id='addexistingservice_serviceinformationform']/div[2]/div[2]/input"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=brownFieldServiceName)
            time.sleep(2)
            if addfirmware:
                utility.execLog("Adding Firmware catalog")
                xpath="//*[@id='addexistingservice_serviceinformationform']/div[4]/div[2]/input"
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                time.sleep(3)
                xpath="//*[@id='addexistingservice_serviceinformationform']/div[5]/div[2]/select"
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setIndex=2,selectBy="INDEX")
                time.sleep(3)
                utility.execLog("Successfully added firmware catalog")
                
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
            time.sleep(7)
            xpath = "//*[@id='form_clustersettings_comp']/div/div[2]/input"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=brownFieldCompName)
            time.sleep(2)
            xpath = "//*[@ data-automation-id='asm_guid']"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=targetVirtualMachineServiceTag)
            time.sleep(3)
            xpath = "//*[@ data-automation-id='datacenter']"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=dataCenterName)
            time.sleep(3)
            xpath = "//*[@ data-automation-id='cluster']"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=clusterName)
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
            time.sleep(15)
            utility.execLog("Successfully move in next page")
            
            actions = ActionChains(self.browserObject)
            xpath = "//*[@id='serverTable']/tbody/tr"
            tr = self.browserObject.find_elements_by_xpath(xpath)
            utility.execLog(len(tr))
            for i in xrange(0,len(tr)):
                j=i+1
                xpath = "//*[@id='serverTable']/tbody/tr["+str(j)+"]/td[6]"
                serverInventory = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                utility.execLog("Server Availability :%s"%serverInventory)
                serverInventory = str(serverInventory.strip())
                if serverInventory != "Yes":
                    xpath = "(//*[contains(@id,'server')]/i[contains(@class,'icon-ic-status-warn-core warning')])["+str(j)+"]"
                    element = WebDriverWait(self.browserObject, 10).until(EC.presence_of_element_located((By.XPATH, xpath)))
                    utility.execLog("tooltip")
                    actions.move_to_element_with_offset(element,5,5).click().perform()
                    time.sleep(20)
                    xpath = "//*[contains(@id,'server')]//*[contains(@class,'tooltip-inner')]"
                    toolTipText = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    utility.execLog(toolTipText)
                    actTooltipText = "This component is not currently marked as Managed or Reserved"
                    
                if serverDiscov and serverState:
                    actServerInvt = "No"
                    if (actTooltipText in toolTipText) and (serverInventory == actServerInvt):
                        utility.execLog("Test case passed")
                    else:
                        utility.execLog("Test case failed")
                        return self.browserObject,False, "Test case failed"
                elif serverDiscov:
                    actServerInvt = "Yes"
                    if (serverInventory == actServerInvt):
                        utility.execLog("Test case passed")
                    else:
                        utility.execLog("Test case failed")
                        return self.browserObject,False, "Test case failed"
                
                else:
                    actServerInvt = "No"
                    if ("not in ASM inventory" in toolTipText) and  (actServerInvt == serverInventory):
                        utility.execLog("Test case passed")
                    else:
                        utility.execLog("Test case failed")
                        return self.browserObject,False, "Test case failed"
                
            xpath = "//*[@id='storageTable']/tbody/tr"
            tr = self.browserObject.find_elements_by_xpath(xpath)
            utility.execLog(len(tr))
            j=0
            for i in xrange(0,len(tr)/2):
                j = i*2 + 1
                xpath = "//*[@id='storageTable']/tbody/tr["+str(j)+"]/td[4]"
                storageInventory = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                utility.execLog("Storage Availability :%s"%storageInventory)
                storageInventory = str(storageInventory.strip())
                if storageInventory != "Yes":
                    xpath = "//*[@id='storage_"+str(i)+"']/i"
                    element = WebDriverWait(self.browserObject, 10).until(EC.presence_of_element_located((By.XPATH, xpath)))
                    utility.execLog("tooltip")
                    actions.move_to_element_with_offset(element,5,5).click().perform()
                    time.sleep(20)
                    xpath = "//*[contains(@id,'storage')]//*[contains(@class,'tooltip-inner')]"
                    toolTipText = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    utility.execLog(toolTipText)
                
                if storageDiscov and serverDiscov and (not storageState) and serverState:
                    utility.execLog("storageDiscov and serverDiscov and (not storageState) and serverState")
                    acttoolTipText1 = "The server(s) this component is attached to are not marked as Managed or Reserved"
                    actStorageInvt = "No"
                    if (acttoolTipText1 in toolTipText) and  (actStorageInvt == storageInventory):
                        utility.execLog("Test case passed")
                    else:
                        utility.execLog("Test case failed")
                        return self.browserObject,False, "Test case failed"
                
                elif storageDiscov and storageState:
                    acttoolTipText1 = "The server(s) this component is attached to are not marked as Managed or Reserved"
                    acttoolTipText2 = "This component is not currently marked as Managed or Reserved"
                    actStorageInvt = "No"
                    if ( (acttoolTipText1 in toolTipText) or (acttoolTipText2 in toolTipText)) and  (actStorageInvt == storageInventory):
                        utility.execLog("Test case passed")
                    else:
                        utility.execLog("Test case failed")
                        return self.browserObject,False, "Test case failed"
                
                elif storageDiscov and serverDiscov and (not storageState) and (not serverState):
                    actStorageInvt = "Yes"
                    if (actStorageInvt == storageInventory):
                        utility.execLog("Test case passed")
                    else:
                        utility.execLog("Test case failed")
                        return self.browserObject,False, "Test case failed"
                
                elif storageDiscov:
                    acttoolTipText1 = "The server(s) this component is attached to are not in ASM inventory"
                    actStorageInvt = "No"
                    if (acttoolTipText1 in toolTipText) and  (actStorageInvt == storageInventory):
                        utility.execLog("Test case passed")
                    else:
                        utility.execLog("Test case failed")
                        return self.browserObject,False, "Test case failed"
                else:
                    acttoolTipText1 = "This component, and the server(s) it is attached to, are not in ASM inventory"
                    acttoolTipText2 = "This component is not in ASM inventory"
                    acttoolTipText3 = "This component is not in inventory and the server(s) it's attached to are not marked as Managed or Reserved."
                    actStorageInvt = "No"
                    if ((acttoolTipText1 in toolTipText) or (acttoolTipText2 in toolTipText) or (acttoolTipText3 in toolTipText)) and  (actStorageInvt == storageInventory):
                        utility.execLog("Test case passed")
                    else:
                        utility.execLog("Test case failed")
                        return self.browserObject,False, "Test case failed"    
            if serverInventory=="Yes" and storageInventory == "Yes":
                utility.execLog("Deploying brownfield service...")
                self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
                time.sleep(5)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                time.sleep(10)
                if addfirmware:
                    self.viewServiceDetails(brownFieldServiceName)
                    time.sleep(7)
                    utility.execLog("Verifying firmware catalog on Service")
                    xpath="//*[@id='referenceFirmwareRepository']/li[2]/span"
                    firmwareText=self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="GET_TEXT")
                    time.sleep(3)
                    utility.execLog("Firmware catalog :%s"%firmwareText)
                    if(firmwareText != ""):
                        utility.execLog("Firmware catalog verified successfully:%s"%firmwareText)
                        return self.browserObject, True, "Brownfield done successfully"
                    else:
                        utility.execLog("Firmware catalog not verified successfully")
                        return self.browserObject, False, "Brownfield not done"
                return self.browserObject, True, "Brownfield done successfully" 
            else:
                self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Cancel")), action="CLICK")
                time.sleep(5)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
                time.sleep(7)     
                return self.browserObject, True, "Test case passed"
        except Exception as e:
            return self.browserObject, False, "Error while doing brownfield : %s"%e
    
    def validateOSIP(self, serviceName,networks):       
        '''
        Description: Verifying OS IP at the Service Page for a particular service
        '''
        try:
            serverOSIPList = []
            starthyperMgntStartIPList = []
            endHyperMgntStartIPList = []
            for network in networks:
                utility.execLog(network)
                boolean=int(network["VLAN ID"]) == 37
                if boolean:
                    startIPHyperManagement=network["Starting IP Address"]
                    starthyperMgntStartIPList=startIPHyperManagement.split("\n")
                    utility.execLog(len(starthyperMgntStartIPList))
                    
                    endIPHyperManagement=network["Ending IP Address"]
                    endHyperMgntStartIPList=endIPHyperManagement.split("\n")
                    utility.execLog(len(endHyperMgntStartIPList))
                    break
            for i in xrange(0,len(starthyperMgntStartIPList))  :
                utility.execLog("....")
                strtIP=starthyperMgntStartIPList[i]
                endIP=endHyperMgntStartIPList[i]
                
                stip=strtIP
                vstip='.'.join([s for s in stip.split('.')[:-1]]) 
                eip=endIP
                l1= [ip for ip in range(int(stip.split('.')[-1]), int(eip.split('.')[-1])+1)] 
                l2= [vstip+'.'+ str(ip) for ip in l1]
                serverOSIPList += l2
                utility.execLog(serverOSIPList)
                
            VMOSIPList = []
            startLanIPList = []
            endIPListPublicLan = []
            for network in networks:
                utility.execLog(network)
                boolean=((network["Network Type"] == "Public LAN") or (network["Network Type"] == "Private LAN") ) and  (network["IP Address Setting"]== "Static")
                if boolean:
                    startIPLAN=network["Starting IP Address"]
                    startLanIPList=startIPLAN.split("\n")
                    utility.execLog(len(startLanIPList))
                    
                    endIPLAN=network["Ending IP Address"]
                    endIPListPublicLan=endIPLAN.split("\n")
                    utility.execLog(len(endIPListPublicLan))
                    
                    for i in xrange(0,len(startLanIPList)) :
                        utility.execLog("....")
                        strtIP=startLanIPList[i]
                        endIP=endIPListPublicLan[i]
                        stip=strtIP
                        vstip='.'.join([s for s in stip.split('.')[:-1]]) 
                        eip=endIP
                        l1= [ip for ip in range(int(stip.split('.')[-1]), int(eip.split('.')[-1])+1)] 
                        l2= [vstip+'.'+ str(ip) for ip in l1]
                        VMOSIPList += l2
                        utility.execLog(VMOSIPList)
            utility.execLog(VMOSIPList)
            utility.execLog("Selecting service name")
            time.sleep(5)
            self.browserObject, status, result = self.selectService(serviceName)
            time.sleep(5)
            if not status:
                utility.execLog("There is no service available")
                return self.browserObject, True, result
                
            self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "View Details")), action="CLICK")
            time.sleep(10)
            utility.execLog("View Service %s details"%serviceName)
            try:
                if self.browserObject.find_element_by_xpath("//*[@id='serverTable']/tbody/tr/td[5]/ul/li/a[contains(@href,'https://172.31.37')]/span").is_displayed():
                    utility.execLog("Server os Ip...")
                    elements = self.browserObject.find_elements_by_xpath("//*[@id='serverTable']/tbody/tr/td[5]/ul/li/a[contains(@href,'https://172.31.37')]/span") 
                    serOSIPLIST=[str(element.text) for element in elements]
                    utility.execLog(serOSIPLIST) 
#                     verifyOSIp=set(serOSIPLIST).issubset(serverOSIPList)
                    for osIp in serOSIPLIST:
                        if osIp in serverOSIPList :
                            verifyOSIp=True
                        else:
                            verifyOSIp=False
                            break
                    if verifyOSIp:
                        utility.execLog("Successfully verified OS IP of service")
                    else:
                        return self.browserObject, False, "OS IP of server not validated"
            except:
                try:
                    
                    if self.browserObject.find_element_by_xpath("//*[@id='serverTable']/tbody/tr/td[5]/ul/li/a[contains(@href,'https://172.31')]/span").is_displayed():
                        utility.execLog("Server os Ip...")
                        elements = self.browserObject.find_elements_by_xpath("//*[@id='serverTable']/tbody/tr/td[5]/ul/li/a[contains(@href,'https://172.31')]/span") 
                        serOSIPLIST=[str(element.text) for element in elements]
                        utility.execLog(serOSIPLIST) 
#                     verifyOSIp=set(serOSIPLIST).issubset(serverOSIPList)
                        for osIp in serOSIPLIST:
                            if osIp in VMOSIPList :
                                verifyOSIp=True
                            else:
                                verifyOSIp=False
                                break
                            if verifyOSIp:
                                utility.execLog("Successfully verified OS IP of service in Baremetal")
                            else:
                                return self.browserObject, False, "OS IP of server not validated"
                except:
                    utility.execLog("It is a Baremetal service and selected workload  in network or Server component is not added.So we can not validate Server OS ip.")
                
            try:
                if self.browserObject.find_element_by_xpath("//*[@id='vmTable']/tbody/tr/td[4]/ul/li/a/span").is_displayed():
                    utility.execLog("VM os Ip...")
                    elements = self.browserObject.find_elements_by_xpath("//*[@id='vmTable']/tbody/tr/td[4]/ul/li/a/span") 
                    vmOSIp=[str(element.text) for element in elements]
                    utility.execLog(vmOSIp) 
#                     verifyOSIp=set(vmOSIp).issubset(VMOSIPList)
                    for osIp in vmOSIp:
                        if osIp in VMOSIPList :
                            verifyOSIp=True
                        else:
                            verifyOSIp=False
                            break
                    if verifyOSIp:
                        utility.execLog("Successfully verified OS IP of service at VM")
                    else:
                        return self.browserObject, False, "OS IP of VM not validated"
            except:
                try:
                    if self.browserObject.find_element_by_xpath("//*[@id='btnVmOSIPtoggle']/i[1]").is_displayed():
                        self.browserObject.find_element_by_xpath("//*[@id='btnVmOSIPtoggle']/i[1]").click()
                        time.sleep(2)
                        utility.execLog("For chevron")
                        utility.execLog("VM os Ip...")
                        elements = self.browserObject.find_elements_by_xpath("//*[@id='vmTable']/tbody/tr/td[4]/div/ul/li/a/span") 
                        vmOSIp=[str(element.text) for element in elements]
                        utility.execLog(vmOSIp) 
#                     verifyOSIp=set(vmOSIp).issubset(VMOSIPList)
                        for osIp in vmOSIp:
                            if osIp in VMOSIPList :
                                verifyOSIp=True
                            else:
                                verifyOSIp=False
                                break
                            if verifyOSIp:
                                utility.execLog("Successfully verified OS IP of service at VM:%s"%osIp)
                            else:
                                return self.browserObject, False, "OS IP of VM not validated"
                except:
                    utility.execLog("VM component is not added.So we can not validate VM OS ip.")
            return self.browserObject, True,"Successfully verified OS IP of Service"
        except Exception as e:
            return self.browserObject, False, "Error while verifying OS IP for the service: %s"%e