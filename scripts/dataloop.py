#!/usr/bin/python

"""rest.py: a class accepts external requests in RESTful format:
   project S-CribManager - Python."""

'''
@author: Dan Cvrcek
@copyright: Copyright 2016, Enigma Bridge Ltd
@credits: Dan Cvrcek
@license: GPL version 3 (e.g., https://www.gnu.org/copyleft/gpl.html)
@version: 1.0
@email: info@enigmabridge.com
@status: Test
'''
import socket
import sys,traceback
import time
import json

_HOST = '0.0.0.0' # inet interface on which REST listens 
_PORT = 2003 # outbound TCP port 
_PORT_INT = 11110 # this is the PORT on which scribTCP.py is listening 
_MODE = "PUSH"  # PUSH = we send to a server, PULL - we wait for connection

apikey = "TEST_API"
token = "5eee7391-9357-4c40-8e4c-dbcef384f824"
fingerprint = "7b604f93-ad74-4e42-b2a7-674aab5460b0"

INTERVAL=4

starting = True
newtime = time.time()
sumobjects = 0
sumuse = 0
sumcard = 0
alluse = 0


def checkHexNumber(dongleid, length = None):
    #remove all spaces
    dongleid = dongleid.replace(" ","")
    try:
        int(dongleid, 16)
        if not length is None:
            dongleid = dongleid.zfill(length)
    except:
        raise ValueError("ERR201 Input is not in a valid format - hex number")

    return dongleid.upper()

def startServer():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print 'Socket created'

    #Bind socket to local host and port
    try:
        s.bind(("", _PORT))
    except socket.error as msg:
        print 'Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
        sys.exit()

    print 'Socket bind complete'

    #Start listening on socket
    s.listen(10)
    print 'Socket now listening'

    #now keep talking with the client
    while True:
        #wait to accept a connection - blocking call
        conn, addr = s.accept()
        print 'Connected with ' + addr[0] + ':' + str(addr[1])
        received = readDataFromSHSM('{"function":"GetCurrentStatistics","apikey":"'+apikey+'", "version":"1.0", "nonce":"nonce"}\n')
        #parsed = json.loads(received)
        #parsed = parsed['result'] 
        # display data locally
        responseData = displayData(received)
        # send data to the visualisation server
        #responseData = json.dumps(parsed)
        headers = "HTTP/1.0 200 OK\nContent-Type: text/json; encoding=utf8\n"\
            "Content-Length: %d\n"\
            "Connection: close\n\n"%len(responseData)
        conn.send(headers+responseData)
        #print headers+responseData 
        #response = conn.recv(1024)
        #print "Submission result (first 1k):" + response
        conn.close()

def readDataFromSHSM(request):
    sock = None
    received = None
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # Connect to server and send data
        sock.connect(('localhost', _PORT_INT))
        
        sock.sendall(request)
        # Receive data from the server and shut down
        received = ""
        while True:
            data = sock.recv(8192)
            if not data: break
            received += data
    except ValueError, e:
        if not sock is None:
            sock.close()
        print("Error when receiving RESTful request (%s)"%e)
        return "ERR203"
    except:
        print("Unexpected error %s", traceback.format_exc(5))
        return "ERR204"
    sock.close()

    if received is None:
        return "ERR205"
    else:
        return received

def displayData(response, conn = None):
  global starting
  global newtime
  global sumobjects
  global sumuse
  global sumcard
  global alluse

  conn = ""
  try:
    if (response is None) or (len(response)<10):
      return None 
     
    parsed = json.loads(response)
    data = parsed.get("result")
    try:
      cryptocards = data.get("cryptocards")
    except:
      cryptocards=[]
    try:
      queues = data.get("queues")
    except:
      queues = []
    if not starting:
       oldtime = newtime
       sumobjects_old=sumobjects
       sumuse_old = sumuse
       sumcard_old = sumcard
       alluse_old = alluse
    else:
       sumcard_old = {}
       sumuse_old = {}
       sumobjects_old = {}
    newtime = time.time()
    sumobjects={}
    sumuse={}
    sumuseControl={}
    sumuseSeed={}
    sumcard={}
    sumcardControl={}
    sumcardSeed={}
    queue_d = {}
    alluse=0
#    for ids in objectids:
#      sumuse[ids]=0
#      sumobjects[ids]=0

    for line in cryptocards:
      objects = line.get('loadedobjects')
      key2 = line.get('cardid')
      keyType = line.get('cardtype')
      keyType = "CRYPTO_CARD"
      sumcard[key2]=0
      if "CONTROL_CARD" in keyType:
        sumcardControl[key2]=0
      elif "CRYPTO_CARD" in keyType:
        sumcard[key2]=0
      elif "SEED_CARD" in keyType:
        sumcardSeed[key2]=0

      for obj in objects:
        key = obj.get('objectid')
        if "CRYPTO_CARD" in keyType:
          #read detail in cryptocards.loadedobjects.*
	  #sumcard - sum of all requests processed by a card
          sumcard[key2]+=int(obj.get('requests'))
	  #to compute a difference, set the old value to 0 if it does nto exist
          if not (key2 in sumcard_old.keys()):
            sumcard_old[key2]=0
          # sumuse will contain a sum of usages per object
          if not (key in sumuse_old.keys()):
            sumuse_old[key]=0
          #sumobjects is the total number of UO instances on across all crypto cards
          if not (key in sumobjects_old.keys()):
            sumobjects_old[key]=0
          
          if key in sumuse:
            sumuse[key]+=int(obj.get('requests'))
          else:
            sumuse[key]=int(obj.get('requests'))
          if key in sumobjects:
            sumobjects[key]+=1
          else:  
            sumobjects[key]=1
        elif "CONTROL_CARD" in key2:
          if key2 in sumcardControl.keys():
            sumcardControl[key2]+=int(obj.get('requests'))
          else:  
            sumcardControl[key2]=int(obj.get('requests'))
          if key in sumuseControl:
            sumuseControl[key]+=int(obj.get('requests'))
          else:
            sumuseControl[key]=int(obj.get('requests'))
      #alluse now requires a bit better treatment ....
    for key in sumuse.keys():
      print "object "+str(key)+" "+str(sumuse_old[key])+" "+obj.get('requests') 
      if sumuse_old[key]<sumuse[key]:
          alluse += sumuse[key]-sumuse_old[key]


    # we 've finished cards, let's do queues
    for obj in queues:
      key = str(obj.get('queue'))
      queue_d[key]=str(obj.get('length'))
      
    newtimeint = int(newtime)
    if starting:
      for card in sumcard.keys():
        conn += fingerprint+".Card."+str(card)+" "+"0 "+str(newtimeint)+"\n"
      for key in sumuse.keys():
        conn += fingerprint+".Use."+key+" "+"0 "+str(newtimeint)+"\n"
      for key in sumobjects.keys():
        conn += fingerprint+".Objects."+key+" "+"0 "+str(newtimeint)+"\n"
      conn += fingerprint+".tps 0"+"\n"
      for key in queue_d.keys():
        conn += fingerprint+".Queue."+key+" "+queue_d[key]+" "+str(newtimeint)+"\n"

      print "FIRST TIME"
    else:
      INTERVAL = newtime - oldtime
      for card in sumcard.keys():
        if (sumcard[card]-sumcard_old[card])<0:
            sumcard_old[card] = sumcard[card]
        conn += fingerprint+".Card."+str(card)+" "+str((sumcard[card]-sumcard_old[card])/INTERVAL)+" "+str(newtimeint)+"\n"
      for key in sumuse.keys():
        if (sumuse[key]-sumuse_old[key])<0:
            sumuse_old[key] = sumuse[key]
        conn += fingerprint+".Use."+key+" "+str((sumuse[key]-sumuse_old[key])/INTERVAL)+" "+str(newtimeint)+"\n"
      for key in sumobjects.keys():
        conn += fingerprint+".Objects."+key+" "+str(sumobjects[key])+" "+str(newtimeint)+"\n"
      for key in queue_d.keys():
        conn += fingerprint+".Queue."+key+" "+queue_d[key]+" "+str(newtimeint)+"\n"

#      if ((alluse-alluse_old)<0):
#          alluse_old = alluse
      #conn += "smarthsm.Load "+str((alluse-alluse_old)/INTERVAL)+" "+str(newtimeint)+"\n"
      conn += fingerprint+".tps "+str(alluse/INTERVAL)+" "+str(newtimeint)+"\n"
      print "tps "+str(alluse/INTERVAL)
    starting = False
  except:
    a=traceback.print_exc()
    #with open('exception.txt', 'a') as f:
    #    f.write(a)
    pass
  return conn



if __name__ == "__main__":

  if _MODE == "PUSH":
    while True:
      try:
        data = readDataFromSHSM('{"function":"GetCurrentStatistics", "apikey":\"'+apikey+'\", "version":"1.0", "nonce":"nonce"}\n') 
        formattedData = displayData(data)
        if not formattedData is None:
#            conn = socket.create_connection(("graphite.dataloop.io", 2003))
#            print formattedData
#            conn.sendall(formattedData)
#            conn.close()
            starting = False
        else:
            print "No response"
      except:
        traceback.print_exc()
        pass
      time.sleep(INTERVAL)
  elif _MODE == "PULL":
    startServer()
  else:
    print("Incorrect mode set, we expect PUSH or PULL, %s set instead."%_MODE)


