#!/usr/bin/env bash
echo "Migrate the Data "
echo "Wait for the Mongo starting..."
sleep 10
#mongo admin --host localhost --eval "db.createUser({user: 'admin', pwd: 'admin', roles: [{role: 'userAdminAnyDatabase', db: 'admin'}]});"
mongo admin << EOF
use test
db.dropDatabase()
db.cloneDatabase("$1")
EOF
echo "DB is migrated successfully!"
