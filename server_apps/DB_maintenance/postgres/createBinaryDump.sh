#!/bin/bash


set pth=research/zunloads/databases/postgres_dumps/${DBNAME}
set dirname=`date +"%Y.%m.%d.1"`

# increment until we get name which has not been taken                                               
while ( -d $pth/$dirname )
        set z=$dirname:e
        set y=$dirname:r
@ x = $z + 1
        set dirname=$y.$x
end

mkdir $pth/$dirname

dumpLocation=/research/zunloads/databases/postgres_dumps/${DBNAME}
echo "dumpLocation"
echo $dumpLocation

cd $dumpLocation

latestDump=`ls -td -- */ | head -n 1 | cut -d'/' -f1`
echo $latestDump

#mkdir /research/zunloads/databases/postgres_self_dumps/${DBNAME}/$latestDump
# dump the fixed database
${PGBINDIR}/pg_dump -Fc ${DBNAME} -f /research/zunloads/databases/postgres_self_dumps/${DBNAME}/$latestDump 

#latestBinaryDump=$latestDump.bak

#pg_dump -Fc ${DBNAME} >  /research/zunloads/databases/postgres_self_dumps/${DBNAME}/$latestDump/$latestDump.bak

