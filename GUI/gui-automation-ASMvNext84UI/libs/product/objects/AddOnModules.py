"""
Author: Saikumar Kalyankrishnan
Created: Feb 28th 2017
Description: Object Repository for Add-On Module Page
"""

class AddOnModules():
    def AddOnModulesObjects (self, key):
        AddOnModulesObjects = {'title': "//*[@id='page_addonmodules']//h2",
                               'addModule': "modalTest new_addonmodule_link",
                               'addOnModuleTable': "addonmodulesTable",
                               'addOnModulePath': "addonmodulefile",
                               'addOnModuleSave': "addOnModuleForm",
                               'addOnModuleCancel': "addonmodulecancel"
                               }
        return AddOnModulesObjects.get(key)