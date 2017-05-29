"""
Created on Jun 30, 2015

@author: Suman_P
"""

import os
import random
import traceback
from libs.core.dellunit.case import DellTestCase
from libs.core.dellunit.unittest2 import SkipTest
from libs.product import utility
from libs.product import globalVars
from libs.product.pages import Login

logsdir = globalVars.logsDir

reboot_timeout=2700
reboot_sleep=300
wait_time_after_reboot=300
BLOCKED, ERROR, FAILURE, OMITTED, SUCCESS = range(1, 6)

STATUS = {BLOCKED : "BLOCKED", 
          ERROR : "ERROR", 
          FAILURE : "FAILURE", 
          OMITTED : "OMITTED", 
          SUCCESS : "PASSED"}

COLOR = {"PASSED" : "#ffffff",
         "FAILURE" : "#ff9999",
         "ERROR" : "#ff0000", 
         "BLOCKED" : "#ff5999",
         "OMITTED" : "#ddeeff",
         "DESC": " #FE9A2E",
         }

disc_param = utility.disc_param()

STATUS_HTML_HEADER_TMPL = """<html><body><table class="heading_table" border=1> <tr align=center bgcolor=#A4A4A4> <th> Steps </th> <th> Procedure </th> <th> Description </th><th> Status </th></tr>\n""" 
STATUS_HTML_TMPL = """<tr align=center bgcolor=%s> <td> Step %s </td> <td> %s </td> <td> %s </td><td> %s </td></tr>\n"""
STATUS_HTML_DESC_TMPL = """<tr align=center bgcolor=%s> <td colspan=4> %s </td> </tr>\n"""
FINAL_STATUS_HTML_TMPL = """</table>\n\n<br><br><TABLE class="heading_table" border=1><tr align=center bgcolor=%s> <td> Final Status </td> <td> %s </td> <td> %s </td> </tr></TABLE>\n"""


class TestBase(DellTestCase):
    """
    Description:
        Test class to be used as base class for the automation framework. This 
        class is derived from "unittest.TestCase" class.
    
    """
    def __init__(self, *args, **kwargs):
        """
        Description: 
            Initializing an object of this class. 
            passExpectation (Boolean): Indicating expectancy whether the testcase
                is expected to pass(True) or Fail(False)
            resultCode (String): Indicating whether the test case passed, failed,
                omitted, blocked or error
            stepCount (Int): Counter to keep track of the number of steps that
                has been performed by the test case
                
        Input:
            args (Tuple): Variable number of arguments passed as tuple
            kwargs (Dict): Variable number of arguments passed as key-value
                pair
                
        Output:
            None
        
        """
        # Initializing derived classes
        DellTestCase.__init__(self, "test_functionality")
        self.tcID = args[0]        
        self.iteration = utility.iteration
        self._step_results = []
        self.logDesc(args[0])
        self.msg = []
        self.finalStatus = SUCCESS
        self.FAILED = False
        self.passExpectation = True
        self.applianceIP = globalVars.configInfo['Appliance']['ip']
        self.loginUser = globalVars.configInfo['Appliance']['username']
        self.loginPassword = globalVars.configInfo['Appliance']['password']  
        #self.delResource = globalVars.configInfo['Appliance']['del_resource']
        self.browserName = globalVars.configInfo['Appliance']['browser']
        self.browserObject = globalVars.browserObject
        
        self._testMethodDoc = "@testcase: %s\n%s" % (self.tcID, self.__doc__.strip())
        
        execLogLoc = os.path.join(utility.logsdir, "exec_log")
        self.xmlString = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>"""
        self.executionTime = {}
        self.serverGroup = []
        self.runInventoryList = {}
        
        while True:
            if utility.getFileLists(location=execLogLoc, recursion=True, 
                                 filePatt="%s_%s_log.txt" % (self.tcID, self.iteration))[1]:
                self.iteration += 1
            else:
                break
            
        # Setting global tcID and iteration
        utility.set_tc_id(self.tcID)
        utility.set_Iteration(self.iteration)
        
        disc_param["Appliance IP"] = self.applianceIP
        disc_param["Browser Name"] = globalVars.browserName
        if self.browserObject:
            disc_param["Browser Version"] = self.browserObject.capabilities["version"]
       
        
        # Setting Runtime data
        for k, v in disc_param.items():
            self.add_runtime_data(k, v) 
        
    
    def setUp(self):
        """
        Add Log URLs
                
        """
        # Setting global tcID and iteration
        utility.set_tc_id(self.tcID)
        utility.set_Iteration(self.iteration)
               
        # Building execFile location
        execFile = "%s_%s_log.txt" % (utility.tc_id, utility.iteration)
        execFile = os.path.join("exec_log", execFile)
        
        # Building StatusFile location
        statusFile = "%s_%s_log.html" % (utility.tc_id, utility.iteration)
        statusFile = os.path.join("status_log", statusFile)
        
        # Create respective URLs
        execURL = execFile.replace("\\", "/")
        stepsURL = statusFile.replace("\\", "/")
        
        # Adding the log URLs
        self.add_test_url("Execution log", execURL)
        self.add_test_url("Steps", stepsURL)
            
    
    def tearDown(self):
        """
        
        """
        # Generate Status file
        statusFile = os.path.join(utility.logsdir, "status_log", "%s_%s_log.html" % \
                                 (self.tcID, self.iteration))
        with open(statusFile, "w") as fptr:
            fptr.write("<html>\n<head />\n")
            fptr.write("<body>\n")
            fptr.write("<table width='100%' border='1'>\n")
            fptr.write(STATUS_HTML_HEADER_TMPL)
            counter = 1
            for elems in self._step_results:
                if elems["name"].lower() == "logheader":
                    fptr.write(STATUS_HTML_DESC_TMPL % (COLOR["DESC"], 
                                                        elems["description"]))
                    continue
                fptr.write(STATUS_HTML_TMPL % (COLOR.get(elems["result"].upper()), 
                                               counter, elems["name"], 
                                               elems["description"], 
                                               elems["result"].upper()))
                counter += 1
            
            # Writing Final Status of the test case
            color = COLOR[STATUS[self.finalStatus]]
            if self.finalStatus == SUCCESS:
                color = "#ccff99"
                self.msg.append("No Failures")
            fptr.write(FINAL_STATUS_HTML_TMPL % (color, "<br />".join(self.msg), 
                                                 STATUS[self.finalStatus]))
            fptr.write("<a href='javascript:window.close()'>Close</a>\n")
            fptr.write("</body></html>")
        

    def failure(self, msg, nature="", resultCode=FAILURE, raiseExc=False):
        """
        Description:
            Overriding the unittest.TestCase.fail method to capture the failure 
            status(fail, omitted, blocked etc) messages in the HTML format on 
            the fly.
            
        Input:
            nature (String): Type of operation being performed like pre-run setup,
                validation, post-run cleanup etc.
            msg (String): Status message
            resultCode (Int): Result of the operation like fail, blocked etc 
                taken from global variables defined here like FAIELD, BLOCKED, 
                OMITTED etc.
                
        Output:
            None
        
        """
        self.finalStatus = min(self.finalStatus, resultCode)
        resultCode = STATUS[resultCode]
        self.add_step_result(nature, msg, resultCode)
            
        self.FAILED = True
        self.msg.append(msg)
        
        if resultCode.__contains__("OMIT"):
            if raiseExc:
                self.omit(msg)
        elif resultCode.__contains__("BLOCK"):
            if raiseExc:
                self.block(msg)
        else:
            if raiseExc:
                if self.browserObject:
                    try:
                        screenShot = "%s_%s_screenshot.png" % (self.tcID, self.iteration)
                        screenShot = os.path.join("screen_shots",screenShot)
                        self.screenshotpath = os.path.join(utility.logsdir, screenShot)
                        self.browserObject.get_screenshot_as_file(self.screenshotpath)
                        srcShotURL = screenShot.replace("\\", "/")
                        self.add_test_url("screenshot",srcShotURL)
                    except Exception as e:
                        self.add_step_result("", "Web Driver Exception while capturing Screenshot :: Error -> %s"%str(e), resultCode)
                self.fail(msg)   
        
    
    def succeed(self, msg="", nature="", resultCode=SUCCESS):
        """
        Description:
            Method to capture the success status(pass) messages in the HTML 
            format on the fly.
            
        Input:
            nature (String): Type of operation being performed like pre-run setup,
                validation, post-run cleanup etc.
            msg (String): Status message
            resultCode (Int): Result of the successful operation like pass
                taken from global variables defined here SUCCESS.
                
        Output:
            None
        
        """
        resultCode = STATUS[resultCode]
        self.add_step_result(nature, msg, resultCode)
#        if tblib:
#            tblib.tbstatus.send_message(" --> ".join([self.tcID, nature, msg, 
#                                                      resultCode]))
        
        
    def failOnExpectation(self, nature="", msg="", resultCode=FAILURE,
                          raiseExc=False):
        """
        Description:
            Method to capture the status(fail, omitted, blocked etc) messages 
            in the HTML format on the fly in case the test case is expected to 
            have failed. In such cases, failed operations are treated as success
            and successful operations are treated as failure.  
            
        Input:
            nature (String): Type of operation being performed like pre-run setup,
                validation, post-run cleanup etc.
            msg (String): Status message
            resultCode (Int): Result of the operation like fail, blocked etc 
                taken from global variables defined here like FAIELD, BLOCKED, 
                OMITTED etc.
                
        Output:
            None
        
        """
        if self.passExpectation:
            self.failure(nature, msg, resultCode, raiseExc)
        else:
            self.succeed(nature, msg)
            
            
    def succeedOnExpectation(self, nature="", msg="", resultCode=FAILURE,
                             raiseExc=False):
        """
        Description:
            Method to capture the status(fail, omitted, blocked etc) messages 
            in the HTML format on the fly in case the test case is expected to 
            have failed. Ideally, same as succeed method.
            
        Input:
            nature (String): Type of operation being performed like pre-run setup,
                validation, post-run cleanup etc.
            msg (String): Status message
            resultCode (Int): Result of the operation like fail, blocked etc 
                taken from global variables defined here like FAIELD, BLOCKED, 
                OMITTED etc.
                
        Output:
            None
        
        """
        if self.passExpectation:
            self.succeed(nature, msg)
        else:
            self.failure(nature, msg, resultCode, raiseExc)
        
        
    def logDesc(self, description=""):
        """
        Description:
            Special formatting of the status html messages which indicates the 
            nature of operation being performed in the test case.
            
        Input:
            description (String): Header Message
            color (String): HTML hexadecmimal color codes
            
        Output:
            None
        
        """
        description = description.strip()
        if description:
            self.add_step_result("logheader", description, "")
        
    
    def getChoice(self, oldVal, validVal):
        """
        Description:
            Return a random element from validVal list which is not same as 
            the oldVal element.
        
        Input:
            validval (List): A sequence of values from which the new element 
                will be picked in random.
            oldVal (type of element in the list): The value from the 
                sequence which cannot be picked again
        
        Output:
            newVal (type of element in the list): The randomly picked value 
                from the sequence which is not same as the oldVal
        
        """
        newVal = random.choice(validVal)
        if oldVal == newVal:
            newVal = self.getChoice(oldVal, validVal)
            
        return newVal
        
        
    def generateValue(self, old, minVal=1, maxVal=99):
        """
        Description:
            Returns a random number within the given range defined by minVal 
            and maxVal as string which is not same as the value of old
            
        Input:
            old (String): The number in the range which cannot be picked again 
            minVal (Int): Minimum threshold of the range
            maxVal (Int): Maximum threshold of the range
        
        Output:
            newVal (String): The randomly picked value from the range which is
                not same as the old
        
        """
        old = int(old)
        newVal = random.randint(minVal, maxVal)
        if newVal == old:
            newVal = self.generateValue(old)
        
        return str(newVal)
            
    
    @classmethod
    def func_exec(cls, fun):
        """
        Description:
            Decorator to wrap the test_functionality method execution of the 
            test case
             
        Input:
            cls (Classname): Class name of the called function
            func (Functionmane): Name of the called function
         
        Output:
            Returns function handler
         
        """
        def func_handler(*kwarg,**kwargs):
            try:
                fun(*kwarg,**kwargs)
            except AssertionError, ae:
                utility.execLog(traceback.format_exc())
                raise ae 
            except SkipTest, se:
                utility.execLog(traceback.format_exc())
                raise se
            except Exception, e:
                utility.execLog(traceback.format_exc())
                #kwarg[0].finalStatus = min(kwarg[0].finalStatus, ERROR)
                #kwarg[0].msg.append(e.__repr__()) 
                raise e
            return fun
        return func_handler
    
    
    @classmethod
    def autoLogin(cls, fun):
        """
        Description:
            Decorator to check for Appliance Login Status 
        Input:
            cls (Classname): Class name of the called function
            func (Functionmane): Name of the called function
         
        Output:
            Returns function handler
         
        """
        def func_handler(*kwarg,**kwargs):
            try:
                kwarg[0].browserObject = globalVars.browserObject
                userEle = kwarg[0].browserObject.find_element_by_id("txtUsername")
                if not userEle:
                    loginObject = Login.Login(kwarg[0].browserName, kwarg[0].applianceIP)
                    globalVars.loginObject = loginObject
                    globalVars.browserObject = loginObject.loginApp(kwarg[0].loginUser, kwarg[0].loginPassword)
                    kwarg[0].browserObject = globalVars.browserObject
                    kwarg[0].succeed("Logged into ASM Appliance IP:%s"%kwarg[0].applianceIP)
                result = fun(*kwarg,**kwargs)
                return result         
            except AssertionError, ae:
                utility.execLog(traceback.format_exc())
                raise ae 
            except SkipTest, se:
                utility.execLog(traceback.format_exc())
                raise se
            except Exception, e:
                utility.execLog(traceback.format_exc())
                raise
            return fun
        return func_handler
    
    
    
