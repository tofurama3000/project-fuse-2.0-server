#!/bin/bash

# Prep keys and ppas
sudo apt-get update
# general update/ppa prep
sudo apt-get install -y software-properties-common debconf-utils
# Java prep
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update

# Install Java 8
sudo echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
sudo apt-get install -y oracle-java8-installer

# install mysql
sudo apt install -y mysql-server mysql-client

# install git
sudo apt install git
