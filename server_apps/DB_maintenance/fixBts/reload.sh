#!/bin/tcsh 

$INFORMIXDIR/bin/dbaccess -a $DBNAME dropBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess -a $DBNAME dropload.sql

$INFORMIXDIR/bin/dbaccess -a $DBNAME createBtsIndexes.sql

$INFORMIXDIR/bin/dbaccess -a $DBNAME fix_bts.sql

exit 0;
