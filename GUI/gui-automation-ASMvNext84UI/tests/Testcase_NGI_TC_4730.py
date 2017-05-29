""" Author       : Mirlan Kaiyrbaev
Created Date : Jan 12, 2016
Description  : Install OS on the first VD, using 'HDD/SSD/Any Available', selecting any '# of disks' options.
Test Flow    : 1) Create a Linux (CentOS/RHEL 7.2/latest) Bare Metal OS deployment template.
               2) Create Internal Virtual Disks (VDs) using Advanced RAID Configuration
               3) Create 'First VD' and Install OS on the first VD, using 'HDD/SSD/Any Available', selecting any '# of disks' options.
               4) Create Two other VDs with two other RAID levels, any # of disks, any disk requirements.
               5) Deploy the template and verify the expected outcomes listed against each step.
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
        fileName = "Testcase_NGI_TC_4730.json"
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
        self.serviceName = "Service_" + self.templateName

    def postRunCleanup(self):
        """
        Creating Post Run setup to be executed after running the test case
        """
        self.logDesc("Post Run Cleanup")
        self.deleteService(self.serviceName)
        self.logout()

    def runTestCase(self):
        """
        Running Test Case
        """
        self.logDesc("Running Test Case")

        # Create Template and Publish
        self.buildTemplate(self.components, publishTemplate=True)

        # Deploy Service
        self.deployService(self.templateName, serviceName=self.serviceName)

        # Wait for Deployment to complete
        result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        if len(result) > 0:
            if result[0]["Status"] in ("Success"):
                self.succeed("Successfully Deployed Service '%s'" % self.serviceName)
            else:
                self.failure("Failed to Deploy Service '%s'" % self.serviceName, raiseExc=True)
        else:
            self.failure("Failed to Deploy Service '%s' in expected time" % self.serviceName, raiseExc=True)

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
