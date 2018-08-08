#!/bin/sh

echo "Stop and delete the Controller"
kubectl delete -f eureka.yaml
#kubectl delete -f test.yaml
kubectl delete -f core.yaml
kubectl delete -f zuul.yaml
kubectl delete -f service.yaml

