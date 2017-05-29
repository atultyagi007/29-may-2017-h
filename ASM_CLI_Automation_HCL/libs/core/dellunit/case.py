#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
Dell-specific extensions to unittest.TestCase
"""

import re
import os
import sys
import logging
from ConfigParser import ConfigParser

from dellunit.unittest2 import TestCase

from dellunit.unittest2.case import skip as _skip, SkipTest as SkipTest2
from dellunit.unittest2 import SkipTest


try:
    from tblib import rram
except ImportError:
    pass

TESTCASE_KEY_PATTERN = re.compile(r'@\w+: (\w+)')


def omit(reason):
    """
    Omit decorator
    """

    reason = 'OMIT: %s' % reason
    return _skip(reason)


def block(reason):
    """
    Block decorator
    """

    reason = 'BLOCK: %s' % reason
    return _skip(reason)


def load_config():
    config = ConfigParser()
    if len(sys.argv) > 1:
        if not os.path.exists(sys.argv[1]):
            logging.warn('Unable to locate INI: %s', sys.argv[1])
        config.read(sys.argv[1])
    return config


class DellTestCase(TestCase):

    """
    Base Dell-specific functionality
    """

    longMessage = True

    def __init__(self, *args, **kwargs):
        self.config = load_config()
        self.failure_retry = False
        self._runtime_data = {}
        self._group_urls = []
        self._test_urls = []

        TestCase.__init__(self, *args, **kwargs)
        self.logger = logging.getLogger(self.id())

    def add_group_url(self, name, uri=None):
        """
        Add a url to the test group output
        """
        if uri is None:
            uri = name

        self._group_urls.append((name, uri))

    def add_test_url(self, name, uri=None):
        """
        Add a url to the test case output
        """
        if uri is None:
            uri = name

        self._test_urls.append((name, uri))

    def add_runtime_data(self, name, value):
        """
        Add data to the runtime info block
        """

        self._runtime_data[name] = value

    def omit(self, reason):
        """
        Handle the Dell OMIT state
        """

        self.skipTest('OMIT: ' + reason)

    def block(self, reason):
        """
        Handle the Dell BLOCK state
        """

        self.skipTest('BLOCK: ' + reason)

    def shortDescription(self, doc=None):
        """
        The default implentation is a bit dumb, in that it cannot handle
        docstrings like this one that start with a blank line.
        """

        if not doc:
            doc = self._testMethodDoc

        if doc:
            for line in doc.splitlines():
                line = line.strip()
                if line and not TESTCASE_KEY_PATTERN.match(line):
                    return line

    def fullDescription(self, doc=None):
        """
        Like short description, but get a cleaned-up version of the whole thing
        """

        if not doc:
            doc = self._testMethodDoc

        if doc:
            newlines = []
            lines = doc.splitlines()
            for line in lines:
                line = line.strip()
                if not line:
                    continue
                if TESTCASE_KEY_PATTERN.match(line):
                    continue
                newlines.append(line)
            output = ' '.join(newlines)
            output = output.strip()
            return output

        return ''

    def add_step_result(self, name, description, result):
        if name:
            name = str(name).strip()
        if description:
            description = str(description).strip()
        if result:
            result = result.lower()
        step_result = {'name': name,
                       'description': description,
                       'result': result}
        self._step_results.append(step_result)

    def get_step_summary(self):
        pass_count = len([x for x in self._step_results if x['result'] == 'pass'])
        fail_count = len([x for x in self._step_results if x['result'] == 'fail'])
        block_count = len([x for x in self._step_results if x['result'] == 'block'])
        omit_count = len([x for x in self._step_results if x['result'] == 'omit'])
        return "%d passed, %d failed, %d blocked, %d omitted" % (
               pass_count, fail_count, block_count, omit_count)

    def run_steps(self, *steps, **kwargs):
        """
        Run test steps one at a time, passing output of all previous steps to
        subsequent steps.
        @param continue_on_fail: Message failed steps and keep going.
        @param step_args: Additional arguments passed to all steps
        @param test_func: A function to determine the overall result
        """

        def default_test(results):
            """
            The default checker just checks to see if *everything* passed
            """

            pass_count = len([x for x in self._step_results if x['result'] == 'pass'])
            self.assertEqual(pass_count, len(steps), 'All steps did not pass')

        continue_on_fail = kwargs.get('continue_on_fail', False)
        step_args = kwargs.get('step_args', None)
        test_func = kwargs.get('test_func', default_test)
        output = []
        self._step_results = []
        self.publish_vars = ['_step_results']

        for num, step in enumerate(steps, 1):
            self.step_message = ''
            try:
                output.append(step(output, step_args))
            except AssertionError, err:
                self.add_step_result(self.shortDescription(step.func_doc), err, 'fail')
                if continue_on_fail is True:
                    self.logger.error('Step %s error: %s', num, err)
                else:
                    raise
            except (SkipTest, SkipTest2), reason:
                skip_type, reason = str(reason).split(': ', 1)
                self.add_step_result(self.shortDescription(step.func_doc), reason, skip_type)
                if continue_on_fail is True:
                    self.logger.warn('Step %s %s: %s', num, skip_type.lower(), reason)
                else:
                    raise
            else:
                self.add_step_result(self.shortDescription(step.func_doc), self.step_message, 'pass')

        test_func(self._step_results)



class TestBenchCase(DellTestCase):

    """
    Adds specific TestBench functionality
    """

    def __init__(self, *args, **kwargs):
        try:
            self.rram = rram
        except NameError:
            self.rram = None
        DellTestCase.__init__(self, *args, **kwargs)
        from tblib import tbinit

    def init_rram(self):
        """
        Initialize RRAM
        """

        if self.rram:
            self.rram.init(self.config.get('Env', 'JobId'))
        else:
            self.block('No RRAM available!')
