#!/usr/bin/python3

import sys
import pystache
import os

NUM_REQUIRED_ARGS = 2

args = sys.argv

if len(args) < NUM_REQUIRED_ARGS :
    print("""Missing required args, use should be
    python3 """ + sys.argv[0] + """ [path_to_server_dir] [path_to_client_dir]
Please enter those arguments and try again""")
    sys.exit(1)

context = {
    'jar': os.path.join(args[1], "target/project-fuse-2.0-1.0-SNAPSHOT.jar"),
    'app_js': os.path.join(args[2], "app.js"),
    'client_dir': args[2],
    'server_dir': os.path.join(args[1], "target")
}

DIRECTORY = os.path.dirname(os.path.realpath(__file__))
OUTPUT_DIR = os.path.join(DIRECTORY, '../../')
with open(os.path.join(DIRECTORY, 'project_fuse_client.service')) as in_file:
    template = in_file.read()

with open(os.path.join(OUTPUT_DIR, 'project_fuse_client.service'), 'w') as out_file:
    out_file.write(pystache.render(template, context))

with open(os.path.join(DIRECTORY, 'project_fuse_server.service')) as in_file:
    template = in_file.read()

with open(os.path.join(OUTPUT_DIR, 'project_fuse_server.service'), 'w') as out_file:
    out_file.write(pystache.render(template, context))
