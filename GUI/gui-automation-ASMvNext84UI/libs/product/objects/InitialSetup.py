"""
Author:  Saikumar Kalyankrishnan
Created:  Feb 15th 2017
Description:  Object Repository for Initial Setup Page
"""

class InitialSetup():
    def InitialSetupObjects (self, key):
        InitialSetupObjects = {'checkSetupWizard': "//h4[@class='modal-title ng-binding']",
                               'wizardWelcome': "//div[@class='wizard-step-buttons']//li[@title='Welcome']/span",
                               'wizardLicensing': "//div[@class='wizard-step-buttons']//li[@title='Licensing']/span",
                               'wizardTZNTP': "//div[@class='wizard-step-buttons']//li[@title='Time Zone and NTP Settings']/span",
                               'wizardProxy': "//div[@class='wizard-step-buttons']//li[@title='Proxy Settings']/span",
                               'wizardDHCP': "//div[@class='wizard-step-buttons']//li[@title='DHCP Settings']/span",
                               'wizardSummary': "//div[@class='wizard-step-buttons']//li[@title='Summary']/span",
                               'titleHeader': "//section[@class='wizard-step']//h2[@class='ng-binding']",
                               'validateSummary': "page_initialsetup_summary",
                               'next': "//div[@class='wizard-nav-buttons']//button[@ng-click='next($event)']",
                               'back': "//div[@class='wizard-nav-buttons']//button[@ng-click='previous($event)']",
                               'finish': "//div[@class='wizard-nav-buttons']//button[@ng-click='finish($event)']",
                               'cancel': "//div[@class='wizard-nav-buttons']//button[@ng-click='cancel($event)']",
                               'serviceTag': "//input[contains(@ng-model,'serviceTag')]",
                               'timeZone': "timeZone",
                               'selectedTimeZone': "//*[@id='timeZone']//option[@selected='selected']",
                               'primaryNTP': "preferredNTPServer",
                               'pNTPValue': "//input[@id='preferredNTPServer' and contains(@class, 'not-empty')]",
                               'secondaryNTP': "secondaryNTPServer",
                               'ntpError': "//div[contains(@class, 'alert-danger')]",
                               'confirmButton': "btnConfirm",
                               'pageWelcome': "//p[normalize-space(text())='Step 1 of 6']",
                               'pageLicensing': "//p[normalize-space(text())='Step 2 of 6']",
                               'pageTZNTP': "//p[normalize-space(text())='Step 3 of 6']",
                               'pageProxy': "//p[normalize-space(text())='Step 4 of 6']",
                               'pageDHCP': "//p[normalize-space(text())='Step 5 of 6']",
                               'pageSummary': "//p[normalize-space(text())='Step 6 of 6']",
                               }
        return InitialSetupObjects.get(key)