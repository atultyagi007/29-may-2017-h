#!/usr/bin/python
# -*- coding: utf-8 -*-

RT_LOG_AVAIL = True

import logging.handlers
import logging as py_log
import os

try:
    from tblib import log as rt_log
except ImportError:
    try:
        from dell_logging import log as rt_log
    except ImportError:
        RT_LOG_AVAIL = False


DELL_LEVELS = {
    'DEBUG': 'DBUG',
    'CRITICAL': 'CRIT',
    'WARNING': 'WARN',
    'ERROR': 'CRIT',
    'ANNOUNCE': 'ANOU',
    }

py_log.ANNOUNCE = 25
py_log.addLevelName(py_log.ANNOUNCE, 'ANNOUNCE')

if RT_LOG_AVAIL:
    LVL_MAP = {
        py_log.CRITICAL: rt_log.CRIT,
        py_log.ERROR: rt_log.CRIT,
        py_log.ANNOUNCE: rt_log.ANOU,
        py_log.WARNING: rt_log.WARN,
        py_log.INFO: rt_log.INFO,
        py_log.DEBUG: rt_log.DBUG,
        py_log.NOTSET: rt_log.DBUG,
        }

class DellFormatter(py_log.Formatter):

    """
    Formatter to emit 4-character loglevel indicators

    Overrides the formatting parameters to match the Dell Spec.
    """

    def __init__(self, msgfmt='%(asctime)s %(levelname)s [%(name)s] %(message)s',
                       datefmt='%Y-%m-%d %H:%M:%S'):
        py_log.Formatter.__init__(self, msgfmt, datefmt)

    def format(self, record):
        """
        Format the outgoing log record
        """

        if record.levelname in DELL_LEVELS.keys():
            record.levelname = DELL_LEVELS[record.levelname]
        return py_log.Formatter.format(self, record)

class RealTimeLogHandler(py_log.Handler):

    """
    Real Time Log Handler, see module's docstring for more info
    """

    def __init__(self, logobj, level=py_log.NOTSET):
        py_log.Handler.__init__(self, level)
        self.rt_log = logobj

    def setLevel(self, level):
        """
        Set logging level
        """

        rt_lvl = LVL_MAP.get(level, rt_log.INFO)
        py_log.Handler.setLevel(self, level)
        self.rt_log.setlevel(rt_lvl)

    def emit(self, record):
        """
        Output the log record
        """

        rt_lvl = LVL_MAP.get(record.levelno, rt_log.INFO)
        try:
            self.rt_log.write(rt_lvl, record.getMessage())
        except Exception, err:
            self.handleError(record)

    def close(self):
        self.flush()
        if self.rt_log:
            self.rt_log.close()
            self.rt_log = None

def logger_announce(self, msg, *args, **kwargs):
    """
    Log 'msg % args' with severity 'ANNOUNCE'.

    To pass exception information, use the keyword
    argument exc_info with a true value, e.g.
        logger.announce('problem', exc_info=1)
    """

    if self.manager.disable >= py_log.ANNOUNCE:
        return
    if py_log.ANNOUNCE >= self.getEffectiveLevel():
        self._log(py_log.ANNOUNCE, msg, args, **kwargs)


py_log.Logger.announce = logger_announce


def root_announce(msg, *args, **kwargs):
    """
    Log a message with severity 'ANNOUNCE' on the root logger.
    """

    if len(py_log.root.handlers) == 0:
        py_log.basicConfig()
    py_log.root.announce(msg, *args, **kwargs)


py_log.announce = root_announce

if hasattr(logging.handlers.SysLogHandler, 'priority_map'):
    logging.handlers.SysLogHandler.priority_map['ANNOUNCE'] = 'announce'


def apply_formatter(logger_name=''):
    """
    Convenience function to apply the DellFormatter to all of the handlers for
    the given logger.

    apply_formatter with no arguments will apply the DellFormatter to all of
    the handlers for the root logger.
    """
    logger = py_log.getLogger(logger_name)

    if logger_name == '' and len(logger.handlers) == 0:
        py_log.basicConfig(level=py_log.DEBUG)

    for handler in logger.handlers:
        handler.setFormatter(DellFormatter())

def add_realtime_handler(logger_name='', prefix=None, appver='v1.0', **kwargs):
    """
    Convenience function to add the Realtime Log handler to the given logger.

    add_realtime_handler with no arguments will add the Realtime Log handler to
    the root logger.
    """

    if os.environ.get('LOGCFG'):
        pass
    elif os.environ.get('LOGSVR'):
        pass
    elif kwargs.get('logsvr'):
        pass
    else:
        print "Don't know how to find the log server."
        print 'Set the LOGSVR environment variable'
        print 'or call add_realtime_handler(logsvr="<net address>")'
        return

    logger = logging.getLogger(logger_name)
    if len(logger.handlers) <= 0:
        logger.level=py_log.DEBUG

    for handler in logger.handlers:
        if isinstance(handler, RealTimeLogHandler):
            return handler # It's already there, just return it

    rt_log.init(appver=appver, **kwargs)
    if prefix:
        rt_log.setprefix(prefix)

    rt_handler = RealTimeLogHandler(rt_log)

    logger.addHandler(rt_handler)
    return rt_handler


if len(logging.root.handlers) == 0:
    apply_formatter()
