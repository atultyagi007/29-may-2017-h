"""
Author: Saikumar Kalyankrishnan
Created: Feb 15th 2017
Description: Object Repository for Virtual Identity Pool Page
"""

class VirtualIdentityPools():
    def VirtualIdentityPoolsObjects (self, key):
        VirtualIdentityPoolsObjects = {'title': "//*[@id='page_pools']//h1",
                                       'createPool': "new_pool_link",
                                       'exportPool': "btnExportPool",
                                       'deletePool': "delete_pool_link",
                                       'poolsTable': "pools_table",
                                       'selectAllPools': "selectAllPools",
                                       'summarySection': "summary",
                                       'editMACPool': "btnEditMacPool",
                                       'editIQNPool': "btnEditIQNPool",
                                       'editWWPNPool': "btnEditWWPNPool",
                                       'editWWNNPool': "btnEditWWNNPool",
                                       'next': "//div[@class='wizard-nav-buttons']//button[@ng-click='next($event)']",
                                       'back': "//div[@class='wizard-nav-buttons']//button[@ng-click='previous($event)']",
                                       'finish': "//div[@class='wizard-nav-buttons']//button[@ng-click='finish($event)']",
                                       'cancel': "//div[@class='wizard-nav-buttons']//button[@ng-click='cancel($event)']",
                                       'pagePoolInfo': "//p[normalize-space(text())='Step 1 of 6']",
                                       'poolName': "pool_name",
                                       'poolDescription': "pool_description",
                                       'pageVirtualMAC': "//p[normalize-space(text())='Step 2 of 6']",
                                       'pageVirtualIQN': "//p[normalize-space(text())='Step 3 of 6']",
                                       'iqnPrefix': "iqn_prefix",
                                       'pageVirtualWWPN': "//p[normalize-space(text())='Step 4 of 6']",
                                       'pageVirtualWWNN': "//p[normalize-space(text())='Step 5 of 6']",
                                       'pageSummary': "//p[normalize-space(text())='Step 6 of 6']",
                                       'submitConfirm': "//div[normalize-space(text())='Are you sure you want to submit this information?']"
                       }
        return VirtualIdentityPoolsObjects.get(key)