#!/bin/sh
set -x
sudo docker tag $2 jkong85/dg-controller-$1:0.1
sudo docker tag $2 jkong85/dg-imo-$1:0.1

