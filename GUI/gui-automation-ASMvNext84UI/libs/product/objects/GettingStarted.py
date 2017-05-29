"""
Author: Saikumar Kalyankrishnan/Mirlan Kaiyrbaev
Created: Feb 22nd 2017
Description: Object Repository for Getting Started Page
"""

class GettingStarted():
    def GettingStartedObjects (self, key):
        GettingStartedObjects = {'gettingstarted': "gettingstarted",
                                 'lnkNetworks': "lnkNetworks",
                                 'lnkDiscoverResources': "lnkDiscoverResources",
                                 'lnkConfigureInitialSetup': "lnkConfigureInitialSetup",
                                 'lnkConfigureResources': "lnkConfigureResources",
                                 'lnkCreateTemplate': "lnkCreateTemplate",
                                 'restoreNowLink': "restoreNowLink",
                                 'directoryPath': "directoryPath",
                                 'directoryUserName': "directoryUserName",
                                 'backupLocationPassword': "backupLocationPassword",
                                 'encryptionPassword': "encryptionPassword",
                                 'btnTestBackupNowConnection': "btnTestBackupNowConnection",
                                 'closeBtn': ".//button[text()='Close']",
                                 'cancelBackupNow': "cancelBackupNow",
                                 'submitBackupNow': "submitBackupNow",
                                 'btnConfirm': "btnConfirm",
                                 'btnLogin': "btnLogin",
                                 'btnClose': "btnClose",
                                 'initHWconf': ".//*[contains(text(), 'Initial Hardware Configuration')]",
                                 'btnNext': ".//button[text()='Next']",
                                 'popUpHeader': ".//*[contains(@class, 'modal-sm')]//h4",
        }
        return GettingStartedObjects.get(key)
