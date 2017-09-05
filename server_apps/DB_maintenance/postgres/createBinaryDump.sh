#!/bin/bash


dumpLocation=/research/zunloads/databases/postgres_dumps/${DBNAME}
echo "dumpLocation"
echo $dumpLocation

cd $dumpLocation

latestDump=`ls -td -- */ | head -n 1 | cut -d'/' -f1`
echo $latestDump

mkdir /research/zunloads/databases/postgres_self_dumps/${DBNAME}/$latestDump
# dump the fixed database
${PGBINDIR}/pg_dump -Fd ${DBNAME} -j 5 -f /research/zunloads/databases/postgres_self_dumps/${DBNAME}/$latestDump 

#latestBinaryDump=$latestDump.bak

#pg_dump -Fc ${DBNAME} >  /research/zunloads/databases/postgres_self_dumps/${DBNAME}/$latestDump/$latestDump.bak

