#!/usr/bin/env bash
echo "Migrate the Data "
echo "Wait for the Mongo starting..."

mongod --httpinterface &

/opt/http &

while true;do echo hello;sleep 10000;done
