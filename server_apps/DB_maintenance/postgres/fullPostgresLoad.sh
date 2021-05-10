#!/bin/bash

startTime=$(date)
echo $startTime

${SOURCEROOT}/server_apps/DB_maintenance/postgres/runPostgresBuild.sh
${SOURCEROOT}/server_apps/DB_maintenance/postgres/doClobWork.sh
${SOURCEROOT}/server_apps/DB_maintenance/postgres/fixIndexes.sh

cd ${SOURCEROOT}
gradle deployPostgres

${SOURCEROOT}/server_apps/DB_maintenance/postgres/createBinaryDump.sh

endTime=$(date)
echo $endTime
