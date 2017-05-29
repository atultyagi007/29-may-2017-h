""" Author       : Mirlan Kaiyrbaev
Created Date : Jan 5, 2016
Description  : Check if the created VDs puts the selected disks in Non-RAID mode on PERC.
               Verify the First VD is not allowed to be configured as Non-RAID disks.
Test Flow    : 1) Create a Server (Hardware Only) deployment template.
               2) Create First VD earmarked for OS Installation using any available options.
               3) Check selecting "Non-RAID" RAID Level for First VD is validated as an Unsupported option.
               4) Create a second VD and set the 'RAID Level' to 'Non-RAID'.
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
        fileName = "Testcase_NGI_TC_4731.json"
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
        self.buildTemplate(self.components, publishTemplate=True)
        #Deploy Service
        #=======================================================================
        # self.serviceName = "Service_" + self.templateName
        # self.deployService(self.templateName, serviceName=self.serviceName, retryOnFailure=self.retryOnFailure)
        # #Wait for Deployment to complete
        # """
        # ASM-8050 causes puppet to run every 5 minutes during a deployment.
        # If the networks are being configured, this can lead to an error
        # for the server health status, saying "Could not connect to host".
        # I put this loop in to handle that condition.
        # """
        # attempt = 5
        # while attempt:
        #     result = self.getDeploymentStatus(self.serviceName, deleteStatus=False, timeout=10800, waitTime=300)
        #     if result:
        #         if result[0]["Status"] in ("Success"):
        #             self.succeed("Successfully Deployed Service %s".format(self.serviceName))
        #             break
        #         else:
        #             self.failure("Service %s not showing Success, retrying Result: %s".format(self.serviceName, result))
        #             attempt -= 1
        #     else:
        #         self.failure("Failed to Deploy Service %s in expected time".format(self.serviceName), raiseExc=True)
        #
        #     if not attempt:
        #         self.failure("Failed to Deploy Service %s".format(self.serviceName), raiseExc=True)

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
