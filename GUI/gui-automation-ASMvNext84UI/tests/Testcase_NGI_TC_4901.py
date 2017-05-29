""" Author       : Mirlan Kaiyrbaev
Created Date : Dec 14, 2016
Description  : Provide template level validation to volume names instead of waiting and failing during template deployment because volume name(s) already exists.
Test Flow    : 1) Create volume names on selected storage devices (not specific storage device)
               2) Deploy template
               3) Create a second template and add a same storage resource from first template
               4) Publish Template
"""

from tests.globalImports import *

tc_id = utility.get_tc_data(__file__)


class Testcase(Manager.Manager):
    """
        Verify all information is correct and all links work and point to the correct page
    """

    def __init__(self, *args, **kwargs):
        """
        Initialization
        """
        Manager.Manager.__init__(self, tc_id, *args, **kwargs)

    def preRunSetup(self):
        """
        Creating Pre-requisite Setup for running the test scenario
        """
        self.logDesc("Pre Run Setup")
        # Check for current logged in user
        self.verifyCurrentUser(userRole='Administrator', loginAsUser=True)
        #Read Components from JSON file
        fileName = "Testcase_NGI_TC_4901a.json"
        self.components = self.getTemplateConfiguration(fileName)
        if len(self.components) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName, self.components))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"%(fileName), raiseExc=True)
        # Delete existing Template with same name
        tempList = self.getTemplates(option="My Templates")
        self.templateName = self.components["Template"]["Name"]
        for template in tempList:
            if self.templateName == template["Name"]:
                self.deleteTemplate(self.templateName)
        self.retryOnFailure = self.components["Template"]["retryOnFailure"]

        #Read Components from JSON file
        fileName1 = "Testcase_NGI_TC_4901b.json"
        self.components1 = self.getTemplateConfiguration(fileName1)
        if len(self.components1) > 0:
            self.succeed("Able to read Template Configuration :: '%s' -> Content :: %s"%(fileName1, self.components1))
        else:
            self.failure("Failed to read Template Configuration :: '%s'"% fileName1, raiseExc=True)

        #Delete existing Template with same name
        tempList = self.getTemplates(option="My Templates")
        self.templateName1 = self.components1["Template"]["Name"]
        for template in tempList:
            if self.templateName1 == template["Name"]:
                self.deleteTemplate(self.templateName1)
        self.retryOnFailure = self.components1["Template"]["retryOnFailure"]

        self.serviceName = "Service_" + self.templateName

    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        self.logout()

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")

        # Create Template and Publish
        self.buildTemplate(self.components)

        #Deploy Service
        self.deployService(self.templateName, serviceName=self.serviceName, retryOnFailure=self.retryOnFailure)

        # Wait for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'" % self.serviceName)
            else:
                self.failure("Failed to Deploy Service '%s'" % self.serviceName, raiseExc=True)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time" % self.serviceName, raiseExc=True)

        #Create 2nd Template and Publish
        self.buildTemplate(self.components1)

        self.color_of_device_icon(self.templateName1, "storage", "yellow")

    @BaseClass.TestBase.func_exec
    def test_functionality(self):
        """
        This is the execution starting function
        """

        self.browserObject = globalVars.browserObject

        self.preRunSetup()

        self.runTestCase()

        self.postRunCleanup()

        self.logout()
