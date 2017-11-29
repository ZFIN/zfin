#!/bin/bash -e

${PGBINDIR}/psql ${DBNAME} < <!--|ROOT_PATH|-->/server_apps/Reports/PubTracking/monthlyBinLengthReport_PG.sql
