#!/bin/sh

kubectl delete -f eureka.yaml

kubectl delete -f test.yaml

kubectl delete -f zuul.yaml

kubectl delete -f service.yaml

kubectl delete -f kubeclient.yaml 
