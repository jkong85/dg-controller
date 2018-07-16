#!/bin/sh

echo "Stop and delete the Controller"
kubectl delete -f eureka.yaml
kubectl delete -f test.yaml
kubectl delete -f zuul.yaml
kubectl delete -f service.yaml


echo "Stop and delete Car1"
sudo kubectl delete deployments car1-0-eureka
sudo kubectl delete deployments car1-0-zuul
sudo kubectl delete deployments car1-0-test
sudo kubectl delete svc car1-0

sudo kubectl delete deployments car1-1-eureka
sudo kubectl delete deployments car1-1-zuul
sudo kubectl delete deployments car1-1-test
sudo kubectl delete svc car1-1




echo "Stop and delete Car2"
sudo kubectl delete deployments car2-0-eureka
sudo kubectl delete deployments car2-0-zuul
sudo kubectl delete deployments car2-0-test
sudo kubectl delete svc car2-0

sudo kubectl delete deployments car1-1-eureka
sudo kubectl delete deployments car1-1-zuul
sudo kubectl delete deployments car1-1-test
sudo kubectl delete svc car2-1


