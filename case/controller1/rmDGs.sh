#!/bin/sh

echo "Stop and delete $1"
sudo kubectl delete deployments $1-$2-eureka
sudo kubectl delete deployments $1-$2-zuul
sudo kubectl delete deployments $1-$2-test
sudo kubectl delete deployments $1-$2-speed
sudo kubectl delete deployments $1-$2-oil
sudo kubectl delete deployments $1-$2-location
sudo kubectl delete svc $1-$2

