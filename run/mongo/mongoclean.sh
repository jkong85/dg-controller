#!/usr/bin/env bash
echo "Clean MongoDB db: test"
mongo admin << EOF
use test
db.dropDatabase()
EOF
echo "DB is migrated successfully!"
