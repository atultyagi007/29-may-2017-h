"""
Author: P Suman/Saikumar Kalyankrishnan/HCL Team
Created/Modified: Jun 30th 2015/Feb 15th 2017
Description: Functions/Operations related to Login Page
"""

from CommonImports import *
from libs.product.pages.Controller import Controller
from libs.product.objects.Login import Login

osType = platform.system()

class Login(Controller, Login):
    """
    Description:
        Class which includes all the operations related to Login page
    """
    def __init__(self, browserName, applianceIP, newInstance=True, browserObject=None):
        """
        Description: 
            Initializing an object of this class.             
        Input:
            browserName (String): On which Browser to execute
            applianceIP (String): ASM Appliance IP
        Output:
            None
        """
        self.pageTitle = "Active System Manager"
        self.homePage = "Welcome to Active System Manager"        
        self.browserName = browserName
        self.applianceIP = applianceIP
        self.newInstance = newInstance
        if browserObject:
            self.browserObject = browserObject
        else:
            self.browserObject = None
                
    def getBrowserHandle(self):
        """
        Gets Browser Handle
        """
        try:
            if self.newInstance:
                self.bitVersion = self.getPlatformArch()
                utility.execLog("Creating Browser Handle")
                if self.browserName == "Firefox":
                    self.browserObject = self.getFireFoxBrowser()            
                elif self.browserName == "Ie":
                    self.browserObject = self.getIEBrowser()            
                else:
                    self.browserObject = self.getChromeBrowser()        
                self.browserObject.maximize_window()
                utility.execLog("Maximized Browser window")
            return self.browserObject, True, "Able to fetch Browser Handle"
        except Exception as e:
            return self.browserObject, False, "Failed to launch Browser" + str(e)
        
    def getPlatformArch(self):
        """
        Description: 
            API which returns OS Architecture based on the OS Type             
        Input:
            None
                
        Output:
            osArch (String): Returns OS Architecture
        """
        osArch = ""
        if osType == "Windows":
            osDrive = os.getenv("systemdrive")
            if os.path.isdir(os.path.join(osDrive, "Program Files (x86)")):
                osArch = 'x86_64'
            else:
                osArch = 'i386'
        elif osType == "Linux":
            if platform.machine() == 'x86_64':
                osArch = 'x86_64'
            elif platform.machine() == 'i686' or platform.machine() == 'i386':
                osArch = 'i386'
            
        return osArch
    
    def getIEBrowser(self):
        """
        Description: 
            API which creates an IE Browser Object and returns             
                
        Input:
            None
                
        Output:
            IE Browser Object
        """
        try:
            from libs.thirdparty.selenium.webdriver import DesiredCapabilities as DC
            # Disabling Protected Mode
            iexp = DC.INTERNETEXPLORER
            iexp['ignoreProtectedModeSettings'] = True            
            iexp["unexpectedAlertBehaviour"] = "accept"
        
            # Setting the Path to IEDriverServer.exe
            path = [os.environ["PATH"]]
            path.append(os.path.join(os.getcwd(), "tools", "IE", osType, self.bitVersion))
            path = os.pathsep.join(path)
            os.putenv("PATH", path) 
        
            # driver = webdriver.Ie(capabilities=iexp)
            driver = webdriver.Ie(capabilities=iexp)
            driver.implicitly_wait(10)
            # Navigation Error issue
            if str(driver.title) == 'Certificate Error: Navigation Blocked':
                driver.get("javascript:document.getElementById('overridelink').click();")
            utility.execLog("Successfully obtained IE Browser handle")            
            return driver
        except Exception as e:
            utility.execLog("Unable to launch IE Browser :: Error -> %s" % str(e))
            raise e
    
    def getFireFoxBrowser(self):
        """
        Description: 
            API which creates an Firefox Browser Object and returns             
                
        Input:
            None
                
        Output:
            Firefox Browser Object
        """            
        try:
            profile = webdriver.FirefoxProfile()
            profile.set_preference("browser.download.folderList", 2)
            profile.set_preference("browser.download.manager.showWhenStarting", False)            
            profile.set_preference("browser.download.dir", os.path.join(os.getcwd(), utility.logsdir))
            profile.set_preference("browser.helperApps.neverAsk.saveToDisk", "application/x-gzip")
            driver = webdriver.Firefox(firefox_profile=profile)
            driver.implicitly_wait(10)
            utility.execLog("Successfully obtained Firefox Browser handle")
            return driver
        except Exception as e:
            utility.execLog("Unable to launch Firefox Browser :: Error -> %s" % str(e))
            raise e

    def getChromeBrowser(self):
        """
        Description: 
            API which creates an Chrome Browser Object and returns             
            Updated function for NGI-TC-2637
        Input:
            None
                
        Output:
            Chrome Browser Object
        """
        try:
            # Setting Chrome options for downloads directory
            downloadDir = os.path.join(os.getcwd(), globalVars.downloadDir)
            if not os.path.exists(downloadDir):
                os.makedirs(downloadDir)
            else:
                for the_file in os.listdir(downloadDir):
                    file_path = os.path.join(downloadDir, the_file)
                    try:
                        if os.path.isfile(file_path):
                            os.unlink(file_path)
                    except Exception, e:
                        utility.execLog("Unable to empty the downloads directory :: Error -> %s" % str(e))
                        raise e
            chromeOptions = webdriver.ChromeOptions()
            prefs = {"download.default_directory" : downloadDir}
            chromeOptions.add_experimental_option("prefs",prefs)
            
            # Setting the Path to chromedriver.exe
            path = [os.environ["PATH"]]
            path.append(os.path.join(os.getcwd(), "tools", "Chrome", osType))
            path = os.pathsep.join(path)
            os.putenv("PATH", path)
            downloadPath = os.path.join(os.getcwd(), utility.downloadDir)
            chrome_options = Options()
            chrome_options.add_experimental_option('prefs', {'download.default_directory':downloadPath})
            driver = webdriver.Chrome(chrome_options=chrome_options)
            driver.implicitly_wait(10)
            utility.execLog("Successfully obtained Chrome Browser handle")
            return driver
        except Exception, e:
            utility.execLog("Unable to launch Chrome Browser :: Error -> %s" % str(e))
            raise e

    def launchApplication(self):
        """
        Description: 
            API to launch application with the provided Appliance IP              
        """        
        try:
            if self.browserName == "Ie":
                self.browserObject.get('https://%s' % (self.applianceIP)) 
                if str(self.browserObject.title) == 'Certificate Error: Navigation Blocked':
                    self.browserObject.get("javascript:document.getElementById('overridelink').click();")
            else:            
                self.browserObject.get('https://%s' % (self.applianceIP))
            utility.execLog("Successfully launched Appliance IP: %s" % self.applianceIP)
        except Exception as e:
            utility.execLog("Unable to launch Appliance IP:%s :: Error -> %s" % (self.applianceIP, str(e)))
            raise e
        
    def setUsername(self, username):
        """
        Description: 
            API to set Login Username
        """
        try:
            loop = 1
            while loop < 10:
                try:
                    utility.execLog("Entering Username:%s" % username)
                    WebDriverWait(self.browserObject, globalVars.defaultWaitTime).until(EC.element_to_be_clickable((By.ID, self.LoginObjects('username')))).clear()
                    WebDriverWait(self.browserObject, globalVars.defaultWaitTime).until(EC.element_to_be_clickable((By.ID, self.LoginObjects('username')))).send_keys(username)
                    utility.execLog("Successfully set Username:'%s'" % username)
                    return
                except TimeoutException as e:
                    utility.execLog("Username Textbox is not loaded so doing a Page Refresh...Iteration : %s" % str(loop))
                    self.browserObject.refresh() 
                    loop += 1
            utility.execLog("Unable to set Login Username '%s' :: Username Textbox is not loaded" % username)
            raise Exception("Unable to set Login Username '%s' :: Username Textbox is not loaded" % username)
        except Exception as e:
            utility.execLog("Unable to set Login Username '%s' :: Error -> %s" % (username, str(e)))
            raise e
            
    def setPassword(self, password):
        """
        Description: 
            API to set Login Password
        """
        try:
            utility.execLog("Entering Password:'%s'" % password)
            self.browserObject.find_element_by_id(self.LoginObjects('password')).clear()
            self.browserObject.find_element_by_id(self.LoginObjects('password')).send_keys(password)

            utility.execLog("Successfully set Password:'%s'" % password)
            time.sleep(1)
        except Exception as e:
            utility.execLog("Unable to set Login Password '%s' :: Error -> %s" % (password, str(e)))
            raise e
    
    def validatePageTitle(self, title=None):
        """
        Description: 
            API to validate Login Page Title
        """
        if not title:
            title = self.pageTitle
        if title not in self.browserObject.title:
            utility.execLog("Unable to validate Page Title Actual:'%s' Expected:'%s'" % (self.browserObject.title, title))
            raise Exception("Unable to validate Page Title Actual:'%s' Expected:'%s'" % (self.browserObject.title, title))
        else:
            utility.execLog("Successfully validated Home Page Title -> %s" % title)
        
    def loginApp(self, username = 'admin', password = 'admin', negativeScenario=False, waitTime=0):
        """
        Description: 
            API to set Username and Password and Login to Home Page     
        
        Input:
            username (String): Username to Set. Default is 'admin'
            password (String): Password to Set. Default is 'admin'
                
        Output:
            Landing Page ID: 0 Error/Fail | 1 Default Password Warning | 2 Initial Setup Wizard | 3 Getting Started | 4 Dashboard
        """
        landingPage = 0
        try:
            utility.execLog("username=%s and password=%s" % (str(username), str(password)));
            if self.newInstance:
                self.launchApplication()            
                time.sleep(5)
                self.validatePageTitle(self.pageTitle)
            time.sleep(3)
            self.setUsername(username)
            if waitTime > 0:
                time.sleep(waitTime*60)
            self.setPassword(password)
            utility.execLog("Clicking on Login Button...")
            self.browserObject.find_element_by_id(self.LoginObjects('login')).click()
            time.sleep(10)
            try:
                eleLogin = self.browserObject.find_element_by_xpath(self.LoginObjects('loginError'))
                if negativeScenario:
                    utility.execLog("Successfully verified Login with Username=%s :: Unable to login (negative scenario) (%s)" % (username, eleLogin.text))
                    return self.browserObject, True, "Successfully verified Login with Username=%s :: Unable to login (negative scenario) (%s)" % (username, eleLogin.text)
                else:
                    utility.execLog("Failed to Login with Username %s :: Error Message -> %s" % (username, str(eleLogin.text)))
                    return self.browserObject, False, "Failed to Login with Username %s :: Error Message -> %s" % (username, str(eleLogin.text))
            except:
                utility.execLog("Entered Correct Username & Password...No Error Message Found")
                try:
                    loop = 1
                    while loop < 6:
                        try:
                            utility.execLog("Checking if Getting Started Page is loaded...")
                            WebDriverWait(self.browserObject, 30).until(EC.element_to_be_clickable((By.ID, self.LoginObjects('checkGettingStarted'))))
                            utility.execLog("Successfully logged into Landing Page: Getting Started")
                            landingPage = 3
                            break
                        except Exception as e:
                            try:
                                utility.execLog("Checking if Dashboard Page is loaded...")
                                WebDriverWait(self.browserObject, 30).until(EC.element_to_be_clickable((By.ID, self.LoginObjects('checkDashboard'))))
                                utility.execLog("Successfully logged into Landing Page: Dashboard")
                                landingPage = 4
                                break
                            except Exception as e:
                                try:
                                    defaultPasswordCheck = self.browserObject.find_element_by_id(self.LoginObjects('checkDefaultPasswordWarning'))
                                    if defaultPasswordCheck:
                                        utility.execLog("PopUp: Default Password Warning")
                                        time.sleep(5)
                                        utility.execLog("PopUp: Default Password Warning:Clicking on 'Do Not Show'")
                                        defaultPasswordCheck.click()
                                        time.sleep(2)
                                        utility.execLog("PopUp: Default Password Warning: Clicked on 'Do Not Show'")
                                        defaultPasswordSubmit = self.browserObject.find_element_by_id(self.LoginObjects('defaultPasswordSubmit'))
                                        defaultPasswordSubmit.click()
                                        utility.execLog("PopUp: Default Password Warning: Confirm")
                                        time.sleep(2)
                                        landingPage = 1
                                        # For first-time Appliance Login Scenario
                                        utility.execLog("Checking for PopUp: Initial Setup Wizard...")
                                        setupWizardVisible = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LoginObjects('checkSetupWizard'))), action="GET_TEXT")
                                        if "Setup Wizard" in str(setupWizardVisible):
                                            utility.execLog("PopUp: Initial Setup Wizard")
                                            landingPage = 2
                                        break
                                except Exception as e:
                                    try:
                                        setupWizardVisible = (self.browserObject.find_element_by_xpath(self.LoginObjects('checkSetupWizard'))).text
                                        if "Setup Wizard" in str(setupWizardVisible):
                                            utility.execLog("PopUp: Initial Setup Wizard")
                                            landingPage = 2
                                            break
                                    except Exception as e:
                                        utility.execLog("Landing Page is not loaded so doing a Page Refresh...Iteration : %s" % str(loop))
                                        self.browserObject.refresh()
                                        loop += 1
                    if loop >= 6:
                        return self.browserObject, False, "Failed to verify Landing Page while logging into Appliance '%s' with Username '%s' :: Error -> %s" % (self.applianceIP, username, str(e))
                except:
                    pass
                globalVars.currentUser = username
                utility.execLog("Successfully logged into Landing Page with Username: %s" % username)
                # Validate Page Title
                self.validatePageTitle(self.pageTitle)
                utility.execLog("Successfully validated Home Page Title -> %s" % self.pageTitle)
                return self.browserObject, landingPage,  True, "Successfully Logged in and Validated Landing Page with Username: '%s'" % username
        except Exception as e:
            return self.browserObject, landingPage, False, "Exception while Logging into Appliance '%s' with Username '%s' :: Error -> %s" % (self.applianceIP, username, str(e))
    
    def logoutApp(self):
        """
        Description: 
            API to Logout of the ASM Appliance
        
        Input:
            None
                
        Output:
            None         
        """
        try:
            loggedOut = False
            try:
                self.browserObject.find_element_by_id(self.LoginObjects('appHeaderLeft')).is_displayed()
            except:
                loggedOut = True
            if loggedOut:
                return self.browserObject, True, "Already logged out of the Appliance"
            else:
                utility.execLog("Clicking on Username for Logout Option")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LoginObjects('userButton'))), action="CLICK")
                time.sleep(2)
                utility.execLog("Clicked on Username for Logout Option")
                utility.execLog("Clicking on 'Logout' Option")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.LoginObjects('logout'))), action="CLICK")
                time.sleep(2)
                utility.execLog("Successfully logged out of the Appliance from Username: %s" % globalVars.currentUser)
                return self.browserObject, True, "Successfully logged out of the Appliance with Username: %s" % globalVars.currentUser
        except Exception as e:
            return self.browserObject, False, "Unable to logout of the Appliance with Username: %s :: Error -> %s" % (globalVars.currentUser, str(e))