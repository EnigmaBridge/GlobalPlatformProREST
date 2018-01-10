import json
import time
import urllib2


__author__ = "Dan Cvrcek"
__copyright__ = "Enigma Bridge Ltd"
__email__ = "support@enigmabridge.com"
__status__ = "Development"


UPTIME = 3600

timestamp = time.time() - UPTIME

starting = 0

for players in range(2,3):

    instance_id = ""
    status = 0
    runs = starting
    while runs < 250:
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
                req.add_header('X-Auth-Token', 'business')
                try:
                    response = urllib2.urlopen(req, json.dumps(data_destroy))
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

                req = urllib2.Request('http://localhost:8081/api/v1/mpc/create')
                req.add_header('Content-Type', 'application/json')
                req.add_header('X-Auth-Token', 'business')

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
            needKeyGen = True
            pass

        data_keygen = {
            'instance': instance_id,
            'phase': 'keygen',
            'protocol': 'mpcv1',
            'input': []
        }

        if needKeyGen:
            req = urllib2.Request('http://localhost:8081/api/v1/mpc/run')
            req.add_header('Content-Type', 'application/json')
            req.add_header('X-Auth-Token', 'business')

            try:
                response = urllib2.urlopen(req, json.dumps(data_keygen))
                run_response = response.read()
                # print(run_response)
                # print instance_id
                # with open("results/"+str(players)+"_players_"+str(int(time.time()*1000))+".json", "w") as text_file:
                #  text_file.write(run_response)

                run_json = json.loads(run_response)
                status = run_json['status']
                if status == 0:
                    timings = run_json['response']['detail']['timing']
                    latency = run_json['latency']
                    # print("%d %d %s"%(int(time.time()*1000), latency, json.dumps(timings)))
            except:
                status = -1
            needKeyGen = False

        # lets run the sign protocol now
        data_sign = {
            'instance': instance_id,
            'phase': 'sign',
            'protocol': 'mpcv1',
            'input': [{
                'name': 'signplaintext',
                'value': '045C396F859500BEA636B100C7F01A9B487B143D828CA8F877C17CD0E285DC626A4588587FEF17215693A67A689C0342779BDA146DC108E8382D8C7071E3F42F79'
            },
            {
                'name':'j0',
                'value':str.format("{:02X}",runs%256)
            },
            {
                'name':'j1',
                'value':str.format("{:02X}",runs/256)
            }
            ]
        }

        req = urllib2.Request('http://localhost:8081/api/v1/mpc/run')
        req.add_header('Content-Type', 'application/json')
        req.add_header('X-Auth-Token', 'business')

        try:
            response = urllib2.urlopen(req, json.dumps(data_sign))
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

        pass  # end of 0..1000 cycle

    data_destroy = {
        'instance': instance_id,
        'key': 'password'
    }

    req = urllib2.Request('http://localhost:8081/api/v1/mpc/destroy')
    req.add_header('Content-Type', 'application/json')
    req.add_header('X-Auth-Token', 'business')

    response = urllib2.urlopen(req, json.dumps(data_destroy))

    starting = 0

    # print(response.read())
