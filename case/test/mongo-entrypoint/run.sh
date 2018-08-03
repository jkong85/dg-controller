#!/usr/bin/env bash
echo "Creating mongo users..."
mongo admin --host localhost -u root -p rootPass --eval "db.createUser({user: 'admin', pwd: 'zonePassWord', roles: [{role: 'userAdminAnyDatabase', db: 'admin'}]});"
mongo admin -u root -p rootPass << EOF
use zonedb
db.createUser({user: 'zone', pwd: 'zonePass', roles:[{role:'readWrite',db:'zonedb'}]})
EOF
echo "Mongo users created."
