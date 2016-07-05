#!/bin/tcsh -e

source /private/ZfinLinks/Commons/env/<!--|INSTANCE|-->.env

set processID=`ps aux | grep '[o]ntape' | awk '{print $2}'`;
echo $processID;
kill $processID;

cd <!--|SOURCEROOT|-->/server_apps/DB_maintenance/

gmake dumplogscontinuous

cd <!--|TARGET_ROOT|-->/server_apps/DB_maintenance/


<!--|TARGET_ROOT|-->/server_apps/DB_maintenance/dumpServer.pl
