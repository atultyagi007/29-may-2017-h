"""
Author:  Saikumar Kalyankrishnan
Created:  Feb 21th 2017
Description:  Object Repository commonly used across ASM
"""

class Common():
    def CommonObjects (self, key):
        CommonObjects = {'LoginLogo': "//i[@alt='Application Logo']",
                         'LoginUsername': "loginUsername",
                         'LoginPassword': "loginPassword",
                         'Login': "btnLogin",
                         'GetFormTitle': "//div[@class='modal-content']//h4",
                         'FormClose': "//div[@class='modal-content']//button[@id='btnClose']",
                         'FormHelp': "//div[@class='modal-content']//button[@id='btnHelp']",
                         'FormExpand': "//div[@class='modal-content']//button[@id='btnExpand']",
                         'RedBoxError': "//div[contains(@class, 'alert-danger')]",
                         'RedBoxErrorMessages': "//div[contains(@class, 'alert-danger')]//span[contains(@ng-bind-html, 'error.message')]",
                         'ConfirmYes': "btnConfirm",
                         'ConfirmNo': "//div[@class='modal-content']//button[@ng-click='cancel()']",
                         'AlertClose':"btnOK",
                         'GetRunningJobsCount':".//button[contains(@class, 'alertButton')]"
        }
        return CommonObjects.get(key)