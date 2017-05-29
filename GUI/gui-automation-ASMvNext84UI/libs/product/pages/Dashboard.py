"""
Author: Rajeev Kumar/Raj Patel/Saikumar Kalyankrishnan
Created/Modified: Nov 2nd 2015/Feb 22nd 2017
Description: Functions/Operations related to Dashboard Page

Revision History:
--Date--    --Name--            --TestCase--    --Changes--
06Jan16     Nidhi Aishwarya     TC-3331         Added new function get_TotalResources()
                                TC-3330         Modified function get_IndividualServices() according to new build
                                TC-2513         Modified function get_Storage_Volume() according to new build
                                TC-2463         Modified function get_IndividualServices() according to new build
                                TC-2534         Modified function get_Server_Utilization() according to new build
                                TC-2543         Modified function get_TotalStorage_Volume() according to new build
                                TC-2560         Modified function getOptions() according to new build
07Jan16     Raj Patel           TC-3329         Added new function verifyLinksAndPages()
14Jan16     Ankit Manglic       TC-2635         Added new function getCreateNewTemplateButtonStatus() and getAddExistingServiceButtonStatus()
14Jan16     Raj Patel           TC-3337         Added new function get_Server_Health()
15Jan16     Raj Patel           TC-3332         Added new function verify_ServersInUse()
25Jan16     Nidhi Aishwarya     TC-3328         Modified existing functions get_TotalServer_Utilization() and get_Server_Utilization()
01Feb16     Nidhi Aishwarya     TC-2566         Modified existing function get_IndividualServices() according to new build
01Feb16     Nidhi Aishwarya     TC-2589         Modified existing function get_Storage_Volume() according to new build
08Feb16     Rajeev Kumar        TC-3927         getDashboard_AllLinks()
"""

from CommonImports import *
import re
from libs.thirdparty.selenium.webdriver.common.action_chains import ActionChains
from libs.product.objects.Dashboard import Dashboard
from libs.product.pages import Resources
from libs.product.pages import Services
from libs.product.pages import Logs


class Dashboard(Navigation, Dashboard):
    """
    Description:
        Class which includes all Functions/Operations related to Dashboard Page
    """

    def __init__(self, browserObject):
        """
        Description:
            Initializing an object of Dashboard class
        """
        Navigation.__init__(self, browserObject)
        self.pageTitle = "Dashboard"
        utility.execLog("Dashboard")

    def loadPage(self):
        """
        Description:
            API to load Dashboard Page
        """
        try:
            utility.execLog("Loading Dashboard Page...")
            self.browserObject, status, result = self.selectOption("Dashboard")
            return self.browserObject, status, result
        except Exception as e:
            return self.browserObject, False, "Unable to load Dashboard Page %s" % (str(e) + format_exc())

    # Function not being used
    def validatePageTitle(self, title=None):
        """
        Description:
            API to validate Dashboard Page
        """
        if not title:
            title = self.pageTitle
        getCurrentTitle = self.handleEvent(EC.element_to_be_clickable((By.ID, self.DashboardObjects('page_dashboard'))), action="GET_TEXT")
        if title not in getCurrentTitle:
            utility.execLog("Unable to validate Page Title Actual: '%s' Expected: '%s'" % (getCurrentTitle, title))
            return self.browserObject, False, "Unable to validate Page Title Actual:'%s' Expected:'%s'" % (getCurrentTitle, title)
        else:
            utility.execLog("Successfully validated Page Title: '%s'" % title)
            return self.browserObject, True, "Successfully validated Page Title: '%s'" % title

    # Seems like function is incomplete
    def getDetails(self):
        """
       Description:
            API to get existing Dashboard page details
#
#         """


        bo = self.browserObject.find_element_by_class_name("DashboardSectionHeader")

        if not bo:
            utility.execLog("Dashboard page is not visible since Settings Tab is not expanded")
            self.browserObject.find_element_by_id("menu_settings").click()
            utility.execLog("Settings Tab is expanded")
        #bo.click()
        so = self.browserObject.find_element_by_id("services")

        if not bo:
            utility.execLog("Dashboard is not visible since Settings Tab is not expanded")
            self.browserObject.find_element_by_id("menu_settings").click()
            utility.execLog("Settings Tab is expanded")
        so.click()
        self.browserObject.find_element_by_class_name("clearfix").click()

    def getSection_Learn(self):

        """
         Description:
          Function to click Links under Learn on the Dashboard for Standard & Read-only user.
        # TestCase 2585, Created By HCL
        """
        utility.execLog("in getSection_Learn in dashboard page ")

        utility.execLog("Navigating to dashboard")
        self.handleEvent(EC.element_to_be_clickable((By.ID, self.DashboardObjects('deployLearn'))),
                         action="CLICK")
        self.handle_window("Deploy Service")
        self.handleEvent(EC.element_to_be_clickable((By.ID, self.DashboardObjects('templateLearn'))),
                         action="CLICK")
        self.handle_window("Templates")
        return "true"

    def handle_window(self, expected_window_title):

        self.browserObject = globalVars.browserObject
        driver = self.browserObject
        utility.execLog("Getting window handle")
        for h in driver.window_handles[1:]:
            driver.switch_to_window(h)
            window_title = driver.title
            utility.execLog('Title of window is: "{}"'.format(window_title))
            if window_title == expected_window_title:
                utility.execLog("Information page is opened and verified ")
            else:
                utility.execLog("Information page not found")
            driver.close()
        driver.switch_to_window(driver.window_handles[0])

    def getSection_ServerPool(self):

        """
         Description:
          Function to Click on any individual server pool on the Dashboard for Read-only.
           #  TestCase 2502, Created By HCL
        """
        utility.execLog("Navigating to Server Pool Section at Dashboard")
        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.DashboardObjects('viewServerPool'))),
                         action="CLICK")
        pageObject = Resources.Resources(self.browserObject)
        pageObject.getOptions("Server Pools")
        utility.execLog("At Resources page")
        server_pool_name = self.handleEvent(EC.visibility_of_element_located((By.CLASS_NAME, self.DashboardObjects('bottomDetails'))),
                                            action="GET_TEXT")
        # this line just filters text so it can get first word from string
        server_pool_name = str(re.search('^\w+', server_pool_name).group())
        utility.execLog(server_pool_name)
        if server_pool_name == "Global":
            utility.execLog("Verified clicking on any individual server pool on the Dashboard displays information about that server pool under Resources")
        else:
            utility.execLog("TestCase failed")
        return "true"

    def getSection_Storage(self):

        """
         Description:
          Function to Click on any individual server pool on any Storage Group for Read-only.
           #  TestCase 2460, Created By HCL
        """

        utility.execLog("Navigating to Storage Capacity Section")
        utility.execLog("Clicking on first link")
        self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('storageGroup'))),
                         action="CLICK")
        pageObject = Resources.Resources(self.browserObject)
        pageObject.getOptions()
        Resources_pagetitle = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('resourcesHeader'))),
                                               action="GET_TEXT")
        utility.execLog('Page title is: "{}"'.format(Resources_pagetitle))
        if Resources_pagetitle == "Resources":
            utility.execLog("Verified that for a Read-only user, clicking on any individual storage type on the Dashboard displays all the available storages under Resources")
        else:
            utility.execLog("TestCase is failed")
        return "true"

    def getOptions(self):

        """
        Description:
            API to get Options and their Accessibility for Dashboard Page (2560)

#     TestCase 2560, Created By HCL 11/25/2015
        """
        utility.execLog("Verifying Service Overview Section")
        service_section_title = self.handleEvent(EC.visibility_of_element_located((By.CLASS_NAME, self.DashboardObjects('grayTitle'))),
                                                 action="GET_TEXT")
        if "Service Overview" in service_section_title:
            utility.execLog("Verified Service overview section present")
            options = ["Services with Critical Errors", "Services with Warnings", "Services In-Progress", "Services Pending", "Healthy Services", "Canceled Services"]
            i = 0
            eachService = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('eachService'))))
            for element in eachService:
                service_name_actual = element.get_attribute("title")
                utility.execLog(service_name_actual)
                service_name_expected = options[i]
                if service_name_expected in service_name_actual:
                    utility.execLog('Verified: "{}" is in "{}"'.format(service_name_expected, service_name_actual))
                else:
                    utility.execLog("Service Not found")
                i += 1
        else:
            utility.execLog("Service overview section not found")

        server_utilization_section_title = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('serverUtilization'))),
                                                            action="GET_TEXT")
        if server_utilization_section_title == "Server Utilization in Services":
            utility.execLog("Verified Server Utilization section present")
        else:
            utility.execLog("Server Utilization section not found")

        total_storage_capacity_section_title = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('totalStorageCapacity'))),
                                                                action="GET_TEXT")
        if total_storage_capacity_section_title == "Total Storage Capacity":
            utility.execLog("Verified Total Storage Capacity section present")
        else:
            utility.execLog("Total Storage Capacity section not found")

        recent_activity_section_title = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('activityLog'))))
        if recent_activity_section_title:
            utility.execLog("Verified Activity Log section present")
            recent_activity_view_all_section_title = self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('viewAll'))),
                                                                      action="GET_TEXT")
            if recent_activity_view_all_section_title == "View All":
                utility.execLog("Verified View All link present")
        else:
            utility.execLog("Activity Log section not found")

        learn_section_title = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('learn'))),
                                               action="GET_TEXT")
        if learn_section_title == "Learn":
            utility.execLog("Verified Learn section present")
        else:
            utility.execLog("Learn section not found")
        return "true"

#  TestCase 2463, Created By HCL     11/26/2015
    def get_IndividualServices(self):
        """
        Description:
            API to get Options and their Accessibility for Dashboard Page (2560)
        """
        utility.execLog("Checking various sections at Dashboard page")
        utility.execLog("Verifying Service Overview Section")
        service_section = self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('totalServicesCount'))),
                                           action="GET_TEXT")
        utility.execLog("Total service count is '{}'".format(service_section))
        if service_section == "0":
            utility.execLog("No Service has been deployed, hence exiting")
            return "true"
        else:
            object_names = ['critical', 'warning', 'inProgress', 'pending', 'healthy']
            # object_names = ['critical', 'warning', 'inProgress', 'pending', 'healthy', 'canceled'] devs removed 'canceled' from UI
            for each_name in object_names:
                service = self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.DashboardObjects(each_name))))
                service_name = service.text
                service_count = get_first_num(service_name)
                if service_count is 0:
                    utility.execLog('"{}" number is "{}", so will click on the next one'.format(each_name.capitalize(), service_count))
                    continue
                service.click()
                utility.execLog("Clicked on '{}'".format(each_name.capitalize()))
                status = self.get_Services_title(service_name)
                self.selectOption("Dashboard")
                utility.execLog("Verified '{}' Services page".format(each_name.capitalize()))
            return status

# HCL 26/11/2015
    def get_Services_title(self, service_state):
        service_page = self.handleEvent(
            EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('pageTitle'))), action="GET_TEXT")
        if 'In-Progress' in service_state:
            utility.execLog("Verified Page title is Service for: '{}'".format(service_page))
            return "true"
        utility.execLog('Verifying if "{}" is in "{}"'.format(service_page, service_state))
        if service_page in service_state:
            utility.execLog("Verified Page title is Service for: '{}'".format(service_state))
            return "true"
        else:
            utility.execLog("Page title is not verified for: '{}'".format(service_state))
            return "false"

# HCL 27/11/2015
# No usage?
    def get_Services(self):

        time.sleep(5)
        Service_page_title = self.browserObject.find_element_by_xpath(".//*[@id='page_services']/header/div/a").text
        utility.execLog(Service_page_title )

# HCL 27/11/2015
    # TODO after Service page update
    def get_ViewLink(self, option):

        if option == "Total Services":
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.DashboardObjects('totalServices'))), action="CLICK")
            utility.execLog("Clicked on Total Services link")
            pageObject = Services.Services(self.browserObject)
            utility.execLog("Validating Page title")
            pageObject.validatePageTitle()
            pageObject.getOptions()
            pageObject.getCategories()
            self.browserObject = globalVars.browserObject
            return "true"
        if option == "View All":
            View_All_link_attribute = self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('viewAll'))), attributeName="disabled", action="GET_ATTRIBUTE_VALUE")
            if View_All_link_attribute:
                utility.execLog("TestCase_NG1-TC-2591 is passed : View All link is disabled for Standard User")
                self.browserObject = globalVars.browserObject
                return "true"

            else:
                utility.execLog("TestCase_NG1-TC-2591 is failed")
                return "false"

        if option == "recentActivityViewAll":
            utility.execLog("Navigating to recentlActivityViewAll Section on Dashboard")
            self.handleEvent(EC.element_to_be_clickable((By.ID, self.DashboardObjects('viewAll'))), action='CLICK')
            utility.execLog("Clicked on ViewAlllink")

            pageObject = Logs.Logs(self.browserObject)
            utility.execLog("Validating Page title")
            self.browserObject = pageObject.getDetails()

            self.browserObject = globalVars.browserObject
            return "true"
    # waiting for templates page re-factory
    def get_RecentActivity (self, name, option):

        expected_activity_string = "Deployment job "+str(name)+" completed for service template Test Template."
        expected_activity_string_standard = "Deployment job "+str(name)+" has been started for service template Test Template."
        recent_activity_text = ""

        utility.execLog("Navigate to first notification list")
        n = 8
        while n > 0:
            try:
                recent_activity_text = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('recentActivityText'))), action='GET_TEXT')
                break
            except:
                n -= 1
                self.browserObject.refresh()
                time.sleep(60)
                utility.execLog("Page is reloaded %s"%str(n))
                continue
        if self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('viewAll'))), attributeName="disabled", action="GET_ATTRIBUTE_VALUE"):
            return "true"

        utility.execLog(recent_activity_text)
        utility.execLog(expected_activity_string)
        if option == "Standard":
            if recent_activity_text == expected_activity_string_standard:
                utility.execLog("Test Case passed")
                return "true"
            else:
                return "false"
        else:
            if recent_activity_text == expected_activity_string:

                utility.execLog("Test Case passed")
                return "true"

            elif recent_activity_text == expected_activity_string_standard:

                utility.execLog("Test Case passed")
                return "true"

            else:

                return "false"
    # Need to test this when Services in Dashboard will be visible(JIRA bug ASM-8888)
    def get_DashboardServices(self):
        total_services = self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('totalServicesCount'))), action='GET_TEXT')
        if total_services == '0':
            utility.execLog('No services are found, total is: {}'.format(total_services))
            return "true"

        all_services = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('deployedBy'))))
        for i, each_service in enumerate(all_services):
            deployed_by_text = total_services[i].text
            utility.execLog(deployed_by_text)
            if (deployed_by_text == "admin")or(deployed_by_text == "autostandard")or(deployed_by_text == "standard"):
                utility.execLog("verified deployed by user")

            else:
                utility.execLog("Test Case failed")
                return "false"
        utility.execLog ("Test Case passed")
        return "true"

    def get_displayServers(self, option, servers_name_list):

        utility.execLog(servers_name_list)
        serverpool = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('viewServerPool'))))
        servers_list = []
        for i, server in enumerate(serverpool):
            server_name = server[i].text
            server_name = utility.convertUTA(server_name)
            servers_list.append(server_name)
            utility.execLog(servers_list)
        if servers_list == servers_name_list:
            return "true"
        else:
            return "false"
            # utility.execLog("This testcase will fail as servers are not visible on server pool on 172.31.43.1")

    def get_Total_Services(self):

        utility.execLog("Checking various sections at Dashboard page")
        total_services = self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('totalServicesCount'))),
                                          action="GET_TEXT")
        if total_services == "0":
            utility.execLog("No services have been deployed, '{}'".format(total_services))
            return "true"

        utility.execLog('Total Services "{}"'.format(total_services))
        actual_total_services = 0
        service_states = ['critical', 'warning', 'inProgress', 'pending', 'healthy']
        # object_names = ['canceled'] 'canceled' removed from UI
        for service in service_states:
            service_state = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects(service))),
                                             action="GET_TEXT")
            service_count = get_first_num(service_state)
            utility.execLog('Service count for "{}" is "{}"'.format(service.capitalize(), service_count))
            actual_total_services += service_count

        utility.execLog('Total services found is "{}"'.format(actual_total_services))
        if actual_total_services == int(total_services):
            utility.execLog("Verified sum of individual service is equal to Total Services, {} == {}".format(actual_total_services, total_services))
            return "true"
        else:
            return "false"

    def get_Server_Utilization(self):

        utility.execLog("get_Server_Utilization()..Dashboard")
        serverpool = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('pools'))))

        for index in xrange(0, len(serverpool)):
            server = serverpool[index]
            try:
                utility.execLog("Checking '{}'".format(server.text))
            except Exception as e:
                utility.execLog('Error was raised as: "{}"'.format(e))
                serverpool = self.handleEvent(
                    EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('pools'))))
                server = serverpool[index]
            server_name = server.text
            server_name = str(server_name)
            server_name = server_name.split("\n", 2)[1]
            each_server_used = get_first_num(server_name)
            each_total_server = get_second_num(server_name)
            total_actual_server = 0

            utility.execLog(("Total used server is : %s" % str(each_server_used)))
            utility.execLog(("Total servers is : %s" % str(each_total_server)))
            server_name = server.find_element_by_xpath('./h4/a/span').text
            if each_total_server is 0:
                utility.execLog("'{}' pool has 0 servers, so moving on next server pool".format(server_name))
                continue
            server.find_element_by_xpath('./h4/a').click()

            # In Resources page
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('serverTable'))))
            # Fetch Table header Details
            utility.execLog("Able to identify user Table")
            # Fetch Resource Details
            tBodyRows = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('numberOfServers'))))
            servers_status = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('serversStatus'))))
            rows = xrange(0, len(tBodyRows))
            number_of_server_used = 0
            utility.execLog(rows)
            try:
                length = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('itemsFound'))), action="GET_TEXT")
            except:
                length = 0
            utility.execLog("Length of pool:%s" % length)
            if length < 1:
                utility.execLog("No Servers available hence exiting")
                return "false"

            for rindex in rows:
                colls = servers_status[rindex].text
                utility.execLog("State of Server:%s " % str(colls))

                if str(colls) == "In Use":
                    number_of_server_used += 1
                    utility.execLog(number_of_server_used)
                total_actual_server = length
            utility.execLog("Verified total servers for total_actual_server: %s" % str(total_actual_server))
            utility.execLog("Verified total servers for each_total_server: %s" % str(each_total_server))
            if number_of_server_used == int(each_server_used):
                utility.execLog("Verified number of servers used for " + server_name)
                if int(total_actual_server) == int(each_total_server):
                    utility.execLog("Verified total servers for " + server_name)
                else:
                    utility.execLog("Test Case failed for " + server_name)
                    return "false"
            self.loadPage()

        utility.execLog("Verified for All Servers")
        return "true"

    def get_TotalServer_Utilization(self):

        serverpool = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('pools'))))
        total_server_pool_dashboard = len(serverpool)
        utility.execLog(total_server_pool_dashboard)
        self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.DashboardObjects('globalPool'))),
                         action="CLICK")
        serverpool_resourcespage = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('serverPools'))))
        total_server_pool_resources = len(serverpool_resourcespage)
        if total_server_pool_dashboard == total_server_pool_resources:
                utility.execLog("Verified number of server pools at dashbaord page")
                return "true"
        else:
            utility.execLog("TestCase failed")
            return "false"

    def get_TotalStorage_Volume(self):

        serverpool = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('storage_group_used'))))
        Total__used_storage = 0
        Total_Storage = 0
        for server in serverpool:
            each_server_information = server.text
            if each_server_information == "":
                break

            utility.execLog(each_server_information)
            storage = each_server_information.split()
            used_storage = storage[0]
            utility.execLog(used_storage)

            totalstorage = storage[3]
            utility.execLog(totalstorage)
            Total__used_storage += int(used_storage)
            Total_Storage += int(totalstorage)
            Total__used_storage = int(Total__used_storage)
            Total_Storage = int(Total_Storage)

        utility.execLog(Total__used_storage)
        calculated_percent = float(Total__used_storage*100/Total_Storage)
        calculated_percent_round = round(calculated_percent)
        calculated_percent = int(calculated_percent_round)
        utility.execLog(calculated_percent)

        utility.execLog("Total used storage is : %s"%str(Total__used_storage) + ' GB')
        utility.execLog("Total storage is : %s"%str(Total_Storage) + ' GB')
        utility.execLog("Total storage Volume Utilization is : %s"%str(calculated_percent) + '%')
        time.sleep(2)
        if Total__used_storage == 0:
            utility.execLog("Storage Utilization is zero hence exiting")
            return "true"
        time.sleep(5)
        actions = ActionChains(self.browserObject)
        retry = 5
        while retry > 0:
            try:
                utility.execLog("Inside retry")
                element = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.DashboardObjects('storage_used'))))
                utility.execLog("Size of element => %s " % str(element.size))
                utility.execLog("Location of element => %s " % str(element.location))
                utility.execLog("Tooltip")
                actions.move_to_element_with_offset(element, 2, 10).perform()
#                 actions.move_to_element_with_offset(element,0,0).click()
#                 utility.execLog("tooltip1")
#                 element = WebDriverWait(self.browserObject, 10).until(EC.presence_of_element_located((By.XPATH, "//*/h3[contains(text(),'Total Storage Capacity')]/parent::*//*[local-name()='g' and @class='highcharts-series highcharts-tracker']//*[local-name()='path']")))
#                 utility.execLog("tool")
#                 actions.move_to_element_with_offset(element,0,0).click_and_hold()
#                 utility.execLog("tooltip2")
                tool_tip = self.handleEvent(EC.presence_of_element_located((By.XPATH, self.DashboardObjects('storage_used_tooltip'))))
                #actions.move_to_element(self.browserObject.find_element_by_xpath("//*/h3[contains(text(),'Total Storage Capacity')]/parent::*//*[local-name()='g' and @class='highcharts-series highcharts-tracker']//*[local-name()='path' and @fill ='#0685C2']").click())
                utility.execLog("tool tip element => %s" % str(tool_tip.text))
                statement = tool_tip.text
                if statement != "":
                    break
                else:
                    utility.execLog("else..")
                    retry -= 1
            except:
                utility.execLog("exception..")
                retry -= 1

        #actions.move_to_element(self.browserObject.find_element_by_xpath("//*/h3[contains(text(),'Total Storage Capacity')]/parent::*/div[@id='dashboardStorageCapacityPie']/div/div[@class='highcharts-tooltip']/span")).click_and_hold()

        utility.execLog("statement")
        utility.execLog("tool tip text =< %s" % str(statement))
        utility.execLog("found")
        values = statement.split()
        Storage_Volume_percent = values[2]
        Storage_Volume = Storage_Volume_percent.split('%')
        Storage_Volume_final = Storage_Volume[0]
        utility.execLog("Total Used Volume on Graph tooltip is : %s" % str(Storage_Volume_final)+'%')

        if calculated_percent == int(Storage_Volume_final):

            utility.execLog("Verified number of servers used ")
            return "true"
        else:
            return "false"

    def get_Storage_Volume(self):

        storagepool = self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('storageGroup'))))
        free_space_compllent = 0.0
        total_storage_compllent = 0.0
        total_storage_equallogic = 0.0
        total_freespace_equallogic = 0.0
        storage_unit1 = ""
        for storage in storagepool:
            storage_name = storage.find_element_by_xpath('./span[1]').text
            storage_name = utility.convertUTA(storage_name)
            utility.execLog('Storage name - "{}"'.format(storage_name))
            storage_text = storage.find_element_by_xpath('./span[2]').text
            utility.execLog(storage_text)
            storages_data = storage_text.split()
            each_storage_used = storages_data[0]
            each_storage_used = int(each_storage_used)
            each_total_storage = storages_data[3]
            each_total_storage = int(each_total_storage)

            utility.execLog(("Total used storage is: %s" % str(each_storage_used)) + ' GB ')
            utility.execLog(("Total  storage is: %s" % str(each_total_storage)) + ' GB ')
            storage.click()
            time.sleep(5)
            utility.execLog("Able to identify Resources Table")

            # Fetch Resource Details
            tRows= self.handleEvent(EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('resources'))))
            rows = len(tRows)
            utility.execLog("Number of resources found on Resources page: '{}'".format(rows))
            for row in tRows:
                row.find_element_by_xpath('./td[1]').click()
                col_value = row.find_element_by_xpath('./td[10]').text

                storage_group = (str(col_value)).strip()
                utility.execLog('Storage name is "{}" and storage group is "{}"'.format(storage_name, storage_group))
                if storage_name == "Dell EqualLogic Group" and storage_group == "Dell  EqualLogic":
                    if int(each_storage_used) == int(each_total_storage):
                        free_space = 0
                    else:
                        free_space_text = self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('freeGroupSpace'))), action="GET_TEXT")
                        # self.browserObject.execute_script("arguments[0].click();", eql)
                        # free_space_text = self.browserObject.execute_script("return arguments[0].textContent;", eql)
                        utility.execLog("Free space: %s" % free_space_text)
                        free_space = free_space_text.split()
                        free_space = free_space[0]
                        free_space = utility.convertUTA(free_space)
                    total_freespace_equallogic += float(free_space)
                    utility.execLog(total_freespace_equallogic)

                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.DashboardObjects('lnkViewDeviceDetails'))), action="CLICK")

                    # TODO: will need to finish the rest of function, after Resources page is updated for 8.4 UI
                    total_storage_text = self.browserObject.find_element_by_id("storagepooltotal").text
                    total_storage = total_storage_text.split()
                    total_storage = total_storage[0]
                    total_storage = utility.convertUTA(total_storage)
                    utility.execLog(total_storage)
                    total_storage_equallogic = float(total_storage_equallogic) + float(total_storage)
                    total_storage_equallogic_round = round(total_storage_equallogic)
                    self.selectOption("Resources")

                if storage_name == "Dell Compellent Arrays" and storage_group == "Dell  Compellent":
                    if int(each_storage_used) == int(each_total_storage):
                        free_space = 0
                    else:
                        cplelmt = self.browserObject.find_element_by_xpath(".//*[@id='compellentfreediskspace']")
                        free_space_text= self.browserObject.execute_script("return arguments[0].textContent;", cplelmt)
                        utility.execLog(free_space_text)
                        free_space = free_space_text.split()
                        free_space =free_space[0]
                        free_space= utility.convertUTA(free_space)
                    free_space_compllent = float(free_space_compllent) + float(free_space)
                    utility.execLog(free_space_compllent)
                    time.sleep(2)
                    self.browserObject.find_element_by_id("lnkViewDeviceDetails").click()
                    time.sleep(5)
                    total_storage_text =  self.browserObject.find_element_by_id("storagediskspacetotal").text
                    utility.execLog("total_storage_text")
                    utility.execLog(total_storage_text)
                    total_storage = total_storage_text.split()
                    total_storage1 = total_storage[0]
                    total_storage1 = utility.convertUTA(total_storage1)
                    storage_unit1 = total_storage[1]
                    utility.execLog("storage_unit1")
                    utility.execLog(storage_unit1)
                    utility.execLog("storage_unit1")
                    total_storage_compllent = float(total_storage_compllent) + float(total_storage1)
                    utility.execLog(total_storage_compllent)

                    navObject = Navigation.Navigation(self.browserObject)
                    navObject.selectOption("Resources")
            self.selectOption("Dashboard")

        if storage_name == "Dell Compellent Arrays":
            utility.execLog("Dell Compellent Arrays")
            if storage_unit1 == 'TB':
                time.sleep(3)
                utility.execLog("storage_unit1 == 'TB':")
                total_storage_compllent = float(total_storage_compllent*1024)

            total_storage_compllent = round(float(total_storage_compllent))
            total_storage_compllent = int(total_storage_compllent)
            utility.execLog("Total Storage Compellent is "+str(total_storage_compllent))
            used_storage = float(total_storage_compllent) - float(free_space_compllent)
            used_storage = round(float(used_storage))
            used_storage = int(used_storage)
            utility.execLog("Used Storage Compellent is "+str(used_storage))
            time.sleep(3)

            if ((each_storage_used == used_storage) and (each_total_storage ==  total_storage_compllent)):
                utility.execLog("Verified for Storage : " + storage_name )
                status =  "true"
            else :
                utility.execLog("failed for storage : " + storage_name)
                status =  "false"


        if storage_name == "Dell EqualLogic Group":
            utility.execLog("Dell EqualLogic Group")

            used_storage = float(total_storage_equallogic) - float(total_freespace_equallogic)
            used_storage = round(float(used_storage))
            used_storage = int(used_storage)

            if ((each_storage_used == used_storage) and (each_total_storage == int(total_storage_equallogic_round))):
                utility.execLog("Verified for Storage : " + storage_name )
                status = "true"

            else:
                utility.execLog("failed for storage : " + storage_name)
                status = "false"

        return status

    # Created for TC-2496
    def getDeployServiceButtonStatus(self):
        """
        Verify export to file button is disabled or enabled.
        """
        utility.execLog("Verifying Deploy Service Button state.")
        deploy_link = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('deployLink'))),
                                       action='GET_ATTRIBUTE_VALUE', attributeName='disabled')
        utility.execLog('Deploy link is "{}"'.format(deploy_link))
        if deploy_link == 'true':
            return True
        elif deploy_link is None:
            return False

    def get_TotalResources(self):
        """
        API to fetch all the resources on the dashboard page
        """

        def check_for_pages(amount):
            total_resources = amount
            try:
                num_of_pages = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('pagination'))), action="GET_TEXT", retry=False, wait_time=1)
                num_of_pages = get_first_num(num_of_pages)
            except:
                utility.execLog('Pagination element not found so, num. of pages is 1')
                return total_resources
            utility.execLog('Number of pages is "{}"'.format(num_of_pages))
            page = 1
            while num_of_pages > page:
                utility.execLog("Moving to Page: %s" % str(page + 1))
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('nextPage'))),
                                 action="CLICK")
                tRows = self.handleEvent(
                    EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('resources'))))
                count = len(tRows)
                total_resources += count
                page += 1
            return total_resources

        resources = ['chassis', 'servers', 'switches', 'storage']
        select_resources = ['Dell Chassis', 'Servers', 'Switches', 'Storage']

        for resource, select_resource in zip(resources, select_resources):
            resource_count = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.DashboardObjects(resource))), action='GET_TEXT')
            utility.execLog('Number of "{}" is "{}" in Dashboard page'.format(resource.capitalize(), resource_count))
            self.selectOption("Resources")
            if resource == 'chassis':
                self.handleEvent(EC.element_to_be_clickable((By.ID, self.DashboardObjects('showFilterLink'))),
                                     action="CLICK", wait_time=5)
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('ddlView'))),
                             action="SELECT", setValue=select_resource)
            tRows = self.handleEvent(
                EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('resources'))))
            count = len(tRows)
            count = check_for_pages(count)
            utility.execLog('Number of "{}" is "{}" in Resources page'.format(select_resource, count))
            if int(resource_count) != count:
                utility.execLog('"{}" is not equal to "{}"'.format(resource_count, count))
                return "false"
            self.selectOption("Dashboard")

        return "true"

    # Created for TC-2635

    def getCreateNewTemplateButtonStatus(self):
        """
        Verify Create New Template button is disabled or enabled.
        """
        utility.execLog("Verifying Add a Template Button state.")
        add_template_btn = self.handleEvent(
            EC.visibility_of_element_located((By.ID, self.DashboardObjects('createTemplateLink'))),
            action='GET_ATTRIBUTE_VALUE', attributeName='disabled')
        utility.execLog('Add Template button is "{}"'.format(add_template_btn))
        if add_template_btn == 'true':
            return True
        elif add_template_btn is None:
            return False

    # Created for TC-2635

    def getAddExistingServiceButtonStatus(self):
        """
        Verify Add Existing Service button is disabled or enabled.
        """
        utility.execLog("Verifying Add Existing Service Button state.")
        existing_service_btn = self.handleEvent(
            EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('addExistingService'))),
            action='GET_ATTRIBUTE_VALUE', attributeName='disabled')
        utility.execLog('Add Existing Service button is "{}"'.format(existing_service_btn))
        if existing_service_btn == 'true':
            return True
        elif existing_service_btn is None:
            return False

    # TODO: need to test function after Resources page will be ready
    def get_Server_Health(self, option):
        utility.execLog("Server Health..")

        def server_health_info():
            total_server_expected_val = self.handleEvent(
                EC.visibility_of_element_located((By.ID, self.DashboardObjects('serverCount'))), action='GET_TEXT')
            server_critical = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('serverCritical'))),
                action='GET_TEXT')
            error_server_expected_val = get_first_num(server_critical)
            server_warning = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('serverWarning'))), action='GET_TEXT')
            warning_server_expected_val = get_first_num(server_warning)
            server_healthy = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('serverHealthy'))), action='GET_TEXT')
            healthy_server_expected_val = get_first_num(server_healthy)
            server_unknown = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('serverUnknown'))), action='GET_TEXT')
            unknown_server_expected_val = get_first_num(server_unknown)

            servers = [total_server_expected_val, error_server_expected_val, warning_server_expected_val,
                       healthy_server_expected_val, unknown_server_expected_val]
            return servers

        try:
            utility.execLog("Mouse is moving")
            try:
                actions = ActionChains(self.browserObject)
                server_health_critical = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('server_health_critical'))))
                utility.execLog("Size of element => %s " % str(server_health_critical.size))
                utility.execLog("Location of element => %s " % str(server_health_critical.location))
                actions.move_to_element_with_offset(server_health_critical, 60, 5).perform()
                tool_critical = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('server_health_tooltip'))))
                utility.execLog(tool_critical.text)
                stm = tool_critical.text
                va = stm.split()
                utility.execLog(va[1])
            except:
                utility.execLog("No Critical tool tip")

            servers = server_health_info()
            health = ['All', 'Critical', 'Warning', 'Healthy', 'Unknown']

            pageObject = Resources.Resources(self.browserObject)
            globalVars.browserObject, status, result = pageObject.loadPage()

            for h, server in zip(health, servers):
                if server != 0:
                    pageObject.selectResources_Health("Servers", h)
                    utility.execLog("Successfully selected Resources with Healthy all")
                    server_actual_val = self.handleEvent(
                        EC.presence_of_all_elements_located((By.XPATH, self.DashboardObjects('devicesFound'))), action="GET_TEXT")
                    if server == int(server_actual_val):
                        utility.execLog("'{}' Server Health is correct and complete".format(health))
                        utility.execLog('"{}" is equal to "{}"'.format(server, server_actual_val))
                    else:
                        return self.browserObject, False, "'{}' Server Health is not correct and complete".format(health)

            total_server = sum(servers)
            utility.execLog("Total Server: '{}'".format(total_server))

            pageObject.singleResourceDiscovery()
#             pageObject1 = Dashboard.Dashboard(self.browserObject)
            globalVars.browserObject, status, result = self.loadPage()
            utility.execLog("Successfully loaded Dashboard page")
            servers = server_health_info()
            total_server = sum(servers)

            total_server_expected_val_new = self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('serverCount'))), action='GET_TEXT')
            utility.execLog("total server after discoverd:")
            utility.execLog(total_server_expected_val_new)
            if total_server == int(total_server_expected_val_new):
                utility.execLog("Test Case Passed")
                return self.browserObject, True, "Successful"
            else:
                utility.execLog("Test Case Failed")
                return self.browserObject, False, "not Successful"

        except Exception as e:
            return self.browserObject, False, "Unsuccessful '%s'" % e

    def verify_ServersInUse(self):

        utility.execLog('Hovering over "Servers in Use" utilization piece of pie')
        try:
            time.sleep(5)
            actions = ActionChains(self.browserObject)
            server_used = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('server_used'))))
            server_used_size = server_used.size
            utility.execLog("Size of element => %s " % str(server_used_size))
            utility.execLog("Location of element => %s " % str(server_used.location))
            if server_used_size['width'] < 1:
                return 0
            actions.move_to_element_with_offset(server_used, 5, 2).perform()
            tool_tip = self.handleEvent(
                EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('server_used_tool'))),
                action="GET_TEXT")
            utility.execLog(tool_tip)
            values = tool_tip.split()
            Actual_Servers_In_Use = values[3]
            utility.execLog(Actual_Servers_In_Use)
            return Actual_Servers_In_Use
        except Exception as e:
            utility.execLog("Exception while trying to hover over on  of server utilization  :: Error -> %s" % str(e))
            raise e

#     HCL 3329

    def verifyLinksAndPages(self):
        utility.execLog("Going to verify links under Quick Action")

        buttons = ['deployLink', 'createTemplateLink', 'addExistingService']
        header = ['Deploy Service', 'Create Template', 'Add Existing Service']
        popups = ['popUpDeploy', 'popUpTemplate', 'popUpAddService']
        try:
            for button, popup in zip(buttons, popups):
                self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.DashboardObjects(button))), action="CLICK")
                h4 = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects(popup))), action="GET_TEXT")
                i = buttons.index(button)
                if h4 == header[i]:
                    utility.execLog("'{}' link is verifying".format(header[i]))
                    self.handleEvent(EC.element_to_be_clickable((By.ID, self.DashboardObjects('btnClose'))),
                                     action='CLICK', wait_time=5, retry=False)
                    if h4 != 'Create Template':
                        self.handleEvent(EC.element_to_be_clickable((By.ID, self.DashboardObjects('btnConfirm'))),
                                         action='CLICK')
                    utility.execLog("'{}' link verified successfully".format(header[i]))
                    time.sleep(2)
                else:
                    return self.browserObject, False, "'{}' link not verified successfully".format(header[i])

            return self.browserObject, True, "Successful"
        except Exception as e:
            return self.browserObject, False, "Unsuccessful '%s'" % e

    def getDashboard_AllLinks(self):
        utility.execLog(" Going to verify getDashboard_AllLinks")
        try:
            self.verifyLinksAndPages()
            self.getSection_Learn()

            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.DashboardObjects('totalServices'))), action="CLICK")
            txt6 = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('servicesHeader'))),
                                    action="GET_TEXT")
            if txt6 == "Services":
                utility.execLog("Services link is verified Successfully")
            else:
                return self.browserObject, False, "Service is not verified successfully"
            self.loadPage()

            # Total Servers
            self.handleEvent(EC.element_to_be_clickable((By.XPATH, self.DashboardObjects('totalServers'))),
                             action="CLICK")
            txt7 = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('resourcesHeader'))),
                                    action="GET_TEXT")
            if txt7 == "Resources":
                utility.execLog("Total Servers link is verified")
            else:
                return self.browserObject, False, "Total Servers are not verified successfully"
            self.loadPage()

            # Checking Services' links
            object_names = ['critical', 'warning', 'inProgress', 'pending', 'healthy']
            # object_names = ['canceled'] 'canceled' removed from UI
            for each_name in object_names:
                service = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects(each_name))))
                service_name = service.text
                service_count = get_first_num(service_name)
                if service_count is 0:
                    utility.execLog('"{}" number is "{}", so will click on the next one'.format(each_name.capitalize(), service_count))
                    continue
                service.click()
                utility.execLog("Clicked on '{}'".format(each_name.capitalize()))
                status = self.get_Services_title(service_name)
                if status != 'true':
                    return self.browserObject, False, "Page title is not verified for: '{}'".format(service_name)
                self.selectOption("Dashboard")
                time.sleep(5)
                utility.execLog("Verified '{}' Services page".format(each_name.capitalize()))

            # Checking Servers' links
            servers = ['serverCritical', 'serverWarning', 'serverHealthy', 'serverUnknown']
            for server in servers:
                self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects(server))),
                                 action="CLICK")
                header = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('resourcesHeader'))),
                                          action="GET_TEXT", wait_time=5, retry=False)
                server = get_after_capital(server)
                if header == "Resources":
                    utility.execLog("'{}' servers are verified".format(server))
                else:
                    return self.browserObject, False, "'{}' servers are verified".format(server)
                self.loadPage()

                utility.execLog("'{}' servers are verified successfully".format(server))

            # Checking Logs' link
            self.handleEvent(EC.visibility_of_element_located((By.ID, self.DashboardObjects('viewAll'))),
                             action="CLICK")
            logs_header = self.handleEvent(EC.visibility_of_element_located((By.XPATH, self.DashboardObjects('logsHeader'))),
                                           action="GET_TEXT")
            if logs_header == "Logs":
                utility.execLog("View All Links are verified")
            else:
                return self.browserObject, False, "View All Links are not verified successfully"
            self.loadPage()

            utility.execLog("View All Links are verified successfully")

            return self.browserObject, True, "Successful"
        except Exception as e:
            return self.browserObject, False, "Unsuccessful '%s'" % e

    # Test cases which use this function needs refactor
    def numberResourceDiscovered(self, resource):

        # self.browserObject.find_element_by_xpath("//body[@id='appHtml']").click
        try:
            dashboardResourceDiscovered = self.browserObject.find_element_by_xpath("//div[contains(@data-bind, '{}discovered')]".format(resource["Type"].lower())).text
            return self.browserObject, True, dashboardResourceDiscovered
        except Exception as e:
            return self.browserObject, False, "Failed to find number of resources of type {} :: Error :: {}"\
                .format(resource["Type"], e)

    # Test cases which use this function needs refactor
    def getActivityMessage(self, resource):

        try:
            dashboardRecentActivity = self.browserObject.find_element_by_xpath("//ul/li[contains(.,'{}')]"\
                                                                               .format(resource)).text
            return self.browserObject, True, dashboardRecentActivity
        except Exception as e:
            return self.browserObject, False, "Unable to find log message for {} :: Error -> {}"\
                                .format(resource, e) + format_exc()

# Helpful functions for Dashboard class


def get_first_num(text):
    num = int(re.search('\d+', text).group())
    return num


def get_second_num(text):
    num = int(re.search('\s\d+', text).group())
    return num


def get_after_capital(text):
    word = re.findall('[A-Z][^A-Z]*', text)
    return word[0]
