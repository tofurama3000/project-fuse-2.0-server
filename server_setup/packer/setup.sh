#!/bin/bash

# Prep keys and ppas
sudo apt-get update
sudo apt-get -y upgrade
# general update/ppa prep
sudo apt-get install -y software-properties-common debconf-utils
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
sudo apt install -y python3-pip
sudo pip3 install pystache
