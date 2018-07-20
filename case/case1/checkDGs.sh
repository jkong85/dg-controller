#!/bin/sh

echo "Stop and delete the Controller"
sudo kubectl get pods -o wide

sudo kubectl get svc

