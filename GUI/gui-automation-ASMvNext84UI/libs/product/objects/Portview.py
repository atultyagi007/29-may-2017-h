"""
Author: Pavan G/Saikumar Kalyankrishnan
Created: Mar 14th 2016/Mar 14th 2017
Description: Object Repository for Portview Page
"""


class Portview():
    def PortviewObjects(self, key):
        PortviewObjects = {'portViewSelectConnection': "dLabel",
                           'tableHeadXpathNPAR': ".//table[contains(@id, 'nicPartitionDetailsTableNICIntegrated1port1')]/thead/tr/th",
                           'tableHeadXpathNIC': "(//*[@id='nicDetailsTable']/thead)[1]/tr/th",
                           'tableHeadXpathIoDetails': "(//*[@id='iomDetailsTable']/thead)[1]/tr/th",
                           'tableHeadXpathToRDetails': "(.//*[@id='torDetailsTable']/thead)[1]/tr/th",
                           # 'toRWarningMessage': "//div[@class='toggle_nicdetails00 in section-indent']/div/table[@id='torDetailsTable']/tbody/tr[1]/td/div/h4",
                           'tableHeadXpathVlanDetails': "(//*[@id='valnDetailsTable']/thead)[1]/tr/th",
                           'rowsXpathNPAR': ".//table[contains(@id, 'nicPartitionDetailsTable')]/tbody/tr",
                           'rowsXapthNIC': ".//*[@id='nicDetailsTable']/tbody/tr",
                           'rowsXpathIO': "//table[@id='iomDetailsTable']/tbody/tr",
                           'rowsXpathToR': "//table[@id='torDetailsTable']/tbody/tr[2]",
                           'rowsXpathVlan': "(.//*[@id='valnDetailsTable'])[1]/tbody/tr",
                           'tableXpathNPAR': ".//*[contains(@id, 'nicPartitionDetailsTableNICIntegrated')]",
                           'tableXpathNIC': "//table[@id='nicDetailsTable']",
                           'tableXpathIODetails': "//table[@id='iomDetailsTable']",
                           'tableXpathToR': "//table[@id='torDetailsTable']",
                           'tableXpathVLAN': "//table[@id='valnDetailsTable']",
                           'toRPorts': ".//*[name()='svg' and contains(@ng-repeat, 'downlink in torSwitch')]/*[name()='text']/*[name()='tspan']",
                           'ioModuleDownLinkValue': ".//*[@class='IOMSlotElements']/*[name()='svg']//*[name()='svg']/*[name()='text']/*[name()='tspan']",
                           'nicInformationValue': ".//*[@id='NicPortClick']/../*[name()='text' and contains(text(), 'Port')]",
                           'toRHealthCheck': ".//*[name()='path' and contains(@class, 'SwitchLabelBG healthy')]",
                           'ioModuleUplinkCheck': ".//*[contains(@class, 'IOMLabelTop')]/*[name()='path' and contains(@class, 'healthy')]",
                           'ioModuleDownLinkCheck': ".//*[contains(@class, 'IOMLabelBottom')]/*[name()='path' and @class='healthy']",
                           'ioUplinkPorts': ".//*[contains(@class, 'IOMLabelTop')]//*[name()='tspan']",
                           'nicCardLinkCheck': ".//*[contains(@class, 'TextBG healthy')]",

                           # Locators not used in pages\Portview.py

                           # 'vlanClick': "//ul[@class='list-unstyled list-inline vlansList']/li/div/ul/li[1]",
                           # 'viewAllSettings': "//a[@id='lnkViewSettings']",
                           # 'serviceSettingsonViewAllSettings': ".//*[@id='page_service_settings']",
                           # 'presenceOfServerinViewAll': "//form[@id='formDeploymentDetails']/ul/li/span[1]/h3[contains(text(), 'Server')]/parent:: span",
                           # 'presenceOfESXIinViewAllSettings': "//span[@id='btncomponenttoggletemplate']/h3[contains(text(), 'Server')]/parent::span/parent::li/fieldset/div[2]/ul/li/div/label[contains(.,'OS Image')]/parent::div/parent::li/div[2]/p[contains(.,'esxi')]",
                           # 'cancelButtonOnViewAllSettings': ".//*[@id='cancel_form_settings']",
                           # 'mainFormID': ".//*[@id='appHtml']",
                           # 'maximizeASMMenu': "//button[@class='click-toggle-nav btn btn-link button-nav']",
                           # 'resourcesOption': ".//*[@id='resourcessub-omp']",
                           }
        return PortviewObjects.get(key)
