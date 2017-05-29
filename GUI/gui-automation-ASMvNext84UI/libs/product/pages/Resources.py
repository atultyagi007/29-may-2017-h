"""
Author: P Suman/Saikumar Kalyankrishnan/HCL Team
Created/Modified: Oct 7th 2015/Mar 8th 2017
Description: Functions/Operations related to Resources Page
"""

from CommonImports import *
from libs.product.objects.Resources import ResourceObjectRepo
from libs.product.pages import Login
from traceback import format_exc
from test.test_os import resource
from unicodedata import category
from tests import resources
from lib2to3.fixer_util import Attr

class Resources(Navigation, ResourceObjectRepo):
    """
    Description:
        Class which includes Functions/Operations related to Resources Management
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Resources class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Resources"
        self.browserObject = browserObject
        self.resourceTab = self.ResourcesObjects("all_resources_tab")
        self.serverPoolTab = self.ResourcesObjects("server_pools_tab")
        self.resourceTable = self.ResourcesObjects("resource_table")
        self.serverPoolTable = self.ResourcesObjects("server_pool_table")
        self.serverTable = self.ResourcesObjects("server_table")
        self.userTable = self.ResourcesObjects("users_table")
        self.loopCount = 5
        utility.execLog("Resources")

    def loadPage(self):
        """
        Description:
            API to Load Resources Page
        """
        try:
            utility.execLog("Loading Resources Page...")
            self.browserObject, status, result =  self.selectOption("Resources")
            time.sleep(5)
            return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Resources Page :: Error -> %s" % (str(e) + format_exc())

    def validatePageTitle(self, title=None):
        """
        Description:
            API to validate Resources Page
        """
        if not title:
            title = self.pageTitle
        getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects('title'))), action="GET_TEXT")
        if title not in getCurrentTitle:
            utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
            return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
        else:
            utility.execLog("Successfully validated Page Title: '%s'" % title)
            return self.browserObject, True, "Successfully validated Page Title: '%s'" % title
    
    def getOptions(self, option="Resources"):
        """
        Description:
            API to get Options and their Accessibility for Resources Page 
        """
        optionList = {}
        chkLink = ""
        options = []
        expanderStatus = False
        try:
            if option == "Server Pools":
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.serverPoolTab)), action="CLICK")
                xpath = self.ResourcesObjects("generic_row_count")%({"tid":self.serverPoolTable})
                chkLink = self.ResourcesObjects("select_first_pool")
            else:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
                xpath = self.ResourcesObjects("generic_row_count")%({"tid":self.resourceTable})
                chkLink = self.ResourcesObjects("select_first_device")
            resourceSelected = False
            utility.execLog("Scrolling to top of the window")
            self.browserObject.execute_script("window.scrollTo(0, 0);")
            totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath)), action="GET_ELEMENTS_BY_XPATH"))
            utility.execLog("Total Number of Rows : %s"%totalRows)
            if totalRows > 0:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, chkLink)), action="CLICK")
                utility.execLog("Selected first Resource")
                resourceSelected = True
            if option == "Resources":
                utility.execLog("Checking if Resource Options are collapsed")
                expanderStatus = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("export_all_option"))), action="IS_DISPLAYED")            
                if not expanderStatus:
                    utility.execLog("Resource options are collapsed....expanding to view options")
                    self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("options_expander"))), action="CLICK")
                utility.execLog("Expanded Resource options to View")
            if resourceSelected:
                if option == "Server Pools":
                    options = [self.ResourcesObjects("create_new_server_pool"), self.ResourcesObjects("edit_server_pool"), 
                               self.ResourcesObjects("delete_server_pool")]
                else:                   
                    if expanderStatus: 
                        options = [self.ResourcesObjects("discover_device"), self.ResourcesObjects("remove_device"),
                                   self.ResourcesObjects("update_resource"), self.ResourcesObjects("chassis_config"),
                                   self.ResourcesObjects("change_resource_state"), self.ResourcesObjects("export_all")]
                    else:
                        options = [self.ResourcesObjects("discover_device"), self.ResourcesObjects("remove_device"),
                                   self.ResourcesObjects("update_resource"), self.ResourcesObjects("chassis_config"),
                                   self.ResourcesObjects("select_managed_state"), self.ResourcesObjects("export_all")]
            else:
                if option == "Server Pools":
                    options = [self.ResourcesObjects("create_new_server_pool")]
                else:
                    options = [self.ResourcesObjects("discover_device"), self.ResourcesObjects("export_all")]
            utility.execLog("Verifying Options :: %s"%options)
            for opt in options:
                if option == "Server Pools":
                    status = self.handleEvent(EC.presence_of_element_located((By.ID, opt)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled") 
                    optName = self.handleEvent(EC.presence_of_element_located((By.ID, opt)), action="GET_TEXT")
                else:
                    status = self.handleEvent(EC.presence_of_element_located((By.XPATH, opt)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled") 
                    optName = self.handleEvent(EC.presence_of_element_located((By.XPATH, opt)), action="GET_TEXT")
                if status == "true":
                    optionList[optName] = "Disabled"
                else:
                    optionList[optName] = "Enabled"
                utility.execLog("Able to fetch Option '%s' Info , Status '%s'"%(optName, status))
            utility.execLog("Able to fetch Options :: %s"%str(optionList))
            if resourceSelected:
                return self.browserObject, True, optionList
            else:
                return self.browserObject, False, "There should be atleast one Resource/Server Pool to verify some Options :: Available Options '%s'"%str(optionList) 
        except Exception as e:
            return self.browserObject, False, "Unable to read Options on %s Page :: Error -> %s"%(option, str(e) + format_exc())
        
    def getResources(self, resourceType, health='All', serviceName=None):
        """
        Description:
            API to get existing Resources
        """
        resourceList = []        
        try:            
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selecting Resource Type '%s'"%resourceType)
            if resourceType != "All":
                try:
                    showFilterDialog = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("device_filter_dialog"))), action="IS_DISPLAYED")
                    if not showFilterDialog:
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("show_filter_button"))), action="CLICK")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("resource_type_filter"))), action="SELECT", selectBy="VALUE", setValue=resourceType.lower())
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.ID,  self.ResourcesObjects("resource_type_filter"))), action="SELECT", setValue=resourceType)
            else:
                self.browserObject.refresh()
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("discover_device"))))
            utility.execLog("Able to Select Resource Type '%s'....Selecting Health '%s'"%(resourceType, health)) 
            try:
                showFilterDialog = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("device_filter_dialog"))), action="IS_DISPLAYED")
                if not showFilterDialog:
                    self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("show_filter_button"))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("health_filter"))), action="SELECT", selectBy="VALUE", setValue=health.lower())
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID,  self.ResourcesObjects("health_filter"))), action="SELECT", setValue=health)
            utility.execLog("Able to Select Health Type '%s'....Reading '%s' Table"%(health, self.resourceTable))
            if serviceName:
                try:
                    showFilterDialog = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("device_filter_dialog"))), action="IS_DISPLAYED")
                    if not showFilterDialog:
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("show_filter_button"))), action="CLICK")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("service_filter"))), action="SELECT", selectBy="VALUE", setValue=serviceName.lower())
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.ID,  self.ResourcesObjects("service_filter"))), action="SELECT", setValue=serviceName)
                utility.execLog("Able to Select Service Name '%s'"%health)
            utility.execLog("Reading '%s' Table"%self.resourceTable)            
            try:
                utility.execLog("Moving to First Page")
                pageIndex = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("current_page"))), action="GET_ATTRIBUTE_VALUE", attributeName="value") 
                if pageIndex== "" or pageIndex == "1":
                    utility.execLog("Already on First Page")
                else:
                    utility.execLog("Resource List exceeds one page so moving to First Page")
                    self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("first_page"))), action="CLICK")
            except:
                pass
            try:
                lastPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("total_pages"))), action="GET_TEXT")
                utility.execLog("Last Page Text :: '%s'"%lastPage)                 
                if lastPage == '': 
                    lastPage = '1'
                else:
                    lastPage = lastPage.rsplit(" ", 1)[1]
            except Exception as e:
                utility.execLog("Exception while fetching Last Page :: %s"%str(e))
                lastPage = 1
            lastPage = int(lastPage)
            utility.execLog("Total No. of Pages: %s"%str(lastPage))
            for page in range(1, lastPage+1):
                if lastPage > 1:
                    try:
                        currentPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("current_page"))), action="GET_ATTRIBUTE_VALUE", attributeName="value")
                    except:
                        currentPage = 1
                    if currentPage != str(page):
                        utility.execLog("Moving to Page: %s"%str(page))
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("next_page"))), action="CLICK")
                self.loopCount = 5
                resList = self.readResourceTable()
                resourceList.extend(resList)
            utility.execLog("Total Resources :: %s"%str(len(resourceList)))
            return self.browserObject, True, resourceList      
        except Exception as e:
            return self.browserObject, False, "Unable to read Resources :: Error -> %s"%(str(e) + format_exc())
    
    def readResourceTable(self):
        """
        Reads Device Table
        """
        resList = []
        try:
            if self.loopCount > 0:
                totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("device_table_header")))))
                utility.execLog("Total Number of Columns : %s"%str(totalColumns))
                tableColumns = []
                for col in range(2, totalColumns + 1):
                    xpath = self.ResourcesObjects("device_table_header_col")%({"hid":col})
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableColumns.append(colName)
                        utility.execLog("Able to fetch Column Name: '%s'"%colName)
                tableColumns = [x for x in tableColumns if x !='']
                utility.execLog("Able to fetch %s Table Columns '%s'"%(self.resourceTable, str(tableColumns)))
                totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("device_count")))))
                utility.execLog("Total Number of Rows : %s"%str(totalRows))     
                for row in range(1, totalRows + 1):
                    tableElements = []
                    for col in range(2, totalColumns + 1):
                        xpath = self.ResourcesObjects("device_element")%({"rid":row, "cid":col})
                        if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                            colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                            tableElements.append(colValue)
                    tempDict = dict(zip(tableColumns, tableElements))
                    utility.execLog("Able to fetch %s Info '%s'"%(self.resourceTable, str(tempDict)))
                    resList.append(tempDict)
                return resList
            else:
                utility.execLog("Maximum retries exceeded for reading Resources Table :: Retries ('%s')"%str(self.loopCount))
                raise "Maximum retries exceeded for reading Resources Table :: Retries ('%s')"%str(self.loopCount)
        except StaleElementReferenceException as se:
            utility.execLog("Resource Page reloaded '%s'"%str(se) + format_exc())
            self.loopCount = self.loopCount - 1
            self.readResourceTable()
        except Exception as e:
            utility.execLog("Unable to read Resources Table :: Error -> %s"%str(e) + format_exc())
            raise e
        finally:
            return resList
    
    def getServerPools(self):
        """
        Description:
            API to get existing Server Pools
        """
        serverPoolList = []
        try:
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.serverPoolTab)), action="CLICK")
            utility.execLog("Reading %s Table"%self.serverPoolTable)
            self.loopCount = 5
            serverPoolList = self.readServerPoolTable()
            return self.browserObject, True, serverPoolList      
        except Exception as e:
            return self.browserObject, False, "Unable to read Server Pools :: Error -> %s"%(str(e) + format_exc())
    
    def readServerPoolTable(self):
        """
        Reads Server Pool Table
        """
        poolList = []
        try:
            if self.loopCount > 0:
                totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("generic_table_header")%({"tid":self.serverPoolTable})))))
                utility.execLog("Total Number of Columns : %s"%str(totalColumns))
                tableColumns = []
                for col in range(2, totalColumns + 1):
                    xpath = self.ResourcesObjects("generic_table_header_col")%({"tid":self.serverPoolTable, "hid":col})
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
                tableColumns = [x for x in tableColumns if x !='']
                tableColumns.append("ServerInfo")
                tableColumns.append("UserInfo")
                utility.execLog("Able to fetch %s Table Columns '%s'"%(self.serverPoolTable, str(tableColumns)))
                totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("generic_row_count")%({"tid":self.serverPoolTable})))))
                utility.execLog("Total Number of Rows : %s"%str(totalRows))                
                for row in range(1, totalRows + 1):
                    tableElements = []
                    for col in range(2, totalColumns + 1):
                        xpath = self.ResourcesObjects("generic_table_element")%({"tid":self.serverPoolTable, "rid":row, "cid":col})
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                    xpath = self.ResourcesObjects("select_server_pool_by_row")%({"rid":row}) 
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    utility.execLog("Reading Servers Information in Server Pool '%s'"%tableElements[1])
                    self.browserObject, status, result = self.getServersInServerPool(row)
                    tableElements.append(result)
                    utility.execLog("Reading Users Information in Server Pool '%s'"%tableElements[1])
                    self.browserObject, status, result = self.getUsersOfServerPool(row)
                    tableElements.append(result)
                    userDict = dict(zip(tableColumns, tableElements))
                    utility.execLog("Able to fetch %s Info '%s'"%(self.serverPoolTable, str(userDict)))
                    poolList.append(userDict)
            else:
                utility.execLog("Maximum retries exceeded for reading Server Pools Table :: Retries ('%s')"%str(self.loopCount))
                raise "Maximum retries exceeded for reading Server Pools Table :: Retries ('%s')"%str(self.loopCount)
        except StaleElementReferenceException as se:
            utility.execLog("Resource Page reloaded '%s'"%str(se) + format_exc())
            self.loopCount = self.loopCount - 1
            self.readServerPoolTable()
        except Exception as e:
            utility.execLog("Unable to read Resources :: Error -> %s"%str(e) + format_exc())
            raise e
        finally:
            return poolList
    
    def getServersInServerPool(self, row):
        """
        Get Servers Information in Server Pool
        """
        detailList = []
        try:           
            #self.handleEvent(EC.presence_of_element_located((By.ID, "serverpools")), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, self.ResourcesObjects("server_pool_servers_tab"))), action="CLICK")
            utility.execLog("Reading %s Table"%self.serverTable)
            try:
                lastPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("total_pages"))), action="GET_TEXT")                 
                if lastPage == '': 
                    lastPage = '1'
                else:
                    lastPage = lastPage.rsplit(" ", 1)[1]
            except:
                lastPage = 1
            lastPage = int(lastPage)
            utility.execLog("Total No. of Pages: %s"%str(lastPage))
            xpath = self.ResourcesObjects("select_server_pool_by_row")%({"rid":row}) 
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
            for page in range(1, lastPage+1):
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, self.ResourcesObjects("server_pool_servers_tab"))), action="CLICK")
                if lastPage > 1:
                    try:
                        currentPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("current_page"))), action="GET_ATTRIBUTE_VALUE", attributeName="value")
                    except:
                        currentPage = 1
                    if currentPage != str(page):
                        utility.execLog("Moving to Page: %s"%str(page))
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("next_page"))), action="CLICK")
                totalColumns = totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("generic_table_header")%{"tid":self.serverTable}))))
                utility.execLog("Total Number of Columns : %s"%str(totalColumns))
                tableColumns = []
                for col in range(2, totalColumns + 1):
                    xpath = self.ResourcesObjects("generic_table_header_col")%{"tid":self.serverTable, "hid":col}
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
                tableColumns = [x for x in tableColumns if x !='']
                utility.execLog("Able to fetch %s Table Columns '%s'"%(self.serverTable, str(tableColumns)))
                try:
                    totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                            self.ResourcesObjects("generic_row_count")%{"tid":self.serverTable}))))
                except:
                    utility.execLog("No Servers found in Server Pool")
                    totalRows = 0
                utility.execLog("Total Number of Rows : %s"%str(totalRows))
                if totalRows == 1:
                    cols = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.ResourcesObjects("generic_table_header_cols")%{"tid":self.serverTable}))))
                    if cols == 1:
                        totalRows = 0                
                for row in range(1, totalRows + 1):
                    tableElements = []
                    for col in range(2, totalColumns):
                        xpath = self.ResourcesObjects("generic_table_element")%({"tid":self.serverTable, "rid":row, "cid":col})
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                    tempDict = dict(zip(tableColumns, tableElements))
                    utility.execLog("Able to fetch %s Info '%s'"%(self.serverTable, str(tempDict)))
                    detailList.append(tempDict)
            return self.browserObject, True, detailList      
        except Exception as e:
            utility.execLog("Unable to read Servers Information :: Error -> %s"%str(e) + format_exc())
            raise e
    
    def getUsersOfServerPool(self, row):
        """
        Get Users Information related to Server Pool
        """
        detailList = []
        try:            
            #self.handleEvent(EC.element_to_be_clickable((By.ID, "page_serverpools")), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, self.ResourcesObjects("server_pool_users_tab"))), action="CLICK")
            utility.execLog("Reading %s Table"%self.serverTable)
            try:
                lastPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("total_pages"))), action="GET_TEXT")                 
                if lastPage == '': 
                    lastPage = '1'
                else:
                    lastPage = lastPage.rsplit(" ", 1)[1]
            except:
                lastPage = 1
            lastPage = int(lastPage)
            utility.execLog("Total No. of Pages: %s"%str(lastPage))
            xpath = self.ResourcesObjects("select_server_pool_by_row")%({"rid":row}) 
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
            for page in range(1, lastPage+1):
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, self.ResourcesObjects("server_pool_users_tab"))), action="CLICK")
                if lastPage > 1:
                    try:
                        currentPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("current_page"))), action="GET_ATTRIBUTE_VALUE", attributeName="value")
                    except:
                        currentPage = 1
                    if currentPage != str(page):
                        utility.execLog("Moving to Page: %s"%str(page))
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("next_page"))), action="CLICK")
                totalColumns = totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("generic_table_header")%({"tid":self.userTable})))))
                utility.execLog("Total Number of Columns : %s"%str(totalColumns))
                tableColumns = []
                for col in range(2, totalColumns + 1):
                    xpath = self.ResourcesObjects("generic_table_header_col")%({"tid":self.userTable, "hid":col})
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
                tableColumns = [x for x in tableColumns if x !='']
                utility.execLog("Able to fetch %s Table Columns '%s'"%(self.userTable, str(tableColumns)))
                try:
                    totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("generic_row_count")%({"tid":self.userTable})))))
                except:
                    utility.execLog("No Users found for Server Pool")
                    totalRows = 0
                utility.execLog("Total Number of Rows : %s"%str(totalRows))
                if totalRows == 1:
                    cols = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.ResourcesObjects("generic_table_header_cols")%({"tid":self.userTable})))))
                    if cols == 1:
                        totalRows = 0                
                for row in range(1, totalRows + 1):
                    tableElements = []
                    for col in range(2, totalColumns):
                        xpath = self.ResourcesObjects("generic_table_element")%({"tid":self.userTable, "rid":row, "cid":col})
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                    tempDict = dict(zip(tableColumns, tableElements))
                    utility.execLog("Able to fetch %s Info '%s'"%(self.userTable, str(tempDict)))
                    detailList.append(tempDict)
            return self.browserObject, True, detailList
        except Exception as e:
            utility.execLog("Unable to read Resources :: Error -> %s"%str(e) + format_exc())
            raise e
    
    def selectResource(self, resourceID, select=True):
        """
        Select specified Resource
        """
        try:
            utility.execLog("Reading Resources Table")
            notFound = True
            page=1
            try:
                utility.execLog("Waiting to identify Total pages")
                self.handleEvent(EC.text_to_be_present_in_element((By.XPATH, self.ResourcesObjects("total_pages")), "Page"))
                utility.execLog("Able to identify Total pages")
                lastPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("total_pages"))), action="GET_TEXT")                    
                utility.execLog("Last Page Text :: '%s'"%lastPage)
                if lastPage == '': 
                    lastPage = '1'
                else:
                    lastPage = lastPage.rsplit(" ", 1)[1]
            except Exception as e:
                utility.execLog("Exception while fetching Last Page :: %s"%str(e))
                lastPage = 1
            lastPage = int(lastPage)
            utility.execLog("Total No. of Pages: %s"%str(lastPage))
            if lastPage > 1:
                try:
                    utility.execLog("More than one page so moving to first page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("first_page"))), action="CLICK")
                    time.sleep(5)
                    utility.execLog("Moved to first page")
                except:
                    utility.execLog("Failed to move to first page")
            while page <= lastPage:
                try:
                    self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("search_device_table")%({"resId":resourceID}))), action="IS_DISPLAYED")
                    notFound = False
                except:
                    utility.execLog("Unable to find Resource '%s' in page '%s'"%(resourceID, page))
                if notFound:
                    if page==lastPage:
                        return self.browserObject, False, "Failed to find Resource :: {}, retries exhausted".format(resourceID)
                    else:
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("next_page"))), action="CLICK")
                        page=page+1
                else:
                    utility.execLog("Verified Resources Table contains {}".format(resourceID))
                    break
            utility.execLog("Selecting Resource '%s'"%resourceID)
            if select:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_device_by_info")%({"tid":self.resourceTable, "resInfo":resourceID}))), action="CLICK")
                utility.execLog("Able to Select Resource '%s'" % (resourceID))
            else:                
                globalVars.deviceId = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("get_device_id_search_by_info")%({"resInfo":resourceID}))), action="GET_TEXT")
                if globalVars.deviceId == "":
                    self.failure("Failed to find resource {}!".format(resourceID), raiseExc=True)
            return self.browserObject, True, "Able to Select Resource '%s'" % (resourceID)
        except Exception as e:
            utility.execLog("Unable to select Resource '%s' :: Error -> %s" %(resourceID, str(e) + format_exc()))
            return self.browserObject, False, "Unable to select Resource '%s' :: Error -> %s" %(resourceID, str(e) + format_exc())
    
    def deleteResource(self, resourceName):
        """
        Deletes existing Resource
        """
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            self.browserObject, status, result = self.selectResource(resourceName)
            if not status:
                return self.browserObject, False, result
            utility.execLog("Successfully selected Resources '%s'....Checking if Resource Options are collapsed"%resourceName)
            utility.execLog("Scrolling to top of the window")
            self.browserObject.execute_script("window.scrollTo(0, 0);")
            expanderStatus = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("export_all_option"))), action="IS_DISPLAYED")
            if not expanderStatus:
                utility.execLog("Resource options are collapsed....expanding to view options")
                self.browserObject.execute_script("return arguments[0].scrollIntoView();", self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab))))
                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("options_expander"))), action="CLICK")
            utility.execLog("Clicking on 'Remove' to delete Resource")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("remove_device") )), action="CLICK")
            utility.execLog("Clicked on 'Remove' to delete Resource")
            time.sleep(5)
            try:
                self.handleEvent(EC.title_contains("Confirm"))
                utility.execLog("Confirm Box Loaded...Confirming to Delete Resource: '%s'" % resourceName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            except:
                utility.execLog("Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Confirm"))
                return self.browserObject, False, "Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Confirm")
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                return self.browserObject, False, "Failed to Delete Resource :: '%s' :: Error -> '%s'"%(resourceName, 
                                                    str(errorMessage))
            except:
                return self.browserObject, True, "Successfully Deleted Resource '%s'"%resourceName           
        except Exception as e:
            return self.browserObject, False, "Failed to Delete Resource :: '%s' :: Error -> %s"%(resourceName, 
                                                    str(e) + format_exc())
    
    def findResource(self, resourceToFind, select=False):
        """
        Finds existing Resource
        """
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            self.browserObject, status, result = self.selectResource(resourceToFind, select)
            if not status:
                return self.browserObject, False, result
            return self.browserObject, True, "Successfully Found Resource {}".format(resourceToFind)          
        except Exception as e:
            return self.browserObject, False, "Failed to Find Resource :: {} :: Error -> {}".format(resourceToFind, e)

    def runInventory(self, resourceList, waitForCompletion=False):
        """
        Run Inventory on existing Resource(s)
        """
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            for resourceName in resourceList:      
                self.browserObject, status, result = self.selectResource(resourceName)
                if not status:
                    return self.browserObject, False, result
            utility.execLog("Scrolling to top of the window")
            self.browserObject.execute_script("window.scrollTo(0, 0);")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("run_inventory"))), action="CLICK")
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                return self.browserObject, False, "Failed to Run Inventory :: '%s' :: Error -> '%s'"%(resourceList, 
                                                    str(errorMessage))
            except:
                try:
                    self.handleEvent(EC.title_contains("Alert"))
                    utility.execLog("Alert Box Loaded...Closing Alert")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('AlertClose'))), action="CLICK")                    
                    if waitForCompletion:
                        self.browserObject, status, result = self.waitJobComplete("Inventory")
                        if status:
                            utility.execLog("Inventory update completed")
                            return self.browserObject, True, "Inventory update completed"
                        else:
                            return self.browserObject, status, result
                    return self.browserObject, True, "Able to Run Inventory on Resource(s) :: '%s'"%(resourceList)
                except:
                    utility.execLog("Failed to Verify Alert Box :: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Alert"))
                    return self.browserObject, False, "Failed to Verify Alert Box :: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Alert")
        except Exception as e:
            return self.browserObject, False, "Failed to Run Inventory on Resource(s) :: '%s' :: Error -> '%s'"%(resourceList, str(e) + format_exc())
    
    def getResourceTypes(self):
        """
        Description:
            API to get existing Resource Types
        """
        rtList = []
        try:
            utility.execLog("Reading Resource Types")
            showFilterDialog = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("device_filter_dialog"))), action="IS_DISPLAYED")
            if not showFilterDialog:
                self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("show_filter_button"))), action="CLICK")
            rtList = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("resource_type_filter"))), action="GET_ELEMENTS_BY_TAG", setValue="option", returnContent="TEXT")
            utility.execLog("Able to Read Resource Types on Resources Page '%s'"%str(rtList))
            try:
                self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("hide_filter_button"))), action="CLICK")
            except:
                pass
            return self.browserObject, True, rtList            
        except Exception as e:
            return self.browserObject, False, "Unable to read Resource Types :: Error -> %s" % str(e) + format_exc()

    def exportData(self):
        """
        Description: 
            API to Export All Resources to CSV file
        """
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")     
            utility.execLog("Scrolling to top of the window")
            self.browserObject.execute_script("window.scrollTo(0, 0);")
            utility.execLog("Checking if Resource Options are collapsed")       
            expanderStatus = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("export_all_option"))), action="IS_DISPLAYED")
            if not expanderStatus:
                utility.execLog("Resource options are collapsed....expanding to view options")
                self.browserObject.execute_script("return arguments[0].scrollIntoView();", self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab))))
                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("options_expander"))), action="CLICK")
            utility.execLog("Clicking on 'Export All'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("export_all"))), action="CLICK")
            utility.execLog("Clicked on 'Export All'")
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                return self.browserObject, False, "Failed to Export All Resources :: Error -> '%s'"%(str(errorMessage))
            except:
                return self.browserObject, True, "Successfully initiated Export All Resources"
        except Exception as e:
            return self.browserObject, False, "Exception while Exporting All Resources :: Error -> %s"%str(e) + format_exc()

    def createServerPool(self, serverPoolName, serverList, userList, addAllServers=False, addFirstHealthyServer=False):
        """
        API to create Server pool
        """
        try:
            utility.execLog("Clicking on Server Pool Tab %s"%self.serverPoolTable)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.serverPoolTab)), action="CLICK")
            utility.execLog("Clicked on Server Pool Tab....Clicking on New Server Pool Link")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("create_new_server_pool"))), action="CLICK")
            utility.execLog("Clicked on New Server Pool Link....Verifying 'Create Server Pool' wizard")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))))
            utility.execLog("Moving to Server Pool Information Page from Welcome Page")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
            utility.execLog("Moving to Server Pool Information Page from Welcome Page....Entering Server Pool Name")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("server_pool_name"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("server_pool_name"))), action="SET_TEXT", setValue=serverPoolName)
            utility.execLog("Moving to Add Servers Page from Server Pool Information Page")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
            self.browserObject, status, result = self.selectServersForServerPool(serverList, addAllServers, addFirstHealthyServer)
            utility.execLog(result)
            if not status:
                return self.browserObject, status, "Failed to Add Servers '%s', Users '%s' to  Server Pool '%s' :: Error -> %s"%(serverList, userList, serverPoolName, result)            
            utility.execLog("Moving to Add Users Page from Add Servers Page")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
            if len(userList) > 0:
                self.browserObject, status, result = self.selectUsersForServerPool(userList)
                utility.execLog(result)
                if not status:
                    return self.browserObject, status, "Failed to Add Servers '%s', Users '%s' to  Server Pool '%s' :: Error -> %s"%(serverList, userList, serverPoolName, result)
            utility.execLog("Moving to Summary Page from Add Users Page")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
            utility.execLog("Clicking on 'Finish' in Summary Page")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_finish"))), action="CLICK")
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                utility.execLog("Failed to Add Servers '%s', Users '%s' to  Server Pool '%s' :: Error -> %s"%(serverList, userList, serverPoolName, errorMessage))
                return self.browserObject, False, "Failed to Add Servers '%s', Users '%s' to  Server Pool '%s' :: Error -> %s"%(serverList, userList, serverPoolName, errorMessage)
            except:
                utility.execLog("Successfully Added Servers '%s', Users '%s' to  Server Pool '%s'"%(serverList, userList, serverPoolName))
            return self.browserObject, True, "Successfully Added Servers '%s', Users '%s' to  Server Pool '%s'"%(serverList, userList, serverPoolName)
        except Exception as e:
            utility.execLog("Failed to Add Servers '%s', Users '%s' to  Server Pool '%s' :: Error -> %s"%(serverList, userList, serverPoolName, str(e) + format_exc()))
            return self.browserObject, False, "Failed to Add Servers '%s', Users '%s' to  Server Pool '%s' :: Error -> %s"%(serverList, userList, serverPoolName, str(e) + format_exc())

    def selectServersForServerPool(self, serverList, addAllServers, addFirstHealthyServer):
        """
        Select Servers in Server Pool
        """
        try:
            self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "wizard")), action="CLICK")
            self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "wizard")), action="CLICK")
            if not addAllServers:
                utility.execLog("Reading %s Table"%self.serverTable)
                try:
                    utility.execLog("Getting Total Pages")
                    lastPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("total_pages"))), action="GET_TEXT")                 
                    if lastPage == '': 
                        lastPage = '1'
                    else:
                        lastPage = lastPage.rsplit(" ", 1)[1]
                except:
                    utility.execLog("Couldn't locate total pages element so assuming only one page")
                    lastPage = 1
                lastPage = int(lastPage)
                utility.execLog("Total No. of Pages: %s"%str(lastPage))
                if addFirstHealthyServer:
                    addedList = ["1.1.1.1"]
                else:
                    addedList = deepcopy(serverList)
                for page in range(1, lastPage+1):
                    if lastPage > 1:
                        try:
                            currentPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("current_page"))), action="GET_ATTRIBUTE_VALUE", attributeName="value")
                        except:
                            currentPage = 1
                        if currentPage != str(page):
                            utility.execLog("Moving to Page: %s"%str(page))
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("next_page"))), action="CLICK")
                    try:
                        totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,
                                            self.ResourcesObjects("generic_row_count")%({"tid":self.serverTable})))))
                    except:
                        utility.execLog("No Users found for Server Pool")
                        totalRows = 0
                    utility.execLog("Total Number of Rows : %s"%str(totalRows))                    
                    if addFirstHealthyServer:                        
                        for row in range(1, totalRows + 1):
                            xpath = self.ResourcesObjects("generic_table_element")%({"tid":self.serverTable, "rid":row, "cid":2})
                            colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                            if "green" in colValue.lower():                                
                                xpath = self.ResourcesObjects("generic_table_element")%({"tid":self.serverTable, "rid":row, "cid":4})
                                colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                                xpath = self.ResourcesObjects("select_server_user_for_server_pool")%({"tid":self.serverTable, "rid":row})
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                                utility.execLog("Able to select Server '%s'"%(colValue))
                                addedList.remove("1.1.1.1")
                                return self.browserObject, True, "Selected Server(s) '%s'"%str(colValue)
                    else:
                        for row in range(1, totalRows + 1):
                            if len(addedList) <= 0:
                                return self.browserObject, True, "Selected Servers '%s'"%str(serverList)
                            xpath = self.ResourcesObjects("generic_table_element")%({"tid":self.serverTable, "rid":row, "cid":4})
                            colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                            if colValue in addedList:
                                addedList.remove(colValue)
                                xpath = self.ResourcesObjects("select_server_user_for_server_pool")%({"tid":self.serverTable, "rid":row})
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                                utility.execLog("Able to select Server '%s'"%(colValue))
                if len(addedList) > 0:
                    return self.browserObject, False, "Failed to add some of the Server(s) :: '%s'"%addedList
                else:
                    return self.browserObject, True, "Selected Server(s) '%s'"%str(serverList)
            else:
                utility.execLog("Selecting All Servers Check Box")
                status = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_all_servers"))), action="IS_SELECTED")
                if not status:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_all_servers"))), action="CLICK")
                utility.execLog("Selected All Servers Check Box")
                return self.browserObject, True, "Selected All Server(s)"
        except Exception as e:
            utility.execLog("Unable to select Server(s) '%s' :: Error -> %s"%(serverList, str(e) + format_exc()))
            return self.browserObject, False, "Unable to select Server(s) '%s' :: Error -> %s"%(serverList, str(e) + format_exc())

    def selectUsersForServerPool(self, userList):
        """
        Select Users in Server Pool
        """
        try:           
            #self.handleEvent(EC.element_to_be_clickable((By.ID, "form_serverpool_addusers")), action="CLICK")
            utility.execLog("Reading '%s' Table"%self.userTable)
            try:
                utility.execLog("Getting Total Pages")
                lastPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("total_pages"))), action="GET_TEXT")                 
                if lastPage == '': 
                    lastPage = '1'
                else:
                    lastPage = lastPage.rsplit(" ", 1)[1]
            except:
                utility.execLog("Couldn't locate total pages element so assuming only one page")
                lastPage = 1
            lastPage = int(lastPage)
            utility.execLog("Total No. of Pages: %s"%str(lastPage))
            usrList = deepcopy(userList)
            for page in range(1, lastPage+1):
                if lastPage > 1:
                    try:
                        currentPage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("current_page"))), action="GET_ATTRIBUTE_VALUE", attributeName="value")
                    except:
                        currentPage = 1
                    if currentPage != str(page):
                        utility.execLog("Moving to Page: %s"%str(page))
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("next_page"))), action="CLICK")
                try:
                    totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("generic_row_count")%({"tid":self.userTable})))))
                except:
                    utility.execLog("No Users found for Server Pool")
                    totalRows = 0
                utility.execLog("Total Number of Rows : %s"%str(totalRows))
                for row in range(1, totalRows + 1):
                    if len(usrList) <= 0:
                        return self.browserObject, True, "Selected User(s) '%s'"%str(userList)
                    xpath = self.ResourcesObjects("generic_table_element")%({"tid":self.userTable, "rid":row, "cid":2})
                    colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    if colValue in usrList:
                        usrList.remove(colValue)
                        xpath = self.ResourcesObjects("select_server_user_for_server_pool")%({"tid":self.userTable, "rid":row})
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                        utility.execLog("Able to select User '%s'"%(colValue))
            if len(usrList) > 0:
                return self.browserObject, False, "Failed to add some of the User(s) :: '%s'"%usrList
            else:
                return self.browserObject, True, "Selected User(s) '%s'"%str(userList)
        except Exception as e:
            utility.execLog("Unable to select User(s) '%s' :: Error -> %s"%(userList, str(e) + format_exc()))
            return self.browserObject, False, "Unable to select User(s) '%s' :: Error -> %s"%(userList, str(e) + format_exc())
    
    def deleteServerPool(self, serverPoolName):
        """
        API to Delete Server Pool(Tested)
        """
        try:
            utility.execLog("Clicking on Server Pool Tab '%s'"%self.serverPoolTable)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.serverPoolTab)), action="CLICK")
            utility.execLog("Selecting Server Pool '%s'"%serverPoolName)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_device_by_info")%({"tid":self.serverPoolTable, "resInfo":serverPoolName}))), action="CLICK")
            utility.execLog("Able to select Server Pool '%s'"%serverPoolName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("delete_server_pool"))), action="CLICK")
            try:
                self.handleEvent(EC.title_contains("Confirm"))
                utility.execLog("Confirm Box Loaded...Confirming to Delete Server Pool: '%s'" % serverPoolName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            except:
                utility.execLog("Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'") % (self.browserObject.title, "Confirm")
                return self.browserObject, False, "Failed to Verify Confirm Delete Box :: Actual --> '%s' :: Expected --> '%s'" % (self.browserObject.title, "Confirm")
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                return self.browserObject, False, "Failed to Delete Server Pool :: '%s' :: Error -> '%s'"%(serverPoolName, 
                                                    str(errorMessage))
            except:
                return self.browserObject, True, "Successfully Deleted Server Pool '%s'"%serverPoolName
        except Exception as e:
            utility.execLog("Failed to Delete Server Pool '%s' :: Error -> %s"%(serverPoolName, str(e) + format_exc()))
            return self.browserObject, False, "Failed to Delete Server Pool '%s' :: Error -> %s"%(serverPoolName, str(e) + format_exc())
    
    def resourceFilterOptions(self, resourceType="All", health=None):
        """
        Selects specified Resource Type
        """
        try:
            utility.execLog("Clicking on Resources Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Clicked on Resources Tab....Selecting Resource Type :: '%s'"%resourceType)            
            if resourceType != "All":
                try:
                    utility.execLog("Verifying whether Device Filter dialog is Displayed")
                    showFilterDialog = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("device_filter_dialog"))), action="IS_DISPLAYED")                    
                    if not showFilterDialog:
                        utility.execLog("Device Filter dialog is not Displayed....Clicking on Show Filter Button")
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("show_filter_button"))), action="CLICK")
                        utility.execLog("Clicked on Show Filter Button")
                    utility.execLog("Selecting Resource Type '%s'"%resourceType)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("resource_type_filter"))), action="SELECT", selectBy="VALUE", setValue=resourceType.lower())
                except:
                    utility.execLog("Selecting Resource Type '%s' with visible text"%resourceType)
                    self.handleEvent(EC.element_to_be_clickable((By.ID,  self.ResourcesObjects("resource_type_filter"))), action="SELECT", setValue=resourceType)
            else:
                utility.execLog("Selecting Resource Type '%s'....Performing Page Refresh"%resourceType)
                self.browserObject.refresh()
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("discover_device"))))
            utility.execLog("Selected Resource Type '%s'"%resourceType)
            if health:
                try:
                    utility.execLog("Verifying whether Device Filter dialog is Displayed")
                    showFilterDialog = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("device_filter_dialog"))), action="IS_DISPLAYED")                    
                    if not showFilterDialog:
                        utility.execLog("Device Filter dialog is not Displayed....Clicking on Show Filter Button")
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("show_filter_button"))), action="CLICK")
                        utility.execLog("Clicked on Show Filter Button")
                    utility.execLog("Selecting Health Type '%s'"%health)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("health_filter"))), action="SELECT", selectBy="VALUE", setValue=health.lower())
                    utility.execLog("Selected Health Type '%s'"%health)
                except:
                    utility.execLog("Selecting Health Type '%s' with visible text"%health)
                    self.handleEvent(EC.element_to_be_clickable((By.ID,  self.ResourcesObjects("health_filter"))), action="SELECT", setValue=health)
                    utility.execLog("Selected Health Type '%s'"%health)
        except Exception as e:
            raise e                    
    
    def verifyViewDetails(self, resourceType="All", verifyAll=False):
        """
        Verifies whether user can View Details of a Resource
        """
        try:
            self.resourceFilterOptions(resourceType)
            try:
                totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                    self.ResourcesObjects("device_count")))))
            except:
                utility.execLog("No Servers found in Server Pool")
                totalRows = 0
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            failList = []
            existingIP = ""
            for row in range(1, totalRows + 1):
                xpath = self.ResourcesObjects("device_element")%({"rid":row, "cid":2})
                self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLICK")
                xpath = self.ResourcesObjects("device_element")%({"rid":row, "cid":6})
                existingIP = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                utility.execLog("Able to Select Row with Resource IP :: %s"%(existingIP))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
                utility.execLog("Able to click on 'View Details'")                
                time.sleep(5)
                utility.execLog("Waiting for 'View Details' Page to load")
                self.handleEvent(EC.text_to_be_present_in_element((By.XPATH, self.ResourcesObjects("view_details_header")), existingIP))
                utility.execLog("'View Details' Page loaded")
                actualIP = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("view_details_header"))), action="GET_TEXT")
                utility.execLog("Able to Read Info on 'View Details' Page for Resource IP :: %s"%(actualIP))
                if existingIP.strip() == actualIP.strip():
                    utility.execLog("Verified 'View Details' for Resource :: %s"%(actualIP))
                else:
                    failList.append(actualIP)
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, self.pageTitle)), action="CLICK")
                if not verifyAll:
                    break
            if len(failList) > 0:
                return self.browserObject, False, "Failed to verify View Details of some of Resources(s) :: '%s'"%failList
            else:
                if verifyAll:
                    return self.browserObject, True, "Successfully verified View Details of '%s' Resources(s)"%totalRows
                else:
                    return self.browserObject, True, "Successfully verified View Details of '%s' Resource"%existingIP
        except Exception as e:
            utility.execLog("Exception while trying to verify View Details of Resource(s) :: Error -> %s"%(str(e) + format_exc()))
            return self.browserObject, False, "Exception while trying to verify View Details of Resource(s) :: Error -> %s"%(str(e) + format_exc())
    
    def verifyIPAddressLink(self, resourceType="All", verifyAll=False, clickAndVerify=False):
        """
        Verifies whether IP Address Link of a Resource is enabled
        """
        try:
            self.resourceFilterOptions(resourceType)
            try:
                totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                    self.ResourcesObjects("device_count")))))
            except:
                utility.execLog("No Servers found in Server Pool")
                totalRows = 0
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            disabledList = []
            enabledList = []
            for row in range(1, totalRows + 1):
                xpath = self.ResourcesObjects("device_element")%({"rid":row, "cid":2})
                self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLICK")
                xpath = self.ResourcesObjects("device_element")%({"rid":row, "cid":6})
                ipAddress = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                enabled = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_ENABLED")
                if not enabled:
                    disabledList.append(ipAddress)
                else:
                    enabledList.append(ipAddress)
                if enabled:
                    if clickAndVerify:
                        utility.execLog("Clicking on IP Address link")
                        self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLICK")
                        utility.execLog("Clicked on IP Address link....Fetching Current Browser Handle")
                        currentWindowHandle = self.browserObject.current_window_handle
                        utility.execLog("Fetched Current Browser Handle....Switching to New Tab")
                        self.browserObject.switch_to_window(self.browserObject.window_handles[1])
                        utility.execLog("Switched to New Tab....Closing New Tab")
                        self.browserObject.close()
                        utility.execLog("Closed New Tab....Moving back to Main Tab")
                        self.browserObject.switch_to_window(currentWindowHandle)
                        utility.execLog("Moved back to Main Tab")
                if not verifyAll:
                    break
            if len(disabledList) > 0:
                return self.browserObject, False, "IP Address Link is Disabled on some of Resources(s) :: '%s'"%disabledList
            else:
                return self.browserObject, True, "IP Address Link is Enabled on '%s' Resources(s)"%enabledList
        except Exception as e:
            utility.execLog("Exception while trying to verify View Details of Resource(s) :: Error -> %s"%(str(e) + format_exc()))
            return self.browserObject, False, "Exception while trying to verify View Details of Resource(s) :: Error -> %s"%(str(e) + format_exc())

    def readResourceMemoryTable(self, resourceName):
        """
        Returns Memory table details of provided Resource
        """
        resMemoryList = []
        try:
            self.resourceFilterOptions("Servers")
            self.browserObject, status, result = self.selectResource(resourceName)
            if not status:
                return self.browserObject, status, result
            utility.execLog("Able to Select Resource '%s'....Clicking on 'View Details'"%(resourceName))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
            utility.execLog("Clicked on 'View Details'....Fetching Title to verify Details Page")
            if "Device Details" in self.browserObject.title:
                utility.execLog("Verified Details Page....Clicking on Memory Tab")
            else:
                utility.execLog("Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title)
                return self.browserObject, False, "Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("memory"))), action="CLICK")            
            totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("device_details_table_header")%{"tid":"device_memory"}))))
            utility.execLog("Total Number of Columns : %s"%totalColumns)
            tableColumns = []
            for col in range(2, totalColumns + 1):
                xpath = self.ResourcesObjects("device_details_table_header_col")%({"hid":col, "tid":"device_memory"})
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch Table Columns '%s'"%(tableColumns))
            try:
                totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("device_details_row_count")%{"tid":"device_memory"}))))
            except:
                utility.execLog("No DIMMS found in Server")
                totalRows = 0
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            for row in range(1, totalRows + 1):
                tableElements = []
                for col in range(2, totalColumns + 1):
                    xpath = self.ResourcesObjects("device_details_table_element")%({"rid":row, "cid":col, "tid":"device_memory"})
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Able to fetch Info '%s'"%(str(tempDict)))
                resMemoryList.append(tempDict)
            return self.browserObject, True,  resMemoryList
        except Exception as e:
            utility.execLog("Exception while reading Resource's Memory Table :: Error -> %s" %(str(e) + format_exc()))
            return self.browserObject, False,  "Exception while reading Resource's Memory Table :: Error -> %s" %(str(e) + format_exc())
             
    def readResourceMemoryTableFromiDRAC(self, resourceName, idracUser="root", idracPwd="calvin"):
        """
        returns memory table of existing Resource
        """
        try:
            self.resourceFilterOptions("Servers")
            self.browserObject, status, result = self.selectResource(resourceName)
            if not status:
                return self.browserObject, status, result
            utility.execLog("Able to Select Resource '%s'....Clicking on 'View Details'"%(resourceName))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
            utility.execLog("Clicked on 'View Details'....Fetching Title to verify Details Page")
            if "Device Details" in self.browserObject.title:
                utility.execLog("Verified Details Page....Clicking on Memory Tab")
            else:
                utility.execLog("Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title)
                return self.browserObject, False, "Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("launch_idrac"))), action="CLICK")
            utility.execLog("Clicked on 'Launch iDRAC GUI' link'....Fetching Current Browser Handle")
            currentWindowHandle = self.browserObject.current_window_handle
            utility.execLog("Fetched Current Browser Handle....Switching to iDRAC Window")
            self.browserObject.switch_to_window(self.browserObject.window_handles[1])
            utility.execLog("Switched to iDRAC Window....Setting iDRAC Username '%s'"%idracUser)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_user"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_user"))), action="SET_TEXT", setValue=idracUser)
            utility.execLog("Able to set iDRAC Username '%s'....Setting iDRAC Password '%s'"%(idracUser, idracPwd))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_pwd"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_pwd"))), action="SET_TEXT", setValue=idracPwd)
            utility.execLog("Able to set iDRAC Password '%s'....Clicking on login"%(idracPwd))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_login"))), action="CLICK")
            utility.execLog("Logged into iDRAC....Switching to 'Tree List'")
            self.browserObject.switch_to_frame(self.handleEvent(EC.element_to_be_clickable((By.NAME, self.ResourcesObjects("idrac_tree")))))
            utility.execLog("Switched to 'Tree List'....Clicking on 'Hardware' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("idrac_hardware_tab"))), action="CLICK")
            time.sleep(5)
            utility.execLog("Clicked on 'Hardware' Tab....Clicking on 'Memory' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_memory"))), action="CLICK")
            time.sleep(5)
            utility.execLog("Clicked on 'Memory' Tab....Switching to Default Content")
            self.browserObject.switch_to_default_content()
            utility.execLog("Switching to 'da' frame")
            self.handleEvent(EC.frame_to_be_available_and_switch_to_it((By.NAME, self.ResourcesObjects("idrac_da_frame"))))
            resMemoryList = []
            totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                    self.ResourcesObjects("idrac_memory_table_header")))))
            utility.execLog("Total Number of Columns : %s"%totalColumns)
            tableColumns = []
            for col in range(2, totalColumns + 1):
                xpath = self.ResourcesObjects("idrac_memory_table_header_col")%({"hid":col})
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch Table Columns '%s'"%(tableColumns))
            try:
                totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("idrac_memory_row_count")))))
            except:
                utility.execLog("No DIMMS found in Server")
                totalRows = 0
            utility.execLog("Total Number of Rows : %s"%str(totalRows))     
            for row in range(1, totalRows + 1):
                tableElements = []
                for col in range(2, totalColumns + 1):
                    xpath = self.ResourcesObjects("idrac_memory_table_element")%({"rid":row, "cid":col})
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Able to fetch Info '%s'"%(str(tempDict)))
                resMemoryList.append(tempDict)
            try:
                utility.execLog("Closing iDRAC Tab")
                self.browserObject.close()
                utility.execLog("Closed iDRAC Tab....Moving back to Main Tab")
                self.browserObject.switch_to_window(currentWindowHandle)
                utility.execLog("Moved back to Main Tab")
            except:
                pass
            return self.browserObject, True,  resMemoryList
        except Exception as e:
            utility.execLog("Exception while reading Resource's Memory Table from iDRAC :: Error -> %s" %(str(e) + format_exc()))
            return self.browserObject, False,  "Exception while reading Resource's Memory Table from iDRAC :: Error -> %s" %(str(e) + format_exc())
    
    def verifyUpdateDisabled(self, selectComplaint=False, selectInUse= False, selectUnmanaged=False):
        '''
        Verify Update firmware option is disabled for compliant, unmanaged and In use resources
        '''
        try:
            self.browserObject, status, result = self.getResources("Servers")
            if not status:
                return self.browserObject, status, result
            if selectComplaint:
                valid = [resource for resource in result if resource["Compliance"] == "Compliant"]
            if selectInUse: 
                valid = [resource for resource in valid if resource["Deployment Status"] == "In Use"]
            if selectUnmanaged:
                valid = [resource for resource in valid if resource["Managed State"] == "Unmanaged"]
            if len(valid) <= 0:
                return self.browserObject, False, "Resource not found"
            utility.execLog("Identified Resources '%s'"%valid)
            resList = [res["Management IP"] for res in valid]
            self.browserObject, status, result = self.isUpdateFirmwareEnabled(resList)            
            if not status:
                return self.browserObject, True, "Update Firmware button is disabled"
            else:
                return self.browserObject, False, "Update Firmware button is enabled"
        except Exception as e:
            utility.execLog("Exception while verifying 'Update Firmware' option is Enabled/Disabled :: Error -> %s" %(str(e) + format_exc()))
            return self.browserObject, False, "Exception while verifying 'Update Firmware' option is Enabled/Disabled :: Error -> %s" %(str(e) + format_exc())
    
    def isUpdateFirmwareEnabled(self, resourceList):
        """
        Verifies whether Update Firmware button is enabled for provided Resource 
        """    
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            for resourceName in resourceList:
                self.browserObject, status, result = self.selectResource(resourceName)
                if not status:
                    return self.browserObject, status, result
            status = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("update_resource"))), action="IS_ENABLED")
            utility.execLog("Update Firmware Option is Enabled ? '%s'"%status)
            if status:
                return self.browserObject, True, "Update Firmware option is Enabled for Resource '%s'"%resourceName
            else:
                return self.browserObject, False, "Update Firmware option is Disabled for Resource '%s'"%resourceName
        except Exception as e:
            utility.execLog("Exception while verifying 'Update Firmware' option is Enabled/Disabled :: Error -> %s" %(str(e) + format_exc()))
            return self.browserObject, False,  "Exception while verifying 'Update Firmware' option is Enabled/Disabled :: Error -> %s" %(str(e) + format_exc())
    
    def discoverResource(self, resourceType, resourceStartIP, resourceEndIP, manageResource,
                      serverCredential, storageCredential, chassisCredential, vcenterCredential,
                      switchCredential, scvmmCrdential, emCredential="", editStatus=False, resourcePool="", newCredential=False, 
                      credential=None, ipAddressing=False, existingServerIPAddress=False, hwNetworkName=""):
        """
        Discovers a Resource        
        """
        try:
            utility.execLog("Clicking on Resources Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Clicked on Resources Tab....Clicking on 'Discover'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("discover_device"))), action="CLICK")
            utility.execLog("Clicked on 'Discover' link....Clicking on Discovery Wizard")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("discovery_wizard"))), action="CLICK")
            utility.execLog("Clicked on Discovery Wizard....Verifying Discovery Wizard Page Title")
            if editStatus:
                if "Edit Resource" not in self.browserObject.title:
                    return self.browserObject, False, "Failed to verify Page Title :: Expected 'Edit Resource' , Actual '%s'"%self.browserObject.title
                else:
                    utility.execLog("Verified Page Title :: Expected 'Edit Resource' , Actual '%s'"%self.browserObject.title)
            else:
                if "Discover" not in self.browserObject.title:
                    return self.browserObject, False, "Failed to verify Page Title :: Expected 'Discover' , Actual '%s'"%self.browserObject.title
                else:
                    utility.execLog("Verified Page Title :: Expected 'Discover' , Actual '%s'"%self.browserObject.title)
            utility.execLog("Verified Discovery Wizard Page Title....Clicking on 'Next'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
            utility.execLog("Clicked on 'Next'....Clicking on 'Add Resource Type' link")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("add_resource_type"))), action="CLICK")
            utility.execLog("Clicked on 'Add Resource Type' link")
            if not editStatus:
                # Set Resource Type
                utility.execLog("Selecting Resource Type '%s'" % resourceType)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_resource_type")%{"rid":1})), action="SELECT", setValue=resourceType)
                utility.execLog("Selected Resource Type '%s'" % resourceType)
            # Set Resource Start IP
            utility.execLog("Setting Resource Start IP '%s'" % resourceStartIP)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("IP_start_range")%{"rid":1})), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("IP_start_range")%{"rid":1})), action="SET_TEXT", setValue=resourceStartIP)
            utility.execLog("Able to Set Resource Start IP '%s'....Setting Resource End IP '%s'" %(resourceStartIP, resourceEndIP))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("IP_end_range")%{"rid":1})), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("IP_end_range")%{"rid":1})), action="SET_TEXT", setValue=resourceEndIP)
            utility.execLog("Able to set Resource End IP '%s'....Setting Resource State to '%s'"%(resourceEndIP, manageResource))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("resource_state")%{"rid":1})), action="SELECT", setValue=manageResource)
            utility.execLog("Able to set Resource State to '%s'" % manageResource)
            #Setting Server Pool
            if (resourceType == "Server" or resourceType == "Chassis") and resourcePool != "":
                utility.execLog("Selecting Server Pool")
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_server_pool")%{"rid":1})), action="SELECT", setValue=resourcePool)
                    utility.execLog("Selected Resource Pool '%s'"%str(resourcePool))
                except:
                    utility.execLog("Resource Pool %s is not available, hence creating it."%str(resourcePool))
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("create_server_pool")%{"rid":1})), action="CLICK")
                    utility.execLog("Clicked on New Server Pool Link....Verifying 'Create Server Pool' wizard")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))))
                    utility.execLog("Moving to Server Pool Information Page from Welcome Page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                    utility.execLog("Moving to Server Pool Information Page from Welcome Page....Entering Server Pool Name")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("server_pool_name"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("server_pool_name"))), action="SET_TEXT", setValue=resourcePool)
                    utility.execLog("Moving to Add Users Page from Server Pool Information Page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                    self.browserObject, status, result = self.selectUsersForServerPool(["admin"])
                    utility.execLog(result)
                    if not status:
                        return self.browserObject, status, "Failed to Add Users '%s' to  Server Pool '%s' :: Error -> %s"%(["admin"], resourcePool, result)
                    utility.execLog("Moving to Summary Page from Add Users Page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                    utility.execLog("Clicking on 'Finish' in Summary Page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_finish"))), action="CLICK")
                    utility.execLog("Created Resource Pool '%s'"%str(resourcePool))
            # Set Credential
            credentialName = ""
            if resourceType in ("Server", "Storage", "vCenter", "Switch", "SCVMM", "Element Manager"):
                if resourceType == "Server":
                    credentialName = serverCredential
                elif resourceType == "Storage":
                    credentialName = storageCredential
                elif resourceType == "Switch":
                    credentialName = switchCredential
                elif resourceType == "vCenter":
                    credentialName = vcenterCredential
                elif resourceType == "SCVMM":
                    credentialName = scvmmCrdential
                elif resourceType == "Element Manager":
                    credentialName = emCredential
                utility.execLog("Setting '%s' Resource Credential to '%s'" % (resourceType, credentialName))
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_server_cred")%{"rid":1})), action="SELECT", setValue=credentialName)
                    utility.execLog("Able to set '%s' Resource Credential to '%s'....Clicking on 'Next'" % (resourceType, credentialName))
                except Exception as e:
                    if credential:
                        self.browserObject, status, result = self.createCredential(credential)
                        if not status:
                            return self.browserObject, status, result
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_server_cred")%{"rid":1})), action="SELECT", setValue=credentialName)
                        utility.execLog("Able to set '%s' Resource Credential to '%s'....Clicking on 'Next'" % (resourceType, credentialName))
                    else:
                        raise e
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                utility.execLog("Successfully clicked 'Next' on Discover Resources Dialog")
            elif newCredential:
                utility.execLog("Creating New Credential")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("create_chassis_cred")%{"rid":1})), action="CLICK")
                utility.execLog("Clicked on Create Credential....Verifying Page Title")
                if "Create Credentials" not in self.browserObject.title:
                    return self.browserObject, False, "Failed to verify Page Title :: Expected 'Create Credentials' , Actual '%s'"%self.browserObject.title
                else:
                    utility.execLog("Verified Page Title :: Expected 'Create Credentials' , Actual '%s'"%self.browserObject.title)
                utility.execLog("Entering Credential Name '%s'"%chassisCredential)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_name"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_name"))), action="SET_TEXT", setValue=chassisCredential)
                utility.execLog("Entered Credential Name '%s'....Entering Credential UserName '%s'"%(chassisCredential, "root"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_username"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_username"))), action="SET_TEXT", setValue="root")
                utility.execLog("Entered Credential UserName '%s'....Entering Credential Password '%s'"%("root", "calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_password"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_password"))), action="SET_TEXT", setValue="calvin")
                utility.execLog("Entered Credential Password '%s'....Entering Credential Confirm Password '%s'"%("calvin", "calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_confirm_password"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_confirm_password"))), action="SET_TEXT", setValue="calvin")
                utility.execLog("Entered Credential Confirm Password '%s'....Clicking on 'Save'"%("calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("save_credential"))), action="CLICK")
                utility.execLog("Able to create Credential '%s'" % chassisCredential)
                #Creating Server Credential
                utility.execLog("Creating New Server Credential ...")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("create_server_cred")%{"rid":1})), action="CLICK")
                utility.execLog("Clicked on Create Credential....Verifying Page Title")
                if "Create Credentials" not in self.browserObject.title:
                    return self.browserObject, False, "Failed to verify Page Title :: Expected 'Create Credentials' , Actual '%s'"%self.browserObject.title
                else:
                    utility.execLog("Verified Page Title :: Expected 'Create Credentials' , Actual '%s'"%self.browserObject.title)
                utility.execLog("Entering Credential Name '%s'"%serverCredential)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_name"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_name"))), action="SET_TEXT", setValue=serverCredential)
                utility.execLog("Entered Credential Name '%s'....Entering Credential UserName '%s'"%(serverCredential, "root"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_username"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_username"))), action="SET_TEXT", setValue="root")
                utility.execLog("Entered Credential UserName '%s'....Entering Credential Password '%s'"%("root", "calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_password"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_password"))), action="SET_TEXT", setValue="calvin")
                utility.execLog("Entered Credential Password '%s'....Entering Credential Confirm Password '%s'"%("calvin", "calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_confirm_password"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_confirm_password"))), action="SET_TEXT", setValue="calvin")
                utility.execLog("Entered Credential Confirm Password '%s'....Clicking on 'Save'"%("calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("save_credential"))), action="CLICK")
                utility.execLog("Able to create Server Credential '%s'" % serverCredential)
                #Creating Switch Credential
                utility.execLog("Creating New Switch Credential ...")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("create_iom_cred")%{"rid":1})), action="CLICK")
                utility.execLog("Clicked on Create Credential....Verifying Page Title")
                if "Create Credentials" not in self.browserObject.title:
                    return self.browserObject, False, "Failed to verify Page Title :: Expected 'Create Credentials' , Actual '%s'"%self.browserObject.title
                else:
                    utility.execLog("Verified Page Title :: Expected 'Create Credentials' , Actual '%s'"%self.browserObject.title)
                utility.execLog("Entering Credential Name '%s'"%switchCredential)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_name"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_name"))), action="SET_TEXT", setValue=switchCredential)
                utility.execLog("Entered Credential Name '%s'....Entering Credential UserName '%s'"%(switchCredential, "root"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_username"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_username"))), action="SET_TEXT", setValue="root")
                utility.execLog("Entered Credential UserName '%s'....Entering Credential Password '%s'"%("root", "calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_password"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_password"))), action="SET_TEXT", setValue="calvin")
                utility.execLog("Entered Credential Password '%s'....Entering Credential Confirm Password '%s'"%("calvin", "calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_confirm_password"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_confirm_password"))), action="SET_TEXT", setValue="calvin")
                utility.execLog("Entered Credential Confirm Password '%s'....Clicking on 'Save'"%("calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("save_credential"))), action="CLICK")
                utility.execLog("Able to create Switch Credential '%s'....Clicking on 'Next'" % switchCredential)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                utility.execLog("Successfully clicked 'Next' on Discover Resources Dialog")
                loopCount = 15
                while loopCount:
                    try:
                        if self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_chassis_cred"))), action="IS_DISPLAYED"):
                            break
                        else:
                            time.sleep(120)
                            loopCount = loopCount - 1
                            continue
                    except: 
                        time.sleep(120)
                        loopCount = loopCount - 1
                        continue
                utility.execLog("Configuring Chassis '%s'" % resourceStartIP)
                utility.execLog("Selecting Chassis Credential '%s'" % (chassisCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_chassis_cred"))), action="SELECT", setValue=chassisCredential)
                utility.execLog("Selected Chassis Credential '%s'....Selecting Server Credential '%s'" %(chassisCredential, serverCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_blade_cred"))), action="SELECT", setValue=serverCredential)
                utility.execLog("Selected Server Credential '%s'....Selecting IOM Credential '%s'" %(serverCredential, switchCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_IOM_cred"))), action="SELECT", setValue=switchCredential)
                utility.execLog("Selected IOM Credential '%s'....Clicking on 'Next'" % switchCredential)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                utility.execLog("Successfully clicked 'Next' on Discover Resources Dialog")
            else:
                utility.execLog("Selecting Chassis Credential '%s'" % (chassisCredential))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_chassis_cred")%{"rid":1})), action="SELECT", setValue=chassisCredential)
                utility.execLog("Selected Chassis Credential '%s'....Selecting Server Credential '%s'" %(chassisCredential, serverCredential))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_blade_cred")%{"rid":1})), action="SELECT", setValue=serverCredential)
                utility.execLog("Selected Server Credential '%s'....Selecting IOM Credential '%s'" %(serverCredential, switchCredential))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_IOM_cred")%{"rid":1})), action="SELECT", setValue=switchCredential)
                utility.execLog("Selected IOM Credential '%s'....Clicking on 'Next'" % switchCredential)
                utility.execLog("Able to create Switch Credential '%s'....Clicking on 'Next'" % switchCredential)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                utility.execLog("Successfully clicked 'Next' on Discover Resources Dialog")
                loopCount = 15
                while loopCount:
                    try:
                        utility.execLog("Checking if 'Manage Credentials' is Enabled")
                        if self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("manage_chassis_credentials"))), action="IS_DISPLAYED"):
                            break
                        else:
                            time.sleep(60)
                            loopCount = loopCount - 1
                            continue
                    except: 
                        time.sleep(60)
                        loopCount = loopCount - 1
                        continue
                utility.execLog("Configuring Chassis '%s'" % resourceStartIP)
                utility.execLog("Selecting Chassis Credential '%s'" % (chassisCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_chassis_cred"))), action="SELECT", setValue=chassisCredential)
                utility.execLog("Selected Chassis Credential '%s'....Selecting Server Credential '%s'" %(chassisCredential, serverCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_blade_cred"))), action="SELECT", setValue=serverCredential)
                utility.execLog("Selected Server Credential '%s'....Selecting IOM Credential '%s'" %(serverCredential, switchCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_IOM_cred"))), action="SELECT", setValue=switchCredential)
                utility.execLog("Selected IOM Credential '%s'" % switchCredential)
                try:
                    time.sleep(5)
                    self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Select Rack Servers for Initial Configuration")))
                    loopCount = 15                    
                    while loopCount:
                        try:
                            utility.execLog("Checking if 'Manage Credentials' is Enabled")
                            if self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("manage_rack_credentials"))), action="IS_DISPLAYED"):
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("manage_rack_credentials"))), action="CLICK")
                                break
                            else:
                                time.sleep(60)
                                loopCount = loopCount - 1
                                continue
                        except: 
                            time.sleep(60)
                            loopCount = loopCount - 1
                            continue
                    utility.execLog("Configuring Rack Server '%s'....Setting Rack Credential to '%s'"%(resourceStartIP, credentialName))
                    self.handleEvent(EC.element_to_be_clickable((By.NAME, self.ResourcesObjects("select_rack_credential"))), action="SELECT", setValue=credentialName)
                    utility.execLog("Able to Set Rack Credential to '%s'"%(credentialName))
                    if ipAddressing:
                        utility.execLog("Clicking on 'IP Addressing'")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("ip_addressing"))), action="CLICK")
                        utility.execLog("Clicked on 'IP Addressing'")
                        if existingServerIPAddress:
                            utility.execLog("Selecting option 'Use existing server IP address'")
                            self.handleEvent(EC.element_to_be_clickable((By.NAME, self.ResourcesObjects("enable_existing_ip"))), action="CLICK")
                            utility.execLog("Selected option 'Use existing server IP address'")
                        else:
                            utility.execLog("Selecting option 'Assign static IP address from this network'")
                            self.handleEvent(EC.element_to_be_clickable((By.NAME, self.ResourcesObjects("enable_static_nw"))), action="CLICK")
                            utility.execLog("Selected option 'Assign static IP address from this network'....Selecting 'Hardware Management' Network '%s'"%hwNetworkName)
                            self.handleEvent(EC.element_to_be_clickable((By.NAME, self.ResourcesObjects("select_hw_network"))), action="SELECT", setValue=hwNetworkName)
                            utility.execLog("Selected 'Hardware Management' Network '%s'"%hwNetworkName)
                    else:
                        utility.execLog("Using existing server IP address for IP Addressing")
                except:
                    utility.execLog("Not a Rack Server")
            try:
                utility.execLog("Trying to Click 'Next' on Discover Resources Dialog if any")
                if self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="IS_ENABLED"):
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                    utility.execLog("Clicked 'Next' on Discover Resources Dialog")
                else:
                    utility.execLog("Next Button is not enabled")
            except:
                utility.execLog("No need to click 'Next' button")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_finish"))), action="CLICK")
            utility.execLog("Successfully clicked 'Finish' on Discover Resources Dialog")
            try:
                self.handleEvent(EC.title_contains("Confirm"))
                utility.execLog("Confirm Box Loaded...Confirming to Discover Resource '%s'"%resourceType)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            except:
                utility.execLog("Failed to Verify Confirm Box for Resource Discovery '%s':: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Confirm"))
                return self.browserObject, False, "Failed to Verify Confirm Box for Resource Discovery '%s':: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Confirm")
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects('cancel_button'))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
                return self.browserObject, False, "Failed to Initiate Resource Discovery of Type '%s' with StartingIP : '%s' and EndingIP : '%s' :: Error -> '%s'" % (resourceType,
                                                    resourceStartIP, resourceEndIP, str(errorMessage))
            except:
                return self.browserObject, True, "Successfully Initiated Resource Discovery of Type '%s' with StartingIP : '%s' and EndingIP : '%s'" % (resourceType,
                                                    resourceStartIP, resourceEndIP)
        except Exception as e:
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects('cancel_button'))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            except:
                pass
            return self.browserObject, False, "Failed to Initiate Resource Discovery of Type '%s' with StartingIP : '%s' and EndingIP : '%s' :: Error -> '%s'" % (resourceType,
                                                    resourceStartIP, resourceEndIP, (str(e) + format_exc()))
    
    def discoverResource2(self, resource, credential):
        """
        Discovers a new Resource
        """
        try:
            serverCredential = storageCredential = chassisCredential = vcenterCredential = switchCredential = scvmmCrdential = emCredential = ""
            if resource["Type"] == "Storage":
                storageCredential = credential["Name"]
            elif resource["Type"] == "Switch":
                switchCredential = credential["Name"]
            elif resource["Type"] == "vCenter":
                vcenterCredential = credential["Name"]
            elif resource["Type"] == "SCVMM":
                scvmmCrdential = credential["Name"]
            elif resource["Type"] == "Element Manager":
                emCredential = credential["Name"]
            else:
                serverCredential = credential["Name"]
                chassisCredential = credential["Name"]
            return self.discoverResource(resource["Type"], resource["StartIP"], resource["EndIP"], resource["Resource State"], serverCredential, storageCredential, 
                                  chassisCredential, vcenterCredential, switchCredential, scvmmCrdential, emCredential, editStatus=False,
                                  resourcePool=resource["Pool"], newCredential=False, credential=credential)
        except Exception as e:
            utility.execLog("Exception while trying to Discover Resource :: Error '%s'"%(str(e) + format_exc()))
            return self.browserObject, False, "Exception while trying to Discover Resource :: Error '%s'"%(str(e) + format_exc()) 
    
    def singleResourceDiscovery(self, resourceType="Server", resourceIP = "172.31.61.81", resourceState = "Managed", credentialName = "Dell PowerEdge iDRAC Default",
                                userName="root", password="calvin", ipAddress=False, existingserverIPaddress=False, unCheck=False, hwnwName=""):
        """
        Description:
            API to get existing Resources            
        """
        try:
            serverCredential = storageCredential = chassisCredential = vcenterCredential = switchCredential = scvmmCrdential = emCredential = ""
            if resource["Type"] == "Storage":
                storageCredential = credentialName
            elif resource["Type"] == "Switch":
                switchCredential = credentialName
            elif resource["Type"] == "vCenter":
                vcenterCredential = credentialName
            elif resource["Type"] == "SCVMM":
                scvmmCrdential = credentialName
            elif resource["Type"] == "Element Manager":
                emCredential = credentialName
            else:
                serverCredential = credentialName
                chassisCredential = credentialName
            self.browserObject, status, result = self.discoverResource(resourceType, resourceIP, resourceIP, resourceState, serverCredential, storageCredential, 
                                  chassisCredential, vcenterCredential, switchCredential, scvmmCrdential, emCredential, editStatus=False,
                                  resourcePool="", newCredential=False, credential=None, ipAddressing=ipAddress,
                                  existingServerIPAddress=existingserverIPaddress, hwNetworkName=hwnwName)
            if not status:
                return self.browserObject, status, result
            utility.execLog(result)
            utility.execLog("Waiting for discovery to complete")
            self.browserObject, status, result = self.waitJobComplete("Discovery")
            if not status:
                return self.browserObject, status, result
            utility.execLog("Discovery completed....Verifying whether Resource is available")
            self.browserObject.refresh()
            time.sleep(10)
            self.browserObject, status, result = self.getResources(resourceType, "Healthy")
            if not status:
                return self.browserObject, status, result
            utility.execLog(result)
            srvList = [res for res in result if res["Management IP"] == resourceIP]
            if len(srvList) > 0:
                utility.execLog("Successfully Discovered Resource '%s'....Verified Discovered Resource in Resource Pool"%resourceIP)
                return self.browserObject, True, "Successfully Discovered Resource '%s'....Verified Discovered Resource in Resource Pool"%resourceIP
            else:
                utility.execLog("Failed to Verify Resource '%s' in Resource Pool"%resourceIP)
                return self.browserObject, False, "Failed to Verify Resource '%s' in Resource Pool"%resourceIP
        except Exception as e:
            return self.browserObject, False, "Exception while trying to discover Resource '%s' :: Error -> %s"%(resourceIP, (str(e) + format_exc()))
    
    def discoverMultipleResource(self, resourceType, resourceStartIP, resourceEndIP, manageResource,
                      serverCredential, storageCredential, chassisCredential, vcenterCredential,
                      switchCredential, scvmmCrdential, emCredential, resourceCount, resourcePool=""):
        """
        Creates multiple new Resource        
        """
        try:
            resourceCount = resourceCount + 1
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("add_resource_type"))), action="CLICK")
            utility.execLog("Clicked on 'Add Resource Type' link....Selecting Resource Type '%s'" % resourceType)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_resource_type")%{"rid":resourceCount})), action="SELECT", setValue=resourceType)
            utility.execLog("Selected Resource Type '%s'" % resourceType)
            # Set Resource Start IP
            utility.execLog("Setting Resource Start IP '%s'" % resourceStartIP)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("IP_start_range")%{"rid":resourceCount})), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("IP_start_range")%{"rid":resourceCount})), action="SET_TEXT", setValue=resourceStartIP)
            utility.execLog("Able to Set Resource Start IP '%s'....Setting Resource End IP '%s'" %(resourceStartIP, resourceEndIP))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("IP_end_range")%{"rid":resourceCount})), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("IP_end_range")%{"rid":resourceCount})), action="SET_TEXT", setValue=resourceEndIP)
            utility.execLog("Able to set Resource End IP '%s'....Setting Resource State to '%s'"%(resourceEndIP, manageResource))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("resource_state")%{"rid":resourceCount})), action="SELECT", setValue=manageResource)
            utility.execLog("Able to set Resource State to '%s'" % manageResource)
            #Setting Server Pool
            if (resourceType == "Server" or resourceType == "Chassis") and resourcePool != "":
                utility.execLog("Selecting Server Pool")
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_server_pool")%{"rid":resourceCount})), action="SELECT", setValue=resourcePool)
                    utility.execLog("Selected Resource Pool '%s'"%str(resourcePool))
                except:
                    utility.execLog("Resource Pool %s is not available, hence creating it."%str(resourcePool))
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("create_server_pool")%{"rid":resourceCount})), action="CLICK")
                    utility.execLog("Clicked on New Server Pool Link....Verifying 'Create Server Pool' wizard")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))))
                    utility.execLog("Moving to Server Pool Information Page from Welcome Page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                    utility.execLog("Moving to Server Pool Information Page from Welcome Page....Entering Server Pool Name")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("server_pool_name"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("server_pool_name"))), action="SET_TEXT", setValue=resourcePool)
                    utility.execLog("Moving to Add Users Page from Server Pool Information Page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                    self.browserObject, status, result = self.selectUsersForServerPool(["admin"])
                    utility.execLog(result)
                    if not status:
                        return self.browserObject, status, "Failed to Add Users '%s' to  Server Pool '%s' :: Error -> %s"%(["admin"], resourcePool, result)
                    utility.execLog("Moving to Summary Page from Add Users Page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                    utility.execLog("Clicking on 'Finish' in Summary Page")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_finish"))), action="CLICK")
                    utility.execLog("Created Resource Pool '%s'"%str(resourcePool))
            # Set Credential
            credentialName = ""
            if resourceType in ("Server", "Storage", "vCenter", "Switch", "SCVMM", "Element Manager"):
                if resourceType == "Server":
                    credentialName = serverCredential
                elif resourceType == "Storage":
                    credentialName = storageCredential
                elif resourceType == "Switch":
                    credentialName = switchCredential
                elif resourceType == "vCenter":
                    credentialName = vcenterCredential
                elif resourceType == "SCVMM":
                    credentialName = scvmmCrdential
                elif resourceType == "Element Manager":
                    credentialName = emCredential
                utility.execLog("Setting '%s' Resource Credential to '%s'"%(resourceType, credentialName))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_server_cred")%{"rid":resourceCount})), action="SELECT", setValue=credentialName)
                utility.execLog("Able to set '%s' Resource Credential to '%s'"%(resourceType, credentialName))
                #===============================================================
                # self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                # utility.execLog("Successfully clicked 'Next' on Discover Resources Dialog")
                #===============================================================
            else:
                utility.execLog("Selecting Chassis Credential '%s'" % (chassisCredential))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_chassis_cred")%{"rid":resourceCount})), action="SELECT", setValue=chassisCredential)
                utility.execLog("Selected Chassis Credential '%s'....Selecting Server Credential '%s'" %(chassisCredential, serverCredential))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_blade_cred")%{"rid":resourceCount})), action="SELECT", setValue=serverCredential)
                utility.execLog("Selected Server Credential '%s'....Selecting IOM Credential '%s'" %(serverCredential, switchCredential))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_IOM_cred")%{"rid":resourceCount})), action="SELECT", setValue=switchCredential)
                utility.execLog("Selected IOM Credential '%s'" % switchCredential)
            return self.browserObject, True, "Successfully Added Details for Resource Type %s"%resourceType
        except Exception as e:
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects('cancel_button'))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            except:
                pass
            return self.browserObject, False, "Failed to Initiate Resource Discovery of Type '%s' with StartingIP : '%s' and EndingIP : '%s' :: Error -> '%s'" % (resourceType,
                                                    resourceStartIP, resourceEndIP, (str(e) + format_exc()))
        
    def finishMultipleResourceDiscovery(self, resourceStartIP, chassisCredential, cserverCredential, switchCredential,
                                                                        rackserverCredential="Dell PowerEdge iDRAC Default"):
        """
        Description: Completing Resource Discovery
        """
        try:
            utility.execLog("Clicking on 'Next'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
            utility.execLog("Successfully clicked 'Next' on Discover Resources Dialog")
            if chassisCredential:
                loopCount = 15
                while loopCount:
                    try:
                        if self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_chassis_cred"))), action="IS_DISPLAYED"):
                            break
                        else:
                            time.sleep(120)
                            loopCount = loopCount - 1
                            continue
                    except: 
                        time.sleep(120)
                        loopCount = loopCount - 1
                        continue
                utility.execLog("Configuring Chassis '%s'" % resourceStartIP)
                utility.execLog("Selecting Chassis Credential '%s'" % (chassisCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_chassis_cred"))), action="SELECT", setValue=chassisCredential)
                utility.execLog("Selected Chassis Credential '%s'....Selecting Server Credential '%s'" %(chassisCredential, cserverCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_blade_cred"))), action="SELECT", setValue=cserverCredential)
                utility.execLog("Selected Server Credential '%s'....Selecting IOM Credential '%s'" %(cserverCredential, switchCredential))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("manage_IOM_cred"))), action="SELECT", setValue=switchCredential)
                utility.execLog("Selected IOM Credential '%s'....Clicking on 'Next'" % switchCredential)                
            try:
                time.sleep(5)
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Select Rack Servers for Initial Configuration")))
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Select Rack Servers for Initial Configuration")), action="CLICK")
                loopCount = 15                    
                while loopCount:
                    try:
                        utility.execLog("Checking if 'Manage Credentials' is Enabled")
                        if self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_all_rack_servers"))), action="IS_ENABLED"):
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("select_all_rack_servers"))), action="CLICK")
                            time.sleep(3)
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("manage_rack_credentials"))), action="IS_DISPLAYED")
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("manage_rack_credentials"))), action="CLICK")
                            break
                        else:
                            time.sleep(60)
                            loopCount = loopCount - 1
                            continue
                    except: 
                        time.sleep(60)
                        loopCount = loopCount - 1
                        continue                
                utility.execLog("Configuring Rack Server '%s'....Setting Rack Credential to '%s'"%(resourceStartIP, rackserverCredential))
                self.handleEvent(EC.element_to_be_clickable((By.NAME, self.ResourcesObjects("select_rack_credential"))), action="SELECT", setValue=rackserverCredential)
                utility.execLog("Able to Set Rack Credential to '%s'"%(rackserverCredential))
            except:
                utility.execLog("Not a Rack Server")
            seviceTagsList = []
            try:
                utility.execLog("Trying to Click 'Next' on Discover Resources Dialog if any")
                if self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="IS_ENABLED"):
                    try:
                        seviceTagsElements = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                            self.ResourcesObjects("get_chassis_service_tags")))) 
                        for servicTagEle in seviceTagsElements:
                            utility.execLog("Service Tag :%s"%servicTagEle.text)
                            seviceTagsList.append(servicTagEle.text)
                    except:
                        pass
                    try:
                        seviceTagsElements = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                            self.ResourcesObjects("get_rack_service_tags")))) 
                        for servicTagEle in seviceTagsElements:
                            utility.execLog("Service Tag :%s"%servicTagEle.text)
                            seviceTagsList.append(servicTagEle.text)
                    except:
                        pass
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
                    utility.execLog("Clicked 'Next' on Discover Resources Dialog")
                else:
                    utility.execLog("Next Button is not enabled")
            except:
                utility.execLog("No need to click 'Next' button")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_finish"))), action="CLICK")
            utility.execLog("Successfully clicked 'Finish' on Discover Resources Dialog")
            time.sleep(2)
            try:
                self.handleEvent(EC.title_contains("Confirm"))
                utility.execLog("Confirm Box Loaded...Confirming to Discover Resource")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            except:
                utility.execLog("Failed to Verify Confirm Box for Resource Discovery '%s':: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Confirm"))
                return self.browserObject, False, "Failed to Verify Confirm Box for Resource Discovery '%s':: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Confirm")
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects('cancel_button'))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
                return self.browserObject, False, "Failed to Initiate Resource Discovery with StartingIP : '%s' :: Error -> '%s'" % (resourceStartIP, 
                                                                                                str(errorMessage))
            except:
                return self.browserObject, True, "Successfully Initiated Resource Discovery with StartingIP : '%s'" %(resourceStartIP)
        except Exception as e:
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects('cancel_button'))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
            except:
                pass
            return self.browserObject, False, "Failed to Initiate Resource Discovery with StartingIP : '%s' :: Error -> '%s'" % (resourceStartIP, (str(e) + format_exc()))
    
    def verifyResourceHealth(self, resourceType):
        """
        Description:
            API to get existing Resources
        """
        try:            
            #Verify Healthy Devices
            self.browserObject, status, resList = self.getResources(resourceType, "Healthy")
            if not status:
                return self.browserObject, status, resList
            self.browserObject, status, result = self.verifyState("Health", resList, "green")
            if not status:
                return self.browserObject, status, result
            else:
                utility.execLog(result)
            #Verify Warning Devices
            self.browserObject, status, resList = self.getResources(resourceType, "Warning")
            if not status:
                return self.browserObject, status, resList
            self.browserObject, status, result = self.verifyState("Health", resList, "warning")
            if not status:
                return self.browserObject, status, result
            else:
                utility.execLog(result)
            #Verify Unknown Devices
            self.browserObject, status, resList = self.getResources(resourceType, "Unknown")
            if not status:
                return self.browserObject, status, resList
            self.browserObject, status, result = self.verifyState("Health", resList, "Unknown")
            if not status:
                return self.browserObject, status, result
            else:
                utility.execLog(result)
            #Verify Critical Devices
            self.browserObject, status, resList = self.getResources(resourceType, "Critical")
            if not status:
                return self.browserObject, status, resList
            self.browserObject, status, result = self.verifyState("Health", resList, "red")
            if not status:
                return self.browserObject, status, result
            else:
                utility.execLog(result)
            return self.browserObject, True, "Successfully verified State for Resources of Type '%s'"%resourceType      
        except Exception as e:
            return self.browserObject, False, "Exception while verifying State for Resources of Type '%s' :: Error -> %s"%(resourceType, (str(e) + format_exc()))  
        
    def verifyState(self, attribType, resList, status):
        """
        Description:
        API to verify Resource's Health Status
        """
        try:
            failList = []
            for resource in resList:
                if str(resource[attribType]).lower() != status.lower():
                    utility.execLog("Failed to verify Resource State :: Actual '%s' , Expected '%s'"%(resource, status))
                    failList.append(resource)
                else:
                    utility.execLog("Verified Resource State :: Actual '%s' , Expected '%s'"%(resource, status))
            if len(failList) > 0:
                return self.browserObject, False, "Failed to verify State for Resource(s) :: '%s'"%failList
            else:
                return self.browserObject, True, "Successfully Verified State for Resource(s) :: '%s'"%resList
        except Exception as e:
            return self.browserObject, False, "Exception while verifying Resource's State :: Error -> %s" %(str(e) + format_exc())

    def changeServerState(self, serverIP, state):
        """
        Changes Server State by launching iDRAC GUI
        """        
        try:
            utility.execLog("Launching iDRAC in New Tab for Server '%s'"%serverIP)
            self.browserObject.execute_script("window.open('');")
            utility.execLog("Switching to New Tab 'https://%s/login.html'"%serverIP)
            for h in self.browserObject.window_handles[1:]:
                self.browserObject.switch_to_window(h)
                time.sleep(10)
            self.browserObject.get("https://%s/login.html"%str(serverIP))
            #self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("launch_idrac"))), action="CLICK")
            utility.execLog("Launched iDRAC")
            utility.execLog("Setting iDRAC Username '%s'"%"root")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_user"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_user"))), action="SET_TEXT", setValue="root")
            utility.execLog("Able to set iDRAC Username '%s'....Setting iDRAC Password '%s'"%("root", "calvin"))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_pwd"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_pwd"))), action="SET_TEXT", setValue="calvin")
            utility.execLog("Able to set iDRAC Password '%s'....Clicking on login"%("calvin"))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_login"))), action="CLICK")
            time.sleep(60)
            utility.execLog("Logged into iDRAC....Switching to 'da' frame")
            self.handleEvent(EC.frame_to_be_available_and_switch_to_it((By.NAME, self.ResourcesObjects("idrac_da_frame"))))
            utility.execLog("Switched to 'da' frame....Switching to 'sysIframe' Frame")
            self.handleEvent(EC.frame_to_be_available_and_switch_to_it((By.ID, self.ResourcesObjects("idrac_sys_frame"))))
            utility.execLog("Switched to 'sysIframe' frame")
            serverState = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("idrac_get_power_state"))), action="GET_TEXT")
            utility.execLog("Server current state is %s"%str(serverState))
            if state in serverState:
                utility.execLog("Server current state %s is same as change state %s....Closing IDRAC Tab"%(str(serverState), str(state)))
                self.browserObject.close()
                utility.execLog("Closed IDRAC Tab..Switching Browser Handle to Current Window")
                self.browserObject.switch_to_window(self.browserObject.window_handles[0])
                utility.execLog("Switched Browser Handle to Current Window")
                return self.browserObject, True, "Server already in %s state"%str(state)
            else:
                utility.execLog("Server current state in %s , changing state to %s"%(str(serverState),str(state)))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("idrac_set_power_state"))), action="CLICK")
                time.sleep(3)
                utility.execLog("Hitting enter key to change state")
                self.browserObject.switch_to_alert().accept()
                utility.execLog("Confirm pop-up closed.")
                time.sleep(60)
                self.browserObject.switch_to_default_content()
                self.handleEvent(EC.frame_to_be_available_and_switch_to_it((By.NAME, self.ResourcesObjects("idrac_da_frame"))))
                utility.execLog("Switched to 'da' frame....Switching to 'sysIframe' Frame")
                self.handleEvent(EC.frame_to_be_available_and_switch_to_it((By.ID, self.ResourcesObjects("idrac_sys_frame"))))
                serverState = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("idrac_get_power_state"))), action="GET_TEXT")
                if state in serverState:
                    utility.execLog("Closing IDRAC Tab")
                    self.browserObject.close()
                    utility.execLog("Closed IDRAC Tab..Switching Browser Handle to Current Window")
                    self.browserObject.switch_to_window(self.browserObject.window_handles[0])
                    utility.execLog("Switched Browser Handle to Current Window")
                    return self.browserObject, True, "Server state is changed to %s"%str(state)
                else:
                    self.browserObject.switch_to_window(self.browserObject.window_handles[0])
                    utility.execLog("Switched Browser Handle to Current Window")
                    return self.browserObject, False, "Unable to change the state of Server %s to %s"%(str(serverIP),str(state))
        except Exception as e:
            for h in self.browserObject.window_handles[1:]:
                self.browserObject.switch_to_window(h).close()
            utility.execLog("Switching Browser Handle to Current Window")
            self.browserObject.switch_to_window(self.browserObject.window_handles[0])
            utility.execLog("Switched Browser Handle to Current Window")
            utility.execLog("Exception generated while changing the state of server. Error --> %s"%(str(e) + format_exc()))
            return self.browserObject, False, "Exception generated while changing the state of server. Error --> %s"%(str(e) + format_exc())

    def getPerformanceData(self, resourceName):
        """
        Returns Performance Data
        """
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            self.browserObject, status, result = self.selectResource(resourceName)
            if not status:
                return self.browserObject, status, result
            utility.execLog("Able to Select Resource '%s'....Clicking on 'View Details'"%(resourceName))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
            utility.execLog("Clicked on 'View Details'....Fetching Title to verify Details Page")
            try:
                self.handleEvent(EC.title_contains("Device Details"))
                utility.execLog("Verified Details Page....Fetching Performance Data")
            except:
                utility.execLog("Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title)
                return self.browserObject, False, "Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title
            time.sleep(5)
            perfData = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("get_performance_data"))), action="GET_TEXT")
            utility.execLog("Performance Data captured '%s'"%perfData)
            return self.browserObject, True, perfData
        except Exception as e:
            utility.execLog("Exception while reading performance data for resource '%s' :: Error -> %s"%(resourceName, str(e) + format_exc()))
            return self.browserObject, False, "Exception while reading performance data for resource '%s' :: Error -> %s"%(resourceName, str(e) + format_exc())
     
    def clearFilterService(self, serviceName):
        '''
        Description: Clears 'Resource Type' Filters when navigating between pages
        '''        
        try:
            utility.execLog("Selecting Service Name '%s' in Service Name Filter" % serviceName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("service_filter"))), action="SELECT", setValue=serviceName)
            utility.execLog("Selected Service Name '%s' in Service Name Filter....Selecting 'All' in Resource Type Filter"%serviceName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("resource_type_filter"))), action="SELECT", setValue="All")
            utility.execLog("Selected 'All' in Resource Type Filter")
            return self.browserObject, True, "Able to set Service Name Filter to '%s'"%serviceName
        except Exception as e:
            utility.execLog("Exception while setting Service Name Filter to '%s' :: Error -> %s"%(serviceName, str(e) + format_exc()))
            return self.browserObject, False, "Exception while setting Service Name Filter to '%s' :: Error -> %s"%(serviceName, str(e) + format_exc())
            
    def getResourcesDeployed(self, serviceName):
        """
        Description:
            API to get resources for Deployed Services.
        """
        deployedResources = {}
        try:            
            for resourceType in ["Storage", "Servers", "VM Manager"]:
                self.browserObject, status, result = self.getResources(resourceType, "All", serviceName=serviceName)
                if not status:
                    return self.browserObject, status, result
                deployedResources[resourceType] = result
            return self.browserObject, True, deployedResources    
        except Exception as e:
            return self.browserObject, False, "Exception while trying to retrieve Resource(s) for Deployed Service '%s' :: Error -> %s"%(serviceName, str(e) + format_exc())
        
    def getStorageDetails(self, resourceName, storageType):
        """
        Description:
            API to get volumes in a Storage
        """
        deployedStorage = []
        try:
            utility.execLog("Selecting 'Storage' in Resource Filter")
            self.resourceFilterOptions("Storage")
            utility.execLog("Selected 'Storage' in Resource Filter....Selecting Resource '%s'"%resourceName)
            self.browserObject, status, result = self.selectResource(resourceName)
            if not status:
                return self.browserObject, status, result
            utility.execLog("Able to Select Resource '%s'....Clicking on 'View Details'"%(resourceName))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
            utility.execLog("Clicked on 'View Details'....Fetching Title to verify Details Page")
            time.sleep(5)
            try:
                self.handleEvent(EC.title_contains("Device Details"))
                utility.execLog("Verified Details Page....Clicking on Memory Tab")
            except:
                utility.execLog("Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title)
                return self.browserObject, False, "Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("volumes"))), action="CLICK")
            if storageType == 'Compellent':
                tableName=self.ResourcesObjects("cpl_table")
            elif storageType == "EqualLogic":
                tableName=self.ResourcesObjects("eql_table")
            totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("device_details_table_header")%{"tid":tableName}))))
            utility.execLog("Total Number of Columns : %s"%totalColumns)
            tableColumns = []
            for col in range(1, totalColumns + 1):
                xpath = self.ResourcesObjects("device_details_table_header_col")%({"hid":col, "tid":tableName})
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch Table Columns '%s'"%(tableColumns))
            try:
                totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("device_details_row_count")%{"tid":tableName}))))
            except:
                utility.execLog("No volumes found on '%s' Storage"%resourceName)
                totalRows = 0
            utility.execLog("Total Number of Rows : %s"%str(totalRows))     
            for row in range(1, totalRows + 1):
                tableElements = []
                for col in range(1, totalColumns + 1):
                    xpath = self.ResourcesObjects("device_details_table_element")%({"rid":row, "cid":col, "tid":tableName})
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Able to fetch Info '%s'"%(str(tempDict)))
                deployedStorage.append(tempDict)
            utility.execLog(deployedStorage)
            if (len(deployedStorage) == 0):
                return self.browserObject, False, "No volumes found on '%s' Storage"%resourceName
            else:
                return self.browserObject, True, deployedStorage
        except Exception as e:
            return self.browserObject, False, "Unable to retrieve Volumes on %s Storage: Error -> %s" %(storageType, str(e) + format_exc())
    
    def getResourceDetails(self, resourceName, resourceType):
        """
        Description:
            API to get Resource Details
        """
        resourceInfo = []
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            self.browserObject, status, result = self.selectResource(resourceName)
            if not status:
                return self.browserObject, status, result
            utility.execLog("Able to Select Resource '%s'....Clicking on 'View Details'"%(resourceName))
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
            utility.execLog("Clicked on 'View Details'....Fetching Title to verify Details Page")
            if "Device Details" in self.browserObject.title:
                utility.execLog("Verified Details Page....Clicking on Memory Tab")
            else:
                utility.execLog("Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title)
                return self.browserObject, False, "Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title
            if resourceType == "Dell Chassis":
                opts=[{"Blades": [self.ResourcesObjects("blades"), self.ResourcesObjects("blades_table")]}, 
                                  {"IOMS": [self.ResourcesObjects("ioms"), self.ResourcesObjects("ioms_table")]}]
            for opt in opts:
                deviceInfo = []
                tableName = opt.values()[0][1]
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, opt.values()[0][0])), action="CLICK")
                totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                            self.ResourcesObjects("generic_table_header")%{"tid":tableName}))))
                utility.execLog("Total Number of Columns : %s"%totalColumns)
                tableColumns = []
                for col in range(2, totalColumns + 1):
                    xpath = self.ResourcesObjects("generic_table_header_col")%({"hid":col, "tid":tableName})
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableColumns.append(colName)
                        utility.execLog("Able to fetch Column Name: '%s'"%colName)
                tableColumns = [x for x in tableColumns if x !='']
                utility.execLog("Able to fetch Table Columns '%s'"%(tableColumns))
                try:
                    totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                            self.ResourcesObjects("generic_row_count")%{"tid":tableName}))))
                except:
                    utility.execLog("No '%s' found in Resource '%s'"%(opt.keys()[0], resourceName))
                    totalRows = 0
                utility.execLog("Total Number of Rows : %s"%str(totalRows))     
                for row in range(1, totalRows + 1):
                    tableElements = []
                    for col in range(2, totalColumns + 1):
                        xpath = self.ResourcesObjects("generic_table_element")%({"rid":row, "cid":col, "tid":tableName})
                        if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                            colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                            tableElements.append(colValue)
                    tempDict = dict(zip(tableColumns, tableElements))
                    utility.execLog("Able to fetch Info '%s'"%(str(tempDict)))
                    deviceInfo.append(tempDict)
                utility.execLog("Able to Info '%s'"%({opt.keys()[0]:deviceInfo}))
                resourceInfo.append({opt.keys()[0]:deviceInfo})
            utility.execLog(resourceInfo)
            return self.browserObject, True, resourceInfo
        except Exception as e:
            return self.browserObject, False, "Unable to retrieve info for Resource '%s' of Type '%s': Error -> %s" %(resourceName, resourceType, str(e) + format_exc())
    
    def verifyResourceModelAvailblity(self, resourceType, resourceModel):
        """
        Verifies if Resource of Model is available in Resources 
        """
        count = 0
        try:
            self.browserObject, status, result = self.getResources(resourceType)
            if not status:
                return self.browserObject, status, result
            for resource in result:
                if resourceModel in resource["Model"]:
                    count = count + 1
            if count > 0:
                return self.browserObject, True, "Resource Model %s Available"%resourceModel
            else:
                return self.browserObject, False, "Resource Model %s is Not Available"%resourceModel
        except Exception as e:
            return self.browserObject, False, "Exception while verifying Resource of Model is available in Resources :: Error -> %s"%(str(e) + format_exc())
        
    def loadIdentifyResources(self):
        """
        Description: To load Identify Resouces Page under Discover Resources
        """
        try:
            utility.execLog("Clicking on Resources Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Clicked on Resources Tab....Clicking on 'Discover'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("discover_device"))), action="CLICK")
            utility.execLog("Clicked on 'Discover' link....Clicking on Discovery Wizard")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("discovery_wizard"))), action="CLICK")
            utility.execLog("Clicked on Discovery Wizard....Verifying Discovery Wizard Page Title")
            time.sleep(3)
            try:
                self.handleEvent(EC.title_contains("Discover"))
                utility.execLog("Verified Page Title :: Expected 'Discover' , Actual '%s'"%self.browserObject.title)
            except:
                utility.execLog("Failed to verify Page Title :: Expected 'Discover' , Actual '%s'"%self.browserObject.title)
                return self.browserObject, False, "Failed to verify Page Title :: Expected 'Discover' , Actual '%s'"%self.browserObject.title
            utility.execLog("Verified Discovery Wizard Page Title....Clicking on 'Next'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("wizard_next"))), action="CLICK")
            utility.execLog("Clicked on 'Next' in Resource Discovery Dialog")
            return self.browserObject, True, "Successfully clicked 'Next' on Discover Resources Dialog"
        except Exception as e:
            return self.browserObject, False, "Unable To Load Identify Resource Page :: '%s'"%(str(e) + format_exc())
            
    def serverDeploymentStatus(self, resourceType, resourceIP, servicedeploymentStatus):
        """
        Verifies provided Server Deployment Status
        """
        try:
            self.browserObject, status, result = self.getResources(resourceType)
            if not status:
                return self.browserObject, status, result
            for resource in result:
                if resource["Management IP"] == resourceIP:
                    if resource["Deployment Status"] == servicedeploymentStatus:
                        return self.browserObject, True, "Verified Deployment status of Server Actual '%s' Expected '%s'"%(resource["Deployment Status"],
                                                                                                                        servicedeploymentStatus)
            return self.browserObject, False, "Failed to verify Deployment status of Server Actual '%s' Expected '%s'"%(resource["Deployment Status"],
                                                                                                                        servicedeploymentStatus)
        except Exception as e:
            return self.browserObject, False, "Exception while trying to verify Deployment status of Server :: Error '%s'"%(str(e) + format_exc())
        
    def validateServerState(self, ipList, resourceType="Servers"):
        """
        Verifies provided Server(s) Deployment Status is 'Not in use'
        """
        try:
            self.resourceFilterOptions(resourceType)
            self.browserObject, status, result = self.getResources(resourceType)
            if not status:
                return self.browserObject, status, result
            for resourceIP in ipList:
                utility.execLog("Server IP from List :%s"%resourceIP)
                valid = [resource for resource in result if resource["Management IP"] == resourceIP]
                if valid[0]["Deployment Status"] == "Not in Use":
                    utility.execLog("Verified Deployment status of Server Actual '%s' Expected '%s'"%(valid[0]["Deployment Status"], "Not in use"))
                else:
                    utility.execLog("Failed to verify Deployment status of Server Actual '%s' Expected '%s'"%(valid[0]["Deployment Status"],
                                                                                                                            "Not in use"))
                    return self.browserObject, False, "Failed to verify Deployment status of Server Actual '%s' Expected '%s'"%(valid[0]["Deployment Status"],
                                                                                                                            "Not in use")
            return self.browserObject, True, "Verified Deployment status of server '%s'"%ipList
        except Exception as e:
            utility.execLog("Exception while trying to verify Deployment status of Servers '%s' :: Error '%s'"%(ipList, str(e) + format_exc()))
            return self.browserObject, False, "Exception while trying to verify Deployment status of Servers '%s' :: Error '%s'"%(ipList, str(e) + format_exc())
    
    def serverConsoleState(self, ipList):
        """
        Verifies provided Server(s) Power status is OFF
        """
        try:
            for serverIP in ipList:
                utility.execLog("Launching iDRAC in New Tab for Server '%s'"%serverIP)
                self.browserObject.execute_script("window.open('');")
                utility.execLog("Switching to New Tab 'https://%s/login.html'"%serverIP)
                for h in self.browserObject.window_handles[1:]:
                    self.browserObject.switch_to_window(h)
                    time.sleep(10)
                self.browserObject.get("https://%s/login.html"%str(serverIP))
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("launch_idrac"))), action="CLICK")
                utility.execLog("Launched iDRAC")
                utility.execLog("Setting iDRAC Username '%s'"%"root")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_user"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_user"))), action="SET_TEXT", setValue="root")
                utility.execLog("Able to set iDRAC Username '%s'....Setting iDRAC Password '%s'"%("root", "calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_pwd"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_pwd"))), action="SET_TEXT", setValue="calvin")
                utility.execLog("Able to set iDRAC Password '%s'....Clicking on login"%("calvin"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_login"))), action="CLICK")
                time.sleep(60)
                utility.execLog("Logged into iDRAC....Switching to 'da' frame")
                self.handleEvent(EC.frame_to_be_available_and_switch_to_it((By.NAME, self.ResourcesObjects("idrac_da_frame"))))
                utility.execLog("Switched to 'da' frame....Switching to 'sysIframe' Frame")
                self.handleEvent(EC.frame_to_be_available_and_switch_to_it((By.ID, self.ResourcesObjects("idrac_sys_frame"))))
                utility.execLog("Switched to 'sysIframe' frame")
                serverState = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("idrac_get_power_state"))), action="GET_TEXT")
                utility.execLog("Server current state is %s"%str(serverState))
                if serverState == "OFF":
                    utility.execLog("Server current state is '%s', Expected 'OFF'....Closing IDRAC Tab"%(serverState))
                    self.browserObject.close()
                    utility.execLog("Closed IDRAC Tab..Switching Browser Handle to Current Window")
                    self.browserObject.switch_to_window(self.browserObject.window_handles[0])
                    utility.execLog("Switched Browser Handle to Current Window")
                else:
                    utility.execLog("Failed to verify Server Power state :: Actual '%s' Expected 'OFF'"%serverState)
                    self.browserObject.close()
                    utility.execLog("Closed IDRAC Tab..Switching Browser Handle to Current Window")
                    self.browserObject.switch_to_window(self.browserObject.window_handles[0])
                    utility.execLog("Switched Browser Handle to Current Window")
                    return self.browserObject, False, "Failed to verify Server Power state :: Actual '%s' Expected 'OFF'"%serverState 
            utility.execLog("Successfully validated server(s) console state")
            return self.browserObject, True , "Successfully validated server(s) console state"
        except Exception as e:
            utility.execLog("Exception generated while verifying the power state of server(s) :: Error -> %s"%(str(e) + format_exc()))
            return self.browserObject, False , "Exception generated while verifying the power state of server(s) :: Error -> %s"%(str(e) + format_exc())
            
    def getServerIp(self, complianceState, deploymentState):
        """
        Returns Server matching provided Compliance State and Deployment State
        """
        try:
            self.browserObject, status, result = self.getResources("Servers")
            if not status:
                return self.browserObject, status, result
            valid = [resource for resource in result if resource["Compliance"] == complianceState and resource["Deployment Status"].lower() == deploymentState.lower()]
            if len(valid) > 0:
                return self.browserObject, True, valid[0]["Management IP"]
            else:
                return self.browserObject, False, "Unable to find Server with Compliance State '%s' and Deployment State '%s'"%(complianceState,
                                                                                                                        deploymentState)
        except  Exception as e:
            return self.browserObject, False, "Error :%s"%str(e)
        
    def verifyDeploymentFirmwareUpdate(self, serverIp):
        """
        Verifies Firmware Update Status
        """
        try:
            expectedUpdateStates= {"Updating service":("Non-Compliant", "Updating"),"Deploying":("Non-Compliant",), "In Use":("Compliant",), "Pending Updates":("Non-Compliant",),"Deployment Failed":("Update Failed",)}
            updateStates={}
            self.browserObject, status, result = self.getResources("Servers")
            if not status:
                return self.browserObject, status, result
            valid = [resource for resource in result if resource["Management IP"] == serverIp]
            if len(valid) <= 0:
                return self.browserObject, False, "Server %s Not Found"%serverIp
            deploymentState = valid[0]["Deployment Status"]
            if deploymentState=="Not in use":
                utility.execLog("Firmware update not initialized on Service Deployment initiation")
                return self.browserObject, False, "Firmware update not initialized on Service Deployment initiation"
            loopcount=0
            wait=180
            while loopcount<wait:
                self.browserObject, status, result = self.getResources("Servers")
                if not status:
                    return self.browserObject, status, result
                valid = [resource for resource in result if resource["Management IP"] == serverIp]
                deploymentState = valid[0]["Deployment Status"]
                compliance = valid[0]["Compliance"]
                updateStates[deploymentState]=compliance
                correctUpdateState= False
                #verify Update states
                for stateIndex in xrange(0,len(expectedUpdateStates[deploymentState])):
                    if updateStates[deploymentState] ==expectedUpdateStates[deploymentState][stateIndex]:
                        utility.execLog("Matching expected update state deployment state : %s and compliance state %s"%(deploymentState, compliance))
                        correctUpdateState= True
                if not correctUpdateState:
                    return self.browserObject, False, "Unexpected expected update states deployment state : %s and compliance state %s"%(deploymentState, compliance)
                #check for Depoyment complete
                if deploymentState in ("In Use", "Failed"):
                    utility.execLog("Deployment finished State: %s"%deploymentState)
                    break
                time.sleep(60)
                loopcount +=1
            if deploymentState=="Deploying":
                return self.browserObject, False, "Deployment exceeding expected time: Terminating the Testcase"
            return self.browserObject, True, "Update firmware on Deployment Matching expected update state of deployment and compliance status"
        except Exception as e:
            utility.execLog("Error while verifying update state: %s"%e)
            return self.browserObject, False, "Error while verifying update state: %s"%e
        
    def getReourceModelComplainceStatus(self, resourceType, resourceModel):
        """
        Returns Resource(s) Compliance Status for provided Model
        """
        try:
            statusDict={}
            self.browserObject, status, result = self.getResources(resourceType)
            if not status:
                return self.browserObject, status, result
            valid = [resource for resource in result if resourceModel.lower() in resource["Model"].lower()]
            utility.execLog("Identified Resources '%s'"%valid)
            for res in valid:
                statusDict[res["Management IP"]] = res["Compliance"]
            return self.browserObject, True, statusDict
        except Exception as e:
            return self.browserObject, False, "Error %s"%e
        
    def changeResourceState(self, resourceIpList, resourceState="Unmanaged"):
        """
        Changes Resource State
        """
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            utility.execLog("Trying to change Resource State")
            loopCount=3
            while loopCount:
                try:
                    for resourceIP in resourceIpList:
                        self.selectResource(resourceIP, True)
                    utility.execLog("Successfully selected Resources '%s'"%resourceIpList)
                    utility.execLog("Scrolling to top of the window")
                    self.browserObject.execute_script("window.scrollTo(0, 0);")
                    utility.execLog("Checking if Resource Options are collapsed")
                    expanderStatus = self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("export_all_option"))), action="IS_DISPLAYED")
                    if not expanderStatus:
                        utility.execLog("Resource options are collapsed....expanding to view options")
                        self.browserObject.execute_script("return arguments[0].scrollIntoView();", self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab))))
                        self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("options_expander"))), action="CLICK")
                        if resourceState.lower() == "unmanaged":
                            option = self.ResourcesObjects("select_unmanaged_state")
                        elif resourceState.lower() == "reserved":
                            option = self.ResourcesObjects("select_reserved_state")
                        else:
                            option = self.ResourcesObjects("select_managed_state")
                        utility.execLog("Selecting option '%s'"%option)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, option)), action="CLICK")
                    else:
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("change_resource_state"))), action="SELECT", setValue=resourceState)
                    time.sleep(2)
                    try:
                        self.handleEvent(EC.title_contains("Confirm"))
                        utility.execLog("Confirm Box Loaded...Confirming to Change Resource State")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
                    except:
                        utility.execLog("Failed to Verify Confirm Box for Changing Resource State '%s':: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Confirm"))
                        return self.browserObject, False, "Failed to Verify Confirm Box for Changing Resource State '%s':: Actual --> '%s' :: Expected --> '%s'"%(self.browserObject.title, "Confirm")
                    try:
                        self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                        errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects('cancel_button'))), action="CLICK")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
                        utility.execLog("Failed to Change Resource State for Resources : '%s' :: Error -> '%s'" % (resourceIpList, 
                                                                                                        str(errorMessage)))
                        return self.browserObject, False, "Failed to Change Resource State for Resources : '%s' :: Error -> '%s'" % (resourceIpList, 
                                                                                                        str(errorMessage))
                    except:
                        utility.execLog("Successfully Initiated Change Resource State for Resources : '%s'" %(resourceIpList))
                        return self.browserObject, True, "Successfully Initiated Change Resource State for Resources : '%s'" %(resourceIpList)
                except:
                    loopCount = loopCount - 1
                    utility.execLog("Resource page not loaded successfully try to reload")
                    self.browserObject.refresh()
                    time.sleep(10)
            utility.execLog("Maximum retries (3) exceeded, Unable to Change Resource '%s' State"%resourceIpList)
            return self.browserObject, False, "Maximum retries (3) exceeded, Unable to Change Resource '%'s State"%resourceIpList
        except Exception as e:
            utility.execLog("Exception while trying to Change Resource(s) '%s' State to '%s' :: Error -> %s"%(resourceIpList, resourceState, str(e) + format_exc()))
            return self.browserObject, False, "Exception while trying to Change Resource(s) '%s' State to '%s' :: Error -> %s"%(resourceIpList, resourceState, str(e) + format_exc())
    
    def getInprogressJobs(self):
        """
        Get Jobs Running
        """
        jobList = []
        try:
            utility.execLog("Checking if View Jobs Running window is visible")
            status = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects('verify_view_jobs_window'))), action="GET_ATTRIBUTE_VALUE", attributeName="class")
            if "ng-hide" in status:
                utility.execLog("'View Jobs Running' window is not visible so clicking on it")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects('jobs_inprogress_count'))), action="CLICK")
            utility.execLog("'View Jobs Running' window is visible")
            #Getting Discovery Jobs
            try:
                jobName = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("get_discovery_jobs"))), action="GET_TEXT")
                if jobName != "":
                    jobList.append({"Discovery": map(lambda y:str(y).strip(), jobName.split("\n"))})
            except:
                pass
            #Getting Update Jobs
            try:
                jobName = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("get_update_jobs"))), action="GET_TEXT")
                if jobName != "":
                    jobList.append({"UpdateJobs": map(lambda y:str(y).strip(), jobName.split("\n"))})
            except:
                pass
            self.browserObject.refresh()
        except Exception as e:
            utility.execLog("Exception while trying to read In-Progress Jobs :: Error %s"%(str(e) + format_exc()))
        finally:
            return jobList
        
    def waitJobComplete(self, job):
        '''
        Wait for an Event to Complete
        '''
        try:
            loopCount=1
            completed = False
            while loopCount <= 60:
                utility.execLog("Fetching Jobs In-Progress")
                jobList = self.getInprogressJobs()
                utility.execLog("Jobs In-Progress '%s'"%jobList)
                if len(jobList) > 0:
                    if job == "Discovery":
                        if not jobList[0].has_key(job):
                            completed = True
                            break
                    else:
                        updateJobs = [ujob["UpdateJobs"] for ujob in jobList if ujob.has_key("UpdateJobs")]
                        if len(updateJobs) > 0:
                            updateJobs = updateJobs[0]
                            pending = [ujob for ujob in updateJobs if job in ujob]
                            if len(pending) <= 0:
                                completed = True
                                break
                        else:
                            completed = True
                            break
                else:
                    completed = True
                    break
                utility.execLog("'%s' Job is In-Progress so waiting for 1 min to check again :: Iteration '%s'"%(job, loopCount)) 
                time.sleep(60)
                loopCount = loopCount + 1
                continue
            try:
                utility.execLog("Checking if View Jobs Running window is visible")
                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects('verify_view_jobs_window'))))
                utility.execLog("Checking if View Jobs Running window is closed")                
            except:
                utility.execLog("'View Jobs Running' window is visible so clicking on it to close")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects('jobs_inprogress_count'))), action="CLICK")
                utility.execLog("'View Jobs Running' window is closed")
            if completed:
                utility.execLog("'%s' Job Completed"%job)
                return self.browserObject, True, "'%s' Job Completed"%job
            else:
                utility.execLog("%s Job Exceeding expected time '60' mins"%job)
                return self.browserObject, False, "%s Job Exceeding expected time '60' mins"%job
        except Exception as e:
            return self.browserObject, False, "Exception while trying to verify '%s' Job Completion :: Error %s"%(job, str(e) + format_exc())
        
    def serverUpdatedCheck(self, checkState):
        """
        Verify Firmware Update is Disabled for Error Services
        """
        try:
            self.browserObject, status, result = self.getResources("Servers")
            if not status:
                return self.browserObject, status, result
            valid = [resource for resource in result if resource["Health"].lower() in ("error", "unknown")]
            if len(valid) <= 0:
                return self.browserObject, False, "Resource not found"
            utility.execLog("Identified Resources '%s'"%valid)
            resList = [res["Management IP"] for res in valid]
            self.browserObject, status, result = self.isUpdateFirmwareEnabled(resList)            
            if status:
                utility.execLog("Verified that firmware update button is disabled for Error Services")
                return True
            else:
                utility.execLog("Failed to Verify that firmware update button is disabled for Error Services")
                return False
        except:
            utility.execLog("Exception has occurred")
            return False 
    
    def createCredential(self, credential):
        """
        Creates a New Credential
        """
        try:
            utility.execLog("Creating New Credential")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("create_chassis_cred")%{"rid":1})), action="CLICK")
            utility.execLog("Clicked on Create Credential....Verifying Page Title")
            if "Create Credentials" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify Page Title :: Expected 'Create Credentials' , Actual '%s'"%self.browserObject.title
            else:
                utility.execLog("Verified Page Title :: Expected 'Create Credentials' , Actual '%s'"%self.browserObject.title)
            utility.execLog("Entering Credential Name '%s'"%credential["Name"])
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_name"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_name"))), action="SET_TEXT", setValue=credential["Name"])
            utility.execLog("Entered Credential Name '%s'....Entering Credential UserName '%s'"%(credential["Name"], credential["Username"]))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_username"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_username"))), action="SET_TEXT", setValue=credential["Username"])
            utility.execLog("Entered Credential UserName '%s'....Entering Credential Password '%s'"%(credential["Username"], credential["Password"]))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_password"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_password"))), action="SET_TEXT", setValue=credential["Password"])
            utility.execLog("Entered Credential Password '%s'....Entering Credential Confirm Password '%s'"%(credential["Password"], credential["Password"]))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_confirm_password"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_confirm_password"))), action="SET_TEXT", setValue=credential["Password"])            
            #Set Domain
            if credential["Domain"]:
                utility.execLog("Entering Credential Domain '%s'"%(credential["Domain"]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_domain"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_domain"))), action="SET_TEXT", setValue=credential["Domain"])
                utility.execLog("Entered Credential Domain '%s'"%(credential["Domain"]))
            #Set SNMP Config
            if credential["SNMP"]:
                utility.execLog("Entering Credential SNMP String '%s'"%(credential["SNMP"]))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_snmp"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("credential_snmp"))), action="SET_TEXT", setValue=credential["SNMP"])
                utility.execLog("Entered Credential SNMP String '%s'"%(credential["SNMP"]))
            utility.execLog("Entered Credential Confirm Password '%s'....Clicking on 'Save'"%("calvin"))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("save_credential"))), action="CLICK")
            utility.execLog("Able to create Credential '%s'" % credential["Name"])     
            try:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                #INCOMPLETE: Processing Error Messages
                errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects('cancel_button'))), action="CLICK")
                return self.browserObject, False, "Failed to Create Credential :: '%s' :: Error -> '%s'"%(credential["Name"], 
                                                    str(errorMessage))
            except:
                return self.browserObject, True, "Successfully Created Credential '%s'"%credential["Name"]
        except Exception as e:
            return self.browserObject, False, "Failed to Create Credential :: {} of Type :: {} :: Error -> {}"\
                .format(credential["Name"], credential["Type"], e)

    def changeIOSwitchState(self, resourceType, ServerIPPool, IoSwitchIp, resourceState):
        """
        Description:
             API to get IO switches based on the chassis we are using for the deployment and convert the switch state
        """
        try:
            ioSwitches = []
            self.browserObject, status, result = self.getResources(resourceType)
            if not status:
                return self.browserObject, status, result
            for resource in result:
                self.browserObject, status, result = self.getResourceDetails(resource["Management IP"])
                if not status:
                    return self.browserObject, status, result
                if resource.has_key("Blades"):
                    for blade in resource["Blades"]:
                        if blade["Management IP"] in ServerIPPool:
                            if resource.has_key("IOMS"):
                                for iom in resource["IOMS"]:
                                    if iom["Management IP"] in IoSwitchIp:
                                        ioSwitches.append(iom["Management IP"])
                utility.execLog("Navigating back to the resources page")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("resources_link"))), action="CLICK")
                time.sleep(3)
            utility.execLog("Setting the Resource Type value to All")
            self.resourceFilterOptions("All")
            #Executes this loop if Io switches are identified for the current running configuration chassis and if we want to convert the state to Unmanaged 
            if len(ioSwitches)>0 and resourceState=='Unmanaged':
                COMMAND="conf \n  interface range tengigabitethernet 0/1-32 \n  no vlan tagged 33-62 \n no vlan untagged"
                for eachIoIp in ioSwitches:
                    utility.execLog("Connecting to the Switch '%s'"%eachIoIp)
                    status, result= self.connectSSH("Switch", eachIoIp, COMMAND)
            else:
                status, result = True,"Resource State needs to be in Managed, hence skipped connecting to the switch step"
                utility.execLog("No IO switch for the chassis or switch state to be converted is not Unmanaged")
            #Converts the switch state to the required option
            switchIPS = deepcopy(ioSwitches) 
            if status and "error" not in result:   
                self.browserObject, status, switches = self.getResources("Switches")
                if not status:
                    return self.browserObject, status, switches
                for eachIOIP in ioSwitches:
                    for switch in switches:
                        if eachIOIP == switch["Management IP"]:
                            if switch["Managed State"] == resourceState:
                                utility.execLog("Switch '%s' state is same as required '%s', so need not change State"%(eachIOIP, resourceState))
                                switchIPS.remove(eachIOIP)
                
                utility.execLog("Changing State of Switches '%s' to '%s'"%(switchIPS, resourceState))
                self.changeResourceState(switchIPS, resourceState)
                utility.execLog("Changed State of Switches '%s' to '%s'"%(switchIPS, resourceState))
                del switchIPS
                #After converting the state verifies the state value in the resource table
                self.browserObject, status, switches = self.getResources("Switches")
                if not status:
                    return self.browserObject, status, switches
                filtered = [switch for switch in switches if switch["Management IP"] in ioSwitches]
                
                self.browserObject, status, result = self.verifyState("Managed State", filtered, resourceState)
                if status:
                    utility.execLog("Successfully changed State to '%s' for Resources '%s"%(resourceState, ioSwitches))
                    return self.browserObject, True, "Successfully changed State to '%s' for Resources '%s"%(resourceState, ioSwitches)
                else:
                    utility.execLog("Failed to changed State to '%s' for Resources '%s"%(resourceState, result))
                    return self.browserObject, False, "Failed to changed State to '%s' for Resources '%s"%(resourceState, result)
        except Exception as e:
            return self.browserObject, False, "Exception while trying to change IO State for Switches :: Error -> %s"%(str(e) + format_exc())
    
    def getDeviceInfoTable(self, resourceIp):
        '''
        Fetch information from device-info table 
        '''
        try:
            deviceInfo={}
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            utility.execLog('Selecting resource %s'%resourceIp)
            self.browserObject, status, result = self.selectResource(resourceIp)
            if not status:
                return self.browserObject, status, result
            utility.execLog('Fetching device-info table data')
            totalRows=len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("device_info_count")))))
            for row in range(1, totalRows+1):
                try:
                    componentName = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("device_info_name")%{"rid":row})), action="GET_TEXT")
                    if "Firmware" in componentName: 
                        componentValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("view_compliance_rpt"))), action="GET_TEXT")                    
                    else:
                        componentValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("device_info_value")%{"rid":row})), action="GET_TEXT")
                    deviceInfo[componentName]=componentValue
                except:
                    pass
            utility.execLog("Device Info Table :: '%s'"%deviceInfo)
            return self.browserObject, True, deviceInfo
        except Exception as e:
            return self.browserObject, False, "Exception while reading Device Info Table :: Error -> '%s'"%(str(e) + format_exc())
    
    def verifyPerformanceMetrics(self, serverGeneration="13"):
        """
        View Details of a Resources
        """
        try:
            self.browserObject, status, result = self.getResources("Servers")
            if not status:
                return self.browserObject, status, result
            if serverGeneration == "13":
                model="3"
            else:
                model="2"            
            for resource in result:
                if str(resource["Model"]).strip()[-2] == model and resource["Health"].lower() == "green":
                    self.browserObject, status, result = self.selectResource(resource["Management IP"])
                    if not status:
                        return self.browserObject, status, result
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
                    utility.execLog("Able to click on 'View Details'")
                    time.sleep(10)
                    actualIP = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("view_details_header"))), action="GET_TEXT")
                    utility.execLog("Able to Read Info on 'View Details' Page for Resource IP :: %s"%(actualIP))
                    if resource["Management IP"].strip() == actualIP.strip():
                        utility.execLog("Verified 'View Details' for Resource :: %s"%(actualIP))
                    else:
                        return self.browserObject, False, "Failed to Verify 'View Details' for Resource :: %s"%(actualIP)
                    try:
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.ResourcesObjects("performance_metrics"))), action="IS_DISPLAYED")
                        utility.execLog("Performance metrics is displayed for Generation '%s' Server/Blade"%serverGeneration)
                        return self.browserObject, True, "Performance metrics is displayed for Generation '%s' Server/Blade"%serverGeneration
                    except:
                        return self.browserObject, False, "Performance metrics is not displayed for Generation '%s' Server/Blade"%serverGeneration
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("resources_link"))), action="CLICK")
            utility.execLog("Unable to find Server of Generation '%s'"%serverGeneration)
            return self.browserObject, False, "Unable to find Server of Generation '%s'"%serverGeneration
        except Exception as e:
            utility.execLog("Exception while trying to verify Performance Metrics for Server Generation '%s' :: Error -> "%(serverGeneration,
                                                                                            (str(e) + format_exc())))
            return self.browserObject, False, "Exception while trying to verify Performance Metrics for Server Generation '%s' :: Error -> "%(serverGeneration,
                                                                                            (str(e) + format_exc()))

    def verifyServerVCenterDetails(self, serverIP, dataCenterName="", clusterName="", serverOSIP="", vmHostName=""):
        """
        Verify the existence of vCenter Details for particular server
        """
        try:
            self.resourceFilterOptions("VM Manager")
            self.browserObject, status, result = self.selectResource(serverIP)
            if not status:
                return self.browserObject, status, result
            attrName = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("verify_vcenter_details"))), action="GET_ATTRIBUTE_VALUE", attributeName="textContent")
            attrNames = attrName.split("\n")
            vcenterInfo = [str(ele).strip() for ele in attrNames if ele.strip() != ""]
            if dataCenterName != "":
                if dataCenterName in vcenterInfo:
                    utility.execLog("Data Center '%s' is displayed in '%s' vCenter details "%(str(dataCenterName), str(serverIP)))
                else:
                    utility.execLog("Data Center '%s' is not displayed in '%s' vCenter details "%(str(dataCenterName), str(serverIP)))
                    return self.browserObject, False, "Data Center '%s' is not displayed in '%s' vCenter details "%(str(dataCenterName), str(serverIP))
            if clusterName != "":
                if clusterName in vcenterInfo:
                    utility.execLog("Cluster Name '%s' is displayed in '%s' vCenter details "%(str(clusterName), str(serverIP)))
                else:
                    utility.execLog("Cluster Name '%s' is not displayed in '%s' vCenter details "%(str(clusterName), str(serverIP)))
                    return self.browserObject, False, "Cluster Name '%s' is not displayed in '%s' vCenter details "%(str(clusterName), str(serverIP))
            if serverOSIP != "":
                if serverOSIP in vcenterInfo:
                    utility.execLog("Server OS IP '%s' is displayed in '%s' vCenter details "%(str(serverOSIP), str(serverIP)))
                else:
                    utility.execLog("Server OS IP '%s' is not displayed in '%s' vCenter details "%(str(serverOSIP), str(serverIP)))
                    return self.browserObject, False, "Server OS IP '%s' is not displayed in '%s' vCenter details "%(str(serverOSIP), str(serverIP))
            if vmHostName != "":
                if vmHostName in vcenterInfo:
                    utility.execLog("VM Host name '%s' is displayed in '%s' vCenter details "%(str(vmHostName), str(serverIP)))
                else:
                    utility.execLog("VM Host name '%s' is not displayed in '%s' vCenter details "%(str(vmHostName), str(serverIP)))
                    return self.browserObject, False, "VM Host name '%s' is not displayed in '%s' vCenter details "%(str(vmHostName), str(serverIP))
            return self.browserObject, True, "All vCenter Details are displayed for Server %s"%str(serverIP)        
        except Exception as e:
            utility.execLog("Exception while verifying '%s' vCenter details :: Error -> %s"%(str(serverIP), (str(e) + format_exc())))
            return self.browserObject, False, "Exception while verifying '%s' vCenter details :: Error -> %s"%(str(serverIP), (str(e) + format_exc()))
    
    def updateServerFirmware(self, updateAll=False, scheduleUpdate=False, changeManageState=None):
        """
        Update Firmware for Available Servers
        """
        try:
            self.browserObject, status, result = self.getResources("Servers")
            if not status:
                return self.browserObject, status, result
            nonCompliantServers = []            
            for resource in result:
                if resource["Compliance"] not in ("Compliant", "Updating", "Unknown") and resource["Deployment Status"] \
                            not in ("In Use", "Pending Updates", "Deployment Failed") and resource["Managed State"] =="Managed" \
                            and resource["Management IP"] == "100.68.64.182":
                            nonCompliantServers.append(resource)
                            if not updateAll:
                                break
            if len(nonCompliantServers) <= 0:
                utility.execLog("Non-Compliant, Managed, Not In-use Resource not found")
                return self.browserObject, True, "Non-Compliant, Managed, Not In-use Resource not found"
            else: 
                resIPS = [resource["Management IP"] for resource in nonCompliantServers]
                if changeManageState:
                    if nonCompliantServers[0]["Managed State"] == changeManageState:
                        pass    ##Resource already in desired state
                    else:
                        self.browserObject, status, result = self.changeResourceState(resIPS, changeManageState)
                        if not status:
                            return self.browserObject, status, result
                time.sleep(5)       
                for resourceName in resIPS:      
                    self.browserObject, status, result = self.selectResource(resourceName, select=True)
                    if not status:
                        return self.browserObject, False, result
                utility.execLog("Scrolling to top of the window")
                self.browserObject.execute_script("window.scrollTo(0, 0);")
                utility.execLog("Clicking on Update Resource")   
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("update_resource"))), action="CLICK")
                except:
                    utility.execLog("Update Resource button is disabled for Non-compliant Resource")
                    return self.browserObject, False, "Update Resource button is disabled for Non-compliant Resource"
                time.sleep(2)                                            
                if scheduleUpdate:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("schedule_update"))), action="CLICK")
                    timeNow = datetime.now()
                    scheduleTime = timeNow + timedelta(minutes=3)
                    scheduleTime = scheduleTime.strftime("%m/%d/%Y %H:%M %p")
                    utility.execLog("Current Time stamp '%s'....Scheduled Firmware update Time for Resource '%s'"%(timeNow.strftime("%m/%d/%Y %H:%M %p"), scheduleTime))
                    utility.execLog("Clearing Existing Schedule Date/Time")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("set_schedule_date"))), action="CLEAR")
                    utility.execLog("Cleared Existing Schedule Date/Time....Setting Schedule Time '%s'"%scheduleTime)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("set_schedule_date"))), action="SET_TEXT", setValue=scheduleTime)
                    utility.execLog("Able to Set Schedule Time '%s'"%scheduleTime)
                utility.execLog("Clicking on Apply Update")
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.ResourcesObjects("apply_firmware_update"))), action="CLICK")
                utility.execLog("Clicked on Apply Update..Verifying whether Confirm Box Loaded")
                currentTitle = self.browserObject.title
                if "Confirm" in currentTitle:
                    utility.execLog("Confirm Box Loaded...Confirming to Apply Updates")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
                else:
                    utility.execLog("Failed to Verify Confirm Box :: Actual --> '%s' :: Expected --> '%s'"%(currentTitle, "Confirm"))
                    return self.browserObject, False, "Failed to Verify Confirm Box :: Actual --> '%s' :: Expected --> '%s'" %(currentTitle, "Confirm")
                try:
                    self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                    errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                    return self.browserObject, False, "Failed to Update Resource(s) :: Error -> '%s'"%(str(errorMessage))
                except:
                    utility.execLog("No errors observed while Updating Firmware")
                if scheduleUpdate:
                    utility.execLog("Sleeping for '%s' seconds for Firmware Update to start"%(3*60 + 60))
                    time.sleep(3*60 + 60)
                utility.execLog("Wait for Server Update firmware to complete")
                self.browserObject, status, result=self.waitJobComplete("Firmware/Software Updating")
                if not status:
                    return self.browserObject, False, result
                self.browserObject, status,result=self.getResources("Servers")
                if not status:
                    return self.browserObject, False, result
                else:
                    serverDetailList=result
                utility.execLog("Get list of non-compliant servers after update")
                updateFailed=[server['Management IP'] for server in serverDetailList if server['Management IP'] in resIPS and server['Compliance'] != 'Compliant']
                #click on serverfirmware status
                if len(updateFailed)>0:
                    return self.browserObject, False, "Servers remain non-complaint after update '%s'"%updateFailed
                else:
                    #validating Compliance in iDRAC
                    self.browserObject, status, result = self.selectResource(resIPS[0])
                    if not status:
                        return self.browserObject, status, result
                    utility.execLog("Clicking on Firmware Compliance Report")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("view_compliance_rpt" ))), action="CLICK")
                    utility.execLog("Clicked on Firmware Compliance Report....Fetching current 'iDRAC' Firmware Version")
                    currentVersion = self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("current_fw_version")%{"component":"Integrated Dell Remote Access Controller"})), action="GET_TEXT")
                    utility.execLog("Fetched current 'iDRAC' Firmware Version '%s'....Closing Firmware Compliance Report"%currentVersion)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects("FormClose" ))), action="CLICK")
                    utility.execLog("Closed Firmware Compliance Report....Launching iDRAC")
                    self.browserObject, status, result = self.selectResource(resIPS[0])
                    if not status:
                        return self.browserObject, status, result
                    utility.execLog("Able to Select Resource '%s'....Clicking on 'View Details'"%(resIPS[0]))
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
                    utility.execLog("Clicked on 'View Details'....Fetching Title to verify Details Page")
                    if "Device Details" in self.browserObject.title:
                        utility.execLog("Verified Details Page....Clicking on Memory Tab")
                    else:
                        utility.execLog("Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title)
                        return self.browserObject, False, "Failed to verify Details Page....Expected Title : 'Device Details', Actual Title : '%s'"%self.browserObject.title
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("launch_idrac"))), action="CLICK")
                    utility.execLog("Clicked on 'Launch iDRAC GUI' link'....Fetching Current Browser Handle")
                    currentWindowHandle = self.browserObject.current_window_handle
                    utility.execLog("Fetched Current Browser Handle....Switching to iDRAC Window")
                    self.browserObject.switch_to_window(self.browserObject.window_handles[1])
                    utility.execLog("Switched to iDRAC Window....Setting iDRAC Username '%s'"%"root")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_user"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_user"))), action="SET_TEXT", setValue="root")
                    utility.execLog("Able to set iDRAC Username '%s'....Setting iDRAC Password '%s'"%("root", "calvin"))
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_pwd"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_pwd"))), action="SET_TEXT", setValue="calvin")
                    utility.execLog("Able to set iDRAC Password '%s'....Clicking on login"%("calvin"))
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_login"))), action="CLICK")
                    time.sleep(15)          
                    self.browserObject.switch_to_frame(self.handleEvent(EC.presence_of_element_located((By.NAME, "da"))))
                    self.browserObject.switch_to_frame(self.handleEvent(EC.presence_of_element_located((By.NAME, "help"))))
                    idracFwVersion = self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("idrac_fw_version" ))), action="GET_TEXT")
                    try:
                        utility.execLog("Closing iDRAC Tab")
                        self.browserObject.close()
                        utility.execLog("Closed iDRAC Tab....Moving back to Main Tab")
                        self.browserObject.switch_to_window(currentWindowHandle)
                        utility.execLog("Moved back to Main Tab")
                    except:
                        pass
                    if idracFwVersion == currentVersion:
                        utility.execLog("Verified correct Firmware updated in iDRAC")
                        return self.browserObject, True, "Resource is in Compliant State"
                    else:
                        utility.execLog("Incorrect Firmware updated in iDRAC")
                        return self.browserObject,False,"Firmware version indicated at iDrac mismatches the version indicated at Resources page"
        except Exception as e:
            utility.execLog("Exception while trying to Update Server Firmware :: Error -> %s"%str(e) + format_exc())
            return self.browserObject, False, "Exception while trying to Update Server Firmware :: Error -> %s"%str(e) + format_exc()

    def updateResource(self, resourceType, scheduleWait=None, updateAll=False):
        '''
        Description: Update Resource Firmware from resource page
        '''
        try:
            self.browserObject, status, result = self.getResources(resourceType)
            if not status:
                return self.browserObject, status, result
            nonCompliantServers = []            
            for resource in result:
                if resource["Compliance"] not in ("Compliant", "Updating", "Unknown") and resource["Deployment Status"] \
                            not in ("In Use", "Pending Updates", "Deployment Failed") and resource["Managed State"] =="Managed" \
                            and resource["Management IP"] == "100.68.64.182":
                    nonCompliantServers.append(resource)
                    if not updateAll:
                        break
            if len(nonCompliantServers) <= 0:
                utility.execLog("Non-Compliant, Managed, Not In-use Resource not found")
                return self.browserObject, True, "Non-Compliant, Managed, Not In-use Resource not found"
            else: 
                resIPS = [resource["Management IP"] for resource in nonCompliantServers]
                for resourceName in resIPS:      
                    self.browserObject, status, result = self.selectResource(resourceName, select=True)
                    if not status:
                        return self.browserObject, False, result
                utility.execLog("Scrolling to top of the window")
                self.browserObject.execute_script("window.scrollTo(0, 0);")   
                utility.execLog("Clicking on Update Resource")
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("update_resource"))), action="CLICK")
                except:
                    utility.execLog("Update Resource button is disabled for Non-compliant Resource")
                    return self.browserObject, False, "Update Resource button is disabled for Non-compliant Resource"
                time.sleep(2)                                            
                if scheduleWait:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("schedule_update"))), action="CLICK")
                    timeNow = datetime.now()
                    scheduleTime = timeNow + timedelta(minutes=scheduleWait)
                    scheduleTime = scheduleTime.strftime("%m/%d/%Y %H:%M %p")
                    utility.execLog("Current Time stamp '%s'....Scheduled Firmware update Time for Resource '%s'"%(timeNow.strftime("%m/%d/%Y %H:%M %p"), scheduleTime))
                    utility.execLog("Clearing Existing Schedule Date/Time")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("set_schedule_date"))), action="CLEAR")
                    utility.execLog("Cleared Existing Schedule Date/Time....Setting Schedule Time '%s'"%scheduleTime)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("set_schedule_date"))), action="SET_TEXT", setValue=scheduleTime)
                    utility.execLog("Able to Set Schedule Time '%s'"%scheduleTime)
                utility.execLog("Clicking on Apply Update")
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.ResourcesObjects("apply_firmware_update"))), action="CLICK")
                utility.execLog("Clicked on Apply Update..Verifying whether Confirm Box Loaded")
                currentTitle = self.browserObject.title
                if "Confirm" in currentTitle:
                    utility.execLog("Confirm Box Loaded...Confirming to Apply Updates")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects('ConfirmYes'))), action="CLICK")
                else:
                    utility.execLog("Failed to Verify Confirm Box :: Actual --> '%s' :: Expected --> '%s'"%(currentTitle, "Confirm"))
                    return self.browserObject, False, "Failed to Verify Confirm Box :: Actual --> '%s' :: Expected --> '%s'" %(currentTitle, "Confirm")
                try:
                    self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.CommonObjects("RedBoxError"))))
                    errorMessage = str(self.handleEvent(EC.visibility_of_element_located((By.ID, self.CommonObjects("RedBoxErrorMessages"))), action="GET_TEXT"))
                    return self.browserObject, False, "Failed to Update Resource(s) :: Error -> '%s'"%(str(errorMessage))
                except:
                    utility.execLog("No errors observed while Updating Firmware")
                if scheduleWait:
                    utility.execLog("Sleeping for '%s' seconds for Firmware Update to start"%(scheduleWait*60 + 60))
                    time.sleep(scheduleWait*60 + 60)
                utility.execLog("Wait for Firmware Update to complete")
                self.browserObject, status, result=self.waitJobComplete("Firmware/Software Updating")
                if not status:
                    return self.browserObject, False, result
                self.browserObject, status,result=self.getResources(resourceType)
                if not status:
                    return self.browserObject, False, result
                else:
                    serverDetailList=result
                utility.execLog("Get list of non-compliant resources after update")
                updateFailed=[server['Management IP'] for server in serverDetailList if server['Management IP'] in resIPS and server['Compliance'] != 'Compliant']
                if len(updateFailed)>0:
                    return self.browserObject, False, "Some Resources remain non-complaint after update '%s'"%updateFailed
                else:
                    return self.browserObject, True, "Firmware Update completed successfully on selected Resources '%s'"%resIPS
        except Exception as e:
            utility.execLog("Exception while trying to Update Resource Firmware :: Error -> %s"%str(e) + format_exc())
            return self.browserObject, False, "Exception while trying to Update Resource Firmware :: Error -> %s"%str(e) + format_exc()
    
    def getCustomBundle_CompilaneReports(self, resourceType):
        """
        Description:
            API to check custom bundles compilance report for switches & storage
        """
        try:
            self.browserObject, status, result = self.getResources(resourceType)
            if not status:
                return self.browserObject, status, result
            for resource in result:
                self.browserObject, status, result = self.selectResource(resource["Management IP"])
                if not status:
                    return self.browserObject, status, result
                utility.execLog("Clicking on Firmware Compliance Report")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("view_compliance_rpt" ))), action="CLICK")
                utility.execLog("Clicked on Firmware Compliance Report....Fetching current 'iDRAC' Firmware Version")
                currentVersion = self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("current_fw_version")%{"component":"Integrated Dell Remote Access Controller"})), action="GET_TEXT")
                utility.execLog("Fetched current 'iDRAC' Firmware Version '%s'....Closing Firmware Compliance Report"%currentVersion)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.CommonObjects("FormClose" ))), action="CLICK")
                return self.browserObject, True,"Successfully searched "
        except Exception as e:
            utility.execLog("Exception while trying to verify Custom Bundle Resource Firmware :: Error -> %s"%str(e) + format_exc())
            return self.browserObject, False, "Exception while trying to verify Custom Bundle Resource Firmware :: Error -> %s"%str(e) + format_exc()
    
    def getServersPortView(self, serviceName, serverName):
        """
        Description:
            API to get Deployed Services and Navigate to PortView Page
        """
        try:
            self.browserObject, status, result = self.getResources("Servers", serviceName=serviceName)
            if not status:
                return self.browserObject, status, result
            utility.execLog("Checking Server Count: %d" %len(result))
            if len(result)>0:
                for resource in result:
                    existingIP = resource["Management IP"]
                    self.browserObject, status, result = self.selectResource(existingIP)                    
                    if status:
                        utility.execLog("Able to Select Row with Resource IP :: %s"%())
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
                        utility.execLog("Able to click on 'View Details'")
                        actualIP = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("view_details_header"))), action="GET_TEXT")
                        utility.execLog("Able to Read Info on 'View Details' Page for Resource IP :: %s"%(actualIP))
                        if existingIP.strip() == actualIP.strip():
                            utility.execLog("Verified 'View Details' for Resource :: %s"%(actualIP))
                        time.sleep(5)
                        return self.browserObject, True, "Successfully found the Deployed Service and navigating to the Port View page"
                    else:
                        return self.browserObject, False, result
            else:
                return self.browserObject, False, "There are no Servers." 
        except Exception as e:
            return self.browserObject, False, "Unable to navigate to the Port View page for specified Server or find the Service Name :: Error -> %s" %(str(e) + format_exc())
    
    def viewComplianceReportSwitch(self, resourceIp, resourceType, version=""):
        """
        Fetches Switch Firmware Version
        """
        try:
            self.browserObject, status, result = self.getResourceFirmwareVersion(resourceType, resourceIp)
            if not status:
                return self.browserObject, status, result
            currentVersion = result[0]["Current Version"]
            expectedVersion = result[0]["Expected Version"]
            curentVersion=float(currentVersion[0:3])
            if((curentVersion == version) and (curentVersion < 9.9)):
                return self.browserObject, True, "Current version is Lower than 9.9"
            elif((curentVersion == version) and (curentVersion >= version) ):
                utility.execLog("IOM verified successfully Current version is %s and expexted version %s"%(curentVersion, expectedVersion))
                return self.browserObject, True, "IOM verified successfully"
            else:
                utility.execLog("Current version is higher than 9.6 please downgrade the FW")
                return self.browserObject, True, "Current version is higher than 9.6 please downgrade the FW"
        except Exception as e:
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("close_compliance_rpt" ))), action="CLICK")
            except:
                pass
            utility.execLog("Exception while trying to read Switch Firmware Version :: Error -> %s"%str(e) + format_exc())
            return self.browserObject, False, "Exception while trying to read Switch Firmware Version :: Error -> %s"%str(e) + format_exc()
    
    def verifyUsageStatsDisplayed(self):
        '''
        Description: For 13G servers on viewing details
        System Usage, CPU usage, Memory and I/O usage values are displayed along with graphs and pie charts
        '''
        try:            
            self.browserObject, status, result = self.getResources("Servers")
            if not status:
                return self.browserObject, status, result
            resourceFound=False
            usageCategories={"System Usage":["systemUsageHistoricalData", "overallSystemUsage", "dLabel"], "CPU Usage":["cpuUsageHistoricalData", "cpuSystemUsage", "dLabel2"], 
                                            "Memory Usage":["memoryUsageHistoricalData", "memorySystemUsage", "dLabel2"], "I/O Usage":["ioUsageHistoricalData", "ioSystemUsage", "dLabel2"]}
            for resource in result:
                if str(resource["Model"]).strip()[-2] == "3" and resource["Health"].lower() == "green":
                    self.browserObject, status, result = self.selectResource(resource["Management IP"])
                    if not status:
                        return self.browserObject, status, result
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_details"))), action="CLICK")
                    resourceFound=True
            if not resourceFound:
                return self.browserObject, False, "Resource not found"
            time.sleep(10)
            #Listing Usage categories present
            for category in usageCategories.keys():
                try:
                    self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("device_stats")%{"refName":category})), action="IS_DISPLAYED")
                    utility.execLog("'%s' is Displayed"%category)
                except:
                    return self.browserObject, False, "Usage category %s information unavailable"%category
            #Verify historical graphs displays values
            for categoryName, categoryValue in usageCategories.items():
                try:
                    utility.execLog("Verifying '%s' Historical data graph values present"%categoryName)
                    xpath=self.ResourcesObjects("historical_graphs")%{"refName":categoryValue[0]}
                    usageGraphValueList = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
                    if usageGraphValueList >= 2:
                        utility.execLog("'%s' graph is reflecting values"%categoryName)
                    else:
                        utility.execLog("'%s' graph is not reflecting values"%categoryName)
                        return self.browserObject, False, "'%s' graph is not reflecting values"%categoryName
                except Exception as e:
                    return self.browserObject, False, "Graph values not found"
            utility.execLog("Usage graphs are not blank and displays values")
            #Verify Pie Charts are displayed for all usage categories
            for categoryName, categoryValue in usageCategories.items():
                try:
                    utility.execLog("Verifying '%s' Historical data pie-chart is displayed"%categoryName)
                    xpath=self.ResourcesObjects("historical_graphs")%{"refName":categoryValue[1]}
                    usageGraphValueList = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
                    if usageGraphValueList >= 1:
                        utility.execLog("'%s' Historical data pie-chart is displayed"%categoryName)
                    else:
                        utility.execLog("'%s' Historical data pie-chart is not displayed"%categoryName)
                        return self.browserObject,False, "%s Historical data pie-chart is Not displayed"%categoryName
                except Exception as e:
                    return self.browserObject, False, "Pie-chart not available for '%s' usage"%categoryName
            #Verify Historical data graph x-axis dropdown and respective changes
            graphDurationXvalue={"Last Hour":"Minutes","Last Day":"Hours", "Last Week":"Days", "Last Month":"Days", "Last Year": "Months"}
            for categoryName, categoryValue in usageCategories.items():
                utility.execLog("Verifying %s Historical Data Graph changes scale on selecting different duration of usage"%categoryName)                
                filterPath=self.ResourcesObjects("chart_filter")%{"refName":categoryValue[0]}                
                for duration, xScale in graphDurationXvalue.items():
                    utility.execLog("Clicking on Filter Option Link")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, filterPath)), action="CLICK")
                    utility.execLog("Clicked on Filter Option Link")
                    xpath=self.ResourcesObjects("select_chart_filter")%{"refName":categoryValue[0], "refOption":duration}
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    time.sleep(2)
                    xpath=self.ResourcesObjects("chart_xaxis")%{"refName":categoryValue[0]}
                    xAxisScale= self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    if xScale in xAxisScale:
                        utility.execLog("For '%s' graph-- duration selected '%s'-- X-axis scale changes to '%s'"%(categoryName, duration, xAxisScale))
                    else:
                        return self.browserObject, False, "%s Usage History graph's x-axis scale doesn't alter on altering period"%categoryName
            utility.execLog("X value of graph verified to change on selecting different duration")
            return self.browserObject, True, "System Usage, CPU Usage, Memory and I/O Usage values are displayed along with graphs and pie charts"
        except Exception as e:
            utility.execLog("Exception while trying to verify Usage Stats :: Error -> %s"%str(e) + format_exc())
            return self.browserObject, False, "Exception while trying to verify Usage Stats :: Error -> %s"%str(e) + format_exc()
    
    def getResourceFirmwareVersion(self, resourceType, resourceIp):
        """
        Fetches Resource Firmware Info
        """        
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Selecting Resource Type 'All'")
            self.resourceFilterOptions("All")
            self.browserObject, status, result = self.selectResource(resourceIp)
            if not status:
                return self.browserObject, status, result
            time.sleep(5)
            utility.execLog("Clicking on Firmware Compliance Report")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("view_compliance_rpt"))), action="CLICK")
            utility.execLog("Clicked on Firmware Compliance Report....Selecting 'Firmware' Component")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("select_firmware_component"))), action="CLICK")
            utility.execLog("Selected 'Firmware' Component")
            firmwareDetails=[]
            totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("generic_table_header")%{"tid":self.ResourcesObjects("firmware_table")}))))
            utility.execLog("Total Number of Columns : %s"%totalColumns)
            tableColumns = []
            for col in range(2, totalColumns + 1):
                xpath = self.ResourcesObjects("generic_table_header_col")%({"hid":col, "tid":self.ResourcesObjects("firmware_table")})
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch Table Columns '%s'"%(tableColumns))
            try:
                totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, 
                                        self.ResourcesObjects("generic_row_count")%{"tid":self.ResourcesObjects("firmware_table")}))))
            except:
                utility.execLog("Firmware info not found")
                totalRows = 0
            utility.execLog("Total Number of Rows : %s"%str(totalRows))     
            for row in range(1, totalRows + 1):
                tableElements = []
                for col in range(2, totalColumns + 1):
                    xpath = self.ResourcesObjects("generic_table_element")%({"rid":row, "cid":col, "tid":self.ResourcesObjects("firmware_table")})
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Able to fetch Info '%s'"%(str(tempDict)))
                firmwareDetails.append(tempDict)
            utility.execLog("Fetched Firmware Information....Closing Firmware Compliance Report")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("close_compliance_rpt" ))), action="CLICK")
            return self.browserObject, True, firmwareDetails
        except Exception as e:
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.ResourcesObjects("close_compliance_rpt" ))), action="CLICK")
            except:
                pass
            utility.execLog("Exception while trying to fetch Firmware Details :: Error -> %s"%str(e) + format_exc())
            return self.browserObject, False, "Exception while trying to fetch Firmware Details :: Error -> %s"%str(e) + format_exc()
    
    def verifyStatus(self, resourceType, healthType, resourceIP):
        """
        Verifies Health Status of the provided Resource 
        """
        try:
            utility.execLog("Selecting 'All Resources' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.resourceTab)), action="CLICK")
            utility.execLog("Selected 'All Resources' Tab....Fetching Resources")
            self.browserObject, status, resources = self.getResources(resourceType, healthType)
            if not status:
                return self.browserObject, status, resources
            self.browserObject, status, result = self.selectResource(resourceIP)
            if not status:
                return self.browserObject, status, result
            resInfo = [resource for resource in resources if resource["Management IP"] == resourceIP]
            if len(resInfo) <= 0:
                return self.browserObject, False, "Failed to read Resource '%s' Information"%resourceIP
            manufacture_model = resInfo[0]["Model"]
            utility.execLog("Manufacturer model : %s"%manufacture_model)            
            #actions = ActionChains(self.browserObject)            
            #tool_h = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("health_tooltip")%{"refName":resourceIP})))
            #actions.move_to_element(tool_h.click())
            healthStatus = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("health_tooltip")%{"refName":resourceIP})), action="GET_ATTRIBUTE_VALUE", attributeName="title")
            if healthStatus !="":
                utility.execLog("Health Tool tip '%s' for resource '%s'"%(healthStatus, resourceIP))
                return self.browserObject, True, "Health Tool tip '%s' for resource '%s'"%(healthStatus, resourceIP)
            else:
                utility.execLog("Health Tool tip is blank for resource '%s'"%(resourceIP))
                return self.browserObject, False, "Health Tool tip is blank for resource '%s'"%(resourceIP)
        except Exception as e:
            utility.execLog("Exception while trying to click on Health status  :: Error -> %s"%(str(e) + format_exc()))
            return self.browserObject, False,"Exception while trying to click on Health status  :: Error -> %s"%(str(e) + format_exc())
    
    #===========================================================================
    # def readDatacenterTree(self, pattern, count=1, maxCount=2, child=1, maxChild=5):
    #     """
    #     """
    #     xpattern = pattern + "[%d]"%child        
    #     ypattern = xpattern + "/ul/li"
    #     attrName = self.browserObject.find_element_by_xpath(xpattern).get_attribute("textContent")
    #     className = self.browserObject.find_element_by_xpath(xpattern).find_element_by_tag_name("i").get_attribute("class")
    #     attrNames = attrName.split("\n")
    #     attrName = [str(ele).strip() for ele in attrNames if ele.strip() != ""][0]
    #     print attrName
    #     maxCount = len(self.browserObject.find_elements_by_xpath(ypattern))
    #     if maxCount > 0:            
    #         zpattern = ypattern + "[%d]"%count
    #         count = count + 1            
    #         self.readDatacenterTree(zpattern, count, child)
    #     else:
    #         zpattern = pattern.rsplit("/", 2)[0]
    #         self.readDatacenterTree(zpattern, count, child)
    #     if len(content) <= 0:
    #         content = {}
    #         elems = len(self.browserObject.find_elements_by_xpath(pattern))
    #         for bIndex in xrange(1, elems + 1):
    #             xpattern = pattern + "[%d]"%bIndex
    #             attrName = self.browserObject.find_element_by_xpath(xpattern).get_attribute("textContent")
    #             className = self.browserObject.find_element_by_xpath(xpattern).find_element_by_tag_name("i").get_attribute("class")
    #             attrNames = attrName.split("\n")
    #             attrName = [str(ele).strip() for ele in attrNames if ele.strip() != ""][0]
    #             if "folder" in className:
    #                 content["Folders"] = {attrName:{}}
    #             elif "cluster" in className:
    #                 content["Clusters"] = {attrName:{}}
    #             elif "host" in className:
    #                 content["Hosts"] = {attrName:{}}
    #         return self.readDatacenterTree(content, pattern, {})
    #     else:
    #         for itmKey, itmVal in content.items():
    #             xpattern = pattern + "[contains(.,'%s')]/ul/li"
    #             elems = len(self.browserObject.find_elements_by_xpath(xpattern))
    #             if len(elems) > 0:
    #                 for bIndex in xrange(1, elems + 1):
    #                     ypattern = xpattern + "[%d]"%bIndex
    #                     attrName = self.browserObject.find_element_by_xpath(ypattern).get_attribute("textContent")
    #                     className = self.browserObject.find_element_by_xpath(ypattern).find_element_by_tag_name("i").get_attribute("class")
    #                     attrNames = attrName.split("\n")
    #                     attrName = [str(ele).strip() for ele in attrNames if ele.strip() != ""][0]
    #                     if "folder" in className:
    #                         itmVal["Folders"] = {attrName:{}}
    #                     elif "cluster" in className:
    #                         itmVal["Clusters"] = {attrName:{}}
    #                     elif "host" in className:
    #                         itmVal["Hosts"] = {attrName:{}}
    #                     elif "vm" in className:
    #                         itmVal["Vms"] = {attrName:{}}
    #                     content[itmKey] = itmVal
    #             else:
    #                  continue
    #                 
    #         
    #     exitPattern = pattern.split("/")
    #     if len(exitPattern) < 2:
    #         return content, pattern
    #     try:
    #         attrName = self.browserObject.find_element_by_xpath(pattern).get_attribute("textContent")
    #         className = self.browserObject.find_element_by_xpath(pattern).find_element_by_tag_name("i").get_attribute("class")            
    #         attrNames = attrName.split("\n")
    #         attrName = [str(ele).strip() for ele in attrNames if ele.strip() != ""][0]
    #         pattern = pattern + "/ul/li"
    #         elems = self.browserObject.find_elements_by_xpath(pattern)
    #         if len(elems) == 0:
    #             pattern = pattern.rsplit("/", 1)[0]
    #             self.browserObject.find_element_by_xpath(pattern)
    #     except:
    #         return 
    #===========================================================================
    
    def getAllVCenterDetails(self, vcenterIP):
        """
        Fetchs all Datacenters, Clusters and Hosts in a Vcenter
        """
        dcDict={}
        try:
            utility.execLog("Filtering Resources of type 'VM Manager'")
            self.resourceFilterOptions("VM Manager")
            self.browserObject, status, result = self.selectResource(vcenterIP)
            if not status:
                return self.browserObject, False, result
            datacenters = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.ResourcesObjects("get_datacenters")))))
            for dcIndex in xrange(1, datacenters + 1):
                dcName = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("get_datacenter")%{"refId":dcIndex})), action="GET_TEXT")
                utility.execLog("Retrieved Data center Name %s"%dcName)
                hasClusters = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.ResourcesObjects("get_clusters")%{"refDC":dcName}))))
                if hasClusters <= 1:
                    dcDict[dcName] = {}
                    continue                
                clusters = {}
                for cIndex in xrange(1, hasClusters+1):
                    clusterName = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("get_cluster")%{"refCluster":dcName, "refId":cIndex})), action="GET_TEXT")
                    utility.execLog("Retrieved Cluster Name %s"%clusterName)
                    hasHosts = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.ResourcesObjects("get_hosts")%{"refCluster":clusterName})))
                    hosts = {}
                    if len(hasHosts) <= 1:
                        clusters[clusterName] = hosts
                        continue
                    for hIndex in xrange(1, hasHosts+1):
                        hostName = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("get_host")%{"refCluster":clusterName, "refId":hIndex})), action="GET_TEXT")
                        utility.execLog("Retrieved Host Name %s"%hostName)
                        hasVMS = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.ResourcesObjects("get_vms")%{"refHost":hostName})))
                        vms=[]
                        if len(hasVMS) <= 1:
                            hosts[hostName] = vms
                            continue
                        for vIndex in xrange(1, hasVMS+1):
                            vmName = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.ResourcesObjects("get_vm")%{"refHost":hostName, "refId":vIndex})), action="GET_TEXT")
                            utility.execLog("Retrieved VM Name %s"%vmName)
                            vms.append(vmName)
                        hosts[hostName] = vms
                    clusters[clusterName] = hosts
                dcDict[clusterName] = clusters                                         
            #dcDict[dcName] = {"Hosts": hosts, "Clusters":clusters}
            utility.execLog("vCenter Details '%s'"%dcDict)
            return self.browserObject, True, dcDict
        except Exception as e:
            utility.execLog("Exception while verifying vCenter '%s' details :: Error -> %s"%(vcenterIP, str(e) + format_exc()))
            return self.browserObject, False, dcDict
    
    def getVCenterDetails(self, serverIP):
        """
        Verify the existence of vCenter Details for particular server
        """
        try:
            self.resourceFilterOptions("VM Manager")
            #===================================================================
            # self.browserObject, status, result = self.runInventory([serverIP], waitForCompletion=True)
            # if not status:
            #     return self.browserObject, status, result
            #===================================================================
            utility.execLog("Again selecting resource %s"%str(serverIP))
            self.browserObject, status, result = self.selectResource(serverIP)
            if not status:
                return self.browserObject, status, result
            utility.execLog("Expanding vCenter Details")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.ResourcesObjects("expand_vcenter_details"))), action="CLICK")
            time.sleep(1)
            dcDict={}
            datacenters = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.ResourcesObjects("get_datacenters"))))
            for elem in datacenters:
                dcText = str(elem.get_attribute("textContent"))
                utility.execLog("Retrieved Data center name %s"%str(dcText))
                elemToClick = elem.find_element_by_xpath("parent::li/span[1]")
                self.browserObject.execute_script("return arguments[0].scrollIntoView();", elemToClick)
#                 self.browserObject.execute_script("window.scrollBy(0, -150);")
                elemToClick.click()
                utility.execLog("Expanded cluster list for data center %s"%str(dcText))
                time.sleep(1)
                clusterDict= {}
                try:
                    clusterElems = elem.find_elements_by_xpath("parent::li//i[contains(@class,'cluster')]/parent::li/span[contains(@data-bind,'text:')]")
                    for clusterElem in clusterElems:
                        clusterText = str(clusterElem.get_attribute("textContent"))
                        hostList=[]
                        clusterElemToClick = clusterElem.find_element_by_xpath("parent::li/span[1]")
                        self.browserObject.execute_script("return arguments[0].scrollIntoView();", clusterElemToClick)
                        clusterElemToClick.click()
                        utility.execLog("Expanded host list for cluster %s"%str(clusterText))
                        time.sleep(1)
                        try:
                            hostElems = clusterElem.find_elements_by_xpath("parent::li/ul/li//i[contains(@class,'host')]/parent::li/span[contains(@data-bind,'text:')]")
                            for hostElem in hostElems:
                                hostText = str(hostElem.get_attribute("textContent"))
                                hostList.append(hostText)
                                utility.execLog("Added host %s to hostList %s"%(str(hostText), str(hostList)))
                        except:
                            utility.execLog("Cluster %s does not have any host"%str(clusterText))
                        utility.execLog("Adding hostList %s to clusterDict for cluster %s"%(str(hostList), str(clusterText)))
                        clusterDict[clusterText] = hostList
                except:
                    utility.execLog("Data Center %s does not have any cluster"%str(dcText))
                dcDict[dcText] = clusterDict
                utility.execLog("Added cluster Dictionary %s to the data center dictionary for data center %s"%(str(clusterDict), str(dcText)))
                utility.execLog("Data Center Dict is %s"%str(dcDict))
            return self.browserObject, True, dcDict
        except Exception as e:
            utility.execLog("Exception while verifying vCenter details for server %s  :: Error -> %s"%(str(serverIP),str(e)))
            return self.browserObject, False, dcDict
    
    def assignUserstoServer(self, serverName, userName):
        """
        API to assign Server to the user
        """
        self.selectOption("Resources")
        time.sleep(10)
        
        self.browserObject.find_element_by_id("tabserverpools").click()
        time.sleep(2)
        table = self.browserObject.find_element_by_id("page_serverpools")
        utility.execLog("Able to fetch %s Table Rows" % self.serverTable)
        tableBody = table.find_element_by_tag_name("tbody")
        tableBodyRows = tableBody.find_elements_by_tag_name("tr")
        for rowindex in xrange(0, len(tableBodyRows)):
            tableBodyRows[rowindex].find_elements_by_tag_name("td")
            server_name = tableBodyRows[rowindex].find_element_by_xpath("./td[2]/span").text
            if server_name == serverName:
                tableBodyRows[rowindex].find_element_by_xpath("./td[1]").click()
                self.browserObject.find_element_by_id("edit_serverpool_link").click()
                time.sleep(5)
                self.browserObject.find_element_by_xpath(".//*[@id='appHtml']/article/div/section/nav/ol/li[4]/a").click()
                time.sleep(2)
                tRows = self.browserObject.find_elements_by_xpath(".//*[@id='userTable']/tbody/tr")
                utility.execLog(len(tRows))
                for row in xrange(0, len(tRows)):
                    user_name = tRows[row].find_element_by_xpath("./td[2]").text
                    utility.execLog(user_name)
                    if user_name == userName:
                        checked = tRows[row].find_element_by_id("chkUser_" + str(row)).get_attribute("checked")
                        if not checked:
                            tRows[row].find_element_by_id("chkUser_" + str(row)).click()
                        time.sleep(2)
                        self.browserObject.find_element_by_id("btnWizard-Finish").click()
                        time.sleep(5)
                        utility.execLog("Assigned the user")
                        return
            else:
                status = "Server not found"
                utility.execLog(status)
                return status