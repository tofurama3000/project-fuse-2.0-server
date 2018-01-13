"""
This manages the configuration for elasticsearch
"""

import sys
import os

# Determine if we are python 3 or python 2
VERSION = 3
if sys.version_info[0] < 3:
    VERSION = 2

# Get the correct http library depending on the version
if VERSION == 2:
    import httplib
else:
    import http.client

def get_directory(path):
    """Returns the list of files in a directory"""
    return os.listdir(path)

def get_connection():
    if VERSION == 2:
        return httplib.HTTPConnection('localhost', 9200)
    else:
        return http.client.HTTPConnection('localhost', 9200)

def send_request_to_elasticsearch(uri, data):
    url = '/' + uri

    con = get_connection()
    try:
        con.request('GET', url)
        response = con.getresponse()
    finally:
        con.close()

    if response.status == 200:
        print("Index, found, dropping index")
        con = get_connection()
        try:
            con.request('DELETE', url)
            response = con.getresponse()
        finally:
            con.close()

        if response.status != 200:
            raise Exception("Unable to delete existing index for " + uri)
        print("Index dropped")

    print("Creating index")
    con = get_connection()
    try:
        con.request('PUT', url, data, {
            'Content-Type': 'application/json'
        })
        response = con.getresponse()
    finally:
        con.close()

    if response.status != 200:
        raise Exception("Unable to createindex for " + uri)
    print("Index made")

DIRECTORY = os.path.dirname(os.path.realpath(__file__))

files = get_directory(DIRECTORY)

json_files = []

for file in files:
    if file[-5:] == ".json":
        json_files.append(file)

for file in json_files:
    uri = file[0:-5]
    f = open(os.path.join(DIRECTORY, file),'r')
    try:
        request_string = f.read()
    finally:
        f.close()
    print("Doing index for " + uri)
    try:
        send_request_to_elasticsearch(uri, request_string)
    except Exception as e:
        print("ERROR " + e)

print('')
print("Done with indexes, please reindex all your data")
