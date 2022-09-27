#!/bin/bash -e

${PGBINDIR}/psql -v ON_ERROR_STOP=1 -d ${DBNAME} -f zgcCount.sql > zgcStatistics.txt 2> err.txt;
