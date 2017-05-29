"""
Author: P Suman/Saikumar Kalyankrishnan/HCL Team
Created/Modified: Oct 7th 2015/Mar 8th 2017
Description: Functions/Operations related to Templates Page
"""

import ast
from CommonImports import *
from libs.product.globalVars import storageComponent
from libs.product.pages.TranslateJSONString import TranslateJSONString
from datetime import timedelta
from datetime import datetime
from libs.product.objects.Common import Common
from libs.product.objects.Templates import Templates

class Templates(Navigation, Common, Templates):
    """
    Description:
        Class which includes Functions/Operations related to Templates
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Templates class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Templates"        
        utility.execLog("Templates")
        self.loopCount = 5
        self.temps = {"My Templates":[self.TemplatesObjects("tabMyTemplates"), self.TemplatesObjects('tabMyTemplates'), self.TemplatesObjects("categoryTemplates"), self.TemplatesObjects("viewTemplateList"), self.TemplatesObjects("viewTemplateIcons")],
                     "Sample Templates":[self.TemplatesObjects("tabSampleTemplates"), self.TemplatesObjects("templateTableSample"), "examplecategorytemplates", self.TemplatesObjects("viewTemplateList"), self.TemplatesObjects("viewTemplateIcons")]}
        self.staticIPList = {}
        self.manualServerList = []
    
    def loadPage(self):
        """
        Description:
            API to Load Templates Page
        """
        try:
            utility.execLog("Loading Templates Page...")
            self.browserObject, status, result =  self.selectOption("Templates")
            return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Templates Page %s" % (str(e) + format_exc())
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Templates Page              
        """
        if not title:
            title = self.pageTitle
        getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('title'))), action="GET_TEXT")
        if title not in getCurrentTitle:
            utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
            return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
        else:
            utility.execLog("Successfully validated Page Title: '%s'" % title)
            return self.browserObject, True, "Successfully validated Page Title: '%s'" % title
        
    def check_xpath_exists(self, xpath):
        try:
            self.handleEvent(EC.presence_of_element_located((By.XPATH,xpath)))
        except:
            return False
        return True
    
    def getOptions(self, templateName=None, templateType="My Templates", templateDetailOptions=False):
        """
        Description:
            API to get Options and their Accessibility for Templates Page 
        """
        optionList = {}
        try:
            if self.loopCount > 0:
                utility.execLog("Reading Templates Table")
                options = {"Add Template":self.TemplatesObjects('addTemplate'),"Export All":self.TemplatesObjects("exportAllTemplates")}
                                                                    
                for optName, optValue in options.items():
                    utility.execLog("Reading Option '%s'"%optName)
                    status=self.handleEvent(EC.presence_of_element_located((By.XPATH, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                    if status=='true': 
                        optionList[optName] = "Disabled"
                    else:
                        optionList[optName] = "Enabled"
                        
                if templateName:
                    self.browserObject, status, result = self.selectTemplate(templateName, templateType)                    
                    if status:
                        time.sleep(5)
                        opts = {"Edit":self.TemplatesObjects("editTemplate"), "Delete":self.TemplatesObjects("deleteTemplate"), "Deploy Service":self.TemplatesObjects("deployTemplate")}                    
                        for optName, optValue in opts.items():
                            #status = self.browserObject.find_element_by_id("templateDetails").find_element_by_xpath(".//ul/li/a[contains(text(), '%s')]"%optName).is_enabled()
                            status = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled") 
                            if templateType == "Sample Templates":
                                if status is None or status == "" or status == "true" or status == 'None':
                                    optionList[optName] = "Disabled"
                                else:
                                    optionList[optName] = "Enabled"
                            else:
                                if status == "true":
                                    optionList[optName] = "Disabled"
                                else:
                                    optionList[optName] = "Enabled"   
                        elems = self.handleEvent(EC.presence_of_all_elements_located((By.CLASS_NAME, "btn-default")), action="GET_ELEMENTS_BY_CLASS", setValue="btn-default")                      
                        for index in range(len(elems)):
                            status = elems[index].get_attribute("disabled")
                            text = str(elems[index].text)
                            if templateType == "Sample Templates":
                                if status is None or status == "" or status == 'None':
                                    if elems[index].is_displayed():
                                        optionList[text] = "Enabled"
                                    else:
                                        optionList[text] = "Disabled"
                                elif status == "true":
                                    optionList[text] = "Disabled"
                                else:
                                    optionList[text] = "Enabled"
                            else:
                                if status == "true":
                                    optionList[text] = "Disabled"
                                else:
                                    optionList[text] = "Enabled"
                            time.sleep(1)
                        if templateDetailOptions:
                            utility.execLog("Clicking on 'View Details'")
                            xpath = self.TemplatesObjects("viewDetails")
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                            time.sleep(10)
                            detailOpts = {"Detail Edit":self.TemplatesObjects("detailEdit"), "Delete Template":self.TemplatesObjects("detailDeletTemplate"), "Detail Deploy Service":self.TemplatesObjects("detailDeployService"),
                                      "Publish Template":self.TemplatesObjects("publishTemplate"), "Import Template":self.TemplatesObjects("detailImportTemplate"),
                                      "View All Settings":self.TemplatesObjects("viewAllSettings")}
                            for optName, optValue in detailOpts.items():
                                status = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                                if templateType == "Sample Templates":
                                    if status is None or status == "" or status == 'None':
                                        if self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="IS_DISPLAYED"): 
                                            optionList[optName] = "Enabled"
                                        else:
                                            optionList[optName] = "Disabled"
                                    elif status == "true":
                                        optionList[optName] = "Disabled"
                                    else:
                                        optionList[optName] = "Enabled"
                                else:
                                    if status == "true":
                                        optionList[optName] = "Disabled"
                                    else:
                                        optionList[optName] = "Enabled"
            optionList = dict((k, v) for k, v in optionList.iteritems() if k is not None and k != '')
            return self.browserObject, True, optionList
        except StaleElementReferenceException as se:
            utility.execLog("Templates Page reloaded '%s'"%(str(se) + format_exc()))
            self.loopCount = self.loopCount - 1
            return self.getOptions(templateName, templateType, templateDetailOptions) 
        except AssertionError as ae:
            utility.execLog("Templates Page reloaded '%s'"%(str(ae) + format_exc()))
            self.loopCount = self.loopCount - 1
            return self.getOptions(templateName, templateType, templateDetailOptions) 
        except Exception as e:
            return self.browserObject, False, "Unable to read Options on Templates Page :: Error -> %s"%(str(e) + format_exc())
    
    def getDetails(self, option, templateCategory, viewType):
        """
        Description:
            API to get existing Templates
            viewType: List (List/Icons)
        """
        templateList = []
        try:
            utility.execLog("Reading Templates Table")            
            time.sleep(10)            
            opts = {}            
            if option != "Both":                
                opts[option] = self.temps[option]
            else:
                opts = self.temps 
            for key, value in opts.items():
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, value[0])), action="CLICK")
                time.sleep(10)
                utility.execLog("Click on View Type '%s'"%viewType)
                if viewType == "Icons":
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, value[4])), action="CLICK")
                    time.sleep(10)
                    try:
                        if key == "My Templates":
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("templateThumbNail"))), action="CLICK")
                            time.sleep(5)
                    except:
                        pass
                    utility.execLog("Able to identify '%s' Table"%key)   
                    xpath = value[2]+'/li'                                   
                    templateCollection = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
                    if templateCollection > 0:
                        utility.execLog("Able to identify '%s' Table Header"%key)
                        for row in range(templateCollection + 1):
                            temp = {}
                            xpath= self.TemplatesObjects("categoryTemplates")+"/li[%i]/strong/a"%row
                            name = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                            xpath = "//table[@id='%s']/li[%i]/a"%(value[2], row)
                            tempId = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName="id") 
                            temp["Name"] = name
                            temp["Id"] = tempId                
                            utility.execLog("Able to fetch Templates Info '%s'"%str(temp))
                            templateList.append(temp)     
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, value[3])), action="CLICK")
                    time.sleep(10)
                    if templateCategory != "All" and key == "My Templates":
                        utility.execLog("Selecting Category '%s' on 'My Templates' Page"%templateCategory)
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "ddlView")), action="SELECT", setValue=templateCategory)
                        time.sleep(5)
                    #Fetch Table header Details
                    xpath = "//table[@id='%s']/thead/tr[1]/th"%value[1]                
                    totalColumns = len(self.browserObject.find_elements_by_xpath(xpath))
                    utility.execLog("Total Number of Columns : %s"%str(totalColumns))
                    tableColumns = []
                    for col in range(1, totalColumns + 1):
                        xpath = "//table[@id='%s']/thead/tr[1]/th[%i]"%(value[1], col)
                        colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        if colName == "":
                            tableColumns.append("Attachments")
                        else:
                            tableColumns.append(colName)
                        utility.execLog("Able to fetch Column Name: '%s'"%colName)
                    tableColumns = [x for x in tableColumns if x !='']
                    utility.execLog("Able to fetch %s Table Columns '%s'"%(value[1], str(tableColumns)))
                    xpath = "//table[@id='%s']/tbody/tr"%value[1]                
                    totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
                    utility.execLog("Total Number of Rows : %s"%str(totalRows))
                    for row in range(1, totalRows+1):
                        tableElements = []
                        for col in range(1, totalColumns + 1):
                            xpath = "//table[@id='%s']/tbody/tr[%i]/td[%i]"%(value[1], row, col)
                            colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                            tableElements.append(colValue)
                        tempDict = dict(zip(tableColumns, tableElements))
                        templateList.append(tempDict)      
                        utility.execLog("Able to fetch Template Info '%s'"%str(tempDict))
            utility.execLog("Able to fetch Templates Info '%s'"%str(templateList))
            return self.browserObject, True, templateList            
        except Exception as e:
            return self.browserObject, False, "Unable to read Templates :: Error -> %s"%(str(e) + format_exc())
        finally:
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, "tab-mytemplates")), action="CLICK")
            except:
                pass
    
    def selectTemplate(self, templateName, templateType="My Templates"):
        """
        Description:
            Selects specified Template
        """
        try:
            utility.execLog("Reading Templates Table")            
            opts = self.temps[templateType]
            utility.execLog("Clicking on '%s' Tab"%templateType)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, opts[0])), action="CLICK")
            try:
                utility.execLog("Clicking on View Type 'List'")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, opts[3])), action="CLICK")
            except:
                utility.execLog("View Type already set to 'List'")
            if templateType == "My Templates":
                self.handleEvent(EC.element_to_be_clickable((By.ID, "ddlView")), action="SELECT", setValue="All Categories", wait_time=10)
                time.sleep(5)
            #Identify Total Rows
            xpath = self.TemplatesObjects('searchTemplate').format(templateName)
            try:
                utility.execLog('Searching Template %s'%templateName)
                self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)))
                utility.execLog('Template Found')
                utility.execLog('Selecting Template %s'%templateName)
                xpath=xpath+"/parent::td/parent::tr/td[1]"
                self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLICK")
                utility.execLog("Able to Select Template '%s'"%(templateName))
                return self.browserObject, True, "Able to Select Template '%s'"%(templateName.encode('utf-8'))
            except:
                utility.execLog("Failed to Select Template '%s'"%(templateName)) 
                return self.browserObject, False, "Failed to Select Template '%s'"%(templateName.encode('utf-8'))
        except Exception as e:
            return self.browserObject, False, "Failed to select Template '%s' :: Error -> %s"%(templateName.encode('utf-8'), str(e) + format_exc())
    
    def deleteTemplate(self, templateName):
        """
        Deletes existing Template
        """
        try:
            self.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("deleteTemplate"))), action="CLICK")
            time.sleep(3)
            utility.execLog("Identifying Confirm Dialog box")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('confirmModalForm'))), action="CLICK")
            try:
                utility.execLog("Confirming to Delete Template '%s'"%templateName)
            except:
                utility.execLog("Confirming to Delete Template")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("submitConfirmForm"))), action="CLICK")
            time.sleep(3)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False)
                errorMessage = eleError.find_element_by_tag_name("h3").text
                time.sleep(2)
                return self.browserObject, False, "Failed to Delete Template :: '%s' :: Error -> '%s'"%(templateName.encode('utf-8'), 
                                                    str(errorMessage))
            except:
                try:
                    return self.browserObject, True, "Successfully Deleted Template '%s'"%templateName.encode('utf-8')  
                except:
                    return self.browserObject,True,"Succesfully Deleted Template"
                     
        except Exception as e:
            return self.browserObject, False, "Failed to Delete Template :: '%s' :: Error -> %s"%(templateName.encode('utf-8'), 
                                                    str(e) + format_exc())
    
    #===========================================================================
    def cloneTemplate(self, templateName, templateType='My Templates'):
        """
        Clones existing Template
        """
        try:
            utility.execLog("Selecting '%s' Tab"%templateType)
            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.temps[templateType][0])), action="CLICK")
            utility.execLog("Selected '%s' Tab"%templateType)
            utility.execLog("Selecting Template '%s'"%templateName)
            self.browserObject, status, result = self.selectTemplate(templateName,templateType)
            if not status:
                return self.browserObject, False, result
            utility.execLog("Selecting Option 'Clone'")
            xpath = self.TemplatesObjects("cloneTemplateBtn")
            self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLICK")
            time.sleep(3)
            utility.execLog("Selected Option 'Clone'")
            
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("templateNameClone"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("templateNameClone"))), action="SET_TEXT", setValue=templateName+"_clone")
            #Selecting Template Category
            utility.execLog("Selecting Template Category 'Automation'")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("templateCategoryClone"))), action="SELECT", setValue="Automation", wait_time=10)
                utility.execLog("Selected Template Category 'Automation'")
            except:
                utility.execLog("Template Category 'Automation' is not available so Creating Category 'Automation'")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("templateCategoryClone"))), action="SELECT", setValue="Create New Category")
                utility.execLog("Entering Template Category Name 'Automation'")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('createCategoryClone'))), action='SET_TEXT', setValue='Automation')
#                 select= Select(self.browserObject.find_element_by_id("category"))
#                 select.select_by_visible_text("Create New Category")
#                 self.handleEvent(EC.element_to_be_clickable((By.ID, "category")), action="SELECT", setValue="Create New Category")
#                 self.handleEvent(EC.element_to_be_clickable((By.ID, "category")), action="SET_TEXT", setValue="Create New Category")
                
#                 self.handleEvent(EC.element_to_be_clickable((By.ID, "createcategory")), action="CLEAR")
#                 self.handleEvent(EC.element_to_be_clickable((By.ID, "createcategory")), action="SET_TEXT", setValue="Automation")
            templateDescription = "Clone Template "+templateName
            #Entering Template Description
            utility.execLog("Entering Template Description '%s'"%templateDescription)
            utility.execLog("Entering Template Description")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("templateDescriptionClone"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("templateDescriptionClone"))), action="SET_TEXT", setValue=templateDescription)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('btnWizardNext'))), action="CLICK")
            time.sleep(5)
            try:
                error=self.handleEvent(EC.presence_of_element_located((By.XPATH,".//*[@id='page_templateinformation']/div/h3")),action="GET_TEXT")
                return self.browserObject, False, error
            except:
                utility.execLog("No Error detected, proceeding to next step")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardFinish'))), action="CLICK")
            time.sleep(20)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,".//*[@id='popupModal']//*[contains(text(),'OK')]")),action="CLICK")
            time.sleep(10)
            self.handleEvent(EC.presence_of_element_located((By.XPATH,".//*[@id='templatearticle']//*[contains(text(),'Templates')]")),action="CLICK")
            time.sleep(10)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,".//*[@id='popupModal']//*[contains(text(),'OK')]")),action="CLICK")
            time.sleep(5)
            return self.browserObject, True, "clone created for %s"%templateName
#             utility.execLog("Identifying Confirm Dialog box")
#             eleDialog = self.browserObject.find_element_by_id("confirm_modal_form")
#             eleDialog.click()
#             time.sleep(3)
#             utility.execLog("Confirming to Delete Template '%s'"%templateName)
#             eleDialog.find_element_by_id("submit_confirm_form").click()
#             time.sleep(3)
#             try:
#                 eleError = self.browserObject.find_element_by_class_name("clarity-error")
#                 errorMessage = eleError.find_element_by_tag_name("h3").text
#                 time.sleep(2)
#                 return self.browserObject, False, "Failed to Delete Template :: '%s' :: Error -> '%s'"%(templateName, 
#                                                     str(errorMessage))
#             except:
#                 return self.browserObject, True, "Successfully Deleted Template '%s'"%templateName           
        except Exception as e:
            return self.browserObject, False, "Failed to clone Template :: '%s' :: Error -> %s"%(templateName, 
                                                    str(e))
    #===========================================================================
    
    def getTemplateCategories(self):
        """
        Description:
            API to get existing Template Categories
        """
        tcList = []
        try:
            utility.execLog("Reading Templates Categories")  
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('tabMyTemplates'))), action="CLICK")
            utility.execLog("Reading Categories on 'My Templates' Page")
            tcList = self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('templateCatgoryDDL'))), action="GET_ELEMENTS_BY_TAG", setValue="option", returnContent="TEXT")
            utility.execLog("Able to Read Categories on 'My Templates' Page '%s'"%str(tcList))
            return self.browserObject, True, tcList            
        except Exception as e:
            return self.browserObject, False, "Unable to read Template Categories :: Error -> %s"%(str(e) + format_exc())
    
    def createTemplate(self, templateName, components, templateDescription="", managePermissions=False, 
                       userList=["All"], manageFirmware=False, templateType="New", cloneTemplateName=None, 
                       publishTemplate=False, usersOnly=False, repositoryOnly=False, repositoryName=None, 
                       passExpectation=False, editStatus=False):
        """
        Description:
            API to create New Template
        """
        try:
            utility.execLog("Clicking on 'My Templates' Tab")  
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('tabMyTemplates'))), action="CLICK")          
            utility.execLog("Clicking on 'Create Template' Link")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('addTemplate'))), action="CLICK")
            #Selecting template Type
            if templateType == "Clone":
                if not cloneTemplateName:
                    return self.browserObject, False, "Existing Template Name is not provided, Template Clone needs Existing Template Name"
                self.browserObject, status, result= self.cloneTemplate(templateName=cloneTemplateName) 
                return self.browserObject, status, result
#                utility.execLog("Selecting option 'Clone'")
#                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('cloneTemplate'))), action="CLICK")
#                utility.execLog('Selecting clone template category')
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('cloneTemplateCategory'))), action="SELECT", setValue='Automation')
#                utility.execLog("Selecting Existing Template Name '%s' to Clone"%cloneTemplateName)
#                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('templateToBeCloned'))), action="SELECT", setValue=cloneTemplateName)
            utility.execLog("Selecting option 'New'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('createNewTemplate'))), action="CLICK")
            time.sleep(2)
            utility.execLog('Entering Template Name')
            self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('templateNameBox'))), action='SET_TEXT', setValue=templateName)
            time.sleep(5)
            
            utility.execLog("Clicking Next Button")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardNext'))), action='CLICK')
            if "Create Template" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify page title in 'Create Template' Dialog :: Actual :'%s', Expected:'Create Template'"%(self.browserObject.title)
            utility.execLog("Verified page title in 'Create Template' Dialog :: Actual :'%s', Expected:'Create Template'"%(self.browserObject.title))
            
            #Selecting Template Category
            utility.execLog("Selecting Template Category 'Automation'")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateCategoryNew'))), action="SELECT", setValue="Automation")
                utility.execLog("Selected Template Category 'Automation'")
            except:
                utility.execLog("Template Category 'Automation' is not available so Creating Category 'Automation'")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("templateCategoryNew"))), action="SELECT", setValue="Create New Category")
                utility.execLog("Entering Template Category Name 'Automation'")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("createCategory"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("createCategory"))), action="SET_TEXT", setValue="Automation")
            #Entering Template Description
            utility.execLog("Entering Template Description '%s'"%templateDescription)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateDescriptionNew'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateDescriptionNew'))), action="SET_TEXT", setValue=templateDescription)
            #Select/Deselect Manage Firmware Option
            if manageFirmware:
                utility.execLog("Selecting 'Manage Firmware' Option")
                if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('enableManageFirmwareNew'))), action="IS_SELECTED"):
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('enableManageFirmwareNew'))), action="CLICK") 
                time.sleep(2)
                if repositoryOnly:
                    utility.execLog("Fetching Firmware Repositories")
                    elems = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('selectRepositoryNew'))), action="GET_ELEMENTS_BY_TAG", setValue="option")
                    optList = [str(option.text) for option in elems] 
                    time.sleep(5)
                    utility.execLog("Able to Fetch Firmware Repositories :: '%s'"%optList)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardCancel'))), action="CLICK")
                    time.sleep(5)
                    return self.browserObject, True, optList
                else:
                    if repositoryName:
                        utility.execLog("Selecting Firmware Repository '%s'"%repositoryName)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('selectRepositoryNew'))), action="SELECT", setValue=repositoryName) 
                        time.sleep(2)
                        utility.execLog("Selected Firmware Repository '%s'"%repositoryName)
                    else:
                        return self.browserObject, False, "Repository Name not provided for Managing Firmware"
            #Manage Permissions
            if managePermissions:
                utility.execLog("Select 'Manage Permissions' Option")
                status, result=self.managePermissions(usersOnly=usersOnly,userList=userList, passExpectation=passExpectation)
                if not status:
                    return self.browserObject, False, result
                if usersOnly:
                    return self.browserObject, status, result
            #Saving Template
            utility.execLog("Clicking 'Save' to Save Template Information")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_createtemplate")), action="CLICK")
            time.sleep(20)
            self.handleEvent(EC.title_contains(("Template Builder")))
            pageTitle = "Template Builder" 
            if "Template Builder" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify page title in 'Template Builder' Page :: Actual :'%s', Expected:'%s'"%(self.browserObject.title, pageTitle)
            utility.execLog("Clicked 'Save' to Save Template Information")
            utility.execLog("Verified page title in 'Template Builder' Page :: Actual :'%s', Expected:'%s'"%(self.browserObject.title, pageTitle))
            if components:
                componentList = json.loads(components)
                for component in componentList.keys():
                    if "Storage" in component:
                        #Adding Storage
                        self.addStorage(componentList[component]["Type"], componentList[component]["Name"], componentList[component]["VolumeName"],
                                        componentList[component]["Size"], componentList[component]["AuthType"], componentList[component]["AuthUser"], componentList[component]["AuthPwd"], componentList[component]["IQNIP"], editStatus)
                    elif "Server" in component:
                        #Adding Server
                        self.addServer(componentList[component]["TargetBootDevice"], componentList[component]["RaidLevel"], componentList[component]["ServerPool"],
                                componentList[component]["AutoGenerateHostName"], componentList[component]["HostnamePattern"], componentList[component]["OsImage"],
                                componentList[component]["AdminPassword"], componentList[component]["NTPServer"], componentList[component]["NetworkOptions"], editStatus)
            #Publishing Template
            if publishTemplate:
                status, result=self.publishTemplate()
                if not status:
                    return self.browserObject, False, result
            #Handling Draft Templates Dialog
            else:
                utility.execLog("Navigating back to Templates Page")
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                utility.execLog("Identifying Confirm Dialog box and Confirming to Draft Template")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('alertClose'))), action="CLICK")
                utility.execLog("Navigated back to Templates Page")
            return self.browserObject, True, "Successfully Created Template:: '%s'"%(templateName)                         
        except Exception as e:
            return self.browserObject, False, "Unable to create New Template :: Error -> %s"%(str(e) + format_exc())
    
    def createTemplateBasic(self, templateName, storageName, templateDescription="", managePermissions=False, userList=["All"], manageFirmware=False, 
                       templateType="New", cloneTemplateName=None, publishTemplate=False, usersOnly=False, passExpectation=False, volumeName="autoVolume", volumeSize="10GB"):
        """
        Description:
            API to create New Template
        """
        try:
            
            utility.execLog("Clicking on 'My Templates' Tab")            
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('tabMyTemplates'))), action="CLICK")          
            time.sleep(5)
            utility.execLog("Moved to 'My Templates' Tab")
            #Click on Create Template Link 
            utility.execLog("Clicking on 'Create Template' Link")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('addTemplate'))), action="CLICK")
            time.sleep(5)
            #Selecting template Type
            if templateType == "Clone":
                if not cloneTemplateName:
                    return self.browserObject, False, "Existing Template Name is not provided, Template Clone needs Existing Template Name" 
                utility.execLog("Selecting option 'Clone'")
                self.browserObject, status, result= self.cloneTemplate(templateName=cloneTemplateName)
                if not status:
                    return self.browserObject, status, result
                utility.execLog("Able to Select Existing Template Name '%s' to Clone"%cloneTemplateName)
            utility.execLog("Selecting option 'New'")
            if not self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('createNewTemplate'))), action= "IS_SELECTED"):
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('createNewTemplate'))), action="CLICK")
            time.sleep(2)
            utility.execLog('Entering Template Name')
            self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('templateNameBox'))), action='SET_TEXT', setValue=templateName)
            time.sleep(5)
            utility.execLog("Clicking Next Button")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardNext'))), action='CLICK')
            self.handleEvent(EC.invisibility_of_element_located((By.ID, self.TemplatesObjects('loadingSpinner'))))
            time.sleep(5)
            utility.execLog("Moved to 'Create Template' Dialog")
            if "Create Template" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify page title in 'Create Template' Dialog :: Actual :'%s', Expected:'Create Template'"%(self.browserObject.title)
            utility.execLog("Verified page title in 'Create Template' Dialog :: Actual :'%s', Expected:'Create Template'"%(self.browserObject.title))
            #Selecting Template Category
            utility.execLog("Selecting Template Category 'Automation'")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateCategoryNew'))), action="SELECT", setValue="Automation")
                utility.execLog("Selected Template Category 'Automation'")
            except:
                utility.execLog("Template Category 'Automation' is not available so Creating Category 'Automation'")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("templateCategoryNew"))), action="SELECT", setValue="Create New Category")
                utility.execLog("Entering Template Category Name 'Automation'")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("createCategoryNew"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("createCategoryNew"))), action="SET_TEXT", setValue="Automation")
            
            #Entering Template Description
            utility.execLog("Entering Template Description '%s'"%templateDescription)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateDescriptionNew'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateDescriptionNew'))), action="SET_TEXT", setValue=templateDescription)
            #Select/Deselect Manage Firmware Option
            if manageFirmware:
                utility.execLog("Select 'Manage Firmware' Option")
                if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('enableManageFirmwareNew'))), action="IS_SELECTED"):
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('enableManageFirmwareNew'))), action="CLICK") 
                time.sleep(2)
                utility.execLog("Selected 'Manage Firmware' Option")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('selectRepositoryNew'))), action="SELECT", selectBy="INDEX", setValue=1)
                time.sleep(2)
                utility.execLog("Selected firmware option")
            #Manage Permissions
            if managePermissions:
                utility.execLog("Select 'Manage Permissions' Option")
                status, result=self.managePermissions(usersOnly=False,userList=userList)
                if not status:
                    return self.browserObject, False, result
                if usersOnly:
                    return self.browserObject, status, result
            #Saving Template
            utility.execLog("Clicking 'Save' to Save Template Information")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('btnWizardSave'))), action= "CLICK")
            self.handleEvent(EC.invisibility_of_element_located((By.ID, self.TemplatesObjects('loadingSpinner'))))
            time.sleep(5)
            self.handleEvent(EC.title_contains(("Template Builder")))
            pageTitle = "Template Builder" 
            if "Template Builder" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify page title in 'Template Builder' Page :: Actual :'%s', Expected:'%s'"%(self.browserObject.title, pageTitle)
            utility.execLog("Clicked 'Save' to Save Template Information")
            utility.execLog("Verified page title in 'Template Builder' Page :: Actual :'%s', Expected:'%s'"%(self.browserObject.title, pageTitle))
            #Adding Components
            if volumeName=="autoVolume":
                date = datetime.now().strftime('%y%m%d%H%M%S') 
                volumeName = volumeName  +str(date)[6:]
            
            self.addStorageBasic("EqualLogic", storageName, volumeName, volumeSize, "CHAP", "grpadmin", "dell1234", None)
            time.sleep(5)
            #Publishing Template
            if publishTemplate:
                status, result=self.publishTemplate()
                if not status:
                    return self.browserObject, False, result
            else:
                #Handling Draft Templates Dialog
                utility.execLog("Navigating back to Templates Page")
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                utility.execLog("Identifying Confirm Dialog box and Confirming to Draft Template")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('alertClose'))), action="CLICK")
                utility.execLog("Navigated back to Templates Page")
            return self.browserObject, True, "Successfully Created Template:: '%s'"%(templateName)                          
        except Exception as e:
            utility.execLog("In exception")
            return self.browserObject, False, "Unable to create New Template :: Error -> %s"%str(e)
    
    def addStorageBasic(self, storageType, storageName, volumeName, volumeSize, authType, authUser, authPassword, iqnOrIP, portType="", targetStorage=""):
        """
        Adds Storage Component
        """
        try:
            #Selecting Storage Component
            utility.execLog("Click on Add Storage Component")         
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('addStorage'))), action="CLICK")
            time.sleep(5)
            utility.execLog("Clicked on Add Storage Component")
            self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="CLICK")
            storageComponentText = self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="GET_TEXT")
            if "Storage Component" not in storageComponentText:
                return self.browserObject, False, "Failed to verify page title in 'Storage Component' Page :: Actual :'%s', Expected:'Storage Component'"%(storageComponentText)
            utility.execLog("Verified page title in 'Storage Component' Page :: Actual :'%s', Expected:'Storage Component'"%(storageComponentText))
            #Selecting Storage Type
            utility.execLog("Selecting Storage Type '%s'"%storageType)         
            time.sleep(10)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('ddlComponents'))), action="SELECT", setValue=storageType)
            
            time.sleep(10)
            utility.execLog("Selected Storage Type '%s'"%storageType)
            #Continuing with other Options
            utility.execLog("Clicking on 'Continue' in Storage Conponent Page")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('btnContinueToSettings'))), action="CLICK")
            time.sleep(10)
            utility.execLog("Clicked on 'Continue' in Storage Conponent Page")
            #Selecting Storage Name 
            utility.execLog("Selecting  '%s'"%storageName)
            xpath = self.TemplatesObjects('targetStorage')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=storageName)
            time.sleep(5)
            utility.execLog("Creating Volume '%s'"%volumeName)
            xpath = self.TemplatesObjects('storageVolumeName')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Specify a new storage volume name now")
            time.sleep(3)
            xpath = self.TemplatesObjects('storageVolumeNameNew')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=volumeName)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
            time.sleep(2)
            time.sleep(2)
            utility.execLog("Entering Storage Volume Size '%s'"%volumeSize)
            xpath = self.TemplatesObjects('storageSize')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=volumeSize)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
            time.sleep(2)
            if "EqualLogic" in storageType:
                utility.execLog("Selecting Authentication Type '%s'"%authType)
                xpath = self.TemplatesObjects('authentication')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=authType)
                time.sleep(5)
                if authType == "CHAP":
                    utility.execLog("Entering CHAP User Name '%s'"%authUser)
                    xpath = self.TemplatesObjects('chapUsername')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=authUser)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                    time.sleep(2)
                    utility.execLog("Entering CHAP Password '%s'"%authPassword)
                    xpath = self.TemplatesObjects('chapPassword')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=authPassword)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                    time.sleep(2)
                else:
                    utility.execLog("Entering IQN/IP Details '%s'"%iqnOrIP)
                    xpath = self.TemplatesObjects('IQNIP')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=iqnOrIP)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                    time.sleep(2)
            elif "Compellent" in storageType:
                utility.execLog("Storage is Compellent hence no auth type")
                utility.execLog("selecting port Type to %s"%str(portType))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('portType'))), action="SELECT", setValue=portType)
                time.sleep(1)
            
            #Saving Storage Component
            utility.execLog("Clicking on 'Add' to Save Storage Component Details")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('templateBuilderDetails'))), action="CLICK")
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('confirmComponentAdd'))), action="CLICK")
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_adjustresources2")), action="CLICK")

            time.sleep(10)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('RedBoxError'))))
                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                utility.execLog("Unable to Add Storage Component(s) :: Error -> %s"%errorMessage)                
            except:
                utility.execLog("Successfully Added Storage Component(s) :: '%s'"%(storageName))
            else:
                raise "Unable to Add Storage Component(s) :: Error -> %s"%errorMessage
        except Exception as e:
            utility.execLog("Unable to Add Storage Component :: Error -> %s"%(str(e) + format_exc()))
            raise e
    
#    def addStorage(self, storageType, storageName, volumeName, volumeSize, authType, authUser, authPassword,iqnOrIP, editStatus):
#        """
#        Adds Storage Component
#        """
#        try:
#            #Selecting Storage Component
#            utility.execLog("Click on Add Storage Component")
#            time.sleep(0.5)
#            self.handleEvent(EC.invisibility_of_element_located((By.CLASS_NAME, "loader-spinner")))
#            self.handleEvent(EC.element_to_be_clickable((By.ID, "addStorage")), action="CLICK")
#            time.sleep(0.5)
#            self.handleEvent(EC.invisibility_of_element_located((By.CLASS_NAME, "loader-spinner")))
#            self.handleEvent(EC.element_to_be_clickable((By.ID, "page_componenteditor")), action="CLICK")
#            storageComponentText = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_componenteditor")), action="GET_TEXT")
#            if "Storage Component" not in storageComponentText:
#                return self.browserObject, False, "Failed to verify page title in 'Storage Component' Page :: Actual :'%s', Expected:'Storage Component'"%(storageComponentText)
#            utility.execLog("Verified page title in 'Storage Component' Page :: Actual :'%s', Expected:'Storage Component'"%(storageComponentText))
#            utility.execLog("Selecting Storage Type '%s'"%storageType)
#            self.handleEvent(EC.presence_of_element_located((By.XPATH, ".//option[text()='%s']"%storageType)))
#            self.handleEvent(EC.element_to_be_clickable((By.ID, "ddlComponents")), action="SELECT", setValue=storageType)
#            utility.execLog("Clicking on 'Continue' in Storage Conponent Page")
#            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnContinueToSettings")), action="CLICK")
#            time.sleep(5)
#            utility.execLog("Selecting Storage Name '%s'"%storageName)
#            xpath = "//select[@data-automation-id='asm_guid']"
#            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=storageName)
#            time.sleep(5)
#            utility.execLog("Selecting Storage Volume '%s'"%volumeName)
#            xpath = "//select[@data-automation-id='title']"
#            try:
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=volumeName)
#                time.sleep(5)
#            except:
#                utility.execLog("Storage Volume '%s' is not available so Creating Volume"%volumeName)
#                try:
#                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Create New Volume...")
#                    time.sleep(5)
#                except:
##                     For 831 build
#                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Specify a new storage volume name now")
#                    time.sleep(5)
#
#                utility.execLog("Entering Storage Volume Name '%s'"%volumeName)
#                try:
#                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//input[@data-automation-id='$new$title']")), action="SET_TEXT", setValue=volumeName)
#                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//input[@data-automation-id='$new$title']")), action="SET_TEXT", setValue="\t")
#                    time.sleep(2)
#                except:
#                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//input[@data-automation-id='volume_new']")), action="SET_TEXT", setValue=volumeName)
#                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//input[@data-automation-id='volume_new']")), action="SET_TEXT", setValue="\t")
#                    time.sleep(2)
#
#                utility.execLog("Entering Storage Volume Size '%s'"%volumeSize)
#                xpath = "//input[@data-automation-id='size']"
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=volumeSize)
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
#                time.sleep(2)
#            utility.execLog("Selecting Authentication Type '%s'"%authType)
#            xpath = "//select[@data-automation-id='auth_type']"
#            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=authType)
#            time.sleep(5)
#            if authType == "CHAP":
#                utility.execLog("Entering CHAP User Name '%s'"%authUser)
#                xpath = "//input[@data-automation-id='chap_user_name']"
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=authUser)
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
#                time.sleep(2)
#                utility.execLog("Entering CHAP Password '%s'"%authPassword)
#                xpath = "//input[@data-automation-id='passwd']"
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=authPassword)
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
#                time.sleep(2)
#            else:
#                utility.execLog("Entering IQN/IP Details '%s'"%iqnOrIP)
#                xpath = "//textarea[@data-automation-id='iqnOrIP']"
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=iqnOrIP)
#                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
#                time.sleep(2)
#            #Saving Storage Component
#            utility.execLog("Clicking on 'Save' to Save Storage Component Details")
#            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
#            time.sleep(0.5)
#            self.handleEvent(EC.invisibility_of_element_located((By.CLASS_NAME, "spinner")))
#            time.sleep(0.5)
#            self.handleEvent(EC.invisibility_of_element_located((By.CLASS_NAME, "loader-spinner")))
#            utility.execLog("Clicked on 'Save' to Save Storage Component Details")
#        except Exception as e:
#            utility.execLog("Unable to Add Storage Component :: Error -> %s"%(str(e) + format_exc()))
#            raise e
        


    def viewComponentDetails(self, templateName=None):
        """
        Description:
            API to view settings of each component in a Template 
        """
        try:
            self.browserObject, status, result = self.selectTemplate(templateName)
            time.sleep(5)
            utility.execLog("Clicking on 'View Details'")
            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("viewDetailsBtn"))),action="CLICK")
            time.sleep(10)
            components={"Storage":{},"Server":{},"cluster":{},"VM":{}}
            for key in components:
                if key=="Storage":
                    allInstance=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,"//*[@id='storage']/parent::*")))
                    i=1
                    for instance in allInstance:
                        components[key][key+str(i)]={}
                        instance.click()
                        #click to view components
                        self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("viewComponents"))),action="CLICK")
                        time.sleep(2)
                        #click to continue
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("btnContinueToSettings"))),action="CLICK")
                        time.sleep(2)
                        valuedDict=self.getTemplateInfo(key)
                        components[key][key+str(i)]=valuedDict
                        i=i+1
                        #Click close button 
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("cancelConfirmForm"))),action="CLICK")
                        time.sleep(5)
                if key=="Server":
                    allInstance=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,"//*[@id='server']/parent::*")))
                    i=1
                    for instance in allInstance:
                        components[key][key+str(i)]={}
                        #click component to view
                        instance.click()
                        #click to view components
                        self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("viewComponents"))),action="CLICK")
                        time.sleep(2)
                        #click to continue
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("btnContinueToSettings"))),action="CLICK")
                        time.sleep(2)
                        valuedDict=self.getTemplateInfo(key)
                        components[key][key+str(i)]=valuedDict
                        i=i+1
                        #Click close button 
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("cancelConfirmForm"))),action="CLICK")
                        time.sleep(5)
                if key=="cluster":
                    allInstance=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,"//*[@id='cluster']/parent::*")))
                    i=1
                    for instance in allInstance:
                        components[key][key+str(i)]={}
                        #click component to view
                        instance.click()
                        #click to view components
                        self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("viewComponents"))),action="CLICK")
                        time.sleep(2)
                        #click to continue
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("btnContinueToSettings"))),action="CLICK")
                        time.sleep(2)
                        valuedDict=self.getTemplateInfo(key)
                        components[key][key+str(i)]=valuedDict
                        i=i+1
                        #Click close button 
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("cancelConfirmForm"))),action="CLICK")
                        time.sleep(5)
                if key=="VM":
                    allInstance=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,"//*[@id='vm']/parent::*")))
                    i=1
                    for instance in allInstance:
                        components[key][key+str(i)]={}                      
                        #click component to view
                        instance.click()
                        #click to view components
                        self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("viewComponents"))),action="CLICK")
                        time.sleep(2)
                        #click to continue
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("btnContinueToSettings"))),action="CLICK")
                        time.sleep(2)
                        valuedDict=self.getTemplateInfo(key)
                        components[key][key+str(i)]=valuedDict
                        i=i+1
                        #Click close button 
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("cancelConfirmForm"))),action="CLICK")
                        time.sleep(5)
            utility.execLog("componets=%s"%components)
                
        except Exception as e:
            print "excp=%s"%e
            
    def getTemplateInfo(self,component):
        try:
            collapsedTabs= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects('collapsedTabs'))))
            collaps=0
            subcomponentDict={}
            #if no collapse tab there
            if collaps==0:
                subcomponentXpath=self.TemplatesObjects('viewSettings').format(collaps)
                subcomponentList=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,subcomponentXpath)))
                for subIndex in range(0, len(subcomponentList)):
                    subcomponent=subcomponentList[subIndex]
                    subcomponentName=subcomponent.text
                    try:
                        subcomponentValue=subcomponentList[subIndex].find_element_by_xpath(".//ancestor::div[2]//p")
                        subcomponentValue= subcomponentValue.text
                    except Exception as e:
                        subcomponentValue=''
                    subcomponentDict.update({subcomponentName:subcomponentValue})
            
            for collapsedTab in collapsedTabs:
                if collapsedTab.is_displayed():
                    time.sleep(2)
                    collaps=collaps+1
                    collapsedTab.click()
                    subcomponentXpath=self.TemplatesObjects('viewSettings').format(collaps)
                    subcomponentList=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,subcomponentXpath)))
                    for subIndex in range(0, len(subcomponentList)):
                        subcomponent=subcomponentList[subIndex]
                        subcomponentName=subcomponent.text
                        try:
                            subcomponentValue=subcomponentList[subIndex].find_element_by_xpath(".//ancestor::div[2]//p")
                            subcomponentValue= subcomponentValue.text
                        except Exception as e:
                            subcomponentValue=''
                        subcomponentDict.update({subcomponentName:subcomponentValue})               
            return subcomponentDict            
        except Exception as e:
            raise e
        
        
    def getStaticIPList(self):
        return self.staticIPList
    
    def getManualServerList(self):
        return self.manualServerList

    def deployService(self, templateName, serviceName, serviceDescription="", noofDeployments=1, managePermissions=False, 
                    repositoryName=None, userList=["All"], manageFirmware=False, deployNow=True, scheduleDeploymentWait=None, 
                    usersOnly=False, repositoryOnly=False, passExpectation=False, volumeName=None, staticIP=False, networks=None, manualServer=False, retryOnFailure ="false",checkinValidServerList=False, getSelectedRepo=False, deployPast= False):
        """
        Description:
            API to Deploy Service
        """
        try:
            self.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('deployTemplateBtn'))), action="CLICK")
            utility.execLog("Identifying Deploy Service Dialog")
            deployDialogText = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="GET_TEXT") 
            if "Deploy Service" not in deployDialogText:
                return self.browserObject, False, "Failed to verify Deploy Service Page :: Expected:'%s' and Actual:%s"%("Deploy Service", deployDialogText)
            try:
                utility.execLog("Verified Deploy Service Page :: Expected:'%s' and Actual:%s"%("Deploy Service", deployDialogText))
            except:
                utility.execLog("Verified Deploy Service Page")
            time.sleep(0.5)
            self.handleEvent(EC.invisibility_of_element_located((By.CLASS_NAME, self.TemplatesObjects('loadingSpinner'))))
            try:
                utility.execLog("Selecting Template '%s'"%templateName)
            except:
                utility.execLog("Selecting Template")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('selectedTemplate'))),action= 'SELECT', setValue=templateName)
            try:
                utility.execLog("Entering Service Name '%s'"%serviceName)
            except:
                utility.execLog("Entering Service Name")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('serviceName'))), action= 'CLEAR')
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('serviceName'))), action= "SET_TEXT", setValue=serviceName.encode('utf-8'))
            utility.execLog("Entering Service Description '%s'"%serviceDescription)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('serviceDescription'))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('serviceDescription'))), action="SET_TEXT", setValue=serviceDescription)
            
            #Setting User-Entered No. of Deployments for Multi-Service Deployments
            if globalVars.noOfDeployments=="":
                noofDeployments=1
            else:
                noofDeployments=globalVars.noOfDeployments
            utility.execLog("Entering No of Deployments '%s'"%str(noofDeployments))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('numDeployments'))), action= "CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('numDeployments'))), action= "SET_TEXT", setValue=noofDeployments)
            
            if manageFirmware:
                utility.execLog("Selecting 'Manage Firmware' Option")
                if not self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('manageFirmware'))), action='IS_SELECTED'):
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('manageFirmware'))), action='CLICK')
                time.sleep(2)
                if repositoryOnly:
                    utility.execLog("Fetching Firmware Repositories")
#                    elems = self.handleEvent(EC.element_to_be_clickable((By.ID, "firmwarepackage")), action="GET_ELEMENTS_BY_TAG", setValue="option")
                    elems=self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('firmwarePackage'))), action="GET_ELEMENTS_BY_TAG", setValue='option')
                    optList = [str(option.text) for option in elems] 
#                    selectedRepo=self.handleEvent(EC.presence_of_element_located((By.ID,"firmwarepackage")), action="SELECT", selectBy="FIRST_OPTION")
                    selectedRepo= self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('firmwarePackage'))), action="SELECT", selectBy='FIRST_OPTION')
                    time.sleep(5)
                    utility.execLog("Able to Fetch Firmware Repositories :: '%s'"%optList)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardCancel'))), action="CLICK")
                    time.sleep(5)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
                    if getSelectedRepo:
                        utility.execLog('Repository Selected in the Deploy service wizard:: %s'%selectedRepo)
                        return self.browserObject, True, selectedRepo
                    else:
                        return self.browserObject, True, optList
                else:
                    if repositoryName:
                        utility.execLog("Selecting Firmware Repository '%s'"%repositoryName)
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('firmwarePackage'))), action="SELECT", setValue=repositoryName)
                        time.sleep(2)
                        utility.execLog("Selected Firmware Repository '%s'"%repositoryName)
                    else:
                        return self.browserObject, False, "Repository Name not provided for Managing Firmware"
            else:
                utility.execLog("Unselecting 'Manage Firmware' option")
                if self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('manageFirmware'))), action='IS_SELECTED'):
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('manageFirmware'))), action='CLICK')
            #Manage Permissions
            if managePermissions:
                status, result=self.managePermissions(usersOnly=usersOnly,userList=userList, passExpectation=passExpectation)
                if not status:
                    return self.browserObject, False, result
                if usersOnly:
                    return self.browserObject, status, result
#                
            utility.execLog("Moving to 'Deployment Settings' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardNext'))), action="CLICK")
            time.sleep(5)
#            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnViewAllSettings")))
            utility.execLog("Moved to 'Deployment Settings' Tab")
            
            #utility.execLog("Setting Storage Volume name. %s"%volumeName)
#             if volumeName==None:
#                 utility.execLog("Volume name none")
#                 try:
#                     Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("autoVolume")
#                     utility.execLog("Volume name none")
#                     time.sleep(2)
#                 except:
#                     Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("Create New Volume...")
#                     time.sleep(2)
#                     self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").clear()
#                     time.sleep(2)
#                     self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").send_keys("autoVolume")
#                     time.sleep(1)
#             else:
#                 try:
#                     Select(self.browserObject.find_element_by_id("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text(volumeName)
#                     time.sleep(2)
#                 except:
#                     Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("Create New Volume...")
#                     time.sleep(2)
#                     self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").clear()
#                     time.sleep(2)
#                     self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").send_keys(volumeName)
#                     time.sleep(1)
              
            #Manual Entry Section for Deployment
            #Static IP --> Manual Entry 
            if (staticIP and networks!=None):
                #utility.execLog(networks)
                utility.execLog("Selecting 'User Entered IP'")
                
                #Initialization
                staticIP = globalVars.staticIP
                
                #For Validation
                for key in staticIP.keys():
                    self.staticIPList[key] = []
                
                serverInstance = len(self.browserObject.find_elements_by_xpath("//*[contains(text(),'Server Source')]"))
                vmInstance = len(self.browserObject.find_elements_by_xpath("//*[contains(text(),'Path')]")) + len(self.browserObject.find_elements_by_xpath("//*[contains(text(),'IP Source')]")) - int(serverInstance)
                vmRowCount = 1
                serverRowCount = vmInstance + 2
                
                #Setting Static IPs for VMs
                while (vmInstance > 0):
                    parentXPATH=".//*[@id='DeploySettings']/li[%i]"%vmRowCount
                    labelXPATH=parentXPATH+"/fieldset/div/ul/li[2]/div[1]/label"
                    
                    if (self.check_xpath_exists(labelXPATH)):
                        labelName = self.handleEvent(EC.presence_of_element_located((By.XPATH, labelXPATH)), action="GET_TEXT")
                        if (labelName == "IP Source"):
                            vmNameXPATH=parentXPATH+"/h3"
                            vmName=self.handleEvent(EC.presence_of_element_located((By.XPATH, vmNameXPATH)), action="GET_TEXT")
                        
                            xpath=parentXPATH+"/fieldset/div/ul[1]/li[2]/div[2]/ul/li[2]/label/input"
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                            time.sleep(2)
                                
                            utility.execLog("Setting Static IPs for %s "%vmName)
                            row = 4 
                            rootXPATH=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[1]/label"%row
                                
                            if (self.check_xpath_exists(rootXPATH)):
                                nwType = None           
                                nwLabel = self.handleEvent(EC.presence_of_element_located((By.XPATH, rootXPATH)), action="GET_TEXT")
                                utility.execLog("Label:%s"%nwLabel)
                                
                                for nwCount in xrange(0, len(networks)):
                                    if(str(networks[nwCount]['Name']) in nwLabel):
                                        if(networks[nwCount]['IP Address Setting'] == 'Static'):
                                            nwType = networks[nwCount]['Network Type']
                                                    
                                if (nwType!=None):
                                    utility.execLog("Network Type:%s"%nwType)
                                    if(len(staticIP[nwType]) > 0 and staticIP[nwType][0]!=""):
                                        xpath=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[2]/select"%row
                                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Manual Entry")
                                        time.sleep(2)
                                          
                                        #Updating the Row Count if IP is provided
                                        row=row+2
                                                 
                                        xpath=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[2]/input"%row
                                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=staticIP[nwType][0])
                                        self.staticIPList[nwType].append(staticIP[nwType][0])
                                        del staticIP[nwType][0]
                                        time.sleep(2)
                                    else:
                                        xpath=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[2]/select"%row
                                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="ASM Selected IP")
                                        time.sleep(2)
                    vmInstance=vmInstance-1
                    vmRowCount=vmRowCount+1
                
                #Setting Static IPs for Servers
                while (serverInstance > 0):
                    #Setting IPs manually for Server 
                    parentXPATH=".//*[@id='DeploySettings']/li[%i]"%serverRowCount
                
                    serverNameXPATH=parentXPATH+"/h3"
                    serverName=self.handleEvent(EC.presence_of_element_located((By.XPATH, serverNameXPATH)), action="GET_TEXT")
                
                    xpath=parentXPATH+"/fieldset/div/ul[1]/li[2]/div[2]/ul/li[2]/label/input"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    time.sleep(2)
                
                    utility.execLog("Setting Static IPs for %s "%serverName)
                    row = 4 
                    rootXPATH=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[1]/label"%row
                    
                    while (self.check_xpath_exists(rootXPATH)):
                        nwType = None           
                        nwLabel = self.handleEvent(EC.presence_of_element_located((By.XPATH, rootXPATH)), action="GET_TEXT")
                        utility.execLog("Label:%s"%nwLabel)
                        
                        for nwCount in xrange(0, len(networks)):
                            if(str(networks[nwCount]['Name']) in nwLabel):
                                if(networks[nwCount]['IP Address Setting'] == 'Static'):
                                    nwType = networks[nwCount]['Network Type']
                                    
                        if (nwType!=None):
                            utility.execLog("Network Type:%s"%nwType)
                            if(len(staticIP[nwType]) > 0 and staticIP[nwType][0]!=""):
                                xpath=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[2]/select"%row
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Manual Entry")
                                time.sleep(2)
                                #Updating the Row Count if IP is provided
                                row=row+2
            
                                xpath=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[2]/input"%row
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=staticIP[nwType][0])
                                self.staticIPList[nwType].append(staticIP[nwType][0])
                                del staticIP[nwType][0]
                                time.sleep(2)

                            else:
                                xpath=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[2]/select"%row
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="ASM Selected IP")
                                time.sleep(2)
                        
                        row=row+2
                        rootXPATH=parentXPATH+"/fieldset/div/ul[1]/li[%i]/div[1]/label"%row
                    serverInstance=serverInstance-1
                    serverRowCount=serverRowCount+1
                utility.execLog("Updating Static IP List for Validation: %s"%str(self.staticIPList))
              
            #Manual Entry for Servers
            if not checkinValidServerList:
                if manualServer:
                    manualServerList=globalVars.manualServer
                    
                    #Initialization
                    serverInstance = len(self.browserObject.find_elements_by_xpath("//*[contains(text(),'Server Source')]"))
                    vmInstance = len(self.browserObject.find_elements_by_xpath("//*[contains(text(),'Path')]")) + len(self.browserObject.find_elements_by_xpath("//*[contains(text(),'IP Source')]")) - int(serverInstance)
                    vmRowCount = 1
                    serverRowCount = vmInstance + 2
                    
                    while (serverInstance > 0):
                        parentXPATH=".//*[@id='DeploySettings']/li[%i]"%serverRowCount
                    
                        serverNameXPATH=parentXPATH+"/h3"
                        serverName=self.handleEvent(EC.presence_of_element_located((By.XPATH, serverNameXPATH)), action="GET_TEXT")
                        
                        if (len(manualServerList)>0 and manualServerList[0]!=""):
                                utility.execLog("Setting Manual Entry for %s "%serverName)
                                
                                sourceXPATH=parentXPATH+"/fieldset/div/ul[2]/li[2]/div[2]/select"
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, sourceXPATH)), action="SELECT", setValue="Manual Entry")
                                time.sleep(2)
                            
                                try:
                                    valueXPATH=parentXPATH+"/fieldset/div/ul[2]/li[4]/div[2]/select"
                                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, valueXPATH)), action="SELECT", setValue=manualServerList[0])
                                    self.manualServerList.append(manualServerList[0])
                                    del manualServerList[0]
                                    time.sleep(2)
                                except Exception as e:
                                    utility.execLog("Error: %s; Unable to find the User-Entered Server for %s. Either the User-Entered Server is not compatible with the template or incorrectly entered"%(str(e)), serverName)
                                    raise e
                        else:
                            xpath=parentXPATH+"/fieldset/div/ul[2]/li[2]/div[2]/select"
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Server Pool")
                            time.sleep(2)
                        serverInstance=serverInstance-1
                        serverRowCount=serverRowCount+1
                    utility.execLog("Updating Manual Server List for Validation: %s"%str(self.manualServerList))
                    
                try:             
                    retryOnFailureOptions= self.browserObject.find_elements_by_xpath(".//*[@data-automation-id='migrate_on_failure']")
                    if retryOnFailure== "false":
                        for checkbox in retryOnFailureOptions:
                            if checkbox.is_selected():
                                checkbox.click()
                except:
                    utility.execLog("No server added in template")
                    
                time.sleep(2)  
                utility.execLog("Moving to 'Schedule Deployment' Tab")
                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('btnWizardNext'))), action= "CLICK")
                time.sleep(5)            
                if deployNow:
                    utility.execLog("Selecting 'Deploy Now' Option") 
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('deployNow'))), action="CLICK")               
                    time.sleep(5)
                else:
                    utility.execLog("Selecting 'Schedule Later' Option")   
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('scheduleDeployment'))), action="CLICK")
                    time.sleep(5)
                    utility.execLog("Selected 'Schedule Later' Option")
                    utility.execLog("Open Date Table & Select Past/Current Date")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('scheduleCalendar'))), action="CLICK")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('toggleDateTime'))), action= "CLICK")
                    if not deployPast:
                        for loopCounter in range(0, int(scheduleDeploymentWait)):
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('incrementMinutes'))), action= "CLICK")
                    else:
                        selectedDeployTime= self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('selectedDeployTime'))), action= "GET_TEXT")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('decrementHours'))), action= "CLICK")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('scheduleCalendar'))), action="CLICK")
                        decreementDeployTime= self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('selectedDeployTime'))), action= "GET_TEXT")
                        if decreementDeployTime==selectedDeployTime:
                            utility.execLog("Unable to set past deployment time")
                            utility.execLog("Cancel deployment wizard")
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardCancel'))), action="CLICK")
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('submitConfirmForm'))), action= "CLICK")
                            return self.browserObject, True, "Unable to set past deployment time"
                        else:
                            utility.execLog("Able to set past deployment time")
                            utility.execLog("Cancel deployment wizard")
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardCancel'))), action="CLICK")
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action= "CLICK")
                            return self.browserObject, False, "Able to set past deployment time"
                utility.execLog("Clicking on 'Finish' Option")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('btnWizardFinish'))),action="CLICK")
                time.sleep(5)
                utility.execLog("Confirming on 'Yes' to Cancel Deploy Wizard")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
                time.sleep(10)
                try:
                    eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                    errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action= "GET_TEXT")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardCancel'))), action="CLICK")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
                    time.sleep(2)
                    return self.browserObject, False, "Failed to Deploy Service :: '%s' :: Error -> '%s'"%(serviceName, 
                                                        str(errorMessage))
                except:
                    #Move to Templates Page
                    utility.execLog("Navigating back to Services Page")
                    self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Services")), action="CLICK")
                    time.sleep(3)
                    utility.execLog("Navigated back to Services Page")
                    return self.browserObject, True, "Successfully Initiated Deployment :: '%s'"%(serviceName)
            else:
                utility.execLog("No deployment only validation of manual server list")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('deploymentServerPool'))), action="SELECT", setValue="Manual Entry")
                time.sleep(5)
                select=Select(self.browserObject.find_element_by_xpath(self.TemplatesObjects('deploymentServerSelect')))
                deployServerList=[]
                for option in select.options:
                    deployServerList.append(option.text)
                utility.execLog("deployServerList={}".format(deployServerList))
            
                for inValidServer in checkinValidServerList:
                    if inValidServer in deployServerList:
                        utility.exeLog("InValid servers present in deployServer list") 
                        self.failure( inValidServer,raiseExc=True)
                        break
                utility.execLog("No InValid servers present in deployServer list")
                utility.execLog("Click Cancle")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardCancel'))), action="CLICK")
                time.sleep(10)
                utility.execLog("Click OK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
                time.sleep(1)
                return self.browserObject, True,"No InValid servers present in deployServer list"
        except Exception as e:
            return self.browserObject, False, "Unable to Initiate Deployment :: Error -> %s"%(str(e) + format_exc())
    
    def deployServiceBasic(self, templateName, serviceName, serviceDescription="", noofDeployments=1, managePermissions=False, 
                      repositoryName=None, userList=["All"], manageFirmware=False, deploymentScheduleTime=None, 
                       usersOnly=False, passExpectation=False, deployNow=True, volumeName=""):
        """
        Description:
            API to Deploy Service
        """
        try:
            self.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('deployTemplateBtn'))), action="CLICK")
            time.sleep(10)
            utility.execLog("Identifying Deploy Service Dialog")
            deployDialog = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))))
            if "Deploy Service" not in deployDialog.text:
                return self.browserObject, False, "Failed to verify Deploy Service Page :: Expected:'%s' and Actual:%s"%("Deploy Service", deployDialog.text)
            utility.execLog("Verified Deploy Service Page :: Expected:'%s' and Actual:%s"%("Deploy Service", deployDialog.text))
            time.sleep(3)
            utility.execLog("Clicking on 'Service Information' Tab")
            navigationTabs= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects('DeployNavTabs'))))
            time.sleep(2)
            navigationTabs[0].find_element_by_xpath("./span").click()
            time.sleep(2)
            utility.execLog("Clicked on 'Service Information' Tab")
            utility.execLog("Selecting Template '%s'"%templateName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('selectedTemplate'))),action= 'SELECT', setValue=templateName)
            utility.execLog("Selected Template '%s'"%templateName)
            utility.execLog("Entering Service Name '%s'"%serviceName)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('serviceName'))), action= "SET_TEXT", setValue=serviceName)
            utility.execLog("Entered Service Name '%s'"%serviceName)
            
            utility.execLog("Entering Service Description '%s'"%serviceDescription)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('serviceDescription'))), action="SET_TEXT", setValue=serviceDescription)
            utility.execLog("Entered Service Description '%s'"%serviceDescription)
            utility.execLog("Entering No of Deployments '%s'"%str(noofDeployments))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('numDeployments'))), action= "CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('numDeployments'))), action= "SET_TEXT", setValue=noofDeployments)            
            utility.execLog("Entered No of Deployments '%s'"%str(noofDeployments))
            if manageFirmware:
                utility.execLog("Select 'Manage Firmware' Option")
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('manageFirmware'))), action='CLICK')
                    time.sleep(2)
                    utility.execLog("Selected 'Manage Firmware' Option")
                    if repositoryName:
                        utility.execLog("Selecting Repository '%s'"%repositoryName)
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('firmwarePackage'))), action="SELECT", setValue=repositoryName)
                        utility.execLog("Selected Repository '%s'"%repositoryName)
                    else:
                        return self.browserObject, False, "Repository Name not provided for Managing Firmware"
                except Exception as e:
                    return self.browserObject, False, "'Manage Firmware' Option not available"
            #Manage Permissions
            if managePermissions:
                utility.execLog("Select 'Manage Permissions' Option")
                status, result=self.managePermissions(usersOnly=usersOnly,userList=userList, passExpectation=passExpectation)
                if not status:
                    return self.browserObject, False, result
                if usersOnly:
                    return self.browserObject, status, result
            utility.execLog("Moving to 'Deployment Settings' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardNext'))), action="CLICK")
            time.sleep(5)
#            deployPage.find_element_by_id("btnViewAllSettings")
            utility.execLog("Moved to 'Deployment Settings' Tab")
            
            utility.execLog("Setting Storage Volume name.")
#            if volumeName=="":
#                try:
#                    Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("autoVolume")
#                    time.sleep(1)
#                except:
#                    try:
#                        Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("Create New Volume...")
#                        time.sleep(2)
#                        self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").clear()
#                        time.sleep(2)
#                        self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").send_keys("autoVolume")
#                        time.sleep(2)
#                    except:
#                        utility.execLog("New volume name drop down not available in 831 build")
#            else:
#                try:
#                    Select(self.browserObject.find_element_by_id("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text(volumeName)
#                    time.sleep(2)
#                except:
#                    try:
#                        Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("Create New Volume...")
#                        time.sleep(2)
#                        self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").clear()
#                        time.sleep(2)
#                        self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").send_keys(volumeName)
#                        time.sleep(2)
#                    except:
#                        utility.execLog("New volume name drop down not available in 831 build")
            utility.execLog("Moving to 'Schedule Deployment' Tab")
            self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('btnWizardNext'))), action= "CLICK")
            time.sleep(5)
#            deployPage.find_element_by_id("deploynow")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('deployNow'))))
            utility.execLog("Moved to 'Schedule Deployment' Tab")
            if deployNow:
                utility.execLog("Selecting 'Deploy Now' Option")  
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('deployNow'))), action="CLICK")              
                time.sleep(5)
                utility.execLog("Selected 'Deploy Now' Option")
            else:
                utility.execLog("Selecting 'Schedule Later' Option")              
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('scheduleDeployment'))), action="CLICK")
                time.sleep(5)
                utility.execLog("Selected 'Schedule Later' Option")
                utility.execLog("Open Date Table & Select Past/Current Date")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('scheduleCalendar'))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('toggleDateTime'))), action= "CLICK")
                for loopCounter in range(0, int(deploymentScheduleTime)):
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('incrementMinutes'))), action= "CLICK")
            utility.execLog("Clicking on 'Finish' Option")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('btnWizardFinish'))),action="CLICK")
            time.sleep(5)
            utility.execLog("Clicked on 'Finish' Option")
            utility.execLog("Clicking on 'Confirm Dialog' Option")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
            time.sleep(10)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action= "GET_TEXT")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardCancel'))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
                time.sleep(2)
                return self.browserObject, False, "Failed to Deploy Service :: '%s' :: Error -> '%s'"%(serviceName, 
                                                    str(errorMessage))
            except:
                #Move to Templates Page
                utility.execLog("Navigating back to Services Page")
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Services")), action="CLICK")
#                self.browserObject.find_element_by_class_name("breadcrumbs").find_element_by_tag_name("a").click()
                time.sleep(3)
                utility.execLog("Navigated back to Services Page")
                time.sleep(10)
                return self.browserObject, True, "Successfully Initiated Deployment :: '%s'"%(serviceName)
        except Exception as e:
            return self.browserObject, False, "Unable to Initiate Deployment :: Error -> %s"%str(e)
    
    def editTemplate(self, templateName, managePermissions=True, userList=["All"], manageFirmware=False,  
                       deleteUsers=False, usersOnly=False, firmwareName=None):
        """
        Edit existing Service
        """
        try:
            time.sleep(2)
            self.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            utility.execLog("Moving to Template Editor Page")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "editTemplateLink")), action="CLICK")
            time.sleep(10)            
            templatePageText = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_templatebuilder")), action="GET_TEXT") 
            if templateName not in templatePageText:
                return self.browserObject, False, "Failed to verify Template Builder Page :: Expected:'%s' and Actual:%s"%(templateName, templatePageText)
            utility.execLog("Verified Template Builder Page :: Expected:'%s' and Actual:%s"%(templateName, templatePageText))
            utility.execLog("Clicking on Edit Template Option")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnEditTemplateName")), action="CLICK")
            time.sleep(5)
            editTemplatePage = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_componenteditor")), action="GET_TEXT") 
            if "Edit Template Information" not in editTemplatePage:
                utility.execLog("Failed to verify Edit Template Page :: Actual: %s, Expected: %s"%(editTemplatePage, "Edit Template Information"))
                return self.browserObject, False, "Failed to verify Edit Template Page :: Actual: %s, Expected: %s"%(editTemplatePage, "Edit Template Information")
            utility.execLog("Moved to Edit Template Page and Verified Page Title :: Actual: %s, Expected: %s"%(editTemplatePage, "Edit Template Information"))
            if usersOnly:
                userList = []           
                utility.execLog("Reading Existing Users")
                xpath = "//div[@class='checkboxlist']/div"
                usersAdded = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath)))) + 1
                for loop in range(1, usersAdded):
                    xpath = "//div[@class='checkboxlist']/div[%i]/span"%loop
                    userList.append(self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT"))
                utility.execLog("Able to Read Existing Users for Template '%s'"%str(userList))
                self.handleEvent(EC.element_to_be_clickable((By.ID, "cancel_edittemplatename_form")), action="CLICK")
                time.sleep(5)
                return self.browserObject, True, userList
            #Manage Permissions
            if managePermissions:
                utility.execLog("Select 'Manage Permissions' Option")
                if not self.handleEvent(EC.element_to_be_clickable((By.ID, "managePermissions")), action="IS_SELECTED"):
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "managePermissions")), action="CLICK")
                    time.sleep(2)
                    utility.execLog("Selected 'Manage Permissions' Option")
                else:
                    utility.execLog("'Manage Permissions' Option is already Selected")
                deletedUsers = []
                if deleteUsers:
                    utility.execLog("Reading Existing Users")
                    xpath = "//div[@class='checkboxlist']/div"
                    usersAdded = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath)))) + 1
                    for loop in range(1, usersAdded):
                        xpath = "//div[@class='checkboxlist']/div[%i]/span"%loop
                        for ruser in userList:
                            if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") == ruser:
                                xpath = "//div[@class='checkboxlist']/div[%i]/input"%loop
                                self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="CLICK")
                                utility.execLog("Selected User '%s'"%ruser)
                                deletedUsers.append(ruser)
                    utility.execLog("Clicking on Remove User(s)")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "delete_user_link")), action="CLICK")
                    time.sleep(2)
                    utility.execLog("Clicking on 'Save'")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_edittemplatename_form")), action="CLICK")
                    time.sleep(5)
                    try:
                        eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False)
                        errorMessage = eleError.find_element_by_tag_name("h3").text
                        self.browserObject.find_element_by_class_name("breadcrumbs").find_element_by_tag_name("a").click()
                        time.sleep(2)
                        return self.browserObject, False, "Failed to Create Template :: '%s' :: Error -> '%s'"%(templateName, 
                                                            str(errorMessage))
                    except:
                        #Move to Templates Page
                        utility.execLog("Navigating back to Templates Page")
                        self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                        time.sleep(3)
                        #Handling Draft Templates Dialog
                        try:
                            utility.execLog("Identifying Confirm Dialog box and Confirming to Draft Template")
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "popupModal")), action="CLICK")
                            time.sleep(5)
                            self.browserObject.refresh()
                            time.sleep(10)
                        except:
                            pass
                        failedUsers = set(userList).difference(set(deletedUsers))
                        if len(failedUsers) <= 0:
                            return self.browserObject, True, "Successfully Removed Users '%s' Access to the Template"%str(deletedUsers)
                        else:
                            return self.browserObject, False, "Failed to Remove Some/All Users '%s' Access to the Template, Removed Users '%s'"%(str(failedUsers), str(deletedUsers))
                else:           
                    if 'All' in userList:
                        utility.execLog("Selecting Users '%s' to provide Template Access"%str(userList))
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "allStandardUsers")), action="CLICK")
                        time.sleep(2)                    
                    else:
                        utility.execLog("Selecting Users '%s' to provide Template Access"%str(userList))
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
                        totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))  
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
                                self.handleEvent(EC.element_to_be_clickable((By.ID, "cancel_edittemplatename_form")), action="CLICK")
                                time.sleep(5)
                                return self.browserObject, False, "Failed to Select User '%s'"%user
                        utility.execLog("Clicking on 'Add' to save Added Users")      
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_user_form")), action="CLICK")
            if manageFirmware:
                self.browserObject.find_element_by_id("managefirmware").click()
                utility.execLog("Clicking the Firmware update checkbox")
                self.browserObject.find_element_by_id("managefirmware").click()
                time.sleep(2)
                utility.execLog("Selecting Firmware %s"%firmwareName)
                select = Select(self.browserObject.find_element_by_id("firmwarepackage"))
                select.select_by_visible_text(firmwareName)
                time.sleep(2)
            utility.execLog("Clicking on 'Save'")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_edittemplatename_form")), action="CLICK")
            time.sleep(5)
            utility.execLog("Clicked on 'Save'")
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False)
                errorMessage = eleError.find_element_by_tag_name("h3").text
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")                
                time.sleep(2)
                return self.browserObject, False, "Failed to Create Template :: '%s' :: Error -> '%s'"%(templateName, 
                                                    str(errorMessage))
            except:
                #Move to Templates Page
                utility.execLog("Navigating back to Templates Page")
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                time.sleep(3)
                #Handling Draft Templates Dialog
                try:
                    utility.execLog("Identifying Confirm Dialog box and Confirming to Draft Template")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "popupModal")), action="CLICK")
                    time.sleep(5)
                    self.browserObject.refresh()
                    time.sleep(10)
                except:
                    pass
                return self.browserObject, True, "Successfully Edited Template '%s'"%templateName           
        except Exception as e:
            return self.browserObject, False, "Failed to Edit Template :: '%s' :: Error -> %s"%(templateName, 
                                                    str(e) + format_exc())
    
    def exportData(self):
        """
        Description: 
            API to Export All Templates to CSV file
        """
        try:
            utility.execLog("Clicking on 'My Templates' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('tabMyTemplates'))), action="CLICK")
            time.sleep(3)
            utility.execLog("Clicking on 'Export All'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('exportAllTemplates'))), action="CLICK")
            time.sleep(20)
            utility.execLog("Clicked on 'Export All'")
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('RedBoxError'))), retry=False)
                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('RedBoxErrorMessages'))))
                time.sleep(2)
                return self.browserObject, False, "Failed to Export All Templates :: Error -> '%s'"%(str(errorMessage))
            except:
                return self.browserObject, True, "Successfully initiated Export All Templates"
        except Exception as e:
            return self.browserObject, False, "Exception while Exporting All Templates :: Error -> %s"%(str(e) + format_exc())
    
    def verifyNTP(self, primaryNTP):
        """
        Description: To verify pre-population of NTP Server while adding Servers in Template
        """
        utility.execLog("Checking for pre-population of NTP Server")
        utility.execLog("Fetching NTP Server")
        xpath= self.TemplatesObjects('ntpServer')
        ntp = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName="value")
        
        if ntp != primaryNTP:
            utility.execLog("NTP Server value which is pre-populated mismatches with the Preferred NTP Server info set in the appliance")
            return False
        else:
            utility.execLog("NTP Server value which is pre-populated matches with the Preferred NTP Server info set in the appliance")
            #Check to see if the NTP Server field is editable or not. If yes, setting it to primaryNTP (Single or Multiple, based on Testcase)
            try:
                utility.execLog("Entering NTP Server")
                xpath= self.TemplatesObjects('ntpServer')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=primaryNTP)
            except Exception as e:
                raise "Exception while editing pre-populated NTP Server field :: Error -> %s"%(str(e) + format_exc())
            return True
        
    def buildTemplate(self, components, resourceInfo, networks, publishTemplate=False, verifyNTP=False, editClone=False, OS_Image="",
                                                                                managePermissions=True, importConfigFromRefServer=False,validatePoolAndManualServers=False):
        """
        Description:
            API to create New Template
        """
        try:
            templateName = (components["Template"]["Name"]).encode('utf-8')
            utility.execLog("Clicking on 'My Templates' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('tabMyTemplates'))), action="CLICK") 
          
            #Creating a new template 
            if not editClone:
                utility.execLog("Clicking on 'Add a Template' Link")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('addTemplate'))), action="CLICK")
                utility.execLog("Checking 'Create Template' Dialog")
                time.sleep(5)
                if "Add a Template" not in self.browserObject.title:
                    return self.browserObject, False, "Failed to verify page title in 'Create Template' Dialog :: Actual :'%s', Expected:'Create Template'"%(self.browserObject.title)
                utility.execLog("Verified page title in 'Create Template' Dialog :: Actual :'%s', Expected:'Create Template'"%(self.browserObject.title))
                #Selecting template Type
#                 utility.execLog("Selecting option 'New'")
#                 self.handleEvent(EC.element_to_be_clickable((By.ID, "template_new")), action="CLICK")
            
#             templateName = (components["Template"]["Name"]).encode('utf-8')
                utility.execLog('Clicking on Create New Template')
                if not self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('createNewTemplate'))), action= "IS_SELECTED"):
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('createNewTemplate'))), action="CLICK")
            #Entering Template Name
                try:
                    utility.execLog("Entering Template Name '%s'"%templateName)
                except:
                    utility.execLog("Entering Template Name")
                
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('templateNameBox'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('templateNameBox'))), action="SET_TEXT", setValue=templateName)
                utility.execLog("Clicking 'Next' button")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardNext'))), action="CLICK") 
                
                #Selecting Template Category
                utility.execLog("Selecting Template Category 'Automation'")
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateCategoryNew'))), action="SELECT", setValue="Automation")
                    utility.execLog("Selected Template Category 'Automation'")
                except:
                    utility.execLog("Template Category 'Automation' is not available so Creating Category 'Automation'")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateCategoryNew'))), action="SELECT", setValue="Create New Category")
                    utility.execLog("Entering Template Category Name 'Automation'")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('createCategoryNew'))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('createCategoryNew'))), action="SET_TEXT", setValue="Automation")
                templateDescription = (components["Template"]["Description"]).encode('utf-8')
                #Entering Template Description
                try:
                    utility.execLog("Entering Template Description '%s'"%templateDescription)
                except:
                    utility.execLog("Entering Template Description")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateDescriptionNew'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateDescriptionNew'))), action="SET_TEXT", setValue=templateDescription)
                
                if managePermissions:
                    utility.execLog("Select 'ASM Administrator and all standard users' Option")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('allStandardUsersPermission'))), action="CLICK")
                    time.sleep(2)
                #Saving Template
                
                utility.execLog("Clicking 'Save' to Save Template Information")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardSave'))), action="CLICK")
                time.sleep(20)
            
            #Click on Edit an existing test case    
            else:
                utility.execLog("Selecting existing template %s to Edit"%templateName)
                self.browserObject, status, result = self.selectTemplate(templateName,"My Templates")
                if not status:
                    return self.browserObject, False, result
                utility.execLog("Editing Template %s"%templateName)
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('editTemplateBtn'))),action="CLICK")
                time.sleep(10)
                
            pageTitle = "Template Builder" 
            if "Template Builder" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify page title in 'Template Builder' Page :: Actual :'%s', Expected:'%s'"%(self.browserObject.title, pageTitle)
            utility.execLog("Clicked 'Save' to Save Template Information")
            utility.execLog("Verified page title in 'Template Builder' Page :: Actual :'%s', Expected:'%s'"%(self.browserObject.title, pageTitle))           
            #################################################
            #Add Storage Components
            storage = components["Storage"]
            instances = int(storage["Instances"])
            #Get the number of storage instances for 
            if editClone:
                try:
                    storageList= self.handleEvent(EC.presence_of_all_elements_located((By.ID, self.TemplatesObjects('componentStorage'))))
                    instances=len(storageList) # counting the the number storage instances present in the clone template
                except:
                    utility.execLog("No Storage exist in the clone template")
                    
            if instances > 0 :
                storageName = ""
                storageType = str(storage["Type"])
                storageIP = str(storage["IPAddress"])                
                storageSizes = storage["Size"]
                authType = storage["Auth_Type"]
                if authType.lower() == "chap":
                    authType = "CHAP"
                else:
                    authType = "IQN/IP"
                chapUser = storage["CHAP_User"]
                chapPassword = storage["CHAP_Password"]
                osName = storage["Operating_SystemName_Compellent"]
                portType = storage["PortType_Compellent"]
                if storageIP:
                    storageName = [resource["Asset/Service Tag"] for resource in resourceInfo if resource["IP Address"] == storageIP]
                else:
                    storageName = [resource["Asset/Service Tag"] for resource in resourceInfo if resource["Manufacturer/Model"] == storageType]
                if len(storageName) > 0:
                    storageName = storageName[0]
                if storageName == "":
                    raise "Unable to find Storage Resource"
                iqnIP = str(storage["Initiator_IQN_IP_Addresses"])
                aggregateName = str(storage["Aggregate_Name"])
                snapshotsSpace = str(storage["Snapshot_Space"])
                nfsIP = str(storage["NFS_IP"])
                poolNameVNX = ""
                try:
                    poolNameVNX = str(storage["PoolName_VNX"])
                except:
                    poolNameVNX = "Pool 0"
                typeVNX = ""
                try:
                    typeVNX = str(storage["Type_VNX"])
                except:
                    typeVNX = "Non Thin"
                configureSANSwitchVNX = ""
                try:
                    configureSANSwitchVNX = str(storage["ConfigureSANSwitch_VNX"])
                except:
                    configureSANSwitchVNX = "true"
                currentTime = datetime.now().strftime('%y%m%d%H%M%S')
                utility.execLog("Date time :%s"%currentTime)
                trimDatetime = currentTime[6:]

                for storageIndex in xrange(instances):
                    if len(storageSizes) == instances:
                        storageSize = str(storageSizes[storageIndex])
                    else:
                        storageSize = str(storageSizes[0]) 
                    if("New_Volume_Name" in storage.keys()):
                        volumeName = storage["New_Volume_Name"].strip().encode('utf-8') + str(storageIndex)
                    else:      
                        volumeName = "HCLVlmRp" + trimDatetime + str(storageIndex)             
                    self.addStorageComponent(storageType, storageName, volumeName, storageSize, authType, chapUser, chapPassword, iqnIP, osName,
                                portType, aggregateName, snapshotsSpace, nfsIP, editClone, storageIndex, poolNameVNX=poolNameVNX, typeVNX=typeVNX,
                                                                                                        configureSANSwitchVNX=configureSANSwitchVNX)
                    time.sleep(10)
            ##################################################
            #Add Server Components
            server = components["Server"]
            instances = int(server["Instances"])
            #Get the Server instances for editing the clone Template
            if editClone:
                try:
                    serverList = self.handleEvent(EC.presence_of_all_elements_located((By.ID, self.TemplatesObjects('componentServer'))))
                    instances=len(serverList) # counting the the number server instances present in the clone template
                except Exception as e:
                    utility.execLog("No Server exists in the clone template %s"%e)
            
            if instances > 0:
                #Adding Server component for new Template
                if not editClone:
                    status, result,inValidServerTagList=self.addServerComponent(server, networks, verifyNTP, importConfigFromRefServer=importConfigFromRefServer)
                    if status:
                        pass
                    else:
                        if validatePoolAndManualServers:
                            return self.browserObject, False,result,inValidServerTagList
                        else:
                            return self.browserObject, False,result
                    
                    if verifyNTP:
                        return self.browserObject,status,result
                #Editing Server component of pre-existing Template
                else:
                    for serverIndex in xrange(instances):
                        self.addServerComponent(server, networks, verifyNTP, editClone, serverIndex, importConfigFromRefServer=importConfigFromRefServer)
            ##################################################
            #Add Cluster Components
            cluster = components["Cluster"]
            instances = int(cluster["Instances"])  
            #Get cluster instances for the clone Template  
            if editClone:
                try:
                    clusters= self.handleEvent(EC.presence_of_all_elements_located((By.ID, self.TemplatesObjects('componentCluster'))))
                    instances=len(clusters) # counting the the number cluster instances present in the clone template
                except:
                    utility.execLog("No cluster exist in clone template")        
            if instances > 0 :
                targetVMM = ""
                clusterType = str(cluster["Type"])
                if clusterType == "VMWare":
                    vmmType = "vCenter"
                else:
                    vmmType = "HyperV"
                clusterIP = str(cluster["IPAddress"])                
                if clusterIP:
                    targetVMM = [resource["Asset/Service Tag"] for resource in resourceInfo if resource["IP Address"] == clusterIP]
                else:
                    targetVMM = [resource["Asset/Service Tag"] for resource in resourceInfo if resource["Resource Name"] == vmmType]
                if len(targetVMM) > 0:
                    targetVMM = targetVMM[0]
                if targetVMM == "":
                    raise "Unable to find VM Manager"      
                #Adding cluster component for new Template 
                if not editClone:          
                    self.addClusterComponent(cluster, targetVMM)
                #Editing cluster cluster component of a pre-existing Template
                else:
                    for clusterIndex in xrange(instances):
                        self.addClusterComponent(cluster, targetVMM, editClone, clusterIndex)
            ##################################################
            #Add VM Components
            vm = components["VM"]
            instances = int(vm["Instances"])
            #Get the number of vm instances in the clone template
            if editClone:
                try:
#                    vmList= self.browserObject.find_elements_by_xpathby_xpath("//*[contains(@class, 'component vm')]")
                    vmList= self.handleEvent(EC.presence_of_all_elements_located((By.ID, self.TemplatesObjects('componentVM'))))
                    instances=len(vmList) # counting the the number vm instances present in the clone template
                except:
                    utility.execLog("No VM exist in clone template")
            if instances > 0:
                if not editClone:
                    status, result=self.addVMComponent(vm, verifyNTP,OS_Image=OS_Image)
                    if verifyNTP:
                        return self.browserObject,status,result
                else:
                    for vmIndex in xrange(instances):
                        self.addVMComponent(vm, verifyNTP, editClone, vmIndex)
            ##################################################
            #Add Application Components
            application = components["Application"]
            instances = int(application["Instances"])
            if instances > 0:
                if instances > 1:
                    self.addMultipleApplicationComponent(application)
                else:
                    self.addApplicationComponent(application)
            ##################################################            
            #Publishing Template
            if publishTemplate:
                utility.execLog("Clicking 'Publish Template' to Publish Template")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('btnPublish'))), action="CLICK")
                time.sleep(7)
                utility.execLog("Identifying Confirm Dialog box")
                self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.TemplatesObjects('confirmDailogBox'))), action="CLICK")
                time.sleep(5)
                utility.execLog("Confirming to Publish Template '%s'"%templateName)
                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('confirmButton'))), action="CLICK")
                time.sleep(15)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False)
                errorMessage = eleError.find_element_by_tag_name("h3").text
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                try:
                    time.sleep(5)
                    utility.execLog("Identifying Confirm Dialog box and Confirming to Draft Template")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "popupModal")), action="CLICK")
                except:
                    pass
                return self.browserObject, False, "Failed to Create Template :: '%s' :: Error -> '%s'"%(templateName, 
                                                    str(errorMessage))
            except:
                #Move to Templates Page
                utility.execLog("Navigating back to Templates Page")
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                time.sleep(3)
                if not publishTemplate:
                    #Handling Draft Templates Dialog
                    utility.execLog("Identifying Confirm Dialog box and Confirming to Draft Template")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "popupModal")), action="CLICK")
                    time.sleep(5)
                utility.execLog("Navigated back to Templates Page")
                if validatePoolAndManualServers:
                    return self.browserObject, True, "Successfully Created Template:: '%s'"%(templateName),inValidServerTagList
                else:
                    return self.browserObject, True, "Successfully Created Template:: '%s'"%(templateName)                          
        except Exception as e:
            if validatePoolAndManualServers:
                    return self.browserObject, False, "Unable to create New Template :: Error -> %s"%(str(e) + format_exc()),inValidServerTagList
            else:
                return self.browserObject, False, "Unable to create New Template :: Error -> %s"%(str(e) + format_exc())
        
        
    def validateVDSSetting(self,components,networkConfigDict):
        try:
            templateName = (components["Template"]["Name"]).encode('utf-8')
            utility.execLog("Clicking on 'My Templates' Tab")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("tabMyTemplates"))), action="CLICK") 
          
            utility.execLog("Selecting existing template %s to Edit"%templateName)
            self.browserObject, status, result = self.selectTemplate(templateName,"My Templates") 
            time.sleep(10)
            self.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result

            utility.execLog("Clicking on 'View Details'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("viewDetailsBtn"))), action="CLICK")
            time.sleep(10)
            # verify next page title
            title=self.browserObject.title 
            pageTitle = "Template Builder" 
            if "Template Builder" not in title:
                return self.browserObject, False, "Failed to verify page title in 'Template Builder' Page :: Actual :'%s', Expected:'%s'"%(self.browserObject.title, pageTitle)
            utility.execLog("Clicked 'Save' to Save Template Information")
            utility.execLog("Verified page title in 'Template Builder' Page :: Actual :'%s', Expected:'%s'"%(self.browserObject.title, pageTitle))  
            
            utility.execLog("Clicking on 'View All Settings'")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("viewAllSettings"))), action="CLICK")
            time.sleep(10)
            utility.execLog("Verifying Template Settings Page") 
            templatePage=self.browserObject.title           
            if "Template Settings" not in templatePage:
                return self.browserObject, False, "Failed to verify Template Settings Page Title :: Expected:'Template Settings' and Actual:%s"%templatePage.text
            utility.execLog("Verified Template Settings Page Title :: Expected:'Template Settings' and Actual:%s"%templatePage)
            utility.execLog("Fetching All Components") 
            
            utility.execLog("Verifying vmware vshpere cluster presence")
            try:
                cluster= self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vmWareCluster"))))
                #textAll=cluster.text
            except Exception as e:
                    utility.execLog("%s"%e)
                    return self.browserObject, False, "unable to Verifying vmware vshpere cluster presence %s"%e
            utility.execLog("vmware vshpere cluster is present")
            
            #expand all folded tags
            utility.execLog("expand folded tags")

            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vmWareCluster"))), action="CLICK")
            time.sleep(1)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterSettings"))), action="CLICK")
            time.sleep(1)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vSphereVDSSettings"))), action="CLICK")
            time.sleep(1)
      
            #validate Cluter Components:-
            
            mismatch=[]
            for key in networkConfigDict:
                if(key=="vdsCount"):
                    if(networkConfigDict[key]!=self.countDisplayedObj(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects("vds")))))):
                        mismatch.append(key)
                elif(networkConfigDict[key]!=self.countDisplayedObj(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, "//*[contains(text(),"+"'"+key+"'"+")]"))))):
                    mismatch.append(key)
            if(len(mismatch)==0):
                utility.execLog("Successfully validate Cluter Components::%s"%networkConfigDict)
                return self.browserObject, True, "Successfully validate Cluter Components::%s "%networkConfigDict
            else:
                utility.execLog("Failed to validate Cluter Components")
                utility.execLog("Mismatch Cluter Components are ::%s"%mismatch)
                return self.browserObject, False, "unable to validate Cluter Component:: %s"%mismatch
       
        except Exception as e:
            utility.execLog("%s"%e)
            return self.browserObject, False, "unable to validate Cluter Component %s"%e
        
    def countDisplayedObj(self,objlist):
        displayed=[]
        for obj in objlist:
            if(obj.is_displayed()):
                displayed.append(obj)
                #utility.execLog("count=%sand value=%s"(displayed.__len__()),displayed)
        return displayed.__len__()

    def addStorageComponent(self, storageType, storageName, volumeName, volumeSize, authType, authUser, authPassword, iqnOrIP, osName,
                portType, aggregateName, snapshotsSpace, nfsIP, editClone=False, storageIndex=0, poolNameVNX="Pool 0", typeVNX="Non Thin",
                                                                                                                configureSANSwitch="true", volumeNameTemplate=None, typeUnity='Thick'):
        """
        Adds Storage Component
        """
        try:
            #Selecting Storage Component
            utility.execLog("Click on Add Storage Component")
            try: 
                ####Click on Edit Storage component for editing clone Template######################
                if editClone:
                    try:
                        storageList= self.browserObject.find_elements_by_xpath("//*[contains(@class,'component storage')]")
                        selectStorage=storageList[storageIndex]
                        utility.execLog("Click to Edit configuration of storage component#%d"%(storageIndex+1))
                        selectStorage.click()
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH,"//button[@class='btn btn-primary btn-edit']")), action="CLICK")
                        time.sleep(5)  
                    except Exception as e:
                        utility.execLog("%s"%e)
                        time.sleep(5)
                        return self.browserObject, False, "unable to edit storage settings %s"%e
                ####################################################################################################
                #Click on ADD Storage Component for building new Template
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('addStorage'))), action="CLICK")
                    utility.execLog("Clicked on Add Storage")
                    time.sleep(1)
                    self.handleEvent(EC.invisibility_of_element_located((By.CLASS_NAME, self.TemplatesObjects('loadingSpinner'))))
                self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="CLICK")
                time.sleep(5)
                storageComponentText = self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="GET_TEXT")
                if "Storage Component" not in storageComponentText:
                    return self.browserObject, False, "Failed to verify page title in 'Storage Component' Page :: Actual :'%s', Expected:'Storage Component'"%(storageComponentText)
                utility.execLog("Verified page title in 'Storage Component' Page :: Actual :'%s', Expected:'Storage Component'"%(storageComponentText))
            except:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@class,'btn btn-default dropdown-toggle')]")), action="CLICK")
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "lnkAdjustResources_Storage")), action="CLICK")
                time.sleep(5)
            if not editClone:
                utility.execLog("Selecting Storage Type '%s'"%storageType)         
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('ddlComponents'))), action="SELECT", setValue=storageType)

            # Checking if There are no components to associate.
            time.sleep(0.5)
            val = self.is_element_present(By.XPATH, self.TemplatesObjects('verifyNoAssociateCmp'))
            if val:
                utility.execLog("There are no components to associate.")
            else:
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('associateAll'))), action="CLICK")
                except:
                    utility.execLog("Not able to associate resources")

            try:
                utility.execLog("Clicking on 'Continue' in Storage Component Page")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('btnContinueToSettings'))), action="CLICK")
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('continue_form_adjustresources'))), action="CLICK")

            time.sleep(5)
            utility.execLog("Selecting Storage Name '%s'"%storageName)
            xpath = self.TemplatesObjects('targetStorage')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=storageName)
            time.sleep(5)
            utility.execLog("Selecting Storage Volume '%s'"%volumeName)
            xpath = self.TemplatesObjects('storageVolumeName')
            if "HCLVlmRp" in volumeName:
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Select an existing storage volume")
                    time.sleep(3)
                    xpath = "//select[@data-automation-id='volume_existing']"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=volumeName)
                    time.sleep(3)
                except:
                    utility.execLog("Storage Volume '%s' is not available" % volumeName)
                    pass
            utility.execLog("Creating Volume '%s'"%volumeName)
            try:
                xpath = self.TemplatesObjects('storageVolumeName')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Specify a new storage volume name now")
                time.sleep(3)
                xpath = self.TemplatesObjects('storageVolumeNameNew')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=volumeName)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                time.sleep(2)
                utility.execLog("Entering Storage Volume Size '%s'"%volumeSize)
                xpath = self.TemplatesObjects('storageSize')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=volumeSize)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                time.sleep(2)
            except:
                # for build#830
                xpath = self.TemplatesObjects('storageVolumeName')
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=volumeName)
                    time.sleep(3)
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Create New Volume...")
                    time.sleep(3)
                    utility.execLog("Entering Storage Volume Name '%s'"%volumeName)
                    xpath = "//input[@data-automation-id='$new$title']"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=volumeName)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                    utility.execLog("Entering Storage Volume Size '%s'"%volumeSize)
                    xpath = "//input[@data-automation-id='size']"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=volumeSize)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                    time.sleep(2)

            if "EqualLogic" in storageType:
                utility.execLog("Selecting Authentication Type '%s'"%authType)
                xpath = self.TemplatesObjects('authentication')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=authType)
                time.sleep(5)
                if authType == "CHAP":
                    utility.execLog("Entering CHAP User Name '%s'"%authUser)
                    xpath = self.TemplatesObjects('chapUsername')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=authUser)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                    time.sleep(2)
                    utility.execLog("Entering CHAP Password '%s'"%authPassword)
                    xpath = self.TemplatesObjects('chapPassword')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=authPassword)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                    time.sleep(2)
                else:
                    utility.execLog("Entering IQN/IP Details '%s'"%iqnOrIP)
                    xpath = self.TemplatesObjects('IQNIP')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=iqnOrIP)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="\t")
                    time.sleep(2)
            elif "Compellent" in storageType:
                utility.execLog("Storage is Compellent hence no auth type")
                utility.execLog("Selecting operating system name %s "%str(osName))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('selectOS'))), action="SELECT", setValue=osName)
                time.sleep(1)
                utility.execLog("selecting port Type to %s"%str(portType))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('portType'))), action="SELECT", setValue=portType)
                time.sleep(1)
            elif "VNX" in storageType:
                
                utility.execLog("Selecting Pool name %s from dropdown"%str(poolNameVNX))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('storagePool'))), action="SELECT", setValue=poolNameVNX)
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('storageComponentType'))), action="SELECT", setValue=typeVNX)
                except:
                    utility.execLog("Selected with deafault type")
                utility.execLog("Type is selected successfully")
                if configureSANSwitch == "false":
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('configureSan'))), action="CLICK")
                else:
                    utility.execLog("SAN Switch configure as a default")
            else:
                utility.execLog("Storage is Unity")
                utility.execLog('Selecting Pool name')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('storagePool'))), action="SELECT", setValue=poolNameVNX)
                utility.execLog('Entering Storage Volume Name')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('storageVolumeName'))), action="SET_TEXT", setValue=volumeName)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('storageVolumeName'))), action="SET_TEXT", setValue='\t')
                utility.execLog('Entering Volume name Template')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('volumeNameTemplate'))), action= "SET_TEXT", setvalue=volumeNameTemplate)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('volumeNameTemplate'))), action= "SET_TEXT", setvalue='\t')
                utility.execLog('Entereing Volume Size')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('storageSize'))), action= "SET_TEXT", setvalue=volumeNameTemplate)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('storageSize'))), action= "SET_TEXT", setvalue='\t')
                utility.execLog("Selecting Storage Component Type")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('storageComponentType'))), action="SELECT", setValue=typeUnity)
                configureSANSwitchEnabled=self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('configureSan'))), action='IS_SELECTED')
                if configureSANSwitch=='true':
                    if not configureSANSwitchEnabled:
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('configureSan'))), action='CLICK')
                else:
                    if configureSANSwitchEnabled:
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('configureSan'))), action='CLICK')
                time.sleep(1)
            #Saving Storage Component
            utility.execLog("Clicking on 'Add' to Save Storage Component Details")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('templateBuilderDetails'))), action="CLICK")
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_adjustresources2")), action="CLICK")

            time.sleep(10)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('RedBoxError'))))
                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('RedBoxErrorMessages'))), action="GET_TEXT")
                utility.execLog("Unable to Add Storage Component(s) :: Error -> %s"%errorMessage)                
            except:
                utility.execLog("Successfully Added Storage Component(s) :: '%s'"%(storageName))
            else:
                raise "Unable to Add Storage Component(s) :: Error -> %s"%errorMessage
        except Exception as e:
            utility.execLog("Unable to Add Storage Component :: Error -> %s"%(str(e) + format_exc()))
            raise e
        
    #Validate Settings for the server
    def validateSettingsServer(self):
#         PresenceOfValidateSettings=self.check_xpath_exists("//button[@id='templatebuilder_validatesettings' and contains(@data-bind,'disable')]")
        PresenceOfValidateSettings=self.check_xpath_exists("//button[@id='templatebuilder_validatesettings' and @class='btn btn-default']")
        time.sleep(3)
        if(PresenceOfValidateSettings):
            utility.execLog("Validate Settings Button is Available")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//button[@id='templatebuilder_validatesettings'  and @class='btn btn-default']")),
                                                                                                                        action="CLICK", setValue="\t")
            time.sleep(5)
            NoOfRows = self.browserObject.find_elements_by_xpath("//tbody[@data-bind='foreach: pageddata']/tr")
            if(len(NoOfRows)>0):
                NoOfAvailableServersText = self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//span[@id='items_count']")),
                                                                                                        action="GET_TEXT", setValue="\t")
                if((str(len(NoOfRows)))==str(NoOfAvailableServersText)):
                    utility.execLog("Number of available Servers are '%s'"%str(NoOfAvailableServersText))
                else:
                    utility.execLog("Number of available server rows doesnt match with the available server text: No of Rows are '%s', Text is '%s'"%(str(len(NoOfRows)))%str(NoOfAvailableServersText))
            else:
                utility.execLog("No servers available in the validated settings window")   
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//footer/button[@id='cancel_form_templatesettings']")), action="CLICK", setValue="\t")
            time.sleep(5)
        else:
            utility.execLog("Validate Settings Button is not Available")
            
    def validateServerForTemplate(self,serverDict):
        '''
        API to validate Servers during template creation(tested)
        
        '''
        try:
            serverPoolDetailList=serverDict["serverPoolDetail"]
            PresenceOfValidateSettings=self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects('validateSettings'))), action='IS_DISPLAYED')
            time.sleep(3)
            inValidServerList=[]
            
            if(PresenceOfValidateSettings):
                utility.execLog("Validate Settings Button is Available")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('validateSettings'))),action="CLICK", setValue="\t")
                time.sleep(5)
                errorMessage=False
                try:
                    errorMessage=self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action='GET_TEXT', wait_time=30)
                except Exception as e:
                    pass
                if errorMessage:
                    raise "Error during validation:- {}".format(errorMessage)  
                NoOfRows= len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects('validateSettingsTableRows')))))
                validateSettingsInfo=self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('validateSettingsInfo'))), action='GET_TEXT')
                totalValidCount=validateSettingsInfo.split(' ')[0]
                totalServerCount=validateSettingsInfo.split(' ')[2]
#                totalServerCount=self.browserObject.find_element_by_xpath("//*[@id='page_templatebuilder_validatesettings']/p/b/span[@data-bind='text: validateResponse.totalservers']").text
                if totalServerCount<=0:
                    raise "TotalServerCount can not be Zero.This is know GUI BUG:-totalServerCount={}".totalServerCount   
                elif not (str(NoOfRows)==totalValidCount):
                    raise "Number of rows in validation table is not equals to total valid count servers"
                elif not (int(totalServerCount)==len(serverPoolDetailList)):
                    raise "Number of rows in validation table is not equals to total valid count servers"
                    
                row=1
                validateServertagList=[]
                for row in range(1,NoOfRows+1):
                    Servertag= self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('validServerTag').format(row))), action="GET_TEXT")
                    validateServertagList.append(Servertag)
                utility.execLog("validateServertagList={}".format(validateServertagList))
                
            else:
                utility.execLog("Validate Settings Button is not Available")
                raise "Validate Settings Button is not Available"
           
            for  serverDetail in  serverPoolDetailList:
                if serverDetail["Asset/Service Tag"] not in validateServertagList:
                    inValidServerList.append(serverDetail)
            utility.execLog("inValidServerList={}".format(inValidServerList))
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('cancelFormValidateSettings') )), action="CLICK", setValue="\t")
            time.sleep(5)
            return True,inValidServerList
        except Exception as e:
            utility.execLog("exception")
            return False,str(e)
    
            
    def addServerComponent(self, component, networkInfo, verifyNTP = False, editClone= False, serverIndex=0, importConfigFromRefServer= False,staticIP=False,manualServer=False):
        """
        Adds Server Component        
        """
        try:
            instances = component["Instances"]
            #Selecting Server Component
            utility.execLog("Click on Add Server Component")
            selectServerComponent = "Server" 
            if not editClone: 
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('addServer'))), action="CLICK")       
                    time.sleep(5)            
                    self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="CLICK")
                    time.sleep(3)
                    serverComponentText = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="GET_TEXT")
                    if "Server" not in serverComponentText:
                        return self.browserObject, False, "Failed to verify page title in 'Server Component' Page :: Actual :'%s', Expected:'Server Component'"%(serverComponentText)
                    utility.execLog("Verified page title in 'Server Component' Page :: Actual :'%s', Expected:'Server Component'"%(serverComponentText))
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('dropdownToggle'))), action="CLICK")
                    time.sleep(2)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('lnkAdjustResourcesServer'))), action="CLICK")
                    time.sleep(5)
                    if staticIP:
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('componentDuplicate'))), action="CLICK") 
                        utility.execLog("Selected new server option")
                        time.sleep(10)
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('componenttoduplicate'))), action="SELECT", setValue=selectServerComponent)
                        time.sleep(5)
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('continueFormAdjustresources'))), action="CLICK")
                        time.sleep(7)
                        if (staticIP and networkInfo!=None):
                            #utility.execLog(networks)
                            utility.execLog("Selecting 'User Entered IP'")
                
                            #Initialization
                            staticIP = globalVars.staticIP
                
                            #For Validation
                            for key in staticIP.keys():
                                self.staticIPList[key] = []
                
                            serverInstance = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects('serverSource')))))
                            serverRowCount = 1
                
                            #Setting Static IPs for Servers
                            while (serverInstance > 0):
                                #Setting IPs manually for Server 
                                parentXPATH=".//*[@id='DeploySettings']/li[%i]"%serverRowCount
                                xpath=parentXPATH+"/fieldset/div/ul[2]/li[2]/div[2]/ul/li[2]/label/input"
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                                time.sleep(2)
                                row = 4 
                                rootXPATH=parentXPATH+"/fieldset/div/ul[2]/li[%i]/div[1]/label"%row

                                while (self.check_xpath_exists(rootXPATH)):
                                    nwType = None           
                                    nwLabel = self.handleEvent(EC.presence_of_element_located((By.XPATH, rootXPATH)), action="GET_TEXT")
                                    utility.execLog("Label:%s"%nwLabel)
                        
                                    for nwCount in xrange(0, len(networkInfo)):
                                        if(str(networkInfo[nwCount]['Name']) in nwLabel):
                                            if(networkInfo[nwCount]['IP Address Setting'] == 'Static'):
                                                nwType = networkInfo[nwCount]['Network Type']
                                    
                                    if (nwType!=None):
                                        utility.execLog("Network Type:%s"%nwType)
                                        if(len(staticIP[nwType]) > 0 and staticIP[nwType][0]!=""):
                                            xpath=parentXPATH+"/fieldset/div/ul[2]/li[%i]/div[2]/select"%row
                                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Manual Entry")
                                            time.sleep(2)
                                            #Updating the Row Count if IP is provided
                                            row=row+2
            
                                            xpath=parentXPATH+"//fieldset/div/ul[2]/li[%i]/div[2]/input"%row
                                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=staticIP[nwType][0])
                                            self.staticIPList[nwType].append(staticIP[nwType][0])
                                            del staticIP[nwType][0]
                                            time.sleep(2)

                                        else:
                                            xpath=parentXPATH+"/fieldset/div/ul[2]/li[%i]/div[2]/select"%row
                                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="ASM Selected IP")
                                            time.sleep(2)
                        
                                    row=row+2
                                    rootXPATH=parentXPATH+"/fieldset/div/ul[2]/li[%i]/div[1]/label"%row
                                serverInstance=serverInstance-1
                                serverRowCount=serverRowCount+1
                            utility.execLog("Updating Static IP List for Validation: %s"%str(self.staticIPList))
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('pageAdjustResources'))), action="CLICK")
                            time.sleep(3)
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitFormAdjustresources1'))), action="CLICK")
                            time.sleep(20)
                            return  True, "Successfully scaleup server with manual os ip"
                            
                    else:
                        self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('componentNew'))), action="CLICK") 
                        utility.execLog("Selected new server option")
                        time.sleep(2)
                utility.execLog("Selecting 'Server' Component")
                
                try:
                    utility.execLog("..........")
                    selectServerComponent = str(component["SelectServerComponent"])
                except:
                    utility.execLog("*********")
                #self.handleEvent(EC.element_to_be_clickable((By.ID, "ddlComponents")), action="SELECT", setValue="Server")
                try:
                    time.sleep(15)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('ddlComponents'))), action="SELECT", setValue=selectServerComponent)
                    time.sleep(3)
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('componenttoduplicate'))), action="SELECT", setValue=selectServerComponent)
                    time.sleep(3)
                try:
                    utility.execLog("Entering No of 'Instances' : %i"%instances)
                    if instances==1:
                        utility.execLog("Server 'Instances' set to 1")
                    else:
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('instances'))), action="CLEAR")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('instances'))), action="SET_TEXT", setValue=instances)
                except:
                    utility.execLog("Number of instances not required scale-up mode with new server.")
            ##############clicking edit on server component of clone Template##############################
            else:
                try:
                    serverList= self.handleEvent(EC.presence_of_all_elements_located((By.ID,self.TemplatesObjects('componentServer'))))
                    utility.execLog("Click to Edit configuration of server component#%d"%(serverIndex+1))
                    serverList[serverIndex].click()
                    time.sleep(5)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('componentEdit'))), action="CLICK")
                    time.sleep(5)  
                except Exception as e:
                    utility.execLog("%s"%e)
                    return self.browserObject, False, "unable to edit server settings %s"%e
            ##################################################################################################
            try:
                utility.execLog("Selecting 'Associate All Resources'")
                if self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('associateAll'))), action="IS_DISPLAYED", wait_time=10):                    
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('associateAll'))), action="CLICK")
            except Exception as e:
                utility.execLog("No components to 'Associate All Resources'")
                pass
            utility.execLog("Clicking on 'Continue' in Server Conponent Page")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('btnContinueToSettings'))), action="CLICK")
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('continueFormAdjustresources'))), action="CLICK")
            ##########Import Configuration From Reference Server###
            if importConfigFromRefServer:
                self.handleEvent(EC.element_to_be_clickable((By.ID,"btnCloneFromReference")), action= "CLICK")
                time.sleep(4)
                serverList=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects('selectReferenceServer'))))
                serverList[0].click()
                time.sleep(4)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitFormCloneServer'))), action="CLICK")
                time.sleep(4)
            ######################################################
            time.sleep(5)            
            utility.execLog("Clicking on 'Auto-generate Host Name'")
            xpath = self.TemplatesObjects('autoGenerateHostName')
            isSelected=self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="IS_SELECTED")
            if not isSelected:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                time.sleep(2)
            utility.execLog("Entering Host Name Template")
            xpath = self.TemplatesObjects('hostNameTemplate')
            hostNameTemplate = component["HostNameTemplate"].encode('utf-8')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=hostNameTemplate)            
            osImage = str(component["OSImage_Value"][0])
            utility.execLog("Setting Razor Image as '%s'"%osImage)
            xpath = self.TemplatesObjects('osImage')
            if not editClone:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=osImage)
                time.sleep(3)
            else:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('osImageAlt'))), action="SELECT", setValue=osImage, selectBy="VALUE")

            installHyperV = component["install_HyperV"].encode('utf-8')
            if installHyperV == "true" :
                try:
                    utility.execLog("selecting the install Hyper-V checkbox.")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('installHyperV'))), action="CLICK")
                except:
                    utility.execLog("Hyper-V install option not available")
            else:
                utility.execLog("Hyper-V install option no need to click")

            try:
                utility.execLog("Selecting OS Image version from dropdown")
                osImageVer = str(component["Os_Image_Version"])
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('osImageVersion'))), action="SELECT", setValue=osImageVer, selectBy="VISIBLE_TEXT", wait_time=10)
                time.sleep(1)

                utility.execLog("Entering the product key")
                productKey = str(component["Product_Key"])
                xpath = "//*[contains(@id,'setting_product_key_asm')]"                
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('productKey'))), action="SET_TEXT", setValue=productKey)

                utility.execLog("Entering the domain name")
                domainName = str(component["domain_name"])
                xpath = self.TemplatesObjects('domainName')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=domainName)

                utility.execLog("Entering the domain admin username")
                domainAdminName = str(component["Domain_Admin_Username"])
                xpath = self.TemplatesObjects('domainUserName')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=domainAdminName)

                utility.execLog("Entering the domain admin password")
                domainAdminpass = str(component["Domain_Admin_Password"])
                xpath = self.TemplatesObjects('domainAdminPassword')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=domainAdminpass)

                utility.execLog("Entering the domain admin password confirmation")
                xpath = self.TemplatesObjects('domainAdminConfirmPassword')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=domainAdminpass)
            except Exception as e:
                utility.execLog("OS Image details are not required. Error-> %s"%str(e))
            try:
                utility.execLog("Entering FQ domain name")
                fqDomainName = str(component["FQ_Domain_Name"])
                xpath = self.TemplatesObjects('fqDomainName')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR", wait_time=10)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)), action="SET_TEXT", setValue=fqDomainName)
            except:
                utility.execLog("FQ domain name is not required")
  
            utility.execLog("Entering Administrator Password")
            xpath= self.TemplatesObjects('adminPassword')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="Dell1234")
            utility.execLog("Entering Confirm Administrator Password")
            xpath= self.TemplatesObjects('confirmAdminPassword')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="Dell1234")
            primaryNTP = globalVars.configInfo['Appliance']['ntpserver']
            
            #To check pre-population of NTP Server in Template
            checkNTP = self.verifyNTP(primaryNTP)
            
            #Code Snippet to Add multiple comma separated NTP Servers. The input is taken from config.ini else default value provided in TC-4721, TC-4722, TC-4723, TC-4724
            #Based on verifyNTP, we return after checking for pre-population for TC-4506, 4507
            if checkNTP:
                if verifyNTP:
                    self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                    #Navigating back to Template page
                    self.handleEvent(EC.presence_of_element_located((By.XPATH, self.Navigation('Templates'))), action= "CLICK")
                    time.sleep(10)
                    try:
                        utility.execLog("Acknowledging the popup message")
                        self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('closePopup'))), action= "CLICK")
                        time.sleep(5)
                    except:
                        utility.execLog("no popup message displayed")
                    return True,"NTP Server value which is pre-populated matches with the Preferred NTP Server info set in the appliance"
                else:
                    if (globalVars.configInfo['Appliance']['multiplentp'] != ""):
                        primaryNTP = globalVars.configInfo['Appliance']['multiplentp'] #For multiple NTP --> TC-4721, 4723, 4724
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('ntpServer'))), action="SET_TEXT", setValue=primaryNTP)
            else:
                self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                raise "NTP Server value which is pre-populated mismatches with the Preferred NTP Server info set in the appliance"
            
            #Check to see if the NTP Server field is editable or not. If yes, setting it to primaryNTP (Single or Multiple, based on Testcase)
#             try:
#                 utility.execLog("Entering NTP Server")
#                 xpath= "//input[@data-automation-id='ntp_server']"
#                 self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
#                 self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=primaryNTP)
#             except Exception as e:
#                 raise "Exception while editing pre-populated NTP Server field :: Error -> %s"%(str(e) + format_exc())
            if selectServerComponent == "Server (O/S Installation Only)":
                utility.execLog("Adding Server component")
                time.sleep(5)
                serverPoolName = str(component["ServerPoolName"])
                utility.execLog("Setting Server Pool as '%s'"%serverPoolName)
                xpath = self.TemplatesObjects('setServerPool')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=serverPoolName)
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_adjustresources2")), action="CLICK")
                utility.execLog("Added Server component")
                time.sleep(30)
                return True, "Server component Added successfully"
             
            utility.execLog("Selecting vSan checkbox")
            try:
                vsan = component["vSAN"]
                if "true" in vsan:
                    xpath=self.TemplatesObjects('setVsan')
                    isSelected=self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="IS_SELECTED")
                    if isSelected:
                        isSelected=self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    storageType=str(component["vSANStorageType"])
                    xpath="//input[@type='radio' and @value='%s']"%storageType
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,xpath)), action="CLICK")
                    utility.execLog("slected vSAN storage type %s"%storageType)
                else:
                    utility.execLog("vSAN value is false")
                time.sleep(1)
            except Exception as e:
                utility.execLog("vsan in not required Error:> %s"%str(e)) 
            iscsiInitiator = component["iSCSI_Initiator"]
            utility.execLog("Setting ISCSI Initiator as '%s'"%iscsiInitiator)
            xpath = self.TemplatesObjects('isciInitiator')
            try:
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED", wait_time=10):
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=iscsiInitiator, selectBy="VALUE")
            except Exception as e:
                utility.execLog("Need not Set ISCSI Initiator as '%s:: Error-> %s'"%(str(iscsiInitiator), str(e)))
            try:
                memEnabled = component["memEnabled"]
                utility.execLog("Setting Mem as '%s'"%memEnabled)
                xpath = self.TemplatesObjects('equalLogicMem')
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED", wait_time=10):
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=memEnabled, selectBy="VALUE")
            except Exception as e:
                utility.execLog("Need not Set Mem as '%s'"%memEnabled)
                
            #Hardware Settings
            utility.execLog("Hardware Settings")
            xpath = self.TemplatesObjects('hardwareSettingTab')
            expandStatus = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName="class")
            if "collapsed" in expandStatus:
                utility.execLog("Component 'Hardware Settings' is collapsed so expanding")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                time.sleep(3)
            targetBootDevice = 'string:'+str(component["target_boot_device_value"])
            utility.execLog("Setting Target Boot Device as '%s'"%targetBootDevice)
            xpath = self.TemplatesObjects('targetBootDevice')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=targetBootDevice, selectBy="VALUE")
#             if targetBootDevice == "HD":
#                 pass
                #===============================================================
                # utility.execLog("Setting Raid Level as '%s'"%raidLevel)
                # hardwareSettings.find_element_by_id("raidtype_basic").click()
                # time.sleep(2)
                # eleServer = hardwareSettings.find_element_by_id("ddlBasicRaidLevel_1")
                # Select(eleServer).select_by_visible_text(raidLevel)
                # time.sleep(3)
                #===============================================================
            if (targetBootDevice in ("HD","SD_WITH_RAID","NONE_WITH_RAID")):
                raid_type = ""
                try:
                    raid_type = str(component["Raid_Type"])
                except:
                    utility.execLog("Basic Configuration")
                utility.execLog("Selecting Raid value %s"%str(raid_type))
                if "Basic" in raid_type:
                    self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('raidTypeBasic'))), action="CLICK")
                    raid_basic = str(component["Raid_Basic"])
                    utility.execLog("Selecting Raid Basic value %s"%str(raid_basic))
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('selectRaidLevel'))), action="SELECT", setValue=raid_basic, selectBy="VISIBLE_TEXT")
                    time.sleep(1)
                elif "Advance" in raid_type:
                    self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('raidTypeAdvanced'))), action="CLICK")
                    utility.execLog("Performing Raid Advance settings")
                    raidInfo = str(component["Raid_Advance"])
                    raidConfig = getattr(globalVars, raidInfo) 
                    utility.execLog("Raid Configuration :: '%s'"%raidConfig)
                    raidAdvConf = json.loads(raidConfig)

                    if "InternalVD" in raidAdvConf:
                        loopIteration = 1

                        for internalVD in raidAdvConf["InternalVD"]:
                            self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('btnAddVirtualDisk'))), action="CLICK")
                            utility.execLog("Clicked add internal virtual disk for disk {}.".format(loopIteration))
                            time.sleep(2)

                            utility.execLog("Select Raid Level {} for Internal VD #{}".format(str(internalVD["raidLevel"]), loopIteration))
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('raidLevel'). \
                                                            format(loopIteration - 1))), action="SELECT", setValue=internalVD["raidLevel"], selectBy="VISIBLE_TEXT")
                            if str(internalVD["raidLevel"]) != "RAID 0"and str(internalVD["raidLevel"]) != "non-RAID":
                                objectClass = self.browserObject.find_element_by_xpath(self.TemplatesObjects('internalVirtualdiskTable').format(loopIteration - 1)).get_attribute('class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Did not find Internal Virtual Disk status in expected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                                else:
                                    utility.execLog("Found Internal Virtual Disk status in expected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                            elif str(internalVD["raidLevel"]) == "RAID 0" or str(internalVD["raidLevel"]) == "Non-RAID":
#                                objectClass = self.browserObject.find_element_by_xpath("//div[contains(@data-bind,'visible: virtualdisks')]/table/tbody/tr").get_attribute('class')
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('internalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if str(objectClass) == "":
                                    utility.execLog("Found Internal Virtual Disk status in expected mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                                else:
                                    utility.execLog("Did not find Internal Virtual Disk status in unexpected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))

                            utility.execLog("Select # Disks dropdown value {} for Internal VD #{}".format(str(internalVD["noOfDisksType"]), loopIteration))
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('ddlDiskComparator').format(loopIteration - 1))),
                                        action="SELECT", setValue=internalVD["noOfDisksType"], selectBy="VISIBLE_TEXT")

                            if str(internalVD["raidLevel"]) != "RAID 0" and str(internalVD["raidLevel"]) != "non-RAID":
                                objectClass = self.browserObject.find_element_by_xpath(self.TemplatesObjects('internalVirtualDiskTable')).get_attribute('class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Did not find Internal Virtual Disk status in expected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                                else:
                                    utility.execLog("Found Internal Virtual Disk status in expected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                            elif str(internalVD["raidLevel"]) == "RAID 0" or str(internalVD["raidLevel"]) == "Non-RAID":
#                                objectClass = self.browserObject.find_element_by_xpath("//div[contains(@data-bind,'visible: virtualdisks')]/table/tbody/tr").get_attribute('class')
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('internalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if str(objectClass) == "":
                                    utility.execLog("Found Internal Virtual Disk status in expected mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                                else:
                                    utility.execLog("Did not find Internal Virtual Disk status in unexpected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))

                            utility.execLog("Enter number of disks value {} in input box for disk #{}".format(str(internalVD["noOfDisks"]), loopIteration))
#                            xpath = "//div[contains(@data-bind,'visible: virtualdisks')]/table/tbody/tr/td/select[@id=" + "'ddlDiskComparator_{}".format(loopIteration - 1) + "']/parent::td/input[@id='ddlNumberOfDisks']"
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('ddlNumberOfDisks'))), action="CLEAR")
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=internalVD["noOfDisks"])

                            if str(internalVD["raidLevel"]) != "RAID 0"and str(internalVD["raidLevel"]) != "non-RAID":
#                                objectClass = self.browserObject.find_element_by_xpath("//div[contains(@data-bind,'visible: virtualdisks')]/table/tbody/tr").get_attribute('class')
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('internalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Did not find Internal Virtual Disk status in expected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                                else:
                                    utility.execLog("Found Internal Virtual Disk status in expected valid error, Raid type: {}".format(str(internalVD["raidLevel"])))
                            elif str(internalVD["raidLevel"]) == "RAID 0" or str(internalVD["raidLevel"]) == "Non-RAID":
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('internalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Found Internal Virtual Disk status in expected mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                                else:
                                    utility.execLog("Did not find Internal Virtual Disk status in unexpected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))

                            utility.execLog("Select Disk Requirements dropdown value {} for Internal VD #{}".format(str(internalVD["diskRequirements"]), loopIteration))
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//div[contains(@data-bind,'visible: virtualdisks')]/table/tbody/tr/td/select[@id='ddlDiskType_{}']". \
                                                                         format(loopIteration - 1))), action="SELECT", setValue=internalVD["diskRequirements"], selectBy="VISIBLE_TEXT")

                            if str(internalVD["raidLevel"]) != "RAID 0"and str(internalVD["raidLevel"]) != "non-RAID":
                                objectClass = self.browserObject.find_element_by_xpath("//div[contains(@data-bind,'visible: virtualdisks')]/table/tbody/tr").get_attribute('class')
#                                if str(objectClass) != "":
                                if 'invalid' in str(objectClass):
                                    utility.execLog("Did not find Internal Virtual Disk status in expected valid mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                                else:
                                    utility.execLog("Found Internal Virtual Disk status in expected valid mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                            elif str(internalVD["raidLevel"]) == "RAID 0" or str(internalVD["raidLevel"]) == "Non-RAID":
                                objectClass = self.browserObject.find_element_by_xpath("//div[contains(@data-bind,'visible: virtualdisks')]/table/tbody/tr").get_attribute('class')
                                if str(objectClass) == "":
                                    utility.execLog("Found Internal Virtual Disk status in expected mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                                else:
                                    utility.execLog("Did not find Internal Virtual Disk status in unexpected error mode, Raid type: {}".format(str(internalVD["raidLevel"])))
                        
                            loopIteration += 1
                    else:
                        utility.execLog("Did not find Internal Virtual Disk information")

                    if "IntVDGlobalHotspares" in raidAdvConf:
                        utility.execLog("Click 'Add Internal Virtual disk' button")
                        self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('enableGlobalHotspares'))), action="CLICK")
                        time.sleep(2)
                        utility.execLog("Enter number of disks value %s in input box"%str(raidAdvConf["IntVDGlobalHotspares"]["number"]))
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('totalHotsparesInt'))), action="CLEAR")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('totalHotsparesInt'))), action="SET_TEXT",
                                             setValue=raidAdvConf["IntVDGlobalHotspares"]["number"])

                        utility.execLog("Enter minimum # SSDs value %s in input box"%str(raidAdvConf["IntVDGlobalHotspares"]["ssd"]))
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('minimumSSDInt'))), action="CLEAR")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('minimumSSDInt'))), action="SET_TEXT",
                                             setValue=raidAdvConf["IntVDGlobalHotspares"]["ssd"])
                    else:
                        utility.execLog("Did not find Internal Virtual Disk Global Hotspares information")

                    if "ExternalVD" in raidAdvConf:
                        loopIteration = 1
                        for externalVD in raidAdvConf["ExternalVD"]:
#                    if "select" in raidAdvConf["ExternalVD"]["value"]:
                            utility.execLog("Click 'Add External Virtual disk' button")
                            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('btnExternalVirtualDisk') )), action="CLICK")
                            time.sleep(2)
                            utility.execLog("Select Raid Level %s for External VD"%str(externalVD["raidLevel"]))
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('raidLevelExternal'). \
                                                            format(loopIteration - 1))), action="SELECT", setValue=externalVD["raidLevel"], selectBy="VISIBLE_TEXT")

                            if str(externalVD["raidLevel"]) != "RAID 0"and str(externalVD["raidLevel"]) != "non-RAID":
#                                objectClass = self.browserObject.find_element_by_xpath("//div[contains(@data-bind,'visible: externalvirtualdisks')]/table/tbody/tr").get_attribute('class')
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('externalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Did not find External Virtual Disk status in expected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                                else:
                                    utility.execLog("Found External Virtual Disk status in expected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                            elif str(externalVD["raidLevel"]) == "RAID 0" or str(externalVD["raidLevel"]) == "Non-RAID":
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('externalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Found External Virtual Disk status in expected mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                                else:
                                    utility.execLog("Did not find External Virtual Disk status in unexpected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))

                            utility.execLog("Select # Disks dropdown value %s for External VD"%str(externalVD["noOfDisksType"]))
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//div[contains(@data-bind,'visible: externalvirtualdisks')]/table/tbody/tr/td/select[@id='ddlDiskComparator_{}']". \
                                                            format(loopIteration - 1))), action="SELECT", setValue=externalVD["noOfDisksType"], selectBy="VISIBLE_TEXT")

                            if str(externalVD["raidLevel"]) != "RAID 0" and str(externalVD["raidLevel"]) != "non-RAID":
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('externalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Did not find External Virtual Disk status in expected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                                else:
                                    utility.execLog("Found External Virtual Disk status in expected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                            elif str(externalVD["raidLevel"]) == "RAID 0" or str(externalVD["raidLevel"]) == "Non-RAID":
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('externalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Found External Virtual Disk status in expected mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                                else:
                                    utility.execLog("Did not find External Virtual Disk status in unexpected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))

                            utility.execLog("Enter number of disks value %s in input box"%str(externalVD["noOfDisks"]))
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "Number1")), action="CLEAR")
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "Number1")), action="SET_TEXT", setValue = externalVD["noOfDisks"])

                            if str(externalVD["raidLevel"]) != "RAID 0"and str(externalVD["raidLevel"]) != "non-RAID":
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('externalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Did not find External Virtual Disk status in expected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                                else:
                                    utility.execLog("Found External Virtual Disk status in expected valid error, Raid type: {}".format(str(externalVD["raidLevel"])))
                            elif str(externalVD["raidLevel"]) == "RAID 0" or str(externalVD["raidLevel"]) == "Non-RAID":
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('externalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Found External Virtual Disk status in expected mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                                else:
                                    utility.execLog("Did not find External Virtual Disk status in unexpected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))

                            utility.execLog("Select Disk REquirements dropdown value %s"%str(externalVD["diskRequirements"]))
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//div[contains(@data-bind,'visible: externalvirtualdisks')]/table/tbody/tr/td/select[@id='ddlDiskType_{}']". \
                                                            format(loopIteration - 1))), action="SELECT", setValue=externalVD["diskRequirements"], selectBy="VISIBLE_TEXT")

                            if str(externalVD["raidLevel"]) != "RAID 0"and str(externalVD["raidLevel"]) != "non-RAID":
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('externalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' in str(objectClass):
                                    utility.execLog("Did not find External Virtual Disk status in expected valid mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                                else:
                                    utility.execLog("Found External Virtual Disk status in expected valid mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                            elif str(externalVD["raidLevel"]) == "RAID 0" or str(externalVD["raidLevel"]) == "Non-RAID":
                                objectClass = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('externalVirtualDiskTable'))), action="GET_ATTRIBUTE_VALUE", attributeName='class')
                                if 'invalid' not in str(objectClass):
                                    utility.execLog("Found External Virtual Disk status in expected mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                                else:
                                    utility.execLog("Did not find External Virtual Disk status in unexpected error mode, Raid type: {}".format(str(externalVD["raidLevel"])))
                        
                            loopIteration += 1
                    else:
                        utility.execLog("Did not find External Virtual Disk information")

                    if "ExtVDGlobalHotspares" in raidAdvConf:
                        utility.execLog("Select Enable Global hot spares checkbox for external virtual disk.")
                        self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('enableGlobalHotsparesExt'))), action="CLICK")
                        time.sleep(1)
                        utility.execLog("Enter Total # Hotspares value %s"%str(raidAdvConf["ExtVDGlobalHotspares"]["number"]))
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('totalHotsparesExt'))), action="CLEAR")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('totalHotsparesExt'))), action="SET_TEXT", setValue=raidAdvConf["ExtVDGlobalHotspares"]["number"])
                        utility.execLog("Enter Minimum # SSDs from total hotspares value %s"%str(raidAdvConf["ExtVDGlobalHotspares"]["ssd"]))
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('minimumSSDExt'))), action="CLEAR")
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('minimumSSDExt'))), action="SET_TEXT", setValue=raidAdvConf["ExtVDGlobalHotspares"]["ssd"])
                    else:
                        utility.execLog("Did not find External Virtual Disk Global Hotspares information")
                        
            else:
                utility.execLog("Raid configuration is not required.")    
            serverPoolName = str(component["ServerPoolName"])
            utility.execLog("Setting Server Pool as '%s'"%serverPoolName)
            xpath = self.TemplatesObjects('selectServerPool')
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=serverPoolName)
            #BIOS Settings for VSAN
            utility.execLog("BIOS Settings ")
            xpath = self.TemplatesObjects('biosSettingsTab')
            expandStatus =self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName='class')
            if "collapsed" in expandStatus:
                utility.execLog("Component 'BIOS settings' is collapsed so expnading")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)), action="CLICK")
                time.sleep(3)
                try:
                    nodeIntleave = str(component["NodeInterleave"])
                    utility.execLog("selecting Node Inter leave values from dropdown %s"%str(nodeIntleave))
                    xpath = self.TemplatesObjects('nodeInterleaving')
                    if nodeIntleave != "":
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=nodeIntleave)
                        
                except Exception as e:
                    utility.execLog("By default selected enabled")
                    time.sleep(2)
            
            #Network Settings
            utility.execLog("Network Settings")
            xpath = self.TemplatesObjects('networkSettingTab')
            expandStatus = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_ATTRIBUTE_VALUE", attributeName="class")
            if "collapsed" in expandStatus:
                utility.execLog("Component 'Network Settings' is collapsed so expanding")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                time.sleep(3)
            ##################Editing network for clone template################################    
            if editClone:
                interfaceDelete= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects('btnDeleteInterface'))))
                for interfaceDeleteBtn in interfaceDelete:
                    interfaceDeleteBtn.click()
                    time.sleep(5)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('btnConfirmDeleteInterface'))), action="CLICK")
                    time.sleep(5)
            ##########################################################################################       
            nwInfo = str(component["Networks"])
            nwConfig = getattr(globalVars, nwInfo) 
            utility.execLog("Networks :: '%s'"%nwConfig)
            networks = json.loads(nwConfig)
            index = 0
            staticPublicLAN = False
            staticPublicLANName = ""
            
            for key, network in networks.items():
                #if index != 1:
                utility.execLog("Adding New Interface")
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('btnAddInterface'))), action="CLICK")
                time.sleep(3)                
                interfaces = self.handleEvent(EC.presence_of_all_elements_located((By.CLASS_NAME, self.TemplatesObjects('interfaceAccordion'))))
                utility.execLog("network['fabricType'] = %s"%str(network["fabricType"]))
                if network["fabricType"] == "Ethernet":
                    xpath=self.TemplatesObjects('fabricEthernet')
                    interfaces[index].find_element_by_xpath(xpath).click()
                else:
#                     xpath="//div[@id='fabricTypeInputs']/div[2]/label/input"
                    xpath= self.TemplatesObjects('fabricFibreCh')
                    radio=interfaces[index].find_element_by_xpath(xpath)
#                    radio = self.browserObject.find_element_by_xpath("(//div[@id='fabricTypeInputs']/div[2]/label/input)[%s]"%str(index+1))
                    self.browserObject.execute_script("arguments[0].click();", radio) 
                    continue                  
#                     interfaces[index].find_element_by_xpath(xpath).click()  
                portLayout='string:'+network["portLayout"]              
                Select(interfaces[index].find_element_by_id(self.TemplatesObjects('portLayout'))).select_by_value(portLayout)
                if network["enablePartition"] == "True":
                    interfaces[index].find_element_by_xpath("partitioning").click()
                    time.sleep(3)
                if network["enableRedundancy"] == "True":
                    interfaces[index].find_element_by_xpath("redundancy").click()
                    time.sleep(3)
                ports = network["Ports"]
                #tables = interfaces[index].find_element_by_id("InterfaceContent_%d"%index).find_elements_by_class_name("table-striped")
                networkPorts = interfaces[index].find_elements_by_class_name("resourcedropdown")
                partitionIndex = 0
                multipleNetworks = []
                sortedPorts = sorted(ports.keys()) 
                for portName in sortedPorts:
                    option = ports[portName]
                    port = ports[portName]
                    utility.execLog("Selecting Networks for Port '%s' on Interface '%s' with Networks '%s'"%(option, key, str(port)))
                    sortedPartitions = sorted(port.keys())
                    for partitionName in sortedPartitions:
                        partitionNetworks = port[partitionName]
                        utility.execLog("Selecting Partition '%s' on Port '%s' on Interface '%s' with Networks '%s'"%(partitionName, 
                                        option, key, str(port)))
                        xpath = "./button"
                        valPart = partitionIndex
                        for validPartition in range(valPart, len(networkPorts)):
                            if not networkPorts[validPartition].is_displayed():
                                partitionIndex += 1
                            else:
                                break
                        if networkPorts[partitionIndex].is_displayed():
                            networkPorts[partitionIndex].find_element_by_xpath(xpath).click()
                            #networksXpath = "./tbody/td[2]/div/ul/li"
                            networksXpath = "./ul/li"
                            networkNames = networkPorts[partitionIndex].find_elements_by_xpath(networksXpath)
                            userNetworks = []
                            dupNetworks= False
                            for usN in partitionNetworks:
                                for net in networkInfo:
                                    if usN[-1].isdigit():
                                        dupNetworks = True
                                        usN = usN[:-1]
#                                    if net["Network Type"] == usN:
                                    if net["Name"] == usN or net["Network Type"] == usN:
                                        if net["Network Type"] == "Public LAN" and net["IP Address Setting"] == "Static":
                                            staticPublicLAN = True
                                            staticPublicLANName = net["Name"]

                                        if dupNetworks:
                                            if net["Name"].encode('utf-8') not in multipleNetworks:
                                                userNetworks.append(net["Name"].encode('utf-8'))
                                                multipleNetworks.append(net["Name"].encode('utf-8'))
                                                break
                                        else:
                                            userNetworks.append(net["Name"].encode('utf-8'))
                                            break
                            #userNetworks = [net["Name"] for usN in partitionNetworks for net in networkInfo if net["Network Type"] == usN]
                            #userNetworks = [net["Name"] for usN in partitionNetworks for net in networkInfo if net["Network Type"] == usN]
                            for userNetwork in userNetworks:
                                for nw in networkNames:
                                    nwName = ((nw.find_element_by_xpath("span[2]").text)).encode('utf-8')
                                    nwName= nwName.strip()
                                    if nwName == userNetwork:
                                        utility.execLog("Selecting VLAN '%s' "%(nwName))
                                        nw.find_element_by_tag_name("input").click()
                                        break

                            networkPorts[partitionIndex].find_element_by_xpath(xpath).click()
                            utility.execLog("Selecting Partition '%s' on Port '%s' on Interface '%s' with Networks '%s'"%(partitionName, 
                                            option, key, str(port)))
                        partitionIndex += 1
                index += 1
            try:
                utility.execLog("Selecting Network Default Gateway")
                xpath = self.TemplatesObjects('defaultGateway')
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED", wait_time=10) and staticPublicLAN == True:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="{}".format(staticPublicLANName, selectBy="NAME"))
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="dhcp_workload", selectBy="VALUE")
                time.sleep(5)
            except Exception as e:
                utility.execLog("No Static Network Default Gateway")
                pass
            self.validateSettingsServer()
            status,inValidServeTagrList=self.validateServerForTemplate(component)
            if status:
                pass
            else:
                return False,"Unable to Validate  Servers",inValidServeTagrList
            #Saving Server Component
            utility.execLog("Clicking on 'Save' to Save Server Component Details")
            try:
                time.sleep(5)
                self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects('templateBuilderDetails'))), action="CLICK")
                utility.execLog("Clicking on 'Save' to Save button")
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('submitConfirmForm'))), action="CLICK")
            except Exception as e:
                utility.execLog("Error on click save %s"%e)
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('submitFormAdjustresources2'))), action="CLICK")
            time.sleep(10)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action= "GET_TEXT")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                time.sleep(3)
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                try:
                    time.sleep(5)
                    utility.execLog("Identifying Confirm Dialog box and Confirming to Draft Template")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('popupModal'))), action="CLICK")
                except:
                    pass                
                utility.execLog("Unable to Add Server Component(s) :: Error -> %s"%errorMessage)
                raise "Unable to Add Server Component(s) :: Error -> %s"%errorMessage
            except:
                utility.execLog("Successfully Added Server Component(s) :: '%s'"%(component))
                return True,"Successfully Added Server Component(s)",inValidServeTagrList
        except Exception as e:
            utility.execLog("Unable to Add Server Component(s) :: Error -> %s"%(str(e) + format_exc()))
            return False,"Unable to Add Server Component(s)",inValidServeTagrList
            #raise e
    
    def addClusterComponent(self, component, targetVMM, editClone=False, clusterIndex=0):
        """
        Adds Cluster Component
        """
        try:
            instances = component["Instances"]
            ##############clicking edit on cluster component of clone Template##############################
            if editClone:
                try:
                    clusterList= self.browserObject.find_elements_by_xpath("//*[contains(@class,'component cluster')]")
                    selectCluster=clusterList[clusterIndex]
                    utility.execLog("Click to Edit configuration of cluster component#%d"%(clusterIndex+1))
                    selectCluster.click()
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,"//button[@class='btn btn-primary btn-edit']")), action="CLICK")
                    time.sleep(5)  
                except Exception as e:
                    utility.execLog("%s"%e)
                    time.sleep(5)
                    return self.browserObject, False, "unable to edit cluster settings %s"%e
            ######################################################################################################
            else:
                utility.execLog("Click on Add Cluster Component") 
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("addCluster"))), action="CLICK")       
                time.sleep(15)      
            try:      
                self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects("pageComponentEditor"))), action="CLICK")
                time.sleep(3)
                clusterComponentText = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, self.TemplatesObjects("pageComponenteditor"))), action="GET_TEXT")
                if "Cluster Component" not in clusterComponentText:
                    return self.browserObject, False, "Failed to verify page title in 'Cluster Component' Page :: Actual :'%s', Expected:'Cluster Component'"%(clusterComponentText)
                utility.execLog("Verified page title in 'Cluster Component' Page :: Actual :'%s', Expected:'Cluster Component'"%(clusterComponentText))
            except:
#                xpath="//*[contains(@class,'btn btn-default dropdown-toggle')]"
                xpath= self.TemplatesObjects('dropdownToggle')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath )), action="CLICK")
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("lnkAdjustResources_Cluster"))), action="CLICK")
                time.sleep(5)
                
            if not editClone:    
                utility.execLog("Selecting Cluster Component Name")
                if component["Type"].lower() == "vmware":
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("ddlComponents"))), action="SELECT", setValue="VMWare Cluster")
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("ddlComponents"))), action="SELECT", setValue="Hyper-V Cluster")
                time.sleep(3)
                
            try:
                utility.execLog("Entering No of 'Instances' : %i"%instances)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("txtInstances"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("txtInstances"))), action="SET_TEXT", setValue=instances)
            except:
                utility.execLog("Number of instances not required in scale-up mode.")
            try:            
                if self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects("associateAll"))), action="IS_DISPLAYED"):
                    utility.execLog("Selecting 'Associate All Resources'")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("associateAll"))), action="CLICK")
            except Exception as e:
                utility.execLog("No components to 'Associate All Resources'")
            utility.execLog("Clicking on 'Continue' in Cluster Conponent Page")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("btnContinueToSettings"))), action="CLICK")
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("continueFormAdjustResources"))), action="CLICK")
            time.sleep(5)
            if component["Type"].lower() == "vmware":
                utility.execLog("Entering Cluster Settings.")
                #xpath = "//label[contains(text(),'Target Virtual Machine Manager')]/parent::div/parent::li/div[2]/select" 
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("targetVirtualMachineManager"))), action="SELECT", setValue=targetVMM)
                utility.execLog("Entering Data Center Settings.")
                dcName = component["DCName"].encode('utf-8')
                if dcName == "":
                    dcName = "autoDC"
                #xpath = "//label[contains(text(),'Data Center Name')]/parent::div/parent::li/div[2]/select" 
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("dataCenterName"))), action="SELECT", setValue=dcName)
                    time.sleep(5)
                except:
#                     try:
                    utility.execLog("Datacenter Name '%s' is not available so Creating New Datacenter"%dcName)
#                     except:
#                         utility.execLog("Datacenter Name is not available so Creating New Datacenter")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("DataCenterName"))), action="SELECT", setValue="Create New Datacenter...")
                    time.sleep(5)
                    #xpath = "//label[contains(text(),'New datacenter name')]/parent::div/parent::li/div[2]/input"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newDatacenterName"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newDatacenterName"))), action="SET_TEXT", setValue=dcName)
                clusterName = component["ClusterName"].encode("utf-8")
                if clusterName == "":
                    clusterName = "autoCluster"
                #xpath = "//label[contains(text(),'Cluster Name')]/parent::div/parent::li/div[2]/select"
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterName"))), action="SELECT", setValue=clusterName)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterName"))), action="SET_TEXT", setValue="\t")
                    time.sleep(5)
                except:
                    try:
                        utility.execLog("Cluster Name '%s' is not available so Creating New Cluster"%clusterName)
                    except:
                        utility.execLog("Cluster Name is not available so Creating New Cluster")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterName"))), action="SELECT", setValue="Create New Cluster...")
                    time.sleep(5)
                    #xpath = "//label[contains(text(),'New cluster name')]/parent::div/parent::li/div[2]/input"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterName"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterName"))), action="SET_TEXT", setValue=clusterName)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterName"))), action="SET_TEXT", setValue="\t")
                    time.sleep(3)
                haEnabled =  component["HA_Config"]
                utility.execLog("Setting HA Option to '%s'"%haEnabled)
                if haEnabled == "true":
                    if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("haEnabled"))), action="IS_SELECTED"):
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("haEnabled"))), action="CLICK")
                drsEnabled =  component["DRS_Config"]
                utility.execLog("Setting DRS Option to '%s'"%haEnabled)
                if drsEnabled == "true":
                    if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("drsEnabled"))), action="IS_SELECTED"):
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("drsEnabled"))), action="CLICK")
                vsanEnabled =  component["VSAN_Enabled"]
                utility.execLog("Setting VSAN Option to '%s'"%vsanEnabled)
                if vsanEnabled == "true":
                    if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vsanEnabled"))), action="IS_SELECTED"):
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vsanEnabled"))), action="CLICK")
                if component["SwitchType"] == "Distributed":
                    utility.execLog("Selecting 'Distributed' Switch")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("distributed"))), action="CLICK")
                    time.sleep(3)
                    utility.execLog("Enter vSphere VDS Settings")
                    '''
                    testCount=0
                    testList=self.browserObject.find_elements_by_xpath(xpath)
                    for test in testList:
                        if(test.is_displayed()):
                            testCount=testCount + 1
                            display=test
                            utility.execLog("testCount =%s"%testCount)
                    '''
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vSphereVDSSettings"))), action="CLICK")
                    #display.click()
                    time.sleep(1)
                    vdsNames= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects("vdsNames")))) 
                    i=1
                    for elem in vdsNames:
                        utility.execLog("Entering the VDS Name for %s"%str(elem))
                        Select(elem).select_by_visible_text("Create VDS Name ...")
                        time.sleep(1)
                        #xpath = "(//label[contains(text( ),'New VDS Name')]/parent::div/parent::li/div[2]/input)[%s]"%str(i)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newVDSNames"))), action="SET_TEXT", setValue="VDSName %s"%str(i))
                        i += 1
                    portGroups= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects("portGroups"))))
                    j=1
                    for elem in portGroups:
                        utility.execLog("Creating the Port Group for %s"%str(elem))
                        Select(elem).select_by_visible_text("Create Port Group ...")
                        time.sleep(1)
                        #xpath = "(//label[contains(text( ),'New Port Group')]/parent::div/parent::li/div[2]/input)[%s]"%str(j)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newPortGroups"))), action="SET_TEXT", setValue="Port Group %s"%str(j))
                        time.sleep(1)
                        j += 1
                    
                else:
                    utility.execLog("Selecting 'Standard' Switch")
                    #self.handleEvent(EC.element_to_be_clickable((By.ID, "radioSetting_1")), action="CLICK")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("standard"))),action="CLICK")
                    time.sleep(3)
                vsanEnabled =  component["VSAN_Enabled"]
                utility.execLog("Setting VSAN Option to '%s'"%vsanEnabled)
                if vsanEnabled == "true":
                    if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vsanEnabled"))), action="IS_SELECTED"):
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vsanEnabled"))), action="CLICK")
                    utility.execLog("Setting Failure tolerance method and number of tolerance")
                try:
                    toleranceMethod = component["failure_tolerance_method"]
                    toleranceNumber = component["number_of_failure_to_tolerate"]
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("failureTolerance"))), action="SELECT", setValue=toleranceMethod, selectBy="VALUE")
                    time.sleep(2)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("settingFailures"))), action="SELECT", setValue=toleranceNumber, selectBy="VALUE")
                except:
                    utility.execLog("Need nto to set number of faillure to tolereate")
                try:
                    haEnabled =  component["HA_Config"]
                    utility.execLog("Setting HA Option to '%s'"%haEnabled)
                    if haEnabled == "true":
                        if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("haEnabled"))), action="IS_SELECTED"):
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("haEnabled"))), action="CLICK")
                    drsEnabled =  component["DRS_Config"]
                    utility.execLog("Setting DRS Option to '%s'"%haEnabled)
                    if drsEnabled == "true":
                        if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("drsEnabled"))), action="IS_SELECTED"):
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("drsEnabled"))), action="CLICK")
                except:
                    utility.execLog("HA and DRS options are not available")
                try:
                    utility.execLog("Setting Storage DRS enabled checkbox.")
                    storageDrs = component["storage_drs_enabled"]
                    if storageDrs:
                        if not self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("storageDrs"))), action="IS_SELECTED"):
                            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("storageDrs"))), action="CLICK")
                        time.sleep(2)
                        utility.execLog("Entering storage cluster name %s"%str(clusterName))
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("storageDrsCluster"))), action="SET_TEXT", setValue=clusterName)
                        utility.execLog("selecting drs storage checkboxes.")
                        drsStorageCheckboxes = self.browserObject.find_elements_by_xpath("//*[contains(text(),'Datastores to Add to Cluster')]//parent::div//parent::li/div[2]/ul/li/div/label/input")
                        for item in drsStorageCheckboxes:
                            item.click()                            
                except Exception as e:
                    utility.execLog("Storage DRS Enabled option not available. :: Error-> %s"%str(e))
            else:
                utility.execLog("Entering Cluster Settings.")
                #xpath = "//label[contains(text(),'Target Virtual Machine Manager')]/parent::div/parent::li/div[2]/select" 
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("targetVirtualMachineManager"))), action="SELECT", setValue=targetVMM)
                time.sleep(1)
                utility.execLog("Entering Host Group Settings.")
                hgName = component["HGName"]
                if hgName == "":
                    hgName = "autoHostGroup"
                #xpath = "//select[@data-automation-id='hostgroup']" 
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("hostGroup"))), action="SELECT", setValue=hgName)
                    time.sleep(5)
                except:
                    utility.execLog("Host Group Name '%s' is not available so Creating New Host Group"%hgName)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="New Host Group...")
                    time.sleep(5)
                    #xpath = "//input[@data-automation-id='$new$hostgroup']"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newHostGroup"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newHostGroup"))), action="SET_TEXT", setValue=hgName)
                clusterName = component["ClusterName"].encode('utf-8')
                if clusterName == "":
                    clusterName = "autoCluster"
                #xpath = "//select[@data-automation-id='name']" 
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterNameHyper"))), action="SELECT", setValue=clusterName)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterNameHyper"))), action="SET_TEXT", setValue="\t")
                    time.sleep(5)
                except:
                    try:
                        utility.execLog("Cluster Name '%s' is not available so Creating New Cluster"%clusterName)
                    except:
                        utility.execLog("Cluster Name is not available so Creating New Cluster") 
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterNameHyper"))), action="SELECT", setValue="New Cluster...")
                    time.sleep(5)
                    #xpath = "//input[@data-automation-id='$new$name']"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterNameHyper"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterNameHyper"))), action="SET_TEXT", setValue=clusterName)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterNameHyper"))), action="SET_TEXT", setValue="\t")
                    time.sleep(3)
                hypervClusterIP = component["ClusterIP"]
                #xpath = "//input[@data-automation-id='ipaddress']"
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("hypervClusterIP"))), action="SET_TEXT", setValue=hypervClusterIP)
                time.sleep(3)
            utility.execLog("Submitting the Cluster form")
            try:
                #self.handleEvent(EC.presence_of_element_located((By.ID,"templateBuilderDetails")), action="CLICK")
                #time.sleep(3)
                utility.execLog("clicking on save button")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("submitConfirmForm"))), action="CLICK")
            except Exception as e:
                utility.execLog(e)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("submitFormAdjustResources"))), action="CLICK")
            time.sleep(10)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action= "GET_TEXT")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                utility.execLog("Failed to Added Cluster Component(s) :: '%s Error ::%s'"%(component,errorMessage))
            except:
                utility.execLog("Successfully Added Cluster Component(s) :: '%s'"%(component))
            else:
                raise "Unable to Add Cluster Component(s)"
        except Exception as e:
            utility.execLog("Unable to Add Cluster Component(s) :: Error -> %s"%(str(e) + format_exc()))
            raise e             
            
    def addVMComponent(self, component, verifyNTP=False, editClone= False, vmIndex=0, OS_Image="", networkInfo=[], staticIP=False,manualServer=False):
        """
        Adds VM Component
        """
        try:
            ###########################click on Edit VM component#######################
            if editClone:
                try:
                    vmList= self.browserObject.find_elements_by_xpath("//*[contains(@class,'component vm')]")
                    selectvm=vmList[vmIndex]
                    utility.execLog("Click to Edit configuration of vm component#%d"%(vmIndex+1))
                    selectvm.click()
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,"//button[@class='btn btn-primary btn-edit']")), action="CLICK")
                    time.sleep(5)  
                except Exception as e:
                    utility.execLog("%s"%e)
                    time.sleep(5)
                    return self.browserObject, False, "unable to edit cluster settings %s"%e
            ################################################################################
            ###############click on add new VM component#####################
            else:
                instances = component["Instances"]
                utility.execLog("Click on Add VM Component")  
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('addVM'))), action="CLICK")       
                    time.sleep(5)            
                    self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="CLICK")
                    time.sleep(3)
                    vmComponentText = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, self.TemplatesObjects('pageComponenteditor'))), action="GET_TEXT")
                    if "VM Component" not in vmComponentText:
                        return self.browserObject, False, "Failed to verify page title in 'Virtual Machine Component' Page :: Actual :'%s', Expected:'Virtual Machine Component'"%(vmComponentText)
                    utility.execLog("Verified page title in 'Virtual Machine Component' Page :: Actual :'%s', Expected:'Virtual Machine Component'"%(vmComponentText))
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('dropdownToggle'))), action="CLICK")
                    time.sleep(2)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('lnkAdjustResourcesVM'))), action="CLICK")
                    time.sleep(5)
                    if staticIP:
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('componentDuplicate'))), action="CLICK") 
                        utility.execLog("Selected new vm option")
                        time.sleep(2)
                        utility.execLog("Selecting Virtual Machine Component Name")
                        if component["Type"].lower() == "vmwareclonevm":
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('componenttoduplicate'))), action="SELECT", setValue="Clone vCenter Virtual Machine")
                        elif component["Type"].lower() == "hypervclonevm":
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('componenttoduplicate'))), action="SELECT", setValue="Clone Hyper-V Virtual Machine")
                        else:
                            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('componenttoduplicate'))), action="SELECT", setValue="vCenter Virtual Machine")
                            time.sleep(3)
                        
                        self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('continueFormAdjustresources'))), action="CLICK")
                        time.sleep(7)
                        if (staticIP and networkInfo!=None):
                            #utility.execLog(networks)
                            utility.execLog("Selecting 'User Entered IP'")
                
                            #Initialization
                            staticIP = globalVars.staticIP
                
                            #For Validation
                            for key in staticIP.keys():
                                self.staticIPList[key] = []
                
                            vmInstance = 1
                            vmRowCount = 1
                            
                            #Setting Static IPs for VMs
                            while (vmInstance > 0):
                                parentXPATH=".//*[@id='DeploySettings']/li[%i]"%vmRowCount
                                xpath=parentXPATH+"/fieldset/div/ul[2]/li[2]/div[2]/ul/li[2]/label/input"
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                                time.sleep(2)
                                labelXPATH=parentXPATH+"/fieldset/div/ul/li[2]/div[1]/label"
                     
                                if (self.check_xpath_exists(labelXPATH)):
                                    labelName = self.handleEvent(EC.presence_of_element_located((By.XPATH, labelXPATH)), action="GET_TEXT")
                                    if (labelName == "IP Source"):
                                        
                                        xpath=parentXPATH+"/fieldset/div/ul[2]/li[2]/div[2]/ul/li[2]/label/input"
                                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                                        time.sleep(2)
                                        row = 4 
                                        rootXPATH=parentXPATH+"/fieldset/div/ul[2]/li[%i]/div[1]/label"%row
                                 
                                        if (self.check_xpath_exists(rootXPATH)):
                                            nwType = None           
                                            nwLabel = self.handleEvent(EC.presence_of_element_located((By.XPATH, rootXPATH)), action="GET_TEXT")
                                            utility.execLog("Label:%s"%nwLabel)
                                 
                                            for nwCount in xrange(0, len(networkInfo)):
                                                if(str(networkInfo[nwCount]['Name']) in nwLabel):
                                                    if(networkInfo[nwCount]['IP Address Setting'] == 'Static'):
                                                        nwType = networkInfo[nwCount]['Network Type']
                                                     
                                            if (nwType!=None):
                                                utility.execLog("Network Type:%s"%nwType)
                                                if(len(staticIP[nwType]) > 0 and staticIP[nwType][0]!=""):
                                                    xpath=parentXPATH+"/fieldset/div/ul[2]/li[%i]/div[2]/select"%row
                                                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="Manual Entry")
                                                    time.sleep(2)
                                           
                                                    #Updating the Row Count if IP is provided
                                                    row=row+2
                                                  
                                                    xpath=parentXPATH+"/fieldset/div/ul[2]/li[%i]/div[2]/input"%row
                                                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=staticIP[nwType][0])
                                                    self.staticIPList[nwType].append(staticIP[nwType][0])
                                                    del staticIP[nwType][0]
                                                    time.sleep(2)
                                            else:
                                                xpath=parentXPATH+"/fieldset/div/ul[2]/li[%i]/div[2]/select"%row
                                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue="ASM Selected IP")
                                                time.sleep(2)
                                    
                                vmInstance=vmInstance-1
                                vmRowCount=vmRowCount+1
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "page_adjustresources")), action="CLICK")
                            time.sleep(5)
                            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_adjustresources1")), action="CLICK")
                            time.sleep(10)
                            try:
                                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action= "GET_TEXT")
                                utility.execLog("Unable to Add VM Component(s) :: Error -> %s"%errorMessage)
                                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                                time.sleep(3)
                                return False, "VM component not scale up"
                            except:
                                utility.execLog("Successfully Added VM Component(s) :: '%s'"%(component))
                                return True, "Successfully scale up VM component"
                            
                    else:
                        self.handleEvent(EC.element_to_be_clickable((By.ID, "component_new")), action="CLICK") 
                        utility.execLog("Selected new VM option")
                        time.sleep(2)
                utility.execLog("Selecting Virtual Machine Component Name")
                if component["Type"].lower() == "vmwareclonevm":
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('ddlComponents'))), action="SELECT", setValue="Clone vCenter Virtual Machine")
                elif component["Type"].lower() == "hypervclonevm":
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('ddlComponents'))), action="SELECT", setValue="Clone Hyper-V Virtual Machine")
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('ddlComponents'))), action="SELECT", setValue="vCenter Virtual Machine")
                time.sleep(3)
          
            try:
                utility.execLog("Entering No of 'Instances' : %i"%instances)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('txtInstances'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('txtInstances'))), action="SET_TEXT", setValue=instances)
            except:
                utility.execLog("Number of instances not required in scale-up mode")
            try:            
                if self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('associateAll'))), action="IS_DISPLAYED"):
                    utility.execLog("Selecting 'Associate All Resources'")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('associateAll'))), action="CLICK")
            except NoSuchElementException:
                utility.execLog("No components to 'Associate All Resources'")
            utility.execLog("Clicking on 'Continue' in Cluster Conponent Page")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('btnContinueToSettings'))), action="CLICK")
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('continueFormAdjustResources'))), action="CLICK")
            time.sleep(5)
            
            if "hypervclonevm" in component["Type"]:
                utility.execLog("Clicking on 'Auto-generate Host Name'")
                xpath = self.TemplatesObjects('autoGenerateVmName')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                utility.execLog("Selecting template")
                vmTemplateName = component["vm_template_name"]
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmTemplate'))), action="SELECT", setValue=vmTemplateName)
                time.sleep(1)
                utility.execLog("Entering path")
                path = component["path"]
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmPath'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmPath'))), action="SET_TEXT", setValue=path)
                utility.execLog("Selecting Network checkbox.")
#                 xpath=(//label[contains(text( ),'Networks')]/parent::div/parent::li/div[2]//input)[1]
                xpath = self.TemplatesObjects('vmNetwork')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                time.sleep(1)
                utility.execLog("Selecting value for Static Network Default Gateway")
                try:
                    staticNWDefaultGatway = component["Static Network Default Gateway"]
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmDefaultGateway'))), action="SELECT", setValue=staticNWDefaultGatway)
                    utility.execLog("Selected value for Static Network Default Gateway")
                    time.sleep(3)
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmDefaultGateway'))), action="SELECT", setValue="dhcp_workload", selectBy="VALUE")
                    utility.execLog("Selected value for Static Network Default Gateway is 'DHCP / No Gateway'")
                    time.sleep(3)
                startAction = component["start_action"]
                stopAction = component["stop_action"]
                utility.execLog("Selecting start up action")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmStartAction'))), action="SELECT", setValue=startAction)
                time.sleep(1)
                utility.execLog("Selecting stop action")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmStopAction'))), action="SELECT", setValue=stopAction)
                time.sleep(1)
            else:    
                autoHost = component["AutoGenerateHostName"]
                if autoHost == "true":
                    utility.execLog("Clicking on 'Auto-generate Host Name'")
                    xpath = self.TemplatesObjects('autoGenerateHostName')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    time.sleep(2)
                    hostNameTemplate = component["HostNameTemplate"].encode('utf-8')
                    if hostNameTemplate != "":
                        utility.execLog("Entering Host Name Template '%s'"%hostNameTemplate)
                        xpath = self.TemplatesObjects('vmHostName')
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=hostNameTemplate)
                if OS_Image == "":
                    osImage = component["OS_Image"]
                    utility.execLog("Setting Razor Image as '%s'"%osImage)
                    xpath = self.TemplatesObjects('vmOSImage')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=osImage)
                    time.sleep(3)
                else:
                    utility.execLog("Else Setting Razor Image as '%s'"%OS_Image)
                    xpath = self.TemplatesObjects('vmOSImage')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=OS_Image)
                    time.sleep(3) 
                try:            
                    osImageVersion = component["OS_Image_Version"]
                    xpath = self.TemplatesObjects('vmOSImageVersion')
                    if self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        utility.execLog("Selecting OS Image Version '%s'"%osImageVersion)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SELECT", setValue=osImageVersion)
                except Exception as e:
                    utility.execLog("Need not specify OS Image Version '%s'"%osImageVersion)
                utility.execLog("Entering Administrator Password")
                xpath= self.TemplatesObjects('vmOSPassword')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="Dell1234")
                utility.execLog("Entering Confirm Administrator Password")
                xpath= self.TemplatesObjects('vmOSPasswordCnfrm')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue="Dell1234")
                try:            
                    productKey = component["Product_Key"]
                    xpath = self.TemplatesObjects('vmOSProductKey')
                    if self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="IS_DISPLAYED", wait_time=10):
                        utility.execLog("Entering Product Key : '%s'"%productKey)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=productKey)
                except:
                    utility.execLog("Need not specify Product Key : '%s'"%productKey)                
                primaryNTP = globalVars.configInfo['Appliance']['ntpserver']
                
                checkNTP = self.verifyNTP(primaryNTP)
                
                #Code Snippet to Add multiple comma separated NTP Servers. The input is taken from config.ini else default value provided in TC-4721, TC-4722, TC-4723, TC-4724
                #Based on verifyNTP, we return after checking for pre-population for TC-4506, 4507
                if checkNTP:
                    if verifyNTP:
                        self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                        #Navigating back to Template page
                        self.handleEvent(EC.presence_of_element_located((By.XPATH, self.Navigation('Templates'))), action= "CLICK")
                        time.sleep(10)
                        try:
                            utility.execLog("Acknowledging the popup message")
                            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects('closePopup'))), action= "CLICK")
                            time.sleep(5)
                        except:
                            utility.execLog("no popup message displayed")
                        return True,"NTP Server value which is pre-populated matches with the Preferred NTP Server info set in the appliance"
                    else:
                        if (globalVars.configInfo['Appliance']['multiplentp'] != ""):
                            primaryNTP = globalVars.configInfo['Appliance']['multiplentp'] #For multiple NTP --> TC-4721, 4723, 4724
                else:
                    self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                    raise "NTP Server value which is pre-populated mismatches with the Preferred NTP Server info set in the appliance"

#                 #Check to see if the NTP Server field is editable or not. If yes, setting it to primaryNTP (Single or Multiple, based on Testcase)
#                 try:
#                     utility.execLog("Entering NTP Server")
#                     xpath= "//input[@data-automation-id='ntp_server']"
#                     self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLEAR")
#                     self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="SET_TEXT", setValue=primaryNTP)
#                 except Exception as e:
#                     raise "Exception while editing pre-populated NTP Server field :: Error -> %s"%(str(e) + format_exc())
                    
                utility.execLog("Enter Virtual Machine settings")
                xpath = self.TemplatesObjects('vmSettingTab')
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                utility.execLog("Virtual Machine Setitngs tab expanded.")
                time.sleep(1)
                cpuCount = component["CPU_Count"]
                utility.execLog("Entering number of CPU's to %s"%str(cpuCount))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmCPUCount'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmCPUCount'))), action="SET_TEXT", setValue=cpuCount)
                time.sleep(1)
                memoryMb = component["Memory_MB"]
                utility.execLog("Entering memory value %s"%str(memoryMb))
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmMemoryMB'))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmMemoryMB'))), action="SET_TEXT", setValue=memoryMb)
                time.sleep(1)
                utility.execLog("Selecting Network checkbox.")
                try:
                    networks = component["Networks"]
                    if networks == "All":
#                        elements=self.browserObject.find_elements_by_xpath("//label[contains(text( ),'Networks')]/parent::div/parent::li/div[2]//input")
                        elements=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects('vmNetwork'))))
                        for i in xrange(1,len(elements)+1):
                            self.browserObject.find_element_by_xpath(self.TemplatesObjects('vmNetwork')+"["+str(i)+"]").click()
                            time.sleep(2)
                except:
                    xpath = self.TemplatesObjects('vmNetwork')
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    time.sleep(2)
                utility.execLog("Selecting value for Static Network Default Gateway")
                try:
                    staticNWDefaultGatway = component["Static Network Default Gateway"]
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('vmDefaultGateway'))), action="SELECT", setValue=staticNWDefaultGatway)
                    utility.execLog("Selected value for Static Network Default Gateway")
                    time.sleep(3)
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('vmDefaultGateway'))), action="SELECT", setValue="dhcp_workload", selectBy="VALUE")
                    utility.execLog("Selected value for Static Network Default Gateway is 'DHCP / No Gateway'")
                    time.sleep(3)
            utility.execLog("Submitting the VM form")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('confirmComponentAdd'))), action="CLICK")
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_adjustresources2")), action="CLICK")
            time.sleep(10)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action= "GET_TEXT")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                utility.execLog("Failed to Added Cluster Component(s) :: '%s Error ::%s'"%(component,errorMessage))
                utility.execLog("Unable to Add VM Component(s) :: Error -> %s"%errorMessage)         
            except:
                utility.execLog("Successfully Added VM Component(s) :: '%s'"%(component))
                return True, "Successfully Added VM component"
            else:
                raise "Unable to Add VM Component(s) :: Error -> %s"%errorMessage
        except Exception as e:
            utility.execLog("Unable to Add VM Component(s) :: Error -> %s"%(str(e) + format_exc()))
            raise e
        
    def addApplicationComponent(self, component):
        """
        Add Application Component
        """
        try:
            utility.execLog("Click on Add Application Component")  
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addApplication"))),action="CLICK")       
                time.sleep(5)   
            except:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@class,'btn btn-default dropdown-toggle')]")), action="CLICK")
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "lnkAdjustResources_Application")), action="CLICK")
                time.sleep(10)
            try:            
                if self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("selectServerVM"))), action="IS_DISPLAYED"):
                    utility.execLog("Selecting Available Resources")
                    try:
                        elements=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("selectServerVM"))))
                        for i in xrange(0,len(elements)):
                            elements[i].click()
                    except:
                        raise "Unable to Add Application Component(s)"
                else:
                    raise "Unable to Add Application Component(s):- Please add Server or VM first"
            except NoSuchElementException:
                utility.execLog("No components to 'Associate Resources'")
            utility.execLog("Clicking on 'Continue' in Application Component Page")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
            time.sleep(5)
            component=component["Application"]
            applicationType = component["Type"]
            utility.execLog("Selecting the application type %s"%str(component["Type"].lower()))
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("selectApplication"))),action="SELECT",setValue=applicationType)
            except Exception as e:
                raise "Unable to Add Application Component(s) :: Error -> Application type ' %s"%str(e)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("addClick"))),action="CLICK")
            utility.execLog("Added the application %s"%str(applicationType))
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
            utility.execLog("Clicked Next button on Identify Applications page")
            time.sleep(3)
            if "say_something" in applicationType.lower():
                addOnString = component["addOn_modle_string"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_title_say_something')]")), action="SET_TEXT", setValue=addOnString)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("saySomething"))),action="SET_TEXT",setValue=addOnString)
                time.sleep(1)
            elif applicationType.lower() in ("linux_postinstall", "linuxpostinstall"):
                installPackages = component["install_packages"]
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("installPackages"))),action="SET_TEXT",setValue=installPackages)
                utility.execLog("Value %s entered for install_packages"%str(installPackages))
                uploadShare = component["upload_share"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_upload_share_linux_postinstall')]")), action="SET_TEXT", setValue=uploadShare)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("uploadShare"))),action="SET_TEXT",setValue=uploadShare)
                utility.execLog("Value %s entered for upload_share"%str(uploadShare))
                uploadFile = component["upload_file"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_upload_file_linux_postinstall')]")), action="SET_TEXT", setValue=uploadFile)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("uploadFile"))),action="SET_TEXT",setValue=uploadFile)
                utility.execLog("Value %s entered for upload_file"%str(uploadFile))
                executeFileCommand = component["execute_file_command"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_execute_file_command_linux_postinstall')]")), action="SET_TEXT", setValue=executeFileCommand)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("executeFileCommand"))),action="SET_TEXT",setValue=executeFileCommand)
                utility.execLog("Value %s entered for execute_file_command"%str(executeFileCommand))
                yumProxy = component["yum_proxy"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_yum_proxy_linux_postinstall')]")), action="SET_TEXT", setValue=yumProxy)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("yumProxy"))),action="SET_TEXT",setValue=yumProxy)
                utility.execLog("Value %s entered for yum_proxy"%str(yumProxy))
                time.sleep(1)
            elif "windowspostinstall" in applicationType.lower():
                windowspostinstallshare = component["windows_postinstall_share"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_share_windows_postinstall')]")), action="SET_TEXT", setValue=windowspostinstallshare)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("share"))),action="SET_TEXT",setValue=windowspostinstallshare)
                utility.execLog("Value %s entered for upload_file"%str(windowspostinstallshare))
                windowsinstallCommand = component["windows_install_command"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_install_command_windows_postinstall')]")), action="SET_TEXT", setValue=windowsinstallCommand)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("installCommand"))),action="SET_TEXT",setValue=windowsinstallCommand)
                utility.execLog("Value %s entered for execute_file_command"%str(windowsinstallCommand))
                uploadFile = component["upload_file"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_upload_file_windows_postinstall')]")), action="SET_TEXT", setValue=uploadFile)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("uploadFile"))),action="SET_TEXT",setValue=uploadFile)
                utility.execLog("Value %s entered for upload_file"%str(uploadFile))
                executeFileCommand = component["execute_file_command"]
                #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_execute_file_command_windows_postinstall')]")), action="SET_TEXT", setValue=executeFileCommand)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("executeFileCommand"))),action="SET_TEXT",setValue=executeFileCommand)
                utility.execLog("Value %s entered for execute_file_command"%str(executeFileCommand))
                time.sleep(1)
            elif "citrixxd7" in applicationType.lower():
                sourceLocation = component["citrix_source_location"]
                if sourceLocation != "":
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("sourceLocation"))),action="CLEAR")
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("sourceLocation"))),action="SET_TEXT",setValue=sourceLocation)
                    utility.execLog("Value %s entered for upload_file"%str(sourceLocation))
                time.sleep(1)
            utility.execLog("Clicking Finish button on Add Applications page")  
            #self.handleEvent(EC.element_to_be_clickable((By.ID,"page_addapplications_applicationsettings")), action= "CLICK")
            #time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardFinish"))),action="CLICK")
            #self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
            utility.execLog("Clicked Finish button on Add Applications page")
            time.sleep(2)
            utility.execLog("Submitting the Application form")
            #self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("btnConfirm"))),action="CLICK")
            time.sleep(10)
            try: 
                errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.UsersObjects("clarityError"))), action="GET_TEXT")
                time.sleep(3)
                return self.browserObject, False, "Unable to Add Application Component"
            except:
                return self.browserObject, True, "Unable to Add Application Component"
            else:
                raise "Unable to Add Application Component(s) :: Error -> %s"%errorMessage
        except Exception as e:
            utility.execLog("Unable to Add Application Component(s) :: Error -> %s"%(str(e) + format_exc()))
            raise e
        
    def addNetworkResourceForScaleup(self, components):
        """
        scaleup service by adding network
        """
        try:
            utility.execLog("")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("addComponent"))), action="CLICK")
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("scaleUpNetwork"))), action="CLICK")
            #self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("dropdownToggle"))),action="SELECT",selectBy="VISIBLE_TEXT",setValue="Network")
            time.sleep(5)
            i = 0
            for item in components["select_network"]:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("ddlAvailableNetworks"))), action="SELECT", setValue=item,selectBy="VISIBLE_TEXT")
                utility.execLog("Selected Value %s for network dropdown on add network resource page"%str(item))
                time.sleep(1)
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("btn_addavailablenetwork"))), action="CLICK")
                utility.execLog("Added the item selected")
                time.sleep(2)
                utility.execLog("Selecting port group")
                try:
                    try:
                        self.handleEvent(EC.element_to_be_clickable((By.classname, "form-control ddlPortGroup")), action="SELECT", setValue=components["port_group"])
                        utility.execLog("Selected %s value from the Port Group dropdown"%str(components["port_group"]))
                    except:
                        utility.execLog("port Group %s is not available to select in dropdown"%str(components["port_group"]))
                        self.handleEvent(EC.element_to_be_clickable((By.classname, "form-control ddlPortGroup")), action="SELECT", setValue="New Port Group")
                        time.sleep(1)
                        utility.execLog("Selected 'New Port Group' value from the dropdown")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'txtPortGroup')]")), action="SET_TEXT", setValue="NewPortGroup")
                        utility.execLog("Entered new port group value to 'NewPortGroup' ")
                except:
                    utility.execLog("Flow is not VDS flow so Port Group is not available")        
                    utility.execLog("Selecting resource for the added netwrok %s"%str(item))
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "(//div[@class='dropdown resourcedropdown']/button)[%s]"%str(i+1))), action="CLICK")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("selectResource"))),action="CLICK")
                    utility.execLog("Select resource button clicked")
                    time.sleep(1)
                    if "all" in components["select_resource"][i].lower():
                        utility.execLog("Adding both host and VM to the added network")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, +self.TemplatesObjects("selctHost")+"[%s]"%str(i+1))), action="CLICK")
                        utility.execLog("Host resource selected")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("selcetVM")+"[%s]"%str(i+1))), action="CLICK")
                        utility.execLog("VM resource selected")
                    elif "host" in components["select_resource"][i].lower():
                        utility.execLog("Adding host to the added network")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, +self.TemplatesObjects("selctHost")+"[%s]"%str(i+1))), action="CLICK")
                        utility.execLog("Host resource selected")
                    elif "vm" in components["select_resource"][i].lower():
                        utility.execLog("Adding VM to the added network")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("selcetVM")+"[%s]"%str(i+1))), action="CLICK")
                        utility.execLog("VM resource selected")
                    else:
                        utility.execLog("select_resource value entered in json does not match any option")
                    i = i+1
            utility.execLog("Save the added networks")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("saveNetworkButton"))), action="CLICK")
        except Exception as e:
            utility.execLog("Error generated while adding network component(s) to scaleup service :: Error -> %s"%(str(), str(e)))
            
    def scaleupService(self, serviceName, components, resourceInfo, networks, verifyNTP=False,staticIP=False,manualServer=False):
        """
        Scaleup service by adding different resources
        """
        try:
            #################################################
            #Add Storage resource
            storage = components["Scaleup"]["Storage"]
            instances = int(storage["Instances"])
            if instances > 0 :
                storageName = ""
                storageType = str(storage["Type"])
                storageIP = str(storage["IPAddress"])                
                storageSizes = storage["Size"]
                authType = storage["Auth_Type"]
                if authType.lower() == "chap":
                    authType = "CHAP"
                else:
                    authType = "IQN/IP"

                chapUser = storage["CHAP_User"]
                chapPassword = storage["CHAP_Password"]
                osName = storage["Operating_SystemName_Compellent"]
                portType = storage["PortType_Compellent"]
                if storageIP:
                    storageName = [resource["Resource Name"] for resource in resourceInfo if resource["IP Address"] == storageIP]
                else:
                    storageName = [resource["Resource Name"] for resource in resourceInfo if resource["Manufacturer/Model"] == storageType]

                if len(storageName) > 0:
                    storageName = storageName[0]

                if storageName == "":
                    raise "Unable to find Storage Resource"

                iqnIP = str(storage["Initiator_IQN_IP_Addresses"])
                aggregateName = str(storage["Aggregate_Name"])
                snapshotsSpace = str(storage["Snapshot_Space"])
                nfsIP = str(storage["NFS_IP"])
                currentTime = datetime.now().strftime('%y%m%d%H%M%S')
                utility.execLog("Date time :%s"%currentTime)
                trimDatetime = currentTime[6:]
                volumeName = ""
                poolNameVNX = ""
                try:
                    poolNameVNX = str(storage["PoolName_VNX"])
                except:
                    poolNameVNX = "Pool 0"
                typeVNX = ""
                try:
                    typeVNX = str(storage["Type_VNX"])
                except:
                    typeVNX = "Non Thin"
                configureSANSwitchVNX = ""
                try:
                    configureSANSwitchVNX = str(storage["ConfigureSANSwitch_VNX"])
                except:
                    configureSANSwitchVNX = "true"

                for index in xrange(instances):
                    if len(storageSizes) == instances:
                        storageSize = str(storageSizes[index])
                    else:
                        storageSize = str(storageSizes[0])       
                    try:
                        volumeName = storage["Volume_Name"] + str(index)
                    except:
                        volumeName = "HCLSclUp" + trimDatetime
                    utility.execLog("ScaleUP Volume name :%s"%volumeName)             
                    self.addStorageComponent(storageType, storageName, volumeName, storageSize, authType, chapUser, chapPassword, iqnIP, osName,
                            portType, aggregateName, snapshotsSpace, nfsIP, poolNameVNX=poolNameVNX, typeVNX=typeVNX,
                                                                                                        configureSANSwitchVNX=configureSANSwitchVNX)
                    
                utility.execLog("waiting for storage to get added.")
                counter = 60
                while counter > 0:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addComponent"))),action="IS_DISPLAYED"):
                        break
                    else:
                        counter = counter - 1
                        time.sleep(120)
                        continue
                
            ##################################################
            #Add Server resource
            utility.execLog("Adding server component for service %s scaleup"%str(serviceName))
            server = components["Scaleup"]["Server"]
            
            instances = int(server["Instances"])
            if instances > 0:
                status, result=self.addServerComponent(server, networks, verifyNTP,staticIP=staticIP,manualServer=manualServer)
                if verifyNTP:
                    return self.browserObject,status,result
                utility.execLog("waiting for Server to get added.")
                counter = 80
                while counter > 0:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addComponent"))),action="IS_DISPLAYED"):
                        break
                    else:
                        counter = counter - 1
                        time.sleep(120)
                        continue
                
            ##################################################
            #Add Cluster resource
            cluster = components["Scaleup"]["Cluster"]
            instances = int(cluster["Instances"])            
            if instances > 0 :
                targetVMM = ""
                clusterType = str(cluster["Type"])
                if clusterType == "VMWare":
                    vmmType = "vCenter"
                else:
                    vmmType = "HyperV"
                clusterIP = str(cluster["IPAddress"])                
                if clusterIP:
                    targetVMM = [resource["Asset/Service Tag"] for resource in resourceInfo if resource["IP Address"] == clusterIP]
                else:
                    targetVMM = [resource["Asset/Service Tag"] for resource in resourceInfo if resource["Resource Name"] == vmmType]
                if len(targetVMM) > 0:
                    targetVMM = targetVMM[0]
                if targetVMM == "":
                    raise "Unable to find VM Manager"                 
                self.addClusterComponent(cluster, targetVMM)
                utility.execLog("waiting for Cluster to get added.")
                counter = 50
                while counter > 0:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addComponent"))),action="IS_DISPLAYED"):
                        break
                    else:
                        counter = counter - 1
                        time.sleep(120)
                        continue

            ##################################################
            #Add VM resource
            vm = components["Scaleup"]["VM"]
            instances = int(vm["Instances"])
            if instances > 0:
                status, result= self.addVMComponent(vm, verifyNTP,networkInfo=networks, staticIP=staticIP,manualServer=manualServer)
                if verifyNTP:
                    return self.browserObject,status,result
                utility.execLog("waiting for VM to get added.")
                counter = 50
                while counter > 0:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addComponent"))),action="IS_DISPLAYED"):
                        break
                    else:
                        counter = counter - 1
                        time.sleep(120)
                        continue
                
            ##################################################
            #Add Application resource
            application = components["Scaleup"]["Application"]
            instances = int(application["Instances"])
            if instances > 0:
                self.addApplicationComponent(application)
                utility.execLog("waiting for application to get added.")
                counter = 50
                while counter > 0:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addComponent"))),action="IS_DISPLAYED"):
                        break
                    else:
                        counter = counter - 1
                        time.sleep(120)
                        continue
            
            ##################################################
            #Add Network resource
            scaleupnetwork = components["Scaleup"]["Network"]
            instances = int(scaleupnetwork["Instances"])
            if instances > 0:
                self.addNetworkResourceForScaleup(scaleupnetwork)
                utility.execLog("waiting for scaleupnetwork to get added.")
                counter = 30
                while counter > 0:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addComponent"))),action="IS_DISPLAYED"):
                        break
                    else:
                        counter = counter - 1
                        time.sleep(120)
                        continue
                        
            ################################################## 
            return self.browserObject, True, "Service scaleup Successful:: '%s'"%(serviceName)  
        except Exception as e:
            return self.browserObject, False, "Exception generated while scaling up the service. Error-> %s"%str(e)
          
    def scaleDownService(self, serviceName, components):
        """
        Scaleup service by adding different resources
        """
        try:
            utility.execLog("Fetching instance information of different resources")
            storage = components["Scaledown"]["Storage"]
            storageInstances = int(storage["Instances"])
            
            server = components["Scaledown"]["Server"]
            serverInstances = int(server["Instances"])
            
            cluster = components["Scaledown"]["Cluster"]
            clusterInstances = int(cluster["Instances"])
            
            vm = components["Scaledown"]["VM"]
            vmInstances = int(vm["Instances"])
            
            if storageInstances or serverInstances or clusterInstances or vmInstances > 0:
                utility.execLog("Clicking on Delete Resource link")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("deleteComponent"))), action="CLICK")
                time.sleep(2)
        
            ###############################################
            #Removing Storage resource
            utility.execLog("Removing storage")
            utility.execLog("Number of instances => %s"%str(storageInstances))
            if storageInstances > 0 :
                storageIPs = storage["storgae_ip"]
                volumeNames = storage["volume_name"]   
                for index in xrange(storageInstances):
                    if len(storageIPs) == storageInstances:
                        storageIP = str(storageIPs[index])
                    else:
                        storageIP = str(storageIPs[0]) 
                    if len(volumeNames) == storageInstances:
                        volumeName = str(volumeNames[index])
                    else:
                        volumeName = str(volumeNames[0])  
                    self.removeStorage(storageIP, volumeName)
                
            ##################################################
            # Removing Server resource
            utility.execLog("Removing Server")
            utility.execLog("Number of instances => %s"%str(serverInstances))
            if serverInstances > 0 :
                serverIPs = server["server_ip"]
                for index in xrange(serverInstances):
                    if len(serverIPs) == serverInstances:
                        serverIP = str(serverIPs[index])
                    else:
                        serverIP = str(serverIPs[0]) 
                    self.removeServer(serverIP)
                 
            ##################################################
            # Removing Cluster resource
            utility.execLog("Removing Cluster")
            utility.execLog("Number of instances => %s"%str(clusterInstances))
            if clusterInstances > 0 :
                clusterIPs = cluster["cluster_ip"]
                for index in xrange(clusterInstances):
                    if len(clusterIPs) == clusterInstances:
                        clusterIP = str(clusterIPs[index])
                    else:
                        clusterIP = str(clusterIPs[0]) 
                    self.removeCluster(clusterIP)             
            ##################################################
            # Removing VM resource
            utility.execLog("Removing VM")
            utility.execLog("Number of instances => %s"%str(vmInstances))
            if vmInstances > 0 :
                vmHostNames = vm["vm_hostName"]
                for index in xrange(vmInstances):
                    if len(vmHostNames) == vmInstances:
                        vmHostName = str(vmHostNames[index])
                    else:
                        vmHostName = str(vmHostNames[0]) 
                    self.removeVM(vmHostName) 
            
            ##################################################        
            utility.execLog("Click the Delete button to remove the resources")
            if storageInstances or serverInstances or clusterInstances or vmInstances > 0:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("submitFormDeleteResources"))), action="CLICK")
                time.sleep(3)
                utility.execLog("waiting for scaling down of resources")
                counter = 40
                while counter > 0:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("deleteComponent"))),action="IS_DISPLAYED"):
                        break
                    else:
                        counter = counter - 1
                        time.sleep(120)
                        continue
                
            ##################################################
            # Remove Application resource
            utility.execLog("Scaling down application")
            application = components["Scaledown"]["Application"]
            instances = int(application["Instances"])
            if instances > 0:
                applicationTypes = application["Type"]
                self.removeApplication(applicationTypes)
                utility.execLog("waiting for application to get removed.")
                counter = 10
                while counter > 0:
                    if self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("deleteComponent"))),action="IS_DISPLAYED"):
                        break
                    else:
                        counter = counter - 1
                        time.sleep(120)
                        continue
            ##################################################
            
            return self.browserObject, True, "Service scale down Successful:: '%s'"%(serviceName)  
        except Exception as e:
            return self.browserObject, False, "Exception generated while scaling down the service. Error-> %s"%str(e)
        
    def removeStorage(self, storageIP, volumeName):
        '''
        Many number of tables so having different body
        '''
        try:
            utility.execLog("Fetching storage table rows.")
            #tableRows = self.browserObject.find_elements_by_xpath("//*[@id='page_delete_resources']//table[@id='storageTable']/tbody/tr")
            tableRows = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("storageTable"))))
            utility.execLog("Number of rows in storage table => %s"%str(len(tableRows)))
            for index in range(len(tableRows)):
                if index!=0 and index%2!=0:
                    continue
                
                xpathIP="//*[@id='page_delete_resources']//table[@id='storageTable']/tbody[%s]/tr[%s]/td[4]"%(index+1,index+1)
                xpathVolume="//*[@id='page_delete_resources']//table[@id='storageTable']/tbody[%s]/tr[%s]/td[1]"%(index+1,index+2)
                curentStorageIP=self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpathIP)),action="GET_TEXT")
                curentVolumeName=self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpathVolume)),action="GET_TEXT")
                utility.execLog("curentStorageIP => %s"%str(curentStorageIP))
                if (storageIP==curentStorageIP) and  (volumeName in curentVolumeName):
                    utility.execLog("cluster to scale down IPaddress matched")
                    xpath="//*[@id='page_delete_resources']//table[@id='storageTable']/tbody[%s]/tr[%s]/td[1]/input"%(index+1,index+1)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="CLICK")
                    utility.execLog("Clicked on desired storage checkbox and existing loop")
                    time.sleep(1)
                    break  
            utility.execLog("Storage IP %s or volume name %s provided are not available"%(str(storageIP), str(volumeName))) 
        except Exception as e:
            utility.execLog("Exception generated while scaling down the storage. Error-> %s"%str(e))
            raise e
        
    def removeServer(self, serverIP):
        try:
            utility.execLog("Fetching server table rows.")
            tableRows = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("serverTable"))))
            utility.execLog("Number of rows in server table => %s"%str(len(tableRows)))
            for index in range(len(tableRows)):
                xpath=self.TemplatesObjects("serverTable")+"[%s+1]/td[5]"%index
                currentServerIP=self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="GET_TEXT")
                utility.execLog("currentServerIP => %s"%str(currentServerIP))
                
                if serverIP==currentServerIP:
                    utility.execLog("Server to scale down IPaddress matched")
                    xpath=self.TemplatesObjects("serverTable")+"[%s+1]/td[1]/input"%index
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="CLICK")
                    utility.execLog("Clicked on desired server checkbox and existing loop")
                    time.sleep(1)
                    break  
        except Exception as e:
            utility.execLog("Exception generated while scaling down the Server. Error-> %s"%str(e))
            raise e
        
    def removeCluster(self, clusterIP):
        try:
            utility.execLog("Fetching cluster table rows.")
            tableRows = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("clusterTable"))))
            #tableRows = self.browserObject.find_elements_by_xpath("//*[@id='page_delete_resources']//table[@id='clusterTable']/tbody/tr")
            #utility.execLog("Number of rows in cluster table => %s"%str(len(tableRows)))
            for index in range(len(tableRows)):
                xpath=self.TemplatesObjects("clusterTable")+"[%s+1]/td[6]"%index
                currentClusterIP=self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="GET_TEXT")
                utility.execLog("currentClusterIP= %s"%currentClusterIP)
                utility.execLog("clusterIP to Delete => %s"%str(clusterIP))
                if clusterIP==currentClusterIP:
                    utility.execLog("cluster to scale down IPaddress matched")
                    xpath=self.TemplatesObjects("clusterTable")+"[%s+1]/td[1]/input"%index
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="CLICK")
                    utility.execLog("Clicked on desired cluster checkbox and existing loop")
                    time.sleep(1)
                    break  
        except Exception as e:
            utility.execLog("Exception generated while scaling down the cluster. Error-> %s"%str(e))
            raise e
        
    def removeVM(self, vmHostName):
        try:
            utility.execLog("Fetching VM table rows.")
            tableRows = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("vmTable"))))
            utility.execLog("Number of rows in VM table => %s"%str(len(tableRows)))
            for index in range(len(tableRows)):
                xpath=self.TemplatesObjects("vmTable")+"[%s+1]/td[4]"%index
                currentVM=self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="GET_TEXT")
                utility.execLog("currentVM => %s"%str(currentVM))
                if vmHostName ==currentVM:
                    utility.execLog("VM to scale down vmHostNames matched")
                    xpath=self.TemplatesObjects("vmTable")+"[%s+1]/td[1]/input"%index
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="CLICK")
                    utility.execLog("Clicked on desired VM checkbox and existing loop")
                    time.sleep(1)
                    break  
        except Exception as e:
            utility.execLog("Exception generated while scaling down the VM. Error-> %s"%str(e))
            raise e
        
    def removeApplication(self, application):
        try:
            utility.execLog("Clicking on application icon")
            #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[@id='svgComponents']/*[@class='component application']/*[@id='background']")), action="CLICK")
            self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("appDegrade"))),action="CLICK")
            time.sleep(2)
            #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(text(),'Stop Managing Applications')]")), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(text(),'Stop Managing Applications')]")), action="CLICK")
            time.sleep(2)
            utility.execLog("Selecting the applications to remove")
            for item in application:
                xpath="//*[contains(text(),'%s')]/parent::tr/td[1]/input"%str(item)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)), action="CLICK")
                time.sleep(1)
            utility.execLog("Clicking the Stop Managing button")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("stopManagingAPP"))), action="CLICK")
            time.sleep(2)
        except Exception as e:
            utility.execLog("Exception generated while scaling down the application. Error-> %s"%str(e))
            raise e
			
        
    def verifyAllLink(self):
        """
        Verify Template page link in Tile View .
        """
        
        try:
            if self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("exportAll"))),action= "IS_ENABLED"):
                utility.execLog("Expoprt Link is verified successfully")
                
            else:
                utility.execLog("Expoprt All Link  not  verified successfully")
                return self.browserObject, False, "ExpoprtAllLink not  verified successfully"
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("addTemplate"))),action="CLICK")
            time.sleep(5)
            temp_header=self.browserObject.title
            utility.execLog(temp_header)
            if("Add a Template" in temp_header):
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardCancel"))),action="CLICK")
                time.sleep(5)
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("submitConfirmForm"))),action="CLICK")
                utility.execLog("Create Template link verified successfully")
            else:
                utility.execLog("Create Template link not  verified successfully")
                return self.browserObject, False, "Create Template link not  verified successfully"
            time.sleep(5)
            
            return self.browserObject, True, "All link verified successfully"
            
        except Exception as e:
            return self.browserObject, False,"Exception while trying to verifying All link '%s'"%e     
        
    def deployService_pastTime(self, templateName, serviceName, serviceDescription="", noofDeployments=1, managePermissions=False, 
                      repositoryName=None, userList=["All"], manageFirmware=False, deploymentScheduleTime=None, 
                       usersOnly=False, passExpectation=False, deployNow=True):
        """
        Description:
            API to Deploy Service
        """
        try:
            self.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            self.browserObject.find_element_by_id("deployLink").click()
            time.sleep(10)
            utility.execLog("Identifying Deploy Service Dialog")
            deployDialog = self.browserObject.find_element_by_class_name("deploywizard")
            if "Deploy Service" not in deployDialog.text:
                return self.browserObject, False, "Failed to verify Deploy Service Page :: Expected:'%s' and Actual:%s"%("Deploy Service", deployDialog.text)
            utility.execLog("Verified Deploy Service Page :: Expected:'%s' and Actual:%s"%("Deploy Service", deployDialog.text))
            time.sleep(3)
            deployPage = deployDialog.find_element_by_tag_name("section")
            utility.execLog("Clicking on 'Service Information' Tab")
            navigationTabs = deployPage.find_elements_by_xpath("./nav/ol/li")
            time.sleep(2)
            navigationTabs[0].find_element_by_xpath("./a").click()
            time.sleep(2)
            utility.execLog("Clicked on 'Service Information' Tab")
            utility.execLog("Selecting Template '%s'"%templateName)
            Select(deployPage.find_element_by_id("selectedtemplate")).select_by_visible_text(templateName)
            utility.execLog("Selected Template '%s'"%templateName)
            utility.execLog("Entering Service Name '%s'"%serviceName)
            deployPage.find_element_by_id("servicename").send_keys(serviceName)
            utility.execLog("Entered Service Name '%s'"%serviceName)
            utility.execLog("Entering Service Description '%s'"%serviceDescription)
            deployPage.find_element_by_id("servicedescription").send_keys(serviceDescription)
            utility.execLog("Entered Service Description '%s'"%serviceDescription)
            utility.execLog("Entering No of Deployments '%s'"%str(noofDeployments))
            deployPage.find_element_by_id("numberofdeployments").clear()
            deployPage.find_element_by_id("numberofdeployments").send_keys(noofDeployments)
            utility.execLog("Entered No of Deployments '%s'"%str(noofDeployments))
            #Select/Deselect Manage Firmware Option
            if manageFirmware:
                utility.execLog("Select 'Manage Firmware' Option")
                try:
                    deployPage.find_element_by_id("managefirmware").click()
                    time.sleep(2)
                    utility.execLog("Selected 'Manage Firmware' Option")
                    if repositoryName:
                        utility.execLog("Selecting Repository '%s'"%repositoryName)
                        Select(deployPage.find_element_by_id("firmwarepackage")).select_by_visible_text(repositoryName)
                        utility.execLog("Selected Repository '%s'"%repositoryName)
                    else:
                        return self.browserObject, False, "Repository Name not provided for Managing Firmware"
                except Exception as e:
                    return self.browserObject, False, "'Manage Firmware' Option not available"
            #Manage Permissions
            if managePermissions:
                utility.execLog("Select 'Manage Permissions' Option")
                deployPage.find_element_by_id("managePermissions").click()
                time.sleep(2)
                utility.execLog("Selected 'Manage Permissions' Option")     
                if usersOnly:                    
                    utility.execLog("Selecting 'Specific User' Option")
                    deployPage.find_element_by_id("specificUsers").click()
                    time.sleep(2)
                    utility.execLog("Selected 'Specific User' Option")
                    utility.execLog("Selecting 'Add Users' option")
                    deployPage.find_element_by_id("new_user_link").click()
                    time.sleep(2)
                    utility.execLog("Selected 'Add Users' option")
                    addUser = self.browserObject.find_element_by_id("page_assign_user")
                    addUser.click()
                    time.sleep(2)
                    if "Add User" not in self.browserObject.title:
                        return self.browserObject, False, "Failed to verify page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title)
                    utility.execLog("Verified page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title))
                    utility.execLog("Identifying Users Table 'users_table'")
                    tableBody = addUser.find_element_by_id("users_table").find_element_by_tag_name("tbody")
                    tableRows = tableBody.find_elements_by_tag_name("tr")
                    utility.execLog("Identified 'users_table' body and rows")
                    userList = []
                    for row in tableRows:
                        tableCols = row.find_elements_by_tag_name("td")
                        time.sleep(2)
                        userList.append({"Name":str(tableCols[1].text), "Role":str(tableCols[2].text)})
                    self.browserObject.find_element_by_id("page_assign_user").find_element_by_id("cancel_user_form").click()
                    time.sleep(5)
                    deployPage.find_element_by_id("btnWizard-Cancel").click()
                    time.sleep(5)
                    self.browserObject.find_element_by_id("confirm_modal_form").find_element_by_id("submit_confirm_form").click()
                    time.sleep(5)
                    return self.browserObject, True, userList
                else:           
                    if 'All' in userList:
                        utility.execLog("Selecting Users '%s' to provide Template Access"%str(userList))
                        deployPage.find_element_by_id("allStandardUsers").click()
                        time.sleep(2)                    
                        utility.execLog("Selected Users '%s' for Template Access"%str(userList))
                    else:
                        utility.execLog("Selecting Users '%s' to provide Template Access"%str(userList))
                        utility.execLog("Selecting 'Specific User' Option")
                        deployPage.find_element_by_id("specificUsers").click()
                        time.sleep(2)
                        utility.execLog("Selected 'Specific User' Option")
                        utility.execLog("Selecting 'Add Users' option")
                        deployPage.find_element_by_id("new_user_link").click()
                        time.sleep(2)
                        utility.execLog("Selected 'Add Users' option")
                        addUser = self.browserObject.find_element_by_id("page_assign_user")
                        addUser.click()
                        time.sleep(2)
                        if "Add User" not in self.browserObject.title:
                            return self.browserObject, False, "Failed to verify page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title)
                        utility.execLog("Verified page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title))
                        utility.execLog("Identifying Users Table 'users_table'")
                        tableBody = addUser.find_element_by_id("users_table").find_element_by_tag_name("tbody")
                        tableRows = tableBody.find_elements_by_tag_name("tr")
                        utility.execLog("Identified 'users_table' body and rows")
                        for user in userList:
                            selected = False
                            for row in tableRows:
                                tableCols = row.find_elements_by_tag_name("td")
                                time.sleep(2)
                                uName = str(tableCols[1].text)
                                if user == uName:
                                    #userID = "chkUser_" + str(tableRows.index(row))
                                    utility.execLog("Added User '%s'"%user)
                                    tableCols[0].click()
                                    selected = True
                            if not selected and not passExpectation:
                                self.browserObject.find_element_by_id("page_assign_user").find_element_by_id("cancel_user_form").click()
                                time.sleep(5)
                                deployPage.find_element_by_id("btnWizard-Cancel").click()
                                time.sleep(5)
                                self.browserObject.find_element_by_id("confirm_modal_form").find_element_by_id("submit_confirm_form").click()
                                time.sleep(5)
                                return self.browserObject, False, "Failed to Select User '%s'"%user
                        if passExpectation:
                            utility.execLog("Clicking on 'Cancel' in Add Users")
                            self.browserObject.find_element_by_id("page_assign_user").find_element_by_id("cancel_user_form").click()
                            time.sleep(5)
                            utility.execLog("Clicked on 'Cancel' in Add Users")
                        else:
                            utility.execLog("Clicking on 'Add' to save Added Users")
                            addUser.find_element_by_id("submit_user_form").click()
                            time.sleep(2)
                            utility.execLog("Selected 'Add Users' option")
            utility.execLog("Moving to 'Deployment Settings' Tab")
            deployPage.find_element_by_id("btnWizard-Next").click()
            time.sleep(5)
            deployPage.find_element_by_id("btnViewAllSettings")
            utility.execLog("Moved to 'Deployment Settings' Tab")
            utility.execLog("Moving to 'Schedule Deployment' Tab")
            deployPage.find_element_by_id("btnWizard-Next").click()
            time.sleep(5)
            deployPage.find_element_by_id("deploynow")
            utility.execLog("Moved to 'Schedule Deployment' Tab")
            if deployNow:
                utility.execLog("Selecting 'Deploy Now' Option")                
                deployPage.find_element_by_id("deploynow").click()
                time.sleep(10)
                utility.execLog("Selected 'Deploy Now' Option")
            else:
                utility.execLog("Selecting 'Schedule Later' Option")              
                deployPage.find_element_by_id("schedule").click()
                time.sleep(10)
                utility.execLog("Selected 'Schedule Later' Option")
                utility.execLog("Open Date Table & Select Past/Current Date")
                dateStr = datetime.now().strftime('%I:%M %p')
                sel=Select(self.browserObject.find_element_by_id("scheduleMeridiem"))
                selected_option = sel.first_selected_option
                optionText = selected_option.text
                if not optionText  in dateStr:
                    sel.select_by_visible_text('PM') 
                               
                strTimeMins = dateStr.split(':')[1][:2]
                self.intTimeMins = int(strTimeMins) + deploymentScheduleTime
                    
                strTimeHr = dateStr.split(':')[0]
                self.intTimeHr = int(strTimeHr)
                
                if self.intTimeMins > 59:
                    self.intTimeMins = self.intTimeMins%60
                    self.intTimeHr = self.intTimeHr+1
                    
                if self.intTimeMins < 16:
                    strIntTimeMins = "0"+str(self.intTimeMins - 7)
                    utility.execLog("Selected mins %s"%strIntTimeMins)
                else:
                    strIntTimeMins = str(self.intTimeMins - 7)
                    utility.execLog("Selected mins %s"%strIntTimeMins)
                utility.execLog("Selected mins %s"%str(strIntTimeMins))
                time.sleep(3)
                selMin =Select(self.browserObject.find_element_by_id("scheduleMinute"))
                selMin.select_by_visible_text(strIntTimeMins)
                
                if self.intTimeHr < 10:
                    strIntTimeHr = "0"+str(self.intTimeHr)
                else:
                    strIntTimeHr = str(self.intTimeHr)
                utility.execLog("Selected Hour %s"%str(strIntTimeHr))    
                selHr =Select(self.browserObject.find_element_by_id("scheduleHour"))
                selHr.select_by_visible_text(strIntTimeHr)
            
            utility.execLog("Clicking on 'Finish' Option")
            deployPage.find_element_by_id("btnWizard-Finish").click()
            time.sleep(15)
            utility.execLog("Clicked on 'Finish' Option")
            utility.execLog("Clicking on 'Confirm Dialog' Option")
            self.browserObject.find_element_by_id("confirm_modal_form").find_element_by_id("submit_confirm_form").click()
            time.sleep(15)
            eleError = self.browserObject.find_element_by_xpath("//div[contains(@class,'clarity-error')]")
            errorMessage = eleError.find_element_by_tag_name("h3").text
            time.sleep(5)
            
            if(errorMessage=="The schedule date is already past."):
                time.sleep(5)
                utility.execLog("Closing error message wondow")
                deployPage.find_element_by_id("btnWizard-Cancel").click()
                time.sleep(3)
                self.browserObject.find_element_by_id("confirm_modal_form").find_element_by_id("submit_confirm_form").click()
                utility.execLog("Closed...")
                time.sleep(2)
                utility.execLog("Test case passed successfully")
                
                return self.browserObject, True, "Test case passed successfully"
            else:
                return self.browserObject, False, "Test case not passed"
            
            
        except Exception as e:
            return self.browserObject, False, "Unable to Initiate Deployment :: Error -> %s"%str(e)
        
    
    def createNewTemplate(self, templateName):
        """
        Create new template
        """
        try:
            utility.execLog("Clicking on 'My Templates' Tab")            
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects('tabMyTemplates'))), action= 'CLICK')
            time.sleep(2)
#            self.browserObject.find_element_by_id("viewtemplatelist").click()
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('viewTemplateList'))), action='CLICK')
            time.sleep(1)
            utility.execLog("Moved to 'My Templates' Tab")
            utility.execLog("Clicking create new template button.")
            utility.execLog("Selecting option 'New'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('createNewTemplate'))), action="CLICK")
            time.sleep(2)
            utility.execLog('Entering Template Name')
            self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('templateNameBox'))), action='SET_TEXT', setValue=templateName)
            time.sleep(5)
            
            utility.execLog("Clicking Next Button")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('btnWizardNext'))), action='CLICK')
            if "Create Template" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify page title in 'Create Template' Dialog :: Actual :'%s', Expected:'Create Template'"%(self.browserObject.title)
            utility.execLog("Verified page title in 'Create Template' Dialog :: Actual :'%s', Expected:'Create Template'"%(self.browserObject.title))
            
            #Selecting Template Category
            utility.execLog("Selecting Template Category 'Automation'")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('templateCategoryNew'))), action="SELECT", setValue="Automation")
                utility.execLog("Selected Template Category 'Automation'")
            except:
                utility.execLog("Template Category 'Automation' is not available so Creating Category 'Automation'")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("templateCategoryNew"))), action="SELECT", setValue="Create New Category")
                utility.execLog("Entering Template Category Name 'Automation'")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("createCategory"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("createCategory"))), action="SET_TEXT", setValue="Automation")
            #Saving Template
            utility.execLog("Clicking 'Save' to Save Template Information")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_form_createtemplate")), action="CLICK")
            time.sleep(20)
            return self.browserObject, True, "Draft template created successfully."
        except Exception as e:
            return self.browserObject, False, "::Error -> %s"%str(e)
        
#    def addServer(self, serverType, flowType="", storageType="", osImage="esxi-5.1", adminPass="dell1234", ntpServer="172.20.0.8", iscsiInitiator= "Hardware Initiator", fcoe= False, serverPool=""):
#        """
#        """
#        try:
#            self.browserObject.find_element_by_class_name("addserver-image").click()
#            utility.execLog("Clicked add server button")
#            time.sleep(5)
#            utility.execLog("Selecting the server type %s"%str(serverType))
#            Select(self.browserObject.find_element_by_id("ddlComponents")).select_by_visible_text(serverType)
#            time.sleep(2)
#            utility.execLog("Selecting the storage component.")
#            self.browserObject.find_element_by_id("chkComponent_0").click()
#            time.sleep(1)
#            self.browserObject.find_element_by_id("btnContinueToSettings").click()
#            time.sleep(5)
#            
#            utility.execLog("Performing OS Settings.")
#            self.browserObject.find_element_by_xpath("(//span[contains(text(),'OS Settings')])[2]").click()
#            utility.execLog("OS Settings tab expanded.")
#            time.sleep(5)
#            self.browserObject.find_element_by_xpath("//label[contains(text(),'Auto-generate Host Name')]//parent::div//parent::li/div[2]/input").click()
#            utility.execLog("Selecting OS Image %s"%str(osImage))
#            Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'OS Image')]//parent::div//parent::li/div[2]/select")).select_by_visible_text(osImage)
#            utility.execLog("Entering admin Password %s "%str(adminPass))
#            self.browserObject.find_element_by_xpath("//label[contains(text(),'Administrator password')]//parent::div//parent::li/div[2]/div/input").send_keys(adminPass)
#            self.browserObject.find_element_by_xpath("//label[contains(text(),'Confirm administrator password')]//parent::div//parent::li/div[2]/div/input").send_keys(adminPass)
#            utility.execLog("Entering NTP Server %s"%str(ntpServer))
#            self.browserObject.find_element_by_xpath("//label[contains(text(),'NTP Server')]//parent::div//parent::li/div[2]/input").send_keys(ntpServer)
#            if not fcoe:
#                utility.execLog("Selecting iSCSI Initiator value %s"%str(iscsiInitiator))
#                Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'Select iSCSI Initiator')]//parent::div//parent::li/div[2]/select")).select_by_visible_text(iscsiInitiator)
#            time.sleep(1)
#            if storageType=="EqualLogic":
#                utility.execLog("Selecting Install EqualLogic MEM value")
#                Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'Install EqualLogic MEM')]//parent::div//parent::li/div[2]/select")).select_by_visible_text("False")
#                time.sleep(1)
#            
#            if serverPool != "":
#                utility.execLog("Performing Hardware settings")
#                self.browserObject.find_element_by_xpath("//span[contains(text(),'Hardware Settings')]").click()
#                utility.execLog("Hardware Settings tab expanded.")
#                time.sleep(2)
#                Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'Server Pool')]//parent::div//parent::li/div[2]/select")).select_by_visible_text(serverPool)
#                time.sleep(1)
#                utility.execLog("Selected server pool %s"%str(serverPool))
#                
#            utility.execLog("Perform Network settings")
#            self.browserObject.find_element_by_xpath("//span[contains(text(),'Network Settings')]").click()
#            utility.execLog("Network Settings tab expanded.")
#            time.sleep(2)
#            if flowType=="diverge":
#                utility.execLog("Performing Network settings for flow type %s"%str(flowType))
#                utility.execLog("Adding 2 interfaces")
#                self.browserObject.find_element_by_id("btnAddInterface").click()
#                time.sleep(2)
#                self.browserObject.find_element_by_id("btnAddInterface").click()
#                time.sleep(1)
#                utility.execLog("Enabling partitioning for Interface 1.")
#                self.browserObject.find_element_by_xpath("(.//*[@id='partitioning']/div/label/input)[1]").click()
#                time.sleep(2)
#                utility.execLog("Select networks for Interface_1 , Port_1")
#                networks = self.browserObject.find_elements_by_xpath("//span[contains(text(),'Interface 1')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button")
#                selNetworks=["autoPXE", "autoHypervisorManagement", "autoHypervisorMigration", "autoPublicLAN"]
#                i =1
#                for elem in networks:
#                    utility.execLog("Selecting the network %s for port %s of Interface 1"%(str(selNetworks[i-1]), str(i)))
#                    elem.click()
#                    time.sleep(1)
#                    self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[i-1]), str(i))).click()
#                    time.sleep(1)
#                    i = i+1
#                    elem.click()
#                    time.sleep(1)
#                utility.execLog("Select networks for Port 2 of Interface_1.")
#                networks = self.browserObject.find_elements_by_xpath("//span[contains(text(),'Interface 1')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button")
#                i = 6
#                j = 0
#                for elem in networks:
#                    utility.execLog("Selecting the network %s for port %s of Interface 1"%(str(selNetworks[j]), str(i-1)))
#                    elem.click()
#                    time.sleep(1)
#                    self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[j]), str(i))).click()
#                    time.sleep(1)
#                    i = i+1
#                    j = j+1
#                    elem.click()
#                    time.sleep(1)
#                
#                utility.execLog("Select networks for Port 1 of Interface_2.")    
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button)[1]").click()
#                time.sleep(1)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'autoSANISCSI')])[17]/parent::li/input").click()
#                time.sleep(1)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button)[1]").click()
#                time.sleep(1)
#                utility.execLog("Select networks for Port 2 of Interface_2.")    
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button)[1]").click()
#                time.sleep(1)
#                if storageType=="Compellent":
#                    self.browserObject.find_element_by_xpath("(//span[contains(text(),'autoSANISCSI1')])[13]/parent::li/input").click()
#                else:
#                    try:
#                        self.browserObject.find_element_by_xpath("(//span[contains(text(),'autoSANISCSI')])[13]/parent::li/input").click()
#                    except:
#                        self.browserObject.find_element_by_xpath("(//span[contains(text(),'autoSANISCSI')])[26]/parent::li/input").click()
#                time.sleep(1)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button)[1]").click()
#                time.sleep(1)
#            elif flowType=="converge":
#                utility.execLog("Performing Network settings for flow type %s"%str(flowType))
#                utility.execLog("Adding interface")
#                self.browserObject.find_element_by_id("btnAddInterface").click()
#                time.sleep(2)
#                utility.execLog("Enabling partitioning for Interface 1.")
#                self.browserObject.find_element_by_xpath("(.//*[@id='partitioning']/div/label/input)[1]").click()
#                time.sleep(2)
#                utility.execLog("Select networks for Interface_1 , Port_1")
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 1')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button)[1]").click()
#                time.sleep(1)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'autoPXE')])[1]/parent::li/input").click()
#                time.sleep(1)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 1')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button)[1]").click()
#                time.sleep(1)
#                networks = self.browserObject.find_elements_by_xpath("//span[contains(text(),'Interface 1')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button")
#                if fcoe:
#                    selNetworks=["autoHypervisorManagement", "autoHypervisorMigration", "autoFCoE1", "autoPublicLAN"]
#                else:
#                    selNetworks=["autoHypervisorManagement", "autoHypervisorMigration", "autoSANISCSI", "autoPublicLAN"]
#                i =1
#                for elem in networks:
#                    utility.execLog("Selecting the network %s for port %s of Interface 1"%(str(selNetworks[i-1]), str(i)))
#                    elem.click()
#                    time.sleep(1)
#                    if selNetworks[i-1]=="autoSANISCSI":
#                        self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[i-1]), str(i+2))).click()
#                    else:
#                        self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[i-1]), str(i))).click()
#                    time.sleep(1)
#                    i = i+1
#                    elem.click()
#                    time.sleep(1)
#                
#                utility.execLog("Select networks for Port 2 of Interface_1.")
#                utility.execLog("Select networks for Interface_1 , Port_1")
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 1')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button)[1]").click()
#                time.sleep(1)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'autoPXE')]/parent::li/input)[5]").click()
#                time.sleep(1)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 1')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button)[1]").click()
#                time.sleep(1)
#                if fcoe:
#                    selNetworks=["autoHypervisorManagement", "autoHypervisorMigration", "autoFCoE2", "autoPublicLAN"] 
#                elif storageType=="Compellent":
#                    selNetworks=["autoHypervisorManagement", "autoHypervisorMigration", "autoSANISCSI1", "autoPublicLAN"]
#                else:
#                    selNetworks=["autoHypervisorMigration", "autoHypervisorManagement", "autoSANISCSI", "autoPublicLAN"]
#                networks = self.browserObject.find_elements_by_xpath("//span[contains(text(),'Interface 1')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button")
#                i = 6
#                j = 0
#                for elem in networks:
#                    utility.execLog("Selecting the network %s for port %s of Interface 1"%(str(selNetworks[j]), str(i-1)))
#                    elem.click()
#                    time.sleep(1)
#                    if selNetworks[j]=="autoSANISCSI1" or selNetworks[j]=="autoFCoE2":
#                        self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[j]), str(i-1))).click()
#                    elif selNetworks[j]=="autoSANISCSI":
#                        self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[j]), str(i+6))).click()
#                    else:
#                        self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[j]), str(i))).click()
#                    time.sleep(1)
#                    i = i+1
#                    j = j+1
#                    elem.click()
#                    time.sleep(1)
#            else:
#                return self.browserObject, False, "flow type should be either 'converge' or 'diverge' "
#            
##             utility.execLog("Selecting the network default gateway")
##             Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'Static Network Default Gateway')]/parent::div/parent::li/div[2]/select")).select_by_index(1)
##             time.sleep(1)
#            
#            utility.execLog("Submitting the server form")
#            self.browserObject.find_element_by_id("submit_confirm_form").click()
#            time.sleep(10)
#            return self.browserObject, True, "Server added successfully."
#        except Exception as e:
#            return self.browserObject, False, "Exception generated while adding server to the template::Error -> %s"%str(e)
            
            
    def addCluster(self, clusterType, dcName, clusterName):
        """
        """ 
        targetVMM="asmvc55u301"
        try:
            utility.execLog("Clicking Add Cluster button")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("addCluster"))), action="CLICK")       
            time.sleep(15)
            utility.execLog("Selecting the Cluster component %s"%str(clusterType))
            Select(self.browserObject.find_element_by_id("ddlComponents")).select_by_visible_text(clusterType)
            time.sleep(2)
            utility.execLog("Selecting the Server component.")

            try:            
                if self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects("associateAll"))), action="IS_DISPLAYED"):
                    utility.execLog("Selecting 'Associate All Resources'")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("associateAll"))), action="CLICK")
            except Exception as e:
                utility.execLog("No components to 'Associate All Resources'")
            utility.execLog("Clicking on 'Continue' in Cluster Conponent Page")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("btnContinueToSettings"))), action="CLICK")
            except:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("continueFormAdjustResources"))), action="CLICK")
            time.sleep(5)
            
            utility.execLog("Entering the Cluster Settings.")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("targetVirtualMachineManager"))), action="SELECT", setValue=targetVMM)
            #Select(self.browserObject.find_element_by_xpath(self.TemplatesObjects("targetVirtualMachineManager"))).select_by_visible_text(targetVMM)
            utility.execLog("Entering Data Center Settings.")
            
            if dcName == "":
                dcName = "autoDC"
            
            #xpath = "//label[contains(text(),'Data Center Name')]/parent::div/parent::li/div[2]/select" 
            try:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("dataCenterName"))), action="SELECT", setValue=dcName)
                time.sleep(5)
            except:
#                     try:
                utility.execLog("Datacenter Name '%s' is not available so Creating New Datacenter"%dcName)
#                     except:
#                         utility.execLog("Datacenter Name is not available so Creating New Datacenter")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("dataCenterName"))), action="SELECT", setValue="Create New Datacenter...")
                time.sleep(5)
                #xpath = "//label[contains(text(),'New datacenter name')]/parent::div/parent::li/div[2]/input"
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newDatacenterName"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newDatacenterName"))), action="SET_TEXT", setValue=dcName)
            
            if clusterName == "":
                clusterName = "autoCluster"
                #xpath = "//label[contains(text(),'Cluster Name')]/parent::div/parent::li/div[2]/select"
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterName"))), action="SELECT", setValue=clusterName)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterName"))), action="SET_TEXT", setValue="\t")
                    time.sleep(5)
                except:
                    try:
                        utility.execLog("Cluster Name '%s' is not available so Creating New Cluster"%clusterName)
                    except:
                        utility.execLog("Cluster Name is not available so Creating New Cluster")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("clusterName"))), action="SELECT", setValue="Create New Cluster...")
                    time.sleep(5)
                    #xpath = "//label[contains(text(),'New cluster name')]/parent::div/parent::li/div[2]/input"
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterName"))), action="CLEAR")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterName"))), action="SET_TEXT", setValue=clusterName)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newClusterName"))), action="SET_TEXT", setValue="\t")
                    time.sleep(3)
            utility.execLog("Selecting 'Distributed' Switch")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("distributed"))), action="CLICK")
            time.sleep(3)
            
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("vSphereVDSSettings"))), action="CLICK")
                    #display.click()
            time.sleep(1)
            vdsNames= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects("vdsNames")))) 
            i=1
            for elem in vdsNames:
                utility.execLog("Entering the VDS Name for %s"%str(elem))
                Select(elem).select_by_visible_text("Create VDS Name ...")
                time.sleep(1)
                #xpath = "(//label[contains(text( ),'New VDS Name')]/parent::div/parent::li/div[2]/input)[%s]"%str(i)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newVDSNames"))), action="SET_TEXT", setValue="VDSName %s"%str(i))
                i += 1
            portGroups= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects("portGroups"))))
            j=1
            for elem in portGroups:
                utility.execLog("Creating the Port Group for %s"%str(elem))
                Select(elem).select_by_visible_text("Create Port Group ...")
                time.sleep(1)
                #xpath = "(//label[contains(text( ),'New Port Group')]/parent::div/parent::li/div[2]/input)[%s]"%str(j)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("newPortGroups"))), action="SET_TEXT", setValue="Port Group %s"%str(j))
                time.sleep(1)
                j += 1
                
            try:
                
                utility.execLog("clicking on save button")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("submitConfirmForm"))), action="CLICK")
            except Exception as e:
                utility.execLog(e)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects("submitFormAdjustResources"))), action="CLICK")
            time.sleep(10)
            try:
                errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.UsersObjects("clarityError"))), action="GET_TEXT")
                time.sleep(3)
                utility.execLog("Failed to Added Cluster Component")
            except:
                utility.execLog("Successfully Added Cluster Component(s)")    
           
            
            return self.browserObject, True, "Cluster added successfully."
        except Exception as e:
            self.handleEvent(EC.presence_of_element_located((By.XPATH, "//select[@data-automation-id='asm_guid']")), action="SELECT", setValue=targetVMM)
            return self.browserObject, False, "Exception Generated while adding Cluster::Error -> %s"%str(e)
                
            
#    def addVM(self, vmType, adminPass="dell1234", ntpServer="172.20.0.8"):
#        """
#        """
#        try:
#            utility.execLog("Clicking Add VM button")
#            self.browserObject.find_element_by_class_name("addvm-image").click()
#            time.sleep(5)
#            utility.execLog("Selecting the VM Component %s"%str(vmType))
#            Select(self.browserObject.find_element_by_id("ddlComponents")).select_by_visible_text(vmType)
#            time.sleep(2)
#            utility.execLog("Selecting the Cluster.")
#            self.browserObject.find_element_by_id("chkComponent_0").click()
#            time.sleep(1)
#            self.browserObject.find_element_by_id("btnContinueToSettings").click()
#            time.sleep(8)
#            utility.execLog("Selecting the Auto-generate Host Name checkbox.")
#            self.browserObject.find_element_by_xpath("//label[contains(text( ),'Auto-generate Host Name')]/parent::div/parent::li/div[2]/input").click()
#            time.sleep(1)
#            utility.execLog("Selecting the OS Image.")
#            Select(self.browserObject.find_element_by_xpath("//label[contains(text( ),'OS Image')]/parent::div/parent::li/div[2]/select")).select_by_index(1)
#            time.sleep(2)
#            self.browserObject.find_element_by_xpath("//label[contains(text( ),'Administrator password')]/parent::div/parent::li/div[2]/div/input").send_keys(adminPass)
#            self.browserObject.find_element_by_xpath("//label[contains(text( ),'Confirm administrator password')]/parent::div/parent::li/div[2]/div/input").send_keys(adminPass)
#            self.browserObject.find_element_by_xpath("//label[contains(text( ),'NTP Server')]/parent::div/parent::li/div[2]/input").send_keys(ntpServer)
#            
#            utility.execLog("Enter Virtual Machine settings")
#            self.browserObject.find_element_by_xpath("//span[contains(text(),'Virtual Machine Settings')]").click()
#            utility.execLog("Virtual Machine Setitngs tab expanded.")
#            time.sleep(1)
#            utility.execLog("Selecting Network checkbox.")
#            self.browserObject.find_element_by_xpath("//label[contains(text( ),'Networks')]/parent::div/parent::li/div[2]//input").click()
#            Select(self.browserObject.find_element_by_xpath("//label[contains(text( ),'Static Network Default Gateway')]/parent::div/parent::li/div[2]/select")).select_by_index(1)
#            time.sleep(1)
#            utility.execLog("Submitting the VM form")
#            self.browserObject.find_element_by_id("submit_confirm_form").click()
#            time.sleep(15)
#            return self.browserObject, True, "VM added successfully."
#        except Exception as e:
#            return self.browserObject, False, "::Error -> %s"%str(e)

        
    
#    def addApplication(self, applicationType="linux_postinstall", uploadedFile="hello.sh", executeFileCmd="sh"):
#        """
#        """
#        try:
#            utility.execLog("Clicking Add Application button")
#            #self.browserObject.find_element_by_id("addApplication").click()
#            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addApplication"))),action="CLICK")
#            time.sleep(5)
#            
#            utility.execLog("Selecting the Server component.")
#            #self.browserObject.find_element_by_class_name("//*[contains(text(),'Server')]/parent::tr//input").click()
#            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("selectServer"))),action="CLICK")
#            time.sleep(1)
#            #self.browserObject.find_element_by_id("btnWizard-Next").click()
#            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
#            time.sleep(3)
#            utility.execLog("Add Application %s from select dropdopwn"%str(applicationType))
#            #Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'Add Application')]/parent::div/select")).select_by_visible_text(applicationType)
#            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("selectApplication"))),action="SELECT",setValue=applicationType)
#            time.sleep(1)
#            utility.execLog("Clicking Add button")
#            #self.browserObject.find_element_by_id("identifyapplications_add").click()
#            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("addClick"))),action="CLICK")
#            time.sleep(3)
#            utility.execLog("Clicking Next button")
#            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
#            time.sleep(3)
#            utility.execLog("Adding the uploaded file %s and execute command %s"%(str(uploadedFile), str(executeFileCmd)))
#            #self.browserObject.find_element_by_xpath("//label[contains(text( ),'Upload File')]/parent::div/parent::li/div[2]//input").send_keys(uploadedFile)
#            #self.browserObject.find_element_by_xpath("//label[contains(text( ),'Execute File Command')]/parent::div/parent::li/div[2]//input").send_keys(executeFileCmd)
#            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("uploadFile"))),action="SET_TEXT",setValue=uploadedFile)
#            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("executeFileCommand"))),action="SET_TEXT",setValue=executeFileCmd)
#            time.sleep(1)
#            #self.browserObject.find_element_by_id("btnWizard-Finish").click()
#            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardFinish"))),action="CLICK")
#            time.sleep(2)
#            utility.execLog("Submitting the VM form")
#            #self.browserObject.find_element_by_id("submit_confirm_form").click()
#            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("btnConfirm"))),action="CLICK")
#            time.sleep(15)
#            return self.browserObject, True, "Application added successfully."
#        except Exception as e:
#            return self.browserObject, False, "Exception generated while adding application ::Error -> %s"%str(e)
        
        
    def publishTemplate(self, templateName):
        """
        Click Publish Template and navigate back to tempates page
        """
        try:
            utility.execLog("Clicking 'Publish Template' to Publish Template")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('btnPublish'))), action="CLICK")
            time.sleep(7)
            utility.execLog("Identifying Confirm Dialog box")
            self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.TemplatesObjects('confirmDailogBox'))), action="CLICK")
            time.sleep(5)
            utility.execLog("Confirming to Publish Template")
            self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('confirmButton'))), action="CLICK")
            time.sleep(10)
            try:
                eleError = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxError'))))
                errorMessage = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.CommonObjects('RedBoxErrorMessages'))), action= "GET_TEXT")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('cancelConfirmForm'))), action="CLICK")
                time.sleep(3)
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                try:
                    time.sleep(5)
                    utility.execLog("Identifying Confirm Dialog box and Confirming to Draft Template")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('popupModal'))), action="CLICK")
                except:
                    pass     
                return False, "Failed to Create Template :: Error -> '%s'"%(str(errorMessage))
                                                    
            except:
                #Move to Templates Page
                utility.execLog("Navigating back to Templates Page")
                self.handleEvent(EC.element_to_be_clickable((By.LINK_TEXT, "Templates")), action="CLICK")
                return True, "Able to publish the template"
                time.sleep(3)
        except Exception as e:
            return False, "Exception generated while publishing template ::Error -> %s"%str(e)
            
            
#    def addServerFC(self, serverType, flowType="", osImage="esxi-5.1", adminPass="dell1234", ntpServer="172.20.0.8", iscsiInitiator= "Hardware Initiator"):
#        """
#        """
#        try:
#            self.browserObject.find_element_by_class_name("addserver-image").click()
#            utility.execLog("Clicked add server button")
#            time.sleep(5)
#            utility.execLog("Selecting the server type %s"%str(serverType))
#            Select(self.browserObject.find_element_by_id("ddlComponents")).select_by_visible_text(serverType)
#            time.sleep(2)
#            utility.execLog("Selecting the storage component.")
#            self.browserObject.find_element_by_id("chkComponent_0").click()
#            time.sleep(1)
#            self.browserObject.find_element_by_id("btnContinueToSettings").click()
#            time.sleep(5)
#            
#            utility.execLog("Performing OS Settings.")
#            self.browserObject.find_element_by_xpath("(//span[contains(text(),'OS Settings')])[2]").click()
#            utility.execLog("OS Settings tab expanded.")
#            time.sleep(5)
#            self.browserObject.find_element_by_xpath("//label[contains(text(),'Auto-generate Host Name')]//parent::div//parent::li/div[2]/input").click()
#            utility.execLog("Selecting OS Image %s"%str(osImage))
#            Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'OS Image')]//parent::div//parent::li/div[2]/select")).select_by_visible_text(osImage)
#            utility.execLog("Entering admin Password %s "%str(adminPass))
#            self.browserObject.find_element_by_xpath("//label[contains(text(),'Administrator password')]//parent::div//parent::li/div[2]/div/input").send_keys(adminPass)
#            self.browserObject.find_element_by_xpath("//label[contains(text(),'Confirm administrator password')]//parent::div//parent::li/div[2]/div/input").send_keys(adminPass)
#            utility.execLog("Entering NTP Server %s"%str(ntpServer))
#            self.browserObject.find_element_by_xpath("//label[contains(text(),'NTP Server')]//parent::div//parent::li/div[2]/input").send_keys(ntpServer)
#            
##             utility.execLog("Selecting iSCSI Initiator value %s"%str(iscsiInitiator))
##             Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'Select iSCSI Initiator')]//parent::div//parent::li/div[2]/select")).select_by_visible_text(iscsiInitiator)
#            time.sleep(1)
#            
#            utility.execLog("Perform Hardware settings")
#            self.browserObject.find_element_by_xpath("//span[contains(text(),'Hardware Settings')]").click()
#            select = Select(self.browserObject.find_element_by_xpath("//select[contains(@id,'setting_server_pool_asm')]"))
#            select.select_by_visible_text("serTest")
#            time.sleep(5)
#            
#            
#            utility.execLog("Perform Network settings")
#            self.browserObject.find_element_by_xpath("//span[contains(text(),'Network Settings')]").click()
#            utility.execLog("Network tab expanded.")
#            time.sleep(2)
#            utility.execLog("Adding 2 interfaces")
#            self.browserObject.find_element_by_id("btnAddInterface").click()
#            time.sleep(5)
#            self.browserObject.find_element_by_id("btnAddInterface").click()
#            time.sleep(5)
#            
#            self.browserObject.find_element_by_xpath("(.//*[@id='fabricTypeInputs']/div[2]/label/input)[1]").click()
#            time.sleep(3)
#            
#            utility.execLog("Enabling partitioning for Interface 2.")
#            self.browserObject.find_element_by_xpath("(.//*[@id='partitioning']/div/label/input)[2]").click()
#            time.sleep(5)
#            
#            utility.execLog("Select networks for Interface_2 , Port_1")
#            networks = self.browserObject.find_elements_by_xpath("//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button")
#            selNetworks=["autoPXE", "autoHypervisorManagement", "autoHypervisorMigration", "autoPublicLAN"]
#            k =9
#            i=1
#            for elem in networks:
#                utility.execLog("Selecting the network %s for port %s of Interface 2"%(str(selNetworks), str(i)))
#                elem.click()
#                time.sleep(2)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[i-1]), str(k))).click()
#                time.sleep(3)
#                i = i+1
#                k=k+1
#                elem.click()
#                time.sleep(3)
#            utility.execLog("Select networks for Port 2 of Interface_2.")
#            networks = self.browserObject.find_elements_by_xpath("//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button")
##             selNetworks=["autoPXE", "autoHypervisorManagement", "autoHypervisorMigration", "autoPublicLAN"]
#            i = 14
#            j = 0
#            for elem in networks:
#                utility.execLog("Selecting the network %s for port %s of Interface 2"%(str(selNetworks), str(i-1)))
#                elem.click()
#                time.sleep(1)
#                self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')])[%s]/parent::li/input"%(str(selNetworks[j]), str(i))).click()
#                time.sleep(1)
#                i = i+1
#                j = j+1
#                elem.click()
#                time.sleep(1)
#            
#            utility.execLog("Selected networks for Port 1 of Interface_2.")    
##             self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button)[1]").click()
##             time.sleep(1)
##             self.browserObject.find_element_by_xpath("(//span[contains(text(),'autoSANISCSI')])[9]/parent::li/input").click()
##             time.sleep(1)
##             self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[1]/table/tbody/tr/td[2]/div/button)[1]").click()
##             time.sleep(1)
##             utility.execLog("Select networks for Port 2 of Interface_2.")    
##             self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button)[1]").click()
##             time.sleep(1)
##             self.browserObject.find_element_by_xpath("(//span[contains(text(),'autoSANISCSI')])[14]/parent::li/input").click()
##             time.sleep(1)
##             self.browserObject.find_element_by_xpath("(//span[contains(text(),'Interface 2')]/parent::span/parent::div/parent::div//section[1]/section[2]/table/tbody/tr/td[2]/div/button)[1]").click()
##             time.sleep(1)
#            
#            utility.execLog("Selecting the network default gateway")
#            Select(self.browserObject.find_element_by_xpath("//label[contains(text(),'Static Network Default Gateway')]/parent::div/parent::li/div[2]/select")).select_by_index(1)
#            time.sleep(1)
#            
#            utility.execLog("Submitting the server form")
#            self.browserObject.find_element_by_id("submit_confirm_form").click()
#            time.sleep(5)
#            return self.browserObject, True, "Server added successfully."
#        except Exception as e:
#            return self.browserObject, False, "Exception generated while adding server to the template::Error -> %s"%str(e)
        
    def exportTemplate(self, templateName):
        """
        Export the service to a file
        """
        try:
            time.sleep(5)
            globalVars.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            
            utility.execLog("Clicking on Export to File")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("exportTemplate"))),action="CLICK")
            time.sleep(5)
            utility.execLog("Clicked on Export to File")
            currentTime = datetime.now().strftime('%y%m%d%H%M%S')
            fName="test"+currentTime
            time.sleep(5)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("fileName"))),action="KEY",setValue=fName)
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("useEncPwdFromBackup"))),action="CLICK")
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("encryptionPassword"))),action="KEY",setValue="admin")
            time.sleep(1)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("verifyEncryptPassword"))),action="KEY",setValue="admin")
            time.sleep(1)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("export"))),action="CLICK")
            utility.execLog("Successfully clicked on export file")
            time.sleep(10)
            utility.execLog("Verifying the downloaded export file.")
            
            file_path = os.path.abspath("downloads\\"+fName+".gpg")
            utility.execLog(file_path)
            if(file_path !=""):
                utility.execLog("Successfully downloaded export file.")
                return self.browserObject, True, "File successfully downloaded for file %s"%str(fName)
            else:
                return self.browserObject, False, "File not successfully downloaded for file %s"%str(fName) 
            
        except Exception as e:
            utility.execLog("Exception while trying to download the for export file :: Error -> %s"%str(e))
            raise e
        
        
        
    def importTemplate(self, templateName):
        """
        Export the service to a file
        """
        try:
            utility.execLog("importTemplate()...Templates")
            globalVars.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            
            utility.execLog("Clicking on edit template")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("editTemplateLink"))),action="CLICK")
            time.sleep(5)
            utility.execLog("Clicking on to import template")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("detailImportTemplate"))),action="CLICK")
            time.sleep(5)
            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("selectTemplate"))),action="SELECT",selectBy= "VISIBLE_TEXT",setValue=templateName)
            time.sleep(5)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("importButton"))),action="CLICK")
            time.sleep(5)
            actual_templateName=self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("templateAfterImoprt"))),action="GET_TEXT")
            utility.execLog("actual_templateName=%s and templateName=%s"%(actual_templateName,templateName))
            if actual_templateName == templateName :
                utility.execLog("Template successfully imported")
                return self.browserObject, True, "Template successfully imported"
            else:
                return self.browserObject, False, "Template not imported"
        except Exception as e:
            utility.execLog("Exception while trying to import template :: Error -> %s"%str(e))
            raise e
        
    def creatTemplate_specificStandarUser(self, templateName,userList=["standard2"], publishTemplate=True):
        """
        Export the service to a file
        """
        try:
            utility.execLog("creatTemplate_specificStandarUser()...Templates")
            time.sleep(5)
            globalVars.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            utility.execLog("Clicking on edit template..")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("editTemplateLink"))),action="CLICK")
            time.sleep(5)
            utility.execLog("Clicked on edit template..")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("btnEditTemplateName"))),action="CLICK")
            time.sleep(10)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("addStandardUser"))),action="CLICK")
            time.sleep(5)
            utility.execLog("Selected 'Specific User' Option")
            utility.execLog("Selecting 'Add Users' option")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addNewUsers"))),action="CLICK")
            time.sleep(5)
            utility.execLog("Selected 'Add Users' option")
            if "Add User" not in self.browserObject.title:
                return self.browserObject, False, "Failed to verify page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title)
            utility.execLog("Verified page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title))
            utility.execLog("Identifying Users Table 'users_table'")
            tableBody = self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("usersTable"))))
            tableRows = tableBody.find_elements_by_tag_name("tr")
            utility.execLog("Identified 'users_table' body and rows")
            for user in userList:
                utility.execLog("1st for")
                for row in tableRows:
                    utility.execLog("2nd for")
                    tableCols = row.find_elements_by_tag_name("td")
                    time.sleep(5)
                    uName = str(tableCols[1].text)
                    if user == uName:
                        tableCols[0].find_element_by_xpath(".//input").click()
                        time.sleep(5)
                        self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("usersFormSave"))),action="CLICK")
                        time.sleep(5)
                        utility.execLog("Selected 'Add Users' option")
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardSave"))),action="CLICK")
                        utility.execLog("Successfully Saved Template Information")
                        time.sleep(10)
                        utility.execLog("Template successfully given permission to specific standard user")
                        break
                    
            
            if publishTemplate:
                utility.execLog("Clicking 'Publish Template' to Publish Template")
                try:
                    time.sleep(5)
                    self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("btnPublish"))),action="CLICK")
                    time.sleep(5)
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("btnPublish"))),action="CLICK")
                    time.sleep(5)
                        
                utility.execLog("Clicked on 'Publish Template' to Publish Template")
                utility.execLog("Identifying Confirm Dialog box")
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("submitConfirmForm"))),action="CLICK")
                time.sleep(5)
                return self.browserObject, True, "Template successfully given permission to specific standard user and publish"
            else:
                return self.browserObject, False, "Template not published"
            
        except Exception as e:
            utility.execLog("Exception while trying to import template :: Error -> %s"%str(e))
            return self.browserObject, False, "Template not imported"
        
#    def Deploy_ServerTemplate(self, templateName, serverName,opt=None):
#          
#        """
#        Description:
#           It adds server to the created template
#           
#        """    
#        time.sleep(5)
#        self.selectTemplate(templateName)
#        time.sleep(5)
#        utility.execLog("Successfully selected Beofore edit link")
#        self.browserObject.find_element_by_id("editTemplateLink").click()
#        utility.execLog("After edit link")
#        time.sleep(20)
#        self.browserObject.find_element_by_id("addServer").click()
#        time.sleep(10)
#        select = Select(self.browserObject.find_element_by_id("ddlComponents"))
#        select.select_by_index(1)
#        time.sleep(10)
#        self.browserObject.find_element_by_id("btnContinueToSettings").click()
#        time.sleep(10)
#        select = Select(self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='server_pool']"))
#        try:
#            select.select_by_visible_text(serverName)
#            if opt == "server_check":
#                return "false"
#        except:
#            utility.execLog("unable to select server:"+serverName)
#            if opt == "server_check":
#                utility.execLog("Verified that the Admin is unable to deploy service from a template with server pool he does not have access to.")
#                self.browserObject.find_element_by_id("cancel_confirm_form").click()
#                time.sleep(2)
#                return "true"
#        time.sleep(5)
#        utility.execLog("expanding OS setting section")
#        self.browserObject.find_element_by_xpath(".//*[@id='formComponent']//div//div[4]//div[@class = 'page-header']//a//i[1]").click()
#        time.sleep(5)
#        self.browserObject.find_element_by_xpath(".//*[@id='templateSimpleDetails']//*[@class = 'form-group']//*[@data-automation-id = 'generate_host_name']").click()
#        time.sleep(5)
#        select = Select(self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='razor_image']"))
#        select.select_by_visible_text("red7")
#        time.sleep(5)
#        #         self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='admin_password']").send_keys("dell1234")
#        self.browserObject.find_element_by_xpath(".//input[contains(@id,'setting_admin_password_asm')]").send_keys("Dell1234")
#        time.sleep(5)
##         self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='admin_confirm_password']").send_keys("dell1234")
#        self.browserObject.find_element_by_xpath(".//input[contains(@id,'setting_admin_confirm_password_asm')]").send_keys("Dell1234")
#        time.sleep(5)
##         self.browserObject.find_element_by_xpath(".//*[@id = 'templateSimpleDetails']//*[@class= 'form-group']//*[@class = 'form-control' and @data-automation-id='ntp_server']").send_keys("172.20.0.8")
#        
#        self.browserObject.find_element_by_xpath(".//input[contains(@id,'setting_ntp_server_asm')]").clear()
#        time.sleep(3)
#        self.browserObject.find_element_by_xpath(".//input[contains(@id,'setting_ntp_server_asm')]").send_keys("172.20.0.8")
#        time.sleep(5)
#        
#        utility.execLog("expanding Network setting section")
#        self.browserObject.find_element_by_xpath(".//*[@id='formComponent']//div//div[5]//div[@class = 'page-header']//a//i[1]").click()
#        time.sleep(5)
#        self.browserObject.find_element_by_id("btnAddInterface").click()
#        time.sleep(5)
#        utility.execLog("Selecting PXE option")
#        self.browserObject.find_element_by_xpath(".//*[@class='accordion interfaceAccordion']//section//section[1]//*[@class='table table-striped']//tr[1]//td[2]//*[@class='btn btn-default dropdown-toggle']//*[@class='caret']").click()
#        time.sleep(5)
#        options_list = self.browserObject.find_elements_by_xpath(".//*[@class='accordion interfaceAccordion']//section//section[1]//*[@class='table table-striped']//tr[1]//td[2]//*[@class='dropdown-menu']//li")
#        utility.execLog("option..")
#        utility.execLog("lenght of option %s"%str(len(options_list)))
#        for option in xrange(1, len(options_list)+1):
#            option_text = self.browserObject.find_element_by_xpath(".//*[@class='accordion interfaceAccordion']//section//section[1]//*[@class='table table-striped']//tr[1]//td[2]//*[@class='dropdown-menu']//li["+str(option)+"]").text
#            utility.execLog("option1..: %s"%str(option_text))
#            if option_text == "autoPXE":
#                self.browserObject.find_element_by_xpath(".//*[@class='accordion interfaceAccordion']//section//section[1]//*[@class='table table-striped']//tr[1]//td[2]//*[@class='dropdown-menu']//li["+str(option)+"]//input").click()
#                   
#        time.sleep(5)
#        self.browserObject.find_element_by_xpath(".//*[@class='accordion interfaceAccordion']//section//section[2]//*[@class='table table-striped']//tr[1]//td[2]//*[@class='btn btn-default dropdown-toggle']//*[@class='caret']").click()
#        time.sleep(5)
#        options_list = self.browserObject.find_elements_by_xpath(".//*[@class='accordion interfaceAccordion']//section//section[2]//*[@class='table table-striped']//tr[1]//td[2]//*[@class='dropdown-menu']//li")
#        for option in xrange(1, len(options_list)+1):
#            option_text = self.browserObject.find_element_by_xpath(".//*[@class='accordion interfaceAccordion']//section//section[2]//*[@class='table table-striped']//tr[1]//td[2]//*[@class='dropdown-menu']//li["+str(option)+"]").text
#            utility.execLog("option2..: %s"%str(option_text))
#            if option_text == "autoPXE":
#                self.browserObject.find_element_by_xpath(".//*[@class='accordion interfaceAccordion']//section//section[2]//*[@class='table table-striped']//tr[1]//td[2]//*[@class='dropdown-menu']//li["+str(option)+"]//input").click()
#        time.sleep(5)
#        utility.execLog("network added") 
#        self.browserObject.find_element_by_xpath("(//*[@id='templateSimpleDetails']/li[6]/div[1]/label)[4]").click()
#        time.sleep(5)
#        
#        selct1=Select(self.browserObject.find_element_by_xpath(".//select[contains(@id,'setting_default_gateway_asm')]"))
#        time.sleep(3)
#        selct1.select_by_visible_text("DHCP / No Gateway")
#        time.sleep(5)
#        
#        self.browserObject.find_element_by_id("submit_confirm_form").click()
#        time.sleep(20)
#         
#        self.browserObject.find_element_by_id("btnPublish").click()
#        time.sleep(10)
#        utility.execLog("network added") 
#        self.browserObject.find_element_by_id("submit_confirm_form").click()
#        time.sleep(30)
#        if opt == "No Deployment":
#            utility.execLog("Inside function")
#            return 
#        else:
#            self.browserObject.find_element_by_id("deployLink").click()
#            time.sleep(25)
#
#            odate = time.strftime("%H:%M:%S") 
#            Service_name = "ASM_GUI_Automation" + str(odate)
#            utility.execLog(Service_name)
#            time.sleep(5)
#            self.browserObject.find_element_by_id("servicename").send_keys(Service_name)
#            time.sleep(2)
#            self.browserObject.find_element_by_id("managePermissions").click()
#            time.sleep(2)
#            self.browserObject.find_element_by_id("btnWizard-Next").click()
#            time.sleep(5)
#
#            
#            self.browserObject.find_element_by_id("btnWizard-Next").click()
#            utility.execLog("Deployed a service from a template shared by the Admin")
#            time.sleep(7)
#            self.browserObject.find_element_by_id("btnWizard-Finish").click()
#            utility.execLog("click to finished button")
#        
#            #handle pop up for confirm deploy services
#            time.sleep(10)
#            self.browserObject.find_element_by_xpath(".//*[@id='submit_confirm_form']").click()
#            time.sleep(5)
#            try:
#                expected_statement = "User admin does not have access to the Server Pool."
#                error_statement = self.browserObject.find_element_by_xpath(".//*[@id='page_deploywizard_scheduledeployment']/div/h3").text
#                if error_statement == expected_statement:
#                    utility.execLog("Verified that the Admin is unable to deploy service from a template with server pool he does not have access to.")
#                    self.browserObject.find_element_by_id("btnWizard-Cancel").click()
#                    time.sleep(5)
#                    self.browserObject.find_element_by_xpath(".//*[@id='submit_confirm_form']").click()
#                    time.sleep(5)
#                    return "true"
#                else :
#                    return "false"      
#            except:
#                                                                       
#                time.sleep(20) 
#                
#            utility.execLog("Template has been created") 
#            return Service_name
    
    def check_Service_OwnedBy_Standard(self):
        """
        Description:
            Standard user can adjust any resources in a service owned by him 
        """
        utility.execLog("Wait for some minute service is deploying......")
        time.sleep(150)
        self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("service"))))
        state1= self.browserObject.find_element_by_xpath("//*[@id='serviceActions']/div[3]/div[2]/span[2]").text
        state=self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("state"))),action="GET_TEXT")
        utility.execLog("Value of State..  :%s"%str(state))
        #Healthy
        if (state=="Warning"):
            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("deleteComponent"))),action="CLICK")
            time.sleep(15)
            utility.execLog("result")
            #delete any first resource
            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("deleteFirstResource"))),action="CLICK")
            time.sleep(10)
            self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("submitFormDeleteResources"))),action="CLICK")
            time.sleep(150)
            state= self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("state"))),action="GET_TEXT")
            #Healthy
            if state=="Warning": 
                utility.execLog("Deleted Successfully")
                return "true"
            else:
                utility.execLog("Not deleted....")
                return "false"       
        else:
  
            return "false"
    
    def deployClonedTemplate(self,templateName):
        """
        Description:
            Deploy cloned template
        """
        try:
            time.sleep(5)
            utility.execLog("deployClonedTemplate() .. Templates")
            currentTime = datetime.now().strftime('%y%m%d%H%M%S')  
            self.templateName1 = "HCLTemplate" + currentTime
            self.serviceName = "Test" + currentTime
            self.browserObject, status, result=self.selectTemplate(templateName)
            if not  status:
                return self.browserObject , status,result
                
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("cloneTemplateBtn"))),action="CLICK")
            time.sleep(5)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("templateNameClone"))),action="KEY",setValue=self.templateName1)
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("templateCategoryClone"))),action="SELECT",selectBy="VISIBLE_TEXT",setValue="Test")
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("managePermissionClone"))),action="CLICK")
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("selectAllStandardUsersClone"))),action="CLICK")
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
            time.sleep(5)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardFinish"))),action="CLICK")
            time.sleep(10)
            try:
                self.browserObject.find_element_by_xpath("//*[@id='popupModal']/div/div/div[3]/button").click()
                time.sleep(5)
            except:
                utility.execLog("Successfully finished template")
          
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("btnPublish"))),action="CLICK")
            time.sleep(5)
  
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("submitConfirmForm"))),action="CLICK")
            time.sleep(20)
            
            utility.execLog("Successfully published template")
    
            deployList=self.handleEvent(EC.presence_of_all_elements_located((By.ID,self.TemplatesObjects("detailDeployService"))))
            for deploy in deployList:
                if deploy.is_displayed():
                    deploy.click()
                    
            time.sleep(15)
           
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("serviceName"))),action="KEY",setValue=self.serviceName)
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("specificUser"))),action="CLICK")
            time.sleep(5)
            #self.browserObject.find_element_by_id("btnWizard-Next").click()
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
            time.sleep(5)
            try:
                Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("autoVolume")
                time.sleep(5)
            except:
                try:
                    Select(self.browserObject.find_element_by_xpath("//*[contains(text(),'Storage Volume Name')]/parent::div/parent::li/div[2]/select")).select_by_visible_text("Create New Volume...")
                    time.sleep(3)
                    self.browserObject.find_element_by_xpath("//*[contains(text(),'New Volume Name')]/parent::div/parent::li/div[2]/input").send_keys("autoVolume")
                    time.sleep(3)
                except:
                    utility.execLog("Its is a 831 build so need of volume name.")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
            time.sleep(5)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardFinish"))),action="CLICK")
            time.sleep(5)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("submitConfirmForm"))),action="CLICK")
            time.sleep(20)
            status_text=self.browserObject.find_element_by_xpath("//*[@id='serviceActions']/ul[2]/li[2]/span[2]").text
            if(status_text=="In Progress"):
                utility.execLog("Cloned template published and deployed successfully")
                time.sleep(3)
                return self.browserObject, True, "Cloned template published and deployed successfully"
            
        except Exception as e:
            return self.browserObject, False, "Cloned template not deployed successfully '%s'"%str(e) 
        
        
    def getCreateTemplateLink(self, templateName):
        """
        Description:
            check create Template link is not available for default
        """
        try:
            utility.execLog("getCreateTemplateLink() .. Templates")
            time.sleep(10)
            self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('tabSampleTemplates'))), action='CLICK')
            time.sleep(5)
            try:
                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('addTemplate'))), action='CLICK')
                utility.execLog("Create template link is present")
                return self.browserObject, False, "Create Template Link is present under default category"
            except:
                utility.execLog("Create Template Link is not present under Sample Templates category")
                return self.browserObject, True, "Create Template Link is not present under default category" 
        except Exception as e:
            return self.browserObject, False, "Error :: %s"%e     
        
    def addAttachment(self,templateName):
        """
        Description:
            Add attachment option on template allows user to upload documents. The max size is 50MB
        """
        try:
            time.sleep(10)
            utility.execLog("addAttachment() .. Templates")
            self.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject, False, result
            self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("editTemplateLink"))),action="CLICK")
            utility.execLog("clicked to edit Templates")
            time.sleep(10)
            self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("btnAddAttachments"))),action="CLICK")
            utility.execLog("Successfully clicked to addAttachment")
            time.sleep(5)
            self.handleEvent(EC.presence_of_element_located((By.CSS_SELECTOR,'input[type="file"]')),action="CLEAR")
            file_path = os.path.abspath("docs\\Testcase_NGI-TC-3347_testFile.jpg")
            utility.execLog("File path:")
            utility.execLog(file_path)
            new_file_path = file_path.replace("\\", "\\\\")
            utility.execLog(new_file_path)
            self.handleEvent(EC.presence_of_element_located((By.CSS_SELECTOR,'input[type="file"]')),action="KEY",setValue=new_file_path)
            time.sleep(5)
            
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardSave"))),action="CLICK")
            utility.execLog("Successfully clicked to addAttachment")
            time.sleep(5)
            self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects("btnAddAttachments"))),action="CLICK")
            utility.execLog("Successfully clicked to addAttachment")
            time.sleep(5)
            self.handleEvent(EC.presence_of_element_located((By.CSS_SELECTOR,'input[type="file"]')),action="CLEAR")
            file_path1 = os.path.abspath("docs\\Testcase_NGI-TC-3347_testFile.mp4")
            utility.execLog("File path1:")
            utility.execLog(file_path1)
            new_file_path1 = file_path1.replace("\\", "\\\\")
            utility.execLog(new_file_path1)
            self.handleEvent(EC.presence_of_element_located((By.CSS_SELECTOR,'input[type="file"]')),action="KEY",setValue=new_file_path1)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardSave"))),action="CLICK")
            
            try:
                self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("tooBigAttachement"))))
                utility.execLog("Sorry you cannot exceed 50 MB.")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("cancelAttachement"))),action="CLICK")
            except Exception as e:
                utility.execLog("Error...")
                return self.browserObject, False, "You can upload greater then 50 MB size"
            
            uploaded_list=self.handleEvent(EC.presence_of_all_elements_located((By.ID,self.TemplatesObjects("btnAttachment"))))
            uploaded_val=len(uploaded_list)
            utility.execLog("Uploaded file list:")
            utility.execLog(uploaded_val)
            if(uploaded_val==1):
                utility.execLog("Successfully uploaded Attachment")
                return self.browserObject, True, "Successfully uploaded Attachment"
            else:
                utility.execLog("Not uploaded Attachment")
                return self.browserObject, False, "Successfully not uploaded Attachment"
            
        except Exception as e:
            return self.browserObject, False, "Attachment not uploaded successfully '%s'"%str(e)    
        
    def changeApplicationInstallOrder(self, components, resourceInfo, networks):
        """
         Description:
             API to change Application order:- 
             input: Template Name
         """
        templateName=""
        utility.execLog('Selecting template :%s'%templateName)
        self.browserObject, status, result = self.selectTemplate(templateName)
        if not status:
            return self.browserObject,False,result
        self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("editTemplateLink"))),action="CLICK")
        try:
            utility.execLog("Click on Add Application Component")  
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addApplication"))),action="CLICK")       
                time.sleep(5)   
            except:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@class,'btn btn-default dropdown-toggle')]")), action="CLICK")
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "lnkAdjustResources_Application")), action="CLICK")
                time.sleep(5)
            try:            
                if self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("selectServerVM"))), action="IS_DISPLAYED"):
                    utility.execLog("Selecting Available Resources")
                    try:
                        elements=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("selectServerVM"))))
                        for i in xrange(0,len(elements)):
                            elements[i].click()
                    except:
                        raise "Unable to Add Application Component(s)"
                else:
                    raise "Unable to Add Application Component(s):- Please add Server or VM first"
            except NoSuchElementException:
                utility.execLog("No components to 'Associate Resources'")
            utility.execLog("Clicking on 'Continue' in Application Component Page")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
            time.sleep(5)
            utility.execLog("Selecting the application type linuxpostinstall")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("selectApplication"))), action="SELECT", setValue="linux_postinstall")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("addClick"))),action="CLICK")
            utility.execLog("Added the application linuxpostinstall")
            time.sleep(2)
            utility.execLog("Selecting the application type citrixxd7")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("selectApplication"))), action="SELECT", setValue="citrix_xd7")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("addClick"))),action="CLICK")
            utility.execLog("Added the application citrixxd7")
            time.sleep(2)
            utility.execLog("Selecting the application type windowspostinstall")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("selectApplication"))), action="SELECT", setValue="windows_postinstall")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("addClick"))),action="CLICK")
            utility.execLog("Added the application windowspostinstall")
            time.sleep(2)
            utility.execLog("Fetching and storing original order of the applications in a list.")
            #elements = self.browserObject.find_elements_by_xpath("//table[@class='table table-striped identifyApplicationsTable table-condensed']/tbody/tr/td[3]")
            elements=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("applicationAddTable"))))
            self.orgList=[]
            for elem in elements:
                self.orgList.append(elem.text)
            utility.execLog("Original list %s"%(self.orgList))
            utility.execLog("Changing order of application.")
            #xpath1 = "//table[@class='table table-striped identifyApplicationsTable table-condensed']/tbody/tr[2]/td[2]/ul/li[1]/ul/li[1]/a/i"
            
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("changeApplicationOrderUP"))), action="CLICK")
            time.sleep(1)
            #xpath2 = "//table[@class='table table-striped identifyApplicationsTable']/tbody/tr[2]/td[2]/ul/li[1]/ul/li[2]/a/i"
            
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("changeApplicationOrderDown"))), action="CLICK")
            time.sleep(1)
            utility.execLog("Fetching and storing order of the applications in a list after it's changed.")
            elements=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("applicationAddTable"))))
            self.newList=[]
            for elem in elements:
                self.newList.append(elem.text)
            utility.execLog("list after modification %s"%(self.newList))
            for x in range(len(self.orgList)):
                utility.execLog("orgElem=> %s :: newElem=> %s"%(str(self.orgList[x]), str(self.newList[x])))
                if str(self.orgList[x]) == str(self.newList[x]):
                    return self.browserObject, False, "Order of applications could not be changed."
            utility.execLog("Delete linuxpostinstall and verify applications is no more in added list.") 
            
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects("removeApplication"))), action="CLICK")
            time.sleep(1)
            utility.execLog("Verify linuxpostinstall application is no more available in added application list")
            utility.execLog("Fetching the added applications after deletion.")
            delElements=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("applicationAddTable"))))  
            for ele in delElements:
                if str(ele.text) == "linuxpostinstall":
                    return self.browserObject, False, "Order of applications could not be changed."
            return self.browserObject, True, "Successfully changed the Order of applications and able to delete as well.."
        except Exception as e:
            return self.browserObject, False, "Unable to add applications :: Error -> %s"%(str(e) + format_exc())           
        
        
        
    def addMultipleApplicationComponent(self, component):
        """
        Add Multiple application component in the order specified in .json file
        """
        try:
            utility.execLog("Click on Add Multile Application Component")  
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("addApplication"))),action="CLICK")       
                time.sleep(5)   
            except:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@class,'btn btn-default dropdown-toggle')]")), action="CLICK")
                time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.ID, "lnkAdjustResources_Application")), action="CLICK")
                time.sleep(5)
            try:            
                if self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("selectServerVM"))), action="IS_DISPLAYED"):
                    utility.execLog("Selecting Available Resources")
                    try:
                        elements=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects("selectServerVM"))))
                        for i in xrange(0,len(elements)):
                            elements[i].click()
                    except:
                        raise "Unable to Add Application Component(s)"
                else:
                    raise "Unable to Add Application Component(s):- Please add Server or VM first"
            except NoSuchElementException:
                utility.execLog("No components to 'Associate Resources'")
            utility.execLog("Clicking on 'Continue' in Application Component Page")
            self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Next")), action="CLICK")
            time.sleep(5)
            
            applicationInstances = int(component["Instances"])
            applicationTypes = component["Type"]
            if not ( len(applicationTypes) == applicationInstances):
                utility.execLog("Application instances provided ie %s doe snto match with the number of application provided ie. %s"%(str(applicationInstances), str(len(applicationTypes))))
                raise "Application instances provided ie %s doe snto match with the number of application provided ie. %s"%(str(applicationInstances), str(len(applicationTypes)))
            else:
                for applicationIndex in xrange(applicationInstances):
                    applicationType = applicationTypes[applicationIndex]
                    utility.execLog("Selecting the application type %s"%str(applicationType.lower()))
                    try:
                        self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("selectApplication"))),action="SELECT",setValue=applicationType)
                    except Exception as e:
                        utility.execLog("Application type provided %s does not match any option"%str(applicationType))
                        raise "Unable to Add Application Component(s) :: Error -> Application type ' %s ' provided does nto match any option."%str(applicationType)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("addClick"))),action="CLICK")
                    utility.execLog("Added the application %s"%str(applicationType))
                    time.sleep(2)
                self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardNext"))),action="CLICK")
                utility.execLog("Clicked Next button on Identify Applications page")
                time.sleep(3)
                    
            for applicationIndex in xrange(applicationInstances):
                applicationType = applicationTypes[applicationIndex]
                utility.execLog("Application type is %s"%str(applicationType))       
                if applicationType.lower() == "saysomething":
                    utility.execLog("Entering values for application type saysomething")  
                    addOnString = component["addOn_modle_string"]
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("saySomething"))),action="SET_TEXT",setValue=addOnString)
                    time.sleep(1)
                elif applicationType.lower() == "adamlinuxpostinstall" or applicationType.lower() == "linux_postinstall":
                    installPackages = component["install_packages"]
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("installPackages"))),action="SET_TEXT",setValue=installPackages)
                    utility.execLog("Value %s entered for install_packages"%str(installPackages))
                    uploadShare = component["upload_share"]
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_upload_share_linux_postinstall')]")), action="SET_TEXT", setValue=uploadShare)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("uploadShare"))),action="SET_TEXT",setValue=uploadShare)
                    utility.execLog("Value %s entered for upload_share"%str(uploadShare))
                    uploadFile = component["upload_file"]
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_upload_file_linux_postinstall')]")), action="SET_TEXT", setValue=uploadFile)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("uploadFile"))),action="SET_TEXT",setValue=uploadFile)
                    utility.execLog("Value %s entered for upload_file"%str(uploadFile))
                    executeFileCommand = component["execute_file_command"]
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_execute_file_command_linux_postinstall')]")), action="SET_TEXT", setValue=executeFileCommand)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("executeFileCommand"))),action="SET_TEXT",setValue=executeFileCommand)
                    utility.execLog("Value %s entered for execute_file_command"%str(executeFileCommand))
                    yumProxy = component["yum_proxy"]
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_yum_proxy_linux_postinstall')]")), action="SET_TEXT", setValue=yumProxy)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("yumProxy"))),action="SET_TEXT",setValue=yumProxy)
                    utility.execLog("Value %s entered for yum_proxy"%str(yumProxy))
                    time.sleep(1)
                elif applicationType.lower() == "windowspostinstall":
                    windowspostinstallshare = component["windows_postinstall_share"]
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_share_windows_postinstall')]")), action="SET_TEXT", setValue=windowspostinstallshare)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("share"))),action="SET_TEXT",setValue=windowspostinstallshare)
                    utility.execLog("Value %s entered for upload_file"%str(windowspostinstallshare))
                    windowsinstallCommand = component["windows_install_command"]
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_install_command_windows_postinstall')]")), action="SET_TEXT", setValue=windowsinstallCommand)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("installCommand"))),action="SET_TEXT",setValue=windowsinstallCommand)
                    utility.execLog("Value %s entered for execute_file_command"%str(windowsinstallCommand))
                    uploadFile = component["upload_file"]
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_upload_file_windows_postinstall')]")), action="SET_TEXT", setValue=uploadFile)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("uploadFile"))),action="SET_TEXT",setValue=uploadFile)
                    utility.execLog("Value %s entered for upload_file"%str(uploadFile))
                    executeFileCommand = component["execute_file_command"]
                    #self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//*[contains(@id,'setting_execute_file_command_windows_postinstall')]")), action="SET_TEXT", setValue=executeFileCommand)
                    self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("executeFileCommand"))),action="SET_TEXT",setValue=executeFileCommand)
                    utility.execLog("Value %s entered for execute_file_command"%str(executeFileCommand))
                    time.sleep(1)
                elif applicationType.lower() == "citrixxd7":
                    sourceLocation = component["citrix_source_location"]
                    if sourceLocation != "":
                        self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("sourceLocation"))),action="CLEAR")
                        self.handleEvent(EC.presence_of_element_located((By.XPATH,self.TemplatesObjects("sourceLocation"))),action="SET_TEXT",setValue=sourceLocation)
                        utility.execLog("Value %s entered for upload_file"%str(sourceLocation))
                    time.sleep(1)
            utility.execLog("Clicking Finish button on Add Applications page")  
            #self.handleEvent(EC.element_to_be_clickable((By.ID,"page_addapplications_applicationsettings")), action= "CLICK")
            #time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.TemplatesObjects("btnWizardFinish"))),action="CLICK")
            #self.handleEvent(EC.element_to_be_clickable((By.ID, "btnWizard-Finish")), action="CLICK")
            utility.execLog("Clicked Finish button on Add Applications page")
            time.sleep(2)
            utility.execLog("Submitting the Application form")
            #self.handleEvent(EC.element_to_be_clickable((By.ID, "submit_confirm_form")), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects("btnConfirm"))),action="CLICK")
            time.sleep(10)
            try: 
                errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.UsersObjects("clarityError"))), action="GET_TEXT")
                time.sleep(3)
                return self.browserObject, False, "Unable to Add Application Component"
            except:
                return self.browserObject, True, "Unable to Add Application Component"
            else:
                raise "Unable to Add Application Component(s) :: Error -> %s"%errorMessage
        except Exception as e:
            utility.execLog("Unable to Add Application Component(s) :: Error -> %s"%(str(e) + format_exc()))
            raise e

    def color_of_device_icon(self, device_name):
        """
        Getting color of added device icon
        :return: Name of color as a string
        """
        if "Template Builder" not in self.browserObject.title:
            return self.browserObject, False, "Failed to verify page title in 'Template Builder' Dialog :: Actual :'%s', Expected:'Template Builder'" % (
            self.browserObject.title)
        utility.execLog("Getting color of '%s' icon" % device_name)
        device_name = device_name.lower()
        try:
            elem_class_value = self.handleEvent(EC.presence_of_element_located((By.CSS_SELECTOR, ".%s" % device_name)),
                                                attributeName="class", action="GET_ATTRIBUTE_VALUE")
            if "warning" in elem_class_value:
                utility.execLog("Icon is yellow", elem_class_value)
                return self.browserObject, True, "yellow"
            else:
                utility.execLog("Icon is grey", elem_class_value)
                return self.browserObject, True, "grey"
        except Exception as e:
            return self.browserObject, False, "Unable to locate element :: Error -> %s"%(str(e) + format_exc())
    def getTemplateFirmwarePackage(self, templateName):
        '''
        Select a template and get template information : cataogory, user permission, firmware used
        '''
        try:
            utility.execLog('Selecting template :%s'%templateName)
            self.browserObject, status, result = self.selectTemplate(templateName)
            if not status:
                return self.browserObject,False,result
            utility.execLog('View template Information') 
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('viewDetailsBtn'))), action='CLICK')
            utility.execLog("Opening Template in 'view' mode")
            time.sleep(10)
            utility.execLog('Get firmware/repository package information')
            firmwarePackage=self.handleEvent(EC.presence_of_element_located((By.ID,self.TemplatesObjects('templateReferenceFwRepo'))), action="GET_TEXT")
            utility.execLog('Firmware package used : %s'%firmwarePackage)
            return self.browserObject,True, firmwarePackage
        except Exception as e:
            return self.browserObject, False, "Error : %s"%e
			
    def  managePermissions(self, usersOnly=False,userList=['ALL'], passExpectation=False):
        try:
            if usersOnly:                    
                utility.execLog("Selecting 'Specific User' Option")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('specificUser'))), action='CLICK')
                time.sleep(2)
                utility.execLog("Selecting 'Add Users' option")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('addNewUsers'))), action='CLICK')
                time.sleep(5)
                if "Add User" not in self.browserObject.title:
                    return False, "Failed to verify page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title)
                utility.execLog("Verified page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title))
                utility.execLog("Identifying Users Table 'users_table'")
                totalRows =len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.TemplatesObjects('userFormTable')))))
                if totalRows == 1:
                    xpath= self.TemplatesObjects('userFormTable')+'[1]/td'
                    cols = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
                    if cols == 1:
                        totalRows = 0
                utility.execLog("Reading Existing Users")
                userList = []
                for row in range(1, totalRows+1):
                    xpath= self.TemplatesObjects('userFormTable')+"[%i]/td[2]"%row
                    name = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") 
                    xpath= self.TemplatesObjects('userFormTable')+"[%i]/td[3]"%row
                    role = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    userList.append({"Name":str(name), "Role":str(role)})
                utility.execLog("User list '%s'"%str(userList))
                utility.execLog("Exiting from Users Page")
                self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('usersFormCancel'))), action ="CLICK")
                time.sleep(5)                    
                utility.execLog("Exiting from Deploy Service Page")
                self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('btnWizardCancel'))))
                time.sleep(5)
                utility.execLog("Confirming on 'Yes' to Cancel Deploy Wizard")
                self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('submitConfirmForm'))), action= "CLICK")
                time.sleep(5)
                return True, userList
            else:   
                utility.execLog("Selecting Users '%s' to provide Template Access"%str(userList))        
                if 'All' in userList:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('allStandardUsersPermission'))), action="CLICK")
                    return True, 'Permission given to all standard users'
                    time.sleep(2)                    
                else:
                    utility.execLog("Selecting 'Specific User' Option")
                    self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('specificUser'))),action="CLICK")
                    time.sleep(2)
                    utility.execLog("Selecting 'Add Users' option")
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.TemplatesObjects('addNewUsers'))), action="CLICK")
                    time.sleep(2)
                    utility.execLog("Checking 'Users' Page")
                    time.sleep(2)
                    if "Add User" not in self.browserObject.title:
                        return False, "Failed to verify page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title)
                    utility.execLog("Verified page title in 'Add User(s)' Dialog :: Actual :'%s', Expected:'Add User(s)'"%(self.browserObject.title))
                    xpath = self.TemplatesObjects('userFormTable')
                    totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
                    if totalRows == 1:
                        xpath = self.TemplatesObjects('userFormTable')+"[1]/td"
                        cols = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
                        if cols == 1:
                            totalRows = 0
                    utility.execLog("Reading Existing Users")
                    for user in userList:
                        selected = False
                        for row in range(1, totalRows+1):
                            xpath = self.TemplatesObjects('userFormTable')+ "[%i]/td[2]"%row
                            uName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT") 
                            if user == uName:
                                xpath = self.TemplatesObjects('userFormTable')+ "[%i]/td[1]/input"%row
                                utility.execLog("Adding User '%s'"%user)
                                self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                                selected = True
                        if not selected and not passExpectation:
                            utility.execLog("Exiting from Users Page")
                            self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('usersFormCancel'))), action ="CLICK")
                            time.sleep(5)                    
                            utility.execLog("Clicking Cancel Wizard")
                            self.handleEvent(EC.presence_of_element_located((By.XPATH, self.TemplatesObjects('btnWizardCancel'))), action= "CLICK")
                            time.sleep(5)
                            utility.execLog("Confirming on 'Yes' to Cancel  Wizard")
                            self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('submitConfirmForm'))), action= "CLICK")
                            time.sleep(5)
                            return False, "Failed to Select User '%s'"%user
                    if passExpectation:
                        utility.execLog("Clicking on 'Cancel' in Add Users")
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('usersFormCancel'))), action ="CLICK")
                        time.sleep(5)
                        utility.execLog("Clicked on 'Cancel' in Add Users")
                        return True, "Clicked on 'Cancel' in Add Users"
                    else:
                        utility.execLog("Clicking on 'Add' to save Added Users")
                        self.handleEvent(EC.presence_of_element_located((By.ID, self.TemplatesObjects('usersFormSave'))), action ="CLICK")
                        time.sleep(2)
                        utility.execLog("Selected 'Add Users' option")
                        return True, "Selected 'Add Users' option"
        except Exception as e:
            return False, "Error while adding user permission ::%s"%e
            
    def getTemplateSettings(self, templateName=None):
        """
        Description:
             API to get All Settings in a Template 
        """
        optionList = {}
        try:
            self.browserObject, status, result = self.selectTemplate(templateName)
            time.sleep(5)
            utility.execLog("Clicking on 'View Details'")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.TemplatesObjects('viewDetailsBtn'))), action="CLICK")
            time.sleep(10)
            utility.execLog("CLicking on view all settings")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.TemplatesObjects('btnViewAllSettings'))), action= "CLICK")
            utility.execLog('Expanding all collapsed tabs')
            collapsedTabs= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects('collapsedTabs'))))
            for collapsedTab in collapsedTabs:
                if collapsedTab.is_displayed():
                    time.sleep(2)
                    collapsedTab.click()
            components=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.TemplatesObjects('formViewAllSettings'))))
            componentInstances= len(components)
            for index in range(1, componentInstances+1):
                componentXpath=self.TemplatesObjects('formViewAllSettings')+'[{}]/a/ul/li[2]/h3'.format(index)
                component='component{}'.format(index)+self.handleEvent(EC.presence_of_element_located((By.XPATH,componentXpath)), action="GET_TEXT")
                subcomponentXpath=self.TemplatesObjects('formViewAllSettings')+'[{}]//label'.format(index)
                subcomponentList=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,subcomponentXpath)))
                subcomponentDict={}
                for subIndex in range(0, len(subcomponentList)):
                    subcomponent=subcomponentList[subIndex]
                    subcomponentName=subcomponent.text
                    try:
                        subcomponentValue=subcomponentList[subIndex].find_element_by_xpath(".//ancestor::div[2]//p")
                        subcomponentValue= subcomponentValue.text
                    except Exception as e:
                        subcomponentValue=''
                    subcomponentDict.update({subcomponentName:subcomponentValue})
                optionList.update({component:subcomponentDict})
            return self.browserObject, True, optionList
        except Exception as e:
            return self.browserObject, False, "Error while reading template setting details ::%s"%e