#!/bin/sh
set -x

if [ ! "$1" ]; then
    sudo docker images
    exit
fi
sudo docker tag $2 jkong85/dg-controller-$1:0.1
sudo docker tag $2 jkong85/dg-imo-$1:0.1

