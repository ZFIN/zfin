#!/bin/bash

startTime=$(date)
echo $startTime

#echo "****Process an informix dump for loading into postgres."
#cd $SOURCEROOT/server_apps/DB_maintenance/postgres
#./process_dumps.sh

echo "****Build liquibase changelog"

cd $SOURCEROOT/server_apps/DB_maintenance/postgres/liquibase

rm -rf /tmp/changelogMigrationFile.xml
rm -rf $SOURCEROOT/source/org/zfin/db/postgres/changelogMigrationFile.xml


./liquibase --driver=com.informix.jdbc.IfxDriver --url="jdbc:informix-sqli://${HOSTNAME}:${INFORMIX_PORT}/${DBNAME}:INFORMIXSERVER=${INFORMIXSERVER};DB_LOCALE=en_US.utf8" --defaultSchemaName="informix" --classpath="/opt/zfin/source_roots/swirl/ZFIN_WWW/lib/Java/ifxjdbc-3.70.JC1.jar" --changeLogFile="/tmp/changelogMigrationFile.xml" generateChangeLog


cp /tmp/changelogMigrationFile.xml $SOURCEROOT/source/org/zfin/db/postgres/changelogMigrationFile.xml

# grep -A find the line, and then push the next, if available 100,000 lines to next command, 
# -C 1 means go to the matched pattern and 1 line before it. So find the first "<addPrimaryKey" reference,
# and then take the previous line and all the rest of the file off and put it in /tmp/constraints.xml This
# gets the first instance of the first key in the file and grabs its changeset definition.

grep -A100000 "<addPrimaryKey" /tmp/changelogMigrationFile.xml -C 1 > /tmp/constraints.xml

# xmlHeader is a copied version of the header required by liquibase to manage XML data definition 
# language.  

cat $SOURCEROOT/server_apps/DB_maintenance/postgres/xmlHeader.xml /tmp/constraints.xml > $SOURCEROOT/source/org/zfin/db/postgres/changelogConstraintFile.xml 

cat $SOURCEROOT/source/org/zfin/db/postgres/changelogMigrationFile.xml $SOURCEROOT/server_apps/DB_maintenance/postgres/xmlFooter.xml > cat $SOURCEROOT/source/org/zfin/db/postgres/tableMigration.xml





