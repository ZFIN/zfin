#!/bin/bash


dumpLocation=/research/zunloads/databases/postgres_dumps/${DBNAME}
echo "dumpLocation"
echo $dumpLocation

cd $dumpLocation

latestDump=`ls -td -- */ | head -n 1 | cut -d'/' -f1`
echo $latestDump

mkdir $dumpLocation/$latestDump

pg_basebackup -h localhost -D /research/zunloads/databases/postgres_base_backup/${DBNAME}/$latestDump --wal-method=stream --progress --verbose

