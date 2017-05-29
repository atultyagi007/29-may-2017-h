# log.py - Log Server client library
# ex: set expandtab softtabstop=4 shiftwidth=4:
# $Id: log.py 16 2008-10-25 01:11:48Z duane_voth $
#
# djv = duane_voth@dell.com
# 2008/08/08 - djv - original
# 2008/08/28 - djv - fixes/enhancments via peer review
# 2008/09/05 - djv - get as close as we can to python3
# 2008/09/11 - djv - include stderr, search for logsvr= in argv
# 2008/10/17 - djv - implement multi-client configuration linking
# 2009/02/02 - djv - implement persistent delivery, reliable is now per msg
# 2009/03/05 - djv - default delivery changed from minimal to reliable
# 2009/04/13 - djv - deal with some spurious Unicode exceptions
# 2009/04/17 - djv - prepare for Python 3
# 2010/05/24 - djv - enable passing logcfg to allready running programs
# v1.04.00
# 2011/02/07 - djv - 1.04.00 add RDTP, make all UDP delivery options RDTP
# 2011/03/02 - djv - 1.04.02 make log.py thread safe
# 2011/03/31 - djv - 1.04.03 close() resets Seqno (allow multiple inits)
# 2011/04/28 - djv - 1.04.04 catch and retry RDTP send errors
# 2011/05/27 - djv - 1.04.05 remake connection for nic down errors
# 2012/05/30 - djv - 1.04.06 debug for Windows connection failures
# 2013/01/31 - djv - 1.04.07 usage documentation update; support reopen
# 2013/05/30 - djv - 1.04.08 print e.args properly
#
# Usage:
#
#   To use the Logsvr within TestBench, minimally include the following code:
#
#           import logging
#           import dell_logging
#           ...
#           logging.info("Starting test (loop %d)" % loop)
#
#   To use the Logsvr standalone, minimally include the following code:
#
#           import log
#
#           log.init(appver='v1.23')
#           log.write(log.INFO, "Starting test (loop %d)" % loop)
#           ...
#           log.close()
#
#   The appver argument is required.
#
#           appver    (string)       string identifying the calling program
#
#   log.init() accpets a number of additional optional keyword arguments:
#
#           logsvr    (string)       default list of message destinations
#           logcfg    (string)       for attaching to another programs log
#           appname   (string)       defaults to sys.argv[0].split('.')[0]
#           appid     (int)          defaults to os.getpid()
#           applogdir (string)       defaults to None
#           delivery  (constant)     defaults to log.MINIMAL
#           prefix    (string)       defaults to "" (prefix string per msg)
#           logfname  (string)       defaults to appname (log fname override)
#           largs     (list of strs) defaults to []  (args added to intro msg)
#           max_instances (int)      defaults to 256
#
#       The optional logsvr argument can provide a set of default
#       destinations for the log messages.  If none is provided, and
#       no sys.argv[] argument starts with 'logsvr=' and no environment
#       variable named LOGSVR is present, then the default destination
#       of 'stderr:' is assumed.  The order of precedence for the
#       destination configuration is:
# 
#           command line            overrides all
#           environment LOGCFG var  overrides log.init() arg
#           log.init() arg          overrides internal default
#           internal default:       'stderr:'
# 
#       logsvr string format:
#
#           A logsvr string is a list of log message destinations separated
#           by commas.  Each destination has the form:
#
#               <device type:><device name><;options>
#
#           ex. string:  logsvr='192.168.1.1;persistent,syslog:,file:myapp.log'
#
#           Whitespace should only appear within device names - no where else
#           in the string.
#
#           Valid device types are:
#                   stderr:             the standard error tty
#                   stdout:             the standard out tty
#                   syslog:             the system log (if supported)
#                   file:               the named file
#                   net:                a remote host
#
#                   A remote host can be a number or a name.  Delivery
#                   options for a remote connection are:
#
#                   reliable        RDTP (releable datagram service)
#                   persistent      TCP connection held open for all packets
#
#           Currently only one message destination of each type is supported
#           so both a file and a remote server can be specified but two files
#           or two remote servers cannot.  stderr and stdout are considered
#           the "same" device type.
#
#           Note: When syslog, stderr, stdout, or file destinations are
#           used there is no support for unicode messages, elimination
#           of adjacent identical msgs, nor the aggregation of messages
#           from multiple apps/instances to a single log file.
#
#       If logfname is specified, logsvr will NOT insert _NNN to make the
#       log file unique.  This allows multiple apps across multiple machines
#       to log to the same file if they use the sane logfname and logdir.
#
#       When multiple copies of the log client code are used (via python
#       to C/C++ bindings, DLLs, or multiple executables) on a single machine,
#       race conditions will occur when writting to the same file.  To avoid
#       this, run the logsvr (either standalone, or via a test framework) and
#       make it the destination for all messages.  The logsvr contains code
#       to properly handle race conditions and can be run standalone on the
#       machine being tested (if no test framework is being used).
#
#       logcfg is used to make multiple programs write to the same file.
#       The parent program should first call log.create_instance()
#       to establish a logsvr channel for child programs, and then
#       send logcfg to the helper program.  create_instance() always
#       places the helper program's logcfg in the environment so that
#       os.system(), subprocess, and other fork/exec process creation
#       mechanisms will automatically get the new logcfg (create_instance()
#       must be called before each new process is created).  To pass a
#       logcfg to an already running program, send the string returned
#       from create_instance() to the running program and have it call
#       log.init(..., logcfg=<string>) .  See documentation at the
#       create_instance() function for more details.
#
#
#   Log message levels:
#
#       log.CRIT    critical errors that cause the app to terminate
#       log.WARN    unexpected conditions that don't cause the app to terminate
#       log.ANOU    announcement - the app has something important to report
#       log.INFO    informational events
#       log.DBUG    app trouble shooting messages

_version_ = "1.04.08"
_date_    = "2013/05/30"

import os
import sys
import time
import struct
import socket

try:
    import thread
    SendLock = thread.allocate_lock()
except ImportError:
    # if there is no threading support, we don't need locks
    class NullLock:
        def acquire(self):
            return
        def release(self):
            return
    SendLock = NullLock()

# There can be multiple destinations for log messages but only
# one destination per type of log device.  Ie. one udp or tcp
# address, plus the syslog, plus a file...
Logstdo = None
Logfile = None
Logslog = None
Logsock = None          # non-zero if a socket is open
Loghost = None          # non-zero for all network destinations
Prefix  = ""

HDRVER  = 1
HDRLEN  = 16            # header v1 length
UDPPORT = 65101
TCPPORT = 65102

MINMTU = 1400           # max message length for UDP
                        # keep this less than all inter-segment MTUs

Logcfg    = None
Nlinks    = 0
Appname   = None
Appver    = None
Appid     = 1
Ainst     = 1
Applogdir = None
Logfname  = None
MaxInst   = 256

Seqno       = 0
SEQ_DISABLE = 2**32 - 1
SEQ_INC     = 1         # must be a power of 2
SEQ_MASK    = (SEQ_INC-1)
SEQ_MAX     = SEQ_DISABLE & ~SEQ_MASK

# TCP needs a message separator
# (which cannot be any kind of unicode sequence)
TCPEOL = "\x0a\x00\x00\x0a"

# Log levels

LSYS = 0
CRIT = 1
WARN = 2
ANOU = 3
INFO = 4
DBUG = 5
ALL  = 5

ValidLevels = [LSYS, CRIT, WARN, ANOU, INFO, DBUG]
LevelName  =  ["LSYS", "CRIT", "WARN", "ANOU", "INFO", "DBUG"]
CurrentLevel = ALL

class LogExc_Fatal(Exception):
    pass

# Debug
if sys.platform.startswith('lin'):
    DbgLogFn = "/tmp/rlog_dbg.txt"
else:
    DbgLogFn = "/rlog_dbg.txt"

# set to True to get local debug
if False:
    # ASCII7 only and lose the quote chracters
    A7 = "." * 32 + ' !.#$%&.()*+,-./' \
       + '0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_' \
       + '.abcdefghijklmnopqrstuvwxyz{|}~.' + "." * 128
    if len(A7) != 256:
        raise LogExc_Fatal

    def prt(msg):
        # difficult to handle various OS' file sharing restrictions,
        # push the problem off to their shell
        os.system("echo '%-14s %s' >> " % (time.time(), msg) + DbgLogFn)

    def prtdbg(lbl, msg):
        if len(msg) > 0:
            lbl += '[' + msg[HDRLEN:].translate(A7) + ']'
        prt(lbl)
else:
    def prt(msg):
        print(msg)

    def prtdbg(lbl, msg):
        pass


# UDP/TCP Delivery options

# note: when RDTP was added, minimal, redundant, and reliable
# all became the same thing.  They are included for backward
# compatibility.
DeliveryName = ["minimal", "redundant", "reliable", "persistent"]

Delivery     = "reliable"           # default delivery method


# Reliable Datagram Transport Protocol
class Rdtp_connection(object):
    def __init__(self, addr):
        self.addr  = addr
        self.sock  = None
        self.msg   = None           # the current msg in flight
        self.sent  = time.time()    # current msg send time
        self.rtrip = 0.1            # minimum round trip time (secs)
        self._connect()

    def _connect(self):
        if self.sock:
            self.sock.close()
            del self.sock
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.setblocking(0)
        self.sock.connect(self.addr)

    def send(self, msg):
        self.msg = msg              # queue the message
        start = time.time()
        self._send(msg)             # send it
        prtdbg("    send", msg)
        self._flush()               # wait for ack
        rtrip = time.time() - start
        if rtrip < self.rtrip:
            self.rtrip = rtrip      # remember the minimum round trip time

    def close(self):
        self.sock.close()

    def _flush(self):
        dly = 0.0001                # reasonable for short haul networks
        loops = 0
        self._check_for_ack()       # do while anyone?
        while self.msg:
            time.sleep(dly)
            if dly < 1: dly *= 1.4  # exponential backoff for resend
            if dly >= 1:
                loops += 1
                if loops == 10:
                    prt("log.py no reply from %s: " % self.addr[0] +
                        "retrying indefinitely ...")
            self._check_for_ack()
        if loops > 10:
            prt("log.py connection succeeded after %d trys" % loops)

    def _check_for_ack(self):
        # receive acks
        rmsg = ""
        while self.msg and rmsg != None:
            try:
                rmsg = self.sock.recv(4096)
            except socket.error, e:
                rmsg = None
                # 10022 for Windows and 22 for Linux, is returned when the
                # NIC is down, but the socket also becomes worthless.
                # have to re-establish the connection first
                #     6 - Bad file descriptor (EBADF)
                if e.args[0] in [6, 22, 10006, 10022]:
                    try:
                        self._connect()
                    except socket.error, e:
                        prt("rxrecon: %s %s" % (str(e.args), self.addr))
                    #else:
                        prtdbg("rxrecon successful", '')
                # silently retry for any of the following recoverable errors:
                #    11 - Resource temporarily unavailable
                #    42 - EWOULDBLOCK  (seen on python 2.4)
                #   111 - Connection Refused
                # Windows errors are 10000 and up:
                # 10035 - Resource temporarily unavilable
                # (ignore errors in the following list)
                elif not e.args[0] in [11, 42, 111, 10035]:
                    prt("rdtprx: %s" % str(e.args))     # mention others
            else:
                # is this an ack?
                if self.msg and self.msg.startswith(rmsg):
                    prtdbg("     ack", self.msg)
                    self.sent = time.time()
                    self.msg = None
                    return

        # resend msg if necessary
        if self.msg:
            # Limit the baseline resend rate:  Too small a value
            # floods the network with retries, too large a value
            # reduces RDTP throughput over a lagy long haul network.
            if self.sent + self.rtrip < time.time():
                prtdbg("  resend", self.msg)
                self._send(self.msg)

    def _send(self, msg):
        # it makes no difference if the physical layer drops the msg
        # or if the local software stack utterly fails, retry is
        # handled above in _flush()
        try:
            self.sock.sendall(msg)
        except socket.error, e:
            if e.args[0] in [6, 22, 10006, 10022]:
                # for these errors, re-establish connection
                try:
                    self._connect()
                except socket.error, e:
                    prt("txrecon: %s %s" % (str(e.args), self.addr))
                #else:
                    prtdbg("txrecon successful", '')
            # for these errors, silently ignore
            elif not e.args[0] in [11, 42, 111, 10035]:
                prt("rdtptx: %s" % str(e.args))     # mention others
        


# Transmission Control Protocol
class Tcp_connection(object):
    def __init__(self, addr):
        self.addr = addr

        # wait for the logsvr forever
        attempt = 1
        while True:
            try:
                self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.sock.connect(addr)
                if attempt > 1:
                    prt("log.py connection succeeded after %d trys" % attempt)
                return
            except socket.gaierror:
                del self.sock
                prt("log.py unknown host %s" % addr[0])
                raise LogExc_Fatal
            except socket.error:
                del self.sock
                if attempt == 1:
                    prt("log.py cannot connect to %s: %s: "  % (addr[0],
                        sys.exc_info()[1]) + "retrying indefinitely ...")
                attempt += 1
                time.sleep(2)

    def send(self, msg):
        prtdbg("    send", msg)
        self.sock.send(msg)

    def close(self):
        self.sock.close()


def _packmsg(lvl, txt):
    """internal function: package up a msg"""
    global Seqno
    if Seqno != SEQ_DISABLE:
        Seqno += SEQ_INC
        if Seqno >= SEQ_MAX:
            # wrap sequence number
            Seqno = SEQ_INC + (Seqno & SEQ_MASK)
    hdr = struct.pack(">BBBBLLL", HDRLEN, HDRVER, lvl, 0, Appid, Ainst, Seqno)
    if sys.version_info[0] >= 3:
        chksum = 255 - ((sum(hdr + txt.encode()) - 1) & 0xff)
    else:
        chksum = 255 - ((sum(map(ord, list(hdr) + list(txt)))  - 1) & 0xff)
    # repack with checksum
    hdr = struct.pack(">BBBBLLL", HDRLEN, HDRVER, lvl, chksum, Appid, Ainst, Seqno)
    if sys.version_info[0] >= 3:
        # python3.0 ??  try test_utf16.py with no args...
        return hdr + txt.encode()
    else:
        # getting spurious Unicode exceptions with  "hdr + txt"
        ords = map(ord, list(hdr) + list(txt))
        args = ["%dB" % len(ords)] + ords
        return struct.pack(*args)


def write(lvl, msg):
    """Log a message.  msg can be ASCII, utf8, or utf16."""
    global Logsock, Loghost

    # don't report messages above CurrentLevel
    if lvl > CurrentLevel:
        return

    # prefix each message if requested
    msg = Prefix + str(msg)

    if not lvl in ValidLevels:
        write(WARN, "invalid log level %d" % lvl)
        lvl = WARN

    if Loghost:
        # truncate UDP messages that are too long
        nmsg = msg
        total_len = len(nmsg) + HDRLEN
        if Delivery != "persistent" and total_len > MINMTU:
            # negative numbers index from the back of a python slice
            nmsg = nmsg[:MINMTU - total_len]
            prt("truncated %d log message bytes" % (total_len - MINMTU))

        # add the TCP delimiter
        if Delivery == "persistent":
            nmsg += TCPEOL

        # protect RDTP and global use of Seqno
        SendLock.acquire()
        pmsg = _packmsg(lvl, nmsg)
        try:
            Logsock.send(pmsg)
        except socket.gaierror:
            prt("log.py send to " + Loghost + " failed: "
                + str(sys.exc_info()[1]))
        SendLock.release()

    if Logslog:
        # syslog support
        try:
            import syslog
            lmsg = LevelName[lvl] + ' ' + msg
            syslvl = [syslog.LOG_EMERG, syslog.LOG_CRIT, syslog.LOG_WARNING,
                      syslog.LOG_NOTICE, syslog.LOG_INFO, syslog.LOG_DEBUG][lvl]
            if len(msg) < 1 or msg[-1] != '\n':  lmsg += '\n'
            # use the facility: LOG_USER
            syslog.syslog(syslvl, lmsg)
        except ImportError: pass

    if Logfile or Logstdo:
        # client must only time stamp log entries in local files/devices
        tstamp = time.strftime("%Y-%m-%d %H:%M:%S ")
        lmsg = LevelName[lvl] + ' ' + msg
        if len(msg) < 1 or msg[-1] != '\n':  lmsg += '\n'

        if Logfile:
            fh = open(Logfile, 'a')
            fh.write(tstamp + lmsg)
            fh.close()

        if Logstdo:
            Logstdo.write(tstamp + lmsg)


def close():
    """Send a log close message to the server"""
    global Loghost, Logsock, Seqno
    # setlevel(ALL)
    write(LSYS, ".close appid=%d" % (Appid))

    if Loghost:
        Logsock.close()
        Loghost = None

    Seqno = 0


def setlevel(lvl):
    """Set the minimum log message level that will be reported."""
    if lvl > 255 or lvl < 0:
        prt("setlevel: invalid log level %d" % lvl)
    else:
        global CurrentLevel
        # refuse to shut off CRIT and WARN messages
        if lvl < WARN:
            lvl = WARN
        CurrentLevel = lvl
        write(LSYS, ".setlevel %d" % (lvl))


def setprefix(pfx):
    """Set the log message prefix for this client"""
    global Prefix
    # enforce a prefix format so the logsvr can parse it out
    Prefix = "[%s] " % pfx


def getappid():
    """Get the appid (int) being used by this log client"""
    return Appid


def getinstance():
    """Get this client's instance number"""
    return Ainst


def getcfg():
    """Get this client's server configuration"""
    # a log server config string for use in environment variables
    return Logcfg

def get_client_config():
    """Get this client's system configuration"""
    # get the entire log client configuration as a dictionary
    # (for use resuming log file recording across a total system reset)
    # only the init function arguments needed to resume talking to
    # the same log file are returned here.
    return {'appid' : Appid,
            'appver' : Appver,
            'appname' : Appname,
            'ainst' : Ainst,
            'prefix' : Prefix, 
            'applogdir' : Applogdir,
            'logfname' : Logfname,
            'logsvr' : Loghost}

def create_instance():
    """Create a new 'child' logging client instance that reports
to the same log as the parent.  When multiple logging clients are
running either within separate programs and the programs have been
started via popenN, os.system, or subprocess, or multiple clients
embedded within shared libraries, and are all logging to the same
destination (only a remote server is supported in this manner),
then each client needs a unique instance number.  create_instance()
creates this number and passes it to the child program or shared
library via the environment variable LOGCFG.  create_instance()
should be called by the parent process before the child process
is started).

Since each parent can have multiple children, instance numbers
form a hierarchy where each generation scales up the starting
instance number for their children by MaxInst.  If MaxInst is
256 then 4 generations can be supported within the 32bit
instance number passed in each header, and each parent can
spawn 256 children.
"""
    global Nlinks
    Nlinks += 1
    cfg = "ainst=%d|" % (Ainst * MaxInst + Nlinks) + Logcfg
    os.environ['LOGCFG'] = cfg
    return cfg


def init(appver=None, appid=-1, appname=sys.argv[0].split('.')[0],
         ainst=1, applogdir=None, logsvr="stderr:", logcfg="",
         logfname=None, prefix="", largs=None):
    """Initialize the logging service.  appver (string) is required if appid is not 0."""
    global Logstdo, Logfile, Logslog, Logsock, Loghost
    global Appname, Appver, Appid, Delivery, Ainst, Logcfg
    global Prefix, Logfname, Applogdir

    Prefix = ""             # init message should not have a prefix
    if int(appid) == -1:
        appid = os.getpid()
    Appid  = int(appid)
    Appname = appname
    Appver  = appver
    Ainst = ainst
    Applogdir = applogdir
    Logfname  = logfname

    # check for a LOGSVR environment variable
    if 'LOGSVR' in os.environ:
        logsvr = os.environ['LOGSVR']

    # check for a logsvr= argument
    for arg in sys.argv:
        if arg.startswith("logsvr="):
            logsvr = arg[7:]

    # check for a LOGCFG environment variable
    if 'LOGCFG' in os.environ:
        logcfg = os.environ['LOGCFG']
        #prt("using LOGCFG env var: " + str(cfg))

    # check for a logcfg= command line argument
    # format: "logcfg=appid=1234|192.168.1.1;reliable,file:x.log"
    for arg in sys.argv:
        if arg.startswith("logcfg="):
            # remove whitespace
            logcfg = "".join(ch for ch in arg[7:] if not ch.isspace())

    # format: "ainst=123|appid=1234|192.168.1.1;reliable,file:x.log"
    for kw in logcfg.split('|'):
        if kw.startswith("appid="):
            Appid = int(kw[6:])
        elif kw.startswith("ainst="):
            Ainst = int(kw[6:])
        elif len(kw) > 0:
            logsvr = kw

    if appid != 0 and appver == None:
        prt("log.init error: appver (string) required")
        return 0

    for dest in logsvr.split(','):
        dest = dest.strip().lower()
        if dest.startswith("file:"):
            Logfile = dest.split(':')[1]
        elif dest.startswith("stderr:"):
            Logstdo = sys.stderr
        elif dest.startswith("stdout:"):
            Logstdo = sys.stdout
        elif dest.startswith("syslog:"):
            # log messages to the OS system log if supported
            try:
                import syslog
                Logslog = dest
                syslog.openlog("%s[%u]" % (appname, Appid))
            except ImportError: pass
        else:
            # else it's an IP address or host name
            if dest.startswith("net:"):
                dest = dest[4:]
            host = dest
            deliv = Delivery
            if host.find(';') > -1: host, deliv = host.split(';')
            if host.find(':') > -1:
                prt("port numbers cannot be changed: " + host)
                continue
            if not deliv in DeliveryName:
                prt("unknown log delivery option: " + deliv)
                continue
            Delivery = deliv
            Loghost = host
            if Delivery in ["persistent"]:
                Logsock = Tcp_connection((Loghost, TCPPORT))
            else:
                Logsock = Rdtp_connection((Loghost, UDPPORT))

    args  = ', appid=%d' % Appid
    args += ', deliv=' + Delivery
    args += ', logsvr=' + str(logsvr)
    if applogdir:  args += ', logdir=' + applogdir
    if logfname:   args += ', logfn=' + logfname
    if largs:
        for arg in largs:  args += ', ' + 'ua_' + arg
    # conform to: 0.1 PG Event Logging & Return Codes Specification  6/19/2007
    intro = "%s, %s, %s" % (appname, appver, sys.argv[1:]) + args + '\n'
    write(LSYS, intro)

    Prefix = prefix
    # save our config for client chaining
    Logcfg = "appid=%d|%s" % (Appid, logsvr)

    #prt("Delivery = %s" % Delivery)
    #prt("Loghost  = %s" % Loghost)
    #prt("Logslog  = %s" % Logslog)
    #prt("Logfile  = %s" % Logfile)
    #prt("Logstdo  = %s" % Logstdo)
    #prt("Logcfg   = %s" % Logcfg)

    return 1


if __name__ == "__main__":
    init(appver="0.001")
    write(INFO, "just testing...")
    close()
