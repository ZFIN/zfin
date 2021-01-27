#!/bin/bash -e

${PGBINDIR}/psql -d ${DBNAME} -f zgcCount.sql > zgcStatistics.txt 2> err.txt;
