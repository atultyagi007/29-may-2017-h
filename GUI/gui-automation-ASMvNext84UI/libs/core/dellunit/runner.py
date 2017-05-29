#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
Dell Test Runner

Runs the provided tests and provides structured output that can be 
serialized easily

"""

import os
import sys

import logging
from dellunit.unittest2.util import strclass
from dellunit.unittest2 import loader

from result import TestResult


class DellTestRunner(object):

    """
    The Dell Test Runner
    """


    def __init__(self, separate_output=False,
                 retry_failed=True, verbosity=0, 
                 logtime=None, timestamp_report=False):
        self.separate_output = separate_output
        self.retry_failed = retry_failed
        self.logger = logging.getLogger('dellunit')
        self.verbosity = verbosity
        self.logtime = logtime
        self.timestamp_report = timestamp_report

    def run(self, tests):
        """
        Run the given test case or test suite.
        """

        result = TestResult(separate_output=self.separate_output,
                            retry_failed=self.retry_failed,
                            verbosity=self.verbosity,
                            logtime=self.logtime,
                            timestamp_report=self.timestamp_report)

        tests(result)  # run the tests - first try

        if self.retry_failed:
            result.retry_pass = True
            result.verbosity = logging.DEBUG
            failed_test_names = self.get_failed_testnames(result)
            default_loader = loader.defaultTestLoader
            failed_tests = default_loader.loadTestsFromNames(failed_test_names)
            if failed_tests._tests:
                self.logger.info('Retrying failed tests...')
                failed_tests(result)  # run the tests - retry

        return result

    def get_failed_testnames(self, result):
        """
        Get the names of failed tests from a result
        """

        for r in result:
            if r['status'] == 'fail':
                yield r['name']
