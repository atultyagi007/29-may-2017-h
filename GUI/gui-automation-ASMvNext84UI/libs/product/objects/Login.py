"""
Author:  Saikumar Kalyankrishnan
Created:  Feb 15th 2017
Description:  Object Repository for Login Page
"""

class Login():
    def LoginObjects (self, key):
        LoginObjects = {'username': "loginUsername",
                        'password': "loginPassword",
                        'remember': "rememberMe",
                        'login': "btnLogin",
                        'loginError': "//div[@class='text-center ng-binding']",
                        'popup': "//div[@class='modal-content']",
                        'checkDefaultPasswordWarning': "chkDismissUseDefaultPasswordModal",
                        'defaultPasswordSubmit': "submitDefaultPasswordForm",
                        'checkGettingStarted': "gettingstarted",
                        'checkDashboard': "page_dashboard",
                        'checkSetupWizard': "//h4[@class='modal-title ng-binding']",
                        'appHeaderLeft': "navHover",
                        'appHeaderRight': "navbar",
                        'userButton': "//button[@class='btn dropdown-toggle userButton customButton ng-binding']",
                        'logout': "//button[@ng-click='logout()']",
        }
        return LoginObjects.get(key)