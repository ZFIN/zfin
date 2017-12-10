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

echo "alter table publication add abstract_text text" | ${PGBINDIR}/psql ${DBNAME}
echo "alter table person add nonzf_pubs_text text" | ${PGBINDIR}/psql ${DBNAME}

echo "update publication set abstract_text = convert_from(pub_abstract, 'UTF-8')" | ${PGBINDIR}/psql ${DBNAME}
echo "update person set nonzf_pubs_text = convert_from(nonzf_pubs, 'UTF-8')" | ${PGBINDIR}/psql ${DBNAME}

echo "update publication set pub_abstract = null" | ${PGBINDIR}/psql ${DBNAME}
echo "update person set nonzf_pubs = null" | ${PGBINDIR}/psql ${DBNAME}

echo "alter table publication alter column pub_abstract type text using pub_abstract::text" | ${PGBINDIR}/psql ${DBNAME};
echo "alter table person alter column nonzf_pubs type text using nonzf_pubs::text" | ${PGBINDIR}/psql ${DBNAME};

echo "update publication set pub_abstract = abstract_text" | ${PGBINDIR}/psql ${DBNAME}
echo "update person set nonzf_pubs = nonzf_pubs_text" | ${PGBINDIR}/psql ${DBNAME}

echo "alter table publication drop abstract_text" | ${PGBINDIR}/psql ${DBNAME}
echo "alter table person drop nonzf_pubs_text" | ${PGBINDIR}/psql ${DBNAME}

endTime=$(date)
echo $startTime
