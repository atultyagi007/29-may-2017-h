"""
Author: P Suman/Saikumar Kalyankrishnan/HCL Team
Created/Modified: Sep 15th 2015/Mar 8th 2017
Description: Functions/Operations related to Users Page

Revision History:
--Date--     --Name--        --TestCase--    --Changes--
04Jan16    Ankit Manglic        TC-2496      Added new function editUserInformation()
04Jan16    Raj Patel            TC-2657      Added new function editUserRole()
21Jan16    Ankit Manglic        TC-2609      Updated function editUserInformation() with current password new id.
03Feb16    Ankit Manglic        TC-3947      Added new function filterByGroup()
03Feb16    Ankit Manglic        TC-3948      Added new function createDirectory() and importADUsers()
"""

from CommonImports import *
from libs.product.objects.Common import Common
from libs.product.objects.Users import Users

class Users(Navigation, Common, Users):
    """
    Description:
        Class which includes Functions/Operations related to Users Page
    """
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of Users class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Users"
        utility.execLog("Users")
                
    def loadPage(self):
        """
        Description:
            API to Load Users Page
        """
        try:
            utility.execLog("Loading Users Page...")
            self.browserObject, status, result =  self.selectOption("Users")
            return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Users Page %s" % (str(e) + format_exc())

    def validatePageTitle(self, title=None):
        """
        Description:
            API to validate Users Page
        """
        if not title:
            title = self.pageTitle
        getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.UsersObjects('title'))), action="GET_TEXT")
        if title not in getCurrentTitle:
            utility.execLog("Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title))
            return self.browserObject, False, "Failed to validate Page Title :: Actual --> '%s' :: Expected --> '%s'" % (getCurrentTitle, title)
        else:
            utility.execLog("Successfully validated Page Title: '%s'" % title)
            return self.browserObject, True, "Successfully validated Page Title: '%s'" % title

    
    def createUser(self, currentPassword, userName, userPassword,userRole,firstName=None, lastName=None, 
                   userEmail=None, userPhone=None, enableUser=False):
        """
        Creates a New Local User        
        """
        try:
            errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, "//span[@ng-bind-html='error.message | htmlSafe']")), action="GET_TEXT")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("newUserlink"))), action="CLICK")
            time.sleep(2)            
            self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME,self.UsersObjects("pageEditUser"))), action="CLICK")
            header = self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME,self.UsersObjects("pageEditUser"))), action="GET_TEXT")
            if "Create User" not in header:
                return self.browserObject, False, "Failed to verify Create Local User Page Actual: '%s' Expected: 'Create Local User'"%header
            utility.execLog("Create Local User Page loaded")
            utility.execLog("Setting Current Password %s"%currentPassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("currentPassword"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("currentPassword"))), action="SET_TEXT", setValue=currentPassword)
            utility.execLog("Setting Username %s"%userName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userName"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userName"))), action="SET_TEXT", setValue=userName)
            utility.execLog("Setting Password %s"%userPassword)
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userPassword"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userPassword"))), action="SET_TEXT", setValue=userPassword)
            utility.execLog("Setting Confirm Password %s"%userPassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userConfirmPassword"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userConfirmPassword"))), action="SET_TEXT", setValue=userPassword)
            if firstName!=None:
                utility.execLog("Setting User Firstname %s"%firstName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userFirstName"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userFirstName"))), action="SET_TEXT", setValue=firstName)
            if lastName!=None:
                utility.execLog("Setting User Lastname %s"%lastName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userLastname"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userLastname"))), action="SET_TEXT", setValue=lastName)
            utility.execLog("Setting User Role %s"%userRole)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userRole"))), action="SELECT", setValue=userRole)
            if userEmail!=None:
                utility.execLog("Setting User Email %s"%userEmail)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEmail"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEmail"))), action="SET_TEXT", setValue=userEmail)
            if userPhone!=None:
                utility.execLog("Setting User Phone No %s"%userPhone)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userPhone"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userPhone"))), action="SET_TEXT", setValue=userPhone)
            if enableUser:
                utility.execLog("Setting Enable User to %s"%str(enableUser))
                if not self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEnabled"))), action="IS_SELECTED"):
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEnabled"))), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("submitUserForm"))), action="CLICK")
            try: 
                errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.UsersObjects("clarityError"))), action="GET_TEXT")
                time.sleep(3)
                return self.browserObject, False, "Failed to Create Local User :: '%s' with Role '%s' :: Error -> '%s'"%(userName, userRole, 
                                                    str(errorMessage))
            except:
                return self.browserObject, True, "Successfully Created Local User :: '%s' with Role :: '%s'"%(userName, userRole)
            finally:
                try:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("cancelUserForm"))), action="CLICK")
                except:
                    pass
        except Exception as e:
            return self.browserObject, False, "Failed to Create Local User :: '%s' with Role :: '%s' :: Error -> %s"%(userName, 
                                                    userRole, str(e) + format_exc())
    
    def editUser(self, userName, currentPassword, userPassword=None, firstName=None, lastName=None, 
                   userRole=None, userEmail=None, userPhone=None, enableUser=True):
        """
        Description:
            API to Edit existing User
        """
        try:
            utility.execLog("Reading Users Table")
            userSelected = False
            tableName= self.UsersObjects("usersTable")
            xpath = "//table[@id='%s']/tbody/tr"%tableName
            totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,xpath))))
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            for row in range(1, totalRows + 1):
                xpath = "//table[@id='%s']/tbody/tr[%i]/td[2]"%(tableName, row)
                uName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                utility.execLog("Able to fetch User Info '%s'"%uName)
                if userName == uName and uName != "admin":
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[1]/input"%(tableName, row)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    utility.execLog("Selected User '%s'"%uName)
                    userSelected = True
                    break
            if not userSelected:
                return self.browserObject, False, "Failed to Select User '%s' to Edit"%userName
            utility.execLog("Clicking 'Edit' for User '%s'"%userName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("editUserLink"))), action="CLICK")
            time.sleep(2)
            #self.handleEvent(EC.element_to_be_clickable((By.ID, "page_edit_user")), action="CLICK")
            editUserPageText = self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.UsersObjects("modalContent"))), action="GET_TEXT")
            #editUserPageText = self.handleEvent(EC.element_to_be_clickable((By.ID, "page_edit_user")), action="GET_TEXT")
            if "Edit User" not in editUserPageText:
                return self.browserObject, False, "Failed to verify Edit User Page"
            utility.execLog("Edit User Page loaded")
            utility.execLog("Setting Current Password %s"%currentPassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("currentPassword"))), action="CLEAR")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("currentPassword"))), action="SET_TEXT", setValue=currentPassword)
            if userPassword:
                utility.execLog("Setting Password %s"%userPassword)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userPassword"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userPassword"))), action="SET_TEXT", setValue=userPassword)
                utility.execLog("Setting Confirm Password %s"%userPassword)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userConfirmPassword"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userConfirmPassword"))), action="SET_TEXT", setValue=userPassword)
            if firstName:
                utility.execLog("Setting User Firstname %s"%firstName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userFirstName"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userFirstName"))), action="SET_TEXT", setValue=firstName)
            if lastName:
                utility.execLog("Setting User Lastname %s"%lastName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userLastname"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userLastname"))), action="SET_TEXT", setValue=lastName)
            if userRole:
                utility.execLog("Setting User Role %s"%userRole)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userRole"))), action="SELECT", setValue=userRole)
            if userEmail:
                utility.execLog("Setting User Email %s"%userEmail)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEmail"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEmail"))), action="SET_TEXT", setValue=userEmail)
            if userPhone:
                utility.execLog("Setting User Phone No %s"%userPhone)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userPhone"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userPhone"))), action="SET_TEXT", setValue=userPhone)            
            enabled = self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEnabled"))), action="IS_SELECTED")
            if enableUser:
                if not enabled:                    
                    utility.execLog("Setting Enable User to %s"%str(enableUser))
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEnabled"))), action="CLICK")
            else:
                if enabled:
                    utility.execLog("Setting Enable User to %s"%str(enableUser))
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEnabled"))), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("editUserSave"))), action="CLICK")
            try:
                elemnts=self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,self.UsersObjects("validateFormError"))))
                ErorMessage=""
                for error in elemnts:
                    err=error.text
                    ErorMessage=ErorMessage+err+","
                    print"error:-%s"%error.text
                return self.browserObject, False, "Failed to Edit Local User :: '%s'"%(ErorMessage)
            except:
                pass
            try:
                errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.UsersObjects("clarityError"))), action="GET_TEXT")
                time.sleep(3)
                return self.browserObject, False, "Failed to Edit Local User :: '%s' :: Error -> '%s'"%(userName,errorMessage)  
            except:
                return self.browserObject, True, "Successfully Edited Local User :: '%s'"%(userName)
        except Exception as e:
            return self.browserObject, False, "Unable to Edit User %s :: Error -> %s"%(userName, str(e) + format_exc())
    
    def getUsers(self):
        """
        Description:
            API to get existing Users
        """
        userList = []
        try:
            utility.execLog("Clicking on Users Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("tabUsers"))), action="CLICK")
            utility.execLog("Reading Users Table")
            tableName = self.UsersObjects("usersTable")
            xpath = "//table[@id='%s']/thead/tr[1]/th"%tableName                
            totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
            utility.execLog("Total Number of Columns : %s"%str(totalColumns))
            tableColumns = []
            for col in range(2, totalColumns + 1):
                xpath = "//table[@id='%s']/thead/tr[1]/th[%i]"%(tableName, col)                    
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch %s Table Columns '%s'"%(tableName, str(tableColumns)))
            xpath = "//table[@id='%s']/tbody/tr"%tableName                
            totalRows = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH,xpath))))
            utility.execLog("Total Number of Rows : %s"%str(totalRows))                
            for row in range(1, totalRows + 1):
                tableElements = []
                for col in range(2, totalColumns + 1):
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[%i]"%(tableName, row, col)
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Able to fetch Users Info '%s'"%str(tempDict))
                userList.append(tempDict)      
            return self.browserObject, True, userList
        except Exception as e:
            return self.browserObject, False, "Unable to read Users :: Error -> %s"%(str(e) + format_exc())
        
    def createDirectory(self, directoryType, directoryName, directoryUserName, directoryPassword, host, port, protocol, baseDN, directoryFilter, userNameAttr, fNameAttr, lNameAttr, emailAttr):
        """
        """
        try:
            utility.execLog("Clicking Directory Services Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("tabDirectory"))),action="CLICK")
            time.sleep(2)
            utility.execLog("Clicking Create new directory button")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("lnkNewDirectoryService"))),action="CLICK")
            time.sleep(3)
            utility.execLog("Entering Connection settings values")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("DirectoryUserRole"))),action="SELECT",selectBy = "VISIBLE_TEXT",setValue=directoryType)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryServerName"))),action = "SET_TEXT",setValue=directoryName)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryBindDN"))),action = "SET_TEXT",setValue=directoryUserName)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryUserPassword"))),action = "SET_TEXT",setValue=directoryPassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userPasswordConfirm"))),action = "SET_TEXT",setValue=directoryPassword)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userHost"))),action = "SET_TEXT",setValue=host)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userPort"))),action = "SET_TEXT",setValue=port)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("protocol"))),action="SELECT",selectBy = "VISIBLE_TEXT",setValue=protocol)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.UsersObjects("btnWizardNext"))),action="IS_DISPLAYED")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.UsersObjects("btnWizardNext"))),action="CLICK")
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryBasedn"))),action = "SET_TEXT",setValue=baseDN)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryFilter"))),action = "SET_TEXT",setValue=directoryFilter)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userUname"))),action = "SET_TEXT",setValue=userNameAttr)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("fname"))),action = "SET_TEXT",setValue=fNameAttr)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("lname"))),action = "SET_TEXT",setValue=lNameAttr)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("email"))),action = "SET_TEXT",setValue=emailAttr)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.UsersObjects("btnWizardNext"))),action="CLICK")
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.UsersObjects("btnWizardFinish"))),action="CLICK")
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("submitConfirmForm"))),action="CLICK")
            time.sleep(3)
            
            utility.execLog("Reading directory services table to check new service is created")
            self.browserObject, status, dsList  = self.getDirectoryServices()
            if status:
                utility.execLog("Directory list returned is %s"%str(dsList))
                for item in dsList:
                    if directoryName in item["Name"]:
                        return self.browserObject, True, "Successfully created new Directory"
                    else:
                        continue
                return self.browserObject, False, "Failed to create new Directory"
            else:
                return self.browserObject, False, "Directory list returned is %s"%str(dsList)
        except Exception as e:
            return self.browserObject, False, "Unable to create new Directory :: Error -> %s"%str(e)
                    
    
    def getDirectoryServices(self):
        """
        Description:
            API to get existing Directory Services
        """
        dsList = []
        try:
            utility.execLog("Clicking Directory Services Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("tabDirectory"))),action="CLICK")
            utility.execLog("Reading Directory Services Table")
            tableName = self.UsersObjects("usersTable")
            xpath = "//table[@id='%s']/thead/tr[1]/th"%tableName                
            totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
            utility.execLog("Total Number of Columns : %s"%str(totalColumns))
            tableColumns = []
            for col in range(2, totalColumns + 1):
                xpath = "//table[@id='%s']/thead/tr[1]/th[%i]"%(tableName, col)                    
                if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                    colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    tableColumns.append(colName)
                    utility.execLog("Able to fetch Column Name: '%s'"%colName)
            tableColumns = [x for x in tableColumns if x !='']
            utility.execLog("Able to fetch %s Table Columns '%s'"%(tableName, str(tableColumns)))
            xpath = "//table[@id='%s']/tbody/tr"%tableName                
            totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
            utility.execLog("Total Number of Rows : %s"%str(totalRows))                
            for row in range(1, totalRows + 1):
                tableElements = []
                for col in range(2, totalColumns + 1):
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[%i]"%(tableName, row, col)
                    if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
                        colValue = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                        tableElements.append(colValue)
                tempDict = dict(zip(tableColumns, tableElements))
                utility.execLog("Able to fetch Directory Services Info '%s'"%str(tempDict))
                dsList.append(tempDict)      
            return self.browserObject, True, dsList
        except Exception as e:
            return self.browserObject, False, "Unable to read Directory Services :: Error -> %s"%str(e) + format_exc()    
    
    def deleteUser(self, userName):
        """
        Description:
            API to Delete existing User
        Input:
            userName => Deletes Specific User
        """  
        try:
            utility.execLog("Reading Users Table")
            tableName= self.UsersObjects("usersTable")
            xpath = "//table[@id='%s']/tbody/tr"%tableName
            totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            for row in range(1, totalRows + 1):
                xpath = "//table[@id='%s']/tbody/tr[%i]/td[2]"%(tableName, row)
                uName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                utility.execLog("Able to fetch User Info '%s'"%uName)
                if userName == uName and uName != "admin":         
                    #usrId = "chkUser_" + str(row-1)
                    xpathCheck = "//table[@id='%s']/tbody/tr[%i]/td[1]/input"%(tableName, row)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpathCheck)), action="CLICK")
                    #===========================================================
                    # xpath = "//table[@id='%s']/tbody/tr[%i]/td[1]"%(tableName, row)
                    # self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    #===========================================================
                    utility.execLog("Selected User '%s'"%uName)
            status = self.handleEvent(EC.presence_of_element_located((By.ID, self.UsersObjects("deleteUserLink"))), action="IS_ENABLED")
            
            if not status:
                utility.execLog("'Delete' User option is disabled")
                return self.browserObject, False, "Unable to click on 'Delete' to delete user '%s'"%userName
            else:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("deleteUserLink"))), action="CLICK")
                time.sleep(3)
                utility.execLog("Confirming to Delete User '%s'"%userName)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("submitConfirmForm"))), action="CLICK")
                time.sleep(2)
                try:
                    errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.UsersObjects("clarityError"))), action="GET_TEXT")
                    time.sleep(3)
                    return self.browserObject, False, "Failed to Delete User :: '%s' :: Error -> '%s'"%(userName, 
                                                        str(errorMessage))
                except:
                    return self.browserObject, True, "Successfully Deleted user '%s'"%userName
        except Exception as e:
            return self.browserObject, False, "Unable to Delete User %s :: Error -> %s"%(userName, str(e) + format_exc())
    
    def enableUser(self, userName):
        """
        Description:
            API to Enable existing User
        """
        try:
            utility.execLog("Reading Users Table")
            tableName= self.UsersObjects("usersTable")
            xpath = "//table[@id='%s']/tbody/tr"%tableName
            totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            for row in range(1, totalRows + 1):
                xpath = "//table[@id='%s']/tbody/tr[%i]/td[2]"%(tableName, row)
                uName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                utility.execLog("Able to fetch User Info '%s'"%uName)
                if userName == uName and uName != "admin":         
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[1]/input"%(tableName, row)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    utility.execLog("Selected User '%s'"%uName)
            utility.execLog("Clicking 'Enable' to enable User '%s'"%userName)
            status = self.handleEvent(EC.presence_of_element_located((By.ID, self.UsersObjects("enableUser"))), action="IS_ENABLED")
            if not status:
                utility.execLog("'Enable' User option is disabled")
                return self.browserObject, False, "Unable to click on 'Enable' to enable user '%s'"%userName
            else:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("enableUser"))), action="CLICK")
                return self.browserObject, True, "Successfully Enabled user '%s'"%userName
        except Exception as e:
            return self.browserObject, False, "Unable to 'Enable' User %s :: Error -> %s"%(userName, str(e) + format_exc())
    
    def disableUser(self, userName, useEdit=False, currentPassword=""):
        """
        Description:
            API to Disable existing User:- Two way to do
        """
        try:
            utility.execLog("Reading Users Table")
            tableName= self.UsersObjects("usersTable")
            xpath = "//table[@id='%s']/tbody/tr"%tableName
            totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
            utility.execLog("Total Number of Rows : %s"%str(totalRows))
            for row in range(1, totalRows + 1):
                xpath = "//table[@id='%s']/tbody/tr[%i]/td[2]"%(tableName, row)
                uName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                utility.execLog("Able to fetch User Info '%s'"%uName)
                if userName == uName and uName != "admin":         
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[1]/input"%(tableName, row)
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                    utility.execLog("Selected User '%s'"%uName)
            if useEdit:
                utility.execLog("Clicking 'Edit' to disable User '%s'"%userName)
                self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.UsersObjects("editUserLink"))), action="CLICK")
                time.sleep(2)         
                #self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("editUserLink"))), action="CLICK")
                editUserPageText = self.handleEvent(EC.element_to_be_clickable((By.CLASS_NAME, self.UsersObjects("modalContent"))), action="GET_TEXT")
                if "Edit User" not in editUserPageText:
                    return self.browserObject, False, "Failed to verify Edit User Page"
                utility.execLog("Edit User Page loaded")
                #Set Current password
                utility.execLog("Setting Current Password %s"%currentPassword)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("currentPassword"))), action="CLEAR")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("currentPassword"))), action="SET_TEXT", setValue=currentPassword)
                utility.execLog("Unchecking Enable User")
                if self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEnabled"))), action="IS_SELECTED"):
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("userEnabled"))), action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("submitUserForm"))), action="CLICK")
                return self.browserObject, True, "Successfully Disabled user '%s'"%userName
            else:
                utility.execLog("Clicking 'Disable' to disable User '%s'"%userName)
                status = self.handleEvent(EC.presence_of_element_located((By.ID, self.UsersObjects("disableUser"))), action="IS_ENABLED")
                if not status:
                    utility.execLog("'Disable' User option is disabled")
                    return self.browserObject, False, "Unable to click on 'Disable' to disable user '%s'"%userName
                else:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("disableUser"))), action="CLICK")
                    return self.browserObject, True, "Successfully Disabled user '%s'"%userName
        except Exception as e:
            return self.browserObject, False, "Unable to 'Disable' User %s :: Error -> %s"%(userName, str(e) + format_exc())
    
    def clearSelection(self):
        """
        Description:
            API to Deselect All Users
        """
        try:
            utility.execLog("Deselecting All Users")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("selectAllUsers"))), action="CLICK")
            if self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("selectAllUsers"))), action="IS_SELECTED"):
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("selectAllUsers"))), action="CLICK")
            return self.browserObject, True, "Able to Deselect All Users"
        except Exception as e:
            return self.browserObject, False, "Unable to Deselect All Users :: Error -> %s"%str(e)    
    
    def getOptions(self, userName, optionTab):
        """
        Description:
            API to get Options and their Accessibility for Users Page 
        """
        optionList = {}
        try:            
            time.sleep(5)
            if optionTab == "Users":
                utility.execLog("Clicking on Users Tab")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("tabUsers"))), action="CLICK")
                options = [self.UsersObjects("importUsers"), self.UsersObjects("newUserlink")]
                for optValue in options:
                    utility.execLog("Reading Option '%s'"%optValue)
                    optName = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_TEXT")
                    result = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                    if result == "true":
                        optionList[optName] = "Disabled"
                    else:
                        optionList[optName] = "Enabled"
                utility.execLog("Reading Users Table")
                userSelected = False
                tableName= self.UsersObjects("usersTable")
                xpath = "//table[@id='%s']/tbody/tr"%tableName
                totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
                utility.execLog("Total Number of Rows : %s"%str(totalRows))
                for row in range(1, totalRows + 1):
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[2]"%(tableName, row)
                    uName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    utility.execLog("Able to fetch User Info '%s'"%uName)
                    if userName == uName and uName != "admin":
                        xpath = "//table[@id='%s']/tbody/tr[%i]/td[1]/input"%(tableName, row)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                        utility.execLog("Selected User '%s'"%uName)
                        userSelected = True
                        break
                if userSelected:
                    options = [self.UsersObjects("editUserLink"), self.UsersObjects("deleteUserLink"), self.UsersObjects("disableUser"), self.UsersObjects("enableUser")]
                    for optValue in options:
                        utility.execLog("Reading Option '%s'"%optValue)
                        optName = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_TEXT")
                        result = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                        if result == "true":
                            optionList[optName] = "Disabled"
                        else:
                            optionList[optName] = "Enabled"
                else:
                    return self.browserObject, False, "Unable to Select user '%s' to get Options"%userName
            else:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("tabDirectory"))), action="CLICK")
                optName = self.handleEvent(EC.presence_of_element_located((By.ID,self.UsersObjects("lnkNewDirectoryService") )), action="GET_TEXT")
                result = self.handleEvent(EC.presence_of_element_located((By.ID, self.UsersObjects("lnkNewDirectoryService"))), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                if result == "true":
                    optionList[optName] = "Disabled"
                else:
                    optionList[optName] = "Enabled"
                utility.execLog("Reading Directory Services Table")
                userSelected = False
                tableName= self.UsersObjects("usersTable")
                xpath = "//table[@id='%s']/tbody/tr"%tableName
                totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
                utility.execLog("Total Number of Rows : %s"%str(totalRows))
                for row in range(1, totalRows + 1):
                    xpath = "//table[@id='%s']/tbody/tr[%i]/td[2]"%(tableName, row)
                    uName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
                    utility.execLog("Able to fetch User Info '%s'"%uName)
                    if userName == uName and uName != "admin":
                        xpath = "//table[@id='%s']/tbody/tr[%i]/td[1]"%(tableName, row)
                        self.handleEvent(EC.element_to_be_clickable((By.XPATH, xpath)), action="CLICK")
                        utility.execLog("Selected User '%s'"%uName)
                        userSelected = True
                        break
                if userSelected:
                    options = [self.UsersObjects("editDirectoryLink"), self.UsersObjects("deleteUserLink")]
                    for optValue in options:
                        utility.execLog("Reading Option '%s'"%optValue)
                        optName = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_TEXT")
                        result = self.handleEvent(EC.presence_of_element_located((By.ID, optValue)), action="GET_ATTRIBUTE_VALUE", attributeName="disabled")
                        if result == "true":
                            optionList[optName] = "Disabled"
                        else:
                            optionList[optName] = "Enabled"
            return self.browserObject, True, optionList
        except Exception as e:
            return self.browserObject, False, "Unable to read Options on Users Page :: Error -> %s"%(str(e) + format_exc())
        
    # Created for TC-2496    
#     def editUserInformation(self, userName, currentPassword, editPassword=None, editFName=None, editLName=None, editRole=None, editEmail=None, editPhone=None):
#         """
#         Description:
#             Edit the existing user information
#         """
#         try:
#             utility.execLog("Reading Users Table")
#             time.sleep(5)    
#             table = self.browserObject.find_element_by_id(self.UsersObjects("usersTable"))
#             time.sleep(1)
#             # Fetch Table header Details
#             tableBody = table.find_element_by_tag_name("tbody")
#             tableBodyRows = tableBody.find_elements_by_tag_name("tr")
#             utility.execLog("Able to fetch User Table Rows")
#             time.sleep(1)
#             for rowindex in xrange(0, len(tableBodyRows)):   
#                 time.sleep(1)             
#                 rowID = "chkUser_%s"%str(rowindex)
#                 colUser = tableBodyRows[rowindex].find_elements_by_tag_name("td")[1].text   
#                 utility.execLog("Able to fetch User Info '%s'"%colUser)             
#                 if colUser == userName and colUser != "admin":         
#                     utility.execLog("Disabling User '%s'"%colUser)           
#                     eleUser = table.find_element_by_id(rowID)
#                     eleUser.click()
#             utility.execLog("Clicking 'Edit' to disable User '%s'"%colUser)
#             eleEdit = self.browserObject.find_element_by_id(self.UsersObjects("editUserLink"))
#             eleEdit.click()
#             time.sleep(2)            
#             editUserPage = self.browserObject.find_element_by_id(self.UsersObjects("modal-content"))
#             editUserPage.click()
#             time.sleep(2)
#             if "Edit User" not in editUserPage.text:
#                 return self.browserObject, False, "Failed to verify Edit User Page"
#             utility.execLog("Edit User Page loaded")
#             #Set Current password
#             utility.execLog("Setting Current Password %s"%currentPassword)
#             try:
#                 eleEdit = editUserPage.find_element_by_id(self.UsersObjects("currentPassword"))
#             except:
#                 eleEdit = editUserPage.find_element_by_id("Password2")
#             eleEdit.clear()
#             eleEdit.send_keys(currentPassword)
#             time.sleep(1)
#             
#             if editPassword:
#                 utility.execLog("Setting new password %s"%editPassword)
#                 
#             if editFName:
#                 utility.execLog("Setting new First Name %s"%editFName)
#                 
#             if editLName:
#                 utility.execLog("Setting new Last Name %s"%editLName)
#                 
#             if editRole:
#                 utility.execLog("Setting new Role %s"%editRole)
#                 roles = Select(self.browserObject.find_element_by_id(self.UsersObjects("userRole")))
#                 roles.select_by_visible_text(editRole)
#                 
#             if editEmail:
#                 utility.execLog("Setting new Email %s"%editEmail)
#                 
#             if editPhone:
#                 utility.execLog("Setting new Phone %s"%editPhone)
#                 
#             #Save the Page.
#             utility.execLog("Saving the user information")
#             self.browserObject.find_element_by_id(self.UsersObjects("submitUserForm")).click() 
#             time.sleep(3)
#             return self.browserObject, True, "User %s information is updated."%str(userName)
#              
#         except Exception as e:
#             return self.browserObject, False, "Unable to Edit User %s information :: Error -> %s"%(userName, str(e))
        
    # HCL 2657
#     def editUserRole(self, userName, currentPassword,userRole):
#         """
#         Creates a New Local User        
#         """
#         try:
#             time.sleep(2)
#             utility.execLog(" in Edit User Role Function username : %s"%str(userName))
#             utility.execLog("Reading Users Table")
#             time.sleep(5)    
#             table = self.browserObject.find_element_by_id(self.UsersObjects("usersTable"))
#             utility.execLog("found users_table : %s"%str(table))
#             time.sleep(1)
#             # Fetch Table header Details
#             tableBody = table.find_element_by_tag_name("tbody")
#             utility.execLog("found table body : %s"%str(tableBody))
#             tableBodyRows = tableBody.find_elements_by_tag_name("tr")
#             utility.execLog("Able to fetch User Table Rows with length : %s"%str(len(tableBodyRows)))
#             time.sleep(1)
#             for rowindex in xrange(0, len(tableBodyRows)):   
#                 time.sleep(1)             
#                 rowID = "chkUser_%s"%str(rowindex)
#                 colUser = tableBodyRows[rowindex].find_elements_by_tag_name("td")[1].text   
#                 utility.execLog("Able to fetch User Info in EditUserRole'%s'"%colUser)             
#                 if colUser == userName:         
#                     utility.execLog("selecting User '%s'"%colUser)
#                     utility.execLog("row id : '%s'"%rowID)
#                     eleUser = self.browserObject.find_element_by_id(rowID)
#                     time.sleep(5)
#                     eleUser.click()
#                     utility.execLog("Clicking 'Edit' to Change User Role '%s'"%colUser)
#                     time.sleep(5)
#                     eleEdit = self.browserObject.find_element_by_id(self.UsersObjects("editUserLink"))
#                     eleEdit.click()
#                     time.sleep(5)            
#                     editUserPage = self.browserObject.find_element_by_id(self.UsersObjects("pageEditUser"))
#                     editUserPage.click()
#                     time.sleep(5)
#                     if "Edit User" not in editUserPage.text:
#                         return self.browserObject, False, "Failed to verify Edit User Page"
#                     utility.execLog("Edit User Page loaded")
#                     #Set Current password
#                     utility.execLog("Setting Current Password %s"%currentPassword)
#                     eleEdit = editUserPage.find_element_by_id(self.UsersObjects("currentPassword"))
#                     eleEdit.clear()
#                     eleEdit.send_keys(currentPassword)
#                     time.sleep(2)
#                     utility.execLog("Able to set Current Password %s"%currentPassword)
#             
#                     #Set User Role
#                     utility.execLog("Setting User Role %s"%userRole)
#                     select = Select(self.browserObject.find_element_by_id(self.UsersObjects("userRole")))  
#                     select.select_by_visible_text(userRole)
#                     utility.execLog("Able to select user role..")
#                     time.sleep(5)
#                     utility.execLog("Able to set User Role %s"%userRole)
#                     eleCreateUser = self.browserObject.find_element_by_id(self.UsersObjects("submitUserForm"))
#                     eleCreateUser.click()
#                     time.sleep(3)
# 
#         except Exception as e:
#             return self.browserObject, False, "Failed to Create Local User :: '%s' with Role :: '%s' :: Error -> %s"%(userName, userRole, str(e))
            
    def filterByGroup(self, option):
        """
        Function select the 'filter by group' dropdown vlaue to the provided option
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("ddlADGroup"))),action="SELECT",setValue=option,selectBy="VISIBLE_TEXT")
            return self.browserObject, True, "filter by group dropdown value selected to %s"%str(option)
        except Exception as e:
            return self.browserObject, False, "Could not select the dropdown to %s :: Error -> %s"%(str(option), str(e))
        
    def verifyUserAvailable(self, userName):
        try:
            self.browserObject, status, result = self.getUsers()
            if status:
                utility.execLog("User List=> %s"%str(result))
                for item in result:
                    if item["Name"] == userName:
                        return self.browserObject, True, "User %s is present in list"%str(userName)
                return self.browserObject, False, "User %s is not present in list"%str(userName) 
        except Exception as e:
            return self.browserObject, False, "Exception generated while searching the user %s :: Error -> %s"%(str(userName), str(e))
        
    
    def importADUsers(self, directorySource, searchTerm, importRole):
        """
        Import active directory Group/Users
        """
        try:
            utility.execLog("Clicking users tab link")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("tabUsers"))),action="CLICK")
            time.sleep(2)
            utility.execLog("Clicking import AD users link")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("importUsers"))),action="CLICK")
            time.sleep(3)
            utility.execLog("Selecting the directory with value %s"%str(directorySource))
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryddl"))),action="SELECT",selectBy= "VISIBLE_TEXT",setValue=directorySource)
            time.sleep(1)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("searchUserGroups"))),action="SET_TEXT",setValue=searchTerm)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("searchUserGroups"))),action="KEY",setValue=Keys.RETURN)
            #self.browserObject.find_element_by_id(self.UsersObjects("searchUserGroups")).send_keys(Keys.RETURN)
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userRole"))),action="SELECT",selectBy= "VISIBLE_TEXT",setValue=importRole)
            time.sleep(1)
            xpath="//span[contains(text(),'%s')]/parent::td/parent::tr/td[1]/input"%str(searchTerm)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="CLICK")
            time.sleep(1)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("shiftToRight"))),action="CLICK")
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("saveImportUserForm"))),action="CLICK")
            time.sleep(2)
            return self.browserObject, True, "Directory %s successfully imported"%str(searchTerm)
        except Exception as e:
            return self.browserObject, False, "Exception generated while importing Directory %s :: Error -> %s"%(str(searchTerm), str(e))
        
    def verifyeditADUserPasswordField(self, user):
        """
        """
        try:
            #self.browserObject.find_element_by_xpath("//span[contains(text(),'%s')]/parent::td/parent::tr/td[1]/input"%str(user)).click()
            #self.browserObject.find_element_by_xpath("//td[contains(text(),'%s')]/parent::tr/td[1]/input"%str(user)).click()
            xpath="//td[contains(text(),'%s')]/parent::tr/td[1]/input"%str(user)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="CLICK")
            time.sleep(2)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("editUserLink"))),action="CLICK")
            time.sleep(5)
            #test=self.browserObject.find_element_by_xpath(self.UsersObjects("userPassword")).is_enabled
            test=self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("editUserPassword"))),action="IS_ENABLED")
            #self.browserObject.find_element_by_xpath("//input[@disabled='']")
            if test:
                return self.browserObject, True, "AD user password fields are enabled during editing"
            else:
                return self.browserObject, False, "AD user password fields are disabled during editing"
        except Exception as e:
            return self.browserObject, False, "got exception:%s"%e
        
            
    
    def filterUserTableByGroup(self, group, user):
        """
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.UsersObjects("ddlADGroup"))), action="SELECT", setValue=group)
            time.sleep(2)
            #self.browserObject.find_element_by_xpath("//td[normalize-space(text())='%s']"%str(user))
            globalVars.browserObject, status, result = self.getUsers()
            if status:
                utility.execLog("Successfully Fetched Local Users: %s" %str(result))
            else:
                utility.execLog(result, raiseExc=True)
            for user in result:
                if user["Active Directory Group"]!=group:
                    return self.browserObject, False, "User table could not be filtered according to group."    
            return self.browserObject, True, "Able to filter user table by group."
        except Exception as e:
            return self.browserObject, False, "User table could not be filtered according to group."   
        
    def verifyADUserRole(self, user, role):
        try:
            #self.browserObject.find_element_by_xpath("(//span[contains(text(),'%s')]/parent::td/parent::tr/td[5]/span[contains(text(),'%s')])[1]"%(str(user),str(role)))
            #self.browserObject.find_element_by_xpath("(//td[normalize-space(text())='%s']/parent::tr/td[5][normalize-space(text())='%s'])"%(str(user),str(role)))
            xpath="//td[normalize-space(text())='%s']/parent::tr/td[5][normalize-space(text())='%s']"%(str(user),str(role))
            self.handleEvent(EC.presence_of_element_located((By.XPATH,xpath)))
            return self.browserObject, True, "Role assigned is displayed in user table."
        except:
            return self.browserObject, False, "Role assigned is not correct."
        
    def deleteActiveDirectory(self, directoryName):
        """
        Delete existing directory
        """
        try:
            utility.execLog("Clicking Directory Services Tab")
            #self.browserObject.find_element_by_id(self.UsersObjects("tabDirectory")).click()
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("tabDirectory"))),action="CLICK")
            time.sleep(2)
            utility.execLog("Selecting the directory to be deleted.")
            #self.browserObject.find_element_by_xpath("//*[contains(text(),'%s')]/parent::tr/td[1]/input"%str(directoryName)).click()
            xpath="//*[contains(text(),'%s')]/parent::tr/td[1]/input"%str(directoryName)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="CLICK")
            time.sleep(2)
            utility.execLog("click Delete button")
            #self.browserObject.find_element_by_id(self.UsersObjects("deleteUserLink")).click()
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("deleteUserLink"))),action="CLICK")
            time.sleep(2)
            #self.browserObject.find_element_by_id(self.UsersObjects("submitConfirmForm")).click()
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("submitConfirmForm"))),action="CLICK")
            time.sleep(2)
            utility.execLog("Verify directory is deleted")
            try:
                #self.browserObject.find_element_by_xpath("//*[contains(text(),'%s')]/parent::tr/td[1]/input"%str(directoryName))
                xpath="//*[contains(text(),'%s')]/parent::tr/td[1]/input"%str(directoryName)
                self.handleEvent(EC.presence_of_element_located((By.XPATH,xpath)))
                utility.execLog("Directory is not deleted")
                return self.browserObject, False, "Failed to delete Directory service %s"%(str(directoryName))
            except:
                return self.browserObject, True, "Directory service %s is successfully deleted"%(str(directoryName))
        except Exception as e:
            return self.browserObject, False, "Exception generated while deleting directory %s  Error -> %s"%(str(directoryName), str(e))
    
    def verifyEdit(self, directoryType=None, directoryName=None, directoryUserName=None, directoryPassword=None, host=None, port=None, protocol=None, baseDN=None, directoryFilter=None, userNameAttr=None, fNameAttr=None, lNameAttr=None, emailAttr=None):
        """
        Delete existing directory
        """
        
        try:
            utility.execLog("Clicking Directory Services Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("tabDirectory"))),action="CLICK")
            time.sleep(2)
            utility.execLog("Select Directory to be Edit")
            xpath="//*[contains(text(),'%s')]/parent::tr/td[1]/input"%str(directoryName)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,xpath)),action="CLICK")
            time.sleep(1)
            utility.execLog("click Edit button")
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("editDirectoryLink"))),action="CLICK")
            time.sleep(2)
            utility.execLog("Entering Connection settings values")
            if directoryType:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("DirectoryUserRole"))),action="SELECT",selectBy = "VISIBLE_TEXT",setValue=directoryType)
            
            if directoryUserName:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryBindDN"))),action = "SET_TEXT",setValue=directoryUserName)
            if directoryPassword: 
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryUserPassword"))),action = "SET_TEXT",setValue=directoryPassword)
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userPasswordConfirm"))),action = "SET_TEXT",setValue=directoryPassword)
            if host: 
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userHost"))),action = "SET_TEXT",setValue=host)
            if port:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userPort"))),action = "SET_TEXT",setValue=port)
            if protocol:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("protocol"))),action="SELECT",selectBy = "VISIBLE_TEXT",setValue=protocol) 
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.UsersObjects("btnWizardNext"))),action="IS_DISPLAYED")
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.UsersObjects("btnWizardNext"))),action="CLICK")
            time.sleep(3)
            if baseDN:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryBasedn"))),action = "SET_TEXT",setValue=baseDN)
            if directoryFilter: 
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("directoryFilter"))),action = "SET_TEXT",setValue=directoryFilter)
            if userNameAttr: 
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("userUname"))),action = "SET_TEXT",setValue=userNameAttr)
            if fNameAttr:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("fname"))),action = "SET_TEXT",setValue=fNameAttr)
            if lNameAttr:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("lname"))),action = "SET_TEXT",setValue=lNameAttr)
            if emailAttr:
                self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("email"))),action = "SET_TEXT",setValue=emailAttr)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.UsersObjects("btnWizardNext"))),action="CLICK")
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.XPATH,self.UsersObjects("btnWizardFinish"))),action="CLICK")
            time.sleep(3)
            self.handleEvent(EC.element_to_be_clickable((By.ID,self.UsersObjects("submitConfirmForm"))),action="CLICK")
            time.sleep(3)
            
            try:
                errorMessage = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.UsersObjects("clarityError"))), action="GET_TEXT")
                time.sleep(3)
                return self.browserObject, False, "Failed to Edit Active Directoryr :: '%s' :: Error -> '%s'"%(directoryName,errorMessage)  
            except:
                return self.browserObject, True, "Successfully Edited Active Directory :: '%s'"%(directoryName)
        except Exception as e:
            return self.browserObject, False, "Failed to Edit Active Directoryr :: '%s' :: Error -> '%s'"%(directoryName,str(e))
            