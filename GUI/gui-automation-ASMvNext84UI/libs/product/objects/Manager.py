"""
Author: Saikumar Kalyankrishnan
Created: Mar 9th 2017
Description: Object Repository for Manager Page
"""

class Manager():
    def ManagerObjects (self, key):
        ManagerObjects = {'formLogin': "//form[@name='StartScreen']",

                       }
        return ManagerObjects.get(key)