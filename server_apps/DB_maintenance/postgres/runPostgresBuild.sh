#!/bin/bash

startTime=$(date)
echo $startTime

mkdir -p /tmp/abstracts/
mkdir -p /tmp/nonzf_pubs/

echo "drop tables in informixd that are not migrating to postgres"

${INFORMIXDIR}/bin/dbaccess ${DBNAME} ${SOURCEROOT}/server_apps/DB_maintenance/postgres/dropTables.sql
${INFORMIXDIR}/bin/dbaccess ${DBNAME} ${SOURCEROOT}/server_apps/DB_maintenance/postgres/dumpNonKeyIndexes.sql

sed 's/|/;/g' ${SOURCEROOT}/server_apps/DB_maintenance/postgres/nonKeyIndexes.sql > /tmp/fix.sql && mv /tmp/fix.sql ${SOURCEROOT}/server_apps/DB_maintenance/postgres/nonKeyIndexes.sql

echo "drop and recreate database in $DBNAME value"

if ${PGBINDIR}/psql -lqt | cut -d \| -f 1 | grep -qw ${DBNAME}; then
    ${PGBINDIR}/dropdb ${DBNAME}
fi

${PGBINDIR}/createdb ${DBNAME}

echo "****Build liquibase changelog"

cd ${SOURCEROOT}/server_apps/DB_maintenance/postgres/liquibase
rm -rf /tmp/changelogConstraintFile.xml 
rm -rf /tmp/tableMigration.xml
rm -rf /tmp/tables.xml
rm -rf /tmp/changelogMigrationFile.xml
rm -rf ${SOURCEROOT}/source/org/zfin/db/postgres/changelogMigrationFile.xml
rm -rf ${SOURCEROOT}/source/org/zfin/db/postgres/constraints/changelogConstraintFile.xml
rm -rf ${SOURCEROOT}/source/org/zfin/db/postgres/tableMigration.xml
rm -rf /tmp/liquibaseOutput.txt

./liquibase --driver=com.informix.jdbc.IfxDriver --url="jdbc:informix-sqli://${HOSTNAME}:${INFORMIX_PORT}/${DBNAME}:INFORMIXSERVER=${INFORMIXSERVER};DB_LOCALE=en_US.utf8" --defaultSchemaName="informix" --classpath="$SOURCEROOT/lib/Java/informix/ifxjdbc-3.70.JC1.jar" --changeLogFile="/tmp/changelogMigrationFile.xml" generateChangeLog > /tmp/liquibaseOutput.txt

cp /tmp/changelogMigrationFile.xml ${SOURCEROOT}/source/org/zfin/db/postgres/changelogMigrationFile.xml

# grep -A find the line, and then push the next, if available 100,000 lines to next command, 
# -C 1 means go to the matched pattern and 1 line before it. So find the first "<addPrimaryKey" reference,
# and then take the previous line and all the rest of the file off and put it in /tmp/constraints.xml This
# gets the first instance of the first key in the file and grabs its changeset definition.

grep -A100000 "<addPrimaryKey" /tmp/changelogMigrationFile.xml -C 1  > /tmp/changelogConstraintFile.xml 

cat ${SOURCEROOT}/server_apps/DB_maintenance/postgres/xmlHeader.xml /tmp/changelogConstraintFile.xml > ${SOURCEROOT}/source/org/zfin/db/postgres/constraints/changelogConstraintFile.xml 

echo "process changelog files to help lvarchar and dates into postgres syntax"

# strip the top part of the liquibase file off to a file so we can just load the tables first without the constraints.
sed '/<addPrimaryKey/q' /tmp/changelogMigrationFile.xml | head -n -2 > /tmp/tables.xml

# change lvarchar() to text, reorganize timestamps and defaults to postgres syntax
sed 's/LVARCHAR([0-9]*)/text/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml
sed 's/DATETIME YEAR TO SECOND NOT NULL/CURRENT_TIMESTAMP/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml
sed 's/VARCHAR([0-9]*)/text/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml
sed 's/current year to second/CURRENT_TIMESTAMP/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml
sed 's/defaultValueComputed="current year to day"/defaultValueComputed="CURRENT_TIMESTAMP"/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml
sed 's/BOOLEAN(1)/BOOLEAN/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml
sed 's/="f"/="false"/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml
sed 's/="t"/="true"/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml
sed 's/SMALLFLOAT([0-9]*)/numeric/g' /tmp/tables.xml > /tmp/tables.xml.tmp && mv /tmp/tables.xml.tmp /tmp/tables.xml

cat /tmp/tables.xml ${SOURCEROOT}/server_apps/DB_maintenance/postgres/xmlFooter.xml > $SOURCEROOT/source/org/zfin/db/postgres/tableMigration.xml

cd ${SOURCEROOT}
ant buildPostgresDatabase

${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/add_monthly_average_curated_metric.sql

dumpLocation=/research/zunloads/databases/postgres_dumps/${DBNAME}
echo "dumpLocation"
echo $dumpLocation

cd $dumpLocation

latestDump=`ls -td -- */ | head -n 1 | cut -d'/' -f1`
echo $latestDump

cd ${SOURCEROOT}/server_apps/DB_maintenance/postgres/
./loaddatabase.py -d $dumpLocation/$latestDump

cd ${SOURCEROOT}
ant addPostgresConstraints

# generate serial id replacement sql file : reset.sql
${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/resetSerialIds.sql > ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql

#remove header and summary from reset.sql so that we can run it directly and reset the sequences.

sed 's/?column?//g' ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql > ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql.tmp && mv ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql.tmp ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql

sed 's/-//g' ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql > ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql.tmp && mv ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql.tmp ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql

sed 's/(142 rows)//g' ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql > ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql.tmp && mv ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql.tmp ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql

# reset the sequence start values for all serial ids
${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/reset.sql
${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/nonKeyIndexes.sql

# unload the clobs and store in /tmp/abstracts and /tmp/nonzf_pubs
${INFORMIXDIR}/bin/dbaccess ${DBNAME} ${SOURCEROOT}/server_apps/DB_maintenance/postgres/unloadAbstract.sql

# update the schema definition in postgres to use 'bytea' as per the postgres doc suggestions
${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/changeSmartLargeObjects.sql

# create the update statements that we need to load up the clobs.
${SOURCEROOT}/server_apps/DB_maintenance/postgres/createClobLoadStatements.sh

${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/lib/DB_functions/postgresFunctions/bytea_import.sql

# load up the clobs into postgres
${PGBINDIR}/psql ${DBNAME} < ${SOURCEROOT}/server_apps/DB_maintenance/postgres/clobLoad.sql

