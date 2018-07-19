#!/bin/sh

#set -x

result="$(sudo docker images | awk '{print $1 $3}')"

cat all | awk '{print $3}' >> id

result="$(cat id)"

for line in $result
do
    sudo docker rmi -f ${line}
done


