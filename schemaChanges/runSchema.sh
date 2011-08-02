#!/bin/sh
dbaccess -a $DBNAME /research/zunloads/projects/genePage/schema.sql

dbaccess -a $DBNAME schema.sql

echo "execute function regen_term(); " | dbaccess -a $DBNAME 

