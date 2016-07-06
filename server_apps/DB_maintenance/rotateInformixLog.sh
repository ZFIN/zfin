#!/bin/tcsh -e

source /private/ZfinLinks/Commons/env/<!--|INSTANCE|-->.env

set processID=`ps aux | grep '[o]ntape' | awk '{print $2}'`;
echo $processID;
kill $processID;

cd <!--|SOURCEROOT|-->/server_apps/DB_maintenance/

gmake dumplogscontinuous

cd <!--|TARGETROOT|-->/server_apps/DB_maintenance/


<!--|TARGETROOT|-->/server_apps/DB_maintenance/dumpServer.pl
