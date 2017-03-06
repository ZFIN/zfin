#!/bin/bash

/opt/postgres/postgresql/bin/initdb -D /opt/postgres/data -E SQL_ASCII
/opt/postgres/postgresql/bin/pg_ctl start -D /opt/postgres/data -l /opt/postgres/postgresql/database_logfile

/opt/postgres/postgresql/bin/psql -d template1 -a -f $SOURCEROOT/server_apps/DB_maintenance/postgres/addUser.sql
