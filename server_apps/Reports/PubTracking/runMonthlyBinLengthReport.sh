#!/bin/bash -e

${PGBINDIR}/psql -v ON_ERROR_STOP=1 ${DBNAME} < <!--|ROOT_PATH|-->/server_apps/Reports/PubTracking/monthlyBinLengthReport.sql
