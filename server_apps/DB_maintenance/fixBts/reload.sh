#!/bin/tcsh 

$INFORMIXDIR/bin/dbaccess $DBNAME dropBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME dropload.sql

$INFORMIXDIR/bin/dbaccess $DBNAME createBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME createTempBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess $DBNAME fix_bts.sql

exit 0;
