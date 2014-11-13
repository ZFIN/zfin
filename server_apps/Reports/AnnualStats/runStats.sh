#!/bin/bash -e

$INFORMIXDIR/bin/dbaccess -a $DBNAME $SOURCEROOT/server_apps/Reports/AnnualStats/stats.sql

