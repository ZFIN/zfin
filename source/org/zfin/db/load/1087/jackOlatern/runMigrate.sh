#!/bin/bash

echo "Linc RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlantern/migrateLincRNAgenes.sql;

echo "mirna RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlantern/migrateMirnaRegions.sql

echo "ncrna RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlantern/migrateNcRNARegions.sql

echo "sno RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlantern/migrateSnoRegions.sql

echo "trna RNA";
$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlantern/migrateTrnaRegions.sql



