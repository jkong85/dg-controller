#!/bin/sh

#set -x

echo " rm DGs"
./rmDGs.sh dg-core-honda-1 dg-edge1-honda-2 dg-edge2-honda-3

echo "stop controller"

sudo ./stop.sh

sudo kubectl delete svc honda1-core
sudo kubectl delete svc honda1-edge1


./checkDGs.sh

sudo docker images


