#!/bin/sh
set -x
echo "please first save the images info to file "all""
cat all | awk '{print $3}' >> id

result="$(cat id)"

for line in $result
do
    sudo docker rmi -f ${line}
done


