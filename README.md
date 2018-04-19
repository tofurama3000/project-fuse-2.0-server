# Project Fuse 2.0

This is the codebase for Project Fuse 2.0. It has both a server and a UI component.

This project has been tested on Ubuntu 17.10. It is recommended to use a compatible 
version of Ubuntu when running the install scripts to ensure that they work properly.

## DNS routing

DNS routing is based on sub-domains. You will need to route both the root domain and
the `api` subdomain to the server using A name entries.

For example, for `project-fuse.com` you would route both `project-fuse.com` and
`api.project-fuse.com` to the server.

## Installation (Scripts - Simple)

To install through the scripts, first copy the `project-fuse-2.0-server/server_setup`
directory to the desired location and then enter that directory using the terminal.
Next, run the `setup.sh` script.

Next, run the `proj_setup.sh` script. This will clone the codebase from GitHub and it
will configure MySQL and npm, build the Java project, and register the client and server
as services.

Congratulations! You're done!

**Note:** If your domain does not match `project-fuse.com` then do the following:
 * Edit /etc/haproxy/haproxy.cfg as root
   * Change line 31 so that instead of `api.project-fuse.com` it is `api.<your-domain>`

### Updating

To update the Java server (project-fuse-2.0-server), simply pull the new code, run `bash build.sh`, and then run
`sudo service project_fuse_server restart`.

To update the Node server (project-fuse-2.0-client), simply pull the new code, run `npm run build`, and then run
`sudo service project_fuse_client restart`.

## Installation (Manual - Advanced)

To perform a manual install, you will need to do the following steps:

* Update the package manager (if applicable)
* Install Git
* Install Python3 and Pip for Python 3
* Install Pystache using Pip
* Install Java 8
* Install Maven
* Install Nvm (alternatively, install node v8.9.4 as the default)
  * Then install node v8.9.4 as the default
* Install MySQL
* Create the schemas `project_fuse` and `project_fuse_test`
* Install Haproxy
  * This is used for subdomain routing
* Install an SSL certificate (Let's Encrypt offers free, 3-month certificates)
* Install Elasticsearch
  * Make sure to enable it
  * Ubuntu: `sudo /bin/systemctl daemon-reload && sudo /bin/systemctl enable elasticsearch.service && sudo systemctl start elasticsearch.service`
* Create a user that the servers will be ran as (This is for security purposes)
* Copy/clone the code to the desired location
* Create an uploads directory that is writable by the user created above
* In the `project-fuse-2.0-server` directory, run `python3 src/config/setup_app_properties.py`; it takes the following arguments (in order)
  * Email account to use for SMTP (must be valid email format; set to `example@example.com` to setup without it)
  * Email account SMTP password (set to `password` to setup without SMTP)
  * Domain name for the server (e.g `project-fuse.com` for `https://project-fuse.com`)
  * Path to uploads directory
  * IP/hostname for MySQL server
  * user for MySQL server
  * password for MySQL server
  * IP/hostname for Elasticsearch server
* In the `project-fuse-2.0-server` directory, run `mvn -Dmaven.test.skip=true package`
* In the `project-fuse-2.0-server` directory, run `python3 src/config/setup_systemctl_files.py`; ittakes the following command line arguments (in order)
  * The absolute path to `project-fuse-2.0-server`
  * The absolute path to `project-fuse-2.0-client`
* Ensure Elasticsearch is running
* In the `project-fuse-2.0-server` directory, run `python3 src/config/elasticsearch_config.py`
* Setup Haproxy to forward sub-domains starting with `api.` to port 8080 and other domains to port 8081 and to do SSL termination
  * See `project-fuse-2.0-server/server_setup/haproxy.cfg`
* Copy `project-fuse-2.0-server/server_setup/my.cnf` to /etc/mysql/my.cnf

### Starting the servers

* Start the Java and node servers
  * To start the Java server, run `exec /usr/bin/java -jar <path-to-compiled-jar>` where <path-to-compiled-jar> is the path to the JAR that was compiled by Maven
  * To start the Node server, run `node app.js` in the `project-fuse-2.0-client` directory

### Register as a service
 
It is recommended to create some process to ensure that the servers stay running.
This can be done by registering the servers as a service. To register as a service on Ubuntu 17.10, do
the following:

* Copy the files `project_fuse_server.service` and `project_fuse_client.service` found
in `project-fuse-2.0-server` to `/etc/systemd/system`
* Run `sudo systemctl daemon-reload`
* Run `sudo systemctl start project_fuse_client.service`
* Run `sudo systemctl start project_fuse_server.service`

## Updating

To update the Java server (project-fuse-2.0-server), simply pull the new code, run `bash build.sh`,
and restart the server. If it is registered as a service, restart the service.

To update the Node server (project-fuse-2.0-client), simply pull the new code, run `npm run build`,
and restart the server. If it is registered as a service, restart the service.

## Usage

To access the application, navigate to `https://your-domain.com` in your browser where `your-domain.com` is your application's domain.

To access API docs, navigate to `https://api.your-domain.com/swagger-ui.html`.

## API authentication

The API uses headers to perform authentication. To authenticate with the API, first gain a session
ID from the server by sending a POST request to the `/login` endpoint with a JSON body that has the fields:
 * email - user's email address to authenticate with
 * password - user's password to authenticate with
 
The session ID will be returned in the JSON object as the field `data.sessionId`

Next, in future requests set the header `SESSIONID` to be the session id obtained previously.


# Project Fuse 2.0 Server

This is the server aspect of Project Fuse 2.0

It runs on Java 8 and uses Spring Boot. It also contains the configuration needed for setting up a host environment and for configuring the database and Elasticsearch.

To update, simply update the code, run `bash build.sh` and then restart the server.
