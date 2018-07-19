#!/bin/sh

#set -x

#result="$(sudo docker images | grep "<none>" | awk '{print $0}')"
#result="$(sudo docker images | awk '{print $1 $3}' | grep "<none>" )"
result="$(sudo docker images | awk '{print $1 $3}')"

echo $result
exit

for line in $result 
do
    echo "line is: $line"
    NAME="$(echo $line | awk '{print $1}')"
    echo $NAME
    #if [ "$NAME" == "<none>" ]; then
    if [ "$NAME" == "dg-imo-speed" ]; then
        echo "yes"
    else
        echo "no"
    fi 
done


