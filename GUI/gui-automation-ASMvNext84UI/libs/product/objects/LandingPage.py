"""
Author:  Saikumar Kalyankrishnan
Created:  Feb 15th 2017
Description:  Object Repository for Landing Page
"""

class LandingPage():
    def LandingPageObjects (self, key):
        LandingPageObjects = {'username': "//div[@class='UserDetails']/span",
                              'appHeaderLeft':  "navHover",
                              'appHeaderRight':  "navbar",
                              'userButton': "//button[@class='btn dropdown-toggle userButton customButton ng-binding']",
                              'helpButton': "//button[contains(@class, 'userButton') and contains(@ng-click, 'help')]",
                              'about': "//button[contains(@ng-click, 'about')]",
                              'help': "//button[contains(@class, 'link') and contains(@ng-click, 'help')]",
                              'aboutForm': "//div[contains(@class, 'about')]",
                              'aboutClose': "btnClose",
                              'showWelcomeScreen': "showgettingstarted",
                              }
        return LandingPageObjects.get(key)