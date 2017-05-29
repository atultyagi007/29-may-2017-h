import sys
import time
import traceback
from os import path as ospath

import warnings
with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    warnings.warn("deprecated", DeprecationWarning)
    import paramiko

status = ["Connection Created.", "Connection Closed.", "File Copied."]

class SSHConnection:
	"""
		The I{SSHConnection} module is a wrapper written on top of Python's U{B{I{paramiko}} <http://www.lag.net/paramiko/>} module. This module takes care of most aspects of authentication and opening channels. 
		
		This class wraps up 2 most important things, which are widely used over the SSH Connection :- 
			1. B{SSH Connection:}
				-	With this, You can create a Connection with the remote server (I{Running SSH Server}) over the SSH Connection and you can execute commands on that server.
			2. B{SCP File:}
				-	With this, You can copy the files from your local machine to the remote server (I{Running SSH Server}) over the SSH Connection.

		A typical example of use case if this module is::
			from SSHConnection import SSHConnection
			ssh = SSHConnection("10.94.131.102", "root", "calvin", 22, 120)
			result1, error1 = ssh.Connect()
			if (error1 != ""):
				return 
			result2, error2 = ssh.Execute("ls -l")
			result3, error3 = ssh.Close()
		Here, result2 will contain the output of the command.
	
		This module can be used for
			- I{Windows to Linux Connection over SSH} 
			- I{Linux to Linux Connection over SSH} 

		Pre-Requisite for using this module:
			- U{B{I{Python 2.6+}} <http://www.python.org/download/releases/2.6.4/>} should be installed on your machine.
			- U{B{I{paramiko}} <http://www.lag.net/paramiko/>} should be installed on your machine.
			- B{I{SSH}} service should be running on the remote machine.

		Restrictions on usage of this module.
			- This module can only be used to connect the Linux machines (I{Where, SSH Server is running}).

		@type	HOST	: string
		@ivar   HOST	: The server IP to connect. This is of String type.
		@type	USER	: string
		@ivar   USER	: The username to authenticate. This is of String type.
		@type	PASSWD	: string
		@ivar   PASSWD	: The password to use to authenticate user. This is of String type.
		@type	PORT	: number
		@ivar   PORT	: The port which will be used for SSH Connection. The default port is 22. This is of integer type.
		@type	TIMEOUT	: number
		@ivar   TIMEOUT	: The timeout to wait for the connection. The default timeout valus is I{120 secs}.
	"""
	def __init__(self, HOST, USER, PASSWD, PORT=22, TIMEOUT=120):
		"""
		Constructor for creating the object of the I{SSHConnection} class. It will be automatically called, when you create an object of I{SSHConnection} class.

			@type	HOST	: string
			@param  HOST	: The server IP to connect. This is of String type.
			@type	USER	: string
			@param  USER	: The username to authenticate. This is of String type.
			@type	PASSWD	: string
			@param  PASSWD	: The password to use to authenticate user. This is of String type.
			@type	PORT	: number
			@param  PORT	: The port which will be used for SSH Connection. The default port is 22. This is of integer type.
			@type	TIMEOUT	: number
			@param  TIMEOUT	: The timeout to wait for the connection. The default timeout valus is I{120 secs}.
			@rtype			:	None
		"""
		self.HOST = HOST
		self.USER = USER
		self.PASSWD	= PASSWD
		self.PORT = PORT
		self.TIMEOUT = TIMEOUT
	
	def Connect(self):
		"""
			Connect to an SSH server and authenticate to it.  The server's host key	is checked against the system host keys.  If the server's hostname is not found in either set of host keys, the missing host key policy	is used.

			It will return a tuple containing the result and the error (e.g. I{(result, error)} ). If the connection is created successfully, then error will be empty. Otherwise error will contain the exception raised during the connection process.

			@rtype	:	tuple
		"""
		try:
			self.ssh = paramiko.SSHClient()
			self.ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
			self.ssh.connect(self.HOST, port=self.PORT, username=self.USER, 
						password=self.PASSWD,timeout=self.TIMEOUT)
			time.sleep(10)
			return "Connection Created.", ""
		except:
			result = "ERROR: Unable to connect through SSH."
			err_code = "ERROR: " + traceback.format_exc()
			return result, err_code
	
	def Close(self):
		"""
			Close the SSH Connection created with the Server.

			It will returns a tuple containing the result and the error (e.g. I{(result, error)} ). If the connection is closed successfully, then error will be empty. Otherwise error will contain the exception raised during the closing process.
			
			@rtype	:	tuple
		"""
		try:
			if (self.ssh):
				self.ssh.close()
			return "Connection Closed.", ""
		except:
			result = "ERROR: Unable to close the session."
			err_code = "ERROR: " + traceback.format_exc()
			return result, err_code
	
	def Execute(self, command):
		"""
			Execute a command on the SSH server. The connection to the server must be created with the SSH Server before running the command. (i.e. you must call L{Connect} method before calling this function.).

			@type  command	: string
			@param command	: The command to execute. This is of String type.
			@rtype			: tuple
			@return			: Returns a tuple containing the result and the error (e.g. I{(result, error)} )

			It will return a tuple containing the result and the error (e.g. I{(result, error)} ). If the command is executed successfully,then error will be empty. Otherwise error will contain the exception raised during the running the command.
		"""
		try:
			if (self.ssh):
				stdin, stdout, stderr = self.ssh.exec_command(command)
				result = "".join(stdout.readlines())
				error  = "".join(stderr.readlines())
				return result, error
			else:
				return "ERROR: Unable to execute the command.", "ERROR: Connection to the server is not created."
		except:
			result = "ERROR: Unable to execute the command."
			err_code = "ERROR: " + traceback.format_exc()
			return result, err_code

	def Execute_Multiple_Commands(self, cmdlist):
		"""
			Execute multiple commands on the SSH server one after another. The connection to the server must be created with the SSH Server before running the command. (i.e. you must call L{Connect} method before calling this function.).

			@type  cmdlist	: list
			@param cmdlist	: A list containing all the commands.
			@rtype			: dictionary
			@return			: Returns a dictionary containing the output of the commands. The I{key} of dictionary will be the command itself, where as the I{value} will be the output of the command. output will be a tuple containing the result and the error (e.g. I{(result, error)} ). If the command is executed successfully,then error will be empty. Otherwise error will contain the exception raised during the running the command.
		"""
		resultdict = {}
		for cmd in cmdlist:
			try:
				if (self.ssh):
					stdin, stdout, stderr = self.ssh.exec_command(cmd)
					result = "".join(stdout.readlines())
					error  = "".join(stderr.readlines())
					cmdres = (result, error)
				else:
					cmdres = ("ERROR: Unable to execute the command.", "ERROR: Connection to the server is not created.")
			except:
				result = "ERROR: Unable to execute the command."
				err_code = "ERROR: " + traceback.format_exc()
				cmdres = (result, err_code)

			resultdict[cmd] = cmdres
		return resultdict, ""

	def Scp(self, source, destination):
		"""
			Copy the file to the server with SSH Connection. The connection to the server must be created with the SSH Server before running the command. (i.e. you must call L{Connect} method before calling this function.).

			@type  source		: string
			@param source		: This is the source file path to copy. This is of String type.
			@type  destination	: string
			@param destination	: This is the destination source file path on server. This is of String type.
			@rtype				: tuple
			@return				: Returns a tuple containing the result and the error (e.g. I{(result, error)} )

			It will return a tuple containing the result and the error (e.g. I{(result, error)} ). If the file copied successfully,then error will be empty. Otherwise error will contain the exception raised during the copying process.
		"""
		try:
			if (not ospath.exists(source)):
				return "ERROR: Source file does not exists.",\
						"ERROR: Source file does not exists."
			trans = self.ssh.get_transport()

			# this is all it takes to send a file!
			scp = paramiko.SCPClient(trans)
			if (ospath.isdir(source)):
				status = scp.put_r(source, destination)
			else:
				status = scp.put_p(source, destination)
			#trans.close()

			return "File Copied.", ""
		except:
			result = "ERROR: Unable to copy the file."
			err_code = "ERROR: " + traceback.format_exc()
			return result, err_code

def run_ssh_cmd(host, user, passwd, command, port=22, timeout=120):
	"""
		Wrapper written over the 3 functions of the I{SSHConnection} class to execute the given command on remote machine over ssh. 

		It performs 3 tasks :-
			-	Creates a SSH Connection with the given host on the given port.
			-	Executes the given command on the remote host.
			-	Close the SSH Connection after the execution of command is over.

		@type	host	: string
		@param  host	: The server IP to connect. This is of String type.
		@type	user	: string
		@param  user	: The username to authenticate. This is of String type.
		@type	passwd	: string
		@param  passwd	: The password to use to authenticate user. This is of String type.
		@type   command	: string
		@param  command	: The command to execute. This is of String type.
		@type	port	: number
		@param  port	: The port which will be used for SSH Connection. The default port is 22. This is of integer type.
		@type	timeout	: number
		@param  timeout	: The timeout to wait for the connection. The default timeout valus is I{120 secs}.
		@rtype			: tuple
		@return			: Returns a tuple containing the result and the error (e.g. I{(result, error)} )
	
	"""
	ssh = SSHConnection(host, user, passwd, port, timeout)
	result1 = ssh.Connect()
	if (result1[0] not in status):
		return result1[0], result1[1]

	result2 = ssh.Execute(command)

	result3 = ssh.Close()
	return result2[0], result2[1]

def run_multiple_ssh_cmd(host, user, passwd, cmdlist, port=22, timeout=120):
	"""
		Wrapper written over the 3 functions of the I{SSHConnection} class to execute multiple commands on remote machine over ssh. Commands are given in the form of list.

		It performs 3 tasks :-
			-	Creates a SSH Connection with the given host on the given port.
			-	Executes the commands given cmdlist.
			-	Close the SSH Connection after the execution of command is over.

		@type	host	: string
		@param  host	: The server IP to connect. This is of String type.
		@type	user	: string
		@param  user	: The username to authenticate. This is of String type.
		@type	passwd	: string
		@param  passwd	: The password to use to authenticate user. This is of String type.
		@type   cmdlist	: list
		@param  cmdlist	: The list of commands to execute.
		@type	port	: number
		@param  port	: The port which will be used for SSH Connection. The default port is 22. This is of integer type.
		@type	timeout	: number
		@param  timeout	: The timeout to wait for the connection. The default timeout valus is I{120 secs}.
		@rtype			: dict
		@return			: Returns a dictionary, which contais the command as the key and the a tuple, containing the result and the error (e.g. I{(result, error)} ), as the value of the dictionary.
	"""
	if (isinstance(cmdlist, list)):
		ssh = SSHConnection(host, user, passwd, port, timeout)
		result1 = ssh.Connect()
		if (result1[0] not in status):
			return result1[0], result1[1]
		resultdict = {}
		for command in cmdlist:
			result = ssh.Execute(command)
			resultdict[command] = [result[0], result[1]]

		result3 = ssh.Close()
		return resultdict, ""
	else:
		return "ERROR: Unable to execute the command." , "cmdlist is not an object of list. Please provide a list of commands for cmdlist."

def scpfile(host, user, passwd, source, destination, port=22, timeout=120):
	"""
		Wrapper written over the 3 functions of the I{SSHConnection} class to copy the file from the local machine to remote machine over ssh. 

		It performs 3 tasks :-
			-	Creates a SSH Connection with the given host on the given port.
			-	Copies the given source file at the remote server on the given destination path.
			-	Close the SSH Connection after the execution of command is over.

		@type	host		: string
		@param  host		: The server IP to connect. This is of String type.
		@type	user		: string
		@param  user		: The username to authenticate. This is of String type.
		@type	passwd		: string
		@param  passwd		: The password to use to authenticate user. This is of String type.
		@type	source		: string
		@param	source		: The source file location in the local machine, which needs to copy. This may be a single file or a directory.
		@type   destination	: string
		@param  destination	: The destination file location in the remote machine.
		@type	port		: number
		@param  port		: The port which will be used for SSH Connection. The default port is 22. This is of integer type.
		@type	timeout		: number
		@param  timeout		: The timeout to wait for the connection. The default timeout valus is I{120 secs}.
		@rtype				: tuple
		@return				: Returns a tuple containing the result and the error (e.g. I{(result, error)} ).
	"""
	ssh = SSHConnection(host, user, passwd, port, timeout)
	result1 = ssh.Connect()
	if (result1[0] not in status):
		return result1[0], result1[1]

	result2 = ssh.Scp(source, destination)

	result3 = ssh.Close()
	return result2[0], result2[1]

def scp_multiple_files(host, user, passwd, src_file_list, destination, port=22, timeout=120):
	"""
		Wrapper written over the 3 functions of the I{SSHConnection} class to copy multiple files or directores from the local machine to remote machine over ssh. 

		It performs 3 tasks :-
			-	Creates a SSH Connection with the given host on the given port.
			-	Copies the given source file at the remote server on the given destination path.
			-	Close the SSH Connection after the execution of command is over.

		@type	host			: string
		@param  host			: The server IP to connect. This is of String type.
		@type	user			: string
		@param  user			: The username to authenticate. This is of String type.
		@type	passwd			: string
		@param  passwd			: The password to use to authenticate user. This is of String type.
		@type	src_file_list	: string
		@param	src_file_list	: The source file location in the local machine, which needs to copy. This may contain the name of the file or directory.
		@type   destination		: string
		@param  destination		: The destination file location in the remote machine.
		@type	port			: number
		@param  port			: The port which will be used for SSH Connection. The default port is 22. This is of integer type.
		@type	timeout			: number
		@param  timeout			: The timeout to wait for the connection. The default timeout valus is I{120 secs}.
		@rtype					: dict
		@return					: Returns a tuple containing the result and the error (e.g. I{(result, error)} ).
	"""
	if (isinstance(src_file_list, list)):
		ssh = SSHConnection(host, user, passwd, port, timeout)
		result1 = ssh.Connect()
		if (result1[0] not in status):
			return result1[0], result1[1]

		resultdict = {}
		for source in src_file_list:
			print source
			result2 = ssh.Scp(source, destination)
			resultdict[source] = [result2[0], result2[1]]

		result3 = ssh.Close()
		return resultdict, ""
	else:
		return "ERROR: Unable to copy the file.", "src_file_list is not a list. Please provide a list of source files for src_file_list."


"""
if (__name__ == "__main__"):
	if (len(sys.argv) >= 6):
		host = sys.argv[1]
		user = sys.argv[2]
		passwd = sys.argv[3]
		port = (eval(sys.argv[4]) if (sys.argv[4].isdigit()) else 22)
		command = sys.argv[5]
		timeout = (sys.argv[6] if ((len(sys.argv) >= 7) and (sys.argv[6].isdigit())) else 120)

		## This is for running single command
		result, err_code = run_ssh_cmd(host, user, passwd, command, port, timeout)
		print "\n\nRESULT :- \n\n", result
		print "\n\nERROR :- \n\n", err_code

		## This code is for running multiple commands.
		#cmdlist = ["racadm getconfig -g cfgSerial", "racadm getniccfg", "racadm getconfig -g cfgRacTuning"]
		#result, err_code = run_multiple_ssh_cmd(host, user, passwd, cmdlist, port, timeout)
		#for k, v in result.items():
		#	print "-"*100
		#	print "COMMAND :- \n%s" % str(k)
		#	print "\n"
		#	print "RESULT :- \n%s" % str(v[0])
		#	print "\n"
		#	print "ERROR :- \n%s" % str(v[1])
		#	print "\n"
		#	print "-"*100
		#	print "\n\n"
	else:
		print "\n\nUSAGE: %s <HOST> <USER> <PASSWD> <PORT> <COMMAND> [<TIMEOUT=120>]\n" % sys.argv[0]
"""

"""
if (__name__ == "__main__"):
	if (len(sys.argv) >= 7):
		host = sys.argv[1]
		user = sys.argv[2]
		passwd = sys.argv[3]
		port = sys.argv[4]
		source = sys.argv[5]
		destination = sys.argv[6]
		timeout = (sys.argv[7] if ((len(sys.argv) >= 8) and (sys.argv[7].isdigit())) else 120)

		## This is for copying single file.
		result, err_code = scpfile(host, user, passwd, source, destination, port, timeout)
		print "\n\nRESULT :- \n\n", result
		print "\n\nERROR :- \n\n", err_code

		## This is for copying multiple files
		#src_file_list = ["C:\\Comman Modules\\SSHConnection.py", "C:\\Comman Modules\\HTMLTestRunner.py", "C:\\Comman Modules\\SSH Connection Module.doc"]
		#src_file_list = ["C:\\Comman Modules\\Folder1", "C:\\Comman Modules\\Folder2", "C:\\Comman Modules\\Folder3"]
		#result, err_code = scp_multiple_files(host, user, passwd, src_file_list, destination, port, timeout)
		#for k, v in result.items():
		#	print "-"*100
		#	print "COMMAND :- \n%s" % str(k)
		#	print "\n"
		#	print "RESULT :- \n%s" % str(v[0])
		#	print "\n"
		#	print "ERROR :- \n%s" % str(v[1])
		#	print "\n"
		#	print "-"*100
		#	print "\n\n"
	else:
		print "\n\nUSAGE: %s <HOST> <USER> <PASSWD> <PORT> <SOURCE> <DESTINATION> [<TIMEOUT=120>]\n" % sys.argv[0]
"""

