import sys
import json
import random
import string
import ast

__version__ = "1.10"

def version():
  return __version__


### Numeric Functions ##############################
def convertValToInt(val):
  try:
    int(val)
    return int(val)
  except:
    f = val[-1].lower()

    if f == 'h' or f == 'x':
      return int(val[:-1], 16)
    elif f == 'd':
      return int(val[:-1], 10)
    elif f == 'o':
      return int(val[:-1], 8)
    elif f == 'b':
      return int(val[:-1], 2)
    else:
      raise ValueError("Invalid min/minEX and/or max/maxEx values.")

def rand_int(min, max, count, fmt='d'):
  min = convertValToInt(min)
  max = convertValToInt(max)
  count = int(count)
  fmt = fmt.replace('H', 'X')
  fmt = fmt.replace('h', 'x')
  retList = []

  for x in range(count):
    if fmt == 'd' or fmt == '':
      retList.append(random.randint(min, max))
    else:
      retList.append(format(random.randint(min, max), fmt))
  return retList

def rand_intSeq(min, max, count='', fmt='d'):
  min = convertValToInt(min)
  max = convertValToInt(max)

  fmt = fmt.replace('H', 'X')
  fmt = fmt.replace('h', 'x')
  retList = []

  if (count == ''):
    retList = random.sample(range(min, max+1), max-min+1)
  else:
    retList = random.sample(range(min, max+1), int(count))
  if fmt != 'd':
    tmpList = []
    for x in retList:
      tmpList.append(format(x, fmt))
    retList = tmpList
  return retList

def rand_intEx(min, max, count, minEx, maxEx, fmt='d'):
  min = convertValToInt(min)
  max = convertValToInt(max)
  minEx = convertValToInt(minEx)
  maxEx = convertValToInt(maxEx)
  count = int(count)
  fmt = fmt.replace('H', 'X')
  fmt = fmt.replace('h', 'x')
  retList = []

  if (minEx <= min and maxEx >= max):
    raise ValueError("Invalid min/minEX and/or max/maxEx values.")

  i = 0
  while i < count:
    x = random.randint(min, max)
    if x < minEx or x > maxEx:
      if fmt == 'd':
        retList.append(x)
      else:
        retList.append(format(x, fmt))
      i += 1
  return retList

def rand_float(min, max, count, precision=0):
  min = float(min)
  max = float(max)
  count = int(count)
  retList = []

  for x in range(count):
    r = random.uniform(min, max)
    retList.append(r if precision == 0 else format(r, "." + str(precision) + "f"))
  return retList

def rand_floatEx(min, max, count, minEx, maxEx, precision=0):
  min = float(min)
  max = float(max)
  minEx = float(minEx)
  maxEx = float(maxEx)
  count = int(count)
  retList = []

  if (minEx <= min and maxEx >= max):
    raise ValueError("Invalid min/minEX and/or max/maxEx values.")

  i = 0
  while i < count:
    x = random.uniform(min, max)
    if x < minEx or x > maxEx:
      retList.append(x if precision == 0 else format(x, "." + str(precision) + "f"))
      i += 1
  return retList

def rand_normal(mean, std, count,  precision=0):
  mean = float(mean)
  std = float(std)
  count = int(count)
  retList = []

  for x in range(count):
    r = random.gauss(mean, std)
    retList.append(r if precision == 0 else format(r, "." + str(precision) + "f"))
  return retList


###  String Functions ############################
Lower = string.ascii_lowercase
Upper = string.ascii_uppercase
Alpha = Lower + Upper
Number = string.digits
AlphaNum = Alpha + Number
AlphaNumID = AlphaNum + '._-'
SpecialChar = string.punctuation  # No space(' '), \n, \t \r
AlphaAll = AlphaNum + SpecialChar


def rand_lower(minLen, maxLen, exChar='', count=1):
  if (sys.version_info < (3, 0)):
    tmpStr = Lower.translate(None, exChar.encode('ascii','ignore'))
  else:
    tmpStr = Lower.translate(dict.fromkeys(map(ord, exChar), None))
  retList = []
  for x in range(count):
    #retList.append(''.join(random.choice(tmpStr) for i in range(rand_int(minLen, maxLen, 1)[0])))
    retList.append(''.join(random.choice(tmpStr) for i in range(random.randint(minLen, maxLen))))
  return retList

def rand_upper(minLen, maxLen, exChar, count):
  if (sys.version_info < (3, 0)):
    tmpStr = Upper.translate(None, exChar.encode('ascii','ignore'))
  else:
    tmpStr = Upper.translate(dict.fromkeys(map(ord, exChar), None))
  retList = []
  for x in range(count):
    #retList.append(''.join(random.choice(tmpStr) for i in range(rand_int(minLen, maxLen, 1)[0])))
    retList.append(''.join(random.choice(tmpStr) for i in range(random.randint(minLen, maxLen))))
  return retList

def rand_alpha(minLen, maxLen, exChar, count):
  if (sys.version_info < (3, 0)):
    tmpStr = Alpha.translate(None, exChar.encode('ascii','ignore'))
  else:
    tmpStr = Alpha.translate(dict.fromkeys(map(ord, exChar), None))
  retList = []
  for x in range(count):
    #retList.append(''.join(random.choice(tmpStr) for i in range(rand_int(minLen, maxLen, 1)[0])))
    retList.append(''.join(random.choice(tmpStr) for i in range(random.randint(minLen, maxLen))))
  return retList

def rand_numstr(minLen, maxLen, exChar, count):
  if (sys.version_info < (3, 0)):
    tmpStr = Number.translate(None, exChar.encode('ascii','ignore'))
  else:
    tmpStr = Number.translate(dict.fromkeys(map(ord, exChar), None))
  retList = []
  for x in range(count):
    #retList.append(''.join(random.choice(tmpStr) for i in range(rand_int(minLen, maxLen, 1)[0])))
    retList.append(''.join(random.choice(tmpStr) for i in range(random.randint(minLen, maxLen))))
  return retList

def rand_alphaNum(minLen, maxLen, exChar, count):
  if (sys.version_info < (3, 0)):
    tmpStr = AlphaNum.translate(None, exChar.encode('ascii','ignore'))
  else:
    tmpStr = AlphaNum.translate(dict.fromkeys(map(ord, exChar), None))
  retList = []
  for x in range(count):
    #retList.append(''.join(random.choice(tmpStr) for i in range(rand_int(minLen, maxLen, 1)[0])))
    retList.append(''.join(random.choice(tmpStr) for i in range(random.randint(minLen, maxLen))))
  return retList

def rand_id(minLen, maxLen, exChar, count):
  if (sys.version_info < (3, 0)):
    tmpStr = Alpha.translate(None, exChar.encode('ascii','ignore'))
    tmpStrID = AlphaNumID.translate(None, exChar.encode('ascii','ignore'))
  else:
    tmpStr = Alpha.translate(dict.fromkeys(map(ord, exChar), None))
    tmpStrID = AlphaNumID.translate(dict.fromkeys(map(ord, exChar), None))
  retList = []
  for x in range(count):
    #retList.append(random.choice(tmpStr) + ''.join(random.choice(tmpStrID) for i in range(rand_int(minLen-2, maxLen-2, 1)[0])) + random.choice(tmpStr))
    # exclude '.-_' from the first and last charactor.
    retList.append(random.choice(tmpStr) + ''.join(random.choice(tmpStrID) for i in range(random.randint(minLen-2, maxLen-2))) + random.choice(tmpStr))
  return retList

def rand_alphaAll(minLen, maxLen, exChar, count):
  if (sys.version_info < (3, 0)):
    tmpStr = AlphaAll.translate(None, exChar.encode('ascii','ignore'))
  else:
    tmpStr = AlphaAll.translate(dict.fromkeys(map(ord, exChar), None))
  retList = []
  for x in range(count):
    #retList.append(random.choice(AlphaAll) + ''.join(random.choice(AlphaNumID) for i in range(rand_int(minLen-2, maxLen-2, 1)[0])) + random.choice(AlphaNum))
    retList.append(''.join(random.choice(tmpStr) for i in range(random.randint(minLen, maxLen))))
  return retList

def rand_myCharSet(minLen, maxLen, CharSet, count=1):
  retList = []
  for x in range(count):
    retList.append(''.join(random.choice(CharSet) for i in range(random.randint(minLen, maxLen))))
  return retList

### Date Functions ##############################
def rand_date(min, max, count, format='%Y-%m-%d'):
  import datetime

  if min == '':
    # substract 10 years
    minDT = datetime.datetime.now().replace(year=datetime.datetime.now().year-10)
  else:
    minDT = datetime.datetime.strptime(min, format)

  if max == '':
    # add 10 years
    maxDT = datetime.datetime.now().replace(year=datetime.datetime.now().year+10)
  else:
    maxDT = datetime.datetime.strptime(max, format)

  retList = []
  for x in range(count):
    retList.append((minDT + datetime.timedelta(days=random.randint(0, (maxDT-minDT).days))).strftime(format))

  return retList

def rand_time(min, max, count, format='%H:%M:%S'):
  import datetime

  if (min == ''):
    min = datetime.datetime.combine(datetime.datetime.today(), datetime.time.min)
  else:
    min = datetime.datetime.strptime(min, format)

  if (max == ''):
    max = datetime.datetime.combine(datetime.datetime.today(), datetime.time.max)
  else:
    max = datetime.datetime.strptime(max, format)

  #print (min, max)
  retList = []
  for x in range(count):
    retList.append((min + datetime.timedelta(seconds=random.randint(0, (max-min).seconds))).strftime(format))

  return retList

def rand_datetime(min, max, count, format='%Y-%m-%d %H:%M:%S'):
  import datetime

  if min == '':
    # substract 10 years
    minDT = datetime.datetime.now().replace(year=datetime.datetime.now().year-10)
  else:
    minDT = datetime.datetime.strptime(min, format)

  if max == '':
    # add 10 years
    maxDT = datetime.datetime.now().replace(year=datetime.datetime.now().year+10)
  else:
    maxDT = datetime.datetime.strptime(max, format)

  retList = []
  for x in range(count):
    retList.append((minDT + datetime.timedelta(seconds=random.randint(0, (maxDT-minDT).days * 86400 + (maxDT-minDT).seconds))).strftime(format))

  return retList

### IP Address Functions  #####################
def rand_ip(network, subnet, count, gw=1, allowDup=False, isTable=False):
  # ipv4
  from netaddr import IPNetwork

  retResult = ''
  retList = []

  tmpret = ''
  #returns Network, netmask, broadcast, possible gateway ip, ip lists
  if (network.lower() == 'ipv4'):
    tmpret = rand_ip('0.0.0.0', '0', 1, 1, 'false')
  elif (network.lower() == 'ipv6'):
    tmpret = rand_ip('::', '0', 1, 1, 'false')

  if (tmpret != ''):
    network = tmpret['IP List'][0]
    #print (network)

  ip = IPNetwork(network + '/' + str(subnet))

  if (allowDup != 'true' and (count > (ip.size-3))):
    raise ValueError("Count requested is larger then possible IP addresses.")

  retResult += '"Network": "' + str(ip.network) + '",'
  retResult += '"Netmask": "' + str(ip.netmask) + '",'
  retResult += '"Broadcast": "' + str(ip.broadcast) + '",'

  retResult += '"Gateway": "' + (str(ip[gw]) if gw == 1 else str(ip[-2])) + '",'

  # randamly pick ip addresses

  if (gw == 1):
    start = 2
  else:
    start = 1

  #random.sample(range(start, start + len(ip)-4))

  # store the numbers randomly in a temp list without duplication
  tmpList = []
  i = 0
  while i < count:
    r = random.randint(start, start + ip.size-4)
    if (allowDup == 'true' or not r in tmpList):
      tmpList.append(r)
      i += 1

  #return json.loads('{' + retResult + '"IP List": ' + str(list(str(ip[x]) for x in sorted(tmpList))).replace("'", '"') + '}')
  if (isTable):
    return json.loads(str(list(str(ip[x]) for x in tmpList)).replace("'", '"'))  # no need to sort
  else:
    return json.loads('{' + retResult + '"IP List": ' + str(list(str(ip[x]) for x in tmpList)).replace("'", '"') + '}')  # no need to sort

  #return json.loads('{' + retResult + '"IP List": ' + str(list(str(ip[x]) for x in sorted(random.sample(xrange(start, start + ip.size-2), count)))).replace("'", '"') + '}')


### MAC Address Functions  #####################
def rand_mac(count, prefix = ''):
  prefix = prefix.strip().upper()
  seperator = '-'

  #print (prefix)
  for c in prefix[:]:  # find the first seperator
    if (not c in Upper + Number):
      seperator = c
      break

  psize = len(prefix.split(seperator))

  #if (prefix.split(seperator)[-1] == ''):
  #  psize -= 1
  #  prefix = prefix[:len(prefix)-1].strip()

  singleLastHex = False
  if (prefix != ''):
    for i in range (0, psize):
      if (len(prefix.split(seperator)[i]) != 2):
        if (len(prefix.split(seperator)[i]) == 0 and i == psize-1):
          psize -= 1
          prefix = prefix[:len(prefix)-1].strip()
          break
        elif (len(prefix.split(seperator)[i]) == 1 and i == psize-1):
          singleLastHex = True
        else:
          raise ValueError("Invalid value in prefix.")
      int(prefix.split(seperator)[i], 16)
  else:
    psize = 0

  if (count > pow(16, (6-psize) * 2 + (1 if singleLastHex else 0))):
    raise ValueError("Count is larger than possible MAC addresses.")

  if (singleLastHex):
    psize -= 1


  retList = []
  x = 0
  while x < count:
    retStr = ''
    for i in range(0, 6-psize):
      if (i == 0 and singleLastHex):
        retStr += ('%X' % random.randint(0x00, 0x0f))
      else:
        retStr += seperator + ('%02X' % random.randint(0x00, 0xff))
    if (prefix == ''):
      retStr = retStr[1:]
    else:
      retStr = prefix + retStr

    if (not retStr in retList):
      retList.append(retStr)
      x += 1

  return retList

### Table Function  #####################
def table(json, rows = ''):
  return processJsonTable(json, rows)

def processJsonTable(data, rows = ''):

  if (type(data) is str):  # assume it is a json string
    data = json.loads(data)


  if ('rows' in data.keys()):
    count = data['rows'] if rows == '' else int(rows)
  else:
    count = 1

  retList = []

  ipInfoList = []
  col = 0
  for c in data['columns']:
    col += 1
    #print(c)
    if ('min' in c.keys()):
      min = c['min']
    if ('max' in c.keys()):
      max = c['max']
    if ('mean' in c.keys()):
      mean = c['mean']
    if ('std' in c.keys()):
      std = c['std']
    if ('Ex' in c['type']):
      if ('minEx' in c.keys()):
        minEx = c['minEx']
      if ('maxEx' in c.keys()):
        maxEx = c['maxEx']

    #if (c['type'] == 'intSeq'):
    #  retList.append(eval("rand_" + option + '(min, max, fmt)'))
    #  continue;

    if ('float' in c['type'] or 'normal' in c['type']):
      if 'precision' in c.keys():
        precision = int(ast.literal_eval(c['precision']))
      else:
        precision = 0

    if ('int' in c['type']):
      if ('outFormat' in c.keys()):
        fmt = c['outFormat']
      else:
        fmt = 'd'

    if (c['type'] == 'float'):
      retList.append(eval("rand_" + c['type'] + '(min, max, count, precision)'))
    elif (c['type'] == 'normal'):
      retList.append(eval("rand_" + c['type'] + '(mean, std, count, precision)'))
    elif (c['type'] == 'intEx'):
      retList.append(eval("rand_" + c['type'] + '(min, max, count, minEx, maxEx, fmt)'))
    elif (c['type'] == 'floatEx'):
      retList.append(eval("rand_" + c['type'] + '(min, max, count, minEx, maxEx, precision)'))
    elif (c['type'] in ['int', 'intSeq']):
      retList.append(eval("rand_" + c['type'] + '(min, max, count, fmt)'))
    elif (c['type'] in ['lower','upper','alpha','numstr', 'aplhaNum','id','alphaAll']):
      if ('exChar' in c.keys()):
        exChar = c['exChar']
      else:
        exChar = ''
      #print("rand_" + c['type'] + '(min, max, exChar, count)')
      retList.append(eval("rand_" + c['type'] + '(min, max, exChar, count)'))
    elif (c['type'] == 'ip'):
      if ('allowDup' in c.keys()):
        allowDup = c['allowDup']
      else:
        allowDup = 'false'
      ###retList.append( eval("rand_" + c['type'] + "(c['ip'], c['subnet'], count, c['gw'], allowDup)")['IP List'] )
      tmpIPList = eval("rand_" + c['type'] + "(c['ip'], c['subnet'], count, c['gw'], allowDup)")

      tmp = '{"Column": ' + str(col) + ','
      tmp += '"Network": "' + tmpIPList["Network"] + '",'
      tmp += '"Netmask": "' + tmpIPList["Netmask"] + '",'
      tmp += '"Broadcast": "' + tmpIPList["Broadcast"] + '",'
      tmp += '"Gateway": "' + tmpIPList["Gateway"] + '"}'

      ipInfoList.append(json.loads(tmp))
      #print(eval("rand_" + c['type'] + "(c['ip'], c['subnet'], count, c['gw'], allowDup, true)"))
      #retList.append( eval("rand_" + c['type'] + "(c['ip'], c['subnet'], count, c['gw'], allowDup, true)") )
      retList.append(tmpIPList["IP List"])
    elif (c['type'] in 'datetime'):
      if ('format' in c.keys()):
        format = c['format']
      retList.append( eval("rand_" + c['type'] + '(min, max, count, format)') )
    elif (c['type'] in 'mac'):
      if ('prefix' in c.keys()):
        prefix = c['prefix']
      retList.append( eval("rand_" + c['type'] + '(count, prefix)') )
    else:
      #raise ValueError("Invalid Type in json: " + c['type'])
      raise ValueError("Invalid type in input json: " + c['type'])

  #print ("Completed")
  retList = list(map(list, zip(*retList)))
  retList.append(ipInfoList)

  return retList

def retriveFromFile(name):
  import os
  if (not os.path.exists('SavedFiles\\' + name + '\\returnValues.txt')):
    #return { "Success" : 0, "ReturnValues" : "Name: " + name + " not found." }
    raise Exception("Name: " + name + " not found.")
  retFile = open('SavedFiles\\' + name + '\\returnValues.txt', 'r')
  retJson = retFile.read()
  retFile.close()

  js = json.loads(retJson)

  #return { "Success" : 1, "ReturnValues" : js['ReturnValues'] }
  return { "ReturnValues" : js['ReturnValues'] }
