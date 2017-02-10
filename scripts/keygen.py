__author__ = "Dan Cvrcek"
__copyright__ = "Enigma Bridge Ltd"
__email__ = "support@enigmabridge.com"
__status__ = "Development"

'''
This script runs and measures latency for keygen phase of the MPC protocol.
'''

import json
import urllib2
import time

UPTIME = 3600

timestamp = time.time() - UPTIME

starting = 0

for players in range(4, 11):

    instance_id = ""
    status = 0
    runs = starting
    while runs < 1000:
        runs += 1;
        if ((time.time() - timestamp) >= UPTIME) or status != 0:
            if (instance_id is not None) and len(instance_id) > 2:
                # let's delete existing instance
                data_destroy = {
                    'instance': instance_id,
                    'key': 'password'
                }

                req = urllib2.Request('http://localhost:8081/api/v1/mpc/destroy')
                req.add_header('Content-Type', 'application/json')
                req.add_header('X-Auth-Token', 'public')
                try:
                    response = urllib2.urlopen(req, json.dumps(data_destroy))
                except:
                    pass
                pass

            notDone = True
            while notDone:
                data_create = {
                    'size': players,
                    'protocol': 'mpcv1'
                }

                req = urllib2.Request('http://localhost:8081/api/v1/mpc/create')
                req.add_header('Content-Type', 'application/json')
                req.add_header('X-Auth-Token', 'public')

                response = urllib2.urlopen(req, json.dumps(data_create))
                create_response = response.read()
                try:
                    # print(create_response)
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
            pass

        data_keygen = {
            'instance': instance_id,
            'phase': 'keygen',
            'protocol': 'mpcv1',
            'input': []
        }

        req = urllib2.Request('http://localhost:8081/api/v1/mpc/run')
        req.add_header('Content-Type', 'application/json')
        req.add_header('X-Auth-Token', 'public')

        try:
            response = urllib2.urlopen(req, json.dumps(data_keygen))
            run_response = response.read()
            # print(run_response)
            # print instance_id
            with open("results/" + str(players) + "_players_" + str(int(time.time() * 1000)) + ".json",
                      "w") as text_file:
                text_file.write(run_response)

            run_json = json.loads(run_response)
            status = run_json['status']
            if status == 0:
                timings = run_json['response']['detail']['timing']
                latency = run_json['latency']
                print("%d %d %s" % (int(time.time() * 1000), latency, json.dumps(timings)))
        except:
            status = -1

    data_destroy = {
        'instance': instance_id,
        'key': 'password'
    }

    req = urllib2.Request('http://localhost:8081/api/v1/mpc/destroy')
    req.add_header('Content-Type', 'application/json')
    req.add_header('X-Auth-Token', 'public')

    response = urllib2.urlopen(req, json.dumps(data_destroy))

    starting = 0

    # print(response.read())
