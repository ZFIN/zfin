#!/bin/bash

echo "Linc RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlatern/migrateLincRNAgenes.sql;

echo "mirna RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlatern/migrateMirnaRegions.sql

echo "ncrna RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlatern/migrateNcRNARegions.sql

echo "sno RNA";

$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlatern/migrateSnoRegions.sql

echo "trna RNA";
$INFORMIXDIR/bin/dbaccess $DBNAME $SOURCEROOT/src/org/zfin/db/load/1087/jackOlatern/migrateTrnaRegions.sql



