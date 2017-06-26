#!/bin/bash

$INFORMIXDIR/bin/dbaccess $DBNAME migrateLincRNAgenes.sql;
$INFORMIXDIR/bin/dbaccess $DBNAME migrateMirnaRegions.sql
$INFORMIXDIR/bin/dbaccess $DBNAME migrateNcRNARegions.sql
$INFORMIXDIR/bin/dbaccess $DBNAME migrateSnoRegions.sql
$INFORMIXDIR/bin/dbaccess $DBNAME migrateTrnaRegions.sql



