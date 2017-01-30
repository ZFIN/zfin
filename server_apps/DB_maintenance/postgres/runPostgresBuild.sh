#!/bin/bash

startTime=$(date)
echo $startTime

echo "****Process an informix dump for loading into postgres."
cd $SOURCEROOT/server_apps/DB_maintenance/postgres
./process_dumps.sh

echo "****Build liquibase changelog"

cd $SOURCEROOT/server_apps/DB_maintenance/postgres/liquibase

./liquibase --driver=com.informix.jdbc.IfxDriver --url="jdbc:informix-sqli://${HOSTNAME}:${INFORMIX_PORT}/${DBNAME}:INFORMIXSERVER=${INFORMIXSERVER};DB_LOCALE=en_US.utf8" --defaultSchemaName="informix" --classpath="/opt/zfin/source_roots/swirl/ZFIN_WWW/lib/Java/ifxjdbc-3.70.JC1.jar" --changeLogFile="/tmp/file.xml" --changeLogFile="/tmp/changelogMigrationFile.xml" generateChangeLog

rm $SOURCEROOT/source/org/zfin/db/postgres/changelogMigrationFile.xml
cp /tmp/file.xml $SOURCEROOT/source/org/zfin/db/postgres/changelogMigrationFile.xml

grep -A100000 "<addPrimaryKey" /tmp/file.xml -C 1 > /tmp/constraints.xml

cat $SOURCEROOT/source/org/zfin/db/postgres/xmlHeader.txt /tmp/constraints.xml > $SOURCEROOT/source/org/zfin/db/postgres/changelogConstraintFile.xml



