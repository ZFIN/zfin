#!/bin/bash

startTime=$(date)
echo $startTime

rm -rf /tmp/abstracts/*

# unload the clobs and store in /tmp/abstracts and /tmp/nonzf_pubs
${INFORMIXDIR}/bin/dbaccess ${DBNAME} ${SOURCEROOT}/server_apps/DB_maintenance/postgres/unloadAbstract.sql

# update the schema definition in postgres to use 'bytea' as per the postgres doc suggestions
${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/changeSmartLargeObjects.sql

# create the update statements that we need to load up the clobs.
${SOURCEROOT}/server_apps/DB_maintenance/postgres/createClobLoadStatements.sh

${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/lib/DB_functions/postgresFunctions/bytea_import.sql

# load up the clobs into postgres
${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/clobLoad.sql

endTime=$(date)
echo $startTime
