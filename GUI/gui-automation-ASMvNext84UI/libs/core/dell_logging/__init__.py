#!/usr/bin/python
# -*- coding: utf-8 -*-

"""
This module allows usage of Python's built-in logging module to produce log
output which meets the Dell Logging Specification.

This module also contains a Logging Handler (RealTimeLogHandler) to attach
TestBench RealTime logging to Python's logging stream.

Basic Usage
===========

    >>> import logging
    >>> import dell_logging
    >>> logging.info('This is a test message')
    >>> logging.announce('This is an announcement')


RealTime Logging
================

    >>> import logging
    >>> import dell_logging
    >>> dell_logging.add_realtime_handler(logsvr=<LOGSVRIP>)
    >>> logging.info('This is a test message')

"""

__name__ = 'dell_logging'
__version__ = '1.0'
__description__ = 'Dell Logging Extentions'


"""
This strange construct is so that python3's 2to3 works properly.
setup.py import's this file so it has to be simple enough to work in python2 &
python3
"""

try:
    from dell_logging.main import *
except:
    pass 
