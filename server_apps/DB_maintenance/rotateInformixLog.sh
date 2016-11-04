#!/bin/bash

#source /private/ZfinLinks/Commons/env/watson.env
echo "set process id"

currentLog=`readlink -f logs`;
processID=`pgrep 'ontape'`;
echo $processID;
kill $processID;

cd <!--|SOURCEROOT|-->/server_apps/DB_maintenance/

echo "dump logs continuous"
gmake dumplogscontinuous

echo "dump server"
<!--|TARGETROOT|-->/server_apps/DB_maintenance/dumpServer.pl

rm -rf $currentLog
