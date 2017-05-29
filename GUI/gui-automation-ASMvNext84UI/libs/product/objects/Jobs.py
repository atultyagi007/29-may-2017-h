"""
Author: Saikumar Kalyankrishnan
Created: Feb 23rd 2017
Description: Object Repository for Jobs Page
"""

class Jobs():
    def JobsObjects (self, key):
        JobsObjects = {'title': "//*[@id='page_jobs']//h2",
                       'jobsTable': "JobsGrid",
                       'selectAllJobs': "selectAllJobs",
                       'cancelJobs': "cancelJob"
                       }
        return JobsObjects.get(key)