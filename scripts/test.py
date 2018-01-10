import json
import urllib2

data_create = {
    'size': 5,
    'protocol': 'mpcv1'
}

req = urllib2.Request('http://localhost:8081/api/v1/mpc/create')
req.add_header('Content-Type', 'application/json')
req.add_header('X-Auth-Token', 'public')

response = urllib2.urlopen(req, json.dumps(data_create))
create_response = response.read()
print(create_response)
create_json = json.loads(create_response)
instance_id = create_json['response']['instance']
latency = create_json['latency']
print("%s %d" % (instance_id, latency))

data_destroy = {
    'instance': instance_id,
    'key': 'password'
}

req = urllib2.Request('http://localhost:8081/api/v1/mpc/destroy')
req.add_header('Content-Type', 'application/json')
req.add_header('X-Auth-Token', 'public')

response = urllib2.urlopen(req, json.dumps(data_destroy))

print(response.read())
