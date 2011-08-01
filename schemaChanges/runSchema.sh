#!/bin/sh
dbaccess -a $DBNAME /research/zunloads/projects/genePage/schema.sql

dbaccess -a $DBNAME schema.sql

