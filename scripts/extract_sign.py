__author__ = "Dan Cvrcek"
__copyright__ = "Enigma Bridge Ltd"
__email__ = "support@enigmabridge.com"
__status__ = "Development"

'''
A simple example of extracting dat from log files - this one extracts timings for SIGN into a set of files.
'''
import json

file1 = None
file2 = None
oldLength = 0;
with open("results_sign_2_10.txt", "r") as ins:
    for line in ins:
        overall_time = line.split()[1]
        json_data_ = line.split()[2:]
        json_data = json.loads(''.join(json_data_))
        sign_time = json_data['INS_SIGN'][0][0]
        items = len(json_data['INS_SIGN'])
        if (items > oldLength):
            if file1 is not None:
                file1.close()
            file1 = open("parsed1_" + str(items), "w")
            if file2 is not None:
                file2.close()
            file2 = open("parsed2_" + str(items), "w")
        elif (items == oldLength):
            file1.write("%d\n" % (int(sign_time)))
            file2.write("%d\n" % (int(overall_time)))
        oldLength = items

if file1 is not None:
    file1.close()
if file2 is not None:
    file2.close()
