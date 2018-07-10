#!/bin/bash -e

${PGBINDIR}/psql -d ${DBNAME} -f zgcCount_PG.sql > zgcStatistics.txt 2> err.txt;
