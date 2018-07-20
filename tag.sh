#!/bin/sh
set -x

if [ ! "$1" ]; then
    sudo docker images
    echo "To tag an image, sudo ./tag.sh name tag_id"
    exit
fi
sudo docker tag $2 jkong85/dg-controller-$1:0.1
sudo docker tag $2 jkong85/dg-imo-$1:0.1

