#!/bin/sh
rm eureka_server_ip

kubectl delete -f eureka.yaml
sleep 5 

kubectl delete -f test.yaml
sleep 5 

kubectl delete -f zuul.yaml
sleep 5 

kubectl delete -f service.yaml
