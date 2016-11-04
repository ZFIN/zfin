#!/bin/bash

#source /private/ZfinLinks/Commons/env/watson.env
echo "set process id"

logToBeDeleted=`readlink -f logs`;
processID=`pgrep 'ontape'`;
echo $processID;
kill $processID;

cd <!--|SOURCEROOT|-->/server_apps/DB_maintenance/

echo "dump logs continuous"
<!--|TARGETROOT|-->/server_apps/DB_maintenance/dumpLogsContinuous.pl

rm -rf $logToBeDeleted

echo "dump server"
<!--|TARGETROOT|-->/server_apps/DB_maintenance/dumpServer.pl
