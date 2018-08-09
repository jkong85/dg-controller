#!/bin/sh
echo "======================================================"
echo "Get Pods Info"
sudo kubectl get pods -o wide

echo "======================================================"
echo "Get SVC Info"
sudo kubectl get svc

