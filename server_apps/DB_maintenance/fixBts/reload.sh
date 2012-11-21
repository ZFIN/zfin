#!/bin/tcsh 

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/fixBts/dropBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/fixBts/dropload.sql

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/fixBts/createBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/fixBts/createTempBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/fixBts/fix_bts.sql

exit 0;
