import sys
import traceback
from time import sleep
from telnetlib import Telnet
from datetime import datetime
from re import compile, search
from optparse import OptionParser

class TelnetException(Exception):
    pass

class TelnetConnection:
    """
        The I{TelnetConnection} module is a wrapper written on top of Python's B{I{telnetlib}} module. This module takes care of most aspects of authentication and opening channels. An instance of this class represents a connection to a telnet server.
        
        This module can be used connecting the remote machines through I{Telnet} and running the commands using telnet connection.

        A typical example of use case if this module is::

            from TelnetConnection import TelnetConnection
            try:
                tn = TelnetConnection("10.94.131.109", "root", "Dell123", 23, 30)
            except TelnetException, e:
                return "Error in creating Telnet Connection".
            else:
                try:
                    result = tn.execute("ls -l")
                    return result
                except TelnetException. e:
                    return "Error in executing command."
                finally:
                    tn.close()


        Here, I{result} will contain the output of the command.
    
        This module can be used for
            - I{Windows to Linux Connection} 
            - I{Linux to Linux Connection}
            - I(Windows to Windows Connection}
            - I(Linux to Windows Connection}

        Pre-Requisite for using this module:
            - U{B{I{Python 2.6+}} <http://www.python.org/download/releases/2.6.4/>} should be installed on your machine.
            - B{I{telnetlib}} module comes by default with python installation. So there is no need for installing telnetlib.
            - B{I{Telnet}} service should be running on the remote machine.

        @type   HOST    : string
        @ivar   HOST    : The server IP to connect. This is of String type.
        @type   USER    : string
        @ivar   USER    : The username to authenticate. This is of String type.
        @type   PASSWD  : string
        @ivar   PASSWD  : The password to use to authenticate user. This is of String type.
        @type   PORT    : number
        @ivar   PORT    : The port which will be used for SSH Connection. The default port is 22. This is of integer type.
        @type   TIMEOUT : number
        @ivar   TIMEOUT : The timeout to wait for the connection. The default timeout valus is I{120 secs}.
    """
    def __init__(self, host, user, password, port=23, timeout=30, is_user=False):
        """
            @type   HOST    : string
            @param  HOST    : The server IP to connect. This is of String type.
            @type   USER    : string
            @param  USER    : The username to authenticate. This is of String type.
            @type   PASSWD  : string
            @param  PASSWD  : The password to use to authenticate user. This is of String type.
            @type   PORT    : number
            @param  PORT    : The port which will be used for SSH Connection. The default port is 22. This is of integer type.
            @type   TIMEOUT : number
            @param  TIMEOUT : The timeout to wait for the connection. The default timeout valus is I{120 secs}.
            @rtype          : None
        """
        self.host = host
        self.port = port
        self.user = user
        self.password = password
        self.timeout = timeout

        self.login_list = [compile("login: "), compile("login:"), compile("login: "), compile("login: ")]
        self.passwd_list = [compile("Password: "), compile("password:"), compile("password: "), compile("Password: ")]
        if not is_user:
            self.prompt_list = [compile("/admin1->"), compile("\$"), compile("%s>"%user[-3:]), compile("~.#")]
        else:
            self.prompt_list = [compile("/admin1->"),compile(":.*\\.*\>"), compile("\$"), compile(".*>"), compile("%s>"%user[-3:]), compile("~.#")]

        self.create_session()
    
    def create_session(self):
        """
            Connect to a remote machine thought telnet server and authenticate to it. This will be called when creating the TelnetConnection class object.

            It will return a None if the connection to the remote machine is successful. Otherwise it will raise an I{TelnetException}.

            @rtype  :   None
        """
        #print "# Connecting to Remote machine using telnet"
        self.sessionobj = Telnet(self.host, self.port)
        res1 = self.sessionobj.expect(self.login_list, timeout=self.timeout)
        if (res1[0] == -1):
            print "TELNET SESSION :- ", res1
            raise TelnetException, "ERROR: Login prompt does not match. %s" % res1[2]
        self.sessionobj.write(self.user + "\r\n")

        res2 = self.sessionobj.expect(self.passwd_list, timeout=self.timeout)
        if (res2[0] == -1):
            print "TELNET SESSION :- ", res2
            raise TelnetException, "ERROR: Password prompt does not match. %s" % res2[2]
        
        self.sessionobj.write(self.password + "\r\n")
        res3 = self.sessionobj.expect(self.prompt_list, timeout=self.timeout)
        if (res3[0] == -1):
            print "TELNET SESSION :- ", res3
            raise TelnetException, "ERROR: Command prompt does not match. %s" % res3[2]

    """
    Bug 3786  replace the following  getresult method with the following getresult method.
    It accounts for the case where only one crlf is returned.
    
    def getresult(self, result):
        return result[result.find("\r\n") + 2 : result.rfind("\r\n")].strip()
    """
    def getresult(self,result):
        crlf = '\r\n'
        ct = result.count(crlf)
      
        if ct >= 2:
            return result[result.find('\r\n') + 2 : result.rfind('\r\n')].strip()
        if ct == 1:
            return result[:result.rfind('\r\n')].strip()
        return result

    def execute(self, cmd, extratimeout=0):
        """
            Execute a command on remote machine using the Telnet Connection. The connection to the server must be created with the Remote Mchine before running the command.

            @type  cmd          : string
            @param cmd          : The command to execute. This is of String type.
            @type extratimeout  : number
            @param extratimeout : The extra time required by the command to finish, apart from the timeout given while creating object. It will add both the time and will wait for that much time duration.
            @rtype              : string
            @return             : Returns a string containing the output of the command.

            If the command is executed successfully, It will return a result of the command. Otherwise it will return error.
        """
        error = ""
        self.sessionobj.write(cmd + "\r\n")
        res4 = self.sessionobj.expect(self.prompt_list, timeout=(self.timeout +  extratimeout))
        if (res4[0] == -1):
#           print "TELNET SESSION :- ", res4
            error = "ERROR: Command prompt does not match after running command. %s" % res4[2]
        result = self.getresult(res4[2])
        #if (search("ERROR:", result)):
        #   error = result
        return result, error

    def execute_multiple_commands(self, cmdlist, extratimeout=0):
        """
            Execute multiple commands on remote machine one after another using the Telnet Connection. The connection to the server must be created with the Remote Mchine before running the command.

            @type  cmd          : list
            @param cmd          : A list containing all the commands.
            @type extratimeout  : number
            @param extratimeout : The extra time required by the command to finish, apart from the timeout given while creating object. It will add both the time and will wait for that much time duration.
            @rtype              : dictionary
            @return             : Returns a dictionary containing the output of the commands. The I{key} of dictionary will be the command itself, where as the I{value} will be the output of the command.

        """
        resultdict = {}
        for cmd in cmdlist:
            error = ""
            self.sessionobj.write(cmd + "\r\n")
            res4 = self.sessionobj.expect(self.prompt_list, timeout=(self.timeout +  extratimeout))
            if (res4[0] == -1):
                print "TELNET SESSION :- ", res4
                error = "ERROR: Command prompt does not match after running command. %s" % res4[2]
            result = self.getresult(res4[2])
            if (search("ERROR:", result)):
                error += result
            resultdict[cmd] = (result, error)
        return resultdict, ""

    def enter_racadm_shell(self):
        error = ""
        racadm_prompt = ["racadm>>"]
        self.sessionobj.write("racadm\r\n")
        print "# Entering in to racadm shell prompt."
        res1 = self.sessionobj.expect(racadm_prompt, timeout=self.timeout)
        print "%s" % res1[2]
        if (res1[0] == -1):
            print "TELNET SESSION :- ", res1
            error = "ERROR: Racadm Shell Prompt not found. %s" % res1[2]
            return "", error
        else:
            return "Entered into racadm shell.", ""

    def execute_racadm_shell_cmd(self, cmd, extratimeout=0):
        """
            Execute a command using the racadm shell using the Telnet Connection. The connection to the server must be created with the Remote Mchine before running the command.

            @type  cmd          : string
            @param cmd          : The command to execute. This is of String type.
            @type extratimeout  : number
            @param extratimeout : The extra time required by the command to finish, apart from the timeout given while creating object. It will add both the time and will wait for that much time duration.
            @rtype              : string
            @return             : Returns a string containing the output of the command.

            If the command is executed successfully, It will return a result of the command. Otherwise it will return error.
        """
        racadm_prompt = ["racadm>>"]
        print "# Running command \"%s\"" % cmd
        print "%s" % cmd
        self.sessionobj.write(cmd + "\r\n")
        res2 = self.sessionobj.expect(racadm_prompt, timeout=(self.timeout +  extratimeout))
        if (res2[0] == -1):
            print "TELNET SESSION :- ", res2
            error = "ERROR: Error in running the command. %s" % res2[2]

        result = self.getresult(res2[2])
        error = (result if (search("ERROR:", result)) else "")
        return result, error

    def exit_racadm_shell(self):
        print "# Exiting from racadm shell."
        self.sessionobj.write("quit\r\n")
        res3 = self.sessionobj.expect(self.prompt_list, timeout=self.timeout)
        if (res3[0] == -1):
            print "TELNET SESSION :- ", res3
            return "", "ERROR: Command prompt not found after exiting from racadm shell. %s" % res3[2]
        else:
            return "Exited from racadm shell.", ""

    def execute_in_loop(self, cmd, secs):
        start = datetime.now()
        error = ""
        racadm_prompt = ["racadm>>"]
        while True:
            print "# %s" % cmd
            self.sessionobj.write(cmd + "\r\n")
            res2 = self.sessionobj.expect(racadm_prompt, timeout=self.timeout)
            if (res2[0] == -1):
                print "TELNET SESSION :- ", res2
                error += "ERROR: Error in running the command. %s" % res2[2]
            result = self.getresult(res2[2])
            error += (result if (search("ERROR:", result)) else "")

            end = datetime.now()
            delta = end - start
            if (delta.seconds >= secs):
                break
        return error
                
    def execute_multiple_racadm_shell_cmd(self, cmdlist, extratimeout=0):
        """
            Execute a command using the racadm shell using the Telnet Connection. The connection to the server must be created with the Remote Mchine before running the command.

            @type  cmd          : string
            @param cmd          : The command to execute. This is of String type.
            @type extratimeout  : number
            @param extratimeout : The extra time required by the command to finish, apart from the timeout given while creating object. It will add both the time and will wait for that much time duration.
            @rtype              : string
            @return             : Returns a string containing the output of the command.

            If the command is executed successfully, It will return a result of the command. Otherwise it will return error.
        """
        racadm_prompt = ["racadm>>"]

        resultdict = {}
        for cmd in cmdlist:
            print "# %s" % cmd
            error = ""
            self.sessionobj.write(cmd + "\r\n")
            res2 = self.sessionobj.expect(racadm_prompt, timeout=(self.timeout +  extratimeout))
            if (res2[0] == -1):
                print "TELNET SESSION :- ", res2
                error = "ERROR: Error in running the command. %s" % res2[2]
            result = self.getresult(res2[2])
            if (search("ERROR:", result)):
                error = result
            resultdict[cmd] = result

        return resultdict, ""

    def execute_multiple_commands_in_loop(self, cmdlist, secs):
        start = datetime.now()
        error = ""
        retdict = {}
        resdict = {}
        i = 1
        while True:
            resdict = self.execute_multiple_racadm_shell_cmd(cmdlist)
            end = datetime.now()
            delta = end - start
            retdict[i] = resdict
            if (delta.seconds >= secs):
                break
            i += 1
        return retdict, error

    def timeout(self, sleeptime):
        sleep(sleeptime)

    def close(self):
        """
            Close the Telnet Connection Created with the Remote machine.
        
            @rtype  :   None
        """
        #print "# Exiting from the telnet connection."
        self.sessionobj.write("exit\r\n")

def ParseArgment():
    parser = OptionParser()
    
    parser.add_option("--ip", help="Specifies IP address of the remote computer to connect to.")
    parser.add_option("--user", help="Specifies the user name to log in with on the remote system.")
    parser.add_option("--passwd", help="Specifies the password to authenticate user on the remote system.")
    parser.add_option("--port", help="Specifies a port number or service name.")
    parser.add_option("--action", help="Specifies the action to do after successful login. It can be one of the following (runcmd/timeout)")
    parser.add_option("--sleeptime", help="Specifies the timeout in secs if the --action given in timeout")
    parser.add_option("--command", help="Specifies the command to run on remote computer if --action given is runcmd")

    options, args = parser.parse_args()

    return (options, options.ip, options.user, options.passwd, options.port, options.action, options.sleeptime, options.command)

def ValidateValues(ip, user, passwd, port, action, sleeptime, cmd):
    error = []

    if (ip == None):
        error.append("--ip".ljust(10) + "- Please specify the IP.")
    if (user == None):
        error.append("--user".ljust(10) + "- Please specify the Username.")
    if (passwd == None):
        error.append("--passwd".ljust(10) + "- Please specify the Password.")
    if (port == None):
        error.append("--port".ljust(10) + "- Please specify the Port.")
    elif (not port.isdigit()):
        error.append("--port".ljust(10) + "- Please specify the integer value for port number")
    elif (not ((int(port) >=1) and (int(port) <= 65535))):
        error.append("--port".ljust(10) + "- Port number should be in between 1-65535.")
    if (action == None):
        error.append("--action".ljust(10) + "- Please specify the action.")
    if (action not in ["runcmd", "timeout"]):
        error.append("--action".ljust(10) + "- Should be one the the following (runcmd/timeout).")
    if ((action == "runcmd") and (cmd == None)):
        error.append("--command".ljust(10) + "- Please specify the command for action=runcmd.")
    if ((action == "timeout") and (sleeptime == None)):
        error.append("--sleeptime".ljust(10) + "- Please specify the sleeptime for action=timeout.")
    elif ((action == "timeout") and (not sleeptime.isdigit())):
        error.append("--sleeptime".ljust(10) + "- Please specify an interger value for sleeptime.")

    return error

if __name__ == "__main__":
    if (len(sys.argv) == 2):
        options, ip, user, passwd, port, action, sleeptime, cmd = ParseArgment()
    elif (len(sys.argv) >= 6):
        try:
            options, ip, user, passwd, port, action, sleeptime, cmd = ParseArgment()
            error = ValidateValues(ip, user, passwd, port, action, sleeptime, cmd)
            if (error == []):
                session  = TelnetConnection(ip, user, passwd, int(port))
                if (action == "runcmd"):
                    result = session.execute(cmd)
                    print "\n", result, "\n"
                else:
                    session.timeout(int(sleeptime))
                session.close()
            else:
                print "\n", "\n".join(error), "\n"
        except Exception, e:
            error = traceback.format_exc()
            ep = error.strip().split("\n")
            print "\n", ep[len(ep)-3].strip() + "\n" + ep[len(ep)-1], "\n"
    else:
        print "Usage: TelnetConnection -h|--help for the help."

