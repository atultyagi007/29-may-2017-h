"""
Author: Saikumar Kalyankrishnan
Created: Mar 8th 2017
Description: Object Repository for Services Page
"""

class Services():
    def ServicesObjects (self, key):
        ServicesObjects = {'title': "//*[@id='page_services']//h1",

                       }
        return ServicesObjects.get(key)