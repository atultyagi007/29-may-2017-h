"""
Author:  Saikumar Kalyankrishnan
Created:  Feb 15th 2017
Description:  Object Repository for Navigation Page

Deprecated starting 8.4.0: 
'Username':  "omp-txtUsername",
'GettingStartedMenu':  "button-getting-started",
'Logout':  "omp-lnkLogout"
"""

class Navigation():
    def NavigationObjects (self, key):
        NavigationObjects = {'Dashboard': "//div[@id='pinaccordion']//a[normalize-space(text())='Dashboard']",
                        'Services': "//div[@id='pinaccordion']//a[normalize-space(text())='Services']",
                        'Templates': "//div[@id='pinaccordion']//a[normalize-space(text())='Templates']",
                        'Resources': "//div[@id='pinaccordion']//a[normalize-space(text())='Resources']",
                        'Settings': "//div[@id='pinaccordion']//a[normalize-space(text())='Settings']",
                        'GettingStartedMain': "//div[@id='pinaccordion']//a[normalize-space(text())='Getting Started']",
                        'QuickActions': "//div[@id='pinaccordion']//button[normalize-space(text())='Quick Actions']",
                        'ExpandMenu': "//div[@id='navHover']//div[contains(@class, 'pull-left top-nav-arrow')]//button",
                        'MenuBar': "//div[@id='navHover']//div[@class='top-menu']",
                        'ExpandSettings': "settings_menu",
                        'SettingsMenu': "pin-settings",
                        'AddOnModules': "//*[@id='pin-settings']//button[normalize-space(text())='Add-On Modules']",
                        'BackupRestore': "//*[@id='pin-settings']//button[normalize-space(text())='Backup and Restore']",
                        'Credentials': "//*[@id='pin-settings']//button[normalize-space(text())='Credentials Management']",
                        'GettingStarted': "//*[@id='pin-settings']//button[normalize-space(text())='Getting Started']",
                        'Jobs': "//*[@id='pin-settings']//button[normalize-space(text())='Jobs']",
                        'Logs': "//*[@id='pin-settings']//button[normalize-space(text())='Logs']",
                        'Networks': "//*[@id='pin-settings']//button[normalize-space(text())='Networks']",
                        'Repositories': "//*[@id='pin-settings']//button[normalize-space(text())='Repositories']",
                        'InitialApplianceSetup': "//*[@id='pin-settings']//button[normalize-space(text())='Initial Appliance Setup']",
                        'Users': "//*[@id='pin-settings']//button[normalize-space(text())='Users']",
                        'VirtualApplianceManagement': "//*[@id='pin-settings']//button[normalize-space(text())='Virtual Appliance Management']",
                        'VirtualIdentityPools': "//*[@id='pin-settings']//button[normalize-space(text())='Virtual Identity Pools']",
                        'Pin': "//div[@id='navHover']//div[@class='top-menu']//button[contains(@ng-click, 'pinNav')]",
                        'Unpin': "unpin",
                        'Close': "//div/div/div[2]/nav/div/div[1]/button[2]",
                             }
        return NavigationObjects.get(key)