#!/bin/bash
set -x

#echo "Pull Docker Images "
#sudo docker pull jkong85/dg-controller-eureka:0.1
#sudo docker pull jkong85/dg-controller-zuul:0.1
#sudo docker pull jkong85/dg-controller-test:0.1
#sudo docker pull jkong85/dg-imo-eureka:0.1
#sudo docker pull jkong85/dg-imo-zuul:0.1
#sudo docker pull jkong85/dg-imo-speed:0.1
#sudo docker pull jkong85/dg-imo-oil:0.1
#sudo docker pull jkong85/dg-imo-location:0.1

sudo docker pull mongo:3.4
sudo docker pull adoptopenjdk/openjdk8-openj9:jdk8u162-b12_openj9-0.8.0-alpine

# Copy mongo to /opt, and then mount /opt with mongo deployment
echo "TAKE CARE!!!!!  COPY Mongo scripts and apps on EACH VM node!!!!!!!!!!!!!!"
echo "Copy Mongo Scripts and Apps to /opt/dgmongo"
sudo mkdir /opt/dgmongo

cd mongo/
echo "Compile HTTP server on MongoDB deployment"
env GOOS=linux GOARCH=amd64 go build  http.go
echo "Copy scripts to /opt/dgmongo"
sudo cp mongoclone.sh /opt/dgmongo
sudo cp mongorun.sh /opt/dgmongo
sudo cp mongoclean.sh /opt/dgmongo
sudo cp http /opt/dgmongo
sudo chmod u+x /opt/dgmongo/http
sudo chmod u+x /opt/dgmongo/mongo*
cd ../
ls -al /opt/dgmongo
