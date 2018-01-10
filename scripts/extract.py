import json

file1 = None
file2 = None
oldLength = 0
for no in range(0,20):
  counter = 0
  overall_time = 0
  server_time = 0
  sign_time = 0
  filename = "par_decrypt_"+str(no)+".txt"
  with open(filename, "r") as ins:
    for line in ins:
      counter += 1
      overall_time += int(line.split()[1])
      server_time += int(line.split()[2])
      json_data_ = line.split()[3:]
      json_data = json.loads(''.join(json_data_))
      sign_time += int(json_data['INS_DECRYPT'][0][0])

  print("%d %d %d"%( int(overall_time/counter), int(server_time/counter), int(sign_time/counter)))


