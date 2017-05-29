# -*- coding: utf-8 -*-

"""
Enhanced TestResult class and output handler
"""

import os
import sys
import datetime
import json
import re
import logging
import traceback
from dell_logging import main as dell_logging

from dellunit.unittest2 import result
from dellunit.case import TestBenchCase
from dellunit import __version__

try:
    import tblib.tbstatus
except ImportError:
    tblib = None

def json_handler(obj):
    if hasattr(obj, 'isoformat'):
        return obj.isoformat(' ')
    elif hasattr(obj, 'resolution'):
        return str(obj)
    elif hasattr(obj, 'next'):
        return [x for x in obj]
    else:
        message = 'Object of type %s with value %s is not JSON serializable'
        raise TypeError, message % (type(obj), obj)

class UTC(datetime.tzinfo):
    """
    UTC TimeZone
    """

    ZERO = datetime.timedelta(0)
    HOUR = datetime.timedelta(hours=1)

    def utcoffset(self, dt):
        return UTC.ZERO

    def tzname(self, dt):
        return "UTC"

    def dst(self, dt):
        return UTC.ZERO

TESTCASE_ID_PATTERN = re.compile(r'@testcase: (\d+)')

def get_testcase_id(docstr=""):
    """
    Extract a testcase id from a given docstring
    """
    if docstr is None:
        return
    result = TESTCASE_ID_PATTERN.findall(docstr)
    if result:
        return result[0]

class TestResult(result.TestResult):

    """
    Enhanced TestResult class to handle omit/blocked results

    These are handled as special cases of skip
    """

    def __init__(self, retry_failed=True,
                 separate_output=False, verbosity=0, 
                 logtime=None, timestamp_report=False):
        result.TestResult.__init__(self)
        self.separate_output = separate_output
        self.retry_failed = retry_failed
        self.retry_pass = False
        self.logger = None
        self.tz = UTC()
        self.verbosity = verbosity
        self.logtime = logtime
        self.timestamp_report = timestamp_report

        self.result = []

    def __iter__(self):
        return self.result.__iter__()

    def _exc_info_to_string(self, err, test):
        """
        Converts a sys.exc_info()-style tuple of values into a string.
        Tweaked the output a bit over the original
        """
        exctype, value, tb = err
        # Skip test runner traceback levels
        while tb and self._is_relevant_tb_level(tb):
            tb = tb.tb_next
        if exctype is test.failureException:
            # Skip assert*() traceback levels
            length = self._count_relevant_tb_levels(tb)
            msgLines = traceback.format_exception(exctype, value, tb, length)
        else:
            msgLines = traceback.format_exception(exctype, value, tb)

        return ''.join(msgLines)

    def startTest(self, test):
        """
        Start the Test
        """

        if self.separate_output:
            self.config_outputs(test)
        else:
            self.buffer = True
        self.logger = logging.getLogger(test.id())
        self.logger.setLevel(self.verbosity)
        result.TestResult.startTest(self, test)

        if self.separate_output:
            formatter = dell_logging.DellFormatter()
        else:
            msgfmt = '%(levelname)s %(message)s'
            formatter = dell_logging.DellFormatter(msgfmt=msgfmt)
        handler = logging.StreamHandler(sys.stderr)
        handler.setFormatter(formatter)
        self.logger.addHandler(handler)

        test.failure_retry = self.retry_pass
        self.last_start_time = datetime.datetime.now(self.tz)

    def get_safe_filename(self, basename):
        """
        Make a safe filename so that we don't overwrite existing files
        """

        count = 0
        (start, ext) = os.path.splitext(basename)
        logfile = os.path.join('logs', basename)
        while os.path.exists(logfile):
            count = count + 1
            logfile = os.path.join('logs', '%s_%02d%s' % (start, count,
                                   ext))
        return logfile

    def config_outputs(self, test):
        """
        Redirect stdout, stderr to files so we can keep logfile size
        down
        """

        testid = test.id()
        output_file = self.get_safe_filename('%s_o.txt' % testid)
        error_file = self.get_safe_filename('%s_e.txt' % testid)
        logdir = 'logs' + os.sep
        test.add_test_url('stderr', error_file.replace(logdir, ''))
        test.add_test_url('stdout', output_file.replace(logdir, ''))

        self._stderr_buffer = open(error_file, 'w')
        self._stdout_buffer = open(output_file, 'w')
        sys.stdout = self._stdout_buffer
        sys.stderr = self._stderr_buffer

    def reset_outputs(self):
        """
        Reset outputs back to system defaults
        """

        sys.stdout = self._original_stdout
        sys.stderr = self._original_stderr
        self._stdout_buffer = None
        self._stderr_buffer = None
        self.buffer = False

    def get_outputs(self):
        """
        Get buffered output
        """

        try:
            stdout = sys.stdout.getvalue()
        except AttributeError:
            stdout = ''
        try:
            stderr = sys.stderr.getvalue()
        except AttributeError:
            stderr = ''
        self.reset_outputs()
        return (stdout.strip(), stderr.strip())

    def add_result(self, status, test, stdout, stderr):
        """
        Add a result to the result list
        """

        result = {'status': status,
                  'class_doc': test.fullDescription(test.__class__.__doc__),
                  'description': test.fullDescription(),
                  'stdout': stdout,
                  'stderr': stderr,
                  'class': test.__class__.__name__,
                  'name': test.id(),
                  'start': self.last_start_time,
                  'end': datetime.datetime.now(self.tz),
                  'urls': test._test_urls,
                  'group_urls': test._group_urls,
                  'runtime_data': test._runtime_data,
                 }

        if hasattr(test, 'publish_vars'):
            for var_name in test.publish_vars:
                new_name = var_name.lstrip('_')
                result[new_name] = getattr(test, var_name)

        self.result.append({'status': status, 'name': test.id()})

        testcase_id = get_testcase_id(test._testMethodDoc) or get_testcase_id(result['class_doc'])
        result['testcase'] = testcase_id
        test._test_urls = []

        RESULT_FILE = (os.path.join('logs', 'testdata_%s.json'%self.logtime) if (self.timestamp_report) else os.path.join('logs', 'testdata.json'))
        with open(RESULT_FILE, 'a') as result_data:
            json.dump(result, result_data, default=json_handler)
            result_data.write('\n')

    def addSuccess(self, test):
        """
        Add a success result
        """

        result.TestResult.addSuccess(self, test)
        (stdout, stderr) = self.get_outputs()
        self.add_result('pass', test, stdout, stderr)
        self.logger.announce('PASS')
        if tblib and isinstance(test, TestBenchCase):
            tblib.tbstatus.send_test_result(tblib.tbstatus.PASS)

    def addError(self, test, err):
        """
        Add an error result
        """

        # Turn ImportError into BLOCK
        if len(err) > 1 and isinstance(err[1], ImportError):
            self.logger.error(err[1])
            return self.addBlock(test, 'ImportError: ' + str(err[1]))

        result.TestResult.addError(self, test, err)
        (_, _exc_str) = self.errors[-1]
        sys.stderr.write(_exc_str)
        (stdout, stderr) = self.get_outputs()
        self.add_result('error', test, stdout, stderr)
        if len(err) > 0 and err[1]:
            self.logger.announce('ERROR: %s', err[1])
        else:
            self.logger.announce('ERROR: %s', err)
        self.logger.exception('Exception:')

    def addFailure(self, test, err):
        """
        Add a fail result
        """

        result.TestResult.addFailure(self, test, err)
        (_, _exc_str) = self.failures[-1]
        sys.stderr.write(_exc_str)
        (stdout, stderr) = self.get_outputs()
        self.add_result('fail', test, stdout, stderr)
        self.logger.announce('FAIL: %s', err)
        if tblib and isinstance(test, TestBenchCase):
            tblib.tbstatus.send_test_result(tblib.tbstatus.FAIL)


    def addOmit(self, test, reason):
        """
        Add an omit result
        """

        result.TestResult.addSkip(self, test, reason)
        (stdout, stderr) = self.get_outputs()
        self.add_result('omit', test, stdout, reason)
        self.logger.announce('OMIT: %s', reason)

    def addBlock(self, test, reason):
        """
        Add a block result
        """

        result.TestResult.addSkip(self, test, reason)
        (stdout, stderr) = self.get_outputs()
        self.add_result('block', test, stdout, reason)
        self.logger.announce('BLOCK: %s', reason)
        if tblib and isinstance(test, TestBenchCase):
            tblib.tbstatus.send_test_result(tblib.tbstatus.BLOCKED)

    def addSkip(self, test, reason):
        """
        Add a skip result
        """

        if reason.startswith('BLOCK:'):
            self.addBlock(test, reason[7:])
        elif reason.startswith('OMIT:'):
            self.addOmit(test, reason[6:])
        else:
            result.TestResult.addSkip(self, test, reason)
