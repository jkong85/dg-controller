#!/bin/sh


for k in $*
do
    if  [ ! "$k" ] ;then
        echo "Compile end!"
        exit
    else
        echo "Stop and delete $1"
        sudo kubectl delete deployments $k-eureka
        sudo kubectl delete deployments $k-zuul
        sudo kubectl delete deployments $k-mongo
        sudo kubectl delete deployments $k-speed
        sudo kubectl delete deployments $k-oil
        sudo kubectl delete deployments $k-location
        sudo kubectl delete svc $k
    fi
done

