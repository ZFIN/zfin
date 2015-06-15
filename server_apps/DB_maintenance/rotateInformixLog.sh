#!/bin/tcsh 

export INFORMIXDIR=<!--|INFORMIX_DIR|-->
export INFORMIXSERVER=<!--|INFORMIX_SERVER|-->
export INFORMIX_PORT=<!--|INFORMIX_PORT|-->
export INFROMIXSQLHOSTS=<!--|INFORMIXSQLHOSTS|-->
export INSTANCE=<!--|INSTANCE|-->

echo $INFORMIXDIR
echo $INFORMIXSERVER
echo $INFORMIX_PORT
echo $INFORMIXSQLHOSTS
echo $INSTANCE


set processID=`$(ps aux | grep '[o]ntape' | awk '{print $2}')`;
echo $processID;
kill $processID;

cd <!--|SOURCEROOT|-->/server_apps/DB_maintenance/

gmake dumplogscontinuous
