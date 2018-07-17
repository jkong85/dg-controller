#!/bin/sh

echo "Stop and delete the Controller"
kubectl delete -f eureka.yaml
kubectl delete -f test.yaml
kubectl delete -f zuul.yaml
kubectl delete -f service.yaml

./rmDGs.sh car1 0
./rmDGs.sh car1 1
./rmDGs.sh car1 2
./rmDGs.sh car2 0
./rmDGs.sh car2 1

