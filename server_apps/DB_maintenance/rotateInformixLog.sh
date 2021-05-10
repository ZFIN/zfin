#!/bin/bash

#source /private/ZfinLinks/Commons/env/watson.env
echo "set process id"

cd <!--|TARGETROOT|-->/server_apps/DB_maintenance/

logToBeDeleted=`readlink -f <!--|TARGETROOT|-->/server_apps/DB_maintenance/logs`;

echo $logToBeDeleted;
processID=`pgrep 'ontape'`;
echo $processID;
kill $processID;

cd <!--|SOURCEROOT|-->/server_apps/DB_maintenance/

echo "dump logs continuous"
#<!--|TARGETROOT|-->/server_apps/DB_maintenance/dumpLogsContinuous.pl
#The systemd script calls dumpLogsContinuous.pl
sudo /bin/systemctl restart ontape

/bin/rm -rf $logToBeDeleted

echo "dump server"
<!--|TARGETROOT|-->/server_apps/DB_maintenance/dumpServer.pl
