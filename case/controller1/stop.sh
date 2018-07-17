#!/bin/sh

echo "Stop and delete the Controller"
kubectl delete -f eureka.yaml
kubectl delete -f test.yaml
kubectl delete -f zuul.yaml
kubectl delete -f service.yaml

./rmDgs.sh car1 0
./rmDgs.sh car1 1
./rmDgs.sh car1 2
./rmDgs.sh car2 0
./rmDgs.sh car2 1

