#!/usr/bin/env bash
echo "Start mongo..."
#mongod & 
echo "Creating mongo users..."
sleep 10
mongo admin --host localhost --eval "db.createUser({user: 'admin', pwd: 'admin', roles: [{role: 'userAdminAnyDatabase', db: 'admin'}]});"
mongo admin << EOF
use dg 
db.createUser({user: 'dg', pwd: 'dg', roles:[{role:'readWrite',db:'dg'}]})
EOF
echo "Mongo users created."
