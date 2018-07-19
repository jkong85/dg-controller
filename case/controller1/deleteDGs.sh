#!/bin/sh

set -x

echo "Stop and delete DGs $1 by Controller"

curl -XDELETE http://172.18.101:30002/test/destroy?serviceName=$1&type=$2&node=$3

