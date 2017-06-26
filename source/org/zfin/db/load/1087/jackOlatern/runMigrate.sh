#!/bin/bash

!echo "Linc RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME migrateLincRNAgenes.sql;

!echo "mirna RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME migrateMirnaRegions.sql

!echo "ncrna RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME migrateNcRNARegions.sql

!echo "sno RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME migrateSnoRegions.sql

!echo "trna RNA";
$INFORMIXDIR/bin/dbaccess $DBNAME migrateTrnaRegions.sql



