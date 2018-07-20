#!/bin/sh

echo "Stop and delete $1"
sudo kubectl delete deployments $1-eureka
sudo kubectl delete deployments $1-zuul
sudo kubectl delete deployments $1-speed
sudo kubectl delete deployments $1-oil
sudo kubectl delete deployments $1-location
sudo kubectl delete svc $1

