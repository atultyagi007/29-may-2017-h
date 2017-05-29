#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
Unittest Main Launcher
"""

import sys
import os
import types
import shutil

# Unittest2 expects to import itself
sys.path.insert(1, os.path.dirname(__file__))

from dellunit import loader
from dellunit.runner import DellTestRunner as runner
from dellunit import report, __version__, __build__

import logging
import time
logging.Formatter.converter = time.gmtime  # From docs, log in UTC
from core.dell_logging import main as dell_logging

from core import iniparser

try:
    import json
except ImportError:
    import simplejson as json

try:
    from product import hooks
except ImportError:
    hooks = None

logfile = None
def setup_logging(logtime, timestamp_report=False):
    global logfile
    if not os.path.exists('logs'):
        os.mkdir('logs')

    logfile = (os.path.join('logs', 'runtime_%s.log'%logtime) if (timestamp_report) else os.path.join('logs', 'runtime.log'))

    file_handler = logging.FileHandler(logfile, 'w', 'utf-8')
    file_handler.setLevel(logging.DEBUG)
    file_handler.setFormatter(dell_logging.DellFormatter())

    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.DEBUG)
    console_handler.setFormatter(dell_logging.DellFormatter())

    root_logger = logging.getLogger()
    root_logger.handlers = []
    root_logger.addHandler(file_handler)
    root_logger.addHandler(console_handler)

def wipe_logfiles(exceptions):
    def del_file(filename):
        if filename in exceptions:
            pass
        elif os.path.isdir(full_filename):
            shutil.rmtree(full_filename)
        else:
            os.remove(full_filename)

    for filename in os.listdir('logs'):
        full_filename = os.path.join('logs', filename)
        try:
            del_file(filename)
        except Exception, e:
            logging.warn('Unable to wipe: %s - %s', filename, e)

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


try:
    from dellunit.unittest2.signals import installHandler
except ImportError:
    installHandler = None

__unittest = True


def _checkLevel(level):
    """
    Select a logging level by number or name
    Blatantly copied from python2.7's logging module
    """

    try:
        level = int(level)
    except:
        pass

    if isinstance(level, (int, long)):
        rv = level
    elif str(level) == level:
        if level not in logging._levelNames:
            raise ValueError('Unknown level: %r' % level)
        rv = logging._levelNames[level]
    else:
        raise TypeError('Level not an integer or a valid string: %r' % level)
    return rv

def _parse_log_level(value):
    value = value.upper()
    try:
        value = _checkLevel(value)
    except ValueError:
        pass
    return value

class TestProgram(object):

    """
    A command-line program that runs a set of tests; this is primarily
    for making test modules conveniently executable.
    """

    def __init__(self, logtime):
        self.info = {}
        self.parseConfig(sys.argv)
        self.logtime = logtime
        setup_logging(self.logtime, self.timestamp_report)
        self.runtimelogfile = logfile
        self.info['runtimelogfile'] = os.path.split(self.runtimelogfile)[1]

        self.logger = logging.getLogger('dellunit')

        self.logger.info("DellUnit v%s", __version__)
        self.logger.info("Build: %s", __build__)


        if hasattr(hooks, 'setUp'):
            self.logger.info('Running product-level startup hook')
            hooks.setUp()

        self.applyConfig()

        if self.config['tests']:
            self.createTests()
        else:
            self.do_discovery('tests')

        self.runTests()

        if hasattr(hooks, 'tearDown'):
            self.logger.info('Running product-level teardown hook')
            hooks.tearDown()

    def parseConfig(self, argv):

        defaults = {
            'Information': {
                'Name': 'Test Report',
                'Description': '',
            },
            'dellunit': {
                'failfast': 'True',
                'verbosity': 'DEBUG',
                'catchbreak': 'False',
                'buffer': 'True',
                'wipe_logs': 'False',
                'tests': '()',
                'retry_failed': 'True',
                'separate_output': 'False',
                'timestamp_report': 'False',
                'mail_server': 'mail.delllabs.net',
                'mail_users': '()'
            },
        }

        config = iniparser.IniParser(defaults=defaults)
        if len(argv) > 1:
            if not os.path.exists(argv[1]):
                logging.error('Unable to locate "%s"', argv[1])
                sys.exit(1)
            else:
                logging.info('Loading config from %s', argv[1])
            config.read(argv[1])

        config.to_bool('dellunit', 'separate_output')
        config.to_bool('dellunit', 'catchbreak')
        config.to_bool('dellunit', 'retry_failed')
        config.to_bool('dellunit', 'failfast')
        config.to_bool('dellunit', 'wipe_logs')
        config.to_bool('dellunit', 'timestamp_report')
        config.to_tuple('dellunit', 'mail_users')
        config.to_tuple('dellunit', 'tests')
        config.transform('dellunit', 'verbosity', _parse_log_level)
        self.config = config.get_dict(('dellunit'))

        self.info = config.get_dict(('Information'))
        self.mail_users = self.config['mail_users']
        self.catchbreak = self.config['catchbreak']
        self.retry_failed = self.config['retry_failed']
        self.failfast = self.config['failfast']
        self.separate_output = self.config['separate_output']
        self.mail_server = self.config['mail_server']
        self.wipe_logs = self.config['wipe_logs']
        self.timestamp_report = self.config['timestamp_report']

    def applyConfig(self):

        self.logger.info('logfile is: %s' % self.runtimelogfile)
        level_name = logging.getLevelName(self.config['verbosity'])
        self.logger.info('Setting log level to: %s', level_name)
        self.logger.setLevel(self.config['verbosity'])

        if self.wipe_logs:
            logfile = self.runtimelogfile.split('\\')[-1]   #get just the filename
            wipe_logfiles((logfile,))

        for option, value in self.config.items():
            self.logger.debug('%s: %s', option, value)

    def createTests(self, Loader=loader.TestLoader):
        loader = Loader()
        suites = []
        for name in self.config['tests']:
            tests = loader.loadTestsFromName(name)
            if len(tests._tests) == 0:
                self.logger.info('Discovering tests in "%s"...', name)
                if os.path.exists(os.path.join('tests', name)):
                    tests = loader.discover(name, 'test*.py', 'tests')
            if tests:
                suites.append(tests)
        self.test = loader.suiteClass(suites)

    def do_discovery(self, start_dir, pattern='test*.py',
                     top_level_dir=None, Loader=loader.TestLoader):
        loader = Loader()
        self.test = loader.discover(start_dir, pattern, top_level_dir)

    def runTests(self):

        if self.catchbreak:
            installHandler()

        run = runner(retry_failed=self.retry_failed,
                     separate_output=self.separate_output,
                     verbosity=self.config['verbosity'], 
                     logtime=self.logtime, 
                     timestamp_report=self.timestamp_report)

        self.logger.debug('Discovery complete, running test(s)')
        self.result = run.run(self.test)
