#!/bin/bash

#source /private/ZfinLinks/Commons/env/watson.env
echo "set process id"

logToBeDeleted=`readlink -f <!--|TARGETROOT|-->/server_apps/DB_maintenance/logs`;
echo $logToBeDeleted;
processID=`pgrep 'ontape'`;
echo $processID;
kill $processID;

cd <!--|SOURCEROOT|-->/server_apps/DB_maintenance/

echo "dump logs continuous"
<!--|TARGETROOT|-->/server_apps/DB_maintenance/dumpLogsContinuous.pl

/bin/rm -rf <!--|TARGETROOT|-->/server_apps/DB_maintenance/$logToBeDeleted

echo "dump server"
<!--|TARGETROOT|-->/server_apps/DB_maintenance/dumpServer.pl
