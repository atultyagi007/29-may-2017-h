from os import path as ospath
from os import rename as osrename
from os import remove as osremove
from traceback import format_exc
from shutil import copy as shutilcopy
from shutil import rmtree as removedirtree
from csv import reader as csvreader, writer as csvwriter

def pathExists(filename, directory="."):
	fullpath = ospath.join(directory, filename)
	if (ospath.exists(fullpath)):
		return True
	else:
		return False

def pathJoin(a, *p):
	# In Windows
	# if the first argument is a drive letter, change it to represent the drive path
	# i.e if a=P:, change it to a=P:\
	if a.endswith(":"):
		a = ospath.join(a, "\\")
	return ospath.join(a, *p)

def pathSplit(path):
	return ospath.split(path)

def readFile(filename):
	if (ospath.exists(filename)):
		try:
			filehandle = open(filename, "r")
			filedata = filehandle.read().splitlines()
			filehandle.close()
			return True, filedata
		except:
			return False, "ERROR: %s" % str(format_exc())
	else:
		return False, "ERROR: File \"%s\" does not exists." % filename

def writeFile(filename, data):
	dirname = ospath.dirname(filename)
	directory = (dirname if (dirname != "") else ".")
	if (ospath.exists(directory)):
		try:
			filehandle = open(filename, "w")
			filehandle.write(str(data))
			filehandle.close()
			return True, "File written."
		except:
			return False, "ERROR: %s" % str(format_exc())
	else:
		return False, "ERROR: Directory \"%s\" does not exists." % filename
	
def readCsvFile(filename, delimiter=","):
	if (ospath.exists(filename)):
		try:
			filehandle = open(filename, "rU")
			reader = csvreader(filehandle, delimiter=delimiter)
			retlist = []
			for row in reader:
				if row:
					retlist.append(row)
			filehandle.close()
			return True, retlist
		except:
			return False, "ERROR: %s" % str(format_exc())
	else:
		return False, "ERROR: File \"%s\" does not exists." % filename
	
def writeCsvFile(filename, data, delimiter=","):
	dirname = ospath.dirname(filename)
	directory = (dirname if (dirname != "") else ".")
	if (ospath.exists(directory)):
		try:
			filehandle = open(filename, "w")
			writer = csvwriter(filehandle, delimiter=delimiter)
			writer.writerows(data)
			filehandle.close()
			return True, "File written."
		except:
			return False, "ERROR: %s" % str(format_exc())
	else:
		return False, "ERROR: Directory \"%s\" does not exists." % filename

def deleteFile(filename):
	if (ospath.exists(filename)):
		try:
			osremove(filename)
			return True, "File deleted."
		except:
			return False, "ERROR: %s" % str(format_exc())
	else:
		return False, "ERROR: File \"%s\" does not exists." % filename

def renameFile(oldfilename, newfilename):
	if (ospath.exists(oldfilename)):
		newdirname = ospath.dirname(newfilename)
		directory = (newdirname if (newdirname != "") else ".")
		if (ospath.exists(directory)):
			try:
				osrename(oldfilename, newfilename)
				return True, "File renamed."
			except:
				return False, "ERROR: %s" % str(format_exc())
		else:
			return False, "ERROR: Directory \"%s\" does not exists." % newfilename
	else:
		return False, "ERROR: File \"%s\" does not exists." % oldfilename
	
def copyFile(source, destination):
	if (not ospath.exists(source)):
		return False, "Source file \"%s\" does not exists." % source
	if (not ospath.isfile(source)):
		return False, "Source file \"%s\" is a directory. " % source
	destdirname = ospath.dirname(destination)
	directory = (destdirname if (destdirname != "") else ".")
	if (not ospath.exists(directory)):
		return False, "Destination directory \"%s\" does not exists." % destination
	if (ospath.isfile(destination)):
		return False, "Destination file \"%s\" already exists." % destination

	try:
		shutilcopy(source, destination)
		return True, "File copied."
	except:
		return False, "ERROR: %s" % str(format_exc())


def remove_dir(path):
	try:
		removedirtree(path)
	except Exception,e:
		pass
