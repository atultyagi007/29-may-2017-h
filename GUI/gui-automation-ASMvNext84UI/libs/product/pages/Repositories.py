
"""
Created on Oct 26, 2015

@author: Suman_P
"""

from CommonImports import *
from libs.thirdparty.selenium.common.exceptions import NoSuchElementException
import paramiko
from libs.product.objects.Repositories import RepositoriesObjectRepo
import re


class Repositories(Navigation, RepositoriesObjectRepo):
    """
    Description:
        Class which includes all the operations related to Repository Management

    """
    def __init__(self, browserObject):
        """
        Description:
            Initializing an object of this class.
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Repositories"
        self.loopCount = 5
        utility.execLog("Repositories")
        self.temps = {"OS": [self.RepositoriesObjects('repo_tab'), self.RepositoriesObjects('OS_repos')], "Firmware": [self.RepositoriesObjects('FW_tab'),
                                                                                                                       self.RepositoriesObjects('FW_repos')]}

    def loadPage(self):
        """
        Description:
            API to load Repositories Page
        """
        try:
            utility.execLog("Loading Repositories Page")
            self.browserObject, status, result = self.selectOption("Repositories")
            return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Repositories Page %s"% e.message

    # No usage
    # def validatePageTitle(self, title=None):
    #     """
    #     Description:
    #         API to validate Login Page
    #     """
    #     if not title:
    #         title = self.pageTitle
    #     getCurrentTitle = self.handleEvent(
    #         EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('page_repositories'))),
    #         action="GET_TEXT")
    #     if title not in getCurrentTitle:
    #         utility.execLog("Unable to validate Page Title Actual:'%s' Expected:'%s'" % (getCurrentTitle, title))
    #         return self.browserObject, False, "Unable to validate Page Title Actual:'%s' Expected:'%s'" % (
    #         getCurrentTitle, title)
    #     else:
    #         utility.execLog("Successfully validated Page Title '%s'" % title)
    #         return self.browserObject, True, "Successfully validated Page Title '%s'" % title

    # Not complete
    def getOptions(self):
        """
        Description:
            API to get Options and their Accessibility for Repositories Page
        """
        pass

    def getDetails(self, option="Firmware"):
        """
        Finds data for existing OS, Firmware or Both Repositories
        :param option: accepts 'Firmware', 'OS' or "Both'
        :return: list of data as a dictionaries for each repository
        """

        def get_repo_data(repos, col_num):
            """
            Finds 'State', 'Repositories', 'Image Type', 'Source Path', 'In Use' data for all OS Image Repositories and
            'State', 'Repository Name', 'Source', 'Custom Bundles' for all Firmware/Software Repositories
            :param repos: list of OS or Firmware locators
            :param col_num: 5 for OS and 4 for Firmware, based on number of colons required
            :return: list of data from tables
            """
            repos_data = []
            for repo in repos:
                tds = repo.find_elements_by_xpath("./td")
                td_text = []
                for index, td in enumerate(tds):
                    if index == 0 and col_num == 4:
                        text = td.text
                        text = text.split('\n')
                        if len(text) > 1:
                            td_text.append(text[1])
                            continue
                    if index == col_num:
                        break
                    td_text.append(td.text)
                repos_data.append(td_text)
            return repos_data

        def zipped_data(repos_data):
            """
            Makes a dictionary out of colon names as a key and data from repositories under that colon as a value
            eg. {'In Use': 'False', etc.}
            :param repos_data: list of repository data within list
            :return: list of data as dictionary for each repository
            """
            os_col_names = ['State', 'Repositories', 'Image Type', 'Source Path', 'In Use']
            fw_col_names = ['State', 'Repository Name', 'Source', 'Custom Bundles']

            repo_data = []
            for repo in repos_data:
                if len(repo) == 4:
                    zipped = zip(fw_col_names, repo)
                elif len(repo) == 5:
                    zipped = zip(os_col_names, repo)
                repo_data.append(dict(zipped))
            return repo_data

        try:
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_tab'))), action="CLICK")
            os_repos = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('OS_repos'))))
            os_repos_data = get_repo_data(os_repos, col_num=5)
            utility.execLog("Able to fetch OS Repositories data: {}".format(os_repos_data))
            if option == "OS":
                utility.execLog('Returning: "{}"'.format(zipped_data(os_repos_data)))
                return self.browserObject, True, zipped_data(os_repos_data)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            fw_repos = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('FW_repos'))))
            fw_repos_data = get_repo_data(fw_repos, col_num=4)
            utility.execLog("Able to fetch Firmware Repositories data: {}".format(fw_repos_data))
            if option == "Firmware":
                utility.execLog('Returning: "{}"'.format(zipped_data(fw_repos_data)))
                return self.browserObject, True, zipped_data(fw_repos_data)
            else:
                data = zipped_data(os_repos_data) + zipped_data(fw_repos_data)
                utility.execLog('Returning: "{}"'.format(zipped_data(data)))
                return self.browserObject, True, data
        except Exception as e:
            return self.browserObject, False, "Unable to read Repositories :: Error -> {}".format(e)

    def selectRepo(self, repositoryName, repositoryType="Firmware"):
        """
        Selects(clicks on) specified repository
        :param repositoryType: 'OS' or 'Firmware'
        :return: instance of the browser, boolean and message
        """

        def find_repo_name(repo_name, all_repos):
            """
            Finds repo in OS and Firmware then clicks on it
            :return: False or True
            """
            for repo in all_repos:
                all_names = repo.find_elements_by_xpath("./td[2]")
                for name in all_names:
                    if name.text == repo_name:
                        name.click()
                        return True
            return False

        try:
            utility.execLog('selectRepo()')
            if repositoryType == "OS":
                utility.execLog('repositoryType: "{}"'.format(repositoryType))
                os_repos = self.handleEvent(
                    EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('OS_repos'))))
                utility.execLog("Total Repositories Available in {}: {}".format(repositoryType, len(os_repos)))
                if find_repo_name(repositoryName, os_repos) is False:
                    return self.browserObject, False, "Failed to Select '%s' Repository '%s'" % (
                        repositoryType, repositoryName)
            if repositoryType == "Firmware":
                utility.execLog('repositoryType: "{}"'.format(repositoryType))
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))),
                                 action="CLICK")
                fw_repos = self.handleEvent(
                    EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('FW_repos'))))
                utility.execLog("Total Repositories Available in {}: {}".format(repositoryType, len(fw_repos)))
                if find_repo_name(repositoryName, fw_repos) is False:
                    utility.execLog("Failed to Select '%s' Repository '%s'" % (repositoryType, repositoryName))
                    return self.browserObject, False, "Failed to Select '%s' Repository '%s'" % (repositoryType, repositoryName)
            utility.execLog("Able to Select '%s' Repository '%s'" % (repositoryType, repositoryName))
            return self.browserObject, True, "Able to Select '%s' Repository '%s'" % (repositoryType, repositoryName)
        except Exception as e:
            return self.browserObject, False, "Unable to select '%s' Repository '%s' :: Error -> %s" % (
            repositoryType, repositoryName, str(e))

    def readBundleInformation(self, repositoryName):
        """
        Select specified Repository and View Bundles
        """

        def get_bundle_data(buns):
            """
            Finds 'Name', 'Version', 'Date and Time' data for all bundles
            :param buns: list of bundle elements
            :return: list of data from tables
            """
            b_data = []
            for bundle in buns:
                tds = bundle.find_elements_by_xpath("./td")
                td_text = []
                for index, td in enumerate(tds):
                    if index == 3:
                        break
                    td_text.append(td.text)
                b_data.append(td_text)
            return b_data

        def zipped_data(b_data):
            """
            Matches every data element with 'Name', 'Version', 'Date and Time' as key, and value
            :param b_data: list of bundles data
            :return: list of dictionaries
            """
            col_names = ["Name", "Version", "Date and Time"]
            bundle_data = []
            for bundle in b_data:
                zipped = zip(col_names, bundle)
                bundle_data.append(dict(zipped))
            return bundle_data

        def get_pages():
            """
            Finds how many pages exists
            :return: None if nothing found or number of pages as integer
            """
            try:
                self.handleEvent(EC.text_to_be_present_in_element((By.ID, self.RepositoriesObjects('pager')), "Page"))
                footer = self.handleEvent(EC.presence_of_element_located((By.ID, self.RepositoriesObjects('pager'))),
                                          action="GET_TEXT")
                # filters text to get last number
                pages_num = int(re.search('\d+$', footer).group())
                utility.execLog("Total pages is: {}".format(pages_num))
                return pages_num
            except NoSuchElementException as e:
                pages_num = None
                utility.execLog("Pagination element not found, error message: {}".format(e))
                return pages_num

        try:
            utility.execLog("Selecting Firmware Repository '%s'" % repositoryName)
            self.browserObject, status, result = self.selectRepo(repositoryName)
            if status is False:
                return self.browserObject, False, result
            utility.execLog("Clicking on View Bundles for Repository '%s'" % repositoryName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('view_bundles'))),
                             action="CLICK")
            # wait for page to load
            bundle_page_text = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.RepositoriesObjects('fw_bundle_page_title'))),
                action="GET_TEXT")
            if repositoryName != bundle_page_text:
                return self.browserObject, False, "Failed to verify Repository Name '%s' in View Bundles Page" % repositoryName
            utility.execLog("Reading Bundle Information")
            # finds bundles on first page
            total_bundles = []
            bundles_on_a_page = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects("bundles"))))
            total_bundles += bundles_on_a_page
            # finds data on first page
            bundles_data = []
            bundles_data += get_bundle_data(bundles_on_a_page)
            # checks if there is more than 1 page
            pages = get_pages()
            if pages is not None:
                page = 1
                while page < pages:
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('next_button'))),
                                     action="CLICK")
                    bundles = self.handleEvent(
                        EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects("bundles"))))
                    total_bundles += bundles
                    bundles_data += get_bundle_data(bundles)
                    page += 1
            utility.execLog("Total bundles number is: {}".format(len(total_bundles)))
            # matches every data element with 'Name', 'Version', 'Date and Time' as key, and value
            data = zipped_data(bundles_data)
            # closes bundles page
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close_bundles'))),
                             action="CLICK")
            return self.browserObject, True, data
        except Exception as e:
            return self.browserObject, False, "Unable to read Bundles :: Error -> %s" % str(e)

    def deleteRepository(self, repositoryName, repositoryType="Firmware"):
        """
        Deletes existing Repository
        """
        try:
            # getting all repos in "Firmware"
            if repositoryType == "Firmware":
                utility.execLog("Navigating to Firmware Tab")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
                utility.execLog("Getting Firmware Repositories")
                repos = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('FW_repos'))))
                # selecting 'Available Actions' by repository name
                for repo in repos:
                    repo_name = repo.find_element_by_xpath('./td[2]').text
                    if repositoryName == repo_name:
                        available_actions = repo.find_element_by_xpath('./td[5]/select')
                        break
            # getting all repos in "OS"
            elif repositoryType == "OS":
                utility.execLog("Getting OS Repositories")
                repos = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('OS_repos'))))
                # selecting 'Available Actions' by repository name
                for repo in repos:
                    repo_name = repo.find_element_by_xpath('./td[2]').text
                    if repositoryName == repo_name:
                        available_actions = repo.find_element_by_xpath('./td[6]/select')
                        break

            utility.execLog("Deleting '{}' Repository".format(repositoryName))
            try:
                Select(available_actions).select_by_visible_text('Delete')
            except Exception as e:
                return self.browserObject, False, "Firmware Repository %s cannot be deleted, error: %s" % repositoryName, e

            utility.execLog("Confirming to Delete Repository '%s'" % repositoryName)
            if repositoryType == "Firmware":
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btnConfirm'))), action="CLICK")
            elif repositoryType == "OS":
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_save'))), action="CLICK")
            time.sleep(1)
            utility.execLog('Waiting for spinning wheel to be gone')
            self.handleEvent(EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))))
            utility.execLog('Spinning wheel is gone now')
            try:
                utility.execLog('Checking for presence of error message')
                eleError = self.handleEvent(EC.presence_of_element_located((By.CLASS_NAME, "clarity-error")), retry=False, wait_time=10)
                errorMessage = eleError.find_element_by_tag_name("h3").text
                return self.browserObject, False, "Failed to Initiate Delete Repository :: '%s' :: Error -> '%s'" % (repositoryName, str(errorMessage))
            except:
                utility.execLog('No error message found')
                utility.execLog("Successfully Initiated Delete Repository '%s'" % repositoryName)
                return self.browserObject, True, "Successfully Initiated Delete Repository '%s'" % repositoryName
        except Exception as e:
            utility.execLog("Failed to Initiate Delete Repository :: '%s' :: Error -> %s" % (repositoryName, str(e)))
            return self.browserObject, False, "Failed to Initiate Delete Repository :: '%s' :: Error -> %s" % (repositoryName, str(e))

    def addRepository(self, addType, repoPath="", repoUser="", repoPassword="", defaultRepository=True,
                      onlyTestConnection=False):
        """
        Adds a New Repository
        """
        try:
            utility.execLog("Navigating to Firmware Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            utility.execLog("Clicking on 'Add Repository'")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('Add_FW'))), action="CLICK")
            utility.execLog("Verifying 'Add Firmware Repository' Page")
            pageTitle = self.handleEvent(
                EC.element_to_be_clickable((By.CLASS_NAME, self.RepositoriesObjects('modal_title'))), action="GET_TEXT")
            if "Add Firmware / Software Repository" not in pageTitle:
                return self.browserObject, False, "Failed to verify 'Add Firmware Repository' Page Actual:'%s' Expected:'%s'" % (
                pageTitle, "Add Firmware / Software Repository")
            utility.execLog("Verified 'Add Firmware Repository' Page Actual:'%s' Expected:'%s'" % (
            pageTitle, "Add Firmware / Software Repository"))
            if addType == "Network Path":
                utility.execLog("Selecting Network Path Option")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_local'))),
                                 action="CLICK")
                utility.execLog("Setting Network Path '%s'" % repoPath)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('local_file_path'))),
                                 action="SET_TEXT", setValue=repoPath)
                if repoUser:
                    utility.execLog("Setting Network Username '%s'" % repoUser)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('local_UN'))),
                                     action="SET_TEXT", setValue=repoUser)
                if repoPassword:
                    utility.execLog("Setting Network Password '%s'" % repoPassword)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('password'))),
                                     action="SET_TEXT", setValue=repoPassword)
            elif addType == "Local Drive":
                utility.execLog("Selecting Local Drive Option")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_file'))),
                                 action="CLICK")
                utility.execLog("Setting Local Drive Path '%s'" % repoPath)
                self.handleEvent(EC.presence_of_element_located((By.ID, self.RepositoriesObjects('file_browse_path'))),
                                 action="SET_TEXT", setValue=repoPath)
            else:
                utility.execLog("Selecting 'Import From Dell FTP' Option")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_FTP'))),
                                 action="CLICK")
            if defaultRepository:
                utility.execLog("Selecting 'Make Default Repository' Option")
                if not self.handleEvent(
                        EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('set_catalog_default'))),
                        action="IS_SELECTED"):
                    self.handleEvent(
                        EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('set_catalog_default'))),
                        action="CLICK")
            else:
                utility.execLog("Unselect 'Make Default Repository' Option")
                if self.handleEvent(
                        EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('set_catalog_default'))),
                        action="IS_SELECTED"):
                    self.handleEvent(
                        EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('set_catalog_default'))),
                        action="CLICK")
            if onlyTestConnection and addType != "Local Drive":
                utility.execLog("Selecting 'Test Connection' Option")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_TC'))), action="CLICK")
                time.sleep(1)
                try:
                    utility.execLog("Checking for presence of error message")
                    eleError = self.handleEvent(
                        EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))), wait_time=5,
                        retry=False)
                    errorMessage = eleError.find_element_by_tag_name("ul").text
                    utility.execLog(errorMessage)
                    return self.browserObject, False, "Failed to Initiate Test Connection for Repository :: Error -> '%s'" % (
                        str(errorMessage))
                except:
                    utility.execLog("No error message found")
                    utility.execLog("Identifying Confirm Dialog box")
                    self.handleEvent(EC.text_to_be_present_in_element((By.XPATH, self.RepositoriesObjects('dialog_box')), "Success"))
                    utility.execLog("Confirming Test Connection")
                    self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('btn_close'))),
                                     action="CLICK")
                    utility.execLog("Test of connection was successful")
                    return self.browserObject, True, "Successfully Initiated Test Connection for Repository"
            else:
                utility.execLog("Clicking on 'Save' to Add Repository")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_save'))),
                                 action="CLICK")
                time.sleep(1)
                self.handleEvent(EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('loading_in'))))
                utility.execLog("Clicked on 'Save' to Add Repository")
                try:
                    utility.execLog("Checking for presence of error message")
                    eleError = self.handleEvent(
                        EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))), wait_time=5,
                        retry=False)
                    errorMessage = eleError.find_element_by_tag_name("ul").text
                    return self.browserObject, False, "Failed to Initiate Add Repository :: '%s' :: Error -> '%s'" % (
                           repoPath, str(errorMessage))
                except:
                    utility.execLog("No error message found")
                    return self.browserObject, True, "Successfully Initiated Add Repository '%s'" % repoPath
        except Exception as e:
            utility.execLog("Failed to Initiate Add Repository :: '%s' :: Error -> %s" % (repoPath, str(e)))
            return self.browserObject, False, "Failed to Initiate Add Repository :: '%s' :: Error -> %s" % (
                   repoPath, str(e))

    def setDefaultRepository(self, repositoryName):
        """
        Sets as Default Repository
        """
        try:
            utility.execLog("Navigating to Firmware Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            utility.execLog("Selecting Firmware Repository '%s' to make it as Default" % repositoryName)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('default_FW'))),
                             action="SELECT", setValue=repositoryName)
            self.handleEvent(EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('loading_in'))))
            try:
                utility.execLog("Checking for presence of error message")
                eleError = self.handleEvent(
                    EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                    retry=False)
                errorMessage = eleError.find_element_by_tag_name("ul").text
                return self.browserObject, False, "Failed to Set Default Repository :: '%s' :: Error -> '%s'" % (
                repositoryName,
                str(errorMessage))
            except:
                utility.execLog("Error message not found")
                return self.browserObject, True, "Successfully Set Default Repository '%s'" % repositoryName
        except Exception as e:
            return self.browserObject, False, "Failed to Set Default Repository :: '%s' :: Error -> %s" % (
                   repositoryName, str(e))

    def getDefaultRepository(self):
        """
        Gets Default Repository
        """
        repositoryName = "Select"
        try:
            utility.execLog("Navigating to Firmware Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            utility.execLog("Fetching the Default Repository")
            time.sleep(1)
            repositoryName = self.handleEvent(
                EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('default_FW'))), action="SELECT",
                selectBy="FIRST_OPTION")
            try:
                utility.execLog("Checking for presence of error message")
                eleError = self.handleEvent(
                    EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))), wait_time=5, retry=False)
                errorMessage = eleError.find_element_by_tag_name("ul").text
                return self.browserObject, False, "Failed to fetch Default Repository :: '%s' :: Error -> '%s'" % (
                repositoryName, str(errorMessage))
            except:
                utility.execLog("Able to fetch Default Repository :: '%s'" % repositoryName)
                return self.browserObject, True, repositoryName
        except StaleElementReferenceException as se:
            utility.execLog("Repositories Page reloaded '%s'" % str(se))
            return self.getDefaultRepository()
        except AssertionError as ae:
            utility.execLog("Repositories Page reloaded '%s'" % str(ae))
            return self.getDefaultRepository()
        except Exception as e:
            return self.browserObject, False, "Exception while trying to fetch Default Repository :: '%s' :: Error -> %s" % (repositoryName, str(e))

    # ===========================================================================
    # def deleteResource(self, resourceName):
    #     """
    #     Deletes existing Resource
    #     """
    #     try:
    #         time.sleep(2)
    #         self.browserObject, status, result = self.selectResource(resourceName)
    #         if not status:
    #             return self.browserObject, False, result
    #         eleResource = self.browserObject.find_element_by_id("removeDeviceLink")
    #         eleResource.click()
    #         time.sleep(3)
    #         utility.execLog("Identifying Confirm Dialog box")
    #         eleDialog = self.browserObject.find_element_by_id("confirm_modal_form")
    #         eleDialog.click()
    #         time.sleep(3)
    #         utility.execLog("Confirming to Delete Resource '%s'"%resourceName)
    #         eleDialog.find_element_by_id("submit_confirm_form").click()
    #         time.sleep(3)
    #         return self.browserObject, True, "Successfully Deleted Resource '%s'"%resourceName
    #         #===================================================================
    #         # try:
    #         #     eleError = self.browserObject.find_element_by_class_name("clarity-error")
    #         #     errorMessage = eleError.find_element_by_tag_name("h3").text
    #         #     discoverResourcePage.find_element_by_id("cancel_resource_form").click()
    #         #     time.sleep(2)
    #         #     return self.browserObject, False, "Failed to Create Resource :: '%s' with VLAN :: '%s' :: Error -> '%s'"%(resourceName,
    #         #                                         resourceVLAN, str(errorMessage))
    #         # except:
    #         #     return self.browserObject, True, "Successfully Created Resource:: '%s' with VLAN :: '%s'"%(resourceName, resourceVLAN)
    #         #===================================================================
    #     except Exception as e:
    #         return self.browserObject, False, "Failed to Delete Resource :: '%s' :: Error -> %s"%(resourceName,
    #                                                 str(e))
    # ===========================================================================

    # 'Read only' doesnt have an access to Repositories anymore, so this function will never get chance to be executed
    # def verify_firmwarepage(self):
    #
    #     self.browserObject.find_element_by_id("tabFirmware").click()
    #     utility.execLog("Selecting Firmware Repository")
    #     time.sleep(10)
    #     add_button = self.browserObject.find_element_by_id("new_firmwarepackage_link")
    #     time.sleep(15)
    #     delete_button = self.browserObject.find_element_by_xpath(
    #         "(//*[contains(@id,'settingsfw_ddlAvailableActions')])[1]")
    #     if ((add_button != "true") and (delete_button != "true")):
    #         utility.execLog(" Verified Add and Delete button is disabled for read only user")
    #         return "true"
    #
    #     else:
    #         utility.execLog("Test Case failed")
    #         return "false"

    def getDetails_CustomBundles(self):
        """
        Description:
            API to get Repositories custom bundles
        """
        optionDict = {}
        try:
            utility.execLog("Open Repositories & Select firmware Custom Bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('add_custom_bundle'))),
                             action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_name'))),
                             action="SET_TEXT", setValue="S6000Bundle")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_description'))),
                             action="SET_TEXT", setValue="Dell Switch")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_type'))),
                             action="SELECT", setValue="Switch")
            utility.execLog("selecting Switch components...")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_model'))),
                             action="SELECT", setValue="Dell Networking S4810/S4820")
            utility.execLog("Selecting device model number ..")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_version'))),
                             action="SET_TEXT", setValue="9.7(0.0)")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('criticality'))),
                             action="SELECT", setValue="Recommended")
            utility.execLog("Select Criticality values from Dropdown")

            file_path = os.path.abspath("docs\\FTOS-SE-9.7.0.0.bin")
            #             file_path = os.path.abspath("\\10.255.7.219\SELab\LAB\Firmware\Force10\S4810\9.7.0.0\\FTOS-SI-9.7.0.0.bin")
            utility.execLog("File path: {}".format(file_path))
            new_file_path = file_path.replace("\\", "\\\\")
            utility.execLog("New path: {}".format(new_file_path))
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
                             action="SET_TEXT", setValue=new_file_path)

            utility.execLog("Upload Custom Bundles :: %s" % optionDict)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('fw_bundle_save'))),
                             action="CLICK")

            return self.browserObject, True, optionDict
        except Exception as e:
            utility.execLog("Exception generated hence closing the modal window.")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('close'))),
                             action="CLICK")
            return self.browserObject, False, "Unable to upload custom bundles  :: Error -> %s" % str(e)

    def getCustomBundle_DeleteCustom(self):
        """
        Description:
            API to check exist or add Repositories custom bundles
        """

        try:
            utility.execLog("Open Repositories & Select firmware Custom Bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('add_custom_bundle'))),
                             action="CLICK")

            odate = time.strftime("%H:%M:%S")
            actBundle = "S4810Bundle" + str(odate)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_name'))),
                             action="SET_TEXT", setValue=actBundle)
            utility.execLog(actBundle)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_description'))),
                             action="SET_TEXT", setValue="Dell Switch")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_type'))),
                             action="SELECT", setValue="Switch")
            utility.execLog("selecting Switch components...")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_model'))),
                             action="SELECT", setValue="Dell Networking S4810/S4820")
            utility.execLog("Selecting device model number ..")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_version'))),
                             action="SET_TEXT", setValue="9.7(0.0)")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('criticality'))),
                             action="SELECT", setValue="Recommended")
            utility.execLog("Select Criticality values from Dropdown")

            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
                             action="CLEAR")
            file_path = os.path.abspath("docs\\FTOS-SE-9.7.0.0.bin")
            #             file_path = os.path.abspath("\\10.255.7.219\SELab\LAB\Firmware\Force10\S4810\9.7.0.0\\FTOS-SI-9.7.0.0.bin")
            utility.execLog("File path: {}".format(file_path))
            new_file_path = file_path.replace("\\", "\\\\")
            utility.execLog("New File path: {}".format(new_file_path))
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
                             action="SET_TEXT", setValue=new_file_path)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('fw_bundle_save'))),
                            action="CLICK")
            time.sleep(1)
            self.handleEvent(EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))), wait_time=90)
            utility.execLog("Bundle saved...")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('view_bundles'))),
                             action="CLICK")
            utility.execLog("View firmware packages")

            try:
                utility.execLog("Looking for Custom bundles tables")
                c_bundles = self.handleEvent(
                    EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('user_bundles'))))
                utility.execLog("Number of custom bundles is: {}".format(len(c_bundles)))
                for bundle in c_bundles:
                    bundle_text = bundle.text
                    bundle_text_list = bundle_text.split(' ')
                    if actBundle == bundle_text_list[0]:
                        utility.execLog('Removing: "{}"'.format(bundle_text_list[0]))
                        time.sleep(1)
                        bundle.find_element_by_xpath(self.RepositoriesObjects('lnkRemoveUserBundle')).click()
                        break
                utility.execLog("Clicked on 'Remove' button")
                time.sleep(1)
                self.handleEvent(
                    EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))), wait_time=90)
                c_bundles_new = self.handleEvent(
                    EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('user_bundles'))))
                utility.execLog("Number of custom bundles is: {}".format(len(c_bundles_new)))
                if len(c_bundles) - 1 == len(c_bundles_new):
                    utility.execLog("'{}' was successfully deleted".format(actBundle))

            except:
                utility.execLog("Unable to find user bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close_bundles'))),
                             action="CLICK")
            utility.execLog("Successfully Closed Page bundles page")

            return self.browserObject, True, "Test case verified"
        except Exception as e:
            return self.browserObject, False, "Unable to upload custom bundles  :: Error -> %s" % str(e)

    def getCustomBundle_CompilaneReport(self):
        """
        Description:
            API to check exist or add Repositories custom bundles
        """

        try:
            utility.execLog("Open Repositories & Select firmware Custom Bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('add_custom_bundle'))),
                             action="CLICK")

            odate = time.strftime("%H:%M:%S")
            actBundle = "S4810Bundle" + str(odate)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_name'))),
                             action="SET_TEXT", setValue=actBundle)
            utility.execLog(actBundle)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_description'))),
                             action="SET_TEXT", setValue="Dell Switch")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_type'))),
                             action="SELECT", setValue="Switch")
            utility.execLog("selecting Switch components...")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_model'))),
                             action="SELECT", setValue="Dell Networking S4810/S4820")
            utility.execLog("Selecting device model number ..")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_version'))),
                             action="SET_TEXT", setValue="9.7(0.0)")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('criticality'))),
                             action="SELECT", setValue="Recommended")
            utility.execLog("Select Criticality values from Dropdown")

            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
                             action="CLEAR")
            file_path = os.path.abspath("docs\\FTOS-SE-9.7.0.0.bin")
            #             file_path = os.path.abspath("\\10.255.7.219\SELab\LAB\Firmware\Force10\S4810\9.7.0.0\\FTOS-SI-9.7.0.0.bin")
            utility.execLog("File path:")
            utility.execLog(file_path)
            new_file_path = file_path.replace("\\", "\\\\")
            utility.execLog(new_file_path)
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
                             action="SET_TEXT", setValue=new_file_path)
            utility.execLog("Upload Custom Bundles :: ")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('fw_bundle_save'))),
                                 action="CLICK")
            time.sleep(1)
            self.handleEvent(
                EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))),
                wait_time=90)
            utility.execLog("saved...")

            return self.browserObject, True, "Test case verified"
        except Exception as e:
            return self.browserObject, False, "Unable to upload custom bundles  :: Error -> %s" % str(e)

            #     def addFirmwareRepository(self, option, networkPath, localPath, makeDefault, testConnection, waitAvailable, verifyCopying= False):
            #         try:
            #             utility.execLog("Open Repositories & Select firmware")
            #             time.sleep(2)
            #             self.browserObject.find_element_by_id("tabFirmware").click()
            #             time.sleep(5)
            #             self.browserObject.find_element_by_id("new_firmwarepackage_link").click()
            #             time.sleep(2)
            #             utility.execLog("%s option"%option)
            #             makeDefaultSelected=self.browserObject.find_element_by_id("makeDefault")
            #             if makeDefault:
            #                 if not makeDefaultSelected.is_selected():
            #                     makeDefaultSelected.click()
            #             else:
            #                 if makeDefaultSelected.is_selected():
            #                     makeDefaultSelected.click()
            #             if option == "defaultftp":
            #                 self.browserObject.find_element_by_id("getLatestFromDell").click()
            #
            #             elif option == "networkPath":
            #                 utility.execLog("selecting %s option"%option)
            #                 self.browserObject.find_element_by_id("networkpath").click()
            #                 filePath = self.browserObject.find_element_by_id("filepath")
            #                 utility.execLog("path :%s"%networkPath)
            #                 filePath.send_keys(networkPath)
            #
            #             elif option == "localPath":
            #                 try:
            #                     utility.execLog("selecting %s option"%option)
            #                     self.browserObject.find_element_by_id("createfromfile").click()
            #                     utility.execLog("path :%s"%localPath)
            #                     self.browserObject.find_element_by_css_selector('input[type="file"]').clear()
            #                     self.browserObject.find_element_by_css_selector('input[type="file"]').send_keys(localPath)
            #                     time.sleep(5)
            #                 except Exception as e:
            #                     utility.execLog("Error uploading from local path %s"%e)
            #
            #             if testConnection:
            #                 self.browserObject.find_element_by_id("btnTestFWConnection").click()
            #                 time.sleep(3)
            #                 try:
            #                     message = self.browserObject.find_element_by_xpath(".//*[@id='page_create_firmwarepackage']/div/h3").text
            #                     utility.execLog("Error Test Connection: %s"%message)
            #                     time.sleep(2)
            #                     return self.browserObject, False, "Unable to upload Firmware :: Error -> %s"%str(message)
            #                 except:
            #                     self.browserObject.find_element_by_id("ok_confirm_form").click()
            #                     time.sleep(2)
            #             self.handleEvent(EC.element_to_be_clickable((By.ID, "firmwarepackageSubmit")), action="CLICK")
            #             time.sleep(2)
            #             waitLoadingPage=10
            #             countLoop=0
            #             while countLoop<waitLoadingPage:
            #                 loaderWaitDisplayed= self.handleEvent(EC.presence_of_element_located((By.ID, 'loader')), action='IS_DISPLAYED')
            #                 if loaderWaitDisplayed:
            #                     utility.execLog("Loading Page after clicking save button")
            #                     time.sleep(30)
            #                 else:
            #                     utility.execLog("Loading Page Complete")
            #                     break
            #                 countLoop+=1
            #             try:
            #                 xpath=".//*[@id='page_create_firmwarepackage']/div/h3"
            #                 message = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
            #                 utility.execLog("Error on Save: %s"%message)
            #                 time.sleep(5)
            #                 utility.execLog("Error Message on Save ---%s"%message)
            #                 return self.browserObject, False, "Unable to upload Firmware :: Error -> %s"%message
            #             except:
            #                 utility.execLog("Firmware loading initiated")
            #                 time.sleep(30)
            #
            #
            #             if waitAvailable:
            #                 try:
            #                     table = self.browserObject.find_element_by_id("firmwarepackageTable")
            #                     tBody = table.find_element_by_tag_name("tbody")
            #                     tr = tBody.find_elements_by_tag_name("tr")
            #                     for rowindex in xrange(0, len(tr)):
            #                         source = tr[rowindex].find_element_by_xpath("./td[3]/span").text
            #                         utility.execLog("selected row %s" %source)
            #                         if option == "defaultftp":
            #                             if source == "ftp://ftp.dell.com/catalog/ASMCatalog.cab":
            #                                 utility.execLog("selected row")
            #                                 break
            #                         elif option == "networkPath":
            #                             if source[:-3] in networkPath:
            #                                 utility.execLog("selected row")
            #                                 break
            #                         elif option == "localPath":
            #                             if source == "Disk":
            #                                 utility.execLog("selected row")
            #                                 break
            #                     copyingInitiated= False
            #                     state=None
            #                     loopCounter =0
            #                     while loopCounter<200:
            #                         try:
            #                             state= tr[rowindex].find_element_by_xpath("./td[1]/span[2]").text
            #                             utility.execLog("state : %s"%state)
            #                             if state == "Copying":
            #                                 utility.execLog("state : %s"%state)
            #                                 copyingInitiated=True
            #                                 if verifyCopying:
            #                                     break
            #                                 time.sleep(60)
            #                             elif state == "Error":
            #                                 utility.execLog("state : %s"%state)
            #                                 return self.browserObject, False, "Firmware in Error state"
            #                             elif state == "Available":
            #                                 utility.execLog("state : %s"%state)
            #                                 time.sleep(10)
            #                                 break
            #                         except Exception as e:
            #                             utility.execLog("Error : %s"%str(e))
            #                             self.browserObject.refresh()
            #                             time.sleep(10)
            #                             self.browserObject.find_element_by_id("tabFirmware").click()
            #                             time.sleep(5)
            #                             table = self.browserObject.find_element_by_id("firmwarepackageTable")
            #                             tBody = table.find_element_by_tag_name("tbody")
            #                             tr = tBody.find_elements_by_tag_name("tr")
            #                             for rowindex in xrange(0, len(tr)):
            #                                 source = tr[rowindex].find_element_by_xpath("./td[3]/span").text
            #                                 utility.execLog("selected row %s" %source)
            #                                 if option == "defaultftp":
            #                                     if source == "ftp://ftp.dell.com/catalog/ASMCatalog.cab":
            #                                         utility.execLog("selected row")
            #                                         break
            #                                 elif option == "networkPath":
            #                                     if source[:-3] in networkPath:
            #                                         utility.execLog("selected row")
            #                                         break
            #                                 elif option == "localPath":
            #                                     if source == "Disk":
            #                                         utility.execLog("selected row")
            #                                         break
            #                         finally:
            #                             loopCounter+=1
            #                 except Exception as e:
            #                     utility.execLog("not saved...%s"%str(e))
            #                     time.sleep(25)
            # #                 if state!="Available":
            #                 #Verifying Copying state
            #                 if verifyCopying:
            #                     if copyingInitiated:
            #                         return self.browserObject, True, "Copying State displayed after Add Catalog initated"
            #                     else:
            #                         return self.browserObject, False, "Copying State not displayed after Add Catalog"
            #                 #Verifying Available State
            #                 if loopCounter==200 and state!="Available":
            #                     utility.execLog("download exceeding time limit..Terminating test case")
            #                     return self.browserObject, False,"download exceeding time limit..Terminating test case.. Current State%s"%state
            #             return self.browserObject, True,"Successfully downloaded Catalog"
            #         except Exception as e:
            #             time.sleep(10)
            #             return self.browserObject, False, "Unable to upload Firmware  :: Error -> %s"%str(e)

    def getTestConnection(self, option, networkPath=None, localPath=None):
        utility.execLog("Open Repositories & Select firmware")
        self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
        self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('Add_FW'))), action="CLICK")
        utility.execLog("%s option" % option)
        try:
            if option == "defaultftp":
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_FTP'))),
                                 action="CLICK")

            elif option == "networkPath":
                utility.execLog("selecting %s option" % option)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_local'))),
                                 action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('local_file_path'))),
                                 action="SET_TEXT", setValue=networkPath)
                utility.execLog("path :%s" % networkPath)

                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_TC'))), action="CLICK")
                #             status = self.browserObject.find_element_by_xpath(".//*[@id='confirm_modal_form']/header/h1/span[2]").text
                #             if status == "Success":
                #                 utility.execLog("Test Connection Successfull")
                self.handleEvent(
                    EC.text_to_be_present_in_element((By.XPATH, self.RepositoriesObjects('dialog_box')), "Success"))
                utility.execLog("Confirming Test Connection")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('btn_close'))),
                                 action="CLICK")
                utility.execLog("Closing 'Add Firmware / Software Repository'")
                time.sleep(1)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_cancel'))),
                                 action="CLICK")
                time.sleep(1)
                self.handleEvent(
                    EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))),
                    wait_time=10)

            return self.browserObject, True, "Test connection successfull"
        except:
            message = self.handleEvent(
                EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                action="GET_TEXT")
            return self.browserObject, False, "Test Connection failed :: Error -> %s" % str(message)

    def verifyDefaultFirmware(self, repositoryName):
        try:
            self.browserObject, status, result = self.getDefaultRepository()
            if not status:
                return self.browserObject, False, result
            else:
                defaultRepoName = result
            if repositoryName == defaultRepoName:
                return self.browserObject, True, "Default Repository Set to %s" % repositoryName
            else:
                return self.browserObject, False, 'Default Repositoty not set to %s Default Firmware Repo ::%s' % (
                repositoryName, defaultRepoName)
        except Exception as e:
            return self.browserObject, False, "Error Fetching Default repo info ::%s" % e
            #             utility.execLog("Open Repositories & Select firmware")
            #             time.sleep(2)
            #             self.browserObject.find_element_by_id("tabFirmware").click()
            #             time.sleep(5)
            #             table = self.browserObject.find_element_by_id("firmwarepackageTable")
            #             tBody = table.find_element_by_tag_name("tbody")
            #             tr = tBody.find_elements_by_tag_name("tr")
            #             for rowindex in xrange(0, len(tr)):
            #                 source = tr[rowindex].find_element_by_xpath("./td[3]/span").text
            #                 utility.execLog("selected row %s" %source)
            #                 if pathType == "defaultftp":
            #                     if source == "ftp://ftp.dell.com/catalog/ASMCatalog.cab":
            #                         utility.execLog("selected row")
            #                         break
            #                 elif pathType == "networkPath":
            #                     if networkPath in source:
            #                         utility.execLog("selected row")
            #                         break
            #                 elif pathType == "localPath":
            #                     if source == "Disk":
            #                         utility.execLog("selected row")
            #                         break
            #
            #             firmware =  tr[rowindex].find_element_by_xpath("./td[3]/span").text
            #             try:
            #                 select=Select(self.browserObject.find_element_by_id("ddlDefaultFirmware"))
            #                 defaultFirmware = select.first_selected_option
            #                 if (defaultFirmware.text in firmware) or (firmware in defaultFirmware.text):
            #                     return self.browserObject, True, "%s Verified as default firmware"%firmware
            #                 else:
            #                     return self.browserObject, False, "%s not verified as default firmware"%firmware
            #             except Exception as e:
            #                 utility.execLog("Error %s"%e)
            #                 return self.browserObject, False, "%s not the default firmware %s"%(firmware,e)

            # except Exception as e:
            #     return self.browserObject, False, "Error : %s"%e

    def setDefaultFirmware(self, firmware):
        try:
            utility.execLog("Open Repositories & Select firmware")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('default_FW'))),
                                 action="SELECT", setValue=firmware)
                time.sleep(1)
                self.handleEvent(
                    EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))))
                return self.browserObject, True, "firmware selected as default "

            except Exception as e:
                utility.execLog("Error %s" % e)
                return self.browserObject, False, "%s cannot be made default firmware" % firmware

        except Exception as e:
            utility.execLog("Error %s" % e)
            return self.browserObject, False, "firmware not selected as default "

    def addStorageCustomBunddle(self, BunddleName, BunddleModel, BunddleVersion):
        try:
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            firmware = self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('default_FW'))),
                                        action="SELECT", selectBy="")
            if firmware == "Select":
                utility.execLog("No Default firmware  set")
                return self.browserObject, False, "No Default firmware set"
            tr = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('FW_repos'))))
            # Select default firmware
            for row in xrange(0, len(tr)):
                if firmware == tr[row].find_element_by_xpath("./td[2]").text:
                    tr[row].find_element_by_xpath("./td[2]").click()
                    break
            # click on add bundle
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('add_custom_bundle'))),
                             action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_name'))),
                             action="SET_TEXT", setValue=BunddleName + "_" + BunddleModel)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_description'))),
                             action="SET_TEXT", setValue="Bunddle " + BunddleName + "_" + BunddleModel)
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_type'))),
                                 action="SELECT", setValue="Storage")
            except Exception as e:
                utility.execLog("Error %s" % e)
                return self.browserObject, False, "device type : Storage not available"
            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_model'))),
                                 action="SELECT", setValue=BunddleModel)
            except Exception as e:
                utility.execLog("Error %s" % e)
                return self.browserObject, False, "device model : %s not available" % BunddleModel

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('bundle_version'))),
                             action="SET_TEXT", setValue=BunddleVersion)
            # Save Bundle
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('fw_bundle_save'))),
                             action="CLICK")
            time.sleep(1)
            self.handleEvent(
                EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))), wait_time=90)
            try:
                errorMessage = self.handleEvent(
                    EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                    action="GET_TEXT", wait_time=10)
                return self.browserObject, False, errorMessage
            except:
                utility.execLog("uploading custom bundle")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('view_bundles'))),
                             action="CLICK")
            trow = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('user_bundles'))))

            for row in xrange(0, len(trow)):
                name = trow[row].find_element_by_xpath("./td[1]").text
                version = trow[row].find_element_by_xpath("./td[2]").text
                if name == BunddleName + "_" + BunddleModel and version == BunddleVersion:
                    bundleFound = True
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('close'))),
                             action="CLICK")
            if bundleFound:
                utility.execLog("Bunddle Created %s" % BunddleName)
                return self.browserObject, True, "%s Bunddle created succesfully" % BunddleName
            else:
                return self.browserObject, False, "%s Bunddle not created" % BunddleName

        except Exception as e:
            utility.execLog("Error %s" % e)
            return self.browserObject, False, "%s Bunddle not created" % e

    def hostSSH(self, command):
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.load_system_host_keys()
        ssh.connect(hostname=globalVars.applianceIP, username="delladmin", password="delladmin")
        shell = ssh.invoke_shell()
        shell.send("sudo -i\n")
        time.sleep(3)
        receiveBuffer = shell.recv(1024)
        shell.send("delladmin\n")
        time.sleep(3)
        receiveBuffer = shell.recv(2048)
        shell.send(command)
        time.sleep(3)
        receiveBuffer = shell.recv(1024)
        ssh.close()
        return receiveBuffer

    def addOSRepository(self, resource, verifyOSRepo=True):
        """
        Creates a New OS Repository
        verifyOSRepo = False; if verification of OSRepo is not required to check if it's Available (mainly used for Testcase_ApplianceSetup)
        """
        try:
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('Add_repo'))),
                             action="CLICK")
            addOSImagePage = self.handleEvent(
                EC.element_to_be_clickable((By.CLASS_NAME, self.RepositoriesObjects('modal_title'))), action="GET_TEXT")
            if "Add OS Image Repository" not in addOSImagePage:
                return self.browserObject, False, "Failed to verify Add OS Image Repository Page"
            utility.execLog("Add OS Image Repository page loaded")

            utility.execLog("Setting Repository Name {}".format(resource["Repository Name"]))
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_name'))),
                             action="SET_TEXT", setValue=resource["Repository Name"])
            utility.execLog("Able to set Repository Name {}".format(resource["Repository Name"]))

            utility.execLog("Setting ImageType {}".format(resource["Image Type"]))
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_image_type'))),
                             action="SELECT", setValue=resource["Image Type"])
            utility.execLog("Able to set Image Type {}".format(resource["Image Type"]))

            utility.execLog("Setting Source Path and Filename {}".format(resource["Source Path"]))
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_path'))),
                             action="SET_TEXT", setValue=resource["Source Path"])
            utility.execLog("Able to set Source Path and Filename {}".format(resource["Source Path"]))

            if resource["User Name"]:
                utility.execLog("Setting User Name {}".format(resource["User Name"]))
                self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_UN'))),
                                 action="SET_TEXT", setValue=resource["User Name"])
                utility.execLog("Able to set User Name {}".format(resource["User Name"]))

            if resource["Password"]:
                utility.execLog("Setting Password {}".format(resource["Password"]))
                self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_psw'))),
                                 action="SET_TEXT", setValue=resource["Password"])
                utility.execLog("Able to set Password {}".format(resource["Password"]))

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_TC'))), action="CLICK")
            time.sleep(1)

            try:
                errorMessage = self.handleEvent(
                    EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                    action="GET_TEXT", wait_time=10)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_cancel'))),
                                 action="CLICK")
                time.sleep(1)
                utility.execLog("Failed to Add OS Image Repository :: {} :: Error -> {}".format(resource["Repository Name"],
                                                                                       errorMessage))
                return self.browserObject, False, \
                       "Failed to Add OS Image Repository :: {} :: Error -> {}".format(resource["Repository Name"],
                                                                                       errorMessage)
            except:
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('btn_close'))),
                                 action="CLICK")
            time.sleep(1)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_save'))),
                             action="CLICK")
            time.sleep(1)
            self.handleEvent(
                EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))))

            try:
                errorMessage = self.handleEvent(
                    EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                    action="GET_TEXT", wait_time=10)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_cancel'))),
                                 action="CLICK")
                return self.browserObject, False, \
                       "Failed to Add OS Image Repository :: {} :: Error -> {}".format(resource["Repository Name"],
                                                                                       errorMessage)
            except:
                self.browserObject.find_element_by_xpath(
                    ".//*[@id='tab-iso']/div/article/table/tbody/tr/td[contains(.,'{}')]".format(
                        resource["Repository Name"]))
                try:
                    utility.execLog("Verify OS Image Repositories Table contains {}, and it is being copied".format(
                        resource["Image Type"]))
                    is_displayed = self.handleEvent(
                        EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('spinner'))),
                        action="IS_DISPLAYED")
                    utility.execLog(
                        "Verified OS Image Repositories Table contains {}, and it is being copied: {}".format(
                            resource["Image Type"], is_displayed))

                    if verifyOSRepo:
                        utility.execLog("Verify OS Image Repositories Table contains {}, and it is Available".format(
                            resource["Image Type"]))

                        found = False
                        numTries = 0
                        while not found and numTries < 60:
                            try:
                                utility.execLog(
                                    "Waiting for '{}' to be done copying".format(resource["Repository Name"]))
                                time.sleep(1)
                                self.handleEvent(
                                    EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('copying'))),
                                    wait_time=300)
                                last_repo = self.handleEvent(EC.presence_of_all_elements_located(
                                    (By.XPATH, self.RepositoriesObjects('OS_repos'))))[-1]
                                val = last_repo.find_element_by_xpath("./td[1]/span/span/span/span/span").text
                                if str(val) == 'green':
                                    found = True
                                elif val == 'red':
                                    return self.browserObject, False, "'{}' is Unavailable, status is 'red'".format(
                                        resource["Repository Name"])
                            except:
                                numTries += 1
                                if numTries == 4:
                                    utility.execLog(
                                        "Verify directory '/opt/Dell/ASM/temp/repo_download_*' exists, and it contains {}" \
                                        .format(resource["Source Path"][resource["Source Path"].rfind("/") + 1:]))
                                    receiveBuffer = self.hostSSH("\ls -l /opt/Dell/ASM/temp/repo_download_*\n")
                                    if any(resource["Source Path"][resource["Source Path"].rfind("/") + 1:] in \
                                                   receiveLine for receiveLine in receiveBuffer.split("\n")):
                                        utility.execLog("Found /opt/Dell/ASM/temp/repo_download_*/{}" \
                                                        .format(
                                            resource["Source Path"][resource["Source Path"].rfind("/") + 1:]))
                                    else:
                                        utility.execLog("Failed to find /opt/Dell/ASM/temp/repo_download_*/{}" \
                                                        .format(
                                            resource["Source Path"][resource["Source Path"].rfind("/") + 1:]))

                        if found:
                            utility.execLog("Verified OS Image Repositories Table contains {}, and it is Available" \
                                            .format(resource["Image Type"]))
                        else:
                            utility.execLog("Failed to verify OS Image Repositories Table contains {}, and is Available" \
                                            .format(resource["Image Type"]))

                        utility.execLog("Verify directory '/opt/Dell/ASM/temp/repo_download_*' does not exist")
                        receiveBuffer = self.hostSSH("\ls -l /opt/Dell/ASM/temp/repo_download_*\n")
                        if "cannot access /opt/Dell/ASM/temp/repo_download_*: No such file or directory" in receiveBuffer:
                            utility.execLog("Did not find unexpected /opt/Dell/ASM/temp/repo_download_*/{}" \
                                            .format(resource["Source Path"][resource["Source Path"].rfind("/") + 1:]))
                        else:
                            utility.execLog("Found unexpected /opt/Dell/ASM/temp/repo_download_*/{}" \
                                            .format(resource["Source Path"][resource["Source Path"].rfind("/") + 1:]))

                except:
                    utility.execLog("Failed to verify OS Image Repositories Table contains {}, and it is being copied" \
                                    .format(resource["Image Type"]))

            return self.browserObject, True, "Successfully Added OS Image Repository :: {}".format(
                resource["Repository Name"])

        except Exception as e:
            return self.browserObject, False, \
                   "Failed to Add OS Image Repository :: '{}' :: Error -> {}".format(resource["Repository Name"], e)

    def deleteOSRepository(self, resource):
        """
        Creates a New OS Repository
        """
        try:
            utility.execLog("Reading OS Image Repository Table")
            notFound = True
            numIterations = 0
            while notFound and numIterations < 3:
                try:
                    tds = self.handleEvent(
                        EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('os_repos_names'))))
                    numIterations += 1
                    for td in tds:
                        if td.text == resource["Repository Name"]:
                            notFound = False
                except:
                    numIterations += 1

            if notFound:
                utility.execLog("Failed to find Resource :: {}, retries exhausted".format(
                    resource["Repository Name"]))
                return self.browserObject, False, "Failed to find Resource :: {}, retries exhausted".format(
                    resource["Repository Name"])
            else:
                utility.execLog("Verified Resources Table contains {}".format(resource["Repository Name"]))

            utility.execLog("Able to Select Resource '%s'" % (resource["Repository Name"]))

            repos = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('OS_repos'))))
            for repo in repos:
                if resource["Repository Name"] in repo.text:
                    select = Select(repo.find_element_by_xpath('./td[6]/select'))
                    select.select_by_visible_text('Delete')
            time.sleep(1)
            self.handleEvent(
                EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinning_wheel'))))

            try:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_save'))),
                                 action="CLICK")
                notFound = True
                numIterations = 0
                while notFound and numIterations < 10:
                    try:
                        self.handleEvent(
                            EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinner'))),
                            action="IS_DISPLAYED")
                        numIterations += 1
                    except:
                        notFound = False

                if notFound:
                    utility.execLog("Successfully deleted OS Image Repository {}".format(resource["Repository Name"]))
                    return self.browserObject, True, "Removed OS Image Repository :: {}".format(
                        resource["Repository Name"])
                else:
                    utility.execLog("Failed to delete OS Image Repository {}".format(resource["Repository Name"]))
                    return self.browserObject, False, "Remove OS Image Repository :: {}".format(
                        resource["Repository Name"])

            except:
                return self.browserObject, False, \
                       "Failed to Delete OS Image Repository :: {}".format(resource["Repository Name"])

        except Exception as e:
            return self.browserObject, False, \
                   "Failed to Delete OS Image Repository :: '{}' :: Error -> {}".format(resource["Repository Name"], e)

    def verifyOSRepository(self, resource):
        """
        Description:
            API to verify OS Image Repository exists in the table
        """
        try:
            utility.execLog("Verify OS Image Repositories Table contains {}".format(resource["Repository Name"]))
            repos = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('os_repos_names'))))
            for repo in repos:
                if repo.text == resource["Repository Name"]:
                    utility.execLog(
                        "Verified OS Image Repositories Table contains {}".format(resource["Repository Name"]))
                    break
            try:
                utility.execLog("Verify OS Image Repositories Table contains {}".format(resource["Image Type"]))
                repos = self.handleEvent(
                    EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('OS_repos'))))
                for repo in repos:
                    if resource["Repository Name"] in repo.text:
                        img_type = repo.find_element_by_xpath('./td[3]')
                        if img_type.text == resource["Image Type"]:
                            utility.execLog(
                                "Verified OS Image Repositories Table contains {}".format(resource["Image Type"]))
                            break
                try:
                    utility.execLog("Verify OS Image Repositories Table contains {}".format(resource["Source Path"]))
                    for repo in repos:
                        if resource["Repository Name"] in repo.text:
                            img_type = repo.find_element_by_xpath('./td[4]')
                            if img_type.text == resource["Source Path"]:
                                utility.execLog(
                                    "Verified OS Image Repositories Table contains {}".format(resource["Source Path"]))
                                break

                except:
                    return self.browserObject, False, "Failed to verify OS Image Repository :: {} image type {}".format(
                        resource["Repository Name"], resource["Source Path"])

                utility.execLog(
                    "Verify directory '/var/lib/razor/repo-store/{}' exists, and there are RPM files present" \
                    .format(resource["Repository Name"]))
                ssh = paramiko.SSHClient()
                ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
                ssh.load_system_host_keys()
                ssh.connect(hostname=globalVars.applianceIP, username="delladmin", password="delladmin")
                stdin, stdout, stderr = ssh.exec_command("ls -l /var/lib/razor/repo-store\n")
                response = ''.join(stdout.readlines()).split("\n")
                if any(resource["Repository Name"] in responseLine for responseLine in response):
                    utility.execLog("Found /var/lib/razor/repo-store/{}".format(resource["Repository Name"]))
                else:
                    utility.execLog("Failed to find /var/lib/razor/repo-store/{}".format(resource["Repository Name"]))

                if "SLES" in resource["Repository Name"]:
                    stdin, stdout, stderr = ssh.exec_command(
                        "ls /var/lib/razor/repo-store/{}/suse/x86_64".format(resource["Repository Name"]))
                elif "CentOS" in resource["Repository Name"] or "RHEL" in resource["Repository Name"]:
                    stdin, stdout, stderr = ssh.exec_command(
                        "ls /var/lib/razor/repo-store/{}/Packages".format(resource["Repository Name"]))
                elif "Win" in resource["Repository Name"]:
                    stdin, stdout, stderr = ssh.exec_command(
                        "ls /var/lib/razor/repo-store/{}/sources".format(resource["Repository Name"]))

                response = ''.join(stdout.readlines()).split("\n")
                if any(".rpm" in receiveLine for receiveLine in response):
                    utility.execLog(
                        "Found RPM files in /var/lib/razor/repo-store/{}".format(resource["Repository Name"]))
                else:
                    utility.execLog(
                        "Failed to find RPM files in /var/lib/razor/repo-store/{}".format(resource["Repository Name"]))
                ssh.close()

            except:
                return self.browserObject, False, "Failed to verify OS Image Repository :: {} image type {}".format(
                    resource["Repository Name"], resource["Image Type"])

            return self.browserObject, True, \
                   "Found OS Image :: {} in OS Image Repository table".format(resource["Repository Name"])
        except:
            return self.browserObject, False, "Failed to find OS Image Repository :: {}".format(resource["Repository Name"])
        
    def addSwitch_CustomBundles(self,option, catalogName=None, pathInfoJson=None):
        """
        Description:
            API to get Repositories custom bundles
        """
        optionDict = {"S4810": {"bundleName": "S4810",
                                "bundleDescription": "Dell Switch",
                                "model": "Dell Networking S4810/S4820"
                                },
                      "S5000": {"bundleName": "S5000",
                                "bundleDescription": "Dell Switch",
                                "model": "Dell Networking S5000"
                                },
                      "S6000": {"bundleName": "S6000",
                                "bundleDescription": "Dell Switch",
                                "model": "Dell Networking S6000"
                                },
                      "S4820": {"bundleName": "S4820",
                                "bundleDescription": "Dell Switch",
                                "model": "Dell Networking S4810/S4820"
                                },
                      "I/O-Aggregator": {"bundleName": "I/O-Aggregator",
                                         "bundleDescription": "MIOA Switch",
                                         "model": "Dell PowerEdge M I/O Aggregator Switch"
                                         },
                      "MXL-10/40GbE": {"bundleName": "MXL-10/40GbE",
                                       "bundleDescription": "MXL Switch",
                                       "model": "Dell Networking MXL 10/40GbE blade switch"
                                       },
                      "PE-FN-2210S-IOM": {"bundleName": "PE-FN-2210S-IOM",
                                          "bundleDescription": "PE-FN-2210S-IOM",
                                          "model": "Dell Networking FN2210"
                                          },
                      "PE-FN-410S-IOM": {"bundleName": "PE-FN-410S-IOM",
                                         "bundleDescription": "Fx2 Switch",
                                         "model": "Dell Networking FN410"
                                         },
                      "N3000": {"bundleName": "N3000",
                                "bundleDescription": "N3000 Bundle",
                                "model": "Dell Networking N3000"
                                },
                      "N4000": {"bundleName": "N4000",
                                "bundleDescription": "N4000 Bundle",
                                "model": "Dell Networking N4000"
                                }}
        try:
            if not pathInfoJson:
                BundleFilePath = globalVars.switchBundleRepository[option]
            else:
                BundleFilePath = pathInfoJson['switchBundleRepository'][option]
            filePath = BundleFilePath.split("\\")
            filename = filePath[-1]
            versionText = filename.split("-")
            versionText = versionText[-1]
            versionText = versionText.split('.')
            version = '.'.join(versionText[:2]) + '(' + '.'.join(versionText[2:-1]) + ')'
            utility.execLog("version %s" % version)

            utility.execLog("Open Repositories & Select firmware Custom Bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            utility.execLog("Get Default Firmware")
            defaultFirmware = self.handleEvent(
                EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('default_FW'))), action="SELECT",
                selectBy="")
            utility.execLog("Default Firmware : %s" % defaultFirmware)
            if not catalogName:
                if defaultFirmware == "Select":
                    utility.execLog("No Default firmware set")
                    return self.browserObject, False, "No Default firmware set"
            else:
                self.selectRepo(catalogName)

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('add_custom_bundle'))),
                             action="CLICK")
            self.handleEvent(EC.presence_of_element_located((By.ID, self.RepositoriesObjects('bundle_name'))),
                             action="SET_TEXT", setValue=optionDict[option]["bundleName"])
            self.handleEvent(EC.presence_of_element_located((By.ID, self.RepositoriesObjects('bundle_description'))),
                             action="SET_TEXT", setValue=optionDict[option]["bundleDescription"])
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_type'))),
                             action="SELECT", setValue="Switch")
            utility.execLog("selecting Switch components...")

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('device_model'))),
                             action="SELECT", setValue=optionDict[option]["model"])
            utility.execLog("Selecting device model number ..")

            self.handleEvent(EC.presence_of_element_located((By.ID, self.RepositoriesObjects('bundle_version'))),
                             action="SET_TEXT", setValue=version)

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('criticality'))),
                             action="SELECT", setValue="Recommended")
            utility.execLog("Selected Criticality values from Dropdown")

            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
                             action="SET_TEXT", setValue=BundleFilePath)

            utility.execLog("Upload Custom Bundles :: %s" % (optionDict[option]["bundleName"]))
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('fw_bundle_save'))),
                             action="CLICK")
            waitLoadingPage = 20
            countLoop = 0
            while countLoop < waitLoadingPage:
                try:
                    loaderWaitDisplayed = self.handleEvent(
                        EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('spinner'))),
                        action="IS_DISPLAYED")
                except:
                    loaderWaitDisplayed = False
                if loaderWaitDisplayed:
                    utility.execLog("Loading Page after clicking save button")
                    self.handleEvent(
                        EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinner'))))
                else:
                    utility.execLog("Loading Page Complete")
                    break
                countLoop += 1
            try:
                errorMessage = self.handleEvent(
                    EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                    action="GET_TEXT", wait_time=10)
                return self.browserObject, False, errorMessage
            except:
                utility.execLog("uploading custom bundle")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('view_bundles'))),
                             action="CLICK")
            time.sleep(1)
            self.handleEvent(EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinner'))))
            trow = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('user_bundles'))))
            bundleFound = False
            for row in xrange(0, len(trow)):
                name = trow[row].find_element_by_xpath("./td[1]").text
                updatedVersion = trow[row].find_element_by_xpath("./td[2]").text
                if name == optionDict[option]["bundleName"] and updatedVersion == version:
                    bundleFound = True
                    break
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close_bundles'))),
                             action="CLICK")
            if bundleFound:
                utility.execLog("Bundle Created %s" % optionDict[option]["bundleName"])
                return self.browserObject, True, optionDict[option]["bundleName"]
            else:
                return self.browserObject, False, optionDict[option]["bundleName"]

        except Exception as e:
            return self.browserObject, False, "Unable to upload custom bundles  :: Error -> %s" % str(e)

    # No usage found
    def addFirmwareRepositoryNoVerify(self, option, networkPath, localPath, makeDefault, testConnection):
        try:
            utility.execLog("Open Repositories & Select firmware")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('Add_FW'))), action="CLICK")
            utility.execLog("%s option" % option)
            if makeDefault is False:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('set_catalog_default'))),
                                 action="CLICK")

            if option == "defaultftp":
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_FTP'))),
                                 action="CLICK")

            elif option == "networkPath":
                utility.execLog("selecting %s option" % option)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_local'))),
                                 action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('local_file_path'))),
                                 action="SET_TEXT", setValue=networkPath)
                utility.execLog("path :%s" % networkPath)

            elif option == "localPath":
                try:
                    utility.execLog("selecting %s option" % option)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_file'))),
                                     action="CLICK")
                    utility.execLog("path :%s" % localPath)
                    self.handleEvent(
                        EC.presence_of_element_located((By.ID, self.RepositoriesObjects('file_browse_path'))),
                        action="CLEAR")
                    self.handleEvent(
                        EC.presence_of_element_located((By.ID, self.RepositoriesObjects('file_browse_path'))),
                        action="SET_TEXT", setValue=localPath)
                except Exception as e:
                    utility.execLog("Error uploading from local path %s" % e)
            if testConnection:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_TC'))), action="CLICK")
                try:
                    message = self.handleEvent(
                        EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                        action="GET_TEXT")
                    return self.browserObject, False, "Unable to upload Firmware :: Error -> %s" % message
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close'))),
                                     action="CLICK")

                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_save'))),
                                     action="CLICK")

            try:
                message = self.handleEvent(
                    EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                    action="GET_TEXT")
                utility.execLog("Error  save ---%s" % message)
                utility.execLog("Error message on save ---%s" % message)
                return self.browserObject, False, "Unable to upload Firmware :: Error -> %s" % message
            except Exception as e:
                utility.execLog("saved...%s" % str(e))

        except Exception as e:
            return self.browserObject, False, "Unable to upload Firmware  :: Error -> %s" % str(e)

    def verifyErrorRepopath(self, resource):

        try:
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('Add_repo'))), action="CLICK")
            addOSImagePage = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.RepositoriesObjects('fw_bundle_page_title'))),
                action="GET_TEXT")
            if "Add OS Image Repository" != addOSImagePage:
                return self.browserObject, False, "Failed to verify Add OS Image Repository Page"
            utility.execLog("Add OS Image Repository page loaded")

            utility.execLog("Setting Repository Name {}".format(resource["Repository Name"]))
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_name'))),
                             action="SET_TEXT", setValue=resource["Repository Name"])
            utility.execLog("Able to set Repository Name {}".format(resource["Repository Name"]))

            utility.execLog("Setting ImageType {}".format(resource["Image Type"]))
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_image_type'))),
                             action="SELECT", setValue=resource["Image Type"])
            utility.execLog("Able to set Image Type {}".format(resource["Image Type"]))

            utility.execLog("Setting Source Path and Filename {}".format(resource["Source Path"]))
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_path'))),
                             action="SET_TEXT", setValue=resource["Source Path"])
            utility.execLog("Able to set Source Path and Filename {}".format(resource["Source Path"]))

            if resource["User Name"]:
                utility.execLog("Setting User Name {}".format(resource["User Name"]))
                self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_UN'))),
                                 action="SET_TEXT", setValue=resource["User Name"])
                utility.execLog("Able to set User Name {}".format(resource["User Name"]))

            if resource["Password"]:
                utility.execLog("Setting Password {}".format(resource["Password"]))
                self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('repo_psw'))),
                                 action="SET_TEXT", setValue=resource["Password"])
                utility.execLog("Able to set Password {}".format(resource["Password"]))

            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_save'))),
                             action="CLICK")

            try:
                eleError = self.handleEvent(
                    EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                    wait_time=10)
                errorMessage = eleError.find_element_by_tag_name("ul").text
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('repo_cancel'))),
                                 action="CLICK")
                utility.execLog("Test case Passed")
                return self.browserObject, True, \
                       "Failed to Add OS Image Repository :: {} :: Error -> {}".format(resource["Repository Name"],
                                                                                       errorMessage)
            except:
                utility.execLog("Test case failed")
                return self.browserObject, False, "Test case failed"

        except Exception as e:
            return self.browserObject, False, "Unable to verify os repo path :: Error -> %s" % str(e)

    def getFirmwareDownloadState(self, firmwareName):
        """
        Description: Returns the state of firmware : Available, copying or Error
        """
        try:
            utility.execLog("Open Repositories & Select firmware")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            trows = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('FW_repos'))))
            firmwareFound = False
            firmwareState = None
            for rowIndex in xrange(len(trows)):
                firmwareSelected = trows[rowIndex].find_element_by_xpath("./td[2]").text
                if firmwareSelected == firmwareName:
                    firmwareState = trows[rowIndex].find_element_by_xpath("./td[1]").text
                    firmwareFound = True
                    break
            utility.execLog("'{}' state is '{}'".format(firmwareName, firmwareState))
            if firmwareFound:
                return self.browserObject, True, firmwareState
            else:
                return self.browserObject, False, "%s : Firmware not found" % firmwareName
        except Exception as e:
            return self.browserObject, False, "Unable to detect state : Error :%s" % e

    # No usage found
    # def verifyRepositoryDeletion(self, inUse, state):
    #     """
    #     Code to verify that a repository can't be deleted :
    #     1:- when it is used by any resource. inUse=True
    #     2:- when it is pending state. state=Copying
    #     """
    #     self.loopCount = 2
    #     try:
    #         if self.loopCount > 0:
    #             xpath = self.RepositoriesObjects('OS_repos')
    #             totalColumns = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
    #             utility.execLog("Total Number of Columns : %s" % str(totalColumns))
    #             tableColumns = []
    #             for col in range(1, totalColumns + 1):
    #                 xpath = ".//*[@id='tab-iso']/div/article/table/thead/tr/th[%i]" % col
    #                 if self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="IS_DISPLAYED"):
    #                     colName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
    #                     tableColumns.append(colName)
    #                     utility.execLog("Able to fetch Column Name: '%s'" % colName)
    #             tableColumns = [x for x in tableColumns if x != '']
    #             utility.execLog("Able to fetch %s Table Columns '%s'" % (str(tableColumns)))
    #             xpath = self.RepositoriesObjects('OS_repos')
    #             totalRows = len(self.browserObject.find_elements_by_xpath(xpath))
    #             utility.execLog("Total Number of Rows : %s" % str(totalRows))
    #
    #             for row in range(1, totalRows + 1):
    #                 for col in range(1, totalColumns + 1):
    #                     repository = self.browserObject.find_element_by_xpath(
    #                         ".//*[@id='tab-iso']/div/article/table/tbody/tr[%i]/td[2]" % row).text
    #                     displayed = self.browserObject.find_element_by_xpath(
    #                         ".//*[@id='tab-iso']/div/article/table/tbody/tr[%i]/td[6]/select" % row).is_displayed()
    #                     enabled = self.browserObject.find_element_by_xpath(
    #                         ".//*[@id='tab-iso']/div/article/table/tbody/tr[%i]/td[6]/select" % row).is_enabled()
    #                     if state and col == 1:
    #                         xpath = ".//*[@id='tab-iso']/div/article/table/tbody/tr[%i]/td[%i]" % (row, col)
    #                         state = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)),
    #                                                  action="GET_ATTRIBUTE_VALUE", attributeName="ng-switch-when").text
    #                         if state == "copying" and enabled and displayed:
    #                             utility.execLog("Failed.....")
    #                             utility.execLog("Repository  %s is clickable :: during pending state" % repository)
    #                         else:
    #                             utility.execLog(
    #                                 "Test Repository  %s , state::%s , can delete ::%s" % (repository, state, enabled))
    #                         break
    #                     if inUse and col == 5:
    #                         xpath = ".//*[@id='tab-iso']/div/article/table/tbody/tr[%i]/td[%i]" % (row, col)
    #                         inUse = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)),
    #                                                  action="GET_TEXT")
    #                         if inUse == "True" and enabled and displayed:
    #                             utility.execLog("Failed.....")
    #                             utility.execLog("Repository  %s is clickable :: after being used" % repository)
    #                         else:
    #                             utility.execLog(
    #                                 "Test Repository  %s , Use::%s , can delete ::%s" % (repository, inUse, enabled))
    #                         break
    #             return self.browserObject, True, "Repositories count=%i" % totalRows
    #
    #         else:
    #             utility.execLog(
    #                 "Maximum retries exceeded for reading Repository Table :: Retries ('%s')" % str(self.loopCount))
    #             raise "Maximum retries exceeded for reading Repository Table :: Retries ('%s')" % str(self.loopCount)
    #     except StaleElementReferenceException as se:
    #         utility.execLog("Repository Page reloaded '%s'" % str(se) + format_exc())
    #         self.loopCount -= 1
    #         self.verifyRepositoryDeletion()
    #     except Exception as e:
    #         utility.execLog("Unable to read Repository Table :: Error -> %s" % str(e) + format_exc())
    #         raise e

    # No usage found
    # def fatchFirstRepository(self):
    #     try:
    #         xpath = self.RepositoriesObjects('OS_col_names')
    #         totalColumn = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
    #         xpath = self.RepositoriesObjects('OS_repos')
    #         totalRow = len(self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, xpath))))
    #         for row in range(1, totalRow + 1):
    #             for col in range(1, totalColumn + 1):
    #                 repository = self.browserObject.find_element_by_xpath(
    #                     ".//*[@id='tab-iso']/div/article/table/tbody/tr[{}]/td[2]".format(row)).text
    #                 image = self.browserObject.find_element_by_xpath(
    #                     ".//*[@id='tab-iso']/div/article/table/tbody/tr[{}]/td[3]".format(row)).text
    #                 sourcePath = self.browserObject.find_element_by_xpath(
    #                     ".//*[@id='tab-iso']/div/article/table/tbody/tr[{}]/td[4]".format(row)).text
    #                 firstRepo = {"Repository Name": repository, "Image Type": image, "Source Path": sourcePath,
    #                              "User Name": "", "Password": ""}
    #                 return firstRepo
    #             break
    #     except Exception as e:
    #         utility.execlog("Unable to fatch fatchFirstRepository::%s" % str(e) + format_exc())

    # No usage
    # def checkRepositoryState(self, repositoryName):
    #     xpath = ".//*[@id='tab-iso']/div/article/table/tbody/tr/td[2][contains(., '{}')]".format(repositoryName)
    #     try:
    #         state = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
    #         return self.browserObject, True, state
    #     except Exception as e:
    #         return self.browserObject, False, str(e)

    # No usage
    # def delnow(self, repoName):
    #     try:
    #         root_command = "sudo rm -rf /var/lib/razor/repo-store/" + repoName + "\n"
    #         # root_command="ls /var/lib/razor/repo-store/\n"
    #         # Create an SSH client
    #         self.client = paramiko.SSHClient()
    #
    #         # Make sure that we add the remote server's SSH key automatically
    #         self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    #
    #         # Connect to the client
    #         self.client.connect(globalVars.applianceIP, username="delladmin", password="delladmin")
    #
    #         # Create a raw shell
    #         self.shell = self.client.invoke_shell()
    #
    #         # Send the su command
    #         self.send_string_and_wait("sudo bash\n", 5, True)
    #
    #         # Send the client's su password followed by a newline
    #         self.send_string_and_wait("delladmin" + "\n", 5, True)
    #
    #         # Send the install command followed by a newline and wait for the done string
    #         # self.send_string_and_wait_for_string(root_command, root_command_result, True)
    #         status, result = self.send_string_and_wait_for_string(repoName, root_command, "", True)
    #
    #         # Close the SSH connection
    #         self.client.close()
    #         utility.execLog("#######Close Connection to {} ####### ".format(globalVars.applianceIP))
    #         return status, result
    #     except Exception as e:
    #         utility.execLog("#####Exception occured::{}".format(str(e)))
    #         return False, str(e)

    # Part of delnow() which is not used
    # def send_string_and_wait(self, command, wait_time, should_print):
    #     # Send the su command
    #     self.shell.send(command)
    #
    #     # Wait a bit, if necessary
    #     time.sleep(wait_time)
    #
    #     # Flush the receive buffer
    #     receive_buffer = self.shell.recv(1024)
    #
    #     # Print the receive buffer, if necessary
    #     if should_print:
    #         utility.execLog("send_string_and_wait={}".format(str(receive_buffer)))

    # Part of delnow() which is not used
    # def send_string_and_wait_for_string(self, repoName, rmCommand, wait_string, should_print):
    #     result, error = self.returnCommand("whoami")
    #     utility.execLog("whoami first = {}".format(result))
    #     countLs = 0
    #     countRm = 0
    #     lsCommand = "ls /var/lib/razor/repo-store"
    #     result, error = self.returnCommand(lsCommand)
    #     while countLs < 15 and repoName not in str(result):
    #         countLs += 1
    #         result, error = self.returnCommand(lsCommand)
    #         utility.execLog("Folder {} is yet to create. Count={}".format(repoName, countLs))
    #
    #     while (countRm < 10):
    #         countRm = countRm + 1
    #         utility.execLog("Delete command Count={}".format(countRm))
    #         self.noReturnCommand(rmCommand)
    #         result, error = self.returnCommand(lsCommand)
    #
    #     if (repoName in str(result)):
    #         utility.execLog("{} is not deleted.".format(repoName))
    #         return False, \
    #                "Failed....Repository :: {} is not Deleted".format(repoName)
    #     else:
    #         utility.execLog("Repo {} is deleted.".format(repoName))
    #         return True, \
    #                "Success...Repository :: {} is Deleted".format(repoName)

    # Not used
    # def returnCommand(self, command):
    #     stdin, stdout, stderr = self.client.exec_command(command)
    #     time.sleep(2)
    #     # receive_buffer = self.shell.recv(1024)
    #     # utility.execLog("returnCommand={}".format(str(receive_buffer)))
    #     result = "".join(stdout.readlines())
    #     error = "".join(stderr.readlines())
    #     utility.execLog("ReturnCommand Execute {} command on {}".format(command, globalVars.applianceIP))
    #     utility.execLog("ReturnCommand result={}".format(result))
    #
    #     return result, error

    # Not used
    # def noReturnCommand(self, command):
    #     self.shell.send(command)
    #     time.sleep(5)
    #     receive_buffer = self.shell.recv(1024)
    #     utility.execLog("noReturnCommand {} exceuted on {}.".format(command, globalVars.applianceIP))
    #     utility.execLog("noReturnCommand result={}".format(str(receive_buffer)))

    def reSynchrozeFW(self, repositoryType="Firmware"):
        """
        Retry FW  when in Error state
        """
        try:
            self.colIndex = -1
            utility.execLog("Navigating to Firmware Tab")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.temps[repositoryType][0])), action="CLICK")
            repos = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('FW_repos'))))
            utility.execLog("Length of Row: %i" % len(repos))

            for repo in repos:
                if "Error" in repo.text:
                    select = Select(repo.find_element_by_xpath("./td[5]/select"))
                    select.select_by_visible_text("Resynchronize")
                    time.sleep(1)
                    self.handleEvent(EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects("spinner"))))
                    state = repo.find_element_by_xpath("./td[1]")

                    if "Copying" in state.text:
                        utility.execLog("Successfully Retried when it is in Error state")
                        return self.browserObject, True, "Successfully Retried when it is in Error state"

            return self.browserObject, True, "No FW catalog in Error state"

        except Exception as e:
            return self.browserObject, False, "Failed to Initiate Delete Repository :: '%s' :: Error -> %s" % (
            repositoryType, str(e))

    def clickEditCustomBundle(self, catalogName, bundleName):
        """
        Description:
            Edit an Existing Custom Bundle, add new Bundle and Save.
        """

        try:
            utility.execLog("Open Repositories & Select firmware Custom Bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            utility.execLog("Selecting  Catalog %s" % catalogName)
            tr = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('FW_repos'))))
            for row in xrange(0, len(tr)):
                catalogSearched = tr[row].find_element_by_xpath("./td[2]").text
                if catalogSearched == catalogName:
                    tr[row].find_element_by_xpath("./td[2]").click()
                    break
            utility.execLog("Click View Bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('view_bundles'))),
                             action="CLICK")
            time.sleep(1)
            self.handleEvent(EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects("spinner"))))
            trow = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('user_bundles'))))
            # Existing Bundle File name
            utility.execLog("Editing Bundle %s" % bundleName)

            bundleFound = False
            for row in xrange(0, len(trow)):
                name = trow[row].find_element_by_xpath("./td[1]").text
                if name == bundleName:
                    rowNum = row
                    bundleFound = True
                    trow[row].click()
                    break
            if not bundleFound:
                return False, "%s Bundle Not Found" % bundleName
            else:
                try:
                    editButton = self.browserObject.find_element_by_xpath(
                        "//tr[%d]//*[@id='lnkEditUserBundle']" % (int(rowNum) + 1))
                    self.browserObject.execute_script("arguments[0].click();", editButton)
                except Exception as e:
                    utility.execLog("Unable to Click Edit on Bundle Error: %s" % e)
                    return False, "Unable to Click Edit on Bundle Error: %s" % e
                utility.execLog("Able to click edit on Bundle %s" % bundleName)
                return True, "Able to click edit on Bundle %s" % bundleName
        except Exception as e:
            return self.browserObject, False, "Unable to click edit custom bundles  :: Error -> %s" % str(e)

    def verifyCustomBundlePath(self, catalogName, bundleName):
        """
        Click Edit on custom bundle and verify correct bundle path
        """
        try:
            bundlePath = globalVars.switchBundleRepository[bundleName]
            utility.execLog("Click edit on Bundle")
            status, result = self.clickEditCustomBundle(catalogName, bundleName)
            if not status:
                utility.execLog(result)
                return self.browserObject, False, result
            filePath = bundlePath.split("\\")
            filename = filePath[-1]
            xpath = self.RepositoriesObjects('current_file_name')
            time.sleep(1)
            existingFileName = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")

            if filename in existingFileName:
                utility.execLog("Closing Bundle Form")
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('btn_close_edit_bundle'))),
                                 action="CLICK")
                time.sleep(1)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close_bundles'))),
                                 action="CLICK")
                time.sleep(1)
                utility.execLog("Bundle Name %s displayed as existing file name" % bundleName)
                return self.browserObject, True, "Bundle Name %s displayed as existing file name" % bundleName
            else:
                utility.execLog("Closing Bundle Form")
                self.handleEvent(
                    EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('btn_close_edit_bundle'))),
                    action="CLICK")
                time.sleep(1)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close_bundles'))),
                                 action="CLICK")
                time.sleep(1)
                utility.execLog("Bundle Name %s not displayed as existing file name fileName : %s Existing FileName:%s" % (
                bundleName, filename, existingFileName))
                return self.browserObject, False, "Bundle Name %s not displayed as existing file name fileName : %s Existing FileName:%s" % (
                bundleName, filename, existingFileName)
        except Exception as e:
            return self.browserObject, False, "Error :: %s" % e

    # No usage by any test case
    # def editCustomBundlePath(self, catlogName, bundleName, newBundlePath):
    #     '''
    #     Edit custom Bundle by editing Bundle Name, Version and Path
    #     '''
    #     try:
    #         utility.execLog("Click edit on Bundle")
    #         self.clickEditCustomBundle(catlogName, bundleName)
    #         newFilePath = newBundlePath.split("\\")
    #         newFilename = newFilePath[-1]
    #         versionText = newFilename.split("-")
    #         versionText = versionText[-1]
    #         versionText = versionText.split('.')
    #         version = '.'.join(versionText[:2]) + '(' + '.'.join(versionText[2:-1]) + ')'
    #         utility.execLog("version %s" % version)
    #         try:
    #             self.handleEvent(
    #                 EC.element_to_be_clickable((By.XPATH, "//*[@id='page_add_bundle']//*[@id='bundleVersion']")),
    #                 action="CLEAR")
    #             self.handleEvent(
    #                 EC.element_to_be_clickable((By.XPATH, "//*[@id='page_add_bundle']//*[@id='bundleVersion']")),
    #                 action="SET_TEXT", setValue=version)
    #             utility.execLog("Edit Bundle Path")
    #             self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
    #                              action="CLEAR")
    #             self.handleEvent(EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
    #                              action="SET_TEXT", setValue=newBundlePath)
    #         except Exception as e:
    #             utility.execLog("Unable to Edit Custom bundle")
    #             return self.browserObject, False, "Unable to Edit Custom bundle"
    #         self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('fw_bundle_save'))),
    #                          action="CLICK")
    #         waitLoadingPage = 20
    #         countLoop = 0
    #         while countLoop < waitLoadingPage:
    #             loaderWaitDisplayed = self.handleEvent(
    #                 EC.visibility_of_element_located((By.XPATH, self.RepositoriesObjects('spinner'))),
    #                 action="IS_DISPLAYED")
    #             if loaderWaitDisplayed:
    #                 utility.execLog("Loading Page after clicking save button")
    #                 time.sleep(10)
    #             else:
    #                 utility.execLog("Loading Page Complete")
    #                 break
    #             countLoop += 1
    #         try:
    #             xpath = self.RepositoriesObjects('alert_danger')
    #             message = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT")
    #             utility.execLog("Error on Save: %s" % message)
    #             utility.execLog("Error Message on Save ---%s" % message)
    #             return self.browserObject, False, "Unable to upload Firmware :: Error -> %s" % message
    #         except:
    #             utility.execLog("Changes Saved")
    #         utility.execLog("Closing Bundle Form")
    #         self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close_bundles'))),
    #                          action="CLICK")
    #         time.sleep(2)
    #         return self.browserObject, True, "Able to Edit Custom bundle"
    #     except Exception as e:
    #         return self.browserObject, False, "Error :%s" % e

    # NO usage
    # def waitRepositoryAvailable(self, repositoryName):
    #     '''
    #     wait for copying state of repository to end
    #     '''
    #     try:
    #         utility.execLog("Searching for repository %s" % repositoryName)
    #         self.selectRepo(repositoryName)
    #         self.browserObject, status, result = self.selectRepo(repositoryName)
    #         if not status:
    #             return self.browserObject, False, result
    #
    #         loopCounter = 0
    #         downloadState = 'Copying'
    #         while downloadState == "Copying":
    #             repoList = self.getRepositories(repoType="Firmware")  # getRepositories not found
    #             for repo in repoList:
    #                 if repo["Repository Name"] == repositoryName:
    #                     downloadState = repo["state"]
    #             time.sleep(30)
    #             loopCounter += 1
    #             if loopCounter > 240:
    #                 break
    #         if downloadState == "Copying":
    #             return self.browserObject, False, "Download exceeding expected time duration"
    #         return self.browserObject, True, "Catalog Download Complete"
    #     except Exception as e:
    #         return self.browserObject, False, "Error :: %s" % e

    def deleteCustomBundle(self, catalogName, bundleName):
        '''
        delete a custom bundle
        '''
        try:
            utility.execLog("Open Repositories & Select firmware Custom Bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")
            utility.execLog("Selecting  Catalog %s" % catalogName)

            self.selectRepo(catalogName)
            utility.execLog("Click View Bundles")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('view_bundles'))),
                             action="CLICK")
            time.sleep(1)
            self.handleEvent(
                EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects("spinner"))))
            trow = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.RepositoriesObjects('user_bundles'))))
            # Existing Bundle File name

            utility.execLog("Deleting Bundle %s" % bundleName)

            bundleFound = False
            for row in xrange(0, len(trow)):
                name = trow[row].find_element_by_xpath("./td[1]").text
                if name == bundleName:
                    rowNum = row
                    bundleFound = True
                    trow[row].click()
                    break
            if not bundleFound:
                return self.browserObject, False, "%s Custom Bundle Not Found" % bundleName
            else:
                try:
                    time.sleep(1)
                    self.handleEvent(
                        EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects("spinner"))))
                    trow[rowNum].find_element_by_xpath(self.RepositoriesObjects('lnkRemoveUserBundle')).click()
                    time.sleep(1)
                    self.handleEvent(
                        EC.invisibility_of_element_located((By.XPATH, self.RepositoriesObjects("spinner"))), wait_time=90)
                except Exception as e:
                    utility.execLog("Unable to delete Custom Bundle Error: %s" % e)
                    return self.browserObject, False, "Unable to delete Custom Bundle Error: %s" % e
            utility.execLog("Closing Bundle Form")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close_bundles'))),
                             action="CLICK")
            time.sleep(2)
            return self.browserObject, True, "Able to delete custom bundle"
        except Exception as e:
            return self.browserObject, False, "Unable to delete Custom Bundle Error: %s" % e

    def addFirmwareRepository(self, option, networkPath, localPath, makeDefault, testConnection, waitAvailable):
        try:
            utility.execLog("Open Repositories & Select firmware")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_tab'))), action="CLICK")

            globalVars.browserObject, status, result = self.getDetails(option="Firmware")
            repoListPreCatalogAdd = [repo['Repository Name'] for repo in result]
            utility.execLog("Repository List pre catalog add:: %s" % repoListPreCatalogAdd)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('Add_FW'))),
                             action="CLICK")
            utility.execLog("%s option" % option)
            if not status:
                return self.browserObject, False, result
            makeDefaultSelected = self.handleEvent(
                EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('set_catalog_default'))))
            if makeDefault:
                if not makeDefaultSelected.is_selected():
                    makeDefaultSelected.click()
            else:
                if makeDefaultSelected.is_selected():
                    makeDefaultSelected.click()
            if option == "defaultftp":
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_FTP'))),
                                 action="CLICK")

            elif option == "networkPath":
                utility.execLog("selecting %s option" % option)
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_local'))),
                                 action="CLICK")
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('local_file_path'))), action="SET_TEXT",
                                 setValue=networkPath)
                utility.execLog("path :%s" % networkPath)

            elif option == "localPath":
                try:
                    utility.execLog("selecting %s option" % option)
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('catalog_file'))),
                                     action="CLICK")
                    utility.execLog("path :%s" % localPath)
                    self.handleEvent(
                        EC.visibility_of_element_located((By.ID, self.RepositoriesObjects('fw_package_file'))),
                        action="SET_TEXT", setValue=localPath)
                except Exception as e:
                    utility.execLog("Error uploading from local path %s" % e)

            if testConnection:
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_TC'))), action="CLICK")
                try:
                    message = self.handleEvent(
                        EC.presence_of_element_located((By.CLASS_NAME, self.RepositoriesObjects('alert_danger'))),
                        action="GET_TEXT", wait_time=10)
                    utility.execLog("Error Test Connection: %s" % message)
                    return self.browserObject, False, "Unable to upload Firmware :: Error -> %s" % str(message)
                except:
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('btn_close'))),
                                     action="CLICK")
                    time.sleep(1)
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.RepositoriesObjects('FW_save'))), action="CLICK")
            waitLoadingPage = 25
            countLoop = 0
            while countLoop < waitLoadingPage:
                try:
                    loaderWaitDisplayed = self.handleEvent(
                        EC.element_to_be_clickable((By.XPATH, self.RepositoriesObjects('spinning_wheel'))),
                        action="IS_DISPLAYED")
                except:
                    loaderWaitDisplayed = False
                if loaderWaitDisplayed:
                    utility.execLog("Loading Page after clicking save button")
                    time.sleep(30)
                else:
                    utility.execLog("Loading Page Complete")
                    break
                countLoop += 1
            try:
                xpath = self.RepositoriesObjects('alert_danger')
                message = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)), action="GET_TEXT", wait_time=10)
                utility.execLog("Error Message on Save --- %s" % message)
                return self.browserObject, False, "Unable to upload Firmware :: Error -> %s" % message
            except:
                utility.execLog("Firmware loading initiated")
                self.browserObject, status, result = self.getDetails(option='Firmware')
                repoListPostCatalogAdd = [repo['Repository Name'] for repo in result]
                utility.execLog("Repository List Post Catalog add ::%s" % repoListPostCatalogAdd)
                catalogAdded = [catalog for catalog in repoListPostCatalogAdd if catalog not in repoListPreCatalogAdd]
                catalogAdded = catalogAdded[0]
                utility.execLog("Catalog Added %s" % catalogAdded)
            if waitAvailable:
                globalVars.browserObject, status, result = self.getDetails(option="Firmware")
                status, result = self.waitCatalogAvailable(catalogAdded)
                if status:
                    return self.browserObject, True, catalogAdded
                else:
                    return self.browserObject, False, result
            return self.browserObject, True, catalogAdded
        except Exception as e:
            return self.browserObject, False, "Unable to Download Firmware  :: Error -> %s" % str(e)

    def waitCatalogAvailable(self, catalogName):
        '''
        wait for catalog download complete
        '''
        try:
            self.browserObject, status, repoList = self.getDetails(option="Firmware")
            repoNameList = [repo['Repository Name'] for repo in repoList]
            if catalogName in repoNameList:
                utility.execLog('Catalog %s Found in List' % catalogName)
            else:
                return False, "Catalog %s not found in list" % catalogName

            loopCounter = 300
            while loopCounter:
                for repo in repoList:
                    if repo['Repository Name'] == catalogName:
                        catalogState = repo['State']
                        utility.execLog("Catalog %s :: State :: %s" % (repo['Repository Name'], catalogState))
                        if catalogState != 'Copying':
                            utility.execLog(
                                'Catalog %s Download Complete. State:: %s' % (repo['Repository Name'], catalogState))
                            break
                self.browserObject.refresh()
                time.sleep(30)
                self.browserObject, status, repoList = self.getDetails(option="Firmware")
                if catalogState == 'Error':
                    return False, '%s Download Complete in Error State' % catalogName
                elif catalogState == 'Available':
                    return True, 'Catalog Download Complete'
                loopCounter -= 1
            if catalogState == 'Copying':
                return False, '%s Downloading Catalog Exceeding Expected time limit' % catalogName
        except Exception as e:
            return False, "Error %s" % e

    # No usage
    # def getFirmwareInfo(self, firmwareName):
    #     '''
    #     Get fimrmware Information
    #     '''
    #     try:
    #         componentDict = {}
    #         utility.execLog('Selecting Firmware %s' % firmwareName)
    #         self.selectRepo(firmwareName)
    #         utility.execLog('Reading Firmware Information table')
    #         componentList = ['bundles', 'components', 'created', 'updated', 'customBundles']
    #         for component in componentList:
    #             xpath = ".//*[@id='firmwareInfo']//*[contains(@data-bind, '{}')]".format(component)
    #             componentDict[component] = self.handleEvent(EC.presence_of_element_located((By.XPATH, xpath)),
    #                                                         action='GET_TEXT')
    #             utility.execLog('Component : {} :: {}'.format(component, componentDict[component]))
    #         utility.execLog('Get Services affected by the template')
    #         xpath = ".//*[@id='firmwareInfo']//*[contains(@data-bind, 'services/details')]"
    #         componentDict['services'] = self.browserObject.find_elements_by_xpath(xpath)
    #     except:
    #         utility.execLog("Error")
