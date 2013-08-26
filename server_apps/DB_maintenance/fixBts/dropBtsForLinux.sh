#!/bin/tcsh 

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/trunk/server_apps/DB_maintenance/fixBts/dropBtsIndexes.sql

echo "EXECUTE FUNCTION SYSBldPrepare('bts.3.00', 'drop');" | $INFORMIXDIR/bin/dbaccess $DBNAME
 
echo "EXECUTE FUNCTION SYSBldUnRegister('bts.3.00','sysblderrorlog');" | $INFORMIXDIR/bin/dbaccess $DBNAME
