#!/bin/bash
set -e

service postgresql start
sleep 5

# postgresql database user
/usr/local/pgsql/bin/psql -U postgres -h localhost -d postgres -c "drop user if exists jooqdemouser"
/usr/local/pgsql/bin/psql -U postgres -h localhost -d postgres -c "create user jooqdemouser with createdb login password 'jooqdemouser'"

# run foreground
/usr/bin/supervisord -c /etc/supervisord.conf
