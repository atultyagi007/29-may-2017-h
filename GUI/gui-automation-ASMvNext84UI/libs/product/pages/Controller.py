"""
Author: P Suman/Saikumar Kalyankrishnan
Created/Modified: Feb 16th 2016/Feb 15th 2017
Description: Operations related to Selenium Web Driver
"""

import os
import json
import time
import platform
from traceback import format_exc
from libs.product import utility
from libs.product import globalVars
from libs.product.pages import UIException
from libs.thirdparty.selenium import webdriver
from libs.thirdparty.selenium.common.exceptions import NoSuchElementException, WebDriverException
from libs.thirdparty.selenium.common.exceptions import NoSuchFrameException
from libs.thirdparty.selenium.common.exceptions import StaleElementReferenceException
from libs.thirdparty.selenium.common.exceptions import TimeoutException
from libs.thirdparty.selenium.webdriver.common.keys import Keys
from libs.thirdparty.selenium.webdriver.common.by import By
from libs.thirdparty.selenium.webdriver.support.ui import WebDriverWait 
from libs.thirdparty.selenium.webdriver.support import expected_conditions as EC
from libs.thirdparty.selenium.webdriver.firefox.firefox_binary import FirefoxBinary
from libs.thirdparty.selenium.webdriver.support.select import Select
from libs.thirdparty.selenium.webdriver.chrome.options import Options
from libs.core.SSHConnection import SSHConnection

TIME = globalVars.defaultWaitTime
FREQUENCY = 0.5

class Controller:    
    """
    Description:
        Class which includes all the operations related to Webdriver
    """
   
    def __init__(self, browserObject):
        """
        Description: 
            Initializing an object of this class.             
        Input:
            browserObject: Webdriver Instance
        Output:
            None
        """  
        self.browserObject = browserObject        
    
    def connectSSH(self, hostType, HOST, COMMAND, userName=None, password=None, timeout=120):
        """
        Executes command on provided HOST by connecting through SSH  
        """
        connObj = None
        try:
            if (hostType == 'ToR'):
                USERNAME = "admin"
                PASSWORD = "dell1234"
            elif(hostType == 'Brocade'):
                USERNAME = "admin"
                PASSWORD = "password"
            elif (hostType == 'Switch'):
                USERNAME = "root"
                PASSWORD = "calvin"
            else:
                USERNAME = userName
                PASSWORD = password
            if (USERNAME != "" and PASSWORD != "" and HOST != "" and COMMAND != ""):
                utility.execLog("Attempting to connect to Host '%s' using Username '%s' and Password '%s' and execute command(s) '%s'" % (HOST, USERNAME, PASSWORD, str(COMMAND)))
                connObj = SSHConnection(HOST, USERNAME, PASSWORD, 22, timeout)
                connection_result, connection_error = connObj.Connect()                
                if connection_error != "":
                    return False, connection_error
                utility.execLog("Able to connect to Host :: %s" % str(connection_result))
                utility.execLog("Executing Command(s) '%s'" % str(COMMAND))
                if type(COMMAND) is list:
                    res, err = connObj.Execute_Multiple_Commands(COMMAND)
                    utility.execLog("Execution Status :: Result '%s', Error '%s'" % (res, err))
                    if err != "":
                        return False, err
                    else:
                        return True, res 
                else:        
                    res, err = connObj.Execute(COMMAND)
                    utility.execLog("Execution Status :: Result '%s', Error '%s'" % (res, err))
                    if err != "":
                        return False, err
                    else:
                        return True, res
            else:
                return False, "One of the attributes missing HOST/USERNAME/PASSWORD//COMMAND ('%s', '%s', '%s', '%s')" % (HOST, USERNAME, PASSWORD, COMMAND)
        except Exception as e:
            return False, "Exception while executing command :: Error -> %s" % (str(e) + format_exc())
        finally:
            if connObj:
                close_res, close_err = connObj.Close()
                utility.execLog("Connection Closing Status :: Result '%s', Error '%s'" % (close_res, close_err))

    def handleEvent(self, reference, action=None, setValue="", setIndex=0, attributeName="", retry=True, retryCount=1, selectBy="VISIBLE_TEXT", returnContent=None, wait_time=TIME, freq=FREQUENCY):
        """
        Perform Action
        """
        wait = WebDriverWait(self.browserObject, wait_time, freq)
        try:
            if retryCount > 1:
                utility.execLog("Spinner Blocked Clicking on Element...Retry Count : %s" % str(retryCount))
            element = wait.until(reference)
            retnValue = element
            if action == "CLEAR":
                element.clear()
            elif action == "SET_TEXT":
                setValue=str(setValue)
                element.send_keys(setValue.decode("utf-8"))
            elif action == "KEY":
                element.send_keys(setValue)
            elif action == "GET_TEXT":
                try:
                    retnValue = str(element.text)
                except:
                    retnValue = str(element.text.encode('ascii', 'ignore'))
            elif action == "GET_ATTRIBUTE_VALUE":
                retnValue = str(element.get_attribute(attributeName))
            elif action == "CLICK":
                element.click()
            elif action == "SELECT":
                if selectBy == "VISIBLE_TEXT":
                    Select(element).select_by_visible_text(setValue)
                elif selectBy == "VALUE":
                    Select(element).select_by_value(setValue)
                elif selectBy == "INDEX":
                    Select(element).select_by_index(setIndex)
                else:
                    retnValue = Select(element).first_selected_option.text
            elif action == "IS_DISPLAYED":
                retnValue = element.is_displayed()
            elif action == "IS_ENABLED":
                retnValue = element.is_enabled()
            elif action == "IS_SELECTED":
                retnValue = element.is_selected()
            elif action == "GET_ELEMENTS_BY_TAG":
                if returnContent:
                    if returnContent == "TEXT":
                        retnValue = []
                        time.sleep(1)
                        elems = element.find_elements_by_tag_name(setValue)
                        for ele in elems:
                            retnValue.append(str(ele.text)) 
                    else:
                        retnValue = element.find_elements_by_tag_name(setValue)
                else:
                    retnValue = element.find_elements_by_tag_name(setValue)
            elif action in ("GET_ELEMENTS_BY_CLASS", "GET_ELEMENTS_BY_XPATH"):
                retnValue = element
            if retryCount > 1:
                utility.execLog("Event Handle was Successful on Retry Count : %s" % str(retryCount))
            return retnValue
        except AssertionError as ae:
            class_names = ["fade loading", "loader-background", "stale element", "fade in loading"]
            for class_name in class_names:
                if class_name in str(ae):
                    wait.until(EC.invisibility_of_element_located((By.XPATH, ".//*[contains(@class, '{}')]".format(class_name))))
                    if retryCount <= 3 and retry:
                        utility.execLog("Spinner Blocked Clicking on Element...Retrying")
                        retryCount += 1
                        return self.handleEvent(reference, action, setValue, attributeName=attributeName, retry=retry, retryCount=retryCount, selectBy=selectBy)
                    else:
                        utility.execLog("Spinner Blocked Clicking on Element...Maximum Retries Exceeded!")
                        utility.execLog("Exception :: %s" % str(ae) + format_exc())
                        raise ae
                else:
                    continue
            utility.execLog("Exception :: %s" % str(ae) + format_exc())
            raise ae
        except WebDriverException as we:
            class_names = ["fade loading", "loader-background", "stale element", "fade in loading"]
            for class_name in class_names:
                if class_name in str(we):
                    wait.until(EC.invisibility_of_element_located((By.XPATH, ".//*[contains(@class, '{}')]".format(class_name))))
                    if retryCount <= 3 and retry:
                        utility.execLog("Spinner Blocked Clicking on Element...Retrying")
                        retryCount += 1
                        return self.handleEvent(reference, action, setValue, attributeName=attributeName, retry=retry, retryCount=retryCount, selectBy=selectBy)
                    else:
                        utility.execLog("Spinner Blocked Clicking on Element...Maximum Retries Exceeded!")
                        utility.execLog("Exception :: %s" % str(we) + format_exc())
                        raise we
                else:
                    continue
            utility.execLog("Exception :: %s" % str(we) + format_exc())
            raise we
        except Exception as ee:
            class_names = ["fade loading", "loader-background", "stale element", "fade in loading"]
            for class_name in class_names:
                if class_name in str(ee):
                    wait.until(EC.invisibility_of_element_located((By.XPATH, ".//*[contains(@class, '{}')]".format(class_name))))
                    if retryCount <= 3 and retry:
                        utility.execLog("Spinner Blocked Clicking on Element...Retrying")
                        retryCount += 1
                        return self.handleEvent(reference, action, setValue, attributeName=attributeName, retry=retry, retryCount=retryCount, selectBy=selectBy)
                    else:
                        utility.execLog("Spinner Blocked Clicking on Element...Maximum Retries Exceeded!")
                        utility.execLog("Exception :: %s" % str(ee) + format_exc())
                        raise ee
                else:
                    continue
            if 'alert-danger' in str(reference.locator):
                utility.execLog("No Error Message was found, locator used: {}".format(reference.locator))
                raise ee
            utility.execLog("Failed to perform Action or Couldn't locate the {}".format(reference.locator))
            utility.execLog("Exception :: %s" % str(ee) + format_exc())
            raise ee

    def waitLoading(self,waitSeconds):
        """
        Wait till Loader is displayed on the page
        INCOMPLETE: Requires update to web-element ID
        """
        countLoop = 0
        loaderSpinnerDisplayed = False
        while countLoop < waitSeconds:
            try:
                loaderSpinnerDisplayed = self.handleEvent(EC.presence_of_element_located((By.ID, 'loader')), action='IS_DISPLAYED')
            except:
                loaderSpinnerDisplayed = False
            if loaderSpinnerDisplayed:
                utility.execLog("Loading Page...Loader spinner is displayed")
                time.sleep(1)
            else:
                utility.execLog("Loading Page Complete")
                break
            countLoop += 1
        if loaderSpinnerDisplayed:
            utility.execLog('Loading Exceeding wait time %s seconds' % waitSeconds)

    def is_element_present(self, locator, value):
        try:
            self.browserObject.find_element(locator, value)
        except NoSuchElementException:
            utility.execLog("Element not found")
            return False
        return True