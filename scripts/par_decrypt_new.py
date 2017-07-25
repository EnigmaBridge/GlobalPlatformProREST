__author__ = "Dan Cvrcek"
__copyright__ = "Enigma Bridge Ltd"
__email__ = "support@enigmabridge.com"
__status__ = "Development"

'''
This script can test parallel processing of "decrypt" phase in MPC protocol
'''

import json
import urllib2
import time
import threading
import socket
import requests
import sys
import collections
import traceback
import multiprocessing

UPTIME = 3600
players = 3 
threads = 8
requestno = 500

lock = threading.Lock()
sem = threading.Semaphore(0)
sem_file = threading.Semaphore(1)

MAX_TIMES=50
last_times_count = 0
last_times = collections.deque(maxlen=MAX_TIMES)
last_print = 0

output_file_global = open("par_decrypt.txt","w",0)

class FuncEncrypt(threading.Thread):
    def __init__(self, group=None, target=None, name=None,
                 args=(), kwargs=None, verbose=None):
#        super(FuncEncrypt, self).__init__(args=args)
        threading.Thread.__init__(self, group=None, target=None, name=None,
                                  verbose=None)
        self.name = args[0]

    def run(self):
        
#        global last_times_count
#        global last_times
#        global sem_file
#        global last_print
#        global MAX_TIMES
        output_file = open("par_decrypt_" + str(self.name) + ".txt", "w",0)
        timestamp = time.time() - UPTIME
        starting = 1000 - requestno
        encrypted_text = None
        firstRun = True
        instance_id = ""
        status = 0
        runs = starting
        print("> starting %s %d" % (self.name, time.time()))
        while runs < 1000:
            runs += 1;
            if ((time.time() - timestamp) >= UPTIME) or status != 0:
                if (instance_id is not None) and len(instance_id) > 2:
                    # let's delete existing instance
                    data_destroy = {
                        'instance': instance_id,
                        'key': 'password'
                    }

                    req = urllib2.Request('http://mpc.enigmabridge.com:8081/api/v1/mpc/destroy')
                    req.add_header('Content-Type', 'application/json')
                    req.add_header('X-Auth-Token', 'business')
                    try:
                        response = urllib2.urlopen(req, json.dumps(data_destroy))
                        response.fp._sock.close()
                        response.close()
                    except:
                        pass
                    pass

                notDone = True
                needKeyGen = True
                while notDone:
                    data_create = {
                        'size': players,
                        'protocol': 'mpcv1'
                    }

                    req = urllib2.Request('http://mpc.enigmabridge.com:8081/api/v1/mpc/create')
                    req.add_header('Content-Type', 'application/json')
                    req.add_header('X-Auth-Token', 'business')

                    lock.acquire()
                    try:
                        response = urllib2.urlopen(req, json.dumps(data_create))
                    finally:
                        lock.release()

                    if firstRun:
                        firstRun = False
                        #print("> response %s %d" % (self.name, time.time()))
                    create_response = response.read()
                    response.fp._sock.close()
                    response.close()
                    try:
                        #print(create_response)
                        create_json = json.loads(create_response)
                        instance_id = create_json['response']['instance']
                        latency = create_json['latency']
                        if create_json['status'] == 0:
                            notDone = False
                            # print("%s %d"%(instance_id, latency))
                    except:
                        pass
                    time.sleep(1)
                timestamp = time.time()

                data_keygen = {
                    'instance': instance_id,
                    'phase': 'keygen',
                    'protocol': 'mpcv1',
                    'input': []
                }

                req = urllib2.Request('http://mpc.enigmabridge.com:8081/api/v1/mpc/run')
                req.add_header('Content-Type', 'application/json')
                req.add_header('X-Auth-Token', 'business')

                try:
                    response = urllib2.urlopen(req, json.dumps(data_keygen))
                    run_response = response.read()
                    response.fp._sock.close()
                    response.close()
                    #print(run_response)
                    # print instance_id
                    #with open("results/" + str(players) + "_players_" + str(int(time.time() * 1000)) + ".json",
                    #          "w") as text_file:
                    #    text_file.write(run_response)

                    run_json = json.loads(run_response)
                    status = run_json['status']
                    if status == 0:
                        timings = run_json['response']['detail']['timing']
                        latency = run_json['latency']
                        # print("keygen>> %d %d %s"%(int(time.time()*1000), latency, json.dumps(timings)))
                except:
                    status = -1

                data_encrypt = {
                    'instance': instance_id,
                    'phase': 'encrypt',
                    'protocol': 'mpcv1',
                    'input': [{'name': 'enplaintext',
                               'value': '045C396F859500BEA636B100C7F01A9B487B143D828CA8F877C17CD0E285DC626A4588587FEF17215693A67A689C0342779BDA146DC108E8382D8C7071E3F42F79'}]
                }

                req = urllib2.Request('http://mpc.enigmabridge.com:8081/api/v1/mpc/run')
                req.add_header('Content-Type', 'application/json')
                req.add_header('X-Auth-Token', 'business')

                try:
                    response = urllib2.urlopen(req, json.dumps(data_encrypt))
                    run_response = response.read()
                    response.fp._sock.close()
                    response.close()
                    # print(run_response)
                    # print instance_id
                    # with open("results/"+str(players)+"_players_"+str(int(time.time()*1000))+".json", "w") as text_file:
                    #  text_file.write(run_response)

                    run_json = json.loads(run_response)
                    status = run_json['status']
                    if status == 0:
                        timings = run_json['response']['detail']['timing']
                        latency = run_json['latency']
                        encrypted_text = run_json['response']['detail']['result']['@encryption'][0][0]
                        #print("encrypt>> %d %d %s"%(int(time.time()*1000), latency, json.dumps(timings)))
                except:
                    status = -1
                needKeyGen = False

                sem.acquire()
                pass

            if (encrypted_text is None) or (len(encrypted_text) < 10):
                print("ERRROR")
                sys.exit()
            # lets run the sign protocol now
            data_decrypt = {
                'instance': instance_id,
                'phase': 'decrypt',
                'protocol': 'mpcv1',
                'input': [{'name': 'ciphertext', 'value': encrypted_text}]
            }
            ##req = urllib2.Request('http://localhost:8081/api/v1/mpc/run')
            ##req.add_header('Content-Type', 'application/json')
            ##req.add_header('X-Auth-Token', 'public')
            headers = {'Content-Type': 'application/json', 'X-Auth-Token': 'business'}
            try:
                time_total = -time.time() * 1000
                req = requests.post('http://mpc.enigmabridge.com:8081/api/v1/mpc/run', headers=headers,
                                    data=json.dumps(data_decrypt))
                ##response = urllib2.urlopen(req, json.dumps(data_decrypt))
                time_total += time.time() * 1000
                run_response = req.text
                ##run_response = response.read()
                ##response.fp._sock.close()
                ##response.close()
                #print(run_response)
                # print instance_id
                # with open("results/"+str(players)+"_players_"+str(int(time.time()*1000))+".json", "w") as text_file:
                #   text_file.write(run_response)

                run_json = json.loads(run_response)
                status = run_json['status']
                if status == 0:
                    timings = run_json['response']['detail']['timing']
                    latency = run_json['latency']
                    #output_file.write(
                    #    "%d %d %d %s\n" % (int(time.time() * 1000), int(time_total), latency, json.dumps(timings)))
                    #try:
                    #    sem_file.acquire()
                    #    last_times_count += 1
                    #    last_times.append(time.time()*1000.0)
                    #    if (1000.0*time.time()-last_print) > 1000:
                    #        last_print = 1000.0*time.time()
                    #        if last_times_count>= MAX_TIMES:
                    #            frequency = 1000.0*MAX_TIMES/(last_times[MAX_TIMES - 1]-last_times[0])
                    #        elif last_times_count >2:
                    #            frequency = 1000.0*last_times_count/(last_times[last_times_count - 1]-last_times[0] + 0.001)
                    #        else:
                    #            frequency = 0
                    #        frequency='{"frequency":%d,"data":%s}\n' % (int(frequency),json.dumps(timings))
                    #        output_file_global.write(frequency)
#                        output_file_global.write('{"frequency":%d,"data":%s}\n' % (int(frequency),json.dumps(timings)))
                    #except Exception, e:
                    #    print str(e)
                    #    traceback.print_exc()
                    #finally:
                    #    sem_file.release()
            except:
                status = -1

            pass  # end of 0..1000 cycle

        data_destroy = {
            'instance': instance_id,
            'key': 'password'
        }

        req = urllib2.Request('http://mpc.enigmabridge.com:8081/api/v1/mpc/destroy')
        req.add_header('Content-Type', 'application/json')
        req.add_header('X-Auth-Token', 'business')

        response = urllib2.urlopen(req, json.dumps(data_destroy))

        starting = 0

        print("> exiting %s %d" % (self.name, time.time()))
        # print(response.read())


if __name__ == "__main__":
    th_list = []
    for testing in range(0, threads):
        #new_one = multiprocessing.Process(target=FuncEncrypt, args=[testing])
        new_one = FuncEncrypt(args=[testing])
        th_list.append(new_one)
        new_one.start()
        sem.release()
        sys.stdout.write(".")
        sem.release()
        time.sleep(1)



#    for all_th in th_list:
#        all_th.start()
#    time.sleep(10)
#    print("Starting")
#    for tt in range(0, threads):
#        sys.stdout.write(".")
#        sem.release()
#    sys.stdout.flush()
    for all_th in th_list:
        all_th.join()
