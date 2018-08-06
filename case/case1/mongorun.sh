#!/usr/bin/env bash
echo "Migrate the Data "
echo "Wait for the Mongo starting..."

mongod &

/opt/http &
