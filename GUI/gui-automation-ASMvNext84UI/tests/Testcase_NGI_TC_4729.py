""" Author       : Mirlan Kaiyrbaev
Created Date : Jan 13, 2016
Description  :  Check if the created VDs pick disks ordered by disk size.
                Check by creating VDs with different RAID levels and different number of disks
                Swap the order/sequence of the VDs in a second deployment and check if they picks disks ordered by disk size
Test Flow    : 1) Create a Server (Hardware Only) deployment template.
               2) Create 'Internal Virtual Disks' (VDs) using 'Advanced RAID Configuration' under 'Hardware Settings'.
               3) Create First VD using 'First Disks' or 'Last/Rear Disks' Disk Requirements using 'Exactly' # of Disks.
               4) Check selecting 'Minimum' # of Disks for First VD is validated as an Unsupported option.
               5) Check selecting "Non-RAID" RAID Level for First VD is validated as an Unsupported option.
               6) Create Two other VDs with two other RAID levels, any # of disks, any disk requirements.
               7) Repeat the deployment of VDs with a new template where the two VDs order/sequence is swapped.
               8) Deploy the template and verify the expected outcomes listed against each step.

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
        fileName = "Testcase_NGI_TC_4729.json"
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
