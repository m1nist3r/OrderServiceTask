#!/bin/bash
set -e

mongo <<EOF
db = db.getSiblingDB('orderservice')

db.createUser({
  user: 'orders',
  pwd: '$ORDERS_PASSWORD',
  roles: [{ role: 'readWrite', db: 'orders' }],
});
EOF