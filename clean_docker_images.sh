#!/bin/sh
set -x

if [ ! "$1" ]; then
    echo "please specify the images name"
    exit
fi

sudo docker images | grep $1 | awk '{print $3}' | xargs sudo docker rmi -f

exit 

echo "please first save the images info to file "all""
cat $1 | awk '{print $3}' >> id

result="$(cat id)"

for line in $result
do
    sudo docker rmi -f ${line}
done

rm $1
rm id

sudo docker images


