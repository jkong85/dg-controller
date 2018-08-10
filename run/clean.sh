#!/bin/sh

#set -x

clear

echo " Remove the DGs left if existed"
./rmDGs.sh dg-core-honda-1 dg-edge1-honda-2 dg-edge2-honda-3

sudo kubectl delete svc honda1-core
sudo kubectl delete svc honda1-edge1
sudo kubectl delete svc honda1-edge2


echo "<<<<<Stop all controller Components>>>>>"
sudo ./stop.sh


./checkDGs.sh

sudo docker images


