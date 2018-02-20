#!/bin/bash

# Clone repos
cd ~/

nvm use v8.9.4
git clone https://github.com/tofurama3000/project-fuse-2.0-client.git
cd project-fuse-2.0-client/
cp src/config.prod.js src/config.js
npm install

cd ..
git clone https://github.com/tofurama3000/project-fuse-2.0-server.git
cd project-fuse-2.0-server
mkdir uploads
mysql -u root -proot -e "create schema project_fuse"
mysql -u root -proot -e "create schema project_fuse_test"
python3 src/config/setup_app_properties.py \
    projectfuse@gmail.com \
    password \
    project-fuse.com \
    $(pwd)/uploads \
    localhost \
    root \
    root \
    localhost
mvn -Dmaven.test.skip=true package

python3 src/config/setup_systemctl_files.py $(pwd) $(cd ../project-fuse-2.0-client && pwd)
sudo mv project_fuse_server.service /etc/systemd/system
sudo mv project_fuse_client.service /etc/systemd/system
sudo systemctl daemon-reload
sudo systemctl start project_fuse_client.service
sudo systemctl start project_fuse_server.service
