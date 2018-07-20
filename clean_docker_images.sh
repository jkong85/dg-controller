#!/bin/sh
set -x
echo "please first save the images info to file "all""
cat $1 | awk '{print $3}' >> id

result="$(cat id)"

for line in $result
do
    sudo docker rmi -f ${line}
done

rm $1
rm id


