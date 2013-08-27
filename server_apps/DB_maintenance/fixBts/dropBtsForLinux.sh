#!/bin/tcsh 

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/trunk/server_apps/DB_maintenance/fixBts/dropBtsIndexes.sql

EXECUTE FUNCTION SYSBldPrepare('ifxmngr', 'sysblderrorlog'); 
EXECUTE FUNCTION SYSBldPrepare('bts.3.00', 'sysblderrorlog'); 
EXECUTE FUNCTION SYSBldUnRegister('bts.3.00','sysblderrorlog');
