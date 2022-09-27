#!/bin/bash

startTime=$(date)
echo $startTime

# add view for helping with missing indexes
${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/tablesWithoutIndexes.sql

# add zactvd_zdb_id_unique_constraint -- to solve ODC problem.
echo 'create unique index zactvd_zdb_id_pk_index on zdb_active_data(zactvd_zdb_id)' | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME}

# analyze db (aka:update statistics high)
echo 'vacuum (analyze);' | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME}

# set up slow query capture
echo 'CREATE EXTENSION pg_stat_statements;' | ${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME}

endTime=$(date)
echo $endTime
