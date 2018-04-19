#!/usr/bin/python3

import sys
import pystache
import os

NUM_REQUIRED_ARGS = 9

args = sys.argv

if len(args) < NUM_REQUIRED_ARGS :
    print("""Missing required args, use should be
    python3 """ + sys.argv[0] + """ [email_address] [email_password] [base_url] [upload_path] [elasticsearch_host] [mysql_user] [mysql_password] [mysql_host]
Please enter those arguments and try again""")
    sys.exit(1)

context = {
    'email_address': args[1],
    'email_password': args[2],
    'base_url': args[3],
    'upload_path': args[4],
    'elasticsearch_host': args[5],
    'mysql_user': args[6],
    'mysql_password': args[7],
    'mysql_host': args[8],
    'mysql_db': 'project_fuse',
    'use_elasticsearch': 'true'
}

DIRECTORY = os.path.dirname(os.path.realpath(__file__))
MAIN_DIR = os.path.join(DIRECTORY, '../main/resources/')
TEST_DIR = os.path.join(DIRECTORY, '../test/resources/')
with open(os.path.join(MAIN_DIR, 'application.properties.prod')) as in_file:
    template = in_file.read()

with open(os.path.join(MAIN_DIR, 'application.properties'), 'w') as out_file:
    out_file.write(pystache.render(template, context))

context['mysql_db'] = 'project_fuse_test'
context['use_elasticsearch'] = 'false'
with open(os.path.join(TEST_DIR, 'application.properties'), 'w') as out_file:
    out_file.write(pystache.render(template, context))
