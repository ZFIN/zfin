#! /bin/tcsh 

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/dropBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/dropload.sql

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/createBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/createTempBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME /research/zcentral/www_homes/$INSTANCE/server_apps/DB_maintenance/fix_bts.sql

exit 0;