#!/bin/bash

# Prep keys and ppas
sudo apt-get update
sudo apt-get -y upgrade
# general update/ppa prep
sudo apt-get install -y software-properties-common debconf-utils wget
# Java prep
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update

# Install Java 8
sudo echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
sudo apt install -y oracle-java8-installer
sudo apt-get install -y oracle-java8-set-default

# install git
sudo apt install -y git
# Install python 3 pip
sudo apt install -y python3 python3-pip
sudo pip3 install pystache

#Install Maven
sudo apt install -y maven

# Setup NVM
curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -
sudo apt-get install -y nodejs build-essential

# Install MySQL
echo "mysql-server mysql-server/root_password select root" | sudo debconf-set-selections
echo "mysql-server mysql-server/root_password_again select root" | sudo debconf-set-selections
sudo apt-get install -y mysql-server

#Install haproxy
sudo apt install -y haproxy
sudo cp haproxy.cfg /etc/haproxy/haproxy.cfg
sudo cp default_haproxy /etc/default/haproxy
sudo mkdir -p /etc/haproxy/certs

# Make a key file (replace the outputed file with one from a certificate authority if applicable)
openssl req \
    -new \
    -newkey rsa:4096 \
    -days 365 \
    -nodes \
    -x509 \
    -subj "/C=US/ST=Denial/L=Springfield/O=Dis/CN=www.example.com" \
    -keyout /tmp/project-fuse.com.key \
    -out /tmp/project-fuse.com.cert
cat /tmp/project-fuse.com.cert /tmp/project-fuse.com.key > /tmp/project-fuse.com.pem
sudo mv /tmp/project-fuse.com.pem /etc/haproxy/certs/project-fuse.com.pem

#Setup user accounts
sudo groupadd project_fuse
sudo useradd project_fuse
sudo usermod -a -G project_fuse project_fuse
