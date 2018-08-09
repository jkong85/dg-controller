#!/bin/sh

echo "======================================================"
echo "Get Docker Images Info"
sudo docker images |grep dg-

echo "======================================================"
echo "Get Pods Info"
sudo kubectl get pods -o wide

echo "======================================================"
echo "Get Deploy Info"
sudo kubectl get deployments -o wide

echo "======================================================"
echo "Get SVC Info"
sudo kubectl get svc

